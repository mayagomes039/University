package Model;
import Data.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class SubServicoFacade implements ISubServico {
    private Map<String, Posto> postos = PostoDAO.getInstance();
    private Map<String, Servico> servicos = ServicoDAO.getInstance();
    private Map<String, Veiculo> veiculos = VeiculoDAO.getInstance();


    // Criar novos serviços e agendamento ---------------------------------------------------------------------------------------------------


    public Servico criaServico(int tipoServ, Posto posto, String idCliente, String matricula, String nomeServico, int tipoVeiculo){
        Servico servico;
        if (tipoServ ==1) {
            servico = new ServicoUniversal(nomeServico,matricula,idCliente,posto.getMecanico(), posto.getIdP(), null, null, Duration.ofMinutes(30));
            Veiculo veiculo = veiculos.get(matricula);
            veiculo.getServicos_universais().add((ServicoUniversal) servico);
        }
        else if (tipoServ==2) {
            servico = new ServicoGasolina(nomeServico,matricula,idCliente,posto.getMecanico(), posto.getIdP(), null, null, Duration.ofHours(1));
            if (tipoVeiculo ==1){
                VeiculoGasolina veiculo = (VeiculoGasolina) veiculos.get(matricula);
                veiculo.getServicos_gasolina().add((ServicoGasolina) servico);
            }
            else { // (tipoVeiculo ==4)
                VeiculoHibrido veiculo = (VeiculoHibrido) veiculos.get(matricula);
                veiculo.getServicos_gasolina().add((ServicoGasolina) servico);
            }

        }
        else if (tipoServ==3) {
            servico = new ServicoGasoleo(nomeServico,matricula,idCliente,posto.getMecanico(), posto.getIdP(), null, null, Duration.ofHours(1));
            if (tipoVeiculo ==2){
                VeiculoGasoleo veiculo = (VeiculoGasoleo) veiculos.get(matricula);
                veiculo.getServicos_gasoleo().add((ServicoGasoleo) servico);
            }
            else { // (tipoVeiculo ==4)
                VeiculoHibrido veiculo = (VeiculoHibrido) veiculos.get(matricula);
                veiculo.getServicos_gasoleo().add((ServicoGasoleo) servico);
            }
        }
        else { // (tipoServ==4)
            servico = new ServicoEletrico(nomeServico,matricula,idCliente,posto.getMecanico(), posto.getIdP(), null, null, Duration.ofHours(1));
            if (tipoVeiculo ==3){
                VeiculoEletrico veiculo = (VeiculoEletrico) veiculos.get(matricula);
                veiculo.getServicos_eletricos().add((ServicoEletrico) servico);
            }
            else { // (tipoVeiculo ==4)
                VeiculoHibrido veiculo = (VeiculoHibrido) veiculos.get(matricula);
                veiculo.getServicos_eletricos().add((ServicoEletrico) servico);
            }
        }

        return servico;
    }

    // procurar um horario para o serviço
    public LocalDateTime procuraHorario(String id_posto, Duration duracaoprev, Cliente cliente, List<LocalDateTime> horariosNegados) {
        if (horariosNegados == null)  horariosNegados = new ArrayList<>();
        for (Map.Entry<String, Posto> entry : postos.entrySet()) {
            if (Objects.equals(entry.getKey(), id_posto)) {
                //percorrer os horarios disponiveis e ver um que tem duração igual
                Posto posto = postos.get(entry.getKey());
                List<LocalDateTime> disponibilidades = posto.getDisponibilidades();
                //ordenar
                disponibilidades.sort(Comparator.naturalOrder());

                //verificar se a duração corresponde
                for (int i = 0; i < disponibilidades.size() - 1; i++) {
                    LocalDateTime horario = disponibilidades.get(i);

                    //confirmar que nao foi negado anteriormente
                    if (horariosNegados.isEmpty() || (!horariosNegados.contains(horario))) {
                        LocalDateTime nextDateTime = disponibilidades.get(i + 1);
                        Duration duracao = Duration.between(horario, nextDateTime);

                        //A duracao_prevista é menor ou igual a duracao do horario
                        if (duracaoprev.compareTo(duracao) <= 0) {
                            return horario;
                        }
                    }
                }
            }
        }
        return null;
    }


    public void confirmaAgendamento(Servico servico, Cliente cliente, List<LocalDateTime> horariosNegados, LocalDateTime horarioSugerido) {
        //remover o horario escolhido das disponibilidades
        Posto posto = postos.get(servico.getId_posto());
        PostoDAO.removerHorarioDisponibilidades(posto, horarioSugerido);

        //atualisar a data do serviço; e addicionar o novo serviço aos serviços da facade
        servico.setData_comeco(horarioSugerido);
        servicos.put(servico.getIdS(), servico);

        ServicoDAO servicoDAO = new ServicoDAO();
        servicoDAO.atualizaAgendamentoServico(servico.getIdS(), horarioSugerido);
        servicoDAO.put(servico.getIdS(),servico);
    }

    //Concluir um serviço --------------------------------------------------------------------------------------------------------------------------

    public void concluirServico(Servico servico, LocalDateTime data_terminio) {
        servico.setData_terminio(data_terminio);
        ServicoDAO.atualizaFimServico(servico.getIdS(), data_terminio);
    }

    //Notificar o cliente --------------------------------------------------------------------------------------------------------------------------
    public String notificaCliente(Cliente cliente){
        String email = cliente.getEmail();
        String mensagem = "Enviar a : " + email + "\n" + "Informamos que o seu serviço foi concluído! \n" +
                "Quando desejar, pode comparecer a estação para efetuar ao pagamento e levantar o seu veículo.";
        return mensagem;
    }

    // Agenda das tarefas --------------------------------------------------------------------------------------
    public Map<String, Servico> getAgendaTarefa(Funcionario func){
        Map<String, Servico> tarefas = new HashMap<>();
        for (Map.Entry<String, LocalDateTime> entry : func.getAgendaTarefas().entrySet()) {
            String idS = entry.getKey();
            Servico serv = servicos.get(idS);
            tarefas.put(serv.getIdS(), serv);
        }
        return tarefas;
    }


    //Metodos auxiliares ---------------------------------------------------------------------------------------------------------


    public Map<String, Posto> getPostos() {
        return postos;
    }

    public void setPostos(Map<String, Posto> postos) {
        this.postos = postos;
    }

    public Map<String, Servico> getServicos() {
        return servicos;
    }

    public void setServicos(Map<String, Servico> servicos) {
        this.servicos = servicos;
    }

    public Map<String, Veiculo> getVeiculos() {
        return veiculos;
    }

    public void setVeiculos(Map<String, Veiculo> veiculos) {
        this.veiculos = veiculos;
    }

}
