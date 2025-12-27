package pt.uminho.di.aa.miradourum.projections.PontoInteresse;

import java.time.LocalDateTime;

public interface PIDetailsShortProjection {
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
    boolean getState();

}
