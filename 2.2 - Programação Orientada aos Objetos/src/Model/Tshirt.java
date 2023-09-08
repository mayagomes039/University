package Model;

import java.io.Serializable;
import java.util.Objects;

public class Tshirt extends Artigo implements Serializable, Cloneable {
    private String tamanho;
    private Padrao padrao;


    public Tshirt(String tamanho, Padrao padrao, String nome, String condicao, String descricao, String marca, float preco_base, float correcao_preco, AvaliacaoEstado avaliacao_estado, int num_donos, String vendido, String ID_T, String email_vendedor) {
        super(nome, condicao, descricao, marca, preco_base, correcao_preco, avaliacao_estado, num_donos, vendido, ID_T, email_vendedor);
        this.tamanho = tamanho;
        this.padrao = padrao;
    }

    public String getTamanho() {
        return tamanho;
    }

    public void setTamanho(String tamanho) {
        this.tamanho = tamanho;
    }

    public Padrao getPadrao() {
        return padrao;
    }

    public void setPadrao(Padrao padrao) {
        this.padrao = padrao;
    }

    @Override
    public void CalculaCorrecao() {
        if ((padrao == Padrao.RISCAS || padrao == Padrao.PALMEIRAS) && Objects.equals(this.getUsado(), "S")) {
            float calculo = this.getPreco_base()/2;
            this.setPreco_base(calculo);
        }
    }

    public String toString() {
        String desc = this.getDescricao()+". Padrão: " + padrao;
        return  super.toString().replace("<INSERIRTAMANHO>", tamanho) + ";<>;"  +desc;
    }

    public Tshirt clone() throws CloneNotSupportedException {
        Tshirt clone = (Tshirt) super.clone();
        return clone;
    }

}
