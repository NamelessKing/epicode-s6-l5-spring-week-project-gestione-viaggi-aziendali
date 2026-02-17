package it.epicode.gestioneviaggiaziendali.service;

import it.epicode.gestioneviaggiaziendali.dto.request.CreateEmployeeRequest;
import it.epicode.gestioneviaggiaziendali.dto.request.UpdateEmployeeRequest;
import it.epicode.gestioneviaggiaziendali.dto.response.EmployeeResponse;
import it.epicode.gestioneviaggiaziendali.dto.response.UploadAvatarResponse;
import it.epicode.gestioneviaggiaziendali.entity.Employee;
import it.epicode.gestioneviaggiaziendali.exception.ConflictException;
import it.epicode.gestioneviaggiaziendali.exception.NotFoundException;
import it.epicode.gestioneviaggiaziendali.repository.BookingRepository;
import it.epicode.gestioneviaggiaziendali.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final BookingRepository bookingRepository;

    public EmployeeService(EmployeeRepository employeeRepository, BookingRepository bookingRepository) {
        this.employeeRepository = employeeRepository;
        this.bookingRepository = bookingRepository;
    }

    public Page<EmployeeResponse> findAll(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(this::toResponse);
    }

    public EmployeeResponse findById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee con id " + id + " non trovato"));
        return toResponse(employee);
    }

    public EmployeeResponse create(CreateEmployeeRequest request) {
        if (employeeRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username gi\u00e0 utilizzato");
        }
        if (employeeRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email gi\u00e0 utilizzata");
        }
        Employee employee = new Employee(
                request.username(),
                request.name(),
                request.surname(),
                request.email()
        );
        return toResponse(employeeRepository.save(employee));
    }

    public EmployeeResponse update(Long id, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee con id " + id + " non trovato"));

        if (!employee.getEmail().equalsIgnoreCase(request.email())
                && employeeRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email gi\u00e0 utilizzata");
        }

        employee.setName(request.name());
        employee.setSurname(request.surname());
        employee.setEmail(request.email());
        return toResponse(employeeRepository.save(employee));
    }

    public void delete(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee con id " + id + " non trovato"));
        if (bookingRepository.existsByEmployeeId(id)) {
            throw new ConflictException("Impossibile eliminare il dipendente: esistono prenotazioni collegate");
        }
        employeeRepository.delete(employee);
    }

    public UploadAvatarResponse updateAvatar(Long id, String avatarUrl) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee con id " + id + " non trovato"));
        employee.setAvatarUrl(avatarUrl);
        employeeRepository.save(employee);
        return new UploadAvatarResponse(avatarUrl);
    }

    private EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getUsername(),
                employee.getName(),
                employee.getSurname(),
                employee.getEmail(),
                employee.getAvatarUrl()
        );
    }
}
