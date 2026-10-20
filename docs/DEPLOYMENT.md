# LearnLoop Production Deployment Guide (Render + Vercel + PostgreSQL)

## 1. Deployment Architecture Options

LearnLoop is designed for deployment using modern cloud infrastructure:
- **Frontend**: **Vercel** edge hosting with static asset optimization, PWA service worker caching, and `/api/*` rewrites.
- **Backend**: **Render** Web Service running the multi-stage Spring Boot container with dynamic `$PORT` mapping and `/actuator/health` probe.
- **Database**: **Render Managed PostgreSQL** (or external PostgreSQL 15/16/17 on AWS RDS, Supabase, Neon, or Aiven) with automatic `DATABASE_URL` parsing and SSL enforcement (`sslmode=require`).
- **Messaging**: **Managed Apache Kafka** (Upstash Kafka or Confluent Cloud) with SASL_SSL authentication.

---

## 2. Production Environment Variable Checklist

Configure these variables in your Render and Vercel dashboards:

| Variable | Target Platform | Description | Production Example |
| :--- | :--- | :--- | :--- |
| `PORT` | Render | Container internal listening port | `8080` (managed by Render) |
| `SPRING_PROFILES_ACTIVE` | Render | Active Spring profile | `prod` |
| `DATABASE_URL` | Render | Full PostgreSQL URI (auto-wired by Render) | `postgres://user:pass@dpg-xxx.render.com/learnloop` |
| `DB_SSL_MODE` | Render | PostgreSQL SSL connection requirement | `require` |
| `KAFKA_BOOTSTRAP_SERVERS`| Render | Kafka broker endpoints | `xxxx.upstash.io:9092` |
| `KAFKA_SECURITY_PROTOCOL`| Render | Kafka security protocol | `SASL_SSL` |
| `KAFKA_SASL_MECHANISM`   | Render | SASL mechanism | `PLAIN` |
| `KAFKA_SASL_JAAS_CONFIG` | Render | SASL JAAS config string | `org.apache.kafka.common.security.plain.PlainLoginModule required username="..." password="...";` |
| `JWT_SECRET` | Render | 256-bit cryptographically secure key | `[GENERATED_RANDOM_HMAC_SECRET]` |
| `JWT_EXPIRATION_MS` | Render | Access token lifespan in milliseconds | `86400000` (24h) |
| `ADMIN_BOOTSTRAP_ENABLED`| Render | Initial admin provision switch | `true` (set `false` after initial run) |
| `ADMIN_BOOTSTRAP_EMAIL`  | Render | Initial admin email | `admin@yourinstitution.edu` |
| `ADMIN_BOOTSTRAP_PASSWORD`| Render | Strong initial admin password | `[STRONG_ADMIN_PASSWORD]` |
| `CORS_ALLOWED_ORIGINS`   | Render | Whitelisted frontend origins | `https://*.vercel.app,https://yourdomain.com` |

---

## 3. Step-by-Step Deployment Procedure

### Step 1: Deploy Backend to Render
1. Log into [Render Dashboard](https://dashboard.render.com/).
2. Click **New +** -> **Blueprint** (or connect your repo using `render.yaml`).
   - If using `render.yaml`, Render automatically creates both the **PostgreSQL database** (`learnloop-db`) and the **Web Service** (`learnloop-backend`).
   - If setting up manually:
     - Create a **PostgreSQL** database: name `learnloop`, user `learnloop_user`.
     - Create a **Web Service**: Runtime **Docker**, select your GitHub repo, set Health Check Path to `/actuator/health`.
3. Set the environment variables listed in the checklist above.
4. Copy the live backend URL: `https://learnloop-backend.onrender.com`.

### Step 2: Deploy Frontend to Vercel
1. Log into [Vercel Dashboard](https://vercel.com/) and click **Add New...** -> **Project**.
2. Import your GitHub repository (`aiverseofshalab/LearnLoop`).
3. In Project Settings:
   - Framework Preset: **Other**.
   - Root Directory: `./` (leave default, `vercel.json` configures `outputDirectory: "src/main/resources/static"`).
4. Update `vercel.json` rewrites or set `window.LEARNLOOP_API_BASE` in `config.js` to your Render backend URL.
5. Click **Deploy**. Your PWA is live at `https://your-project.vercel.app`.

---

## 4. Production Smoke Verification

Run these verification checks immediately after deployment:

1. **Actuator Health**:
   ```bash
   curl -I https://learnloop-backend.onrender.com/actuator/health
   # Must return HTTP 200 OK with {"status":"UP"}
   ```
2. **OpenAPI / Swagger Documentation**:
   Visit `https://learnloop-backend.onrender.com/swagger-ui.html`
3. **PWA Manifest & Service Worker**:
   - Check `https://your-project.vercel.app/manifest.json` returns valid JSON with `display: standalone`.
   - In browser DevTools -> Application -> Service Workers -> Verify `sw.js` is registered.
4. **End-to-End Registration & Outbox Dispatch**:
   - Register a student account through the Vercel web UI.
   - Enroll in an active course.
   - Verify that the notification arrives in the student's drawer via Kafka outbox processing.
