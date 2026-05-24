package br.com.ajasoftware.clinica.domain.dto.atendimento;

import br.com.ajasoftware.clinica.domain.entity.atendimento.TipoPagamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for creating or updating an Atendimento (master + detail items).
 * Totals are always recalculated server-side from the items list; they must not be sent by the client.
 */
public record AtendimentoRequestDTO(

        @NotNull(message = "O usuário responsável é obrigatório.")
        Long codUsuario,

        @NotNull(message = "A data da consulta/exame é obrigatória.")
        LocalDate dataConsultaExame,

        @NotNull(message = "O cliente é obrigatório.")
        Long codCliente,

        @NotNull(message = "A clínica é obrigatória.")
        Long codClinica,

        @NotNull(message = "O tipo de pagamento é obrigatório.")
        TipoPagamento tipoPagamento,

        @NotNull(message = "O número de parcelas é obrigatório.")
        @Min(value = 1, message = "O número de parcelas deve ser pelo menos 1.")
        Integer parcelas,

        @NotEmpty(message = "Informe pelo menos um procedimento/exame.")
        List<@Valid AtendimentoConsultaExameRequestDTO> itens
) {}
