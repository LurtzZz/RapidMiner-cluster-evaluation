package com.rapidminer.operator.gui;

import java.util.List;
import java.util.Map;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.DensityData;
import com.rapidminer.operator.ResultObjectAdapter;

/**
 * IO object for result of Density operator.
 * @author Jan Jakeš
 *
 */
public class DensityIOObject extends ResultObjectAdapter {
	
	/** Version. */
	private static final long serialVersionUID = 1L;
	
	/** {@link DensityData} */
	private DensityData data;
	
	/**
	 * Constructs a new instance.
	 * @param data {@link DensityData} 
	 */
	public DensityIOObject(DensityData data) {
		this.data = data;
	}
	
	public ExampleSet getExampleSet() {
		return this.data.getExampleSet();
	}
	
	
	/** Prepares data to print. */
	@Override
	public String toResultString() { 

		StringBuilder builder = new StringBuilder();
		Map<String, List<String>> typicalValue = this.data.getTypicalValues();
		builder.append("Typical values of example set:\n\n");		
		builder.append(String.format("%-20s \t %s\n", "Attribute:", "Typical value:"));
		builder.append("===============================================================\n");
		for (String key : typicalValue.keySet()) {
			if (typicalValue.get(key).size() == 0) {
				builder.append(String.format("%-20s \t (no typical value)\n", key));
				builder.append("---------------------------------------------------------------\n");
			} else {
				builder.append(String.format("%-20s \t ", key));
				for (int i = 0; i < typicalValue.get(key).size(); i++) {
					if (i == 0) {
						builder.append(String.format("%s", typicalValue.get(key).get(i)));
					} else {
						builder.append(String.format(", %s", typicalValue.get(key).get(i)));
					}
				}	
				builder.append("\n");
				builder.append("---------------------------------------------------------------\n");
			}
		}
		builder.append("\n\n\n");
		
		
		Map<String, Map<String, List<String>>> typicalValueOfCluster = this.data.getTypicalValuesOfCluster();
		builder.append("Typical value of clusters:\n\n");
		for (String cluster : typicalValueOfCluster.keySet()) {
			builder.append(String.format("Cluster: %s\n", cluster));
			builder.append(String.format("%-20s \t %s\n", "Attribute:", "Typical value:"));
			builder.append("===============================================================\n");
			for (String key : typicalValueOfCluster.get(cluster).keySet()) {
				if (typicalValueOfCluster.get(cluster).get(key).size() == 0) {
					builder.append(String.format("%-20s \t (no typical value)\n", key));
					builder.append("---------------------------------------------------------------\n");
				} else {
					builder.append(String.format("%-20s \t ", key));
					for (int i = 0; i < typicalValueOfCluster.get(cluster).get(key).size(); i++) {
						if (i == 0) {
							builder.append(String.format("%s", typicalValueOfCluster.get(cluster).get(key).get(i)));
						} else {
							builder.append(String.format(", %s", typicalValueOfCluster.get(cluster).get(key).get(i)));
						}
					}
					builder.append("\n");
					builder.append("---------------------------------------------------------------\n");
				}
			}
			builder.append("\n\n\n");
		}
		return builder.toString();
	}
	
	/** Name of IO object. */
	@Override
	public String getName() {
		return "Typical values";
	}
}
