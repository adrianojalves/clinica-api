package br.com.ajasoftware.clinica.controller.medical.procedures;

import br.com.ajasoftware.clinica.repository.UserRepository;
import br.com.ajasoftware.clinica.service.medical.procedures.MedicalProcedureService;
import br.com.ajasoftware.clinica.service.security.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MedicalProcedureController.class)
@AutoConfigureMockMvc(addFilters = false)
class MedicalProcedureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MedicalProcedureService service;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @DisplayName("Should export Excel file for medical procedures successfully")
    @WithMockUser
    void shouldExportExcelSuccessfully() throws Exception {
        byte[] mockBytes = "mock excel content".getBytes();
        Mockito.when(service.exportToExcel()).thenReturn(mockBytes);

        mockMvc.perform(get("/api/clinica/procedures/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=procedimentos_medicos.xlsx"))
                .andExpect(header().string("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(content().bytes(mockBytes));
    }

    @Test
    @DisplayName("Should import Excel file and update tags successfully")
    @WithMockUser
    void shouldImportExcelSuccessfully() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "procedimentos.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "excel data".getBytes()
        );

        Mockito.doNothing().when(service).importAndUpdateFromExcel(any());

        mockMvc.perform(multipart("/api/clinica/procedures/import")
                        .file(mockFile)
                        .with(csrf()))
                .andExpect(status().isOk());

        Mockito.verify(service, Mockito.times(1)).importAndUpdateFromExcel(any());
    }
}
