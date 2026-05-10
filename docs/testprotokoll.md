# Testprotokoll

## Ziel

Dieses Dokument beschreibt die Tests fuer das Ticketverwaltungssystem.
Getestet werden Login, Rollen, Tickets, Klassifikation, Rueckfragen, Dashboard und REST API.
Die Weboberflaeche nutzt Thymeleaf mit Bootstrap 5.
Beim manuellen Test wird deshalb auch geprueft, ob Navigation, Formulare, Tabellen und Buttons korrekt angezeigt werden.

## Automatische Tests

Die automatischen Tests werden mit Maven gestartet:

```bash
./mvnw test
```

| Testklasse | Was wird getestet? | Erwartung |
| --- | --- | --- |
| `TicketsystemApplicationTests` | Spring-Kontext mit MySQL | Anwendung startet ohne Fehler |
| `ClassificationServiceTest` | Klassifikation mit Regeln | Passende Texte werden klassifiziert. Unklare Texte gehen zur manuellen Pruefung |
| `TicketServiceTest` | Ticketlogik | Support sieht alle Tickets. Benutzer sehen eigene Tickets. Neue Tickets bekommen Klassifikation und Historie |
| `DashboardServiceTest` | Dashboard | Zahlen und Prozentwerte werden gebaut |
| `ClassificationRuleAdminServiceTest` | Admin-Regeln | Tokens werden richtig gelesen. Falsche Gewichtungen werden abgelehnt |
| `ClassificationRuleAdminServicePersistenceTest` | Speichern von Regeln | Aendern von Tokens erzeugt keinen Unique-Key-Fehler |
| `ApiControllerTest` | REST API | API liefert passende DTO-Antworten |

## Manuelle Tests

| Nr. | Testfall | Voraussetzung | Schritte | Erwartetes Ergebnis | Status |
| --- | --- | --- | --- | --- | --- |
| 1 | Login als Benutzer | Anwendung laeuft, Demo-User existieren | `/login` oeffnen, `user/password` eingeben | Benutzer kommt zur Ticketliste und sieht eigene Tickets | Offen |
| 2 | Login als Support | Anwendung laeuft, Demo-User existieren | `/login` oeffnen, `support/password` eingeben | Support sieht alle Tickets und den Dashboard-Link | Offen |
| 3 | Ticket erstellen | Benutzer ist angemeldet | Neues Ticket mit Titel und Beschreibung speichern | Ticket wird gespeichert und Vorschlag ist sichtbar | Offen |
| 4 | Automatische Klassifikation | Regeln fuer Drucker/VPN/Login sind aktiv | Ticket mit passenden Begriffen erstellen | Kategorie, Prioritaet, Score und Confidence werden gespeichert | Offen |
| 5 | Manuelle Pruefung | Keine Regel passt | Ticket mit unklarer Beschreibung erstellen | Ticket bekommt Status `MANUAL_REVIEW_REQUIRED` | Offen |
| 6 | Finale Klassifikation | Support ist angemeldet | Ticket oeffnen und finale Kategorie/Prioritaet speichern | Finale Werte werden gespeichert. Vorschlag bleibt erhalten | Offen |
| 7 | Statuswechsel | Support ist angemeldet | Ticketstatus mit Notiz aendern | Status wird geaendert und in der Historie gespeichert | Offen |
| 8 | Rueckfrage stellen | Support ist angemeldet | Im Ticket eine Rueckfrage stellen | Status wird `WAITING_FOR_USER`, Kommentar wird gespeichert | Offen |
| 9 | Rueckfrage beantworten | Ticket wartet auf Benutzer | Als Ticket-Ersteller antworten | Kommentar wird gespeichert, Status wird `IN_PROGRESS` | Offen |
| 10 | Dashboard als Support | Support ist angemeldet | `/dashboard` oeffnen | Statistik wird angezeigt | Offen |
| 11 | Dashboard als Benutzer | Normaler Benutzer ist angemeldet | `/dashboard` oeffnen | Zugriff wird verweigert | Offen |
| 12 | Admin-Regeln pflegen | Admin ist angemeldet | Regel erstellen oder bearbeiten | Tokens und Gewichtungen werden gespeichert | Offen |
| 13 | REST API Ticketliste | Support-Zugangsdaten vorhanden | `GET /api/tickets` mit HTTP Basic ausfuehren | JSON-Liste wird geliefert | Offen |
| 14 | REST API Klassifikation | Support-Zugangsdaten vorhanden | `POST /api/classification/preview` mit JSON ausfuehren | JSON mit Kategorie, Prioritaet, Score und Confidence wird geliefert | Offen |
| 15 | REST API Rechte | Normaler Benutzer ist vorhanden | `GET /api/dashboard` mit `user/password` ausfuehren | Antwort ist `403 Forbidden` | Offen |
| 16 | Ticketformular pruefen | Benutzer ist angemeldet | Ticket ohne Beschreibung speichern | Fehler wird angezeigt. Ticket wird nicht gespeichert | Offen |

## REST-Beispiele

```bash
curl -u support:password http://localhost:8080/api/tickets
```

```bash
curl -u support:password \
  -H "Content-Type: application/json" \
  -d '{"title":"VPN funktioniert nicht","description":"Benutzer kann sich nicht verbinden"}' \
  http://localhost:8080/api/classification/preview
```

## Bewertung

Die Tests decken die wichtigsten Bereiche ab:

- Login und Rollen
- Ticket-Ablauf
- Klassifikation
- Unterschied zwischen Vorschlag und finaler Klassifikation
- Dashboard
- REST API
- Formularpruefung
- Bootstrap-Oberflaeche
