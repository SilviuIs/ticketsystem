# Datenbank

Die Anwendung nutzt MySQL.
Die Datenbank speichert Benutzer, Rollen, Tickets, Kommentare, Statushistorie und Klassifikationsregeln.
Die Weboberflaeche ist davon getrennt und wird mit Thymeleaf und Bootstrap 5 umgesetzt.
Die eigene CSS-Datei enthaelt nur kleine Anpassungen zu Bootstrap.

Das produktionsnahe Schema wird ueber Flyway verwaltet.
Die erste Migration liegt hier:

```text
src/main/resources/db/migration/V1__initial_schema.sql
```

Das alte Schema-Skript `src/main/resources/db/mysql/schema.sql` bleibt nur als Referenz erhalten.

## Datenbank erstellen

Die Datenbank und der Benutzer koennen lokal so angelegt werden:

```sql
CREATE DATABASE IF NOT EXISTS ticketsystem
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'ticketsystem'@'localhost' IDENTIFIED BY 'ticketsystem';
GRANT ALL PRIVILEGES ON ticketsystem.* TO 'ticketsystem'@'localhost';
FLUSH PRIVILEGES;
```

Die Tabellen werden beim Start durch Flyway erzeugt oder validiert.

Danach kann die Demo-Anwendung gestartet werden:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```

## Demo-Werte

```text
Database: ticketsystem
User: ticketsystem
Password: ticketsystem
```

Diese Werte gelten nur fuer das lokale Demo-Profil.
Das Produktionsprofil nutzt Umgebungsvariablen ohne Default-Passwort:

```bash
export TICKETSYSTEM_DB_URL="jdbc:mysql://localhost:3306/ticketsystem?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Berlin"
export TICKETSYSTEM_DB_USERNAME="ticketsystem"
export TICKETSYSTEM_DB_PASSWORD="change-me"
```

## Demo-Zugaenge

Demo-Benutzer werden nur erstellt, wenn das Spring-Profil `demo` aktiv ist.
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

## Wichtige Tabellen

| Tabelle | Inhalt |
| --- | --- |
| `users` | Benutzerkonten |
| `roles` | Rollen |
| `user_roles` | Verbindung zwischen Benutzer und Rollen |
| `tickets` | Tickets mit Status und Klassifikation |
| `comments` | Kommentare zu Tickets |
| `ticket_status_history` | Historie der Statusaenderungen |
| `categories` | Ticketkategorien |
| `priorities` | Prioritaeten |
| `classification_rules` | Regeln fuer automatische Klassifikation |
| `classification_terms` | Begriffe und Gewichtung pro Regel |

## Wichtige Entscheidung

Die Regeln fuer Klassifikation liegen in den Tabellen `classification_rules` und `classification_terms`.
Darum kann Admin Regeln aendern, ohne Java-Code zu aendern.

Tickets, Kommentare und Statushistorie sind getrennt.
So kann man sehen, was passiert ist und wer etwas geaendert hat.

Flyway versioniert Schema-Aenderungen.
Demo-Daten werden nicht in Flyway-Migrationen gespeichert, sondern nur durch das Profil `demo` erzeugt.

## Hibernate

Hibernate validiert nur die vorhandene Datenbank:

```yaml
ddl-auto: validate
```

Die Tabellen werden also nicht automatisch neu erzeugt.
Sie werden ueber Flyway-Migrationen gepflegt.

## Tests

Automatische Tests verwenden nicht diese MySQL-Datenbank.
Sie laufen mit dem Profil `test` und einer H2-In-Memory-Datenbank.
Dadurch kann `./mvnw test` ohne lokalen MySQL-Server ausgefuehrt werden.
