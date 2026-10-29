package br.edu.ifrn.chamados.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Chamado de suporte aberto por um solicitante.
 *
 * <p>Relacionamentos: tem um solicitante, uma categoria (que define o setor responsavel)
 * e, opcionalmente, um tecnico atribuido ao assumir o atendimento.</p>
 *
 * <p>O ciclo de vida e controlado por {@link StatusChamado}:
 * ABERTO -&gt; EM_ATENDIMENTO -&gt; RESOLVIDO, e ABERTO -&gt; CANCELADO.</p>
 */
@Entity
@Table(name = "chamados")
@Getter
@Setter
@NoArgsConstructor
public class Chamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 2000)
    private String descricao;

    /** Local onde o problema ocorre (ex.: Laboratorio de Informatica 2, Bloco B). */
    @Column(name = "local", nullable = false, length = 120)
    private String local;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Prioridade prioridade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusChamado status;

    @Column(name = "data_abertura", nullable = false)
    private LocalDateTime dataAbertura;

    @Column(name = "data_fechamento")
    private LocalDateTime dataFechamento;

    /** Descricao da solucao aplicada. Obrigatoria ao resolver o chamado. */
    @Column(length = 2000)
    private String solucao;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    /** Tecnico que assumiu o chamado. Nulo enquanto o chamado esta ABERTO. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_id")
    private Usuario tecnico;

    @OneToMany(mappedBy = "chamado", fetch = FetchType.LAZY)
    @OrderBy("dataHora ASC")
    private List<Interacao> interacoes = new ArrayList<>();

    public Chamado(String titulo, String descricao, String local, Prioridade prioridade,
                   Usuario solicitante, Categoria categoria) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.local = local;
        this.prioridade = prioridade;
        this.solicitante = solicitante;
        this.categoria = categoria;
    }

    /** Setor responsavel pelo chamado, derivado da categoria. */
    public Setor getSetorResponsavel() {
        return categoria == null ? null : categoria.getSetor();
    }
}
