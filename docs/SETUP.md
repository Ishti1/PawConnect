# CatConnect Setup Guide

## 1. Install prerequisites

| Tool | Version | Download |
|------|---------|----------|
| JDK | 17+ | [Adoptium](https://adoptium.net/) |
| Online MySQL | 8.0+ | PlanetScale, Railway, AWS RDS, Aiven, etc. |

Build uses **Gradle Wrapper** (`gradlew.bat`) — no global Maven or Gradle install needed.

Verify Java:

```bash
java -version
```

Set `JAVA_HOME` to your JDK folder if `gradlew` reports an invalid `JAVA_HOME`.

## 2. Online MySQL database

The backend defaults to **MySQL** (not local H2). All devices share the same data when they use one backend connected to this database.

### Create the database

1. Create a MySQL 8 database on your provider (name it `catconnect`).
2. Allow connections from your backend host IP (or `%` for cloud-hosted backend).
3. Run the schema once against the remote DB:

```bash
mysql -h YOUR-HOST -u YOUR_USER -p catconnect < database/schema.sql
mysql -h YOUR-HOST -u YOUR_USER -p catconnect < database/seed.sql
```

Or paste `database/schema.sql` and `database/seed.sql` in your provider’s SQL console.

### Configure the backend

**Option A — local secrets file (recommended)**

```bash
copy backend\src\main\resources\application-local.properties.example backend\src\main\resources\application-local.properties
```

Edit `application-local.properties`:

```properties
spring.datasource.url=jdbc:mysql://YOUR-HOST:3306/catconnect?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD
app.jwt.secret=YOUR_LONG_RANDOM_SECRET_AT_LEAST_32_CHARS
```

This file is gitignored — never commit passwords.

**Option B — environment variables**

```powershell
$env:CATCONNECT_DB_URL = "jdbc:mysql://YOUR-HOST:3306/catconnect?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:CATCONNECT_DB_USER = "YOUR_DB_USER"
$env:CATCONNECT_DB_PASSWORD = "YOUR_DB_PASSWORD"
```

### Local-only development (no online DB)

```powershell
cd backend
.\gradlew.bat bootRun -Dspring.profiles.active=dev
```

Uses a local H2 file under `backend/data/`.

## 3. Run backend

```bash
cd backend
.\gradlew.bat bootRun
```

Test: `curl http://localhost:8080/api/health`

## 4. Run frontend

Login screen has **no server URL field**. Email and password only.

```bash
cd frontend
.\gradlew.bat run
```

To point the client at a remote backend (optional):

```powershell
$env:CATCONNECT_API_URL = "http://YOUR-SERVER:8080/api"
.\gradlew.bat run
```

Default API URL: `http://localhost:8080/api`

## Troubleshooting

| Issue | Fix |
|-------|-----|
| Invalid `JAVA_HOME` | Point `JAVA_HOME` to your JDK folder |
| Cannot connect to MySQL | Check host, port, firewall, SSL params, and credentials |
| Access denied for MySQL user | Grant user access from your backend IP |
| Connection refused on login | Ensure backend is running and reachable |
| JavaFX module errors | Run via `.\gradlew.bat run`, not plain `java` |
| Chat not syncing across devices | Use **one** backend + **one** online MySQL database |
