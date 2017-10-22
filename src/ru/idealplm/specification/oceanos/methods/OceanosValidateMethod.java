package ru.idealplm.specification.oceanos.methods;

import java.util.ArrayList;
import java.util.Arrays;

import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;

import ru.idealplm.specification.core.Error;
import ru.idealplm.specification.core.Specification;
import ru.idealplm.specification.methods.IValidateMethod;

public class OceanosValidateMethod implements IValidateMethod
{
	private Specification specification = Specification.getInstance();
	
	@Override
	public boolean validateData() {
		System.out.println("...METHOD... ValidateMethod");
		ArrayList<String> acceptableTypesOfPart = new ArrayList<String>(Arrays.asList("Сборочная единица", "Комплект", "Комплекс"));
		
		TCComponentBOMLine topBOMLine = specification.getTopBOMLine();
		
		if(topBOMLine==null){
			Specification.errorList.addError(new Error("ERROR", "Отсутствует состав для построения спецификации"));
			return false;
		}
		
		try{
			TCComponentItem item = topBOMLine.getItem();
			if(!"Oc9_CompanyPart".equals(item.getType())){
				Specification.errorList.addError(new Error("ERROR", "Недопустимый вид изделия!"));
				return false;
			} else if(!acceptableTypesOfPart.contains(item.getProperty("oc9_TypeOfPart"))){
				Specification.errorList.addError(new Error("ERROR", "Недопустимый тип изделия!"));
				return false;
			}
		}catch(Exception ex){
			ex.printStackTrace();
			return false;
		}
		
		return true;
		
	}

}
