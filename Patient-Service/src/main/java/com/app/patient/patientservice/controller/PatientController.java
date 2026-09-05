package com.app.patient.patientservice.controller;

import com.app.patient.patientservice.dto.PatientRequest;
import com.app.patient.patientservice.dto.PatientResponse;
import com.app.patient.patientservice.mapper.PatientMapper;
import com.app.patient.patientservice.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

// REST controller for patient CRUD. Base path: /api/v1/patients.
@Slf4j
@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    // GET / — returns all patients.
    @GetMapping("/")
    public ResponseEntity<List<PatientResponse>> getAllPatients() {
        log.debug("GET /api/v1/patients/ — retrieving all patients");
        List<PatientResponse> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

    // GET /{id} — returns patient by ID, or 404 if not found.
    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable UUID id) {
        log.debug("GET /api/v1/patients/{} — retrieving patient by id", id);
        return patientService.getPatientById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("Patient not found for id: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    // POST / — creates a new patient; validates request body; returns 201 Created.
    @PostMapping("/")
    public ResponseEntity<PatientResponse> createPatient(@Valid @RequestBody PatientRequest request) {
        log.info("POST /api/v1/patients/ — creating patient with email: {}", request.email());
        PatientResponse response = patientService.createPatient(PatientMapper.toEntity(request));
        String email = response.email();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PUT /{id} — updates name/phone/address of an existing patient; returns 200 OK.
    @PutMapping("/update")
    public ResponseEntity<PatientResponse> updatePatient(
            @Valid @RequestBody PatientRequest request) {
        log.info("PUT Patient Request : {}", request);
        PatientResponse response = patientService.updatePatient(PatientMapper.toEntity(request));
        return ResponseEntity.ok(response);
    }

    // DELETE ?email={email} — deletes patient by email query parameter; returns 204 No Content.
    @DeleteMapping
    public ResponseEntity<Void> deletePatient(@RequestParam(required = true) String email) {
        log.info("DELETE /api/v1/patients?email={} — deleting patient by email", email);
        patientService.deletePatient(email.trim());
        return ResponseEntity.noContent().build();
    }
}
