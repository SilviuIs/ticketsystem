# Production Hardening

## Umgesetzt

- Flyway verwaltet das Datenbankschema versioniert unter `src/main/resources/db/migration`.
- Demo-Daten bleiben im Profil `demo` und sind nicht Teil der Schema-Migrationen.
- Das Profil `prod` liest Datenbankzugangsdaten aus Umgebungsvariablen ohne Default-Passwort.
- API-Fehler verwenden ein einheitliches JSON-Format.
- API-Responses fuer fehlende oder unzureichende Authentifizierung liefern JSON statt Login-Redirect.
- Ticketlisten sind fuer Web und API paginiert.
- Actuator ist aktiv. Health und Info sind oeffentlich, Metrics ist geschuetzt.
- Support- und Admin-Aktionen werden service-seitig geloggt, ohne Kommentartexte oder Secrets zu loggen.
- Wichtige Domain-Aktionen werden zusaetzlich in `audit_events` gespeichert.
- Login und API werden mit einem einfachen In-Memory-Rate-Limit gegen Brute-Force- und Flooding-Versuche geschuetzt.

## Production-Start

```bash
export TICKETSYSTEM_DB_URL="jdbc:mysql://localhost:3306/ticketsystem?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Berlin"
export TICKETSYSTEM_DB_USERNAME="ticketsystem"
export TICKETSYSTEM_DB_PASSWORD="change-me"
export TICKETSYSTEM_COOKIE_SECURE="true"
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

## Health Check

```bash
curl http://localhost:8080/actuator/health
```

## Bewusste Grenzen

Das Rate Limiting ist absichtlich einfach gehalten:

```yaml
ticket-system.rate-limit.api.requests-per-minute: 120
ticket-system.rate-limit.login.requests-per-minute: 10
```

Die Umsetzung ist in-memory und damit nicht cluster-faehig. Fuer echte Produktion mit mehreren Instanzen sollte das Limit am Gateway, Load Balancer oder mit gemeinsamem Backend wie Redis umgesetzt werden.
- Die Demo-Zugaenge sind nur fuer lokale Demo-Umgebungen gedacht.
- Backup/Restore, externe Secret Stores und Deployment-Konfiguration sind nicht Teil dieses Projekts.
