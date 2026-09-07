# Campus Budget Request & Club Management Ecosystem

A full-stack app for managing campus clubs, OTP-based attendance, and budget
request/approval workflows — with role-based access control (ADMIN / STAFF / STUDENT)
and JWT authentication. One Expo codebase runs on web AND mobile (same pattern as
PDFShare), backed by a Spring Boot + MySQL API.

```
campus-app/
├── backend/    Spring Boot (Java) + MySQL — REST API, JWT auth, RBAC
 
```

## 1. Backend setup

1. Install Java 17+ (Java 21 also works fine), Maven, and MySQL.
2. Edit `backend/src/main/resources/application.properties`:
   - Set `spring.datasource.password` to your MySQL root password.
   - `app.jwt.secret` already has a real generated secret in this zip — you can leave
     it as-is for local dev, or generate a fresh one with `openssl rand -base64 64`.
3. Run it:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   Hibernate auto-creates the `campus_app` database and tables on first run.
4. API is live at `http://localhost:8080`.

**If you see "Access denied for user 'root'"**: your MySQL password in
`application.properties` doesn't match your real one — check it in MySQL Workbench
and update the file to match.

**If you see "Public Key Retrieval is not allowed"**: this is already fixed in this
zip's `application.properties` via `&allowPublicKeyRetrieval=true` in the datasource
URL — if you're still seeing it, confirm that flag is present on the
`spring.datasource.url` line.


## 2. Backend CORS for the web app

`app.cors.allowed-origins` in `application.properties` already includes
`http://localhost:8081` (Expo web's default port). If you access the web app via your
LAN IP instead of `localhost` (e.g. `http://10.56.239.113:8081`), add that exact IP
too:
```
app.cors.allowed-origins=http://localhost:8081,http://localhost:19006,http://localhost:5173,http://YOUR_LAN_IP:8081
```
Restart the backend after editing this file — it only reads it on startup.

## Core flows implemented

- **Auth**: register/login issue a short-lived access token + longer-lived refresh
  token; auto-refreshes on 401.
- **RBAC**: enforced both at the Spring Security filter-chain level and per-method
  (`@PreAuthorize`) — students, staff, and admins see different capabilities.
- **Clubs**: staff/admin create clubs (need a valid numeric Staff/Admin user ID as
  coordinator); students browse and join.
- **OTP attendance**: staff picks a club, generates a time-boxed numeric OTP (5 min
  expiry, configurable). Students enter the session ID + OTP; backend checks
  membership, expiry, and duplicate check-ins.
- **Budget requests**: students apply with a title/purpose/amount against a club;
  only that club's coordinating staff member (or an admin) can approve/reject.

