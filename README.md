# RoomReady

Housekeeping for a 60-room hotel: from the guest's checkout to a room that is ready for the next one.

## Run it in a container

You need Docker and nothing else: the image builds the app itself. From this folder:

    docker build -t roomready .
    docker run -p 8080:8080 roomready

Open http://localhost:8080/ for the board - or http://<the back-office PC's IP>:8080/ from the front-desk screen.
`http://localhost:8080/actuator/health` answers `{"status":"UP"}` while the app is up and can reach its database.

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
- [ ] Ticket 10 · It survives being switched off

Built by Vishnuppriya M on [LearnTube Guided Projects](https://guidedprojects.learntube.ai).
<!-- learntube:end -->
