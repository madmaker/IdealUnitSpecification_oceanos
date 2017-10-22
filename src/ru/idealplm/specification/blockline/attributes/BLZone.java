package ru.idealplm.specification.blockline.attributes;

import java.util.ArrayList;
import java.util.Iterator;

import ru.idealplm.specification.core.Specification.FormField;
import ru.idealplm.specification.core.SpecificationSettings;

public class BLZone {
	
	public boolean exceedsLimit = false;
	private ArrayList<String> docZone = new ArrayList<String>(1);
	
	public BLZone(String zone) {
		if(zone.isEmpty()) return;
		if(!zone.equals("*)")){
			if(zone.length() > SpecificationSettings.columnLengths.get(FormField.ZONE)-1) exceedsLimit = true;
			for(String f : zone.split(",")){
				if(!containsZone(f.trim())) docZone.add(f.trim());
			}
		} else {
			docZone.add(zone);
		}
	}
	
	public void addZone(String zone){
		if(zone.isEmpty()) return;
		if(zone.equals("*)")) {
			System.out.println("Clearing zone");
			for(String v : docZone){
				System.out.println("-"+v);
			}
			docZone.clear();
			docZone.add("*)");
			System.out.println("Afeter Clearing zone");
			for(String v : docZone){
				System.out.println("-"+v);
			}
			return;
		}
		if(zone.length() > SpecificationSettings.columnLengths.get(FormField.ZONE)-1) exceedsLimit = true;
		for(String f : zone.split(",")){
			if(!containsZone(f.trim())) docZone.add(f.trim());
		}
		if(docZone.size()>1) exceedsLimit = true;
	}
	public void addZone(BLZone blzone){
		for(String zone:blzone.toStringList()){
			addZone(zone);
		}
	}
	
	public ArrayList<String> toStringList(){
		return docZone;
	}
	
	public boolean containsZone(String zone) {
		boolean result = docZone.contains(zone);
		return result;
	}
	
	@Override
	public String toString() {
		if(docZone.size()==1 && docZone.get(0).equals("*)")) return "*)";
		Iterator<String> it = docZone.iterator();
		StringBuilder sb = new StringBuilder();
		if(exceedsLimit && it.hasNext()) sb.append("*) " + it.next() + (it.hasNext()?", ":""));
		while(it.hasNext()){
			sb.append(it.next() + (it.hasNext()?", ":""));
		}
		return sb.toString().trim();
	}
}
