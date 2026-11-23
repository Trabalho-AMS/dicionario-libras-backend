# Dicionário de Libras — Backend

Backend da [Expansão do Dicionário de Libras](https://github.com/Trabalho-AMS/dicionario-libras),
trabalho final de AMS0002 (UDESC). Frontend: [dicionario-libras-frontend](https://github.com/Trabalho-AMS/dicionario-libras-frontend).

## Stack

- Java 17
- Spring Boot (Web, Data JPA, Validation, Actuator)
- H2 (desenvolvimento) e PostgreSQL (produção)
- Maven (wrapper incluso)

## Como rodar

```bash
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`. Teste com `GET /api/status`.
O console do H2 fica em `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:dicionario`, usuário `sa`).

### Com PostgreSQL

```bash
docker compose up -d
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
```

Variáveis do perfil `prod`: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `CORS_ALLOWED_ORIGINS`.

### Testes

```bash
./mvnw test
```

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
