package it.epicode.gestioneviaggiaziendali.controller;

import it.epicode.gestioneviaggiaziendali.dto.request.CreateEmployeeRequest;
import it.epicode.gestioneviaggiaziendali.dto.request.UpdateEmployeeRequest;
import it.epicode.gestioneviaggiaziendali.dto.response.EmployeeResponse;
import it.epicode.gestioneviaggiaziendali.dto.response.UploadAvatarResponse;
import it.epicode.gestioneviaggiaziendali.service.CloudinaryService;
import it.epicode.gestioneviaggiaziendali.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
    public Page<EmployeeResponse> getAll(Pageable pageable) {
        return employeeService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public EmployeeResponse getById(@PathVariable Long id) {
        return employeeService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse create(@Valid @RequestBody CreateEmployeeRequest request) {
        return employeeService.create(request);
    }

    @PutMapping("/{id}")
    public EmployeeResponse update(@PathVariable Long id, @Valid @RequestBody UpdateEmployeeRequest request) {
        return employeeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        employeeService.delete(id);
    }

    @PostMapping("/{id}/avatar")
    public UploadAvatarResponse uploadAvatar(@PathVariable Long id, @RequestParam("avatar") MultipartFile avatar) {
        String url = cloudinaryService.uploadImage(avatar);
        return employeeService.updateAvatar(id, url);
    }
}
