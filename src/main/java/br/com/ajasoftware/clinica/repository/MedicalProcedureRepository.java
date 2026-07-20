package br.com.ajasoftware.clinica.repository;

import br.com.ajasoftware.clinica.domain.entity.medical.procedures.MedicalProcedure;
import br.com.ajasoftware.clinica.domain.filter.medical.procedure.ProcedureFilterDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedicalProcedureRepository extends JpaRepository<MedicalProcedure, Long> {

    /**
     * Dynamic search for procedures using SpEL to access filter properties.
     * Follows the established project architecture for repositories.
     */
    @Query("SELECT p FROM MedicalProcedure p WHERE p.active = true AND " +
            "(:#{#filter.name} IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%')) OR (p.tag IS NOT NULL AND LOWER(p.tag) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%')))) AND " +
            "(:#{#filter.type} IS NULL OR p.type = :#{#filter.type})")
    Page<MedicalProcedure> findActiveWithFilters(@Param("filter") ProcedureFilterDTO filter, Pageable pageable);

    @Query("SELECT p FROM MedicalProcedure p WHERE " +
            "(:#{#filter.name} IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%')) OR (p.tag IS NOT NULL AND LOWER(p.tag) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%')))) AND " +
            "(:#{#filter.type} IS NULL OR p.type = :#{#filter.type})")
    Page<MedicalProcedure> findWithFilters(@Param("filter") ProcedureFilterDTO filter, Pageable pageable);
}