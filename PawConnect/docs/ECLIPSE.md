# CatConnect — Eclipse IDE Setup

Use **Eclipse IDE for Enterprise Java and Spring Developers** (includes Java, Maven, Gradle Buildship, and Spring Tools).

Download: [Eclipse Downloads](https://www.eclipse.org/downloads/packages/) → *Eclipse IDE for Enterprise Java and Spring Developers*

## 0. Install Lombok in Eclipse (required for backend)

The backend uses Lombok (`@Getter`, `@Builder`, etc.). Without it, Maven shows 64+ “cannot find symbol” errors.

1. Download `lombok.jar` from [projectlombok.org](https://projectlombok.org/download)
2. Double-click `lombok.jar` → select your Eclipse install → **Install / Update**
3. Restart Eclipse

Then: **Maven → Update Project** on `catconnect-backend` (Alt+F5).

## 1. Fix Java (JDK)

Your PC may have a broken `JAVA_HOME` pointing to a missing JDK 17 folder.

1. **Window → Preferences → Java → Installed JREs**
2. **Add…** → Standard VM → point to your JDK folder, e.g.  
   `C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot`
3. Check it as **default**
4. **Apply and Close**

CatConnect targets **Java 17** bytecode; JDK 17–25 is fine.

## 2. Import the backend (Java + Spring Boot)

### Option A — Maven (recommended in Eclipse)

1. **File → Import → Maven → Existing Maven Projects**
2. **Root directory:** `CatConnect/backend` (folder that contains `pom.xml`)
3. Finish. Wait for Maven update to finish (progress bar bottom-right).

### Option B — Gradle

1. **File → Import → Gradle → Existing Gradle Project**
2. **Project root directory:** `CatConnect/backend`
3. Finish. Let Gradle sync complete.

### Run Spring Boot

1. Open `src/main/java/com/catconnect/CatConnectApplication.java`
2. Right-click → **Run As → Spring Boot App**  
   (If you don’t see that: **Run As → Java Application**)
3. Console should show Spring Boot started on port **8080**
4. Test: open `http://localhost:8080/api/health` in a browser

**Working directory:** Right-click project → **Properties → Run/Debug Settings →** your launch → **Arguments** tab → **Working directory** = `${workspace_loc:/catconnect-backend}` (the `backend` project folder) so `../storage/images` resolves correctly.

**Database:** Edit `src/main/resources/application.properties` (MySQL user/password) before running.

---

## 3. Import the frontend (Java + JavaFX)

1. **File → Import → Gradle → Existing Gradle Project**  
   (or Maven if you add only Maven — Gradle is already configured)
2. **Project root:** `CatConnect/frontend`
3. Finish.

### Run JavaFX (easiest: Gradle task)

1. Open the **Gradle** view (**Window → Show View → Gradle**)
2. Expand `catconnect-frontend → Tasks → application`
3. Double-click **`run`**

### Run JavaFX (Run Configuration)

1. **Run → Run Configurations… → Java Application → New**
2. **Name:** CatConnect Frontend  
3. **Project:** catconnect-frontend (or your frontend project name)  
4. **Main class:** `com.catconnect.app.CatConnectApp`
5. **Environment** tab (optional):  
   - Variable: `CATCONNECT_API_URL`  
   - Value: `http://localhost:8080/api`
6. **Run**

Start the **backend first**, then the frontend.

---

## 4. Project layout in Eclipse

| Eclipse project | Folder | Technology |
|-----------------|--------|------------|
| `catconnect-backend` | `backend/` | Java + Spring Boot |
| `catconnect-frontend` | `frontend/` | Java + JavaFX |

You can import both into one workspace.

---

## 5. Optional: launch configs from repo

Import launch files from `eclipse/launches/`:

**Run → Run Configurations… →** select **Java Application** or **Gradle Task** → **Import…** → browse to `CatConnect/eclipse/launches/`

| File | Purpose |
|------|---------|
| `CatConnect-Backend.launch` | Run Spring Boot main class |
| `CatConnect-Frontend-Gradle.launch` | Run JavaFX via Gradle `run` task |

---

## 6. Common issues

| Problem | Fix |
|---------|-----|
| Invalid `JAVA_HOME` when using Gradle | Set JRE in Preferences (step 1); or run `gradlew-run.ps1` from terminal |
| Maven: 64 errors `builder()`, `getEmail()` not found | Install **Lombok** Eclipse plugin + enable annotation processing (see §0) |
| Maven project not building | Right-click project → **Maven → Update Project** (Alt+F5) |
| `System::load` / `enable-native-access` warnings | Safe to ignore, or add JVM arg `--enable-native-access=ALL-UNNAMED` |
| Spring Boot App missing | Install *Spring Tools 4* or use **Enterprise Java and Spring** Eclipse package |
| Frontend: JavaFX / module errors | Use Gradle task **`run`** instead of plain Java Application |
| Login fails / Backend OFFLINE on login screen | Start **backend** first; wait for `Started CatConnectApplication` in Console |
| Invalid email or password | Use `demo@catconnect.com` / `password123` (reset on each backend start) |
| Backend won't start (MySQL) | Default profile is **dev** (H2) — no MySQL needed unless you set `spring.profiles.active=mysql` |
| MySQL errors on startup | Start MySQL; fix `application.properties` credentials |

---

## 7. Demo login

- Email: `demo@catconnect.com`
- Password: `password123`

(Seeded automatically on first backend run if the database is empty.)
