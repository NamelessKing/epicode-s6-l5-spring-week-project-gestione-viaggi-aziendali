package it.epicode.gestioneviaggiaziendali.dto.response;

import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        LocalDateTime requestDate,
        String notes,
        EmployeeSummary employee,
        TravelSummary travel
) {
}
