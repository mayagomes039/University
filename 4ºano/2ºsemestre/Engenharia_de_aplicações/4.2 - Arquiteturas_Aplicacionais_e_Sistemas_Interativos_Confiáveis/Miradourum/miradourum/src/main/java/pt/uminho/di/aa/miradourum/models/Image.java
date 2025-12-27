package pt.uminho.di.aa.miradourum.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID")
    private Long id;

    @Column(name="Url")
    private String url;

    @ManyToOne
    @JoinColumn(name = "ReviewID",referencedColumnName = "ID")
    @JsonBackReference
    private Review review;


    public Image(Review review) {
        this.review = review;
    }

    public Image() {}



    public Review getReview() {
        return review;
    }

    public void setReview(Review review) {
        this.review = review;
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
