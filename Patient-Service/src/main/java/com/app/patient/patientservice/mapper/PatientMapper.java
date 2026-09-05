package com.app.patient.patientservice.mapper;

import com.app.patient.patientservice.dto.PatientRequest;
import com.app.patient.patientservice.dto.PatientResponse;
import com.app.patient.patientservice.models.Patient;

// Stateless utility class for mapping between Patient entities and DTOs. Not instantiable.
public final class PatientMapper {

    private PatientMapper() {
        throw new UnsupportedOperationException("PatientMapper is a utility class and cannot be instantiated.");
    }

    // Maps a Patient entity to a PatientResponse DTO (outbound). Audit fields excluded.
    public static PatientResponse toResponse(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getName(),
                patient.getEmail(),
                patient.getPhone(),
                patient.getAddress()
        );
    }

    // Maps a validated PatientRequest DTO to a Patient entity (inbound). System-managed fields omitted.
    public static Patient toEntity(PatientRequest request) {
        return Patient.builder()
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .address(request.address())
                .birthDate(request.birthDate())
                .regDate(request.regDate())
                .build();
    }
}
