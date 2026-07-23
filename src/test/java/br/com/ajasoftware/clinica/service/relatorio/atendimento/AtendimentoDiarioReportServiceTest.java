package br.com.ajasoftware.clinica.service.relatorio.atendimento;

import br.com.ajasoftware.clinica.domain.dto.relatorio.atendimento.AtendimentoDiarioReportFilter;
import br.com.ajasoftware.clinica.domain.entity.atendimento.Atendimento;
import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoPagamento;
import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoStatus;
import br.com.ajasoftware.clinica.domain.entity.atendimento.TipoPagamento;
import br.com.ajasoftware.clinica.domain.entity.client.Client;
import br.com.ajasoftware.clinica.domain.entity.clinics.Clinic;
import br.com.ajasoftware.clinica.repository.AtendimentoPagamentoRepository;
import br.com.ajasoftware.clinica.repository.AtendimentoRepository;
import br.com.ajasoftware.clinica.repository.ClientRepository;
import br.com.ajasoftware.clinica.repository.ClinicRepository;
import br.com.ajasoftware.clinica.repository.UserRepository;
import br.com.ajasoftware.clinica.service.relatorio.ReportRenderingService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AtendimentoDiarioReportServiceTest {

    @Mock
    private AtendimentoRepository atendimentoRepository;

    @Mock
    private AtendimentoPagamentoRepository pagamentoRepository;

    @Mock
    private ClinicRepository clinicRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReportRenderingService reportRenderingService;

    @InjectMocks
    private AtendimentoDiarioReportService reportService;

    @Captor
    private ArgumentCaptor<Map<String, Object>> variablesCaptor;

    @Test
    @DisplayName("Should correctly calculate daily report totals including surcharges (acrescimo)")
    void shouldCorrectlyCalculateDailyReportTotalsWithSurcharges() {
        // Arrange
        AtendimentoDiarioReportFilter filter = new AtendimentoDiarioReportFilter();
        filter.setStatus(AtendimentoStatus.ENCAMINHADO);

        Client client = new Client();
        client.setName("Client A");

        Clinic clinic = new Clinic();
        clinic.setName("Clinic Alpha");

        Atendimento atendimento = new Atendimento();
        atendimento.setId(1L);
        atendimento.setStatus(AtendimentoStatus.ENCAMINHADO);
        atendimento.setCliente(client);
        atendimento.setClinica(clinic);
        atendimento.setTotalPrice(new BigDecimal("260.00"));
        atendimento.setValorAcrescimo(new BigDecimal("60.00"));
        atendimento.setValorDesconto(BigDecimal.ZERO);
        atendimento.setItens(new ArrayList<>());

        List<AtendimentoPagamento> pagamentos = List.of(
                new AtendimentoPagamento(1L, atendimento, TipoPagamento.DINHEIRO, new BigDecimal("320.00"), BigDecimal.ZERO, 1)
        );

        when(atendimentoRepository.findEntitiesForReport(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(atendimento));
        when(pagamentoRepository.findByAtendimentoId(1L)).thenReturn(pagamentos);
        when(reportRenderingService.render(eq("financeiro/atendimento/relatorio-atendimento-diario"), variablesCaptor.capture()))
                .thenReturn(new byte[]{1, 2, 3});

        // Act
        byte[] pdf = reportService.generate(filter);

        // Assert
        assertNotNull(pdf);
        Map<String, Object> vars = variablesCaptor.getValue();
        assertNotNull(vars);

        BigDecimal sumTotalGeral = (BigDecimal) vars.get("sumTotalGeral");
        // totalGeral must be exactly 320.00 (not 380.00)
        assertEquals(new BigDecimal("320.00"), sumTotalGeral);

        List<?> itens = (List<?>) vars.get("itens");
        assertEquals(1, itens.size());
    }
}
