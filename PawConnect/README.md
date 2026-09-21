# PawConnect

**Presentation Video**: https://youtu.be/n4EIP4eHk1E

A community platform for pet lovers — connect with vets, shelters, shops, and fellow pet parents.

## Tech Stack

| Layer | Technology |
|-------|------------|
| **Backend API** | **Java 17** + **Spring Boot 3.5** (REST, JPA, Security, JWT) |
| **Desktop UI** | **Java 17** + **JavaFX 21** |
| **Database** | **MySQL 8** (Aiven Cloud) |
| **File Storage** | **Cloudinary** |
| **Build** | Gradle Wrapper (`gradlew-run.ps1`) |

## Architecture

```
PawConnect
├── frontend/          JavaFX desktop client
├── backend/           Spring Boot REST API
├── database/          MySQL schema & seed data
├── storage/images/    Uploaded pet & moment photos
└── docs/              Setup, API, and architecture guides
```

## Features

| Module | Description |
|--------|-------------|
| User accounts | Register, login, profile & nickname management |
| **Google OAuth** | Sign in with any Google account (system browser flow) |
| Lost & Found | Report and search missing/found cats |
| Nearby Vet | Vet clinics with ratings and contact |
| Cat care knowledge | Articles and care tips |
| Nearby cat shop | Pet supply stores near you |
| Your cat moments | Photo timeline of your cats |
| Donation & support | Support shelters and causes |
| Shelter directory | Browse local shelters |
| Adoption corner | Cats available for adoption |
| Emergency vet | 24/7 emergency clinic contacts |
| Cat memes | Curated meme feed |
| Community Chat | Real-time WebSocket messaging |
| Paw Map | Interactive map of vets, shops & shelters |

## Prerequisites

- **Java 17+** (JDK)
- Internet connection (uses Aiven cloud MySQL — no local DB setup needed)

## Quick Start

### 1. Backend

```powershell
cd backend
.\gradlew-run.ps1
```

Starts the Spring Boot API at `http://localhost:8080`.

### 2. Frontend

```powershell
cd frontend
.\gradlew-run.ps1
```

Opens the JavaFX desktop app. By default it connects to `http://localhost:8080/api`.

> To point the frontend at a different backend, create/edit `%USERPROFILE%\.catconnect\config.properties`:
> ```properties
> api.url=http://localhost:8080/api
> ```

## Login Options

### Email & Password
- **Register** with your email, display name, and a password (min 6 characters).
- **Login** with email + password.

### Demo account
- Email: `demo@catconnect.com`
- Password: `password123`

### Google Sign-In
1. Click **Login with Google** on the login screen.
2. Your **system browser** (Chrome/Edge/Firefox) opens Google's sign-in page.
3. Sign in and approve access — the browser shows **"✓ Signed in!"**.
4. PawConnect logs you in automatically.
5. **First-time Google users** are shown a nickname setup dialog before entering the app.

> Google OAuth uses the system browser (not an embedded WebView), which is the approach recommended by Google for desktop apps (RFC 8252).

## Changing Your Nickname

Navigate to **Manage Account** in the sidebar:

- **Google users** — only the **Nickname** field is shown. No password needed; your Google session authenticates the change.
- **Email/password users** — provide your current email and password, then enter the new nickname.

## User Content (Add / Delete Your Own Posts)

| Screen | Add | Delete |
|--------|-----|--------|
| Lost & Found | + Add Lost / Found Post | Only on **your** posts |
| Cat Moments | + Add Moment | Only on **your** moments |
| Cat Memes | + Add Meme | Only on **your** memes |
| Adoption | + List a Cat | Only on **your** listings |

Vets, shops, shelters, and care articles are directory data (read-only in the app).

## Community Chat

Open **Community Chat** in the sidebar for real-time messaging (WebSocket). You must be logged in.

## Documentation

- [Setup guide](docs/SETUP.md)
- [Multiple devices / LAN chat](docs/MULTI_DEVICE.md)
- [Eclipse IDE guide](docs/ECLIPSE.md)
- [API reference](docs/API.md)
- [Architecture](docs/ARCHITECTURE.md)

## License

MIT
