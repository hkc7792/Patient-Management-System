package com.app.patient.patientservice.controller;

import com.app.patient.patientservice.dto.PatientRequest;
import com.app.patient.patientservice.dto.PatientResponse;
import com.app.patient.patientservice.mapper.PatientMapper;
import com.app.patient.patientservice.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Patient Controller", description = "Endpoints for managing patient records (CRUD operations)")
public class PatientController {

    private final PatientService patientService;

    // GET / — returns all patients.
    @Operation(summary = "Retrieve all patients", description = "Fetches a list of all registered patient records in the database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all patients")
    })
    @GetMapping("/")
    public ResponseEntity<List<PatientResponse>> getAllPatients() {
        log.debug("GET /api/v1/patients/ — retrieving all patients");
        List<PatientResponse> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

    // GET /{id} — returns patient by ID, or 404 if not found.
    @Operation(summary = "Get patient by ID", description = "Retrieves details of a single patient matching the specified UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patient record found"),
            @ApiResponse(responseCode = "404", description = "Patient record not found for the given UUID")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatientById(
            @Parameter(description = "UUID of the patient to retrieve", required = true)
            @PathVariable UUID id) {
        log.debug("GET /api/v1/patients/{} — retrieving patient by id", id);
        return patientService.getPatientById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("Patient not found for id: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    // POST / — creates a new patient; validates request body; returns 201 Created.
    @Operation(summary = "Create a new patient", description = "Registers a new patient with validated name, email, birth date, and contact details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Patient record successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or duplicate email address")
    })
    @PostMapping("/")
    public ResponseEntity<PatientResponse> createPatient(@Valid @RequestBody PatientRequest request) {
        log.info("POST /api/v1/patients/ — creating patient with email: {}", request.email());
        PatientResponse response = patientService.createPatient(PatientMapper.toEntity(request));
        String email = response.email();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PUT /update — updates name/phone/address of an existing patient; returns 200 OK.
    @Operation(summary = "Update an existing patient", description = "Updates details (name, phone, address, birthDate) of an existing patient matched by email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patient record successfully updated"),
            @ApiResponse(responseCode = "404", description = "Patient record not found for the specified email"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
    @PutMapping("/update")
    public ResponseEntity<PatientResponse> updatePatient(
            @Valid @RequestBody PatientRequest request) {
        log.info("PUT Patient Request : {}", request);
        PatientResponse response = patientService.updatePatient(PatientMapper.toEntity(request));
        return ResponseEntity.ok(response);
    }

    // DELETE ?email={email} — deletes patient by email query parameter; returns 204 No Content.
    @Operation(summary = "Delete patient by email", description = "Deletes a patient record matching the specified email address.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Patient record successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Patient record not found for the specified email")
    })
    @DeleteMapping
    public ResponseEntity<Void> deletePatient(
            @Parameter(description = "Email address of the patient to delete", required = true)
            @RequestParam(required = true) String email) {
        log.info("DELETE /api/v1/patients?email={} — deleting patient by email", email);
        patientService.deletePatient(email.trim());
        return ResponseEntity.noContent().build();
    }
}

