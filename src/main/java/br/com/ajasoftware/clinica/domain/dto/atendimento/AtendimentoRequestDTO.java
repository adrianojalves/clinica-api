package br.com.ajasoftware.clinica.domain.dto.atendimento;

import br.com.ajasoftware.clinica.domain.entity.atendimento.TurnoAtendimento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record AtendimentoRequestDTO(

        LocalDate dataConsultaExame,

        TurnoAtendimento turno,

        String observacao,

        @NotNull(message = "O cliente é obrigatório.")
        Long codCliente,

        @NotNull(message = "A clínica é obrigatória.")
        Long codClinica,

        @NotNull(message = "O número de parcelas é obrigatório.")
        @Min(value = 1, message = "O número de parcelas deve ser pelo menos 1.")
        Integer parcelas,

        @NotEmpty(message = "Informe pelo menos um procedimento/exame.")
        List<@Valid AtendimentoConsultaExameRequestDTO> itens
) {}
