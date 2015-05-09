package com.rapidminer.operator;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.gui.DensityIOObject;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetPassThroughRule;
import com.rapidminer.operator.ports.metadata.ExampleSetPrecondition;
import com.rapidminer.operator.ports.metadata.SetRelation;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.Ontology;


/**
 * Class represent Density operator.
 * @author Jan Jakeš
 *
 */
public class Density extends Operator {
	
	/** The parameter name for &quot;Indicates the direction of the sorting.&quot; */
	public static final String PARAMETER_SORTING_DIRECTION = "sorting_direction";
	
	/** Input port. */
	private InputPort exampleSetInput = getInputPorts().createPort("cluster set");
	
	/** Output port. */
	private OutputPort densityOutput = getOutputPorts().createPort("density output");
	
	/** Output port. */
	private OutputPort exampleSetOutput = getOutputPorts().createPort("original cluster set");
	
	
	/**
	 * Constructs a new instance.
	 * @param description {@link OperatorDescription}
	 */
	public Density(OperatorDescription description) {
		super(description);
		
		exampleSetInput.addPrecondition(new ExampleSetPrecondition(exampleSetInput, new String[] { "cluster" },
				Ontology.ATTRIBUTE_VALUE));
		
		
		getTransformer().addGenerationRule(densityOutput, DensityIOObject.class);
		getTransformer().addRule(new ExampleSetPassThroughRule(exampleSetInput, exampleSetOutput, SetRelation.EQUAL) {

			@Override			
			public ExampleSetMetaData modifyExampleSet(ExampleSetMetaData metaData) throws UndefinedParameterError {
				return metaData;
			}
		});		
	}
	
	
	@Override
	public void doWork() throws OperatorException {
		final ExampleSet exampleSet = exampleSetInput.getData(ExampleSet.class);
		
		final DensityData densityData = new DensityData(exampleSet);
		
		final DensityIOObject densityIOObject = new DensityIOObject(densityData);				
		
		densityOutput.deliver(densityIOObject);
		exampleSetOutput.deliver(exampleSet);
	}
}


