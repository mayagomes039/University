package Model;

import java.util.List;

public class VeiculoEletrico extends Veiculo{

    private List<ServicoEletrico> servicos_eletricos;
    public VeiculoEletrico(String matricula,  List<ServicoUniversal> servicos_universais, List<ServicoEletrico> servicos_eletrico) {
        super(matricula,servicos_universais);
        this.servicos_eletricos = servicos_eletrico;
    }

    public List<ServicoEletrico> getServicos_eletricos() {
        return servicos_eletricos;
    }

    public void setServicos_eletricos(List<ServicoEletrico> servicos_eletricos) {
        this.servicos_eletricos = servicos_eletricos;
    }
}
