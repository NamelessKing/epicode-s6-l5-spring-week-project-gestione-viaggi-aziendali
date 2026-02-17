package it.epicode.gestioneviaggiaziendali.service;

import it.epicode.gestioneviaggiaziendali.dto.request.CreateTravelRequest;
import it.epicode.gestioneviaggiaziendali.dto.request.UpdateTravelRequest;
import it.epicode.gestioneviaggiaziendali.dto.request.UpdateTravelStatusRequest;
import it.epicode.gestioneviaggiaziendali.dto.response.TravelResponse;
import it.epicode.gestioneviaggiaziendali.entity.Travel;
import it.epicode.gestioneviaggiaziendali.exception.ConflictException;
import it.epicode.gestioneviaggiaziendali.exception.NotFoundException;
import it.epicode.gestioneviaggiaziendali.repository.BookingRepository;
import it.epicode.gestioneviaggiaziendali.repository.TravelRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TravelService {

    private final TravelRepository travelRepository;
    private final BookingRepository bookingRepository;

    public TravelService(TravelRepository travelRepository, BookingRepository bookingRepository) {
        this.travelRepository = travelRepository;
        this.bookingRepository = bookingRepository;
    }

    public Page<TravelResponse> findAll(Pageable pageable) {
        return travelRepository.findAll(pageable).map(this::toResponse);
    }

    public TravelResponse findById(Long id) {
        Travel travel = travelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Travel con id " + id + " non trovato"));
        return toResponse(travel);
    }

    public TravelResponse create(CreateTravelRequest request) {
        Travel travel = new Travel();
        travel.setDestination(request.destination());
        travel.setTravelDate(request.travelDate());
        if (request.status() != null) {
            travel.setStatus(request.status());
        }
        return toResponse(travelRepository.save(travel));
    }

    public TravelResponse update(Long id, UpdateTravelRequest request) {
        Travel travel = travelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Travel con id " + id + " non trovato"));
        travel.setDestination(request.destination());
        travel.setTravelDate(request.travelDate());
        return toResponse(travelRepository.save(travel));
    }

    public TravelResponse updateStatus(Long id, UpdateTravelStatusRequest request) {
        Travel travel = travelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Travel con id " + id + " non trovato"));
        travel.setStatus(request.status());
        return toResponse(travelRepository.save(travel));
    }

    public void delete(Long id) {
        Travel travel = travelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Travel con id " + id + " non trovato"));
        if (bookingRepository.existsByTravelId(id)) {
            throw new ConflictException("Impossibile eliminare il viaggio: esistono prenotazioni collegate");
        }
        travelRepository.delete(travel);
    }

    private TravelResponse toResponse(Travel travel) {
        return new TravelResponse(
                travel.getId(),
                travel.getDestination(),
                travel.getTravelDate(),
                travel.getStatus()
        );
    }
}
