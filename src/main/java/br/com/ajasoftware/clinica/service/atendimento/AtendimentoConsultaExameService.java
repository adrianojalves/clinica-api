package br.com.ajasoftware.clinica.service.atendimento;

import br.com.ajasoftware.clinica.domain.dto.atendimento.AtendimentoConsultaExameResponseDTO;
import br.com.ajasoftware.clinica.repository.AtendimentoConsultaExameRepository;
import br.com.ajasoftware.clinica.repository.AtendimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Service layer for AtendimentoConsultaExame (detail) management.
 *
 * Currently exposes only read operations. When item-level CRUD is added,
 * this service will use {@link AtendimentoTotalsCalculator} to recalculate
 * the parent Atendimento totals after every mutation — keeping that responsibility
 * out of this service (SRP / DIP).
 */
@Service
@RequiredArgsConstructor
public class AtendimentoConsultaExameService {

    private final AtendimentoConsultaExameRepository repository;
    private final AtendimentoRepository atendimentoRepository;

    /**
     * Returns all items belonging to the given Atendimento.
     *
     * @param atendimentoId the parent Atendimento id.
     * @return list of detail DTOs (never null, may be empty).
     * @throws ResponseStatusException 404 if the Atendimento does not exist.
     */
    @Transactional(readOnly = true)
    public List<AtendimentoConsultaExameResponseDTO> listByAtendimento(Long atendimentoId) {
        if (!atendimentoRepository.existsById(atendimentoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Atendimento não encontrado.");
        }
        return repository.findByAtendimentoId(atendimentoId);
    }
}
