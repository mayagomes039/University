package pt.uminho.di.aa.miradourum.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public class CreatePontoInteresseDTO {
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Difficulty is required")
    @Min(value = 1, message = "Difficulty must be between 1 and 5")
    @Max(value = 5, message = "Difficulty must be between 1 and 5")
    private Integer dificulty;

    @NotNull(message = "Accessibility is required")
    private Boolean accessibility;

    @NotNull(message = "Premium status is required")
    private Boolean premium;

    @NotBlank(message = "Creator email is required")
    @Email(message = "Invalid email format")
    private String creatorEmail;

    @NotEmpty(message = "At least one image is required")
    private List<String> imageUrls;

    // Getters e Setters
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDificulty() { return dificulty; }
    public void setDificulty(Integer dificulty) { this.dificulty = dificulty; }

    public Boolean getAccessibility() { return accessibility; }
    public void setAccessibility(Boolean accessibility) { this.accessibility = accessibility; }

    public Boolean getPremium() { return premium; }
    public void setPremium(Boolean premium) { this.premium = premium; }

    public String getCreatorEmail() { return creatorEmail; }
    public void setCreatorEmail(String creatorEmail) { this.creatorEmail = creatorEmail; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
}
