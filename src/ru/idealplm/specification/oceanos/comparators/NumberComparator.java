package ru.idealplm.specification.oceanos.comparators;

import ru.idealplm.specification.blockline.BlockLine;
import ru.idealplm.specification.core.BlockLineComparator;
import ru.idealplm.specification.core.Specification;

public class NumberComparator implements BlockLineComparator {

	Specification.FormField sortField;
	public NumberComparator(Specification.FormField sortField){
		this.sortField = sortField;
	}
	
	@Override
	public int compare(BlockLine line0, BlockLine line1) {
		int field0;
		int field1;
		try{
			field0 = Integer.parseInt(line0.attributes.getStringValueFromField(sortField));
		} catch (NumberFormatException ex) {
			return -1;
		}
		try{
			field1 = Integer.parseInt(line1.attributes.getStringValueFromField(sortField));
		} catch (NumberFormatException ex) {
			return 1;
		}
		return field0 < field1 ? -1 : field0 == field1 ? 0 : 1;
	}

}
