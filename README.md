# Dicionário de Libras — Backend

Backend da [Expansão do Dicionário de Libras](https://github.com/Trabalho-AMS/dicionario-libras),
trabalho final de AMS0002 (UDESC). Frontend: [dicionario-libras-frontend](https://github.com/Trabalho-AMS/dicionario-libras-frontend).

## Stack

- Java 17
- Spring Boot (Web, Data JPA, Validation, Actuator)
- PostgreSQL 17 e bucket S3 ([SeaweedFS](https://github.com/seaweedfs/seaweedfs)), ambos no Docker
- Maven (wrapper incluso)

## Bibliotecas

| Biblioteca | Para quê |
|---|---|
| [Lombok](https://projectlombok.org/) | Gera getters, setters, construtores e builders (`@Getter`, `@RequiredArgsConstructor`, `@Builder`...) |
| [MapStruct](https://mapstruct.org/) | Converte entidades em DTOs e vice-versa. Os mappers (`@Mapper`) viram beans do Spring automaticamente |
| [Flyway](https://documentation.red-gate.com/fd) | Migrações do banco em `src/main/resources/db/migration` (`V1__esquema_inicial.sql`) |
| [springdoc-openapi](https://springdoc.org/) | Documentação da API e Swagger UI em `http://localhost:8080/swagger-ui.html` |
| Bean Validation | Validação dos DTOs de entrada (`@NotBlank`, `@Size`, `@Valid`...) |
| Spring Boot Test (JUnit 5, Mockito, AssertJ) | Testes |

O esquema do banco é criado **somente** pelas migrações do Flyway; o Hibernate apenas valida (`ddl-auto: validate`).
Ao criar uma entidade, crie também a migração com a tabela correspondente.

## Como rodar

Requer Java 17 e Docker.

```bash
docker compose up -d        # sobe o PostgreSQL (porta 5433) e o bucket S3 (porta 8333)
./mvnw spring-boot:run      # sobe a API em http://localhost:8080
```

- Status: `GET http://localhost:8080/api/status`
- Swagger: `http://localhost:8080/swagger-ui.html`
- Na primeira execução o Flyway cria a tabela e a aplicação cria o bucket `dicionario-libras`.

| Serviço | Endereço | Credenciais |
|---|---|---|
| PostgreSQL | `localhost:5433`, banco `dicionario_libras` | `dicionario` / `dicionario` |
| Bucket S3 | `http://localhost:8333` | access key `dicionario`, secret `dicionario123` |

As configurações podem ser trocadas por variáveis de ambiente: `DB_URL`, `DB_USER`, `DB_PASSWORD`,
`S3_ENDPOINT`, `S3_PUBLIC_ENDPOINT` (endereço do bucket visto pelo navegador, usado nos links dos vídeos), `S3_REGION`, `S3_ACCESS_KEY`, `S3_SECRET_KEY`, `S3_BUCKET` e `CORS_ALLOWED_ORIGINS`.

Para apagar os dados e começar do zero: `docker compose down -v`.

### Sistema completo com Docker

Para subir backend, frontend, banco e bucket juntos, use o `docker-compose.yml` do
[repositório principal](https://github.com/Trabalho-AMS/dicionario-libras#como-rodar-o-sistema-completo-docker).
O `Dockerfile` deste repositório compila o jar com o Maven Wrapper e roda em Java 17.

### Testes

```bash
./mvnw test
```

Os testes de integração sobem um PostgreSQL temporário com [Testcontainers](https://testcontainers.com/),
então o Docker precisa estar rodando.

## API

Documentação interativa no Swagger: `http://localhost:8080/swagger-ui.html`.

| Método | Rota | O que faz |
|---|---|---|
| `POST` | `/api/submissoes` | Envia um sinal (multipart); fica `EM_ANALISE` |
| `GET` | `/api/submissoes` | Lista as submissões |
| `GET` | `/api/submissoes/{id}` | Detalhe, com link temporário do vídeo |
| `PUT` | `/api/submissoes/{id}` | Altera a submissão (vídeo opcional: sem ele, o atual é mantido) |
| `DELETE` | `/api/submissoes/{id}` | Exclui a submissão e apaga o vídeo e o termo do bucket (LGPD) |

Campos do formulário: `palavra`, `acepcao`, `exemplo`, `classeGramatical`, `assunto`, `regiao` (UF; vazio é o
sinal nacional), `configuracaoMao`, `video`, `duracaoSeg`, `aceiteTermo`, `sinalizanteMenor` e `termoResponsavel`.

Erros voltam como `{"status": 422, "mensagem": "O vídeo tem 45 s; o limite é 30 s"}`, com uma mensagem que diz
como corrigir. A duração do vídeo é lida pelo navegador e enviada em `duracaoSeg`; formato e tamanho são
conferidos no backend.

## Arquitetura

Monólito modular, conforme a [proposta de arquitetura](https://github.com/Trabalho-AMS/dicionario-libras/blob/main/docs/04-arquitetura.md).
Cada módulo fica em `src/main/java/br/udesc/dicionariolibras/<modulo>`, com as camadas `http` (rotas),
`aplicacao` (casos de uso), `dominio` (regras) e `infra` (banco e arquivos).

Neste protótipo há só o módulo `submissoes`, que faz o CRUD das submissões com upload do vídeo, e o pacote
`compartilhado`, com a configuração comum (CORS, Swagger e tratamento de erros). Os outros módulos da
proposta (`identidade`, `dicionario`, `midia`, `privacidade`, `notificacoes` e `auditoria`) entram quando
forem implementados.

| Arquivo | Camada | O que faz |
|---|---|---|
| `SubmissaoController` | `http` | Rotas do CRUD; recebe o formulário multipart (`FormularioSubmissao`) |
| `SubmissaoService` | `aplicacao` | Casos de uso: criar, listar, detalhar, alterar e excluir |
| `Submissao` | `dominio` | Entidade; valida o vídeo (MP4/WebM, até 30 s e 50 MB) e o consentimento (aceite do termo e, se menor de 18 anos, termo do responsável) |
| `SubmissaoRepository` | `dominio` | Interface do repositório: o que o service pode pedir ao banco |
| `SubmissaoRepositoryJpa` | `infra` | Implementa o `SubmissaoRepository` com JPA e PostgreSQL |
| `S3ArmazenamentoDeArquivos` | `infra` | Guarda os arquivos no bucket S3 e gera os links temporários |

O fluxo segue as camadas: `SubmissaoController` → `SubmissaoService` → `SubmissaoRepository` (interface no
domínio) → `SubmissaoRepositoryJpa` (infra). Assim a aplicação não depende do banco, só da interface.

O banco tem uma tabela, `submissao`, criada pela migração `V1` do Flyway. Os arquivos ficam no bucket: a
tabela guarda só a chave de cada um.

> Se você já tinha rodado uma versão anterior, apague o banco antigo uma vez: `docker compose down -v`.
