package pt.uminho.di.aa.miradourum.dto;

import pt.uminho.di.aa.miradourum.models.Image;
import pt.uminho.di.aa.miradourum.models.ImagePontoInteresse;
import pt.uminho.di.aa.miradourum.projections.PontoInteresse.PIDetailsFullProjection;
import pt.uminho.di.aa.miradourum.projections.Review.ReviewProjection;

import java.time.LocalDateTime;
import java.util.List;

public class PIDetailsFullDTO {
    private Long id;
    private double latitude;
    private double longitude;
    private String name;
    private String description;
    private Integer difficulty;
    private boolean accessibility;
    private boolean premium;
    private double score;
    private LocalDateTime creationDate;
    private List<ReviewDTO> reviews;
    private List<ImagePontoInteresse> images;
    private boolean state;

    // Construtores, getters e setters
    public PIDetailsFullDTO() {}

    public PIDetailsFullDTO(PIDetailsFullProjection projection) {
        this.id = projection.getId();
        this.latitude = projection.getLatitude();
        this.longitude = projection.getLongitude();
        this.name = projection.getName();
        this.description = projection.getDescription();
        this.difficulty = projection.getDifficulty();
        this.accessibility = projection.getAccessibility();
        this.premium = projection.getPremium();
        this.score = projection.getScore();
        this.creationDate = projection.getCreationDate();
        this.images = projection.getImages();
        this.state = projection.getState();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
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

    public Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public boolean isAccessibility() {
        return accessibility;
    }

    public void setAccessibility(boolean accessibility) {
        this.accessibility = accessibility;
    }

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public List<ReviewDTO> getReviews() {
        return reviews;
    }

    public void setReviews(List<ReviewDTO> reviews) {
        this.reviews = reviews;
    }

    public List<ImagePontoInteresse> getImages() {
        return images;
    }

    public void setImages(List<ImagePontoInteresse> images) {
        this.images = images;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}

