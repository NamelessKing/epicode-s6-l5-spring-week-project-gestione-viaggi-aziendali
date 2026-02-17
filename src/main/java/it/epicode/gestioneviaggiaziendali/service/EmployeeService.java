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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository,
                           BookingRepository bookingRepository,
                           PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.bookingRepository = bookingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<EmployeeResponse> findAll(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(this::toResponse);
    }

    public EmployeeResponse findById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee con id " + id + " non trovato"));
        return toResponse(employee);
    }

    public Employee findEntityById(Long id) {
        // Metodo interno usato dal filtro JWT per ottenere l'entity completa (con ruolo e password)
        return employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee con id " + id + " non trovato"));
    }

    public EmployeeResponse create(CreateEmployeeRequest request) {
        // 1) Controllo vincoli di unicita (username/email).
        // 2) Creo entity Employee valorizzando anche la password (per login JWT).
        // 3) Salvo e ritorno la response senza password.
        if (employeeRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username gia utilizzato");
        }
        if (employeeRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email gia utilizzata");
        }
        Employee employee = new Employee(
                request.username(),
                request.name(),
                request.surname(),
                request.email(),
                passwordEncoder.encode(request.password())
        );
        return toResponse(employeeRepository.save(employee));
    }

    public EmployeeResponse update(Long id, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee con id " + id + " non trovato"));

        if (!employee.getEmail().equalsIgnoreCase(request.email())
                && employeeRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email gia utilizzata");
        }

        employee.setName(request.name());
        employee.setSurname(request.surname());
        employee.setEmail(request.email());
        return toResponse(employeeRepository.save(employee));
    }

    public Employee findByEmail(String email) {
        // Usato dal login: serve l'entity completa (con password) per verificare le credenziali.
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Employee con email " + email + " non trovato"));
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
