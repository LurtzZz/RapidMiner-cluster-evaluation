package com.rapidminer.operator;

import java.util.List;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.gui.SilhouetteIOObject;
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
 * Class represent Silhouette operator.
 * @author Jan Jakeš
 *
 */
public class Silhouette extends Operator {
	
	/** Input port. */
	private InputPort exampleSetInput = getInputPorts().createPort("cluster set");
	
	/** Output port. */
	private OutputPort silhouetteSetOutput = getOutputPorts().createPort("silhouette set");
	
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
	public Silhouette(OperatorDescription description) {
		super(description);

		exampleSetInput.addPrecondition(new ExampleSetPrecondition(exampleSetInput, new String[] { "cluster" },
				Ontology.ATTRIBUTE_VALUE));
		
		
		getTransformer().addRule(new ExampleSetPassThroughRule(exampleSetInput, exampleSetOutput, SetRelation.EQUAL) {

			@Override
			public ExampleSetMetaData modifyExampleSet(ExampleSetMetaData metaData) throws UndefinedParameterError {
				return metaData;
			}
		});
		
		getTransformer().addGenerationRule(silhouetteSetOutput, SilhouetteIOObject.class);
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
		logNote("Start Silhouette calculation ...");
		final SilhouetteData silhouetteData = new SilhouetteData();
		silhouetteData.calculateSilhouette(separatedMap, measure);
		silhouetteData.setClusterSet(clusterSet);
		final SilhouetteIOObject silhouetteIOObject = new SilhouetteIOObject(silhouetteData);
	
		
		final Attributes attributes = clusterSet.getAttributes();
		final Attribute silhouetteAtt = AttributeFactory.createAttribute("silhouette", Ontology.REAL);
		clusterSet.getExampleTable().addAttribute(silhouetteAtt);
		attributes.setSpecialAttribute(silhouetteAtt, "Silhouette");
		
		final Map<String, Double[]> mapOfSilhouette = silhouetteData.getSilhouette();
		for (String key : mapOfSilhouette.keySet()) {
			int i = 0;
			for (Double silhouette : mapOfSilhouette.get(key)) {
				final Example example = separatedMap.get(key).get(i);
				example.setValue(silhouetteAtt, silhouette);
				i++;
			}
		}
		
		silhouetteSetOutput.deliver(silhouetteIOObject);
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
