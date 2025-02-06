package com.cac.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.cac.model.AdminDto;
import com.cac.model.ContactForm;
import com.cac.model.Doctor;
import com.cac.model.Patient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import com.cac.model.UserInfo;

@Controller
public class UserClientController {

	@Value("${base.url}")
	private String baseUrl;

	@Autowired
	private RestTemplate restTemplate;

	@GetMapping("/")
	public String homePage(HttpSession session, Model model) {
		cleanUpSessionAttributes(session, model);

		String redirectUrl = (String) session.getAttribute("redirectUrl");
		if (redirectUrl != null) {
			session.removeAttribute("redirectUrl");
		}

		String contactMessage = (String) session.getAttribute("contactFormMessage");
		if (contactMessage != null) {
			model.addAttribute("contactFormMessage", contactMessage);
			session.removeAttribute("contactFormMessage");
		}
		model.addAttribute("contactForm", new ContactForm());
		return "MainPage";
	}

	public String unAuthorizedAccess(Model model) {
		return "unauthorized";
	}

	@GetMapping("/patientLoginForm")
	public String patientLoginForm(@RequestParam(value = "redirectUrl", required = false) String redirectUrl,
			HttpSession session, Model model) {
		// Clean up session attributes if needed
		cleanUpSessionAttributes(session, model);

		// Store the redirectUrl in the session so it can be used after login
		if (redirectUrl != null) {
			session.setAttribute("redirectUrl", redirectUrl);
		}

		model.addAttribute("userInfo", new UserInfo());
		return "patient/patientLoginForm";
	}

	@GetMapping("/adminLoginForm")
	public String adminLoginForm(HttpSession session, Model model) {
		cleanUpSessionAttributes(session, model);
		model.addAttribute("userInfo", new UserInfo());
		return "admin/adminLoginForm";
	}

	@GetMapping("/doctorLoginForm")
	public String doctorLoginForm(HttpSession session, Model model) {
		cleanUpSessionAttributes(session, model);
		model.addAttribute("userInfo", new UserInfo());
		return "doctor/doctorLoginForm";
	}

	@GetMapping("/patientHomePage")
	public String patientHomePage(HttpSession session, Model model) {
		cleanUpSessionAttributes(session, model);
		return "patient/patientHomePage";
	}

	@GetMapping("/logout")
	public String logout(HttpSession session, Model model) {
		String role = (String)session.getAttribute("userRole");
		session.invalidate();

		// Now send a request to the backend to invalidate the backend session
		restTemplate.postForEntity(baseUrl + "/api/admin/logout", null, String.class);

		model.asMap().clear();
		String redirectUrl = "/";
		if(role!=null && role.equalsIgnoreCase("admin")) redirectUrl = "/adminLoginForm";
		if(role!=null && role.equalsIgnoreCase("doctor")) redirectUrl = "/doctorLoginForm";
		if(role!=null && role.equalsIgnoreCase("patient")) redirectUrl = "/patientLoginForm";
		return "redirect:"+redirectUrl;
	}

	@PostMapping("/login")
	public String userLogin(@ModelAttribute("userInfo") UserInfo userInfo, Model model,
			HttpSession session) {

		String requestUrl = baseUrl + "/login";
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json"); // Set the content type to JSON
		HttpEntity<UserInfo> requestEntity = new HttpEntity<>(userInfo, headers);

		try {
			ResponseEntity<UserInfo> response = restTemplate.postForEntity(requestUrl, requestEntity,
					UserInfo.class);
			UserInfo user = response.getBody();

			model.addAttribute("username", user.getUsername());

			if (user.getRole().equalsIgnoreCase("doctor")) {

				ResponseEntity<Doctor> doctorObj = restTemplate.getForEntity(
						baseUrl + "/api/doctors/searchByUsername/" + user.getUsername(), Doctor.class);

				session.setAttribute("message", "Welcome " + doctorObj.getBody().getDoctorName());
				session.setAttribute("userRole", "doctor");
				session.setAttribute("doctorObj", doctorObj.getBody());
				
				return "redirect:/doctorHomePage/" + user.getUsername();


			} else if (user.getRole().equalsIgnoreCase("patient")) {

				ResponseEntity<Patient> patientObj = restTemplate.getForEntity(
						baseUrl + "/api/patient/viewPatient/" + Integer.parseInt(userInfo.getUsername()), Patient.class,
						Integer.class);
				session.setAttribute("message", "Welcome " + user.getPassword());
				session.setAttribute("patientObj", patientObj.getBody());
				session.setAttribute("userRole", "patient");
				if(session.getAttribute("redirectUrl")!=null){
					String redirectUrl = (String) session.getAttribute("redirectUrl");
					session.removeAttribute("redirectUrl");
					int patientId = patientObj.getBody().getPatientId();
					redirectUrl = redirectUrl.replace("/0/", "/"+patientId+"/");
					System.out.println(redirectUrl);
					return "redirect:"+redirectUrl;
				}
				return "redirect:/patientPage";

			} else if (user.getRole().equalsIgnoreCase("admin")) {
				ResponseEntity<AdminDto> adminObj = restTemplate
						.getForEntity(baseUrl + "/api/admin/viewAdminInfo/" + userInfo.getUsername(), AdminDto.class);
				session.setAttribute("message", "Welcome " + user.getUsername());
				session.setAttribute("adminObj", adminObj.getBody());
				session.setAttribute("userRole", "admin");
				return "redirect:/adminPage";
			}

		} catch (HttpStatusCodeException e) {
			try {
				ObjectMapper objectMapper = new ObjectMapper();
				Map<String, String> errorMessage = objectMapper.readValue(e.getResponseBodyAsString(),
						new TypeReference<Map<String, String>>() {
						});
				session.setAttribute("errorMessage", errorMessage.get("error"));
			} catch (Exception parseException) {

				session.setAttribute("errorMessage", "An error occurred while parsing the validation errors.");
			}
		}
		String role = userInfo.getRole();
		System.out.println(role);
		if (role.equals("patient"))
			return "redirect:/patientLoginForm";
		if (role.equals("doctor"))
			return "redirect:/doctorLoginForm";
		return "redirect:/adminLoginForm";
	}

	private void cleanUpSessionAttributes(HttpSession session, Model model) {
		String errorMessage = (String) session.getAttribute("errorMessage");
		if (errorMessage != null) {
			model.addAttribute("errorMessage", errorMessage);
			session.removeAttribute("errorMessage");
		}
		String message = (String) session.getAttribute("message");
		if (message != null) {
			model.addAttribute("message", message);
			session.removeAttribute("message");
		}
	}

	@GetMapping("/patientRegistration")
	public String registrationPage(Model model) {
		model.addAttribute("patient", new Patient());

		return "patient/registration";
	}

	@PostMapping("/registerPatient")
	public String submitPatientRegistration(@ModelAttribute("patient") Patient patient, Model model)
			throws JsonMappingException, JsonProcessingException {
		Patient patientObj = null;
		String reuestUrl = baseUrl + "/api/patient/registerPatient";
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		HttpEntity<Patient> requestEntity = new HttpEntity<>(patient, headers);

		try {
			ResponseEntity<Patient> response = restTemplate.postForEntity(reuestUrl, requestEntity,
					Patient.class);
			patientObj = response.getBody();
			model.addAttribute("patientId", patientObj.getPatientId());
			model.addAttribute("patientName", patientObj.getPatientName());
			model.addAttribute("patientMessage", "Registration Successfully.");
			return "statuspage";
		} catch (HttpStatusCodeException e) {
			ObjectMapper objectMapper = new ObjectMapper();
			if (e.getStatusCode() == HttpStatus.BAD_REQUEST || e.getStatusCode() == HttpStatus.CONFLICT) {
				// Parse validation errors from the response body
				Map<String, String> errors = objectMapper.readValue(e.getResponseBodyAsString(),
						new TypeReference<Map<String, String>>() {
						});
				model.addAttribute("validationErrors", errors);
				return "patient/registration";
			} else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
				Map<String, String> errors = objectMapper.readValue(e.getResponseBodyAsString(),
						new TypeReference<Map<String, String>>() {
						});
				model.addAttribute("errorMessage", errors.get("error"));
			}
			return "statusPage";
		} catch (Exception e) {
			model.addAttribute("errorMessage", e.getMessage());
			return "statusPage";
		}

	}

	@PostMapping("/contactForm/submit")
	public String addContactForm(@ModelAttribute("contactForm") ContactForm contactForm,  
        HttpSession session, 
        Model model) {

		String requestUrl = baseUrl + "/addContactFormDetails";
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		HttpEntity<ContactForm> requestEntity = new HttpEntity<>(contactForm, headers);

		try {
			ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, requestEntity, String.class);
			session.setAttribute("contactFormMessage", response.getBody());
			return "redirect:/";
		} catch (HttpStatusCodeException e) {
			Map<String, String> errors = null;
            try {
                errors = new ObjectMapper().readValue(
                        e.getResponseBodyAsString(), new TypeReference<Map<String, String>>() {
                        });
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            model.addAttribute("validationError", errors);
			System.out.println(errors);

            model.addAttribute("errorMessage", "Please correct the highlighted fields.");
			
		}
		return "MainPage";
	}


}
