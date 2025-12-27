package pt.uminho.di.aa.miradourum.dto;

public class ResetPasswordDTO {
    @jakarta.validation.constraints.Email(message = "Email deve ser válido")
    @jakarta.validation.constraints.NotBlank(message = "Email é obrigatório")
    private String email;

    @jakarta.validation.constraints.NotBlank(message = "Password é obrigatória")
    @jakarta.validation.constraints.Size(min = 6, message = "Password deve ter pelo menos 6 caracteres")
    private String newPassword;

    // getters and setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}