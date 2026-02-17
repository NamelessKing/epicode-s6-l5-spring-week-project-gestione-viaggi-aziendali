package it.epicode.gestioneviaggiaziendali.controller;

import it.epicode.gestioneviaggiaziendali.dto.request.CreateTravelRequest;
import it.epicode.gestioneviaggiaziendali.dto.request.UpdateTravelRequest;
import it.epicode.gestioneviaggiaziendali.dto.request.UpdateTravelStatusRequest;
import it.epicode.gestioneviaggiaziendali.dto.response.TravelResponse;
import it.epicode.gestioneviaggiaziendali.service.TravelService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/travels")
public class TravelController {

    private final TravelService travelService;

    public TravelController(TravelService travelService) {
        this.travelService = travelService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')") // Tutti gli utenti autenticati possono leggere i viaggi
    public Page<TravelResponse> getAll(Pageable pageable) {
        return travelService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')") // Tutti gli utenti autenticati possono leggere i viaggi
    public TravelResponse getById(@PathVariable Long id) {
        return travelService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ADMIN')") // Solo ADMIN può creare viaggi
    public TravelResponse create(@Valid @RequestBody CreateTravelRequest request) {
        return travelService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')") // Solo ADMIN può modificare viaggi
    public TravelResponse update(@PathVariable Long id, @Valid @RequestBody UpdateTravelRequest request) {
        return travelService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN')") // Solo ADMIN può cambiare lo stato del viaggio
    public TravelResponse updateStatus(@PathVariable Long id,
                                       @Valid @RequestBody UpdateTravelStatusRequest request) {
        return travelService.updateStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ADMIN')") // Solo ADMIN può eliminare viaggi
    public void delete(@PathVariable Long id) {
        travelService.delete(id);
    }
}
