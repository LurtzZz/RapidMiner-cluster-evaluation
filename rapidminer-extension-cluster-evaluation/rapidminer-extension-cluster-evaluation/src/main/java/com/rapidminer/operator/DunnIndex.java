package com.rapidminer.operator;

import java.util.List;
import java.util.Map;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.gui.DunnIndexIOObject;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetPassThroughRule;
import com.rapidminer.operator.ports.metadata.ExampleSetPrecondition;
import com.rapidminer.operator.ports.metadata.SetRelation;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;

/**
 * Class represent Dunn Index operator.
 * @author Jan Jakeš
 *
 */
public class DunnIndex extends Operator {

	/** Input port. */
	private InputPort exampleSetInput = getInputPorts().createPort("cluster set");
	
	/** Output port. */
	private OutputPort dunnIndexOutput = getOutputPorts().createPort("dunn index");
	
	/** Output port. */
	private OutputPort exampleSetOutput = getOutputPorts().createPort("original cluster set");
	
	/** {@link DistanceMeasureHelper} */
	private DistanceMeasureHelper measureHelper = new DistanceMeasureHelper(this);
	
	/** Selected measure. */
	private DistanceMeasure presetMeasure = null;
	
	
	/**
	 * Constructs a new instance.
	 * @param description {@link OperatorDescription}
	 */
	public DunnIndex(OperatorDescription description) {
		super(description);
		
		exampleSetInput.addPrecondition(new ExampleSetPrecondition(exampleSetInput, new String[] { "cluster" },
				Ontology.ATTRIBUTE_VALUE));
		
		getTransformer().addGenerationRule(dunnIndexOutput, DunnIndexIOObject.class);
		
		getTransformer().addRule(new ExampleSetPassThroughRule(exampleSetInput, exampleSetOutput, SetRelation.EQUAL) {

			@Override
			public ExampleSetMetaData modifyExampleSet(ExampleSetMetaData metaData) throws UndefinedParameterError {
				return metaData;
			}
		});	
	}
	
	
	@Override
	public void doWork() throws OperatorException {
		final ExampleSet clusterSet = exampleSetInput.getData(ExampleSet.class);		
		final ClusterSeparator clusterSeparator = new ClusterSeparator(clusterSet);
		final Map<String, List <Example>> separatedMap = clusterSeparator.getSeparatedClusters();
		
		final DistanceMeasure measure;
		if (presetMeasure != null) {
			measure = presetMeasure;
			measure.init(clusterSet);
		} else {
			measure = measureHelper.getInitializedMeasure(clusterSet);
		}
		
		final DunnIndexData dunnIndexData = new DunnIndexData();
		dunnIndexData.calculateDunnIndex(separatedMap, measure);
		DunnIndexIOObject dunnIndexIOObject = new DunnIndexIOObject(dunnIndexData);
		
		dunnIndexOutput.deliver(dunnIndexIOObject);
		exampleSetOutput.deliver(clusterSet);
	}
	

	@Override
	public List<ParameterType> getParameterTypes() {
		final List<ParameterType> types = super.getParameterTypes();
		for(ParameterType a : DistanceMeasures.getParameterTypes(this)) {
			if (a.getKey() == DistanceMeasures.PARAMETER_MEASURE_TYPES) {
				a.setDefaultValue(DistanceMeasures.DIVERGENCES_TYPE);
			}
	        if (a.getKey() == DistanceMeasures.PARAMETER_DIVERGENCE) {
	        	a.setDefaultValue(6);
	        }
			types.add(a);
		}
		return types;
	}
	
}
