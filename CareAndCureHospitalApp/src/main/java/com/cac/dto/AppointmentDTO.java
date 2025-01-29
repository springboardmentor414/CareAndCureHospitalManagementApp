package com.cac.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.Future;

@Data
public class AppointmentDTO {
    private int appointmentId;
    private int doctorId;
    private String patientName;
    private String doctorName;
    @Future(message = "Appointment date must be in future")
    private LocalDate appointmentDate;
    @Future(message = "Appointment time must be in future")
    private LocalTime appointmentTime;
    private String status;

    // Constructors
    public AppointmentDTO() {}

    public AppointmentDTO(int appointmentId, String patientName, String doctorName, LocalDate appointmentDate, LocalTime appointmentTime, String status) {
        this.appointmentId = appointmentId;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }

    // Getters and Setters
    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

