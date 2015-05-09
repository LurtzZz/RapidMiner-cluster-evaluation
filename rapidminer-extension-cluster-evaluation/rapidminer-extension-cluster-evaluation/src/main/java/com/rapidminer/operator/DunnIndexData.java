package com.rapidminer.operator;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rapidminer.example.Example;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

/**
 * Class for calculating Dunn index.
 * @author Jan Jakeš
 *
 */
public class DunnIndexData {

	/** Version. */
	private static final long serialVersionUID = 1L;
	
	/** Value of Dunn index. */
	private Double dunnIndex;
	
	/** Constructs a new instance. */
	public DunnIndexData() { }	
	
	
	/**
	 * Calculates Dunn index value.
	 * @param separatedMap map of object by clusters
	 * @param measure selected measure
	 */
	public void calculateDunnIndex(Map<String, List <Example>> separatedMap, DistanceMeasure measure) {
		final Double minBetweenDistance = getMinBetweenDistance(separatedMap, measure);
		if (minBetweenDistance == 0.0) {
			this.dunnIndex = 0.0;
		}
		final Double maxIntraDistance = getMaxIntraDistance(separatedMap, measure);	
		this.dunnIndex = getDunnIndex(minBetweenDistance, maxIntraDistance);
	}
	
	/**
	 * Return calculated Dunn index.
	 * @param minBetweenDistance minimum distance of the nearest clusters
	 * @param maxIntraDistance maximum distance of two objects in the same cluster
	 * @return Dunn index
	 */
	private Double getDunnIndex(Double minBetweenDistance, Double maxIntraDistance) {		
		if (maxIntraDistance == 0.0) {
			return 0.0;
		}		
		return (minBetweenDistance / maxIntraDistance);
	}
	
	/** 
	 * Returns minimum distance of the nearest clusters.
	 * @param separatedMap map of object by clusters
	 * @param measure selected measure
	 * @return minimum distance of the nearest clusters
	 */
	private Double getMinBetweenDistance(Map<String, List<Example>> separatedMap,
			DistanceMeasure measure) {
		Double minBetweenDistance = Double.MAX_VALUE;
		final Set<String> unusedKeys = new HashSet<>(separatedMap.keySet()); 
		for (String key : separatedMap.keySet()) {
			unusedKeys.remove(key);
			final List<Example> cluster = separatedMap.get(key);
			for (String keyOut : unusedKeys) {
				final List<Example> clusterOut = separatedMap.get(keyOut); 
				final Double distance = getMinBetweenDistance(cluster, clusterOut, measure);				
				if (distance < minBetweenDistance) {				
					minBetweenDistance = distance;
				}
			}
		}
		return minBetweenDistance;
	}
	
	/**
	 * Returns distance of two clusters.
	 * @param cluster cluster
	 * @param clusterOut cluster
	 * @param measure selected measure
	 * @return distance of two clusters
	 */
	private Double getMinBetweenDistance(List<Example> cluster, List<Example> clusterOut, DistanceMeasure measure) {
		Double minBetweenDistance = Double.MAX_VALUE;
		for (Example example : cluster) {
			for (Example exampleOut : clusterOut) {
				final Double distance = measure.calculateDistance(example, exampleOut);
				if (distance < minBetweenDistance) {
					minBetweenDistance = distance;
				}
			}
		}
		return minBetweenDistance;
	}
	
	/**
	 * Returns maximum distance of two objects in the same cluster
	 * @param separatedMap map of object by clusters
	 * @param measure selected measure
	 * @return maximum distance of two objects in the same cluster
	 */
	private Double getMaxIntraDistance(Map<String, List<Example>> separatedMap,
			DistanceMeasure measure) {
		Double maxIntraDistance = 0.0;
		for (String key : separatedMap.keySet()) {
			final List<Example> cluster = separatedMap.get(key);
			for (int i = 0; i < cluster.size(); i++) {
				for (int j = i + 1; j < cluster.size(); j++) {
					final Double distance = measure.calculateDistance(cluster.get(i), cluster.get(j));
					if (distance > maxIntraDistance) {
						maxIntraDistance = distance;
					}
				}
			}
		}		
		return maxIntraDistance;
	}
	
	public Double getDunnIndex () {
		return this.dunnIndex;
	}
}
