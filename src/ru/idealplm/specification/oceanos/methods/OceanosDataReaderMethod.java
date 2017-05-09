package ru.idealplm.specification.oceanos.methods;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.services.rac.cad.StructureManagementService;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.CreateBOMWindowsInfo;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSData;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelInfo;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelOutput;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelPref;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelResponse;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.RelationAndTypesFilter;

import ru.idealplm.specification.oceanos.handlers.OceanosBlockLineFactory;
import ru.idealplm.specification.oceanos.handlers.linehandlers.OceanosBlockLineHandler;
import ru.idealplm.specification.oceanos.util.PerfTrack;
import ru.idealplm.utils.specification.blockline.BlockLine;
import ru.idealplm.utils.specification.BlockList;
import ru.idealplm.utils.specification.Error;
import ru.idealplm.utils.specification.Specification;
import ru.idealplm.utils.specification.Specification.BlockContentType;
import ru.idealplm.utils.specification.Specification.BlockType;
import ru.idealplm.utils.specification.Specification.FormField;
import ru.idealplm.utils.specification.methods.IDataReaderMethod;
import ru.idealplm.utils.specification.util.GeneralUtils;

public class OceanosDataReaderMethod implements IDataReaderMethod
{	
	public ProgressMonitorDialog pd;
	private Specification specification = Specification.getInstance();
	private StructureManagementService smsService = StructureManagementService.getService(specification.session);
	private BlockList blockList;
	private ArrayList<String> bl_sequence_noList;
	private HashMap<String, Boolean> oc9_IsFromEAsmList;
	private HashMap<String, Boolean> oc9_DisableChangeFindNoList;
	private ArrayList<String> docTypesShort;
	private ArrayList<String> docTypesLong;
	private ArrayList<String> docKitTypesShort;
	private ArrayList<String> docKitTypesLong;
	private HashMap<String, BlockLine> materialUIDs;
	private HashMap<String, BlockLine> uids;
	private HashMap<String, BlockLine> uidsSubstitute;
	private OceanosBlockLineFactory blFactory;
	private TCComponentBOMLine topBOMLine;
	private TCComponentBOMLine bomLine;
	
	public OceanosDataReaderMethod() {
		bl_sequence_noList = new ArrayList<String>();
		oc9_IsFromEAsmList = new HashMap<String, Boolean>();
		oc9_DisableChangeFindNoList = new HashMap<String, Boolean>();
		docTypesShort = new ArrayList<String>();
		docTypesLong = new ArrayList<String>();
		docKitTypesShort = new ArrayList<String>();
		docKitTypesLong = new ArrayList<String>();
		materialUIDs = new HashMap<String, BlockLine>();
		uids = new HashMap<String, BlockLine>();
		uidsSubstitute = new HashMap<String, BlockLine>();
		blFactory = new OceanosBlockLineFactory();
	}
	
	boolean atLeastOnePosIsFixed = false;
	boolean atLeastOneME = false;
	boolean hasPrevRev = false;
	
	private void parseBOMLineData(TCComponentBOMLine bomLine)
	{
		try
		{
			BlockLine line = blFactory.newBlockLine(bomLine);
			line.isSubstitute = false;
			uids.put(line.uid, line);
			for(TCComponentBOMLine comp : bomLine.listSubstitutes())
			{
				BlockLine substituteLine = blFactory.newBlockLine(comp);
				substituteLine.attributes.setPosition(line.attributes.getPosition()+"*");
				substituteLine.attributes.setQuantity("-1");
				substituteLine.isSubstitute = true;
				line.addSubstituteBlockLine(substituteLine);
				uidsSubstitute.put(substituteLine.uid, substituteLine);
			}
			if(line.blockType == BlockType.ME) atLeastOneME = true;
			if(!line.isRenumerizable) {
				atLeastOnePosIsFixed = true;
				System.out.println("NOTRENUM:"+line.attributes.getId());
			}
			validateBOMLineAttributess(line);
			
			if(line.blockContentType == BlockContentType.MATERIALS){
				if(materialUIDs.containsKey(line.uid+line.getProperty("SE Cut Length"))){
						BlockLine storedLine = materialUIDs.get(line.uid+line.getProperty("SE Cut Length"));
						if(storedLine.getProperty("FromGeomMat").isEmpty()){
							storedLine.addProperty("FromGeomMat", line.getProperty("FromGeomMat"));
						}
						if(storedLine.getProperty("FromMat").isEmpty()){
							storedLine.addProperty("FromMat", line.getProperty("FromMat"));
						}
						if(line.getProperty("SE Cut Length").isEmpty() && storedLine.getProperty("SE Cut Length").isEmpty()){
							// If both are null, then we just update stored line attributes with current line attributes
							storedLine.attributes.createKits();
							storedLine.attributes.addKit(line.attributes.getKits());
							storedLine.addRefBOMLine(line.getRefBOMLines().get(0));
							storedLine.attributes.addQuantity(line.attributes.getStringValueFromField(FormField.QUANTITY));
						} else if(!line.getProperty("SE Cut Length").isEmpty() && !storedLine.getProperty("SE Cut Length").isEmpty()) {
							// If both have different not null SE Cut Length attribute values 
							// then we just update stored line attributes with current line attributes
							storedLine.attributes.createKits();
							storedLine.attributes.addKit(line.attributes.getKits());
							storedLine.addRefBOMLine(line.getRefBOMLines().get(0));
							storedLine.attributes.addQuantity(line.attributes.getStringValueFromField(FormField.QUANTITY));
						} else {
							// If one is empty and other one is not, then we have an error
							// It is not allowed for the same material to have 2 occurences,
							// where one of them does have SE Cut Length attribute and the other one doesn't
							specification.getErrorList().addError(new Error("ERROR", "Отсутствует значение атрибута SE Cut Length для материала с именем "+line.attributes.getStringValueFromField(FormField.ID)));
						}
				} else {
					materialUIDs.put(line.uid+line.getProperty("SE Cut Length"), line);
					blockList.getBlock(line.blockContentType, line.blockType).addBlockLine(line.uid+line.getProperty("SE Cut Length"), line);
				}
			} else {
				blockList.getBlock(line.blockContentType, line.blockType).addBlockLine(line.uid, line);
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}
	
	private void readBOMData(IProgressMonitor monitor)
	{
		ExpandPSOneLevelInfo levelInfo = new ExpandPSOneLevelInfo();
		ExpandPSOneLevelPref levelPref = new ExpandPSOneLevelPref();

		levelInfo.parentBomLines = new TCComponentBOMLine[] { bomLine };
		levelInfo.excludeFilter = "None";
		levelPref.expItemRev = true;
		levelPref.info = new RelationAndTypesFilter[0];

		ExpandPSOneLevelResponse levelResp = smsService.expandPSOneLevel(levelInfo, levelPref);

		if (levelResp.output.length > 0)
		{
			monitor.beginTask("Чтение данных структуры сборки", levelResp.output.length);
			for (ExpandPSOneLevelOutput levelOut : levelResp.output)
			{
				for (ExpandPSData psData : levelOut.children)
				{
					parseBOMLineData(psData.bomLine);
					monitor.worked(1);
					if(monitor.isCanceled())
					{
						throw new CancellationException("Чтение данных структуры сборки было отменено");
					}
				}
			}
			monitor.done();
		}
	}

	@Override
	public void readData() {
		try{
			loadDocumentTypes();
			blockList = specification.getBlockList();
			topBOMLine = specification.getTopBOMLine();
			
			PerfTrack.prepare("Getting BOM");
			
			TCComponentItem topItem = topBOMLine.getItem();
			TCComponentItemRevision topItemR = topBOMLine.getItemRevision();
			TCComponent[] revisions = topItem.getRelatedComponents("revision_list");
			for(int i = 0; i < revisions.length; i++){
				if(revisions[i].getUid().equals(topItemR.getUid()) && i>0){
					hasPrevRev = true;
					break;
				}
			}
			
			CreateBOMWindowsInfo bomWinInfo = new CreateBOMWindowsInfo();
										
			bomWinInfo.item = topItem;
			bomWinInfo.itemRev = topItemR;
			bomWinInfo.bomView = topBOMLine.getBOMView();
									       
			StructureManagementService.CreateBOMWindowsResponse bomResp = smsService.createBOMWindows(new CreateBOMWindowsInfo[] { bomWinInfo });
			
			if(bomResp.output.length > 0)
			{
				bomLine = bomResp.output[0].bomLine;
			}
			
			try {
				pd.run(true, true, new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						monitor.beginTask("Чтение данных", 100);
						readSpecifiedItemData(bomLine);
						readTopIRDocuments(bomLine);
						readGeneralNoteForm();
						readBOMData(monitor);
						monitor.done();
					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();
			} catch (CancellationException ex) {
				System.out.println(ex.getMessage());
			}
			
			BlockList tempList = new BlockList();
			for(int i = 0; i < blockList.size(); i++){
				if(blockList.get(i).size()!=0) {
					tempList.addBlock(blockList.get(i));
					for(BlockLine line:blockList.get(i).getListOfLines()){
						line.build();
					}
				}
			}
			
			Iterator<Entry<String, BlockLine>> it = materialUIDs.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry<String, BlockLine> pair = (Map.Entry<String, BlockLine>)it.next();
		        BlockLine line = pair.getValue();
		        if(!line.getProperty("FromMat").isEmpty() && !line.getProperty("FromGeomMat").isEmpty() && !line.getProperty("SE Cut Length").isEmpty()){
		        	specification.getErrorList().addError(new Error("ERROR", "Материал с заполненным атрибутом SE Cut Length дублируется в составе геометрии материала. Имя: " + line.attributes.getStringValueFromField(FormField.NAME)));
		        }
		        System.out.println(pair.getKey() + " = " + pair.getValue());
		        it.remove(); // avoids a ConcurrentModificationException
		    }
			
			specification.setBlockList(tempList);
			if(tempList.size()==0){
				specification.getErrorList().addError(new Error("ERROR", "Отсутствуют разделы спецификации."));
			}
			if(atLeastOneME && Specification.settings.getStringProperty("MEDocumentId")==null){
				specification.getErrorList().addError(new Error("ERROR", "Отсутствует документ МЭ."));
			}
			for(Entry<String,BlockLine> entry:uidsSubstitute.entrySet()){
				if(uids.containsKey(entry.getKey()))	{
					specification.getErrorList().addError(new Error("ERROR", "Объект с идентификатором " + entry.getValue().attributes.getId() + " присутствует в составе и заменах одновременно."));
				}
			}
			
			Specification.settings.addBooleanProperty("canRenumerize", !atLeastOnePosIsFixed);
			Specification.settings.addBooleanProperty("canUseReservePos", atLeastOnePosIsFixed && hasPrevRev);
			Specification.settings.addBooleanProperty("canReadLastRevPos", !atLeastOnePosIsFixed && hasPrevRev);
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}

	
	private synchronized void validateBOMLineAttributess(BlockLine line){
		if(line.blockType==BlockType.ME) atLeastOneME = true;
		if(atLeastOneME) Specification.settings.addBooleanProperty("hasMEBlocks", true);
		String bl_sequence_no = line.attributes.getPosition();
		int posInList = bl_sequence_noList.indexOf(bl_sequence_no);
		if(posInList==-1){
			bl_sequence_noList.add(bl_sequence_no);
			oc9_IsFromEAsmList.put(bl_sequence_no, line.blockType!=BlockType.ME);
			oc9_DisableChangeFindNoList.put(bl_sequence_no, line.isRenumerizable);
		} else {
			if(oc9_IsFromEAsmList.get(bl_sequence_no)!=(line.blockType!=BlockType.ME)){
				this.specification.getErrorList().addError(new Error("ERROR", "У вхождений с номером позиции "+bl_sequence_no+"разные значения свойства \"Позиция из МЭ\""));
			} else if (oc9_DisableChangeFindNoList.get(bl_sequence_no)!=line.isRenumerizable){
				this.specification.getErrorList().addError(new Error("ERROR", "У вхождений с номером позиции "+bl_sequence_no+"разные значения свойства \"Запрет смены позиции\""));
			}
		}
	}
	
	private void readTopIRDocuments(TCComponentBOMLine bomLine){
		try {
			
			TCComponentItemRevision topIR = bomLine.getItemRevision();
			TCComponent[] documents = topIR.getRelatedComponents("Oc9_DocRel");
			TCComponentItemRevision documentIR;
			String IRid = topIR.getItem().getProperty("item_id");
			String uid;
			String format;
			String id;
			String name;
			String object_name;
			String shortType;
			boolean isBaseDoc = true;
			boolean gostNameIsFalse;
			OceanosBlockLineHandler blockLineHandler = new OceanosBlockLineHandler();
			
			for(TCComponent document : documents){
				documentIR = ((TCComponentItem)document).getLatestItemRevision();
				name = "";
				/*if(documentIR.getProperty("oc9_Format").length() > Specification.columnLengths.get(Specification.FormField.FORMAT)-1){					
					format = "*)";
					remark = "*) " + documentIR.getProperty("oc9_Format");
				} else {
				}*/
				uid = documentIR.getUid();
				format = documentIR.getProperty("oc9_Format");
				id = document.getProperty("item_id");
				object_name = documentIR.getProperty("object_name"); 
				shortType = getType(id);
				System.out.println("FOUND short type:"+shortType);
				if(id.equals(IRid)){
					specification.setSpecificationItemRevision(documentIR);
					Specification.settings.addStringProperty("LITERA1", documentIR.getProperty("oc9_Litera1"));
					Specification.settings.addStringProperty("LITERA2", documentIR.getProperty("oc9_Litera2"));
					Specification.settings.addStringProperty("LITERA3", documentIR.getProperty("oc9_Litera3"));
					Specification.settings.addStringProperty("PERVPRIM", documentIR.getItem().getProperty("oc9_PrimaryApp"));
					Specification.settings.addStringProperty("INVNO", documentIR.getItem().getProperty("oc9_InvNo"));
					try{
						for (AIFComponentContext compContext : documentIR.getChildren()){
							if ((compContext.getComponent() instanceof TCComponentDataset) 
									&& compContext.getComponent().getProperty("object_desc").equals("Спецификация")) {
								if(((TCComponent)compContext.getComponent()).isCheckedOut()){
									specification.getErrorList().addError(new Error("ERROR", "Набор данных заблокирован."));
								}
							}
	
						}
					} catch(Exception ex) {
						ex.printStackTrace();
					}
					continue;
				}
				if(shortType!=null){
					gostNameIsFalse = documentIR.getProperty("oc9_GOSTName").equalsIgnoreCase("нет");
					isBaseDoc = id.substring(0, id.lastIndexOf(" ")).equals(IRid);
					//name = (!gostName || !isBaseDoc) ? object_name : docTypesLong.get(docTypesShort.indexOf(shortType));
					if(gostNameIsFalse || !isBaseDoc){
						name += object_name;
					}
					if(!gostNameIsFalse){
						name += "\n" + docTypesLong.get(docTypesShort.indexOf(shortType));
					}
					
					BlockLine resultBlockLine = new BlockLine(blockLineHandler);
					resultBlockLine.attributes.setFormat(format);
					resultBlockLine.attributes.setId(id);
					resultBlockLine.attributes.setName(name);
					resultBlockLine.attributes.setQuantity("-1");
					resultBlockLine.addProperty("Type", shortType);
					resultBlockLine.build();
					blockList.getBlock(BlockContentType.DOCS, BlockType.DEFAULT).addBlockLine(uid, resultBlockLine);
				} else if(shortType==null){
					shortType = getKitType(id);
					if(shortType!=null){
						gostNameIsFalse = documentIR.getProperty("oc9_GOSTName").equalsIgnoreCase("нет");
						isBaseDoc = id.substring(0, id.lastIndexOf(" ")).equals(IRid);
						//name = (!gostName || !isBaseDoc) ? object_name : docKitTypesLong.get(docKitTypesShort.indexOf(shortType));
						name += object_name;
						if(!gostNameIsFalse){
							name += "\n" + docKitTypesLong.get(docKitTypesShort.indexOf(shortType));
						}
						
						BlockLine resultBlockLine = new BlockLine(blockLineHandler);
						resultBlockLine.attributes.setFormat(format);
						resultBlockLine.attributes.setId(id);
						resultBlockLine.attributes.setName(name);
						resultBlockLine.attributes.setQuantity("-1");
						resultBlockLine.addProperty("Type", shortType);
						resultBlockLine.build();
						blockList.getBlock(BlockContentType.KITS, BlockType.DEFAULT).addBlockLine(uid, resultBlockLine);
					}
				}
				
				if(shortType!=null){
					if(shortType.equals("МЭ") && isBaseDoc){
						if(Specification.settings.getStringProperty("MEDocumentId")!=null) {
							specification.getErrorList().addError(new Error("ERROR", "Определено более одного документа МЭ."));
						} else {
							Specification.settings.addStringProperty("MEDocumentId", id);
						}
					}
				} else {
					specification.getErrorList().addError(new Error("ERROR", "Не определен тип для документа: " + id));
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		
	}
	
	private void readGeneralNoteForm(){
		try{
			TCComponentItemRevision specIR = specification.getSpecificationItemRevision();
			TCComponent tempComp;
			if(specIR!=null){
				if((tempComp = specIR.getRelatedComponent("Oc9_SignRel"))!=null){
					Specification.settings.addStringProperty("Designer", tempComp.getProperty("oc9_Designer"));
					Specification.settings.addStringProperty("Check", tempComp.getProperty("oc9_Check"));
					Specification.settings.addStringProperty("TCheck", tempComp.getProperty("oc9_TCheck"));
					Specification.settings.addStringProperty("NCheck", tempComp.getProperty("oc9_NCheck"));
					Specification.settings.addStringProperty("Approver", tempComp.getProperty("oc9_Approver"));
					//TODO okeanos
					String designDate = tempComp.getProperty("oc9_DesignDate").equals("")?null:GeneralUtils.parseDateFromTC(tempComp.getProperty("oc9_DesignDate"));
					String checkDate = tempComp.getProperty("oc9_CheckDate").equals("")?null:GeneralUtils.parseDateFromTC(tempComp.getProperty("oc9_CheckDate"));
					String tCheckDate = tempComp.getProperty("oc9_TCheckDate").equals("")?null:GeneralUtils.parseDateFromTC(tempComp.getProperty("oc9_TCheckDate"));
					String nCheckDate = tempComp.getProperty("oc9_NCheckDate").equals("")?null:GeneralUtils.parseDateFromTC(tempComp.getProperty("oc9_NCheckDate"));
					String approveDate = tempComp.getProperty("oc9_ApproveDate").equals("")?null:GeneralUtils.parseDateFromTC(tempComp.getProperty("oc9_ApproveDate"));
					System.out.println(":DATE1:"+tempComp.getProperty("oc9_DesignDate"));
					System.out.println(":DATE2:"+designDate);
					Specification.settings.addStringProperty("DesignDate", designDate);
					Specification.settings.addStringProperty("CheckDate", checkDate);
					Specification.settings.addStringProperty("TCheckDate", tCheckDate);
					Specification.settings.addStringProperty("NCheckDate", nCheckDate);
					Specification.settings.addStringProperty("ApproveDate", approveDate);
				}
				if(specIR.getRelatedComponent("IMAN_master_form_rev")!=null){
					Specification.settings.addStringProperty("blockSettings", specIR.getRelatedComponent("IMAN_master_form_rev").getProperty("object_desc"));
				}
			}
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	private void readSpecifiedItemData(TCComponentBOMLine bomLine){
		try{
			Specification.settings.addStringProperty("AddedText", bomLine.getItemRevision().getProperty("oc9_AddNote").trim().equals("")?null:bomLine.getItemRevision().getProperty("oc9_AddNote").trim());
			Specification.settings.addStringProperty("OBOZNACH", bomLine.getItem().getProperty("item_id"));
			Specification.settings.addStringProperty("NAIMEN", bomLine.getItemRevision().getProperty("object_name"));
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	private void loadDocumentTypes(){
		String[] docTypes = Specification.preferenceService.getStringArray(Specification.preferenceService.TC_preference_site, "Oc9_Spec_DocumentTypesPriority");
		for(String docType : docTypes){
			int posOfFirstSpace = docType.indexOf(" ");
			if(posOfFirstSpace!=-1){
				System.out.println("FOUND value of pref:"+docType+":"+ docType.substring(0, posOfFirstSpace) +":"+docType.substring(posOfFirstSpace + 1, docType.length()));
				docTypesShort.add(docType.substring(0, posOfFirstSpace));
				docTypesLong.add(docType.substring(posOfFirstSpace + 1, docType.length()));
			}
		}
		String[] docKitTypes = Specification.preferenceService.getStringArray(Specification.preferenceService.TC_preference_site, "Oc9_Spec_DocumentComplexTypesPriority");
		for(String docKitType : docKitTypes){
			int posOfFirstSpace = docKitType.indexOf(" ");
			if(posOfFirstSpace!=-1){
				System.out.println("FOUND value of pref:"+docKitType+":"+ docKitType.substring(0, posOfFirstSpace) +":"+docKitType.substring(posOfFirstSpace + 1, docKitType.length()));
				docKitTypesShort.add(docKitType.substring(0, posOfFirstSpace));
				docKitTypesLong.add(docKitType.substring(posOfFirstSpace + 1, docKitType.length()));
			}
		}
	}
	
	private String getType(String input){
		String result = null;
		if(input.contains(" ")){
			input = input.substring(input.lastIndexOf(" ")+1, input.length());
		} else {
			return result;
		}
		String symbolPart = input.replaceAll("[^А-Яа-я]+", "");
		System.out.println("LOOKING for:"+input+":"+symbolPart);
		for(String type : docTypesShort){
			if(type.equals(input) && type.length()==input.length()){
				result = type;
				break;
			} else if(type.equals(symbolPart) && type.length()!=input.length()){
				result = type;
			} else if(type.equals(symbolPart) && type.length()==input.length()){
				result = type;
				break;
			}
		}
		return result;
	}
	
	private String getKitType(String input){
		String result = null;
		String symbolPart = input.replaceAll("[^А-Яа-я]+", "");
		for(String type : docKitTypesShort){
			if(type.equals(input) && type.length()==input.length()){
				result = type;
				break;
			} else if(type.equals(symbolPart) && type.length()!=input.length()){
				result = type;
			} else if(type.equals(symbolPart) && type.length()==input.length()){
				result = type;
				break;
			}
		}
		return result;
	}
	
}
