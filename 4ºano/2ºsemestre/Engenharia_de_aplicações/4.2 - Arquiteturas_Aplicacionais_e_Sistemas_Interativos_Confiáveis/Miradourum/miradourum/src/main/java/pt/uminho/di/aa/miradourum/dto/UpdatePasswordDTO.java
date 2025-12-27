package pt.uminho.di.aa.miradourum.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdatePasswordDTO {
    @NotBlank(message = "Password cannot be empty")
    private String newPassword;

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
