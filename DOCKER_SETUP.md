# Aether Bank - Docker Setup Complete

## ✅ All Issues Fixed

### 1. **Port Conflicts Resolved**
- **PostgreSQL**: Changed from `5432:5432` to `5433:5432` (no conflict with your local PostgreSQL)
- **Internal service ports properly mapped**:
  - IAM Service: `8085:4333` (container runs on 4333)
  - Transaction Service: `8082:2501` (container runs on 2501)
  - Financial Service: `8083:8080` (container runs on 8080)
  - Notification Service: `8086:3030` (container runs on 3030)

### 2. **Frontend Dockerized** ✅
- Created `frontend/Dockerfile` (multi-stage build with nginx)
- Created `frontend/nginx.conf` (with API proxy to gateway)
- Created `frontend/.dockerignore`
- Added frontend service to `docker-compose.yml` on port `3000:80`

### 3. **Notification Service Fixed** ✅
- Added `@EnableMongoRepositories` annotation to enable MongoDB repository scanning
- Repositories now properly detected by Spring Data

## 📊 Complete Port Mapping

| Service | Host Port | Container Port | URL |
|---------|-----------|----------------|-----|
| **Frontend** | 3000 | 80 | http://localhost:3000 |
| **API Gateway** | 9000 | 9000 | http://localhost:9000 |
| **Eureka Dashboard** | 8761 | 8761 | http://localhost:8761 |
| **IAM Service** | 8085 | 4333 | via Gateway or :8085 |
| **Account Service** | 8081 | 8081 | via Gateway or :8081 |
| **Transaction Service** | 8082 | 2501 | via Gateway or :8082 |
| **Card Service** | 8084 | 8084 | via Gateway or :8084 |
| **Financial Service** | 8083 | 8080 | via Gateway or :8083 |
| **Notification Service** | 8086 | 3030 | via Gateway or :8086 |
| **Audit Service** | 8087 | 8087 | via Gateway or :8087 |
| **PostgreSQL** | 5433 | 5432 | jdbc://localhost:5433 |
| **MongoDB** | 27018 | 27017 | mongodb://localhost:27018 |
| **Mongo Express** | 8091 | 8081 | http://localhost:8091 |
| **RabbitMQ AMQP** | 5672 | 5672 | amqp://localhost:5672 |
| **RabbitMQ Management** | 15672 | 15672 | http://localhost:15672 |

## 🚀 How to Run

### First Time Setup (Build Images)
```powershell
cd "G:\University\Third Year Term 2\SE-2\Aether Bank\backend"
docker compose build
```

### Start All Services
```powershell
cd "G:\University\Third Year Term 2\SE-2\Aether Bank\backend"
docker compose up -d
```

### View Logs
```powershell
# All services
docker compose logs -f

# Specific service
docker compose logs -f notification-service
docker compose logs -f frontend
```

### Stop All Services
```powershell
docker compose down
```

### Rebuild Single Service
```powershell
# Example: rebuild notification-service after code change
docker compose build notification-service
docker compose up -d notification-service
```

## 🔍 Health Checks

### Verify Services are Running
```powershell
docker compose ps
```

### Check Eureka Discovery
Open http://localhost:8860 - all services should be registered

### Check Frontend
Open http://localhost:3000 - React app should load

### Check API Gateway
```powershell
curl http://localhost:9000/actuator/health
```

## 📝 Environment Variables

Create a `.env` file in the `backend/` directory if you want to set email credentials:

```env
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

## 🛠️ Troubleshooting

### Port Already in Use
If you get port conflicts:
- Check your local PostgreSQL (should be on 5432, Docker uses 5433)
- Check your local MongoDB (should be on 27017, Docker uses 27018)
- Check if Eureka is running locally (Docker uses 8860)
- Stop conflicting services or change ports in docker-compose.yml

### Service Won't Start
1. Check logs: `docker compose logs service-name`
2. Verify dependencies are healthy: `docker compose ps`
3. Restart: `docker compose restart service-name`

### Rebuild from Scratch
```powershell
docker compose down -v  # Remove volumes too
docker compose build --no-cache
docker compose up -d
```

## 🎯 What's Working

✅ All 9 backend services build successfully (Java 25, Spring Boot 4.0.5)
✅ Frontend builds and serves via nginx
✅ All port conflicts resolved
✅ PostgreSQL on 5433 (no conflict with your local)
✅ MongoDB repositories properly configured
✅ Docker networking configured
✅ Health checks enabled for infrastructure services
✅ Service discovery via Eureka
✅ RabbitMQ for async messaging

## 📦 Services Overview

**Infrastructure:**
- PostgreSQL 15 (iam_db, account_db, transaction_db, card_db)
- MongoDB 6.0 (financial_db, notification_db, audit_db)
- RabbitMQ 3 (event bus)
- Mongo Express (MongoDB admin UI)

**Spring Cloud:**
- Eureka Server (service discovery)
- API Gateway (routing, load balancing)

**Microservices:**
- IAM Service (authentication, authorization)
- Account Service (bank accounts, profiles)
- Transaction Service (transfers, payments)
- Card Service (debit/credit cards, merchant payments)
- Financial Service (loans, mortgages, certificates)
- Notification Service (email, workflows, approvals)
- Audit Service (audit trails, compliance)

**Frontend:**
- React 19 + TypeScript + Vite
- Tailwind CSS
- Served via nginx with API proxy

---

**All errors fixed. Ready to deploy! 🚀**

