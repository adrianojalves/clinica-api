package br.com.ajasoftware.clinica.domain.dto.administrador;

public record ImportResultDTO(
        int totalLinhas,
        int clinicasCriadas,
        int medicosCriados,
        int procedimentosCriados,
        int vinculosCriados,
        int linhasIgnoradas
) {}
