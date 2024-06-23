package Model;

import java.util.Map;

public interface ISubUtilizador {
    public boolean logIn(String idUtilizador, String palavra_passe);
    public boolean logOut();
    public void registaHorarioChegada();
    public void registaHoraFimdeTurno();
    public boolean verificaCompetencias(Funcionario mecanico, Posto posto);
    public boolean logInFichaCliente(String nifCliente);

    public void logOutCliente() ;

    public String getFuncionarioAtual() ;

    public void setFuncionarioAtual(String funcionarioAtual);

    public String getClienteAtual();

    public void setClienteAtual(String clienteAtual);

    public Map<String, Cliente> getClientes();

    public void setClientes(Map<String, Cliente> clientes);

    public Map<String, Funcionario> getFuncionarios();

    public void setFuncionarios(Map<String, Funcionario> funcionarios);

}
