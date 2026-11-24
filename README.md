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
| [Flyway](https://documentation.red-gate.com/fd) | Migrações do banco em `src/main/resources/db/migration` (`V1__descricao.sql`, `V2__...`) |
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
- Na primeira execução o Flyway cria as tabelas e os dados iniciais, e a aplicação cria o bucket `dicionario-libras`.

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

## Arquitetura

Monólito modular, conforme a [proposta de arquitetura](https://github.com/Trabalho-AMS/dicionario-libras/blob/main/docs/04-arquitetura.md).
Cada módulo fica em `src/main/java/br/udesc/dicionariolibras/<modulo>` e só acessa outro módulo pela interface pública dele.

| Módulo | Responsabilidade |
|---|---|
| `identidade` | Cadastro, login e perfis |
| `dicionario` | Verbetes, sinais, variações regionais e busca |
| `submissoes` | Envio de contribuições e curadoria |
| `midia` | Upload, validação e processamento de vídeo |
| `privacidade` | Consentimento e descarte de dados (LGPD) |
| `notificacoes` | E-mails ao usuário |
| `auditoria` | Registro das ações |
| `compartilhado` | Configurações e utilitários comuns |

Camadas internas de cada módulo:

| Camada | Conteúdo |
|---|---|
| `http` | Controllers (rotas) |
| `aplicacao` | Casos de uso |
| `dominio` | Entidades e regras de negócio |
| `infra` | Repositórios, banco e arquivos |

## Classes do domínio

Implementação do [diagrama de classes](https://github.com/Trabalho-AMS/dicionario-libras/blob/main/docs/03-modelagem/README.md).

| Classe | Módulo | Regras |
|---|---|---|
| `Usuario`, `Perfil` | `identidade` | `ehCurador()`: curador ou administrador |
| `Verbete` | `dicionario` | `adicionarSinal()`: no máximo um sinal padrão publicado por verbete |
| `Sinal` | `dicionario` | Sem região é o padrão nacional; `publicar()` / `despublicar()` |
| `Regiao`, `ConfiguracaoMao`, `Assunto` | `dicionario` | Dados de apoio (as 27 UFs e assuntos vêm na migração `V2`) |
| `Video` | `midia` | `validar()`: MP4/WebM, até 30 s e 50 MB (RF04), com mensagem de como corrigir; `transcodificar()` |
| `Consentimento` | `privacidade` | `ehValido()`: termo aceito, não revogado e, se menor de 18 anos, termo do responsável (LGPD) |
| `Submissao`, `StatusSubmissao` | `submissoes` | `enviar()`, `aprovar()`, `solicitarAjuste()`, `rejeitar()`; o curador não avalia a própria submissão e ajuste/rejeição exigem justificativa |

Detalhes de implementação:

- Entre módulos diferentes, as entidades se referenciam **pelo id** (ex.: `Submissao.autorId`, `Sinal.videoId`), sem chave
  estrangeira entre tabelas de módulos diferentes, como pede a arquitetura.
- Ao aprovar, a `Submissao` registra o evento `SubmissaoAprovada`; ao pedir ajuste ou rejeitar, `SubmissaoDevolvida`.
  Os eventos são publicados quando a submissão é salva no repositório, para os outros módulos reagirem.
- O campo `Video.url` guarda a chave do arquivo no bucket; o acesso é feito por URL temporária assinada
  (`ArmazenamentoDeArquivos.urlTemporaria`).
- `solicitarAjuste` e `rejeitar` recebem também o curador, para registrar quem decidiu.
