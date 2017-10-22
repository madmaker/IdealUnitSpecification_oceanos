package ru.idealplm.specification.oceanos.comparators;

import java.util.Comparator;

import ru.idealplm.specification.blockline.BlockLine;
import ru.idealplm.specification.core.Specification.FormField;

public class DetailComparator implements Comparator<BlockLine> {

	@Override
	public int compare(BlockLine bl0, BlockLine bl1) {
		
		String format0 = bl0.attributes.getStringValueFromField(FormField.FORMAT);
		String format1 = bl1.attributes.getStringValueFromField(FormField.FORMAT);
		if(format0.equals("��") && !format1.equals("")){
			return 1;
		}  else if (!format0.equals("��") && format1.equals("��")){
			return -1;
		}
		
		DefaultComparator dc = new DefaultComparator(FormField.ID);
		
		return dc.compare(bl0, bl1);
	}

}
