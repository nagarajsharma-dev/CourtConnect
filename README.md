# CourtConnect

A court and sports-venue booking platform built with Spring Boot — inspired by apps like Playo. Users can browse venues, pick a sport, and book an available time slot on a specific court. Admins can create and manage venues, courts, and bookings.

## Features

- **Venue → Court → Booking** hierarchy: venues have operating hours and amenities; each venue has one or more courts; each court supports one or more sports
- **Slot-based booking**: available time slots are generated dynamically from a venue's operating hours minus existing bookings, so double-booking a court is impossible
- **One slot, one booking per user**: a user can hold bookings across different sports/courts at different times, but cannot book two courts for the same overlapping time slot
- **Role-based access**: regular users browse and book; admins manage venues, courts, and view/manage all bookings
- **Photo & amenity support** for venues/courts

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 4.1 |
| Persistence | Spring Data JPA (Hibernate) |
| Database | MySQL |
| Security | Spring Security |
| Validation | Spring Boot Validation |
| Frontend | Static HTML, CSS, JavaScript (served from `src/main/resources/static`) |
| Build tool | Maven (via Maven Wrapper) |

## Project Structure

```
CourtConnect/
├── src/
│   └── main/
│       ├── java/com/example/resourcebooking/
│       │   ├── model/          # Venue, Court, Booking entities
│       │   ├── repository/     # Spring Data JPA repositories
│       │   ├── service/        # Business logic (slot generation, overlap checks)
│       │   └── controller/     # REST controllers
│       └── resources/
│           ├── static/         # Frontend (HTML/CSS/JS)
│           └── application.properties
├── uploads/                    # Uploaded venue/court photos
├── implementation_plan.md      # Architecture & migration notes
└── pom.xml
```

## Getting Started

### Prerequisites

- Java 17+
- MySQL 8+
- Maven (or use the included wrapper — no local install needed)

### Setup

1. **Clone the repo**
   ```bash
   git clone https://github.com/nagarajsharma-dev/CourtConnect.git
   cd CourtConnect
   ```

2. **Create a MySQL database**
   ```sql
   CREATE DATABASE courtconnect;
   ```

3. **Configure the connection** in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/courtconnect
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   spring.jpa.hibernate.ddl-auto=update
   ```

4. **Run the app**
   ```bash
   ./mvnw spring-boot:run
   ```
   (Windows: `mvnw.cmd spring-boot:run`)

5. Open **http://localhost:8080** in your browser.

## Core Booking Rule

The booking logic enforces two overlap checks before confirming any booking:

1. **Court availability** — no two bookings can overlap for the same court on the same date.
2. **User availability** — no user can hold two overlapping bookings across different courts at the same time.

Both are checked using time-range overlap logic (`startTime < requestedEnd AND endTime > requestedStart`), not simple equality, so partial overlaps are also caught.

## Roadmap

See [`implementation_plan.md`](./implementation_plan.md) for the full architecture migration plan (Venue/Court/Booking refactor, slot-generation logic, and frontend UI shift).

Planned next steps:
- Admin "Create Court" flow (name, location, amenities, photos, supported sports)
- Reviews/ratings per venue
- Payment integration
- Recurring bookings

## License

No license specified yet.
