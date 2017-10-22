package ru.idealplm.specification.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ru.idealplm.specification.blockline.BlockLine;
import ru.idealplm.specification.core.Specification.BlockContentType;
import ru.idealplm.specification.core.Specification.BlockType;

public class Block {
	
	public boolean isRenumerizable = true;
	public int reserveLinesNum = 0;
	public int reservePosNum = 0;
	public int intervalPosNum = 0;
	public BlockType blockType;
	public BlockContentType blockContentType;
	public String blockTitle;
	private ArrayList<BlockLine> blockLines;
	private ArrayList<String> blockLineUIDs;
	private Comparator<BlockLine> renumComparator;
	private Comparator<BlockLine> nonRenumComparator;
	private int renumerizableLines = 0;
	
	public Block(BlockContentType blockContentType, BlockType blockType, Comparator<BlockLine> sortComparator, Comparator<BlockLine> nonSortComparator, int reserveLinesNum) {
		this.blockContentType = blockContentType;
		this.blockType = blockType;
		this.blockTitle = Specification.blockTitles.get(blockContentType);
		this.renumComparator = sortComparator;
		this.nonRenumComparator = nonSortComparator;
		this.reserveLinesNum = reserveLinesNum;
		blockLines = new ArrayList<BlockLine>();
		blockLineUIDs = new ArrayList<String>();
	}
	
	public void sort(boolean renum) {
		if(blockLines!=null){
			if(renum){
				Collections.sort(blockLines, renumComparator);
			} else {
				Collections.sort(blockLines, nonRenumComparator);
			}
		}
	}

	public ArrayList<BlockLine> getListOfLines() {
		return blockLines;
	}
	
	public int size(){
		return blockLines.size();
	}
	
	public void addBlockLine(String uid, BlockLine blockLine){
		int pos = -1;
		if((pos = blockLineUIDs.indexOf(uid))!=-1){
			if(blockLine.isSubstitute!=blockLines.get(pos).isSubstitute){
				Specification.errorList.addError(new Error("ERROR", "Замена с идентификатором " + blockLine.attributes.getId() + " дублируется в составе сборки."));
			}
			updateBlockLine(blockLines.get(pos), blockLine);
			return;
		}
		if(!blockLine.isSubstitute) renumerizableLines++;
		blockLines.add(blockLine);
		blockLineUIDs.add(uid);
	}
	
	public void updateBlockLine(BlockLine target, BlockLine line){
		if(line.isSubstitute){
			return;
		}
		target.attributes.getRemark().insert(line.attributes.getRemark().getAll());
		if(target.attributes.getKits()!=null){
			target.attributes.getKits().addKits(line.attributes.getKits());
		}
		target.attributes.addQuantity(String.valueOf(line.attributes.getQuantity()));
		target.attributes.getZone().addZone(line.attributes.getZone());
		target.refBOMLines.addAll(line.getRefBOMLines());
		
		boolean targetPosIsEmpty = target.attributes.getPosition().isEmpty();
		boolean linePosIsEmpty = line.attributes.getPosition().isEmpty();
		boolean differentPos = !target.attributes.getPosition().equals(line.attributes.getPosition());
				
		if(differentPos && targetPosIsEmpty){
			target.attributes.setPosition(line.attributes.getPosition());
		} else if (differentPos && linePosIsEmpty){
			line.attributes.setPosition(target.attributes.getPosition());
		} else if (differentPos && !linePosIsEmpty && !targetPosIsEmpty){
			Specification.errorList.addError(new Error("ERROR", "Разные номера позиций у входимости с идентификатором " + line.attributes.getId()));
		}
	}
	
	public int getRenumerizableLinesCount(){
		return renumerizableLines;
	}

}
