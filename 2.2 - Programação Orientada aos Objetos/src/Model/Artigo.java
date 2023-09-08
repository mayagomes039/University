package Model;


import java.io.Serializable;
import java.util.UUID;

public abstract class Artigo implements Serializable, Cloneable {
    private String nome;
    private String usado;
    private String descricao;
    private String marca;
    private String codigo_alfanumerico;
    private float preco_base;
    private float correcao_preco;
    private AvaliacaoEstado avaliacao_estado;
    private int num_donos;
    private String vendido;
    private String ID_T;
    private String email_vendedor; //str é o email


    public abstract void CalculaCorrecao();


    public Artigo (String nome, String condicao, String descricao, String marca, float preco_base, float correcao_preco, AvaliacaoEstado avaliacao_estado, int num_donos, String vendido, String ID_T, String vendedor) {
        this.nome = nome;
        this.usado = condicao;
        this.descricao = descricao;
        this.marca = marca;
        this.codigo_alfanumerico = UUID.randomUUID().toString();
        this.preco_base = preco_base;
        this.correcao_preco = correcao_preco;
        this.avaliacao_estado = avaliacao_estado;
        this.num_donos = num_donos;
        this.vendido = vendido;
        this.ID_T = ID_T;
        this.email_vendedor = vendedor;
    }

    public Artigo (Artigo x){
        this.nome = x.getNome();
        this.usado = x.getUsado();
        this.descricao = x.getDescricao();
        this.marca = x.getMarca();
        this.codigo_alfanumerico = x.getCodigo();
        this.preco_base = x.getPreco_base();
        this.correcao_preco = x.getCorrecao();
        this.avaliacao_estado = x.getAvaliacao ();
        this.num_donos = x.getNum_donos();
        this.vendido = x.getVendido ();
        this.ID_T = x.getID_T();
        this.email_vendedor = x.getEmail_vendedor();
    }


    public String getNome () {
        return this.nome;
    }

    public String getUsado() {
        return this.usado;
    }

    public String getDescricao () {
        return this.descricao;
    }

    public String getMarca () {
        return this.marca;
    }

    public String getCodigo () {
        return this.codigo_alfanumerico;
    }

    public float getPreco_base () {
        return this.preco_base;
    }

    public float getCorrecao () {
        return this.correcao_preco;
    }

    public int getNum_donos () {
        return this.num_donos;
    }

    public String getVendido () {
        return this.vendido;
    }


    public AvaliacaoEstado getAvaliacao () {
        return this.avaliacao_estado;
    }

    public String getID_T (){ return this.ID_T;}


    public String getCodigo_alfanumerico() {
        return codigo_alfanumerico;
    }

    public void setPreco_base(float calculo){
        preco_base = calculo;
    }
    /*
    public void setCondicao (boolean x){
        this.condicao = x;
    }
    public void setDescricao (String x){
        this.descricao = x;
    }
    public void setMarca (String x){
        this.marca = x;
    }
    public void setCodigo (String x){
        this.codigo_alfanumerico = x;
    }

    public void setPreco_base (float x){
        this.preco_base = x;
    }
    public void setNum_donos (int x){
        this.num_donos = x;
    }
    public void setCorrecao (float x){
        this.correcao_preco = x;
    }

    public void setAvaliacao (AvaliacaoEstado x){
        this.avaliacao_estado = x;
    }

     */
    public void setVendido (String x){
        this.vendido = x;
    }

    public String getEmail_vendedor() {
        return email_vendedor;
    }

    public void setEmail_vendedor(String email_vendedor) {
        this.email_vendedor = email_vendedor;
    }

    @Override
    public String toString() {
        return nome + ";<>;" + usado + ";<>;"  + marca+ ";<>;<INSERIRTAMANHO>;<>;"+ preco_base + ";<>;"
                +avaliacao_estado+ ";<>;" + num_donos + ";<>;" +vendido+ ";<>;" +ID_T;
    }

    public Artigo clone() throws CloneNotSupportedException {
        return (Artigo) super.clone();
    }


}
