package it.epicode.gestioneviaggiaziendali.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import it.epicode.gestioneviaggiaziendali.entity.Employee;
import it.epicode.gestioneviaggiaziendali.exception.NotFoundException;
import it.epicode.gestioneviaggiaziendali.service.EmployeeService;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtTools jwtTools;
    private final EmployeeService employeeService;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtFilter(JwtTools jwtTools, EmployeeService employeeService, ObjectMapper objectMapper) {
        this.jwtTools = jwtTools;
        this.employeeService = employeeService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // ===== LOGICA PASSO-PASSO (numerata) =====
        // 1) Questo metodo dice a Spring Security quando SALTARE il filtro.
        // 2) Evitiamo di filtrare gli endpoint pubblici (login/registrazione),
        //    altrimenti si crea un "circolo vizioso":
        //    - per fare login serve un token
        //    - ma per ottenere il token devi fare login
        //
        // Esempio: /auth/login e /auth/register sono pubblici.
        String path = request.getServletPath();
        return pathMatcher.match("/auth/**", path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // ===== LOGICA PASSO-PASSO (numerata) =====
        // 1) Leggo l'header Authorization.
        // 2) Verifico che esista e abbia formato "Bearer <token>".
        // 3) Estraggo il token rimuovendo "Bearer ".
        // 4) Verifico il token (firma + scadenza) con JwtTools.
        // 5) Se valido, creo un Authentication e lo metto nel SecurityContext.
        // 6) Passo la richiesta al filtro successivo.
        // 7) Se non valido, rispondo 401 e NON faccio proseguire la chain.

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Token mancante o formato errato.
            // Esempio corretto: Authorization: Bearer eyJhbGciOi...
            writeUnauthorized(response, request.getRequestURI(),
                    "Token mancante o formato non valido", "MISSING_TOKEN");
            return;
        }

        String token = authHeader.substring(7); // rimuove "Bearer "

        try {
            // 4) Verifico firma e scadenza del token
            jwtTools.verifyTokenAndGetClaims(token);

            // 5) Estraggo l'id dal token (subject) e carico l'utente dal DB
            Long employeeId = jwtTools.extractIdFromToken(token);
            Employee authenticatedEmployee = employeeService.findEntityById(employeeId);

            // 6) Creo un Authentication completo (principal = Employee, authorities = ruoli)
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            authenticatedEmployee,
                            null,
                            authenticatedEmployee.getAuthorities()
                    );

            // Aggiungo dettagli della richiesta (IP, session id, ecc.)
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 7) Salvo nel contesto di sicurezza (da qui in poi l'utente e "autenticato")
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 8) Passo al prossimo filtro/endpoint
            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException | NotFoundException ex) {
            // Tutte le eccezioni di JJWT finiscono qui: token scaduto, firma invalida, token malformato, ecc.
            writeUnauthorized(response, request.getRequestURI(),
                    "Token non valido o scaduto", "INVALID_TOKEN");
        }
    }

    private void writeUnauthorized(HttpServletResponse response,
                                   String path,
                                   String detail,
                                   String errorCode) throws IOException {
        // ===== NOTA IMPORTANTE =====
        // Le eccezioni lanciate nei filtri NON passano automaticamente dal GlobalExceptionHandler.
        // Quindi qui costruiamo manualmente una risposta coerente con ProblemDetail.

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problem.setTitle("Unauthorized");
        problem.setDetail(detail);
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("path", path);
        problem.setProperty("errorCode", errorCode);

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
