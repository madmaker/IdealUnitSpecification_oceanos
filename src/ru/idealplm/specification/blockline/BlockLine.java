package ru.idealplm.specification.blockline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.teamcenter.rac.kernel.TCComponentBOMLine;

import ru.idealplm.specification.blockline.attributes.BlockLineAttributes;
import ru.idealplm.specification.core.BlockLineHandler;
import ru.idealplm.specification.core.Specification.BlockContentType;
import ru.idealplm.specification.core.Specification.BlockType;

public class BlockLine {
	
	public BlockLineAttributes attributes;
	public BlockContentType blockContentType;
	public BlockType blockType;
	public boolean isSubstitute = false;
	public boolean isRenumerizable = true;
	public int lineHeight = 1;
	public String uid = "";
	
	public ArrayList<BlockLine> substituteBlockLines;
	public ArrayList<TCComponentBOMLine> refBOMLines;
	public ArrayList<BlockLine> attachedLines = new ArrayList<BlockLine>();
	
	private HashMap<String, String> props;
	private BlockLineHandler blockLineHandler;
	
	public BlockLine() {
		attributes = new BlockLineAttributes();
	}
	
	public BlockLine(BlockLineHandler blockLineHandler) {
		this();
		this.blockLineHandler = blockLineHandler;
	}
	
	public BlockLine(BlockLineHandler blockLineHandler, boolean isSubstitute) {
		this(blockLineHandler);
		this.isSubstitute = isSubstitute;
	}
	
	public void addSubstituteBlockLine(BlockLine blockLine){
		if(this.substituteBlockLines==null) substituteBlockLines = new ArrayList<BlockLine>(2);
		substituteBlockLines.add(blockLine);
	}
	
	public void addRefBOMLine(TCComponentBOMLine bomLine){
		if(this.refBOMLines==null) refBOMLines = new ArrayList<TCComponentBOMLine>(2);
		refBOMLines.add(bomLine);
	}
	
	public ArrayList<BlockLine> getSubstituteBlockLines(){
		return substituteBlockLines;
	}
	
	public ArrayList<TCComponentBOMLine> getRefBOMLines(){
		return refBOMLines;
	}
	
	public ArrayList<BlockLine> getAttachedLines(){
		return attachedLines;
	}
	public void addProperty(String key, String value){
		if(props==null) props = new HashMap<String,String>();
		props.put(key, value);
	}
	public String getProperty(String key){
		return (props!=null)?props.get(key):null;
	}
	
	public BlockLine build(){
		if(attachedLines!=null){
			for(BlockLine line:attachedLines){
				line.build();
			}
		}
		if(blockLineHandler!=null) blockLineHandler.prepareBlockLine(this);
		return this;
	}
}
