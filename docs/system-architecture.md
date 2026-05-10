# System Architecture

## Overview

Aether Bank is organized as a browser frontend, an API gateway, a discovery server, and multiple domain services. The backend is containerized with shared infrastructure for databases and messaging.

## Frontend

- React + TypeScript + Vite application
- Route-based modules for auth, accounts, transactions, cards, payments, loans, mortgages, savings, investments, FX, beneficiaries, notifications, profile, workflow, and admin
- Lazy-loaded pages and guarded routes
- Generated client code for backend APIs and hooks

## Backend Services

- `api-gateway`: entry point for client traffic
- `eureka-server`: service discovery
- `iam-service`: identity, authentication, and user records
- `account-service`: customer and bank account management
- `transaction-service`: transfers and transaction processing
- `card-service`: card lifecycle and card activity
- `financial-service`: financial product and investment-related operations
- `notification-service`: notification dispatch and email integration
- `audit-service`: audit event capture and review

## Infrastructure Services

- PostgreSQL for relational persistence
- MongoDB for document-oriented persistence
- RabbitMQ for asynchronous messaging

## Interaction Model

1. The browser talks to the frontend.
2. The frontend calls the gateway and generated API clients.
3. The gateway resolves services through Eureka.
4. Domain services read and write their own storage.
5. Events are published to RabbitMQ where downstream services subscribe.
6. Notifications and audit records are produced from service events.

## Security and Access Control

- Authentication is required for application pages outside the public landing and auth flows.
- Route guards enforce role-based access in the frontend.
- Workflow pages are available to assigned employee and management roles.
- Admin pages are limited to admin-level roles.

## Data Storage Notes

- PostgreSQL databases are created for the main transactional services.
- MongoDB databases are used for services that benefit from flexible document storage.
- RabbitMQ persists broker state through a dedicated volume.

## Operational Notes

- The Docker compose file is the canonical local topology.
- Health checks are defined for the database and broker infrastructure and for key Spring services.
- Notification delivery relies on SMTP configuration supplied through environment variables.
