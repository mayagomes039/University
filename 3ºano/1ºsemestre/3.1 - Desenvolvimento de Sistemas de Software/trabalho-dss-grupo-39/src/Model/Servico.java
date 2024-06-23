package Model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public class Servico {
    private String idS;
    private String nome;
    private String matricula;

    private String id_cliente;

    private String id_mecanico;

    private String id_posto;

    private LocalDateTime data_comeco;
    private LocalDateTime data_terminio;

    private Duration duracao_prevista;

    public Servico(String idS,String nome, String matricula, String id_cliente, String id_mecanico, String id_posto, LocalDateTime data_comeco, LocalDateTime data_terminio, Duration duracao_prevista) {
        this.idS = idS;
        this.nome = nome;
        this.matricula = matricula;
        this.id_cliente = id_cliente;
        this.id_mecanico = id_mecanico;
        this.id_posto = id_posto;
        this.data_comeco = data_comeco;
        this.data_terminio = data_terminio;
        this.duracao_prevista = duracao_prevista;
    }

    public Servico(String idS,String nome, String matricula, String id_cliente, String id_mecanico, String id_posto, LocalDateTime data_comeco, Duration duracao_prevista) {
        this.idS = idS;
        this.nome = nome;
        this.matricula = matricula;
        this.id_cliente = id_cliente;
        this.id_mecanico = id_mecanico;
        this.id_posto = id_posto;
        this.data_comeco = data_comeco;
        this.duracao_prevista = duracao_prevista;
    }

    public Servico(String nome, String matricula, String id_cliente, String id_mecanico, String id_posto, LocalDateTime data_comeco, LocalDateTime data_terminio, Duration duracao_prevista) {
        this.idS = UUID.randomUUID().toString();
        this.nome = nome;
        this.matricula = matricula;
        this.id_cliente = id_cliente;
        this.id_mecanico = id_mecanico;
        this.id_posto = id_posto;
        this.data_comeco = data_comeco;
        this.data_terminio = data_terminio;
        this.duracao_prevista = duracao_prevista;
    }

    public String getIdS() {
        return idS;
    }

    public void setIdS(String idS) {
        this.idS = idS;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getId_cliente() {
        return id_cliente;
    }

    public void setId_cliente(String id_cliente) {
        this.id_cliente = id_cliente;
    }

    public String getId_mecanico() {
        return id_mecanico;
    }

    public void setId_mecanico(String id_mecanico) {
        this.id_mecanico = id_mecanico;
    }

    public String getId_posto() {
        return id_posto;
    }

    public void setId_posto(String id_posto) {
        this.id_posto = id_posto;
    }

    public LocalDateTime getData_comeco() {
        return data_comeco;
    }

    public void setData_comeco(LocalDateTime data_comeco) {
        this.data_comeco = data_comeco;
    }

    public LocalDateTime getData_terminio() {
        return data_terminio;
    }

    public void setData_terminio(LocalDateTime data_terminio) {
        this.data_terminio = data_terminio;
    }

    public Duration getDuracao_prevista() {
        return duracao_prevista;
    }

    public void setDuracao_prevista(Duration duracao_prevista) {
        this.duracao_prevista = duracao_prevista;
    }
}
