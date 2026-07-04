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

- Rate Limiting ist noch nicht umgesetzt. Fuer echte Produktion sollte eine cluster-faehige Loesung am Gateway oder mit gemeinsamem Backend genutzt werden.
- Die Demo-Zugaenge sind nur fuer lokale Demo-Umgebungen gedacht.
- Backup/Restore, externe Secret Stores und Deployment-Konfiguration sind nicht Teil dieses Projekts.
