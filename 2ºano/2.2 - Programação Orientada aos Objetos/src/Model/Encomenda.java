package Model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Encomenda implements Serializable, Cloneable {
    private String ID_E;
    private List<String> lista_artigos;
    private DimensaoEncomenda dimensao;
    private float preco_artigos;
    private float preco_final;
    private EstadoEncomenda estado;
    private LocalDate dataCriacao;
    private LocalDate dataFinalizacao;
    private LocalDate dataExpedicao;

    public Encomenda() {
        this.ID_E = UUID.randomUUID().toString();
        this.lista_artigos = new ArrayList<>();
        this.dimensao = null;
        this.preco_artigos = 0;
        this.preco_final = 0;
        this.estado = EstadoEncomenda.PENDENTE;
        this.dataCriacao = LocalDate.now();
    }

    public String getID_E() {
        return ID_E;
    }

    public void setID_E(String ID_E) {
        this.ID_E = ID_E;
    }

    public List<String> getLista_artigos() {
        return lista_artigos;
    }

    public void setLista_artigos(List<String> lista_artigos) {
        this.lista_artigos = lista_artigos;
    }

    public DimensaoEncomenda getDimensao() { return dimensao;}

    public void setDimensao(DimensaoEncomenda dimensao) {
        this.dimensao = dimensao;
    }

    public float getPreco_artigos() {
        return preco_artigos;
    }

    public void setPreco_artigos(float preco_artigos) {
        this.preco_artigos = preco_artigos;
    }

    public EstadoEncomenda getEstado() {
        return estado;
    }

    public void setEstado(EstadoEncomenda estado) {
        this.estado = estado;
    }

    public LocalDate getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDate dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public float getPreco_final() {
        return preco_final;
    }

    public void setPreco_final(float preco_final) {
        this.preco_final = preco_final;
    }

    public String toString() {
        String df = dataFinalizacao!= null? dataFinalizacao.toString():"-";
        String de = dataExpedicao!= null? dataExpedicao.toString():"-";
        return dimensao+ ";<>;" + preco_artigos+ ";<>;" + estado+ ";<>;" + dataCriacao
                +";<>;" + df +";<>;" +de;
    }

    public void addArtigo(String codigo){
        lista_artigos.add(codigo);
    }

    public LocalDate getDataFinalizacao() {
        return dataFinalizacao;
    }

    public void setDataFinalizacao(LocalDate dataFinalizacao) {
        this.dataFinalizacao = dataFinalizacao;
    }

    public LocalDate getDataExpedicao() {
        return dataExpedicao;
    }

    public void setDataExpedicao(LocalDate dataExpedicao) {
        this.dataExpedicao = dataExpedicao;
    }

    public void calculaDimensao(){
        if (lista_artigos.size()==1) this.dimensao = DimensaoEncomenda.PEQUENO;
        else if (lista_artigos.size()<=5) this.dimensao = DimensaoEncomenda.MEDIO;
        else this.dimensao = DimensaoEncomenda.GRANDE;
    }

    public Encomenda clone() {
        try {
            return (Encomenda) super.clone();
        }catch (CloneNotSupportedException e){
            return null;
        }
    }

    public void removeArtigo(int i){
        lista_artigos.remove(i);
    }

    public void somaPreco (float a){
        preco_artigos += a;
    }

    public void subPreco (float a){
        preco_artigos -= a;
    }


}
