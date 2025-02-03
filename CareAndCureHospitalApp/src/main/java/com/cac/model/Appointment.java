package com.cac.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.Size;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "appointment")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Appointment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int appointmentId;
  
  private LocalDate appointmentDate;
  private LocalTime appointmentTime;
  private String status="Scheduled";
  @Size(min = 10, message = "Reason must be at least 10 characters long")
  private String reason;
  
  @Size(min = 5, message = "Reason of cancellation must be at least 10 characters long")
  private String reasonOfCancellation;

  @ManyToOne(fetch = FetchType.EAGER) 
  @JoinColumn(name = "patient_id", nullable = false)
  private Patient patient;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "doctor_id", nullable = false)
  @JsonIgnore
  private Doctor doctor;

  @Transient
  private String doctorName;

  @Transient
  private Integer doctorId;
  @Transient
  private String doctorPhoneNumber;
  @Transient
  private String specialty;

  @PostLoad
  public void setDoctorNameAndId() {
    if (doctor != null) {
      this.doctorPhoneNumber=doctor.getContactNumber();
      this.specialty=doctor.getSpecialization();
      this.doctorName = doctor.getDoctorName();  
      this.doctorId = doctor.getDoctorId(); 
    }
  }
  public  void  setMyStatues(String s){
    this.status=s;
  }
public int getAppointmentId() {
	return appointmentId;
}
public void setAppointmentId(int appointmentId) {
	this.appointmentId = appointmentId;
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
public String getReason() {
	return reason;
}
public void setReason(String reason) {
	this.reason = reason;
}
public String getReasonOfCancellation() {
	return reasonOfCancellation;
}
public void setReasonOfCancellation(String reasonOfCancellation) {
	this.reasonOfCancellation = reasonOfCancellation;
}
public Patient getPatient() {
	return patient;
}
public void setPatient(Patient patient) {
	this.patient = patient;
}
public Doctor getDoctor() {
	return doctor;
}
public void setDoctor(Doctor doctor) {
	this.doctor = doctor;
}
public String getDoctorName() {
	return doctorName;
}
public void setDoctorName(String doctorName) {
	this.doctorName = doctorName;
}
public Integer getDoctorId() {
	return doctorId;
}
public void setDoctorId(Integer doctorId) {
	this.doctorId = doctorId;
}
public String getDoctorPhoneNumber() {
	return doctorPhoneNumber;
}
public void setDoctorPhoneNumber(String doctorPhoneNumber) {
	this.doctorPhoneNumber = doctorPhoneNumber;
}
public String getSpecialty() {
	return specialty;
}
public void setSpecialty(String specialty) {
	this.specialty = specialty;
}

}

