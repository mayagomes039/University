package pt.uminho.di.aa.miradourum.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.uminho.di.aa.miradourum.models.PontoInteresse;
import pt.uminho.di.aa.miradourum.models.Review;
import pt.uminho.di.aa.miradourum.projections.PontoInteresse.PIDetailsShortWithVisitedProjection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PontoInteresseRepository extends JpaRepository<PontoInteresse, Long> {
    @Query("SELECT p FROM PontoInteresse p WHERE p.state = true")
    <T> List<T> findByState(Class<T> type);

    @Query("SELECT p FROM PontoInteresse p WHERE p.state = false")
    List<PontoInteresse> findIncative();

    @Query("""
        SELECT p FROM PontoInteresse p 
        WHERE (:score IS NULL OR p.score >= :score)
           AND (:date IS NULL OR p.creationDate >= :date)
           AND (:accessibility IS NULL OR p.accessibility = :accessibility)
           AND (:difficulty IS NULL OR p.difficulty = :difficulty)
           AND (:visitantes IS NULL OR 
            (SELECT COUNT(u) FROM User u JOIN u.pontoInteresse pi WHERE pi = p) >= :visitantes)
           AND p.latitude BETWEEN :minLat AND :maxLat
           AND p.longitude BETWEEN :minLon AND :maxLon
    """)
    List<PontoInteresse> findFilteredWithBox(
            @Param("score") Double score,
            @Param("date") LocalDateTime date,
            @Param("accessibility") Boolean accessibility,
            @Param("difficulty") Integer difficulty,
            @Param("visitantes") Integer visitantes,
            @Param("minLat") Double minLat,
            @Param("maxLat") Double maxLat,
            @Param("minLon") Double minLon,
            @Param("maxLon") Double maxLon
    );

    @Query("SELECT r FROM Review r WHERE r.pontoInteresse = :pontoInteresse")
    List<Review> findReviews(@Param("pontoInteresse") PontoInteresse pontoInteresse);

    <T> Optional<T> findById(Long id, Class<T> type);

    @Query("""
    SELECT p.id as id, p.latitude as latitude, p.longitude as longitude, 
           p.name as name, p.description as description, p.difficulty as difficulty, 
           p.accessibility as accessibility, p.premium as premium, p.score as score, 
           p.creationDate as creationDate, p.state as state,
           CASE WHEN r.id IS NOT NULL THEN true ELSE false END as visited
    FROM PontoInteresse p 
    LEFT JOIN p.reviews r ON r.userid = :userId
    WHERE p.state = true
    AND (:minScore IS NULL OR p.score >= :minScore)
    AND (:minCreationDate IS NULL OR p.creationDate >= :minCreationDate)
    AND (:accessibility IS NULL OR p.accessibility = :accessibility)
    AND (:maxDifficulty IS NULL OR p.difficulty <= :maxDifficulty)
    AND (:userLat IS NULL OR :userLng IS NULL OR :maxDistance IS NULL OR 
         (6371 * acos(cos(radians(:userLat)) * cos(radians(p.latitude)) * 
          cos(radians(p.longitude) - radians(:userLng)) + 
          sin(radians(:userLat)) * sin(radians(p.latitude)))) <= :maxDistance)
    """)
    List<PIDetailsShortWithVisitedProjection> findAllActiveWithFilters(
            @Param("userId") Long userId,
            @Param("minScore") Double minScore,
            @Param("minCreationDate") LocalDateTime minCreationDate,
            @Param("accessibility") Boolean accessibility,
            @Param("maxDifficulty") Integer maxDifficulty,
            @Param("userLat") Double userLatitude,
            @Param("userLng") Double userLongitude,
            @Param("maxDistance") Double maxDistance
    );
}