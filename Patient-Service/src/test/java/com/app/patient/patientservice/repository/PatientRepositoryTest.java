package com.app.patient.patientservice.repository;

import com.app.patient.patientservice.models.Patient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

// Data-layer slice tests for PatientRepository.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PatientRepositoryTest {

    @Autowired
    private PatientRepository patientRepository;

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Patient buildPatient(String name, String email) {
        return Patient.builder()
                .name(name)
                .email(email)
                .phone("555-0100")
                .address("123 Test Street")
                .birthDate(LocalDate.of(1990, 6, 15))
                .build();
    }

    // -----------------------------------------------------------------------
    // findByEmail
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("findByEmail — should return patient when email exists")
    void findByEmail_existingEmail_returnsPatient() {
        // Arrange
        Patient patient = buildPatient("Alice Smith", "alice@example.com");
        patientRepository.save(patient);

        // Act
        Optional<Patient> result = patientRepository.findByEmail("alice@example.com");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Alice Smith");
        assertThat(result.get().getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    @DisplayName("findByEmail — should return empty Optional when email does not exist")
    void findByEmail_nonExistentEmail_returnsEmpty() {
        // Act
        Optional<Patient> result = patientRepository.findByEmail("nobody@example.com");

        // Assert
        assertThat(result).isEmpty();
    }

    // -----------------------------------------------------------------------
    // existsByEmail
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("existsByEmail — should return true when patient with that email exists")
    void existsByEmail_existingEmail_returnsTrue() {
        // Arrange
        Patient patient = buildPatient("Bob Jones", "bob@example.com");
        patientRepository.save(patient);

        // Act & Assert
        assertThat(patientRepository.existsByEmail("bob@example.com")).isTrue();
    }

    @Test
    @DisplayName("existsByEmail — should return false when no patient with that email exists")
    void existsByEmail_nonExistentEmail_returnsFalse() {
        // Act & Assert
        assertThat(patientRepository.existsByEmail("ghost@example.com")).isFalse();
    }
}
