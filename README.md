# CatConnect

A community platform for cat lovers — connect with vets, shelters, shops, and fellow cat parents.

## Tech stack (required)

| Layer | Technology |
|-------|------------|
| **Backend API** | **Java 17** + **Spring Boot 3.5** (REST, JPA, Security, JWT) |
| **Desktop UI** | **Java 17** + **JavaFX 21** |
| **Database** | **MySQL 8** |
| **Build** | Gradle Wrapper (`gradlew.bat`) or Maven (`pom.xml`) |

All backend code is plain **Java** under `backend/src/main/java` — Spring Boot controllers, services, JPA entities, and repositories. Gradle/Maven are only build tools; the application runtime is Java + Spring Boot.

## Architecture

```
CatConnect
├── frontend/          JavaFX desktop client
├── backend/           Spring Boot REST API
├── database/          MySQL schema & seed data
├── storage/images/    Uploaded pet & moment photos
└── docs/              Setup, API, and architecture guides
```

## Features

| Module | Description |
|--------|-------------|
| User accounts | Register, login, profile management |
| Lost & Found | Report and search missing/found cats |
| Nearby Vet | Vet clinics with ratings and contact |
| Cat care knowledge | Articles and care tips |
| Cat food recommendation | Food suggestions by age/health |
| Nearby cat shop | Pet supply stores near you |
| Your cat moments | Photo timeline of your cats |
| Donation & support | Support shelters and causes |
| Shelter directory | Browse local shelters |
| Adoption corner | Cats available for adoption |
| Emergency vet | 24/7 emergency clinic contacts |
| Cat memes | Curated meme feed |

## Prerequisites

- **Java 17+** (JDK; no Maven required)
- **Online MySQL 8** (shared database for all devices)

Build with **Gradle Wrapper** (`gradlew.bat` on Windows) — included in `backend/` and `frontend/`. No Maven install required.

If `gradlew` fails with invalid `JAVA_HOME`, run the helper scripts or set `JAVA_HOME` to your JDK (e.g. Eclipse Adoptium):

```powershell
.\backend\gradlew-run.ps1
.\frontend\gradlew-run.ps1
```

## Quick start

### 1. Database (online MySQL)

Configure your cloud MySQL connection — see [Setup guide](docs/SETUP.md):

```powershell
copy backend\src\main\resources\application-local.properties.example backend\src\main\resources\application-local.properties
# Edit with your online DB host, user, and password
```

Run `database/schema.sql` and `database/seed.sql` on the remote database once.

### 2. Backend (Java + Spring Boot)

```bash
cd backend
# Edit src/main/resources/application.properties (DB password, JWT secret)
.\gradlew.bat bootRun
```

Starts `CatConnectApplication` — the Spring Boot entry point. API runs at `http://localhost:8080`

### 3. Frontend

```bash
cd frontend
.\gradlew.bat run
```

### Maven (optional)

If you prefer Maven, `pom.xml` is still available: `mvn spring-boot:run` / `mvn javafx:run`.

Default API URL: `http://localhost:8080/api`

### Demo login

- Email: `demo@catconnect.com`
- Password: `password123`

## User content (add / delete your own posts)

| Screen | Add | Delete |
|--------|-----|--------|
| Lost & Found | + Add Lost / Found Post | Only on **your** posts |
| Cat Moments | + Add Moment | Only on **your** moments |
| Cat Memes | + Add Meme | Only on **your** memes |
| Adoption | + List a Cat | Only on **your** listings |

Vets, shops, shelters, and care articles are directory data (read-only in the app).

## Community Chat

Open **Community Chat** in the sidebar for real-time messaging (WebSocket). You must be logged in.

Restart **backend** and **frontend** after code updates.

**Vets, shops, shelters** are sample directory data from the database seed. To change those locations, edit `database/seed.sql` or rows in MySQL/H2, then restart the backend.

## Documentation

- [Setup guide](docs/SETUP.md)
- [**Multiple devices / LAN chat**](docs/MULTI_DEVICE.md)
- [**Eclipse IDE guide**](docs/ECLIPSE.md)
- [API reference](docs/API.md)
- [Architecture](docs/ARCHITECTURE.md)

## License

MIT
