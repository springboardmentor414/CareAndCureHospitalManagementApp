package com.cac.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cac.exception.UserNotFoundException;
import com.cac.model.AdminInfo;
import com.cac.model.Appointment;
import com.cac.model.Patient;
import com.cac.service.AdminService;
import com.cac.service.AppointmentService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/admin")
public class AdminPatientController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AppointmentService appointmentService;

    // Update Admin details
    @PutMapping("/updateAdmin/{id}")
    public ResponseEntity<AdminInfo> updateAdmin(@PathVariable int id, @Valid @RequestBody AdminInfo adminInfo)
            throws Exception {
        return new ResponseEntity<AdminInfo>(adminService.updateAdmin(id, adminInfo), HttpStatus.ACCEPTED);
    }

    @GetMapping("/viewAdminInfo/{username}")
    public ResponseEntity<AdminInfo> getAdminInfo(@PathVariable String username) throws UserNotFoundException {
        AdminInfo adminInfo = adminService.getAdminInfo(username);
        return new ResponseEntity<>(adminInfo, HttpStatus.OK);
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<Appointment>> getAppointments(@PathVariable String date) {
        LocalDate appointmentDate = LocalDate.parse(date); // This will be handled by global exception handler
        List<Appointment> appointments = appointmentService.getAppointmentsByDate(appointmentDate);

        if (appointments.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // Return 204 No Content if no appointments
        }

        return new ResponseEntity<>(appointments, HttpStatus.OK); // Return 200 OK with appointments
    }

    @GetMapping("/no-show/{startDate}/{endDate}")
    public ResponseEntity<List<Patient>> getNoShowAppointments(@PathVariable String startDate, @PathVariable String endDate) {
        List<Patient> noShows = appointmentService.getNoShowAppointments(LocalDate.parse(startDate), LocalDate.parse(endDate));

        return new ResponseEntity<>(noShows, HttpStatus.OK);
    }

    @GetMapping("/getAppointmentById/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable int id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id).orElse(null));
    }
    

    
}
