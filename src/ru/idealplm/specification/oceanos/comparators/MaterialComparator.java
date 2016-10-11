package ru.idealplm.specification.oceanos.comparators;

import java.util.Comparator;

import ru.idealplm.utils.specification.blockline.BlockLine;
import ru.idealplm.utils.specification.Specification.FormField;

public class MaterialComparator implements Comparator<BlockLine>{

	@Override
	public int compare(BlockLine line0, BlockLine line1) {
		
		
		String field0;
		String field1;
		String cutLength0 = line0.getProperty("SE Cut Length");
		String cutLength1 = line1.getProperty("SE Cut Length");
		if(!cutLength0.isEmpty() && cutLength1.isEmpty()){
			System.out.println("0>1");
			return -1;
		} else if (cutLength0.isEmpty() && !cutLength1.isEmpty()) {
			System.out.println("0<1");
			return 1;
		}
		if(!cutLength0.isEmpty() && !cutLength1.isEmpty()){
			field0 = line0.getProperty("CleanName") + line0.uid + cutLength0;
			field1 = line1.getProperty("CleanName") + line1.uid + cutLength1;
			System.out.println("...comparing "+ field0 + " and " + field1);
		} else {
			field0 = line0.getProperty("CleanName");
			field1 = line1.getProperty("CleanName");
		}
			/*
			field0 = cutLength0.replaceAll("[^0-9.]+", "");
			field1 = cutLength1.replaceAll("[^0-9.]+", "");
			if(field0.isEmpty() && field1.isEmpty()){
				return 0;
			} else if(field0.isEmpty() && !field1.isEmpty()){
				return -1;
			} else if(!field0.isEmpty() && field1.isEmpty()){
				return 1;
			} else {
				double f0 = Double.parseDouble(field0);
				double f1 = Double.parseDouble(field1);
				if(f0==f1) return 0;
				return f0<f1?-1:1;
			}
		}*/
		
		
		int result = 0;

		int len, arg0len, arg1len;

		arg0len = field0.length();
		arg1len = field1.length();

		if (arg0len == arg1len)
			len = arg0len;
		if (arg0len > arg1len)
			len = arg1len;
		else
			len = arg0len;

		int i;
		boolean isEq = true;
		for (i = 0; (i < len) && (isEq); i++)
			if (field0.charAt(i) != field1.charAt(i))
				isEq = false;

		if (i == field0.length() && isEq)
			return -1;
		if (i == field1.length() && isEq)
			return 1;

		i--;
		boolean isArg0Digit = Character.isDigit(field0.charAt(i));
		boolean isArg1Digit = Character.isDigit(field1.charAt(i));

		if (isArg0Digit && isArg1Digit && i > 0) {
			while (((Character.isDigit(field0.charAt(i))) || (Character.isDigit(field1.charAt(i)))) && i>0)
				i--;
			i++;

			if (Integer.valueOf(getCompString(field0, i)) < Integer.valueOf(getCompString(field1, i)))
				result = -1;
			else
				result = 1;
		} else {
			if (field0.charAt(i) < field1.charAt(i))
				result = -1;
			else
				result = 1;
		}
		
		return result;
	}
	
	private String getCompString(String arg0, int i) {
		boolean isNumericArg0 = Character.isDigit(arg0.charAt(i));
		boolean controlState = isNumericArg0;
		String cStringArg0 = "";

		for (int j = i; (isNumericArg0 == controlState) && (j < arg0.length()); j++) {
			if ((Character.isDigit(arg0.charAt(j)) && isNumericArg0) || (!Character.isDigit(arg0.charAt(j)) && !isNumericArg0))
				cStringArg0 = cStringArg0 + arg0.charAt(j);
			else
				controlState = !controlState;
		}

		return cStringArg0;
	}

}
