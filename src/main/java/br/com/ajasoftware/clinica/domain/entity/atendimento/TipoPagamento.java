package br.com.ajasoftware.clinica.domain.entity.atendimento;

public enum TipoPagamento {
    DINHEIRO,
    CARTAO_CREDITO,
    CARTAO_DEBITO,
    PIX;

    public boolean isCartao() {
        return this == CARTAO_CREDITO || this == CARTAO_DEBITO;
    }

    public boolean isParcelavel() {
        return this == CARTAO_CREDITO;
    }
}
