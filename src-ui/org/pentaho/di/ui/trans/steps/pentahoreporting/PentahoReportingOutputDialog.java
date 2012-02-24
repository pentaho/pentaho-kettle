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

package org.pentaho.di.ui.trans.steps.pentahoreporting;

import java.math.BigDecimal;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.pentahoreporting.PentahoReportingOutput;
import org.pentaho.di.trans.steps.pentahoreporting.PentahoReportingOutputMeta;
import org.pentaho.di.trans.steps.pentahoreporting.PentahoReportingOutputMeta.ProcessorType;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.parameters.ParameterDefinitionEntry;
import org.pentaho.reporting.engine.classic.core.parameters.ReportParameterDefinition;



public class PentahoReportingOutputDialog extends BaseStepDialog implements StepDialogInterface
{
    private static Class<?> PKG = PentahoReportingOutput.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private Label        wlInput;
	private Combo        wInput;

    private Label        wlOutput;
    private Combo        wOutput;

    private Label        wlProcessor;
    private List         wProcessor;

	private PentahoReportingOutputMeta input;

  private TableView wFields;

	public PentahoReportingOutputDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(PentahoReportingOutputMeta)in;
		this.transMeta=transMeta;
		if (sname != null) stepname=sname;
		else stepname=BaseMessages.getString(PKG, "PentahoReportingOutputDialog.DefaultStepName"); //$NON-NLS-1$
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);
        setShellImage(shell, input);

		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
			}
		};
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "PentahoReportingOutputDialog.Shell.Text")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "PentahoReportingOutputDialog.Stepname.Label")); //$NON-NLS-1$
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left  = new FormAttachment(0, 0);
		fdlStepname.top   = new FormAttachment(0, margin);
		fdlStepname.right = new FormAttachment(middle, -margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
 		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		// input file line (report definition)
		wlInput=new Label(shell, SWT.RIGHT);
		wlInput.setText(BaseMessages.getString(PKG, "PentahoReportingOutputDialog.InputFilename.Label")); //$NON-NLS-1$
 		props.setLook(wlInput);
		FormData fdlInput = new FormData();
		fdlInput.left = new FormAttachment(0, 0);
		fdlInput.top  = new FormAttachment(wStepname, margin+5);
		fdlInput.right= new FormAttachment(middle, -margin);
		wlInput.setLayoutData(fdlInput);
		wInput=new Combo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wInput);
		wInput.addModifyListener(lsMod);
		FormData fdInput = new FormData();
		fdInput.left = new FormAttachment(middle, 0);
		fdInput.top  = new FormAttachment(wStepname, margin+5);
		fdInput.right= new FormAttachment(100, 0);
		wInput.setLayoutData(fdInput);
		
		String[] fieldNames = new String[] {};
		try {
		  fieldNames = Const.sortStrings(transMeta.getPrevStepFields(stepMeta).getFieldNames());
		} catch(KettleException e) {
		  log.logError("Unexpected error getting fields from previous steps...", e);
		}
		
		wInput.setItems(fieldNames);

        // input file line (report definition)
        wlOutput=new Label(shell, SWT.RIGHT);
        wlOutput.setText(BaseMessages.getString(PKG, "PentahoReportingOutputDialog.OutputFilename.Label")); //$NON-NLS-1$
        props.setLook(wlOutput);
        FormData fdlOutput = new FormData();
        fdlOutput.left = new FormAttachment(0, 0);
        fdlOutput.top  = new FormAttachment(wInput, margin+5);
        fdlOutput.right= new FormAttachment(middle, -margin);
        wlOutput.setLayoutData(fdlOutput);
        wOutput=new Combo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wOutput);
        wOutput.addModifyListener(lsMod);
        FormData fdOutput = new FormData();
        fdOutput.left = new FormAttachment(middle, 0);
        fdOutput.top  = new FormAttachment(wInput, margin+5);
        fdOutput.right= new FormAttachment(100, 0);
        wOutput.setLayoutData(fdOutput);
        wOutput.setItems(fieldNames);
        
        // Fields
        ColumnInfo[] colinf=new ColumnInfo[]
            {
             new ColumnInfo(BaseMessages.getString(PKG, "PentahoReportingOutput.Column.ParameterName"),   ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo(BaseMessages.getString(PKG, "PentahoReportingOutput.Column.FieldName"),       ColumnInfo.COLUMN_TYPE_CCOMBO,  fieldNames, true ),
            };
        
        wFields=new TableView(transMeta, shell, 
                              SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER, 
                              colinf, 
                              1,  
                              lsMod,
                              props
                              );

        FormData fdFields = new FormData();
        fdFields.top   = new FormAttachment(wOutput, margin*2);
        fdFields.bottom= new FormAttachment(wOutput, 250);
        fdFields.left  = new FormAttachment(0, 0);
        fdFields.right = new FormAttachment(100, 0);
        wFields.setLayoutData(fdFields);
        
        // input file line (report definition)
        //
        wlProcessor=new Label(shell, SWT.RIGHT);
        wlProcessor.setText(BaseMessages.getString(PKG, "PentahoReportingOutputDialog.Processor.Label")); //$NON-NLS-1$
        props.setLook(wlProcessor);
        FormData fdlProcessor = new FormData();
        fdlProcessor.left = new FormAttachment(0, 0);
        fdlProcessor.top  = new FormAttachment(wFields, margin+5);
        fdlProcessor.right= new FormAttachment(middle, -margin);
        wlProcessor.setLayoutData(fdlProcessor);
        wProcessor=new List(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wProcessor);
        FormData fdProcessor = new FormData();
        fdProcessor.left = new FormAttachment(middle, 0);
        fdProcessor.top  = new FormAttachment(wFields, margin+5);
        fdProcessor.right= new FormAttachment(100, 0);
        wProcessor.setLayoutData(fdProcessor);
        wProcessor.setItems(ProcessorType.getDescriptions());
        wProcessor.addListener(SWT.Selection, new Listener(){public void handleEvent(Event e){ input.setChanged();}});
        
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString("System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString("System.Button.Cancel")); //$NON-NLS-1$
        wGet=new Button(shell, SWT.PUSH);
        wGet.setText(BaseMessages.getString(PKG, "PentahoReportingOutputDialog.Button.GetParameters")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wGet, wCancel }, margin, wProcessor);

		// Add listeners
		wOK.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { ok();     } });
        wCancel.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { cancel(); } });
		wGet.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { get(); } });
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wInput.addSelectionListener( lsDef );
        wOutput.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );


		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		input.setChanged(changed);
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData() {
		wInput.setText(Const.NVL(input.getInputFileField(), ""));
        wOutput.setText(Const.NVL(input.getOutputFileField(), ""));
        
        for (String name : input.getParameterFieldMap().keySet()) {
          String field = input.getParameterFieldMap().get(name);
          TableItem item = new TableItem(wFields.table, SWT.NONE);
          item.setText(1, name);
          item.setText(2, field);
        }
        wFields.removeEmptyRows();
        wFields.setRowNums();
        wFields.optWidth(true);
        
		wProcessor.select(input.getOutputProcessorType().ordinal());
		wStepname.selectAll();
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);

		dispose();
	}
	
	private void ok()
	{
		stepname = wStepname.getText(); // return value

		input.getParameterFieldMap().clear();
		for (int i=0;i<wFields.nrNonEmpty();i++) {
		  TableItem item = wFields.getNonEmpty(i);
		  String name = item.getText(1);
		  String field = item.getText(2);
		  if (!Const.isEmpty(name) && !Const.isEmpty(field)) {
		    input.getParameterFieldMap().put(name, field);
		  }
		}
		
		input.setInputFileField( wInput.getText() );
        input.setOutputFileField( wOutput.getText() );
        input.setOutputProcessorType( ProcessorType.values()[wProcessor.getSelectionIndex()] );
        
		dispose();
	}
	
	private static String lastFilename;

	private void get() {
	  
	  Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);

      // See if we need to boot the reporting engine. Since this takes time we do it in the background...
      //
	  Runnable runnable = new Runnable() {
	    public void run() {
	        PentahoReportingOutput.performPentahoReportingBoot(log, input.getClass());
	    }
	  };
	  Thread thread = new Thread(runnable);
	  thread.start();
	  
	  try {
	    // Browse for a PRPT...
	    //
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setText(BaseMessages.getString(PKG, "PentahoReportingOutputDialog.ExtractParameters.FileDialog"));
        dialog.setFilterExtensions(new String[] {"*.prpt;*.PRPT", "*"});
        if (lastFilename!=null)
        {
            dialog.setFileName( lastFilename );
        }
        
        dialog.setFilterNames(new String[] {
            BaseMessages.getString(PKG, "PentahoReportingOutputDialog.PentahoReportingFiles"), 
            BaseMessages.getString(PKG, "System.FileType.AllFiles"),
           }
        );
        
        if (dialog.open()==null) return;

        thread.join();        

        String sourceFilename = dialog.getFilterPath()+System.getProperty("file.separator")+dialog.getFileName();
        lastFilename=sourceFilename;
        
        shell.setCursor(busy);
        
        
        // Load the master report...
        //
        MasterReport report = PentahoReportingOutput.loadMasterReport(sourceFilename);
     
        // Extract the definitions...
        //
        ReportParameterDefinition definition = report.getParameterDefinition();
        RowMetaInterface r = new RowMeta();
        for (int i=0;i<definition.getParameterCount();i++) {
          ParameterDefinitionEntry entry = definition.getParameterDefinition(i);
          ValueMeta valueMeta = new ValueMeta(entry.getName(), ValueMetaInterface.TYPE_STRING);
          valueMeta.setComments( getParameterDefinitionEntryTypeDescription(entry) );
          r.addValueMeta(valueMeta);
        }
        
        shell.setCursor(null);

        BaseStepDialog.getFieldsFromPrevious(r, wFields, 1, new int[] { 1 }, new int[] {}, -1, -1, new TableItemInsertListener() {
          
          public boolean tableItemInserted(TableItem item, ValueMetaInterface valueMeta) {
            item.setText(2, valueMeta.getComments());
            return true;
          }
        });
        
	  } catch(Exception e) {
        shell.setCursor(null);
	    new ErrorDialog(shell, 
	      BaseMessages.getString(PKG, "PentahoReportingOutputDialog.ErrorReadingParameters.Title"),
	      BaseMessages.getString(PKG, "PentahoReportingOutputDialog.ErrorReadingParameters.Message"),
	      e
	    );
	  } finally {
	    shell.setCursor(null);
        busy.dispose();
	  }
	  
	}

  private String getParameterDefinitionEntryTypeDescription(ParameterDefinitionEntry entry) {
    
    Class<?> clazz = entry.getValueType();
    String extra=" (";
    
    String namespace = entry.getParameterAttributeNamespaces()[0];
    String[] attributes = entry.getParameterAttributeNames(namespace);
    for (int i=0;i<attributes.length;i++) {
      if (i>0) extra+=", ";
      String attr = entry.getParameterAttribute(namespace, attributes[i], null);
      extra+=attributes[i]+"="+attr;
    }
    extra+=")";
    String type;
    
    if (clazz.equals(String.class)) {
      type = BaseMessages.getString(PKG, "PentahoReportingOutputDialog.ParameterType.String");
    } else if (clazz.equals((new String[0]).getClass())) { 
      type = BaseMessages.getString(PKG, "PentahoReportingOutputDialog.ParameterType.StringArray");
    } else if (clazz.equals(Date.class)) {
      type = BaseMessages.getString(PKG, "PentahoReportingOutputDialog.ParameterType.Date");
    } else if (clazz.equals(byte.class) || clazz.equals(Byte.class)) {
      type = BaseMessages.getString(PKG, "PentahoReportingOutputDialog.ParameterType.Integer");
    } else if (clazz.equals(Short.class) || clazz.equals(short.class)) {
      type = BaseMessages.getString(PKG, "PentahoReportingOutputDialog.ParameterType.Integer");
    } else if (clazz.equals(Integer.class) || clazz.equals(int.class)) {
      type = BaseMessages.getString(PKG, "PentahoReportingOutputDialog.ParameterType.Integer");
    } else if (clazz.equals(Long.class) || clazz.equals(long.class)) {
      type = BaseMessages.getString(PKG, "PentahoReportingOutputDialog.ParameterType.Integer");
    } else if (clazz.equals(Double.class) || clazz.equals(double.class)) {
      type = BaseMessages.getString(PKG, "PentahoReportingOutputDialog.ParameterType.Number");
    } else if (clazz.equals(Float.class) || clazz.equals(float.class)) {
      type = BaseMessages.getString(PKG, "PentahoReportingOutputDialog.ParameterType.Number");
    } else if (clazz.equals(Number.class)) {
      type = BaseMessages.getString(PKG, "PentahoReportingOutputDialog.ParameterType.Numeric");
    } else if (clazz.equals(Boolean.class) || clazz.equals(boolean.class)) {
      type = BaseMessages.getString(PKG, "PentahoReportingOutputDialog.ParameterType.Boolean");
    } else if (clazz.equals(BigDecimal.class)) {
      type = BaseMessages.getString(PKG, "PentahoReportingOutputDialog.ParameterType.BigNumber");
    } else if (clazz.equals((new byte[0]).getClass())) {
      type = BaseMessages.getString(PKG, "PentahoReportingOutputDialog.ParameterType.Binary");
    } else {
      // We don't know
      type = BaseMessages.getString(PKG, "PentahoReportingOutputDialog.ParameterType.Unknown", entry.getValueType().getSimpleName());
    }
    
    if (attributes.length==0) {
      return type;
    } else {
      return type+extra;
    }
  }
}
