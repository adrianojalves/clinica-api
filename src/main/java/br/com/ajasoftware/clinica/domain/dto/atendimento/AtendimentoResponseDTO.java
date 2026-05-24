package br.com.ajasoftware.clinica.domain.dto.atendimento;

import br.com.ajasoftware.clinica.domain.entity.atendimento.Atendimento;
import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoStatus;
import br.com.ajasoftware.clinica.domain.entity.atendimento.TipoPagamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for returning Atendimento data.
 * Foreign keys are exposed as (cod + nome) pairs for display purposes.
 * The canonical constructor is used by the JPQL "new" expression in the repository.
 * The entity constructor is used after persist/update operations.
 */
public record AtendimentoResponseDTO(
        Long id,
        LocalDateTime dataEmissao,
        Long codUsuario,
        String nomeUsuario,
        LocalDate dataConsultaExame,
        Long codCliente,
        String nomeCliente,
        Long codClinica,
        String nomeClinica,
        TipoPagamento tipoPagamento,
        Integer parcelas,
        AtendimentoStatus status,
        BigDecimal totalTransferValue,
        BigDecimal totalPrice,
        BigDecimal totalTransferValueCard,
        BigDecimal totalPriceCard
) {
    public AtendimentoResponseDTO(Atendimento entity) {
        this(
                entity.getId(),
                entity.getDataEmissao(),
                entity.getUsuario().getId(),
                entity.getUsuario().getName(),
                entity.getDataConsultaExame(),
                entity.getCliente().getId(),
                entity.getCliente().getName(),
                entity.getClinica().getId(),
                entity.getClinica().getName(),
                entity.getTipoPagamento(),
                entity.getParcelas(),
                entity.getStatus(),
                entity.getTotalTransferValue(),
                entity.getTotalPrice(),
                entity.getTotalTransferValueCard(),
                entity.getTotalPriceCard()
        );
    }
}
