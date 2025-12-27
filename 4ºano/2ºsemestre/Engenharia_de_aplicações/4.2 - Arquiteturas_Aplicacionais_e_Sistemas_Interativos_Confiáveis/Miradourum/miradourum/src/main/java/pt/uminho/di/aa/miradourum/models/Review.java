package pt.uminho.di.aa.miradourum.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "Comment")
    private String comment;

    @Column(name = "Rating")
    private Integer rating;

    @Column(name = "CreationDate")
    private LocalDateTime creationDate;

    @Column(name = "UserID")
    private Long userid;


    @OneToMany(mappedBy = "review")
    @JsonManagedReference
    private List<Image> images;

    @ManyToOne
    @JsonBackReference
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "PontoInteresseID") // this is the DB column that stores the FK
    private PontoInteresse pontoInteresse;


    public Review(Integer rating, String comment, LocalDateTime creationDate, Long userid, PontoInteresse pontoInteresse) {
        this.rating = rating;
        this.comment = comment;
        this.creationDate = creationDate;
        this.userid = userid;
        this.pontoInteresse = pontoInteresse;
    }

    public Review() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public PontoInteresse getPontoInteresse() {
        return pontoInteresse;
    }

    public void setPontoInteresse(PontoInteresse pontoInteresse) {
        this.pontoInteresse = pontoInteresse;
    }

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }


    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public void addImages(List<Image> images) {
        if (this.images == null) {
            this.images = new ArrayList<Image>();
            this.images.addAll(images);
        }
    }
}
