package ru.idealplm.specification.oceanos.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import ru.idealplm.specification.oceanos.comparators.DefaultComparator;
import ru.idealplm.specification.oceanos.comparators.DetailComparator;
import ru.idealplm.specification.oceanos.comparators.DocumentComparator;
import ru.idealplm.specification.oceanos.comparators.KitComparator;
import ru.idealplm.specification.oceanos.comparators.MaterialComparator;
import ru.idealplm.specification.oceanos.comparators.PositionComparator;
import ru.idealplm.specification.oceanos.gui.ErrorListDialog;
import ru.idealplm.specification.oceanos.gui.MainSpecificationDialog;
import ru.idealplm.specification.oceanos.methods.OceanosAttachMethod;
import ru.idealplm.specification.oceanos.methods.OceanosDataReaderMethod;
import ru.idealplm.specification.oceanos.methods.OceanosPrepareMethod;
import ru.idealplm.specification.oceanos.methods.OceanosReportBuilderMethod;
import ru.idealplm.specification.oceanos.methods.OceanosValidateMethod;
import ru.idealplm.specification.oceanos.methods.OceanosXmlBuilderMethod;
import ru.idealplm.specification.oceanos.util.PerfTrack;
import ru.idealplm.utils.specification.Error;
import ru.idealplm.utils.specification.Block;
import ru.idealplm.utils.specification.BlockList;
import ru.idealplm.utils.specification.Specification;
import ru.idealplm.utils.specification.Specification.BlockContentType;
import ru.idealplm.utils.specification.Specification.BlockType;
import ru.idealplm.utils.specification.SpecificationSettings;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.pse.plugin.Activator;

import ru.idealplm.utils.specification.Specification.FormField;
import ru.idealplm.utils.specification.comparators.NumberComparator;
/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
@SuppressWarnings("restriction")
public class SampleHandler extends AbstractHandler {
	
	public SampleHandler(){}
	
	@SuppressWarnings("restriction")
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		final TCComponentBOMLine topBomLine = Activator.getPSEService().getTopBOMLine();
		final Specification specification = Specification.getInstance();
		Specification.settings.setTemplateStream(SampleHandler.class.getResourceAsStream("/pdf/OceanosSpecPDFTemplate.xsl"));
		Specification.settings.setConfigStream(SampleHandler.class.getResourceAsStream("/pdf/userconfig.xml"));
		OceanosAttachMethod oceanosAttachMethod = new OceanosAttachMethod();
		OceanosDataReaderMethod oceanosDataReaderMethod = new OceanosDataReaderMethod();
		OceanosPrepareMethod oceanosPrepareMethod = new OceanosPrepareMethod();
		OceanosXmlBuilderMethod oceanosXmlBuilderMethod = new OceanosXmlBuilderMethod();
		OceanosValidateMethod oceanosValidateMethod = new OceanosValidateMethod();
		OceanosReportBuilderMethod oceanosReportBuilderMethod = new OceanosReportBuilderMethod(Specification.settings.getTemplateStream(), Specification.settings.getConfigStream());
		
		specification.init(topBomLine, oceanosValidateMethod, oceanosDataReaderMethod, oceanosPrepareMethod, oceanosXmlBuilderMethod, oceanosReportBuilderMethod, oceanosAttachMethod);
		
		DefaultComparator defaultPosComparator = new DefaultComparator(Specification.FormField.POSITION);
		DefaultComparator defaultNameComparator = new DefaultComparator(Specification.FormField.NAME);
		DefaultComparator defaultIDComparator = new DefaultComparator(Specification.FormField.ID);
		DocumentComparator docComparator = new DocumentComparator();
		KitComparator kitComparator = new KitComparator();
		PositionComparator posComparator = new PositionComparator();
		MaterialComparator matComparator = new MaterialComparator();
		DetailComparator detailComparator = new DetailComparator();
		
		BlockList blockList = specification.getBlockList();
		blockList.addBlock(new Block(BlockContentType.DOCS, BlockType.DEFAULT, docComparator, docComparator, 0));
		blockList.addBlock(new Block(BlockContentType.COMPLEXES, BlockType.DEFAULT, defaultIDComparator, posComparator, 0));
		blockList.addBlock(new Block(BlockContentType.ASSEMBLIES, BlockType.DEFAULT, defaultIDComparator, posComparator, 0));
		blockList.addBlock(new Block(BlockContentType.DETAILS, BlockType.DEFAULT, detailComparator, posComparator, 0));
		blockList.addBlock(new Block(BlockContentType.STANDARDS, BlockType.DEFAULT, defaultNameComparator, posComparator, 0));
		blockList.addBlock(new Block(BlockContentType.OTHERS, BlockType.DEFAULT, defaultNameComparator, posComparator, 0));
		blockList.addBlock(new Block(BlockContentType.MATERIALS, BlockType.DEFAULT, matComparator, posComparator, 0));
		blockList.addBlock(new Block(BlockContentType.KITS, BlockType.DEFAULT, kitComparator, kitComparator, 0));
		blockList.addBlock(new Block(BlockContentType.COMPLEXES, BlockType.ME, defaultIDComparator, defaultPosComparator, 0));
		blockList.addBlock(new Block(BlockContentType.ASSEMBLIES, BlockType.ME, defaultIDComparator, defaultPosComparator, 0));
		blockList.addBlock(new Block(BlockContentType.DETAILS, BlockType.ME, detailComparator, defaultPosComparator, 0));
		blockList.addBlock(new Block(BlockContentType.STANDARDS, BlockType.ME, defaultNameComparator, defaultPosComparator, 0));
		blockList.addBlock(new Block(BlockContentType.OTHERS, BlockType.ME, defaultNameComparator, posComparator, 0));
		blockList.addBlock(new Block(BlockContentType.MATERIALS, BlockType.ME, matComparator, posComparator, 0));
		blockList.addBlock(new Block(BlockContentType.KITS, BlockType.ME, kitComparator, kitComparator, 0));
		
		blockList.getBlock(BlockContentType.DOCS, BlockType.DEFAULT).isRenumerizable = false;
		blockList.getBlock(BlockContentType.KITS, BlockType.DEFAULT).isRenumerizable = false;
		blockList.getBlock(BlockContentType.KITS, BlockType.ME).isRenumerizable = false;
		
		setColumnLengthsGost();
		
		String[] emptyValues = {};
		Specification.settings.setNonbreakableWords(Specification.preferenceService.getStringArray(Specification.preferenceService.TC_preference_site, "Oc9_Spec_NonbreakableWords", emptyValues));
		
		
		if(!specification.validate()){
			System.out.println(specification.getErrorList().toString());
		} else {
			try{
				// TODO Need to make it start async right when SP button is pressed
				// ReportBuilder reportBuilder = new PDFBuilder(Specification.getDefaultSpecificationPDFTemplate(), Specification.getDefaultSpecificationPDFConfig());
				PerfTrack.prepare("readBOMData");
				ProgressMonitorDialog pd = new ProgressMonitorDialog(HandlerUtil.getActiveShell(event).getShell());
				try {
					pd.run(true /*fork*/, true /*cancelable*/, new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						monitor.beginTask("Чтение данных", 100);
						specification.readBOMData();
						monitor.done();
					}
					});
				} catch (InvocationTargetException | InterruptedException e) {
					e.printStackTrace();
				}
				PerfTrack.addToLog("readBOMData");
				
				if(specification.getErrorList().size()>0){
					System.out.println("TODO: INFORM ABOUT ERRORS");
					ErrorListDialog elg = new ErrorListDialog(HandlerUtil.getActiveShell(event).getShell(), specification.getErrorList().errorList);
					return null;
				}
				
				readSettings(specification.getBlockList());
				
				PerfTrack.prepare("Creating dialog");
				MainSpecificationDialog mainDialog = new MainSpecificationDialog(HandlerUtil.getActiveShell(event).getShell(), SWT.CLOSE, specification);
				PerfTrack.addToLog("Creating dialog");
				

				mainDialog.open();
				for(int i = 0; i < specification.getBlockList().size(); i++){
					if(specification.getBlockList().get(i).getListOfLines()!=null) System.out.println("Size of " + specification.getBlockList().get(i).blockTitle + " = " + specification.getBlockList().get(i).getListOfLines().size());
				}
				
				if (!Specification.settings.getBooleanProperty("bOkPressed")) { return null; }
				
				saveSettings(specification.getBlockList());
				
				//if(Specification.settings.getBooleanProperty("doRenumerize")){
					PerfTrack.prepare("Locking BOM");
					topBomLine.lock();
					PerfTrack.addToLog("Locking BOM");
				//}
					try {
						pd.open();
						pd.run(true /*fork*/, true /*cancelable*/, new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							monitor.beginTask("Построение спецификации", 100);
							PerfTrack.prepare("prepareBlocks");
							monitor.subTask("Обработка данных");
							specification.prepareBlocks();
							monitor.worked(20);
							PerfTrack.addToLog("prepareBlocks");
							PerfTrack.prepare("makeXmlFile");
							monitor.subTask("Построение XML файла");
							specification.makeXmlFile();
							monitor.worked(20);
							PerfTrack.addToLog("makeXmlFile");
							PerfTrack.prepare("makeReport");
							monitor.subTask("Построение PDF файла");
							specification.makeReportFile();
							monitor.worked(20);
							PerfTrack.addToLog("makeReport");
							PerfTrack.prepare("putInTeamcenter");
							monitor.subTask("Добавление в Teamcenter");
							specification.putInTeamcenter();
							monitor.worked(20);
							PerfTrack.addToLog("putInTeamcenter");
							System.out.println("--- ERROR LIST ---");
							System.out.println(specification.getErrorList().toString());
							System.out.println("--- ERROR LIST ---");
							
							if(Specification.settings.getBooleanProperty("doRenumerize")){
								PerfTrack.prepare("Saving&unlocking BOM");
								try {
									topBomLine.save();
									topBomLine.unlock();
								} catch (TCException e) {
									e.printStackTrace();
								}
								PerfTrack.addToLog("Saving&unlocking BOM");
							}
							monitor.done();
						}
						});
					} catch (InvocationTargetException | InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
				//if(Specification.settings.getBooleanProperty("doRenumerize")){
				//}
				
				PerfTrack.printLog();
			} catch (TCException e) {
				e.printStackTrace();
			} finally {
				specification.cleanUp();
				//specification = null;
			}
		}
		return null;
	}
	
	void readSettings(BlockList blockList){
		String settingsString = Specification.settings.getStringProperty("blockSettings");
		System.out.println("BLOCK_SETTINGS"+settingsString);
		if(settingsString==null || settingsString.isEmpty()) return;
		for(String blockProps:settingsString.split("&")){
			System.out.println(blockProps);
			String[] props = blockProps.split(":");
			if(props.length!=4) continue;
			System.out.println(props[0]);
			Block block = blockList.getBlock(BlockContentType.values()[Character.getNumericValue(props[0].charAt(0))], props[0].charAt(1)=='0'?BlockType.DEFAULT:BlockType.ME);
			if(block!=null){
				block.reservePosNum = Integer.parseInt(props[1]);
				block.reserveLinesNum = Integer.parseInt(props[2]);
				block.intervalPosNum = Integer.parseInt(props[3]);
			}
		}
	}
	
	void saveSettings(BlockList blockList){
		String settingsString = "";
		String del = ":";
		for(Block block:blockList){
			settingsString+=block.blockContentType.ordinal()+(block.blockType==BlockType.DEFAULT?"0":"1")+del+block.reservePosNum+del+block.reserveLinesNum+del+block.intervalPosNum;
			settingsString+="&";
		}
		if(settingsString.endsWith("&")){
			settingsString = settingsString.substring(0, settingsString.length()-1);
		}
		Specification.settings.addStringProperty("blockSettings", settingsString);
	}
	
	void setColumnLengthsArial()
	{
		Specification.settings.setColumnLength(FormField.FORMAT, 3);
		Specification.settings.setColumnLength(FormField.ZONE, 3);
		Specification.settings.setColumnLength(FormField.ID, 3);
		Specification.settings.setColumnLength(FormField.NAME, 204.0);
		Specification.settings.setColumnLength(FormField.POSITION, 3);
		Specification.settings.setColumnLength(FormField.QUANTITY, 3);
		Specification.settings.setColumnLength(FormField.REMARK, 88);
	}
	
	void setColumnLengthsGost()
	{
		Specification.settings.setColumnLength(FormField.FORMAT, 3);
		Specification.settings.setColumnLength(FormField.ZONE, 3);
		Specification.settings.setColumnLength(FormField.ID, 3);
		Specification.settings.setColumnLength(FormField.NAME, 230.0);
		Specification.settings.setColumnLength(FormField.POSITION, 3);
		Specification.settings.setColumnLength(FormField.QUANTITY, 3);
		Specification.settings.setColumnLength(FormField.REMARK, 96);
	}
}
