package com.cac.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Map;

@Data
@Entity
@Table(name = "availability")
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek; 

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "time_slots",
        joinColumns = {
            @JoinColumn(name = "day_of_week", referencedColumnName = "day_of_week"),
            @JoinColumn(name = "doctor_id", referencedColumnName = "doctor_id") 
        }
    )
    @MapKeyColumn(name = "time_slot") // The time slot, e.g., "09:00am"
    @Column(name = "is_available") // Availability as a boolean
    private Map<String, Boolean> timeSlots;


    public enum DayOfWeek {
        SUNDAY,
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY
    }


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public DayOfWeek getDayOfWeek() {
		return dayOfWeek;
	}


	public void setDayOfWeek(DayOfWeek dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}


	public Doctor getDoctor() {
		return doctor;
	}


	public void setDoctor(Doctor doctor) {
		this.doctor = doctor;
	}


	public Map<String, Boolean> getTimeSlots() {
		return timeSlots;
	}


	public void setTimeSlots(Map<String, Boolean> timeSlots) {
		this.timeSlots = timeSlots;
	}
}
