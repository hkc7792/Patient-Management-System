package com.app.patient.patientservice.dto;

import java.util.UUID;

// Response DTO returned to the client. Exposes only safe fields; audit/internal fields omitted.
public record PatientResponse(
        UUID id,
        String name,
        String email,
        String phone,
        String address
) {}
