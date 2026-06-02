package br.com.ajasoftware.clinica.service.atendimento;

import br.com.ajasoftware.clinica.domain.dto.atendimento.AtendimentoConsultaExameRequestDTO;
import br.com.ajasoftware.clinica.domain.dto.atendimento.AtendimentoRequestDTO;
import br.com.ajasoftware.clinica.domain.dto.atendimento.AtendimentoResponseDTO;
import br.com.ajasoftware.clinica.domain.entity.User;
import br.com.ajasoftware.clinica.domain.entity.atendimento.Atendimento;
import org.springframework.security.core.context.SecurityContextHolder;
import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoConsultaExame;
import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoPagamento;
import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoStatus;
import br.com.ajasoftware.clinica.domain.entity.client.Client;
import br.com.ajasoftware.clinica.domain.entity.clinics.Clinic;
import br.com.ajasoftware.clinica.domain.entity.doctors.Doctor;
import br.com.ajasoftware.clinica.domain.entity.medical.procedures.MedicalProcedure;
import br.com.ajasoftware.clinica.domain.filter.atendimento.AtendimentoFilter;
import br.com.ajasoftware.clinica.exceptions.BusinessException;
import br.com.ajasoftware.clinica.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AtendimentoService {

    private final AtendimentoRepository atendimentoRepository;
    private final ClientRepository clientRepository;
    private final ClinicRepository clinicRepository;
    private final DoctorRepository doctorRepository;
    private final MedicalProcedureRepository medicalProcedureRepository;
    private final AtendimentoPagamentoRepository atendimentoPagamentoRepository;
    private final AtendimentoTotalsCalculator totalsCalculator;

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<AtendimentoResponseDTO> list(AtendimentoFilter filter, Pageable pageable) {
        return atendimentoRepository.findWithFilters(filter, pageable);
    }

    @Transactional(readOnly = true)
    public AtendimentoResponseDTO getById(Long id) {
        return new AtendimentoResponseDTO(findOrThrow(id));
    }

    // -------------------------------------------------------------------------
    // Commands
    // -------------------------------------------------------------------------

    @Transactional
    public AtendimentoResponseDTO create(AtendimentoRequestDTO data) {
        Atendimento atendimento = new Atendimento();
        atendimento.setUsuario((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        applyHeader(atendimento, data);
        applyItems(atendimento, data.itens());
        totalsCalculator.recalculate(atendimento);
        atendimentoRepository.save(atendimento);
        return new AtendimentoResponseDTO(atendimento);
    }

    @Transactional
    public AtendimentoResponseDTO update(Long id, AtendimentoRequestDTO data) {
        Atendimento atendimento = findOrThrow(id);
        requireAberto(atendimento);

        applyHeader(atendimento, data);

        atendimento.getItens().clear();
        applyItems(atendimento, data.itens());
        totalsCalculator.recalculate(atendimento);

        return new AtendimentoResponseDTO(atendimento);
    }

    @Transactional
    public void delete(Long id) {
        Atendimento atendimento = findOrThrow(id);
        requireAberto(atendimento);
        atendimentoRepository.delete(atendimento);
    }

    @Transactional
    public AtendimentoResponseDTO finalizar(Long id) {
        Atendimento atendimento = findOrThrow(id);
        requireAberto(atendimento);

        List<AtendimentoConsultaExame> itens = atendimento.getItens();
        List<AtendimentoPagamento> pagamentos = atendimentoPagamentoRepository.findByAtendimentoId(id);

        if (!itens.isEmpty() && pagamentos.isEmpty()) {
            throw new BusinessException(
                    "É necessário informar pelo menos um pagamento para finalizar o atendimento com procedimentos.");
        }

        BigDecimal totalItens = itens.stream()
                .map(i -> i.getPrice() != null ? i.getPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDescontos = pagamentos.stream()
                .map(p -> p.getValorDesconto() != null ? p.getValorDesconto() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalDescontos.compareTo(BigDecimal.ZERO) > 0 && totalItens.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal percentualMax = atendimento.getUsuario().getPercentutalDesconto();
            if (percentualMax == null) percentualMax = BigDecimal.ZERO;

            BigDecimal percentualAplicado = totalDescontos
                    .multiply(new BigDecimal("100"))
                    .divide(totalItens, 2, RoundingMode.HALF_UP);

            if (percentualAplicado.compareTo(percentualMax) > 0) {
                throw new BusinessException(
                        "O desconto aplicado (" + percentualAplicado + "%) excede o limite permitido para o usuário (" + percentualMax + "%).");
            }
        }

        totalsCalculator.recalculateWithPayments(atendimento, pagamentos);
        atendimento.setStatus(AtendimentoStatus.ENCAMINHADO);
        atendimentoRepository.save(atendimento);

        return new AtendimentoResponseDTO(atendimento);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void applyHeader(Atendimento atendimento, AtendimentoRequestDTO data) {
        Client cliente = clientRepository.findById(data.codCliente())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado."));
        Clinic clinica = clinicRepository.findById(data.codClinica())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clínica não encontrada."));

        atendimento.setDataConsultaExame(data.dataConsultaExame());
        atendimento.setTurno(data.turno());
        atendimento.setObservacao(data.observacao());
        atendimento.setCliente(cliente);
        atendimento.setClinica(clinica);
        atendimento.setParcelas(data.parcelas());
    }

    private void applyItems(Atendimento atendimento, List<AtendimentoConsultaExameRequestDTO> itemDtos) {
        for (AtendimentoConsultaExameRequestDTO dto : itemDtos) {
            Doctor doctor = null;
            if (dto.codMedico() != null && dto.codMedico() != 0) {
                doctor = doctorRepository.findById(dto.codMedico())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Médico não encontrado: id=" + dto.codMedico()));
            }

            MedicalProcedure procedure = medicalProcedureRepository.findById(dto.codMedicalProcedure())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Procedimento/exame não encontrado: id=" + dto.codMedicalProcedure()));

            AtendimentoConsultaExame item = new AtendimentoConsultaExame();
            item.setAtendimento(atendimento);
            item.setDoctor(doctor);
            item.setMedicalProcedure(procedure);
            item.setTransferValue(dto.transferValue());
            item.setPrice(dto.price());
            item.setTransferValueCard(dto.transferValueCard());
            item.setPriceCard(dto.priceCard());

            atendimento.getItens().add(item);
        }
    }

    private void requireAberto(Atendimento atendimento) {
        if (atendimento.getStatus() != AtendimentoStatus.ABERTO) {
            throw new BusinessException(
                    "Este atendimento não pode ser modificado pois seu status é " +
                    atendimento.getStatus().name() + ".");
        }
    }

    private Atendimento findOrThrow(Long id) {
        return atendimentoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atendimento não encontrado."));
    }
}
