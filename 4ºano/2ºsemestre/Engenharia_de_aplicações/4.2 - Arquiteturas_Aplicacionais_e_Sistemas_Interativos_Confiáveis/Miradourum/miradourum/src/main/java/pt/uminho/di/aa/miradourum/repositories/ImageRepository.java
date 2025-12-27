package pt.uminho.di.aa.miradourum.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.uminho.di.aa.miradourum.models.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {

}
