# Ticketverwaltungssystem

Das Projekt ist eine kleine Web-Anwendung für internen IT-Support.
Benutzer können Tickets erstellen. Support kann Tickets bearbeiten.
Admin kann Regeln für die automatische Klassifikation pflegen.

## Ziel des Projekts

Das Ziel ist eine wartbare Spring-Boot-Anwendung.
Die Anwendung soll zeigen, wie Tickets erstellt, klassifiziert und bearbeitet werden.

Normale Benutzer sehen nur ihre eigenen Tickets.
Support und Admin sehen alle Tickets.
Bei neuen Tickets macht das System automatisch einen Vorschlag fuer Kategorie und Prioritaet.
Support kann diesen Vorschlag pruefen und final speichern.

## Funktionen

- Login mit Rollen
- Rollen: `USER`, `SUPPORT`, `ADMIN`
- Tickets erstellen und anzeigen
- Tickets bearbeiten
- Automatische Klassifikation mit Regeln
- Vorschlag und finale Klassifikation speichern
- Manuelle Pruefung, wenn keine Regel passt
- Statushistorie fuer Tickets
- Kommentare und Rueckfragen
- Admin-Seite fuer Klassifikationsregeln
- Dashboard mit einfachen Statistiken
- REST API fuer Tickets, Dashboard und Klassifikation
- MySQL als Datenbank

## Technik

- Java 21
- Spring Boot 4
- Spring MVC
- Thymeleaf
- Bootstrap 5
- Spring Security
- Spring Data JPA / Hibernate
- MySQL
- Flyway
- Spring Boot Actuator
- Maven
- Einfaches In-Memory-Rate-Limiting fuer Login und REST API

## Schnellstart fuer Bewertung

Automatische Tests ausfuehren:

```bash
./mvnw test
```

Die Tests verwenden H2 im Profil `test`.
Ein lokaler MySQL-Server ist dafuer nicht noetig.

Anwendung mit MySQL und Demo-Daten starten:

Vorher muss die Datenbank `ticketsystem` existieren.
Falls der Datenbankbenutzer `ticketsystem` noch nicht existiert, siehe Abschnitt `MySQL-Konfiguration`.
Die Tabellen werden beim Start ueber Flyway-Migrationen angelegt.

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```

Danach:

```text
http://localhost:8080
user / password
support / password
admin / password
```

Der aktuelle Projektstand fuer die Abgabe ist zusammengefasst in:

```text
docs/abgabe.md
```

## Weboberflaeche

Die Weboberflaeche wird mit Thymeleaf und Bootstrap 5 umgesetzt.
Bootstrap wird fuer Navigation, Buttons, Formulare, Tabellen, Badges und Alerts genutzt.

Es gibt zusaetzlich eine kleine eigene CSS-Datei:

```text
src/main/resources/static/css/app.css
```

Diese Datei ist nicht als eigenes komplettes Design-System gedacht.
Sie enthaelt nur kleine Anpassungen, zum Beispiel Abstaende, Farben und Layout-Ergaenzungen.
Das Hauptlayout und die UI-Komponenten kommen von Bootstrap.

## Aufbau

Die Anwendung ist in Schichten aufgebaut:

```text
Browser / Thymeleaf + Bootstrap UI
        |
Controller
        |
Service
        |
Repository
        |
MySQL
```

Die Controller nehmen HTTP-Anfragen an.
Die Services enthalten die Fachlogik.
Die Repositories greifen auf die Datenbank zu.

## Projektstruktur

```text
src/main/java/com/example/ticketsystem
+-- controller    Controller fuer Login, Tickets, API und Admin
+-- models        Entitaeten, DTOs und Formularobjekte
+-- repository    Spring Data Repositories
+-- services      Fachlogik, Security, Demo-Daten und Klassifikation

src/main/resources
+-- templates     Thymeleaf-Seiten mit Bootstrap
+-- static/css    kleine Anpassungen zu Bootstrap
+-- db/migration  versionierte Flyway-Migrationen
+-- db/mysql      altes Schema-Skript als Referenz
+-- application.yaml
```

## Rollen

| Rolle | Was darf die Rolle? |
| --- | --- |
| `USER` | Eigene Tickets erstellen und ansehen. Auf Rueckfragen antworten. |
| `SUPPORT` | Alle Tickets ansehen, Status aendern, kommentieren und final klassifizieren. |
| `ADMIN` | Klassifikationsregeln erstellen und bearbeiten. |

## Klassifikation

Die Klassifikation arbeitet mit Regeln.
Eine Regel hat:

- eine Kategorie
- eine Prioritaet
- einen Mindestwert
- mehrere Begriffe mit Gewichtung

Beim Erstellen eines Tickets werden Titel und Beschreibung gelesen.
Dann prueft das System alle aktiven Regeln.
Die Regel mit dem besten Score wird als Vorschlag genutzt, wenn der Mindestwert erreicht ist.

Wenn keine Regel passt, bekommt das Ticket den Status `MANUAL_REVIEW_REQUIRED`.
Dann muss Support die Kategorie und Prioritaet selbst setzen.

Beispielregeln:

| Regel | Kategorie | Prioritaet | Zweck |
| --- | --- | --- | --- |
| `Hardware Drucker` | Hardware | Level 2 | Probleme mit Drucker, Papierstau oder Toner |
| `Account Login` | Account | Level 1 | Login- und Passwortprobleme |
| `Netzwerk Verbindung` | Netzwerk | Level 1 | VPN, WLAN oder Internet |
| `E-Mail Outlook` | E-Mail | Level 2 | Outlook, Postfach oder E-Mail Versand |
| `Software Installation` | Software | Level 3 | Installation, Updates oder Lizenzen |
| `Berechtigung Zugriff` | Berechtigung | Level 2 | Zugriff auf Ordner, Laufwerke oder Freigaben |
| `Security Phishing` | Security | Level 1 | Phishing, Malware oder Warnungen |
| `Performance Arbeitsplatz` | Arbeitsplatz | Level 2 | Langsame Systeme oder Abstuerze |

## Ticket-Ablauf

```text
Benutzer erstellt Ticket
        |
System macht Klassifikationsvorschlag
        |
Support prueft das Ticket
        |
Optional: Support stellt Rueckfrage
        |
Benutzer antwortet
        |
Support setzt finalen Status und finale Klassifikation
```

Eine normale Support-Notiz aendert den Status nicht.
Nur die Aktion `Rueckfrage stellen` setzt den Status auf `WAITING_FOR_USER`.
Wenn der Benutzer antwortet, geht der Status wieder auf `IN_PROGRESS`.

## Demo-Zugaenge

Demo-Daten werden nur erstellt, wenn das Spring-Profil `demo` aktiv ist.
Dann werden diese Zugaenge angelegt:

```text
user / password
support / password
admin / password
```

Start mit Demo-Daten:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```

## Start mit MySQL

Die Anwendung nutzt MySQL.
Vor dem Start muss die Datenbank existieren.
Tabellen und Constraints werden ueber Flyway-Migrationen gepflegt.

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```

Danach ist die Anwendung hier erreichbar:

```text
http://localhost:8080
```

Wenn Port `8080` schon belegt ist:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

## MySQL-Konfiguration

Demo-Profil:

```text
Database: ticketsystem
User: ticketsystem
Password: ticketsystem
```

Das Demo-Profil darf lokale Demo-Zugangsdaten verwenden.
Das Produktionsprofil liest Datenbankzugangsdaten ohne Fallback aus Umgebungsvariablen.

Datenbank und Datenbankbenutzer lokal anlegen:

```sql
CREATE DATABASE IF NOT EXISTS ticketsystem
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'ticketsystem'@'localhost' IDENTIFIED BY 'ticketsystem';
GRANT ALL PRIVILEGES ON ticketsystem.* TO 'ticketsystem'@'localhost';
FLUSH PRIVILEGES;
```

Production-Start:

```bash
export TICKETSYSTEM_DB_URL="jdbc:mysql://localhost:3306/ticketsystem?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Berlin"
export TICKETSYSTEM_DB_USERNAME="ticketsystem"
export TICKETSYSTEM_DB_PASSWORD="change-me"
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

Optional kann das Secure-Flag fuer Session-Cookies gesetzt werden:

```bash
export TICKETSYSTEM_COOKIE_SECURE="true"
```

Lokaler Demo-Start:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```

## Datenbank

Die produktionsnahe Schema-Verwaltung laeuft ueber Flyway:

```text
src/main/resources/db/migration/V1__initial_schema.sql
```

Das alte MySQL-Schema-Skript liegt weiterhin als Referenz unter `src/main/resources/db/mysql/schema.sql`.

Wichtige Tabellen:

- `users`
- `roles`
- `user_roles`
- `tickets`
- `comments`
- `ticket_status_history`
- `audit_events`
- `categories`
- `priorities`
- `classification_rules`
- `classification_terms`

Mehr Informationen stehen in:

```text
docs/database.md
docs/abgabe.md
```

## REST API

Die REST API nutzt die gleiche Service-Logik wie die Thymeleaf-Seiten.
Fuer technische Aufrufe kann HTTP Basic genutzt werden.

Beispiel:

```bash
curl -u support:password http://localhost:8080/api/tickets
```

Wichtige Endpunkte:

| Methode | Pfad | Zweck |
| --- | --- | --- |
| `GET` | `/api/tickets?page=0&size=20` | Sichtbare Tickets paginiert holen |
| `GET` | `/api/tickets/{id}` | Details zu einem Ticket holen |
| `POST` | `/api/tickets` | Ticket erstellen |
| `POST` | `/api/tickets/{id}/status` | Status aendern |
| `POST` | `/api/tickets/{id}/classification` | Finale Klassifikation setzen |
| `POST` | `/api/classification/preview` | Klassifikation testen |
| `GET` | `/api/dashboard` | Dashboard-Daten holen |

Beispiel fuer die Klassifikation:

```bash
curl -u support:password \
  -H "Content-Type: application/json" \
  -d '{"title":"VPN funktioniert nicht","description":"Benutzer kann sich nicht verbinden"}' \
  http://localhost:8080/api/classification/preview
```

API-Fehler werden in einem einheitlichen JSON-Format geliefert.
Das gilt auch fuer API-Requests ohne Login oder ohne ausreichende Rolle.

## Rate Limiting

Login und REST API haben ein einfaches In-Memory-Rate-Limit:

```yaml
ticket-system.rate-limit.api.requests-per-minute: 120
ticket-system.rate-limit.login.requests-per-minute: 10
```

Das schuetzt die Demo-Anwendung gegen einfache Brute-Force- und Flooding-Versuche. Fuer mehrere produktive Instanzen braucht man ein cluster-faehiges Limit am Gateway oder mit gemeinsamem Backend.

## Monitoring

Spring Boot Actuator ist aktiviert.
Oeffentlich erreichbar sind nur Health und Info:

```text
GET /actuator/health
GET /actuator/info
```

Weitere Actuator-Endpunkte wie `/actuator/metrics` sind nur fuer Admins erreichbar.

## Tests

Tests starten:

```bash
./mvnw test
```

Die Tests pruefen unter anderem:

- Start der Spring-Anwendung
- Klassifikation
- Ticketlogik
- Dashboard-Statistik
- Admin-Regeln
- REST API
- API- und Web-Security
- API-Fehlerformat
- Actuator Health
- Rate Limiting fuer Login und REST API

Wichtig: Die automatischen Tests nutzen das Profil `test` mit einer H2-In-Memory-Datenbank.
Darum ist fuer `./mvnw test` kein lokaler MySQL-Server noetig.
Die Anwendung selbst nutzt weiterhin MySQL.

Das Testprotokoll liegt hier:

```text
docs/testprotokoll.md
```

## Wichtige Entscheidungen

- Klassifikationsregeln liegen in der Datenbank.
- Vorschlag und finale Klassifikation werden getrennt gespeichert.
- Statusaenderungen werden in einer eigenen Historie gespeichert.
- Support trifft die finale Entscheidung.
- MySQL wird fuer die laufende Anwendung direkt genutzt.
- Schema-Aenderungen werden ueber Flyway-Migrationen versioniert.
- Automatische Tests laufen mit einer H2-In-Memory-Datenbank im Profil `test`.
- Produktionszugangsdaten werden ueber Umgebungsvariablen gelesen.
- Wichtige Domain-Aktionen werden in `audit_events` nachvollziehbar gespeichert.
- Rate Limiting ist bewusst in-memory gehalten und fuer mehrere Instanzen an Gateway oder Shared Backend auszulagern.

## Moegliche Erweiterungen

- Audit-Log-Tabelle fuer langfristige Nachvollziehbarkeit
- Mehr End-to-End-Tests fuer die Weboberflaeche
- Export oder Druckansicht fuer das Testprotokoll
