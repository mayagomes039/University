package Model;

import java.time.LocalDateTime;
import java.util.*;

public class Funcionario extends Utilizador{
    private List<String> competencias_mecanico;
    private LocalDateTime inicioTurno;
    private LocalDateTime fimTurno;
    private HashMap<String,LocalDateTime> agendaTarefas; //id serviço+hora

    private List<String> historico_servicos;

    public Funcionario (String nome, String id, String palavraPasse, List<String> competencias, LocalDateTime inicioTurno, LocalDateTime fimTurno, HashMap<String,LocalDateTime> agendaTarefas, List<String> historico_servicos) {
        super(nome,id,palavraPasse);
        this.competencias_mecanico = competencias;
        this.inicioTurno = inicioTurno;
        this.fimTurno = fimTurno;
        this.agendaTarefas = agendaTarefas;
        this.historico_servicos = historico_servicos;
    }


    public List<String> getCompetencias_mecanico() {
        return competencias_mecanico;
    }

    public void setCompetencias_mecanico(List<String> competencias_mecanico) {
        this.competencias_mecanico = competencias_mecanico;
    }

    public LocalDateTime getInicioTurno() {
        return inicioTurno;
    }

    public void setInicioTurno(LocalDateTime inicioTurno) {
        this.inicioTurno = inicioTurno;
    }

    public LocalDateTime getFimTurno() {
        return fimTurno;
    }

    public void setFimTurno(LocalDateTime fimTurno) {
        this.fimTurno = fimTurno;
    }


    public HashMap<String, LocalDateTime> getAgendaTarefas() {
        return agendaTarefas;
    }

    public void setAgendaTarefas(HashMap<String, LocalDateTime> agendaTarefas) {
        this.agendaTarefas = agendaTarefas;
    }

    public List<String> getHistorico_servicos() {
        return historico_servicos;
    }

    public void setHistorico_servicos(List<String> historico_servicos) {
        this.historico_servicos = historico_servicos;
    }
}
