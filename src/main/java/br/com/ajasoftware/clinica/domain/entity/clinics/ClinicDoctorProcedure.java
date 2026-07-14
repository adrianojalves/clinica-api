package br.com.ajasoftware.clinica.domain.entity.clinics;

import br.com.ajasoftware.clinica.domain.entity.doctors.Doctor;
import br.com.ajasoftware.clinica.domain.entity.medical.procedures.MedicalProcedure;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "table_clinic_doctor_procedure",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_clinic_doctor_procedure",
                        columnNames = {"clinic_id", "_doctor_id_distinct", "medical_procedure_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClinicDoctorProcedure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_procedure_id", nullable = false)
    private MedicalProcedure medicalProcedure;

    @Column(name = "transfer_value")
    private BigDecimal transferValue;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "transfer_value_card")
    private BigDecimal transferValueCard;

    @Column(name = "price_card", nullable = false)
    private BigDecimal priceCard;

    @Column(name = "price_partner", nullable = false)
    private BigDecimal pricePartner;
}