package pt.uminho.di.aa.miradourum.dto;

public class PaymentResponseDTO {
    private boolean success;
    private boolean active;
    private String message;
    private String expiryDate;
    private String error;

    // Construtores
    public PaymentResponseDTO() {}

    // Getters e Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}