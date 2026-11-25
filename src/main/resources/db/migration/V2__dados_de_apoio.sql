-- Regiões (27 UFs), assuntos e configurações de mão usados no formulário.
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

insert into configuracao_mao (codigo) values
    ('CM01'), ('CM02'), ('CM03'), ('CM04'), ('CM05'), ('CM06'), ('CM07'), ('CM08'), ('CM09'), ('CM10');
