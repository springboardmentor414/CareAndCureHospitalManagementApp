package com.cac.controller;

import com.cac.exception.UserNotFoundException;
import com.cac.dto.CancelAppointmentRequest;
import com.cac.dto.RescheduleDTO;
// import com.careandcure.cac.dto.RescheduleRequest;
import com.cac.model.Appointment;
import com.cac.model.Patient;
import com.cac.model.Doctor;
import com.cac.service.AppointmentService;
import com.cac.service.PatientService;
import com.cac.service.DoctorService;

import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "${frontend.url:http://localhost:8081}")
@RequestMapping("/patient/{patientId}/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    // Get all appointments for a specific patient
    @GetMapping
    public ResponseEntity<List<Appointment>> getAppointmentsByPatientId(@PathVariable int patientId) {
        List<Appointment> appointments = appointmentService.getAppointmentsByPatientId(patientId);
        return appointments.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(appointments);
    }



    // Get appointments for a specific doctor
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Appointment>> getAppointmentsForDoctor(@PathVariable int patientId,
                                                                      @PathVariable int doctorId) {
        List<Appointment> appointments = appointmentService.getAppointmentsForDoctor(doctorId);
        return appointments.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(appointments);
    }

    // Create a new appointment
    @PostMapping(value = "/schedule", consumes = "application/json", produces = "application/json")
public ResponseEntity<?> createAppointment(@PathVariable int patientId, @RequestBody @Validated Appointment appointment) throws UserNotFoundException, MessagingException {
    Patient patient = patientService.getPatientById(patientId);
    if (patient == null) {
        throw new IllegalArgumentException("Patient with ID " + patientId + " not found");
    }

    Doctor doctor = doctorService.getDoctorById(appointment.getDoctorId());
    if (doctor == null) {
        return ResponseEntity.badRequest().body("Doctor with ID " + appointment.getDoctorId() + " not found.");
    }

    boolean isAvailable = appointmentService.isTimeSlotAvailable(appointment.getDoctorId(),
            appointment.getAppointmentDate(), appointment.getAppointmentTime());
    if (!isAvailable) {
        return ResponseEntity.badRequest().body("The selected time slot is already booked. Please choose another time.");
    }



    appointment.setPatient(patient);
    appointment.setStatus("Scheduled");
    appointment.setDoctor(doctor);
    appointment.setDoctorName(doctor.getDoctorName());
    Appointment savedAppointment = appointmentService.createAppointment(appointment);
     System.out.println(savedAppointment.getStatus());
    return ResponseEntity.status(HttpStatus.CREATED).body(savedAppointment);
}

@GetMapping("/{id:\\d+}")
public ResponseEntity<Appointment> getAppointmentById(@PathVariable int patientId, @PathVariable int id) {
    Optional<Appointment> appointment = appointmentService.getAppointmentById(id);
    return appointment.map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
}




   // Update an appointment
@PutMapping("/{id}")
public ResponseEntity<?> updateAppointment(@PathVariable int patientId, @PathVariable int id,
                                           @RequestBody Appointment updatedAppointment) throws MessagingException {
    Patient patient = patientService.getPatientById(patientId);
    if (patient == null) {
        throw new IllegalArgumentException("Patient with ID " + patientId + " not found");
    }

    Appointment existingAppointment = appointmentService.getAppointmentById(id)
            .orElseThrow(() -> new IllegalArgumentException("Appointment with ID " + id + " not found"));

    // Check if doctorId is valid in the request body
    if (updatedAppointment.getDoctorId() == null) {
        return ResponseEntity.badRequest().body("Doctor information is missing or invalid.");
    }

    // Fetch the doctor using doctorId
    Doctor doctor = doctorService.getDoctorById(updatedAppointment.getDoctorId());
    if (doctor == null) {
        return ResponseEntity.badRequest().body("Doctor with ID " + updatedAppointment.getDoctorId() + " not found.");
    }

    // Update the appointment with the correct doctor
    existingAppointment.setDoctor(doctor);

    // Update other appointment details
    existingAppointment.setPatient(patient);
    existingAppointment.setAppointmentDate(updatedAppointment.getAppointmentDate());
    existingAppointment.setAppointmentTime(updatedAppointment.getAppointmentTime());
    existingAppointment.setReason(updatedAppointment.getReason());

    Appointment updated = appointmentService.updateAppointment(existingAppointment);
    return ResponseEntity.ok(updated);
}



    // Cancel an appointment
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable int patientId,
            @RequestBody @Validated CancelAppointmentRequest request) throws UserNotFoundException, MessagingException {
        // Validate the appointment exists
        Appointment appointment = appointmentService.getAppointmentById(request.getAppointmentId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Appointment with ID " + request.getAppointmentId() + " not found for patient " + patientId));

        // Additional validation: Ensure the appointment belongs to the patient
        if (appointment.getPatient().getPatientId() != patientId) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Appointment with ID " + request.getAppointmentId() + " does not belong to patient " + patientId);
        }

        // Cancel the appointment
        appointmentService.cancelAppointment(request.getAppointmentId(), request.getReasonOfCancellation());

        // Return success response
        return ResponseEntity.ok("Appointment with ID " + request.getAppointmentId() + " has been cancelled for patient " + patientId +
                " for the following reason: " + request.getReasonOfCancellation());
    }

    // Delete an appointment
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAppointment(@PathVariable int patientId, @PathVariable int id) {
        appointmentService.deleteAppointmentById(id);
        return ResponseEntity.ok("Appointment with ID " + id + " deleted successfully for patient " + patientId);
    }

    // Check if a time slot is available
    @GetMapping("/check-availability")
    public ResponseEntity<Boolean> checkTimeSlotAvailability(@PathVariable int patientId,
                                                              @RequestParam int doctorId,
                                                              @RequestParam String date,
                                                              @RequestParam String time) {
        LocalDate appointmentDate = LocalDate.parse(date);
        LocalTime appointmentTime = LocalTime.parse(time);
        boolean isAvailable = appointmentService.isTimeSlotAvailable(doctorId, appointmentDate, appointmentTime);
        return ResponseEntity.ok(isAvailable);
    }


    @PutMapping("/reschedule/{appointmentId}")
    public ResponseEntity<?> rescheduleAppointment(
            @PathVariable int appointmentId,
            @RequestBody RescheduleDTO rescheduleRequest) throws MessagingException, UserNotFoundException {

        // Parse the new date and time from the request
        LocalDate rescheduleDate = LocalDate.parse(rescheduleRequest.getNewDate());
        LocalTime rescheduleTime = LocalTime.parse(rescheduleRequest.getNewTime());

        Appointment appointment = appointmentService.getAppointmentById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Appointment with ID " + appointmentId + " not found."));

            boolean isAvailable = appointmentService.isTimeSlotAvailable(appointment.getDoctor().getDoctorId(),
                    rescheduleDate, rescheduleTime);
                if (!isAvailable) {
                    return ResponseEntity.badRequest().body("The selected time slot is already booked. Please choose another time.");
                }
            
        if(rescheduleDate.isBefore(LocalDate.now())) { 
            return ResponseEntity.badRequest().body("Appointment date cannot be in the past.");
        }

        // Reschedule the appointment
        Appointment updatedAppointment = appointmentService.rescheduleAppointment(appointmentId, rescheduleDate, rescheduleTime);

        // Return the updated appointment
        return ResponseEntity.ok(updatedAppointment);
    }
    
}



