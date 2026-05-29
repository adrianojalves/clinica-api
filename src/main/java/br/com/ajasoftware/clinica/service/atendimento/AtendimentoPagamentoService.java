package br.com.ajasoftware.clinica.service.atendimento;

import br.com.ajasoftware.clinica.domain.dto.atendimento.AtendimentoPagamentoRequestDTO;
import br.com.ajasoftware.clinica.domain.dto.atendimento.AtendimentoPagamentoResponseDTO;
import br.com.ajasoftware.clinica.domain.entity.atendimento.Atendimento;
import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoPagamento;
import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoStatus;
import br.com.ajasoftware.clinica.domain.entity.atendimento.TipoPagamento;
import br.com.ajasoftware.clinica.exceptions.BusinessException;
import br.com.ajasoftware.clinica.repository.AtendimentoPagamentoRepository;
import br.com.ajasoftware.clinica.repository.AtendimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AtendimentoPagamentoService {

    private final AtendimentoPagamentoRepository repository;
    private final AtendimentoRepository atendimentoRepository;

    @Transactional(readOnly = true)
    public List<AtendimentoPagamentoResponseDTO> listByAtendimento(Long atendimentoId) {
        findAtendimentoOrThrow(atendimentoId);
        return repository.findByAtendimentoId(atendimentoId)
                .stream()
                .map(AtendimentoPagamentoResponseDTO::new)
                .toList();
    }

    @Transactional
    public AtendimentoPagamentoResponseDTO create(Long atendimentoId, AtendimentoPagamentoRequestDTO data) {
        Atendimento atendimento = findAtendimentoOrThrow(atendimentoId);
        requireAberto(atendimento);

        BigDecimal desconto = data.valorDesconto() != null ? data.valorDesconto() : BigDecimal.ZERO;
        int parcelas = data.parcelas() != null ? data.parcelas() : 1;
        validateCartaoDesconto(data.tipoPagamento(), desconto);
        validateParcelas(data.tipoPagamento(), parcelas);

        AtendimentoPagamento pagamento = new AtendimentoPagamento();
        pagamento.setAtendimento(atendimento);
        pagamento.setTipoPagamento(data.tipoPagamento());
        pagamento.setValor(data.valor());
        pagamento.setValorDesconto(desconto);
        pagamento.setParcelas(parcelas);

        return new AtendimentoPagamentoResponseDTO(repository.save(pagamento));
    }

    @Transactional
    public AtendimentoPagamentoResponseDTO update(Long id, AtendimentoPagamentoRequestDTO data) {
        AtendimentoPagamento pagamento = findOrThrow(id);
        requireAberto(pagamento.getAtendimento());

        BigDecimal desconto = data.valorDesconto() != null ? data.valorDesconto() : BigDecimal.ZERO;
        int parcelas = data.parcelas() != null ? data.parcelas() : 1;
        validateCartaoDesconto(data.tipoPagamento(), desconto);
        validateParcelas(data.tipoPagamento(), parcelas);

        pagamento.setTipoPagamento(data.tipoPagamento());
        pagamento.setValor(data.valor());
        pagamento.setValorDesconto(desconto);
        pagamento.setParcelas(parcelas);

        return new AtendimentoPagamentoResponseDTO(pagamento);
    }

    @Transactional
    public void delete(Long id) {
        AtendimentoPagamento pagamento = findOrThrow(id);
        requireAberto(pagamento.getAtendimento());
        repository.delete(pagamento);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void validateCartaoDesconto(TipoPagamento tipo, BigDecimal desconto) {
        if (tipo.isCartao() && desconto.compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("Não é permitido aplicar desconto em pagamentos com cartão.");
        }
    }

    private void validateParcelas(TipoPagamento tipo, int parcelas) {
        if (parcelas > 1 && !tipo.isParcelavel()) {
            throw new BusinessException("Parcelamento só é permitido para pagamentos com cartão de crédito.");
        }
    }

    private void requireAberto(Atendimento atendimento) {
        if (atendimento.getStatus() != AtendimentoStatus.ABERTO) {
            throw new BusinessException(
                    "Este atendimento não pode ser modificado pois seu status é " +
                    atendimento.getStatus().name() + ".");
        }
    }

    private Atendimento findAtendimentoOrThrow(Long atendimentoId) {
        return atendimentoRepository.findById(atendimentoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atendimento não encontrado."));
    }

    private AtendimentoPagamento findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pagamento não encontrado."));
    }
}
