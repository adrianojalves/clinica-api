package br.com.ajasoftware.clinica.service.relatorio;

import br.com.ajasoftware.clinica.domain.dto.atendimento.AtendimentoReciboItemDTO;
import br.com.ajasoftware.clinica.domain.entity.client.Client;
import br.com.ajasoftware.clinica.domain.entity.clinics.Clinic;
import br.com.ajasoftware.clinica.domain.entity.atendimento.Atendimento;
import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoPagamento;
import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoStatus;
import br.com.ajasoftware.clinica.domain.entity.atendimento.TipoPagamento;
import br.com.ajasoftware.clinica.domain.entity.company.Company;
import br.com.ajasoftware.clinica.repository.CompanyRepository;
import br.com.ajasoftware.clinica.repository.UserRepository;
import br.com.ajasoftware.clinica.repository.RoleRepository;
import br.com.ajasoftware.clinica.repository.AtendimentoRepository;
import br.com.ajasoftware.clinica.repository.AtendimentoPagamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {
        ReportRenderingService.class,
        org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration.class
})
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        FlywayAutoConfiguration.class
})
class ReportRenderingUnitTest {

    @MockBean
    private CompanyRepository companyRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private AtendimentoRepository atendimentoRepository;

    @MockBean
    private AtendimentoPagamentoRepository atendimentoPagamentoRepository;

    @Autowired
    private ReportRenderingService reportRenderingService;

    @BeforeEach
    void setUp() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testRenderEncaminhamentoWithWatermark() throws Exception {
        Company company = new Company();
        company.setCorporateName("Clinica Teste Ltda");
        when(companyRepository.findAll()).thenReturn(List.of(company));

        Client cliente = new Client();
        cliente.setName("João da Silva");

        Clinic clinica = new Clinic();
        clinica.setName("Unidade Centro");
        clinica.setCnpj("12.345.678/0001-90");

        Atendimento atendimento = new Atendimento();
        atendimento.setCliente(cliente);
        atendimento.setClinica(clinica);
        atendimento.setDataEmissao(LocalDateTime.now());
        atendimento.setStatus(AtendimentoStatus.ABERTO);
        atendimento.setItens(new ArrayList<>());

        Map<String, Object> vars = new HashMap<>();
        vars.put("atendimento", atendimento);
        vars.put("isOrcamento", true);
        vars.put("observacaoCompleta", "Observação de Teste");

        byte[] pdfBytes = reportRenderingService.render("atendimento/encaminhamento", vars);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        assertEquals(0x25, pdfBytes[0]);

        File outputDir = new File("target/test-reports");
        outputDir.mkdirs();
        try (FileOutputStream fos = new FileOutputStream(new File(outputDir, "encaminhamento-test.pdf"))) {
            fos.write(pdfBytes);
        }
    }

    @Test
    void testRenderReciboWithWatermark() throws Exception {
        Company company = new Company();
        company.setCorporateName("Clinica Teste Ltda");
        when(companyRepository.findAll()).thenReturn(List.of(company));

        Client cliente = new Client();
        cliente.setName("Maria de Souza");
        cliente.setCpf("987.654.321-00");

        Atendimento atendimento = new Atendimento();
        atendimento.setCliente(cliente);
        atendimento.setStatus(AtendimentoStatus.ABERTO);

        List<AtendimentoPagamento> pagamentos = new ArrayList<>();
        AtendimentoPagamento pag = new AtendimentoPagamento();
        pag.setTipoPagamento(TipoPagamento.DINHEIRO);
        pag.setValor(new BigDecimal("150.00"));
        pag.setValorDesconto(new BigDecimal("10.00"));
        pagamentos.add(pag);

        List<AtendimentoReciboItemDTO> items = new ArrayList<>();
        items.add(new AtendimentoReciboItemDTO("Consulta Geral", new BigDecimal("150.00"), new BigDecimal("10.00"), new BigDecimal("140.00")));

        Map<String, Object> vars = new HashMap<>();
        vars.put("atendimento", atendimento);
        vars.put("pagamentos", pagamentos);
        vars.put("totalBruto", new BigDecimal("150.00"));
        vars.put("totalDesconto", new BigDecimal("10.00"));
        vars.put("totalEfetivo", new BigDecimal("140.00"));
        vars.put("originalTotal", new BigDecimal("150.00"));
        vars.put("items", items);
        vars.put("isOrcamento", true);

        byte[] pdfBytes = reportRenderingService.render("atendimento/recibo", vars);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        assertEquals(0x25, pdfBytes[0]);

        File outputDir = new File("target/test-reports");
        outputDir.mkdirs();
        try (FileOutputStream fos = new FileOutputStream(new File(outputDir, "recibo-test.pdf"))) {
            fos.write(pdfBytes);
        }
    }

    @Test
    void testRenderRelatorioSinteticoItens() throws Exception {
        Company company = new Company();
        company.setCorporateName("Clinica Teste Ltda");
        when(companyRepository.findAll()).thenReturn(List.of(company));

        List<br.com.ajasoftware.clinica.domain.dto.relatorio.atendimento.AtendimentoSinteticoItensReportItemDTO> itens = List.of(
                new br.com.ajasoftware.clinica.domain.dto.relatorio.atendimento.AtendimentoSinteticoItensReportItemDTO(
                        java.time.LocalDate.now(),
                        "Paciente Teste",
                        "Exame A, Exame B",
                        new BigDecimal("250.00"),
                        "Dinheiro, PIX",
                        "Clínica Centro"
                )
        );

        Map<String, Object> vars = new HashMap<>();
        vars.put("itens", itens);
        vars.put("filtrosAplicados", Map.of("Clínica", "Clínica Centro"));
        vars.put("sumValor", new BigDecimal("250.00"));

        byte[] pdfBytes = reportRenderingService.render("financeiro/atendimento/relatorio-sintetico-itens", vars);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        assertEquals(0x25, pdfBytes[0]);

        File outputDir = new File("target/test-reports");
        outputDir.mkdirs();
        try (FileOutputStream fos = new FileOutputStream(new File(outputDir, "sintetico-itens-test.pdf"))) {
            fos.write(pdfBytes);
        }
    }
}
