package br.com.ajasoftware.clinica.service.log;

import br.com.ajasoftware.clinica.domain.dto.log.LogRequestDTO;
import br.com.ajasoftware.clinica.domain.dto.log.LogResponseDTO;
import br.com.ajasoftware.clinica.domain.dto.log.LogUpdateDTO;
import br.com.ajasoftware.clinica.domain.entity.User;
import br.com.ajasoftware.clinica.domain.entity.log.Log;
import br.com.ajasoftware.clinica.domain.filter.log.LogFilter;
import br.com.ajasoftware.clinica.repository.LogRepository;
import br.com.ajasoftware.clinica.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<LogResponseDTO> listAll(LogFilter filter, Pageable pageable) {
        LocalDateTime startDate = filter.getStartDate() != null ? filter.getStartDate().atStartOfDay() : null;
        LocalDateTime endDate = filter.getEndDate() != null ? filter.getEndDate().atTime(23, 59, 59) : null;
        return logRepository.findWithFilter(filter, startDate, endDate, pageable).map(LogResponseDTO::new);
    }

    @Transactional(readOnly = true)
    public LogResponseDTO getById(Long id) {
        return new LogResponseDTO(findEntityById(id));
    }

    @Transactional
    public LogResponseDTO create(LogRequestDTO data) {
        User user = userRepository.findById(data.codUsuario())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        Log log = new Log();
        log.setLog(data.log());
        log.setUser(user);

        logRepository.save(log);
        return new LogResponseDTO(log);
    }

    @Transactional
    public LogResponseDTO update(Long id, LogUpdateDTO data) {
        Log log = findEntityById(id);
        log.setLog(data.log());
        logRepository.save(log);
        return new LogResponseDTO(log);
    }

    @Transactional
    public void delete(Long id) {
        Log log = findEntityById(id);
        logRepository.delete(log);
    }

    private Log findEntityById(Long id) {
        return logRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Log não encontrado."));
    }
}
