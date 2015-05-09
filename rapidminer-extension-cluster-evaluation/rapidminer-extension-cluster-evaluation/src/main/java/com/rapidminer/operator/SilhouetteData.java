package com.rapidminer.operator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

/**
 * Class for calculating silhouette.
 * @author Jan Jakeš
 *
 */
public class SilhouetteData {
	
	/** Version. */
	private static final long serialVersionUID = 1L;
	
	/** Map of silhouette of examples. */
	private Map<String, Double[]> mapOfSilhouette;
	
	/** Map of silhouette of clusters. */
	private Map<String, Double> mapOfAverageSilhouette;
	
	/** Clustered dataset. */
	private ExampleSet clusterSet;
	
	/** Constructs a new instance. */
	public SilhouetteData() {
		mapOfSilhouette = new HashMap<>(); 
		mapOfAverageSilhouette = new HashMap<>();
	}
	
	
	/**
	 * Calculates silhouette of examples of data set and silhouette of clusters
	 * @param separatedElements map of object by clusters
	 * @param measure selected measure
	 */
	public void calculateSilhouette(Map<String, List<Example>> separatedElements, DistanceMeasure measure) {
		if (separatedElements.size() == 1) {
			final String key = separatedElements.keySet().iterator().next();
			this.mapOfAverageSilhouette.put(key, 0.0);
		} else if (separatedElements.size() < 1) {

		} else {
			final Map<String, Double[]> mapOfAverageDistancesIn = calculateAverageDistancesIn(
					separatedElements, measure);
			final Map<String, Double[]> mapOfAverageDistancesOut = calculateAverageDistancesOut(
					separatedElements, measure);	
			this.mapOfSilhouette = getSilhouette(
					mapOfAverageDistancesIn, mapOfAverageDistancesOut);
			this.mapOfAverageSilhouette = getAverageSilhouettes(mapOfSilhouette);	
		}
	}
	
	/**
	 * Calculates average silhouette of clusters.
	 * @param mapOfSilhouette map of silhouette of examples.
	 * @return map of average silhouette of clusters
	 */
	private Map<String, Double> getAverageSilhouettes(Map<String, Double[]> mapOfSilhouette) {
		Map<String, Double> mapOfAverageSilhouette = new HashMap<>();
		for (String key : mapOfSilhouette.keySet()) {
			mapOfAverageSilhouette.put(key, getAverageSilhouette(mapOfSilhouette.get(key)));
		}
		return mapOfAverageSilhouette;
	}
	
	
	/**
	 * Calculates average silhouette of given cluster.
	 * @param clusterSilhouette silhouette of examples of cluster
	 * @return average silhouette of cluster
	 */
	private Double getAverageSilhouette(Double[] clusterSilhouette) {
		Double sumOfSilhouettes = 0.0;
		Double numberOfSilhouette = 0.0;
		for (Double silhouette : clusterSilhouette) {
			sumOfSilhouettes = sumOfSilhouettes + silhouette;
			++numberOfSilhouette;
		}
		if (numberOfSilhouette > 0) {
			return sumOfSilhouettes / numberOfSilhouette; 
		}
		return 0.0;
	}
	
	/**
	 * Calculates silhouette of examples.
	 * @param mapOfAverageDistancesIn average distances into cluster
	 * @param mapOfAverageDistancesOut average distances with other clusters
	 * @return map of silhouette of examples
	 */
	private Map<String, Double[]> getSilhouette(Map<String, Double[]> mapOfAverageDistancesIn, Map<String, Double[]> mapOfAverageDistancesOut) {
		final Map<String, Double[]> mapOfSilhouette = new HashMap<>();
		for (String key : mapOfAverageDistancesIn.keySet()) {
			final Double[] averageDistanceIn = mapOfAverageDistancesIn.get(key);
			final Double[] averageDistanceOut = mapOfAverageDistancesOut.get(key);
			final Double[] silhouettes = new Double[averageDistanceIn.length]; 
			if (averageDistanceIn.length == 1) {
				silhouettes[0] = 0.0;
			} else {
				for (int i = 0; i < averageDistanceIn.length; i++) {
					if (averageDistanceIn[i] < averageDistanceOut[i]) {
						if (averageDistanceOut[i] != 0.0) {
							silhouettes[i] = 1 - (averageDistanceIn[i] / averageDistanceOut[i]);
						} else {
							silhouettes[i] = 0.0;
						}			
					} else if (averageDistanceIn[i] > averageDistanceOut[i]) {
						if (averageDistanceIn[i] != 0.0) {
							silhouettes[i] = (averageDistanceOut[i] / averageDistanceIn[i]) - 1;
						} else {
							silhouettes[i] = 0.0;
						}		
					} else if (averageDistanceIn[i] == averageDistanceOut[i]) {
						silhouettes[i] = 0.0;
					} 
				}
			}
			mapOfSilhouette.put(key, silhouettes);
		}
		return mapOfSilhouette;
	}
	
	
	/**
	 * Calculates average distance between examples and nearest clusters. 
	 * @param separatedMap map of object by clusters
	 * @param measure selected measure
	 * @return map of distance between examples and nearest clusters
	 */
	private Map<String, Double[]> calculateAverageDistancesOut(Map<String, List<Example>> separatedMap, DistanceMeasure measure) {
		final Map<String, Double[]> nearestCluster = findNearestCluster(separatedMap, measure);
		return nearestCluster;
	}
	
	
	/**
	 * Calculates average distances between examples and nearest clusters.
	 * @param separatedMap map of object by clusters
	 * @param measure selected measure
	 * @return map of average distances between examples and nearest cluster
	 */
	private Map<String, Double[]> findNearestCluster(Map<String, List<Example>> separatedMap, DistanceMeasure measure) {
		final Set<String> keys = separatedMap.keySet();
		final Map<String, Double[]> mapOfNearestCluster = new HashMap<>();
		for (String key : keys) { 
			mapOfNearestCluster.put(key, getNearestCluster(separatedMap.get(key), key, separatedMap, measure));	
		}
		return mapOfNearestCluster;
	}

	/**
	 * Calculates average distances between examples and nearest cluster.
	 * @param cluster selected cluster
	 * @param clusterName name of selected cluster
	 * @param separatedMap map of object by clusters
	 * @param measure selected measure
	 * @return average distances between examples and nearest cluster
	 */
	private Double[] getNearestCluster(List<Example> cluster,
			String clusterName, 
			Map<String, List<Example>> separatedMap,
			DistanceMeasure measure) {
		Double[] nearestDistances = new Double[cluster.size()];
		int i = 0;
		for (Example example : cluster) {
			double minAverageDistance = Double.MAX_VALUE;
			for (String key : separatedMap.keySet()) {
				if (!key.equals(clusterName)) {					
					Double averageDistance = getAverageDistance(example, separatedMap.get(key), measure);
					if (minAverageDistance > averageDistance) {
						minAverageDistance = averageDistance;
					}
				}
			}
			nearestDistances[i] = minAverageDistance;
			++i;
		}
		return nearestDistances;
	}
	
	
	/**
	 * Calculates average distances between examples in clusters.
	 * @param separatedMap map of object by clusters
	 * @param measure selected measure
	 * @return map of average distances between examples in cluster
	 */
	private Map<String, Double[]> calculateAverageDistancesIn(Map<String, List<Example>> separatedMap, DistanceMeasure measure) {
		final Set<String> keys = separatedMap.keySet();
		final Map<String, Double[]> mapOfAverageDistances = new HashMap<>();
		for (String key : keys) {
			 mapOfAverageDistances.put(key, getAverageDistancesIn(separatedMap.get(key), measure));			
		}
		return mapOfAverageDistances;
	}
	
	/**
	 * Calculates average distances between examples in cluster. 
	 * @param cluster given cluster
	 * @param measure selected measure
	 * @return average distances between examples in given cluster
	 */
	private Double[] getAverageDistancesIn(List<Example> cluster, DistanceMeasure measure) {
		final Double[] averangeDistances = new Double[cluster.size()];
		if (cluster.size() == 0) {
			
		} else if (cluster.size() == 1) {
			averangeDistances[0] = 0.0;
		} else if (cluster.size() > 1) {
			int i = 0;
			for (Example exampleCalculated : cluster) {
				averangeDistances[i] = getAverageDistance(exampleCalculated, cluster, measure);
				++i;
			}
		}
		return averangeDistances;
	}
	
	
	/**
	 * Calculate average distance between example and given cluster.
	 * @param example {@link Example}
	 * @param cluster given cluster
	 * @param measure selected measure
	 * @return average distance
	 */
	private Double getAverageDistance(Example example, List<Example> cluster, DistanceMeasure measure) {
		Double sumOfDistances = 0.0;
		Double numberOfDistances = 0.0;
		for (Example exampleOut : cluster) {
			if (!example.equals(exampleOut)) {
				sumOfDistances = sumOfDistances + measure.calculateDistance(example, exampleOut);
				numberOfDistances++;
			}
		}
		if (numberOfDistances > 0.0) {
			return sumOfDistances / numberOfDistances;
		}
		return 0.0;
	}
	
	public Map<String, Double[]> getSilhouette () {
		return mapOfSilhouette;
	}
	
	public Map<String, Double> getAverageClusterSilhouette() {
		return mapOfAverageSilhouette;
	}
	
	public ExampleSet getClusterSet() {
		return clusterSet;
	}
	
	public void setClusterSet(ExampleSet clusterSet) {
		this.clusterSet = clusterSet;
	}
	
}
