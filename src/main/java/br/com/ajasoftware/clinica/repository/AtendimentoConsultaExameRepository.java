package br.com.ajasoftware.clinica.repository;

import br.com.ajasoftware.clinica.domain.dto.atendimento.AtendimentoConsultaExameResponseDTO;
import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoConsultaExame;
import br.com.ajasoftware.clinica.domain.dto.relatorio.historico.HistoricoPacienteItemDTO;
import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoStatus;
import br.com.ajasoftware.clinica.domain.entity.medical.procedures.ProcedureType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AtendimentoConsultaExameRepository extends JpaRepository<AtendimentoConsultaExame, Long> {

    /**
     * Retrieves all items of a given Atendimento as DTOs.
     * Uses LEFT JOIN for doctor (nullable) so items without a doctor are included.
     * The JPQL "new" constructor expression builds the DTO directly in the query.
     */
    @Query("""
            SELECT new br.com.ajasoftware.clinica.domain.dto.atendimento.AtendimentoConsultaExameResponseDTO(
                i.id,
                i.atendimento.id,
                i.doctor.id,
                i.doctor.name,
                i.medicalProcedure.id,
                i.medicalProcedure.name,
                i.transferValue,
                i.price,
                i.transferValueCard,
                i.priceCard
            )
            FROM AtendimentoConsultaExame i
            LEFT JOIN i.doctor
            JOIN i.medicalProcedure
            WHERE i.atendimento.id = :atendimentoId
            """)
    List<AtendimentoConsultaExameResponseDTO> findByAtendimentoId(@Param("atendimentoId") Long atendimentoId);

    @Query("""
            SELECT i.medicalProcedure.name, COUNT(i)
            FROM AtendimentoConsultaExame i
            JOIN i.atendimento a
            LEFT JOIN i.doctor d
            WHERE a.status = 'ENCAMINHADO'
            AND (:clinicaId   IS NULL OR a.clinica.id = :clinicaId)
            AND (:doctorId    IS NULL OR d.id         = :doctorId)
            AND (:dataInicial IS NULL OR a.dataEmissao >= :dataInicial)
            AND (:dataFinal   IS NULL OR a.dataEmissao <= :dataFinal)
            GROUP BY i.medicalProcedure.id, i.medicalProcedure.name
            ORDER BY COUNT(i) DESC
            """)
    List<Object[]> countByProcedimento(
            @Param("clinicaId") Long clinicaId,
            @Param("doctorId") Long doctorId,
            @Param("dataInicial") LocalDateTime dataInicial,
            @Param("dataFinal") LocalDateTime dataFinal);

    @Query("""
            SELECT i.atendimento.clinica.id, COUNT(i)
            FROM AtendimentoConsultaExame i
            JOIN i.atendimento a
            JOIN i.medicalProcedure mp
            WHERE a.status = 'ENCAMINHADO'
            AND mp.type = :type
            AND (:clinicaId   IS NULL OR a.clinica.id  = :clinicaId)
            AND (:dataInicial IS NULL OR a.dataEmissao >= :dataInicial)
            AND (:dataFinal   IS NULL OR a.dataEmissao <= :dataFinal)
            GROUP BY i.atendimento.clinica.id
            """)
    List<Object[]> countItensByClinicaAndType(
            @Param("type") ProcedureType type,
            @Param("clinicaId") Long clinicaId,
            @Param("dataInicial") LocalDateTime dataInicial,
            @Param("dataFinal") LocalDateTime dataFinal);

    @Query("""
            SELECT i.atendimento.clinica.id, COUNT(i)
            FROM AtendimentoConsultaExame i
            JOIN i.atendimento a
            JOIN i.medicalProcedure mp
            WHERE a.status = 'ENCAMINHADO'
            AND mp.type <> :excludedType
            AND (:clinicaId   IS NULL OR a.clinica.id  = :clinicaId)
            AND (:dataInicial IS NULL OR a.dataEmissao >= :dataInicial)
            AND (:dataFinal   IS NULL OR a.dataEmissao <= :dataFinal)
            GROUP BY i.atendimento.clinica.id
            """)
    List<Object[]> countItensByClinicaExcludingType(
            @Param("excludedType") ProcedureType excludedType,
            @Param("clinicaId") Long clinicaId,
            @Param("dataInicial") LocalDateTime dataInicial,
            @Param("dataFinal") LocalDateTime dataFinal);

    @Query("""
            SELECT new br.com.ajasoftware.clinica.domain.dto.relatorio.historico.HistoricoPacienteItemDTO(
                a.id,
                a.dataEmissao,
                a.dataConsultaExame,
                mp.name,
                d.name,
                a.clinica.name,
                a.cliente.name
            )
            FROM AtendimentoConsultaExame i
            JOIN i.atendimento a
            JOIN i.medicalProcedure mp
            LEFT JOIN i.doctor d
            WHERE a.status = 'ENCAMINHADO'
            AND (:clienteId   IS NULL OR a.cliente.id = :clienteId)
            AND (:clinicaId   IS NULL OR a.clinica.id = :clinicaId)
            AND (:doctorId    IS NULL OR d.id         = :doctorId)
            AND (:dataInicial IS NULL OR a.dataEmissao >= :dataInicial)
            AND (:dataFinal   IS NULL OR a.dataEmissao <= :dataFinal)
            ORDER BY a.cliente.name, a.clinica.name, a.id, i.id
            """)
    List<HistoricoPacienteItemDTO> findHistoricoPaciente(
            @Param("clienteId") Long clienteId,
            @Param("clinicaId") Long clinicaId,
            @Param("doctorId") Long doctorId,
            @Param("dataInicial") LocalDateTime dataInicial,
            @Param("dataFinal") LocalDateTime dataFinal);
}
