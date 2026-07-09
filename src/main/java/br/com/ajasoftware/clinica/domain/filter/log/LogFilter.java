package br.com.ajasoftware.clinica.domain.filter.log;

import br.com.ajasoftware.clinica.domain.filter.FilterBase;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogFilter extends FilterBase {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    private Long userId;
    private String userName;
    private String log;
}
