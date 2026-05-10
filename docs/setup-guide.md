# Local Setup Guide

## Prerequisites

- Docker and Docker Compose
- Node.js for frontend development
- A compatible Java toolchain for backend development if running services outside containers

## Start the Local Stack

From `backend/`:

```bash
docker-compose up -d
```

This starts PostgreSQL, MongoDB, RabbitMQ, Eureka, the API gateway, the Spring services, and the frontend container.

## Common URLs and Ports

- Frontend: `http://localhost`
- API Gateway: `http://localhost:9000`
- Eureka: `http://localhost:8860`
- RabbitMQ management: `http://localhost:15672`
- Mongo Express: `http://localhost:8091`

Service ports exposed by the compose stack:

- IAM: `8085`
- Account: `8081`
- Transaction: `8082`
- Financial: `8083`
- Card: `8084`
- Notification: `8086`
- Audit: `8087`

## Environment Variables

The backend compose stack already wires the core service URLs and database connections. For standalone runs, supply the required database, RabbitMQ, Eureka, and SMTP variables through each service profile or `.env` file.

The notification service requires SMTP credentials and contact lookup URLs. The frontend also expects its API base URLs to point at the gateway or local service URLs used by the generated client layer.

## Frontend Development

From `frontend/`:

```bash
npm install
npm run dev
```

For production verification:

```bash
npm run build
```

## Backend Development

Each Spring service can be built and run independently from its service directory, or together through the root compose file in `backend/`.

## Shutdown

From `backend/`:

```bash
docker-compose down
```
