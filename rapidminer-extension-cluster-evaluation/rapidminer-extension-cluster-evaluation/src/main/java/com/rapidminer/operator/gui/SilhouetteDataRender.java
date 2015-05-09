package com.rapidminer.operator.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.rapidminer.gui.renderer.AbstractTableModelTableRenderer;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.tools.container.Pair;

/**
 * Silhouette data table render.
 * @author Jan Jakeš
 *
 */
public class SilhouetteDataRender extends AbstractTableModelTableRenderer {
	
	/** Name of view. */
	@Override
	public String getName() {
		return "Data View";
	}
	
	/** Create new data table. */
	@Override
	public TableModel getTableModel(Object renderable, IOContainer container, boolean arg2) {
		if (renderable instanceof SilhouetteIOObject) {
			SilhouetteIOObject object = (SilhouetteIOObject) renderable;
			final List<Pair<String, Double>> values = new ArrayList<Pair<String, Double>>();
			List<String> keys = new ArrayList<>(object.getValueMap().keySet());
			Collections.sort(keys);
			for (String key : keys) {
				values.add(new Pair<String, Double>(key, object.getValueMap().get(key)));
			}
			
			return new AbstractTableModel() {
				private static final long serialVersionUID = 1L;

				@Override
				public int getColumnCount() {
					return 2;
				}

				@Override
				public String getColumnName(int column) {
					if (column == 0)
						return "Cluster";
					return "Silhouette";
				}
				
				@Override
				public int getRowCount() {
					return values.size();
				}

				@Override
				public Object getValueAt(int rowIndex, int columnIndex) {
					Pair<String, Double> pair = values.get(rowIndex);
					if (columnIndex == 0)
						return pair.getFirst();
					return pair.getSecond();
				}
			};
		}
		return new DefaultTableModel();
	}

	/** Is column sortable. */ 
	@Override
	public boolean isSortable() {
		return true;
	}
	
	/** Is autoresize. */
	@Override
	public boolean isAutoresize() {
		return false;
	}
		
	@Override
	public boolean isColumnMovable() {
		return true;
	}
}
