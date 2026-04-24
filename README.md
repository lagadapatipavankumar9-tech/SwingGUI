# SessionHub Desktop Scheduler

Coursework project for `7COM1025 Programming for Software Engineers`.

## Overview

This version of SessionHub is a desktop Swing application for managing weekend
group exercise bookings at Furzefield Leisure Centre. It supports:

- timetable browsing by day and activity
- participant reservations
- reservation changes and cancellations
- attendance recording with feedback
- management reports for attendance, ratings, and revenue

## Project Structure

```text
SessionHub/
├── pom.xml
├── README.md
└── src/
    ├── main/java/sessionhub/
    │   ├── app/
    │   │   └── Main.java
    │   ├── bootstrap/
    │   │   └── DataLoader.java
    │   ├── core/
    │   │   ├── ActivitySession.java
    │   │   ├── DayCategory.java
    │   │   ├── Enrollment.java
    │   │   ├── EnrollmentStatus.java
    │   │   ├── FeedbackNote.java
    │   │   ├── Participant.java
    │   │   └── SessionWindow.java
    │   ├── engine/
    │   │   ├── EnrollmentManager.java
    │   │   └── ReportGenerator.java
    │   └── ui/
    │       └── SessionHubFrame.java
    └── test/java/sessionhub/
        └── SessionHubTest.java
```

## Run In IntelliJ

1. Open the extracted `SessionHub` folder.
2. Import the Maven project from `pom.xml`.
3. Set the project SDK to Java 17.
4. Run `sessionhub.app.Main`.

## Notes

- the application uses in-memory sample data only
- no external database is required
- the desktop UI requires a graphical environment
