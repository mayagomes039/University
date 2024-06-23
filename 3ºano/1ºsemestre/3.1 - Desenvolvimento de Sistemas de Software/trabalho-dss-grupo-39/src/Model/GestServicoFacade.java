package Model;


import View.TextUI;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class GestServicoFacade implements IGestServico {

    private final ISubServico servicoFacade = new SubServicoFacade();
    private final ISubUtilizador utilizadorFacade = new SubUtilizadorFacade();
    private Scanner scanner = new Scanner(System.in);


    public boolean fazLogin (String idUtilizador, String passe){
        return utilizadorFacade.logIn(idUtilizador, passe);
    }

    public boolean fazVerificacaoCompetencias(Funcionario mecanico, Posto posto){
        return utilizadorFacade.verificaCompetencias(mecanico,posto);
    }

    public boolean fazLogOut(){
        return utilizadorFacade.logOut();
    }

    public void fazLogOutCliente(){
        utilizadorFacade.logOutCliente();
    }

    public void fazConclusaoServico(Servico servico, LocalDateTime data){
        servicoFacade.concluirServico(servico, data);
        // addicionar aos historicos
        String idCliente = servico.getId_cliente();
        Cliente cliente = utilizadorFacade.getClientes().get(idCliente);
        Funcionario funcionario = utilizadorFacade.getFuncionarios().get(servico.getId_mecanico());
        funcionario.getHistorico_servicos().add(servico.getIdS());
        cliente.getHistorico_servicos().add(servico.getIdS());
        TextUI.informaCliente(servicoFacade.notificaCliente(cliente));
    }

    public boolean fazLogINCliente(String nif) {
        return utilizadorFacade.logInFichaCliente(nif);
    }

    public Map<String, Servico> dispoTarefas(Funcionario func){
        return servicoFacade.getAgendaTarefa(func);
    }


    public boolean verificaMatricula(String matricula){
        Cliente cliente = utilizadorFacade.getClientes().get(utilizadorFacade.getClienteAtual());
        if (cliente.getVeiculos_cliente().contains(matricula)) return true;
        return false;
    }



    public Servico servicoCombustaoGasolinaPedido(String idCliente, String matricula, String nomeServico) {
        Servico servico = null;

        for (Map.Entry<String, Posto> entry : servicoFacade.getPostos().entrySet()) {
            Posto posto = entry.getValue();
            if (Objects.equals(posto.getNome(), "Combustao_Gasolina")) {
                servico = servicoFacade.criaServico(2, posto, idCliente, matricula, nomeServico,1);
            }
        }
        return servico;
    }

    public Servico servicoCombustaoGasolinaHibridoPedido(String idCliente, String matricula, String nomeServico) {
        Servico servico = null;

        for (Map.Entry<String, Posto> entry : servicoFacade.getPostos().entrySet()) {
            Posto posto = entry.getValue();
            if (Objects.equals(posto.getNome(), "Combustao_Gasolina")) {
                servico = servicoFacade.criaServico(2,posto, idCliente, matricula, nomeServico,4);
            }
        }
        return servico;
    }

    public Servico servicoUniversalPedido(String idCliente, String matricula, String nomeServico) {
        Servico servico = null;
        for (Map.Entry<String, Posto> entry : this.servicoFacade.getPostos().entrySet()) {
            Posto posto = entry.getValue();
            if (Objects.equals(posto.getNome(), "Universal")) {
                int tipo;
                Veiculo veiculo = servicoFacade.getVeiculos().get(matricula);
                if (veiculo instanceof VeiculoGasolina) tipo = 1;
                else if (veiculo instanceof VeiculoGasoleo) tipo = 2;
                else if (veiculo instanceof VeiculoEletrico) tipo = 3;
                else tipo = 4;
                servico = servicoFacade.criaServico(1, posto, idCliente, matricula, nomeServico, tipo);
            }
        }
        return servico;
    }

    public Servico servicoCombustaoGasoleoPedido(String idCliente, String matricula, String nomeServico) {
        Servico servico = null;
        for (Map.Entry<String, Posto> entry : this.servicoFacade.getPostos().entrySet()) {
            Posto posto = entry.getValue();
            if (Objects.equals(posto.getNome(), "Combustao_Gasoleo")) {
                servico = servicoFacade.criaServico(3, posto, idCliente, matricula, nomeServico, 2);
            }
        }
        return servico;
    }
    public Servico servicoCombustaoGasoleoHibridoPedido(String idCliente, String matricula, String nomeServico) {
        Servico servico = null;
        for (Map.Entry<String, Posto> entry : this.servicoFacade.getPostos().entrySet()) {
            Posto posto = entry.getValue();
            if (Objects.equals(posto.getNome(), "Combustao_Gasoleo")) {
                servico = servicoFacade.criaServico(3, posto, idCliente, matricula, nomeServico, 4);
            }
        }
        return servico;
    }

    public Servico servicoEletricoPedido(String idCliente, String matricula, String nomeServico) {
        Servico servico = null;
        for (Map.Entry<String, Posto> entry : this.servicoFacade.getPostos().entrySet()) {
            Posto posto = entry.getValue();
            if (Objects.equals(posto.getNome(), "Eletrico")) {
                servico = servicoFacade.criaServico(4,posto, idCliente, matricula, nomeServico, 3);
            }
        }
        return servico;
    }

    public Servico servicoEletricoHibridoPedido(String idCliente, String matricula, String nomeServico) {
        Servico servico = null;
        for (Map.Entry<String, Posto> entry : this.servicoFacade.getPostos().entrySet()) {
            Posto posto = entry.getValue();
            if (Objects.equals(posto.getNome(), "Eletrico")) {
                servico = servicoFacade.criaServico(4,posto, idCliente, matricula, nomeServico, 4);
            }
        }
        return servico;
    }

    public void fazConfirmaAgendamento(Servico servico, Cliente cliente, List<LocalDateTime> horariosNegados, LocalDateTime horarioSugerido){
        servicoFacade.confirmaAgendamento(servico, cliente, horariosNegados, horarioSugerido);
        //o serviço é atribuído ao mecanico do posto
        Posto posto = servicoFacade.getPostos().get(servico.getId_posto());
        String idMecanico = posto.getMecanico();
        Funcionario mecanico = utilizadorFacade.getFuncionarios().get(idMecanico);
        mecanico.getAgendaTarefas().put(servico.getIdS(), horarioSugerido);
    }



    // Metodos auxiliares (getters e setters) --------------------------------------------------------------------------
    public Map<String, Funcionario> getFuncionarios() {
        return utilizadorFacade.getFuncionarios();
    }

    public String getFuncionarioAtual() {
        return utilizadorFacade.getFuncionarioAtual();
    }

    public String getClienteAtual() {
        return utilizadorFacade.getClienteAtual();
    }

    public Map<String, Posto> getPostos() {
        return servicoFacade.getPostos();
    }

    public Map<String, Servico> getServicos() {
        return servicoFacade.getServicos();
    }

    public Map<String, Veiculo> getVeiculos() {
        return servicoFacade.getVeiculos();
    }

    public Map<String, Cliente> getClientes() {
        return utilizadorFacade.getClientes();
    }

    public Scanner getScanner() {
        return scanner;
    }


    public LocalDateTime getHorarioSugerido(String idPosto, Duration duracaoPrevista, String idCliente, List<LocalDateTime> horariosNegados) {
        return servicoFacade.procuraHorario(idPosto, duracaoPrevista, utilizadorFacade.getClientes().get(idCliente), horariosNegados);
    }

}

