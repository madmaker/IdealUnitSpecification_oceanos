package ru.idealplm.specification.core;

public class Error {
	
	private String severity;
	private String text;

	public Error(String severity, String text){
		this.severity = severity;
		this.text = text;
	}
	
	public String getSeverity(){
		return severity;
	}
	
	public String getText(){
		return text;
	}
}
