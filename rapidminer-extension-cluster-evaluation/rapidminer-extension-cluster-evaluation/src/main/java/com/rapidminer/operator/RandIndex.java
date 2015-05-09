package com.rapidminer.operator;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.gui.RandIndexIOObject;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetPassThroughRule;
import com.rapidminer.operator.ports.metadata.ExampleSetPrecondition;
import com.rapidminer.operator.ports.metadata.SetRelation;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.Ontology;

/**
 * Class represent Rand Index operator.
 * @author Jan Jakeš
 *
 */
public class RandIndex extends Operator {
	
	/** Input port. */
	private InputPort exampleSetInputA = getInputPorts().createPort("cluster set A");
	
	/** Input port. */
	private InputPort exampleSetInputB = getInputPorts().createPort("cluster set B");
	
	/** Output port. */
	private OutputPort randIndexOutput = getOutputPorts().createPort("rand index");
	
	/** Output port. */
	private OutputPort exampleSetOutputA = getOutputPorts().createPort("original cluster set A");
	
	/** Output port. */
	private OutputPort exampleSetOutputB = getOutputPorts().createPort("original cluster set B");
	
	public RandIndex(OperatorDescription description) {
		super(description);
		
		exampleSetInputA.addPrecondition(new ExampleSetPrecondition(exampleSetInputA, new String[] { "cluster" },
				Ontology.ATTRIBUTE_VALUE));
		
		exampleSetInputB.addPrecondition(new ExampleSetPrecondition(exampleSetInputB, new String[] { "cluster" },
				Ontology.ATTRIBUTE_VALUE));
		
		getTransformer().addGenerationRule(randIndexOutput, RandIndexIOObject.class);		
		
		getTransformer().addRule(new ExampleSetPassThroughRule(exampleSetInputA, exampleSetOutputA, SetRelation.EQUAL) {

			@Override
			public ExampleSetMetaData modifyExampleSet(ExampleSetMetaData metaData) throws UndefinedParameterError {
				return metaData;
			}
		});
		
		getTransformer().addRule(new ExampleSetPassThroughRule(exampleSetInputB, exampleSetOutputB, SetRelation.EQUAL) {

			@Override
			public ExampleSetMetaData modifyExampleSet(ExampleSetMetaData metaData) throws UndefinedParameterError {
				return metaData;
			}
		});
		
	}
	
	@Override
	public void doWork() throws OperatorException {		
		final ExampleSet clusterSetA = exampleSetInputA.getData(ExampleSet.class);
		final ExampleSet clusterSetB = exampleSetInputB.getData(ExampleSet.class);

		
		final RandIndexData randIndexData = new RandIndexData();
		randIndexData.calculateRandIndex(clusterSetA, clusterSetB);
		final RandIndexIOObject randIndexIOObject = new RandIndexIOObject(randIndexData);
				
		randIndexOutput.deliver(randIndexIOObject);
		exampleSetOutputA.deliver(clusterSetA);
		exampleSetOutputB.deliver(clusterSetB);
	}
	
}
