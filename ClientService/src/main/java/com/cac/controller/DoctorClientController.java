package com.cac.controller;

import com.cac.model.AdminDto;
import com.cac.model.Appointment;

import com.cac.model.Doctor;
import com.cac.model.Patient;
import com.cac.model.UserInfo;

import org.springframework.http.MediaType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
//import java.awt.PageAttributes.MediaType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DoctorClientController {

    @Value("${base.url}")
    private String backendUrl;

    @Autowired
    private RestTemplate restTemplate;

    private String role = null;

    private AdminDto adminSession = null;

    private Doctor doctorSession = null;

    @ModelAttribute
    public void getRole(@SessionAttribute(name = "userRole", required = false) String userRole, Model model) {
        if (userRole != null) {
            role = userRole;
            model.addAttribute("userRole", userRole);
        }
    }

    @ModelAttribute
    public void checkAdminObject(@SessionAttribute(name = "adminObj", required = false) AdminDto adminObj) {
        if (adminObj != null) {
            this.adminSession = adminObj;
        }
    }

    @ModelAttribute
    public void checkDoctorObject(@SessionAttribute(name = "doctorObj", required = false) Doctor doctor) {
        if (doctor != null) {
            this.doctorSession = doctor;
        }
    }

    // home for doctor management
    @GetMapping("/doctorManagement")
    public String showLandingPage() {
        return "index"; // This maps to index.html
    }

    @GetMapping("/appointment")
    public String showDoctorsss(Model model) {
        try {
            // Fetch active doctors from backend API
            ResponseEntity<Doctor[]> response = restTemplate.getForEntity(backendUrl + "/showDoctors", Doctor[].class);

            // Check if the response is successful and has a body
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Doctor> doctors = Arrays.asList(response.getBody());

                model.addAttribute("doctors", doctors); // Pass doctors to the view
                return "appointment"; // This maps to show-doctors.html
            } else {
                model.addAttribute("errorMessage", "Unable to fetch doctors. Please try againlater.");
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error occurred while fetching doctors: "
                    + e.getMessage());
        }
        return "error";
    }

    @GetMapping("/admin")
    public String showAdmin() {
        return "admin"; // This maps to index.html
    }

    @GetMapping("/appointment-schedule")
    public String show() {
        return "redirect:https://calendar.google.com/calendar/u/0/r/week/2025/1/6?pli=1";
    }

    @GetMapping("/appointments/{doctorId}/filtered")
    public String showFilteredAppointments(
            @PathVariable int doctorId,
            @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Model model) {

        // Construct the backend API URL
        String backendApiUrl = backendUrl + "/appointments/" + doctorId + "/filtered?fromDate="
                + fromDate + "&toDate=" + toDate;

        // Fetch filtered appointments from the backend API
        Appointment[] filteredAppointmentsArray = restTemplate.getForObject(backendApiUrl, Appointment[].class);

        // Convert to a List
        List<Appointment> filteredAppointments = filteredAppointmentsArray != null
                ? Arrays.asList(filteredAppointmentsArray)
                : List.of();

        // Add filtered appointments to the model
        model.addAttribute("appointments", filteredAppointments);

        // Return the view name to render
        return "show-appointments";
    }

    @GetMapping("/appointments/{doctorId}")
    public String showAppointmentsByDoctorId(@PathVariable int doctorId, Model model, HttpSession session) {
        // Check if the session contains an 'admin' attribute

        if (role == null || !role.equalsIgnoreCase("admin")) {
            // If admin is not in session, redirect to the login page
            return "redirect:/adminLoginForm";
        }

        // Construct the backend API URL
        String backendApiUrl = backendUrl + "/appointments/" + doctorId;

        // Fetch appointments from the backend API
        ResponseEntity<List<Appointment>> appointmentsArray = restTemplate.exchange(backendApiUrl, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Appointment>>() {
                });

        // Add the appointments to the model
        model.addAttribute("appointments", appointmentsArray.getBody());

        // Return the view name to render
        return "show-appointments";
    }

    @GetMapping("/admin-add-doctor")
    public String showAddDoctorPage(Model model, HttpSession session) {
        // Check if the session contains an 'admin' attribute
        if (role == null || !role.equalsIgnoreCase("admin")) {
            // If admin is not in session, redirect to the login page
            return "redirect:/adminLoginForm";
        }

        // Proceed with creating a new Doctor object
        Doctor doctor = new Doctor();
        doctor.setSpecialization(""); // Ensure the field is empty
        doctor.setGender(""); // Set empty value to ensure placeholder is shown
        doctor.setSurgeon(null); // Set null value for default placeholder
        doctor.setStatus(null); // Set null value for default placeholder

        model.addAttribute("doctor", doctor);
        return "admin-add-doctor"; // This maps to admin-add-doctor.html
    }

    @PostMapping("/addDoctor")
    public String handleAddDoctor(@ModelAttribute @Valid Doctor doctor,
            BindingResult result,
            Model model,
            HttpSession session) {
        // Check if the session contains an 'admin' attribute
        if (role == null || !role.equalsIgnoreCase("admin")) {
            // If admin is not in session, redirect to the login page
            return "redirect:/adminLoginForm";
        }

        String url = backendUrl + "/addDoctor";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Doctor> request = new HttpEntity<>(doctor, headers);

        try {
            ResponseEntity<Doctor> response = restTemplate.exchange(url, HttpMethod.POST, request, Doctor.class);
            model.addAttribute("successMessage", "Doctor added successfully!");
        } catch (HttpClientErrorException e) {
            Map<String, String> errors = null;
            try {
                errors = new ObjectMapper().readValue(
                        e.getResponseBodyAsString(), new TypeReference<Map<String, String>>() {
                        });
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (errors != null) {
                for (Map.Entry<String, String> entry : errors.entrySet()) {
                    result.rejectValue(entry.getKey(), "", entry.getValue());
                }
            }

            model.addAttribute("errorMessage", "Error while adding doctor. Please correct the highlighted fields.");
        }

        return "admin-add-doctor";
    }

    @GetMapping("/showDoctorsforpats")
    public String showDoctorss(Model model) {
        try {
            // Fetch active doctors from backend API
            ResponseEntity<Doctor[]> response = restTemplate.getForEntity(backendUrl + "/showDoctors", Doctor[].class);

            // Check if the response is successful and has a body
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Doctor> doctors = Arrays.asList(response.getBody());
                model.addAttribute("doctors", doctors); // Pass doctors to the view
            } else {
                model.addAttribute("errorMessage", "Unable to fetch doctors. Please try again later.");
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error occurred while fetching doctors: " + e.getMessage());
        }

        return "show-doctors-for-pats"; // This maps to show-doctors.html
    }

    @GetMapping("/showDoctors")
    public String showDoctors(Model model, HttpSession session) {
        // Check if the session contains an 'admin' attribute
        if (role == null || !role.equalsIgnoreCase("admin")) {
            // If admin is not in session, redirect to the login page
            return "redirect:/adminLoginForm";
        }

        try {
            // Fetch active doctors from backend API
            ResponseEntity<Doctor[]> response = restTemplate.getForEntity(backendUrl + "/showDoctors", Doctor[].class);

            // Check if the response is successful and has a body
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Doctor> doctors = Arrays.asList(response.getBody());
                model.addAttribute("doctors", doctors); // Pass doctors to the view
            } else {
                model.addAttribute("errorMessage", "Unable to fetch doctors. Please try again later.");
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error occurred while fetching doctors: " + e.getMessage());
        }

        return "show-doctors"; // This maps to show-doctors.html
    }

    @GetMapping("/showAppointments")
    public String showAppointments(Model model, HttpSession session) {

        if (role == null || !role.equalsIgnoreCase("admin")) {
            // If admin is not in session, redirect to the login page
            return "redirect:/adminLoginForm";
        }

        try {
            // Fetch active doctors from backend API
            ResponseEntity<Doctor[]> response = restTemplate.getForEntity(backendUrl + "/showDoctors", Doctor[].class);

            // Check if the response is successful and has a body
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Doctor> doctors = Arrays.asList(response.getBody());
                model.addAttribute("doctors", doctors); // Pass doctors to the view
            } else {
                model.addAttribute("errorMessage", "Unable to fetch doctors. Please try again later.");
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error occurred while fetching doctors: " + e.getMessage());
        }

        return "showAppointments"; // This maps to show-appointments.html
    }

    @GetMapping("/show-doctors-for-editing")
    public String showAllDoctors(Model model) {
        String backendUrll = "http://localhost:8082/api/doctors/all"; // Backend service URL
        ResponseEntity<Doctor[]> response = restTemplate.getForEntity(backendUrll, Doctor[].class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Doctor> doctors = Arrays.asList(response.getBody());
            model.addAttribute("doctors", doctors);
        } else {
            model.addAttribute("error", "Unable to fetch the list of doctors.");
        }

        return "show-doctors-for-editing"; // Corresponding HTML template
    }

    @GetMapping("/disableDoctor/{doctorId}")
    public String disableDoctor(@PathVariable int doctorId, RedirectAttributes redirectAttributes) {
        try {
            // Send POST request to backend to disable the doctor
            String url = backendUrl + "/disable/" + doctorId;
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);

            // Add appropriate message based on the backend response
            if (response.getStatusCode().is2xxSuccessful()) {
                redirectAttributes.addFlashAttribute("message", response.getBody());
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to disable the doctor!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error occurred while disabling the doctor: " + e.getMessage());
        }

        return "redirect:/showDoctors"; // Redirect to the list of active doctors
    }

    @GetMapping("/showDoctorsforpatsbyspec")
    public String showDoctors(@RequestParam(required = false) String specialization, Model model) {
        String url = backendUrl + "/doctors";
        if (specialization != null && !specialization.isEmpty()) {
            url += "?specialization=" + specialization;
        }

        // Fetch the doctors from the backend API
        Doctor[] doctorsArray = restTemplate.getForObject(url, Doctor[].class);

        // Convert the array to a list
        List<Doctor> doctors = Arrays.asList(doctorsArray);

        model.addAttribute("doctors", doctors);
        return "show-doctorsbyspec"; // Frontend template for displaying doctors
    }

    @GetMapping("/appointment-by-issue")
    public String searchAppointmentsByIssue(@RequestParam(required = false) String reason, Model model,
            HttpSession session) {
        // Check if the session contains an 'admin' attribute
        if (role == null || !role.equalsIgnoreCase("admin")) {
            // If admin is not in session, redirect to the login page
            return "redirect:/adminLoginForm";
        }
        String url = backendUrl + "/appointments";
        if (reason != null && !reason.isEmpty()) {
            url += "?reason=" + reason;
        }

        try {
            Appointment[] appointmentsArray = restTemplate.getForObject(url, Appointment[].class);
            List<Appointment> appointments = Arrays.asList(appointmentsArray);
            model.addAttribute("appointments", appointments);
        } catch (Exception e) {
            // Log and handle the error gracefully
            e.printStackTrace();
            model.addAttribute("errorMessage", "Unable to fetch appointments for the given issue.");
            return "error-page"; // Create a simple Thymeleaf template for error display
        }

        return "appointment-by-issue";
    }

    @GetMapping("/doctor-by-name")
    public String searchDoctorByName(
            @RequestParam(value = "doctorName", required = false, defaultValue = "") String name, Model model) {
        String url = backendUrl + "/doctors/by-name?doctorName=" + name;

        // Fetch the doctor data from the backend
        Doctor[] doctorsArray = restTemplate.getForObject(url, Doctor[].class);

        // Convert the array to a list
        List<Doctor> doctors = Arrays.asList(doctorsArray);

        model.addAttribute("doctors", doctors);
        return "doctorbyname"; // Thymeleaf template
    }

    @GetMapping("/appointments/filteredByDoctor")
    public String getAppointmentsByDoctor(
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate,
            Model model,
            HttpSession session) {

        // Check if the session contains an 'admin' attribute
        if (role == null || !role.equalsIgnoreCase("admin")) {
            // If admin is not in session, redirect to the login page
            return "redirect:/adminLoginForm";
        }

        StringBuilder urlBuilder = new StringBuilder(backendUrl + "/appointments/filteredByDoctor");

        if ((fromDate != null && !fromDate.isEmpty()) || (toDate != null && !toDate.isEmpty())) {
            urlBuilder.append("?");
            if (fromDate != null && !fromDate.isEmpty()) {
                urlBuilder.append("fromDate=").append(fromDate);
            }
            if (toDate != null && !toDate.isEmpty()) {
                if (urlBuilder.charAt(urlBuilder.length() - 1) != '?') {
                    urlBuilder.append("&");
                }
                urlBuilder.append("toDate=").append(toDate);
            }
        }

        String url = urlBuilder.toString();

        try {
            List<Map<String, Object>> doctorCounts = restTemplate.getForObject(url, List.class);
            model.addAttribute("doctorCounts", doctorCounts);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to fetch data from backend: " + e.getMessage());
        }

        return "show-appointments-by-doctor";
    }

    @GetMapping("/edit/{doctorId}")
    public String editDoctor(@PathVariable int doctorId, Model model) {
        // Pass the doctor ID to the edit page; details will be fetched via JavaScript
        model.addAttribute("doctorId", doctorId);
        return "edit-doctor"; // Corresponds to the edit-doctor.html template
    }

    // doctor home page view
    @GetMapping("/doctorHomePage")
    public String doctorHomePage(HttpSession session, Model model) {

        if (role == null) {
            model.addAttribute("errorMessage", "Login Required !.");
            return "redirect:/doctorLoginForm";
        }
        if (doctorSession == null || !role.equalsIgnoreCase("doctor")) {
            model.addAttribute("errorMessage", "Not authorized to access this page.");
            return "unauthorized";
        }

        cleanUpSessionAttributes(session, model);

        Doctor doctor = (Doctor) session.getAttribute("docObj");
        model.addAttribute("doctor", doctor);
        return "doctor/doctorHomePage";
    }

    // to clean errorMessage and message of session attributes
    private void cleanUpSessionAttributes(HttpSession session, Model model) {
        String errorMessage = (String) session.getAttribute("errorMessage");
        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
            session.removeAttribute("errorMessage");
        }
        String message = (String) session.getAttribute("message");
        if (message != null) {
            model.addAttribute("message", message);
            session.removeAttribute("message");
        }
    }

}
