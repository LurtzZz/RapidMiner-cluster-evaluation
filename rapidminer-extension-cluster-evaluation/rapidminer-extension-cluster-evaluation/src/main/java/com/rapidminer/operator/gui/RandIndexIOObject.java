package com.rapidminer.operator.gui;

import com.rapidminer.operator.RandIndexData;
import com.rapidminer.operator.ResultObjectAdapter;

/**
 * IO object for result of Rand Index operator.
 * @author Jan Jakeš
 *
 */
public class RandIndexIOObject extends ResultObjectAdapter {

	/** Version. */
	private static final long serialVersionUID = 1L;
	
	/** Rand Index value. */
	private Double index;
	
	/**
	 * Constructs a new instance.
	 * @param data {@link RandIndexData}
	 */
	public RandIndexIOObject(RandIndexData data) {
		index = data.getRandIndex();
	}
	
	/** Prepares data to print. */
	@Override
	public String toResultString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("Rand index of two cluster method:\n");
		if (index == -1.0) {
			builder.append("Input datasets do not have equal size.");
		} else if (index == -2.0) {
			builder.append("Input datasets do not have equal elements.");		
		} else {
			builder.append("Value: " + index);
		}
		return builder.toString();
	}
	
	/** Name of IO object. */
	@Override
	public String getName() {
		return "Rand Index";
	}
	
}
