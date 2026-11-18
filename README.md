# Intelligent University Knowledge & Service Platform

> A centralized university platform that combines institutional
> knowledge management, AI-powered question answering, and
> administrative service workflows.

**Project status:** In planning / initial development\
**Project type:** Academic software engineering project\
**Proposed architecture:** Microservices — an Angular frontend behind an
Nginx API gateway, independently deployable Spring Boot services for each
business domain, and Python AI services, communicating through REST and
Apache Kafka

------------------------------------------------------------------------

## Table of Contents

-   [1. Overview](#1-overview)
-   [2. Problem Statement](#2-problem-statement)
-   [3. Objectives](#3-objectives)
-   [4. Main Features](#4-main-features)
-   [5. Architecture](#5-architecture)
-   [6. Technology Stack](#6-technology-stack)
-   [7. Main Workflows](#7-main-workflows)
-   [8. Security and Access Control](#8-security-and-access-control)
-   [9. Planned Repository Structure](#9-planned-repository-structure)
-   [10. Getting Started](#10-getting-started)
-   [11. Configuration](#11-configuration)
-   [12. Development Roadmap](#12-development-roadmap)
-   [13. Testing and Quality](#13-testing-and-quality)
-   [14. Deployment and Operations](#14-deployment-and-operations)
-   [15. Documentation](#15-documentation)
-   [16. Contributing](#16-contributing)
-   [17. License](#17-license)

------------------------------------------------------------------------

## 1. Overview

The **Intelligent University Knowledge & Service Platform** is a
web-based application designed to make university information and
administrative services easier to access.

University documents and procedures are often distributed across
different websites, PDFs, offices, and communication channels. Students
and staff may spend considerable time locating reliable information or
understanding the steps required to complete an administrative
procedure.

This project aims to provide a single platform where authorized users
can:

-   Discover and consult centralized university documents.
-   Ask questions in natural language and receive answers grounded in
    institutional documents.
-   Submit administrative requests and follow their progress.
-   Manage documents, users, and requests according to their
    permissions.

The AI assistant uses **Retrieval-Augmented Generation (RAG)** to
retrieve relevant institutional content before generating an answer.
Answers should include references to the documents or passages used,
helping users verify the information.

## 2. Problem Statement

University information is not always centralized, searchable, or easy to
understand. Students may need to consult several sources or contact
administrative staff to find answers to common questions. Administrative
teams, meanwhile, need a consistent way to receive, assign, and process
requests.

The project addresses these challenges through a unified application
that combines:

1.  A structured institutional document repository.
2.  Semantic and conversational search powered by RAG.
3.  A workflow for submitting and processing administrative requests.
4.  Authentication, authorization, and traceability.

## 3. Objectives

### Functional objectives

-   Centralize institutional documents and their metadata.
-   Make documents discoverable through categories and search.
-   Provide contextual answers to questions about university procedures.
-   Display sources supporting AI-generated answers.
-   Allow students to submit and track administrative requests.
-   Allow authorized staff to process and update requests.
-   Provide administrative tools for managing users, documents, and
    workflows.

### Engineering objectives

-   Apply clean code and separation of concerns.
-   Decompose the system into independently deployable microservices
    aligned with business domains (bounded contexts).
-   Give each service ownership of its own data, with no shared tables
    between services.
-   Implement secure authentication and role-based authorization that
    every service enforces independently.
-   Use Apache Kafka for event-driven, asynchronous communication between
    services.
-   Build separate AI services for document ingestion and RAG
    operations.
-   Containerize every service and its dependencies.
-   Add automated testing, monitoring, and CI/CD practices per service.

## 4. Main Features

### 4.1 Authentication and user management

-   User sign-in and sign-out.
-   User profiles.
-   Role-based access control.
-   Protected application features and API endpoints.
-   Administrative user management.

*Owned by: Identity Service*

### 4.2 Institutional document management

-   Upload and store documents.
-   Organize documents using categories and metadata.
-   Consult and download authorized documents.
-   Track document status and indexing status.
-   Trigger document extraction and indexing after upload.

Supported formats will be defined during implementation. PDF support is
a primary target.

*Owned by: Document Service (metadata, storage) and the AI bounded
context (extraction, indexing)*

### 4.3 AI-powered university assistant

Users can ask natural-language questions about university information,
such as procedures, deadlines, regulations, and institutional documents.

The assistant is designed to:

1.  Receive a user's question.
2.  Convert the question into an embedding.
3.  Retrieve relevant document chunks from the vector index.
4.  Apply access-control filters to the retrieved content.
5.  Build a context-aware prompt.
6.  Generate an answer using an LLM.
7.  Return the answer with references to supporting sources.

The assistant should avoid presenting unsupported information as fact.
If the available documents do not contain enough information, it should
communicate that limitation rather than inventing an answer.

*Owned by: Assistant Service (conversations) and AI Service (retrieval
and generation)*

### 4.4 Administrative request management

-   Submit a request using a defined request type.
-   View submitted requests.
-   Track request status.
-   Add comments or supporting information where permitted.
-   Assign requests to responsible staff.
-   Update status and record processing activity.

Example lifecycle:

`SUBMITTED → UNDER_REVIEW → APPROVED / REJECTED`

Additional states, such as `NEED_MORE_INFORMATION` or `CLOSED`, may be
introduced when requirements are finalized.

*Owned by: Request Service*

### 4.5 Notifications

-   Notify users when relevant request events occur.
-   Notify staff about newly assigned requests.
-   Support asynchronous notification processing.

The initial notification channels will be selected during
implementation. Email can be added as a first channel.

*Owned by: Notification Service*

### 4.6 Administrative dashboard

-   Overview of users and roles.
-   Document and indexing overview.
-   Administrative request statistics.
-   Request queues and processing status.
-   Relevant activity and operational indicators.

*Owned by: Reporting Service (statistics) and Audit Service (activity
records)*

------------------------------------------------------------------------

## 5. Architecture

The platform uses a **microservices architecture**. Each business
capability is implemented as an independently deployable service that
owns its data, exposes a clear API, and communicates with other services
through REST (synchronous) or Apache Kafka (asynchronous events).

All external traffic enters through a single **Nginx API gateway**.
Services are not called directly by the browser, and internal-only
services (such as the AI Service) are never exposed through the gateway.

### High-level architecture

``` text
                              ┌─────────────────────────────┐
                              │      Angular Web Client     │
                              └─────────────────────────────┘
                                             │ HTTPS / REST + JWT
                                             ▼
                              ┌─────────────────────────────┐
                              │      Nginx API Gateway      │
                              │ TLS · routing · rate limits │
                              │ request IDs · upload limits │
                              └─────────────────────────────┘
                                             │ path-based routing /api/v1/...
      ┌────────────┬────────────┬────────────┼────────────┬────────────┬────────────┐
      ▼            ▼            ▼            ▼            ▼            ▼            ▼
 ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐
 │ Identity │ │ Document │ │ Request  │ │Assistant │ │ Notifi-  │ │  Audit   │ │Reporting │
 │ Service  │ │ Service  │ │ Service  │ │ Service  │ │cation Svc│ │ Service  │ │ Service  │
 └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘
      │            │            │            │            │            │            │
      ▼            ▼            ▼            ▼            ▼            ▼            ▼
 ════════════════════════════════════════════════════════════════════════════════════════
       Apache Kafka — domain events · commands · retry / dead-letter topics
 ════════════════════════════════════════════════════════════════════════════════════════
                    ▼ ▲  DocumentProcessingRequested (in) · DocumentIndexed / ...Failed (out)
 ┌─ AI bounded context (Python) ────────────────────────────────────────────────────────┐
 │      ┌──────────────────────────┐              ┌──────────────────────────┐          │
 │      │     Ingestion Worker     │              │   AI Service (FastAPI)   │          │
 │      │ extract · chunk · embed  │              │  embed · retrieve · LLM  │          │
 │      └──────────────────────────┘              └──────────────────────────┘          │
 │                                                                                      │
 │  AI Service is internal only: called by Assistant Service over REST,                 │
 │  never exposed by Nginx. Both processes own the vector store (pgvector).             │
 └──────────────────────────────────────────────────────────────────────────────────────┘

        Data: PostgreSQL (one database per service; pgvector for the AI context)
        Files: MinIO (one bucket per owning service)
        Observability: Prometheus + Grafana
        Runtime: Docker / Docker Compose
```

This diagram represents the intended logical architecture. The exact
deployment topology and communication paths may evolve during
implementation.

### Service catalogue

| Service | Stack | Responsibility | Owned data | Main events published |
|---|---|---|---|---|
| **Identity Service** | Spring Boot | Sign-in, sign-out, token issuance and refresh, users, roles, permissions, profiles | `identity_db`: User, Role, Permission, refresh tokens | `UserCreated`, `UserUpdated`, `UserDeactivated`, `RoleAssigned` |
| **Document Service** | Spring Boot | Document upload, metadata, categories, versions, access rules, logical deletion, indexing status | `document_db`: Document, DocumentVersion, Category, access rules; MinIO bucket `university-documents` | `DocumentUploaded`, `DocumentProcessingRequested`, `DocumentAccessChanged`, `DocumentDeleted` |
| **Request Service** | Spring Boot | Request submission, assignment, status state machine, comments, history, attachments | `request_db`: AdministrativeRequest, RequestComment, status history; MinIO bucket `request-attachments` | `RequestSubmitted`, `RequestAssigned`, `RequestStatusChanged`, `RequestCommentAdded` |
| **Assistant Service** | Spring Boot | Conversations, chat messages, retention policy, orchestration of RAG calls | `assistant_db`: Conversation, ChatMessage, cited sources | `AssistantQuestionAnswered` |
| **AI Service** | Python, FastAPI | Query embedding, vector retrieval with access filtering, prompt construction, LLM generation, citation building, RAG evaluation | `ai_db` (with pgvector): DocumentChunk, embeddings, chunk access metadata | — (called synchronously) |
| **Ingestion Worker** | Python | Kafka consumer: fetch file, extract text, clean, chunk, embed, store vectors | Shares `ai_db` with the AI Service (same bounded context) | `DocumentIndexed`, `DocumentProcessingFailed` |
| **Notification Service** | Spring Boot | Consumes domain events and explicit `NotificationRequested` commands, creates in-app notifications, manages preferences, later sends email | `notification_db`: Notification, preferences, delivery records | — |
| **Audit Service** | Spring Boot | Consumes audit-relevant events from all services into an append-only log; exposes audit queries to authorized users | `audit_db`: AuditLog | — |
| **Reporting Service** | Spring Boot | Builds read models from domain events to serve dashboard statistics without querying other services' databases | `reporting_db`: aggregated indicators | — |

The Ingestion Worker and the AI Service are two processes of the same
bounded context: they are built from the same Python codebase and share
the vector store. No other service reads or writes `ai_db`.

### Architectural principles

-   **Service per bounded context:** each service maps to one business
    domain and can be developed, tested, built, and deployed
    independently.
-   **Database per service:** each service owns its schema and is the
    only one allowed to access it. Services never join across databases
    or read another service's tables. In development, the databases run
    on a single PostgreSQL instance with separate databases and
    credentials; they can be split onto separate instances later
    without code changes.
-   **API gateway:** Nginx is the single entry point. It handles TLS
    termination, path-based routing, rate limiting, upload size limits,
    and request ID generation.
-   **REST for synchronous operations:** used only when the caller needs
    an immediate answer (for example, the Assistant Service calling the
    AI Service). Every synchronous call has a timeout, limited retries,
    and a defined fallback.
-   **Kafka for asynchronous workflows:** document indexing,
    notifications, audit, and reporting are driven by events. Services
    react to events instead of calling each other, which keeps them
    loosely coupled.
-   **Local data copies over runtime calls:** when a service needs data
    owned by another (for example, staff names in the Request Service),
    it keeps a small local projection updated from events rather than
    calling the owner on every request.
-   **Reliable event publishing:** services use the transactional outbox
    pattern so that a database change and its event are never out of
    sync.
-   **Idempotent consumers:** every consumer records processed event IDs
    so that redelivered events are handled once.
-   **Vector search:** pgvector provides vector similarity search within
    PostgreSQL for the initial RAG implementation.
-   **Object storage:** MinIO stores uploaded files; each owning service
    stores metadata and object references in its own database.
-   **Defense in depth:** every service validates tokens and enforces
    authorization itself, independently of the gateway and of frontend
    visibility.
-   **Stateless services:** services keep no user session in memory, so
    any service can be scaled to several instances.

### Communication matrix

| Source | Destination | Mechanism | Purpose |
|---|---|---|---|
| Angular | Nginx gateway | HTTPS / REST | All user operations |
| Nginx gateway | Business services | HTTP / REST | Routing by path prefix |
| Assistant Service | AI Service | HTTP / REST (internal) | Retrieve and generate an answer |
| Business services | Kafka | Producer (via outbox) | Publish domain events |
| Business services | Kafka | Consumer | React to other services' events |
| Ingestion Worker | Kafka | Consumer / Producer | Index documents, report result |
| Document / Request Service | MinIO | S3 API | Store and serve files |
| Ingestion Worker | MinIO | S3 API (read-only) | Fetch documents to index |
| Each service | Its own PostgreSQL database | JPA / Hibernate or SQL driver | Persistence |
| All services | Identity Service | JWKS endpoint (cached) | Fetch the public key to verify JWTs |
| Prometheus | All services | Metrics scraping | Monitoring |

### Gateway routing

| Path prefix | Target service |
|---|---|
| `/` | Angular application |
| `/api/v1/auth`, `/api/v1/users` | Identity Service |
| `/api/v1/documents` | Document Service |
| `/api/v1/requests` | Request Service |
| `/api/v1/assistant` | Assistant Service |
| `/api/v1/notifications` | Notification Service |
| `/api/v1/audit` | Audit Service |
| `/api/v1/reports` | Reporting Service |

### Event contracts

All events share a common envelope so that every consumer can handle
deduplication, tracing, and versioning the same way:

``` json
{
  "eventId": "uuid",
  "eventType": "RequestStatusChanged",
  "eventVersion": 1,
  "occurredAt": "2026-11-18T10:15:00Z",
  "producer": "request-service",
  "correlationId": "request-id-from-gateway",
  "payload": { }
}
```

Planned topics:

| Topic | Producer | Consumers | Key |
|---|---|---|---|
| `identity.user.events` | Identity Service | Request, Notification, Audit, Reporting | userId |
| `document.events` | Document Service | Ingestion Worker, Notification, Audit, Reporting | documentId |
| `document.processing.results` | Ingestion Worker | Document Service, Notification, Reporting | documentId |
| `request.events` | Request Service | Notification, Audit, Reporting | requestId |
| `assistant.events` | Assistant Service | Reporting, Audit | conversationId |

Events are keyed by aggregate ID so that all events for one document or
one request are processed in order. Each consumer group uses retry
topics and a dead-letter topic for messages that keep failing. Event
schemas are versioned and stored in `contracts/events/`.

### Why microservices

The project deliberately adopts microservices to practise distributed
system design: independent deployment, data ownership, event-driven
integration, and per-service scaling and failure isolation. For example,
a slow LLM provider affects only the Assistant and AI services, and a
burst of document uploads can be absorbed by scaling the Ingestion
Worker alone.

This choice also brings costs that the design must address explicitly:

| Concern | How it is handled |
|---|---|
| Data consistency across services | Eventual consistency through events, outbox pattern, idempotent consumers |
| Network failures between services | Timeouts, bounded retries with backoff, fallbacks, dead-letter topics |
| Duplicate authorization logic | A shared security convention (token claims, permission names) implemented in each service |
| Debugging across services | Correlation IDs generated at the gateway and propagated in HTTP headers and event envelopes; structured logs |
| Operational overhead | Docker Compose for the full stack, one Dockerfile and CI job per service, a shared service template |

To keep the scope realistic, services are introduced progressively (see
[Development Roadmap](#12-development-roadmap)). Avoid creating a
service without a concrete domain responsibility.

## 6. Technology Stack

| Area | Technology | Purpose |
|---|---|---|
| Frontend | Angular, TypeScript | User interface |
| Business microservices | Java, Spring Boot | Business logic and REST APIs, one application per service |
| AI services | Python, FastAPI | Document processing and RAG |
| Relational database | PostgreSQL | Application data, one database per service |
| Vector search | pgvector | Embedding storage and similarity search |
| Messaging | Apache Kafka | Asynchronous event-driven communication between services |
| Object storage | MinIO | Uploaded document and attachment storage |
| API gateway / reverse proxy | Nginx | Single entry point, routing, TLS, rate limiting |
| Containers | Docker, Docker Compose | Reproducible development and deployment of all services |
| Monitoring | Prometheus, Grafana | Per-service metrics and dashboards |
| CI/CD | GitHub Actions | Automated validation and delivery per service |

Specific library versions, LLM providers, embedding models, and
deployment environments will be selected and documented during
implementation.

## 7. Main Workflows

### 7.1 Document upload and indexing

1.  An authorized staff member uploads a document through Angular.
2.  Nginx routes the request to the **Document Service**, which
    validates the token, the user's permissions, and the file.
3.  The Document Service stores the file in MinIO.
4.  It persists the document metadata and access rules in `document_db`
    with status `PENDING`, and writes a `DocumentProcessingRequested`
    event to its outbox in the same transaction.
5.  The outbox publisher sends the event to Kafka. The event carries the
    object reference and the document's access metadata.
6.  The **Ingestion Worker** consumes the event and retrieves the file
    from MinIO.
7.  It extracts text, cleans it, and splits it into chunks.
8.  It generates embeddings and stores the chunks, vectors, and access
    metadata in `ai_db` using pgvector.
9.  It publishes `DocumentIndexed` (or `DocumentProcessingFailed` with
    the reason).
10. The Document Service consumes the result and updates the indexing
    status to `INDEXED` or `FAILED`; a failed document can be
    resubmitted for processing.
11. The Notification, Audit, and Reporting services consume the same
    events independently.

Indexing is asynchronous so that a potentially expensive
document-processing operation does not block the upload request.

When a document's access rules change or it is deleted, the Document
Service publishes `DocumentAccessChanged` or `DocumentDeleted`, and the
Ingestion Worker updates or removes the related chunks.

### 7.2 Question answering with RAG

1.  The user submits a question to the **Assistant Service** through
    the gateway.
2.  The Assistant Service validates the JWT, loads the conversation,
    and checks that the user owns it.
3.  It calls the **AI Service** over internal REST, forwarding the
    user's token, the question, and the relevant conversation history.
4.  The AI Service validates the forwarded token itself and derives the
    user's access scope from its claims — never from fields in the
    request body.
5.  It generates an embedding for the question.
6.  It retrieves relevant chunks from pgvector, with the access filter
    applied inside the retrieval query.
7.  The authorized context is supplied to the LLM.
8.  The AI Service returns the answer and source references.
9.  The Assistant Service stores the message and citations, publishes
    `AssistantQuestionAnswered`, and returns the result to Angular.

If the AI Service or the LLM provider is unavailable, the Assistant
Service returns a clear error after its timeout instead of blocking.

**Important:** access filtering must happen during retrieval and must be
enforced by trusted backend logic. Hiding documents in the Angular
interface is not sufficient security.

### 7.3 Administrative request processing

1.  A student submits a request to the **Request Service**.
2.  The Request Service validates the request, checks the student's
    role, persists it, and publishes `RequestSubmitted`.
3.  The request is made available to the appropriate staff workflow.
4.  A responsible staff member reviews and processes it; the Request
    Service enforces valid status transitions and ownership.
5.  Each status change is recorded in the request history and published
    as `RequestStatusChanged`.
6.  The **Notification Service** consumes these events and notifies the
    student and the assigned staff.
7.  The **Audit** and **Reporting** services record the activity and
    update statistics.
8.  The student can consult the updated status.

------------------------------------------------------------------------

## 8. Security and Access Control

Security is a core requirement because the platform handles user
accounts, institutional documents, and administrative requests. In a
microservices architecture, every service is a potential entry point, so
each one enforces security on its own.

Planned security controls include:

-   Authentication and secure password storage in the Identity Service.
-   Short-lived JWT access tokens signed with an asymmetric key by the
    Identity Service; every service verifies signatures with the public
    key published on a JWKS endpoint, so no service needs to call the
    Identity Service on each request.
-   Refresh tokens stored and revocable by the Identity Service; user
    deactivation is published as `UserDeactivated`.
-   Role-based access control (RBAC) with permissions carried as token
    claims.
-   Backend authorization in every service for every protected
    operation, including resource-ownership checks (for example, a
    student cannot read another student's request by changing an ID in
    the URL).
-   Validation of incoming data in each service.
-   Secure handling of uploaded files.
-   Restricted access to documents and request records.
-   Internal-only services (AI Service, Ingestion Worker) reachable only
    on the internal Docker network, never through the gateway.
-   Least-privilege credentials: one database user per service, scoped
    MinIO bucket policies, and Kafka access control in deployed
    environments.
-   Protection of secrets through environment configuration.
-   Audit records for important operations, centralized in the Audit
    Service.
-   Secure communication over HTTPS in deployed environments.
-   Rate limiting at the gateway and other API protections where
    appropriate.
-   RAG-specific protections: content of retrieved documents is never
    treated as instructions, and authorization filters are applied
    before any passage reaches the LLM.

Roles and permissions will be finalized with the functional
requirements. Possible roles include:

| Role | Typical responsibilities |
|---|---|
| Student | Consult accessible documents, use the assistant, submit and track personal requests |
| Administrative Staff | Process requests assigned to their scope |
| Document Manager | Upload and manage institutional documents |
| Administrator | Manage users, roles, configuration, and platform operations |

The final permission model should be based on actions and resource
scope, not simply on frontend pages.

------------------------------------------------------------------------

## 9. Planned Repository Structure

The following is a proposed monorepo structure: one directory per
independently buildable service. Directories can be adjusted as
implementation decisions become concrete.

``` text
university-platform/
│
├── frontend/
│   └── angular-app/
│
├── services/
│   ├── identity-service/          # Spring Boot
│   │   ├── src/
│   │   ├── Dockerfile
│   │   ├── pom.xml
│   │   └── README.md
│   ├── document-service/          # Spring Boot
│   ├── request-service/           # Spring Boot
│   ├── assistant-service/         # Spring Boot
│   ├── notification-service/      # Spring Boot
│   ├── audit-service/             # Spring Boot
│   ├── reporting-service/         # Spring Boot
│   └── ai-service/                # Python: FastAPI API + Ingestion Worker
│       ├── app/
│       │   ├── api/
│       │   ├── ingestion/
│       │   ├── embeddings/
│       │   ├── retrieval/
│       │   ├── rag/
│       │   └── worker/
│       ├── tests/
│       ├── Dockerfile
│       └── requirements.txt
│
├── contracts/
│   ├── events/                    # Versioned Kafka event schemas
│   └── openapi/                   # API specifications per service
│
├── infrastructure/
│   ├── docker/
│   ├── nginx/                     # Gateway routing configuration
│   ├── postgres/                  # Init scripts: one database + user per service
│   ├── kafka/                     # Topic creation scripts
│   ├── prometheus/
│   └── grafana/
│
├── docs/
│   ├── architecture/
│   ├── api/
│   └── decisions/
│
├── scripts/
│
├── .github/
│   └── workflows/                 # One CI workflow per service
│
├── .env.example
├── .gitignore
├── compose.yaml
└── README.md
```

Services share contracts, not code. Avoid shared libraries that contain
domain logic; if a small technical library becomes necessary (for
example, the event envelope), keep it versioned and free of business
rules.

## 10. Getting Started

> **Implementation note:** This section describes the intended local
> development environment. It becomes executable once the application
> code, Dockerfiles, and Compose configuration have been added to the
> repository.

### Prerequisites

Install the following tools:

-   Git
-   Docker Engine
-   Docker Compose plugin
-   Node.js and npm (for local Angular development)
-   Java JDK (version to be fixed by the project)
-   Maven or the Maven Wrapper
-   Python (version to be fixed by the project)

### Clone the repository

``` bash
git clone <repository-url>
cd university-platform
```

### Configure environment variables

Create a local environment file from the example:

``` bash
cp .env.example .env
```

Update the values for your local environment. Do not commit `.env` or
real credentials.

### Start the full platform

Once `compose.yaml` is implemented:

``` bash
docker compose up -d
```

Start only the infrastructure (useful when running one service from your
IDE):

``` bash
docker compose up -d postgres kafka minio nginx
```

Inspect the running services:

``` bash
docker compose ps
```

Follow the logs of one service:

``` bash
docker compose logs -f request-service
```

Rebuild and restart a single service after a change:

``` bash
docker compose up -d --build document-service
```

Stop the environment:

``` bash
docker compose down
```

To remove containers and the Compose-managed volumes as well, use the
destructive command below only when you intentionally want to delete
persisted local data:

``` bash
docker compose down -v
```

### Run services independently

During development, each service can be started on its own while the
rest of the platform runs in Docker.

**Angular frontend**

``` bash
cd frontend/angular-app
npm install
npm start
```

**A Spring Boot service** (same pattern for every Java service)

``` bash
cd services/identity-service
./mvnw spring-boot:run
```

**Python AI Service**

``` bash
cd services/ai-service
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload
```

**Python Ingestion Worker** (same codebase, different entry point)

``` bash
cd services/ai-service
source .venv/bin/activate
python -m app.worker.main
```

### Planned local ports

| Component | Port |
|---|---|
| Nginx gateway | 80 / 443 |
| Identity Service | 8081 |
| Document Service | 8082 |
| Request Service | 8083 |
| Assistant Service | 8084 |
| Notification Service | 8085 |
| Audit Service | 8086 |
| Reporting Service | 8087 |
| AI Service | 8000 (internal) |
| PostgreSQL | 5432 |
| Kafka | 9092 |
| MinIO API / console | 9000 / 9001 |
| Prometheus | 9090 |
| Grafana | 3000 |

The exact commands, ports, and environment variables must match the
final implementation.

## 11. Configuration

Use environment variables for configuration that differs between
environments or contains secrets. Each service reads only the variables
it needs.

An illustrative `.env.example`:

``` dotenv
# Application
APP_ENV=development

# PostgreSQL (one instance in development, one database per service)
POSTGRES_HOST=postgres
POSTGRES_PORT=5432
POSTGRES_ADMIN_USER=postgres
POSTGRES_ADMIN_PASSWORD=change_me

IDENTITY_DB_NAME=identity_db
IDENTITY_DB_USER=identity_user
IDENTITY_DB_PASSWORD=change_me

DOCUMENT_DB_NAME=document_db
DOCUMENT_DB_USER=document_user
DOCUMENT_DB_PASSWORD=change_me

REQUEST_DB_NAME=request_db
REQUEST_DB_USER=request_user
REQUEST_DB_PASSWORD=change_me

ASSISTANT_DB_NAME=assistant_db
ASSISTANT_DB_USER=assistant_user
ASSISTANT_DB_PASSWORD=change_me

NOTIFICATION_DB_NAME=notification_db
NOTIFICATION_DB_USER=notification_user
NOTIFICATION_DB_PASSWORD=change_me

AUDIT_DB_NAME=audit_db
AUDIT_DB_USER=audit_user
AUDIT_DB_PASSWORD=change_me

REPORTING_DB_NAME=reporting_db
REPORTING_DB_USER=reporting_user
REPORTING_DB_PASSWORD=change_me

AI_DB_NAME=ai_db
AI_DB_USER=ai_user
AI_DB_PASSWORD=change_me

# Spring Boot
SPRING_PROFILES_ACTIVE=dev

# Security (Identity Service signs tokens; others verify via JWKS)
JWT_ISSUER=http://identity-service:8081
JWT_JWKS_URI=http://identity-service:8081/.well-known/jwks.json
JWT_ACCESS_TOKEN_TTL=15m
JWT_REFRESH_TOKEN_TTL=7d

# AI service
AI_SERVICE_URL=http://ai-service:8000
AI_SERVICE_TIMEOUT=30s
LLM_PROVIDER=replace_with_provider
LLM_API_KEY=replace_with_local_secret
EMBEDDING_MODEL=replace_with_embedding_model

# Kafka
KAFKA_BOOTSTRAP_SERVERS=kafka:9092

# Object storage
MINIO_ENDPOINT=http://minio:9000
MINIO_DOCUMENTS_BUCKET=university-documents
MINIO_ATTACHMENTS_BUCKET=request-attachments
DOCUMENT_SERVICE_MINIO_ACCESS_KEY=change_me
DOCUMENT_SERVICE_MINIO_SECRET_KEY=change_me
REQUEST_SERVICE_MINIO_ACCESS_KEY=change_me
REQUEST_SERVICE_MINIO_SECRET_KEY=change_me
INGESTION_MINIO_ACCESS_KEY=change_me      # read-only on documents bucket
INGESTION_MINIO_SECRET_KEY=change_me
```

These values are examples, not a complete configuration. The actual
environment variables must be defined by the implemented services. Use
strong local secrets and a secure secret-management approach outside
development.

## 12. Development Roadmap

The project will be developed incrementally. Services are introduced one
at a time so that the platform stays runnable at the end of each phase.

| Phase | Scope | Expected result |
|---|---|---|
| 1. Requirements and design | Use cases, roles, domain model, bounded contexts, service boundaries, API and event contracts | Validated functional and technical design |
| 2. Platform foundation | Monorepo, service template, Docker Compose, Nginx gateway, PostgreSQL with per-service databases, Kafka, MinIO, CI skeleton | Running infrastructure and an empty service deployable end to end |
| 3. Identity Service | Authentication, users, roles, JWT issuance, JWKS, token verification in the service template | Secured foundation reused by every service |
| 4. Document Service | Upload, metadata, categories, MinIO integration, outbox publishing | Usable document repository |
| 5. AI context: RAG foundation | Ingestion Worker (extraction, chunking, embeddings, pgvector), AI Service retrieval with access filtering | Documents indexed asynchronously and retrievable |
| 6. Assistant Service | Conversations, LLM integration, prompts, source references, timeouts and failure handling | Conversational assistant |
| 7. Event reliability and async services | Idempotent consumers, retry and dead-letter topics, Notification Service, Audit Service | Reliable asynchronous processing |
| 8. Request Service | Submission, assignment, status state machine, history, attachments | End-to-end request workflow |
| 9. Angular application and reporting | User-facing screens, dashboards, Reporting Service read models | Complete web experience |
| 10. Quality and operations | Contract and end-to-end tests, per-service metrics, dashboards, CI/CD, hardening | Deployable and maintainable platform |

If time becomes short, the Audit and Reporting services can be delivered
last with a reduced scope without affecting the core user journeys.

The sequence may be adjusted when dependencies or supervisor feedback
require it. Security, testing, and documentation should be developed
continuously rather than postponed until the final phase.

## 13. Testing and Quality

Testing will cover the system at multiple levels.

-   **Unit tests:** validate business rules and individual components
    within each service.
-   **Integration tests:** validate each service against real
    PostgreSQL, Kafka, and MinIO containers.
-   **API tests:** verify request validation, response contracts, and
    authorization for every service.
-   **Contract tests:** verify that producers and consumers agree on the
    REST APIs in `contracts/openapi/` and the event schemas in
    `contracts/events/`, so that one service can change without breaking
    another.
-   **Resilience tests:** verify behaviour when a dependency is slow or
    down (AI Service timeout, Kafka unavailable, duplicate events).
-   **Frontend tests:** validate important Angular components and user
    workflows.
-   **AI evaluation:** assess retrieval relevance, answer faithfulness,
    citation correctness, and behavior when context is insufficient.
-   **End-to-end tests:** validate complete user journeys across
    services through the gateway.

Quality practices:

-   Consistent formatting and naming conventions across services.
-   Clear separation between controllers, services, repositories, and
    DTOs within each service.
-   Centralized error handling and a uniform error response format
    across services.
-   Input validation and meaningful API responses.
-   Database migrations under version control, per service.
-   Code reviews and automated CI checks.

For RAG, a successful HTTP response alone is not enough: retrieval
quality and answer grounding must also be evaluated.

## 14. Deployment and Operations

Every service is built into its own Docker image. Docker Compose will
initially support local development and integrated testing of the full
platform.

Operational capabilities planned for the project:

-   Health checks for every service (liveness and readiness), used by
    Docker Compose dependency ordering.
-   Structured logs including the correlation ID, so a single user
    action can be followed across services.
-   Prometheus metrics exposed by every service, including Kafka
    consumer lag and document-processing metrics.
-   Grafana dashboards per service and for the platform as a whole.
-   Resource-aware container configuration.
-   GitHub Actions workflows per service, triggered only when that
    service's directory changes, building and testing it independently.
-   Environment-specific configuration.
-   Persistent storage and backup considerations for each service's
    database and for MinIO.

A production deployment will additionally require appropriate TLS
configuration, secret management, access restrictions, database backups,
and operational monitoring.

## 15. Documentation

Project documentation will include:

-   Functional requirements and use cases.
-   Architecture diagrams, service boundaries, and architectural
    decision records.
-   Database model and migrations for each service.
-   REST API documentation (OpenAPI) for each service.
-   Authentication and authorization model.
-   RAG ingestion and retrieval design.
-   Kafka topics, event contracts, and processing flows.
-   Local development and deployment instructions.
-   Test strategy and evaluation results.

## 16. Contributing

During development:

1.  Create a branch for each feature or fix.
2.  Keep changes focused and reviewable, ideally within one service.
3.  Add or update tests for changed behavior.
4.  Update documentation and contracts when APIs, events,
    configuration, or architecture change. Treat breaking contract
    changes as new versions.
5.  Avoid committing secrets, local environment files, generated
    artifacts, or uploaded private documents.
6.  Open a pull request and review the changes before merging.

Suggested branch naming:

``` text
feature/document-service-upload
feature/ai-service-retrieval
feature/request-service-workflow
fix/identity-service-token-validation
docs/architecture-overview
```

## 17. License

The project's license has not yet been selected. Add a `LICENSE` file
and update this section once the licensing decision has been made.

------------------------------------------------------------------------

## Project Summary

The Intelligent University Knowledge & Service Platform aims to bring
together three complementary capabilities:

1.  **Knowledge management** --- a centralized, permission-aware
    repository of institutional documents.
2.  **Generative AI** --- a RAG-based assistant that answers questions
    using verifiable university sources.
3.  **Administrative services** --- structured request submission,
    processing, and tracking.

These capabilities are delivered as independently deployable
microservices connected by an API gateway and an event backbone.

The project is also an opportunity to apply practical software
engineering principles across backend development, frontend development,
AI integration, distributed systems and event-driven communication,
security, containerization, testing, and DevOps.
