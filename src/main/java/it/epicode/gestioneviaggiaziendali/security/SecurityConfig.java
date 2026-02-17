package it.epicode.gestioneviaggiaziendali.security;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity // Attiva l'infrastruttura di Spring Security per questa applicazione
@EnableMethodSecurity // Necessario per usare @PreAuthorize sui metodi/controller
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        // Questo Bean costruisce la "catena di filtri" che intercetta TUTTE le richieste HTTP
        // prima che arrivino ai controller. Ogni filtro può:
        // - lasciar passare la richiesta
        // - bloccarla con un errore (401/403)
        //
        // In questa fase iniziale vogliamo solo disattivare i default e lasciare tutto aperto,
        // perché poi inseriremo un filtro JWT custom che farà il vero controllo.

        // Disabilita il login via form HTML (non usiamo pagine server-side).
        // Esempio di default indesiderato: GET /login mostra una pagina HTML.
        http.formLogin(form -> form.disable());

        // Disabilita l'autenticazione HTTP Basic (username/password in header ad ogni richiesta).
        // Con JWT non serve, perché il client userà: Authorization: Bearer <token>
        http.httpBasic(basic -> basic.disable());

        // Disabilita CSRF: utile per form e cookie-based sessioni.
        // Con JWT stateless non usiamo cookie di sessione, quindi la protezione CSRF
        // non è necessaria e spesso blocca le chiamate da frontend (React/Postman).
        http.csrf(csrf -> csrf.disable());

        // Imposta lo stato su STATELESS: Spring Security NON crea né salva sessioni.
        // Ogni richiesta deve essere autenticata in modo indipendente (token).
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Endpoint pubblici e protetti:
        // - /auth/** => pubblici (login/registrazione)
        // - /api/**  => protetti (serve token valido)
        // - il resto resta libero (es. root o actuator se presente)
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
        );

        // Inserisco il filtro JWT prima del filtro standard di login/password.
        // Così intercetto il token, lo verifico e valorizzo il SecurityContext.
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        // Attiva CORS usando la configurazione del bean CorsConfigurationSource.
        // Senza questa riga, la config CORS non viene applicata da Spring Security.
        http.cors(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt e lo standard raccomandato da Spring per hashare le password
        // 12 e un valore comune per bilanciare sicurezza e performance
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Whitelist degli origin frontend autorizzati a chiamare questa API
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));

        // Metodi e header consentiti (per demo: tutti; in produzione restringere)
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
