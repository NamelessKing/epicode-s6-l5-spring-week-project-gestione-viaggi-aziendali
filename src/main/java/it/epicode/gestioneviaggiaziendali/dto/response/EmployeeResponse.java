package it.epicode.gestioneviaggiaziendali.dto.response;

public record EmployeeResponse(
        Long id,
        String username,
        String name,
        String surname,
        String email,
        String avatarUrl
) {
}
