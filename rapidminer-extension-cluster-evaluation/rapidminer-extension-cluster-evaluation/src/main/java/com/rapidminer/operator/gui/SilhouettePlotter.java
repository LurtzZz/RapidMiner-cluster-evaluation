package com.rapidminer.operator.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.DataTableRow;
import com.rapidminer.gui.plotter.PlotterAdapter;
import com.rapidminer.gui.plotter.PlotterConfigurationModel;
import com.rapidminer.gui.plotter.ToolTip;
import com.rapidminer.gui.plotter.conditions.ColumnsPlotterCondition;
import com.rapidminer.gui.plotter.conditions.PlotterCondition;
import com.rapidminer.operator.learner.bayes.DistributionModel;
import com.rapidminer.tools.math.MathFunctions;

/**
 * Silhouette plotter.
 * @author Jan Jakeš
 *
 */
public class SilhouettePlotter extends PlotterAdapter implements MouseListener {

	/** Version. */
	private static final long serialVersionUID = 1L;
	
	/** Max number of column. */
    private static final int MAX_NUMBER_OF_COLUMNS = 1;
	
    /** First dimension. */
	private static final int FIRST  = 0;
	
	/** Second dimension. */
	private static final int SECOND = 1;
	
	/** Third dimension. */
	private static final int THIRD  = 2;
	
	/** {@link SurveyRow} */ 
	private class SilhouetteRow implements Comparable<SilhouetteRow> {
		
		private double[] data;
		private double color;
		
		private SilhouetteRow(double[] data, double color) {
			this.data = data;
			this.color = color;
		}
		
		public int compareTo(SilhouetteRow row) {
			int result = 0;
			for (int i = 0; i < sortingDimensions.length; i++) {
				if (sortingDimensions[i] != -1) {
					result = Double.compare(this.data[sortingDimensions[i]], row.data[sortingDimensions[i]]);
					if (result != 0)
						return result;
				}
			}
			if ((result == 0) && (colorColumn > -1)) {
				result = Double.compare(this.data[colorColumn], row.data[colorColumn]);
			}
			return result;
		}
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof SilhouetteRow)) {
				return false;
			} else {
				return this.data == ((SilhouetteRow)o).data;
			}
		}
		
		@Override
		public int hashCode() {
			return Arrays.hashCode(data);
		}
	}
	
	/** Data set. */ 
	private transient DataTable dataTable;
    
	/** Max weight. */
    private double maxWeight = Double.NaN;

    /** Colored column of data table. */
	private int colorColumn = -1;

	/** Sorting dimensions. */
	private int[] sortingDimensions = new int[] { -1, -1, -1 };
	
	/** Silhouette rows. */
	private List<SilhouetteRow> dataRows = new LinkedList<SilhouetteRow>();
	
	private double[] min, max;
	
	/** Tool tip. */
	private transient ToolTip toolTip = null;
	
	/**
     * Constructs a new instance. 
     * @param settings {@link PlotterConfigurationModel}
	 */
	public SilhouettePlotter(PlotterConfigurationModel settings) {		
		super(settings);
		setBackground(Color.white);
		addMouseListener(this);
		initSilhouettePlotter();
	}
	

	/**
     * Constructs a new instance. 
     * @param settings {@link PlotterConfigurationModel}
	 * @param dataTable {@link DataTable}
	 */
	public SilhouettePlotter(PlotterConfigurationModel settings, DataTable dataTable) {
		this(settings);
		setDataTable(dataTable);
	}
	
    /** Initialization of Silhouette plotter. */
	private void initSilhouettePlotter() {
		final int clusterIndex = settings.getDataTable().getColumnIndex("cluster");
		final int silhouetteIndex = settings.getDataTable().getColumnIndex("silhouette");
		setAxis(1, clusterIndex);
		setAxis(2, silhouetteIndex);
		setPlotColumn(clusterIndex, true);
	}
	
	@Override
	public void setDataTable(DataTable dataTable) {
		super.setDataTable(dataTable);
		this.dataTable = dataTable;
		repaint();
	}
	
	
    @Override
	public PlotterCondition getPlotterCondition() {
        return new ColumnsPlotterCondition(MAX_NUMBER_OF_COLUMNS);
    }
	
    
	@Override
	public void setPlotColumn(int index, boolean plot) {
		if (plot)
			this.colorColumn = index;
		else
			this.colorColumn = -1;
		repaint();
	}
    
	
	@Override
	public boolean getPlotColumn(int index) {
		return colorColumn == index;
	}
    
	@Override
	public String getPlotName() { return "Color"; }
	
	
	@Override
	public int getNumberOfAxes() {
		return sortingDimensions.length;
	}
	
	
	@Override
	public void setAxis(int index, int dimension) {
		sortingDimensions[index] = dimension;
		repaint();
	}
	
	@Override
	public int getAxis(int index) {
		return sortingDimensions[index];
	}
	
	@Override
	public String getAxisName(int index) {
		switch (index) {
			case FIRST:
				return "Sort first dimension";
			case SECOND:
				return "Sort second dimension";
			case THIRD:
				return "Sort third dimension";
			default:
				return "none";
		}
	}
	
	
	/**
	 * Prepares data for plotter.
	 */
	private void prepareData() {
		dataRows.clear();
		this.min = new double[this.dataTable.getNumberOfColumns()];
		this.max = new double[this.dataTable.getNumberOfColumns()];
		for (int d = 0; d < min.length; d++) {
			this.min[d] = Double.POSITIVE_INFINITY;
			this.max[d] = Double.NEGATIVE_INFINITY;
		}
		
		synchronized (dataTable) {			
			Iterator<DataTableRow> i = this.dataTable.iterator();
			while (i.hasNext()) {
				DataTableRow row = i.next();
				for (int d = 0; d < row.getNumberOfValues(); d++) {
					double value = row.getValue(d);
					this.min[d] = MathFunctions.robustMin(this.min[d], value);
					this.max[d] = MathFunctions.robustMax(this.max[d], value);
				}
			}
			i = this.dataTable.iterator();
			while (i.hasNext()) {
				DataTableRow row = i.next();
				double[] data = new double[row.getNumberOfValues()];
				for (int d = 0; d < data.length; d++) {
					data[d] = row.getValue(d);
				}
				double color = 1.0d;
				if (colorColumn >= 0) {
					color = getColorProvider().getPointColorValue(this.dataTable, row, colorColumn, min[colorColumn], max[colorColumn]);
				}
				dataRows.add(new SilhouetteRow(data, color));
			}
            
            this.maxWeight = getMaxWeight(dataTable);
            
		}
		Collections.sort(dataRows);
	}
	
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(getWidth(), this.dataTable.getNumberOfRows() + 3 * MARGIN);
	}
	
	public void setToolTip(ToolTip toolTip) {
		this.toolTip = toolTip;
		repaint();
	}
	

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		paintSilhouettePlot(g);
	}
	
	/**
	 * Paint silhouette plotter.
	 * @param graphics {@link Graphics}
	 */
	public void paintSilhouettePlot(Graphics graphics) {
		prepareData();
		
		// legend
		if ((colorColumn >= 0) && (colorColumn < min.length) && 
			!Double.isInfinite(min[colorColumn]) && !Double.isInfinite(max[colorColumn]) && 
			(dataTable.isNominal(colorColumn) || (min[colorColumn] != max[colorColumn])) && (dataRows.size() > 0)) {
			drawLegend(graphics, dataTable, colorColumn);
		}
		
		// translation
		Graphics2D g = (Graphics2D)graphics.create();
		g.translate(MARGIN, MARGIN);
		int width = getWidth() - 2 * MARGIN;
		
		// frame
		Rectangle2D frame = new Rectangle2D.Double(-1, MARGIN - 1, width + 1, this.dataTable.getNumberOfRows() + 1);
		g.setColor(GRID_COLOR);
		g.draw(frame);
	
		// columns
		float columnDistance = (float)width; 
		float currentX = 0.0f;
		int columnSilhouette = settings.getDataTable().getColumnIndex("silhouette");
		paintSilhouetteFrame(g, columnSilhouette, currentX, columnDistance);
		
		// tool tip
		drawToolTip((Graphics2D)graphics, this.toolTip);
	}
	
	/**
	 * Paint silhouette frame.
	 * @param graphics {@link Graphics}
	 * @param column silhouette column
	 * @param currentX current x position
	 * @param columnDistance width of plotter
	 */
	private void paintSilhouetteFrame(Graphics graphics, int column, float currentX, float columnDistance) {
		Graphics2D g = (Graphics2D)graphics.create();
		g.translate(currentX, 0);
        
        // draw weight rect
        if (dataTable.isSupportingColumnWeights()) {
            Color weightColor = getWeightColor(dataTable.getColumnWeight(column), this.maxWeight);
            Rectangle2D weightRect = new Rectangle2D.Double(0, MARGIN, columnDistance, this.dataTable.getNumberOfRows());
            g.setColor(weightColor);
            g.fill(weightRect);
        }
        
        g.drawString(this.dataTable.getColumnName(column), 0, MARGIN - 3);
		g.translate(0, MARGIN);				
		
		g.translate(1, 0);
		columnDistance--;
		paintSilhouetteRows(g, column, columnDistance);
	}
	
	/**
	 * Paint row in silhouette plotter.
	 * @param g {@link Graphics2D}
	 * @param column silhouette column
	 * @param columnDistance width of plotter
	 */
	private void paintSilhouetteRows(Graphics2D g, int column, float columnDistance) {
		float halfColumnDistance = columnDistance / 2;
		int counter = 0;
		Iterator<SilhouetteRow> s = this.dataRows.iterator();
		while (s.hasNext()) {
			SilhouetteRow row = s.next();
			double[] data = row.data;
			double color = row.color;
			g.setColor(getColorProvider().getPointColor(color));
			double length = data[column] * halfColumnDistance;
			if (length < 0) {
				g.drawLine((int) (halfColumnDistance + length), counter, (int)halfColumnDistance, counter);
			} else {
				g.drawLine((int) halfColumnDistance, counter, (int) (halfColumnDistance + length), counter);
			}
			counter++;
		}
		g.setColor(Color.black);
		g.drawLine((int) halfColumnDistance, 0, (int) halfColumnDistance, this.dataTable.getNumberOfRows());
	}
	
	public void mousePressed(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}

	
	public void mouseReleased(MouseEvent e) {
		int xPos = e.getX();
		int yPos = e.getY();
		if ((xPos > MARGIN) && (xPos < getWidth() - MARGIN) && (yPos - (2 * MARGIN) >= 0) && (yPos - (2 * MARGIN) < dataRows.size())) {			
			int rowIndex = yPos - (2 * MARGIN);
			int columnSilhouette = settings.getDataTable().getColumnIndex("silhouette");
			setToolTip(new ToolTip("Silhouette: " +this.dataRows.get(rowIndex).data[columnSilhouette], xPos, yPos));
		} else {
			setToolTip(null);
		}
	}
	
	@Override
	public String getPlotterName() {
		return "Silhouette";
	}
}
