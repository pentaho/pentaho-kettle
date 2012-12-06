/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

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
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.performance.StepPerformanceSnapShot;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.util.ImageUtil;

public class StepPerformanceSnapShotDialog extends Dialog {

	private static Class<?> PKG = Spoon.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

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
	
	private Shell parent, shell;
	private Map<String, List<StepPerformanceSnapShot>> stepPerformanceSnapShots;
	private Display display;
	private String[] steps;
	private PropsUI props;
	private org.eclipse.swt.widgets.List stepsList;
	private Canvas canvas;
	private Image image;
	private long timeDifference;
	private String title;
	private org.eclipse.swt.widgets.List dataList;

	public StepPerformanceSnapShotDialog(Shell parent, String title, Map<String, List<StepPerformanceSnapShot>> stepPerformanceSnapShots, long timeDifference) {
		super(parent);
		this.parent  = parent;
		this.display = parent.getDisplay();
		this.props   = PropsUI.getInstance();
		this.timeDifference = timeDifference;
		this.title = title;
		this.stepPerformanceSnapShots = stepPerformanceSnapShots;

		Set<String> stepsSet = stepPerformanceSnapShots.keySet();
		steps = stepsSet.toArray(new String[stepsSet.size()]);
		Arrays.sort(steps);
	}
	
	public void open() {
		
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);
		shell.setText(BaseMessages.getString(PKG, "StepPerformanceSnapShotDialog.Title"));
		shell.setImage(GUIResource.getInstance().getImageLogoSmall());
		
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;
		
		shell.setLayout (formLayout);

		// Display 2 lists with the data types and the steps on the left side.
		// Then put a canvas with the graph on the right side
		// 
		dataList = new org.eclipse.swt.widgets.List(shell, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.LEFT | SWT.BORDER);
		props.setLook(dataList);
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
		fdDataList.right  = new FormAttachment(props.getMiddlePct()/2, Const.MARGIN);
		fdDataList.top    = new FormAttachment(0, 0);
		fdDataList.bottom = new FormAttachment(30, 0);
		dataList.setLayoutData(fdDataList);

		stepsList = new org.eclipse.swt.widgets.List(shell, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.LEFT | SWT.BORDER);
		props.setLook(stepsList);
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
		fdStepsList.right  = new FormAttachment(props.getMiddlePct()/2, Const.MARGIN);
		fdStepsList.top    = new FormAttachment(dataList, Const.MARGIN);
		fdStepsList.bottom = new FormAttachment(100, Const.MARGIN);
		stepsList.setLayoutData(fdStepsList);
		
		
		canvas = new Canvas(shell, SWT.NONE );
		props.setLook(canvas);
		FormData fdCanvas = new FormData();
		fdCanvas.left   = new FormAttachment(props.getMiddlePct()/2, 0);
		fdCanvas.right  = new FormAttachment(100, 0);
		fdCanvas.top    = new FormAttachment(0, 0);
		fdCanvas.bottom = new FormAttachment(100, 0);
		canvas.setLayoutData(fdCanvas);
		
		shell.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent event) {
				updateGraph();
			}
		});
		
		shell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				if (image!=null) image.dispose();
			}
		});

		canvas.addPaintListener(new PaintListener() {
		
			public void paintControl(PaintEvent event) {
				if (image!=null) event.gc.drawImage(image, 0, 0);
			}
		});
		
		// Refresh automatically every 5 seconds as well.
		//
		Timer timer = new Timer("step performance snapshot dialog Timer");
		timer.schedule(new TimerTask() {
			public void run() {
				updateGraph();
			}
		}, 0, 5000);
		
		shell.open();
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}
	
	private void updateGraph() {
		
		display.asyncExec(new Runnable() {
		
			public void run() {
				if (!shell.isDisposed() && !canvas.isDisposed()) {
					updateCanvas();
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
		image = new Image(display, imageData);

		// Draw the image on the canvas...
		//
		canvas.redraw();
	}

	/**
	 * @return the shell
	 */
	public Shell getShell() {
		return parent;
	}

	/**
	 * @param shell the shell to set
	 */
	public void setShell(Shell shell) {
		this.parent = shell;
	}

	/**
	 * @return the stepPerformanceSnapShots
	 */
	public Map<String, List<StepPerformanceSnapShot>> getStepPerformanceSnapShots() {
		return stepPerformanceSnapShots;
	}

	/**
	 * @param stepPerformanceSnapShots the stepPerformanceSnapShots to set
	 */
	public void setStepPerformanceSnapShots(Map<String, List<StepPerformanceSnapShot>> stepPerformanceSnapShots) {
		this.stepPerformanceSnapShots = stepPerformanceSnapShots;
	}

}
