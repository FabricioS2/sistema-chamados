package br.edu.ifrn.chamados.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Setor responsavel pelo atendimento (ex.: COTIC, Manutencao).
 *
 * <p>Um setor tem varias {@link Categoria} e varios tecnicos ({@link Usuario}).</p>
 */
@Entity
@Table(name = "setores")
@Getter
@Setter
@NoArgsConstructor
public class Setor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    /** Sigla unica do setor (ex.: COTIC). */
    @Column(nullable = false, length = 20, unique = true)
    private String sigla;

    @OneToMany(mappedBy = "setor", fetch = FetchType.LAZY)
    private List<Categoria> categorias = new ArrayList<>();

    @OneToMany(mappedBy = "setor", fetch = FetchType.LAZY)
    private List<Usuario> tecnicos = new ArrayList<>();

    public Setor(String nome, String sigla) {
        this.nome = nome;
        this.sigla = sigla;
    }
}
