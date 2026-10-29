package br.edu.ifrn.chamados.dto;

import br.edu.ifrn.chamados.model.Chamado;
import br.edu.ifrn.chamados.model.Interacao;
import br.edu.ifrn.chamados.model.Prioridade;
import br.edu.ifrn.chamados.model.StatusChamado;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Representacao de um chamado na resposta da API.
 *
 * <p>Nas listagens o atributo {@code interacoes} volta vazio (o historico nao e carregado
 * para nao pesar a pagina); use {@code GET /chamados/{id}} para ver as interacoes.</p>
 */
public record ChamadoResponse(
        Long id,
        String titulo,
        String descricao,
        String local,
        Prioridade prioridade,
        StatusChamado status,
        LocalDateTime dataAbertura,
        LocalDateTime dataFechamento,
        String solucao,
        UsuarioResponse solicitante,
        UsuarioResponse tecnico,
        CategoriaResponse categoria,
        List<InteracaoResponse> interacoes
) {

    /** Usado nas listagens: sem o historico de interacoes. */
    public static ChamadoResponse de(Chamado chamado) {
        return new ChamadoResponse(
                chamado.getId(),
                chamado.getTitulo(),
                chamado.getDescricao(),
                chamado.getLocal(),
                chamado.getPrioridade(),
                chamado.getStatus(),
                chamado.getDataAbertura(),
                chamado.getDataFechamento(),
                chamado.getSolucao(),
                UsuarioResponse.de(chamado.getSolicitante()),
                UsuarioResponse.de(chamado.getTecnico()),
                CategoriaResponse.de(chamado.getCategoria()),
                List.of());
    }

    /** Usado no detalhe do chamado: inclui o historico completo de interacoes. */
    public static ChamadoResponse de(Chamado chamado, List<Interacao> interacoes) {
        ChamadoResponse base = de(chamado);
        return new ChamadoResponse(
                base.id(),
                base.titulo(),
                base.descricao(),
                base.local(),
                base.prioridade(),
                base.status(),
                base.dataAbertura(),
                base.dataFechamento(),
                base.solucao(),
                base.solicitante(),
                base.tecnico(),
                base.categoria(),
                interacoes.stream().map(InteracaoResponse::de).toList());
    }
}
