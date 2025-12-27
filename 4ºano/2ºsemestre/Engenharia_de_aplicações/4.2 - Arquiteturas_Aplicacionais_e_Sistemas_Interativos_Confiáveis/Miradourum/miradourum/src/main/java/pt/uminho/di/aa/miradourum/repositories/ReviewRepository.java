package pt.uminho.di.aa.miradourum.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import pt.uminho.di.aa.miradourum.models.Review;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Transactional
    void deleteAllByPontoInteresseId(Long pontoInteresseId);
    List<Review> findAllByUserid(Long userid);
}

