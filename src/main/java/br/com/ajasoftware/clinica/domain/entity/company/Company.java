package br.com.ajasoftware.clinica.domain.entity.company;

import br.com.ajasoftware.clinica.domain.entity.address.Address;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "table_company")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "corporate_name")
    private String corporateName; // Legal name

    @Column(name = "trade_name")
    private String tradeName; // Trade name

    private String cnpj;
    private String phone;
    private String email;

    @Column(name = "logo_url")
    private String logoUrl;

    @Embedded
    private Address address;
}