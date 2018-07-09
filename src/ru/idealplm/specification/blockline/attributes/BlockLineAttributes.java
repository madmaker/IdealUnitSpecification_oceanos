package ru.idealplm.specification.blockline.attributes;

import java.util.ArrayList;
import java.util.Arrays;

import ru.idealplm.specification.core.Specification.FormField;
import ru.idealplm.specification.core.SpecificationSettings;
import ru.idealplm.specification.util.LineUtil;

public class BlockLineAttributes {

	private BLFormat format = null;
	private BLZone zone = null;
	private BLRemark remark = null;
	private BLKits kits = null;
	private String position = "";
	private String id = "";
	private ArrayList<String> name = null;
	private float quantity = 0f;
	
	public BlockLineAttributes(){
		this.format = new BLFormat("");
		this.zone = new BLZone("");
		this.remark = new BLRemark("");
		this.name = new ArrayList<String>(2);			
	}
	
	public void setFormat(String format) {
		this.format = new BLFormat(format);
	}
	
	public void setZone(String zone) {
		if(this.zone==null){
			this.zone = new BLZone(zone);
		} else {
			this.zone.addZone(zone);
		}
	}
	
	public void setRemark(String remark){
		if(this.remark==null) this.remark = new BLRemark();
		this.remark.insert(remark);
	}
	
	public void createKits(){
		if(this.kits==null)	this.kits = new BLKits();
	}
	
	public void addKit(String id, String name, double qty){
		if(kits==null) this.kits = new BLKits();
		this.kits.addKit(id, name, qty);
		
	}
	
	public void addKit(BLKits kit){
		this.kits.addKits(kit);
	}
	
	public BLKits getKits(){
		return this.kits;
	}
	
	public void setPosition(String position) {
		this.position = position;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public void setName(String name) {
		this.name = LineUtil.getFittedLines(name, SpecificationSettings.columnLengths.get(FormField.NAME));
	}
	
	public void setQuantity(String quantity) {
		this.quantity = "".equals(quantity) ? 1 : Float.parseFloat(quantity);
	}
	
	public void addQuantity(String quantity){
		this.quantity = this.quantity + ("".equals(quantity) ? 1 : Float.parseFloat(quantity));
	}
	
	public BLFormat getFormat() {
		return format;
	}
	public BLZone getZone() {
		return zone;
	}
	public String getPosition() {
		return position;
	}
	public String getId() {
		return id;
	}
	public ArrayList<String> getName() {
		return name;
	}
	public float getQuantity() {
		return quantity;
	}
	public BLRemark getRemark() {
		if(this.remark==null) remark = new BLRemark();
		return remark;
	}
	
	public String getStringValueFromField(FormField field) {
		switch(field) {
			case FORMAT: return format.toString();
			case ZONE: return zone.toString();
			case POSITION: return position;
			case ID: return id;
			case NAME: return Arrays.toString(name.toArray());
			case QUANTITY: return quantity==0?" ":(quantity%1==0) ? Integer.toString((int)quantity) : String.format("%.3f", quantity);
			case REMARK: return remark.toString();
		}
		return "";
	}
}
