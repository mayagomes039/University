package pt.uminho.di.aa.miradourum.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.uminho.di.aa.miradourum.models.PontoInteresse;
import pt.uminho.di.aa.miradourum.models.Review;
import pt.uminho.di.aa.miradourum.models.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.email = :email")
    User findByEmail(@Param("email") String email);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email")
    boolean existsByEmail(@Param("email") String email);

    @Query("SELECT u.pontoInteresse FROM User u WHERE u.id = :userId")
    List<PontoInteresse> getPontosInteresse(@Param("userId") Long userId);

    @Query("""
    SELECT r FROM Review r
    WHERE r.pontoInteresse IN (
        SELECT u.pontoInteresse FROM User u WHERE u.id = :userId
    )
    AND r.userid = :userId
    """)
    List<Review> getUserReviewsOnVisitedPontos(@Param("userId") Long userId);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.id = :userId AND u.role = 3")
    boolean checkPremium(@Param("userId") Long userId);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.id = :userId AND u.role = 2")
    boolean checkAdmin(@Param("userId") Long userId);

    <T> T findById(Long id, Class<T> clazz);

    @Query("SELECT u.username FROM User u WHERE u.id = :userId")
    String findUsernameById(@Param("userId") Long userId);
}