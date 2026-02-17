package it.epicode.gestioneviaggiaziendali.repository;

import it.epicode.gestioneviaggiaziendali.entity.Booking;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsByEmployeeIdAndTravel_TravelDate(Long employeeId, LocalDate travelDate);
    boolean existsByTravelId(Long travelId);
    boolean existsByEmployeeId(Long employeeId);
    List<Booking> findByEmployeeId(Long employeeId);
}
