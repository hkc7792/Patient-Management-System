package com.app.patient.patientservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

// Inbound request DTO for creating or updating a patient. Validated before reaching the service layer.
public record PatientRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        String phone,
        String address,
        @NotNull @Past LocalDate birthDate,
        @PastOrPresent LocalDate regDate
) {}
