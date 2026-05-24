package br.com.ajasoftware.clinica.domain.entity.atendimento;

import br.com.ajasoftware.clinica.domain.entity.doctors.Doctor;
import br.com.ajasoftware.clinica.domain.entity.medical.procedures.MedicalProcedure;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Represents a detail item of an Atendimento (Service/Appointment).
 * This is the detail entity of the Atendimento Master-Detail relationship.
 * Each item links a medical procedure (and optionally a doctor) to a service order.
 */
@Entity
@Table(name = "table_atendimento_consulta_exame")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AtendimentoConsultaExame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendimento_id", nullable = false)
    private Atendimento atendimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_procedure_id", nullable = false)
    private MedicalProcedure medicalProcedure;

    @Column(name = "transfer_value", precision = 10, scale = 2)
    private BigDecimal transferValue;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "transfer_value_card", precision = 10, scale = 2)
    private BigDecimal transferValueCard;

    @Column(name = "price_card", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceCard;
}
