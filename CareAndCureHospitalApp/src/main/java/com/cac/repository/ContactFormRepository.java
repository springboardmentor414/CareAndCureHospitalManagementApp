package com.cac.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cac.model.ContactForm;

public interface ContactFormRepository extends JpaRepository<ContactForm, Integer>{

    
}
