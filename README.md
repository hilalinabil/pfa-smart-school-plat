# Intelligent University Knowledge & Service Platform

> A centralized university platform that combines institutional
> knowledge management, AI-powered question answering, and
> administrative service workflows.

**Project status:** In planning / initial development\
**Project type:** Academic software engineering project\
**Proposed architecture:** Microservices — an Angular frontend behind an
Nginx edge proxy and a Spring Cloud API gateway with service discovery,
independently deployable Spring Boot services for each business domain,
and Python AI services, communicating through REST and Apache Kafka

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
-   Implement secure authentication and a permission-based authorization
    model, in which roles are configurable data and every service enforces
    permissions independently.
-   Use Apache Kafka for event-driven, asynchronous communication between
    services.
-   Build separate AI services for document ingestion and RAG
    operations.
-   Containerize every service and its dependencies.
-   Add automated testing, monitoring, and CI/CD practices per service.

## 4. Main Features

### 4.1 Authentication, access control, and user management

**Authentication and access control** (Identity & Access Service)

-   User sign-in, sign-out, and token refresh.
-   Account activation, password management, and account deactivation.
-   Permission-based access control with **dynamic roles**: administrators
    create, rename, and delete roles and choose which permissions each
    role grants, at runtime, without code changes.
-   Assignment of roles to users, optionally limited to a scope (for
    example, a department).
-   Protected application features and API endpoints.

**User and profile management** (User Profile Service)

-   User directory: creating, searching, and viewing users.
-   Personal profile: consulting and updating one's own information.
-   Organizational attributes: department, programme, staff unit.

See [8. Security and Access Control](#8-security-and-access-control) for
the permission model.

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

External traffic goes through two layers:

1.  **Nginx (edge proxy):** the only publicly exposed component. It
    terminates TLS, applies rate limits and upload size limits, generates
    request IDs, and serves the Angular application.
2.  **API Gateway (Spring Cloud Gateway):** receives `/api/**` traffic
    from Nginx and routes each path to a service by its **service ID**,
    not by a fixed address. Instances are resolved through the
    **Discovery Server (Eureka)** and balanced client-side with **Spring
    Cloud LoadBalancer**, so a service can run several instances without
    any routing change.

All Spring services load their non-secret configuration from the
**Config Server** at startup and register themselves in the Discovery
Server. Services are not called directly by the browser, and internal-only
services (such as the AI Service) are never routed by Nginx or the
gateway.

### High-level architecture

``` text
                                 ┌─────────────────────────────┐
                                 │      Angular Web Client     │
                                 └─────────────────────────────┘
                                                │ HTTPS
                                                ▼
                                 ┌─────────────────────────────┐     ┌───────────────────────┐
                                 │      Nginx — edge proxy     │     │     Config Server     │
                                 │ TLS · rate limits · req. IDs│     │  Spring Cloud Config  │
                                 │ upload limits · Angular app │     │ ← every Spring service│
                                 └─────────────────────────────┘     └───────────────────────┘
                                                │ /api/**
                                                ▼
                                 ┌─────────────────────────────┐     ┌───────────────────────┐
                                 │         API Gateway         │     │    Discovery Server   │
                                 │     Spring Cloud Gateway    │◄────│    Eureka registry    │
                                 │  routing · CORS · JWT check │     │ ← every Spring service│
                                 └─────────────────────────────┘     └───────────────────────┘
                                                │ lb://service-id  (Spring Cloud LoadBalancer)
      ┌───────────┬───────────┬───────────┬─────┴─────┬───────────┬───────────┬───────────┐
      ▼           ▼           ▼           ▼           ▼           ▼           ▼           ▼
 ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐
 │ Identity│ │   User  │ │ Document│ │ Request │ │Assistant│ │ Notifi- │ │  Audit  │ │Reporting│
 │ & Access│ │ Profile │ │ Service │ │ Service │ │ Service │ │  cation │ │ Service │ │ Service │
 └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘
      │           │           │           │           │           │           │           │
      ▼           ▼           ▼           ▼           ▼           ▼           ▼           ▼
 ════════════════════════════════════════════════════════════════════════════════════════════════
           Apache Kafka — domain events · commands · retry / dead-letter topics
 ════════════════════════════════════════════════════════════════════════════════════════════════
                     ▼ ▲  DocumentProcessingRequested (in) · DocumentIndexed / ...Failed (out)
 ┌─ AI bounded context (Python) ────────────────────────────────────────────────────────────────┐
 │      ┌──────────────────────────┐                  ┌──────────────────────────┐              │
 │      │     Ingestion Worker     │                  │   AI Service (FastAPI)   │              │
 │      │ extract · chunk · embed  │                  │  embed · retrieve · LLM  │              │
 │      └──────────────────────────┘                  └──────────────────────────┘              │
 │                                                                                              │
 │  AI Service is internal only: called by Assistant Service over REST,                         │
 │  never routed by Nginx or the API Gateway. Both processes own pgvector.                      │
 └──────────────────────────────────────────────────────────────────────────────────────────────┘

        Data: PostgreSQL (one database per service; pgvector for the AI context)
        Files: local file storage on Docker volumes (one volume per owning service)
        Observability: Prometheus + Grafana
        Runtime: Docker / Docker Compose
```

This diagram represents the intended logical architecture. The exact
deployment topology and communication paths may evolve during
implementation.

### Platform services

| Service | Stack | Responsibility |
|---|---|---|
| **API Gateway** | Spring Cloud Gateway | Path-based routing to `lb://<service-id>`, CORS, correlation ID propagation, uniform error responses, early JWT signature and expiry check |
| **Discovery Server** | Spring Cloud Netflix Eureka | Registry of running service instances used by the gateway and by service-to-service calls |
| **Config Server** | Spring Cloud Config (native profile) | Centralized, versioned, non-secret configuration served from `infrastructure/config-repo/` |
| **Nginx** | Nginx | Public entry point: TLS, rate limiting, upload limits, request IDs, Angular static files |

The platform services contain no business logic.

### Business service catalogue

| Service | Stack | Responsibility | Owned data | Main events published |
|---|---|---|---|---|
| **Identity & Access Service** | Spring Boot | Credentials, sign-in, sign-out, token issuance and refresh, JWKS, account status, permission catalogue, dynamic roles, role–permission mapping, user–role assignments with scope | `identity_db`: Account, Credential, Permission, Role, RolePermission, RoleAssignment, refresh tokens | `AccountActivated`, `AccountDeactivated`, `RoleAssigned`, `RoleRevoked`, `RolePermissionsChanged` |
| **User Profile Service** | Spring Boot | User directory, personal profiles, organizational attributes (department, programme, staff unit), self-service profile updates | `user_db`: UserProfile, Department, Programme | `UserRegistered`, `UserProfileUpdated` |
| **Document Service** | Spring Boot | Document upload, metadata, categories, versions, access rules, logical deletion, indexing status | `document_db`: Document, DocumentVersion, Category, access rules; files in the `document-storage` volume | `DocumentUploaded`, `DocumentProcessingRequested`, `DocumentAccessChanged`, `DocumentDeleted` |
| **Request Service** | Spring Boot | Request submission, assignment, status state machine, comments, history, attachments | `request_db`: AdministrativeRequest, RequestComment, status history; files in the `request-attachments-storage` volume | `RequestSubmitted`, `RequestAssigned`, `RequestStatusChanged`, `RequestCommentAdded` |
| **Assistant Service** | Spring Boot | Conversations, chat messages, retention policy, orchestration of RAG calls | `assistant_db`: Conversation, ChatMessage, cited sources | `AssistantQuestionAnswered` |
| **AI Service** | Python, FastAPI | Query embedding, vector retrieval with access filtering, prompt construction, LLM generation, citation building, RAG evaluation | `ai_db` (with pgvector): DocumentChunk, embeddings, chunk access metadata | — (called synchronously) |
| **Ingestion Worker** | Python | Kafka consumer: fetch file, extract text, clean, chunk, embed, store vectors | Shares `ai_db` with the AI Service (same bounded context) | `DocumentIndexed`, `DocumentProcessingFailed` |
| **Notification Service** | Spring Boot | Consumes domain events and explicit `NotificationRequested` commands, creates in-app notifications, manages preferences, later sends email | `notification_db`: Notification, preferences, delivery records | — |
| **Audit Service** | Spring Boot | Consumes audit-relevant events from all services into an append-only log; exposes audit queries to authorized users | `audit_db`: AuditLog | — |
| **Reporting Service** | Spring Boot | Builds read models from domain events to serve dashboard statistics without querying other services' databases | `reporting_db`: aggregated indicators | — |

The Identity & Access Service and the User Profile Service are separate
on purpose: the first answers *"who is this and what may they do?"*, the
second answers *"who is this person in the university?"*. Both use the
same user ID, generated once by the User Profile Service when a user is
registered.

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
-   **Two-layer entry point:** Nginx is the single public entry point
    (TLS, rate limiting, upload limits, request IDs). Spring Cloud Gateway
    is the internal API gateway (routing by service ID and cross-cutting
    filters). Routes are configuration, not code.
-   **Service discovery and client-side load balancing:** services
    register in Eureka and are called by service ID (`lb://…`). Scaling
    a service means starting more instances; nothing else changes.
-   **Centralized configuration:** shared and per-service settings live
    in the Config Server's repository; secrets stay in environment
    variables and never enter that repository.
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
-   **File storage behind an interface:** uploaded files are stored
    through a `DocumentStorage` interface (initially on the local
    filesystem); each owning service stores file metadata and storage
    keys in its own database (see [File storage](#file-storage)).
-   **Defense in depth:** every service validates tokens and enforces
    authorization itself, independently of the gateway and of frontend
    visibility.
-   **Stateless services:** services keep no user session in memory, so
    any service can be scaled to several instances.

### Communication matrix

| Source | Destination | Mechanism | Purpose |
|---|---|---|---|
| Angular | Nginx | HTTPS / REST | All user operations |
| Nginx | API Gateway | HTTP / REST | Forward `/api/**` |
| API Gateway | Business services | HTTP / REST via `lb://<service-id>` | Routing by path prefix, load balanced |
| Spring services | Discovery Server | Eureka client | Register and discover instances |
| Spring services | Config Server | HTTP at startup | Load configuration |
| Assistant Service | AI Service | HTTP / REST (internal) | Retrieve and generate an answer |
| Business services | Kafka | Producer (via outbox) | Publish domain events |
| Business services | Kafka | Consumer | React to other services' events |
| Ingestion Worker | Kafka | Consumer / Producer | Index documents, report result |
| Document / Request Service | Own storage volume | `DocumentStorage` (local filesystem) | Store and serve files |
| Ingestion Worker | Document storage volume | Read-only mount | Fetch documents to index |
| Each service | Its own PostgreSQL database | JPA / Hibernate or SQL driver | Persistence |
| All services | Identity & Access Service | JWKS endpoint (cached) | Fetch the public key to verify JWTs |
| Prometheus | All services | Metrics scraping | Monitoring |

### Gateway routing

Nginx serves `/` (Angular) and forwards `/api/**` to the API Gateway. The
gateway routes are defined in `infrastructure/config-repo/api-gateway.yml`:

| Path prefix | Target service ID |
|---|---|
| `/api/v1/auth`, `/api/v1/access` | `identity-service` |
| `/api/v1/users` | `user-service` |
| `/api/v1/documents` | `document-service` |
| `/api/v1/requests` | `request-service` |
| `/api/v1/assistant` | `assistant-service` |
| `/api/v1/notifications` | `notification-service` |
| `/api/v1/audit` | `audit-service` |
| `/api/v1/reports` | `reporting-service` |

Automatic routing of every registered service is disabled: only the paths
listed here are reachable from outside.

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
  "correlationId": "request-id-from-nginx",
  "payload": { }
}
```

Planned topics:

| Topic | Producer | Consumers | Key |
|---|---|---|---|
| `identity.events` | Identity & Access Service | Request, Notification, Audit, Reporting | userId |
| `user.events` | User Profile Service | Identity & Access, Request, Notification, Audit, Reporting | userId |
| `document.events` | Document Service | Ingestion Worker, Notification, Audit, Reporting | documentId |
| `document.processing.results` | Ingestion Worker | Document Service, Notification, Reporting | documentId |
| `request.events` | Request Service | Notification, Audit, Reporting | requestId |
| `assistant.events` | Assistant Service | Reporting, Audit | conversationId |

Events are keyed by aggregate ID so that all events for one document or
one request are processed in order. Each consumer group uses retry
topics and a dead-letter topic for messages that keep failing. Event
schemas are versioned and stored in `contracts/events/`.

### File storage

Uploaded files (documents, request attachments) are written through a small
storage interface owned by each service that stores files:

``` java
public interface DocumentStorage {
    String store(InputStream content, String filename, String contentType); // returns a storage key
    InputStream load(String storageKey);
    void delete(String storageKey);
}
```

-   **Initial implementation:** `LocalFileStorage` (in the service's
    `service/impl` package) writes files to a directory whose path comes
    from configuration (`DOCUMENT_STORAGE_PATH`,
    `REQUEST_ATTACHMENTS_STORAGE_PATH`), never hard-coded.
-   **Volumes:** in Docker each owning service mounts its own named volume
    at that path: `document-storage` (Document Service, read-only for the
    Ingestion Worker) and `request-attachments-storage` (Request Service).
-   **Metadata stays in the database:** the owning service stores the
    file's metadata and storage key in its own database; the volume only
    holds file content.
-   **Later:** an S3-compatible backend (for example SeaweedFS or Garage)
    can be added as another `DocumentStorage` implementation without
    changing business code. It becomes necessary when services run on
    several hosts, because a local volume is tied to one Docker host.

MinIO was the original choice, but its official community container
images are no longer published, so no object-storage container is used
for now.

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
| Debugging across services | Request IDs generated at Nginx, propagated by the API Gateway in HTTP headers and event envelopes; structured logs |
| Operational overhead | Docker Compose for the full stack, one Dockerfile and CI job per service, a shared service template, centralized configuration |
| Finding services and their instances | Eureka discovery and client-side load balancing instead of hard-coded addresses |

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
| File storage | Local filesystem on Docker named volumes, behind a `DocumentStorage` interface | Uploaded document and attachment storage |
| Edge proxy | Nginx | Public entry point, TLS, rate limiting, Angular static files |
| API gateway | Spring Cloud Gateway | Internal routing by service ID, cross-cutting filters |
| Service discovery | Spring Cloud Netflix Eureka | Service registry |
| Load balancing | Spring Cloud LoadBalancer | Client-side balancing across instances |
| Configuration | Spring Cloud Config | Centralized configuration |
| Containers | Docker, Docker Compose | Reproducible development and deployment of all services |
| Monitoring | Prometheus, Grafana | Per-service metrics and dashboards |
| CI/CD | GitHub Actions | Automated validation and delivery per service |

Specific library versions, LLM providers, embedding models, and
deployment environments will be selected and documented during
implementation.

## 7. Main Workflows

### 7.1 Document upload and indexing

1.  An authorized staff member uploads a document through Angular.
2.  Nginx and the API Gateway route the request to the **Document Service**, which
    validates the token, the user's permissions, and the file.
3.  The Document Service stores the file through `DocumentStorage`.
4.  It persists the document metadata and access rules in `document_db`
    with status `PENDING`, and writes a `DocumentProcessingRequested`
    event to its outbox in the same transaction.
5.  The outbox publisher sends the event to Kafka. The event carries the
    storage key and the document's access metadata.
6.  The **Ingestion Worker** consumes the event and retrieves the file
    from the document storage volume (mounted read-only).
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

### 7.4 User registration and onboarding

1.  An administrator creates a user in the **User Profile Service**
    (identity data, department, programme) and selects initial roles.
2.  The User Profile Service persists the profile, generates the user ID,
    and publishes `UserRegistered` with the requested initial roles.
3.  The **Identity & Access Service** consumes the event, creates the
    account in `PENDING_ACTIVATION` state, assigns the roles, and
    publishes a `NotificationRequested` command with a one-time activation
    link.
4.  The user sets a password through the activation link; the account
    becomes `ACTIVE` and `AccountActivated` is published.
5.  Deactivation happens in the Identity & Access Service: the account is
    blocked, refresh tokens are revoked, and `AccountDeactivated` is
    published so other services can update their local data.

### 7.5 Changing what a role can do

1.  An administrator with the `role:manage` permission edits a role in
    the administration screen: adds or removes permissions, or creates a
    new role.
2.  The Identity & Access Service validates that every permission exists
    in the catalogue, saves the change, writes an audit record, and
    publishes `RolePermissionsChanged`.
3.  Users holding the role get the new permissions in their next access
    token (at the latest when the short-lived token expires and is
    refreshed). No service is redeployed.

------------------------------------------------------------------------

## 8. Security and Access Control

Security is a core requirement because the platform handles user
accounts, institutional documents, and administrative requests. In a
microservices architecture, every service is a potential entry point, so
each one enforces security on its own.

Planned security controls include:

-   Authentication and secure password storage (BCrypt or Argon2) in the
    Identity & Access Service. Profile data never contains credentials.
-   Short-lived JWT access tokens signed with an asymmetric key by the
    Identity & Access Service; every service verifies signatures with the
    public key published on a JWKS endpoint, so no service needs to call
    the Identity & Access Service on each request.
-   Refresh tokens stored and revocable by the Identity & Access Service;
    account deactivation is published as `AccountDeactivated`.
-   Permission-based access control: tokens carry the user's effective
    permissions, and services check **permissions, never role names**.
-   Backend authorization in every service for every protected
    operation, including resource-ownership checks (for example, a
    student cannot read another student's request by changing an ID in
    the URL).
-   Validation of incoming data in each service.
-   Secure handling of uploaded files.
-   Restricted access to documents and request records.
-   Internal-only services (AI Service, Ingestion Worker) and platform
    services (Config Server, Discovery Server) reachable only on the
    internal Docker network in deployed environments.
-   Least-privilege access: one database user per service, one storage
    volume per owning service (mounted read-only where only reading is
    needed), and Kafka access control in deployed environments.
-   Protection of secrets through environment configuration.
-   Audit records for important operations, centralized in the Audit
    Service.
-   Secure communication over HTTPS in deployed environments.
-   Rate limiting at Nginx and other API protections where appropriate.
-   Early rejection of missing, expired, or badly signed tokens at the API
    Gateway; full permission checks still happen in each service.
-   RAG-specific protections: content of retrieved documents is never
    treated as instructions, and authorization filters are applied
    before any passage reaches the LLM.

### 8.1 Permission-based access model

The access model separates three things:

| Concept | What it is | Who defines it | Can change at runtime? |
|---|---|---|---|
| **Permission** | One action on one type of resource, optionally limited by scope (for example `request:process:department`) | Developers, because code checks it | No — added with the code that uses it |
| **Role** | A named bundle of permissions (for example "Agent administratif") | Administrators, through the administration screen | **Yes** — create, rename, delete, change permissions |
| **Role assignment** | A user holding a role, optionally within a scope value (for example department `scolarite`) | Administrators | **Yes** |

**Rule:** application code never checks a role name. It checks permissions
only, for example `hasAuthority("request:process")`. Roles are therefore
pure data: adding a new role such as "Responsable de stages" means
creating it and ticking its permissions — no code change, no redeployment.

#### Permission naming

Permissions follow the format `resource:action[:scope]`.

| Scope | Meaning |
|---|---|
| `own` | Only resources the user created or owns |
| `assigned` | Only resources explicitly assigned to the user |
| `department` | Resources belonging to the department(s) of the role assignment |
| `all` | All resources of that type |

When a permission has no scope, it applies globally.

#### Permission catalogue (initial)

Each service declares the permissions it checks. The Identity & Access
Service stores the catalogue (seeded through database migrations) so
administrators can pick from it.

| Service | Permissions |
|---|---|
| Identity & Access | `role:read`, `role:manage`, `role:assign`, `account:manage` |
| User Profile | `user:read:own`, `user:update:own`, `user:read:all`, `user:create`, `user:update:all` |
| Document | `document:read:public`, `document:read:restricted`, `document:upload`, `document:update`, `document:delete`, `document:manage-access` |
| Assistant | `assistant:use` |
| Request | `request:create`, `request:read:own`, `request:read:assigned`, `request:read:department`, `request:read:all`, `request:process`, `request:assign`, `request:comment` |
| Audit | `audit:read:department`, `audit:read:all` |
| Reporting | `report:read:department`, `report:read:all` |
| Platform | `platform:configure` |

#### Default roles (seed data, editable)

The platform starts with the roles from the requirements specification.
They are created by a data migration and can be edited, renamed, or
complemented by administrators afterwards.

| Permission group | Étudiant | Agent administratif | Responsable pédagogique | Administrateur |
|---|---|---|---|---|
| Read public documents, use assistant | ✓ | ✓ | ✓ | ✓ |
| Read and update own profile | ✓ | ✓ | ✓ | ✓ |
| Submit and follow own requests | ✓ | | | |
| Process assigned requests | | ✓ | | |
| Read and process department requests | | | ✓ | ✓ (`all`) |
| Manage documents | | optional | ✓ | ✓ |
| Read audit logs | | | optional (`department`) | ✓ (`all`) |
| Read statistics | | | `department` | `all` |
| Manage users, roles, and permissions | | | | ✓ |

"Optional" entries match the requirement *"selon droits"*: they are not
granted by default but can be enabled per role, or a dedicated role can be
created for them.

#### What the token carries

``` json
{
  "sub": "8f2c…-user-id",
  "roles": ["Agent administratif"],
  "perms": [
    "document:read:public",
    "assistant:use",
    "request:read:assigned",
    "request:process"
  ],
  "scopes": { "department": ["scolarite"] },
  "exp": 1763470000
}
```

-   `perms` are the **effective permissions**: the union of the
    permissions of all the user's roles, computed by the Identity &
    Access Service when the token is issued.
-   `scopes` carry the scope values from the role assignments, used for
    `department`-scoped checks.
-   `roles` are included for display only (for example in the Angular
    header); no authorization decision uses them.
-   Access tokens are short-lived (for example 15 minutes), so permission
    changes take effect at the next refresh.

#### Enforcement in each service

1.  **Permission check:** does the token contain the required permission?
2.  **Scope check:** for scoped permissions, does the resource match the
    user (`own`), the assignment (`assigned`), or the department in
    `scopes` (`department`)?
3.  **Business rule check:** for example, a request in status `CLOSED`
    cannot be modified, whatever the permissions.

Document access rules and the RAG retrieval filter use the same model:
documents carry a visibility level and, optionally, a department, and the
AI Service filters chunks using the `perms` and `scopes` of the forwarded
token. They never reference role names, so renaming or adding a role
cannot break document access.

#### Safeguards

-   Permissions cannot be created or deleted from the UI; only roles can.
-   At least one active account must always keep `role:manage` and
    `account:manage`, so administrators cannot lock themselves out.
-   A role still assigned to users cannot be deleted until it is
    unassigned or replaced.
-   Every change to roles, permissions, and assignments is audited.

------------------------------------------------------------------------

## 9. Planned Repository Structure

The following is a proposed monorepo structure: one directory per
independently buildable service. Directories can be adjusted as
implementation decisions become concrete.

``` text
university-platform/
│
├── pom.xml                        # Maven aggregator only (builds all Java modules)
│
├── frontend/
│   └── angular-app/
│
├── platform/
│   ├── config-server/             # Spring Cloud Config
│   ├── discovery-server/          # Eureka
│   └── api-gateway/               # Spring Cloud Gateway
│
├── services/
│   ├── identity-service/          # Spring Boot — Identity & Access
│   │   ├── src/
│   │   ├── Dockerfile
│   │   ├── pom.xml
│   │   └── README.md
│   ├── user-service/              # Spring Boot — User Profile
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
│   ├── config-repo/               # Config Server files: application.yml + one file per service
│   ├── docker/
│   ├── nginx/                     # Edge proxy configuration
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

Each Java module is an independent Spring Boot application with its own
Maven Wrapper and Dockerfile; the root `pom.xml` only aggregates them.

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

Start only the infrastructure and platform services (useful when running
one business service from your IDE, which then registers in Eureka like
the others):

``` bash
docker compose up -d postgres kafka config-server discovery-server api-gateway nginx
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
| Nginx edge proxy | 80 / 443 |
| API Gateway | 8080 |
| Discovery Server (Eureka dashboard) | 8761 |
| Config Server | 8888 |
| Identity & Access Service | 8081 |
| Document Service | 8082 |
| Request Service | 8083 |
| Assistant Service | 8084 |
| Notification Service | 8085 |
| Audit Service | 8086 |
| Reporting Service | 8087 |
| User Profile Service | 8088 |
| AI Service | 8000 (internal) |
| PostgreSQL | 5432 |
| Kafka | 9092 |
| Prometheus | 9090 |
| Grafana | 3000 |

The exact commands, ports, and environment variables must match the
final implementation.

## 11. Configuration

Configuration is split in two:

-   **Non-secret settings** (routes, timeouts, logging, actuator
    exposure, feature flags) live in `infrastructure/config-repo/`,
    served by the Config Server: one shared `application.yml` plus one
    `<service-id>.yml` per service.
-   **Secrets and environment-specific values** (passwords, keys, URLs)
    come from environment variables. Each service reads only the
    variables it needs.

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

USER_DB_NAME=user_db
USER_DB_USER=user_user
USER_DB_PASSWORD=change_me

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

# Spring Boot / Spring Cloud
SPRING_PROFILES_ACTIVE=dev
CONFIG_SERVER_URL=http://config-server:8888
EUREKA_URL=http://discovery-server:8761/eureka

# Security (Identity & Access Service signs tokens; others verify via JWKS)
JWT_ISSUER=identity-service
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

# File storage (paths inside the owning service's container, on Docker named volumes)
DOCUMENT_STORAGE_PATH=/data/documents
REQUEST_ATTACHMENTS_STORAGE_PATH=/data/request-attachments
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
| 2. Platform foundation | Monorepo, Config Server, Discovery Server, API Gateway, Nginx edge, PostgreSQL with per-service databases, Kafka, file storage volumes, Prometheus, Grafana, CI skeleton, a throwaway sample service proving discovery and load balancing | Whole platform running in Docker; a new service only needs code and configuration |
| 3. Identity & Access and User Profile services | Authentication, JWT issuance, JWKS, permission catalogue, dynamic roles and assignments, user registration and profiles, token verification in the service template | Secured foundation reused by every service |
| 4. Document Service | Upload, metadata, categories, `DocumentStorage` with `LocalFileStorage`, outbox publishing | Usable document repository |
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
    PostgreSQL and Kafka containers and a temporary storage directory.
-   **API tests:** verify request validation, response contracts, and
    authorization for every service.
-   **Authorization tests:** for each protected endpoint, verify access
    with and without the required permission, with each scope, and after
    a role's permissions change.
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
    Docker Compose dependency ordering: Config Server → Discovery Server →
    API Gateway and services → Nginx.
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
    database and for the file storage volumes.

A production deployment will additionally require appropriate TLS
configuration, secret management, access restrictions, database backups,
and operational monitoring.

## 15. Documentation

Project documentation will include:

-   Functional requirements and use cases.
-   Architecture diagrams, service boundaries, and architectural
    decision records (for example, the edge proxy / API gateway split
    and the use of service discovery).
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
feature/identity-dynamic-roles
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
