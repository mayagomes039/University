package pt.uminho.di.aa.miradourum.dto;

import jakarta.validation.constraints.*;

public class ActivatePontoInteresseDTO {
    @NotNull(message = "Campo 'accepted' é obrigatório")
    private Boolean accepted;

    private String comment;

    // Getters e Setters
    public Boolean getAccepted() { return accepted; }
    public void setAccepted(Boolean accepted) { this.accepted = accepted; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    // Validação custom
    @AssertTrue(message = "Campo 'comment' é obrigatório quando accepted=false")
    private boolean isCommentValidWhenRejected() {
        return accepted == null || accepted || (comment != null && !comment.trim().isEmpty());
    }
}