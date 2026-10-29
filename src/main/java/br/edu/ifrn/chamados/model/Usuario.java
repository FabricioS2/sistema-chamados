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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Usuario do sistema: solicitante (servidor/aluno) ou tecnico de um setor.
 *
 * <p>O e-mail e unico. Somente usuarios do tipo {@link TipoUsuario#TECNICO}
 * pertencem a um {@link Setor}.</p>
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 160, unique = true)
    private String email;

    @Column(nullable = false, length = 30)
    private String matricula;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoUsuario tipo;

    /** Setor de lotacao. Nulo para solicitantes; obrigatorio para tecnicos. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_id")
    private Setor setor;

    public Usuario(String nome, String email, String matricula, TipoUsuario tipo, Setor setor) {
        this.nome = nome;
        this.email = email;
        this.matricula = matricula;
        this.tipo = tipo;
        this.setor = setor;
    }

    public boolean isTecnico() {
        return tipo != null && tipo.isTecnico();
    }

    public boolean isSolicitante() {
        return tipo != null && tipo.isSolicitante();
    }

    /** Um tecnico esta lotado no setor informado? */
    public boolean pertenceAoSetor(Long setorId) {
        return setor != null && setorId != null && setor.getId().equals(setorId);
    }
}
