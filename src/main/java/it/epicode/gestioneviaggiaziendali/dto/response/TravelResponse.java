package it.epicode.gestioneviaggiaziendali.dto.response;

import it.epicode.gestioneviaggiaziendali.entity.TravelStatus;
import java.time.LocalDate;

public record TravelResponse(
        Long id,
        String destination,
        LocalDate travelDate,
        TravelStatus status
) {
}
