package com.cac.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;

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

  @FutureOrPresent(message = "Appointment date must be in present or future")
  private LocalDate appointmentDate;
  @FutureOrPresent(message = "Appointment time must be in present or future")
  private LocalTime appointmentTime;
  private String status="Scheduled";
  @Size(min = 10, message = "Reason must be at least 10 characters long")
  private String reason;
  
  @Size(min = 10, message = "Reason of cancellation must be at least 10 characters long")
  private String reasonOfCancellation;

  @ManyToOne(fetch = FetchType.EAGER) 
  @JoinColumn(name = "patient_id", nullable = false)
  @JsonIgnore
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
      this.doctorName = doctor.getDoctorName();  // Assuming Doctor entity has getName()
      this.doctorId = doctor.getDoctorId();  // Assuming Doctor entity has getId()
    }
  }
  public  void  setMyStatues(String s){
    this.status=s;
  }

}

