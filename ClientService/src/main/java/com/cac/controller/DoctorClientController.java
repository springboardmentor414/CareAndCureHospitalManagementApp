package com.cac.controller;

import com.cac.model.AppointmentDTO;
import com.cac.model.Doctor;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
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

//import java.awt.PageAttributes.MediaType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
public class DoctorClientController {

    @Value("${base.url}")
    private String backendUrl;

    @Autowired
    private RestTemplate restTemplate;

    //home for doctor management
    @GetMapping("/doctorManagement")
    public String showLandingPage() {
        return "index"; // This maps to index.html
    }

    // @GetMapping("/admin")
    // public String showAdmin() {
    //     return "admin"; // This maps to index.html
    // }

    //comment by nomit for patient and doctor integration don't uncomment until you need

    // @GetMapping("/adminPage")
    // public String showAdminPage() {
    // return "adminPage"; // This maps to index.html
    // }

    @GetMapping("/appointment")
    public String showDoctorsss(Model model) {
    try {
    // Fetch active doctors from backend API
    ResponseEntity<Doctor[]> response =
    restTemplate.getForEntity(backendUrl+"/showDoctors", Doctor[].class);
    
    // Check if the response is successful and has a body
    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null)
    {
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

    @GetMapping("/appointment-schedule")
    public String show() {
        return "redirect:https://calendar.google.com/calendar/u/0/r/week/2025/1/6?pli=1";
    }

    @GetMapping("/admin-add-doctor")
    public String showAddDoctorPage(Model model) {
        Doctor doctor = new Doctor();
        doctor.setSpecialization(""); // Ensure the field is empty
        doctor.setGender(""); // Set empty value to ensure placeholder is shown
        doctor.setSurgeon(null); // Set null value for default placeholder
        doctor.setStatus(null); // Set null value for default placeholder

        model.addAttribute("doctor", doctor);
        return "admin-add-doctor"; // This maps to admin-add-doctor.html
    }

    // @PostMapping("/addDoctor")
    // public String addDoctor(@ModelAttribute Doctor doctor, Model model) {
    // String url = backendUrl + "/addDoctor";
    // HttpHeaders headers = new HttpHeaders();
    // headers.set("Content-Type", "application/json");
    // HttpEntity<Doctor> request = new HttpEntity<>(doctor, headers);
    //
    // try {
    // ResponseEntity<Doctor> response = restTemplate.exchange(url, HttpMethod.POST,
    // request, Doctor.class);
    // model.addAttribute("successMessage", "Doctor added successfully!");
    // } catch (Exception e) {
    // model.addAttribute("errorMessage", "Error while adding doctor: " +
    // e.getMessage());
    // }
    //
    // // Redirect to adminPage
    // return "redirect:http://localhost:8081/adminPage";
    // }

    @PostMapping("/addDoctor")
    public String handleAddDoctor(@ModelAttribute Doctor doctor, BindingResult result, Model model) {
        String url = backendUrl + "/addDoctor";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Doctor> request = new HttpEntity<>(doctor, headers);

        try {
            // Sending POST request to the backend API
            ResponseEntity<Doctor> response = restTemplate.exchange(url, HttpMethod.POST, request, Doctor.class);

            // If successful, add success message
            model.addAttribute("successMessage", "Doctor added successfully!");

        } catch (HttpClientErrorException e) {
            // Handle validation errors from the backend
            Map<String, String> errors = null;
            try {
                errors = new ObjectMapper().readValue(
                        e.getResponseBodyAsString(), new TypeReference<Map<String, String>>() {
                        });
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // Map backend validation errors to the BindingResult
            if (errors != null) {
                for (Map.Entry<String, String> entry : errors.entrySet()) {
                    result.rejectValue(entry.getKey(), "", entry.getValue());
                }
            }

            // Add an error message to the model
            model.addAttribute("errorMessage", "Error while adding doctor. Please correct the highlighted fields.");
        }

        // Return the same form view to display errors or success message
        return "admin-add-doctor";
    }

    @GetMapping("/appointments/{doctorId}")
    public String showAppointmentsByDoctorId(@PathVariable int doctorId, Model model) {
        // Construct the backend API URL
        String backendApiUrl = backendUrl + "/appointments/" + doctorId;

        // Fetch appointments from the backend API
        AppointmentDTO[] appointmentsArray = restTemplate.getForObject(backendApiUrl, AppointmentDTO[].class);

        // Convert the array to a List for easier processing
        List<AppointmentDTO> appointments = appointmentsArray != null ? Arrays.asList(appointmentsArray) : List.of();

        // Add the appointments to the model
        model.addAttribute("appointments", appointments);

        // Return the view name to render
        return "show-appointments";
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
    public String showDoctors(Model model) {
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
    public String showDoctorssss(Model model) {
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

        return "showAppointments"; // This maps to show-doctors.html
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

    @GetMapping("/edit/{doctorId}")
    public String editDoctor(@PathVariable int doctorId, Model model) {
        // Pass the doctor ID to the edit page; details will be fetched via JavaScript
        model.addAttribute("doctorId", doctorId);
        return "edit-doctor"; // Corresponds to the edit-doctor.html template
    }

    @GetMapping("/disableDoctor/{doctorId}")
    public String disableDoctor(@PathVariable int doctorId, RedirectAttributes redirectAttributes) {
        try {
            // Send POST request to backend to disable the doctor
            String url = backendUrl + "/disable/" + doctorId;
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);

            // Add appropriate message based on the backend response
            if (response.getStatusCode().is2xxSuccessful()) {
                String backendMessage = response.getBody();
                if (backendMessage.contains("successfully")) {
                    redirectAttributes.addFlashAttribute("message", backendMessage);
                } else {
                    redirectAttributes.addFlashAttribute("warning", backendMessage); // Partial success
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to disable the doctor! Please try again.");
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

    // @GetMapping("/showDoctors")
    // public String showActiveDoctors(Model model) {
    // String url = backendUrl + "/viewActiveDoctors";
    // try {
    // ResponseEntity<List<Doctor>> response = restTemplate().exchange(
    // url,
    // HttpMethod.GET,
    // null,
    // new ParameterizedTypeReference<List<Doctor>>() {}
    // );
    // model.addAttribute("doctors", response.getBody());
    // } catch (Exception e) {
    // model.addAttribute("errorMessage", "Unable to fetch active doctors: " +
    // e.getMessage());
    // return "statuspage";
    // }
    //
    // return "show-doctors";
    // }

    // @PostMapping("/disableDoctor")
    // public String disableDoctor(@RequestParam("doctorId") int doctorId, Model
    // model) {
    // String url = backendUrl + "/disableDoctor/" + doctorId;
    //
    // try {
    // restTemplate().exchange(url, HttpMethod.PUT, null, Void.class);
    // model.addAttribute("message", "Doctor disabled successfully!");
    // } catch (Exception e) {
    // model.addAttribute("errorMessage", "Error while disabling doctor: " +
    // e.getMessage());
    // }
    //
    // return "redirect:/showDoctors";
    // }
}
