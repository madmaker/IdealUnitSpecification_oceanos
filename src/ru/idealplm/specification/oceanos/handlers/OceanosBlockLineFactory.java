package ru.idealplm.specification.oceanos.handlers;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;

import ru.idealplm.specification.oceanos.handlers.linehandlers.OceanosBlockLineHandler;
import ru.idealplm.utils.specification.blockline.BlockLine;
import ru.idealplm.utils.specification.BlockLineFactory;
import ru.idealplm.utils.specification.Error;
import ru.idealplm.utils.specification.Specification;
import ru.idealplm.utils.specification.Specification.BlockContentType;
import ru.idealplm.utils.specification.Specification.BlockType;

public class OceanosBlockLineFactory extends BlockLineFactory{
	
	private final String[] blProps = new String[] { 
			"Oc9_Zone",
			"bl_sequence_no",
			"bl_quantity",
			"Oc9_Note",
			"Oc9_IsFromEAssembly", //у вхождений с одинаковым sequence_no должно быть одинаковое значение
			"Oc9_DisChangeFindNo", //у вхождений с одинаковым sequence_no должно быть одинаковое значение
			"oc9_KITName",
			"bl_item_uom_tag",
			"Oc9_KITs"
	};

	@Override
	public BlockLine newBlockLine(TCComponentBOMLine bomLine) {
		try{
			TCComponent item = bomLine.getItem();
			TCComponentItemRevision itemIR = bomLine.getItemRevision();
			String uid = itemIR.getUid();
			String[] properties = bomLine.getProperties(blProps);
			boolean isDefault = properties[4].trim().isEmpty();
			
			//System.out.println("_processing by processor " + id + " *** *** " + bomLine.getItem().getType() + " --> " + Arrays.toString(properties));
			//TODO validateBOMLineAttributes(properties[1], properties[4], properties[5]);
			
			OceanosBlockLineHandler blockLineHandler = new OceanosBlockLineHandler();
			BlockLine resultBlockLine = new BlockLine(blockLineHandler);
			resultBlockLine.attributes.setZone(properties[0]);
			resultBlockLine.attributes.setPosition(properties[1]);
			resultBlockLine.isRenumerizable = properties[5].trim().equals("");
			System.out.println("DISAB["+properties[5]+"]" + " so "+resultBlockLine.isRenumerizable);
			resultBlockLine.uid = uid;
			
			if(item.getType().equals("Oc9_CompanyPart")){
				if(!properties[8].isEmpty()){
					Specification.errorList.addError(new Error("ERROR", "Объект с идентификатором " + item.getProperty("item_id") + " имеет ссылку на комплект."));
				}
				String typeOfPart = item.getProperty("oc9_TypeOfPart");
				if(typeOfPart.equals("Сборочная единица") || typeOfPart.equals("Комплекс")){
					/*********************** Сборки и Комплексы ***********************/
					AIFComponentContext[] relatedDocs = bomLine.getItemRevision().getRelated("Oc9_DocRel");
					String itemID = bomLine.getItem().getProperty("item_id");
					String format = getFormat(itemID, relatedDocs);
					resultBlockLine.attributes.setFormat(format);
					resultBlockLine.attributes.setId(item.getProperty("item_id"));
					resultBlockLine.attributes.setName(itemIR.getProperty("object_name"));
					resultBlockLine.attributes.setQuantity(properties[2]);
					resultBlockLine.attributes.setRemark(properties[3]);
					resultBlockLine.addRefBOMLine(bomLine);
					if(typeOfPart.equals("Сборочная единица")){
						resultBlockLine.blockContentType = BlockContentType.ASSEMBLIES;
						//blockList.getBlock(BlockContentType.ASSEMBLIES, isDefault?"Default":"ME").addBlockLine(uid, resultBlockLine);
					} else {
						resultBlockLine.blockContentType = BlockContentType.COMPLEXES;
						//blockList.getBlock(BlockContentType.COMPLEXES, isDefault?"Default":"ME").addBlockLine(uid, resultBlockLine);
					}
					resultBlockLine.blockType = isDefault?BlockType.DEFAULT:BlockType.ME;
				} else if(typeOfPart.equals("Деталь")){
					/*****************************Детали*********************************/
					boolean hasDraft = false;
					AIFComponentContext[] relatedDocs = bomLine.getItemRevision().getRelated("Oc9_DocRel");
					String itemID = bomLine.getItem().getProperty("item_id");
					String format = getFormat(itemID, relatedDocs);
					if(!format.equals("БЧ")){
						hasDraft = true;
					}
					resultBlockLine.attributes.setFormat(format);
					if(!hasDraft){
						if(itemIR.getProperty("oc9_CADMaterial").equals("")){
							Specification.errorList.addError(new Error("ERROR", "У БЧ-детали с идентификатором " + item.getProperty("item_id") + " не заполнен атрибут \"Исходный материал\""));
						}
						resultBlockLine.attributes.setName(itemIR.getProperty("object_name") + "\n" + itemIR.getProperty("oc9_CADMaterial") + " " + itemIR.getProperty("oc9_AddNote"));
					} else {
						resultBlockLine.attributes.setName(itemIR.getProperty("object_name"));
					}
					resultBlockLine.attributes.setId(item.getProperty("item_id"));
					resultBlockLine.attributes.setQuantity(properties[2]);
					if(hasDraft){
						resultBlockLine.attributes.setRemark(properties[3]);
					} else {
						if(!itemIR.getProperty("oc9_mass").trim().equals("")) {
							resultBlockLine.attributes.setRemark(itemIR.getProperty("oc9_mass") + " кг"/* + properties[3]*/);
							resultBlockLine.attributes.getRemark().insert(properties[3]);
						} else {
							resultBlockLine.attributes.setRemark(properties[3]);
						}
					}
					resultBlockLine.addRefBOMLine(bomLine);
					AIFComponentContext[] relatedBlanks = bomLine.getItemRevision().getRelated("Oc9_StockRel");
					if(relatedBlanks.length>0){
						BlockLine blank = new BlockLine(blockLineHandler);
						TCComponentItem blankItem = (TCComponentItem)relatedBlanks[0].getComponent();
						blank.attributes.setPosition("-");
						blank.attributes.setName(blankItem.getLatestItemRevision().getProperty("object_name") + " " + "Изделие-заготовка для " + resultBlockLine.attributes.getId());
						if(!blankItem.getType().equals("CommercialPart")){
							String blankItemID = blankItem.getProperty("item_id");
							relatedDocs = ((TCComponentItem)relatedBlanks[0].getComponent()).getLatestItemRevision().getRelated("Oc9_DocRel");
							String blankFormat = getFormat(blankItemID, relatedDocs);
							if(!format.equals("БЧ")){
								blank.attributes.setFormat(blankFormat);
							}
							blank.attributes.setId(relatedBlanks[0].getComponent().getProperty("item_id"));
						}
						resultBlockLine.getAttachedLines().add(blank);
					}
					resultBlockLine.blockContentType = BlockContentType.DETAILS;
					resultBlockLine.blockType = isDefault?BlockType.DEFAULT:BlockType.ME;
					//blockList.getBlock(BlockContentType.DETAILS, isDefault?"Default":"ME").addBlockLine(uid, resultBlockLine);
				} else if(typeOfPart.equals("Комплект")){
					/****************************Комплекты********************************/
					resultBlockLine.attributes.setId(item.getProperty("item_id"));
					resultBlockLine.attributes.setName(itemIR.getProperty("object_name"));
					resultBlockLine.attributes.setQuantity(properties[2]);
					resultBlockLine.addRefBOMLine(bomLine);
					resultBlockLine.blockContentType = BlockContentType.KITS;
					resultBlockLine.blockType = isDefault?BlockType.DEFAULT:BlockType.ME;
					//blockList.getBlock(BlockContentType.KITS, isDefault?"Default":"ME").addBlockLine(uid, resultBlockLine);
				} else if(typeOfPart.equals("")){
					Specification.errorList.addError(new Error("ERROR", "У вхождения с обозначением " + properties[2] + "отсутствует значение свойства \"Тип изделия\""));
				}
			} else if(item.getType().equals("CommercialPart")){
				/****************************Коммерческие********************************/
				resultBlockLine.attributes.setName(item.getProperty("oc9_RightName"));
				if(item.getProperty("oc9_RightName").equals("Наименование не согласовано")){
					resultBlockLine.addProperty("bNameNotApproved", "true");
				}
				resultBlockLine.attributes.setQuantity(properties[2]);
				resultBlockLine.attributes.setRemark(properties[3]);
				resultBlockLine.addRefBOMLine(bomLine);
				if(!properties[8].isEmpty()){
					resultBlockLine.attributes.createKits();
					resultBlockLine.attributes.addKit(properties[8], properties[6], properties[2].isEmpty()?1:Integer.parseInt(properties[2]));
				}
				if(item.getProperty("oc9_TypeOfPart").equals("Прочее изделие")){
					resultBlockLine.blockContentType = BlockContentType.OTHERS;
					resultBlockLine.blockType = isDefault?BlockType.DEFAULT:BlockType.ME;
					//blockList.getBlock(BlockContentType.OTHERS, isDefault?"Default":"ME").addBlockLine(uid, resultBlockLine);
				} else {
					resultBlockLine.blockContentType = BlockContentType.STANDARDS;
					resultBlockLine.blockType = isDefault?BlockType.DEFAULT:BlockType.ME;
					//blockList.getBlock(BlockContentType.STANDARDS, isDefault?"Default":"ME").addBlockLine(uid, resultBlockLine);
				}
			} else if(item.getType().equals("Oc9_Material")){
				/****************************Материалы********************************/
				String seCutLength = bomLine.getProperty("SE Cut Length");
				resultBlockLine.attributes.setName(item.getProperty("oc9_RightName") + (seCutLength.isEmpty()?"":(" L="+seCutLength)));
				if(item.getProperty("oc9_RightName").equals("Наименование не согласовано")){
					resultBlockLine.addProperty("bNameNotApproved", "true");
				}
				resultBlockLine.attributes.setQuantity(properties[2]);
				resultBlockLine.addRefBOMLine(bomLine);
				resultBlockLine.addProperty("UOM", properties[7]);
				resultBlockLine.addProperty("SE Cut Length", seCutLength);
				resultBlockLine.addProperty("CleanName", item.getProperty("oc9_RightName"));
				resultBlockLine.addProperty("FromGeomMat", "");
				resultBlockLine.addProperty("FromMat", "true");
				if(!seCutLength.isEmpty() && !properties[7].equals("*")){
					Specification.errorList.addError(new Error("ERROR", "У материала с идентификатором " + item.getProperty("item_id") + " единицы измерения отличны от шт."));
				}
				if(!properties[8].isEmpty()){
					resultBlockLine.attributes.createKits();
					resultBlockLine.attributes.addKit(properties[8], properties[6], properties[2].isEmpty()?1:Integer.parseInt(properties[2]));
				}
				resultBlockLine.blockContentType = BlockContentType.MATERIALS;
				resultBlockLine.blockType = isDefault?BlockType.DEFAULT:BlockType.ME;
			} else if(item.getType().equals("Oc9_GeomOfMat")){
				/*************************Геометрии материалов****************************/
				AIFComponentContext[] materialBOMLines = bomLine.getChildren();
				if(materialBOMLines.length>0){
					if(materialBOMLines.length>1){
						Specification.errorList.addError(new Error("ERROR", "В составе геометрии материала с идентификатором " + item.getProperty("item_id") + " присутствует более одного материала."));
					}
					TCComponentItemRevision materialIR = ((TCComponentBOMLine) materialBOMLines[0].getComponent()).getItemRevision();
					String quantityMS = ((TCComponentBOMLine) materialBOMLines[0].getComponent()).getProperty("bl_quantity");
					float quantityMD = Float.parseFloat(quantityMS.equals("")?"1":quantityMS);
					int quantotyGD = Integer.parseInt(properties[2].equals("")?"1":properties[2]);
					String uom = ((TCComponentBOMLine) materialBOMLines[0].getComponent()).getProperty("bl_item_uom_tag");
					uom = uom.equals("*")?"":uom;
					resultBlockLine.attributes.setName(materialIR.getItem().getProperty("oc9_RightName"));
					resultBlockLine.attributes.setQuantity(String.valueOf(quantityMD*quantotyGD));
					resultBlockLine.attributes.setRemark(uom);
					resultBlockLine.addRefBOMLine(bomLine);
					resultBlockLine.addProperty("SE Cut Length", "");
					resultBlockLine.addProperty("CleanName", materialIR.getItem().getProperty("oc9_RightName"));
					resultBlockLine.addProperty("FromGeomMat", "true");
					resultBlockLine.addProperty("FromMat", "");
					resultBlockLine.uid = materialIR.getUid();
					resultBlockLine.blockContentType = BlockContentType.MATERIALS;
					resultBlockLine.blockType = isDefault?BlockType.DEFAULT:BlockType.ME;
				} else {
					Specification.errorList.addError(new Error("ERROR", "В составе геометрии материала с идентификатором " + item.getProperty("item_id") + " отсутствует материал."));
				}
			}
			return resultBlockLine;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	public String getFormat(String itemID, AIFComponentContext[] relatedDocuments){
		String format = "БЧ";
		try{
			for(AIFComponentContext relatedDoc : relatedDocuments){
				String docID = relatedDoc.getComponent().getProperty("item_id");
				String docFormat = ((TCComponentItem)relatedDoc.getComponent()).getLatestItemRevision().getProperty("oc9_Format");
				if(docID.equals(itemID)){
					format = docFormat;
					break;
				} else if(itemID.contains("-")) {
					if(docID.equals(itemID.substring(0, itemID.lastIndexOf("-")))){
						format = docFormat;
					} else if(docID.contains("-")) {
						if(docID.substring(0, docID.lastIndexOf("-")).equals(itemID.substring(0, itemID.lastIndexOf("-")))){
							format = docFormat;
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return format;
	}

}
