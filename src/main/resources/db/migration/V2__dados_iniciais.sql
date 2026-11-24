-- Dados de apoio: as 27 unidades federativas como regiões, assuntos e configurações de mão básicas.
insert into regiao (uf, nome) values
    ('AC', 'Acre'), ('AL', 'Alagoas'), ('AP', 'Amapá'), ('AM', 'Amazonas'), ('BA', 'Bahia'),
    ('CE', 'Ceará'), ('DF', 'Distrito Federal'), ('ES', 'Espírito Santo'), ('GO', 'Goiás'),
    ('MA', 'Maranhão'), ('MT', 'Mato Grosso'), ('MS', 'Mato Grosso do Sul'), ('MG', 'Minas Gerais'),
    ('PA', 'Pará'), ('PB', 'Paraíba'), ('PR', 'Paraná'), ('PE', 'Pernambuco'), ('PI', 'Piauí'),
    ('RJ', 'Rio de Janeiro'), ('RN', 'Rio Grande do Norte'), ('RS', 'Rio Grande do Sul'),
    ('RO', 'Rondônia'), ('RR', 'Roraima'), ('SC', 'Santa Catarina'), ('SP', 'São Paulo'),
    ('SE', 'Sergipe'), ('TO', 'Tocantins');

insert into assunto (nome) values
    ('Família'), ('Alimentos'), ('Animais'), ('Cores'), ('Educação'), ('Lugares'),
    ('Números'), ('Profissões'), ('Saúde'), ('Tempo'), ('Verbos'), ('Outros');

insert into configuracao_mao (codigo, imagem) values
    ('CM01', null), ('CM02', null), ('CM03', null), ('CM04', null), ('CM05', null),
    ('CM06', null), ('CM07', null), ('CM08', null), ('CM09', null), ('CM10', null);
