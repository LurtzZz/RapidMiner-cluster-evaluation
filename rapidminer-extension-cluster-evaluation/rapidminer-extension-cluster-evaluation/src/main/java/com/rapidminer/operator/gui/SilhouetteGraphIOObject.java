package com.rapidminer.operator.gui;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.ResultObjectAdapter;

public class SilhouetteGraphIOObject  extends ResultObjectAdapter {

	ExampleSet clusterSet;
	
	public SilhouetteGraphIOObject(ExampleSet data) {
		this.clusterSet = data;
	}
	
	@Override
	public String getName() {
		return "Silhouette Graph";
	}
	
	@Override
	public String toResultString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("Silhouette of clusters:\n");
		Attributes attributes = clusterSet.getAttributes();
		Attribute attributeSilhouette = attributes.get("silhouette");
		for (Example e : clusterSet) {
			builder.append(e.getId() + ":\t" + e.getValue(attributeSilhouette) + "\n");
		}
		return builder.toString();
	}
}
