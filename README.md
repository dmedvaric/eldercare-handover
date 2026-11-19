# Care Handover (prototype)

Shift handover board for elder care homes. The nurse finishing a shift records what happened and then the next
nurse sees what still has to be done.



**JAVA 17 + React**



## What the prototype does

* **Handover board:** one card per resident with chips for everything that needs attention.
* **Bed linen:** last change, next due date, flagged "due today" or "overdue", one tap to mark as changed.
* **Mood today:** one-tap 1 to 5 scale, 7-day trend, warning when the last days are clearly lower than before.
* **Medication:** daily medication plus **today-only medication** (disappears automatically tomorrow). Mark as given, or not given with a required reason.
* **Tasks:** standing tasks and **today-only tasks** that expire automatically. Tick off when done.
* **Handover notes:** free text per resident. A note stays highlighted until a nurse confirms "I have read this".
* **What changed:** feed of everything recorded in the last 4 to 24 hours, grouped by resident.
* No login yet: type your name and pick your shift at the top; it is stored in the browser.
* Sample data (fictional residents) is created on every start.

## How to run it

Prerequisites: JDK 17 or newer, Node.js 18 or newer.

1. **Backend:** open `backend/src/main/java/com/eldercare/handover/HandoverApplication.java` and run it
(green arrow). It listens on [http://localhost:8080](http://localhost:8080).

   * Database browser: [http://localhost:8080/h2-console](http://localhost:8080/h2-console), JDBC URL `jdbc:h2:mem:eldercare`, user `sa`, empty password.
3. **Frontend:** open IntelliJ's Terminal and run:

```
   cd frontend
   npm install
   npm run dev
   ```

   Then open [http://localhost:5173](http://localhost:5173). The dev server forwards `/api` to port 8080.

## Structure

```
backend/   Spring Boot app
  model/     JPA entities (Resident, Medication, CareTask, HandoverNote, ...)
  repo/      Spring Data repositories
  service/   HandoverService: all business rules
  api/       REST controller + request/response records
frontend/  React app (Vite)

```

## REST API

|Method|Path|Purpose|
|-|-|-|
|GET|`/api/residents`|Board overview for all residents|
|GET|`/api/activity?hours=8`|"What changed" feed|
|POST|`/api/residents/{id}/bed-change`|Mark bed as changed|
|POST|`/api/residents/{id}/mood`|Record mood (1 to 5)|
|POST|`/api/residents/{id}/medications`|Add medication (`onlyToday` for one-offs)|
|POST|`/api/medications/{id}/administer`|Given / not given (+ reason)|
|POST|`/api/residents/{id}/tasks`|Add task (`onlyToday` for expiring tasks)|
|POST|`/api/tasks/{id}/complete`|Complete task|
|POST|`/api/residents/{id}/notes`|Add handover note|
|POST|`/api/notes/{id}/acknowledge`|Confirm a note was read|

Every write returns the refreshed resident, and every body carries the `nurse` name.





## Improvements (next steps)



* Transfer to Postgres database (make schema for user and for nurses, make relational connections
* Login via Keycloack (JWT, removing stuff from DTO) -- later on multitenancy
* CI/CD -- podman/docker and deploy
* native app so with responsive design so that nurses can login via tablet/phone
* Make audit/logging respecting GDPR
* Proper schaduler (time, mutex, threads?)
* Real time updates
* Escalations, alerts?
* Staff load - permissions per view and
* Family portal --Summary of Daily feelings from resident

