package br.edu.ifrn.chamados.controller;

import br.edu.ifrn.chamados.dto.ChamadoResponse;
import br.edu.ifrn.chamados.dto.PageResponse;
import br.edu.ifrn.chamados.dto.UsuarioRequest;
import br.edu.ifrn.chamados.dto.UsuarioResponse;
import br.edu.ifrn.chamados.model.StatusChamado;
import br.edu.ifrn.chamados.model.TipoUsuario;
import br.edu.ifrn.chamados.service.ChamadoService;
import br.edu.ifrn.chamados.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
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
 * Endpoints de usuarios. Nao ha DELETE de usuario: o historico dos chamados precisa
 * preservar solicitantes e tecnicos.
 */
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final ChamadoService chamadoService;

    public UsuarioController(UsuarioService usuarioService, ChamadoService chamadoService) {
        this.usuarioService = usuarioService;
        this.chamadoService = chamadoService;
    }

    /** Cria um usuario. 201 + header Location; e-mail duplicado retorna 409. */
    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody UsuarioRequest request,
                                                 UriComponentsBuilder uriBuilder) {
        UsuarioResponse response = usuarioService.criar(request);
        URI location = uriBuilder.path("/usuarios/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    /** Lista paginada, com filtros opcionais e combinaveis por nome, tipo e setorId. */
    @GetMapping
    public PageResponse<UsuarioResponse> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) TipoUsuario tipo,
            @RequestParam(required = false) Long setorId,
            @PageableDefault(size = 10, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        return PageResponse.de(usuarioService.listar(nome, tipo, setorId, pageable));
    }

    /** Detalha um usuario. */
    @GetMapping("/{id}")
    public UsuarioResponse buscarPorId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id);
    }

    /** Atualiza os dados de um usuario. */
    @PutMapping("/{id}")
    public UsuarioResponse atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request) {
        return usuarioService.atualizar(id, request);
    }

    /** Chamados abertos por um solicitante ou atribuidos a um tecnico, com filtro opcional por status. */
    @GetMapping("/{id}/chamados")
    public PageResponse<ChamadoResponse> listarChamados(
            @PathVariable Long id,
            @RequestParam(required = false) StatusChamado status,
            @PageableDefault(size = 10, sort = "dataAbertura", direction = Sort.Direction.DESC) Pageable pageable) {
        return PageResponse.de(chamadoService.listarPorUsuario(id, status, pageable));
    }
}
