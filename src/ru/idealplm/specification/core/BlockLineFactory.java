package ru.idealplm.specification.core;

import com.teamcenter.rac.kernel.TCComponentBOMLine;

import ru.idealplm.specification.blockline.BlockLine;

public abstract class BlockLineFactory {
	
	public abstract BlockLine newBlockLine(TCComponentBOMLine bomLine);

}
