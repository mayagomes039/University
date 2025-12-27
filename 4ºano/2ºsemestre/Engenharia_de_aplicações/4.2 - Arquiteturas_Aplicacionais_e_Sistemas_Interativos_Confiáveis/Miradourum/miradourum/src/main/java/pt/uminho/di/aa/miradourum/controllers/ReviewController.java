package pt.uminho.di.aa.miradourum.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pt.uminho.di.aa.miradourum.auth.JwtService;
import pt.uminho.di.aa.miradourum.dto.UpdateReviewDTO;
import pt.uminho.di.aa.miradourum.models.Image;
import pt.uminho.di.aa.miradourum.models.Review;
import pt.uminho.di.aa.miradourum.services.ImageService;
import pt.uminho.di.aa.miradourum.services.ReviewService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final JwtService jwtService;
    private final ImageService imageService;
    private final ReviewService reviewService;

    public ReviewController(JwtService jwtService, ImageService imageService, ReviewService reviewService) {
        this.jwtService = jwtService;
        this.imageService = imageService;
        this.reviewService = reviewService;
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(@PathVariable Long reviewId,
                                          @Valid @RequestBody UpdateReviewDTO reviewDTO,
                                          BindingResult bindingResult,
                                          @RequestHeader("Authorization") String authHeader) {

        // Validar token
        ResponseEntity<?> tokenValidation = jwtService.validateToken(authHeader);
        if (tokenValidation != null) {
            return tokenValidation;
        }

        // Extrair userId do token válido
        Long userId = jwtService.extractUserIdFromValidToken(authHeader);

        // Verificar erros de validação do DTO
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            bindingResult.getAllErrors().forEach(error ->
                    errorMessage.append(error.getDefaultMessage()).append("; ")
            );
            return ResponseEntity.badRequest().body(errorMessage.toString());
        }

        // Verificar se review existe
        Optional<Review> optionalReview = reviewService.getById(reviewId);
        if (optionalReview.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Review not found");
        }

        Review review = optionalReview.get();

        // Verificar autorização - utilizador só pode editar as suas próprias reviews
        if (!review.getUserid().equals(userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authorized to edit this review");
        }

        // Atualizar review
        review.setComment(reviewDTO.getComment());
        review.setRating(reviewDTO.getRating());

        reviewService.updateReview(review);
        reviewService.updateAverageScore(review.getPontoInteresse());

        // Tratar imagens (se fornecidas)
        if (reviewDTO.getImages() != null && !reviewDTO.getImages().isEmpty()) {
            List<Image> newImages = new ArrayList<>();

            for (String imageUrl : reviewDTO.getImages()) {
                Image image = imageService.saveImage(imageUrl, review);
                newImages.add(image);
            }

            reviewService.addImages(review.getId(), newImages);
        }

        // Retornar review atualizada
        Review updatedReview = reviewService.getById(review.getId()).get();
        int size = updatedReview.getImages().size(); // Force lazy loading
        return ResponseEntity.ok(updatedReview);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId,
                                          @RequestHeader("Authorization") String authHeader) {

        // Validar token
        ResponseEntity<?> tokenValidation = jwtService.validateToken(authHeader);
        if (tokenValidation != null) {
            return tokenValidation;
        }

        // Extrair userId e role do token válido
        Long userId = jwtService.extractUserIdFromValidToken(authHeader);
        String token = authHeader.substring(7); // Para extrair role, ainda precisamos do token
        Integer role = jwtService.extractRole(token);

        // Verificar se review existe
        Optional<Review> optionalReview = reviewService.getById(reviewId);
        if (optionalReview.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Review not found");
        }

        Review review = optionalReview.get();

        // Verificar autorização - utilizador pode apagar as suas reviews OU admin pode apagar qualquer review
        if (!review.getUserid().equals(userId) && role != 2) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authorized to delete this review");
        }

        reviewService.deleteReviewById(review);
        reviewService.updateAverageScore(review.getPontoInteresse());

        return ResponseEntity.ok("Review deleted successfully");
    }
}