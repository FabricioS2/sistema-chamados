-- =====================================================================================
-- V3__inserir_chamados_de_exemplo.sql
-- Chamados de exemplo cobrindo os quatro status do ciclo de vida, para a API ja nascer
-- navegavel (listagens, filtros por status/prioridade/setor/periodo e historico).
--
--   chamado 1: ABERTO           (ainda pode ser assumido ou cancelado)
--   chamado 2: EM_ATENDIMENTO   (tecnico do setor MANUT assumiu)
--   chamado 3: RESOLVIDO        (com solucao, data de fechamento e historico)
--   chamado 4: CANCELADO        (cancelado pelo proprio solicitante enquanto ABERTO)
-- =====================================================================================

insert into chamados
    (id, titulo, descricao, local, prioridade, status, data_abertura, data_fechamento, solucao,
     solicitante_id, categoria_id, tecnico_id)
values
    (1, 'Wi-Fi do Laboratório de Informática 2 fora do ar',
     'Os 20 computadores do laboratório aparecem como conectados, mas sem acesso à internet desde as 7h. As turmas do turno matutino estão sem conseguir acessar o SUAP.',
     'Laboratório de Informática 2 - Bloco B', 'ALTA', 'ABERTO',
     (now() - interval '2 days')::timestamp, null, null, 1, 1, null),

    (2, 'Projetor da Sala 12 não liga',
     'O projetor não responde ao controle remoto e a lâmpada de status fica piscando em vermelho. A turma do 3º período está sem condições de assistir às apresentações.',
     'Sala de Aula 12 - Bloco A', 'MEDIA', 'EM_ATENDIMENTO',
     (now() - interval '1 day')::timestamp, null, null, 3, 4, 6),

    (3, 'SUAP indisponível para lançamento de notas',
     'Ao tentar lançar as notas da 2ª unidade, o SUAP retorna erro 500 e a sessão é encerrada. O prazo de fechamento do diário é nesta semana.',
     'Sala dos Professores - Bloco C', 'ALTA', 'RESOLVIDO',
     (now() - interval '5 days')::timestamp, (now() - interval '4 days')::timestamp,
     'Falha de conexão do SUAP com o banco de dados institucional, provocada por sessões esgotadas. O serviço foi reiniciado pela COTIC central e o pool de conexões, ampliado. Lançamento de notas validado com a professora.',
     1, 3, 4),

    (4, 'Troca de lâmpadas queimadas na Biblioteca',
     'Três lâmpadas do salão de leitura estão queimadas, deixando o fundo da biblioteca no escuro no turno da noite.',
     'Biblioteca Central - Salão de leitura', 'BAIXA', 'CANCELADO',
     (now() - interval '3 days')::timestamp, (now() - interval '2 days')::timestamp, null, 2, 5, null);

insert into interacoes (id, texto, data_hora, chamado_id, autor_id) values
    (1, 'Abertura do chamado. O problema começou logo no primeiro horário; já reiniciamos o roteador do laboratório sem sucesso.',
        (now() - interval '2 days')::timestamp, 1, 1),

    (2, 'O projetor parou de funcionar no meio da aula de hoje. Há previsão de troca do equipamento?',
        (now() - interval '1 day')::timestamp, 2, 3),
    (3, 'Chamado assumido pela equipe de manutenção. Verificamos que o cabo de força está com mau contato; faremos a substituição e testaremos a lâmpada.',
        (now() - interval '20 hours')::timestamp, 2, 6),

    (4, 'Chamado recebido pela COTIC. Estamos abrindo chamado junto à COTIC central (Reitoria), responsável pelo SUAP.',
        (now() - interval '5 days + 3 hours')::timestamp, 3, 4),
    (5, 'Serviço restabelecido. Testei o lançamento de notas com uma turma piloto e funcionou normalmente. Por favor, confirme se o problema persiste.',
        (now() - interval '4 days')::timestamp, 3, 4),
    (6, 'Confirmado, consigo lançar as notas normalmente. Obrigada pela agilidade!',
        (now() - interval '4 days + 2 hours')::timestamp, 3, 1),

    (7, 'A equipe da biblioteca informou que a troca já estava na programação da manutenção preventiva desta semana. Cancelo o chamado para evitar duplicidade.',
        (now() - interval '2 days')::timestamp, 4, 2);

select setval(pg_get_serial_sequence('chamados', 'id'), (select max(id) from chamados));
select setval(pg_get_serial_sequence('interacoes', 'id'), (select max(id) from interacoes));
