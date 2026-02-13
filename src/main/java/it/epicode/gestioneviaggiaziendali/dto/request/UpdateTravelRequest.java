package it.epicode.gestioneviaggiaziendali.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdateTravelRequest(
        @NotBlank @Size(min = 2, max = 100) String destination,
        @NotNull LocalDate travelDate
) {
}
