# LearnLoop Production Deployment Guide

## 1. Deployment Architecture Options

LearnLoop can be deployed across modern cloud environments using either:
- **Managed Services (Recommended for Enterprise/Production)**: Managed MySQL 8.4 (AWS RDS, GCP Cloud SQL, or DigitalOcean Managed MySQL) + Managed Kafka (Confluent Cloud or AWS MSK) + Container runner (AWS ECS, GCP Cloud Run, or Kubernetes).
- **Self-Hosted Container Stack**: Docker Compose on an Ubuntu/Debian Linux VM with reverse proxy (Nginx or Caddy) handling SSL termination.

---

## 2. Production Environment Variable Checklist

Configure these variables securely in your secrets manager or `.env`:

| Variable | Description | Production Example |
| :--- | :--- | :--- |
| `PORT` | Container internal listening port | `8080` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `prod` |
| `DB_HOST` | MySQL 8.4 hostname or endpoint | `mysql-prod.internal.cloud` |
| `DB_PORT` | MySQL port | `3306` |
| `DB_NAME` | Relational database name | `learnloop` |
| `DB_USERNAME` | Restricted DB application user | `learnloop_app` |
| `DB_PASSWORD` | Strong password (32+ chars) | `[SECURE_DB_PASSWORD]` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker endpoints | `pkc-xxxx.us-east-1.confluent.cloud:9092` |
| `JWT_SECRET` | 256-bit cryptographically secure key | `[GENERATED_RANDOM_HMAC_SECRET]` |
| `JWT_EXPIRATION_MS` | Access token lifespan in milliseconds | `86400000` (24h) |
| `ADMIN_BOOTSTRAP_ENABLED` | Initial admin provision switch | `false` (once provisioned) |
| `ADMIN_BOOTSTRAP_EMAIL` | Initial admin email | `ops@yourinstitution.edu` |
| `ADMIN_BOOTSTRAP_PASSWORD` | Strong initial admin password | `[STRONG_ADMIN_PASSWORD]` |

---

## 3. Step-by-Step Deployment Procedure

### Step 1: Provision MySQL 8.4 LTS
1. Ensure the MySQL server is version 8.4.x.
2. Create the database:
   ```sql
   CREATE DATABASE learnloop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'learnloop_app'@'%' IDENTIFIED BY 'StrongPasswordHere!';
   GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES ON learnloop.* TO 'learnloop_app'@'%';
   FLUSH PRIVILEGES;
   ```

### Step 2: Provision Apache Kafka
1. Create the required topics with appropriate partitioning:
   - `learnloop.enrollment.events` (Partitions: 3, Replication: 3)
   - `learnloop.discussion.events` (Partitions: 3, Replication: 3)
   - `learnloop.assignment.events` (Partitions: 3, Replication: 3)
   - `learnloop.learning-activity.events` (Partitions: 3, Replication: 3)
   - `learnloop.course.events` (Partitions: 3, Replication: 3)
   - `learnloop.dlt` (Partitions: 3, Replication: 3)

### Step 3: Build & Publish Container Image
```bash
# Build multi-stage production image
docker build -t your-registry.com/learnloop:1.0.0 .

# Push image to registry
docker push your-registry.com/learnloop:1.0.0
```

### Step 4: Run Database Migrations
On startup, Spring Boot executes Flyway migrations automatically against MySQL. To verify schema status independently:
```bash
./mvnw flyway:info -Dflyway.url=jdbc:mysql://DB_HOST:3306/learnloop -Dflyway.user=DB_USER -Dflyway.password=DB_PASS
```

### Step 5: Start Application Container
```bash
docker run -d \
  --name learnloop-app \
  --restart unless-stopped \
  -p 8080:8080 \
  --env-file /etc/learnloop/production.env \
  your-registry.com/learnloop:1.0.0
```

### Step 6: Configure HTTPS & Reverse Proxy
Configure Nginx or Caddy to proxy traffic to port 8080 and enforce TLS 1.3:
```nginx
server {
    listen 443 ssl http2;
    server_name learnloop.yourinstitution.edu;

    ssl_certificate /etc/letsencrypt/live/learnloop/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/learnloop/privkey.pem;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;
    }
}
```

---

## 4. Production Smoke Verification

Run these verification checks immediately after deployment:

1. **Actuator Health**:
   ```bash
   curl -I https://learnloop.yourinstitution.edu/actuator/health
   # Must return HTTP 200 OK with {"status":"UP"}
   ```
2. **OpenAPI / Swagger Documentation**:
   Visit `https://learnloop.yourinstitution.edu/swagger-ui.html`
3. **PWA Manifest & Service Worker**:
   - Check `https://learnloop.yourinstitution.edu/manifest.json` returns valid JSON with `display: standalone`.
   - In browser DevTools -> Application -> Service Workers -> Verify `sw.js` is registered and active.
4. **End-to-End Registration & Outbox Dispatch**:
   - Register a student account through the web UI.
   - Enroll in an active course.
   - Verify in MySQL that `outbox_events` transitioned to `PUBLISHED`.
   - Check notifications drawer for the enrollment confirmation alert.
