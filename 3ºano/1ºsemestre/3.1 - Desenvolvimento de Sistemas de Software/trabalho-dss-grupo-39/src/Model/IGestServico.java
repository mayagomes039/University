package Model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;


public interface IGestServico {
    public boolean fazLogin(String idUtilizador, String passe);

    public boolean fazVerificacaoCompetencias(Funcionario mecanico, Posto posto);

    public boolean fazLogOut();

    public void fazConclusaoServico(Servico servico, LocalDateTime data);

    public boolean fazLogINCliente(String nif);

    public Map<String, Servico> dispoTarefas(Funcionario func);

    public boolean verificaMatricula(String matricula);


    public Servico servicoCombustaoGasolinaPedido(String idCliente, String matricula, String nomeServico);

    public Servico servicoUniversalPedido(String idCliente, String matricula, String nomeServico);

    public Servico servicoCombustaoGasoleoPedido(String idCliente, String matricula, String nomeServico);

    public Servico servicoEletricoPedido(String idCliente, String matricula, String nomeServico);

    public void fazConfirmaAgendamento(Servico servico, Cliente cliente, List<LocalDateTime> horariosNegados, LocalDateTime horarioSugerido);

    // Metodos auxiliares (getters e setters) --------------------------------------------------------------------------
    public Map<String, Funcionario> getFuncionarios();

    public String getFuncionarioAtual();

    public String getClienteAtual();

    public Map<String, Posto> getPostos();

    public Map<String, Servico> getServicos();

    public Map<String, Veiculo> getVeiculos();

    public Map<String, Cliente> getClientes();

    public Scanner getScanner();
    public void fazLogOutCliente();

    public LocalDateTime getHorarioSugerido(String idPosto, Duration duracaoPrevista, String idCliente, List<LocalDateTime> horariosNegados);
}
