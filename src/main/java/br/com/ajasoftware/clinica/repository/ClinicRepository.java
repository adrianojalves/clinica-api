package br.com.ajasoftware.clinica.repository;

import br.com.ajasoftware.clinica.domain.entity.clinics.Clinic;
import br.com.ajasoftware.clinica.domain.filter.clinics.ClinicFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Clinic entity operations.
 */
@Repository
public interface ClinicRepository extends JpaRepository<Clinic, Long> {

    /**
     * Checks if a CNPJ already exists in the database.
     */
    boolean existsByCnpj(String cnpj);

    boolean existsByNameIgnoreCase(String name);

    /**
     * Retrieves a paginated list of clinics applying dynamic filters (Name or CNPJ).
     */
    @Query("""
            SELECT c FROM Clinic c
            WHERE (:#{#filter.name} IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%')))
            AND (:#{#filter.cnpj} IS NULL OR c.cnpj = :#{#filter.cnpj})
            """)
    Page<Clinic> findWithFilter(@Param("filter") ClinicFilter filter, Pageable pageable);
}