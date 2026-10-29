-- =====================================================================================
-- V2__inserir_dados_iniciais.sql
-- Carga inicial: 3 setores, 6 categorias e 7 usuarios (4 tecnicos e 3 solicitantes).
-- Os ids sao fixos de proposito para as consultas de exemplo do README; por isso as
-- sequences das colunas identity sao reposicionadas ao final.
-- =====================================================================================

insert into setores (id, nome, sigla) values
    (1, 'Coordenação de Tecnologia da Informação e Comunicação', 'COTIC'),
    (2, 'Coordenação de Manutenção e Infraestrutura', 'MANUT'),
    (3, 'Biblioteca Central', 'BIB');

insert into categorias (id, nome, descricao, setor_id) values
    (1, 'Rede e Wi-Fi', 'Falhas de conectividade, Wi-Fi dos laboratórios, pontos de rede e acesso à internet.', 1),
    (2, 'Equipamentos de Informática', 'Computadores, notebooks, impressoras e periféricos dos setores e laboratórios.', 1),
    (3, 'Sistemas Institucionais', 'SUAP, e-mail institucional, Moodle e demais sistemas corporativos do IFRN.', 1),
    (4, 'Projetores e Equipamentos de Sala', 'Projetor, tela, computador de sala e cabeamento de áudio e vídeo das salas de aula.', 2),
    (5, 'Elétrica, Hidráulica e Climatização', 'Ar-condicionado, iluminação, tomadas, vazamentos e reparos prediais em geral.', 2),
    (6, 'Acervo e Empréstimos', 'Consulta ao acervo, renovação, devolução e reserva de livros e periódicos.', 3);

insert into usuarios (id, nome, email, matricula, tipo, setor_id) values
    (1, 'Maria Silva de Souza', 'maria.silva@ifrn.edu.br', '2019123456', 'SOLICITANTE', null),
    (2, 'João Pedro Alves', 'joao.alves@escolar.ifrn.edu.br', '2024100200', 'SOLICITANTE', null),
    (3, 'Fernanda Costa Lima', 'fernanda.lima@ifrn.edu.br', '2021003344', 'SOLICITANTE', null),
    (4, 'Carlos Eduardo Pereira', 'carlos.pereira@ifrn.edu.br', '2015001122', 'TECNICO', 1),
    (5, 'Ana Beatriz Rocha', 'ana.rocha@ifrn.edu.br', '2018007788', 'TECNICO', 1),
    (6, 'José Ribamar Santos', 'jose.santos@ifrn.edu.br', '2016004455', 'TECNICO', 2),
    (7, 'Patrícia Gomes Ferreira', 'patricia.ferreira@ifrn.edu.br', '2020009911', 'TECNICO', 3);

-- Reposiciona as sequences para que os proximos inserts (via API) nao colidam com os ids fixos
select setval(pg_get_serial_sequence('setores', 'id'), (select max(id) from setores));
select setval(pg_get_serial_sequence('categorias', 'id'), (select max(id) from categorias));
select setval(pg_get_serial_sequence('usuarios', 'id'), (select max(id) from usuarios));
