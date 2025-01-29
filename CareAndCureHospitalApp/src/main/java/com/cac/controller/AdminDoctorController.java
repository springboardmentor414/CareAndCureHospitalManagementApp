package com.cac.controller;

import com.cac.exception.*;
import com.cac.model.Appointment;
import com.cac.model.Doctor;
import com.cac.service.AppointmentService;
import com.cac.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:8081")
public class AdminDoctorController {

    @Autowired
    private DoctorService doctorService;
    
    @Autowired
    private AppointmentService appointmentService;

    /**
     * Add a new doctor to the system.
     *
     * @param doctor the doctor details
     * @return the saved doctor entity
     */
 
 
    @PostMapping("/addDoctor")
    public ResponseEntity<?> addDoctor(@Valid @RequestBody Doctor doctor, BindingResult result) {
        if (result.hasErrors()) {
            // Collect field-specific validation errors into a map
            Map<String, String> errors = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            FieldError::getDefaultMessage
                    ));

            // Return validation errors with a BAD_REQUEST status
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        // Save doctor details to the database
        Doctor savedDoctor = doctorService.addDoctor(doctor);
        return new ResponseEntity<>(savedDoctor, HttpStatus.CREATED);
    }
    
    // I have commented the eblow portion beacsue of the error in the method name.
    
//    @GetMapping("/appointments/{doctorId}")
//    public ResponseEntity<List<Appointment>> getAppointmentsByDoctorId(@PathVariable int doctorId) {
//        List<Appointment> appointments = appointmentService.getAppointmentsByDoctorId(doctorId);
//        if (appointments.isEmpty()) {
//            return ResponseEntity.noContent().build();
//        }
//        return ResponseEntity.ok(appointments);
//    }
    
    @PutMapping("/doctor/{doctorId}")
    public ResponseEntity<Doctor> updateDoctor(@Valid @RequestBody Doctor doctor, @PathVariable int doctorId) {
        // Fetch the existing doctor by ID
       
        Doctor updatedDoctor = doctorService.updateDoctor(doctorId,doctor);

        return new ResponseEntity<>(updatedDoctor, HttpStatus.OK);
    } 
    
    
    
    
 // The below portion also i have commented.   
    
    
//    @PostMapping("/add")
//    public ResponseEntity<?> addAppointment(@Valid @RequestBody Appointment appointment, BindingResult result) {
//        if (result.hasErrors()) {
//            // Collect field-specific validation errors into a map
//            Map<String, String> errors = result.getFieldErrors().stream()
//                    .collect(Collectors.toMap(
//                            FieldError::getField,
//                            FieldError::getDefaultMessage
//                    ));
//
//            // Return validation errors with a BAD_REQUEST status
//            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
//        }
//
//        // Save appointment details to the database
//        Appointment savedAppointment = appointmentService.addAppointment(appointment);
//        return new ResponseEntity<>(savedAppointment, HttpStatus.CREATED);
//    }

    /**
     * Get a list of all active doctors.
     *
     * @return list of active doctors
     */
    @GetMapping("/showDoctors")
    public ResponseEntity<List<Doctor>> getActiveDoctorss() {
        List<Doctor> doctors = doctorService.getAllDoctors(); // Fetch all doctors
        List<Doctor> activeDoctors = doctors.stream()
                .filter(Doctor::getStatus) // Filter only active doctors
                .collect(Collectors.toList());
        return ResponseEntity.ok(activeDoctors);
    }
    
    @GetMapping("/doctors")
    public List<Doctor> getDoctors(@RequestParam(required = false) String specialization) {
        if (specialization != null && !specialization.isEmpty()) {
            // Filter doctors by specialization
            return doctorService.findDoctorBySpecialization(specialization);
        } else {
            // Show all active doctors if no specialization is provided
            return doctorService.getAllDoctors().stream()
                    .filter(Doctor::getStatus)
                    .collect(Collectors.toList());
        }
    }
    
    @GetMapping("/doctors/by-name")
    public List<Doctor> searchDoctorByName(@RequestParam(value = "doctorName", required = false, defaultValue = "") String name) {
        return doctorService.searchDoctorsByName(name);
    }

    /**
     * Disable a doctor by setting their status to inactive.
     *
     * @param doctorId the ID of the doctor to disable
     * @return a response message indicating success or failure
     */
    @PostMapping("/disable/{doctorId}")
    public ResponseEntity<String> disableDoctor(@PathVariable int doctorId) {
        Doctor doctor = doctorService.getDoctorById(doctorId); // Fetch the doctor by ID

        if (doctor == null) {
            throw new UserNotFoundException("Doctor with ID " + doctorId + " not found!");
        }

        doctor.setStatus(false); // Set status to inactive
        doctorService.saveDoctor(doctor); // Update the database
        return ResponseEntity.ok("Doctor disabled successfully!");
    }

    /**
     * Get a list of all active doctors (optimized query from the service).
     *
     * @return list of active doctors
     */
    @GetMapping("/viewActiveDoctors")
    public ResponseEntity<List<Doctor>> getActiveDoctors() {
        List<Doctor> activeDoctors = doctorService.getActiveDoctors(); // Use service-level filtering
        return new ResponseEntity<>(activeDoctors, HttpStatus.OK);
    }
    
    @GetMapping("/api/doctors/all")
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        List<Doctor> doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(doctors);
    }
    
    
    @GetMapping("doctor/{doctorId}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable int doctorId) {
        Doctor doctor = doctorService.getDoctorById(doctorId);
        if (doctor != null) {
            return ResponseEntity.ok(doctor);
        }
        return ResponseEntity.notFound().build();
    }

}
