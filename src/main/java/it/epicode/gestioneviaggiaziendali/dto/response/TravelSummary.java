package it.epicode.gestioneviaggiaziendali.dto.response;

import java.time.LocalDate;

public record TravelSummary(
        Long id,
        String destination,
        LocalDate travelDate
) {
}
