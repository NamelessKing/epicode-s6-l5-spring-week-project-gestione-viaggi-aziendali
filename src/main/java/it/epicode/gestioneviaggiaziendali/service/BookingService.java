package it.epicode.gestioneviaggiaziendali.service;

import it.epicode.gestioneviaggiaziendali.dto.request.CreateBookingRequest;
import it.epicode.gestioneviaggiaziendali.dto.response.BookingResponse;
import it.epicode.gestioneviaggiaziendali.dto.response.EmployeeSummary;
import it.epicode.gestioneviaggiaziendali.dto.response.TravelSummary;
import it.epicode.gestioneviaggiaziendali.entity.Booking;
import it.epicode.gestioneviaggiaziendali.entity.Employee;
import it.epicode.gestioneviaggiaziendali.entity.Role;
import it.epicode.gestioneviaggiaziendali.entity.Travel;
import it.epicode.gestioneviaggiaziendali.exception.ConflictException;
import it.epicode.gestioneviaggiaziendali.exception.NotFoundException;
import it.epicode.gestioneviaggiaziendali.repository.BookingRepository;
import it.epicode.gestioneviaggiaziendali.repository.EmployeeRepository;
import it.epicode.gestioneviaggiaziendali.repository.TravelRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EmployeeRepository employeeRepository;
    private final TravelRepository travelRepository;

    public BookingService(BookingRepository bookingRepository,
                          EmployeeRepository employeeRepository,
                          TravelRepository travelRepository) {
        this.bookingRepository = bookingRepository;
        this.employeeRepository = employeeRepository;
        this.travelRepository = travelRepository;
    }

    public List<BookingResponse> findAll() {
        return bookingRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public BookingResponse findById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking con id " + id + " non trovata"));
        return toResponse(booking);
    }

    public List<BookingResponse> findMine(Employee currentEmployee) {
        // Ritorna solo le prenotazioni dell'utente autenticato
        return bookingRepository.findByEmployeeId(currentEmployee.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public BookingResponse create(CreateBookingRequest request, Employee currentEmployee) {
        // ===== ANTI-IDOR =====
        // Se l'utente NON e admin, IGNORIAMO l'employeeId nel payload e usiamo l'id del principal.
        // In questo modo un utente non puo prenotare viaggi per conto di altri.
        boolean isAdmin = currentEmployee.getRole() == Role.ADMIN;
        Long effectiveEmployeeId = isAdmin ? request.employeeId() : currentEmployee.getId();

        Employee employee = employeeRepository.findById(effectiveEmployeeId)
                .orElseThrow(() -> new NotFoundException("Employee con id " + effectiveEmployeeId + " non trovato"));
        Travel travel = travelRepository.findById(request.travelId())
                .orElseThrow(() -> new NotFoundException("Travel con id " + request.travelId() + " non trovato"));

        LocalDate travelDate = travel.getTravelDate();
        if (bookingRepository.existsByEmployeeIdAndTravel_TravelDate(employee.getId(), travelDate)) {
            throw new ConflictException("Employee gia prenotato per il giorno " + travelDate);
        }

        Booking booking = new Booking();
        booking.setEmployee(employee);
        booking.setTravel(travel);
        booking.setRequestDate(LocalDateTime.now());
        booking.setNotes(request.notes());

        return toResponse(bookingRepository.save(booking));
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getRequestDate(),
                booking.getNotes(),
                new EmployeeSummary(booking.getEmployee().getId(), booking.getEmployee().getUsername()),
                new TravelSummary(
                        booking.getTravel().getId(),
                        booking.getTravel().getDestination(),
                        booking.getTravel().getTravelDate()
                )
        );
    }
}
