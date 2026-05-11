package br.com.ajasoftware.clinica.repository;

import br.com.ajasoftware.clinica.domain.entity.client.Client;
import br.com.ajasoftware.clinica.domain.filter.client.ClientFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    /**
     * Finds a client by their unique SHA-256 CPF hash.
     */
    Optional<Client> findByCpfHash(String cpfHash);

    /**
     * Dynamic search with filters.
     * Note: We don't filter by encrypted fields (CPF/RG) directly in SQL.
     */
    @Query("""
       SELECT c FROM Client c 
       WHERE (:#{#filter.id} IS NULL OR c.id = :#{#filter.id})
       AND (:#{#filter.name} IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%')))
       AND (:#{#filter.status} IS NULL OR c.active = :#{#filter.status})
       AND (:#{#filter.cpf} IS NULL OR c.cpfHash = :#{#filter.cpf}) 
       """)
    Page<Client> findWithFilter(@Param("filter") ClientFilter filter, Pageable pageable);
}