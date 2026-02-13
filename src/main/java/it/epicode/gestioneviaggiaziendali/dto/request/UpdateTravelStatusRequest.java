package it.epicode.gestioneviaggiaziendali.dto.request;

import it.epicode.gestioneviaggiaziendali.entity.TravelStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTravelStatusRequest(
        @NotNull TravelStatus status
) {
}
