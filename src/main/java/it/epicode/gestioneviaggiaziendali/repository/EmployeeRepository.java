package it.epicode.gestioneviaggiaziendali.repository;

import it.epicode.gestioneviaggiaziendali.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
