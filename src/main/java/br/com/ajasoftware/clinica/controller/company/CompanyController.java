package br.com.ajasoftware.clinica.controller.company;

import br.com.ajasoftware.clinica.domain.dto.company.CompanyDTO;
import br.com.ajasoftware.clinica.service.company.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/clinica/company")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CADASTROS')")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<CompanyDTO> getCompany() {
        CompanyDTO company = companyService.getCompanyInfo();
        if (company == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(company);
    }

    @PutMapping
    public ResponseEntity<CompanyDTO> saveOrUpdate(@RequestBody @Valid CompanyDTO data) {
        CompanyDTO savedCompany = companyService.saveOrUpdate(data);
        return ResponseEntity.ok(savedCompany);
    }

    @PostMapping("/logo")
    public ResponseEntity<CompanyDTO> uploadLogo(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || !file.getContentType().startsWith("image/")) {
            return ResponseEntity.badRequest().build();
        }

        CompanyDTO updatedCompany = companyService.uploadLogo(file);
        return ResponseEntity.ok(updatedCompany);
    }

    @GetMapping("/logo")
    @PreAuthorize("permitAll()")
    public ResponseEntity<byte[]> getLogo() {
        try {
            CompanyDTO company = companyService.getCompanyInfo();
            if (company == null || company.logoUrl() == null) {
                return ResponseEntity.notFound().build();
            }

            // Extract filename from stored path
            String fileName = company.logoUrl().replace("/logos/", "");
            Path path = Paths.get("uploads/logos/").resolve(fileName);

            if (!Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }

            // DYNAMIC DETECTION: probe the content type based on the file bytes/extension
            String contentType = Files.probeContentType(path);

            // Fallback to generic image if detection fails
            if (contentType == null) {
                contentType = "image/png";
            }

            byte[] imageBytes = Files.readAllBytes(path);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageBytes);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}