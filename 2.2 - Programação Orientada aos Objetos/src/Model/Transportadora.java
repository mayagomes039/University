package Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Transportadora implements Serializable, Cloneable {
    private List<String> lista_artigos;
    private float margem_lucro;
    private String ID_T; // é o nome
    private float valor_pequeno = 2.5F;
    private float valor_medio = 3;
    private float valor_grande = 4;
    private float valorFaturado;
    private String criador;

    public float calculaPortes(List<Artigo> x, int num, float imposto) {
        float precoExpedicao =0;

        if (x.size() == 1){
            precoExpedicao = (float) ((valor_pequeno * margem_lucro * (1+imposto)) * 0.9);
        }
        else if (x.size() >= 2 && x.size()<= 5){
            precoExpedicao = (float) ((valor_medio * margem_lucro * (1+imposto)) * 0.9);
        }
        else if (x.size() >5){
            precoExpedicao = (float) ((valor_grande * margem_lucro * (1+imposto)) * 0.9);
        }

        return precoExpedicao;

    }

    public Transportadora( String nome, float margem_lucro, String criador) {
        this.ID_T = nome;
        this.lista_artigos = new ArrayList<>();
        this.margem_lucro = margem_lucro;
        this.valorFaturado = 0;
        this.criador = criador;
    }

    public List<String> getLista_artigos() {
        return lista_artigos;
    }

    public void setLista_artigos(List<String> lista_artigos) {
        this.lista_artigos = lista_artigos;
    }

    public float getMargem_lucro() {
        return margem_lucro;
    }

    public void setMargem_lucro(float margem_lucro) {
        this.margem_lucro = margem_lucro;
    }

    public String getID_T() {
        return ID_T;
    }

    public void setID_T(String ID_T) {
        this.ID_T = ID_T;
    }

    public float getValor_pequeno() {
        return valor_pequeno;
    }

    public void setValor_pequeno(float valor_pequeno) {
        this.valor_pequeno = valor_pequeno;
    }

    public float getVelor_medio() {
        return valor_medio;
    }

    public void setVelor_medio(float velor_medio) {
        this.valor_medio = velor_medio;
    }

    public float getValor_grande() {
        return valor_grande;
    }

    public void setValor_grande(float valor_grande) {
        this.valor_grande = valor_grande;
    }

    public float getValorFaturado() {
        return valorFaturado;
    }

    public void setValorFaturado(float valorFaturado) {
        this.valorFaturado = valorFaturado;
    }

    public String getCriador() {
        return criador;
    }

    public void setCriador(String criador) {
        this.criador = criador;
    }

    @Override
    public String toString() {
        return ID_T+ ";<>;" + margem_lucro +";<>;" + valor_pequeno+";<>;" + valor_medio+";<>;" + valor_grande+";<>;" + criador;
    }

    public Transportadora clone() throws CloneNotSupportedException {
        Transportadora clone = (Transportadora) super.clone();
        return clone;
    }

}
