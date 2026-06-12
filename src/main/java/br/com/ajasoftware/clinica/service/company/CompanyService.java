package br.com.ajasoftware.clinica.service.company;

import br.com.ajasoftware.clinica.domain.dto.company.CompanyDTO;
import br.com.ajasoftware.clinica.domain.entity.address.Address;
import br.com.ajasoftware.clinica.domain.entity.company.Company;
import br.com.ajasoftware.clinica.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    private final String UPLOAD_DIR = "uploads/logos/";

    @Transactional(readOnly = true)
    public CompanyDTO getCompanyInfo() {
        return companyRepository.findAll().stream()
                .findFirst()
                .map(CompanyDTO::new)
                .orElse(null);
    }

    @Transactional
    public CompanyDTO saveOrUpdate(CompanyDTO data) {
        Company company = companyRepository.findAll().stream()
                .findFirst()
                .orElse(new Company());

        company.setCorporateName(data.corporateName());
        company.setTradeName(data.tradeName());
        company.setCnpj(data.cnpj());
        company.setPhone(data.phone());
        company.setEmail(data.email());
        company.setObservacao(data.observacao());

        if (company.getAddress() == null) {
            company.setAddress(new Address());
        }
        company.getAddress().updateInfo(data.address());

        companyRepository.save(company);
        return new CompanyDTO(company);
    }

    @Transactional
    public CompanyDTO uploadLogo(MultipartFile file) {
        Company company = companyRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cadastre os dados da empresa antes de enviar a logo."));

        try {
            File directory = new File(UPLOAD_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + fileName);

            Files.write(filePath, file.getBytes());

            String fileUrl = "/logos/" + fileName;

            company.setLogoUrl(fileUrl);
            companyRepository.save(company);

            return new CompanyDTO(company);

        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar a imagem da logo: " + e.getMessage());
        }
    }
}