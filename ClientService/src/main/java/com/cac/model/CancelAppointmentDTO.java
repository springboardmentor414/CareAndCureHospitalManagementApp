package com.cac.model;

public class CancelAppointmentDTO {
    private Long appointmentId;
    private String reasonOfCancellation;
    
	public CancelAppointmentDTO() {
		//TODO
	}

	public CancelAppointmentDTO(Long appointmentId, String reasonOfCancellation) {
		this.appointmentId = appointmentId;
		this.reasonOfCancellation = reasonOfCancellation;
	}

	public Long getAppointmentId() {
		return appointmentId;
	}

	public void setAppointmentId(Long appointmentId2) {
		this.appointmentId = appointmentId2;
	}

	public String getReasonOfCancellation() {
		return reasonOfCancellation;
	}

	public void setReasonOfCancellation(String reasonOfCancellation) {
		this.reasonOfCancellation = reasonOfCancellation;
	}

}
