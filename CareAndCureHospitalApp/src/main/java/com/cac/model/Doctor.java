package com.cac.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.cac.validation.NotFutureDate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
public class Doctor {

    @Id
    private int doctorId;

    @NotBlank(message = "Doctor name cannot be blank")
    @Size(max = 50, message = "Doctor name must not exceed 50 characters")
    private String doctorName;

    @NotBlank(message = "Specialization is required")
    private String specialization;

    @NotBlank(message = "Qualification is required")
    private String qualification;

    @Size(min = 10, max = 10, message = "Contact number must be 10 digits")
    private String contactNumber;

    @NotBlank(message = "Email ID cannot be blank")
    @Email(message = "Email ID must be valid")
    private String emailId;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Location cannot be blank")
    private String location;

    @Positive(message = "Consultation fees must be positive")
    private double consultationFees;

    @NotNull(message = "Date of joining is required")
     @NotFutureDate(message = "Date of joining cannot be a future date")
    private LocalDate dateOfJoining;

    @NotNull(message = "Surgeon status must be specified")
    private Boolean surgeon;

    @NotNull(message = "Status must be specified")
    private Boolean status;

    @Positive(message = "Years of experience must be positive")
    private int yearsOfExperience;

    // New fields: username and password
    @NotBlank(message = "Username cannot be blank")
    //@Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters")
    @Column(unique = true)
    private String username;

   // @NotNull(message = "password is required")
   // @NotBlank(message = "Password cannot be blank")
    @Size(min = 4, max = 20, message = "Password must be between 4 and 20 characters")
    private String password;

    // One-to-many relationship with Appointment using doctorId
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Appointment> appointments;

    // New Mapping Added By Team-03 For Availability
    // Simplified Map handling for availability (Optional)
    @ElementCollection
    @CollectionTable(name = "doctor_availability", joinColumns = @JoinColumn(name = "doctor_id"))
    @MapKeyColumn(name = "day_of_week")
    @Column(name = "availability_slot")
    @JsonIgnore
    private Map<String, List<String>> availability;

    public Map<String, List<String>> getAvailability() {
        return availability;
    }

    public void setAvailability(Map<String, List<String>> availability) {
        this.availability = availability;
    }

    // Getters and setters
    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getConsultationFees() {
        return consultationFees;
    }

    public void setConsultationFees(double consultationFees) {
        this.consultationFees = consultationFees;
    }

    public LocalDate getDateOfJoining() {
        return dateOfJoining;
    }

    public void setDateOfJoining(LocalDate dateOfJoining) {
        this.dateOfJoining = dateOfJoining;
    }

    public Boolean getSurgeon() {
        return surgeon;
    }

    public void setSurgeon(Boolean surgeon) {
        this.surgeon = surgeon;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public int getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(int yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }
}
