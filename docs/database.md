# Datenbank

Die Anwendung nutzt MySQL.
Die Datenbank speichert Benutzer, Rollen, Tickets, Kommentare, Statushistorie und Klassifikationsregeln.
Die Weboberflaeche ist davon getrennt und wird mit Thymeleaf und Bootstrap 5 umgesetzt.
Die eigene CSS-Datei enthaelt nur kleine Anpassungen zu Bootstrap.

Das Schema liegt hier:

```text
src/main/resources/db/mysql/schema.sql
```

## Datenbank erstellen

Das Schema kann so importiert werden:

```bash
mysql -u root -p < src/main/resources/db/mysql/schema.sql
```

Falls der Datenbankbenutzer noch fehlt:

```sql
CREATE USER IF NOT EXISTS 'ticketsystem'@'localhost' IDENTIFIED BY 'ticketsystem';
GRANT ALL PRIVILEGES ON ticketsystem.* TO 'ticketsystem'@'localhost';
FLUSH PRIVILEGES;
```

Danach kann die Anwendung gestartet werden:

```bash
./mvnw spring-boot:run
```

## Standardwerte

```text
Database: ticketsystem
User: ticketsystem
Password: ticketsystem
```

Die Werte koennen mit Umgebungsvariablen geaendert werden:

```bash
export TICKETSYSTEM_DB_URL="jdbc:mysql://localhost:3306/ticketsystem?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Berlin"
export TICKETSYSTEM_DB_USERNAME="ticketsystem"
export TICKETSYSTEM_DB_PASSWORD="ticketsystem"
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

## Hibernate

Hibernate validiert nur die vorhandene Datenbank:

```yaml
ddl-auto: validate
```

Die Tabellen werden also nicht automatisch neu erzeugt.
Sie werden ueber das Schema-Skript gepflegt.
