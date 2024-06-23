package Model;

import Data.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubUtilizadorFacade implements ISubUtilizador {

    private String funcionarioAtual;
    private String clienteAtual;
    private Map<String, Cliente> clientes = ClienteDAO.getInstance();
    private Map<String, Funcionario> funcionarios = FuncionarioDAO.getInstance();



    // LogIn e LogOut -----------------------------------------------------------------------------------------------
    public boolean logIn(String idUtilizador, String palavra_passe) {
        if (funcionarios.containsKey(idUtilizador)) {
            Funcionario funcionario = funcionarios.get(idUtilizador);
            if (funcionario.getPalavraPasse().equals(palavra_passe)) {
                setFuncionarioAtual(idUtilizador);
                registaHorarioChegada();
                return true;
            }
        }
        return false;
    }

    public boolean logOut() {
        registaHoraFimdeTurno();
        setFuncionarioAtual(null);
        return true;
    }

    // Registos de horarios -----------------------------------------------------------------------------------------

    public void registaHorarioChegada(){
        String funcionarioAtual = getFuncionarioAtual();
        LocalDateTime horaInicio = LocalDateTime.now();
        Funcionario mecanico = funcionarios.get(funcionarioAtual);
        mecanico.setInicioTurno(horaInicio);
        FuncionarioDAO.atualizarDataComeco(funcionarioAtual,horaInicio);
    }

    public void registaHoraFimdeTurno() {
        String funcionarioAtual = getFuncionarioAtual();
        LocalDateTime horafim = LocalDateTime.now();;
        Funcionario mecanico = funcionarios.get(funcionarioAtual);
        mecanico.setFimTurno(horafim);
        FuncionarioDAO.atualizarDataTerminio(funcionarioAtual,horafim);
    }

    // Validar competencias ----------------------------------------------------------------------------------------

    public boolean verificaCompetencias(Funcionario mecanico, Posto posto){
        if (posto == null) return false;
        Boolean competencias = true;
        List<String> competenciasPosto = posto.getCompetencias_posto();
        List<String> competenciasMecanico =mecanico.getCompetencias_mecanico();
        if (!(competenciasPosto.containsAll(competenciasMecanico) && competenciasMecanico.containsAll(competenciasPosto))){
            competencias = false;
        }
        return competencias;
    }

    // Entrar/Sair na ficha de um cliente ---------------------------------------------------------------------------

    public boolean logInFichaCliente(String nifCliente) {
        if (clientes.containsKey(nifCliente)) {
            setClienteAtual(nifCliente);
            return true;
        }else {
            return false;
        }
    }

    public void logOutCliente() {
        setClienteAtual(null);
    }

    // Getter e Setter ---------------------------------------------------------------------------
    public String getFuncionarioAtual() {
        return funcionarioAtual;
    }

    public void setFuncionarioAtual(String funcionarioAtual) {
        this.funcionarioAtual = funcionarioAtual;
    }

    public String getClienteAtual() {
        return clienteAtual;
    }

    public void setClienteAtual(String clienteAtual) {
        this.clienteAtual = clienteAtual;
    }

    public Map<String, Cliente> getClientes() {
        return clientes;
    }

    public void setClientes(Map<String, Cliente> clientes) {
        this.clientes = clientes;
    }

    public Map<String, Funcionario> getFuncionarios() {
        return funcionarios;
    }

    public void setFuncionarios(Map<String, Funcionario> funcionarios) {
        this.funcionarios = funcionarios;
    }
}
