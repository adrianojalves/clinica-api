package br.com.ajasoftware.clinica.domain.entity.clinics;

import br.com.ajasoftware.clinica.domain.entity.address.Address;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CNPJ;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a Clinic in the system.
 */
@Table(name = "table_clinica")
@Entity(name = "Clinic")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Clinic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false)
    private String name;

    private String cnpj;

    @Column(nullable = false)
    private String fone1;

    private String fone2;

    private String site;

    private String email;

    @Column(name = "ativo", nullable = false)
    private Boolean active;

    // The Address fields will be injected directly into table_clinica
    @Embedded
    private Address address;

    private BigDecimal percentual;

    @Column(name = "data_criacao", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "data_atualizacao")
    private LocalDateTime updatedAt;

    /**
     * Executes automatically before the entity is persisted for the first time.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.active = true; // A new clinic is active by default
    }

    /**
     * Executes automatically before the entity is updated.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}