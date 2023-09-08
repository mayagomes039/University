package Model;

import java.io.Serializable;
import java.util.List;

public class PremiumTransportadora extends Transportadora implements Serializable, Cloneable {

    public PremiumTransportadora( String nome, float margem_lucro, String criador) {
        super( nome, margem_lucro, criador);
    }

    @Override
    public float calculaPortes(List<Artigo> x, int num, float imposto) {
        float precoExpedicao = 0;
        if (x.size() == 1){
             precoExpedicao = (float) Math.round(((30 * 100 * getMargem_lucro() * (1+imposto)) * 0.9)/100);
        }
        else if (x.size() >= 2 && x.size()<= 5){
             precoExpedicao = (float) Math.round(((40 * 100 * getMargem_lucro() * (1+imposto)) * 0.9)/100);
        }
        else if (x.size() >5){
             precoExpedicao = (float) Math.round(((50 * 100 * getMargem_lucro() * (1+imposto)) * 0.9)/100);
        }
        return precoExpedicao;
    }


    @Override
    public String toString() {
        return super.toString();
    }

    public PremiumTransportadora clone() throws CloneNotSupportedException {
        PremiumTransportadora clone = (PremiumTransportadora) super.clone();
        return clone;
    }

}
