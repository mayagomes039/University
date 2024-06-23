package Model;

import java.util.List;

public class VeiculoGasoleo extends Veiculo {

    private List<ServicoGasoleo> servicos_gasoleo;

    public VeiculoGasoleo(String matricula,  List<ServicoUniversal> servicos_universais, List<ServicoGasoleo> servicos_gasoleo) {
        super(matricula,servicos_universais);
        this.servicos_gasoleo = servicos_gasoleo;
    }

    public List<ServicoGasoleo> getServicos_gasoleo() {
        return servicos_gasoleo;
    }

    public void setServicos_gasoleo(List<ServicoGasoleo> servicos_gasoleo) {
        this.servicos_gasoleo = servicos_gasoleo;
    }
}
