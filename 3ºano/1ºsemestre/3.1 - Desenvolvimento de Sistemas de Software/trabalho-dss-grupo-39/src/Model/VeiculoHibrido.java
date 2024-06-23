package Model;

import java.util.List;

public class VeiculoHibrido extends Veiculo{
    private List<ServicoGasoleo> servicos_gasoleo;
    private List<ServicoGasolina> servicos_gasolina;
    private List<ServicoEletrico> servicos_eletricos;

    public VeiculoHibrido(String matricula,  List<ServicoUniversal> servicos_universais, List<ServicoGasoleo> servicos_gasoleo, List<ServicoGasolina> servicos_gasolina,List<ServicoEletrico> servicos_eletricos ) {
        super(matricula,servicos_universais);
        this.servicos_gasoleo = servicos_gasoleo;
        this.servicos_gasolina = servicos_gasolina;
        this.servicos_eletricos = servicos_eletricos;
    }

    public List<ServicoGasoleo> getServicos_gasoleo() {
        return servicos_gasoleo;
    }

    public void setServicos_gasoleo(List<ServicoGasoleo> servicos_gasoleo) {
        this.servicos_gasoleo = servicos_gasoleo;
    }

    public List<ServicoGasolina> getServicos_gasolina() {
        return servicos_gasolina;
    }

    public void setServicos_gasolina(List<ServicoGasolina> servicos_gasolina) {
        this.servicos_gasolina = servicos_gasolina;
    }

    public List<ServicoEletrico> getServicos_eletricos() {
        return servicos_eletricos;
    }

    public void setServicos_eletricos(List<ServicoEletrico> servicos_eletricos) {
        this.servicos_eletricos = servicos_eletricos;
    }
}
