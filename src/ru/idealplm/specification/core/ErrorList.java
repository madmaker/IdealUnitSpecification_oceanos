package ru.idealplm.specification.core;

import java.util.ArrayList;
import java.util.List;

public class ErrorList{
	
	public List<Error> errorList;
	
	public ErrorList(){
		errorList = new ArrayList<Error>();
	}
	
	public synchronized void addError(Error error){
		errorList.add(error);
	}
	
	public int size(){
		return errorList.size();
	}
	
	public String toString(){
		StringBuilder stringBuilder = new StringBuilder();
		for(Error error:errorList){
			stringBuilder.append(error.getSeverity() + ": " + error.getText());
			stringBuilder.append("\n");
		}
		return stringBuilder.toString();
	}

}
