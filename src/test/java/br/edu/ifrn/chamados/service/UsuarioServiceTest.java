package br.edu.ifrn.chamados.service;

import br.edu.ifrn.chamados.dto.UsuarioRequest;
import br.edu.ifrn.chamados.dto.UsuarioResponse;
import br.edu.ifrn.chamados.exception.DadosInvalidosException;
import br.edu.ifrn.chamados.exception.RegraNegocioException;
import br.edu.ifrn.chamados.model.Setor;
import br.edu.ifrn.chamados.model.TipoUsuario;
import br.edu.ifrn.chamados.model.Usuario;
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
 * Testes do cadastro de usuarios: e-mail unico (409) e o vinculo de tecnico a um setor.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private SetorRepository setorRepository;

    @InjectMocks
    private UsuarioService service;

    private Setor cotic;
    private Usuario maria;

    @BeforeEach
    void preparar() {
        cotic = new Setor("Coordenação de Tecnologia da Informação e Comunicação", "COTIC");
        cotic.setId(1L);
        maria = new Usuario("Maria Silva", "maria.silva@ifrn.edu.br", "2019123456", TipoUsuario.SOLICITANTE, null);
        maria.setId(1L);
    }

    @Test
    @DisplayName("cria solicitante sem setor")
    void criaSolicitante() {
        when(usuarioRepository.existsByEmailIgnoreCase("novo@ifrn.edu.br")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario salvo = inv.getArgument(0);
            salvo.setId(8L);
            return salvo;
        });

        UsuarioResponse resposta = service.criar(new UsuarioRequest(
                "Novo Solicitante", "novo@ifrn.edu.br", "2026001", TipoUsuario.SOLICITANTE, null));

        assertThat(resposta.id()).isEqualTo(8L);
        assertThat(resposta.tipo()).isEqualTo(TipoUsuario.SOLICITANTE);
        assertThat(resposta.setor()).isNull();
    }

    @Test
    @DisplayName("cria tecnico vinculado ao setor")
    void criaTecnico() {
        when(usuarioRepository.existsByEmailIgnoreCase("tec@ifrn.edu.br")).thenReturn(false);
        when(setorRepository.findById(1L)).thenReturn(Optional.of(cotic));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse resposta = service.criar(new UsuarioRequest(
                "Novo Técnico", "tec@ifrn.edu.br", "2015999", TipoUsuario.TECNICO, 1L));

        assertThat(resposta.tipo()).isEqualTo(TipoUsuario.TECNICO);
        assertThat(resposta.setor().sigla()).isEqualTo("COTIC");
    }

    @Test
    @DisplayName("e-mail duplicado retorna 409 (sem diferenciar maiusculas)")
    void emailDuplicado() {
        when(usuarioRepository.existsByEmailIgnoreCase("MARIA.SILVA@ifrn.edu.br")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(new UsuarioRequest(
                "Outra Maria", "MARIA.SILVA@ifrn.edu.br", "777", TipoUsuario.SOLICITANTE, null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("e-mail");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("tecnico sem setor e dado invalido (400)")
    void tecnicoSemSetor() {
        when(usuarioRepository.existsByEmailIgnoreCase("semsetor@ifrn.edu.br")).thenReturn(false);

        assertThatThrownBy(() -> service.criar(new UsuarioRequest(
                "Técnico Sem Setor", "semsetor@ifrn.edu.br", "888", TipoUsuario.TECNICO, null)))
                .isInstanceOf(DadosInvalidosException.class)
                .hasMessageContaining("setor");
    }

    @Test
    @DisplayName("tecnico com setor inexistente retorna 404")
    void tecnicoComSetorInexistente() {
        when(usuarioRepository.existsByEmailIgnoreCase("t@ifrn.edu.br")).thenReturn(false);
        when(setorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(new UsuarioRequest(
                "Técnico", "t@ifrn.edu.br", "888", TipoUsuario.TECNICO, 999L)))
                .isInstanceOf(br.edu.ifrn.chamados.exception.RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("solicitante nao e vinculado a setor (400)")
    void solicitanteComSetor() {
        when(usuarioRepository.existsByEmailIgnoreCase("s@ifrn.edu.br")).thenReturn(false);

        assertThatThrownBy(() -> service.criar(new UsuarioRequest(
                "Solicitante", "s@ifrn.edu.br", "888", TipoUsuario.SOLICITANTE, 1L)))
                .isInstanceOf(DadosInvalidosException.class);
    }

    @Test
    @DisplayName("atualizar com e-mail de outro usuario retorna 409")
    void atualizaComEmailDeOutro() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(maria));
        when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot("carlos@ifrn.edu.br", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(1L, new UsuarioRequest(
                "Maria Silva", "carlos@ifrn.edu.br", "2019123456", TipoUsuario.SOLICITANTE, null)))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("atualizar os proprios dados mantem o e-mail")
    void atualizaPropriosDados() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(maria));
        when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot("maria.silva@ifrn.edu.br", 1L)).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse resposta = service.atualizar(1L, new UsuarioRequest(
                "  Maria Silva de Souza  ", "maria.silva@ifrn.edu.br", "2019123456", TipoUsuario.SOLICITANTE, null));

        assertThat(resposta.nome()).isEqualTo("Maria Silva de Souza");
        assertThat(resposta.email()).isEqualTo("maria.silva@ifrn.edu.br");
    }
}
