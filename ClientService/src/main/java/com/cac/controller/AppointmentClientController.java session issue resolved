package com.cac.controller;

import com.cac.model.Appointment;
import com.cac.model.AppointmentDTO;
import com.cac.model.CancelAppointmentDTO;
import com.cac.model.Doctor;
import com.cac.model.DoctorDTO;
import com.cac.model.Patient;
import com.cac.model.RescheduleAppointmentDTO;
import com.cac.model.ScheduleAppointmentDTO;
import com.cac.model.SelectSearchDate;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/patient")
public class AppointmentClientController {

	@Autowired
	private RestTemplate restTemplate;

	@Value("${base.url}")
    private String baseUrl;

    Doctor doctorSession = null;

    Patient patientSession = null;

    String role = null;

    @ModelAttribute
    public void getPatient(@SessionAttribute(name = "patientObj", required = false) Patient patObj) {
        patientSession = patObj;

    }

	@ModelAttribute
    public void getPatient(@SessionAttribute(name = "docObj", required = false) Doctor docObj) {
        doctorSession = docObj;

    }

    @ModelAttribute
    public void getRole(@SessionAttribute(name = "userRole", required = false) String userRole, Model model) {
        role = userRole;
        model.addAttribute("userRole", userRole);
    }

    @ModelAttribute
    public String checkLogin() {
        if (role == null)
            return "redirect:/";
        return null;
    }

    private boolean isAuthPatient(int patientId) {
        if (role == null)
            return false;
        if (role.equalsIgnoreCase("patient") && patientSession != null && patientId != patientSession.getPatientId())
            return false;
        return true;
    }

	@GetMapping("/{patientId}/appointments")
	public String showAppointmentsForPatient(@PathVariable int patientId, Model model) {

		if(role==null) {
			model.addAttribute("errorMessage", "Login Required!.");
			return "unauthorized";
		}
		if((role.equalsIgnoreCase("patient") && patientSession!=null && patientSession.getPatientId()!=patientId)){
			model.addAttribute("errorMessage", "Unauthorized Access!.");
			return "unauthorized";
		}
		
		String url = baseUrl + "/patient/" + patientId + "/appointments";
		try {
			ResponseEntity<List<AppointmentDTO>> response = restTemplate.exchange(url, HttpMethod.GET, null,
					new ParameterizedTypeReference<List<AppointmentDTO>>() {
					});
			List<AppointmentDTO> appointments = response.getBody();

			if (appointments == null || appointments.isEmpty()) {
				model.addAttribute("message", "No appointments found.");
			} else {
				model.addAttribute("patientId", patientId);
				model.addAttribute("appointments", appointments);
			}
			return "appointments";
		} catch (ResourceAccessException e) {
			// model.addAttribute("errorMessage", "Unable to fetch appointments. Please
			// check the backend service.");
			String errorMessage = e.getMessage();
			model.addAttribute("errorMessage", errorMessage);
			return "error";
		} catch (Exception e) {
			// model.addAttribute("errorMessage", "An unexpected error occurred while
			// fetching appointments.");
			String errorMessage = e.getMessage();
			model.addAttribute("errorMessage", errorMessage);
			return "error";
		}
	}

	@GetMapping("/{patientId}/appointments/selectDoctor")
	public String showScheduleAppointment(@PathVariable Long patientId, @RequestParam(required = false) String name,
			@RequestParam(required = false) String specialty, @RequestParam(required = false) String experience,
			@RequestParam(required = false) String gender, Model model, HttpSession session) {

				if(role==null && patientId!=0) {
			model.addAttribute("errorMessage", "Please login to continue.");
			session.setAttribute("redirectUrl", "/patient/"+0+"/appointments/selectDoctor");
			return "redirect:/patientLoginForm";
				}

		String url = baseUrl + "/api/doctors";
		try {
			// Make GET request to fetch doctors
			ResponseEntity<List<DoctorDTO>> response = restTemplate.exchange(url, HttpMethod.GET, null,
					new ParameterizedTypeReference<List<DoctorDTO>>() {
					});
			List<DoctorDTO> doctors = response.getBody();

			// Extract unique specialties from active doctors
			List<String> specialties = doctors.stream().filter(DoctorDTO::getStatus) // Filter only active doctors
					.map(DoctorDTO::getSpecialization) // Extract specialties
					.distinct() // Get unique specialties
					.sorted() // Sort the specialties alphabetically
					.collect(Collectors.toList());

			// Apply filters if parameters are provided
			if (doctors != null) {
				doctors = doctors.stream().filter(doctor -> filterByName(doctor, name))
						.filter(doctor -> filterBySpecialty(doctor, specialty))
						.filter(doctor -> filterByExperience(doctor, experience))
						.filter(doctor -> filterByGender(doctor, gender)).filter(DoctorDTO::getStatus) // Ensure the
																										// doctor is
																										// active // //
																										// active
						.collect(Collectors.toList());
			}

			// Add attributes to the model
			model.addAttribute("patientId", patientId);
			model.addAttribute("doctors", doctors);// Add the filtered list of doctors to the model
			model.addAttribute("specialties", specialties);// Add the list of specialties to the model

			// Return the view
			return "appointment-selectDoctor";

		} catch (Exception e) {
			model.addAttribute("errorMessage", "Failed to fetch doctors. Please try again.");
			return "error";
		}

	}

	// Helper method for name filtering (partial match, case-insensitive)
	private boolean filterByName(DoctorDTO doctor, String name) {
		return name == null || name.isEmpty() || doctor.getDoctorName().toLowerCase().contains(name.toLowerCase());
	}

	// Helper method for specialty filtering (partial match, case-insensitive)
	private boolean filterBySpecialty(DoctorDTO doctor, String specialty) {
		return specialty == null || specialty.isEmpty()
				|| doctor.getSpecialization().toLowerCase().contains(specialty.toLowerCase());
	}

	// Helper method for experience filtering (range handling, e.g., "10-15" or
	// "10+")
	private boolean filterByExperience(DoctorDTO doctor, String experience) {
		if (experience == null || experience.isEmpty()) {
			return true; // No filter applied
		}
		try {
			if (experience.contains("-")) {
				// Range format: "min-max"
				String[] expRange = experience.split("-");
				int minExp = Integer.parseInt(expRange[0]);
				int maxExp = Integer.parseInt(expRange[1]);
				return doctor.getYearsOfExperience() >= minExp && doctor.getYearsOfExperience() <= maxExp;
			} else if (experience.endsWith("+")) {
				// Format: "min+"
				int minExp = Integer.parseInt(experience.replace("+", ""));
				return doctor.getYearsOfExperience() >= minExp;
			}
		} catch (NumberFormatException e) {
			// Handle invalid experience format gracefully
			return false;
		}
		return false;
	}

	// Helper method for gender filtering (exact match, case-insensitive)
	private boolean filterByGender(DoctorDTO doctor, String gender) {
		return gender == null || gender.isEmpty() || doctor.getGender().equalsIgnoreCase(gender);
	}

	

	@GetMapping("/{patientId}/appointments/{doctorId}/schedule")
	public String scheduleAppointment(@PathVariable Long patientId, @PathVariable Long doctorId, Model model) {

		String url = baseUrl + "/api/doctors/" + doctorId;
		try {
			// Make GET request to fetch doctors
			ResponseEntity<DoctorDTO> response = restTemplate.exchange(url, HttpMethod.GET, null,
					new ParameterizedTypeReference<DoctorDTO>() {
					});
			DoctorDTO doctor = response.getBody();

			// Get today's date
			LocalDate today = LocalDate.now();

			// Create a list of the next 7 days and format them as 'MMM dd, yyyy'
			List<String> nextSevenDaysFormatted = new ArrayList<>();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
			for (int i = 0; i < 7; i++) {
				nextSevenDaysFormatted.add(today.plusDays(i).format(formatter));
			}

			// Add the formatted dates to the model
			model.addAttribute("nextSevenDaysFormatted", nextSevenDaysFormatted);

			if (doctor != null) {
				model.addAttribute("scheduleAppointmentDTO", new ScheduleAppointmentDTO());
				model.addAttribute("patientId", patientId);
				model.addAttribute("doctorId", doctorId);
				model.addAttribute("doctor", doctor);
			} else {
				model.addAttribute("errorMessage", "Doctor not found.");
				return "error";
			}
		} catch (HttpClientErrorException e) {
			String errorMessage = e.getResponseBodyAsString();
			model.addAttribute("errorMessage", errorMessage);
			return "error";
		} catch (Exception e) {
			model.addAttribute("errorMessage", "Failed to fetch doctor. Please try again.");
			return "error";
		}

		return "appointment-schedule";
	}

	@GetMapping("/{patientId}/appointments/{doctorId}/schedule-timeslots")
	public String timeslotAppointment(@PathVariable Long patientId, @PathVariable Long doctorId,
			@ModelAttribute ScheduleAppointmentDTO scheduleAppointmentDTO, Model model) {

		String url = baseUrl + "/api/availability/time-slots";
		String url1 = baseUrl + "/api/doctors/" + doctorId;
		try {
			String appointmentDateString = scheduleAppointmentDTO.getAppointmentDate();
			System.out.println("Appointment Date String: " + appointmentDateString);

			// Define a formatter to match the input date format
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

			LocalDate appointmentDate = LocalDate.parse(appointmentDateString, formatter);
			DayOfWeek dayOfWeek = appointmentDate.getDayOfWeek();

			UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url).queryParam("dayOfWeek", dayOfWeek)
					.queryParam("doctorId", doctorId);

			System.out.println("Generated URL: " + uriBuilder.toUriString());

			HttpHeaders headers = new HttpHeaders();
			headers.set("Content-Type", "application/json");
			HttpEntity<Void> request = new HttpEntity<>(headers);

			ResponseEntity<Map<String, Boolean>> response = restTemplate.exchange(uriBuilder.toUriString(),
					HttpMethod.GET, request, new ParameterizedTypeReference<Map<String, Boolean>>() {
					});

			Map<String, Boolean> availableTimeSlots = response.getBody();
			System.out.println("Available Time Slots (Raw): " + availableTimeSlots);

			ResponseEntity<DoctorDTO> responseDoctor = restTemplate.exchange(url1, HttpMethod.GET, null,
					new ParameterizedTypeReference<DoctorDTO>() {
					});
			DoctorDTO doctor = responseDoctor.getBody();

			LocalDate today = LocalDate.now();

			// Create a list of the next 7 days and format them as 'MMM dd, yyyy'
			List<String> nextSevenDaysFormatted = new ArrayList<>();
			DateTimeFormatter formatterNew = DateTimeFormatter.ofPattern("MMM dd, yyyy");
			for (int i = 0; i < 7; i++) {
				nextSevenDaysFormatted.add(today.plusDays(i).format(formatterNew));
			}

			if (availableTimeSlots != null) {
				List<String> filteredAndSortedTimeSlots = availableTimeSlots.entrySet().stream()
						.filter(Map.Entry::getValue) // Keep only available slots (value = true)
						.map(Map.Entry::getKey) // Extract the time strings
						.sorted() // Sort the times in order
						.collect(Collectors.toList());

				System.out.println("Filtered and Sorted Time Slots: " + filteredAndSortedTimeSlots);

				model.addAttribute("availableTimeslots", filteredAndSortedTimeSlots);
				model.addAttribute("patientId", patientId);
				model.addAttribute("doctorId", doctorId);
				model.addAttribute("doctor", doctor);
				model.addAttribute("nextSevenDaysFormatted", nextSevenDaysFormatted);
				model.addAttribute("scheduleAppointmentDTO", scheduleAppointmentDTO);

				return "appointment-schedule";
			} else {
				model.addAttribute("errorMessage", "No available time slots found. Please try again later.");
				return "error";
			}
		} catch (HttpClientErrorException e) {
			e.printStackTrace();
			model.addAttribute("errorMessage", e.getResponseBodyAsString());
			return "error";
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("errorMessage", "Failed to fetch available time slots. Please try again.");
			return "error";
		}
	}

	@PostMapping("/{patientId}/appointments/{doctorId}/schedule")
	public String scheduleAppointment(@PathVariable int patientId, @PathVariable int doctorId,
			@ModelAttribute ScheduleAppointmentDTO scheduleAppointmentDTO, Model model) {
		String url = baseUrl + "/patient/" + patientId + "/appointments/schedule";
		String url1 = baseUrl + "/api/availability/schedule";

		System.out.println("scheduleAppointmentDTO Date : " + scheduleAppointmentDTO.getAppointmentDate());
		System.out.println("scheduleAppointmentDTO Time : " + scheduleAppointmentDTO.getAppointmentTime());
		// Set the patient ID in the DTO
		scheduleAppointmentDTO.setPatientId(patientId);

		// Convert appointmentDate and appointmentTime to LocalDate and LocalTime
		LocalDate appointmentDate = null;
		LocalTime appointmentTime = null;

		try {
			// Convert the appointmentDate from "MMM dd, yyyy" format to LocalDate
			DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH);
			appointmentDate = LocalDate.parse(scheduleAppointmentDTO.getAppointmentDate(), dateFormatter);
			System.out.println("Converted appointmentDate: " + appointmentDate);

			// Log the raw appointmentTime before parsing
			System.out.println("Raw appointmentTime: " + scheduleAppointmentDTO.getAppointmentTime());

			// Convert the appointmentTime from "hh:mma" (e.g., 09:00am or 09:00AM) to
			// LocalTime
			DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mma", Locale.ENGLISH);
			appointmentTime = LocalTime.parse(scheduleAppointmentDTO.getAppointmentTime().toUpperCase(), timeFormatter);
			System.out.println("Converted appointmentTime: " + appointmentTime);
		} catch (DateTimeParseException e) {

			try{
				DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH);
			appointmentTime = LocalTime.parse(scheduleAppointmentDTO.getAppointmentTime().toUpperCase(), timeFormatter);
			} catch(DateTimeParseException e1){
				model.addAttribute("errorMessage",
					"Invalid date or time format. Please use MMM dd, yyyy for the date and hh:mma for the time.");
			return "error";
			}
		}

		// Map DTO to backend Appointment entity
		Appointment appointment = new Appointment();
		appointment.setPatientId(scheduleAppointmentDTO.getPatientId());
		System.out.println(appointment.getPatientId());
		appointment.setReason(scheduleAppointmentDTO.getReason());
		System.out.println(appointment.getReason());
		appointment.setDoctorId(doctorId);
		System.out.println(appointment.getDoctorId());

		appointment.setAppointmentDate(appointmentDate);
		System.out.println(appointment.getAppointmentDate());
		appointment.setAppointmentTime(appointmentTime);
		System.out.println(appointment.getAppointmentTime());
		appointment.setStatus(scheduleAppointmentDTO.getStatus());
		System.out.println(appointment.getStatus());
		appointment.setReasonOfCancellation(scheduleAppointmentDTO.getReasonOfCancellation());
		System.out.println(appointment.getReasonOfCancellation());

		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Content-Type", "application/json");
			HttpEntity<Appointment> request = new HttpEntity<>(appointment, headers);

			ResponseEntity<Appointment> response = restTemplate.exchange(url, HttpMethod.POST, request,
					Appointment.class);

			Appointment createdAppointment = response.getBody();

			if (createdAppointment != null) {

				// Step 2: Transform the DTO for the availability scheduling request
				LocalDate appointmentNewDate = appointmentDate;
				System.out.println(appointmentNewDate);
				DayOfWeek dayOfWeek = appointmentNewDate.getDayOfWeek(); // Get DayOfWeek from date
				System.out.println(dayOfWeek);
				String timeSlot = scheduleAppointmentDTO.getAppointmentTime();
				System.out.println(timeSlot);
				Long doctorId1 = appointment.getDoctorId().longValue();
				System.out.println(doctorId1);
				System.out.println(dayOfWeek.toString());

				UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url1)
						.queryParam("dayOfWeek", dayOfWeek).queryParam("doctorId", doctorId)
						.queryParam("timeSlot", timeSlot);

				System.out.println("Generated URL: " + uriBuilder.toUriString());

				// Creating headers for the request
				HttpHeaders secondaryHeaders = new HttpHeaders();
				secondaryHeaders.set("Content-Type", "application/json");

				// Creating the HttpEntity with just the headers (no body)
				HttpEntity<Void> secondaryRequest = new HttpEntity<>(secondaryHeaders);

				// Sending the POST request with the generated URL
				ResponseEntity<String> secondaryResponse = restTemplate.exchange(uriBuilder.toUriString(),
						HttpMethod.POST, secondaryRequest, String.class);
				// Step 3: Check the result of the secondary request
				if (secondaryResponse.getStatusCode().is2xxSuccessful()) {
					model.addAttribute("patientId", patientId);
					model.addAttribute("appointment", createdAppointment);
					return "appointment-confirmation";
				} else {
					model.addAttribute("errorMessage", "Failed to update availability. Please try again.");
					return "error";
				}
			} else {
				model.addAttribute("errorMessage", "Appointment could not be scheduled. Please try again.");
				return "error";
			}
		} catch (HttpClientErrorException e) {
			// model.addAttribute("errorMessage", "Invalid input or scheduling conflict.
			// Please check your details.");
			String errorMessage = e.getResponseBodyAsString();
			model.addAttribute("errorMessage", errorMessage);
			return "error";
		} catch (Exception e) {
			model.addAttribute("errorMessage", "Failed to schedule the appointment. Please try again.");
			return "error";
		}
	}

	@GetMapping("/{patientId}/appointments/cancel/{appointmentId}")
	public String showCancelAppointment(@PathVariable Long patientId, @PathVariable int appointmentId, Model model) {
		CancelAppointmentDTO cancelAppointmentDTO = new CancelAppointmentDTO();
		cancelAppointmentDTO.setAppointmentId(appointmentId); // Pre-fill the appointment ID
		model.addAttribute("cancelAppointmentDTO", cancelAppointmentDTO);
		model.addAttribute("patientId", patientId);
		return "appointment-cancel";
	}

	@PostMapping("/{patientId}/appointments/cancel/{appointmentId}")
	public String cancelAppointment(@PathVariable Long patientId, @PathVariable Long appointmentId,
			@ModelAttribute CancelAppointmentDTO cancelAppointmentDTO, Model model) {
		System.out.println("Cancel Request Received for Patient ID: " + patientId);
		System.out.println("Appointment ID: " + appointmentId);
		System.out.println("ID: " + cancelAppointmentDTO.getAppointmentId());
		System.out.println("Reason: " + cancelAppointmentDTO.getReasonOfCancellation());

		String url = baseUrl + "/patient/" + patientId + "/appointments/cancel";
		String url1 = baseUrl + "/patient/" + patientId + "/appointments/" + appointmentId;
		String url2 = baseUrl + "/api/availability/cancel";

		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Content-Type", "application/json");
			HttpEntity<CancelAppointmentDTO> request = new HttpEntity<>(cancelAppointmentDTO, headers);

			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
			System.out.println(response);
			if (response.getStatusCode().is2xxSuccessful()) {
				ResponseEntity<Appointment> responseAppointment = restTemplate.exchange(url1, HttpMethod.GET, null,
						new ParameterizedTypeReference<Appointment>() {
						});
				Appointment appointments = responseAppointment.getBody();


				LocalDate appointmentDate = appointments.getAppointmentDate();
				System.out.println(appointmentDate);
				DayOfWeek dayOfWeek = appointmentDate.getDayOfWeek(); // Get DayOfWeek from date
				System.out.println(dayOfWeek);
				LocalTime timeSlot = appointments.getAppointmentTime();
				System.out.println(timeSlot);
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mma");

				// Convert the LocalTime to a formatted string
				String formattedTime = timeSlot.format(formatter).toLowerCase();

				// Now formattedTime will be in the "09:00 am" format
				System.out.println(formattedTime);
				Long doctorId = appointments.getDoctorId().longValue();
				System.out.println(doctorId);
				System.out.println(dayOfWeek.toString());

				UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url2)
						.queryParam("dayOfWeek", dayOfWeek).queryParam("doctorId", doctorId)
						.queryParam("timeSlot", formattedTime);

				System.out.println("Generated URL: " + uriBuilder.toUriString());

				// Creating headers for the request
				HttpHeaders secondaryHeaders = new HttpHeaders();
				secondaryHeaders.set("Content-Type", "application/json");

				// Creating the HttpEntity with just the headers (no body)
				HttpEntity<Void> secondaryRequest = new HttpEntity<>(secondaryHeaders);

				// Sending the POST request with the generated URL
				ResponseEntity<String> secondaryResponse = restTemplate.exchange(uriBuilder.toUriString(),
						HttpMethod.POST, secondaryRequest, String.class);

				if (secondaryResponse.getStatusCode().is2xxSuccessful()) {
					System.out.println("Cancellation Successful.");
					System.out.println("Updated availability successfully.");
					return "appointment-cancel-confirmation";
				} else {
					model.addAttribute("errorMessage", "Failed to update availability. Please try again.");
					return "error";
				}

			} else {
				System.out.println("Cancellation Failed: " + response.getStatusCode());
				model.addAttribute("errorMessage", "Cancellation failed. Please try again.");
				return "error";
			}
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("errorMessage", "An unexpected error occurred during cancellation.");
			return "error"; // Return error page
		}
	}

	@GetMapping("/{patientId}/appointments/reschedule/{appointmentId}")
	public String showRescheduleAppointment(@PathVariable Long patientId, @PathVariable Long appointmentId,
			Model model) {

		LocalDate today = LocalDate.now();
		// Create a list of the next 7 days and format them as 'MMM dd, yyyy'
		List<String> nextSevenDaysFormatted = new ArrayList<>();
		DateTimeFormatter formatterNew = DateTimeFormatter.ofPattern("MMM dd, yyyy");
		for (int i = 0; i < 7; i++) {
			nextSevenDaysFormatted.add(today.plusDays(i).format(formatterNew));
		}
		model.addAttribute("nextSevenDaysFormatted", nextSevenDaysFormatted);
		model.addAttribute("rescheduleAppointmentDTO", new RescheduleAppointmentDTO());
		model.addAttribute("patientId", patientId);
		model.addAttribute("appointmentId", appointmentId);
		return "appointment-reschedule";
	}

	@GetMapping("/{patientId}/appointments/{appointmentId}/reschedule-timeslots")
	public String rescheduleTimeslotAppointment(@PathVariable int patientId, @PathVariable int appointmentId,
			@ModelAttribute RescheduleAppointmentDTO rescheduleAppointmentDTO, Model model) {

		String url = baseUrl + "/api/availability/time-slots";
		String url1 = baseUrl + "/patient/" + patientId + "/appointments/" + appointmentId;
		try {
			String appointmentDateString = rescheduleAppointmentDTO.getNewDate();
			System.out.println("Appointment Date String: " + appointmentDateString);

			// Define a formatter to match the input date format
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

			LocalDate appointmentDate = LocalDate.parse(appointmentDateString, formatter);
			DayOfWeek dayOfWeek = appointmentDate.getDayOfWeek();

			ResponseEntity<Appointment> responseAppointment = restTemplate.exchange(url1, HttpMethod.GET, null,
					new ParameterizedTypeReference<Appointment>() {
					});
			Appointment appointments = responseAppointment.getBody();

			Long doctorId = appointments.getDoctorId().longValue();
			System.out.println("doctorid: " + doctorId);

			UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url).queryParam("dayOfWeek", dayOfWeek)
					.queryParam("doctorId", doctorId);

			System.out.println("Generated URL: " + uriBuilder.toUriString());

			HttpHeaders headers = new HttpHeaders();
			headers.set("Content-Type", "application/json");
			HttpEntity<Void> request = new HttpEntity<>(headers);

			ResponseEntity<Map<String, Boolean>> response = restTemplate.exchange(uriBuilder.toUriString(),
					HttpMethod.GET, request, new ParameterizedTypeReference<Map<String, Boolean>>() {
					});

			Map<String, Boolean> availableTimeSlots = response.getBody();
			System.out.println("Available Time Slots (Raw): " + availableTimeSlots);

			LocalDate today = LocalDate.now();

			// Create a list of the next 7 days and format them as 'MMM dd, yyyy'
			List<String> nextSevenDaysFormatted = new ArrayList<>();
			DateTimeFormatter formatterNew = DateTimeFormatter.ofPattern("MMM dd, yyyy");
			for (int i = 0; i < 7; i++) {
				nextSevenDaysFormatted.add(today.plusDays(i).format(formatterNew));
			}

			if (availableTimeSlots != null) {
				List<String> filteredAndSortedTimeSlots = availableTimeSlots.entrySet().stream()
						.filter(Map.Entry::getValue) // Keep only available slots (value = true)
						.map(Map.Entry::getKey) // Extract the time strings
						.sorted() // Sort the times in order
						.collect(Collectors.toList());

				System.out.println("Filtered and Sorted Time Slots: " + filteredAndSortedTimeSlots);

				model.addAttribute("availableTimeslots", filteredAndSortedTimeSlots);
				model.addAttribute("patientId", patientId);
				model.addAttribute("doctorId", doctorId);
				model.addAttribute("appointmentId", appointmentId);
				model.addAttribute("nextSevenDaysFormatted", nextSevenDaysFormatted);
				model.addAttribute("rescheduleAppointmentDTO", rescheduleAppointmentDTO);

				return "appointment-reschedule";
			} else {
				model.addAttribute("errorMessage", "No available time slots found. Please try again later.");
				return "error";
			}
		} catch (HttpClientErrorException e) {
			e.printStackTrace();
			model.addAttribute("errorMessage", e.getResponseBodyAsString());
			return "error";
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("errorMessage", "Failed to fetch available time slots. Please try again.");
			return "error";
		}
	}

	@PostMapping("/{patientId}/appointments/reschedule/{appointmentId}")
	public String rescheduleAppointment(@PathVariable int patientId, @PathVariable int appointmentId,
			@ModelAttribute RescheduleAppointmentDTO rescheduleAppointmentDTO, Model model) {
		String url = baseUrl + "/patient/" + patientId + "/appointments/reschedule/" + appointmentId;
		String url1 = baseUrl + "/patient/" + patientId + "/appointments/" + appointmentId;
		String url2 = baseUrl + "/api/availability/schedule";
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Content-Type", "application/json");
			System.out.println("Appointment new date: " + rescheduleAppointmentDTO.getNewDate());
			System.out.println("Appointment new time: " + rescheduleAppointmentDTO.getNewTime());
			String firstDate = rescheduleAppointmentDTO.getNewDate();
			// Define formatter for input format
	        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

	        // Define formatter for output format
	        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	        // Convert string to LocalDate
	        LocalDate date = LocalDate.parse(firstDate, inputFormatter);

	        // Convert LocalDate to required string format
	        String formattedDate = date.format(outputFormatter);
	        rescheduleAppointmentDTO.setNewDate(formattedDate);
	        System.out.println("Appointment new date: " + rescheduleAppointmentDTO.getNewDate());
	        String time12Hour = rescheduleAppointmentDTO.getNewTime(); // Example: "06:00pm"

	        DateTimeFormatter inputFormatterTime = DateTimeFormatter.ofPattern("hh:mma");
			
	        DateTimeFormatter outputFormatterTime = DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSS");
			
	        LocalTime parsedTime = null;
			try{
				parsedTime= LocalTime.parse(time12Hour, inputFormatterTime);
			}catch(DateTimeParseException e){
				try{
					DateTimeFormatter inputFormatterTime2 = DateTimeFormatter.ofPattern("h:mma");
				parsedTime = LocalTime.parse(time12Hour, inputFormatterTime2);
				} catch (DateTimeParseException e1){
					model.addAttribute("errorMessage", "Invalid time format. Please use hh:mma for the time.");
					return "error";
				}

			}
	        String formattedTime = parsedTime.format(outputFormatterTime);

	        // Set the formatted time back into the DTO
	        rescheduleAppointmentDTO.setNewTime(formattedTime);
	        System.out.println("Appointment new time: " + rescheduleAppointmentDTO.getNewTime());
	        
			HttpEntity<RescheduleAppointmentDTO> request = new HttpEntity<>(rescheduleAppointmentDTO, headers);

			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
			
			if (response.getStatusCode().is2xxSuccessful()) {
				ResponseEntity<Appointment> responseAppointment = restTemplate.exchange(url1, HttpMethod.GET, null,
						new ParameterizedTypeReference<Appointment>() {
						});
				Appointment appointments = responseAppointment.getBody();

				Long doctorId = appointments.getDoctorId().longValue();
				System.out.println("doctorid: " + doctorId); 
				// Convert the appointmentDate from "MMM dd, yyyy" format to LocalDate
				DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH);
				LocalDate appointmentDate = LocalDate.parse(firstDate, dateFormatter);
				System.out.println(appointmentDate);
				DayOfWeek dayOfWeek = appointmentDate.getDayOfWeek(); // Get DayOfWeek from date
				System.out.println(dayOfWeek);
				String timeSlot = time12Hour;
				System.out.println(timeSlot);

				UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url2)
						.queryParam("dayOfWeek", dayOfWeek).queryParam("doctorId", doctorId)
						.queryParam("timeSlot", timeSlot);

				System.out.println("Generated URL: " + uriBuilder.toUriString());

				// Creating headers for the request
				HttpHeaders secondaryHeaders = new HttpHeaders();
				secondaryHeaders.set("Content-Type", "application/json");

				// Creating the HttpEntity with just the headers (no body)
				HttpEntity<Void> secondaryRequest = new HttpEntity<>(secondaryHeaders);

				// Sending the POST request with the generated URL
				ResponseEntity<String> secondaryResponse = restTemplate.exchange(uriBuilder.toUriString(),
						HttpMethod.POST, secondaryRequest, String.class);
				// Step 3: Check the result of the secondary request
				if (secondaryResponse.getStatusCode().is2xxSuccessful()) {
					model.addAttribute("successMessage", "Appointment rescheduled successfully!");
					return "redirect:/patient/" + patientId + "/appointments"; // Redirect to appointment management page
				} else {
					model.addAttribute("errorMessage", "Failed to update availability. Please try again.");
					return "error";
				}
				
			} else {
				model.addAttribute("errorMessage", "Failed to reschedule the appointment. Please try again.");
				return "error";
			}
		} catch (HttpClientErrorException e) {
			// model.addAttribute("errorMessage", "Invalid input or scheduling conflict.
			// Please check your details.");
			String errorMessage = e.getResponseBodyAsString();
			model.addAttribute("errorMessage", errorMessage);
			return "error";
		} catch (Exception e) {
			model.addAttribute("errorMessage", "Failed to schedule the appointment. Please try again.");
			return "error";
		}

		
	}

	@GetMapping("/dailyAppointments")
    public String viewAppointmentsByDate(@RequestParam(value = "date", required = false) String date,
            HttpSession session, Model model) {
        if (role == null || !role.equalsIgnoreCase("ADMIN"))
            return "unauthorized";
        LocalDate selectedDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        String url = baseUrl+"/api/admin/date/" + selectedDate;
        try {
            ResponseEntity<List<AppointmentDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<AppointmentDTO>>() {
                    });
            List<AppointmentDTO> appointments = response.getBody();
            model.addAttribute("appointments", appointments);
            model.addAttribute("selectedDate", selectedDate);
        } catch (HttpStatusCodeException e) {
            model.addAttribute("errorMessage", "Unable to fetch daily appointments: " + e.getResponseBodyAsString());
            return "statusPage";
        }

        return "dailyAppointments";
    }

	@GetMapping("/no-show-patients")
    public String viewNoShowAppointments(@ModelAttribute("selectSearchDate") SelectSearchDate searchDate, HttpSession session, Model model) {
        if (role == null || !role.equalsIgnoreCase("admin"))
            return "unauthorized";

			if(searchDate.getStartDate()==null && searchDate.getEndDate()==null) {
				// model.addAttribute("errorMessage", "Select Date.");
				return "patientNoShowReport";
			}

			if(searchDate.getStartDate()==null){
				model.addAttribute("errorMessage", "Select Start Date.");
				model.addAttribute("selectSearchDate", searchDate);
				return "patientNoShowReport";
			} 
			if(searchDate.getEndDate()==null){
				model.addAttribute("errorMessage", "Select end Date.");
				model.addAttribute("selectSearchDate", searchDate);
				return "patientNoShowReport";
			}

        String url = baseUrl+ "/api/admin/no-show/"+searchDate.getStartDate()+"/"+searchDate.getEndDate();

        try {
            ResponseEntity<List<Patient>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
              null,
                    new ParameterizedTypeReference<List<Patient>>() {
                    });
                    System.out.println("hello");
            List<Patient> noShowAppointments = response.getBody();
			System.out.println(noShowAppointments);
            model.addAttribute("noShowAppointments", noShowAppointments);
			model.addAttribute("selectSearchDate", searchDate);
			return "patientNoShowReport";
        } catch (HttpStatusCodeException e) {
            model.addAttribute("errorMessage", "Unable to fetch no-show appointments: " + e.getResponseBodyAsString());
        }
		return "statusPage";
    }

}
