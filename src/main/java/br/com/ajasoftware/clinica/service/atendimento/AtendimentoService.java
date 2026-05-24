package br.com.ajasoftware.clinica.service.atendimento;

import br.com.ajasoftware.clinica.domain.dto.atendimento.AtendimentoConsultaExameRequestDTO;
import br.com.ajasoftware.clinica.domain.dto.atendimento.AtendimentoRequestDTO;
import br.com.ajasoftware.clinica.domain.dto.atendimento.AtendimentoResponseDTO;
import br.com.ajasoftware.clinica.domain.entity.User;
import br.com.ajasoftware.clinica.domain.entity.atendimento.Atendimento;
import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoConsultaExame;
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

import java.util.List;

/**
 * Service layer for Atendimento (master) management.
 * Delegates total recalculation to {@link AtendimentoTotalsCalculator} (SRP).
 */
@Service
@RequiredArgsConstructor
public class AtendimentoService {

    private final AtendimentoRepository atendimentoRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final ClinicRepository clinicRepository;
    private final DoctorRepository doctorRepository;
    private final MedicalProcedureRepository medicalProcedureRepository;
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

        // Clear + re-add: orphanRemoval=true handles DELETE of old rows
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

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Maps header fields from the DTO onto the entity, resolving all FK references.
     */
    private void applyHeader(Atendimento atendimento, AtendimentoRequestDTO data) {
        User usuario = userRepository.findById(data.codUsuario())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        Client cliente = clientRepository.findById(data.codCliente())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado."));
        Clinic clinica = clinicRepository.findById(data.codClinica())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clínica não encontrada."));

        atendimento.setUsuario(usuario);
        atendimento.setDataConsultaExame(data.dataConsultaExame());
        atendimento.setCliente(cliente);
        atendimento.setClinica(clinica);
        atendimento.setTipoPagamento(data.tipoPagamento());
        atendimento.setParcelas(data.parcelas());
    }

    /**
     * Builds and attaches item entities from the DTO list onto the given Atendimento.
     * Assumes the items collection is empty before this call (either brand-new or cleared for update).
     */
    private void applyItems(Atendimento atendimento, List<AtendimentoConsultaExameRequestDTO> itemDtos) {
        for (AtendimentoConsultaExameRequestDTO dto : itemDtos) {
            Doctor doctor = null;
            if (dto.codMedico() != null && dto.codMedico()!=0) {
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

    /**
     * Guards any write operation: throws BusinessException if the Atendimento is not ABERTO.
     */
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
