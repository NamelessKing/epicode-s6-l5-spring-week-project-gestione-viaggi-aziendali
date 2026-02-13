package it.epicode.gestioneviaggiaziendali.repository;

import it.epicode.gestioneviaggiaziendali.entity.Travel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelRepository extends JpaRepository<Travel, Long> {
}
