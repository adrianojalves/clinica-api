package br.com.ajasoftware.clinica.repository;

import br.com.ajasoftware.clinica.domain.dto.clinics.ClinicDoctorProcedureFilterDTO;
import br.com.ajasoftware.clinica.domain.entity.clinics.ClinicDoctorProcedure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClinicDoctorProcedureRepository extends JpaRepository<ClinicDoctorProcedure, Long> {

    /**
     * Checks if a specific combination of Clinic, Doctor, and Procedure already exists.
     */
    @Query("""
        SELECT COUNT(c) > 0 FROM ClinicDoctorProcedure c
        WHERE c.clinic.id = :clinicId
        AND (:doctorId IS NULL AND c.doctor.id IS NULL OR c.doctor.id = :doctorId)
        AND c.medicalProcedure.id = :medicalProcedureId
        """)
    boolean existsCombination(
            @Param("clinicId") Long clinicId,
            @Param("doctorId") Long doctorId,
            @Param("medicalProcedureId") Long medicalProcedureId
    );

    /**
     * Checks if a specific combination exists, ignoring a specific ID (useful for updates).
     */
    @Query("""
        SELECT COUNT(c) > 0 FROM ClinicDoctorProcedure c
        WHERE c.clinic.id = :clinicId
        AND (:doctorId IS NULL AND c.doctor.id IS NULL OR c.doctor.id = :doctorId)
        AND c.medicalProcedure.id = :medicalProcedureId
        AND c.id <> :id
        """)
    boolean existsCombinationForAnotherId(
            @Param("clinicId") Long clinicId,
            @Param("doctorId") Long doctorId,
            @Param("medicalProcedureId") Long medicalProcedureId,
            @Param("id") Long id
    );

    /**
     * Dynamic search supporting both ID-based and name-based (LIKE) filters simultaneously.
     *
     * Doctor uses LEFT JOIN because the association is nullable — rows without a doctor
     * must still appear when no doctorName/doctorId filter is active.
     *
     * The countQuery mirrors the WHERE clause without JOIN FETCH (not allowed in count queries).
     */
    @Query(value = """
            SELECT cdp FROM ClinicDoctorProcedure cdp
            JOIN FETCH cdp.clinic       c
            LEFT JOIN FETCH cdp.doctor  d
            JOIN FETCH cdp.medicalProcedure mp
            WHERE (:#{#filter.clinicId}         IS NULL OR c.id  = :#{#filter.clinicId})
            AND   (:#{#filter.doctorId}         IS NULL OR d.id  = :#{#filter.doctorId})
            AND   (:#{#filter.medicalProcedureId} IS NULL OR mp.id = :#{#filter.medicalProcedureId})
            AND   (:#{#filter.clinicName}     IS NULL OR LOWER(c.name)  LIKE LOWER(CONCAT('%', :#{#filter.clinicName},     '%')))
            AND   (:#{#filter.doctorName}     IS NULL OR LOWER(d.name)  LIKE LOWER(CONCAT('%', :#{#filter.doctorName},     '%')))
            AND   (:#{#filter.procedureName}  IS NULL OR LOWER(mp.name) LIKE LOWER(CONCAT('%', :#{#filter.procedureName},  '%')) OR (mp.tag IS NOT NULL AND LOWER(mp.tag) LIKE LOWER(CONCAT('%', :#{#filter.procedureName}, '%'))))
            """,
            countQuery = """
            SELECT COUNT(cdp) FROM ClinicDoctorProcedure cdp
            JOIN      cdp.clinic          c
            LEFT JOIN cdp.doctor          d
            JOIN      cdp.medicalProcedure mp
            WHERE (:#{#filter.clinicId}           IS NULL OR c.id  = :#{#filter.clinicId})
            AND   (:#{#filter.doctorId}           IS NULL OR d.id  = :#{#filter.doctorId})
            AND   (:#{#filter.medicalProcedureId} IS NULL OR mp.id = :#{#filter.medicalProcedureId})
            AND   (:#{#filter.clinicName}     IS NULL OR LOWER(c.name)  LIKE LOWER(CONCAT('%', :#{#filter.clinicName},     '%')))
            AND   (:#{#filter.doctorName}     IS NULL OR LOWER(d.name)  LIKE LOWER(CONCAT('%', :#{#filter.doctorName},     '%')))
            AND   (:#{#filter.procedureName}  IS NULL OR LOWER(mp.name) LIKE LOWER(CONCAT('%', :#{#filter.procedureName},  '%')) OR (mp.tag IS NOT NULL AND LOWER(mp.tag) LIKE LOWER(CONCAT('%', :#{#filter.procedureName}, '%'))))
            """)
    Page<ClinicDoctorProcedure> findWithFilters(
            @Param("filter") ClinicDoctorProcedureFilterDTO filter,
            Pageable pageable
    );

    List<ClinicDoctorProcedure> findByClinicId(Long clinicId);

    @Query("""
        SELECT c FROM ClinicDoctorProcedure c
        WHERE c.clinic.id = :clinicId
        AND (:doctorId IS NULL OR c.doctor.id = :doctorId OR c.doctor.id IS NULL)
        AND c.medicalProcedure.id = :medicalProcedureId
        ORDER BY c.doctor.id DESC NULLS LAST
        """)
    List<ClinicDoctorProcedure> findMatchingConfigurations(
            @Param("clinicId") Long clinicId,
            @Param("doctorId") Long doctorId,
            @Param("medicalProcedureId") Long medicalProcedureId
    );
}
