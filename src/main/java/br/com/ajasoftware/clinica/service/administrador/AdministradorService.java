package br.com.ajasoftware.clinica.service.administrador;

import br.com.ajasoftware.clinica.domain.dto.administrador.ImportResultDTO;
import br.com.ajasoftware.clinica.domain.entity.address.Address;
import br.com.ajasoftware.clinica.domain.entity.clinics.Clinic;
import br.com.ajasoftware.clinica.domain.entity.clinics.ClinicDoctorProcedure;
import br.com.ajasoftware.clinica.domain.entity.doctors.Doctor;
import br.com.ajasoftware.clinica.domain.entity.medical.procedures.MedicalProcedure;
import br.com.ajasoftware.clinica.domain.entity.medical.procedures.ProcedureType;
import br.com.ajasoftware.clinica.repository.ClinicDoctorProcedureRepository;
import br.com.ajasoftware.clinica.repository.ClinicRepository;
import br.com.ajasoftware.clinica.repository.DoctorRepository;
import br.com.ajasoftware.clinica.repository.MedicalProcedureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdministradorService {

    private static final Set<String> INVALID_DOCTOR_NAMES = Set.of("ANESTESISTA", "SEM DESCONTO");

    private final ClinicRepository clinicRepository;
    private final DoctorRepository doctorRepository;
    private final MedicalProcedureRepository procedureRepository;
    private final ClinicDoctorProcedureRepository cdpRepository;

    @Transactional
    public ImportResultDTO importarTabela(MultipartFile file) {
        List<String[]> rows;
        try {
            rows = parseCsv(file);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Erro ao ler o arquivo CSV.");
        }

        Map<String, Clinic> clinicMap = new HashMap<>();
        clinicRepository.findAll().forEach(c -> clinicMap.put(normalize(c.getName()), c));

        Map<String, Doctor> doctorMap = new HashMap<>();
        doctorRepository.findAll().forEach(d -> doctorMap.put(normalize(d.getName()), d));

        Map<String, MedicalProcedure> procedureMap = new HashMap<>();
        procedureRepository.findAll().forEach(p -> procedureMap.put(normalize(p.getName()), p));

        // Keys of CDPs created in this batch (to skip re-checking the DB for duplicates within the same import)
        Set<String> batchCdpKeys = new HashSet<>();

        int clinicasCriadas = 0, medicosCriados = 0, procedimentosCriados = 0;
        int vinculosCriados = 0, linhasIgnoradas = 0;

        for (String[] row : rows) {
            // Skip header row
            if (normalize(col(row, 0)).equals("código") || normalize(col(row, 0)).equals("codigo")) {
                continue;
            }

            String clinicName = col(row, 2);
            String tipoStr = col(row, 3);
            String procedureName = col(row, 4);
            String doctorRaw = col(row, 5);
            String transferRaw = col(row, 7);
            String priceRaw = col(row, 8);

            if (clinicName.isEmpty() || procedureName.isEmpty()) {
                linhasIgnoradas++;
                continue;
            }

            boolean clinicIsNew = !clinicMap.containsKey(normalize(clinicName));
            Clinic clinic = resolveClinic(clinicMap, clinicName);
            if (clinicIsNew) clinicasCriadas++;

            ProcedureType type = resolveProcedureType(tipoStr);

            boolean procedureIsNew = !procedureMap.containsKey(normalize(procedureName));
            MedicalProcedure procedure = resolveProcedure(procedureMap, procedureName, type);
            if (procedureIsNew) procedimentosCriados++;

            String cleanedDoctorName = cleanDoctorName(doctorRaw);
            boolean doctorIsNew = cleanedDoctorName != null && !doctorMap.containsKey(normalize(cleanedDoctorName));
            Doctor doctor = cleanedDoctorName != null ? resolveDoctor(doctorMap, cleanedDoctorName) : null;
            if (doctorIsNew) medicosCriados++;

            BigDecimal price = parsePrice(priceRaw);
            BigDecimal transferValue = parsePrice(transferRaw);

            Long doctorId = doctor != null ? doctor.getId() : null;
            String cdpKey = buildCdpKey(clinic.getId(), doctorId, procedure.getId());

            if (batchCdpKeys.contains(cdpKey) || cdpRepository.existsCombination(clinic.getId(), doctorId, procedure.getId())) {
                linhasIgnoradas++;
                continue;
            }

            ClinicDoctorProcedure cdp = new ClinicDoctorProcedure();
            cdp.setClinic(clinic);
            cdp.setDoctor(doctor);
            cdp.setMedicalProcedure(procedure);
            cdp.setPrice(price);
            cdp.setTransferValue(transferValue);
            cdp.setPriceCard(BigDecimal.ZERO);
            cdp.setTransferValueCard(BigDecimal.ZERO);
            cdpRepository.save(cdp);
            batchCdpKeys.add(cdpKey);
            vinculosCriados++;
        }

        return new ImportResultDTO(rows.size(), clinicasCriadas, medicosCriados, procedimentosCriados, vinculosCriados, linhasIgnoradas);
    }

    private Clinic resolveClinic(Map<String, Clinic> clinicMap, String name) {
        String key = normalize(name);
        if (clinicMap.containsKey(key)) return clinicMap.get(key);

        Clinic clinic = new Clinic();
        clinic.setName(name.trim());
        clinic.setFone1("");
        clinic.setAddress(new Address("logradouro", "bairro", "45000000", "0", "complemento", "cidade", "BA"));
        clinic = clinicRepository.save(clinic);
        clinicMap.put(key, clinic);
        return clinic;
    }

    private MedicalProcedure resolveProcedure(Map<String, MedicalProcedure> procedureMap, String name, ProcedureType type) {
        String key = normalize(name);
        if (procedureMap.containsKey(key)) return procedureMap.get(key);

        MedicalProcedure procedure = new MedicalProcedure();
        procedure.setName(name.trim());
        procedure.setType(type);
        procedure = procedureRepository.save(procedure);
        procedureMap.put(key, procedure);
        return procedure;
    }

    private Doctor resolveDoctor(Map<String, Doctor> doctorMap, String cleanedName) {
        String key = normalize(cleanedName);
        if (doctorMap.containsKey(key)) return doctorMap.get(key);

        Doctor doctor = new Doctor();
        doctor.setName(cleanedName.trim());
        doctor.setCrm("");
        doctor.setAddress(new Address("logradouro", "bairro", "45000000", "0", "complemento", "cidade", "BA"));
        doctor = doctorRepository.save(doctor);
        doctorMap.put(key, doctor);
        return doctor;
    }

    private String cleanDoctorName(String raw) {
        if (raw == null || raw.isBlank()) return null;

        String name = raw.trim()
                .replace(' ', ' ')  // non-breaking space
                .replace(' ', ' '); // figure space

        // Extract only the name before parentheses
        int parenIdx = name.indexOf('(');
        if (parenIdx > 0) name = name.substring(0, parenIdx);

        // Remove Dr./Dra./Drª./Doutor[a] prefixes (case-insensitive)
        name = name.replaceAll("(?i)^Dr[aª]?\\.?\\s*", "");
        name = name.replaceAll("(?i)^Doutor[a]?\\s*", "");

        // Normalize multiple spaces
        name = name.replaceAll("\\s+", " ").trim();

        if (name.isEmpty() || name.contains("+") || INVALID_DOCTOR_NAMES.contains(name.toUpperCase())) {
            return null;
        }
        return name;
    }

    private ProcedureType resolveProcedureType(String tipoStr) {
        if (tipoStr == null || tipoStr.isBlank()) return ProcedureType.EXAME;
        return switch (tipoStr.trim().toUpperCase()) {
            case "CONSULTA" -> ProcedureType.CONSULTA;
            case "CIRURGIA" -> ProcedureType.CIRURGIA;
            case "MEDICAÇÃO", "MEDICACAO" -> ProcedureType.MEDICACAO;
            default -> ProcedureType.EXAME;
        };
    }

    private BigDecimal parsePrice(String raw) {
        if (raw == null || raw.isBlank()) return BigDecimal.ZERO;

        String s = raw
                .replace("R$", "")
                .replace(" ", "")
                .replace(" ", "")
                .replace(" ", "")
                .trim();

        if (s.isEmpty() || s.equals("-") || s.equals("−")) return BigDecimal.ZERO;

        // Brazilian format: '.' = thousands separator, ',' = decimal separator
        s = s.replace(".", "").replace(",", ".");

        try {
            return new BigDecimal(s);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private List<String[]> parseCsv(MultipartFile file) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            // Skip UTF-8 BOM if present
            reader.mark(1);
            if (reader.read() != '﻿') reader.reset();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                rows.add(line.split(";", -1));
            }
        }
        return rows;
    }

    private String col(String[] row, int index) {
        if (row.length <= index) return "";
        return row[index] == null ? "" : row[index].trim();
    }

    private String normalize(String name) {
        return name == null ? "" : name.trim().toLowerCase();
    }

    private String buildCdpKey(Long clinicId, Long doctorId, Long procedureId) {
        return clinicId + ":" + (doctorId == null ? "null" : doctorId) + ":" + procedureId;
    }
}
