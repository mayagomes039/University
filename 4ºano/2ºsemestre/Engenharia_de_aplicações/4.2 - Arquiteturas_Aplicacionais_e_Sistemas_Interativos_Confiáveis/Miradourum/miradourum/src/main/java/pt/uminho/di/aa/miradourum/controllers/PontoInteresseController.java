package pt.uminho.di.aa.miradourum.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pt.uminho.di.aa.miradourum.auth.JwtService;
import pt.uminho.di.aa.miradourum.dto.*;
import pt.uminho.di.aa.miradourum.models.*;
import pt.uminho.di.aa.miradourum.projections.PontoInteresse.PIDetailsFullProjection;
import pt.uminho.di.aa.miradourum.projections.PontoInteresse.PIDetailsShortProjection;
import pt.uminho.di.aa.miradourum.projections.PontoInteresse.PIDetailsShortWithVisitedProjection;
import pt.uminho.di.aa.miradourum.services.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/pi")
public class PontoInteresseController {

    private final PontoInteresseService pontoInteresseService;
    private final JwtService jwtService;
    private final ImageService imageService;
    private final ReviewService reviewService;
    private final ImagePontoInteresseService imagePontoInteresseService;
    private final PontoInteresseServiceImpl pontoInteresseServiceImpl;
    private final UserService userService;
    public PontoInteresseController(PontoInteresseService pontoInteresseService,UserService userService, JwtService jwtService, ImageService imageService, ReviewService reviewService, ImagePontoInteresseService imagePontoInteresseService, PontoInteresseServiceImpl pontoInteresseServiceImpl) {
        this.pontoInteresseService = pontoInteresseService;
        this.jwtService = jwtService;
        this.imageService = imageService;
        this.reviewService = reviewService;
        this.imagePontoInteresseService=imagePontoInteresseService;
        this.pontoInteresseServiceImpl = pontoInteresseServiceImpl;
        this.userService = userService;
    }



    //Exemplo de uso no frontend (JSON do body)
    /*
    {
        "maxDistance": 15.0,           // Distância máxima em km (default: 20)
        "minScore": 4.0,               // Classificação mínima (default: null - qualquer)
        "minCreationDate": "2024-01-01T00:00:00",  // Data mínima de adição (default: null)
        "accessibility": true,          // Acessibilidade (default: null - qualquer)
        "maxDifficulty": 3,            // Dificuldade máxima (default: null - qualquer)
        "userLatitude": 41.5518,       // Latitude do usuário (para cálculo de distância)
        "userLongitude": -8.4229       // Longitude do usuário (para cálculo de distância)
    }
    */
    @PostMapping("/filtered")
    public ResponseEntity<?> getAllActivePontosFiltered(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody(required = false) PIFilterDTO filters) {

        // 1. Validar token
        ResponseEntity<?> tokenValidation = jwtService.validateToken(authHeader);
        if (tokenValidation != null) {
            return tokenValidation; // Retorna erro se token inválido
        }

        Long userId = jwtService.extractUserIdFromValidToken(authHeader);

        // 2. Aplicar valores default se não fornecidos
        if (filters == null) {
            filters = new PIFilterDTO();
        }

        // Valores default
        if (filters.getMaxDistance() == null) {
            filters.setMaxDistance(20.0); // 20km default
        }

        // 3. Buscar pontos com filtros
        List<PIDetailsShortWithVisitedProjection> pontos =
                pontoInteresseService.getAllActiveWithFilters(userId, filters);

        return ResponseEntity.ok(pontos);
    }

    @PostMapping
    public ResponseEntity<?> createPonto(@Valid @RequestBody CreatePontoInteresseDTO pontoDTO,
                                         BindingResult bindingResult,
                                         @RequestHeader("Authorization") String authHeader) {

        // 1. Validar token
        ResponseEntity<?> tokenValidation = jwtService.validateToken(authHeader);
        if (tokenValidation != null) {
            return tokenValidation;
        }

        Long userId = jwtService.extractUserIdFromValidToken(authHeader);

        // 2. Verificar erros de validação
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            bindingResult.getAllErrors().forEach(error ->
                    errorMessage.append(error.getDefaultMessage()).append("; ")
            );
            return ResponseEntity.badRequest().body(errorMessage.toString());
        }

        // 3. Criar PontoInteresse
        PontoInteresse pontoInteresse = new PontoInteresse(
                pontoDTO.getLatitude(),
                pontoDTO.getLongitude(),
                pontoDTO.getName(),
                pontoDTO.getDescription(),
                pontoDTO.getDificulty(),
                pontoDTO.getAccessibility(),
                false, // state sempre false inicialmente
                pontoDTO.getPremium(),
                0.0, // rating inicial
                LocalDateTime.now(),
                pontoDTO.getCreatorEmail()
        );

        pontoInteresseService.savePontoInteresse(pontoInteresse);

        // 4. Adicionar imagens
        List<ImagePontoInteresse> images = new ArrayList<>();
        for (String imageUrl : pontoDTO.getImageUrls()) {
            ImagePontoInteresse image = imagePontoInteresseService.saveImage(imageUrl, pontoInteresse);
            images.add(image);
        }

        pontoInteresseService.addImages(pontoInteresse.getId(), images);

        return ResponseEntity.ok(pontoInteresse);
    }

    @PostMapping("/{id}/reviews")
    public ResponseEntity<?> createReviewOnPonto(@PathVariable Long id,
                                                 @Valid @RequestBody CreateReviewDTO reviewDTO,
                                                 BindingResult bindingResult,
                                                 @RequestHeader("Authorization") String authHeader) {

        // 1. Validar token
        ResponseEntity<?> tokenValidation = jwtService.validateToken(authHeader);
        if (tokenValidation != null) {
            return tokenValidation;
        }

        Long userId = jwtService.extractUserIdFromValidToken(authHeader);

        // 2. Verificar erros de validação
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            bindingResult.getAllErrors().forEach(error ->
                    errorMessage.append(error.getDefaultMessage()).append("; ")
            );
            return ResponseEntity.badRequest().body(errorMessage.toString());
        }

        // 3. Verificar se ponto existe e está ativo
        Optional<PontoInteresse> optionalPonto = pontoInteresseService.getByIdComplete(id);
        if (optionalPonto.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ponto de interesse not found");
        }

        PontoInteresse point = optionalPonto.get();
        if (!point.getState()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Point is still inactive.");
        }

        // 4. Criar Review
        LocalDateTime creationDate = LocalDateTime.now();

        Review rev = reviewService.saveReview(
                reviewDTO.getRating(),
                reviewDTO.getComment(),
                creationDate,
                userId,
                point
        );

        // 5. Adicionar imagens
        List<Image> images = new ArrayList<>();
        for (String imageUrl : reviewDTO.getImages()) {
            Image image = imageService.saveImage(imageUrl, rev);
            images.add(image);
        }

        reviewService.addImages(rev.getId(), images);

        // 6. Atualizar relacionamentos
        point.addReview(rev);
        pontoInteresseService.savePontoInteresse(point);
        reviewService.updateAverageScore(point);


        User user = userService.getUserById(userId);
        if (!user.getPontoInteresse().contains(point)) {
            user.getPontoInteresse().add(point);
            userService.saveUserWithoutPasswordEncoding(user);
        }

        return ResponseEntity.ok(point);
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<?> getReviewsOnPonto(@PathVariable Long id,
                                               @RequestHeader("Authorization") String authHeader) {

        // 1. Validar token
        ResponseEntity<?> tokenValidation = jwtService.validateToken(authHeader);
        if (tokenValidation != null) {
            return tokenValidation;
        }

        Long userId = jwtService.extractUserIdFromValidToken(authHeader);

        Optional<PontoInteresse> optionalPonto = pontoInteresseService.getByIdComplete(id);
        if (optionalPonto.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ponto de interesse not found");
        }

        PontoInteresse point = optionalPonto.get();

        if (!point.getState()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Point is still inactive.");
        }

        List<Review> reviews = point.getReviews();

        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/shortdetails/{id}")
    public ResponseEntity<?> getPontoInteresseShortDetails(@PathVariable("id") String id,
                                                           @RequestHeader("Authorization") String authHeader) {
        // 1. Validar token
        ResponseEntity<?> tokenValidation = jwtService.validateToken(authHeader);
        if (tokenValidation != null) {
            return tokenValidation;
        }

        Long userId = jwtService.extractUserIdFromValidToken(authHeader);

        User user =  userService.getUserById(userId);
        Integer userrole = user.getRole();
        PIDetailsShortProjection ponto = pontoInteresseService.getById(Long.valueOf(id), PIDetailsShortProjection.class);

        if (ponto == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("PI not found");

        // Ponto ainda está inativo
        if (!ponto.getState()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Ponto is inactive.");
        }

        if (ponto.getPremium() && (userrole == null || (userrole != 2 && userrole != 3))) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // or use .body("Conteúdo premium")
        }

        return ResponseEntity.ok(ponto);
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<?> getPontoInteresseFullDetails(@PathVariable("id") String id,
                                                          @RequestHeader("Authorization") String authHeader) {
        // 1. Validar token
        ResponseEntity<?> tokenValidation = jwtService.validateToken(authHeader);
        if (tokenValidation != null) {
            return tokenValidation;
        }
        Long userId = jwtService.extractUserIdFromValidToken(authHeader);
        User user =  userService.getUserById(userId);
        Integer userrole = user.getRole();

        PIDetailsFullProjection ponto = pontoInteresseService.getById(Long.valueOf(id), PIDetailsFullProjection.class);
        if (ponto == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("PI not found");

        if (!ponto.getState()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Ponto is inactive.");
        }

        if (ponto.getPremium() && (userrole == null || (userrole != 2 && userrole != 3))) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // or use .body("Conteúdo premium")
        }
        // Converter para DTO e substituir userIds por usernames
        PIDetailsFullDTO dto = new PIDetailsFullDTO(ponto);

        List<ReviewDTO> reviewsWithUsername = ponto.getReviews().stream()
                .map(review -> {
                    String username = userService.getUsernameById(review.getUserid());
                    return new ReviewDTO(review, username);
                })
                .collect(Collectors.toList());

        dto.setReviews(reviewsWithUsername);

        return ResponseEntity.ok(dto);
    }

}
