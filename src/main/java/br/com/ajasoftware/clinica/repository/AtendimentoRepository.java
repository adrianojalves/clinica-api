package br.com.ajasoftware.clinica.repository;

import br.com.ajasoftware.clinica.domain.dto.relatorio.atendimento.AtendimentoReportItemDTO;
import br.com.ajasoftware.clinica.domain.dto.atendimento.AtendimentoResponseDTO;
import br.com.ajasoftware.clinica.domain.dto.relatorio.repasse.RepasseReportItemDTO;
import br.com.ajasoftware.clinica.domain.entity.atendimento.Atendimento;
import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoStatus;
import br.com.ajasoftware.clinica.domain.filter.atendimento.AtendimentoFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AtendimentoRepository extends JpaRepository<Atendimento, Long> {

    @Query(value = """
            SELECT new br.com.ajasoftware.clinica.domain.dto.atendimento.AtendimentoResponseDTO(
                a.id,
                a.dataEmissao,
                a.usuario.id,
                a.usuario.name,
                a.dataConsultaExame,
                a.turno,
                a.observacao,
                a.cliente.id,
                a.cliente.name,
                a.clinica.id,
                a.clinica.name,
                a.parcelas,
                a.status,
                a.totalTransferValue,
                a.totalPrice,
                a.totalTransferValueCard,
                a.totalPriceCard,
                a.valorDesconto,
                a.valorAcrescimo
            )
            FROM Atendimento a
            JOIN a.usuario
            JOIN a.cliente
            JOIN a.clinica
            WHERE (:#{#filter.id}                      IS NULL OR a.id                     = :#{#filter.id})
            AND   (:#{#filter.clienteId}               IS NULL OR a.cliente.id             = :#{#filter.clienteId})
            AND   (:#{#filter.nomeCliente}             IS NULL OR LOWER(a.cliente.name)    LIKE LOWER(CONCAT('%', :#{#filter.nomeCliente}, '%')))
            AND   (:#{#filter.clinicaId}               IS NULL OR a.clinica.id             = :#{#filter.clinicaId})
            AND   (:#{#filter.nomeClinica}             IS NULL OR LOWER(a.clinica.name)    LIKE LOWER(CONCAT('%', :#{#filter.nomeClinica}, '%')))
            AND   (:#{#filter.usuarioId}               IS NULL OR a.usuario.id             = :#{#filter.usuarioId})
            AND   (:#{#filter.nomeUsuario}             IS NULL OR LOWER(a.usuario.name)    LIKE LOWER(CONCAT('%', :#{#filter.nomeUsuario}, '%')))
            AND   (:#{#filter.status}                  IS NULL OR a.status                 = :#{#filter.status})
            AND   (:#{#filter.dataConsultaExameInicio} IS NULL OR a.dataConsultaExame     >= :#{#filter.dataConsultaExameInicio})
            AND   (:#{#filter.dataConsultaExameFim}    IS NULL OR a.dataConsultaExame     <= :#{#filter.dataConsultaExameFim})
            """,
            countQuery = """
            SELECT COUNT(a) FROM Atendimento a
            WHERE (:#{#filter.id}                      IS NULL OR a.id                     = :#{#filter.id})
            AND   (:#{#filter.clienteId}               IS NULL OR a.cliente.id             = :#{#filter.clienteId})
            AND   (:#{#filter.nomeCliente}             IS NULL OR LOWER(a.cliente.name)    LIKE LOWER(CONCAT('%', :#{#filter.nomeCliente}, '%')))
            AND   (:#{#filter.clinicaId}               IS NULL OR a.clinica.id             = :#{#filter.clinicaId})
            AND   (:#{#filter.nomeClinica}             IS NULL OR LOWER(a.clinica.name)    LIKE LOWER(CONCAT('%', :#{#filter.nomeClinica}, '%')))
            AND   (:#{#filter.usuarioId}               IS NULL OR a.usuario.id             = :#{#filter.usuarioId})
            AND   (:#{#filter.nomeUsuario}             IS NULL OR LOWER(a.usuario.name)    LIKE LOWER(CONCAT('%', :#{#filter.nomeUsuario}, '%')))
            AND   (:#{#filter.status}                  IS NULL OR a.status                 = :#{#filter.status})
            AND   (:#{#filter.dataConsultaExameInicio} IS NULL OR a.dataConsultaExame     >= :#{#filter.dataConsultaExameInicio})
            AND   (:#{#filter.dataConsultaExameFim}    IS NULL OR a.dataConsultaExame     <= :#{#filter.dataConsultaExameFim})
            """)
    Page<AtendimentoResponseDTO> findWithFilters(@Param("filter") AtendimentoFilter filter, Pageable pageable);

    @Query("""
            SELECT new br.com.ajasoftware.clinica.domain.dto.relatorio.atendimento.AtendimentoReportItemDTO(
                a.id,
                a.cliente.name,
                a.clinica.name,
                a.usuario.name,
                a.dataEmissao,
                a.dataConsultaExame,
                a.totalPrice,
                a.valorDesconto,
                a.valorAcrescimo
            )
            FROM Atendimento a
            JOIN a.usuario
            JOIN a.cliente
            JOIN a.clinica
            WHERE a.status = :status
            AND (:clinicaId  IS NULL OR a.clinica.id  = :clinicaId)
            AND (:clienteId  IS NULL OR a.cliente.id  = :clienteId)
            AND (:usuarioId  IS NULL OR a.usuario.id  = :usuarioId)
            AND (:dataInicial IS NULL OR a.dataEmissao >= :dataInicial)
            AND (:dataFinal   IS NULL OR a.dataEmissao <= :dataFinal)
            ORDER BY a.dataEmissao, a.id
            """)
    List<AtendimentoReportItemDTO> findForReport(
            @Param("status") AtendimentoStatus status,
            @Param("clinicaId") Long clinicaId,
            @Param("clienteId") Long clienteId,
            @Param("usuarioId") Long usuarioId,
            @Param("dataInicial") LocalDateTime dataInicial,
            @Param("dataFinal") LocalDateTime dataFinal);

    @Query("""
            SELECT a FROM Atendimento a
            JOIN FETCH a.cliente
            JOIN FETCH a.clinica
            JOIN FETCH a.usuario
            WHERE a.status = :status
            AND (:clinicaId  IS NULL OR a.clinica.id  = :clinicaId)
            AND (:clienteId  IS NULL OR a.cliente.id  = :clienteId)
            AND (:usuarioId  IS NULL OR a.usuario.id  = :usuarioId)
            AND (:dataInicial IS NULL OR a.dataEmissao >= :dataInicial)
            AND (:dataFinal   IS NULL OR a.dataEmissao <= :dataFinal)
            ORDER BY a.dataEmissao, a.id
            """)
    List<Atendimento> findEntitiesForReport(
            @Param("status") AtendimentoStatus status,
            @Param("clinicaId") Long clinicaId,
            @Param("clienteId") Long clienteId,
            @Param("usuarioId") Long usuarioId,
            @Param("dataInicial") LocalDateTime dataInicial,
            @Param("dataFinal") LocalDateTime dataFinal);

    @Query("""
            SELECT new br.com.ajasoftware.clinica.domain.dto.relatorio.repasse.RepasseReportItemDTO(
                a.id,
                a.cliente.name,
                a.clinica.name,
                a.dataEmissao,
                a.totalPrice,
                a.valorAcrescimo,
                a.valorDesconto,
                a.totalTransferValue,
                a.totalTransferValueCard
            )
            FROM Atendimento a
            JOIN a.cliente
            JOIN a.clinica
            WHERE a.status = 'ENCAMINHADO'
            AND (:clinicaId   IS NULL OR a.clinica.id  = :clinicaId)
            AND (:dataInicial IS NULL OR a.dataEmissao >= :dataInicial)
            AND (:dataFinal   IS NULL OR a.dataEmissao <= :dataFinal)
            ORDER BY a.clinica.name, a.id
            """)
    List<RepasseReportItemDTO> findForRepasseReport(
            @Param("clinicaId") Long clinicaId,
            @Param("dataInicial") LocalDateTime dataInicial,
            @Param("dataFinal") LocalDateTime dataFinal);

    @Query("""
            SELECT a.clinica.id, a.clinica.name, COUNT(a)
            FROM Atendimento a
            WHERE a.status = 'ENCAMINHADO'
            AND (:clinicaId   IS NULL OR a.clinica.id  = :clinicaId)
            AND (:dataInicial IS NULL OR a.dataEmissao >= :dataInicial)
            AND (:dataFinal   IS NULL OR a.dataEmissao <= :dataFinal)
            GROUP BY a.clinica.id, a.clinica.name
            ORDER BY a.clinica.name
            """)
    List<Object[]> countAtendimentosByClinica(
            @Param("clinicaId") Long clinicaId,
            @Param("dataInicial") LocalDateTime dataInicial,
            @Param("dataFinal") LocalDateTime dataFinal);

    @Query("""
            SELECT a.clinica.id, SUM(a.totalPrice + a.valorAcrescimo - a.valorDesconto)
            FROM Atendimento a
            WHERE a.status = 'ENCAMINHADO'
            AND (:clinicaId   IS NULL OR a.clinica.id  = :clinicaId)
            AND (:dataInicial IS NULL OR a.dataEmissao >= :dataInicial)
            AND (:dataFinal   IS NULL OR a.dataEmissao <= :dataFinal)
            GROUP BY a.clinica.id
            """)
    List<Object[]> sumFaturamentoBrutoByClinica(
            @Param("clinicaId") Long clinicaId,
            @Param("dataInicial") LocalDateTime dataInicial,
            @Param("dataFinal") LocalDateTime dataFinal);

    @Query("""
            SELECT a.clinica.id, SUM(a.totalTransferValue + a.totalTransferValueCard)
            FROM Atendimento a
            WHERE a.status = 'ENCAMINHADO'
            AND (:clinicaId   IS NULL OR a.clinica.id  = :clinicaId)
            AND (:dataInicial IS NULL OR a.dataEmissao >= :dataInicial)
            AND (:dataFinal   IS NULL OR a.dataEmissao <= :dataFinal)
            GROUP BY a.clinica.id
            """)
    List<Object[]> sumRepasseByClinica(
            @Param("clinicaId") Long clinicaId,
            @Param("dataInicial") LocalDateTime dataInicial,
            @Param("dataFinal") LocalDateTime dataFinal);
}
