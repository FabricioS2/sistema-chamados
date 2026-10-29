package br.edu.ifrn.chamados.controller;

import br.edu.ifrn.chamados.dto.ChamadoAssumirRequest;
import br.edu.ifrn.chamados.dto.ChamadoCancelarRequest;
import br.edu.ifrn.chamados.dto.ChamadoFiltro;
import br.edu.ifrn.chamados.dto.ChamadoRequest;
import br.edu.ifrn.chamados.dto.ChamadoResolverRequest;
import br.edu.ifrn.chamados.dto.ChamadoResponse;
import br.edu.ifrn.chamados.dto.InteracaoRequest;
import br.edu.ifrn.chamados.dto.InteracaoResponse;
import br.edu.ifrn.chamados.dto.PageResponse;
import br.edu.ifrn.chamados.model.Prioridade;
import br.edu.ifrn.chamados.model.StatusChamado;
import br.edu.ifrn.chamados.service.ChamadoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;

/**
 * Endpoints de chamados: abertura, consultas, transicoes de status e interacoes.
 *
 * <p>Exemplo de listagem com filtros combinaveis:<br>
 * {@code GET /chamados?status=ABERTO&setorId=1&prioridade=ALTA&page=0&size=10&sort=dataAbertura,desc}</p>
 */
@RestController
@RequestMapping("/chamados")
public class ChamadoController {

    private final ChamadoService chamadoService;

    public ChamadoController(ChamadoService chamadoService) {
        this.chamadoService = chamadoService;
    }

    /** Abre um chamado. 201 + header Location (regras 1 e 2). */
    @PostMapping
    public ResponseEntity<ChamadoResponse> abrir(@Valid @RequestBody ChamadoRequest request,
                                                 UriComponentsBuilder uriBuilder) {
        ChamadoResponse response = chamadoService.abrir(request);
        URI location = uriBuilder.path("/chamados/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    /** Lista paginada de chamados com filtros opcionais (status, prioridade, setor, periodo...). */
    @GetMapping
    public PageResponse<ChamadoResponse> listar(
            @RequestParam(required = false) StatusChamado status,
            @RequestParam(required = false) Prioridade prioridade,
            @RequestParam(required = false) Long setorId,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long solicitanteId,
            @RequestParam(required = false) Long tecnicoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @PageableDefault(size = 10, sort = "dataAbertura", direction = Sort.Direction.DESC) Pageable pageable) {

        ChamadoFiltro filtro = new ChamadoFiltro(status, prioridade, setorId, categoriaId,
                solicitanteId, tecnicoId, dataInicio, dataFim);
        return PageResponse.de(chamadoService.listar(filtro, pageable));
    }

    /** Detalha o chamado com o historico de interacoes. */
    @GetMapping("/{id}")
    public ChamadoResponse buscarPorId(@PathVariable Long id) {
        return chamadoService.buscarPorId(id);
    }

    /** Tecnico assume o chamado: ABERTO -&gt; EM_ATENDIMENTO (regra 3). */
    @PatchMapping("/{id}/assumir")
    public ChamadoResponse assumir(@PathVariable Long id, @Valid @RequestBody ChamadoAssumirRequest request) {
        return chamadoService.assumir(id, request);
    }

    /** Tecnico atribuido resolve o chamado, informando a solucao obrigatoria (regra 4). */
    @PatchMapping("/{id}/resolver")
    public ChamadoResponse resolver(@PathVariable Long id, @Valid @RequestBody ChamadoResolverRequest request) {
        return chamadoService.resolver(id, request);
    }

    /** Solicitante cancela o chamado enquanto ele estiver ABERTO (regra 5). */
    @PatchMapping("/{id}/cancelar")
    public ChamadoResponse cancelar(@PathVariable Long id, @Valid @RequestBody ChamadoCancelarRequest request) {
        return chamadoService.cancelar(id, request);
    }

    /** Registra uma interacao no chamado (regras 6 e 7). 201 + header Location. */
    @PostMapping("/{id}/interacoes")
    public ResponseEntity<InteracaoResponse> registrarInteracao(
            @PathVariable Long id,
            @Valid @RequestBody InteracaoRequest request,
            UriComponentsBuilder uriBuilder) {
        InteracaoResponse response = chamadoService.registrarInteracao(id, request);
        URI location = uriBuilder.path("/chamados/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location).body(response);
    }
}
