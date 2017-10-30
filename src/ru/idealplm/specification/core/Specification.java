package ru.idealplm.specification.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;

import ru.idealplm.specification.methods.IAttachMethod;
import ru.idealplm.specification.methods.IDataReaderMethod;
import ru.idealplm.specification.methods.IPrepareMethod;
import ru.idealplm.specification.methods.IReportBuilderMethod;
import ru.idealplm.specification.methods.IValidateMethod;
import ru.idealplm.specification.methods.IXmlBuilderMethod;

public class Specification {
	
	public static final String CLIENT_ID = UUID.randomUUID().toString();
	private static Specification instance = null;
	private static final Object lock = new Object();
	private static boolean isInitialized = false;
	
	public static enum FormField {FORMAT, ZONE, POSITION, ID, NAME, QUANTITY, REMARK};
	public static enum BlockContentType {DOCS, COMPLEXES, ASSEMBLIES, DETAILS, STANDARDS, OTHERS, MATERIALS, KITS};
	public static enum BlockType {DEFAULT};
	public static final SpecificationSettings settings = SpecificationSettings.getInstance();
	
	private BlockList blockList;
	private File xmlFile = null;
	private File reportFile = null;
	
	private IValidateMethod validateMethod;
	private IDataReaderMethod dataReaderMethod;
	private IPrepareMethod prepareMethod;
	private IXmlBuilderMethod xmlBuilderMethod;
	private IReportBuilderMethod reportBuilderMethod;
	private IAttachMethod attachMethod;
	
	public static ErrorList errorList;
	
	private TCComponentBOMLine topBOMLine;
	private TCComponentItemRevision specificationItemRevision = null;
	public static TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	public static TCPreferenceService preferenceService = session.getPreferenceService();
	
	public static Map<BlockContentType, String> blockTitles = new HashMap<BlockContentType, String>();
	static {
		blockTitles.put(BlockContentType.DOCS, "Документация");
		blockTitles.put(BlockContentType.COMPLEXES, "Комплексы");
		blockTitles.put(BlockContentType.ASSEMBLIES, "Сборочные единицы");
		blockTitles.put(BlockContentType.DETAILS, "Детали");
		blockTitles.put(BlockContentType.STANDARDS, "Стандартные изделия");
		blockTitles.put(BlockContentType.OTHERS, "Прочие изделия");
		blockTitles.put(BlockContentType.MATERIALS, "Материалы");
		blockTitles.put(BlockContentType.KITS, "Комплекты");
	}
	
    public static Specification getInstance() {
    	if (!isInitialized) {
			synchronized (lock) {
				if (instance == null) {
					instance = new Specification();
					isInitialized = true;
				}
			}
		}
		return instance;
    }
    
    public void init(TCComponentBOMLine topBOMLine, IValidateMethod validateMethod, IDataReaderMethod dataReaderMethod, IPrepareMethod prepareMethod, IXmlBuilderMethod xmlBuilderMethod, IReportBuilderMethod reportBuilderMethod, IAttachMethod attachMethod){
    	this.topBOMLine = topBOMLine;
    	this.validateMethod = validateMethod;
    	this.dataReaderMethod = dataReaderMethod;
    	this.prepareMethod = prepareMethod;
    	this.xmlBuilderMethod = xmlBuilderMethod;
    	this.reportBuilderMethod = reportBuilderMethod;
    	this.attachMethod = attachMethod;
    	errorList = new ErrorList();
    	blockList = new BlockList();
    }
	
	public void setSpecificationItemRevision(TCComponentItemRevision specIR){
		this.specificationItemRevision = specIR;
	}
	
	public TCComponentItemRevision getSpecificationItemRevision(){
		return this.specificationItemRevision;
	}
	
	public boolean validate(){
		return validateMethod.validateData();
	}
	
	public void prepareBlocks(){
		prepareMethod.prepareData();
	}
	
	public void readBOMData(){
		dataReaderMethod.readData();
	}
	
	public void makeXmlFile(){
		xmlFile = xmlBuilderMethod.buildXmlFile();
	}
	
	public File getXmlFile(){
		return xmlFile;
	}
	
	public void makeReportFile(){
		reportFile = reportBuilderMethod.buildReportFile();
	}
	
	public File getReportFile(){
		return reportFile;
	}
	
	public void putInTeamcenter(){
		attachMethod.attachReportFile();
	}
	
	public TCComponentBOMLine getTopBOMLine(){
		return topBOMLine;
	}
	
	public BlockList getBlockList(){
		return blockList;
	}
	
	public void setBlockList(BlockList blockList){
		this.blockList = blockList;
	}
	
	public ErrorList getErrorList(){
		return errorList;
	}
	
	public void cleanUp(){
		blockList.clear();
		blockList = null;
		topBOMLine = null;
		xmlFile = null;
		reportFile = null;
		specificationItemRevision= null;
		settings.cleanUp();
	}
}
