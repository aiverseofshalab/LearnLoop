# LearnLoop Production Deployment Checklist & Step-by-Step Guide

## 1. Cloud Architecture Overview

| Component | Target Cloud Provider | Role & Responsibility |
| :--- | :--- | :--- |
| **Frontend** | **Vercel** | Edge-cached static PWA delivery, HTTPS, asset compression, rewrites to backend |
| **Backend** | **Render** | Spring Boot 3.3.5 Docker service, REST API, scheduled outbox publisher, Kafka consumers |
| **Database** | **Render Managed PostgreSQL** | Primary relational store (PostgreSQL 16 on Render or AWS RDS / Supabase) |
| **Messaging** | **Managed Apache Kafka** | Cloud Kafka cluster with SASL_SSL (Confluent Cloud or Upstash Kafka) |

---

## 2. Pre-Deployment Configuration Checklist

- [x] **No Hardcoded Secrets**: Secrets and database passwords are read exclusively via environment variables.
- [x] **PostgreSQL 16 Compatibility**: Flyway migrations `V1`–`V6` optimized for PostgreSQL.
- [x] **Render DATABASE_URL Support**: Built-in conversion from `postgres://...` to JDBC PostgreSQL URL in `DatabaseConfig.java`.
- [x] **CORS Configuration**: Dynamic allowlist configured for `https://*.vercel.app` via `CORS_ALLOWED_ORIGINS`.
- [x] **Dynamic PORT Binding**: Render's `$PORT` is mapped to Spring Boot via `-Dserver.port=${PORT:-8080}` in `Dockerfile`.
- [x] **Resilient Kafka Startup**: `KafkaAdmin.setFatalIfBrokerNotAvailable(false)` configured to prevent boot crashes.
- [x] **Outbox Persistence**: Events are committed atomically in PostgreSQL before dispatching to Kafka topics.
- [x] **Consumer Idempotency**: Deduplication via `processed_events` table prevents duplicate side-effects.
- [x] **PWA & Offline Fallback**: Valid `manifest.json`, `sw.js` (with API exclusion), and responsive layout across mobile and desktop.

---

## 3. Step 1: Provision Cloud Database & Kafka

### A. Render Managed PostgreSQL
1. When deploying via `render.yaml`, Render automatically provisions `learnloop-db` (PostgreSQL 16) and injects `DATABASE_URL`.
2. If provisioning manually on Render:
   - Click **New +** -> **PostgreSQL**.
   - Name: `learnloop-db`, Database: `learnloop`, User: `learnloop_user`.
   - Copy the **Internal Database URL** (for same-region web service) or **External Database URL**.
   - Set `DB_SSL_MODE=require`.

### B. Managed Apache Kafka (e.g. Confluent Cloud or Upstash)
1. Create a Kafka cluster.
2. Create an API Key and Secret (SASL/PLAIN).
3. Ensure the following topics exist (or allow auto-creation):
   - `learnloop.enrollment.events`
   - `learnloop.course.events`
   - `learnloop.discussion.events`
   - `learnloop.assignment.events`
   - `learnloop.learning-activity.events`
   - `learnloop.notification.events`
   - `learnloop.dlt`
4. Note the bootstrap servers (e.g. `pkc-xxxx.us-east-1.confluent.cloud:9092`).

---

## 4. Step 2: Deploy Spring Boot Backend to Render

1. Log in to [Render Dashboard](https://dashboard.render.com/).
2. Click **New +** -> **Web Service**.
3. Connect your Git repository (`learn_loop`).
4. Select **Docker** as the runtime (Render will automatically detect `Dockerfile`).
5. Choose your region (e.g. `Oregon (US West)`).
6. Configure the following **Environment Variables**:

| Variable | Recommended Production Value | Description |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | `prod` | Enables production profile |
| `DATABASE_URL` | `[AUTO_WIRED_OR_POSTGRES_URL]` | Render PostgreSQL Connection String |
| `DB_SSL_MODE` | `require` | Enforces TLS connection to PostgreSQL |
| `KAFKA_BOOTSTRAP_SERVERS` | `[CLOUD_KAFKA_HOST:9092]` | Cloud Kafka bootstrap servers |
| `KAFKA_SECURITY_PROTOCOL` | `SASL_SSL` | SASL SSL for cloud Kafka |
| `KAFKA_SASL_MECHANISM` | `PLAIN` | SASL mechanism |
| `KAFKA_SASL_JAAS_CONFIG` | `org.apache.kafka.common.security.plain.PlainLoginModule required username="KEY" password="SECRET";` | JAAS credentials |
| `JWT_SECRET` | `[GENERATE_RANDOM_256_BIT_SECRET]` | 32+ characters HMAC key |
| `ADMIN_BOOTSTRAP_ENABLED`| `true` (disable after initial boot)| Creates initial system administrator |
| `ADMIN_BOOTSTRAP_EMAIL`  | `admin@yourinstitution.edu` | Admin email |
| `ADMIN_BOOTSTRAP_PASSWORD`| `[STRONG_ADMIN_PASSWORD]` | Admin password |
| `CORS_ALLOWED_ORIGINS`   | `https://*.vercel.app,https://yourdomain.com` | Permits Vercel frontend |

7. Set **Health Check Path** to `/actuator/health`.
8. Click **Create Web Service**.
9. Once deployed, note your Render URL: `https://learnloop-backend.onrender.com`.

---

## 5. Step 3: Deploy Frontend to Vercel

1. Log in to [Vercel Dashboard](https://vercel.com/).
2. Click **Add New...** -> **Project**.
3. Import your Git repository (`learn_loop`).
4. In **Project Settings**:
   - **Framework Preset**: `Other`
   - **Root Directory**: `./` (leave default, `vercel.json` automatically configures `outputDirectory: "src/main/resources/static"`)
5. In `vercel.json` (or via Vercel rewrites), update the destination URL:
   ```json
   "rewrites": [
     {
       "source": "/api/(.*)",
       "destination": "https://[YOUR_RENDER_BACKEND_URL]/api/$1"
     }
   ]
   ```
   *(Alternatively, configure `window.LEARNLOOP_API_BASE = 'https://[YOUR_RENDER_BACKEND_URL]'` in `src/main/resources/static/js/config.js`)*.
6. Click **Deploy**.
7. Your Vercel frontend will be live at `https://[YOUR_PROJECT].vercel.app`.

---

## 6. Step 4: Post-Deployment Smoke Verification

Run these verification tests against your live deployment:

1. **Actuator Health**:
   ```bash
   curl -I https://[YOUR_RENDER_BACKEND_URL]/actuator/health
   # Expected: HTTP/1.1 200 OK with {"status":"UP"}
   ```
2. **Swagger Documentation**:
   Visit `https://[YOUR_RENDER_BACKEND_URL]/swagger-ui.html`
3. **Frontend PWA Delivery**:
   Visit `https://[YOUR_VERCEL_FRONTEND_URL]/`
   - Verify Google Fonts and CSS styles load without console errors.
   - Verify Service Worker `sw.js` registers in DevTools -> Application.
4. **End-to-End User Flow**:
   - Register a Student account through the Vercel UI.
   - Verify that the API call proxies cleanly to the Render backend.
   - Enroll in a course and verify that the notification arrives via Kafka outbox processing.
