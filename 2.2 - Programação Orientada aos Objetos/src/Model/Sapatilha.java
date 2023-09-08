package Model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

public class Sapatilha extends Artigo implements Serializable, Cloneable {


    private int tamanho;
    private String indicacao;
    private String cor;
    private int lancamento;

    public Sapatilha (int tamanho, String indicacao, String cor, int lancamento, String nome, String condicao, String descricao, String marca, float preco_base, float correcao_preco, AvaliacaoEstado avaliacao_estado, int num_donos, String vendido, String ID_T, String email_vendedor){
        super(nome, condicao, descricao, marca, preco_base, correcao_preco, avaliacao_estado, num_donos, vendido, ID_T, email_vendedor);
        this.tamanho = tamanho;
        this.indicacao = indicacao;
        this.cor = cor;
        this.lancamento = lancamento;
    }

    public int getTamanho() {
        return tamanho;
    }

    public void setTamanho(int tamanho) {
        this.tamanho = tamanho;
    }

    public String isIndicacao() {
        return indicacao;
    }

    public void setIndicacao(String indicacao) {
        this.indicacao = indicacao;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public int getLancamento() {
        return lancamento;
    }

    public void setLancamento(int lancamento) {
        this.lancamento = lancamento;
    }

    @Override
    public void CalculaCorrecao() {
        int anoAtual = LocalDate.now().getYear();
        if ((Objects.equals(this.getUsado(), "S") && this.lancamento != anoAtual ) || (Objects.equals(this.getUsado(), "N") && this.tamanho > 45 )) {
            float calculo = (float) (this.getPreco_base() - (100-this.getCorrecao())*0.01);
            this.setPreco_base(calculo);
        }
    }

    @Override
    public String toString() {
        String desc = this.getDescricao()+". Atacadores: " + indicacao+ ". Cor: " + cor + ". Ano de lançamento: " +lancamento;
        return  super.toString().replace("<INSERIRTAMANHO>", Integer.toString(tamanho)) + ";<>;"  +desc;
    }

    public Sapatilha clone() throws CloneNotSupportedException {
        Sapatilha clone = (Sapatilha) super.clone();
        return clone;
    }
}
