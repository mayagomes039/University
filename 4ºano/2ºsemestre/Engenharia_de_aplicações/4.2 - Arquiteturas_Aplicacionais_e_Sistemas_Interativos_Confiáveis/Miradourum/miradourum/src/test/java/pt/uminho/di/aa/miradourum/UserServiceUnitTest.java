package pt.uminho.di.aa.miradourum;

import org.junit.jupiter.api.Assertions;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pt.uminho.di.aa.miradourum.models.Image;
import pt.uminho.di.aa.miradourum.models.PontoInteresse;
import pt.uminho.di.aa.miradourum.models.Review;
import pt.uminho.di.aa.miradourum.models.User;
import pt.uminho.di.aa.miradourum.projections.User.UserProfileProjection;
import pt.uminho.di.aa.miradourum.repositories.ImageRepository;
import pt.uminho.di.aa.miradourum.repositories.PontoInteresseRepository;
import pt.uminho.di.aa.miradourum.repositories.ReviewRepository;
import pt.uminho.di.aa.miradourum.repositories.UserRepository;
import pt.uminho.di.aa.miradourum.services.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootTest
public class UserServiceUnitTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private PontoInteresseRepository pontoInteresseRepository;
    @Autowired
    private ImageRepository imageRepository;

    @BeforeEach
    public void setup() {
        imageRepository.deleteAll();
        reviewRepository.deleteAll();
        // Clear any existing data
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
    }

    @Test
    public void testGetUsers() {
        List<User> users = userService.getUsers();
        Assertions.assertEquals(2, users.size());
    }

    @Test
    public void testGetUserbyId() {

        Date date = new Date();
        User user = new User("quim", "quim@mail.com","pass", 1, "image", date);
        userRepository.save(user);

        User user2 = (User) userService.getUserById(user.getId(), UserProfileProjection.class);

        Assertions.assertEquals(user.getId(),user2.getId());
    }

    @Test
    public void testGetUserbyEmail() {
        User user = userService.getUserByEmail("user2@example.com");
        Assertions.assertEquals("user2", user.getUsername());
    }

    @Test
    public void testSaveUpdateUser() {

        User user = new User();
        user.setUsername("user3");
        user.setPassword("password");

        User user1 = userService.getUserByEmail("user2@example.com");
        user1.setUsername("user4");

        userService.saveUser(user);
        userRepository.save(user1);

        List<User> users = userService.getUsers();

        Assertions.assertEquals(3, users.size());
        Assertions.assertEquals("user4", userService.getUserByEmail("user2@example.com").getUsername());
    }

    @Test
    public void testEmailExists() {
        Assertions.assertTrue(userService.emailExists("user2@example.com"));
    }

    @Test
    public void testPasswordCheck() {
        User user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user5@example.com");
        user1.setPassword("password");
        userService.saveUser(user1);

        String storedHash = userService.getUserByEmail("user5@example.com").getPassword();
        Assertions.assertTrue(bCryptPasswordEncoder.matches("password", storedHash));

    }

    @Test
    public void testGetPontosInteresse(){
        User user1 = userService.getUserByEmail("user2@example.com");
        Long id = user1.getId();
        userService.getPontosInteresse(id);
        List<PontoInteresse> l = new ArrayList<>();
        PontoInteresse pt = new PontoInteresse();
        pt.setCreationDate(LocalDateTime.now());
        l.add(pt);
        pontoInteresseRepository.save(pt);
        user1.setPontoInteresse(l);
        userRepository.save(user1);

        Assertions.assertTrue(userService.getPontosInteresse(id).size() > 0);
    }

    @Test
    public void testGetImagesURL(){
        User user = new User();
        user.setUsername("user10");
        user.setPassword("password");
        user.setEmail("asd");

        userService.saveUser(user);

        // Create and assign a ponto
        PontoInteresse ponto = new PontoInteresse();
        ponto.setName("Test Spot");
        ponto.setCreationDate(LocalDateTime.now());

        List<User> users = new ArrayList<>();
        users.add(user);
        ponto.setUserList(users);
        pontoInteresseRepository.save(ponto); // only if not in-memory

        user.setPontoInteresse(List.of(ponto));
        userRepository.save(user);

        Review rev = new Review();
        rev.setUserid(user.getId());
        rev.setPontoInteresse(ponto); // this is key

        reviewRepository.save(rev);

        Image img = new Image();
        img.setUrl("asd");
        img.setReview(rev);

        imageRepository.save(img);  // only if not cascade

        rev.setImages(List.of(img));

        reviewRepository.save(rev);


        //List<Image> urls = userService.getImages(user.getId());

        //Assertions.assertNotNull(urls);
        
    }

    @Test
    public void testCheckPremium(){
        User user = new User();
        user.setUsername("user1");
        user.setPassword("password");
        user.setRole(1);

        userService.saveUser(user);

        Assertions.assertTrue(userService.checkPremium(user.getId()));
    }

}