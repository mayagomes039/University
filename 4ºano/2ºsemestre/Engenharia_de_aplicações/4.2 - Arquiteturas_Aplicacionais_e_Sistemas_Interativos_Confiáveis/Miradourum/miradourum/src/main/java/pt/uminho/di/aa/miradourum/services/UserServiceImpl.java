package pt.uminho.di.aa.miradourum.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pt.uminho.di.aa.miradourum.models.PontoInteresse;
import pt.uminho.di.aa.miradourum.models.User;
import pt.uminho.di.aa.miradourum.repositories.UserRepository;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    // Make this final and initialize once
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        // Use consistent strength (10 is default)
        this.bCryptPasswordEncoder = new BCryptPasswordEncoder(10);
    }

    @Override
    public List<User> getUsers() {
        return (List<User>) userRepository.findAll();
    }

    @Override
    public <T> T getUserById(Long id, Class<T> clazz) {
        return userRepository.findById((long) id, clazz);
    }

    @Override
    public User getUserById(Long id){
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void saveAndUpdateUser(Long id, String username, String password, String image) {
        User user = userRepository.findById(id, User.class);

        if (user == null) {
            throw new RuntimeException("User not found with id: " + id);
        }

        if (username != null && !username.trim().isEmpty()) {
            user.setUsername(username);
        }


        // Only encode if password is actually being changed
        if (password != null && !password.trim().isEmpty()) {
            user.setPassword(encodePassword(password));
        }

        if (image != null) {

            user.setProfileImage(image);
        }

        userRepository.save(user);
    }

    private String encodePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return bCryptPasswordEncoder.encode(password);
    }

    @Override
    public void saveUser(User user) {
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        // Only encode if password is not already encoded
        if (!isPasswordEncoded(user.getPassword())) {
            user.setPassword(encodePassword(user.getPassword()));
        }

        userRepository.save(user);
    }

    // Add a method that doesn't touch the password at all
    @Override
    public void saveUserWithoutPasswordEncoding(User user) {
        userRepository.save(user);
    }

    // Helper method to check if password is already BCrypt encoded
    private boolean isPasswordEncoded(String password) {
        return password != null && password.startsWith("$2a$") && password.length() == 60;
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean checkPassword(String password, String encodedPassword) {
        if (password == null || encodedPassword == null) {
            return false;
        }

        try {
            return bCryptPasswordEncoder.matches(password, encodedPassword);
        } catch (Exception e) {
            // Log the error in production
            System.err.println("Error checking password: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean checkPremium(Long userId) {
        return userRepository.checkPremium(userId);
    }

    @Override
    public boolean checkAdmin(Long userid){
        return userRepository.checkAdmin(userid);
    }
    @Override
    public List<PontoInteresse> getPontosInteresse(Long userId) {
        return userRepository.getPontosInteresse(userId);
    }

    @Override
    public void updatePassword(User user,String rawPassword){
        String encoded = encodePassword(rawPassword);  // Assuming you inject PasswordEncoder
        user.setPassword(encoded);
        userRepository.save(user);
    }

    @Override
    public String getUsernameById(Long userId) {
        return userRepository.findUsernameById(userId);
    }

}