package br.com.ajasoftware.clinica.controller.administrador;

import br.com.ajasoftware.clinica.domain.dto.administrador.ImportResultDTO;
import br.com.ajasoftware.clinica.service.administrador.AdministradorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdministradorController {

    private final AdministradorService administradorService;

    @PostMapping("/importar-tabela")
    public ResponseEntity<ImportResultDTO> importarTabela(@RequestParam("file") MultipartFile file) {
        ImportResultDTO result = administradorService.importarTabela(file);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/deletar-dados")
    public ResponseEntity<Void> deletarDados() {
        administradorService.deleteAllData();
        return ResponseEntity.noContent().build();
    }
}
