package br.com.ajasoftware.clinica.service.relatorio.repasse;

import br.com.ajasoftware.clinica.domain.dto.relatorio.repasse.RepasseClinicaGroupDTO;
import br.com.ajasoftware.clinica.domain.dto.relatorio.repasse.RepasseReportFilter;
import br.com.ajasoftware.clinica.domain.dto.relatorio.repasse.RepasseReportItemDTO;
import br.com.ajasoftware.clinica.domain.dto.relatorio.repasse.RepasseReportType;
import br.com.ajasoftware.clinica.domain.entity.clinics.PeriodPayment;
import br.com.ajasoftware.clinica.repository.AtendimentoRepository;
import br.com.ajasoftware.clinica.repository.ClinicRepository;
import br.com.ajasoftware.clinica.service.relatorio.ReportRenderingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepasseReportServiceTest {

    @Mock
    private AtendimentoRepository atendimentoRepository;

    @Mock
    private ClinicRepository clinicRepository;

    @Mock
    private ReportRenderingService reportRenderingService;

    @InjectMocks
    private RepasseReportService repasseReportService;

    @Captor
    private ArgumentCaptor<Map<String, Object>> variablesCaptor;

    private List<RepasseReportItemDTO> mockItens;

    @BeforeEach
    void setUp() {
        LocalDateTime day1 = LocalDateTime.of(2026, 7, 13, 10, 0); // Monday
        LocalDateTime day2 = LocalDateTime.of(2026, 7, 14, 15, 30); // Tuesday
        LocalDateTime day15 = LocalDateTime.of(2026, 7, 15, 9, 0); // Fortnight 1 end
        LocalDateTime day16 = LocalDateTime.of(2026, 7, 16, 14, 0); // Fortnight 2 start

        mockItens = List.of(
                new RepasseReportItemDTO(1L, "Client A", "Clinic Alpha", day1, new BigDecimal("100.00"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("80.00"), BigDecimal.ZERO, PeriodPayment.DIARIO),
                new RepasseReportItemDTO(2L, "Client B", "Clinic Alpha", day2, new BigDecimal("200.00"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("160.00"), BigDecimal.ZERO, PeriodPayment.DIARIO),
                new RepasseReportItemDTO(3L, "Client C", "Clinic Alpha", day15, new BigDecimal("150.00"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("120.00"), BigDecimal.ZERO, PeriodPayment.DIARIO),
                new RepasseReportItemDTO(4L, "Client D", "Clinic Alpha", day16, new BigDecimal("300.00"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("240.00"), BigDecimal.ZERO, PeriodPayment.DIARIO)
        );
    }

    @Test
    @DisplayName("Should generate synthetic report without sub-grouping")
    void generateSyntheticReport() {
        // Arrange
        RepasseReportFilter filter = new RepasseReportFilter();
        filter.setTipoRelatorio(RepasseReportType.SINTETICO);

        when(atendimentoRepository.findForRepasseReport(any(), any(), any())).thenReturn(mockItens);
        when(reportRenderingService.render(eq("financeiro/repasse/relatorio-repasse"), variablesCaptor.capture())).thenReturn(new byte[]{1, 2, 3});

        // Act
        byte[] pdf = repasseReportService.generate(filter);

        // Assert
        assertNotNull(pdf);
        assertEquals(3, pdf.length);

        Map<String, Object> vars = variablesCaptor.getValue();
        List<RepasseClinicaGroupDTO> grupos = (List<RepasseClinicaGroupDTO>) vars.get("grupos");
        assertEquals(1, grupos.size());

        RepasseClinicaGroupDTO group = grupos.get(0);
        assertEquals("Clinic Alpha", group.clinicaName());
        assertEquals(1, group.periodGroups().size());
        assertNull(group.periodGroups().get(0).periodLabel());
        assertEquals(4, group.periodGroups().get(0).itens().size());
    }

    @Test
    @DisplayName("Should group items by day when ANALITICO and DIARIO")
    void generateAnaliticoDiario() {
        // Arrange
        RepasseReportFilter filter = new RepasseReportFilter();
        filter.setTipoRelatorio(RepasseReportType.ANALITICO);

        when(atendimentoRepository.findForRepasseReport(any(), any(), any())).thenReturn(mockItens);
        when(reportRenderingService.render(eq("financeiro/repasse/relatorio-repasse"), variablesCaptor.capture())).thenReturn(new byte[]{4, 5});

        // Act
        repasseReportService.generate(filter);

        // Assert
        Map<String, Object> vars = variablesCaptor.getValue();
        List<RepasseClinicaGroupDTO> grupos = (List<RepasseClinicaGroupDTO>) vars.get("grupos");
        RepasseClinicaGroupDTO group = grupos.get(0);
        
        // Items are DIARIO, should have 4 separate period groups (one for each day)
        assertEquals(4, group.periodGroups().size());
        assertEquals("Dia 13/07/2026", group.periodGroups().get(0).periodLabel());
        assertEquals("Dia 14/07/2026", group.periodGroups().get(1).periodLabel());
        assertEquals("Dia 15/07/2026", group.periodGroups().get(2).periodLabel());
        assertEquals("Dia 16/07/2026", group.periodGroups().get(3).periodLabel());
    }

    @Test
    @DisplayName("Should group items by week when ANALITICO and SEMANAL")
    void generateAnaliticoSemanal() {
        // Arrange
        RepasseReportFilter filter = new RepasseReportFilter();
        filter.setTipoRelatorio(RepasseReportType.ANALITICO);

        // Mock items to be SEMANAL
        List<RepasseReportItemDTO> semanalItens = List.of(
                new RepasseReportItemDTO(1L, "A", "Clinic Beta", LocalDateTime.of(2026, 7, 13, 10, 0), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, PeriodPayment.SEMANAL), // Mon (Week 1)
                new RepasseReportItemDTO(2L, "B", "Clinic Beta", LocalDateTime.of(2026, 7, 19, 15, 30), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, PeriodPayment.SEMANAL), // Sun (Week 1)
                new RepasseReportItemDTO(3L, "C", "Clinic Beta", LocalDateTime.of(2026, 7, 20, 9, 0), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, PeriodPayment.SEMANAL)   // Mon (Week 2)
        );

        when(atendimentoRepository.findForRepasseReport(any(), any(), any())).thenReturn(semanalItens);
        when(reportRenderingService.render(eq("financeiro/repasse/relatorio-repasse"), variablesCaptor.capture())).thenReturn(new byte[]{});

        // Act
        repasseReportService.generate(filter);

        // Assert
        Map<String, Object> vars = variablesCaptor.getValue();
        List<RepasseClinicaGroupDTO> grupos = (List<RepasseClinicaGroupDTO>) vars.get("grupos");
        RepasseClinicaGroupDTO group = grupos.get(0);

        // Items should be grouped into 2 weeks
        assertEquals(2, group.periodGroups().size());
        assertEquals("Semana de 13/07/2026 a 19/07/2026", group.periodGroups().get(0).periodLabel());
        assertEquals("Semana de 20/07/2026 a 26/07/2026", group.periodGroups().get(1).periodLabel());
        assertEquals(2, group.periodGroups().get(0).itens().size());
        assertEquals(1, group.periodGroups().get(1).itens().size());
    }

    @Test
    @DisplayName("Should group items by fortnight when ANALITICO and QUINZENAL")
    void generateAnaliticoQuinzenal() {
        // Arrange
        RepasseReportFilter filter = new RepasseReportFilter();
        filter.setTipoRelatorio(RepasseReportType.ANALITICO);

        // Mock items to be QUINZENAL
        List<RepasseReportItemDTO> quinzenalItens = List.of(
                new RepasseReportItemDTO(1L, "A", "Clinic Beta", LocalDateTime.of(2026, 7, 5, 10, 0), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, PeriodPayment.QUINZENAL), // Fortnight 1
                new RepasseReportItemDTO(2L, "B", "Clinic Beta", LocalDateTime.of(2026, 7, 15, 15, 30), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, PeriodPayment.QUINZENAL), // Fortnight 1
                new RepasseReportItemDTO(3L, "C", "Clinic Beta", LocalDateTime.of(2026, 7, 16, 9, 0), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, PeriodPayment.QUINZENAL)   // Fortnight 2
        );

        when(atendimentoRepository.findForRepasseReport(any(), any(), any())).thenReturn(quinzenalItens);
        when(reportRenderingService.render(eq("financeiro/repasse/relatorio-repasse"), variablesCaptor.capture())).thenReturn(new byte[]{});

        // Act
        repasseReportService.generate(filter);

        // Assert
        Map<String, Object> vars = variablesCaptor.getValue();
        List<RepasseClinicaGroupDTO> grupos = (List<RepasseClinicaGroupDTO>) vars.get("grupos");
        RepasseClinicaGroupDTO group = grupos.get(0);

        // Items should be grouped into 2 fortnights
        assertEquals(2, group.periodGroups().size());
        assertEquals("Quinzena de 01/07/2026 a 15/07/2026", group.periodGroups().get(0).periodLabel());
        assertEquals("Quinzena de 16/07/2026 a 31/07/2026", group.periodGroups().get(1).periodLabel());
    }
}
