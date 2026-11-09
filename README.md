# Intelligent University Knowledge & Service Platform

> A centralized university platform that combines institutional
> knowledge management, AI-powered question answering, and
> administrative service workflows.

**Project status:** In planning / initial development\
**Project type:** Academic software engineering project\
**Proposed architecture:** Angular frontend, Spring Boot backend, and an
independent Python AI service

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
-   Design a maintainable backend with clear domain boundaries.
-   Implement secure authentication and role-based authorization.
-   Introduce asynchronous processing with Apache Kafka where
    appropriate.
-   Build a separate AI service for document ingestion and RAG
    operations.
-   Containerize the application and its dependencies.
-   Add automated testing, monitoring, and CI/CD practices.

## 4. Main Features

### 4.1 Authentication and user management

-   User sign-in and sign-out.
-   User profiles.
-   Role-based access control.
-   Protected application features and API endpoints.
-   Administrative user management.

### 4.2 Institutional document management

-   Upload and store documents.
-   Organize documents using categories and metadata.
-   Consult and download authorized documents.
-   Track document status and indexing status.
-   Trigger document extraction and indexing after upload.

Supported formats will be defined during implementation. PDF support is
a primary target.

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

### 4.5 Notifications

-   Notify users when relevant request events occur.
-   Notify staff about newly assigned requests.
-   Support asynchronous notification processing.

The initial notification channels will be selected during
implementation. Email can be added as a first channel.

### 4.6 Administrative dashboard

-   Overview of users and roles.
-   Document and indexing overview.
-   Administrative request statistics.
-   Request queues and processing status.
-   Relevant activity and operational indicators.

------------------------------------------------------------------------

## 5. Architecture

The initial design uses a **modular monolith for the core business
application**, alongside a separately deployable AI service.

This keeps the core business domain cohesive while allowing AI-specific
dependencies and processing to evolve independently. The project does
not split every business module into a separate microservice.

### High-level architecture

``` text
                         ┌──────────────────────┐
                         │     Angular Web      │
                         │       Client         │
                         └──────────┬───────────┘
                                    │ HTTPS / REST
                                    ▼
                         ┌──────────────────────┐
                         │     Nginx Proxy      │
                         │  Routing / TLS       │
                         └──────────┬───────────┘
                                    │
                    ┌───────────────┴────────────────┐
                    ▼                                ▼
        ┌────────────────────────┐       ┌────────────────────────┐
        │ Spring Boot Core API   │       │ Python AI Service      │
        │                        │       │ FastAPI                │
        │ - Authentication       │       │ - Document ingestion   │
        │ - User management      │       │ - Text extraction      │
        │ - Document metadata    │       │ - Chunking             │
        │ - Request management   │       │ - Embeddings           │
        │ - Authorization        │       │ - Retrieval / RAG       │
        └───────────┬────────────┘       └───────────┬────────────┘
                    │                                │
                    └───────────────┬────────────────┘
                                    │
                 ┌──────────────────┼───────────────────┐
                 ▼                  ▼                   ▼
        ┌────────────────┐ ┌────────────────┐  ┌────────────────┐
        │  PostgreSQL    │ │     Kafka      │  │     MinIO      │
        │  + pgvector    │ │  Event broker  │  │ Object storage │
        └────────────────┘ └────────────────┘  └────────────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │ Notification Worker  │
                         │ / Async Consumers    │
                         └──────────────────────┘

        Observability: Prometheus + Grafana
        Runtime: Docker / Docker Compose
```

This diagram represents the intended logical architecture. The exact
deployment topology and communication paths may evolve during
implementation.

### Architectural principles

-   **Modular monolith:** core business capabilities remain in one
    Spring Boot application, organized into well-defined modules.
-   **Service separation:** the AI service owns AI-specific processing
    and exposes a clear API.
-   **REST for synchronous operations:** use request/response
    communication when the caller needs an immediate result.
-   **Kafka for asynchronous workflows:** use events for tasks such as
    document indexing and notifications.
-   **Single primary relational database:** PostgreSQL stores core
    application data.
-   **Vector search:** pgvector provides vector similarity search within
    PostgreSQL for the initial RAG implementation.
-   **Object storage:** MinIO stores uploaded files; the database stores
    their metadata and references.
-   **Defense in depth:** authorization is enforced by backend services,
    not only by frontend visibility.

### Core backend modules

The Spring Boot application may be organized into modules such as:

  -----------------------------------------------------------------------
  Module                              Responsibility
  ----------------------------------- -----------------------------------
  Identity and Access                 Users, roles, authentication,
                                      authorization

  Document Management                 Document metadata, categories,
                                      ownership, lifecycle

  Administrative Requests             Request submission, assignment,
                                      status transitions

  Notifications                       Notification preferences and
                                      delivery records

  Audit                               Important security and business
                                      activity records
  -----------------------------------------------------------------------

The module boundaries should reflect actual domain responsibilities.
Avoid creating abstractions or modules without a concrete need.

## 6. Technology Stack

  -----------------------------------------------------------------------
  Area                    Technology              Purpose
  ----------------------- ----------------------- -----------------------
  Frontend                Angular, TypeScript     User interface

  Core backend            Java, Spring Boot       Business logic and REST
                                                  APIs

  AI backend              Python, FastAPI         Document processing and
                                                  RAG

  Relational database     PostgreSQL              Application data

  Vector search           pgvector                Embedding storage and
                                                  similarity search

  Messaging               Apache Kafka            Asynchronous event
                                                  processing

  Object storage          MinIO                   Uploaded document
                                                  storage

  Reverse proxy           Nginx                   Routing and deployment
                                                  entry point

  Containers              Docker, Docker Compose  Reproducible
                                                  development and
                                                  deployment

  Monitoring              Prometheus, Grafana     Metrics and dashboards

  CI/CD                   GitHub Actions          Automated validation
                                                  and delivery
  -----------------------------------------------------------------------

Specific library versions, LLM providers, embedding models, and
deployment environments will be selected and documented during
implementation.

## 7. Main Workflows

### 7.1 Document upload and indexing

1.  An authorized staff member uploads a document through Angular.
2.  The Spring Boot API validates the request and the user's
    permissions.
3.  The file is stored in MinIO.
4.  Document metadata and access-control information are persisted in
    PostgreSQL.
5.  The backend publishes a document-ingestion event to Kafka.
6.  The AI service consumes the event and retrieves the file.
7.  The AI service extracts text, cleans it, and splits it into chunks.
8.  It generates embeddings and stores the chunks and vectors in
    PostgreSQL using pgvector.
9.  The document's indexing status is updated.

Indexing is asynchronous so that a potentially expensive
document-processing operation does not block the upload request.

### 7.2 Question answering with RAG

1.  The user submits a question.
2.  The request is authenticated and the user's access scope is
    established.
3.  The AI service generates an embedding for the question.
4.  Relevant chunks are retrieved from pgvector.
5.  Retrieval is restricted to documents the user is authorized to
    access.
6.  The retrieved context is supplied to the LLM.
7.  The generated answer and source references are returned to the user.

**Important:** access filtering must happen during retrieval and must be
enforced by trusted backend logic. Hiding documents in the Angular
interface is not sufficient security.

### 7.3 Administrative request processing

1.  A student submits a request.
2.  The backend validates the request and persists it.
3.  The request is made available to the appropriate staff workflow.
4.  A responsible staff member reviews and processes it.
5.  The backend records status changes and relevant activity.
6.  Notifications are generated for relevant events.
7.  The student can consult the updated status.

------------------------------------------------------------------------

## 8. Security and Access Control

Security is a core requirement because the platform handles user
accounts, institutional documents, and administrative requests.

Planned security controls include:

-   Authentication and secure password storage.
-   Role-based access control (RBAC).
-   Backend authorization for every protected operation.
-   Validation of incoming data.
-   Secure handling of uploaded files.
-   Restricted access to documents and request records.
-   Protection of secrets through environment configuration.
-   Audit records for important operations.
-   Secure communication over HTTPS in deployed environments.
-   Rate limiting and other API protections where appropriate.

Roles and permissions will be finalized with the functional
requirements. Possible roles include:

  -----------------------------------------------------------------------
  Role                                Typical responsibilities
  ----------------------------------- -----------------------------------
  Student                             Consult accessible documents, use
                                      the assistant, submit and track
                                      personal requests

  Administrative Staff                Process requests assigned to their
                                      scope

  Document Manager                    Upload and manage institutional
                                      documents

  Administrator                       Manage users, roles, configuration,
                                      and platform operations
  -----------------------------------------------------------------------

The final permission model should be based on actions and resource
scope, not simply on frontend pages.

------------------------------------------------------------------------

## 9. Planned Repository Structure

The following is a proposed monorepo structure. Directories can be
adjusted as implementation decisions become concrete.

``` text
university-platform/
│
├── frontend/
│   └── angular-app/
│
├── backend/
│   └── core-api/
│       ├── src/
│       ├── pom.xml
│       └── README.md
│
├── ai-service/
│   ├── app/
│   │   ├── api/
│   │   ├── ingestion/
│   │   ├── embeddings/
│   │   ├── retrieval/
│   │   └── rag/
│   ├── tests/
│   └── requirements.txt
│
├── infrastructure/
│   ├── docker/
│   ├── nginx/
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
├── .env.example
├── .gitignore
├── compose.yaml
└── README.md
```

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

### Start the infrastructure

Once `compose.yaml` is implemented:

``` bash
docker compose up -d
```

Inspect the running services:

``` bash
docker compose ps
```

Follow service logs:

``` bash
docker compose logs -f
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

During development, each application can also be started independently.

**Angular frontend**

``` bash
cd frontend/angular-app
npm install
npm start
```

**Spring Boot backend**

``` bash
cd backend/core-api
./mvnw spring-boot:run
```

**Python AI service**

``` bash
cd ai-service
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload
```

The exact commands, ports, and environment variables must match the
final implementation.

## 11. Configuration

Use environment variables for configuration that differs between
environments or contains secrets.

An illustrative `.env.example`:

``` dotenv
# Application
APP_ENV=development

# PostgreSQL
POSTGRES_DB=university_platform
POSTGRES_USER=app_user
POSTGRES_PASSWORD=change_me
POSTGRES_HOST=postgres
POSTGRES_PORT=5432

# Spring Boot
SPRING_PROFILES_ACTIVE=dev

# AI service
AI_SERVICE_HOST=ai-service
AI_SERVICE_PORT=8000
LLM_PROVIDER=replace_with_provider
LLM_API_KEY=replace_with_local_secret
EMBEDDING_MODEL=replace_with_embedding_model

# Kafka
KAFKA_BOOTSTRAP_SERVERS=kafka:9092

# Object storage
MINIO_ENDPOINT=http://minio:9000
MINIO_ACCESS_KEY=change_me
MINIO_SECRET_KEY=change_me
MINIO_BUCKET=university-documents
```

These values are examples, not a complete configuration. The actual
environment variables must be defined by the implemented services. Use
strong local secrets and a secure secret-management approach outside
development.

## 12. Development Roadmap

The project will be developed incrementally to keep the core application
usable while introducing more advanced capabilities.

  -----------------------------------------------------------------------
  Phase                   Scope                   Expected result
  ----------------------- ----------------------- -----------------------
  1\. Requirements and    Use cases, roles,       Validated functional
  design                  domain model, API       and technical design
                          boundaries,             
                          architecture            

  2\. Project foundation  Repository, Angular     Running development
                          setup, Spring Boot      skeleton
                          setup, PostgreSQL,      
                          Docker                  

  3\. Identity and access Authentication, users,  Secured application
                          roles, authorization    foundation

  4\. Document management Upload, metadata,       Usable document
                          categories, MinIO       repository
                          integration             

  5\. RAG foundation      Extraction, chunking,   Document-grounded
                          embeddings, pgvector,   question answering
                          retrieval               

  6\. AI assistant        LLM integration,        Conversational
  integration             prompts, source         assistant
                          references, failure     
                          handling                

  7\. Kafka workflows     Indexing events,        Reliable asynchronous
                          consumers, notification processing
                          events                  

  8\. Administrative      Submission, assignment, End-to-end request
  requests                status lifecycle,       workflow
                          history                 

  9\. Angular application User-facing screens,    Complete web experience
                          dashboards, API         
                          integration             

  10\. Quality and        Automated tests,        Deployable and
  operations              metrics, dashboards,    maintainable
                          CI/CD, hardening        application
  -----------------------------------------------------------------------

The sequence may be adjusted when dependencies or supervisor feedback
require it. Security, testing, and documentation should be developed
continuously rather than postponed until the final phase.

## 13. Testing and Quality

Testing will cover the system at multiple levels.

-   **Unit tests:** validate business rules and individual components.
-   **Integration tests:** validate persistence, API behavior, and
    service interactions.
-   **API tests:** verify request validation, response contracts, and
    authorization.
-   **Frontend tests:** validate important Angular components and user
    workflows.
-   **AI evaluation:** assess retrieval relevance, answer faithfulness,
    citation correctness, and behavior when context is insufficient.
-   **End-to-end tests:** validate complete user journeys.

Quality practices:

-   Consistent formatting and naming conventions.
-   Clear separation between controllers, services, repositories, and
    DTOs.
-   Centralized error handling.
-   Input validation and meaningful API responses.
-   Database migrations under version control.
-   Code reviews and automated CI checks.

For RAG, a successful HTTP response alone is not enough: retrieval
quality and answer grounding must also be evaluated.

## 14. Deployment and Operations

The target environment will be containerized with Docker. Docker Compose
will initially support local development and integrated testing.

Operational capabilities planned for the project:

-   Health checks for application services.
-   Centralized and useful application logs.
-   Prometheus metrics.
-   Grafana dashboards.
-   Resource-aware container configuration.
-   Automated build and test pipelines through GitHub Actions.
-   Environment-specific configuration.
-   Persistent storage and backup considerations.

A production deployment will additionally require appropriate TLS
configuration, secret management, access restrictions, database backups,
and operational monitoring.

## 15. Documentation

Project documentation will include:

-   Functional requirements and use cases.
-   Architecture diagrams and architectural decisions.
-   Database model and migrations.
-   REST API documentation.
-   Authentication and authorization model.
-   RAG ingestion and retrieval design.
-   Kafka event contracts and processing flows.
-   Local development and deployment instructions.
-   Test strategy and evaluation results.

## 16. Contributing

During development:

1.  Create a branch for each feature or fix.
2.  Keep changes focused and reviewable.
3.  Add or update tests for changed behavior.
4.  Update documentation when APIs, configuration, or architecture
    change.
5.  Avoid committing secrets, local environment files, generated
    artifacts, or uploaded private documents.
6.  Open a pull request and review the changes before merging.

Suggested branch naming:

``` text
feature/document-management
feature/rag-retrieval
feature/request-workflow
fix/authentication-validation
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

The project is also an opportunity to apply practical software
engineering principles across backend development, frontend development,
AI integration, asynchronous communication, security, containerization,
testing, and DevOps.
