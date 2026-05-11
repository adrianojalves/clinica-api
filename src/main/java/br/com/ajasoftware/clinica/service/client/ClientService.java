package br.com.ajasoftware.clinica.service.client;

import br.com.ajasoftware.clinica.domain.dto.client.ClientRequestDTO;
import br.com.ajasoftware.clinica.domain.dto.client.ClientResponseDTO;
import br.com.ajasoftware.clinica.domain.dto.client.ClientUpdateDTO;
import br.com.ajasoftware.clinica.domain.entity.address.Address;
import br.com.ajasoftware.clinica.domain.entity.client.Client;
import br.com.ajasoftware.clinica.domain.filter.client.ClientFilter;
import br.com.ajasoftware.clinica.repository.ClientRepository;
import br.com.ajasoftware.clinica.utils.SysClinicaUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service layer for Client management.
 * Handles business logic, CPF hashing for LGPD compliance, and data persistence.
 */
@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public Page<ClientResponseDTO> listWithFilter(ClientFilter filter, Pageable pageable) {
        return clientRepository.findWithFilter(filter, pageable)
                .map(ClientResponseDTO::new);
    }

    @Transactional
    public ClientResponseDTO create(ClientRequestDTO data) {
        // Validation for unique CPF using the hash strategy
        String cpfHash = SysClinicaUtils.generateSha256(data.cpf());
        if (clientRepository.findByCpfHash(cpfHash).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Já existe um cliente cadastrado com este CPF.");
        }

        Client client = new Client();
        client.setName(data.name());
        client.setSocialName(data.socialName());
        client.setRg(data.rg());
        client.setCpf(data.cpf());
        client.setCpfHash(cpfHash);
        client.setPhone(data.phone());
        client.setEmail(data.email());
        client.setBiologicalSex(data.biologicalSex());
        client.setSexualOrientation(data.sexualOrientation());

        client.setAddress(new Address(
                data.address().logradouro(),
                data.address().bairro(),
                data.address().cep(),
                data.address().numero(),
                data.address().complemento(),
                data.address().cidade(),
                data.address().uf()
        ));

        clientRepository.save(client);
        return new ClientResponseDTO(client);
    }

    @Transactional
    public ClientResponseDTO update(Long id, ClientUpdateDTO data) {
        Client client = getClientOrThrow(id);

        // Update basic and sensitive data (JPA Converter handles encryption)
        client.setName(data.name());
        client.setSocialName(data.socialName());
        client.setRg(data.rg());
        client.setPhone(data.phone());
        client.setEmail(data.email());
        client.setBiologicalSex(data.biologicalSex());
        client.setSexualOrientation(data.sexualOrientation());

        // Address update using existing pattern
        client.getAddress().updateInfo(data.address());

        // Note: If CPF is allowed to change, we must update the hash too
        if (data.cpf() != null && !data.cpf().isEmpty()) {
            client.setCpf(data.cpf());
            client.setCpfHash(SysClinicaUtils.generateSha256(data.cpf()));
        }

        return new ClientResponseDTO(client);
    }

    @Transactional
    public void changeStatus(Long id, boolean newStatus) {
        Client client = getClientOrThrow(id);
        client.setActive(newStatus);
    }

    @Transactional(readOnly = true)
    public ClientResponseDTO getById(Long id) {
        Client client = getClientOrThrow(id);
        return new ClientResponseDTO(client);
    }

    private Client getClientOrThrow(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado no sistema."));
    }
}