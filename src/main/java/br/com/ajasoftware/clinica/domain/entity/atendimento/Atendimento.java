package br.com.ajasoftware.clinica.domain.entity.atendimento;

import br.com.ajasoftware.clinica.domain.entity.User;
import br.com.ajasoftware.clinica.domain.entity.client.Client;
import br.com.ajasoftware.clinica.domain.entity.clinics.Clinic;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "table_atendimento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Atendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_emissao", nullable = false)
    private LocalDateTime dataEmissao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @Column(name = "data_consulta_exame")
    private LocalDate dataConsultaExame;

    @Enumerated(EnumType.STRING)
    @Column(name = "turno", length = 20)
    private TurnoAtendimento turno;

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Client cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinica_id", nullable = false)
    private Clinic clinica;

    @Column(nullable = false)
    private Integer parcelas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AtendimentoStatus status;

    @Column(name = "total_transfer_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalTransferValue;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "total_transfer_value_card", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalTransferValueCard;

    @Column(name = "total_price_card", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPriceCard;

    @Column(name = "valor_desconto", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDesconto = BigDecimal.ZERO;

    @Column(name = "valor_acrescimo", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorAcrescimo = BigDecimal.ZERO;

    @Column(name = "codigo_guia")
    private Long codigoGuia;

    @OneToMany(mappedBy = "atendimento", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AtendimentoConsultaExame> itens = new ArrayList<>();

    @Column(name = "data_criacao", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "data_atualizacao")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.dataEmissao == null) {
            this.dataEmissao = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = AtendimentoStatus.ABERTO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
