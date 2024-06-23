package Model;

import java.util.List;

public class Cliente extends Utilizador {
    private String morada;
    private String email;
    private int telefone;
    private List<String> veiculos_cliente; //matriculas

    private List<String> historico_servicos;
    private String estacaoFrequentada;


    public Cliente(String nome, String nif, String palavraPasse, String email, int num_tel, String morada, String estacaoFrequentada, List<String> historico, List<String> veiculos) {
        super(nome, nif, palavraPasse);
        this.email = email;
        this.telefone = num_tel;
        this.morada = morada;
        this.estacaoFrequentada = estacaoFrequentada;
        this.historico_servicos = historico;
        this.veiculos_cliente = veiculos;
    }


    public String getMorada() {
        return morada;
    }

    public void setMorada(String morada) {
        this.morada = morada;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getTelefone() {
        return telefone;
    }

    public void setTelefone(int telefone) {
        this.telefone = telefone;
    }

    public List<String> getVeiculos_cliente() {
        return veiculos_cliente;
    }

    public void setVeiculos_cliente(List<String> veiculos_cliente) {
        this.veiculos_cliente = veiculos_cliente;
    }

    public String getEstacaoFrequentada() {
        return estacaoFrequentada;
    }

    public void setEstacaoFrequentada(String estacaoFrequentada) {
        this.estacaoFrequentada = estacaoFrequentada;
    }

    public List<String> getHistorico_servicos() {
        return historico_servicos;
    }

    public void setHistorico_servicos(List<String> historico_servicos) {
        this.historico_servicos = historico_servicos;
    }
}
