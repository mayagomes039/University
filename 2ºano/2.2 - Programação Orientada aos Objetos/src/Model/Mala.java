package Model;


import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

public class Mala extends Artigo implements Serializable, Cloneable {
    private Dimensao dimensao;
    private String material;
    private int ano_colecao;
    private MalaTipo tipo;

    public Mala (Dimensao dimensao, String material, int ano_colecao, MalaTipo tipo, String nome, String condicao, String descricao, String marca, float preco_base, float correcao_preco, AvaliacaoEstado avaliacao_estado, int num_donos, String vendido, String ID_T, String email_vendedor) {
        super(nome, condicao, descricao, marca, preco_base, correcao_preco, avaliacao_estado, num_donos, vendido, ID_T, email_vendedor);
        this.dimensao = dimensao;
        this.material = material;
        this.ano_colecao = ano_colecao;
        this.tipo = tipo;
    }

    public Dimensao getDimensao() {
        return dimensao;
    }

    public void setDimensao(Dimensao dimensao) {
        this.dimensao = dimensao;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public int getAno_colecao() {
        return ano_colecao;
    }

    public void setAno_colecao(int ano_colecao) {
        this.ano_colecao = ano_colecao;
    }

    public MalaTipo getTipo() {
        return tipo;
    }

    public void setTipo(MalaTipo tipo) {
        this.tipo = tipo;
    }

    @Override
    public void CalculaCorrecao() {
        int ano_atual = LocalDate.now().getYear();
        if (this.getAno_colecao()< ano_atual){
            int dif = ano_atual - this.getAno_colecao();
            float calculo = (float) (this.getPreco_base() - (dif*0.5));
            setPreco_base(calculo);
        }

    }

    public String toString() {
        String desc = this.getDescricao()+". Tipo: " + tipo+ ". Material: " +  material + ". Ano de coleção: " + ano_colecao;
        return super.toString().replace("<INSERIRTAMANHO>", dimensao.toString())+ ";<>;" + desc;
    }

    public Mala clone() throws CloneNotSupportedException {
        Mala clone = (Mala) super.clone();
        return clone;
    }
}
