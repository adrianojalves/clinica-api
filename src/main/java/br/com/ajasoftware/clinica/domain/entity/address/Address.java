package br.com.ajasoftware.clinica.domain.entity.address;

import br.com.ajasoftware.clinica.domain.dto.clinics.AddressDataDTO;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Value Object representing an Address.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    private String logradouro;
    private String bairro;
    private String cep;
    private String numero;
    private String complemento;
    private String cidade;
    private String uf;

    public void updateInfo(AddressDataDTO data) {
        if (data.logradouro() != null) {
            this.logradouro = data.logradouro();
        }
        if (data.bairro() != null) {
            this.bairro = data.bairro();
        }
        if (data.cep() != null) {
            this.cep = data.cep();
        }
        if (data.numero() != null) {
            this.numero = data.numero();
        }
        if (data.complemento() != null) {
            this.complemento = data.complemento();
        }
        if (data.cidade() != null) {
            this.cidade = data.cidade();
        }
        if (data.uf() != null) {
            this.uf = data.uf();
        }
    }
}