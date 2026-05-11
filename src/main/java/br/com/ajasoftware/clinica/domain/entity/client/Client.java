package br.com.ajasoftware.clinica.domain.entity.client;

import br.com.ajasoftware.clinica.domain.entity.address.Address;
import br.com.ajasoftware.clinica.infrastructure.security.CryptoConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Represents a Client (Patient) in the system.
 * This entity handles sensitive data with AES-256 encryption via JPA Converters.
 * CPF search is optimized using a SHA-256 hash.
 */
@Entity
@Table(name = "table_cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "social_name")
    private String socialName;

    // Encrypted sensitive fields using our custom CryptoConverter
    @Convert(converter = CryptoConverter.class)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String rg;

    @Convert(converter = CryptoConverter.class)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String cpf;

    @Convert(converter = CryptoConverter.class)
    @Column(columnDefinition = "TEXT")
    private String phone;

    @Convert(converter = CryptoConverter.class)
    @Column(columnDefinition = "TEXT")
    private String email;

    // Biological sex is encrypted at rest
    @Enumerated(EnumType.STRING)
    @Convert(converter = CryptoConverter.class)
    @Column(name = "biological_sex", nullable = false, columnDefinition = "TEXT")
    private BiologicalSex biologicalSex;

    // Sexual orientation is encrypted at rest
    @Enumerated(EnumType.STRING)
    @Convert(converter = CryptoConverter.class)
    @Column(name = "sexual_orientation", columnDefinition = "TEXT")
    private SexualOrientation sexualOrientation;

    // SHA-256 Hash for unique constraint and fast lookups without decrypting
    @Column(name = "cpf_hash", nullable = false, unique = true, length = 64)
    private String cpfHash;

    @Embedded
    private Address address;

    @Column(name = "status")
    private Boolean active = true;

    @Column(name = "data_criacao", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "data_atualizacao")
    private LocalDateTime updatedAt;

    /**
     * Automatic audit for creation timestamp.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Automatic audit for update timestamp.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}