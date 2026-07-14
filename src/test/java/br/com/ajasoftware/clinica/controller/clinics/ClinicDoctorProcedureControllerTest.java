package br.com.ajasoftware.clinica.controller.clinics;

import br.com.ajasoftware.clinica.service.clinics.ClinicDoctorProcedureService;
import br.com.ajasoftware.clinica.service.security.TokenService;
import br.com.ajasoftware.clinica.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClinicDoctorProcedureController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClinicDoctorProcedureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClinicDoctorProcedureService service;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @DisplayName("Should export Excel file successfully")
    @WithMockUser
    void shouldExportExcelSuccessfully() throws Exception {
        byte[] mockBytes = "mock excel content".getBytes();
        Mockito.when(service.exportToExcel(1L)).thenReturn(mockBytes);

        mockMvc.perform(get("/api/clinica/clinic-procedures/export")
                        .param("clinicId", "1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=procedimentos_clinica_1.xlsx"))
                .andExpect(header().string("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(content().bytes(mockBytes));
    }

    @Test
    @DisplayName("Should import Excel file successfully")
    @WithMockUser
    void shouldImportExcelSuccessfully() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "excel data".getBytes()
        );

        Mockito.doNothing().when(service).importAndUpdateFromExcel(any());

        mockMvc.perform(multipart("/api/clinica/clinic-procedures/import")
                        .file(mockFile)
                        .with(csrf()))
                .andExpect(status().isOk());

        Mockito.verify(service, Mockito.times(1)).importAndUpdateFromExcel(any());
    }
}
