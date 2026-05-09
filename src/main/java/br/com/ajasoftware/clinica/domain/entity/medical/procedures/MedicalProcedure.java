package br.com.ajasoftware.clinica.domain.entity.medical.procedures;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "table_medical_procedure")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalProcedure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private ProcedureType type;

    private Boolean active;

    @Column(name = "data_criacao", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "data_atualizacao")
    private LocalDateTime updatedAt;

    /**
     * Executes before inserting into the database to ensure default values.
     */
    @PrePersist
    public void prePersist() {
        if (this.active == null) {
            this.active = true;
        }
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}