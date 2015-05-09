package com.rapidminer.operator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

/**
 * Class for dividing the data by clusters.
 * @author Jan Jakeš
 *
 */
public class ClusterSeparator {

	/** Version. */
	private static final long serialVersionUID = 1L;
	
	/** Clustered dataset. */
	private ExampleSet clusterSet; 
	
	/** Map of object by clusters. */
	private Map <String, List <Example>> separatedClusters;
	
	/** 
	 * Constructs a new instance. 
	 * @param clusterSet clustered dataset
	 */
	public ClusterSeparator(ExampleSet clusterSet) {
		this.clusterSet = clusterSet;
		this.separatedClusters = new HashMap<>();
		separateClusters();
	}
	
	/** Divides examples by clusters.  */
	private Map<String, List<Example>> separateClusters() {
		final Attributes attributes = clusterSet.getAttributes();
		final Attribute cluster = attributes.get("cluster");
		for (Example example : clusterSet) {
			final String clusterString = example.getNominalValue(cluster);
			if (!separatedClusters.containsKey(clusterString)) {
				separatedClusters.put(clusterString, new ArrayList<Example> ());
			}
			separatedClusters.get(clusterString).add(example);			
		}	
		return separatedClusters;
	}
	
	
	public Map<String, List<Example>> getSeparatedClusters () {
		return separatedClusters;
	}
}
