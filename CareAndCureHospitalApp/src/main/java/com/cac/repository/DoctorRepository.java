package com.cac.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cac.model.Doctor;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {

	List<Doctor> findByDoctorName(String name);
	List<Doctor> findBySpecialization(String specialization);
	List<Doctor> findByYearsOfExperienceBetween(int minYears, int maxYears);
	List<Doctor> findByStatus(boolean status);
	 List<Doctor> findByDoctorNameLike(String namePattern);
	 Optional<Doctor> findByUsername(String username);
	    boolean existsByEmailId(String emailId);
	    boolean existsByContactNumber(String contactNumber);
}
