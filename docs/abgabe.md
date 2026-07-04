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
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```

Vorher muss die lokale MySQL-Datenbank `ticketsystem` existieren.
Falls der Datenbankbenutzer `ticketsystem` noch nicht existiert, steht der SQL-Befehl in `README.md` und `docs/database.md`.
Die Tabellen werden durch Flyway-Migrationen angelegt.

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
- Schema-Aenderungen laufen ueber Flyway.
- Hibernate validiert das Schema mit `ddl-auto: validate`.
- Demo-Daten werden nur mit Profil `demo` angelegt.
- Automatische Tests nutzen H2 im Profil `test`.
- API- und Web-Security sind getrennt konfiguriert.
- API-Zugriffe ohne Login liefern `401` statt Login-Redirect.
- Web-Zugriffe ohne Login leiten zur Login-Seite weiter.
- Production-Zugangsdaten werden ueber Umgebungsvariablen gelesen.
- Actuator Health/Info sind oeffentlich, weitere Actuator-Endpunkte sind geschuetzt.

## Dokumentation

- `README.md`: Ueberblick, Start, Rollen, REST API und Tests
- `docs/database.md`: Datenbank, Schema und MySQL-Konfiguration
- `docs/testprotokoll.md`: automatische und manuelle Testfaelle

## Bekannte Abgrenzung

Rate Limiting ist noch nicht cluster-faehig umgesetzt.
Ein dauerhaftes Audit-Log als eigene Datenbanktabelle ist ebenfalls ein sinnvoller naechster Produktionsschritt.
Aktuell gibt es service-level Logging fuer wichtige Support- und Admin-Aktionen.
