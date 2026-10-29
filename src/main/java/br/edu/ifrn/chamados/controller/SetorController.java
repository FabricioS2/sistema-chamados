package br.edu.ifrn.chamados.controller;

import br.edu.ifrn.chamados.dto.CategoriaResponse;
import br.edu.ifrn.chamados.dto.PageResponse;
import br.edu.ifrn.chamados.dto.SetorRequest;
import br.edu.ifrn.chamados.dto.SetorResponse;
import br.edu.ifrn.chamados.service.CategoriaService;
import br.edu.ifrn.chamados.service.SetorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Endpoints de setores. O controller apenas recebe a requisicao, chama o service e devolve
 * a resposta: nenhuma regra de negocio aqui.
 */
@RestController
@RequestMapping("/setores")
public class SetorController {

    private final SetorService setorService;
    private final CategoriaService categoriaService;

    public SetorController(SetorService setorService, CategoriaService categoriaService) {
        this.setorService = setorService;
        this.categoriaService = categoriaService;
    }

    /** Cria um setor. 201 + header Location. */
    @PostMapping
    public ResponseEntity<SetorResponse> criar(@Valid @RequestBody SetorRequest request,
                                                UriComponentsBuilder uriBuilder) {
        SetorResponse response = setorService.criar(request);
        URI location = uriBuilder.path("/setores/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    /** Lista paginada, com filtro opcional por nome (contem, sem diferenciar maiusculas). */
    @GetMapping
    public PageResponse<SetorResponse> listar(
            @RequestParam(required = false) String nome,
            @PageableDefault(size = 10, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        return PageResponse.de(setorService.listar(nome, pageable));
    }

    /** Detalha um setor. */
    @GetMapping("/{id}")
    public SetorResponse buscarPorId(@PathVariable Long id) {
        return setorService.buscarPorId(id);
    }

    /** Atualiza os dados de um setor. */
    @PutMapping("/{id}")
    public SetorResponse atualizar(@PathVariable Long id, @Valid @RequestBody SetorRequest request) {
        return setorService.atualizar(id, request);
    }

    /** Exclui um setor. 409 se houver chamados vinculados (regra 8). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        setorService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    /** Categorias de um setor, paginadas. */
    @GetMapping("/{id}/categorias")
    public PageResponse<CategoriaResponse> listarCategorias(
            @PathVariable Long id,
            @PageableDefault(size = 10, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        return PageResponse.de(categoriaService.listarPorSetor(id, pageable));
    }
}
