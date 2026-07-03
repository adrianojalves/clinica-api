package br.com.ajasoftware.clinica.controller.backup;

import br.com.ajasoftware.clinica.repository.UserRepository;
import br.com.ajasoftware.clinica.service.backup.BackupService;
import br.com.ajasoftware.clinica.service.security.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BackupController.class)
@AutoConfigureMockMvc(addFilters = false)
class BackupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BackupService backupService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @DisplayName("Should return 200 OK and sql file when backup is generated")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBackupSqlSuccessfully() throws Exception {
        String mockSql = "SET FOREIGN_KEY_CHECKS = 0;\nCREATE TABLE test;\nSET FOREIGN_KEY_CHECKS = 1;\n";
        Mockito.when(backupService.generateBackupSql()).thenReturn(mockSql);

        mockMvc.perform(get("/api/admin/backup"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment; filename=backup_clinica_")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString(".sql")))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().string(mockSql));
    }
}
