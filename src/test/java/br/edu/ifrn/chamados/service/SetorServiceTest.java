package br.edu.ifrn.chamados.service;

import br.edu.ifrn.chamados.dto.SetorRequest;
import br.edu.ifrn.chamados.dto.SetorResponse;
import br.edu.ifrn.chamados.exception.RecursoNaoEncontradoException;
import br.edu.ifrn.chamados.exception.RegraNegocioException;
import br.edu.ifrn.chamados.model.Setor;
import br.edu.ifrn.chamados.repository.CategoriaRepository;
import br.edu.ifrn.chamados.repository.ChamadoRepository;
import br.edu.ifrn.chamados.repository.SetorRepository;
import br.edu.ifrn.chamados.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes do cadastro de setores, com foco na regra 8 (exclusao) e na unicidade da sigla.
 */
@ExtendWith(MockitoExtension.class)
class SetorServiceTest {

    @Mock
    private SetorRepository setorRepository;
    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ChamadoRepository chamadoRepository;

    @InjectMocks
    private SetorService service;

    private Setor cotic;

    @BeforeEach
    void preparar() {
        cotic = new Setor("Coordenação de Tecnologia da Informação e Comunicação", "COTIC");
        cotic.setId(1L);
    }

    @Test
    @DisplayName("cria setor com nome e sigla normalizados")
    void criaSetor() {
        when(setorRepository.existsBySiglaIgnoreCase("NOVO")).thenReturn(false);
        when(setorRepository.save(any(Setor.class))).thenAnswer(inv -> {
            Setor salvo = inv.getArgument(0);
            salvo.setId(4L);
            return salvo;
        });

        SetorResponse resposta = service.criar(new SetorRequest("  Setor Novo  ", " NOVO "));

        assertThat(resposta.id()).isEqualTo(4L);
        assertThat(resposta.nome()).isEqualTo("Setor Novo");
        assertThat(resposta.sigla()).isEqualTo("NOVO");
    }

    @Test
    @DisplayName("sigla duplicada (sem diferenciar maiusculas) retorna 409")
    void siglaDuplicada() {
        when(setorRepository.existsBySiglaIgnoreCase("cotic")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(new SetorRequest("Outro nome", "cotic")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Já existe um setor cadastrado com a sigla 'cotic'");

        verify(setorRepository, never()).save(any());
    }

    @Test
    @DisplayName("regra 8: setor com chamados vinculados nao pode ser excluido")
    void naoExcluiSetorComChamados() {
        when(setorRepository.findById(1L)).thenReturn(Optional.of(cotic));
        when(chamadoRepository.existsByCategoriaSetorId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.excluir(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("regra 8");

        verify(setorRepository, never()).delete(any());
    }

    @Test
    @DisplayName("setor com categorias cadastradas nao pode ser excluido (chave estrangeira)")
    void naoExcluiSetorComCategorias() {
        when(setorRepository.findById(1L)).thenReturn(Optional.of(cotic));
        when(chamadoRepository.existsByCategoriaSetorId(1L)).thenReturn(false);
        when(categoriaRepository.existsBySetorId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.excluir(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("categorias");
    }

    @Test
    @DisplayName("setor com tecnicos lotados nao pode ser excluido")
    void naoExcluiSetorComTecnicos() {
        when(setorRepository.findById(1L)).thenReturn(Optional.of(cotic));
        when(chamadoRepository.existsByCategoriaSetorId(1L)).thenReturn(false);
        when(categoriaRepository.existsBySetorId(1L)).thenReturn(false);
        when(usuarioRepository.existsBySetorId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.excluir(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("técnicos");
    }

    @Test
    @DisplayName("setor sem vinculos e excluido")
    void excluiSetorLivre() {
        Setor novo = new Setor("Setor Novo", "NOVO");
        novo.setId(4L);
        when(setorRepository.findById(4L)).thenReturn(Optional.of(novo));
        when(chamadoRepository.existsByCategoriaSetorId(4L)).thenReturn(false);
        when(categoriaRepository.existsBySetorId(4L)).thenReturn(false);
        when(usuarioRepository.existsBySetorId(4L)).thenReturn(false);

        service.excluir(4L);

        verify(setorRepository).delete(novo);
    }

    @Test
    @DisplayName("excluir setor inexistente retorna 404")
    void excluiSetorInexistente() {
        when(setorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.excluir(999L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("atualizar com sigla de outro setor retorna 409")
    void atualizaComSiglaDeOutro() {
        when(setorRepository.findById(1L)).thenReturn(Optional.of(cotic));
        when(setorRepository.existsBySiglaIgnoreCaseAndIdNot("MANUT", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(1L, new SetorRequest("Novo nome", "MANUT")))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("buscar setor inexistente retorna 404")
    void buscaSetorInexistente() {
        when(setorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(999L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Setor com id 999");
    }
}
