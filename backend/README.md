## Infrastructure Setup Guide

### 1. Start the Infrastructure

Open a terminal in the `bank-backend-system` folder and run:
```bash
docker-compose up -d
```

This downloads and runs all 4 databases/brokers in the background. It might take a few minutes the very first time.

---

### 2. Verify RabbitMQ

Open a browser and go to:

**URL:** `http://localhost:15672`  
**Username:** `guest`  
**Password:** `guest`

You will see a dashboard — when your microservices start sending events, you will see the traffic spike here.

---

### 3. Configure Spring Boot

Inside each Spring Boot project, open: or `.yml`, and point your database URLs accordingly:

| Database | Host | Port |
|----------|------|------|
| PostgreSQL | `localhost` | `5432` |
| MySQL | `localhost` | `3306` |

Use `root` / `password` as the credentials.

---

### 4. Stop the Infrastructure

When done working for the day, run:
```bash
docker-compose down
```