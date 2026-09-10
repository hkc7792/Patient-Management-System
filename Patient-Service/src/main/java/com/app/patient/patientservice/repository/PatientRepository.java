package com.app.patient.patientservice.repository;

import com.app.patient.patientservice.models.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

// JPA repository for Patient; provides standard CRUD plus email-based lookups.
@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    // Returns the patient matching the given email, or empty if none found.
    Optional<Patient> findByEmail(String email);

    // Returns true if a patient with the given email already exists.
    boolean existsByEmail(String email);
}
