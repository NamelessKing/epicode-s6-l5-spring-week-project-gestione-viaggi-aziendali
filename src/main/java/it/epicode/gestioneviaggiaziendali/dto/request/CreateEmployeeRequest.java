package it.epicode.gestioneviaggiaziendali.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateEmployeeRequest(
        @NotBlank @Size(min = 3, max = 30) String username,
        @NotBlank @Size(min = 2, max = 40) String name,
        @NotBlank @Size(min = 2, max = 40) String surname,
        @NotBlank @Email String email,
        @NotBlank
        @Size(min = 4)
        @Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{4,}$")
        String password
) {
}
