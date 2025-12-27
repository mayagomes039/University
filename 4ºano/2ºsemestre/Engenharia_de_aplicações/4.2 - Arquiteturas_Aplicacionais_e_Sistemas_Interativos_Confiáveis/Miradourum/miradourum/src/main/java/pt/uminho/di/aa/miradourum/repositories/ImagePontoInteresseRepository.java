package pt.uminho.di.aa.miradourum.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import pt.uminho.di.aa.miradourum.models.ImagePontoInteresse;

public interface ImagePontoInteresseRepository extends JpaRepository<ImagePontoInteresse, Long> {
    @Transactional
    void deleteAllByPontoInteresseId(Long pontoInteresseId);
}

