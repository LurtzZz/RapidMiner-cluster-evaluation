package com.rapidminer.operator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

/**
 * Class for calculating Rand index.
 * @author Jan Jakeš
 *
 */
public class RandIndexData {
	
	/** Version. */
	private static final long serialVersionUID = 1L;
	
	/** Value of Rand index. */
	private Double randIndex;
	
	/** Constructs a new instance. */
	public RandIndexData() { }	
	
	
	/**
	 * Calculates Rand index value.
	 * @param clusterSetA clustered dataset A
	 * @param clusterSetB clustered dataset B
	 */
	public void calculateRandIndex(ExampleSet clusterSetA, ExampleSet clusterSetB) {
		if (clusterSetA.size() == clusterSetB.size()) {
			final Attributes attributes = clusterSetA.getAttributes();
			final Attribute clusterAtt = attributes.get("cluster");
			
			double a = 0;
			double b = 0; 
			double c = 0;
			double d = 0;
			
			for (int i = 0; i < clusterSetA.size(); i++) {
				for (int j = i + 1; j < clusterSetA.size(); j++) {
					Example exampleA1 = clusterSetA.getExample(i);
					Example exampleA2 = clusterSetA.getExample(j);
					Example exampleB1 = clusterSetB.getExample(i);
					Example exampleB2 = clusterSetB.getExample(j);
					
					if (exampleA1.getId() != exampleB1.getId()) {
						exampleB1 = clusterSetB.getExampleFromId(exampleA1.getId());
					} 
					if  (exampleA2.getId() != exampleB2.getId()) {
						exampleB2 = clusterSetB.getExampleFromId(exampleA2.getId());
					}
					if (exampleB1 == null || exampleB2 == null) {
						this.randIndex = -2.0;
						return;
					}
					
					final String pairA1 = exampleA1.getValueAsString(clusterAtt);
					final String pairA2 = exampleA2.getValueAsString(clusterAtt);				
					final String pairB1 = exampleB1.getValueAsString(clusterAtt);
					final String pairB2 = exampleB2.getValueAsString(clusterAtt);
					
					
					final Boolean pairA = comparePair(pairA1, pairA2);
					final Boolean pairB = comparePair(pairB1, pairB2);
					
					if (pairA == true && pairB == true) {
						a++;
					} else if (pairA == true && pairB == false) {
						c++;
					} else if (pairA == false && pairB == true) {
						d++;
					} else if (pairA == false && pairB == false) {
						b++;
					}
				}
			}
			this.randIndex = getRandIntex(a, b, c, d);
		} else {
			this.randIndex = -1.0;
		} 
	}
	
	/**
	 * Compare pair of values.
	 * @param pair1 value 
	 * @param pair2 value
	 * @return
	 */
	private Boolean comparePair(String pair1, String pair2) {
		return pair1.equals(pair2);
	}
	
	/**
	 * Return calculated Rand index.
	 * @param a a
	 * @param b b 
	 * @param c c
	 * @param d d
	 * @return Rand index
	 */
	private double getRandIntex(double a, double b, double c, double d) {		
		if (a == 0 && b == 0 && c == 0 && d == 0) {
			return -2.0;
		}
		return (a + b) / (a + b + c + d );
	}
	
	public Double getRandIndex () {
		return this.randIndex;
	}
}
