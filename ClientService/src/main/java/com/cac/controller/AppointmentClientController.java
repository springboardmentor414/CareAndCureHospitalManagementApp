package com.cac.controller;

import com.cac.model.AppointmentDTO;
import com.cac.model.CancelAppointmentDTO;
import com.cac.model.DoctorDTO;
import com.cac.model.ScheduleAppointmentDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/patient")
public class AppointmentClientController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/{patientId}/appointments")
    public String showAppointmentsForPatient(@PathVariable Long patientId, Model model) {
        String url = "http://localhost:8080/patient/" + patientId + "/appointments";
        try {
            ResponseEntity<List<AppointmentDTO>> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                null, 
                new ParameterizedTypeReference<List<AppointmentDTO>>() {}
            );
            List<AppointmentDTO> appointments = response.getBody();

            if (appointments == null || appointments.isEmpty()) {
                model.addAttribute("message", "No appointments found.");
            } else {
                model.addAttribute("patientId", patientId);
                model.addAttribute("appointments", appointments);
            }
            return "appointments";
        } catch (ResourceAccessException e) {
            // model.addAttribute("errorMessage", "Unable to fetch appointments. Please check the backend service.");
            String errorMessage = e.getMessage();
            model.addAttribute("errorMessage",  errorMessage);
            return "error";
        } catch (Exception e) {
            // model.addAttribute("errorMessage", "An unexpected error occurred while fetching appointments.");
            String errorMessage = e.getMessage();
            model.addAttribute("errorMessage",  errorMessage);
            return "error";
        }
    }

    @GetMapping("/{patientId}/appointments/schedule")
    public String showScheduleAppointment(@PathVariable Long patientId, Model model) {
    	String url = "http://localhost:8080/api/doctors";
        try {
            // Make GET request to fetch doctors
            ResponseEntity<List<DoctorDTO>> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                null, 
                new ParameterizedTypeReference<List<DoctorDTO>>() {}
            );
            
            // Get the list of doctors from the response
            List<DoctorDTO> doctors = response.getBody();

            // Add attributes to the model
            model.addAttribute("scheduleAppointmentDTO", new ScheduleAppointmentDTO());
            model.addAttribute("patientId", patientId);
            model.addAttribute("doctors", doctors); // Add the list of doctors to the model

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to fetch doctors. Please try again.");
            return "error";
        }
        
        // Return the view
        return "appointment-schedule";
    }

    @PostMapping("/{patientId}/appointments/schedule")
    public String scheduleAppointment(@PathVariable Long patientId, 
                                       @ModelAttribute ScheduleAppointmentDTO scheduleAppointmentDTO, 
                                       Model model) {
        String url = "http://localhost:8080/patient/" + patientId + "/appointments/schedule";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<ScheduleAppointmentDTO> request = new HttpEntity<>(scheduleAppointmentDTO, headers);

            ResponseEntity<AppointmentDTO> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                AppointmentDTO.class
            );

            AppointmentDTO createdAppointment = response.getBody();

            if (createdAppointment != null) {
            	model.addAttribute("patientId", patientId);
                model.addAttribute("appointment", createdAppointment);
                return "appointment-confirmation";
            } else {
                model.addAttribute("errorMessage", "Appointment could not be scheduled. Please try again.");
                return "error";
            }
        } catch (HttpClientErrorException e) {
            // model.addAttribute("errorMessage", "Invalid input or scheduling conflict. Please check your details.");
            String errorMessage = e.getResponseBodyAsString();
            model.addAttribute("errorMessage",  errorMessage);
            return "error";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to schedule the appointment. Please try again.");
            return "error";
        }
    }






    @GetMapping("/{patientId}/appointments/cancel/{appointmentId}")
    public String showCancelAppointment(@PathVariable Long patientId, 
                                         @PathVariable Long appointmentId, 
                                         Model model) {
        CancelAppointmentDTO cancelAppointmentDTO = new CancelAppointmentDTO();
        cancelAppointmentDTO.setAppointmentId(appointmentId); // Pre-fill the appointment ID
        model.addAttribute("cancelAppointmentDTO", cancelAppointmentDTO);
        model.addAttribute("patientId", patientId);
        return "appointment-cancel";
    }

    

    @PostMapping("/{patientId}/appointments/cancel/{appointmentId}")
public String cancelAppointment(@PathVariable Long patientId,
                                 @PathVariable Long appointmentId,
                                 @ModelAttribute CancelAppointmentDTO cancelAppointmentDTO,
                                 Model model) {
    System.out.println("Cancel Request Received for Patient ID: " + patientId);
    System.out.println("Appointment ID: " + appointmentId);
    System.out.println("Reason: " + cancelAppointmentDTO.getReasonOfCancellation());

    String url = "http://localhost:8080/patient/" + patientId + "/appointments/cancel";
    // String url = "http://localhost:8081/patient/" + patientId + "/appointments/cancel/" + appointmentId;
    try {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<CancelAppointmentDTO> request = new HttpEntity<>(cancelAppointmentDTO, headers);

        ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            request,
            String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Cancellation Successful");
            return "appointment-cancel-confirmation";
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
    public String showRescheduleAppointment(@PathVariable Long patientId, 
                                             @PathVariable Long appointmentId, 
                                             Model model) {
        model.addAttribute("patientId", patientId);
        model.addAttribute("appointmentId", appointmentId);
        return "appointment-reschedule";
    }
    
    @PostMapping("/{patientId}/appointments/reschedule/{appointmentId}")
    public String rescheduleAppointment(@PathVariable Long patientId, 
                                         @PathVariable Long appointmentId, 
                                         @RequestParam("newDate") String newDate, 
                                         @RequestParam("newTime") String newTime, 
                                         Model model) {
        String url = "http://localhost:8080/patient/" + patientId + "/appointments/reschedule/" + appointmentId;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            Map<String, String> requestPayload = new HashMap<>();
            requestPayload.put("newDate", newDate);
            requestPayload.put("newTime", newTime);
    
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestPayload, headers);
    
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                request,
                String.class
            );
    
            if (response.getStatusCode().is2xxSuccessful()) {
                model.addAttribute("successMessage", "Appointment rescheduled successfully!");
                return "redirect:/patient/" + patientId + "/appointments";  // Redirect to appointment management page
            } else {
                model.addAttribute("errorMessage", "Failed to reschedule the appointment. Please try again.");
                return "error";
            }
        } catch (HttpClientErrorException e) {
            // model.addAttribute("errorMessage", "Invalid input or scheduling conflict. Please check your details.");
            String errorMessage = e.getResponseBodyAsString();
            model.addAttribute("errorMessage",  errorMessage);
            return "error";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to schedule the appointment. Please try again.");
            return "error";
        }
    }
}
