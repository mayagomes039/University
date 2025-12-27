package pt.uminho.di.aa.miradourum.models;



import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
public class ImagePontoInteresse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID")
    private Long id;

    @Column(name="Url")
    private String url;

    @ManyToOne
    @JoinColumn(name = "PontoInteresseID",referencedColumnName = "ID")
    @JsonBackReference
    private PontoInteresse pontoInteresse;


    public ImagePontoInteresse(PontoInteresse pontoInteresse) {
        this.pontoInteresse= pontoInteresse;
    }

    public ImagePontoInteresse() {}



    public PontoInteresse getPontoInteresse() {
        return pontoInteresse;
    }

    public void setPontoInteresse(PontoInteresse pontoInteresse) {
        this.pontoInteresse = pontoInteresse;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getId() {
        return id;
    }
}
