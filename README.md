# RoomReady

Housekeeping for a 60-room hotel: from the guest's checkout to a room that is ready for the next one.

## Run it in a container

You need Docker and nothing else: the image builds the app itself (in a first stage, on a JDK) and runs it on a JRE.
From this folder:

    docker build -t roomready .
    docker run -p 8080:8080 roomready

Open http://localhost:8080/ for the board - or http://<the back-office PC's IP>:8080/ from the front-desk screen.
That run keeps its database inside the container and loses it with the container.

## Keep the board, and configure it from one file

Every setting can come from the environment; the table below lists the ones you may need to change. Copy
`.env.example` to `.env`, change what you need, and run with the database on a named volume at `/data`:

    cp .env.example .env
    docker run -d --name roomready -p 8080:8080 -v roomready-data:/data --env-file .env roomready

| Variable | What it sets | Default |
|---|---|---|
| `DATABASE_URL` | the JDBC URL of the H2 file | `jdbc:h2:file:./data/roomready` (`jdbc:h2:file:/data/roomready` in `.env.example`) |
| `INSPECTION_REQUIRED_FOR` | the room types a finished clean must be inspected for; the others are ready at once | `Standard,Deluxe,Suite` |
| `STALL_AFTER_MINUTES` | minutes after which a clean in progress counts as stalled | `60` |
| `HOST` | the address the server listens on | `127.0.0.1` (`0.0.0.0` in the image) |
| `PORT` | the port | `8080` |

To stop inspecting Standard rooms: set `INSPECTION_REQUIRED_FOR=Deluxe,Suite` in `.env`, then `docker rm -f roomready`
and run the same `docker run` again. Nothing is rebuilt, and the board is as housekeeping left it, because the data
lives on the volume.

Logs go to stdout: `docker logs roomready` shows one line per request and one per event - never a guest.

## One room, from checkout to ready

With the container running on port 8080 (use the current time for `at` and a later time for `arrivalAt`):

    curl -s http://localhost:8080/actuator/health
    # {"status":"UP"}

    curl -s -X POST http://localhost:8080/checkouts -H 'content-type: application/json' -d '{"room":"214","at":"2026-09-28T10:40:00Z","arrivalAt":"2026-09-28T13:00:00Z"}'
    # {"stayId":"ST0029","room":"214","checkedOutAt":"2026-09-28T10:40:00Z","state":"dirty"}

    curl -s -X POST http://localhost:8080/assignments -H 'content-type: application/json' -d '{"room":"214","housekeeper":"HK03"}'
    # {"assignmentId":"AS001","room":"214","assignedTo":"Sunita","assignedAt":"..."}

    curl -s -X POST http://localhost:8080/assignments/AS001/start
    # {"assignmentId":"AS001","startedAt":"..."}

    curl -s -X POST http://localhost:8080/assignments/AS001/finish
    # {"assignmentId":"AS001","finishedAt":"...","cleanMinutes":36}

    curl -s -X POST http://localhost:8080/inspections -H 'content-type: application/json' -d '{"room":"214","inspector":"HK01","result":"pass"}'
    # {"inspectionId":"IN001","room":"214","cleanedBy":"Sunita","inspectedBy":"Grace","result":"pass","state":"ready",...}

    curl -s http://localhost:8080/properties/harbour-view/rooms/214/history
    # checked_out, assigned, started, finished, inspected - in order

Use the ids your own calls returned. The API docs are at http://localhost:8080/swagger-ui.html.

<!-- learntube:start -->
---

[![tests](https://github.com/mvish2511-hub/roomready-java/actions/workflows/tests.yml/badge.svg)](https://github.com/mvish2511-hub/roomready-java/actions/workflows/tests.yml)

### Progress

- [x] Ticket 1 · The hotel, as something you can look at
- [x] Ticket 2 · A checkout makes a room dirty
- [x] Ticket 3 · Assign it, and start the clock
- [x] Ticket 4 · Three ways to say no
- [x] Ticket 5 · The room reaches ready
- [x] Ticket 6 · A clipboard nobody can re-tick
- [x] Ticket 7 · One file becomes an app
- [x] Ticket 8 · The rule table, and then the rule changes
- [x] Ticket 9 · It runs on the machine in the back office
- [x] Ticket 10 · It survives being switched off

Built by Vishnuppriya M on [LearnTube Guided Projects](https://guidedprojects.learntube.ai).
<!-- learntube:end -->
