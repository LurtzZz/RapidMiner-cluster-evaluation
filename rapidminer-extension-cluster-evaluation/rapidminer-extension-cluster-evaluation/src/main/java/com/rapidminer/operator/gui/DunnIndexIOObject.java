package com.rapidminer.operator.gui;

import com.rapidminer.operator.DunnIndexData;
import com.rapidminer.operator.ResultObjectAdapter;

/**
 * IO object for result of Dunn Index operator.
 * @author Jan Jakeš
 *
 */
public class DunnIndexIOObject extends ResultObjectAdapter {

	/** Version. */
	private static final long serialVersionUID = 1L;
	
	/** Dunn Index value. */
	private Double index;
	
	/**
	 * Constructs a new instance.
	 * @param data {@link DunnIndexData} 
	 */
	public DunnIndexIOObject(DunnIndexData data) {
		index = data.getDunnIndex();
	}
	
	/** Prepares data to print. */
	@Override
	public String toResultString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("Dunn index of given clustered dataset:\n");
		builder.append("Value: " + index);

		return builder.toString();
	}
	
	/** Name of IO object. */
	@Override
	public String getName() {
		return "Dunn Index";
	}
	
}
