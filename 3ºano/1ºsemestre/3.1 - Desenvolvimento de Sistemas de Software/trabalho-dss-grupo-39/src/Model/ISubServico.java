package Model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ISubServico {
    public Map<String, Servico> getAgendaTarefa(Funcionario func);
    public Servico criaServico(int tipoServ, Posto posto, String idCliente, String matricula, String nomeServico, int tipoVeiculo);
    public LocalDateTime procuraHorario(String id_posto, Duration duracaoprev, Cliente cliente, List<LocalDateTime> horariosNegados);
    public void confirmaAgendamento(Servico servico, Cliente cliente, List<LocalDateTime> horariosNegados, LocalDateTime horarioSugerido);
    public void concluirServico(Servico servico, LocalDateTime data_terminio);
    public String notificaCliente(Cliente cliente);

    public Map<String, Posto> getPostos();

    public void setPostos(Map<String, Posto> postos);

    public Map<String, Servico> getServicos();

    public void setServicos(Map<String, Servico> servicos);

    public Map<String, Veiculo> getVeiculos();

    public void setVeiculos(Map<String, Veiculo> veiculos);
}
