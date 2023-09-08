package Model;

import java.io.Serializable;
import java.time.LocalDate;


public class PremiumSapatilha extends Sapatilha implements Serializable, Cloneable {
    private String autor;

    public PremiumSapatilha(String autor, int tamanho, String indicacao, String cor, int lancamento, String nome, String condicao, String descricao, String marca, float preco_base, float correcao_preco, AvaliacaoEstado avaliacao_estado, int num_donos, String vendido, String ID_T,String email_vendedor) {
        super(tamanho, indicacao, cor, lancamento, nome, condicao, descricao, marca, preco_base, correcao_preco, avaliacao_estado, num_donos, vendido, ID_T, email_vendedor);
        this.autor = autor;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    @Override
    public void CalculaCorrecao() {
        int ano_atual = LocalDate.now().getYear();
        if (this.getLancamento() < ano_atual){
            int dif = ano_atual - this.getLancamento();
            float calculo = this.getPreco_base() + dif*10;
            this.setPreco_base(calculo);
        }
    }

    @Override
    public String toString() {
        return super.toString() + ". Autor: " + autor;
    }

    public PremiumSapatilha clone() throws CloneNotSupportedException {
        PremiumSapatilha clone = (PremiumSapatilha) super.clone();
        return clone;
    }

}
