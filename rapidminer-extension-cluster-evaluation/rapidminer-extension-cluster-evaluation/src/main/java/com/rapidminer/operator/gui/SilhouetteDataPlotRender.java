package com.rapidminer.operator.gui;

import java.awt.Component;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.DataTableExampleSetAdapter;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.gui.plotter.Plotter;
import com.rapidminer.gui.plotter.PlotterConfigurationModel;
import com.rapidminer.gui.plotter.PlotterConfigurationSettings;
import com.rapidminer.gui.plotter.PlotterPanel;
import com.rapidminer.gui.renderer.AbstractDataTablePlotterRenderer;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.MissingIOObjectException;

/**
 * Silhouette plotter render.
 * @author Jan Jakeš
 *
 */
public class SilhouetteDataPlotRender extends AbstractDataTablePlotterRenderer {

	@Override
	public LinkedHashMap<String,Class<? extends Plotter>> getPlotterSelection() {
		final LinkedHashMap<String, Class<? extends Plotter>> availablePlotters = new LinkedHashMap<String, Class<? extends Plotter>>();
    	availablePlotters.put("Silhouette", SilhouettePlotter.class);
		return availablePlotters;
	}
	
	
    @Override
	public DataTable getDataTable(Object renderable, IOContainer container) {
		ExampleSet exampleSet = ((SilhouetteIOObject) renderable).getClusterSet();
        AttributeWeights weights = null;
        if (container != null) {
            try {
                weights = container.get(AttributeWeights.class);
                for (Attribute attribute : exampleSet.getAttributes()) {
                    double weight = weights.getWeight(attribute.getName());
                    if (Double.isNaN(weight)) { // not compatible
                        weights = null;
                        break;
                    }
                }
            } catch (MissingIOObjectException e) {}
        }
        return  new DataTableExampleSetAdapter(exampleSet, weights);
	}
    
    
    @Override
    public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
    	return new PlotterPanel(getPlotterSettings(renderable, ioContainer));
    }
    
    

    /**
     * Gets configuration of plotter.
     * @param renderable {@link SilhouetteIOObject}
     * @param ioContainer {@link IOContainer}
     * @return {@link PlotterConfigurationModel}
     */
    private PlotterConfigurationModel getPlotterSettings(Object renderable, IOContainer ioContainer) {
    	final String plotterName = "Silhouette";
    	final HashMap<String, Class<? extends Plotter>> availablePlotters = new HashMap<>();
    	availablePlotters.put("Silhouette", SilhouettePlotter.class);
    	
    	PlotterConfigurationSettings configurationSettings = new PlotterConfigurationSettings();
    	configurationSettings.setAvailablePlotters(availablePlotters);
    	configurationSettings.setPlotterName(plotterName);
    	
    	PlotterConfigurationModel configurationModel = new PlotterConfigurationModel(configurationSettings, getPlotterSelection(), getDataTable(renderable, ioContainer));
    	return configurationModel;
    }  
}
