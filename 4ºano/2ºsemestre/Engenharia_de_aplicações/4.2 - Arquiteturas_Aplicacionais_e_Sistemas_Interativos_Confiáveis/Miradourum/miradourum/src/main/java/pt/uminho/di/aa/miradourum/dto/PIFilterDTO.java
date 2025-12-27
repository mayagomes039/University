package pt.uminho.di.aa.miradourum.dto;

import java.time.LocalDateTime;

public class PIFilterDTO {
    private Double maxDistance;
    private Double minScore;
    private LocalDateTime minCreationDate;
    private Boolean accessibility;
    private Integer maxDifficulty;
    private Double userLatitude;
    private Double userLongitude;

    // Construtores
    public PIFilterDTO() {}

    public PIFilterDTO(Double maxDistance, Double minScore, LocalDateTime minCreationDate,
                       Boolean accessibility, Integer maxDifficulty, Double userLatitude, Double userLongitude) {
        this.maxDistance = maxDistance;
        this.minScore = minScore;
        this.minCreationDate = minCreationDate;
        this.accessibility = accessibility;
        this.maxDifficulty = maxDifficulty;
        this.userLatitude = userLatitude;
        this.userLongitude = userLongitude;
    }

    // Getters e Setters
    public Double getMaxDistance() { return maxDistance; }
    public void setMaxDistance(Double maxDistance) { this.maxDistance = maxDistance; }

    public Double getMinScore() { return minScore; }
    public void setMinScore(Double minScore) { this.minScore = minScore; }

    public LocalDateTime getMinCreationDate() { return minCreationDate; }
    public void setMinCreationDate(LocalDateTime minCreationDate) { this.minCreationDate = minCreationDate; }

    public Boolean getAccessibility() { return accessibility; }
    public void setAccessibility(Boolean accessibility) { this.accessibility = accessibility; }

    public Integer getMaxDifficulty() { return maxDifficulty; }
    public void setMaxDifficulty(Integer maxDifficulty) { this.maxDifficulty = maxDifficulty; }

    public Double getUserLatitude() { return userLatitude; }
    public void setUserLatitude(Double userLatitude) { this.userLatitude = userLatitude; }

    public Double getUserLongitude() { return userLongitude; }
    public void setUserLongitude(Double userLongitude) { this.userLongitude = userLongitude; }
}
