package com.cac.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.cac.model.AdminDto;
import com.cac.model.SelectSearchDate;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class AdminCleintController {

	@Autowired
	public RestTemplate restTemplate;

	@Value("${base.url}")
	private String baseUrl;

	private String role=null;

	private AdminDto adminSession=null;

	@ModelAttribute
	public void getRole(@SessionAttribute(name = "userRole", required = false) String userRole, Model model) {
		if (userRole != null) {
			System.out.println(role);
			role = userRole;
			model.addAttribute("userRole", userRole);
		} 
	}

	@ModelAttribute
	public String checkLogin(){
		if(role==null)
		return "redirect:/adminLoginForm";
		return null;
	}

	@ModelAttribute
	public void checkAdminObject(@SessionAttribute(name = "adminObj", required = false) AdminDto adminObj){
		if(adminObj!=null) {
			this.adminSession=adminObj;
			System.out.println(adminSession);
		}
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

	// admin home page after login
	@GetMapping("/adminPage")
	public String adminPage(HttpSession session, Model model) {
		if(role==null || !role.equalsIgnoreCase("admin")) return "unauthorized";
		cleanUpSessionAttributes(session, model);
		model.addAttribute("adminObj", adminSession);
		return "admin/adminPage";
	}

	@ModelAttribute
	public void addModelAttribute(HttpSession session, Model model) {
		String username = (String) session.getAttribute("username");
		if (username != null) {
			model.addAttribute("username", username);
		}
	}

	// Patient View By Admin 
	@GetMapping("/adminPatientPage")
	public String viewAdminPatientPage(HttpSession session, Model model) {
		if(role==null || !role.equalsIgnoreCase("admin")) return "unauthorized";
		cleanUpSessionAttributes(session, model);
		model.addAttribute("selectSearchDate", new SelectSearchDate());
		return "admin/adminPatientPage";
	}

	// Doctor View By Admin
	@GetMapping("/adminDoctorPage")
	public String viewAdminDoctorPage(HttpSession session, Model model) {
		if(role==null || !role.equalsIgnoreCase("admin")) return "unauthorized";
		cleanUpSessionAttributes(session, model);
		return "adminDoctorPage";
	}
	

	@GetMapping("/viewAdminProfile")
	public String viewProfile(HttpSession session, Model model) {
		if(!role.equalsIgnoreCase("admin")) return "redirect:/adminLoginForm";
		if(adminSession==null) return "redirect:/adminLoginForm";
		AdminDto dto = null;
		String url = baseUrl + "/api/admin/viewAdminInfo/" + adminSession.getUsername();
		try {
			ResponseEntity<AdminDto> response = restTemplate.getForEntity(
					url,
					AdminDto.class, 
					String.class);
			dto = response.getBody();
			model.addAttribute("admin", dto);

		} catch (HttpStatusCodeException e) {
			model.addAttribute("errorMessage", "Unable to fetch Admin details. Please try again later." +e.getResponseBodyAsString());
			return "statusPage";
		}
		return "admin/viewAdminProfilePage";
	}
}
