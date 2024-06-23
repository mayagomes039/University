package Model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public class ServicoEletrico extends Servico{
    public ServicoEletrico(String idS,String nome, String matricula, String id_cliente, String id_mecanico, String id_posto, LocalDateTime data_comeco, LocalDateTime data_terminio, Duration duracao_prevista) {
        super(idS, nome, matricula, id_cliente, id_mecanico, id_posto, data_comeco, data_terminio, duracao_prevista);
    }

    public ServicoEletrico(String nome, String matricula, String id_cliente, String id_mecanico, String id_posto, LocalDateTime data_comeco, LocalDateTime data_terminio, Duration duracao_prevista) {
        super( nome, matricula, id_cliente, id_mecanico, id_posto, data_comeco, data_terminio, duracao_prevista);
    }
}
