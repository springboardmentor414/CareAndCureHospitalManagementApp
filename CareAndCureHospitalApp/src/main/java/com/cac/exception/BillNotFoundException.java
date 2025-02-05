package com.cac.exception;

@SuppressWarnings("serial")
public class BillNotFoundException extends RuntimeException{
	 public BillNotFoundException(String message) {
	        super(message);
	    }
}
