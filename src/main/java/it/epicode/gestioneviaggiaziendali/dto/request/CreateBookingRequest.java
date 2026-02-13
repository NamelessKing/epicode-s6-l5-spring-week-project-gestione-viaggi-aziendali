package it.epicode.gestioneviaggiaziendali.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateBookingRequest(
        @NotNull Long employeeId,
        @NotNull Long travelId,
        @Size(max = 500) String notes
) {
}
