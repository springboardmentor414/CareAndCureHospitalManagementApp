package com.cac.model;

public class AppointmentDTO {
    private Long appointmentId;
    private Long patientId;
    private String appointmentDate;
    private String appointmentTime;
    private String status;
    private Long doctorId;
    private String reason;
    private String reasonOfCancellation;
    private String doctorName;
    private Patient patient;

    public Patient getPatient() {
        return patient;
    }
    public void setPatient(Patient patient) {
        this.patient = patient;
    }
    public String getReason() {
		return reason;
	}
	public AppointmentDTO(Long appointmentId, Long patientId, String appointmentDate, String appointmentTime,
            String status, Long doctorId, String reason, String reasonOfCancellation, String doctorName) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = status;
        this.doctorId = doctorId;
        this.reason = reason;
        this.reasonOfCancellation = reasonOfCancellation;
        this.doctorName = doctorName;
    }
    public String getDoctorName() {
        return doctorName;
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
	public Long getDoctorId() {
        return doctorId;
    }
    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }
    
    public Long getAppointmentId() {
        return appointmentId;
    }
    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }
    public Long getPatientId() {
        return patientId;
    }
    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }
    public String getAppointmentDate() {
        return appointmentDate;
    }
    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }
    public String getAppointmentTime() {
        return appointmentTime;
    }
    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
