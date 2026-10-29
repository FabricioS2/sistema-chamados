package br.edu.ifrn.chamados.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes da maquina de estados do chamado:
 * ABERTO -&gt; EM_ATENDIMENTO -&gt; RESOLVIDO e ABERTO -&gt; CANCELADO. Nenhuma outra transicao.
 */
class StatusChamadoTest {

    @ParameterizedTest(name = "{0} -> {1} = {2}")
    @CsvSource({
            "ABERTO, EM_ATENDIMENTO, true",
            "ABERTO, CANCELADO, true",
            "EM_ATENDIMENTO, RESOLVIDO, true",
            "ABERTO, RESOLVIDO, false",
            "EM_ATENDIMENTO, CANCELADO, false",
            "EM_ATENDIMENTO, ABERTO, false",
            "RESOLVIDO, ABERTO, false",
            "RESOLVIDO, EM_ATENDIMENTO, false",
            "RESOLVIDO, CANCELADO, false",
            "CANCELADO, ABERTO, false",
            "CANCELADO, EM_ATENDIMENTO, false",
            "CANCELADO, RESOLVIDO, false"
    })
    @DisplayName("somente as transicoes do ciclo de vida sao permitidas")
    void transicoesPermitidas(StatusChamado origem, StatusChamado destino, boolean permitido) {
        assertThat(origem.permiteTransicaoPara(destino)).isEqualTo(permitido);
    }

    @Test
    @DisplayName("RESOLVIDO e CANCELADO sao status encerrados (regra 6)")
    void statusEncerrados() {
        assertThat(StatusChamado.RESOLVIDO.isEncerrado()).isTrue();
        assertThat(StatusChamado.CANCELADO.isEncerrado()).isTrue();
        assertThat(StatusChamado.ABERTO.isEncerrado()).isFalse();
        assertThat(StatusChamado.EM_ATENDIMENTO.isEncerrado()).isFalse();
    }

    @Test
    @DisplayName("nenhum status encerrado permite sair dele")
    void encerradoNaoTransiciona() {
        for (StatusChamado destino : StatusChamado.values()) {
            assertThat(StatusChamado.RESOLVIDO.permiteTransicaoPara(destino)).isFalse();
            assertThat(StatusChamado.CANCELADO.permiteTransicaoPara(destino)).isFalse();
        }
    }
}
