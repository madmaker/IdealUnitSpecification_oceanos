package ru.idealplm.specification.core;

import java.util.Comparator;

import ru.idealplm.specification.blockline.BlockLine;

public interface BlockLineComparator extends Comparator<BlockLine>{

	public int compare(BlockLine arg0, BlockLine arg1);

}
