package pt.uminho.di.aa.miradourum;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pt.uminho.di.aa.miradourum.models.Image;
import pt.uminho.di.aa.miradourum.models.PontoInteresse;
import pt.uminho.di.aa.miradourum.models.Review;
import pt.uminho.di.aa.miradourum.models.User;
import pt.uminho.di.aa.miradourum.repositories.ImageRepository;
import pt.uminho.di.aa.miradourum.repositories.PontoInteresseRepository;
import pt.uminho.di.aa.miradourum.repositories.ReviewRepository;
import pt.uminho.di.aa.miradourum.repositories.UserRepository;
import pt.uminho.di.aa.miradourum.services.PontoInteresseService;
import pt.uminho.di.aa.miradourum.services.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootTest
public class PontoInteresseUnitTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private PontoInteresseService pontoInteresseService;
    @Autowired
    private PontoInteresseRepository pontoInteresseRepository;
    @BeforeEach
    public void setup() {
        // Clear any existing data
        imageRepository.deleteAll();
        reviewRepository.deleteAll();
        userRepository.deleteAll();

        // Add test users
        User user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        // Set other required fields

        User user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        // Set other required fields

        userRepository.save(user1);
        userRepository.save(user2);
        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);
        // Add new PontoInteresse
        //PontoInteresse interesse = new PontoInteresse("40.7128,-74.0060", "vilarinho das furnas", "description", 4, false, true, false, 4.3, LocalDateTime.now(), users, null);
        //PontoInteresse interesse2 = new PontoInteresse("42.7128,-24.0060", "Moreira de conegos", "description", 4, false, true, false, 4.3, LocalDateTime.now(), users, null);
    }

    @Test
    public void testGetById() {
        PontoInteresse interesse = new PontoInteresse(40.7128,-74.0060, "vilarinho das furnas", "description", 4, false, true, false, 4.3, LocalDateTime.now(), null,null,"jorge@gmail.com");
        PontoInteresse interesse2 = new PontoInteresse(42.7128,-24.0060, "Moreira de conegos", "description", 4, false, true, false, 4.3, LocalDateTime.now(), null, null,"jorge@gmail.com");
        pontoInteresseRepository.save(interesse);
        pontoInteresseRepository.save(interesse2);
        PontoInteresse pontoInteresse = pontoInteresseService.getById(interesse.getId(), PontoInteresse.class);
        Assertions.assertEquals(pontoInteresse.getId(), interesse.getId());
    }

    @Test
    public void savePontoInteresse() {
        PontoInteresse interesse3 = new PontoInteresse(42.7128,-24.0060, "bizela", "description", 4, false, true, false, 4.3, LocalDateTime.now(), null, null,"jorge@gmail.com");

        pontoInteresseRepository.save(interesse3);
        PontoInteresse saved = pontoInteresseService.getById(interesse3.getId(), PontoInteresse.class);

        Assertions.assertEquals(saved.getName(),"bizela");


    }
    @Test
    public void getByState(){
        List<PontoInteresse> points = pontoInteresseService.getInactive();

        for(PontoInteresse p : points){
            Assertions.assertEquals(p.getState(),true);
        }
    }
}