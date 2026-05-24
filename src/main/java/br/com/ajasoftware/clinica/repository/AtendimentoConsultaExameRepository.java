package br.com.ajasoftware.clinica.repository;

import br.com.ajasoftware.clinica.domain.dto.atendimento.AtendimentoConsultaExameResponseDTO;
import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoConsultaExame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AtendimentoConsultaExameRepository extends JpaRepository<AtendimentoConsultaExame, Long> {

    /**
     * Retrieves all items of a given Atendimento as DTOs.
     * Uses LEFT JOIN for doctor (nullable) so items without a doctor are included.
     * The JPQL "new" constructor expression builds the DTO directly in the query.
     */
    @Query("""
            SELECT new br.com.ajasoftware.clinica.domain.dto.atendimento.AtendimentoConsultaExameResponseDTO(
                i.id,
                i.atendimento.id,
                i.doctor.id,
                i.doctor.name,
                i.medicalProcedure.id,
                i.medicalProcedure.name,
                i.transferValue,
                i.price,
                i.transferValueCard,
                i.priceCard
            )
            FROM AtendimentoConsultaExame i
            LEFT JOIN i.doctor
            JOIN i.medicalProcedure
            WHERE i.atendimento.id = :atendimentoId
            """)
    List<AtendimentoConsultaExameResponseDTO> findByAtendimentoId(@Param("atendimentoId") Long atendimentoId);
}
