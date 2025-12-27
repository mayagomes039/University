package pt.uminho.di.aa.miradourum.controllers;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.uminho.di.aa.miradourum.auth.AuthService;
import pt.uminho.di.aa.miradourum.auth.JwtService;
import pt.uminho.di.aa.miradourum.dto.LoginDTO;
import pt.uminho.di.aa.miradourum.dto.RegisterDTO;
import pt.uminho.di.aa.miradourum.models.User;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService,JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginDTO loginDTO,
                                        BindingResult bindingResult,
                                        HttpServletResponse response) {
        try {
            // Verificar erros de validação
            if (bindingResult.hasErrors()) {
                StringBuilder errorMessage = new StringBuilder();
                bindingResult.getAllErrors().forEach(error ->
                        errorMessage.append(error.getDefaultMessage()).append("; ")
                );
                return ResponseEntity.badRequest().body(errorMessage.toString());
            }

            String token = authService.login(loginDTO.getEmail(), loginDTO.getPassword());

            if (token == null) {
                return ResponseEntity.status(401).body("Invalid email or password");
            }

            return ResponseEntity.ok(token);

        } catch (Exception ex) {
            return ResponseEntity.status(401).body(ex.getMessage());
        }
    }


    @PostMapping("/register")
    public ResponseEntity<Object> registerUser(@Valid @RequestBody RegisterDTO registerDTO,
                                               BindingResult bindingResult,
                                               HttpServletResponse response) {
        try {
            // Verificar erros de validação
            if (bindingResult.hasErrors()) {
                StringBuilder errorMessage = new StringBuilder();
                bindingResult.getAllErrors().forEach(error ->
                        errorMessage.append(error.getDefaultMessage()).append("; ")
                );
                return ResponseEntity.badRequest().body(errorMessage.toString());
            }

            User created = authService.register(
                    registerDTO.getEmail(),
                    registerDTO.getUsername(),
                    registerDTO.getPassword(),
                    1, // Role padrão
                    registerDTO.getProfileimage()
            );

            if (created == null) {
                return ResponseEntity.status(401).body("Email already exists");
            } else {
                return ResponseEntity.ok("User registered successfully");
            }

        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Error: " + ex.getMessage());
        }
    }
}
