package br.com.ajasoftware.clinica.service.atendimento;

import br.com.ajasoftware.clinica.domain.entity.User;
import br.com.ajasoftware.clinica.domain.entity.atendimento.Atendimento;
import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoStatus;
import br.com.ajasoftware.clinica.domain.entity.client.Client;
import br.com.ajasoftware.clinica.domain.entity.clinics.Clinic;
import br.com.ajasoftware.clinica.domain.entity.log.Log;
import br.com.ajasoftware.clinica.exceptions.BusinessException;
import br.com.ajasoftware.clinica.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AtendimentoServiceDeleteTest {

    @Mock private AtendimentoRepository atendimentoRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private ClinicRepository clinicRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private MedicalProcedureRepository medicalProcedureRepository;
    @Mock private AtendimentoPagamentoRepository atendimentoPagamentoRepository;
    @Mock private AtendimentoTotalsCalculator totalsCalculator;
    @Mock private LogRepository logRepository;

    @InjectMocks
    private AtendimentoService atendimentoService;

    private User mockUser;
    private Client mockClient;
    private Clinic mockClinic;
    private Atendimento mockAtendimento;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("Admin User");

        mockClient = new Client();
        mockClient.setId(10L);
        mockClient.setName("João da Silva");

        mockClinic = new Clinic();
        mockClinic.setId(2L);
        mockClinic.setName("Clínica Central");

        mockAtendimento = new Atendimento();
        mockAtendimento.setId(123L);
        mockAtendimento.setCliente(mockClient);
        mockAtendimento.setClinica(mockClinic);
        mockAtendimento.setDataConsultaExame(LocalDate.of(2026, 7, 8));
        mockAtendimento.setTotalPrice(new BigDecimal("150.00"));
        mockAtendimento.setTotalPriceCard(new BigDecimal("160.00"));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should delete ABERTO appointment without creating log")
    void deleteAbertoAppointment() {
        mockAtendimento.setStatus(AtendimentoStatus.ABERTO);
        when(atendimentoRepository.findById(123L)).thenReturn(Optional.of(mockAtendimento));

        atendimentoService.delete(123L);

        verify(atendimentoRepository).delete(mockAtendimento);
        verifyNoInteractions(logRepository);
    }

    @Test
    @DisplayName("Should delete ENCAMINHADO appointment and create log")
    void deleteEncaminhadoAppointment() {
        // Setup security context
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockUser);
        SecurityContextHolder.setContext(securityContext);

        mockAtendimento.setStatus(AtendimentoStatus.ENCAMINHADO);
        when(atendimentoRepository.findById(123L)).thenReturn(Optional.of(mockAtendimento));

        atendimentoService.delete(123L);

        verify(atendimentoRepository).delete(mockAtendimento);
        
        ArgumentCaptor<Log> logCaptor = ArgumentCaptor.forClass(Log.class);
        verify(logRepository).save(logCaptor.capture());
        
        Log savedLog = logCaptor.getValue();
        assertNotNull(savedLog);
        assertEquals(mockUser, savedLog.getUser());
        assertTrue(savedLog.getLog().contains("Atendimento ENCAMINHADO excluído"));
        assertTrue(savedLog.getLog().contains("ID: 123"));
        assertTrue(savedLog.getLog().contains("Cliente: João da Silva"));
        assertTrue(savedLog.getLog().contains("Clínica: Clínica Central"));
        assertTrue(savedLog.getLog().contains("Data Consulta: 08/07/2026"));
        assertTrue(savedLog.getLog().contains("Valor Total (Dinheiro): R$ 150,00"));
        assertTrue(savedLog.getLog().contains("Valor Total (Cartão): R$ 160,00"));
    }
}
