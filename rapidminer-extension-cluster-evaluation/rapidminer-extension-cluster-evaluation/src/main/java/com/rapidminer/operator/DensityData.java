package com.rapidminer.operator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SortedExampleSet;

/**
 * Class for finding typical values.
 * @author Jan Jakeš
 *
 */
public class DensityData {

	/** Version. */
	private static final long serialVersionUID = 1L;
	
	/** Complete dataset. */ 
	private ExampleSet exampleSet;
	
	/** Map of object by clusters.  */
	private Map<String, List <Example>> separatedMap;
	
	/** Map of typical values of complete dataset. */
	private Map<String, List<String>> typicalValues;

	/** Map of typical values of clusters */
	private Map<String, Map<String, List<String>>> typicalValuesOfCluster;
	
	/** Constructs a new instance. */
	public DensityData(ExampleSet exampleSet) {
		this.exampleSet = exampleSet;
		final ClusterSeparator clusterSeparator = new ClusterSeparator(this.exampleSet);
		this.separatedMap = clusterSeparator.getSeparatedClusters();
		calculateTypicalValues(this.exampleSet);
		calculateTypicalValuesOfClusters(this.separatedMap, this.exampleSet.getAttributes());
	}
	
	
	
	/**
	 * Finds all typical values.
	 * @param separatedMap map of object by clusters.
	 * @param attributes @link{Attributes} attributes of dataset
	 */
	private void calculateTypicalValuesOfClusters(Map<String, List <Example>> separatedMap, Attributes attributes) {
		this.typicalValuesOfCluster = new HashMap<>();
		for (String key : separatedMap.keySet()) {
			this.typicalValuesOfCluster.put(key, getTypicalValuesOfCluster(separatedMap.get(key), attributes));
		}
	}
	
	
	/**
	 * Finds typical values of clusters.
	 * @param cluster objects of cluster
	 * @param attributes @link{Attributes} attributes of dataset
	 * @return map of typical values of cluster
	 */
	private Map<String, List<String>> getTypicalValuesOfCluster(List<Example> cluster, Attributes attributes) {		
		Map<String, List<String>> typicalValues = new HashMap<String, List<String>>();
		Iterator<Attribute> attributeIterator = attributes.allAttributes();
		while (attributeIterator.hasNext()) {
			Attribute attribute = attributeIterator.next();
			if (attribute.isNominal()) {
				typicalValues.put(attribute.getName(), getNominalTypicalValueOfCluster(cluster, attribute));
			} else if (attribute.isNumerical()) {
				typicalValues.put(attribute.getName(), getNumericalTypicalValueOfCluster(cluster, attribute));
			}
		}
		return typicalValues;
	}
	
	
	/**
	 * Finds typical value of cluster for nominal attribute.
	 * @param cluster objects of cluster
	 * @param attribute selected attribute
	 * @return list of nominal typical values
	 */
	private List<String> getNominalTypicalValueOfCluster(List<Example> cluster, Attribute attribute) {
		final Map<String, Integer> nominalCount = new HashMap<>(); 
		for (int i = 0; i < cluster.size(); i++) {
			final Example example = cluster.get(i);
			final String attributeValue = example.getNominalValue(attribute);	
			if (nominalCount.containsKey(attributeValue)) {
				Integer count = nominalCount.get(attributeValue);
				count++;
				nominalCount.put(attributeValue, count);
			} else {
				nominalCount.put(attributeValue, 0);
			}
		}
		List<String> typicalValue = new ArrayList<>();
		Integer maxCount = 2;
		for (String key : nominalCount.keySet()) {
			final Integer count = nominalCount.get(key);
			if (maxCount < count) {
				typicalValue = new ArrayList<>();
				typicalValue.add(key);
				maxCount = count;
			} else if (maxCount == count) {
				typicalValue.add(key);
			}
		}
		return typicalValue;
	}
	
	
	/**
	 * Finds typical value of cluster for numerical attribute.
	 * @param cluster objects of cluster
	 * @param attribute selected attribute
	 * @return list of numerical typical values for given attribute
	 */
	private List<String> getNumericalTypicalValueOfCluster(List<Example> cluster, Attribute attribute) {
		Collections.sort(cluster, new ExampleComparator(attribute));
		final Integer centerIndex;		
		if (cluster.size() != 0 && (cluster.size() % 2) == 0) {
			centerIndex = (cluster.size() / 2) - 1;
		} else {
			centerIndex = cluster.size() / 2;
		}
		List<String> typicalValue = new ArrayList<>();
		typicalValue.add(cluster.get(centerIndex).getValueAsString(attribute));
		return typicalValue;
	}
	
	
	/**
	 * Finds typical values of dataset.
	 * @param exampleSet dataset
	 */
	private void calculateTypicalValues(ExampleSet exampleSet) {
		this.typicalValues = new HashMap<String, List<String>>();
		final Attributes attributes = exampleSet.getAttributes();
		Iterator<Attribute> attributeIterator = attributes.allAttributes();
		while (attributeIterator.hasNext()) {
			Attribute attribute = attributeIterator.next();
			if (attribute.isNominal()) {
				this.typicalValues.put(attribute.getName(), getNominalTypicalValue(exampleSet, attribute));
			} else if (attribute.isNumerical()) {
				this.typicalValues.put(attribute.getName(), getNumericalTypicalValue(exampleSet, attribute));
			}
		}
	}
	
	
	/**
	 * Finds typical value of dataset for nominal attribute.
	 * @param exampleSet dataset
	 * @param attribute selected attribute
	 * @return list of nominal typical value for given attribute
	 */
	private List<String> getNominalTypicalValue(ExampleSet exampleSet, Attribute attribute) {
		final Map<String, Integer> nominalCount = new HashMap<>(); 
		for (int i = 0; i < exampleSet.size(); i++) {
			final Example example = exampleSet.getExample(i);
			final String attributeValue = example.getNominalValue(attribute);	
			if (nominalCount.containsKey(attributeValue)) {
				Integer count = nominalCount.get(attributeValue);
				count++;
				nominalCount.put(attributeValue, count);				
			} else {
				nominalCount.put(attributeValue, 0);
			}
		}
		
		List<String> typicalValue = new ArrayList<>();
		Integer maxCount = 2;
		for (String key : nominalCount.keySet()) {
			final Integer count = nominalCount.get(key);
			if (maxCount < count) {
				typicalValue = new ArrayList<>();
				typicalValue.add(key);
				maxCount = count;
			} else if (maxCount == count) {
				typicalValue.add(key);
			}
		}
		return typicalValue;
	}
	
	
	/**
	 * Finds typical value of dataset for numerical attribute.
	 * @param exampleSet dataset
	 * @param attribute selected attribute
	 * @return list of numerical typical value for given attribute
	 */
	private List<String> getNumericalTypicalValue(ExampleSet exampleSet, Attribute attribute) { 
		final ExampleSet sortedExampleSet = new SortedExampleSet(exampleSet, attribute, SortedExampleSet.INCREASING);
		final Integer centerIndex;
		if (sortedExampleSet.size() != 0 && (sortedExampleSet.size() % 2) == 0) {
			centerIndex = (sortedExampleSet.size() / 2) - 1;
		} else {
			centerIndex = sortedExampleSet.size() / 2;
		}
		List<String> typicalValue = new ArrayList<>();
		typicalValue.add(sortedExampleSet.getExample(centerIndex).getValueAsString(attribute));
		return typicalValue;
	}
	
	
	/**
	 *  Example comparator for numerical attributes.
	 * @author Jan Jakeš
	 *
	 */
	class ExampleComparator implements Comparator<Example> {		
		private Attribute attribute;
		
		public ExampleComparator(Attribute attribute) {
			this.attribute = attribute;
		}
		
	    @Override
	    public int compare(Example e1, Example e2) {
	        return ((Double) e1.getNumericalValue(attribute)).compareTo(((Double) e2.getNumericalValue(attribute)));
	    }
	}
	
	public ExampleSet getExampleSet() {
		return this.exampleSet;
	}	

	public Map<String, List <Example>> getSeparatedMap() {
		return this.separatedMap;
	}
	
	public Map<String, List<String>> getTypicalValues() {
		return this.typicalValues;
	}
	
	public Map<String, Map<String, List<String>>> getTypicalValuesOfCluster() {
		return this.typicalValuesOfCluster;
	}
	
}

