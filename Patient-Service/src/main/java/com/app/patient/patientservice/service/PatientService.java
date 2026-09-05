package com.app.patient.patientservice.service;

import com.app.patient.patientservice.dto.PatientResponse;
import com.app.patient.patientservice.mapper.PatientMapper;
import com.app.patient.patientservice.models.Patient;
import com.app.patient.patientservice.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

// Service layer for Patient CRUD operations. Responses are mapped to PatientResponse via PatientMapper.
@Slf4j
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    // Saves a new patient. Throws IllegalArgumentException if the email already exists.
    @Transactional
    public PatientResponse createPatient(Patient patient) {
        log.info("Creating patient with email: {}", patient.getEmail());

        if (patientRepository.existsByEmail(patient.getEmail())) {
            log.warn("Attempt to create patient with duplicate email: {}", patient.getEmail());
            throw new IllegalArgumentException(
                    "A patient with email '" + patient.getEmail() + "' already exists.");
        }

        Patient saved = patientRepository.save(patient);
        log.info("Patient created successfully with id: {}", saved.getId());
        return PatientMapper.toResponse(saved);
    }

    // Returns patient by ID, or empty Optional if not found.
    @Transactional(readOnly = true)
    public Optional<PatientResponse> getPatientById(UUID id) {
        log.debug("Fetching patient by id: {}", id);
        return patientRepository.findById(id).map(PatientMapper::toResponse);
    }

    // Returns all patients as a list of PatientResponse DTOs.
    @Transactional(readOnly = true)
    public List<PatientResponse> getAllPatients() {
        log.debug("Fetching all patients");
        return patientRepository.findAll().stream()
                .map(PatientMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Updates name, phone, and address of an existing patient. Throws NoSuchElementException if not found.
    @Transactional
    public PatientResponse updatePatient(UUID id, Patient updatedData) {
        log.info("Updating patient with id: {}", id);

        Patient existing = patientRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Update failed — patient not found with id: {}", id);
                    return new NoSuchElementException("Patient not found with id: " + id);
                });

        existing.setName(updatedData.getName());
        existing.setPhone(updatedData.getPhone());
        existing.setAddress(updatedData.getAddress());

        Patient updated = patientRepository.save(existing);
        log.info("Patient updated successfully with id: {}", updated.getId());
        return PatientMapper.toResponse(updated);
    }

    // Deletes patient by ID. Throws NoSuchElementException if not found.
    @Transactional
    public void deletePatient(UUID id) {
        log.info("Deleting patient with id: {}", id);

        if (!patientRepository.existsById(id)) {
            log.warn("Delete failed — patient not found with id: {}", id);
            throw new NoSuchElementException("Patient not found with id: " + id);
        }

        patientRepository.deleteById(id);
        log.info("Patient deleted successfully with id: {}", id);
    }
}
