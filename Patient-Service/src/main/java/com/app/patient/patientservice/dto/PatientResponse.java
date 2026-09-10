package com.app.patient.patientservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

// Response DTO returned to the client. Exposes only safe fields; audit/internal fields omitted.
@Builder
@Schema(description = "Outbound patient response representation")
public record PatientResponse(

        @JsonIgnore
        @Schema(description = "Unique UUID identifier of the patient", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
        UUID id,

        @Schema(description = "Full name of the patient", example = "John Doe")
        String name,

        @Schema(description = "Unique email address of the patient", example = "john.doe@example.com")
        String email,

        @Schema(description = "Contact phone number", example = "+1234567890")
        String phone,

        @Schema(description = "Residential address", example = "123 Main St, Anytown, USA")
        String address,

        @Schema(description = "Registration date (YYYY-MM-DD)", example = "2026-09-05")
        LocalDate regDate) {}

