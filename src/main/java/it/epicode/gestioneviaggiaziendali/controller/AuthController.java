package it.epicode.gestioneviaggiaziendali.controller;

import it.epicode.gestioneviaggiaziendali.dto.request.CreateEmployeeRequest;
import it.epicode.gestioneviaggiaziendali.dto.request.LoginRequest;
import it.epicode.gestioneviaggiaziendali.dto.response.EmployeeResponse;
import it.epicode.gestioneviaggiaziendali.dto.response.LoginResponse;
import it.epicode.gestioneviaggiaziendali.exception.ValidationException;
import it.epicode.gestioneviaggiaziendali.service.AuthService;
import it.epicode.gestioneviaggiaziendali.service.EmployeeService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    // ===== ROADMAP (ordine di lavoro consigliato) =====
    // 1) Configurare Spring Security in modalita stateless (gia fatto in SecurityConfig).
    // 2) Creare questo controller con gli endpoint pubblici /auth/login e /auth/register.
    // 3) Implementare AuthService + JwtTools per generare il token.
    // 4) Aggiungere il filtro JWT per proteggere gli endpoint /api/** (step successivo).

    private final AuthService authService;
    private final EmployeeService employeeService;

    public AuthController(AuthService authService, EmployeeService employeeService) {
        this.authService = authService;
        this.employeeService = employeeService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Validated LoginRequest body) {
        // ===== LOGICA PASSO-PASSO (numerata) =====
        // 1) Ricevo email + password dal client.
        // 2) Le passo al service che:
        //    2.1) recupera l'utente dal DB
        //    2.2) verifica la password
        //    2.3) genera il JWT
        // 3) Ritorno il token al client.

        String token = this.authService.checkCredentialsAndGenerateToken(body);
        return new LoginResponse(token);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse register(@RequestBody @Validated CreateEmployeeRequest payload,
                                     BindingResult validationResult) {
        // ===== LOGICA PASSO-PASSO (numerata) =====
        // 1) @Validated attiva le regole di validazione definite in CreateEmployeeRequest.
        // 2) BindingResult contiene eventuali errori di validazione.
        // 3) Se ci sono errori, li trasformo in lista di messaggi e lancio ValidationException.
        // 4) Se tutto ok, creo l'utente via EmployeeService.

        if (validationResult.hasErrors()) {
            List<String> errors = validationResult.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getDefaultMessage())
                    .toList();

            // Esempio di output: ["email must be a well-formed email address", "password must not be blank"]
            throw new ValidationException(errors);
        }

        // Nota: la response NON contiene la password, per sicurezza.
        return this.employeeService.create(payload);
    }
}
