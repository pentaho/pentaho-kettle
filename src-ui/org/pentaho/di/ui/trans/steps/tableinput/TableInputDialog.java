 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

/*
 * Created on 18-mei-2003
 *
 */

package org.pentaho.di.ui.trans.steps.tableinput;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.di.ui.core.database.dialog.DatabaseExplorerDialog;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.widget.StyledTextComp;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.spoon.job.JobGraph;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class TableInputDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = TableInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private CCombo       wConnection;

	private Label        wlSQL;
	private StyledTextComp         wSQL;
	private FormData     fdlSQL, fdSQL;

	private Label        wlDatefrom;
	private CCombo       wDatefrom;
	private FormData     fdlDatefrom, fdDatefrom;
    private Listener     lsDateform;

	private Label        wlLimit;
	private TextVar      wLimit;
	private FormData     fdlLimit, fdLimit;
    
    private Label        wlEachRow;
    private Button       wEachRow;
    private FormData     fdlEachRow, fdEachRow; 

    private Label        wlVariables;
    private Button       wVariables;
    private FormData     fdlVariables, fdVariables; 

    private Label        wlLazyConversion;
    private Button       wLazyConversion;
    private FormData     fdlLazyConversion, fdLazyConversion; 

	private Button wbTable;
	private FormData fdbTable;
	private Listener lsbTable;

	private TableInputMeta input;
	private boolean changedInDialog;
	
	private Label        wlPosition;
	private FormData     fdlPosition;
	
	private SQLValuesHighlight lineStyler = new SQLValuesHighlight();

	public TableInputDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(TableInputMeta)in;
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
				changedInDialog = false; // for prompting if dialog is simply closed
				input.setChanged();
			}
		};
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "TableInputDialog.TableInput")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

        // Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "TableInputDialog.StepName")); //$NON-NLS-1$
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(middle, -margin);
		fdlStepname.top  = new FormAttachment(0, margin);
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

		// Connection line
		wConnection = addConnectionLine(shell, wStepname, middle, margin);
		if (input.getDatabaseMeta()==null && transMeta.nrDatabases()==1) wConnection.select(0);
		wConnection.addModifyListener(lsMod);

		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
        wPreview=new Button(shell, SWT.PUSH);
        wPreview.setText(BaseMessages.getString(PKG, "System.Button.Preview")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel , wPreview }, margin, null);

		// Limit input ...
		wlLimit=new Label(shell, SWT.RIGHT);
		wlLimit.setText(BaseMessages.getString(PKG, "TableInputDialog.LimitSize")); //$NON-NLS-1$
 		props.setLook(wlLimit);
		fdlLimit=new FormData();
		fdlLimit.left = new FormAttachment(0, 0);
		fdlLimit.right= new FormAttachment(middle, -margin);
		fdlLimit.bottom = new FormAttachment(wOK, -2*margin);
		wlLimit.setLayoutData(fdlLimit);
		wLimit=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLimit);
		wLimit.addModifyListener(lsMod);
		fdLimit=new FormData();
		fdLimit.left = new FormAttachment(middle, 0);
		fdLimit.right= new FormAttachment(100, 0);
		fdLimit.bottom = new FormAttachment(wOK, -2*margin);
		wLimit.setLayoutData(fdLimit);

        // Execute for each row?
        wlEachRow = new Label(shell, SWT.RIGHT);
        wlEachRow.setText(BaseMessages.getString(PKG, "TableInputDialog.ExecuteForEachRow")); //$NON-NLS-1$
        props.setLook(wlEachRow);
        fdlEachRow = new FormData();
        fdlEachRow.left = new FormAttachment(0, 0);
        fdlEachRow.right = new FormAttachment(middle, -margin);
        fdlEachRow.bottom = new FormAttachment(wLimit, -margin);
        wlEachRow.setLayoutData(fdlEachRow);
        wEachRow = new Button(shell, SWT.CHECK);
        props.setLook(wEachRow);
        fdEachRow = new FormData();
        fdEachRow.left = new FormAttachment(middle, 0);
        fdEachRow.right = new FormAttachment(100, 0);
        fdEachRow.bottom = new FormAttachment(wLimit, -margin);
        wEachRow.setLayoutData(fdEachRow);
        SelectionAdapter lsSelMod = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
                input.setChanged();
            }
        };
        wEachRow.addSelectionListener(lsSelMod);



		// Read date from...
		wlDatefrom=new Label(shell, SWT.RIGHT);
		wlDatefrom.setText(BaseMessages.getString(PKG, "TableInputDialog.InsertDataFromStep")); //$NON-NLS-1$
 		props.setLook(wlDatefrom);
		fdlDatefrom=new FormData();
		fdlDatefrom.left = new FormAttachment(0, 0);
		fdlDatefrom.right= new FormAttachment(middle, -margin);
		fdlDatefrom.bottom = new FormAttachment(wEachRow, -margin);
		wlDatefrom.setLayoutData(fdlDatefrom);
		wDatefrom=new CCombo(shell, SWT.BORDER );
 		props.setLook(wDatefrom);

		for (int i=0;i<transMeta.findNrPrevSteps(stepname);i++)
		{
			StepMeta stepMeta = transMeta.findPrevStep(stepname, i);
			wDatefrom.add(stepMeta.getName());
		}
		
		wDatefrom.addModifyListener(lsMod);
		fdDatefrom=new FormData();
		fdDatefrom.left = new FormAttachment(middle, 0);
		fdDatefrom.right= new FormAttachment(100, 0);
		fdDatefrom.bottom = new FormAttachment(wEachRow, -margin);
		wDatefrom.setLayoutData(fdDatefrom);

        // Replace variables in SQL?
		//
        wlVariables = new Label(shell, SWT.RIGHT);
        wlVariables.setText(BaseMessages.getString(PKG, "TableInputDialog.ReplaceVariables")); //$NON-NLS-1$
        props.setLook(wlVariables);
        fdlVariables = new FormData();
        fdlVariables.left = new FormAttachment(0, 0);
        fdlVariables.right = new FormAttachment(middle, -margin);
        fdlVariables.bottom = new FormAttachment(wDatefrom, -margin);
        wlVariables.setLayoutData(fdlVariables);
        wVariables = new Button(shell, SWT.CHECK);
        props.setLook(wVariables);
        fdVariables = new FormData();
        fdVariables.left = new FormAttachment(middle, 0);
        fdVariables.right = new FormAttachment(100, 0);
        fdVariables.bottom = new FormAttachment(wDatefrom, -margin);
        wVariables.setLayoutData(fdVariables);
        wVariables.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent arg0) { input.setChanged();setSQLToolTip(); } });

        // Lazy conversion?
		//
        wlLazyConversion = new Label(shell, SWT.RIGHT);
        wlLazyConversion.setText(BaseMessages.getString(PKG, "TableInputDialog.LazyConversion")); //$NON-NLS-1$
        props.setLook(wlLazyConversion);
        fdlLazyConversion = new FormData();
        fdlLazyConversion.left = new FormAttachment(0, 0);
        fdlLazyConversion.right = new FormAttachment(middle, -margin);
        fdlLazyConversion.bottom = new FormAttachment(wVariables, -margin);
        wlLazyConversion.setLayoutData(fdlLazyConversion);
        wLazyConversion = new Button(shell, SWT.CHECK);
        props.setLook(wLazyConversion);
        fdLazyConversion = new FormData();
        fdLazyConversion.left = new FormAttachment(middle, 0);
        fdLazyConversion.right = new FormAttachment(100, 0);
        fdLazyConversion.bottom = new FormAttachment(wVariables, -margin);
        wLazyConversion.setLayoutData(fdLazyConversion);
        wLazyConversion.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent arg0) { input.setChanged();setSQLToolTip(); } });

		wlPosition=new Label(shell, SWT.NONE); 
		props.setLook(wlPosition);
		fdlPosition=new FormData();
		fdlPosition.left  = new FormAttachment(0,0);
		fdlPosition.right = new FormAttachment(100, 0);
		fdlPosition.bottom = new FormAttachment(wLazyConversion, -margin);
		wlPosition.setLayoutData(fdlPosition);
		
		
		// Table line...
		wlSQL=new Label(shell, SWT.NONE);
		wlSQL.setText(BaseMessages.getString(PKG, "TableInputDialog.SQL")); //$NON-NLS-1$
 		props.setLook(wlSQL);
		fdlSQL=new FormData();
		fdlSQL.left = new FormAttachment(0, 0);
		fdlSQL.top  = new FormAttachment(wConnection, margin*2);
		wlSQL.setLayoutData(fdlSQL);

		wbTable=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbTable);
		wbTable.setText(BaseMessages.getString(PKG, "TableInputDialog.GetSQLAndSelectStatement")); //$NON-NLS-1$
		fdbTable=new FormData();
		fdbTable.right = new FormAttachment(100, 0);
		fdbTable.top   = new FormAttachment(wConnection, margin*2);
		wbTable.setLayoutData(fdbTable);

		wSQL=new StyledTextComp(shell, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, "");
 		props.setLook(wSQL, Props.WIDGET_STYLE_FIXED);
		wSQL.addModifyListener(lsMod);
		fdSQL=new FormData();
		fdSQL.left  = new FormAttachment(0, 0);
		fdSQL.top   = new FormAttachment(wbTable, margin );
		fdSQL.right = new FormAttachment(100, 0);
		fdSQL.bottom= new FormAttachment(wlPosition, -margin );
		wSQL.setLayoutData(fdSQL);
		wSQL.addModifyListener(new ModifyListener()
            {
                public void modifyText(ModifyEvent arg0)
                {
                    setSQLToolTip();
                    setPosition(); 
                }
            }
        );

		
		wSQL.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e) { setPosition(); }
			public void keyReleased(KeyEvent e) { setPosition(); }
			} 
		);
		wSQL.addFocusListener(new FocusAdapter(){
			public void focusGained(FocusEvent e) { setPosition(); }
			public void focusLost(FocusEvent e) { setPosition(); }
			}
		);
		wSQL.addMouseListener(new MouseAdapter(){
			public void mouseDoubleClick(MouseEvent e) { setPosition(); }
			public void mouseDown(MouseEvent e) { setPosition(); }
			public void mouseUp(MouseEvent e) { setPosition(); }
			}
		);
		
		// Text Higlighting
		lineStyler = new SQLValuesHighlight();
		wSQL.addLineStyleListener(lineStyler);
		
		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();  } };
        lsPreview  = new Listener() { public void handleEvent(Event e) { preview(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();      } };
		lsbTable   = new Listener() { public void handleEvent(Event e) { getSQL();  } };
        lsDateform = new Listener() { public void handleEvent(Event e) { setFags(); } };
        
		wCancel.addListener  (SWT.Selection, lsCancel);
        wPreview.addListener (SWT.Selection, lsPreview);
		wOK.addListener      (SWT.Selection, lsOK    );
		wbTable.addListener  (SWT.Selection, lsbTable);
        wDatefrom.addListener(SWT.Selection, lsDateform);
        wDatefrom.addListener(SWT.FocusOut,  lsDateform);

		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wLimit.addSelectionListener( lsDef );
		
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { checkCancel(e); } } );
		
		getData();
		changedInDialog = false; // for prompting if dialog is simply closed
		input.setChanged(changed);

		// Set the shell size, based upon previous time...
		setSize();
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
public void setPosition(){
		
		String scr = wSQL.getText();
		int linenr = wSQL.getLineAtOffset(wSQL.getCaretOffset())+1;
		int posnr  = wSQL.getCaretOffset();
				
		// Go back from position to last CR: how many positions?
		int colnr=0;
		while (posnr>0 && scr.charAt(posnr-1)!='\n' && scr.charAt(posnr-1)!='\r')
		{
			posnr--;
			colnr++;
		}
		wlPosition.setText(BaseMessages.getString(PKG, "TableInputDialog.Position.Label",""+linenr,""+colnr));

	}
	protected void setSQLToolTip()
    {
       if (wVariables.getSelection())
       {
           wSQL.setToolTipText(transMeta.environmentSubstitute(wSQL.getText()));
       }
    }

    /**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		if (input.getSQL() != null) wSQL.setText(input.getSQL());
		if (input.getDatabaseMeta() != null) wConnection.setText(input.getDatabaseMeta().getName());
		wLimit.setText(input.getRowLimit()); //$NON-NLS-1$
		
        if (input.getLookupStepname() != null)
        {
            wDatefrom.setText(input.getLookupStepname());
            wEachRow.setSelection(input.isExecuteEachInputRow());
        }
        else
        {
            wEachRow.setEnabled(false);
            wlEachRow.setEnabled(false);
        }
        
        wVariables.setSelection(input.isVariableReplacementActive());
        wLazyConversion.setSelection(input.isLazyConversionActive());
               
		wStepname.selectAll();
        setSQLToolTip();
	}
	
	private void checkCancel(ShellEvent e)
	{
		if (changedInDialog)
		{
			int save = JobGraph.showChangedWarning(shell, wStepname.getText());
			if (save == SWT.CANCEL)
			{
				e.doit = false;
			}
			else if (save == SWT.YES)
			{
				ok();
			}
			else
			{
				cancel();
			}
		}
		else
		{
			cancel();
		}
	}

	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	
    private void getInfo(TableInputMeta meta, boolean preview)
    {
        meta.setSQL(preview && !Const.isEmpty(wSQL.getSelectionText())?wSQL.getSelectionText():wSQL.getText());
        meta.setDatabaseMeta( transMeta.findDatabase(wConnection.getText()) );
        meta.setRowLimit( wLimit.getText() );
        meta.setLookupFromStep( transMeta.findStep( wDatefrom.getText() ) );
        meta.setExecuteEachInputRow(wEachRow.getSelection());
        meta.setVariableReplacementActive(wVariables.getSelection());
        meta.setLazyConversionActive(wLazyConversion.getSelection());
    }
    
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		stepname = wStepname.getText(); // return value
		// copy info to TextFileInputMeta class (input)
        
        getInfo(input, false);
        
		if (input.getDatabaseMeta()==null)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(BaseMessages.getString(PKG, "TableInputDialog.SelectValidConnection")); //$NON-NLS-1$
			mb.setText(BaseMessages.getString(PKG, "TableInputDialog.DialogCaptionError")); //$NON-NLS-1$
			mb.open();
		}
		
		dispose();
	}
	
	private void getSQL()
	{
		DatabaseMeta inf = transMeta.findDatabase(wConnection.getText());
		if (inf!=null)
		{	
			DatabaseExplorerDialog std = new DatabaseExplorerDialog(shell, SWT.NONE, inf, transMeta.getDatabases());
            std.setSplitSchemaAndTable(true);
			if (std.open()!= null)
			{
				String sql = "SELECT *"+Const.CR+"FROM "+inf.getQuotedSchemaTableCombination(std.getSchemaName(), std.getTableName())+Const.CR; //$NON-NLS-1$ //$NON-NLS-2$
				wSQL.setText(sql);

				MessageBox yn = new MessageBox(shell, SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_QUESTION);
				yn.setMessage(BaseMessages.getString(PKG, "TableInputDialog.IncludeFieldNamesInSQL")); //$NON-NLS-1$
				yn.setText(BaseMessages.getString(PKG, "TableInputDialog.DialogCaptionQuestion")); //$NON-NLS-1$
				int id = yn.open();
				switch(id)
				{
				case SWT.CANCEL: break;
				case SWT.NO:     wSQL.setText(sql); break;
				case SWT.YES:
					Database db = new Database(loggingObject, inf);
					db.shareVariablesWith(transMeta);
					try
					{
						db.connect();
						RowMetaInterface fields = db.getQueryFields(sql, false);
						if (fields!=null)
						{
							sql = "SELECT"+Const.CR; //$NON-NLS-1$
							for (int i=0;i<fields.size();i++)
							{
								ValueMetaInterface field=fields.getValueMeta(i);
								if (i==0) sql+="  "; else sql+=", "; //$NON-NLS-1$ //$NON-NLS-2$
								sql+=inf.quoteField(field.getName())+Const.CR;
							}
							sql+="FROM "+inf.getQuotedSchemaTableCombination(std.getSchemaName(), std.getTableName())+Const.CR; //$NON-NLS-1$
							wSQL.setText(sql);
						}
						else
						{
							MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
							mb.setMessage(BaseMessages.getString(PKG, "TableInputDialog.ERROR_CouldNotRetrieveFields")+Const.CR+BaseMessages.getString(PKG, "TableInputDialog.PerhapsNoPermissions")); //$NON-NLS-1$ //$NON-NLS-2$
							mb.setText(BaseMessages.getString(PKG, "TableInputDialog.DialogCaptionError2")); //$NON-NLS-1$
							mb.open();
						}
					}
					catch(KettleException e)
					{
						MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
						mb.setText(BaseMessages.getString(PKG, "TableInputDialog.DialogCaptionError3")); //$NON-NLS-1$
						mb.setMessage(BaseMessages.getString(PKG, "TableInputDialog.AnErrorOccurred")+Const.CR+e.getMessage()); //$NON-NLS-1$
						mb.open(); 
					}
					finally
					{
						db.disconnect();
					}
					break;
				}
			}
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(BaseMessages.getString(PKG, "TableInputDialog.ConnectionNoLongerAvailable")); //$NON-NLS-1$
			mb.setText(BaseMessages.getString(PKG, "TableInputDialog.DialogCaptionError4")); //$NON-NLS-1$
			mb.open();
		}
					
	}
	
    private void setFags()
    {
        if (wDatefrom.getText() != null && wDatefrom.getText().length() > 0)
        {
            // The foreach check box... 
            wEachRow.setEnabled(true);
            wlEachRow.setEnabled(true);
            
            // The preview button...
            wPreview.setEnabled(false);
        }
        else
        {
            // The foreach check box... 
            wEachRow.setEnabled(false);
            wEachRow.setSelection(false);
            wlEachRow.setEnabled(false);
            
            // The preview button...
            wPreview.setEnabled(true);
        }
        
    }

    /**
     * Preview the data generated by this step.
     * This generates a transformation using this step & a dummy and previews it.
     *
     */
    private void preview()
    {
        // Create the table input reader step...
        TableInputMeta oneMeta = new TableInputMeta();
        getInfo(oneMeta, true);
        
        TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, wStepname.getText());
        
        EnterNumberDialog numberDialog = new EnterNumberDialog(shell, props.getDefaultPreviewSize(), BaseMessages.getString(PKG, "TableInputDialog.EnterPreviewSize"), BaseMessages.getString(PKG, "TableInputDialog.NumberOfRowsToPreview")); //$NON-NLS-1$ //$NON-NLS-2$
        int previewSize = numberDialog.open();
        if (previewSize>0)
        {
            TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(shell, previewMeta, new String[] { wStepname.getText() }, new int[] { previewSize } );
            progressDialog.open();

            Trans trans = progressDialog.getTrans();
            String loggingText = progressDialog.getLoggingText();

            if (!progressDialog.isCancelled())
            {
                if (trans.getResult()!=null && trans.getResult().getNrErrors()>0)
                {
                	EnterTextDialog etd = new EnterTextDialog(shell, BaseMessages.getString(PKG, "System.Dialog.PreviewError.Title"),  
                			BaseMessages.getString(PKG, "System.Dialog.PreviewError.Message"), loggingText, true );
                	etd.setReadOnly();
                	etd.open();
                } 
                else
                {
                    PreviewRowsDialog prd =new PreviewRowsDialog(shell, transMeta, SWT.NONE, wStepname.getText(), progressDialog.getPreviewRowsMeta(wStepname.getText()), progressDialog.getPreviewRows(wStepname.getText()), loggingText);
                    prd.open();
                }
            }
            
        }
    }
}
