package br.com.ajasoftware.clinica.domain.dto.log;

import br.com.ajasoftware.clinica.domain.entity.log.Log;
import java.time.LocalDateTime;

public record LogResponseDTO(
        Long id,
        LocalDateTime dataHora,
        String log,
        Long codUsuario,
        String nomeUsuario
) {
    public LogResponseDTO(Log entity) {
        this(
                entity.getId(),
                entity.getDateTime(),
                entity.getLog(),
                entity.getUser() != null ? entity.getUser().getId() : null,
                entity.getUser() != null ? entity.getUser().getName() : null
        );
    }
}
