package it.epicode.gestioneviaggiaziendali.service;

import it.epicode.gestioneviaggiaziendali.dto.request.LoginRequest;
import it.epicode.gestioneviaggiaziendali.entity.Employee;
import it.epicode.gestioneviaggiaziendali.exception.UnauthorizedException;
import it.epicode.gestioneviaggiaziendali.security.JwtTools;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final EmployeeService employeeService;
    private final JwtTools jwtTools;

    public AuthService(EmployeeService employeeService, JwtTools jwtTools) {
        this.employeeService = employeeService;
        this.jwtTools = jwtTools;
    }

    public String checkCredentialsAndGenerateToken(LoginRequest body) {
        // ===== LOGICA PASSO-PASSO (numerata) =====
        // 1) Recupero l'utente (Employee) dal database in base all'email fornita.
        //    - Se non esiste, EmployeeService lancia NotFoundException -> verra gestita a livello globale.
        Employee found = this.employeeService.findByEmail(body.email());

        // 2) Verifico la password.
        //    - In questo progetto (didattico) la password e in chiaro.
        //    - In un progetto reale si usa sempre hashing (es. BCrypt).
        if (!found.getPassword().equals(body.password())) {
            // 2.1) Se la password non coincide, rifiuto con 401 Unauthorized.
            throw new UnauthorizedException("Credenziali errate");
        }

        // 3) Se credenziali OK, genero il JWT.
        //    - Dentro il token mettiamo solo informazioni non sensibili (id, username, email).
        String accessToken = jwtTools.generateToken(found);

        // 4) Ritorno il token al controller, che lo invia al client.
        return accessToken;
    }
}
