package it.epicode.gestioneviaggiaziendali.security;

import it.epicode.gestioneviaggiaziendali.entity.Employee;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTools {

    // ===== CONFIGURAZIONE DA application.properties =====
    // jwt.secret -> chiave segreta usata per firmare e verificare il token
    // jwt.expirationms -> durata del token in millisecondi
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expirationms}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        // 1) Converto la stringa in bytes.
        // 2) Creo la chiave HMAC (HS256/HS512). La chiave deve essere abbastanza lunga.
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Employee employee) {
        // ===== LOGICA PASSO-PASSO (numerata) =====
        // 1) Imposto data di emissione (iat) e data di scadenza (exp).
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtExpirationMs);

        // 2) Preparo le claims NON sensibili da inserire nel token.
        //    - NON inserire password o dati personali sensibili.
        //    - Qui mettiamo solo ID/username/email.
        //    Esempio di payload:
        //    {
        //      "sub": "10",
        //      "username": "mrossi",
        //      "email": "m.rossi@example.com",
        //      "iat": 1700000000,
        //      "exp": 1700086400
        //    }
        return Jwts.builder()
                .subject(employee.getId().toString())
                .claim("username", employee.getUsername())
                .claim("email", employee.getEmail())
                .issuedAt(now)
                .expiration(expiration)
                // 3) Firma del token con chiave segreta (HS256 implicito in base alla key).
                .signWith(getSigningKey())
                // 4) Serializzazione finale in stringa compatta "header.payload.signature".
                .compact();
    }

    public Claims verifyTokenAndGetClaims(String token) {
        // ===== LOGICA PASSO-PASSO (numerata) =====
        // 1) Verifico firma e scadenza del token usando la stessa chiave segreta.
        // 2) Se il token e valido, ottengo le Claims (payload).
        // 3) Se e invalido/scaduto/manomesso, JJWT lancia eccezioni (da gestire nel filtro).
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long extractIdFromToken(String token) {
        // Estraiamo il subject (che contiene l'id utente come stringa)
        String subject = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        return Long.valueOf(subject);
    }
}
