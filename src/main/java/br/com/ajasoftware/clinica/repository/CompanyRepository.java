package br.com.ajasoftware.clinica.repository;

import br.com.ajasoftware.clinica.domain.entity.company.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
}