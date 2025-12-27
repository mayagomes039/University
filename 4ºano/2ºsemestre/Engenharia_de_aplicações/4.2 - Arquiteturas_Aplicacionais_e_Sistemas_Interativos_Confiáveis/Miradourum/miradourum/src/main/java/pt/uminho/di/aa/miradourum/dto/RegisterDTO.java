package pt.uminho.di.aa.miradourum.dto;

import jakarta.validation.constraints.*;

public class RegisterDTO {
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Size(max=80, message = "Email must be at most 80 characters.")
    private String email;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max=30, message = "Password must be between 6 and 30 characters")
    private String password;

    @NotBlank(message = "Profile image is required")
    private String profileimage;

    // Getters e Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getProfileimage() { return profileimage; }
    public void setProfileimage(String profileimage) { this.profileimage = profileimage; }
}
