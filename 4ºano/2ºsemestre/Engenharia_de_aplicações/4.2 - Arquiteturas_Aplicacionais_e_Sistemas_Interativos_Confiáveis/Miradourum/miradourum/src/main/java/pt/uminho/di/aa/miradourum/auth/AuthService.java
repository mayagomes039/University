package pt.uminho.di.aa.miradourum.auth;

import org.springframework.stereotype.Service;
import pt.uminho.di.aa.miradourum.models.User;
import pt.uminho.di.aa.miradourum.services.UserService;

@Service
public class AuthService {
    private JwtService jwtService;
    private UserService userService;

    public AuthService(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    public String login(String email, String password) {
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return null;
        }
        if (!userService.checkPassword(password, user.getPassword())) {
            return null;
        }

        return jwtService.generateToken(user);
    }

    public User register(String email, String username, String password, Integer role,String profileImgUrl) {
        if (userService.emailExists(email)) {
            return null;
        }
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        user.setProfileImage(profileImgUrl);
        userService.saveUser(user);
        return user;
    }

    public User updateProfile(Long userId, String username, String password, String profileImgUrl) {
        User user = userService.getUserById(userId);
        if (user == null) {
            return null;
        }

        // Hash the password before saving
        if (username != null && !username.trim().isEmpty()) {
            user.setUsername(username);
        }
        if (password != null && !password.trim().isEmpty()) {
            user.setPassword(password); // Make sure to encode password
        }

        if (profileImgUrl != null && !profileImgUrl.trim().isEmpty()) {
            user.setProfileImage(profileImgUrl);
        }
        userService.saveAndUpdateUser(userId,username,password,profileImgUrl);
        return user;
    }

}
