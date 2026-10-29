package br.edu.ifrn.chamados.service;

import br.edu.ifrn.chamados.dto.CategoriaRequest;
import br.edu.ifrn.chamados.dto.CategoriaResponse;
import br.edu.ifrn.chamados.exception.RecursoNaoEncontradoException;
import br.edu.ifrn.chamados.exception.RegraNegocioException;
import br.edu.ifrn.chamados.model.Categoria;
import br.edu.ifrn.chamados.model.Setor;
import br.edu.ifrn.chamados.repository.CategoriaRepository;
import br.edu.ifrn.chamados.repository.ChamadoRepository;
import br.edu.ifrn.chamados.repository.SetorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.PageRequest;
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
 * Testes do cadastro de categorias, com foco na regra 8 e no vinculo com o setor responsavel.
 */
@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private SetorRepository setorRepository;
    @Mock
    private ChamadoRepository chamadoRepository;

    @InjectMocks
    private CategoriaService service;

    private Setor cotic;
    private Categoria redeWifi;

    @BeforeEach
    void preparar() {
        cotic = new Setor("Coordenação de Tecnologia da Informação e Comunicação", "COTIC");
        cotic.setId(1L);
        redeWifi = new Categoria("Rede e Wi-Fi", "Falhas de conectividade", cotic);
        redeWifi.setId(1L);
    }

    @Test
    @DisplayName("cria categoria vinculada ao setor responsavel")
    void criaCategoria() {
        when(setorRepository.findById(1L)).thenReturn(Optional.of(cotic));
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> {
            Categoria salva = inv.getArgument(0);
            salva.setId(7L);
            return salva;
        });

        CategoriaResponse resposta = service.criar(
                new CategoriaRequest(" Impressoras ", "Problemas de impressão ", 1L));

        assertThat(resposta.id()).isEqualTo(7L);
        assertThat(resposta.nome()).isEqualTo("Impressoras");
        assertThat(resposta.descricao()).isEqualTo("Problemas de impressão");
        assertThat(resposta.setor().sigla()).isEqualTo("COTIC");
    }

    @Test
    @DisplayName("criar categoria em setor inexistente retorna 404")
    void criaCategoriaComSetorInexistente() {
        when(setorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(new CategoriaRequest("Nome", "Descrição", 999L)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Setor");
    }

    @Test
    @DisplayName("regra 8: categoria com chamados vinculados nao pode ser excluida")
    void naoExcluiCategoriaComChamados() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(redeWifi));
        when(chamadoRepository.existsByCategoriaId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.excluir(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("regra 8");

        verify(categoriaRepository, never()).delete(any());
    }

    @Test
    @DisplayName("categoria sem chamados e excluida")
    void excluiCategoriaLivre() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(redeWifi));
        when(chamadoRepository.existsByCategoriaId(1L)).thenReturn(false);

        service.excluir(1L);

        verify(categoriaRepository).delete(redeWifi);
    }

    @Test
    @DisplayName("listar categorias de setor inexistente retorna 404")
    void listaPorSetorInexistente() {
        when(setorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listarPorSetor(999L, PageRequest.of(0, 10)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Setor com id 999");
    }
}
