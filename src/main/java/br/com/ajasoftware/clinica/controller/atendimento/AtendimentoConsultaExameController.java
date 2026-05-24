package br.com.ajasoftware.clinica.controller.atendimento;

import br.com.ajasoftware.clinica.domain.dto.atendimento.AtendimentoConsultaExameResponseDTO;
import br.com.ajasoftware.clinica.service.atendimento.AtendimentoConsultaExameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clinica/atendimentos/{atendimentoId}/itens")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CADASTROS', 'ATENDIMENTO')")
public class AtendimentoConsultaExameController {

    private final AtendimentoConsultaExameService service;

    @GetMapping
    public ResponseEntity<List<AtendimentoConsultaExameResponseDTO>> listByAtendimento(
            @PathVariable Long atendimentoId) {

        return ResponseEntity.ok(service.listByAtendimento(atendimentoId));
    }
}
