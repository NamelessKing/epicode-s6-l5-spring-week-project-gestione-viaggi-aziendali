package it.epicode.gestioneviaggiaziendali.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateBookingRequest(
        // ATTENZIONE: per utenti non-admin questo campo viene ignorato (anti-IDOR).
        // L'id effettivo viene preso dal principal autenticato.
        @NotNull Long employeeId,
        @NotNull Long travelId,
        @Size(max = 500) String notes
) {
}
