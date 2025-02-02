package com.cac.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cac.exception.UserNotFoundException;
import com.cac.model.Doctor;
import com.cac.repository.DoctorRepository;

import jakarta.mail.MessagingException;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private EmailService emailService;

    public String displayWelcomeMessage() {
        return "Welcome to Doctor Management System";
    }

    public Doctor addDoctor(Doctor doctor) {
        Doctor savedDoctor = doctorRepository.save(doctor);

        // Send welcome email
        String subject = "Welcome to the Clinic!";
        String body = "Dear Dr. " + savedDoctor.getDoctorName() + ",\n\n" +
                      "We are delighted to have you join our team. Please let us know if you need any assistance.\n\n" +
                      "Thank you,\nClinic Admin";

        try {
			emailService.sendEmail(savedDoctor.getEmailId(), subject, body);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return savedDoctor;
    }

    public Doctor getDoctorById(int doctorId) {
        return doctorRepository.findById(doctorId).orElseThrow(()->new UserNotFoundException("Doctor not found with id : "+doctorId));
    }

    public void saveDoctor(Doctor doctor) {
        doctorRepository.save(doctor);
    }

    public boolean disableDoctor(int doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor != null) {
            doctor.setStatus(false);
            Doctor dDoctor= doctorRepository.save(doctor);

            // Send notification email
            String subject = "Account Disabled Notification";
            String body = "Dear Dr. " + dDoctor.getDoctorName() + ",\n\n" +
                          "Your account has been temporarily disabled. If you have any questions or believe this is a mistake, please contact support.\n\n" +
                          "Thank you,\nClinic Admin";

            try {
                System.out.println("Sending email to: " + dDoctor.getEmailId());
                emailService.sendEmail(dDoctor.getEmailId(), subject, body);
                System.out.println("Email sent successfully.");
            } catch (Exception e) {
                System.out.println("Error sending email: " + e.getMessage());
                e.printStackTrace();
            }

            return true;
        }
        return false;
    }



    public List<Doctor> getActiveDoctors() {
        return doctorRepository.findByStatus(true);
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public List<Doctor> findDoctorByName(String name) {
        return doctorRepository.findByDoctorName(name);
    }

    public List<Doctor> findDoctorBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization);
    }

    public List<Doctor> findDoctorByExperienceRange(int minYears, int maxYears) {
        return doctorRepository.findByYearsOfExperienceBetween(minYears, maxYears);
    }

    public boolean deleteDoctorById(int doctorId) {
        System.out.println("Checking if doctor exists with ID: " + doctorId);
        boolean exists = doctorRepository.existsById(doctorId);
        System.out.println("Doctor exists: " + exists);

        if (exists) {
            Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
            if (doctor != null) {
                doctorRepository.deleteById(doctorId);

                // Send account deletion email
                String subject = "Account Deletion Notification";
                String body = "Dear Dr. " + doctor.getDoctorName() + ",\n\n" +
                              "Your account has been removed from our system. If this is a mistake, please contact support.\n\n" +
                              "Thank you,\nClinic Admin";
                try {
					emailService.sendEmail(doctor.getEmailId(), subject, body);
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

                System.out.println("Doctor deleted successfully.");
                return true;
            }
        }
        System.out.println("Doctor not found.");
        return false;
    }

    public boolean markDoctorAsInactive(int doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor != null) {
            doctor.setStatus(false);
            doctorRepository.save(doctor);
            return true;
        }
        return false;
    }

    public List<Doctor> searchDoctorsByName(String name) {
        String searchPattern = "%" + name + "%";
        return doctorRepository.findByDoctorNameLike(searchPattern);
    }
    
    public Doctor updateDoctor(int doctorId, Doctor updatedDoctor) {
		Doctor existingDoctor = doctorRepository.findById(doctorId).orElse(null);

		if (existingDoctor != null) {
			// Update fields only if they are provided in the request
			if (updatedDoctor.getDoctorName() != null) {
				existingDoctor.setDoctorName(updatedDoctor.getDoctorName());
			}
			if (updatedDoctor.getSpecialization() != null) {
				existingDoctor.setSpecialization(updatedDoctor.getSpecialization());
			}
			if (updatedDoctor.getQualification() != null) {
				existingDoctor.setQualification(updatedDoctor.getQualification());
			}
			if (updatedDoctor.getContactNumber() != null) {
				existingDoctor.setContactNumber(updatedDoctor.getContactNumber());
			}
			if (updatedDoctor.getEmailId() != null) {
				existingDoctor.setEmailId(updatedDoctor.getEmailId());
			}
			if (updatedDoctor.getGender() != null) {
				existingDoctor.setGender(updatedDoctor.getGender());
			}
			if (updatedDoctor.getLocation() != null) {
				existingDoctor.setLocation(updatedDoctor.getLocation());
			}
			if (updatedDoctor.getConsultationFees() != 0) {
				existingDoctor.setConsultationFees(updatedDoctor.getConsultationFees());
			}
			if (updatedDoctor.getYearsOfExperience() != 0) {
				existingDoctor.setYearsOfExperience(updatedDoctor.getYearsOfExperience());
			}
			if (updatedDoctor.getDateOfJoining() != null) {
				existingDoctor.setDateOfJoining(updatedDoctor.getDateOfJoining());
			}
			existingDoctor.setStatus(updatedDoctor.getStatus());
			existingDoctor.setSurgeon(updatedDoctor.getSurgeon());

			return doctorRepository.save(existingDoctor); // Save the updated doctor record
		}
		return null; // Return null if doctor not found
	}

    
}
