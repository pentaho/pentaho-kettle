/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.ui.spoon.trans;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.performance.StepPerformanceSnapShot;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.delegates.SpoonDelegate;
import org.pentaho.di.ui.trans.dialog.TransDialog;
import org.pentaho.di.ui.util.ImageUtil;

public class TransPerfDelegate extends SpoonDelegate {
	private static Class<?> PKG = Spoon.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	// private static final LogWriter log = LogWriter.getInstance();
	
	private static final int DATA_CHOICE_WRITTEN = 0;
	private static final int DATA_CHOICE_READ = 1;
	private static final int DATA_CHOICE_INPUT = 2;
	private static final int DATA_CHOICE_OUTPUT = 3;
	private static final int DATA_CHOICE_UPDATED = 4;
	private static final int DATA_CHOICE_REJECTED = 5;
	private static final int DATA_CHOICE_INPUT_BUFFER_SIZE = 6;
	private static final int DATA_CHOICE_OUTPUT_BUFFER_SIZE = 7;
	
	private static String[] dataChoices = new String[] {
			BaseMessages.getString(PKG, "StepPerformanceSnapShotDialog.Written"),
			BaseMessages.getString(PKG, "StepPerformanceSnapShotDialog.Read"),
			BaseMessages.getString(PKG, "StepPerformanceSnapShotDialog.Input"),
			BaseMessages.getString(PKG, "StepPerformanceSnapShotDialog.Output"),
			BaseMessages.getString(PKG, "StepPerformanceSnapShotDialog.Updated"),
			BaseMessages.getString(PKG, "StepPerformanceSnapShotDialog.Rejected"),
			BaseMessages.getString(PKG, "StepPerformanceSnapShotDialog.InputBufferSize"),
			BaseMessages.getString(PKG, "StepPerformanceSnapShotDialog.OutputBufferSize"),
		};

	private TransGraph transGraph;

	private CTabItem transPerfTab;
	
	private Map<String, List<StepPerformanceSnapShot>> stepPerformanceSnapShots;
	private String[] steps;
	private org.eclipse.swt.widgets.List stepsList;
	private Canvas canvas;
	private Image image;
	private long timeDifference;
	private String title;
	private org.eclipse.swt.widgets.List dataList;
	private Composite perfComposite;
	private boolean emptyGraph;

	
	/**
	 * @param spoon
	 * @param transGraph
	 */
	public TransPerfDelegate(Spoon spoon, TransGraph transGraph) {
		super(spoon);
		this.transGraph = transGraph;
	}
	
	public void addTransPerf() {
		// First, see if we need to add the extra view...
		//
		if (transGraph.extraViewComposite==null || transGraph.extraViewComposite.isDisposed()) {
			transGraph.addExtraView();
		} else {
			if (transPerfTab!=null && !transPerfTab.isDisposed()) {
				// just set this one active and get out...
				//
				transGraph.extraViewTabFolder.setSelection(transPerfTab);
				return; 
			}
		}
		
		// Add a transLogTab : display the logging...
		//
		transPerfTab = new CTabItem(transGraph.extraViewTabFolder, SWT.NONE );
		transPerfTab.setImage(GUIResource.getInstance().getImageShowPerf());
		transPerfTab.setText(BaseMessages.getString(PKG, "Spoon.TransGraph.PerfTab.Name"));
		
		// Create a composite, slam everything on there like it was in the history tab.
		//
		perfComposite = new Composite(transGraph.extraViewTabFolder, SWT.NONE);
		perfComposite.setBackground(GUIResource.getInstance().getColorBackground());
		perfComposite.setLayout(new FormLayout());
		
        spoon.props.setLook(perfComposite);

        transGraph.getDisplay().asyncExec(new Runnable() {
		
			public void run() {
				setupContent();
			}
		});
		
		transPerfTab.setControl(perfComposite);
		
		transGraph.extraViewTabFolder.setSelection(transPerfTab);
		
		transGraph.extraViewTabFolder.addSelectionListener(new SelectionAdapter() {
				
				public void widgetSelected(SelectionEvent arg0) {
					layoutPerfComposite();
					updateGraph();
				}
			}
		);
	}

    
    public void setupContent() {
    	// there is a potential infinite loop below if this method
    	// is called when the transgraph is not running, so we check
    	// early to make sure it won't happen (see PDI-5009)
    	if (!transGraph.isRunning() || transGraph.trans==null || !transGraph.trans.getTransMeta().isCapturingStepPerformanceSnapShots()) {
    		showEmptyGraph();
    		return; // TODO: display help text and rerty button
    	}
    	
    	if (perfComposite.isDisposed()) return;
    	
    	// Remove anything on the perf composite, like an empty page message
    	//
    	for (Control control : perfComposite.getChildren()) if (!control.isDisposed()) control.dispose();
    	
    	emptyGraph=false;
    	
    	this.title = transGraph.trans.getTransMeta().getName();
    	this.timeDifference = transGraph.trans.getTransMeta().getStepPerformanceCapturingDelay();
		this.stepPerformanceSnapShots = transGraph.trans.getStepPerformanceSnapShots();

		// Wait a second for the first data to pour in...
		// TODO: make this wait more elegant...
		//
		while(this.stepPerformanceSnapShots==null || stepPerformanceSnapShots.isEmpty()) {
			this.stepPerformanceSnapShots = transGraph.trans.getStepPerformanceSnapShots();
			try { Thread.sleep(100L); } catch (InterruptedException e) { }
		}
		
		Set<String> stepsSet = stepPerformanceSnapShots.keySet();
		steps = stepsSet.toArray(new String[stepsSet.size()]);
		Arrays.sort(steps);

		
		// Display 2 lists with the data types and the steps on the left side.
		// Then put a canvas with the graph on the right side
		// 
		Label dataListLabel = new Label(perfComposite, SWT.NONE);
		dataListLabel.setText(BaseMessages.getString(PKG, "StepPerformanceSnapShotDialog.Metrics.Label"));
		spoon.props.setLook(dataListLabel);
    FormData fdDataListLabel = new FormData();

    fdDataListLabel.left   = new FormAttachment(0, 0);
    fdDataListLabel.right  = new FormAttachment(spoon.props.getMiddlePct()/2, Const.MARGIN);
    fdDataListLabel.top    = new FormAttachment(0, Const.MARGIN+5);
    dataListLabel.setLayoutData(fdDataListLabel);
		
		dataList = new org.eclipse.swt.widgets.List(perfComposite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.LEFT | SWT.BORDER);
		spoon.props.setLook(dataList);
		dataList.setItems(dataChoices);
		dataList.addSelectionListener(new SelectionAdapter() {
		
			public void widgetSelected(SelectionEvent event) {
				
				// If there are multiple selections here AND there are multiple selections in the steps list, we only take the first step in the selection...
				//
				if (dataList.getSelectionCount()>1 && stepsList.getSelectionCount()>1) {
					stepsList.setSelection(stepsList.getSelectionIndices()[0]);
				}
				
				updateGraph();
			}
		});

		FormData fdDataList = new FormData();
		fdDataList.left   = new FormAttachment(0, 0);
		fdDataList.right  = new FormAttachment(spoon.props.getMiddlePct()/2, Const.MARGIN);
    fdDataList.top    = new FormAttachment(dataListLabel, Const.MARGIN);
    fdDataList.bottom = new FormAttachment(40, Const.MARGIN);
		dataList.setLayoutData(fdDataList);

    Label stepsListLabel = new Label(perfComposite, SWT.NONE);
    stepsListLabel.setText(BaseMessages.getString(PKG, "StepPerformanceSnapShotDialog.Steps.Label"));
    
    spoon.props.setLook(stepsListLabel);

    FormData fdStepsListLabel = new FormData();
    fdStepsListLabel.left   = new FormAttachment(0, 0);
    fdStepsListLabel.right  = new FormAttachment(spoon.props.getMiddlePct()/2, Const.MARGIN);
    fdStepsListLabel.top    = new FormAttachment(dataList, Const.MARGIN);
    stepsListLabel.setLayoutData(fdStepsListLabel);
    
		stepsList = new org.eclipse.swt.widgets.List(perfComposite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.LEFT | SWT.BORDER);
		spoon.props.setLook(stepsList);
		stepsList.setItems(steps);
		stepsList.addSelectionListener(new SelectionAdapter() {
		
			public void widgetSelected(SelectionEvent event) {
				
				// If there are multiple selections here AND there are multiple selections in the data list, we only take the first data item in the selection...
				//
				if (dataList.getSelectionCount()>1 && stepsList.getSelectionCount()>1) {
					dataList.setSelection(dataList.getSelectionIndices()[0]);
				}

				updateGraph();
			}
		});
		FormData fdStepsList = new FormData();
		fdStepsList.left   = new FormAttachment(0, 0);
		fdStepsList.right  = new FormAttachment(spoon.props.getMiddlePct()/2, Const.MARGIN);
		fdStepsList.top    = new FormAttachment(stepsListLabel, Const.MARGIN);
		fdStepsList.bottom = new FormAttachment(100, Const.MARGIN);
		stepsList.setLayoutData(fdStepsList);
		
		
		canvas = new Canvas(perfComposite, SWT.NONE );
		spoon.props.setLook(canvas);
		FormData fdCanvas = new FormData();
		fdCanvas.left   = new FormAttachment(spoon.props.getMiddlePct()/2,Const.MARGIN);
		fdCanvas.right  = new FormAttachment(100, 0);
		fdCanvas.top    = new FormAttachment(0,Const.MARGIN);
		fdCanvas.bottom = new FormAttachment(100, 0);
		canvas.setLayoutData(fdCanvas);
		
		perfComposite.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent event) {
				updateGraph();
			}
		});
		
		perfComposite.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				if (image!=null) image.dispose();
			}
		});

		canvas.addPaintListener(new PaintListener() {
		
			public void paintControl(PaintEvent event) {
				if (image!=null && !image.isDisposed()) event.gc.drawImage(image, 0, 0);
			}
		});
		
		// Refresh automatically every 5 seconds as well.
		//
		final Timer timer = new Timer("TransPerfDelegate Timer");
		timer.schedule(new TimerTask() {
			public void run() {
				updateGraph();
			}
		}, 0, 5000);

		// When the tab is closed, we remove the update timer
		//
		transPerfTab.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent arg0) {
				timer.cancel();
			}
		});
	}

    /**
     * Tell the user that the transformation is not running or that there is no monitoring configured.
     */
	private void showEmptyGraph() {
		if (perfComposite.isDisposed()) return;
		
		emptyGraph = true;

		Label label = new Label(perfComposite, SWT.CENTER);
		label.setText(BaseMessages.getString(PKG, "TransLog.Dialog.PerformanceMonitoringNotEnabled.Message"));
		label.setBackground(perfComposite.getBackground());
		label.setFont(GUIResource.getInstance().getFontMedium());
		
		FormData fdLabel = new FormData();
		fdLabel.left=new FormAttachment(5,0);
		fdLabel.right=new FormAttachment(95,0);
		fdLabel.top=new FormAttachment(5,0);
		label.setLayoutData(fdLabel);
		
    Button button = new Button(perfComposite, SWT.CENTER);
    button.setText(BaseMessages.getString(PKG, "TransLog.Dialog.PerformanceMonitoring.Button"));
    button.setBackground(perfComposite.getBackground());
    button.setFont(GUIResource.getInstance().getFontMedium());
    
    button.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent event){
        TransGraph.editProperties(spoon.getActiveTransformation(), spoon, spoon.rep, true, TransDialog.Tabs.MONITOR_TAB);
      }
    });

    FormData fdButton = new FormData();
    fdButton.left=new FormAttachment(40,0);
    fdButton.right=new FormAttachment(60,0);
    fdButton.top=new FormAttachment(label, 5);
    button.setLayoutData(fdButton);

    perfComposite.layout(true, true);
	}

	public void showPerfView() {
    	
    	// What button?
    	//
    	// XulToolbarButton showLogXulButton = toolbar.getButtonById("trans-show-log");
    	// ToolItem toolBarButton = (ToolItem) showLogXulButton.getNativeObject();
    	
    	if (transPerfTab==null || transPerfTab.isDisposed()) {
    		addTransPerf();
    	} else {
    		transPerfTab.dispose();
    		
    		transGraph.checkEmptyExtraView();
    	}
    }
    
    
	private void updateGraph() {
		
		transGraph.getDisplay().asyncExec(new Runnable() {
		
			public void run() {
				if (perfComposite!=null && !perfComposite.isDisposed() && canvas!=null && !canvas.isDisposed() && transPerfTab!=null && !transPerfTab.isDisposed()) {
					if (transPerfTab.isShowing()) {
						updateCanvas();
					}
				}
			}
		
		});
	}
	
	private void updateCanvas() {
		Rectangle bounds = canvas.getBounds();
		if (bounds.width<=0 || bounds.height<=0) return;
		
		// The list of snapshots : convert to JFreeChart dataset
		//
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		
		String[] selectedSteps = stepsList.getSelection();
		if (selectedSteps==null || selectedSteps.length==0) {
			selectedSteps = new String[] { steps[0], }; // first step
			stepsList.select(0);
		}
		int[] dataIndices = dataList.getSelectionIndices();
		if (dataIndices==null || dataIndices.length==0) {
			dataIndices = new int[] { DATA_CHOICE_WRITTEN, };
			dataList.select(0);
		}
		
		boolean multiStep = stepsList.getSelectionCount()>1;
		boolean multiData = dataList.getSelectionCount()>1;
		boolean calcMoving = !multiStep && !multiData; // A single metric shown for a single step
		List<Double> movingList = new ArrayList<Double>(); 
		int movingSize = 10;
		double movingTotal = 0;
		int totalTimeInSeconds=0; 
		
		for (int t = 0; t < selectedSteps.length; t++) {

			String stepNameCopy = selectedSteps[t];

			List<StepPerformanceSnapShot> snapShotList = stepPerformanceSnapShots.get(stepNameCopy);
			if (snapShotList != null && snapShotList.size() > 1) {
				totalTimeInSeconds = (int)Math.round( ((double)(snapShotList.get(snapShotList.size()-1).getDate().getTime() - snapShotList.get(0).getDate().getTime() )) / 1000 );
				for (int i = 0; i < snapShotList.size(); i++) {
					StepPerformanceSnapShot snapShot = snapShotList.get(i);
					if (snapShot.getTimeDifference()!=0) {

						double factor = (double)1000 / (double)snapShot.getTimeDifference();

						for (int d=0;d<dataIndices.length;d++) {
							
							String dataType; 
							if (multiStep) {
								dataType = stepNameCopy;
							}
							else {
								dataType = dataChoices[dataIndices[d]];
							}
							String xLabel = Integer.toString((int)Math.round(i*timeDifference/1000));
							Double metric = null; 
							switch(dataIndices[d]) {
							case DATA_CHOICE_INPUT: 
								metric = snapShot.getLinesInput() * factor;
								break;
							case DATA_CHOICE_OUTPUT: 
								metric = snapShot.getLinesOutput() * factor;
								break;
							case DATA_CHOICE_READ: 
								metric = snapShot.getLinesRead() * factor;
								break;
							case DATA_CHOICE_WRITTEN: 
								metric = snapShot.getLinesWritten() * factor;
								break;
							case DATA_CHOICE_UPDATED: 
								metric = snapShot.getLinesUpdated() * factor;
								break;
							case DATA_CHOICE_REJECTED: 
								metric = snapShot.getLinesRejected() * factor;
								break;
							case DATA_CHOICE_INPUT_BUFFER_SIZE: 
								metric = (double)snapShot.getInputBufferSize();
								break;
							case DATA_CHOICE_OUTPUT_BUFFER_SIZE: 
								metric = (double)snapShot.getOutputBufferSize();
								break;
							}
							if (metric!=null) {
								dataset.addValue(metric, dataType, xLabel);
								
								if (calcMoving) {
									movingTotal += metric;
									movingList.add(metric);
									if (movingList.size()>movingSize) {
										movingTotal-=movingList.get(0);
										movingList.remove(0);
									}
									double movingAverage = movingTotal / movingList.size(); 
									dataset.addValue(movingAverage, dataType+"(Avg)", xLabel);
									// System.out.println("moving average = "+movingAverage+", movingTotal="+movingTotal+", m");
								}
							}
						}
					}
				}
			}
		}
		String chartTitle = title;
		if (multiStep) {
			chartTitle+=" ("+dataChoices[dataIndices[0]]+")";
		}
		else {
			chartTitle+=" ("+selectedSteps[0]+")";
		}
		final JFreeChart chart = ChartFactory.createLineChart(chartTitle, // chart title
				BaseMessages.getString(PKG, "StepPerformanceSnapShotDialog.TimeInSeconds.Label", Integer.toString(totalTimeInSeconds), Long.toString(timeDifference)), // domain axis label
				BaseMessages.getString(PKG, "StepPerformanceSnapShotDialog.RowsPerSecond.Label"), // range axis label
				dataset, // data
				PlotOrientation.VERTICAL, // orientation
				true, // include legend
				true, // tooltips
				false // urls
				);
		chart.setBackgroundPaint(Color.white);
		TextTitle title = new TextTitle(chartTitle);
		//title.setExpandToFitSpace(true);
		//org.eclipse.swt.graphics.Color pentahoColor = GUIResource.getInstance().getColorPentaho();
		//java.awt.Color color = new java.awt.Color(pentahoColor.getRed(), pentahoColor.getGreen(),pentahoColor.getBlue());
		//title.setBackgroundPaint(color);
		title.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
		chart.setTitle(title);
		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setForegroundAlpha(0.5f);
		plot.setRangeGridlinesVisible(true);

		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		
		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setTickLabelsVisible(false);
		
		// Customize the renderer...
		//
		LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
		renderer.setBaseShapesVisible(true);
		renderer.setDrawOutlines(true);
		renderer.setUseFillPaint(true);
		renderer.setBaseFillPaint(Color.white);
		renderer.setSeriesStroke(0, new BasicStroke(1.5f));
		renderer.setSeriesOutlineStroke(0, new BasicStroke(1.5f));
		renderer.setSeriesStroke(1, new BasicStroke(2.5f));
		renderer.setSeriesOutlineStroke(1, new BasicStroke(2.5f));
		renderer.setSeriesShape(0, new Ellipse2D.Double(-3.0, -3.0, 6.0, 6.0));
		
		BufferedImage bufferedImage = chart.createBufferedImage(bounds.width, bounds.height);
		ImageData imageData = ImageUtil.convertToSWT(bufferedImage);
		
		// dispose previous image...
		//
		if (image!=null) image.dispose();
		image = new Image(transGraph.getDisplay(), imageData);

		// Draw the image on the canvas...
		//
		canvas.redraw();
	}

	/**
	 * @return the transHistoryTab
	 */
	public CTabItem getTransPerfTab() {
		return transPerfTab;
	}

	/**
	 * @return the emptyGraph
	 */
	public boolean isEmptyGraph() {
		return emptyGraph;
	}
	
	public void layoutPerfComposite() {
		if (!perfComposite.isDisposed()) perfComposite.layout(true,true);
	}

}
