package Model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

public class PremiumMala extends Mala implements Serializable, Cloneable {

    public PremiumMala(Dimensao dimensao, String material, int ano_colecao, MalaTipo tipo, String nome, String condicao, String descricao, String marca, float preco_base, float correcao_preco, AvaliacaoEstado avaliacao_estado, int num_donos, String vendido, String ID_T,String email_vendedor) {
        super(dimensao, material, ano_colecao, tipo, nome, condicao, descricao, marca, preco_base, correcao_preco, avaliacao_estado, num_donos, vendido, ID_T, email_vendedor);
    }

    @Override
    public void CalculaCorrecao() {
        int ano_atual = LocalDate.now().getYear();
        if (this.getAno_colecao() < ano_atual){
            int dif = ano_atual - this.getAno_colecao();
            if (this.getTipo() == MalaTipo.BOLSA){
                float calculo = (float) (this.getPreco_base() + (0.15*dif));
                this.setPreco_base(calculo);
            }
            else if (this.getTipo() == MalaTipo.CARTEIRA){
                float calculo = (float) (this.getPreco_base() + (0.05*dif));
                this.setPreco_base(calculo);
            }
            else if (this.getTipo() == MalaTipo.ESTOJO){
                float calculo = (float) (this.getPreco_base() + (0.1*dif));
                this.setPreco_base(calculo);
            }
            else if (this.getTipo() == MalaTipo.MOCHILA){
                float calculo = (float) (this.getPreco_base() + (0.2*dif));
                this.setPreco_base(calculo);
            }
            else if (this.getTipo() == MalaTipo.VIAGEM){
                float calculo = (float) (this.getPreco_base() + (0.3*dif));
                this.setPreco_base(calculo);
            }

        }
    }

    public String toString() {
        return  super.toString() + getTipo()+ ";<>;" + getDimensao()+ ";<>;" +getMaterial()+ ";<>;" + getAno_colecao();
    }

    public PremiumMala clone() throws CloneNotSupportedException {
        PremiumMala clone = (PremiumMala) super.clone();
        return clone;
    }
}