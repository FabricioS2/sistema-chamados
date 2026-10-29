package br.edu.ifrn.chamados.dto;

import br.edu.ifrn.chamados.model.Prioridade;
import br.edu.ifrn.chamados.model.StatusChamado;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Filtros opcionais e combinaveis de {@code GET /chamados}. Filtro nao informado e ignorado.
 *
 * <p>O periodo de abertura e informado em dias ({@code dataInicio} e {@code dataFim}) e
 * convertido para os limites do dia na camada de service.</p>
 *
 * @param status        ABERTO, EM_ATENDIMENTO, RESOLVIDO ou CANCELADO
 * @param prioridade    BAIXA, MEDIA ou ALTA
 * @param setorId       setor responsavel (setor da categoria do chamado)
 * @param categoriaId   categoria do chamado
 * @param solicitanteId quem abriu o chamado
 * @param tecnicoId     tecnico atribuido
 * @param dataInicio    abertura a partir deste dia (inclusivo)
 * @param dataFim       abertura ate este dia (inclusivo)
 */
public record ChamadoFiltro(
        StatusChamado status,
        Prioridade prioridade,
        Long setorId,
        Long categoriaId,
        Long solicitanteId,
        Long tecnicoId,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
) {

    public static ChamadoFiltro vazio() {
        return new ChamadoFiltro(null, null, null, null, null, null, null, null);
    }
}
