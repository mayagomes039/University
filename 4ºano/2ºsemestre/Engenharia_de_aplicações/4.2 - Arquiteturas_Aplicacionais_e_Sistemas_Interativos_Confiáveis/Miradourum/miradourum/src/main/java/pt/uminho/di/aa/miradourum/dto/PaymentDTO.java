package pt.uminho.di.aa.miradourum.dto;

import jakarta.validation.constraints.*;

public class PaymentDTO {

    @NotBlank(message = "Número do cartão é obrigatório")
    @Pattern(regexp = "^\\d{13,19}$", message = "Número do cartão deve ter entre 13 e 19 dígitos")
    private String cardNumber;

    @NotBlank(message = "Nome do titular é obrigatório")
    @Size(min = 2, max = 100, message = "Nome do titular deve ter entre 2 e 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s]+$", message = "Nome do titular deve conter apenas letras e espaços")
    private String cardName;

    @NotBlank(message = "CVC é obrigatório")
    @Pattern(regexp = "^\\d{3,4}$", message = "CVC deve ter 3 ou 4 dígitos")
    private String ccv;

    @NotBlank(message = "Data de validade é obrigatória")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "Data de validade deve estar no formato MM/AA")
    private String expiry;

    // Construtores
    public PaymentDTO() {}

    public PaymentDTO(String cardNumber, String cardName, String ccv, String expiry) {
        this.cardNumber = cardNumber;
        this.cardName = cardName;
        this.ccv = ccv;
        this.expiry = expiry;
    }

    // Getters e Setters
    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getCcv() {
        return ccv;
    }

    public void setCcv(String ccv) {
        this.ccv = ccv;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    @Override
    public String toString() {
        return "PaymentDTO{" +
                "cardNumber='" + (cardNumber != null ? cardNumber.replaceAll("\\d(?=\\d{4})", "*") : null) + '\'' +
                ", cardName='" + cardName + '\'' +
                ", ccv='" + (ccv != null ? "***" : null) + '\'' +
                ", expiry='" + expiry + '\'' +
                '}';
    }
}