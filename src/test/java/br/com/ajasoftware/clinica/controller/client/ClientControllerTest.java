package br.com.ajasoftware.clinica.controller.client;

import br.com.ajasoftware.clinica.domain.dto.client.ClientRequestDTO;
import br.com.ajasoftware.clinica.domain.dto.client.ClientResponseDTO;
import br.com.ajasoftware.clinica.domain.dto.clinics.AddressDataDTO;
import br.com.ajasoftware.clinica.domain.entity.client.BiologicalSex;
import br.com.ajasoftware.clinica.repository.UserRepository;
import br.com.ajasoftware.clinica.service.client.ClientService;
import br.com.ajasoftware.clinica.service.security.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientController.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClientService clientService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @DisplayName("Should allow ATENDIMENTO role to find client by CPF")
    @WithMockUser(roles = "ATENDIMENTO")
    void shouldAllowAtendimentoToFindByCpf() throws Exception {
        var response = new ClientResponseDTO(1L, "Client Test", null, "123456", "11122233344", null, null, BiologicalSex.MASCULINO, null, true, null);
        Mockito.when(clientService.findByCpf("11122233344")).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/clinica/clients/by-cpf/11122233344"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should allow ATENDIMENTO role to create a client")
    @WithMockUser(roles = "ATENDIMENTO")
    void shouldAllowAtendimentoToCreateClient() throws Exception {
        var addressDTO = new AddressDataDTO("Av. Paulista", "Bela Vista", "01310100", "São Paulo", "SP", "", "1000");
        var request = new ClientRequestDTO("Client Test", null, "123456", "11122233344", null, null, BiologicalSex.MASCULINO, null, addressDTO);
        var response = new ClientResponseDTO(1L, "Client Test", null, "123456", "11122233344", null, null, BiologicalSex.MASCULINO, null, true, addressDTO);

        Mockito.when(clientService.create(any(ClientRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/clinica/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should forbid other roles (like USER) from creating a client")
    @WithMockUser(roles = "USER")
    void shouldForbidUserFromCreatingClient() throws Exception {
        var addressDTO = new AddressDataDTO("Av. Paulista", "Bela Vista", "01310100", "São Paulo", "SP", "", "1000");
        var request = new ClientRequestDTO("Client Test", null, "123456", "11122233344", null, null, BiologicalSex.MASCULINO, null, addressDTO);

        mockMvc.perform(post("/api/clinica/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
