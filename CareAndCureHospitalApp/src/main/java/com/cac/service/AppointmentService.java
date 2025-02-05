package com.cac.service;

import com.cac.exception.UserNotFoundException;
import com.cac.model.Appointment;
import com.cac.model.Doctor;
import com.cac.model.Patient;
import com.cac.repository.AppointmentRepository;
import com.cac.repository.DoctorRepository;
import com.cac.repository.PatientRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private EmailService emailService;

    // Fetch all appointments
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    // Get appointments by patient ID
    public List<Appointment> getAppointmentsByPatientId(int patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with ID: " + patientId));
        return appointmentRepository.findByPatient(patient);
    }

    // Get appointments by doctor ID
    public List<Appointment> getAppointmentsForDoctor(int doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor with ID " + doctorId + " not found"));
        return appointmentRepository.findByDoctor(doctor);
    }

    // Get a specific appointment by ID
    public Optional<Appointment> getAppointmentById(int appointmentId) {
        return appointmentRepository.findById(appointmentId);
    }

    // Create a new appointment
    public Appointment createAppointment(Appointment appointment) throws UserNotFoundException, MessagingException {
        validateAppointmentDetails(appointment);

        boolean isAvailable = isTimeSlotAvailable(
                appointment.getDoctor().getDoctorId(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime());

        if (!isAvailable) {
            throw new IllegalStateException("The selected time slot is already booked. Please choose another time.");
        }

        if (appointment.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Appointment date cannot be in the past.");
        }

        appointment.setStatus("Scheduled");
        Appointment savedAppointment = appointmentRepository.save(appointment);

        sendAppointmentConfirmationEmail(savedAppointment);
        return savedAppointment;
    }

    // Update an existing appointment
    public Appointment updateAppointment(Appointment appointment) throws MessagingException {
        if (!appointmentRepository.existsById(appointment.getAppointmentId())) {
            throw new IllegalArgumentException("Appointment with ID " + appointment.getAppointmentId() + " not found.");
        }

        validateAppointmentDetails(appointment);

        boolean isAvailable = isTimeSlotAvailable(
                appointment.getDoctor().getDoctorId(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime());

        if (!isAvailable) {
            throw new IllegalStateException("The selected time slot is already booked. Please choose another time.");
        }

        Appointment updatedAppointment = appointmentRepository.save(appointment);
        sendAppointmentConfirmationEmail(updatedAppointment);
        return updatedAppointment;
    }

    // Check if a time slot is available
    public boolean isTimeSlotAvailable(int doctorId, LocalDate appointmentDate, LocalTime appointmentTime) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor with ID " + doctorId + " not found"));

        return !appointmentRepository.existsByDoctorAndAppointmentDateAndAppointmentTime(doctor, appointmentDate, appointmentTime);
    }

    // Cancel an appointment
   
    public void cancelAppointment(int appointmentId, String reason) throws UserNotFoundException, MessagingException {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment with ID " + appointmentId + " not found"));

        validateAppointmentDetails(appointment);

        appointment.setStatus("Cancelled");
        appointment.setReasonOfCancellation(reason);
        appointmentRepository.save(appointment);

        sendAppointmentCancellationEmail(appointment);
    }

    // Delete an appointment by ID
    public void deleteAppointmentById(int appointmentId) {
        if (!appointmentRepository.existsById(appointmentId)) {
            throw new IllegalArgumentException("Appointment with ID " + appointmentId + " not found.");
        }
        appointmentRepository.deleteById(appointmentId);
    }

    // Reschedule an appointment
    @Transactional
    public Appointment rescheduleAppointment(int appointmentId, LocalDate newDate, LocalTime newTime) throws UserNotFoundException, MessagingException {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment with ID " + appointmentId + " not found"));

        validateAppointmentDetails(appointment);

        boolean isAvailable = isTimeSlotAvailable(appointment.getDoctor().getDoctorId(), newDate, newTime);
        if (!isAvailable) {
            throw new IllegalStateException("The selected time slot is already booked. Please choose another time.");
        }

        appointment.setAppointmentDate(newDate);
        appointment.setAppointmentTime(newTime);
        appointment.setStatus("Rescheduled");
        appointment.setReasonOfCancellation(null);

        Appointment updatedAppointment = appointmentRepository.save(appointment);
        sendAppointmentRescheduleEmail(updatedAppointment);
        return updatedAppointment;
    }

    // Validate appointment details
    private void validateAppointmentDetails(Appointment appointment) {
        if (appointment.getAppointmentDate() == null || appointment.getAppointmentTime() == null) {
            throw new IllegalArgumentException("Appointment date and time cannot be null.");
        }
        if (appointment.getDoctor() == null || appointment.getDoctor().getDoctorId() == 0) {
            throw new IllegalArgumentException("Valid doctor information is required.");
        }
    }

    // Email notifications
    private void sendAppointmentConfirmationEmail(Appointment appointment) throws MessagingException {
        emailService.sendAppointmentConfirmationEmail(
                appointment.getPatient().getEmailId(),
                appointment.getPatient().getPatientName(),
                appointment.getDoctor().getDoctorName(),
                appointment.getAppointmentDate().toString(),
                appointment.getAppointmentTime().toString());
    }

    private void sendAppointmentCancellationEmail(Appointment appointment) throws MessagingException {
        emailService.sendAppointmentCancellationEmail(
                appointment.getPatient().getEmailId(),
                appointment.getPatient().getPatientName(),
                appointment.getDoctor().getDoctorName(),
                appointment.getAppointmentDate().toString(),
                appointment.getAppointmentTime().toString(),
                appointment.getReasonOfCancellation());
    }

    private void sendAppointmentRescheduleEmail(Appointment appointment) throws MessagingException {
        emailService.sendAppointmentRescheduleEmail(
                appointment.getPatient().getEmailId(),
                appointment.getPatient().getPatientName(),
                appointment.getDoctor().getDoctorName(),
                appointment.getAppointmentDate().toString(),
                appointment.getAppointmentTime().toString());
    }

    public List<Appointment> getDoctorAppointmentsInRange(int doctorId, LocalDate start, LocalDate end) {
        return null;
    }
    
    public List<Appointment> getFilteredAppointments(int doctorId, LocalDate fromDate, LocalDate toDate) {
        // Fetch all appointments for the doctor
        List<Appointment> allAppointments = appointmentRepository.findByDoctor_DoctorId(doctorId);

        // Filter appointments by date range
        return allAppointments.stream()
                .filter(appointment -> !appointment.getAppointmentDate().isBefore(fromDate) &&
                                       !appointment.getAppointmentDate().isAfter(toDate))
                .collect(Collectors.toList());
    }
    public List<Appointment> getAppointmentsByDoctorId(int doctorId) {
        return appointmentRepository.findByDoctor_DoctorId(doctorId);
    }
    
    public List<Appointment> findAppointmentsByIssueFaced(String reason) {
        try {
            return appointmentRepository.findByReasonContainingIgnoreCase(reason);
        } catch (Exception e) {
            // Log and rethrow the exception for debugging
            e.printStackTrace();
            throw new RuntimeException("Error fetching appointments for issue: " + reason, e);
        }
    }
    
    public Appointment addAppointment(Appointment appointment) {
        // Save the appointment to the database and return the saved entity
        return appointmentRepository.save(appointment);
    }
    public List<Map<String, Object>> getAppointmentsCountByDoctor(LocalDate fromDate, LocalDate toDate) {
        List<Appointment> appointments = appointmentRepository.findAll();

        // Filter appointments by the given date range
        if (fromDate != null) {
            appointments = appointments.stream()
                    .filter(a -> !a.getAppointmentDate().isBefore(fromDate))
                    .collect(Collectors.toList());
        }
        if (toDate != null) {
            appointments = appointments.stream()
                    .filter(a -> !a.getAppointmentDate().isAfter(toDate))
                    .collect(Collectors.toList());
        }

        // Group appointments by doctor and count the number of appointments
        Map<Doctor, Long> doctorAppointmentCounts = appointments.stream()
                .collect(Collectors.groupingBy(Appointment::getDoctor, Collectors.counting()));

        // Create a list of maps containing the required data, including consultation fee and revenue
        List<Map<String, Object>> result = doctorAppointmentCounts.entrySet().stream()
                .map(entry -> {
                    Doctor doctor = entry.getKey();
                    long appointmentCount = entry.getValue();
                    double consultationFee = doctor.getConsultationFees();
                    double revenue = consultationFee * appointmentCount; // Calculate revenue
                    Map<String, Object> doctorData = new HashMap<>();
                    doctorData.put("id", doctor.getDoctorId());
                    doctorData.put("name", doctor.getDoctorName());
                    doctorData.put("specialization", doctor.getSpecialization());
                    doctorData.put("count", appointmentCount);
                    doctorData.put("consultationFee", consultationFee);
                    doctorData.put("revenue", revenue);
                    return doctorData;
                })
                .sorted((d1, d2) -> Long.compare((Long) d2.get("count"), (Long) d1.get("count"))) // Sort by count in descending order
                .collect(Collectors.toList());

        return result;
    }

    public List<Appointment> getAppointmentsByDate(LocalDate date) {
        return appointmentRepository.findByAppointmentDate(date);
    }

    public List<Patient> getNoShowAppointments(LocalDate starDate, LocalDate endDate) {
        Set<Patient> notPresentAppointments = new HashSet<>();
        List<Appointment> allAppointments = appointmentRepository.findByAppointmentDateBetween(starDate, endDate);

        // LocalDate currentDate = LocalDate.now();
        // LocalTime currentTime = LocalTime.now();

        for (Appointment appointment : allAppointments) {
            if(appointment.getStatus().equalsIgnoreCase("Scheduled") && appointment.getAppointmentDate().isAfter(starDate) && appointment.getAppointmentDate().isBefore(endDate)) {
                notPresentAppointments.add(appointment.getPatient());
            }
            if(appointment.getStatus().equalsIgnoreCase("ReScheduled") && appointment.getAppointmentDate().isAfter(starDate) && appointment.getAppointmentDate().isBefore(endDate)) {
                notPresentAppointments.add(appointment.getPatient());
            }
            if(appointment.getStatus().equalsIgnoreCase("Cancelled") && appointment.getAppointmentDate().isAfter(starDate) && appointment.getAppointmentDate().isBefore(endDate)) {
                notPresentAppointments.add(appointment.getPatient());
            }
        }

        return new ArrayList<>(notPresentAppointments);
    }

}