package com.cac.service;

import com.cac.model.Availability;
import com.cac.model.Availability.DayOfWeek;
import com.cac.repository.AvailabilityRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class AvailabilityService {

    private final AvailabilityRepository repository;

    public AvailabilityService(AvailabilityRepository repository) {
        this.repository = repository;
    }

    public Map<String, Boolean> getAvailableTimeSlots(DayOfWeek dayOfWeek, Long doctorId) {
        Optional<Availability> availabilityOpt = repository.findByDayOfWeekAndDoctorId(dayOfWeek, doctorId);
        return availabilityOpt.map(Availability::getTimeSlots)
                .orElseThrow(() -> new IllegalArgumentException("Availability not found for the given day and doctor."));
    }

    public void scheduleTimeSlot(DayOfWeek dayOfWeek, Long doctorId, String timeSlot) {
        Optional<Availability> availabilityOpt = repository.findByDayOfWeekAndDoctorId(dayOfWeek, doctorId);
        Availability availability = availabilityOpt.orElseThrow(() -> new IllegalArgumentException("Availability not found for the given day and doctor."));
        Map<String, Boolean> timeSlots = availability.getTimeSlots();
        if (timeSlots.containsKey(timeSlot) && timeSlots.get(timeSlot)) {
            timeSlots.put(timeSlot, false);
            repository.save(availability);
        } else {
            throw new IllegalArgumentException("Time slot is not available or doesn't exist.");
        }
    }

    public void cancelTimeSlot(DayOfWeek dayOfWeek, Long doctorId, String timeSlot) {
        Optional<Availability> availabilityOpt = repository.findByDayOfWeekAndDoctorId(dayOfWeek, doctorId);
        Availability availability = availabilityOpt.orElseThrow(() -> new IllegalArgumentException("Availability not found for the given day and doctor."));
        Map<String, Boolean> timeSlots = availability.getTimeSlots();
        if (timeSlots.containsKey(timeSlot)) {
            timeSlots.put(timeSlot, true);
            repository.save(availability);
        } else {
            throw new IllegalArgumentException("Time slot doesn't exist.");
        }
    }
}

