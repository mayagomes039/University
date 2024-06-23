package View;

import Model.GestServicoFacade;
import Data.ServicoDAO;
import Model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.LocalDate;

import static View.View.cabecalho;

public class TextUI {
    public static Scanner scanner = new Scanner(System.in);


    //Sucesso
    public static void sucesso(Boolean sucesso) {
        if (sucesso){
            System.out.println("Sucesso!");
        }else {
            System.out.println("Erro! Tente novamente:");
        }
    }

    //logIn e logOut ------------------------------------------------------------------------------------------------

    public static List<String> menulogIn(Boolean erro) {
        System.out.println("Bem-vindo ao Sistema de Gestão de Serviços da Oficina de Gualtar!");
        String idUtilizador;
        String palavra_passe;

        if (erro != null) sucesso(false);

        System.out.println("Coloca o seu id/NIF de utilizador:");
        idUtilizador = getStr();
        System.out.println("Digite a sua palavra-passe: ");
        palavra_passe = getStr();

        List<String> credentials = new ArrayList<>();
        credentials.add(idUtilizador);
        credentials.add(palavra_passe);
        return credentials;

    }
    public static String menulogInPosto(Boolean erro) {
        System.out.println("A sua hora de chegada ao turno foi registada com sucesso!");

        String posto;
        if (!erro) sucesso(false);

        System.out.println("Digite o id do seu Posto de trabalho:");
        posto = getStr();

        return posto;
    }

    public static void logOut() {
        System.out.println("A sua hora de fim de turno foi registada com sucesso!");
        System.out.println("Até breve...");
    }

    //Menu do funcionário --------------------------------------------------------------------------------------------

    public static int menuPrincipalFuncionario() {
        View.header("Menu Principal do Funcionário");
        List<String> opcoes = Arrays.asList(
                "1 --- Visualizar agenda de serviços",
                "2 --- Concluir um serviço",
                "3 --- Entrar na ficha de um cliente",
                "4 --- Consultar histórico dos serviços",
                "5 --- LogOut");
        cabecalho(opcoes);
        return veOpcao(1,5);
    }

    // Visualização de tarefas ---------------------------------------------------------------------------------------

    public static void disponibilizaTarefas(Map<String, Servico> tarefas) {
        if (tarefas.isEmpty()) {
            System.out.println("Não tem serviços por realizar.");
        }
        else {
            System.out.println("Os seus serviços pendentes são:");
            for (Map.Entry<String, Servico> entry : tarefas.entrySet())
                System.out.println(entry.getKey() + ": " + entry.getValue().getNome() + " - " + entry.getValue().getData_comeco().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
    }

    // Conclusão de um serviço ---------------------------------------------------------------------------------------
    public static int menuConcluirServicoNome(Map<String, Servico> tarefas) {
        int count=0;
        String idS;
        int opcao;
        if (tarefas.isEmpty()) {
            System.out.println("Não tem serviços por realizar.");
            return 0;
        }
        System.out.println("Introduza o nº do serviço que pretende concluir:");
        for(Map.Entry<String, Servico> entry : tarefas.entrySet()){
            Servico serv = entry.getValue();
            if (LocalDate.now().atStartOfDay().equals(serv.getData_comeco().toLocalDate().atStartOfDay())) {
                count++;
                System.out.println(count + " --- " + entry.getKey() + ": " + entry.getValue().getNome() + " - " + entry.getValue().getData_comeco().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
        }
        opcao = veOpcao(1,count);
        return opcao;
    }

    public static LocalDateTime menuConcluirServicoData() {
        LocalDateTime data_terminio;
        System.out.println("Introduza a data e hora de conclusão do seu serviço (yyyy-MM-dd HH:mm:ss):");
        String strDataHora = getStr();
        data_terminio = stringToLocalDateTime(strDataHora);
        return data_terminio;
    }

    // logIn e logOut do cliente ----------------------------------------------------------------------------------------------
    public static String menulogInCliente() {
        String nifCliente;
        System.out.println("Coloca o id/NIF do cliente:");
        nifCliente = getStr();
        return nifCliente;
    }


    // Menu principal do cliente ----------------------------------------------------------------------------------------------

    public static int menuPrincipalCliente(String email) {
        System.out.println("Bem-vindo à ficha do cliente: " + email);
        View.header("Menu Principal do Cliente");
        List<String> opcoes = Arrays.asList(
                "1 --- Consultar dados de um veículo",
                "2 --- Pedir um serviço",
                "3 --- Histórico de serviços",
                "4 --- LogOut");
        cabecalho(opcoes);
        int opcao = veOpcao(1,4);
        return opcao;
    }



    // Dados do cliente -----------------------------------------------------------------------------------------------
    public static String acedeFicha() {
        String matricula;
        System.out.println("Insere a matricula do veiculo cujo pretende visualizar os dados:");
        matricula = getStr();
        return matricula;
    }
    public static void disponibilizaFicha(Cliente cliente, String matricula) {
        System.out.println("Bem vindo a ficha dos dados do veiculo: " + matricula);

        System.out.println("Nome do proprietário: " + cliente.getNome());
        System.out.println("Email do proprietário: " + cliente.getEmail());
        System.out.println("Número de telemóvel do proprietário: " + cliente.getTelefone());
        System.out.println("Morada do proprietário: " + cliente.getMorada());

        System.out.println("Estação frequentada: " + cliente.getEstacaoFrequentada());
    }

    //Pedidos de serviços ---------------------------------------------------------------------------------------------
    public static String menuPedidoServico() {
        String matricula;
        System.out.println("Insira a matricula do veiculo no qual pretende um serviço:");
        matricula = getStr();
        return matricula;
    }


    public static void imprimeServicosUniversais(){
        System.out.println("Escolhe o serviço que pretende pedir:");
        System.out.println("Serviços universais disponíveis:");
        System.out.println(
                "1 --- Substituição dos pneus\n" +
                "2 --- Calibragem das rodas\n" +
                "3 --- Alinhamento da direção\n" +
                "4 --- Substituição dos injetores\n" +
                "5 --- Substituição dos calços dos travões\n" +
                "6 --- Mudança do óleo dos travões\n" +
                "7 --- Limpeza do interior e/ou exterior\n" +
                "8 --- Substituição do filtro de ar da cabina\n"
        );
    }

    public static int servicosCombustaoGasoleo(){
        System.out.println("Serviços disponíveis para motores de combustão a gasóleo: ");
        System.out.println(
                "9 --- Mudança de óleo do motor\n" +
                "10 --- Substituição dos filtros de óleo, combustível e ar do motor\n" +
                "11 --- Substituição do conversor catalítico\n" +
                "12 --- Substituição da bateria de arranque\n" +
                "13 --- Substituição das velas de incandescência\n" +
                "14 --- Regeneração ou substituição do filtro de partículas\n"
        );
        int opcao = veOpcao(1,14);
        return opcao;
    }

    public static int servicosCombustaoGasolina(){
        System.out.println("Serviços disponíveis para motores de combustão a gasolina: ");
        System.out.println(
                "9 --- Mudança de óleo do motor\n" +
                "10 --- Substituição dos filtros de óleo, combustível e ar do motor\n" +
                "11 --- Substituição do conversor catalítico\n" +
                "12 --- Substituição da bateria de arranque\n" +
                "13 --- Substituição da válvula do acelerador (borboleta)\n" +
                "14 --- Substituição das velas de ignição\n"
        );
        int opcao = veOpcao(1,14);
        return opcao;
    }

    public static void servicosCombustaoGasolinaHibrido(){
        System.out.println("Serviços disponíveis para motores de combustão a gasolina: ");
        System.out.println(
                "9 --- Mudança de óleo do motor\n" +
                        "10 --- Substituição dos filtros de óleo, combustível e ar do motor\n" +
                        "11 --- Substituição do conversor catalítico\n" +
                        "12 --- Substituição da bateria de arranque\n" +
                        "13 --- Substituição da válvula do acelerador (borboleta)\n" +
                        "14 --- Substituição das velas de ignição\n"
        );
    }

    public static void servicosCombustaoGasoleoHibrido(){
        System.out.println("Serviços disponíveis para motores de combustão a gasoleo: ");
        System.out.println(
                "9 --- Mudança de óleo do motor\n" +
                        "10 --- Substituição dos filtros de óleo, combustível e ar do motor\n" +
                        "11 --- Substituição do conversor catalítico\n" +
                        "12 --- Substituição da bateria de arranque\n" +
                        "13 --- Substituição das velas de incandescência\n" +
                        "14 --- Regeneração ou substituição do filtro de partículas\n"
        );
    }

    public static int servicosMotorEletrico(){
        System.out.println("Serviços disponíveis para motores elétricos: ");
        System.out.println(
                "9 --- Avaliação do desempenho da bateria\n" +
                "10 --- Substituição da bateria\n"
        );
        int opcao = veOpcao(1,10);
        return opcao;
    }

    public static int servicosMotorEletricoHibrido(){
        System.out.println("Serviços disponíveis para motores elétricos: ");
        System.out.println(
                "15 --- Avaliação do desempenho da bateria\n" +
                "16 --- Substituição da bateria\n"
        );
        int opcao = veOpcao(1,16);
        return opcao;
    }

    public static int tipoDeHibrido(){
        System.out.println("Digite 0 se o seu veículo possui combustão a gasolina e 1 se possui combustão a gasóleo:");
        int numero = veOpcao(0, 1);
        return numero;
    }




    //Agendamento --------------------------------------------------------------------------------------------------------------

    public static int menuConfirmaAgendamento(String servico, Cliente cliente, List<LocalDateTime> horariosNegados, LocalDateTime horarioSugerido){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedDateTime = horarioSugerido.format(formatter);
        System.out.println("O serviço pode ser agendado para: " + formattedDateTime);
        System.out.println("Pretende confirmar o agendamento do serviço: " + servico +"? Digite 1 se sim e 0 se não:");
        int opcao = veOpcao(0,1);
        return opcao;
    }

    // Históricos de serviços -----------------------------------------------------------------------------------------------
    public static void historicoCliente(Map<String, Servico> historico_servicos) {
        if (historico_servicos.isEmpty()) {
            System.out.println("Nenhum serviço no histórico.");
            return;
        }

        System.out.println("Histórico dos seus serviços:");

        for (Servico servico : historico_servicos.values()) {
            if (servico != null) {
                System.out.println("ID: " + servico.getIdS());
                System.out.println("Nome: " + servico.getNome());
                System.out.println("Matricula do veículo: " + servico.getMatricula());
                System.out.println("Mecânico: " + servico.getId_mecanico());
                System.out.println("Posto: " + servico.getId_posto());
                System.out.println("Data de começo: " + servico.getData_comeco());
                System.out.println("Data de término: " + servico.getData_terminio());

                Duration duracaoPrevista = servico.getDuracao_prevista();
                long horas = duracaoPrevista.toHours();
                long minutos = duracaoPrevista.toMinutesPart();
                System.out.println("Duração prevista: " + horas + " horas e " + minutos + " minutos");

                System.out.println("----------------------------");
            }
        }
    }

    public static void historicoFuncionario(Map<String, Servico> historico_servicos) {

        if (historico_servicos.isEmpty()) {
            System.out.println("Nenhum serviço no histórico.");
            return;
        }

        System.out.println("Histórico dos seus serviços:");

        for (Servico servico : historico_servicos.values()) {
            if (servico != null) {
                System.out.println("ID: " + servico.getIdS());
                System.out.println("Nome: " + servico.getNome());
                System.out.println("Matricula do veículo: " + servico.getMatricula());
                System.out.println("Cliente: " + servico.getId_cliente());
                System.out.println("Data de começo: " + servico.getData_comeco());
                System.out.println("Data de término: " + servico.getData_terminio());

                Duration duracaoPrevista = servico.getDuracao_prevista();
                long horas = duracaoPrevista.toHours();
                long minutos = duracaoPrevista.toMinutesPart();
                System.out.println("Duração prevista: " + horas + " horas e " + minutos + " minutos");

                System.out.println("----------------------------");
            }
        }
    }
    // informa cliente  ---------------------------------------------------------------------------------------------

    public static void informaCliente(String mensagem){
        System.out.println(mensagem);
    }

    // metodos auxiliares ---------------------------------------------------------------------------------------------
    public static String getStr() {
        try {
            String str = scanner.nextLine();
            return str;
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public static int veOpcao(int a, int b) {
        int numero = getNumero();
        while (numero < a || numero > b) {
            System.out.println("Entrada inválida. Digite novamente.");
            numero = getNumero();
        }
        return numero;
    }

    public static int getNumero() {
        int numero;
        try {
            numero = scanner.nextInt();
            scanner.nextLine();
        } catch (InputMismatchException e) {
            scanner.nextLine();
            return -1;
        }
        return numero;
    }

    public static LocalDateTime stringToLocalDateTime(String str) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try {
            return LocalDateTime.parse(str, formatter);
        } catch (Exception e) {
            // Tratar caso a entrada do usuário não esteja no formato esperado
            System.out.println("Erro ao processar a entrada. Certifique-se de inserir no formato correto.");
        }
        return null;
    }

    public static void inexistente(){
        System.out.println("Não existe nenhum horário disponível.");
    }


}
