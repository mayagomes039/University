package pt.uminho.di.aa.miradourum.models;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PontoInteresse
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
    @Column(name = "Latitude")
    private Double latitude;
    @Column(name = "Longitude")
    private Double longitude;
    @Column(name = "Name")
    private String name;
    @Column(name = "Description")
    private String description;
    @Column(name = "Dificulty")
    private Integer difficulty;
    @Column(name = "Accessibility")
    private Boolean accessibility;
    @Column(name = "State")
    private Boolean state;
    @Column(name = "Premium")
    private Boolean premium;
    @Column(name = "Score")
    private Double score;
    @Column(name = "CreationDate")
    private LocalDateTime creationDate;
    @ManyToMany(mappedBy = "pontoInteresse")
    @JsonBackReference
    private List<User> userList;
    @Column(name = "CreatorEmail")
    private String creatorEmail;

    @OneToMany(mappedBy = "pontoInteresse")
    @JsonManagedReference
    private List<ImagePontoInteresse> images;

    @JsonManagedReference
    @OneToMany(mappedBy = "pontoInteresse")
    private List<Review> reviews;


    public PontoInteresse(Double latitude,Double longitude, String name, String description, Integer difficulty, Boolean accessibility, Boolean state, Boolean premium, Double score, LocalDateTime creationDate, List<User> userList, List<Review> reviews,String creatorEmail) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
        this.accessibility = accessibility;
        this.state = state;
        this.premium = premium;
        this.score = score;
        this.creationDate = creationDate;
        this.userList = userList;
        this.reviews = reviews;
        this.creatorEmail = creatorEmail;
    }

    public PontoInteresse(Double latitude, Double longitude, String name, String description, Integer difficulty, Boolean accessibility, Boolean state, Boolean premium, Double score, LocalDateTime creationDate,String creatorEmail) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
        this.accessibility = accessibility;
        this.state = state;
        this.premium = premium;
        this.score = score;
        this.creationDate = creationDate;
        this.creatorEmail = creatorEmail;
    }

    public PontoInteresse() {
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }
    public Boolean getPremium() {
        return premium;
    }

    public void setPremium(Boolean premium) {
        this.premium = premium;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getAccessibility() {
        return accessibility;
    }

    public void setAccessibility(Boolean accessibility) {
        this.accessibility = accessibility;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public Boolean getState() {
        return this.state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }
    public void addReview(Review review) {
        this.reviews.add(review);
    }

    public String getCreatorEmail() {
        return creatorEmail;
    }

    public void setCreatorEmail(String creatorEmail) {
        this.creatorEmail = creatorEmail;
    }

    public List<ImagePontoInteresse> getImages() {
        return images;
    }

    public void setImages(List<ImagePontoInteresse> images) {
        this.images = images;
    }
}
