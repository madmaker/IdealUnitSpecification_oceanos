package ru.idealplm.specification.blockline.attributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import ru.idealplm.specification.core.Specification.FormField;
import ru.idealplm.specification.core.SpecificationSettings;

public class BLFormat {
	
	public boolean exceedsLimit = false;
	private ArrayList<String> formatsList = new ArrayList<String>(1);

	static class DocFormatComparator implements Comparator<String>{

		@Override
		public int compare(String s0, String s1) {
			int arg0len, arg1len;

			arg0len = s0.length();
			arg1len = s1.length();
			
			if(Integer.valueOf(s0.charAt(1))==Integer.valueOf(s1.charAt(1))){
				if(arg0len > arg1len) return 1;
				if(arg0len < arg1len) return -1;
				if(Integer.valueOf(s0.charAt(3)) > Integer.valueOf(s1.charAt(3))) return 1;
				if(Integer.valueOf(s0.charAt(3)) < Integer.valueOf(s1.charAt(3))) return -1;
			} else {
				return (Integer.valueOf(s0.charAt(1)) > Integer.valueOf(s1.charAt(1))) ? -1 : 1 ;
			}
			return 0;
		}
		
	}
	
	public BLFormat(String format) {
		if(!format.equals("*)")){
			if(format.length() > SpecificationSettings.columnLengths.get(FormField.FORMAT)-1) exceedsLimit = true;
			for(String f : format.split(",")){
				if(!containsFormat(f.trim())) formatsList.add(f.trim());
			}
		} else {
			formatsList.add(format);
		}
	}
	
	public ArrayList<String> toStringList(){
		return formatsList;
	}
	
	public boolean containsFormat(String format) {
		boolean result = formatsList.contains(format);
		return result;
	}
	
	@Override
	public String toString() {
		Collections.sort(formatsList, new DocFormatComparator());
		Iterator<String> it = formatsList.iterator();
		StringBuilder sb = new StringBuilder();
		if(exceedsLimit && it.hasNext()) sb.append("*) " + it.next() + (it.hasNext()?", ":""));
		while(it.hasNext()){
			sb.append(it.next() + (it.hasNext()?", ":""));
		}
		return sb.toString().trim();
	}
}
