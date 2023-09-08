package Model;

import Controler.Controler;
import View.View;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Utilizador implements Serializable, Cloneable {
    private String codigo_sistema;
    private String email;
    private String nome;
    private String morada;
    private String num_fiscal;
    private String palavra_passe;
    private float saldo;

    private List<String> lista_artigos; //0s que ele vende ou ja vendeu
    private List<String> lista_encomenda;// compras

    public Utilizador(String email, String nome, String morada, String num_fiscal, String palavra_passe) {
        this.codigo_sistema = UUID.randomUUID().toString();
        this.email = email;
        this.nome = nome;
        this.morada = morada;
        this.num_fiscal = num_fiscal;
        this.palavra_passe = palavra_passe;
        this.saldo = 0;
        this.lista_artigos = new ArrayList<>();
        this.lista_encomenda = new ArrayList<>();
    }

    public String getCodigo_sistema() {
        return codigo_sistema;
    }

    public void setCodigo_sistema(String codigo_sistema) {
        this.codigo_sistema = codigo_sistema;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getMorada() {
        return morada;
    }

    public void setMorada(String morada) {
        this.morada = morada;
    }

    public String getNum_fiscal() {
        return num_fiscal;
    }

    public void setNum_fiscal(String num_fiscal) {
        this.num_fiscal = num_fiscal;
    }

    public String getPalavra_passe() {
        return palavra_passe;
    }

    public void setPalavra_passe(String palavra_passe) {
        this.palavra_passe = palavra_passe;
    }

    public float getSaldo() {
        return saldo;
    }

    public void setSaldo(float saldo) {
        this.saldo = (float) Math.round(saldo * 100) / 100;
    }

    public List<String> getLista_artigos() {
        return lista_artigos;
    }

    public void setLista_artigos(List<String> lista_artigos) {
        this.lista_artigos = lista_artigos;
    }

    public List<String> getLista_encomenda() {
        return lista_encomenda;
    }

    public void setLista_encomenda(List<String> lista_encomenda) {
        this.lista_encomenda = lista_encomenda;
    }

    public String toString() {
        return email+ ";<>;" + nome+ ";<>;"  +codigo_sistema+ ";<>;" +morada+ ";<>;"+ num_fiscal+ ";<>;"  +palavra_passe+ ";<>;" +lista_artigos+ ";<>;"+ lista_encomenda+ ";<>;" + saldo;
    }

    public boolean compra (float precoArtigos, String ID_carrinho) {
        if (saldo < precoArtigos) {
            return false;
        }else {
            saldo = saldo - precoArtigos;
            lista_encomenda.add(ID_carrinho);
            return true;
        }
    }

    public Utilizador clone() throws CloneNotSupportedException {
        Utilizador clone = (Utilizador) super.clone();
        return clone;
    }
}