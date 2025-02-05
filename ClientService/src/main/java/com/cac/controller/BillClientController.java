package com.cac.controller;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cac.model.Appointment;
import com.cac.model.Bill;
import com.cac.model.Doctor;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.ui.Model;
import org.springframework.http.HttpHeaders;
import java.util.*;
import java.time.LocalDate;


@Controller
public class BillClientController {
	
	@Autowired
	public RestTemplate restTemplate;

	@Value("${base.url}")
	private String backendUrl;

	@RequestMapping(value="/index")
	public String regindex() {
	    return "index";
		
	}
	
	@RequestMapping(value="/billHomePage")
	public String registrationPage() {
	    return "frontpage";
		
	}
	
	@RequestMapping(value="/generateBillByAdmin/{appointmentId}")
	public String generateBillPageByAdmin(@PathVariable int appointmentId, Model m) {

		Appointment appointment= restTemplate.getForEntity(backendUrl + "/api/admin/getAppointmentById/"+ appointmentId,Appointment.class).getBody();
		Bill bill = new Bill();
		if(appointment!=null){
		bill.setAppointment(appointment);
		Doctor doctor = restTemplate.getForEntity(backendUrl + "/api/doctors/"+ appointment.getDoctorId(),Doctor.class).getBody();
			if(doctor!=null)
		bill.setConsultationFees(doctor.getConsultationFees());
		bill.setInsuranceApplicable(appointment.getPatient().getHasInsurance());
		}
		m.addAttribute("bill", bill);
	    return "generatebill";
	
	}
	
	
	
	@RequestMapping(value="/generateBill")
	public String generateBillPage(Model m) {
		m.addAttribute("bill", new Bill());
	    return "generatebill";
	
	}
	 
     
     @RequestMapping(value="/searchByBillId")
 	public String searchByBillIdmethod () {
 	   return "viewByBillId";
 	
 	}
     @RequestMapping(value="/searchByPatientId")
 	public String searchByPatientIdmethod() {
 		return "viewByPatientId";
 	
 	}
     @RequestMapping(value="/searchByDate")
 	public String searchByDatemethod() {
 		return "viewByDate";
 	
 	}
     
     @RequestMapping(value="/updateBillStatus")
  	public String updatebill() {
  		return "updateBill";
  	
  	}
    
     
	
     //http://localhost:8080/bills/2
     @RequestMapping(value = "/bills/{appointmentId}", method = RequestMethod.POST)
     public String submitNewBill(@PathVariable("appointmentId") int appointmentId, @ModelAttribute("bill") Bill bill, Model model) throws JsonMappingException, JsonProcessingException {
     
    	 Bill billobj = null;
         System.out.println("AppointmentId -" + appointmentId);
         model.addAttribute("appointmentId", appointmentId);
         String url = "http://localhost:8082/bills/" + appointmentId;

         // Set up headers for the request
         HttpHeaders headers = new HttpHeaders();
         headers.set("Content-Type", "application/json");
         // Wrap the bill object into an HTTP request entity
         HttpEntity<Bill> request = new HttpEntity<>(bill, headers);

         // Send the POST request
         try {
             ResponseEntity<Bill> response = restTemplate.exchange(
                 url,
                 HttpMethod.POST,
                 request,
                 Bill.class
             );

             billobj = response.getBody(); // Get the created bill object from the response

         } catch (HttpClientErrorException e) {
        	 if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                 // 404 error
                 model.addAttribute("errorMessage", "Pls Enter Valid Appointment Id  ");
                 return "statuspage_bill"; 
             } 
             // Handle client errors (4xx)
             ObjectMapper objectMapper = new ObjectMapper();
             JsonNode rootNode = objectMapper.readTree(e.getResponseBodyAsString());
             String errorMessage = rootNode.path("message").asText();
             model.addAttribute("errorMessage", errorMessage);
             return "statuspage_bill"; // Redirect to an error page
         } catch (HttpServerErrorException e) {
             // Handle server errors (5xx)
             model.addAttribute("errorMessage", "Server error: " + e.getMessage());
             return "statuspage_bill"; // Redirect to an error page
         } catch (Exception e) {
             // Handle unexpected errors
             model.addAttribute("errorMessage", "Unexpected error: " + e.getMessage());
             return "statuspage_bill"; // Redirect to an error page
         }

         // If the bill was created successfully, show a success page
         if (billobj != null) {
        	 
             model.addAttribute("bill", billobj); // Optionally add the newly created bill to the model
             double balanceAmount = billobj.getFinalamount() - billobj.getAmountPaid();
        	 model.addAttribute("balanceAmount", balanceAmount);
             System.out.println(balanceAmount);
             return "bill_list"; // A confirmation page to show the bill details
         } else {
             // If the bill wasn't created, show an error message
             model.addAttribute("errorMessage", "Failed to create the bill. Try again.");
             return "statuspage_bill"; // Redirect to an error page
         }
     }
    
             

     @RequestMapping(value = "/findBillByIdforUpdatingPaymentStatus", method = RequestMethod.GET)
 	public String findBillByIdforUpdatingPaymentStatus(@RequestParam("billId") int billId, Model model) {
 	    Bill bill = null;

 	    String url = "http://localhost:8082/bills/" + billId;
 	    HttpHeaders headers = new HttpHeaders();
 	    headers.set("Content-Type", "application/json");

 	    try {
 	        ResponseEntity<Bill> response = restTemplate.exchange(
 	            url,
 	            HttpMethod.GET,
 	            null,
 	            new ParameterizedTypeReference<Bill>() {}
 	        );
 	        if (response != null && response.getBody() != null) {
 	            bill = response.getBody();
 	        }
 	    } catch (HttpClientErrorException e) {
 	    	 if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                 // 404 error
                 model.addAttribute("errorMessage", "Pls Enter Valid Bill Id  ");
                 return "statuspage_bill"; 
             }
 	        model.addAttribute("errorMessage", "Client error: " + e.getMessage());
 	        return "statuspage";
 	    } catch (HttpServerErrorException e) {
 	        model.addAttribute("errorMessage", "Server error: " + e.getMessage());
 	        return "statuspage";
 	    } catch (Exception e) {
 	        model.addAttribute("errorMessage", "Unexpected error: " + e.getMessage());
 	        return "statuspage_bill";
 	    }

 	    if (bill != null) {
 	        model.addAttribute("bill", bill);
 	        return "bill_list_updating";
 	    } else {
 	        model.addAttribute("errorMessage", "No bill details found with the given ID.");
 	        return "statuspage_bill";
 	    }
 	}
     
     
     @RequestMapping(value = "/updatepaymentstatus", method = RequestMethod.POST)
 	public String updatepaymentstatus(@ModelAttribute("bill") Bill bill, Model model) {
    	 Bill billobj = null;
 	    System.out.println("Bill Id for update-"+bill.getBillId());
 	   System.out.println("Bill object"+bill);
 	    String url = "http://localhost:8082/bills/" + bill.getBillId();
 	  
 	// Set up headers for the request
       HttpHeaders headers = new HttpHeaders();
       headers.set("Content-Type", "application/json");
       // Wrap the bill object into an HTTP request entity
       HttpEntity<Bill> request = new HttpEntity<>(bill, headers);
       // Send the POST request
      

 	   
 	    try {
 	        ResponseEntity<Bill> response = restTemplate.exchange(
 	            url,
 	            HttpMethod.PUT,
 	            request,
 	            //new ParameterizedTypeReference<Bill>() {}
 	           Bill.class
 	        );
 	        if (response != null && response.getBody() != null) {
 	            billobj = response.getBody();
 	        }
 	    }
 	  
 	    catch (HttpClientErrorException e) {
 	        model.addAttribute("errorMessage", "Client error: " + e.getMessage());
 	        return "statuspage_bill";
 	    } catch (HttpServerErrorException e) {
 	        model.addAttribute("errorMessage", "Server error: " + e.getMessage());
 	        return "statuspage_bill";
 	    } catch (Exception e) {
 	        model.addAttribute("errorMessage", "Unexpected error: " + e.getMessage());
 	        return "statuspage_bill";
 	    }

 	   // If the bill was created successfully, show a success page
        if (billobj != null) {
            model.addAttribute("bill", billobj); // Optionally add the newly created bill to the model
            return "bill_list"; // A confirmation page to show the bill details
        } else {
            // If the bill wasn't created, show an error message
            model.addAttribute("errorMessage", "Failed to create the bill. Try again.");
            return "statuspage_bill"; // Redirect to an error page
        }
 	}
    	
	
		@RequestMapping(value = "/findBillByPatientId", method = RequestMethod.GET)
	public String findBillByPatientId(@RequestParam("patientId") int patientId, Model model) {
	    List<Bill> bills = null;

	   
	    String url = "http://localhost:8082/bills/patient/" + patientId;
	    
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("Content-Type", "application/json");

	    try {
	        // Send the GET request to fetch the list of bills
	        ResponseEntity<List<Bill>> response = restTemplate.exchange(
	            url,
	            HttpMethod.GET,
	            null,
	            new ParameterizedTypeReference<List<Bill>>() {}
	        );

	        if (response != null && response.getBody() != null) {
	            bills = response.getBody();
	        }
	    } catch (HttpClientErrorException e) {
	        model.addAttribute("errorMessage", "Client error: " + e.getMessage());
	        return "statuspage_bill";
	    } catch (HttpServerErrorException e) {
	        model.addAttribute("errorMessage", "Server error: " + e.getMessage());
	        return "statuspage_bill";
	    } catch (Exception e) {
	        model.addAttribute("errorMessage", "Unexpected error: " + e.getMessage());
	        return "statuspage_bill";
	    }

	    // Check if any bills are returned
	    if (bills != null && !bills.isEmpty()) {
	    	 model.addAttribute("bills", bills);
		        model.addAttribute("patient_id", patientId);
		        return "patient_list";
	      
	    } else {
	        model.addAttribute("errorMessage", "No bills found for the given Patient ID.");
	        return "statuspage_bill";
	    }
	}
		@RequestMapping(value = "/findBillByDate", method = RequestMethod.GET)
		public String findBillByDate1(@RequestParam("billDate") LocalDate billDate, Model model) {
			    List<Bill> bills = null;

		    // Modify URL to fetch the list of bills for the given Patient ID
			    String url = "http://localhost:8082/bills/date/" + billDate;
				  
		    HttpHeaders headers = new HttpHeaders();
		    headers.set("Content-Type", "application/json");

		    try {
		        // Send the GET request to fetch the list of bills
		        ResponseEntity<List<Bill>> response = restTemplate.exchange(
		            url,
		            HttpMethod.GET,
		            null,
		            new ParameterizedTypeReference<List<Bill>>() {}
		        );

		        if (response != null && response.getBody() != null) {
		            bills = response.getBody();
		        }
		    } catch (HttpClientErrorException e) {
		        model.addAttribute("errorMessage", "Client error: " + e.getMessage());
		        return "statuspage_bill";
		    } catch (HttpServerErrorException e) {
		        model.addAttribute("errorMessage", "Server error: " + e.getMessage());
		        return "statuspage_bill";
		    } catch (Exception e) {
		        model.addAttribute("errorMessage", "Unexpected error: " + e.getMessage());
		        return "statuspage_bill";
		    }

		    // Check if any bills are returned
		    if (bills != null && !bills.isEmpty()) {
		        model.addAttribute("bills", bills);
		        model.addAttribute("date", billDate);
		        return "date_list";  
		    } else {
		        model.addAttribute("errorMessage", "No bills found for the given Patient ID.");
		        return "statuspage_bill";
		    }
		}
	    
	

	
	
	
	
	@RequestMapping(value = "/findBillById", method = RequestMethod.GET)
	public String findBillById(@RequestParam("billId") int billId, Model model) {
	    Bill bill = null;

	    String url = "http://localhost:8082/bills/" + billId;
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("Content-Type", "application/json");

	    try {
	        ResponseEntity<Bill> response = restTemplate.exchange(
	            url,
	            HttpMethod.GET,
	            null,
	            new ParameterizedTypeReference<Bill>() {}
	        );
	        if (response != null && response.getBody() != null) {
	            bill = response.getBody();
	        }
	    } catch (HttpClientErrorException e) {
	        model.addAttribute("errorMessage", "Client error: " + e.getMessage());
	        return "statuspage_bill";
	    } catch (HttpServerErrorException e) {
	        model.addAttribute("errorMessage", "Server error: " + e.getMessage());
	        return "statuspage_bill";
	    } catch (Exception e) {
	        model.addAttribute("errorMessage", "Unexpected error: " + e.getMessage());
	        return "statuspage_bill";
	    }

	    if (bill != null) {
	        model.addAttribute("bill", bill);
	        double balanceAmount = bill.getFinalamount() - bill.getAmountPaid();
	       	 model.addAttribute("balanceAmount", balanceAmount);
	            System.out.println(balanceAmount);
	        return "bill_list";
	    } else {
	        model.addAttribute("errorMessage", "No bill details found with the given ID.");
	        return "statuspage_bill";
	    }
	}
}
