package it.epicode.gestioneviaggiaziendali.repository;

import it.epicode.gestioneviaggiaziendali.entity.Booking;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsByEmployeeIdAndTravel_TravelDate(Long employeeId, LocalDate travelDate);
}
