package Model;

import java.util.List;

public class Veiculo {
    private String matricula;
    private List<ServicoUniversal> servicos_universais;



    public Veiculo(String matricula,  List<ServicoUniversal> servicos_universais) {
        this.matricula = matricula;
        this.servicos_universais = servicos_universais;
    }


    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }


    public List<ServicoUniversal> getServicos_universais() {
        return servicos_universais;
    }

    public void setServicos_universais(List<ServicoUniversal> servicos_universais) {
        this.servicos_universais = servicos_universais;
    }
}
