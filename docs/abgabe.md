# Abgabe

## Projektstand

Das Ticketverwaltungssystem ist eine Spring-Boot-Webanwendung fuer internen IT-Support.
Benutzer koennen Tickets erstellen, Support kann Tickets bearbeiten und Admin kann Klassifikationsregeln pflegen.
Die Anwendung enthaelt eine Thymeleaf-Weboberflaeche, eine REST API, rollenbasierte Sicherheit und automatische Klassifikation.

## Schnellpruefung

Automatische Tests:

```bash
./mvnw test
```

Die Tests laufen mit dem Profil `test` und H2-In-Memory-Datenbank.
MySQL ist fuer den Testlauf nicht erforderlich.

Anwendung mit Demo-Daten:

```bash
mysql -u root -p < src/main/resources/db/mysql/schema.sql
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```

Falls der Datenbankbenutzer `ticketsystem` noch nicht existiert, steht der SQL-Befehl in `README.md` und `docs/database.md`.

Danach ist die Anwendung erreichbar unter:

```text
http://localhost:8080
```

Demo-Zugaenge:

```text
user / password
support / password
admin / password
```

## Wichtige Funktionen

- Login mit Rollen `USER`, `SUPPORT` und `ADMIN`
- Ticketanlage und Ticketliste
- Rueckfragen und Kommentare
- Statushistorie
- Automatische Klassifikation mit Datenbankregeln
- Manuelle Pruefung, wenn keine Regel passt
- Finale Klassifikation durch Support
- Admin-Bereich fuer Klassifikationsregeln
- Dashboard mit Statistiken
- REST API mit HTTP Basic fuer technische Aufrufe

## Technische Entscheidungen

- Runtime-Datenbank ist MySQL.
- Hibernate validiert das Schema mit `ddl-auto: validate`.
- Demo-Daten werden nur mit Profil `demo` angelegt.
- Automatische Tests nutzen H2 im Profil `test`.
- API- und Web-Security sind getrennt konfiguriert.
- API-Zugriffe ohne Login liefern `401` statt Login-Redirect.
- Web-Zugriffe ohne Login leiten zur Login-Seite weiter.

## Dokumentation

- `README.md`: Ueberblick, Start, Rollen, REST API und Tests
- `docs/database.md`: Datenbank, Schema und MySQL-Konfiguration
- `docs/testprotokoll.md`: automatische und manuelle Testfaelle

## Bekannte Abgrenzung

Die ER-Diagramm-Dateien sind derzeit nicht Teil des finalen Abgabe-Commits.
Relevant fuer die Bewertung sind Code, Schema, Tests und die textuelle Dokumentation.
