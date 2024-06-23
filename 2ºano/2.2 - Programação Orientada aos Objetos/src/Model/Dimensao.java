package Model;

import java.io.Serializable;

public class Dimensao implements Serializable, Cloneable {
    private float largura;
    private float altura;
    private float comprimento;

    public Dimensao (float largura, float altura, float comprimento){
        this.largura = largura;
        this.altura = altura;
        this.comprimento = comprimento;
    }

    public float getLargura() {
        return largura;
    }

    public void setLargura(float largura) {
        this.largura = largura;
    }

    public float getAltura() {
        return altura;
    }

    public void setAltura(float altura) {
        this.altura = altura;
    }

    public float getComprimento() {
        return comprimento;
    }

    public void setComprimento(float comprimento) {
        this.comprimento = comprimento;
    }

    public String toString() {
        return largura+ "x" + altura+ "x" + comprimento;
    }

    public Dimensao clone() throws CloneNotSupportedException {
        Dimensao clone = (Dimensao) super.clone();
        return clone;
    }
}
