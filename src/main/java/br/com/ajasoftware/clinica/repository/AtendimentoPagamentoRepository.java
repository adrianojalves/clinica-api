package br.com.ajasoftware.clinica.repository;

import br.com.ajasoftware.clinica.domain.dto.relatorio.atendimento.AtendimentoPagamentoReportItemDTO;
import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoPagamento;
import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AtendimentoPagamentoRepository extends JpaRepository<AtendimentoPagamento, Long> {

    List<AtendimentoPagamento> findByAtendimentoId(Long atendimentoId);

    boolean existsByAtendimentoId(Long atendimentoId);

    @Query("SELECT COALESCE(SUM(p.valorDesconto), 0) FROM AtendimentoPagamento p WHERE p.atendimento.id = :atendimentoId")
    BigDecimal sumDescontoByAtendimentoId(@Param("atendimentoId") Long atendimentoId);

    @Query("SELECT COALESCE(SUM(p.valorDesconto), 0) FROM AtendimentoPagamento p WHERE p.atendimento.id = :atendimentoId AND p.id <> :excludeId")
    BigDecimal sumDescontoByAtendimentoIdExcluding(@Param("atendimentoId") Long atendimentoId, @Param("excludeId") Long excludeId);

    @Query("""
            SELECT new br.com.ajasoftware.clinica.domain.dto.relatorio.atendimento.AtendimentoPagamentoReportItemDTO(
                a.id,
                a.cliente.name,
                a.clinica.name,
                p.tipoPagamento,
                p.parcelas,
                p.valor,
                p.valorDesconto
            )
            FROM AtendimentoPagamento p
            JOIN p.atendimento a
            JOIN a.cliente
            JOIN a.clinica
            JOIN a.usuario
            WHERE a.status = :status
            AND (:clinicaId   IS NULL OR a.clinica.id  = :clinicaId)
            AND (:clienteId   IS NULL OR a.cliente.id  = :clienteId)
            AND (:usuarioId   IS NULL OR a.usuario.id  = :usuarioId)
            AND (:dataInicial IS NULL OR a.dataEmissao >= :dataInicial)
            AND (:dataFinal   IS NULL OR a.dataEmissao <= :dataFinal)
            ORDER BY a.dataEmissao, a.id, p.tipoPagamento
            """)
    List<AtendimentoPagamentoReportItemDTO> findPagamentosForReport(
            @Param("status") AtendimentoStatus status,
            @Param("clinicaId") Long clinicaId,
            @Param("clienteId") Long clienteId,
            @Param("usuarioId") Long usuarioId,
            @Param("dataInicial") LocalDateTime dataInicial,
            @Param("dataFinal") LocalDateTime dataFinal);
}
