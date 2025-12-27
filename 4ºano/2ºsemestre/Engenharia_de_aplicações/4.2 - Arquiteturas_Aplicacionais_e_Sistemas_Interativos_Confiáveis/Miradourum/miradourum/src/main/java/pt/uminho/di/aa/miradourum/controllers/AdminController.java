package pt.uminho.di.aa.miradourum.controllers;


import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pt.uminho.di.aa.miradourum.auth.JwtService;
import pt.uminho.di.aa.miradourum.dto.ActivatePontoInteresseDTO;
import pt.uminho.di.aa.miradourum.models.PontoInteresse;
import pt.uminho.di.aa.miradourum.models.User;
import pt.uminho.di.aa.miradourum.services.PontoInteresseService;
import pt.uminho.di.aa.miradourum.services.UserService;
import pt.uminho.di.aa.miradourum.utils.EmailService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final JwtService jwtService;
    private final UserService userService;
    private final PontoInteresseService pontoInteresseService;
    private final EmailService emailService;

    public AdminController(JwtService jwtService, UserService userService, PontoInteresseService pontoInteresseService,EmailService emailService) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.pontoInteresseService = pontoInteresseService;
        this.emailService = emailService;
    }

    //retorna todos os pontos de interesse que estão inativos
    @GetMapping("/pi")
    public ResponseEntity<?> getInactive(@RequestHeader("Authorization") String authHeader) {
        // 1. Check if token is provided
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
        }

        String token = authHeader.substring(7); // Remove "Bearer "

        try {
            // 2. Check if token is expired
            if (jwtService.tokenExpired(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token expired");
            }

            // 3. getRole
            Integer role = jwtService.extractRole(token);
            if (role != 2) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not admin");
            }

            // 4. Fetch all inactive interest points
            List<PontoInteresse> pontosInativos = pontoInteresseService.getInactive();
            return ResponseEntity.ok(pontosInativos);


        } catch (Exception e) {
            // Handle token-related exceptions
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or tampered token");
        }
    }

    //altera o estado de um ponto de interesse para ativo
    @PutMapping("/pi/{id}")
    public ResponseEntity<Object> activatePoint(@PathVariable Long id,
                                                @Valid @RequestBody ActivatePontoInteresseDTO dto,
                                                BindingResult bindingResult,
                                                @RequestHeader("Authorization") String authHeader) {

        // Verificar erros de validação primeiro
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
        }

        String token = authHeader.substring(7);

        try {
            if (jwtService.tokenExpired(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token expired");
            }

            Integer role = jwtService.extractRole(token);
            if (role != 2) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not admin");
            }

            Optional<PontoInteresse> optionalPonto = pontoInteresseService.getByIdComplete(id);
            if (optionalPonto.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ponto de interesse not found");
            }

            PontoInteresse ponto = optionalPonto.get();

            if (ponto.getState() == true) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Ponto de interesse already active");
            }

            if (dto.getAccepted()) {
                ponto.setState(true);
                pontoInteresseService.savePontoInteresse(ponto);
                return ResponseEntity.ok().body("Ponto interesse activated!");
            } else {
                pontoInteresseService.deletePontoInteresse(ponto);
                emailService.sendEmail(ponto.getCreatorEmail(), dto.getComment());
                return ResponseEntity.ok().body("Ponto interesse removed successfully and email sent to user.");
            }

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token expired");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @GetMapping("/isadmin")
    public ResponseEntity<?> isAdmin(@RequestHeader("Authorization") String authHeader) {
        System.out.println(java.time.ZoneId.systemDefault());

        // Validar token
        ResponseEntity<?> tokenValidation = jwtService.validateToken(authHeader);
        if (tokenValidation != null) {
            return (ResponseEntity<Object>) tokenValidation;
        }

        // Extrair userId do token válido
        Long userId = jwtService.extractUserIdFromValidToken(authHeader);

        return ResponseEntity.ok(userService.checkAdmin(userId));

        //return ResponseEntity.ok(userService.checkPremium(userId));
    }
}
