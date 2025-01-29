package com.cac.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.validation.constraints.FutureOrPresent;


@NoArgsConstructor
public class RescheduleDTO{
    public String getNewDate() {
        return newDate;
    }
    public void setNewDate(String newDate) {
        this.newDate = newDate;
    }
    public String getNewTime() {
        return newTime;
    }
    public void setNewTime(String newTime) {
        this.newTime = newTime;
    }
    @FutureOrPresent(message = "Appointment date must be in present or future")
    private  String newDate;
    @FutureOrPresent(message = "Appointment time must be in present or future")
    private  String newTime;
}
