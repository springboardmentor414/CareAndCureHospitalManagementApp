package com.cac.model;

import com.cac.annotations.ValidDateOfBirth;
import com.cac.annotations.ValidInsurance;
// import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@ValidDateOfBirth
@ValidInsurance
@Entity
@Table(name = "patient")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int patientId;

    @Column(name = "patientName", nullable = false, length = 50)
    @NotBlank(message = "Patient Name is mandatory")
    private String patientName;

    @Positive(message = "Age must be required")
    private Integer age;
    private LocalDate dateOfBirth;

    @Column(length = 10)
    @NotBlank(message = "Select gender")
    private String gender;

    // @Column(unique = true, length = 15)
    @NotBlank(message = "Contact Number is mandatory")
    @Column(nullable = false)
    @Pattern(regexp = "^\\+[1-9]{1}[0-9]{0,2}[0-9]{10}$", message = "Contact number must start with a country code (e.g., +1) followed by a 10-digit mobile number")
    private String contactNumber;

    @NotBlank(message = "Address required!")
    private String address;

    @Email(message = "Enter valid email", regexp = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9]+\\.[a-zA-Z]{2,}$")
    @Column(unique = true, nullable = false)
    private String emailId;

    @Lob
    private String medicalHistory;

    @Lob
    private String allergies;

    @Lob
    private String medications;

    @Lob
    private String treatments;

    private boolean isActive;

    // Insurance details fields

    // The 'hasInsurance' flag indicates whether the patient has insurance
    private Boolean hasInsurance = false;

    @Column(length = 50)
    private String insuranceProvider;

    @Column(length = 20)
    private String insurancePolicyNumber;

    private LocalDate insuranceExpiryDate;

    private String insuranceCoverageDetails;

    // Insurance amount limit
    @Column(length=20)
    private Double insuranceAmountLimit;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // @JsonBackReference
    @JsonIgnore
    private List<Appointment> appointments;

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getMedications() {
        return medications;
    }

    public void setMedications(String medications) {
        this.medications = medications;
    }

    public String getTreatments() {
        return treatments;
    }

    public void setTreatments(String treatments) {
        this.treatments = treatments;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getHasInsurance() {
        return hasInsurance;
    }

    public void setHasInsurance(Boolean hasInsurance) {
        this.hasInsurance = hasInsurance;
    }

    public String getInsuranceProvider() {
        return insuranceProvider;
    }

    public void setInsuranceProvider(String insuranceProvider) {
        this.insuranceProvider = insuranceProvider;
    }

    public String getInsurancePolicyNumber() {
        return insurancePolicyNumber;
    }

    public void setInsurancePolicyNumber(String insurancePolicyNumber) {
        this.insurancePolicyNumber = insurancePolicyNumber;
    }

    public LocalDate getInsuranceExpiryDate() {
        return insuranceExpiryDate;
    }

    public void setInsuranceExpiryDate(LocalDate insuranceExpiryDate) {
        this.insuranceExpiryDate = insuranceExpiryDate;
    }

    public String getInsuranceCoverageDetails() {
        return insuranceCoverageDetails;
    }

    public void setInsuranceCoverageDetails(String insuranceCoverageDetails) {
        this.insuranceCoverageDetails = insuranceCoverageDetails;
    }

    public Double getInsuranceAmountLimit() {
        return insuranceAmountLimit;
    }

    public void setInsuranceAmountLimit(Double insuranceAmountLimit) {
        this.insuranceAmountLimit = insuranceAmountLimit;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }
    
}



