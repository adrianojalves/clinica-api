package br.com.ajasoftware.clinica.controller.relatorio.atendimento;

import br.com.ajasoftware.clinica.repository.UserRepository;
import br.com.ajasoftware.clinica.service.relatorio.atendimento.AtendimentoDiarioReportService;
import br.com.ajasoftware.clinica.service.security.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AtendimentoDiarioRelatorioController.class)
@AutoConfigureMockMvc(addFilters = false)
class AtendimentoDiarioRelatorioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AtendimentoDiarioReportService reportService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @DisplayName("Should generate daily attendance PDF report successfully")
    @WithMockUser
    void shouldGenerateDailyReportSuccessfully() throws Exception {
        byte[] mockPdfBytes = "%PDF-1.4 mock content".getBytes();
        Mockito.when(reportService.generate(any())).thenReturn(mockPdfBytes);

        mockMvc.perform(get("/api/clinica/atendimentos/relatorios/atendimento-diario")
                        .param("dataEmissao", "2026-07-20")
                        .param("status", "ENCAMINHADO"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "inline; filename=relatorio-atendimento-diario.pdf"))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(mockPdfBytes));
    }
}
