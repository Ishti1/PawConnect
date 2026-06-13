# CatConnect — Multiple Devices

Use **one shared backend** on your network. Every device runs the **JavaFX client** and points at that same server. Chat already supports many connections at once — the usual problem is each device talking to a different backend (or to `localhost`).

## Architecture

```
┌─────────────┐     ┌─────────────┐
│  Device A   │     │  Device B   │
│  (JavaFX)   │     │  (JavaFX)   │
└──────┬──────┘     └──────┬──────┘
       │                   │
       └─────────┬─────────┘
                 │  HTTP + WebSocket
                 ▼
        ┌─────────────────┐
        │  One backend    │  ← run on ONE PC only
        │  port 8080      │
        └────────┬────────┘
                 ▼
           Database (H2 or MySQL)
```

## Quick setup

### 1. Host PC (runs the backend)

On the machine that stays on and holds the database:

```powershell
cd CatConnect\backend
.\gradlew.bat bootRun
```

Find this PC's LAN IP (PowerShell):

```powershell
ipconfig
```

Use the **IPv4 Address** (e.g. `192.168.1.10`).

Allow inbound **TCP 8080** in Windows Firewall on the host PC.

### 2. Every client device (including the host)

1. Install **Java 17+**.
2. Copy the project or build the frontend on each device.
3. Point the client at the host backend using **one** of these methods:

**Option A — environment variable (per launch):**

```powershell
cd CatConnect\frontend
$env:CATCONNECT_API_URL = "http://192.168.1.10:8080/api"
.\gradlew.bat run
```

Replace `192.168.1.10` with the host PC's IP. This overrides any saved config.

**Option B — config file (persistent on that device):**

Create or edit:

```text
%USERPROFILE%\.catconnect\config.properties
```

Contents:

```properties
api.url=http://192.168.1.10:8080/api
```

Then run the client normally:

```powershell
cd CatConnect\frontend
.\gradlew.bat run
```

If `CATCONNECT_API_URL` is set, it takes priority over the config file. Default is `http://localhost:8080/api`.

### 3. Chat from two devices at once

1. Start the **backend once** on the host.
2. Log in on **Device A** and **Device B** (same or different accounts).
3. Open **Community Chat** on both — messages sync in real time over WebSocket.

## Common mistakes

| Mistake | Result |
|--------|--------|
| Backend running on **each** device | Separate databases; chat does not sync |
| Server URL left as `localhost` on Device B | Device B looks for a backend on itself, not the host |
| Firewall blocks port 8080 | "Backend OFFLINE" on remote devices |
| Backend not running | Login and chat fail everywhere |

## Production / always-on server

For a home or small team setup that stays up:

1. Use **MySQL** on the host: `-Dspring.profiles.active=mysql`
2. Keep **one** backend process running.
3. Point all clients at `http://<host-ip>:8080/api`.

## Internet access (optional)

To use devices outside your home network you need:

- A public IP or VPN
- Reverse proxy with **HTTPS/WSS**
- Strong JWT secret and real user passwords

That is not configured out of the box; LAN use is the supported multi-device path today.
