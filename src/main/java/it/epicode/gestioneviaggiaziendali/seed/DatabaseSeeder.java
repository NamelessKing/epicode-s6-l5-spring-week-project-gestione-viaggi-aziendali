package it.epicode.gestioneviaggiaziendali.seed;

import it.epicode.gestioneviaggiaziendali.entity.Booking;
import it.epicode.gestioneviaggiaziendali.entity.Employee;
import it.epicode.gestioneviaggiaziendali.entity.Role;
import it.epicode.gestioneviaggiaziendali.entity.Travel;
import it.epicode.gestioneviaggiaziendali.entity.TravelStatus;
import it.epicode.gestioneviaggiaziendali.repository.BookingRepository;
import it.epicode.gestioneviaggiaziendali.repository.EmployeeRepository;
import it.epicode.gestioneviaggiaziendali.repository.TravelRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DatabaseSeeder implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final TravelRepository travelRepository;
    private final BookingRepository bookingRepository;

    public DatabaseSeeder(EmployeeRepository employeeRepository,
                          TravelRepository travelRepository,
                          BookingRepository bookingRepository) {
        this.employeeRepository = employeeRepository;
        this.travelRepository = travelRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public void run(String... args) {
        if (employeeRepository.count() > 0 || travelRepository.count() > 0 || bookingRepository.count() > 0) {
            return;
        }

        Employee e1 = new Employee("mrossi", "Mario", "Rossi", "m.rossi@example.com", "Password1");
        Employee e2 = new Employee("lbianchi", "Luca", "Bianchi", "l.bianchi@example.com", "Password1");
        Employee e3 = new Employee("gverdi", "Giulia", "Verdi", "g.verdi@example.com", "Password1");
        // Rendo uno degli utenti admin per testare le autorizzazioni
        e1.setRole(Role.ADMIN);
        employeeRepository.saveAll(List.of(e1, e2, e3));

        Travel t1 = new Travel();
        t1.setDestination("Milano");
        t1.setTravelDate(LocalDate.now().plusDays(5));

        Travel t2 = new Travel();
        t2.setDestination("Roma");
        t2.setTravelDate(LocalDate.now().plusDays(10));
        t2.setStatus(TravelStatus.COMPLETATO);

        Travel t3 = new Travel();
        t3.setDestination("Torino");
        t3.setTravelDate(LocalDate.now().plusDays(15));

        travelRepository.saveAll(List.of(t1, t2, t3));

        Booking b1 = new Booking();
        b1.setEmployee(e1);
        b1.setTravel(t1);
        b1.setRequestDate(LocalDateTime.now().minusDays(1));
        b1.setNotes("Finestrino se possibile");

        Booking b2 = new Booking();
        b2.setEmployee(e2);
        b2.setTravel(t1);
        b2.setRequestDate(LocalDateTime.now().minusHours(6));

        Booking b3 = new Booking();
        b3.setEmployee(e1);
        b3.setTravel(t3);
        b3.setRequestDate(LocalDateTime.now().minusHours(2));

        bookingRepository.saveAll(List.of(b1, b2, b3));
    }
}
