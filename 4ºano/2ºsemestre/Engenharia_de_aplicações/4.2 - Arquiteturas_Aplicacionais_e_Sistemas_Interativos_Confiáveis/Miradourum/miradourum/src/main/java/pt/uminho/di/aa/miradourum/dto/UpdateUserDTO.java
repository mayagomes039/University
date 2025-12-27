package pt.uminho.di.aa.miradourum.dto;

import jakarta.validation.constraints.Size;

public class UpdateUserDTO {
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username; // Opcional

    @Size(min = 6, max=30, message = "Password must be between 6 and 30 characters")
    private String password; // Opcional

    private String profileImage; // Opcional (note: mantive o nome igual ao que usavas)

    // Getters e Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    // Método helper para verificar se pelo menos um campo foi fornecido
    public boolean hasAtLeastOneField() {
        return (username != null && !username.trim().isEmpty()) ||
                (password != null && !password.trim().isEmpty()) ||
                (profileImage != null && !profileImage.trim().isEmpty());
    }
}