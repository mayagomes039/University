package pt.uminho.di.aa.miradourum.projections.PontoInteresse;

import pt.uminho.di.aa.miradourum.models.ImagePontoInteresse;
import pt.uminho.di.aa.miradourum.projections.Review.ReviewProjection;

import java.time.LocalDateTime;
import java.util.List;

public interface PIDetailsFullProjection {
    Long getId();
    double getLatitude();
    double getLongitude();
    String getName();
    String getDescription();
    Integer getDifficulty();
    boolean getAccessibility();
    boolean getPremium();
    double getScore();
    LocalDateTime getCreationDate();
    List<ReviewProjection> getReviews();
    List<ImagePontoInteresse> getImages();
    boolean getState();
}
