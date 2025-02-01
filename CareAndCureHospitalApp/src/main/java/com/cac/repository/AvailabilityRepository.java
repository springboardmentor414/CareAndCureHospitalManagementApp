package com.cac.repository;

import com.cac.model.Availability;
import com.cac.model.Availability.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    // Find availability by dayOfWeek and doctorId
    @Query("SELECT a FROM Availability a WHERE a.dayOfWeek = :dayOfWeek AND a.doctor.id = :doctorId")
    Optional<Availability> findByDayOfWeekAndDoctorId(DayOfWeek dayOfWeek, Long doctorId);

}
