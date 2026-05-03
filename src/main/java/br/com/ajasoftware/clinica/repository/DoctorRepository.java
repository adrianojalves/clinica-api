package br.com.ajasoftware.clinica.repository;

import br.com.ajasoftware.clinica.domain.entity.doctors.Doctor;
import br.com.ajasoftware.clinica.domain.filter.doctors.DoctorFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    boolean existsByCrm(String crm);

    @Query("SELECT d FROM Doctor d WHERE " +
            "(:#{#filter.name} IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%'))) AND " +
            "(:#{#filter.status} IS NULL OR d.active = :#{#filter.status})")
    Page<Doctor> findWithFilter(@Param("filter") DoctorFilter filter, Pageable pageable);
}