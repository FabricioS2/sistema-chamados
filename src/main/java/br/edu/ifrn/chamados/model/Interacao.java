package br.edu.ifrn.chamados.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Interacao (andamento/comentario) registrada em um chamado por um autor.
 *
 * <p>Pertence a um {@link Chamado} e tem um autor ({@link Usuario}).</p>
 */
@Entity
@Table(name = "interacoes")
@Getter
@Setter
@NoArgsConstructor
public class Interacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String texto;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "chamado_id", nullable = false)
    private Chamado chamado;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    public Interacao(String texto, LocalDateTime dataHora, Chamado chamado, Usuario autor) {
        this.texto = texto;
        this.dataHora = dataHora;
        this.chamado = chamado;
        this.autor = autor;
    }
}
