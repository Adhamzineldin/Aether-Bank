# Multi-stage build for React frontend
# Build context is the repository root so we can access ../packages/payment-gateway

# Stage 1: Build the application
FROM node:22-alpine AS build

WORKDIR /app

# Copy the local workspace package referenced by frontend/package.json
# (file:../packages/payment-gateway). Preserve the relative layout so the
# "file:" link resolves correctly inside the container.
COPY packages/payment-gateway /packages/payment-gateway

# Build the local package so its dist/*.d.ts exists even on fresh clones.
WORKDIR /packages/payment-gateway
RUN npm install && npm run build

# Copy frontend package manifests first for better layer caching
WORKDIR /app
COPY frontend/package*.json /app/

# Install dependencies (resolves the file: dependency above)
RUN npm install

# Copy the rest of the frontend source
COPY frontend/ /app/

# Build the application
RUN npm run build

# Stage 2: Serve with nginx
FROM nginx:alpine

# Copy built assets from build stage
COPY --from=build /app/dist /usr/share/nginx/html

# Copy custom nginx configuration
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
