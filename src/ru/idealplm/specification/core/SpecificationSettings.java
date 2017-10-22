package ru.idealplm.specification.core;

import java.io.InputStream;
import java.util.HashMap;

import ru.idealplm.specification.core.Specification.FormField;

public class SpecificationSettings {
	
	public static HashMap<FormField, Double> columnLengths;

	private static SpecificationSettings instance = null;
	private static final Object lock = new Object();
	private static boolean isInitialized = false;
	
	private InputStream templateStream = null;
	private InputStream configStream = null;
	private HashMap<String, String> stringProps;
	private HashMap<String, Boolean> booleanProps;
	private String[] nonbreakableWords = {};
	private String[] emptyValues = {};

	private SpecificationSettings(){
		stringProps = new HashMap<String, String>();
		booleanProps = new HashMap<String, Boolean>();
		columnLengths = new HashMap<FormField, Double>();
		//nonbreakableWords = Specification.preferenceService.getStringArray(Specification.preferenceService.TC_preference_site, "M9_Spec_NonbreakableWords", emptyValues);
	}
	
	public static SpecificationSettings getInstance() {
		if (!isInitialized) {
			synchronized (lock) {
				if (instance == null) {
					instance = new SpecificationSettings();
					isInitialized = true;
				}
			}
		}
		return instance;
	}
	
	public void addStringProperty(String key, String value){
		stringProps.put(key, value);
	}
	public String getStringProperty(String key){
		return stringProps.get(key);
	}
	public void addBooleanProperty(String key, boolean value){
		booleanProps.put(key, value);
	}
	public boolean getBooleanProperty(String key){
		return booleanProps.get(key)==null ? false : booleanProps.get(key);
	}
	
	public void setColumnLength(FormField columnType, double length){
		columnLengths.put(columnType, length);
	}
	public double getColumnLength(FormField columnType){
		return columnLengths.get(columnType);
	}
	
	public String[] getNonbreakableWords(){
		return nonbreakableWords;
	}
	
	public void setTemplateStream(InputStream templateStream){
		this.templateStream = templateStream;
	}
	public InputStream getTemplateStream(){
		if(this.templateStream!=null){
			return this.templateStream;
		} else {
			return getDefaultTemplateStream();
		}
	}
	public void setConfigStream(InputStream configStream){
		this.configStream = configStream;
	}
	public InputStream getConfigStream(){
		if(this.configStream!=null){
			return this.configStream;
		} else {
			return getDefaultConfigStream();
		}
	}
	
	public InputStream getDefaultTemplateStream() {
		System.out.println("Using default template file.");
		return Specification.class.getResourceAsStream("/pdf/DefaultSpecPDFTemplate.xsl");
	}
	public InputStream getDefaultConfigStream() {
		System.out.println("Using default config file.");
		return Specification.class.getResourceAsStream("/pdf/userconfig.xml");
	}
	
	public void cleanUp(){
		templateStream = null;
		configStream = null;
		stringProps.clear();
		booleanProps.clear();
		columnLengths.clear();
	}
}
