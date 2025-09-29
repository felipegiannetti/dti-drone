# 🚁 DTI Drone Delivery System

<div align="center">

![Java](https://img.shields.io/badge/Java-24-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.0-brightgreen?style=for-the-badge&logo=spring)
![React](https://img.shields.io/badge/React-19-blue?style=for-the-badge&logo=react)
![Next.js](https://img.shields.io/badge/Next.js-15.5.4-black?style=for-the-badge&logo=next.js)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql)
![TypeScript](https://img.shields.io/badge/TypeScript-5-blue?style=for-the-badge&logo=typescript)

**Sistema inteligente de gerenciamento e otimização de entregas por drones em áreas urbanas**

</div>

---

## Sobre o Projeto

O **DTI Drone Delivery System** é uma solução completa para gerenciamento de entregas por drones, desenvolvida para uma startup de logística urbana. O sistema combina algoritmos inteligentes de otimização com uma interface moderna para maximizar a eficiência das entregas.

### Problema Resolvido

- **Otimização de Rotas**: Algoritmo híbrido que combina knapsack e nearest neighbor
- **Gestão de Prioridades**: Sistema de entregas baseado em prioridade (HIGH → MEDIUM → LOW)
- **Capacidade e Alcance**: Respeita limitações físicas dos drones (peso e distância)
- **Tempo Real**: Dashboard com visualização em tempo real das operações


### Diferenciais Técnicos

- **Algoritmo Inteligente**: Prioriza entregas por importância, depois otimiza por distância
- **Sistema de Estados**: Drones transitam entre estados (IDLE → PLANNED → IN_FLIGHT → COMPLETED)
- **Arquitetura Escalável**: Backend RESTful + Frontend SPA + Banco Relacional
- **Validação de Negócio**: Regras rigorosas de capacidade, alcance e prioridades


## Funcionalidades

### Core Features

- **Gestão de Drones**: CRUD completo com especificações técnicas
- **Sistema de Pedidos**: Criação com localização, peso e prioridade
- **Planejamento Inteligente**: Algoritmo otimizado de alocação de entregas
- **Visualização de Viagens**: Dashboard com detalhes expansíveis
- **Estados dos Drones**: Transições de estado em tempo real


### Funcionalidades Avançadas

- **Cálculo de Distância**: Manhattan distance para ambiente urbano
- **Validação de Capacidade**: Peso total ≤ capacidade do drone
- **Validação de Alcance**: Rota total ≤ autonomia do drone
- **Estimativa de Tempo**: Baseada na velocidade configurada
- **Persistência de Estado**: Todas as alterações são salvas no banco


## Arquitetura

### Visão Geral

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │    Backend      │    │   PostgreSQL    │
│                 │    │                 │    │                 │
│ • Next.js 15    │◄──►│ • Spring Boot 3 │◄──►│ • Version 16    │
│ • React 19      │    │ • Java 24       │    │ • Relational    │
│ • TypeScript    │    │ • JPA/Hibernate │    │ • ACID          │
│ • Tailwind CSS  │    │ • Maven         │    │ • Transactions  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Estrutura do Projeto

```
dti-drone/
├── src/                           # Código-fonte do backend Java
│   ├── main/java/com/example/backend/
│   │   ├── controller/           # Controllers REST API
│   │   │   ├── DroneController.java      # CRUD de drones
│   │   │   ├── OrderController.java      # CRUD de pedidos  
│   │   │   ├── TripController.java       # CRUD de viagens
│   │   │   ├── TripStopController.java   # CRUD de paradas
│   │   │   └── PlanController.java       # Algoritmo de planejamento
│   │   ├── domain/               # Entidades JPA
│   │   │   ├── Drone.java               # Modelo de drone
│   │   │   ├── Order.java               # Modelo de pedido
│   │   │   ├── Trip.java                # Modelo de viagem
│   │   │   └── TripStop.java            # Modelo de parada
│   │   ├── service/              # Lógica de negócio
│   │   │   ├── DroneService.java        # Operações de drones
│   │   │   ├── OrderService.java        # Operações de pedidos
│   │   │   ├── TripService.java         # Operações de viagens
│   │   │   ├── TripStopService.java     # Operações de paradas
│   │   │   └── PlanningService.java     # Algoritmo inteligente
│   │   ├── repository/           # Acesso a dados JPA
│   │   │   ├── DroneRepository.java     # Consultas de drones
│   │   │   ├── OrderRepository.java     # Consultas de pedidos
│   │   │   ├── TripRepository.java      # Consultas de viagens
│   │   │   └── TripStopRepository.java  # Consultas de paradas
│   │   ├── exception/            # Tratamento de erros
│   │   │   └── GlobalExceptionHandler.java # Handler global
│   │   └── BackendApplication.java       # Classe principal Spring Boot
│   ├── main/resources/
│   │   ├── application.properties       # Configurações local
│   │   └── application-docker.properties # Configurações Docker
│   └── test/                            # Testes automatizados
├── frontend/frontend/            # Frontend Next.js + React
│   ├── src/app/                        # App Router do Next.js 15
│   │   ├── page.tsx                    # Dashboard principal
│   │   ├── layout.tsx                  # Layout base
│   │   └── globals.css                 # Estilos globais Tailwind
│   ├── public/                         # Assets estáticos
│   ├── package.json                    # Dependências frontend
│   ├── next.config.ts                  # Configuração Next.js
│   └── tsconfig.json                   # Configuração TypeScript
├── ops/                          # DevOps e infraestrutura
│   ├── docker-compose.yml             # Orquestração Docker
│   ├── Dockerfile                     # Build do backend
│   ├── dev.env.example               # Variáveis de ambiente
│   └── setup-dev.sh                  # Script de setup
├── target/                       # Build artifacts (gerado pelo Maven)
├── pom.xml                          # Configuração Maven
├── README.md                        # Esta documentação
└── .gitignore                       # Arquivos ignorados pelo Git
```

### Principais Arquivos

| Arquivo | Descrição | Função |
|---------|-----------|---------|
| `PlanningService.java` | **Cérebro do sistema** | Algoritmo knapsack + nearest neighbor |
| `page.tsx` | **Dashboard principal** | Interface React com visualização de viagens |
| `docker-compose.yml` | **Orquestração** | PostgreSQL + Backend em containers |
| `application.properties` | **Configurações** | Conexão banco, porta, profiles |
| `pom.xml` | **Dependências** | Spring Boot 3.4, PostgreSQL, JPA |


## Como Executar

### Pré-requisitos

- **Java 24** ou superior
- **Maven 3.8+**
- **Node.js 18+** e **npm**
- **Docker** e **Docker Compose**
- **Git**

### Para executar

```bash
# 1. Clone o repositório
git clone https://github.com/felipegiannetti/dti-drone.git
cd dti-drone

# 2. Inicie o PostgreSQL via Docker
cd ops
docker-compose up postgres -d

# 3. Execute o backend (as tabelas serão criadas automaticamente)
cd ..
mvn spring-boot:run

# 4. Execute o frontend (novo terminal)
cd frontend/frontend
npm install
npm run dev
```

> **Nota Importante**: As tabelas do banco de dados são criadas **automaticamente** pelo Hibernate na primeira execução. O sistema detecta que o banco está vazio e cria toda a estrutura necessária (`drones`, `orders`, `trips`, `trip_stops`).

### Banco de Dados Inicial

Após a primeira execução, o banco estará criado mas **vazio**. Você pode:

1. **Via Interface**: Usar o frontend para criar drones e pedidos
2. **Via API**: Fazer chamadas REST diretamente
3. **Via SQL**: Conectar no PostgreSQL e inserir dados manualmente

**Exemplos via API REST:**

```bash
# 1. Criar um drone
curl -X POST http://localhost:8080/drones \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Drone Alpha",
    "capacityKg": 10.0,
    "rangeKm": 25.0,
    "speedKmh": 40.0,
    "batteryPct": 100,
    "locationX": 0,
    "locationY": 0
  }'

# 2. Criar outro drone
curl -X POST http://localhost:8080/drones \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Drone Beta",
    "capacityKg": 8.0,
    "rangeKm": 30.0,
    "speedKmh": 45.0,
    "batteryPct": 100,
    "locationX": 2,
    "locationY": 2
  }'

# 3. Criar pedidos de alta prioridade
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerX": 5,
    "customerY": 8,
    "weightKg": 2.5,
    "priority": "HIGH"
  }'

# 4. Criar pedidos de média prioridade
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerX": 8,
    "customerY": 15,
    "weightKg": 4.0,
    "priority": "MEDIUM"
  }'

# 5. Criar pedidos de baixa prioridade
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerX": 15,
    "customerY": 12,
    "weightKg": 2.0,
    "priority": "LOW"
  }'

# 6. Executar o planejamento automático
curl -X POST http://localhost:8080/plan \
  -H "Content-Type: application/json"

# 7. Listar todas as viagens criadas
curl -X GET http://localhost:8080/trips

# 8. Listar todos os drones
curl -X GET http://localhost:8080/drones

# 9. Listar todos os pedidos
curl -X GET http://localhost:8080/orders

# 10. Listar apenas pedidos pendentes
curl -X GET "http://localhost:8080/orders?status=PENDING"

# 11. Listar apenas pedidos planejados
curl -X GET "http://localhost:8080/orders?status=PLANNED"
```

**Exemplo completo de inicialização via PowerShell/Bash:**
```bash
# Execute todos os comandos em sequência para popular o banco
echo "Criando drones..."
curl -s -X POST http://localhost:8080/drones -H "Content-Type: application/json" -d '{"name":"Drone Alpha","capacityKg":10.0,"rangeKm":25.0,"speedKmh":40.0,"batteryPct":100,"locationX":0,"locationY":0}'
curl -s -X POST http://localhost:8080/drones -H "Content-Type: application/json" -d '{"name":"Drone Beta","capacityKg":8.0,"rangeKm":30.0,"speedKmh":45.0,"batteryPct":100,"locationX":2,"locationY":2}'

echo "Criando pedidos..."
curl -s -X POST http://localhost:8080/orders -H "Content-Type: application/json" -d '{"customerX":5,"customerY":8,"weightKg":2.5,"priority":"HIGH"}'
curl -s -X POST http://localhost:8080/orders -H "Content-Type: application/json" -d '{"customerX":12,"customerY":6,"weightKg":3.0,"priority":"HIGH"}'
curl -s -X POST http://localhost:8080/orders -H "Content-Type: application/json" -d '{"customerX":8,"customerY":15,"weightKg":4.0,"priority":"MEDIUM"}'
curl -s -X POST http://localhost:8080/orders -H "Content-Type: application/json" -d '{"customerX":20,"customerY":10,"weightKg":1.5,"priority":"MEDIUM"}'
curl -s -X POST http://localhost:8080/orders -H "Content-Type: application/json" -d '{"customerX":15,"customerY":12,"weightKg":2.0,"priority":"LOW"}'

echo "Executando planejamento..."
curl -s -X POST http://localhost:8080/plan -H "Content-Type: application/json"

echo "Sistema populado com sucesso! Acesse http://localhost:3000 para ver os resultados."
```

**Exemplo de dados iniciais via SQL:**
```sql
-- Conectar no banco: psql -h localhost -U app -d drones
INSERT INTO drones (name, capacity_kg, range_km, speed_kmh, battery_pct, status, location_x, location_y) 
VALUES ('Drone Alpha', 10.0, 25.0, 40.0, 100, 'IDLE', 0, 0);

INSERT INTO orders (customer_x, customer_y, weight_kg, priority, status) 
VALUES (5, 8, 2.5, 'HIGH', 'PENDING'), (12, 15, 4.0, 'MEDIUM', 'PENDING');
```

### Acessos

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **PostgreSQL**: localhost:5432 (usuário: `app`, senha: `app`, database: `drones`)

## Executando Testes

O projeto possui uma suíte completa de testes automatizados cobrindo controllers e services.

### Estrutura de Testes

```
src/test/java/com/example/backend/
├── controller/                    # Testes de integração dos controllers
│   ├── DroneControllerTest.java       # Testes CRUD de drones
│   ├── OrderControllerTest.java       # Testes CRUD de pedidos
│   ├── TripControllerTest.java        # Testes CRUD de viagens
│   ├── TripStopControllerTest.java    # Testes CRUD de paradas
│   └── PlanControllerTest.java        # Testes do algoritmo via API
└── service/                       # Testes unitários dos services
    ├── DroneServiceTest.java          # Testes lógica de drones
    ├── OrderServiceTest.java          # Testes lógica de pedidos
    ├── TripServiceTest.java           # Testes lógica de viagens
    └── TripStopServiceTest.java       # Testes lógica de paradas
```

### Comandos de Teste

```bash
# Executar todos os testes
mvn test

### Principais Cenários Testados

#### **Service Tests (Testes Unitários)**
- **DroneServiceTest**: CRUD, validações de status, localização
- **OrderServiceTest**: CRUD, validações de prioridade, peso
- **TripServiceTest**: Criação de viagens, validações de capacidade
- **TripStopServiceTest**: Sequenciamento, marcação de entregas

#### **Controller Tests (Testes de Integração)**
- **API Endpoints**: Todos os endpoints testados
- **Códigos HTTP**: 200, 201, 400, 404, 409 conforme regras


> **Nota**: Os testes usam um banco H2 em memória, então não afetam seus dados do PostgreSQL de desenvolvimento.


## Como utilizei IA no desenvolvimento

### Objetivo
Acelerar a construção do MVP (backend Spring Boot + frontend Next.js) mantendo qualidade de código e regras de negócio claras.

### Fluxo de trabalho com IA
1. **Ideação/Planejamento**: pedi à IA um esqueleto de arquitetura e mapa de entidades.
2. **Ajustes dirigidos**: iterar através de pedidos de *refactors* (ex.: remover/renomear campos, mudar validações, padronizar enums).
3. **Testes**: solicitei à IA *tests* (JUnit para services e `@WebMvcTest` para controllers), depois ajustei os casos.
4. **Frontend/UI**: pedi componentes base e aprimoramento da estilização (layout, badges, skeletons, feedback).

### O que a IA ajudou a escrever
- **Regras e validações**: esqueleto de regras (capacidade, alcance, status) + `GlobalExceptionHandler`.
- **Testes**: testes de services e controllers (JUnit 5 + `@WebMvcTest`).
- **Frontend**: página única com grid, tabelas, formulários, estados de carregamento e toasts com Tailwind.

### Limites & boas práticas adotadas
- **Sem dependência cega**: todo output da IA foi **revisado** e ajustado conforme os requisitos e meus desejos.

### Ganhos percebidos
- Redução do tempo gasto com código base.
- Padronização de mensagens de erro e contratos HTTP.
- Velocidade na estilização do front sem sacrificar legibilidade.
