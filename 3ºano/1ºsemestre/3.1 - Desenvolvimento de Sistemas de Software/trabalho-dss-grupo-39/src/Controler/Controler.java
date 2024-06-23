package Controler;

import Model.*;
import View.TextUI;
import java.time.LocalDateTime;
import java.util.*;

import static View.TextUI.disponibilizaFicha;

public class Controler {
    private final GestServicoFacade model = new GestServicoFacade();
    private Scanner scanner = new Scanner(System.in);


    public void menuCorre() {
        controlerlogIn();

        TextUI textUI = new TextUI();
        while (true) {
            int opcao = TextUI.menuPrincipalFuncionario();
            switch (opcao) {
                case 1 -> TextUI.disponibilizaTarefas(model.dispoTarefas(model.getFuncionarios().get(model.getFuncionarioAtual())));
                case 2 -> {
                    menuConcluirServico();
                }
                case 3 -> {
                    String nif;
                    while (true) {
                        nif = TextUI.menulogInCliente();
                        if (!model.fazLogINCliente(nif)) {
                            TextUI.sucesso(false);
                        } else {
                            break;
                        }
                    }
                    Cliente cliente = model.getClientes().get(nif);
                    int opcaoCliente = 0;
                    while (opcaoCliente != 4) {
                        opcaoCliente = TextUI.menuPrincipalCliente(cliente.getEmail());
                        switch (opcaoCliente) {
                            case 1 -> {
                                String matricula;
                                while (true) {
                                    matricula = TextUI.acedeFicha();
                                    if (!model.verificaMatricula(matricula)) {
                                        TextUI.sucesso(false);
                                    } else {
                                        break;
                                    }
                                }
                                Cliente cli = model.getClientes().get(model.getClienteAtual());
                                disponibilizaFicha(cli, matricula);
                            }
                            case 2 -> {
                                String matricula;
                                while (true) {
                                    matricula = TextUI.menuPedidoServico();
                                    if (!model.verificaMatricula(matricula)) {
                                        TextUI.sucesso(false);
                                    } else {
                                        break;
                                    }
                                }
                                String clienteAtual = model.getClienteAtual();
                                Veiculo veiculo = model.getVeiculos().get(matricula);
                                if (veiculo instanceof VeiculoGasoleo) {
                                    pedirServicoGasoleo(clienteAtual, matricula);
                                } else if (veiculo instanceof VeiculoGasolina) {
                                    pedirServicoGasolina(clienteAtual, matricula);
                                } else if (veiculo instanceof VeiculoEletrico) {
                                    pedirServicoEletrico(clienteAtual, matricula);
                                } else if (veiculo instanceof VeiculoHibrido) {
                                    int numero = TextUI.tipoDeHibrido();
                                    if (numero == 0) pedirServicoHibridoGasolina(clienteAtual, matricula);
                                    else if (numero == 1) pedirServicoHibridoGasoleo(clienteAtual, matricula);
                                }

                            }
                            case 3 -> {
                                String nifCliente = model.getClienteAtual();
                                Cliente cliente1 = model.getClientes().get(nifCliente);
                                List<String> historico_servicos = cliente1.getHistorico_servicos();
                                Map<String, Servico> map = new HashMap<>();
                                for (String servicoId : historico_servicos) {
                                    Servico servico = model.getServicos().get(servicoId);
                                    map.put(servicoId, servico);
                                }
                                TextUI.historicoCliente(map);
                            }
                            case 4 -> {
                                model.fazLogOutCliente();
                            }
                        }
                    }

                }
                case 4 -> {
                    String id = model.getFuncionarioAtual();
                    Funcionario func = model.getFuncionarios().get(id);
                    List<String> historico_servicos = func.getHistorico_servicos();
                    Map<String, Servico> map = new HashMap<>();
                    for (String servicoId : historico_servicos) {
                        Servico servico = model.getServicos().get(servicoId);
                        map.put(servicoId, servico);
                    }
                    TextUI.historicoFuncionario(map);

                }
                case 5 -> {
                    model.fazLogOut();
                    TextUI.logOut();
                    System.exit(0);
                }
            }
        }
    }

    public void controlerlogIn() {
        List<String> credentials;
        Boolean erro = null;
        do {
            credentials = TextUI.menulogIn(erro);
        } while (!(erro = model.fazLogin(credentials.get(0), credentials.get(1))));

        TextUI.sucesso(true);

        String posto;
        do {
            posto = TextUI.menulogInPosto(erro);
        } while (!(erro =model.fazVerificacaoCompetencias(model.getFuncionarios().get(credentials.get(0)),model.getPostos().get(posto))) );

        TextUI.sucesso(true);
    }

    private void menuConcluirServico() {
        Map<String, Servico> tarefas = model.dispoTarefas(model.getFuncionarios().get(model.getFuncionarioAtual()));
        int num = 0;

        num = TextUI.menuConcluirServicoNome(tarefas);
        if (num == 0) {
            TextUI.sucesso(false);
            return;
        }

        num--;
        String idS = (String) tarefas.keySet().toArray()[num];
        LocalDateTime data = TextUI.menuConcluirServicoData();
        model.fazConclusaoServico(model.getServicos().get(idS), data);
        TextUI.sucesso(true);
    }

    public void pedirServicoUniversal(int numero, String idCliente, String matricula) {
        String nomeServico = null;
        if (numero == 1) nomeServico = "Substituição dos pneus";
        else if (numero == 2) nomeServico = "Calibragem das rodas";
        else if (numero == 3) nomeServico = "Alinhamento da direção";
        else if (numero == 4) nomeServico = "Substituição dos injetores";
        else if (numero == 5) nomeServico = "Substituição dos calços dos travões";
        else if (numero == 6) nomeServico = "Mudança do óleo dos travões";
        else if (numero == 7) nomeServico = "Limpeza do interior e/ou exterior";
        else if (numero == 8) nomeServico = "Substituição do filtro de ar da cabina";

        Servico servico = model.servicoUniversalPedido(idCliente,matricula,nomeServico);
        List<LocalDateTime> horariosNegados = new ArrayList<>();
        while (true){
            LocalDateTime horarioSugerido = model.getHorarioSugerido(servico.getId_posto(), servico.getDuracao_prevista(), idCliente, horariosNegados);
            if (horarioSugerido == null) {
                TextUI.inexistente();
                return;
            }
            if (TextUI.menuConfirmaAgendamento(servico.getNome(), model.getClientes().get(idCliente), horariosNegados, horarioSugerido) == 0){
                horariosNegados.add(horarioSugerido);
            }else { //if(numero == 1){
                this.model.fazConfirmaAgendamento(servico,model.getClientes().get(idCliente),horariosNegados,horarioSugerido);
                TextUI.sucesso(true);
                break;
            }
        }

    }


    public void pedirServicoGasolina(String idCliente, String matricula){
        String nomeServico = null;
        TextUI.imprimeServicosUniversais();
        int numero = TextUI.servicosCombustaoGasolina();
        if (numero <= 8){
            pedirServicoUniversal(numero, idCliente, matricula);
            return;
        }

        if (numero == 9) nomeServico = "Mudança de óleo do motor";
        else if (numero == 10) nomeServico = "Substituição dos filtros de óleo, combustível e ar do motor";
        else if (numero == 11) nomeServico = "Substituição do conversor catalítico";
        else if (numero == 12) nomeServico = "Substituição da bateria de arranque";
        else if (numero == 13) nomeServico = "Substituição da válvula do acelerador (borboleta)";
        else if (numero == 14) nomeServico = "Substituição das velas de ignição";

        Servico servico = model.servicoCombustaoGasolinaPedido(idCliente,matricula,nomeServico);
        List<LocalDateTime> horariosNegados = new ArrayList<>();
        while (true){
            LocalDateTime horarioSugerido = model.getHorarioSugerido(servico.getId_posto(), servico.getDuracao_prevista(), idCliente, horariosNegados);
            if (horarioSugerido == null) {
                TextUI.inexistente();
                return;
            }
            if (TextUI.menuConfirmaAgendamento(servico.getNome(), model.getClientes().get(idCliente), horariosNegados, horarioSugerido) == 0){
                horariosNegados.add(horarioSugerido);
            }else { //if(numero == 1){
                this.model.fazConfirmaAgendamento(servico,model.getClientes().get(idCliente),horariosNegados,horarioSugerido);
                TextUI.sucesso(true);
                break;
            }
        }
    }

    private void pedirServicoGasoleo (String idCliente, String matricula){
        String nomeServico = null;
        TextUI.imprimeServicosUniversais();
        int numero = TextUI.servicosCombustaoGasoleo();
        if (numero <= 8){
            pedirServicoUniversal(numero, idCliente, matricula);
            return;
        }

        if (numero == 9) nomeServico = "Mudança de óleo do motor";
        else if (numero == 10) nomeServico = "Substituição dos filtros de óleo, combustível e ar do motor";
        else if (numero == 11) nomeServico = "Substituição do conversor catalítico";
        else if (numero == 12) nomeServico = "Substituição da bateria de arranque";
        else if (numero == 13) nomeServico = "Substituição das velas de incandescência";
        else if (numero == 14) nomeServico = "Regeneração ou substituição do filtro de partículas";

        Servico servico = model.servicoCombustaoGasoleoPedido(idCliente,matricula,nomeServico);
        List<LocalDateTime> horariosNegados = new ArrayList<>();
        while (true){
            LocalDateTime horarioSugerido = model.getHorarioSugerido(servico.getId_posto(), servico.getDuracao_prevista(), idCliente, horariosNegados);
            if (horarioSugerido == null) {
                TextUI.inexistente();
                return;
            }
            if (TextUI.menuConfirmaAgendamento(servico.getNome(), model.getClientes().get(idCliente), horariosNegados, horarioSugerido) == 0){
                horariosNegados.add(horarioSugerido);
            }else { //if(numero == 1){
                this.model.fazConfirmaAgendamento(servico,model.getClientes().get(idCliente),horariosNegados,horarioSugerido);
                TextUI.sucesso(true);
                break;
            }
        }
    }

    public void pedirServicoEletrico(String idCliente, String matricula) {
        String nomeServico = null;
        TextUI.imprimeServicosUniversais();
        int numero = TextUI.servicosMotorEletrico();
        if (numero <= 8){
            pedirServicoUniversal(numero, idCliente, matricula);
            return;
        }

        if (numero == 9) nomeServico = "Avaliação do desempenho da bateria";
        else if (numero == 10) nomeServico = "Substituição da bateria";

        Servico servico = model.servicoEletricoPedido(idCliente,matricula,nomeServico);
        List<LocalDateTime> horariosNegados = new ArrayList<>();
        while (true){
            LocalDateTime horarioSugerido = model.getHorarioSugerido(servico.getId_posto(), servico.getDuracao_prevista(), idCliente, horariosNegados);
            if (horarioSugerido == null) {
                TextUI.inexistente();
                return;
            }
            if (TextUI.menuConfirmaAgendamento(servico.getNome(), model.getClientes().get(idCliente), horariosNegados, horarioSugerido) == 0){
                horariosNegados.add(horarioSugerido);
            }else { //if(numero == 1){
                this.model.fazConfirmaAgendamento(servico,model.getClientes().get(idCliente),horariosNegados,horarioSugerido);
                TextUI.sucesso(true);
                break;
            }
        }
    }


    private void pedirServicoHibridoGasolina(String idCliente, String matricula) {
        String nomeServico = null;

        TextUI.imprimeServicosUniversais();
        TextUI.servicosCombustaoGasolinaHibrido();
        int numero = TextUI.servicosMotorEletricoHibrido();

        if (numero <= 8){
            pedirServicoUniversal(numero, idCliente, matricula);
            return;
        }
        if (numero > 14) {
            pedirServicoEletricoHibrido(idCliente, matricula, numero);
            return;
        }
        if (numero == 9) nomeServico = "Mudança de óleo do motor";
        else if (numero == 10) nomeServico = "Substituição dos filtros de óleo, combustível e ar do motor";
        else if (numero == 11) nomeServico = "Substituição do conversor catalítico";
        else if (numero == 12) nomeServico = "Substituição da bateria de arranque";
        else if (numero == 13) nomeServico = "Substituição da válvula do acelerador (borboleta)";
        else if (numero == 14) nomeServico = "Substituição das velas de ignição";

        Servico servico = model.servicoCombustaoGasolinaHibridoPedido(idCliente,matricula,nomeServico);
        List<LocalDateTime> horariosNegados = new ArrayList<>();
        while (true){
            LocalDateTime horarioSugerido = model.getHorarioSugerido(servico.getId_posto(), servico.getDuracao_prevista(), idCliente, horariosNegados);
            if (horarioSugerido == null) {
                TextUI.inexistente();
                return;
            }
            if (TextUI.menuConfirmaAgendamento(servico.getNome(), model.getClientes().get(idCliente), horariosNegados, horarioSugerido) == 0){
                horariosNegados.add(horarioSugerido);
            }else { //if(numero == 1){
                this.model.fazConfirmaAgendamento(servico,model.getClientes().get(idCliente),horariosNegados,horarioSugerido);
                TextUI.sucesso(true);
                break;
            }
        }
    }

    public void pedirServicoEletricoHibrido(String idCliente, String matricula, int numero) {
        String nomeServico = null;

        if (numero == 15) nomeServico = "Avaliação do desempenho da bateria";
        else if (numero == 16) nomeServico = "Substituição da bateria";

        Servico servico = model.servicoEletricoHibridoPedido(idCliente,matricula,nomeServico);
        List<LocalDateTime> horariosNegados = new ArrayList<>();
        while (true){
            LocalDateTime horarioSugerido = model.getHorarioSugerido(servico.getId_posto(), servico.getDuracao_prevista(), idCliente, horariosNegados);
            if (horarioSugerido == null) {
                TextUI.inexistente();
                return;
            }
            if (TextUI.menuConfirmaAgendamento(servico.getNome(), model.getClientes().get(idCliente), horariosNegados, horarioSugerido) == 0){
                horariosNegados.add(horarioSugerido);
            }else { //if(numero == 1){
                this.model.fazConfirmaAgendamento(servico,model.getClientes().get(idCliente),horariosNegados,horarioSugerido);
                TextUI.sucesso(true);
                break;
            }
        }
    }

    private void pedirServicoHibridoGasoleo(String idCliente, String matricula) {
        String nomeServico = null;

        TextUI.imprimeServicosUniversais();
        TextUI.servicosCombustaoGasoleoHibrido();
        int numero = TextUI.servicosMotorEletricoHibrido();

        if (numero <= 8){
            pedirServicoUniversal(numero, idCliente, matricula);
            return;
        }
        if (numero > 14) {
            pedirServicoEletricoHibrido(idCliente, matricula, numero);
            return;
        }
        if (numero == 9) nomeServico = "Mudança de óleo do motor";
        else if (numero == 10) nomeServico = "Substituição dos filtros de óleo, combustível e ar do motor";
        else if (numero == 11) nomeServico = "Substituição do conversor catalítico";
        else if (numero == 12) nomeServico = "Substituição da bateria de arranque";
        else if (numero == 13) nomeServico = "Substituição das velas de incandescência";
        else if (numero == 14) nomeServico = "Regeneração ou substituição do filtro de partículas";

        Servico servico = model.servicoCombustaoGasoleoHibridoPedido(idCliente,matricula,nomeServico);
        List<LocalDateTime> horariosNegados = new ArrayList<>();
        while (true){
            LocalDateTime horarioSugerido = model.getHorarioSugerido(servico.getId_posto(), servico.getDuracao_prevista(), idCliente, horariosNegados);
            if (horarioSugerido == null) {
                TextUI.inexistente();
                return;
            }
            if (TextUI.menuConfirmaAgendamento(servico.getNome(), model.getClientes().get(idCliente), horariosNegados, horarioSugerido) == 0){
                horariosNegados.add(horarioSugerido);
            }else { //if(numero == 1){
                this.model.fazConfirmaAgendamento(servico,model.getClientes().get(idCliente),horariosNegados,horarioSugerido);
                TextUI.sucesso(true);
                break;
            }
        }
    }


}
