package it.epicode.gestioneviaggiaziendali.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        // Definiamo esplicitamente il bean per evitare errori di autoconfigurazione.
        // Spring Boot di solito lo crea da solo, ma qui lo forziamo per il JwtFilter.
        // findAndRegisterModules() registra automaticamente il supporto Java Time
        // (Instant, LocalDate, LocalDateTime) se e presente jackson-datatype-jsr310.
        return new ObjectMapper().findAndRegisterModules();
    }
}
