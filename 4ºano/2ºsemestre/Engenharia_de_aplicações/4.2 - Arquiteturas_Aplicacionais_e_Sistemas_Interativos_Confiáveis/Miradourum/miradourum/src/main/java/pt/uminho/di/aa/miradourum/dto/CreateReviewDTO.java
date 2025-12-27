package pt.uminho.di.aa.miradourum.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public class CreateReviewDTO {
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    @NotBlank(message = "Comment is required")
    @Size(max = 500, message = "Comment must not exceed 500 characters")
    private String comment;

    private List<String> images;

    // Getters e Setters
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
}