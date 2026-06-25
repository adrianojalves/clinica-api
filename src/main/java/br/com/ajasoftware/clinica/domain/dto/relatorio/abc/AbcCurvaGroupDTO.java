package br.com.ajasoftware.clinica.domain.dto.relatorio.abc;

import java.util.List;

public record AbcCurvaGroupDTO(
        String curva,
        List<AbcProcedimentoItemDTO> itens,
        long sumQuantidade
) {}
