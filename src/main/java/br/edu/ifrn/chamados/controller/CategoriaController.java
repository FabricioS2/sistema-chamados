package br.edu.ifrn.chamados.controller;

import br.edu.ifrn.chamados.dto.CategoriaRequest;
import br.edu.ifrn.chamados.dto.CategoriaResponse;
import br.edu.ifrn.chamados.dto.PageResponse;
import br.edu.ifrn.chamados.service.CategoriaService;
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
 * Endpoints de categorias.
 */
@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    /** Cria uma categoria em um setor. 201 + header Location. */
    @PostMapping
    public ResponseEntity<CategoriaResponse> criar(@Valid @RequestBody CategoriaRequest request,
                                                   UriComponentsBuilder uriBuilder) {
        CategoriaResponse response = categoriaService.criar(request);
        URI location = uriBuilder.path("/categorias/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    /** Lista paginada, com filtros opcionais e combinaveis por nome e setorId. */
    @GetMapping
    public PageResponse<CategoriaResponse> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Long setorId,
            @PageableDefault(size = 10, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        return PageResponse.de(categoriaService.listar(nome, setorId, pageable));
    }

    /** Detalha uma categoria. */
    @GetMapping("/{id}")
    public CategoriaResponse buscarPorId(@PathVariable Long id) {
        return categoriaService.buscarPorId(id);
    }

    /** Atualiza uma categoria (nome, descricao e setor responsavel). */
    @PutMapping("/{id}")
    public CategoriaResponse atualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
        return categoriaService.atualizar(id, request);
    }

    /** Exclui uma categoria. 409 se houver chamados vinculados (regra 8). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        categoriaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
