package br.com.ajasoftware.clinica.repository;

import br.com.ajasoftware.clinica.domain.dto.clinics.ClinicDoctorProcedureFilterDTO;
import br.com.ajasoftware.clinica.domain.entity.clinics.ClinicDoctorProcedure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClinicDoctorProcedureRepository extends JpaRepository<ClinicDoctorProcedure, Long> {

    /**
     * Checks if a specific combination of Clinic, Doctor, and Procedure already exists.
     */
    boolean existsByClinicIdAndDoctorIdAndMedicalProcedureId(Long clinicId, Long doctorId, Long medicalProcedureId);

    /**
     * Checks if a specific combination exists, ignoring a specific ID (useful for updates).
     */
    boolean existsByClinicIdAndDoctorIdAndMedicalProcedureIdAndIdNot(Long clinicId, Long doctorId, Long medicalProcedureId, Long id);

    /**
     * Dynamic search using SpEL.
     * Uses JOIN FETCH to optimize performance when loading nested entities for the ResponseDTO.
     */
    @Query(value = "SELECT cdp FROM ClinicDoctorProcedure cdp " +
            "JOIN FETCH cdp.clinic " +
            "JOIN FETCH cdp.doctor " +
            "JOIN FETCH cdp.medicalProcedure " +
            "WHERE (:#{#filter.clinicId} IS NULL OR cdp.clinic.id = :#{#filter.clinicId}) AND " +
            "(:#{#filter.doctorId} IS NULL OR cdp.doctor.id = :#{#filter.doctorId}) AND " +
            "(:#{#filter.medicalProcedureId} IS NULL OR cdp.medicalProcedure.id = :#{#filter.medicalProcedureId})",
            countQuery = "SELECT count(cdp) FROM ClinicDoctorProcedure cdp " +
                    "WHERE (:#{#filter.clinicId} IS NULL OR cdp.clinic.id = :#{#filter.clinicId}) AND " +
                    "(:#{#filter.doctorId} IS NULL OR cdp.doctor.id = :#{#filter.doctorId}) AND " +
                    "(:#{#filter.medicalProcedureId} IS NULL OR cdp.medicalProcedure.id = :#{#filter.medicalProcedureId})")
    Page<ClinicDoctorProcedure> findWithFilters(@Param("filter") ClinicDoctorProcedureFilterDTO filter, Pageable pageable);
}