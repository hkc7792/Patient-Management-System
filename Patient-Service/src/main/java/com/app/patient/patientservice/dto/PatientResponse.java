package com.app.patient.patientservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

// Response DTO returned to the client. Exposes only safe fields; audit/internal fields omitted.
@Builder
public record PatientResponse(

        @JsonIgnore
        UUID id,
        String name,
        String email,
        String phone,
        String address,
        LocalDate regDate) {}
