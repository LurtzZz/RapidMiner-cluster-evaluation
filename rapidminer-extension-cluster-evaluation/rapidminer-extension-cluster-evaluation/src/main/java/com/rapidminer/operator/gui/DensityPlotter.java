package com.rapidminer.operator.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JProgressBar;
import javax.swing.filechooser.FileFilter;

import org.jfree.chart.JFreeChart;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.gui.plotter.Plotter;
import com.rapidminer.gui.plotter.PlotterAdapter;
import com.rapidminer.gui.plotter.PlotterConfigurationModel;
import com.rapidminer.gui.plotter.conditions.ColumnsPlotterCondition;
import com.rapidminer.gui.plotter.conditions.PlotterCondition;
import com.rapidminer.gui.tools.SwingTools;


/**
 * Density plotter container.
 * @author Jan Jakeš
 *
 */
public class DensityPlotter extends PlotterAdapter {

	/** Version. */
    private static final long serialVersionUID = 1L;

    /** Maximum number of columns. */
    private static final int MAX_NUMBER_OF_COLUMNS = 50;
    
    /** Name of range axis. */
    public static final String RANGE_AXIS_NAME = "Density";

    /** Images buffer. */
    private BufferedImage[][] images = new BufferedImage[0][0];

    /** Plot dimension. */
    private int plotDimension = -1;

    /** Data set. */
    private transient DataTable dataTable;

    /** Width of plotter.  */
    private int plotterWidth;
    
    /** Height of plotter. */
    private int plotterHeight;
    
    /** Number of row. */
    private int numberOfRow = 1;

    /** Progress bar. */
    private JProgressBar progressBar = new JProgressBar();

    /** Calculation thread. */
    private transient Thread calculationThread = null;

    /** Updates. */
    private boolean stopUpdates = false;

    /**
     * Constructs a new instance. 
     * @param settings {@link PlotterConfigurationModel}
     */
    public DensityPlotter(PlotterConfigurationModel settings) {
        super(settings);     
        setBackground(Color.white);
        this.plotterWidth = 600;
        if (numberOfRow == 1) {
        	this.plotterHeight = plotterWidth;
    	} else {
    		this.plotterHeight = this.plotterWidth;
    	}
        progressBar.setToolTipText("Shows the progress of the Scatter Matrix calculation.");
        initDensityPlotter();
    }

    /**
     * Constructs a new instance. 
     * @param settings {@link PlotterConfigurationModel}
     * @param dataTable dataset
     */
    public DensityPlotter(PlotterConfigurationModel settings, DataTable dataTable) {
        this(settings);
        setDataTable(dataTable);
    }

    /** Initialization of Density plotter. */
    private void initDensityPlotter() {
    	final int clusterIndex = settings.getDataTable().getColumnIndex("cluster");
    	setPlotColumn(clusterIndex, true);
    }
    
    @Override
    public void forcePlotGeneration() {
        updatePlotters();
    }

    /** Indicates if the plotter is currently under a process of value adjustments. Might give
     *  implementing plotters a hint that graphical updates should not be performed until all
     *  settings are made. */
    @Override
    public void stopUpdates(boolean value) {
        this.stopUpdates = value;
    }

    @Override
    public void setDataTable(DataTable dataTable) {
        super.setDataTable(dataTable);
        this.dataTable = dataTable;
        if (!stopUpdates)
            updatePlottersInThread();
    }

    @Override
    public PlotterCondition getPlotterCondition() {
        return new ColumnsPlotterCondition(MAX_NUMBER_OF_COLUMNS);
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (calculationThread == null) {
            for (int x = 0; x < images.length; x++) {
                for (int y = 0; y < images[x].length; y++) {
                    Graphics2D newSpace = (Graphics2D) graphics.create();
                    newSpace.translate(x * plotterWidth, y * plotterHeight + MARGIN);
                    newSpace.drawImage(images[x][y], null, 0, 0);
                }
            }
        }

        // key or legend
        if (plotDimension != -1) {
            drawLegend(graphics, dataTable, plotDimension);
        }
    }

    /** Updates plotters. */ 
    private synchronized void updatePlotters() {
        if (plotDimension >= 0) {
            images = new BufferedImage[dataTable.getNumberOfColumns()][numberOfRow];           
            int counter = 0;
            int firstIndex = 0;
            for (int x = 0; x < dataTable.getNumberOfColumns(); x++) {
                if (x != plotDimension) {
                    int secondIndex = 0;
                    for (int y = 0; y < numberOfRow; y++) {

                    	images[firstIndex][secondIndex] = new BufferedImage(plotterWidth, plotterHeight, BufferedImage.TYPE_INT_ARGB);
                        Graphics2D graphics = images[firstIndex][secondIndex].createGraphics();
                        graphics.setColor(Color.BLACK);
                        Rectangle2D rectangle2d = new Rectangle2D.Double(MARGIN, MARGIN, plotterWidth - MARGIN, plotterHeight - (5 * MARGIN));
                        HashMap<String, Class<? extends Plotter>> availablePlotters = new HashMap<>();
                        availablePlotters.put("HistogramSingle", DensitySinglePlotter.class);
                        
                        PlotterConfigurationModel settingsHisto = new PlotterConfigurationModel(availablePlotters, dataTable);
                        DensitySinglePlotter densitySinglePlotter = new DensitySinglePlotter(settingsHisto, dataTable);
                        densitySinglePlotter.setAxis(0, plotDimension);
                        densitySinglePlotter.setPlotColumn(x, true);
                        JFreeChart chart = densitySinglePlotter.getDensityChart();
    
                        chart.draw(graphics, rectangle2d);
                        secondIndex++;
                        progressBar.setValue(++counter);
                    }
                    firstIndex++;
                }
            }
            progressBar.setValue(0);
            revalidate();
            repaint();
        } else {
            images = new BufferedImage[0][0];
            revalidate();
            repaint();
        }
    }

    private void updatePlottersInThread() {
        if (plotDimension >= 0) {
            if (calculationThread == null) {
                progressBar.setMinimum(0);
                progressBar.setMaximum(images.length * images.length);
                progressBar.setValue(0);

                this.calculationThread = new Thread() {
                    @Override
                    public void run() {
                        updatePlotters();
                        calculationFinished();
                    }
                };
                this.calculationThread.start();
            }
        } else {
            images = new BufferedImage[0][0];
            revalidate();
            repaint();
        }
    }

    private void calculationFinished() {
        calculationThread = null;
    }

    @Override
    public Dimension getPreferredSize() {
        if (images.length > 0)
            return new Dimension((images.length - 1) * plotterWidth  + 2 * MARGIN, numberOfRow * plotterHeight + 2 * MARGIN);
        else
            return new Dimension(2 * MARGIN, 2 * MARGIN);
    }

    @Override
    public String getAxisName(int index) {
        return "none";
    }

    @Override
    public Icon getIcon(int index) {
        return null;
    }

//    @Override
//    public boolean isSaveable() {
//        return true;
//    }

    @Override
    public void save() {
        JFileChooser chooser = SwingTools.createFileChooser("file_chooser.save", null, false, new FileFilter[0]);
        if (chooser.showSaveDialog(DensityPlotter.this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            PrintWriter out = null;
            try {
                out = new PrintWriter(new FileWriter(file));
                dataTable.write(out);
                out.close();
            } catch (Exception ex) {
                SwingTools.showSimpleErrorMessage("cannot_write_to_file_0", ex, file);
            } finally {
                if (out != null)
                    out.close();
            }
        }
    }


    @Override
    public boolean canHandleContinousJittering() {
        return false;
    }


    @Override
    public void setPlotColumn(int index, boolean plot) {
        if (plot)
            this.plotDimension = index;
        else
            this.plotDimension = -1;
        if (!stopUpdates)
            updatePlottersInThread();
    }

    @Override
    public boolean getPlotColumn(int index) {
        return this.plotDimension == index;
    }

    @Override
    public JComponent getOptionsComponent(int index) {
        switch (index) {
        case 0:
            return progressBar;
        default:
            return null;
        }
    }

    @Override
    public String getPlotterName() {
        return "Density attribute plotter";
    }
}
