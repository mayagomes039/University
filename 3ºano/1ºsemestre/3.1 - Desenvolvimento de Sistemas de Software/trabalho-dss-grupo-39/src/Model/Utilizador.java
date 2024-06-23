package Model;

public class Utilizador {
    private String nome;
    private String id;
    private String palavraPasse;

    public Utilizador(String nome, String nif, String palavraPasse) {
        this.id = nif;
        this.nome = nome;
        this.palavraPasse = palavraPasse;
    }


    public String getNome() {
        return nome;
    }


    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getPalavraPasse() {
        return palavraPasse;
    }


    public void setPalavraPasse(String palavraPasse) {
        this.palavraPasse = palavraPasse;
    }
}
