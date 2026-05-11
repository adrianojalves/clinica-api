package br.com.ajasoftware.clinica.domain.filter.client;

import br.com.ajasoftware.clinica.domain.filter.FilterBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientFilter extends FilterBase {
    private String cpf;
    private Boolean status;
}