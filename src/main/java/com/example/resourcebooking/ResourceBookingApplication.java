package com.example.resourcebooking;

import com.example.resourcebooking.model.Court;
import com.example.resourcebooking.model.Role;
import com.example.resourcebooking.model.Sport;
import com.example.resourcebooking.model.Venue;
import com.example.resourcebooking.repository.SportRepository;
import com.example.resourcebooking.repository.UserRepository;
import com.example.resourcebooking.service.CourtService;
import com.example.resourcebooking.service.UserService;
import com.example.resourcebooking.service.VenueService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalTime;
import java.util.List;

@SpringBootApplication
public class ResourceBookingApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResourceBookingApplication.class, args);
	}

	@Bean
	CommandLineRunner initData(UserService userService, VenueService venueService, CourtService courtService, UserRepository userRepository, SportRepository sportRepository) {
		return args -> {
			// Create 'admin' user if it doesn't exist
			if (!userRepository.existsByUsername("admin")) {
				userService.registerUser("admin", "admin@example.com", "password123", Role.ROLE_ADMIN);
				System.out.println("Created admin user");
			}

			// Seed sports
			List<String> defaultSports = List.of("BADMINTON", "TENNIS", "TURF_FOOTBALL", "CRICKET", "BASKETBALL");
			for (String sportName : defaultSports) {
				if (sportRepository.findByName(sportName).isEmpty()) {
					sportRepository.save(new Sport(sportName));
				}
			}

			// Create venue and courts if none exist
			if (venueService.getAllVenues().isEmpty()) {
				Venue mainVenue = new Venue("Downtown Sports Complex", "123 Main St", "Metropolis",
					LocalTime.of(8, 0), LocalTime.of(22, 0), "Parking, Washroom, Cafe");
				mainVenue = venueService.addVenue(mainVenue);

				courtService.addCourt(createDummyCourt(mainVenue, "Badminton Court 1", 50.0));
				courtService.addCourt(createDummyCourt(mainVenue, "Badminton Court 2", 50.0));
				courtService.addCourt(createDummyCourt(mainVenue, "Futsal Turf", 120.0));
				courtService.addCourt(createDummyCourt(mainVenue, "Tennis Court A", 80.0));

				System.out.println("Created dummy venue and courts");
			}
		};
	}

	private Court createDummyCourt(Venue venue, String name, Double price) {
		Court court = new Court();
		court.setVenue(venue);
		court.setName(name);
		court.setLocation(venue.getAddress());
		court.setPricePerHour(price);
		court.setStatus(com.example.resourcebooking.model.CourtStatus.AVAILABLE);
		return court;
	}
}
