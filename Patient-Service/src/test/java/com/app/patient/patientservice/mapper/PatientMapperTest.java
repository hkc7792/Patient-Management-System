package com.app.patient.patientservice.mapper;

import com.app.patient.patientservice.dto.PatientRequest;
import com.app.patient.patientservice.dto.PatientResponse;
import com.app.patient.patientservice.models.Patient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Plain JUnit 5 unit tests for {@link PatientMapper}.
 *
 * <p>No Spring context is loaded — these tests exercise only the static
 * mapping logic and run as fast, isolated unit tests.
 */
class PatientMapperTest {

    // -----------------------------------------------------------------------
    // toResponse(Patient)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("toResponse — should map all five fields from Patient to PatientResponse")
    void toResponse_allFieldsMappedCorrectly() {
        // Arrange
        UUID id = UUID.randomUUID();
        Patient patient = Patient.builder()
                .id(id)
                .name("Clara Perez")
                .email("clara@example.com")
                .phone("555-0200")
                .address("456 Maple Ave")
                .birthDate(LocalDate.of(1985, 3, 22))
                .build();

        // Act
        PatientResponse response = PatientMapper.toResponse(patient);

        // Assert
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.name()).isEqualTo("Clara Perez");
        assertThat(response.email()).isEqualTo("clara@example.com");
        assertThat(response.phone()).isEqualTo("555-0200");
        assertThat(response.address()).isEqualTo("456 Maple Ave");
    }

    @Test
    @DisplayName("toResponse — nullable fields (phone, address) should be preserved as null")
    void toResponse_nullOptionalFields_remainsNull() {
        // Arrange
        Patient patient = Patient.builder()
                .id(UUID.randomUUID())
                .name("Dan Brown")
                .email("dan@example.com")
                .phone(null)
                .address(null)
                .birthDate(LocalDate.of(1992, 11, 5))
                .build();

        // Act
        PatientResponse response = PatientMapper.toResponse(patient);

        // Assert
        assertThat(response.phone()).isNull();
        assertThat(response.address()).isNull();
    }

    // -----------------------------------------------------------------------
    // toEntity(PatientRequest)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("toEntity — should map all fields from PatientRequest to Patient entity")
    void toEntity_allFieldsMappedCorrectly() {
        // Arrange
        LocalDate birthDate = LocalDate.of(1988, 7, 10);
        LocalDate regDate = LocalDate.of(2023, 1, 15);
        PatientRequest request = new PatientRequest(
                "Eva Green",
                "eva@example.com",
                "555-0300",
                "789 Oak Lane",
                birthDate,
                regDate
        );

        // Act
        Patient patient = PatientMapper.toEntity(request);

        // Assert
        assertThat(patient.getName()).isEqualTo("Eva Green");
        assertThat(patient.getEmail()).isEqualTo("eva@example.com");
        assertThat(patient.getPhone()).isEqualTo("555-0300");
        assertThat(patient.getAddress()).isEqualTo("789 Oak Lane");
        assertThat(patient.getBirthDate()).isEqualTo(birthDate);
        assertThat(patient.getRegDate()).isEqualTo(regDate);
    }

    @Test
    @DisplayName("toEntity — id should be null (assigned by DB on persist)")
    void toEntity_idIsNull() {
        // Arrange
        PatientRequest request = new PatientRequest(
                "Frank Castle",
                "frank@example.com",
                null,
                null,
                LocalDate.of(1975, 1, 20),
                null
        );

        // Act
        Patient patient = PatientMapper.toEntity(request);

        // Assert — id must not be set by the mapper; the DB generates it
        assertThat(patient.getId()).isNull();
    }

    @Test
    @DisplayName("toEntity — nullable fields (phone, address, regDate) should be preserved as null")
    void toEntity_nullOptionalFields_remainsNull() {
        // Arrange
        PatientRequest request = new PatientRequest(
                "Grace Hopper",
                "grace@example.com",
                null,
                null,
                LocalDate.of(1906, 12, 9),
                null
        );

        // Act
        Patient patient = PatientMapper.toEntity(request);

        // Assert
        assertThat(patient.getPhone()).isNull();
        assertThat(patient.getAddress()).isNull();
        assertThat(patient.getRegDate()).isNull();
    }
}
