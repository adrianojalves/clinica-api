package br.com.ajasoftware.clinica.repository;

import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AtendimentoPagamentoRepository extends JpaRepository<AtendimentoPagamento, Long> {

    List<AtendimentoPagamento> findByAtendimentoId(Long atendimentoId);

    boolean existsByAtendimentoId(Long atendimentoId);
}
