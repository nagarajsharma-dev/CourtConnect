# Court Booking Platform Refactoring

This plan outlines the architectural shift from a generic resource booking system to a specialized Court Booking Platform (similar to Playo).

## Goal Description
Transform the current `Resource` and `Booking` models into a hierarchical `Venue` → `Court` → `Booking` system. This will introduce time-slot based scheduling, operational hours for venues, and specialized data structures for sports courts.

**Suggested Names for the Platform:**
1. **CourtConnect**
2. **PlaySpace**
3. **TurfTime**
4. **ActiveArena**
5. **GameSetMatch**

## User Review Required
> [!WARNING]  
> This is a major database refactoring. The current `resources` and `bookings` tables will be dropped and recreated to match the new structure. All existing dummy data will be lost and replaced with court-specific dummy data.

## Proposed Changes

### Phase 1: Core Entity Restructuring

#### [NEW] `com/example/resourcebooking/model/Venue.java`
- Create `Venue` entity with fields: `id`, `name`, `address`, `city`, `openTime` (LocalTime), `closeTime` (LocalTime), `amenities`.
- One-to-Many relationship with `Court`.

#### [MODIFY] `com/example/resourcebooking/model/Court.java` (Rename from Resource.java)
- Rename `Resource` to `Court`.
- Add `ManyToOne` relationship to `Venue` (`venueId`).
- Change `type` to `sportType` (e.g., BADMINTON, TURF).
- Add `pricePerHour`.

#### [MODIFY] `com/example/resourcebooking/model/Booking.java`
- Change `resource_id` reference to `court_id`.
- Add `bookingDate` (LocalDate).
- Redefine `startTime` and `endTime` as `LocalTime` (representing the specific slot on the `bookingDate`).

#### [MODIFY] Repositories and Services
- Create `VenueRepository` and `VenueService`.
- Refactor `ResourceRepository` to `CourtRepository`.
- Refactor `BookingRepository` to check for overlapping times based on `courtId`, `bookingDate`, `startTime`, and `endTime`.

#### [MODIFY] `ResourceBookingApplication.java`
- Update the `CommandLineRunner` to seed Venues (e.g., "Downtown Sports Complex") and Courts ("Badminton Court 1", "Futsal Turf") instead of generic laptops/rooms.

### Phase 2: Slot Generation Logic

#### [MODIFY] `com/example/resourcebooking/service/BookingService.java`
- Implement a method `getAvailableSlots(Long courtId, LocalDate date)`.
- Logic: Iterate from Venue `openTime` to `closeTime` in 1-hour increments. Check against existing APPROVED bookings in the database for that date. Return a list of available `LocalTime` slots.

#### [MODIFY] `com/example/resourcebooking/controller/CourtController.java`
- Add `GET /api/courts/{id}/slots?date=YYYY-MM-DD` endpoint.

### Phase 3: Frontend UI Shift

#### [MODIFY] `src/main/resources/static/index.html` & `app.js`
- Redesign the dashboard to list **Venues** first, then click into a Venue to see its **Courts**.
- Replace the free-text Date/Time picker in the Booking Modal with a **Slot Picker**. The user selects a date, the frontend fetches available slots from the API, and displays them as clickable pill buttons.

## Verification Plan

### Automated Tests
- N/A (Manual verification via Swagger/Postman and UI)

### Manual Verification
1. Start the application and verify Hibernate successfully drops/creates the new tables (`venues`, `courts`, `bookings`).
2. Log in and verify the UI shows Venues and Courts.
3. Attempt to book a court slot and verify that the booked slot is instantly removed from the available slots list for that specific date.
4. Verify that booking a slot on a different date/court remains unaffected.
