package com.cac.controller;

import com.cac.exception.DoctorNotFoundException;
import com.cac.model.UserInfo;

import com.cac.model.Appointment;
import com.cac.model.Doctor;
import com.cac.service.UserService;
import com.cac.service.AppointmentService;
import com.cac.service.DoctorService;
import com.cac.service.EmailService;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class AdminDoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @GetMapping("/appointments/{doctorId}/filtered")
    public ResponseEntity<List<Appointment>> getFilteredAppointments(
            @PathVariable int doctorId,
            @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        // Fetch the filtered appointments using the service
        List<Appointment> filteredAppointments = appointmentService.getFilteredAppointments(doctorId, fromDate, toDate);

        if (filteredAppointments.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(filteredAppointments);
    }

    //
    @PostMapping("/addDoctor")
    public ResponseEntity<?> addDoctor(@Valid @RequestBody Doctor doctor, BindingResult result) {
        // Validate input
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        // Save doctor details
        Doctor savedDoctor = doctorService.addDoctor(doctor);

        userService.addDoctor(savedDoctor.getUsername(), savedDoctor.getPassword());

        // Send email notification
        String subject = "Welcome to the Hospital Directory";
        String body = "Dear Dr. " + savedDoctor.getDoctorName() + ",\n\n"
                + "You have been successfully added to the hospital directory.\n\n"
                + "Regards,\nHospital Management Team";
        try {
            emailService.sendEmail(savedDoctor.getEmailId(), subject, body);
        } catch (MessagingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Return the saved doctor with a CREATED status
        return new ResponseEntity<>(savedDoctor, HttpStatus.CREATED);
    }

    //
    //

    @GetMapping("/appointments/{doctorId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByDoctorId(@PathVariable int doctorId) {
        List<Appointment> appointments = appointmentService.getAppointmentsByDoctorId(doctorId);
        if (appointments.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(appointments);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addAppointment(@Valid @RequestBody Appointment appointment, BindingResult result) {
        if (result.hasErrors()) {
            // Collect field-specific validation errors into a map
            Map<String, String> errors = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            FieldError::getDefaultMessage));

            // Return validation errors with a BAD_REQUEST status
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        // Save appointment details to the database
        Appointment savedAppointment = appointmentService.addAppointment(appointment);
        return new ResponseEntity<>(savedAppointment, HttpStatus.CREATED);
    }

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
    public List<Doctor> searchDoctorByName(
            @RequestParam(value = "doctorName", required = false, defaultValue = "") String name) {
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
        // Fetch the doctor by ID
        Doctor doctor = doctorService.getDoctorById(doctorId);

        // Check if the doctor exists
        if (doctor == null) {
            throw new DoctorNotFoundException("Doctor with ID " + doctorId + " not found!");
        }

        // Set status to inactive
        doctor.setStatus(false);

        // Update the database
        doctorService.saveDoctor(doctor);

        // Send email notification
        String subject = "Hospital Directory Update";
        String body = "Dear Dr. " + doctor.getDoctorName() + ",\n\n"
                + "This is to inform you that you are no longer associated with the hospital.\n\n"
                + "Regards,\nHospital Management Team";

        try {
            emailService.sendEmail(doctor.getEmailId(), subject, body);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Doctor disabled successfully, but failed to send notification email.");
        }

        return ResponseEntity.ok("Doctor disabled successfully and notified via email!");
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

    @GetMapping("/appointments")
    public List<Appointment> getAppointments(@RequestParam(required = false) String reason) {
        if (reason != null && !reason.isEmpty()) {
            // Filter appointments by issue faced
            return appointmentService.findAppointmentsByIssueFaced(reason);
        } else {
            // Show all appointments if no issue is specified
            return appointmentService.getAllAppointments();
        }
    }

    @GetMapping("/appointments/filteredByDoctor")
    public List<Map<String, Object>> getAppointmentsByDoctor(
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate) {

        LocalDate from = null, to = null;
        try {
            if (fromDate != null && !fromDate.isEmpty()) {
                from = LocalDate.parse(fromDate);
            }
            if (toDate != null && !toDate.isEmpty()) {
                to = LocalDate.parse(toDate);
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Please use YYYY-MM-DD.");
        }

        return appointmentService.getAppointmentsCountByDoctor(from, to);
    }

    // @PostMapping("/api/admin/login")
    // public ResponseEntity<?> login(@RequestBody UserInfo user, HttpSession
    // session) throws InvalidEntityException {
    // UserInfo authenticatedUser = userService.authenticate(user);

    // // if (authenticatedUser != null) {
    // // // Create session and store user info
    // // session.setAttribute("user", authenticatedUser);
    // // System.out.println("User added to session: " + authenticatedUser);
    // // return ResponseEntity.ok(authenticatedUser); // Send successful response

    // // } else {
    // // // Return invalid credentials response
    // // return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
    // // .body("Invalid credentials");
    // // }

    // }

    @PostMapping("/api/admin/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        // Invalidate the backend session
        session.invalidate();
        // Return a response indicating logout success
        return ResponseEntity.ok("Backend logout successful");
    }

    @GetMapping("doctor/{doctorId}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable int doctorId) {
        Doctor doctor = doctorService.getDoctorById(doctorId);
        if (doctor != null) {
            return ResponseEntity.ok(doctor);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/doctor/{doctorId}")
    public ResponseEntity<?> updateDoctor(@Valid @RequestBody Doctor updatedDoctor, @PathVariable int doctorId,
            BindingResult result) {
        // Validate input
        if (result.hasErrors()) {
            // Collect field-specific validation errors into a map
            Map<String, String> errors = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            FieldError::getDefaultMessage));

            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        // Fetch the existing doctor by ID
        Doctor existingDoctor = doctorService.getDoctorById(doctorId);

        if (existingDoctor == null) {
            throw new DoctorNotFoundException("Doctor with ID " + doctorId + " not found!");
        }

        // Prepare a string to collect details of updated fields
        StringBuilder updatedFields = new StringBuilder();

        // Compare fields and update existingDoctor
        if (updatedDoctor.getDoctorName() != null
                && !updatedDoctor.getDoctorName().equals(existingDoctor.getDoctorName())) {
            existingDoctor.setDoctorName(updatedDoctor.getDoctorName());
            updatedFields.append("Doctor Name updated to: ").append(updatedDoctor.getDoctorName()).append("\n");
        }
        if (updatedDoctor.getSpecialization() != null
                && !updatedDoctor.getSpecialization().equals(existingDoctor.getSpecialization())) {
            existingDoctor.setSpecialization(updatedDoctor.getSpecialization());
            updatedFields.append("Specialization updated to: ").append(updatedDoctor.getSpecialization()).append("\n");
        }
        if (updatedDoctor.getQualification() != null
                && !updatedDoctor.getQualification().equals(existingDoctor.getQualification())) {
            existingDoctor.setQualification(updatedDoctor.getQualification());
            updatedFields.append("Qualification updated to: ").append(updatedDoctor.getQualification()).append("\n");
        }
        if (updatedDoctor.getContactNumber() != null
                && !updatedDoctor.getContactNumber().equals(existingDoctor.getContactNumber())) {
            existingDoctor.setContactNumber(updatedDoctor.getContactNumber());
            updatedFields.append("Contact Number updated to: ").append(updatedDoctor.getContactNumber()).append("\n");
        }
        if (updatedDoctor.getEmailId() != null && !updatedDoctor.getEmailId().equals(existingDoctor.getEmailId())) {
            existingDoctor.setEmailId(updatedDoctor.getEmailId());
            updatedFields.append("Email ID updated to: ").append(updatedDoctor.getEmailId()).append("\n");
        }
        if (updatedDoctor.getGender() != null && !updatedDoctor.getGender().equals(existingDoctor.getGender())) {
            existingDoctor.setGender(updatedDoctor.getGender());
            updatedFields.append("Gender updated to: ").append(updatedDoctor.getGender()).append("\n");
        }
        if (updatedDoctor.getLocation() != null && !updatedDoctor.getLocation().equals(existingDoctor.getLocation())) {
            existingDoctor.setLocation(updatedDoctor.getLocation());
            updatedFields.append("Location updated to: ").append(updatedDoctor.getLocation()).append("\n");
        }
        if (updatedDoctor.getConsultationFees() != 0
                && updatedDoctor.getConsultationFees() != existingDoctor.getConsultationFees()) {
            existingDoctor.setConsultationFees(updatedDoctor.getConsultationFees());
            updatedFields.append("Consultation Fees updated to: ").append(updatedDoctor.getConsultationFees())
                    .append("\n");
        }
        if (updatedDoctor.getYearsOfExperience() != 0
                && updatedDoctor.getYearsOfExperience() != existingDoctor.getYearsOfExperience()) {
            existingDoctor.setYearsOfExperience(updatedDoctor.getYearsOfExperience());
            updatedFields.append("Years of Experience updated to: ").append(updatedDoctor.getYearsOfExperience())
                    .append("\n");
        }
        if (updatedDoctor.getDateOfJoining() != null
                && !updatedDoctor.getDateOfJoining().equals(existingDoctor.getDateOfJoining())) {
            existingDoctor.setDateOfJoining(updatedDoctor.getDateOfJoining());
            updatedFields.append("Date of Joining updated to: ").append(updatedDoctor.getDateOfJoining()).append("\n");
        }
        if (updatedDoctor.getUsername() != null && !updatedDoctor.getUsername().equals(existingDoctor.getUsername())) {
            existingDoctor.setUsername(updatedDoctor.getUsername());
            updatedFields.append("Username updated to: ").append(updatedDoctor.getUsername()).append("\n");
        }
        if (updatedDoctor.getPassword() != null && !updatedDoctor.getPassword().equals(existingDoctor.getPassword())) {
            existingDoctor.setPassword(updatedDoctor.getPassword());
            updatedFields.append("Password updated to: ").append(updatedDoctor.getPassword()).append("\n");
        }
        if (updatedDoctor.getStatus() != existingDoctor.getStatus()) {
            existingDoctor.setStatus(updatedDoctor.getStatus());
            updatedFields.append("Status updated to: ").append(updatedDoctor.getStatus() ? "Active" : "Inactive")
                    .append("\n");
        }
        if (updatedDoctor.getSurgeon() != existingDoctor.getSurgeon()) {
            existingDoctor.setSurgeon(updatedDoctor.getSurgeon());
            updatedFields.append("Surgeon status updated to: ").append(updatedDoctor.getSurgeon() ? "Yes" : "No")
                    .append("\n");
        }

        // Save the updated doctor details
        Doctor savedDoctor = doctorService.updateDoctor(doctorId, existingDoctor);

        // Send an email with the updated details
        String subject = "Hospital Directory Update";
        String body = "Dear Dr. " + savedDoctor.getDoctorName() + ",\n\n"
                + "The following details in your profile have been updated:\n\n"
                + updatedFields.toString()
                + "\nRegards,\nHospital Management Team";

        try {
            emailService.sendEmail(savedDoctor.getEmailId(), subject, body);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Doctor details updated successfully, but failed to send notification email.");
        }

        return new ResponseEntity<>(savedDoctor, HttpStatus.OK);
    }

    @GetMapping("doctor-edit/{doctorId}")
    public ResponseEntity<Doctor> getDoctorrrById(@PathVariable int doctorId) {
        Doctor doctor = doctorService.getDoctorById(doctorId);
        if (doctor != null) {
            return ResponseEntity.ok(doctor);
        }
        return ResponseEntity.notFound().build();
    }

    // @PutMapping("/doctor-edit/{doctorId}")
    // public ResponseEntity<?> updateeDoctor(@Valid @RequestBody Doctor
    // updatedDoctor, @PathVariable int doctorId, BindingResult result) {
    // // Validate input
    // if (result.hasErrors()) {
    // // Collect field-specific validation errors into a map
    // Map<String, String> errors = result.getFieldErrors().stream()
    // .collect(Collectors.toMap(
    // FieldError::getField,
    // FieldError::getDefaultMessage
    // ));
    //
    // return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    // }
    //
    // // Fetch the existing doctor by ID
    // Doctor existingDoctor = doctorService.getDoctorById(doctorId);
    //
    // if (existingDoctor == null) {
    // throw new DoctorNotFoundException("Doctor with ID " + doctorId + " not
    // found!");
    // }
    //
    // // Prepare a string to collect details of updated fields
    // StringBuilder updatedFields = new StringBuilder();
    //
    // // Compare fields and update existingDoctor
    // if (updatedDoctor.getDoctorName() != null &&
    // !updatedDoctor.getDoctorName().equals(existingDoctor.getDoctorName())) {
    // existingDoctor.setDoctorName(updatedDoctor.getDoctorName());
    // updatedFields.append("Doctor Name updated to:
    // ").append(updatedDoctor.getDoctorName()).append("\n");
    // }
    //
    // if (updatedDoctor.getContactNumber() != null &&
    // !updatedDoctor.getContactNumber().equals(existingDoctor.getContactNumber()))
    // {
    // existingDoctor.setContactNumber(updatedDoctor.getContactNumber());
    // updatedFields.append("Contact Number updated to:
    // ").append(updatedDoctor.getContactNumber()).append("\n");
    // }
    // if (updatedDoctor.getEmailId() != null &&
    // !updatedDoctor.getEmailId().equals(existingDoctor.getEmailId())) {
    // existingDoctor.setEmailId(updatedDoctor.getEmailId());
    // updatedFields.append("Email ID updated to:
    // ").append(updatedDoctor.getEmailId()).append("\n");
    // }
    // if (updatedDoctor.getGender() != null &&
    // !updatedDoctor.getGender().equals(existingDoctor.getGender())) {
    // existingDoctor.setGender(updatedDoctor.getGender());
    // updatedFields.append("Gender updated to:
    // ").append(updatedDoctor.getGender()).append("\n");
    // }
    // if (updatedDoctor.getLocation() != null &&
    // !updatedDoctor.getLocation().equals(existingDoctor.getLocation())) {
    // existingDoctor.setLocation(updatedDoctor.getLocation());
    // updatedFields.append("Location updated to:
    // ").append(updatedDoctor.getLocation()).append("\n");
    // }
    //
    //
    //
    // if (updatedDoctor.getPassword() != null &&
    // !updatedDoctor.getPassword().equals(existingDoctor.getPassword())) {
    // existingDoctor.setPassword(updatedDoctor.getPassword());
    // updatedFields.append("Password updated to:
    // ").append(updatedDoctor.getPassword()).append("\n");
    // }
    //
    //
    // // Save the updated doctor details
    // Doctor savedDoctor = doctorService.updateDoctor(doctorId, existingDoctor);
    //
    // // Send an email with the updated details
    // String subject = "Hospital Directory Update";
    // String body = "Dear Dr. " + savedDoctor.getDoctorName() + ",\n\n"
    // + "The following details in your profile have been updated:\n\n"
    // + updatedFields.toString()
    // + "\nRegards,\nHospital Management Team";
    //
    // try {
    // emailService.sendEmail(savedDoctor.getEmailId(), subject, body);
    // } catch (Exception e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body("Doctor details updated successfully, but failed to send notification
    // email.");
    // }
    //
    // return new ResponseEntity<>(savedDoctor, HttpStatus.OK);
    // }
    //
    // @GetMapping("/details/{doctorId}")
    // public ResponseEntity<Doctor> getDoctorrById(@PathVariable int doctorId) {
    // Doctor doctor = doctorService.getDoctorById(doctorId); // Assume this always
    // exists
    // return ResponseEntity.ok(doctor); // Return HTTP 200 with doctor details
    // }

}
