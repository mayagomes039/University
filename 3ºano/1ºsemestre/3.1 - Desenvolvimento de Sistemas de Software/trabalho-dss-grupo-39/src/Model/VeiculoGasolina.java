package Model;

import java.util.List;

public class VeiculoGasolina extends Veiculo{
    private List<ServicoGasolina> servicos_gasolina;

    public VeiculoGasolina(String matricula,  List<ServicoUniversal> servicos_universais, List<ServicoGasolina> servicos_gasolina) {
        super(matricula,servicos_universais);
        this.servicos_gasolina = servicos_gasolina;
    }

    public List<ServicoGasolina> getServicos_gasolina() {
        return servicos_gasolina;
    }

    public void setServicos_gasolina(List<ServicoGasolina> servicos_gasolina) {
        this.servicos_gasolina = servicos_gasolina;
    }
}
