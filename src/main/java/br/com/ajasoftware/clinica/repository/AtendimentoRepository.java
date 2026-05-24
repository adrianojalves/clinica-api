package br.com.ajasoftware.clinica.repository;

import br.com.ajasoftware.clinica.domain.dto.atendimento.AtendimentoResponseDTO;
import br.com.ajasoftware.clinica.domain.entity.atendimento.Atendimento;
import br.com.ajasoftware.clinica.domain.filter.atendimento.AtendimentoFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AtendimentoRepository extends JpaRepository<Atendimento, Long> {

    /**
     * Dynamic search with filters using SpEL.
     * The JPQL "new" constructor expression builds the DTO directly in the query,
     * exposing FK references as (cod + nome) pairs — avoids entity-to-DTO mapping in the service.
     */
    @Query(value = """
            SELECT new br.com.ajasoftware.clinica.domain.dto.atendimento.AtendimentoResponseDTO(
                a.id,
                a.dataEmissao,
                a.usuario.id,
                a.usuario.name,
                a.dataConsultaExame,
                a.cliente.id,
                a.cliente.name,
                a.clinica.id,
                a.clinica.name,
                a.tipoPagamento,
                a.parcelas,
                a.status,
                a.totalTransferValue,
                a.totalPrice,
                a.totalTransferValueCard,
                a.totalPriceCard
            )
            FROM Atendimento a
            JOIN a.usuario
            JOIN a.cliente
            JOIN a.clinica
            WHERE (:#{#filter.id}                      IS NULL OR a.id                     = :#{#filter.id})
            AND   (:#{#filter.clienteId}               IS NULL OR a.cliente.id             = :#{#filter.clienteId})
            AND   (:#{#filter.clinicaId}               IS NULL OR a.clinica.id             = :#{#filter.clinicaId})
            AND   (:#{#filter.usuarioId}               IS NULL OR a.usuario.id             = :#{#filter.usuarioId})
            AND   (:#{#filter.status}                  IS NULL OR a.status                 = :#{#filter.status})
            AND   (:#{#filter.tipoPagamento}           IS NULL OR a.tipoPagamento          = :#{#filter.tipoPagamento})
            AND   (:#{#filter.dataConsultaExameInicio} IS NULL OR a.dataConsultaExame     >= :#{#filter.dataConsultaExameInicio})
            AND   (:#{#filter.dataConsultaExameFim}    IS NULL OR a.dataConsultaExame     <= :#{#filter.dataConsultaExameFim})
            """,
            countQuery = """
            SELECT COUNT(a) FROM Atendimento a
            WHERE (:#{#filter.id}                      IS NULL OR a.id                     = :#{#filter.id})
            AND   (:#{#filter.clienteId}               IS NULL OR a.cliente.id             = :#{#filter.clienteId})
            AND   (:#{#filter.clinicaId}               IS NULL OR a.clinica.id             = :#{#filter.clinicaId})
            AND   (:#{#filter.usuarioId}               IS NULL OR a.usuario.id             = :#{#filter.usuarioId})
            AND   (:#{#filter.status}                  IS NULL OR a.status                 = :#{#filter.status})
            AND   (:#{#filter.tipoPagamento}           IS NULL OR a.tipoPagamento          = :#{#filter.tipoPagamento})
            AND   (:#{#filter.dataConsultaExameInicio} IS NULL OR a.dataConsultaExame     >= :#{#filter.dataConsultaExameInicio})
            AND   (:#{#filter.dataConsultaExameFim}    IS NULL OR a.dataConsultaExame     <= :#{#filter.dataConsultaExameFim})
            """)
    Page<AtendimentoResponseDTO> findWithFilters(@Param("filter") AtendimentoFilter filter, Pageable pageable);
}
