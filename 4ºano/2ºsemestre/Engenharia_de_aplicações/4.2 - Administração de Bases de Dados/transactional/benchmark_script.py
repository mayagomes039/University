#!/usr/bin/env python3
import subprocess
import re
import psycopg2
import time
import os
import logging
from typing import Dict, List, Tuple, Any
from dotenv import load_dotenv

# Carrega as variáveis de ambiente do arquivo .env
load_dotenv(dotenv_path='../.env')

# Configuração de logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler("postgres_tuning.log"),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

# Configuração da conexão PostgreSQL usando variáveis de ambiente
DB_CONFIG = {
    "host": os.getenv("DB_HOST", "localhost"),
    "port": int(os.getenv("DB_PORT", 5432)),
    "database": os.getenv("DB_NAME", "adb"),
    "user": os.getenv("DB_USER", "postgres"),
    "password": os.getenv("DB_PASSWORD", "1234")
}

# Comando para executar benchmark
BENCHMARK_CMD = [
    "java", "-jar", os.getenv("BENCHMARK_CMD_JAR_PATH", "target/transactional-1.0-SNAPSHOT.jar"),
    "-d", f"jdbc:postgresql://{DB_CONFIG['host']}:{DB_CONFIG['port']}/{DB_CONFIG['database']}",
    "-U", DB_CONFIG["user"], 
    "-P", DB_CONFIG["password"],
    "-W", "15",     # Período de warmup em segundos
    "-R", "180",    # Duração do teste em segundos
    "-c", "16"      # Número de clientes concorrentes
]

# Parâmetros a serem testados, em ordem
PARAMETERS_TO_TEST = [
    {
        "name": "shared_buffers",
        "values": ["128MB", "256MB", "512MB", "1GB", "2GB", "4GB"],
        "requires_restart": True
    },
    {
        "name": "work_mem",
        "values": ["4MB", "8MB", "16MB", "32MB", "64MB"],
        "requires_restart": False
    },
    {
        "name": "effective_cache_size",
        "values": ["1GB", "2GB", "4GB", "8GB"],
        "requires_restart": False
    },
    {
        "name": "maintenance_work_mem",
        "values": ["64MB", "128MB", "256MB", "512MB"],
        "requires_restart": False
    },
    {
        "name": "random_page_cost",
        "values": ["4.0", "3.0", "2.0", "1.5", "1.0"],
        "requires_restart": False
    },
    # Parâmetros de paralelismo
    {
        "name": "max_worker_processes",
        "values": ["4", "8", "16", "32"],
        "requires_restart": True
    },
    {
        "name": "max_parallel_workers",
        "values": ["4", "8", "16", "32"],
        "requires_restart": True
    },
    {
        "name": "max_parallel_workers_per_gather",
        "values": ["0", "2", "4", "8"],
        "requires_restart": False
    },
    {
        "name": "parallel_tuple_cost",
        "values": ["0.1", "0.2", "0.5", "1.0"],
        "requires_restart": False
    },
    {
        "name": "parallel_setup_cost",
        "values": ["1000", "1500", "2000"],
        "requires_restart": False
    },
    {
        "name": "min_parallel_table_scan_size",
        "values": ["4MB", "8MB", "16MB"],
        "requires_restart": False
    },
    {
        "name": "min_parallel_index_scan_size",
        "values": ["256kB", "512kB", "1MB", "2MB"],
        "requires_restart": False
    }
]

def execute_command(cmd: List[str]) -> str:
    """Executa um comando e retorna a saída."""
    try:
        logger.info(f"Executando: {' '.join(cmd)}")
        result = subprocess.run(cmd, capture_output=True, text=True, check=True)
        return result.stdout
    except subprocess.CalledProcessError as e:
        logger.error(f"Erro ao executar comando: {e}")
        logger.error(f"Saída de erro: {e.stderr}")
        raise

def parse_benchmark_output(output: str) -> float:
    """Extrai o throughput do resultado do benchmark."""
    match = re.search(r'throughput \(txn/s\) = ([\d.]+)', output)
    if match:
        return float(match.group(1))
    else:
        logger.error("Não foi possível encontrar o throughput no resultado")
        logger.debug(f"Saída completa: {output}")
        return 0.0

def get_postgresql_config_file() -> str:
    """Obtém o caminho do arquivo postgresql.conf."""
    try:
        with psycopg2.connect(**DB_CONFIG) as conn:
            with conn.cursor() as cur:
                cur.execute("SHOW config_file;")
                result = cur.fetchone()
                return result[0] if result else None
    except Exception as e:
        logger.error(f"Erro ao obter arquivo de configuração: {e}")
        raise

def update_postgresql_parameter(param_name, param_value):
    try:
        # Conectar ao banco de dados PostgreSQL
        conn = psycopg2.connect("dbname=adb user=postgres password=1234 host=localhost port=5432")
        cur = conn.cursor()

        # Desabilitar transação automática para garantir que o comando ALTER SYSTEM não esteja em uma transação
        conn.set_session(autocommit=True)

        # Alterar o parâmetro
        cur.execute(f"ALTER SYSTEM SET {param_name} = '{param_value}';")
        print(f"Parâmetro {param_name} atualizado para {param_value}.")

        # Fechar o cursor e a conexão
        cur.close()
        conn.close()

    except Exception as e:
        print(f"Erro ao atualizar parâmetro {param_name}: {e}")

def restart_postgresql() -> None:
    """Reinicia o serviço PostgreSQL."""
    try:
        # Tenta primeiro com systemctl (Linux com systemd)
        logger.info("Reiniciando PostgreSQL")
        os.system("sudo systemctl restart postgresql")
        # Aguarda um tempo para o PostgreSQL voltar online
        time.sleep(10)
        
        # Verifica se está online
        test_connection()
        
    except Exception as e:
        logger.error(f"Erro ao reiniciar PostgreSQL: {e}")
        logger.info("Por favor, reinicie manualmente o PostgreSQL e pressione Enter para continuar...")
        input()

def test_connection() -> bool:
    """Testa a conexão com o PostgreSQL."""
    max_attempts = 5
    attempt = 0
    
    while attempt < max_attempts:
        try:
            with psycopg2.connect(**DB_CONFIG) as conn:
                with conn.cursor() as cur:
                    cur.execute("SELECT 1")
                    return True
        except Exception as e:
            attempt += 1
            logger.warning(f"Tentativa {attempt} falhou: {e}")
            time.sleep(5)
    
    raise Exception("Não foi possível conectar ao PostgreSQL após várias tentativas")

def run_benchmark() -> Tuple[float, Dict[str, Any]]:
    """Executa o benchmark e retorna o throughput e métricas detalhadas."""
    # Registra configuração atual dos parâmetros mais relevantes
    current_config = {}
    try:
        with psycopg2.connect(**DB_CONFIG) as conn:
            with conn.cursor() as cur:
                for param in [p["name"] for p in PARAMETERS_TO_TEST]:
                    cur.execute(f"SHOW {param};")
                    current_config[param] = cur.fetchone()[0]
    except Exception as e:
        logger.warning(f"Não foi possível obter configuração atual: {e}")
    
    # Executa o benchmark
    start_time = time.time()
    output = execute_command(BENCHMARK_CMD)
    execution_time = time.time() - start_time
    throughput = parse_benchmark_output(output)
    
    # Extrai outras métricas potencialmente úteis
    response_time_match = re.search(r'response time \(ms\) = ([\d.]+)', output)
    abort_rate_match = re.search(r'abort rate \(%\) = ([\d.]+)', output)
    
    # Extrai métricas de funções individuais
    function_metrics = {}
    function_section = re.search(r'Response time per function \(ms\)(.*?)Overall metrics', output, re.DOTALL)
    if function_section:
        functions_text = function_section.group(1)
        for line in functions_text.strip().split('\n'):
            if '=' in line:
                func_name, time_ms = line.split('=')
                function_metrics[func_name.strip()] = float(time_ms.strip())
    
    metrics = {
        "timestamp": time.strftime("%Y-%m-%d %H:%M:%S"),
        "throughput": throughput,
        "response_time": float(response_time_match.group(1)) if response_time_match else None,
        "abort_rate": float(abort_rate_match.group(1)) if abort_rate_match else None,
        "execution_time": execution_time,
        "current_config": current_config,
        "function_metrics": function_metrics,
        "raw_output": output
    }
    
    logger.info(f"Benchmark concluído - Throughput: {throughput} txn/s")
    
    # Salva esta execução em um arquivo de histórico
    save_benchmark_run(metrics)
    
    return throughput, metrics

def save_benchmark_run(metrics: Dict[str, Any]) -> None:
    """Salva os resultados de uma execução do benchmark em um arquivo de histórico."""
    os.makedirs("resultados_testes/historico", exist_ok=True)
    
    # Gera um nome de arquivo único com timestamp
    timestamp = time.strftime("%Y%m%d_%H%M%S")
    
    # Adiciona informação sobre configuração ao nome do arquivo
    config_summary = ""
    if metrics["current_config"]:
        # Pega o primeiro parâmetro configurado para identificar o teste
        for param in PARAMETERS_TO_TEST:
            if param["name"] in metrics["current_config"]:
                config_summary = f"{param['name']}_{metrics['current_config'][param['name']]}"
                break
    
    filename = f"resultados_testes/historico/benchmark_{timestamp}_{config_summary}.txt"
    
    with open(filename, "w") as f:
        f.write(f"Execução de Benchmark - {metrics['timestamp']}\n")
        f.write("="*50 + "\n\n")
        
        f.write("Métricas gerais:\n")
        f.write(f"- Throughput: {metrics['throughput']} txn/s\n")
        f.write(f"- Tempo de resposta: {metrics['response_time']} ms\n")
        f.write(f"- Taxa de aborto: {metrics['abort_rate']} %\n")
        f.write(f"- Tempo de execução: {metrics['execution_time']:.2f} segundos\n\n")
        
        f.write("Configuração atual:\n")
        for param, value in metrics["current_config"].items():
            f.write(f"- {param}: {value}\n")
        
        f.write("\nTempo de resposta por função (ms):\n")
        for func, time_ms in metrics["function_metrics"].items():
            f.write(f"- {func}: {time_ms} ms\n")
        
        f.write("\nSaída completa:\n")
        f.write("-"*50 + "\n")
        f.write(metrics["raw_output"])

def optimize_parameters() -> Dict[str, str]:
    """Função principal que testa e otimiza cada parâmetro."""
    optimized_params = {}
    
    logger.info("Iniciando processo de otimização de parâmetros PostgreSQL")
    
    # Testa cada parâmetro
    for param in PARAMETERS_TO_TEST:
        param_name = param["name"]
        values = param["values"]
        requires_restart = param["requires_restart"]
        
        logger.info(f"\n{'='*50}")
        logger.info(f"Testando parâmetro: {param_name}")
        logger.info(f"{'='*50}")
        
        best_value = None
        best_throughput = 0.0
        results = {}
        
        for value in values:
            logger.info(f"\nTestando {param_name} = {value}")
            update_postgresql_parameter(param_name, value)
            
            if requires_restart:
                restart_postgresql()
            
            # Executamos o benchmark
            throughput, metrics = run_benchmark()
            results[value] = metrics
            
            if throughput > best_throughput:
                best_throughput = throughput
                best_value = value
        
        # Definimos o melhor valor encontrado
        logger.info(f"\nMelhor valor para {param_name}: {best_value} (Throughput: {best_throughput} txn/s)")
        update_postgresql_parameter(param_name, best_value)
        
        if requires_restart:
            restart_postgresql()
        
        optimized_params[param_name] = best_value
        
        # Salvamos os resultados detalhados deste parâmetro
        save_parameter_results(param_name, results, best_value)
    
    return optimized_params

def save_parameter_results(param_name: str, results: Dict[str, Dict], best_value: str) -> None:
    """Salva os resultados de um parâmetro em um arquivo."""
    # Cria diretório para resultados se não existir
    os.makedirs("resultados_testes", exist_ok=True)
    
    with open(f"resultados_testes/tuning_results_{param_name}.txt", "w") as f:
        f.write(f"Resultados para {param_name}\n")
        f.write("="*50 + "\n\n")
        
        # Tabela de resultados
        f.write(f"{'Valor':<10} | {'Throughput (txn/s)':<20} | {'Tempo de resposta (ms)':<25} | {'Taxa de aborto (%)':<20}\n")
        f.write("-"*80 + "\n")
        
        for value, metrics in results.items():
            mark = " *" if value == best_value else ""
            f.write(f"{value + mark:<10} | {metrics['throughput']:<20.3f} | {metrics['response_time']:<25.3f} | {metrics['abort_rate']:<20.3f}\n")
        
        f.write("\n\n")
        f.write(f"Melhor valor: {best_value} (Throughput: {results[best_value]['throughput']} txn/s)\n")
        
        # Saída completa para cada valor testado
        f.write("\nDetalhes de cada teste:\n")
        
        for value, metrics in results.items():
            f.write("\n" + "-"*50 + "\n")
            f.write(f"Teste com {param_name} = {value}\n")
            f.write("-"*50 + "\n")
            f.write(metrics['raw_output'])
    
    # Também salvamos um arquivo com apenas os resultados do melhor valor
    with open(f"resultados_testes/best_{param_name}_{best_value}.txt", "w") as f:
        f.write(f"Melhor valor para {param_name} = {best_value}\n")
        f.write("="*50 + "\n\n")
        f.write(f"Throughput: {results[best_value]['throughput']} txn/s\n")
        f.write(f"Tempo de resposta: {results[best_value]['response_time']} ms\n")
        f.write(f"Taxa de aborto: {results[best_value]['abort_rate']} %\n\n")
        f.write("Saída completa:\n")
        f.write("-"*50 + "\n")
        f.write(results[best_value]['raw_output'])

def save_final_results(optimized_params: Dict[str, str]) -> None:
    """Salva os resultados finais da otimização."""
    # Cria diretório para resultados se não existir
    os.makedirs("resultados_testes", exist_ok=True)
    
    # Salva em formato legível
    with open("resultados_testes/postgres_optimized_config.txt", "w") as f:
        f.write("Parâmetros otimizados do PostgreSQL\n")
        f.write("="*50 + "\n\n")
        
        for param, value in optimized_params.items():
            f.write(f"{param} = {value}\n")
    
    # Também salva em formato adequado para postgresql.conf
    with open("resultados_testes/postgresql_optimized.conf", "w") as f:
        f.write("# Configuração otimizada do PostgreSQL\n")
        f.write("# Gerado automaticamente pelo script de otimização\n")
        f.write(f"# Data: {time.strftime('%Y-%m-%d %H:%M:%S')}\n\n")
        
        for param, value in optimized_params.items():
            f.write(f"{param} = {value}\n")

def get_current_parameters() -> Dict[str, str]:
    """Obtém os valores atuais dos parâmetros do PostgreSQL."""
    current_params = {}
    try:
        with psycopg2.connect(**DB_CONFIG) as conn:
            with conn.cursor() as cur:
                for param in [p["name"] for p in PARAMETERS_TO_TEST]:
                    cur.execute(f"SHOW {param};")
                    current_params[param] = cur.fetchone()[0]
    except Exception as e:
        logger.error(f"Erro ao obter parâmetros atuais: {e}")
    
    return current_params

def create_report(optimized_params: Dict[str, str], initial_params: Dict[str, str],
                 initial_metrics: Dict[str, Any], final_metrics: Dict[str, Any]) -> None:
    """Cria um relatório final com todos os resultados."""
    os.makedirs("resultados_testes", exist_ok=True)
    
    with open("resultados_testes/relatorio_final.md", "w") as f:
        f.write("# Relatório de Otimização do PostgreSQL\n\n")
        f.write(f"Data: {time.strftime('%Y-%m-%d %H:%M:%S')}\n\n")
        
        f.write("## Resumo\n\n")
        
        # Tabela comparativa de throughput
        f.write("### Comparação de Performance\n\n")
        f.write("| Métrica | Antes da Otimização | Após a Otimização | Melhoria |\n")
        f.write("|---------|---------------------|-------------------|----------|\n")
        
        initial_throughput = initial_metrics.get("throughput", 0)
        final_throughput = final_metrics.get("throughput", 0)
        
        if initial_throughput > 0:
            improvement = ((final_throughput - initial_throughput) / initial_throughput) * 100
            f.write(f"| Throughput (txn/s) | {initial_throughput:.2f} | {final_throughput:.2f} | {improvement:.2f}% |\n")
        else:
            f.write(f"| Throughput (txn/s) | N/A | {final_throughput:.2f} | N/A |\n")
        
        f.write("\n")
        
        # Parâmetros otimizados
        f.write("## Parâmetros Otimizados\n\n")
        f.write("| Parâmetro | Valor Original | Valor Otimizado |\n")
        f.write("|-----------|----------------|----------------|\n")
        
        for param, value in optimized_params.items():
            original = initial_params.get(param, "N/A")
            f.write(f"| {param} | {original} | {value} |\n")
        
        f.write("\n")
        
        # Mais detalhes sobre a performance final
        f.write("## Detalhes de Performance Final\n\n")
        f.write(f"- **Throughput**: {final_metrics.get('throughput', 'N/A')} txn/s\n")
        f.write(f"- **Tempo de Resposta**: {final_metrics.get('response_time', 'N/A')} ms\n")
        f.write(f"- **Taxa de Aborto**: {final_metrics.get('abort_rate', 'N/A')}%\n\n")
        
        # Tempos de resposta por função
        if 'function_metrics' in final_metrics and final_metrics['function_metrics']:
            f.write("### Tempo de Resposta por Função (ms)\n\n")
            f.write("| Função | Tempo (ms) |\n")
            f.write("|--------|------------|\n")
            
            # Ordena por tempo de resposta (do maior para o menor)
            sorted_funcs = sorted(final_metrics['function_metrics'].items(), 
                                 key=lambda x: x[1], reverse=True)
            
            for func, time_ms in sorted_funcs:
                f.write(f"| {func} | {time_ms} |\n")
        
        f.write("\n")
        
        # Instruções para aplicar configuração
        f.write("## Como Aplicar esta Configuração\n\n")
        f.write("Para aplicar esta configuração otimizada, você pode:\n\n")
        f.write("1. Copiar os parâmetros do arquivo `postgresql_optimized.conf` para o seu arquivo `postgresql.conf`\n")
        f.write("2. Reiniciar o PostgreSQL para aplicar as alterações\n\n")
        
        f.write("Comando para reiniciar (Linux com systemd):\n")
        f.write("```bash\nsudo systemctl restart postgresql\n```\n\n")

def main():
    """Função principal."""
    try:
        logger.info("Iniciando script de otimização de parâmetros PostgreSQL")
        
        # Cria diretórios para resultados
        os.makedirs("resultados_testes", exist_ok=True)
        os.makedirs("resultados_testes/historico", exist_ok=True)
        
        # Verifica conexão com o banco
        test_connection()
        
        # Obtém parâmetros atuais
        initial_params = get_current_parameters()
        
        # Executa um benchmark com a configuração atual
        logger.info("Executando benchmark com a configuração atual...")
        initial_throughput, initial_metrics = run_benchmark()
        
        logger.info(f"Configuração inicial - Throughput: {initial_throughput} txn/s")
        logger.info(f"Salvando detalhes na pasta 'resultados_testes/historico'")
        
        # Executa a otimização
        optimized_params = optimize_parameters()
        
        # Salva resultados finais
        save_final_results(optimized_params)
        
        logger.info("\nOtimização concluída!")
        logger.info("Os parâmetros otimizados foram salvos em 'resultados_testes/postgres_optimized_config.txt'")
        
        # Executa um benchmark final com todos os parâmetros otimizados
        logger.info("\nExecutando benchmark final com todos os parâmetros otimizados...")
        final_throughput, final_metrics = run_benchmark()
        
        # Melhoria percentual
        if initial_throughput > 0:
            improvement = ((final_throughput - initial_throughput) / initial_throughput) * 100
            logger.info(f"\nMelhoria de performance: {improvement:.2f}%")
        
        logger.info(f"\nResultado final - Throughput: {final_throughput} txn/s")
        logger.info(f"Tempo de resposta: {final_metrics['response_time']} ms")
        logger.info(f"Taxa de aborto: {final_metrics['abort_rate']} %")
        
        # Cria relatório final
        create_report(optimized_params, initial_params, initial_metrics, final_metrics)
        logger.info("\nRelatório final gerado em 'resultados_testes/relatorio_final.md'")
        
    except Exception as e:
        logger.error(f"Erro durante a execução: {e}")
        import traceback
        logger.error(traceback.format_exc())

if __name__ == "__main__":
    main()