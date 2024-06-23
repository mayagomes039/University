package Model;

import java.time.LocalDateTime;
import java.util.*;

public class Posto {
    private String idP;
    private String nome;
    private String mecanico;
    private List<LocalDateTime> disponibilidades;
    private List<String> competencias_posto;

    public Posto() {
        this.idP = UUID.randomUUID().toString();
        this.nome = null;
        this.mecanico = null;
        this.disponibilidades = new ArrayList<>();
        this.competencias_posto = new ArrayList<>();
    }

    public Posto(String idP, String nome, String mecanico, List<LocalDateTime> disponibilidades, List<String> competencias_posto) {
        this.idP = idP;
        this.nome = nome;
        this.mecanico = mecanico;
        this.disponibilidades = disponibilidades;
        this.competencias_posto = competencias_posto;
    }

    public String getIdP() {
        return idP;
    }

    public void setIdP(String idP) {
        this.idP = idP;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getMecanico() {
        return mecanico;
    }

    public void setMecanico(String mecanico) {
        this.mecanico = mecanico;
    }

    public List<LocalDateTime> getDisponibilidades() {
        return disponibilidades;
    }

    public void setDisponibilidades(List<LocalDateTime> disponibilidades) {
        this.disponibilidades = disponibilidades;
    }

    public List<String> getCompetencias_posto() {
        return competencias_posto;
    }

    public void setCompetencias_posto(List<String> competencias_posto) {
        this.competencias_posto = competencias_posto;
    }
}

