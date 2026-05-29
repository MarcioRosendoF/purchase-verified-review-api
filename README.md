# Purchase Verified Review API

[English](#english) | [Português](#portugues)

---

<a id="english"></a>
## English

[![Java 17](https://img.shields.io/badge/Java-17-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot 3.x](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL 15](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Container-blue?logo=docker)](https://www.docker.com/)
[![License MIT](https://img.shields.io/badge/License-MIT-yellow)](https://opensource.org/licenses/MIT)

### Overview

A REST API that enforces a single core rule: users can only review products they have actually purchased, preventing unverified or fraudulent feedback.

### Tech Stack

* **Language:** Java 17
* **Framework:** Spring Boot 3.x (Spring Web, Spring Security, Spring Data JPA)
* **Database:** PostgreSQL 15
* **Database Migrations:** Flyway
* **Authentication:** JWT (JSON Web Tokens via JJWT)
* **Build Tool:** Maven
* **Infrastructure:** Docker & Docker Compose
* **Database Testing:** Testcontainers (PostgreSQL Container)

### Getting Started

Follow these commands to set up and run the application locally:

```bash
# 1. Copiar as variáveis de ambiente
cp .env.example .env
# Edite o .env com suas configurações locais (ou deixe os defaults — já funcionam para dev)

# 2. Subir o banco
docker-compose up -d

# 3. Rodar a aplicação
./mvnw spring-boot:run

# 4. Admin criado automaticamente:
#    email: admin@marketplace.com
#    senha: admin123
```

### API Requests Workflow

Below is the workflow to authenticate and test the purchase-verified review rules using `curl`.

#### 1. Authenticate to Obtain the JWT
Authenticate with the default administrator credentials or a registered user to receive the JWT token.
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@marketplace.com", "password": "admin123"}'
```
Response contains a `"token"` string. Set this token and the target product ID in your environment variables:
```bash
export TOKEN="your_extracted_jwt_token"
export PRODUCT_ID="uuid-of-an-active-product"
```

#### 2. Place an Order
A user with the `CUSTOMER` role must purchase the product before being eligible to review it.
```bash
curl -X POST http://localhost:8080/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"productId\": \"$PRODUCT_ID\", \"quantity\": 1}"
```

#### 3. Submit a Verified Product Review
Submit a review for the product that was successfully ordered in the previous step.
```bash
curl -X POST http://localhost:8080/reviews \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"productId\": \"$PRODUCT_ID\", \"rating\": 5, \"comment\": \"Excellent quality and performance.\"}"
```

#### 4. Attempt to Review a Non-Purchased Product
If the user tries to review a product they have not ordered, the request fails with `403 Forbidden` representing the business rule violation.
```bash
export UNPURCHASED_PRODUCT_ID="another-product-uuid-here"

curl -X POST http://localhost:8080/reviews \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"productId\": \"$UNPURCHASED_PRODUCT_ID\", \"rating\": 4, \"comment\": \"Intruding review without purchase.\"}"
```

### Technical Decisions

* **UUID as Primary Key:** UUIDs (Universally Unique Identifiers) are used instead of auto-incrementing integers (`Long`) to prevent sequential ID enumeration attacks, enhancing security on public URLs. They also facilitate distributed database architectures by allowing ID generation on the application side without database round-trips.
* **Soft Delete for Products:** Removing a product only sets a `deletedAt` timestamp instead of physically deleting the database record. This strategy prevents breaking referential integrity for historical orders and reviews while making the product immediately unavailable for new purchases.
* **`Instant` with UTC for Timestamps:** Using `Instant` standardizes all date and time representation to UTC. This approach removes local timezone conflicts between servers, databases, and clients. Formatting times to local timezones is delegated entirely to the client application.
* **Isolated `PurchaseValidatorService`:** The validation logic checking if a buyer has purchased a product is separated into its own service class. This adheres to the Single Responsibility Principle (SRP), making the unit testing process straightforward and allowing the validation rule to be reused in other areas like loyalty programs or marketing campaigns.
* **Flyway for Database Migrations:** Schema changes are versioned and managed through Flyway SQL migration files. This ensures that the database schema is updated incrementally and consistently across all deployment environments (development, testing, and production), replacing risky auto-generation strategies.
* **Testcontainers for Ephemeral Testing:** Instead of pointing integration tests to a shared or local static database server, Testcontainers programmatically spins up a clean, isolated PostgreSQL Docker container before running the test suite. This guarantees that tests run reliably on any developer machine or continuous integration (CI) server without environment drift or port collisions.

---

<a id="portugues"></a>
## Português

[![Java 17](https://img.shields.io/badge/Java-17-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot 3.x](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL 15](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Container-blue?logo=docker)](https://www.docker.com/)
[![License MIT](https://img.shields.io/badge/License-MIT-yellow)](https://opensource.org/licenses/MIT)

### Visão Geral

Uma API REST que impõe uma única regra de negócio central: usuários só podem avaliar produtos que de fato compraram, prevenindo avaliações falsas ou não verificadas.

### Tecnologias Utilizadas

* **Linguagem:** Java 17
* **Framework:** Spring Boot 3.x (Spring Web, Spring Security, Spring Data JPA)
* **Banco de Dados:** PostgreSQL 15
* **Migrações do Banco:** Flyway
* **Autenticação:** JWT (JSON Web Tokens via JJWT)
* **Ferramenta de Build:** Maven
* **Infraestrutura:** Docker & Docker Compose
* **Testes de Banco de Dados:** Testcontainers (PostgreSQL Container)

### Como Rodar

Siga estes comandos para configurar e rodar a aplicação localmente:

```bash
# 1. Copiar as variáveis de ambiente
cp .env.example .env
# Edite o .env com suas configurações locais (ou deixe os defaults — já funcionam para dev)

# 2. Subir o banco
docker-compose up -d

# 3. Rodar a aplicação
./mvnw spring-boot:run

# 4. Admin criado automaticamente:
#    email: admin@marketplace.com
#    senha: admin123
```

### Fluxo de Requisições da API

Abaixo está o fluxo para se autenticar e testar as regras de validação de compra usando `curl`.

#### 1. Autenticar para Obter o JWT
Autentique com as credenciais padrão de administrador ou um usuário registrado para receber o token JWT.
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@marketplace.com", "password": "admin123"}'
```
A resposta contém o campo `"token"`. Defina esse token e o ID do produto alvo em suas variáveis de ambiente:
```bash
export TOKEN="seu_token_jwt_extraido"
export PRODUCT_ID="uuid-de-um-produto-ativo"
```

#### 2. Criar um Pedido (Compra)
Um usuário com a permissão de `CUSTOMER` deve comprar o produto antes de poder avaliá-lo.
```bash
curl -X POST http://localhost:8080/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"productId\": \"$PRODUCT_ID\", \"quantity\": 1}"
```

#### 3. Enviar uma Avaliação Verificada do Produto
Envie uma avaliação para o produto que foi comprado com sucesso no passo anterior.
```bash
curl -X POST http://localhost:8080/reviews \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"productId\": \"$PRODUCT_ID\", \"rating\": 5, \"comment\": \"Excelente qualidade e performance.\"}"
```

#### 4. Tentativa de Avaliar um Produto Não Comprado
Se o usuário tentar avaliar um produto que não comprou, a requisição falha com `403 Forbidden`, representando a violação da regra de negócio.
```bash
export UNPURCHASED_PRODUCT_ID="outro-uuid-de-produto"

curl -X POST http://localhost:8080/reviews \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"productId\": \"$UNPURCHASED_PRODUCT_ID\", \"rating\": 4, \"comment\": \"Tentativa de avaliacao sem compra.\"}"
```

### Decisões Técnicas

* **UUID como Chave Primária:** UUIDs (Universally Unique Identifiers) são usados em vez de inteiros sequenciais autoincrementais (`Long`) para evitar ataques de enumeração de ID, aumentando a segurança em URLs públicas. Eles também facilitam a arquitetura de banco de dados distribuído, permitindo a geração de IDs no lado da aplicação.
* **Soft Delete para Produtos:** A exclusão de um produto apenas define um timestamp em `deletedAt` em vez de remover fisicamente o registro do banco de dados. Isso previne a quebra da integridade referencial para pedidos e avaliações históricas, além de tornar o produto indisponível para novas compras imediatamente.
* **`Instant` com UTC para Timestamps:** O uso de `Instant` padroniza a representação de todas as datas e horários em UTC. Isso elimina conflitos de fusos horários locais entre servidores, bancos de dados e clientes. A formatação de exibição é delegada à aplicação cliente.
* **`PurchaseValidatorService` Isolado:** A lógica de validação que verifica se um comprador adquiriu o produto foi separada em uma classe de serviço exclusiva. Isso atende ao Princípio de Responsabilidade Única (SRP), simplificando testes unitários e permitindo o reuso dessa regra de validação em outras áreas do sistema futuramente.
* **Flyway para Migrações de Banco de Dados:** Alterações de esquema são versionadas e gerenciadas por meio de arquivos SQL sequenciais no Flyway. Isso garante que a estrutura do banco evolua de forma previsível e seja replicada exatamente igual em todos os ambientes (desenvolvimento, testes e produção), substituindo estratégias de geração automática de DDL em produção.
* **Testcontainers para Testes Efêmeros:** Em vez de apontar os testes de integração para um banco de dados estático local ou compartilhado, o Testcontainers provisiona programaticamente um container PostgreSQL limpo e isolado antes de rodar a suíte de testes. Isso garante que os testes executem de forma confiável em qualquer máquina de desenvolvimento ou servidor de integração contínua (CI), livre de conflitos de estado ou colisões de porta.
