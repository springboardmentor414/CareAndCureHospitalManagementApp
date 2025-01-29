package com.cac.service;

import com.cac.exception.*;
import com.cac.model.Appointment;
import com.cac.model.Doctor;
import com.cac.model.Patient;
import com.cac.repository.AppointmentRepository;
import com.cac.repository.DoctorRepository;
import com.cac.repository.PatientRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

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
                appointment.getPatient().getEmail(),
                appointment.getPatient().getPatientName(),
                appointment.getDoctor().getDoctorName(),
                appointment.getAppointmentDate().toString(),
                appointment.getAppointmentTime().toString());
    }

    private void sendAppointmentCancellationEmail(Appointment appointment) throws MessagingException {
        emailService.sendAppointmentCancellationEmail(
                appointment.getPatient().getEmail(),
                appointment.getPatient().getPatientName(),
                appointment.getDoctor().getDoctorName(),
                appointment.getAppointmentDate().toString(),
                appointment.getAppointmentTime().toString(),
                appointment.getReasonOfCancellation());
    }

    private void sendAppointmentRescheduleEmail(Appointment appointment) throws MessagingException {
        emailService.sendAppointmentRescheduleEmail(
                appointment.getPatient().getEmail(),
                appointment.getPatient().getPatientName(),
                appointment.getDoctor().getDoctorName(),
                appointment.getAppointmentDate().toString(),
                appointment.getAppointmentTime().toString());
    }
}
