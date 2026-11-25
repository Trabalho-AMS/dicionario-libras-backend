-- Protótipo: CRUD das submissões de sinais com vídeo.
-- O vídeo e o termo do responsável ficam no bucket S3; aqui fica só a chave de cada arquivo.
create table submissao (
    id                  uuid primary key,
    palavra             varchar(120)  not null,
    acepcao             varchar(1000) not null,
    exemplo             varchar(500),
    classe_gramatical   varchar(40),
    assunto             varchar(60)   not null,
    -- UF da variação regional; nulo é o sinal padrão nacional
    regiao              varchar(2),
    configuracao_mao    varchar(10)   not null,

    video_chave         varchar(255)     not null,
    video_formato       varchar(10)      not null,
    video_duracao_seg   integer          not null,
    video_tamanho_mb    double precision not null,

    -- Consentimento do sinalizante (LGPD)
    versao_termo        varchar(20)              not null,
    consentimento_em    timestamp with time zone not null,
    sinalizante_menor   boolean                  not null,
    termo_responsavel   varchar(255),

    status              varchar(20) not null
        check (status in ('EM_ANALISE', 'AJUSTE_SOLICITADO', 'APROVADA', 'REJEITADA')),
    data_envio          timestamp with time zone not null,
    atualizada_em       timestamp with time zone
);
create index idx_submissao_data_envio on submissao (data_envio desc);
