package br.com.ajasoftware.clinica.domain.dto.atendimento;

import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoConsultaExame;

import java.math.BigDecimal;

/**
 * DTO for returning AtendimentoConsultaExame (detail) data.
 * Foreign keys are exposed as (cod + nome) pairs for display purposes.
 * codMedico / nomeMedico may be null when no doctor is assigned to the item.
 * The canonical constructor is used by the JPQL "new" expression in the repository.
 * The entity constructor is used after persist/update operations.
 */
public record AtendimentoConsultaExameResponseDTO(
        Long id,
        Long codAtendimento,
        Long codMedico,
        String nomeMedico,
        Long codMedicalProcedure,
        String nomeMedicalProcedure,
        BigDecimal transferValue,
        BigDecimal price,
        BigDecimal transferValueCard,
        BigDecimal priceCard
) {
    public AtendimentoConsultaExameResponseDTO(AtendimentoConsultaExame entity) {
        this(
                entity.getId(),
                entity.getAtendimento().getId(),
                entity.getDoctor() != null ? entity.getDoctor().getId() : null,
                entity.getDoctor() != null ? entity.getDoctor().getName() : null,
                entity.getMedicalProcedure().getId(),
                entity.getMedicalProcedure().getName(),
                entity.getTransferValue(),
                entity.getPrice(),
                entity.getTransferValueCard(),
                entity.getPriceCard()
        );
    }
}
