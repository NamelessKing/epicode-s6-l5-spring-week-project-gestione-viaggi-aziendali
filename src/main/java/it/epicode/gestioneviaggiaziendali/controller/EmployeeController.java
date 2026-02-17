package it.epicode.gestioneviaggiaziendali.controller;

import it.epicode.gestioneviaggiaziendali.dto.request.CreateEmployeeRequest;
import it.epicode.gestioneviaggiaziendali.dto.request.UpdateEmployeeRequest;
import it.epicode.gestioneviaggiaziendali.dto.response.EmployeeResponse;
import it.epicode.gestioneviaggiaziendali.dto.response.UploadAvatarResponse;
import it.epicode.gestioneviaggiaziendali.entity.Employee;
import it.epicode.gestioneviaggiaziendali.service.CloudinaryService;
import it.epicode.gestioneviaggiaziendali.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final CloudinaryService cloudinaryService;

    public EmployeeController(EmployeeService employeeService, CloudinaryService cloudinaryService) {
        this.employeeService = employeeService;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')") // Solo ADMIN può vedere tutti i dipendenti
    public Page<EmployeeResponse> getAll(Pageable pageable) {
        return employeeService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')") // Solo ADMIN può leggere il profilo di altri
    public EmployeeResponse getById(@PathVariable Long id) {
        return employeeService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ADMIN')") // Solo ADMIN può creare nuovi dipendenti
    public EmployeeResponse create(@Valid @RequestBody CreateEmployeeRequest request) {
        return employeeService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')") // Solo ADMIN può modificare altri dipendenti
    public EmployeeResponse update(@PathVariable Long id, @Valid @RequestBody UpdateEmployeeRequest request) {
        return employeeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ADMIN')") // Solo ADMIN può eliminare dipendenti
    public void delete(@PathVariable Long id) {
        employeeService.delete(id);
    }

    @PostMapping("/{id}/avatar")
    @PreAuthorize("hasAuthority('ADMIN')") // Solo ADMIN può cambiare avatar di altri
    public UploadAvatarResponse uploadAvatar(@PathVariable Long id, @RequestParam("avatar") MultipartFile avatar) {
        String url = cloudinaryService.uploadImage(avatar);
        return employeeService.updateAvatar(id, url);
    }

    // ===== ENDPOINT "ME" (ANTI-IDOR) =====
    // Questi endpoint permettono all'utente autenticato di accedere al proprio profilo
    // SENZA passare l'id nel path, evitando che un utente possa operare su altri.

    @GetMapping("/me")
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    public EmployeeResponse getMe(@AuthenticationPrincipal Employee currentEmployee) {
        return employeeService.findById(currentEmployee.getId());
    }

    @PutMapping("/me")
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    public EmployeeResponse updateMe(@AuthenticationPrincipal Employee currentEmployee,
                                     @Valid @RequestBody UpdateEmployeeRequest request) {
        return employeeService.update(currentEmployee.getId(), request);
    }

    @PostMapping("/me/avatar")
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    public UploadAvatarResponse uploadMyAvatar(@AuthenticationPrincipal Employee currentEmployee,
                                               @RequestParam("avatar") MultipartFile avatar) {
        String url = cloudinaryService.uploadImage(avatar);
        return employeeService.updateAvatar(currentEmployee.getId(), url);
    }
}
