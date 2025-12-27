package pt.uminho.di.aa.miradourum.projections.PontoInteresse;

import java.time.LocalDateTime;

public interface PIDetailsShortWithVisitedProjection {
    Long getId();
    Double getLatitude();
    Double getLongitude();
    String getName();
    String getDescription();
    Integer getDifficulty();
    Boolean getAccessibility();
    Boolean getPremium();
    Double getScore();
    LocalDateTime getCreationDate();
    Boolean getState();
    Boolean getVisited();
}