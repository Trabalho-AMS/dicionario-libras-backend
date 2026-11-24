-- Usuários de demonstração do protótipo. Ainda não há login: o frontend escolhe um destes usuários
-- e envia o id no cabeçalho X-Usuario-Id.
insert into usuario (id, nome, email, perfil) values
    ('11111111-1111-1111-1111-111111111111', 'Ana Souza',      'ana.souza@demo.com',      'COLABORADOR'),
    ('22222222-2222-2222-2222-222222222222', 'Bruno Oliveira', 'bruno.oliveira@demo.com', 'COLABORADOR'),
    ('33333333-3333-3333-3333-333333333333', 'Marina Costa',   'marina.costa@demo.com',   'CURADOR'),
    ('44444444-4444-4444-4444-444444444444', 'Administrador',  'admin@demo.com',          'ADMINISTRADOR');
