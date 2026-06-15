package br.com.ajasoftware.clinica.repository;

import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface AtendimentoPagamentoRepository extends JpaRepository<AtendimentoPagamento, Long> {

    List<AtendimentoPagamento> findByAtendimentoId(Long atendimentoId);

    boolean existsByAtendimentoId(Long atendimentoId);

    @Query("SELECT COALESCE(SUM(p.valorDesconto), 0) FROM AtendimentoPagamento p WHERE p.atendimento.id = :atendimentoId")
    BigDecimal sumDescontoByAtendimentoId(@Param("atendimentoId") Long atendimentoId);

    @Query("SELECT COALESCE(SUM(p.valorDesconto), 0) FROM AtendimentoPagamento p WHERE p.atendimento.id = :atendimentoId AND p.id <> :excludeId")
    BigDecimal sumDescontoByAtendimentoIdExcluding(@Param("atendimentoId") Long atendimentoId, @Param("excludeId") Long excludeId);
}
