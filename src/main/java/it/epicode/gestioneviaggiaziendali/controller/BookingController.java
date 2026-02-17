package it.epicode.gestioneviaggiaziendali.controller;

import it.epicode.gestioneviaggiaziendali.dto.request.CreateBookingRequest;
import it.epicode.gestioneviaggiaziendali.dto.response.BookingResponse;
import it.epicode.gestioneviaggiaziendali.entity.Employee;
import it.epicode.gestioneviaggiaziendali.service.BookingService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')") // Solo ADMIN può vedere tutte le prenotazioni
    public List<BookingResponse> getAll() {
        return bookingService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')") // Solo ADMIN può leggere una prenotazione specifica
    public BookingResponse getById(@PathVariable Long id) {
        return bookingService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')") // Utenti autenticati possono prenotare
    public BookingResponse create(@AuthenticationPrincipal Employee currentEmployee,
                                  @Valid @RequestBody CreateBookingRequest request) {
        return bookingService.create(request, currentEmployee);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    public List<BookingResponse> getMine(@AuthenticationPrincipal Employee currentEmployee) {
        return bookingService.findMine(currentEmployee);
    }
}
