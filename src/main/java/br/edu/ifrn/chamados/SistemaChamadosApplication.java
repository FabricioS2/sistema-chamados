package br.edu.ifrn.chamados;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Ponto de entrada da API REST do Sistema de Chamados de Suporte do IFRN.
 *
 * <p>A aplicacao sobe o esquema do banco exclusivamente via migrations do Flyway
 * ({@code src/main/resources/db/migration}). O Hibernate apenas valida o esquema
 * mapeado contra o banco ({@code spring.jpa.hibernate.ddl-auto=validate}).</p>
 */
@SpringBootApplication
@EnableTransactionManagement
public class SistemaChamadosApplication {

    public static void main(String[] args) {
        SpringApplication.run(SistemaChamadosApplication.class, args);
    }
}
