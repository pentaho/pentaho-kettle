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

package org.pentaho.di.ui.trans.debug;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.debug.StepDebugMeta;
import org.pentaho.di.trans.debug.TransDebugMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ConditionEditor;
import org.pentaho.di.ui.core.widget.LabelText;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * Allows you to edit/enter the transformation debugging information 
 * 
 * @author matt
 * @since  2007-09-14
 * @since  version 3.0 RC1
 */
public class TransDebugDialog extends Dialog {
    private static Class<?> PKG = TransDebugDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static final int DEBUG_CANCEL = 0;
	public static final int DEBUG_LAUNCH = 1;
	public static final int DEBUG_CONFIG = 2;
	
	private Display display;
    private Shell parent;
    private Shell shell;
    private PropsUI props;
    private int retval;

    private Button wOK, wCancel, wLaunch;
    
    private TableView wSteps;
    
    private TransDebugMeta transDebugMeta;
	private Composite wComposite;
	private LabelText wRowCount;
	private int margin;
	private int middle;
	private Button wFirstRows;
	private Button wPauseBreakPoint;
	private Condition condition;
	private RowMetaInterface stepInputFields;
	private ConditionEditor wCondition;
	private Label wlCondition;
	private Map<StepMeta, StepDebugMeta> stepDebugMetaMap;
	private int previousIndex; 
    
    public TransDebugDialog(Shell parent, TransDebugMeta transDebugMeta) {
    	super(parent);
    	this.parent = parent;
    	this.transDebugMeta = transDebugMeta;
    	props = PropsUI.getInstance();
    	
    	// Keep our own map of step debugging information...
    	//
    	stepDebugMetaMap = new Hashtable<StepMeta, StepDebugMeta>();
    	stepDebugMetaMap.putAll(transDebugMeta.getStepDebugMetaMap());
    	
    	previousIndex=-1;
    	
    	retval = DEBUG_CANCEL;
    }
 
    public int open() {
    	
        display = parent.getDisplay();
        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.SHEET | SWT.RESIZE | SWT.MAX | SWT.MIN);
        props.setLook(shell);
		shell.setImage(GUIResource.getInstance().getImageTransGraph());
        
        FormLayout formLayout = new FormLayout ();
        formLayout.marginWidth  = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(BaseMessages.getString(PKG, "TransDebugDialog.Shell.Title")); //$NON-NLS-1$

        margin = Const.MARGIN;
        middle = props.getMiddlePct();
        
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "TransDebugDialog.Configure.Label"));
        wOK.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { ok(true); }});
        wLaunch= new Button(shell, SWT.PUSH);
        wLaunch.setText(BaseMessages.getString(PKG, "TransDebugDialog.Launch.Label"));
        wLaunch.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { ok(false); }});
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
        wCancel.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { cancel(); }});
        
        BaseStepDialog.positionBottomButtons(shell, new Button[] { wLaunch, wOK, wCancel }, margin, null);
    	
        wOK.setToolTipText(BaseMessages.getString(PKG, "TransDebugDialog.Configure.ToolTip"));
        wLaunch.setToolTipText(BaseMessages.getString(PKG, "TransDebugDialog.Launch.ToolTip"));
        
        // Add the list of steps
        //
        ColumnInfo[] stepColumns = {
				new ColumnInfo(BaseMessages.getString(PKG, "TransDebugDialog.Column.StepName"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), // name, non-numeric, readonly
			};

		int nrSteps = transDebugMeta.getTransMeta().nrSteps();
		wSteps = new TableView(transDebugMeta.getTransMeta(), shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE, stepColumns, nrSteps, true, null, props);
		FormData fdSteps = new FormData();
		fdSteps.left = new FormAttachment(0, 0);
		fdSteps.right = new FormAttachment(middle, -margin);
		fdSteps.top = new FormAttachment(0, margin);
		fdSteps.bottom = new FormAttachment(wOK, -margin * 2);
		wSteps.setLayoutData(fdSteps);
        wSteps.table.setHeaderVisible(false);
        
        // If someone clicks on a row, we want to refresh the right pane...
        //
        wSteps.table.addSelectionListener(new SelectionAdapter() {
		
			public void widgetSelected(SelectionEvent e) {
				// Before we show anything, make sure to save the content of the screen...
				//
				getStepDebugMeta();
				
				// Now show the information...
				//
				showStepDebugInformation();
			}
		
		});
        
        // Now add the composite on which we will dynamically place a number of widgets, based on the selected step...
        //
        wComposite = new Composite(shell, SWT.BORDER);
        props.setLook(wComposite);
        
        FormData fdComposite = new FormData();
        fdComposite.left   = new FormAttachment(middle, 0);
        fdComposite.right  = new FormAttachment(100, 0);
        fdComposite.top    = new FormAttachment(0, margin);
        fdComposite.bottom = new FormAttachment(wOK, -margin*2);
        wComposite.setLayoutData(fdComposite);
        
        // Give the composite a layout...
        FormLayout compositeLayout = new FormLayout ();
        compositeLayout.marginWidth  = Const.FORM_MARGIN;
        compositeLayout.marginHeight = Const.FORM_MARGIN;
        wComposite.setLayout(compositeLayout);
        
        getData();
        
        BaseStepDialog.setSize(shell);
        
        // Set the focus on the OK button
        //
        wLaunch.setFocus();
        
        shell.open();
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch()) display.sleep();
        }
    	return retval;
    }
    
    private void getData() {
    	// Save the latest changes to the screen...
    	//
    	getStepDebugMeta();
    	
    	// Add the steps...
    	//
    	refreshStepList();
    	
	}
    
    private void refreshStepList() {
    	GUIResource resource = GUIResource.getInstance();
    	
    	// Add the list of steps...
    	//
    	int maxIconSize=0;
    	int indexSelected = -1;
    	wSteps.table.removeAll();
    	for (int i=0;i<transDebugMeta.getTransMeta().getSteps().size();i++) {
    		StepMeta stepMeta = transDebugMeta.getTransMeta().getStep(i);
    		TableItem item = new TableItem(wSteps.table, SWT.NONE);
    		Image image = resource.getImagesSteps().get(stepMeta.getStepID());
    		item.setImage(0, image);
    		item.setText(0, "");
    		item.setText(1, stepMeta.getName());
    		
    		if (image.getBounds().width>maxIconSize) maxIconSize=image.getBounds().width;
    		
    		StepDebugMeta stepDebugMeta = stepDebugMetaMap.get(stepMeta);
    		if (stepDebugMeta!=null) {
    			// We have debugging information so we mark the row
    			//
    			item.setBackground(resource.getColorLightPentaho());
    			if (indexSelected<0) indexSelected=i;
    		}
    	}
    	
    	wSteps.removeEmptyRows();
    	wSteps.optWidth(false);
    	wSteps.table.getColumn(0).setWidth(maxIconSize+10);
    	wSteps.table.getColumn(0).setAlignment(SWT.CENTER);
    	
    	
    	// OK, select the first used step debug line...
    	//
    	if (indexSelected>=0) {
    		wSteps.table.setSelection(indexSelected);
    		showStepDebugInformation();
    	}
	}

	/**
     * Grab the step debugging information from the dialog.
     * Store it in our private map
     */
    private void getStepDebugMeta() {
    	int index = wSteps.getSelectionIndex();
    	if (previousIndex>=0)
    	{
	    	// Is there anything on the composite to save yet?
	    	//
	    	if (wComposite.getChildren().length==0) return;
	    	
	    	StepMeta stepMeta = transDebugMeta.getTransMeta().getStep(previousIndex);
	    	StepDebugMeta stepDebugMeta = new StepDebugMeta(stepMeta);
	    	stepDebugMeta.setCondition(condition);
	    	stepDebugMeta.setPausingOnBreakPoint(wPauseBreakPoint.getSelection());
	    	stepDebugMeta.setReadingFirstRows(wFirstRows.getSelection());
	    	stepDebugMeta.setRowCount(Const.toInt(wRowCount.getText(), -1));
	    	
	    	stepDebugMetaMap.put(stepMeta, stepDebugMeta);
    	}	    	
	    previousIndex = index;
    }
    
    private void getInfo(TransDebugMeta meta) {
    	meta.getStepDebugMetaMap().clear();
    	meta.getStepDebugMetaMap().putAll(stepDebugMetaMap);
	}

	private void ok(boolean config) {
		if (config) {
	    	retval=DEBUG_CONFIG;
		}
		else {
	    	retval=DEBUG_LAUNCH;
		}
    	getStepDebugMeta();
    	getInfo(transDebugMeta);
    	dispose();
    }

	private void dispose() {
    	props.setScreen(new WindowProperty(shell));
    	shell.dispose();
    }
    
    private void cancel() {
    	retval=DEBUG_CANCEL;
    	dispose();
    }
    
    private void showStepDebugInformation() {
    	
    	// Now that we have all the information to display, let's put some widgets on our composite.
    	// Before we go there, let's clear everything that was on there...
    	//
    	for (Control control : wComposite.getChildren()) control.dispose();
    	wComposite.layout(true, true);
    	
    	int[] selectionIndices = wSteps.table.getSelectionIndices();
    	if (selectionIndices==null || selectionIndices.length!=1) return;
    	
    	previousIndex = selectionIndices[0];
    	
    	// What step did we click on?
    	//
    	final StepMeta stepMeta = transDebugMeta.getTransMeta().getStep(selectionIndices[0]);
    	
    	// What is the step debugging metadata?
    	// --> This can be null (most likely scenario)
    	//
    	final StepDebugMeta stepDebugMeta = stepDebugMetaMap.get(stepMeta);
    	
    	// At the top we'll put a few common items like first[x], etc.
    	//
    	
    	// The row count (e.g. number of rows to keep)
    	//
    	wRowCount = new LabelText(wComposite, BaseMessages.getString(PKG, "TransDebugDialog.RowCount.Label"), BaseMessages.getString(PKG, "TransDebugDialog.RowCount.ToolTip"));
    	FormData fdRowCount = new FormData();
    	fdRowCount.left   = new FormAttachment(0, 0);
        fdRowCount.right  = new FormAttachment(100, 0);
        fdRowCount.top    = new FormAttachment(0, 0);
        wRowCount.setLayoutData(fdRowCount);
        wRowCount.addSelectionListener(new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent arg0) {ok(false); }});

    	// Do we retrieve the first rows passing?
    	//
    	wFirstRows = new Button(wComposite, SWT.CHECK);
    	props.setLook(wFirstRows);
    	wFirstRows.setText(BaseMessages.getString(PKG, "TransDebugDialog.FirstRows.Label"));
    	wFirstRows.setToolTipText(BaseMessages.getString(PKG, "TransDebugDialog.FirstRows.ToolTip"));
    	FormData fdFirstRows = new FormData();
    	fdFirstRows.left   = new FormAttachment(middle, 0);
        fdFirstRows.right  = new FormAttachment(100, 0);
        fdFirstRows.top    = new FormAttachment(wRowCount, margin);
        wFirstRows.setLayoutData(fdFirstRows);

    	// Do we pause on break point, when the condition is met?
    	//
    	wPauseBreakPoint = new Button(wComposite, SWT.CHECK);
    	props.setLook(wPauseBreakPoint);
    	wPauseBreakPoint.setText(BaseMessages.getString(PKG, "TransDebugDialog.PauseBreakPoint.Label"));
    	wPauseBreakPoint.setToolTipText(BaseMessages.getString(PKG, "TransDebugDialog.PauseBreakPoint.ToolTip"));
    	FormData fdPauseBreakPoint = new FormData();
    	fdPauseBreakPoint.left   = new FormAttachment(middle, 0);
        fdPauseBreakPoint.right  = new FormAttachment(100, 0);
        fdPauseBreakPoint.top    = new FormAttachment(wFirstRows, margin);
        wPauseBreakPoint.setLayoutData(fdPauseBreakPoint);
        
        // The condition to pause for...
        //
        condition = null;
        if (stepDebugMeta!=null) condition = stepDebugMeta.getCondition();
        if (condition==null) condition = new Condition();
        
        // The input fields...
        try {
			stepInputFields = transDebugMeta.getTransMeta().getStepFields(stepMeta);
		} catch (KettleStepException e) {
			stepInputFields = new RowMeta();
		}
        
		wlCondition = new Label(wComposite, SWT.RIGHT);
		props.setLook(wlCondition);
		wlCondition.setText(BaseMessages.getString(PKG, "TransDebugDialog.Condition.Label"));
		wlCondition.setToolTipText(BaseMessages.getString(PKG, "TransDebugDialog.Condition.ToolTip"));
        FormData fdlCondition = new FormData();
    	fdlCondition.left   = new FormAttachment(0, 0);
        fdlCondition.right  = new FormAttachment(middle, -margin);
        fdlCondition.top    = new FormAttachment(wPauseBreakPoint, margin);
        wlCondition.setLayoutData(fdlCondition);
        
        wCondition = new ConditionEditor(wComposite, SWT.BORDER, condition, stepInputFields);
        FormData fdCondition = new FormData();
    	fdCondition.left   = new FormAttachment(middle, 0);
        fdCondition.right  = new FormAttachment(100, 0);
        fdCondition.top    = new FormAttachment(wPauseBreakPoint, margin);
        fdCondition.bottom = new FormAttachment(100, 0);
        wCondition.setLayoutData(fdCondition);

        getStepDebugData(stepDebugMeta);
        
        // Add a "clear" button at the bottom on the left...
        //
        Button wClear = new Button(wComposite, SWT.PUSH);
        props.setLook(wClear);
        wClear.setText(BaseMessages.getString(PKG, "TransDebugDialog.Clear.Label"));
        wClear.setToolTipText(BaseMessages.getString(PKG, "TransDebugDialog.Clear.ToolTip"));
        FormData fdClear = new FormData();
    	fdClear.left   = new FormAttachment(0, 0);
        fdClear.bottom = new FormAttachment(100, 0);
        wClear.setLayoutData(fdClear);
        
        wClear.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					// Clear the preview step information for this step...
					//
					stepDebugMetaMap.remove(stepMeta);
					wSteps.table.setSelection(new int[] {});
					previousIndex = -1;
					
					// refresh the steps list...
					//
					refreshStepList();
					
					showStepDebugInformation();
				}
			}
        );
        
        wComposite.layout(true, true);
    }

	private void getStepDebugData(StepDebugMeta stepDebugMeta) {
		if (stepDebugMeta==null) return;
		
		if (stepDebugMeta.getRowCount()>0) {
			wRowCount.setText(Integer.toString(stepDebugMeta.getRowCount()));  
		}
		else {
			wRowCount.setText("");
		}
		
		wFirstRows.setSelection(stepDebugMeta.isReadingFirstRows());
		wPauseBreakPoint.setSelection(stepDebugMeta.isPausingOnBreakPoint());
	}
}
