package br.com.ajasoftware.clinica.controller.clinics;

import br.com.ajasoftware.clinica.domain.dto.clinics.AddressDataDTO;
import br.com.ajasoftware.clinica.domain.dto.clinics.ClinicRequestDTO;
import br.com.ajasoftware.clinica.domain.dto.clinics.ClinicResponseDTO;
import br.com.ajasoftware.clinica.domain.entity.clinics.PeriodPayment;
import br.com.ajasoftware.clinica.service.clinics.ClinicService;
import br.com.ajasoftware.clinica.service.security.TokenService;
// Adicione a importação do UserRepository (ajuste o pacote conforme o seu projeto)
import br.com.ajasoftware.clinica.repository.UserRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClinicController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClinicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClinicService clinicService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @DisplayName("Should return 201 Created when data is fully valid")
    @WithMockUser
    void shouldCreateClinicSuccessfully() throws Exception {
        var addressDTO = new AddressDataDTO("Av. Paulista", "Bela Vista", "01310100", "São Paulo", "SP", "", "1000");
        var validRequest = new ClinicRequestDTO("Clínica Teste", "12.345.678/0001-95", "11999999999", null, null, "teste@clinica.com", BigDecimal.ZERO, PeriodPayment.SEMANAL, addressDTO);
        var expectedResponse = new ClinicResponseDTO(1L, "Clínica Teste", "12.345.678/0001-95", "11999999999", null, null, "teste@clinica.com", true, BigDecimal.ZERO, PeriodPayment.SEMANAL, addressDTO);

        Mockito.when(clinicService.create(any(ClinicRequestDTO.class))).thenReturn(expectedResponse);

        mockMvc.perform(post("/api/clinica/clinics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.cnpj").value("12.345.678/0001-95"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when CNPJ is mathematically invalid")
    @WithMockUser
    void shouldReturnBadRequestWhenCnpjIsInvalid() throws Exception {
        var addressDTO = new AddressDataDTO("Av. Paulista", "Bela Vista", "01310100", "São Paulo", "SP", "", "1000");
        var invalidRequest = new ClinicRequestDTO("Clínica Teste", "11.111.111/1111-11", "11999999999", null, null, "teste@clinica.com", BigDecimal.ZERO, null, addressDTO);

        mockMvc.perform(post("/api/clinica/clinics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].field").value("cnpj"))
                .andExpect(jsonPath("$[0].message").value("O CNPJ informado é inválido."));
    }

    @Test
    @DisplayName("Should return 409 Conflict when CNPJ already exists in the database")
    @WithMockUser
    void shouldReturnConflictWhenCnpjAlreadyExists() throws Exception {
        var addressDTO = new AddressDataDTO("Av. Paulista", "Bela Vista", "01310100", "São Paulo", "SP", "", "1000");
        var validRequest = new ClinicRequestDTO("Clínica Teste", "12.345.678/0001-95", "11999999999", null, null, "teste@clinica.com", BigDecimal.ZERO, null, addressDTO);

        Mockito.when(clinicService.create(any(ClinicRequestDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Operação falhou: O CNPJ informado já está cadastrado."));

        mockMvc.perform(post("/api/clinica/clinics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .with(csrf()))
                .andExpect(status().isConflict());
    }
}