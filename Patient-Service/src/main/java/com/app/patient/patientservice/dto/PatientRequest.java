package com.app.patient.patientservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

// Inbound request DTO for creating or updating a patient. Validated before reaching the service layer.
@Schema(description = "Inbound payload for patient creation and updates")
public record PatientRequest(
        @Schema(description = "Full name of the patient", example = "John Doe")
        @NotBlank String name,

        @Schema(description = "Unique email address of the patient", example = "john.doe@example.com")
        @NotBlank @Email String email,

        @Schema(description = "Contact phone number", example = "+1234567890")
        String phone,

        @Schema(description = "Residential address", example = "123 Main St, Anytown, USA")
        String address,

        @Schema(description = "Date of birth (must be in the past)", example = "1990-01-15")
        @NotNull @Past LocalDate birthDate,

        @Schema(description = "Registration date (defaults to current date if omitted)", example = "2026-09-05")
        @PastOrPresent LocalDate regDate
) {}

