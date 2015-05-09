package com.rapidminer.operator.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.ResultObjectAdapter;
import com.rapidminer.operator.SilhouetteData;

/**
 * IO object for result of Silhouette operator.
 * @author Jan Jakeš
 *
 */
public class SilhouetteIOObject extends ResultObjectAdapter {

	/** Version. */
	private static final long serialVersionUID = 1L;
	
	/** Average silhouette of clusters. */
	private Map<String, Double> valueMap = new HashMap<String, Double>();

	/** Data set. */
	private ExampleSet clusterSet;
	
	/**
	 * Constructs a new instance.
	 * @param data {@link SilhouetteData}
	 */
	public SilhouetteIOObject(SilhouetteData data) {
		this.valueMap = data.getAverageClusterSilhouette();
		this.clusterSet = data.getClusterSet();
	}
		
	public Map<String, Double> getValueMap() {
		return valueMap;
	}
	
	public void setValue(String key, Double value) {
		valueMap.put(key, value);
	}
	
	public ExampleSet getClusterSet() {
		return clusterSet;
	}
	
	public void setClusterSet(ExampleSet clusterSet) {
		this.clusterSet = clusterSet;
	}
	
	/** Prepares data to print. */
	@Override
	public String toResultString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("Average silhouette of clusters:\n");
		List<String> keys = new ArrayList<>(getValueMap().keySet());
		Collections.sort(keys);
		for (String key : keys) {
			builder.append(key + ":\t" + getValueMap().get(key) + "\n");
		}
		return builder.toString();
	}
	
	/** Name of IO object. */
	@Override
	public String getName() {
		return "Silhouette";
	}
}
