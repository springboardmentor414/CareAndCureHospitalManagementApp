package com.cac.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cac.model.Doctor;
import com.cac.service.DoctorService;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping
    public ResponseEntity<List<Doctor>> getDoctors() {
        List<Doctor> doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(doctors);
    }

    // Get a doctor by ID
    @GetMapping("/{doctorId}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable int doctorId) {
        Doctor doctor = doctorService.getDoctorById(doctorId);

        return ResponseEntity.ok(doctor);
    }

    @GetMapping("/searchByUsername/{username}")
    public ResponseEntity<Doctor> getMethodName(@PathVariable String username) {
        Doctor doctor = doctorService.getDoctorByUsername(username);
        return ResponseEntity.ok(doctor);
    }
    
}
