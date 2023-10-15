package com.atd.microservices.core.ediaggregator.exception;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EDIAggregatorException extends RuntimeException{
	
	private static final long serialVersionUID = -2589421071244904734L;
	
	private Date timestamp;
	private String message;
	
	public EDIAggregatorException(String message) {
		super(message);
		this.message = message;
	}
	
	public EDIAggregatorException(Date timestamp, String message) {
		super();
		this.timestamp = timestamp;
		this.message = message;
	}
	
	public EDIAggregatorException(String message, Throwable e) {
		super(message, e);
		this.message = message;
	}

}
