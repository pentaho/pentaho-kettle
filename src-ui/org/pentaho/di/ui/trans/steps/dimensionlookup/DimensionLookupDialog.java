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
 * Created on 2-jul-2003
 * 24May2006: Make DimensionLookup and CombinationLookup look more alike.
 */
package org.pentaho.di.ui.trans.steps.dimensionlookup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.dimensionlookup.DimensionLookupMeta;
import org.pentaho.di.trans.steps.dimensionlookup.Messages;
import org.pentaho.di.ui.core.database.dialog.DatabaseExplorerDialog;
import org.pentaho.di.ui.core.database.dialog.SQLEditor;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;



/**
 *  Dialog for the Dimension Lookup/Update step. 
 */
public class DimensionLookupDialog extends BaseStepDialog implements StepDialogInterface
{
	private CTabFolder   wTabFolder;
	private FormData     fdTabFolder;

	private CTabItem     wKeyTab, wFieldsTab;

	private FormData     fdKeyComp, fdFieldsComp;

	private CCombo       wConnection;

    private Label        wlSchema;
    private TextVar      wSchema;

	private Label        wlTable;
	private Button       wbTable;
	private TextVar      wTable;

	private Label        wlCommit;
	private Text         wCommit;

	private Label        wlUseCache;
	private Button       wUseCache;

	private Label        wlPreloadCache;
	private Button       wPreloadCache;

    private Label        wlCacheSize;
    private Text         wCacheSize;

	private Label        wlTk;
	private CCombo        wTk;

	private Label        wlTkRename;
	private Text         wTkRename;

	private Group        gTechGroup;
	
	private Label        wlAutoinc;
	private Button       wAutoinc;

	private Label        wlTableMax;
	private Button       wTableMax;	

	private Label        wlSeqButton;
	private Button       wSeqButton;			
	private Text         wSeq;    	

	private Label        wlVersion;
	private CCombo       wVersion;

	private Label        wlDatefield;
	private CCombo       wDatefield;

	private Label        wlFromdate;
	private CCombo       wFromdate;

	private Label        wlUseAltStartDate;
	private Button       wUseAltStartDate;
	private CCombo       wAltStartDate;
	private CCombo       wAltStartDateField;
	
	private Label        wlMinyear;
	private Text         wMinyear;	

	
	private Label        wlTodate;
	private CCombo       wTodate;

	private Label        wlMaxyear;
	private Text         wMaxyear;

	private Label        wlUpdate;
	private Button       wUpdate;

	private Label        wlKey;
	private TableView    wKey;

	private Label        wlUpIns;
	private TableView    wUpIns;

	private Button   wGet, wCreate;
	private Listener lsGet, lsCreate;

	private DimensionLookupMeta input;
	private boolean backupUpdate, backupAutoInc;

	private DatabaseMeta ci;
	
	private ColumnInfo[] ciUpIns;
	
	private ColumnInfo[] ciKey;
	
    private Map<String, Integer> inputFields;
    
    private boolean gotPreviousFields=false;
    
    private boolean gotTableFields=false;
    
	/**
	 * List of ColumnInfo that should have the field names of the selected database table
	 */
	private List<ColumnInfo> tableFieldColumns = new ArrayList<ColumnInfo>();

	public DimensionLookupDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(DimensionLookupMeta)in;
        inputFields =new HashMap<String, Integer>();
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
		FocusListener lsFocusLost = new FocusAdapter() {
			public void focusLost(FocusEvent arg0) {
				setTableFieldCombo();
			}
		};
		backupChanged = input.hasChanged();
		backupUpdate = input.isUpdate();
		backupAutoInc = input.isAutoIncrement();
		ci = input.getDatabaseMeta();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("DimensionLookupDialog.Shell.Title")); //$NON-NLS-1$

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("DimensionLookupDialog.Stepname.Label")); //$NON-NLS-1$
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

        // Update the dimension?
        wlUpdate=new Label(shell, SWT.RIGHT);
        wlUpdate.setText(Messages.getString("DimensionLookupDialog.Update.Label")); //$NON-NLS-1$
        props.setLook(wlUpdate);
        FormData fdlUpdate=new FormData();
        fdlUpdate.left   = new FormAttachment(0, 0);
        fdlUpdate.right  = new FormAttachment(middle, -margin);
        fdlUpdate.top    = new FormAttachment(wStepname, margin);
        wlUpdate.setLayoutData(fdlUpdate);
        wUpdate=new Button(shell, SWT.CHECK);
        props.setLook(wUpdate);
        FormData fdUpdate=new FormData();
        fdUpdate.left = new FormAttachment(middle, 0);
        fdUpdate.top  = new FormAttachment(wStepname, margin);
        fdUpdate.right= new FormAttachment(100, 0);
        wUpdate.setLayoutData(fdUpdate); 

        // Clicking on update changes the options in the update combo boxes!
        wUpdate.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    input.setUpdate(!input.isUpdate());
                    input.setChanged();

                    setFlags();
                }
            }
        );      
        


		// Connection line
		wConnection = addConnectionLine(shell, wUpdate, middle, margin);
		if (input.getDatabaseMeta()==null && transMeta.nrDatabases()==1) wConnection.select(0);
		wConnection.addModifyListener(lsMod);

		wConnection.addModifyListener(new ModifyListener()
    		{
    			public void modifyText(ModifyEvent e)
    			{
    				// We have new content: change ci connection:
    				ci = transMeta.findDatabase(wConnection.getText());
    				setFlags();
    			}
    		}
        );

        // Schema line...
        wlSchema=new Label(shell, SWT.RIGHT);
        wlSchema.setText(Messages.getString("DimensionLookupDialog.TargetSchema.Label")); //$NON-NLS-1$
        props.setLook(wlSchema);
        FormData fdlSchema = new FormData();
        fdlSchema.left = new FormAttachment(0, 0);
        fdlSchema.right= new FormAttachment(middle, -margin);
        fdlSchema.top  = new FormAttachment(wConnection, margin);
        wlSchema.setLayoutData(fdlSchema);

        wSchema=new TextVar(transMeta,shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wSchema);
        wSchema.addModifyListener(lsMod);
        wSchema.addFocusListener(lsFocusLost);
        FormData fdSchema = new FormData();
        fdSchema.left = new FormAttachment(middle, 0);
        fdSchema.top  = new FormAttachment(wConnection, margin);
        fdSchema.right= new FormAttachment(100, 0);
        wSchema.setLayoutData(fdSchema);


		// Table line...
		wlTable=new Label(shell, SWT.RIGHT);
		wlTable.setText(Messages.getString("DimensionLookupDialog.TargeTable.Label")); //$NON-NLS-1$
 		props.setLook(wlTable);
		FormData fdlTable=new FormData();
		fdlTable.left = new FormAttachment(0, 0);
		fdlTable.right= new FormAttachment(middle, -margin);
		fdlTable.top  = new FormAttachment(wSchema, margin);
		wlTable.setLayoutData(fdlTable);

		wbTable=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbTable);
		wbTable.setText(Messages.getString("DimensionLookupDialog.Browse.Button")); //$NON-NLS-1$
		FormData fdbTable=new FormData();
		fdbTable.right= new FormAttachment(100, 0);
		fdbTable.top  = new FormAttachment(wSchema, margin);
		wbTable.setLayoutData(fdbTable);

		wTable=new TextVar(transMeta,shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wTable);
		wTable.addModifyListener(lsMod);
		wTable.addFocusListener(lsFocusLost);
		FormData fdTable=new FormData();
		fdTable.left = new FormAttachment(middle, 0);
		fdTable.top  = new FormAttachment(wSchema, margin);
		fdTable.right= new FormAttachment(wbTable, 0);
		wTable.setLayoutData(fdTable);

		// Commit size ...
		wlCommit=new Label(shell, SWT.RIGHT);
		wlCommit.setText(Messages.getString("DimensionLookupDialog.Commit.Label")); //$NON-NLS-1$
 		props.setLook(wlCommit);
		FormData fdlCommit=new FormData();
		fdlCommit.left = new FormAttachment(0, 0);
		fdlCommit.right= new FormAttachment(middle, -margin);
		fdlCommit.top  = new FormAttachment(wTable, margin);
		wlCommit.setLayoutData(fdlCommit);
		wCommit=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wCommit);
		wCommit.addModifyListener(lsMod);
		FormData fdCommit=new FormData();
		fdCommit.left = new FormAttachment(middle, 0);
		fdCommit.top  = new FormAttachment(wTable, margin);
		fdCommit.right= new FormAttachment(100, 0);
		wCommit.setLayoutData(fdCommit);

        // Use Cache?
        wlUseCache=new Label(shell, SWT.RIGHT);
        wlUseCache.setText(Messages.getString("DimensionLookupDialog.UseCache.Label")); //$NON-NLS-1$
        props.setLook(wlUseCache);
        FormData fdlUseCache=new FormData();
        fdlUseCache.left = new FormAttachment(0, 0);
        fdlUseCache.right= new FormAttachment(middle, -margin);
        fdlUseCache.top  = new FormAttachment(wCommit, margin);
        wlUseCache.setLayoutData(fdlUseCache);
        wUseCache=new Button(shell, SWT.CHECK);
        props.setLook(wUseCache);
        wUseCache.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent arg0) { setFlags(); input.setChanged(); }});
        FormData fdUseCache=new FormData();
        fdUseCache.left = new FormAttachment(middle, 0);
        fdUseCache.top  = new FormAttachment(wCommit, margin);
        fdUseCache.right= new FormAttachment(100, 0);
        wUseCache.setLayoutData(fdUseCache);

        // Preload cache?
        wlPreloadCache=new Label(shell, SWT.RIGHT);
        wlPreloadCache.setText(Messages.getString("DimensionLookupDialog.PreloadCache.Label")); //$NON-NLS-1$
        props.setLook(wlPreloadCache);
        FormData fdlPreloadCache=new FormData();
        fdlPreloadCache.left = new FormAttachment(0, 0);
        fdlPreloadCache.right= new FormAttachment(middle, -margin);
        fdlPreloadCache.top  = new FormAttachment(wUseCache, margin);
        wlPreloadCache.setLayoutData(fdlPreloadCache);
        wPreloadCache=new Button(shell, SWT.CHECK);
        props.setLook(wPreloadCache);
        wPreloadCache.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent arg0) { setFlags(); input.setChanged(); }});
        FormData fdPreloadCache=new FormData();
        fdPreloadCache.left = new FormAttachment(middle, 0);
        fdPreloadCache.top  = new FormAttachment(wUseCache, margin);
        fdPreloadCache.right= new FormAttachment(100, 0);
        wPreloadCache.setLayoutData(fdPreloadCache);

        // Cache size ...
        wlCacheSize=new Label(shell, SWT.RIGHT);
        wlCacheSize.setText(Messages.getString("DimensionLookupDialog.CacheSize.Label")); //$NON-NLS-1$
        props.setLook(wlCacheSize);
        FormData fdlCacheSize=new FormData();
        fdlCacheSize.left = new FormAttachment(0, 0);
        fdlCacheSize.right= new FormAttachment(middle, -margin);
        fdlCacheSize.top  = new FormAttachment(wPreloadCache, margin);
        wlCacheSize.setLayoutData(fdlCacheSize);
        wCacheSize=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wCacheSize);
        wCacheSize.addModifyListener(lsMod);
        FormData fdCacheSize=new FormData();
        fdCacheSize.left = new FormAttachment(middle, 0);
        fdCacheSize.top  = new FormAttachment(wPreloadCache, margin);
        fdCacheSize.right= new FormAttachment(100, 0);
        wCacheSize.setLayoutData(fdCacheSize);

		wlTkRename=new Label(shell, SWT.RIGHT);

        wTabFolder = new CTabFolder(shell, SWT.BORDER);
 		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
 		
		//////////////////////////
		// START OF KEY TAB    ///
		///
		wKeyTab=new CTabItem(wTabFolder, SWT.NONE);
		wKeyTab.setText(Messages.getString("DimensionLookupDialog.KeyTab.CTabItem")); //$NON-NLS-1$

		FormLayout keyLayout = new FormLayout ();
		keyLayout.marginWidth  = 3;
		keyLayout.marginHeight = 3;

		Composite wKeyComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wKeyComp);
		wKeyComp.setLayout(keyLayout);

		//
		// The Lookup fields: usually the key
		//
		wlKey=new Label(wKeyComp, SWT.NONE);
		wlKey.setText(Messages.getString("DimensionLookupDialog.KeyFields.Label")); //$NON-NLS-1$
 		props.setLook(wlKey);
		FormData fdlKey=new FormData();
		fdlKey.left  = new FormAttachment(0, 0);
		fdlKey.top   = new FormAttachment(0, margin);
		fdlKey.right = new FormAttachment(100, 0);
		wlKey.setLayoutData(fdlKey);

		int nrKeyCols=2;
		int nrKeyRows=(input.getKeyStream()!=null?input.getKeyStream().length:1);

		ciKey=new ColumnInfo[nrKeyCols];
		ciKey[0]=new ColumnInfo(Messages.getString("DimensionLookupDialog.ColumnInfo.DimensionField"),  ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		ciKey[1]=new ColumnInfo(Messages.getString("DimensionLookupDialog.ColumnInfo.FieldInStream"),  ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		tableFieldColumns.add(ciKey[0]);
		wKey=new TableView(transMeta, wKeyComp,
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL,
						      ciKey,
						      nrKeyRows,
						      lsMod,
							  props
						      );

		FormData fdKey=new FormData();
		fdKey.left  = new FormAttachment(0, 0);
		fdKey.top   = new FormAttachment(wlKey, margin);
		fdKey.right = new FormAttachment(100, 0);
		fdKey.bottom= new FormAttachment(100, 0);
		wKey.setLayoutData(fdKey);

		fdKeyComp = new FormData();
		fdKeyComp.left  = new FormAttachment(0, 0);
		fdKeyComp.top   = new FormAttachment(0, 0);
		fdKeyComp.right = new FormAttachment(100, 0);
		fdKeyComp.bottom= new FormAttachment(100, 0);
		wKeyComp.setLayoutData(fdKeyComp);

		wKeyComp.layout();
		wKeyTab.setControl(wKeyComp);

		/////////////////////////////////////////////////////////////
		/// END OF KEY TAB
		/////////////////////////////////////////////////////////////

		// Fields tab...
		//
		wFieldsTab = new CTabItem(wTabFolder, SWT.NONE);
		wFieldsTab.setText(Messages.getString("DimensionLookupDialog.FieldsTab.CTabItem.Title")); //$NON-NLS-1$

		Composite wFieldsComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wFieldsComp);
        
        FormLayout fieldsCompLayout = new FormLayout ();
        fieldsCompLayout.marginWidth  = Const.FORM_MARGIN;
        fieldsCompLayout.marginHeight = Const.FORM_MARGIN;
		wFieldsComp.setLayout(fieldsCompLayout);

		// THE UPDATE/INSERT TABLE
		wlUpIns=new Label(wFieldsComp, SWT.NONE);
		wlUpIns.setText(Messages.getString("DimensionLookupDialog.UpdateOrInsertFields.Label")); //$NON-NLS-1$
 		props.setLook(wlUpIns);
		FormData fdlUpIns=new FormData();
		fdlUpIns.left  = new FormAttachment(0, 0);
		fdlUpIns.top   = new FormAttachment(0, margin);
		wlUpIns.setLayoutData(fdlUpIns);

		int UpInsCols=3;
		int UpInsRows= (input.getFieldStream()!=null?input.getFieldStream().length:1);

		ciUpIns=new ColumnInfo[UpInsCols];
		ciUpIns[0]=new ColumnInfo(Messages.getString("DimensionLookupDialog.ColumnInfo.DimensionField"),              ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		ciUpIns[1]=new ColumnInfo(Messages.getString("DimensionLookupDialog.ColumnInfo.StreamField"),                 ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		ciUpIns[2]=new ColumnInfo(Messages.getString("DimensionLookupDialog.ColumnInfo.TypeOfDimensionUpdate"),     ColumnInfo.COLUMN_TYPE_CCOMBO, input.isUpdate()?DimensionLookupMeta.typeDesc:DimensionLookupMeta.typeDescLookup ); //$NON-NLS-1$
		tableFieldColumns.add(ciUpIns[0]);
		wUpIns=new TableView(transMeta, wFieldsComp,
							  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL,
							  ciUpIns,
							  UpInsRows,
							  lsMod,
							  props
							  );

		FormData fdUpIns=new FormData();
		fdUpIns.left  = new FormAttachment(0, 0);
		fdUpIns.top   = new FormAttachment(wlUpIns, margin);
		fdUpIns.right = new FormAttachment(100, 0);
		fdUpIns.bottom= new FormAttachment(100, 0);
		wUpIns.setLayoutData(fdUpIns);
		
	    // 
        // Search the fields in the background
        //
        
        final Runnable runnable = new Runnable()
        {
            public void run()
            {
                StepMeta stepMeta = transMeta.findStep(stepname);
                if (stepMeta!=null)
                {
                    try
                    {
                    	RowMetaInterface row = transMeta.getPrevStepFields(stepMeta);
                        
                        // Remember these fields...
                        for (int i=0;i<row.size();i++)
                        {
                        	inputFields.put(row.getValueMeta(i).getName(), Integer.valueOf(i));
                        }
                        
                        setComboBoxes(); 
                    }
                    catch(KettleException e)
                    {
                        log.logError(toString(),Messages.getString("System.Dialog.GetFieldsFailed.Message"));
                    }
                }
            }
        };
        new Thread(runnable).start();

		fdFieldsComp=new FormData();
		fdFieldsComp.left  = new FormAttachment(0, 0);
		fdFieldsComp.top   = new FormAttachment(0, 0);
		fdFieldsComp.right = new FormAttachment(100, 0);
		fdFieldsComp.bottom= new FormAttachment(100, 0);
		wFieldsComp.setLayoutData(fdFieldsComp);

		wFieldsComp.layout();
		wFieldsTab.setControl(wFieldsComp);

		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(wCacheSize, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(wlTkRename, -2 * margin);		
		wTabFolder.setLayoutData(fdTabFolder);

		
        ////////////////////////////////////////////////////////////////////
		// The next parts are from the bottom upwards so that the table
		// gets a chance to expand when the dialog is enlarged.

		// THE BOTTOM BUTTONS 
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$		
		wGet=new Button(shell, SWT.PUSH);
		wGet.setText(Messages.getString("DimensionLookupDialog.GetFields.Button")); //$NON-NLS-1$
		wCreate=new Button(shell, SWT.PUSH);
		wCreate.setText(Messages.getString("DimensionLookupDialog.SQL.Button")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel , wGet, wCreate }, margin, null);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();    } };
		lsCreate   = new Listener() { public void handleEvent(Event e) { create(); } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };

		wOK.addListener    (SWT.Selection, lsOK    );
		wGet.addListener   (SWT.Selection, lsGet   );
		wCreate.addListener(SWT.Selection, lsCreate);
		wCancel.addListener(SWT.Selection, lsCancel);

		// Todate line
		wlTodate=new Label(shell, SWT.RIGHT);
		wlTodate.setText(Messages.getString("DimensionLookupDialog.Todate.Label")); //$NON-NLS-1$
 		props.setLook(wlTodate);
		FormData fdlTodate=new FormData();
		fdlTodate.left  = new FormAttachment(0, 0);
		fdlTodate.right = new FormAttachment(middle, -margin);
		fdlTodate.bottom= new FormAttachment(wOK, -2 * margin);		
		wlTodate.setLayoutData(fdlTodate); 
		wTodate=new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wTodate);
		wTodate.addModifyListener(lsMod);
		FormData fdTodate=new FormData();
		fdTodate.left   = new FormAttachment(middle, 0);
		fdTodate.right  = new FormAttachment(middle+(100-middle)/3, -margin);
		fdTodate.bottom = new FormAttachment(wOK, -2 * margin); 
		wTodate.setLayoutData(fdTodate); 
		wTodate.addFocusListener(new FocusListener()
        {
            public void focusLost(org.eclipse.swt.events.FocusEvent e)
            {
            }
        
            public void focusGained(org.eclipse.swt.events.FocusEvent e)
            {
                Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                shell.setCursor(busy);
                getFieldsFromTable();
                shell.setCursor(null);
                busy.dispose();
            }
        }
    );

		
		// Maxyear line
		wlMaxyear=new Label(shell, SWT.RIGHT);
		wlMaxyear.setText(Messages.getString("DimensionLookupDialog.Maxyear.Label")); //$NON-NLS-1$
 		props.setLook(wlMaxyear);
		FormData fdlMaxyear=new FormData();
		fdlMaxyear.left  = new FormAttachment(wTodate, margin);
		fdlMaxyear.right = new FormAttachment(middle+2*(100-middle)/3, -margin);
		fdlMaxyear.bottom = new FormAttachment(wOK, -2 * margin);
		wlMaxyear.setLayoutData(fdlMaxyear); 
		wMaxyear=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wMaxyear);
		wMaxyear.addModifyListener(lsMod);
		FormData fdMaxyear=new FormData();
		fdMaxyear.left = new FormAttachment(wlMaxyear, margin);
		fdMaxyear.right= new FormAttachment(100, 0);
		fdMaxyear.bottom  = new FormAttachment(wOK, -2 * margin);
		wMaxyear.setLayoutData(fdMaxyear);
		wMaxyear.setToolTipText(Messages.getString("DimensionLookupDialog.Maxyear.ToolTip")); //$NON-NLS-1$
		
		// Add a line with an option to specify an alternative start date...
		//
		wlUseAltStartDate=new Label(shell, SWT.RIGHT);
		wlUseAltStartDate.setText(Messages.getString("DimensionLookupDialog.UseAlternativeStartDate.Label")); //$NON-NLS-1$
		props.setLook(wlUseAltStartDate); 	
		FormData fdlUseAltStartDate=new FormData();
		fdlUseAltStartDate.left  = new FormAttachment(0, 0);
		fdlUseAltStartDate.right = new FormAttachment(middle, -margin);
		fdlUseAltStartDate.bottom = new FormAttachment(wTodate, -2 * margin);
		wlUseAltStartDate.setLayoutData(fdlUseAltStartDate); 
		wUseAltStartDate=new Button(shell, SWT.CHECK);
 		props.setLook(wUseAltStartDate);
		wUseAltStartDate.setToolTipText(Messages.getString("DimensionLookupDialog.UseAlternativeStartDate.Tooltip",Const.CR)); //$NON-NLS-1$ //$NON-NLS-2$		
		FormData fdUseAltStartDate=new FormData();
		fdUseAltStartDate.left = new FormAttachment(middle, 0);
		fdUseAltStartDate.bottom  = new FormAttachment(wTodate, -2 * margin);
		wUseAltStartDate.setLayoutData(fdUseAltStartDate);
        wUseAltStartDate.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { setFlags(); input.setChanged(); } });
        
		// The choice...
		//
		wAltStartDate=new CCombo(shell, SWT.BORDER);
 		props.setLook(wAltStartDate);
 		// All options except for "No alternative"...
 		wAltStartDate.removeAll();
 		for (int i=1;i<DimensionLookupMeta.getStartDateAlternativeDescriptions().length;i++) {
 			wAltStartDate.add(DimensionLookupMeta.getStartDateAlternativeDescriptions()[i]);
 		}
 		wAltStartDate.setText(Messages.getString("DimensionLookupDialog.AlternativeStartDate.SelectItemDefault"));
		wAltStartDate.setToolTipText(Messages.getString("DimensionLookupDialog.AlternativeStartDate.Tooltip",Const.CR)); //$NON-NLS-1$ //$NON-NLS-2$		
		FormData fdAltStartDate=new FormData();
		fdAltStartDate.left = new FormAttachment(wUseAltStartDate, 2*margin);
		fdAltStartDate.right = new FormAttachment(wUseAltStartDate, 200);
		fdAltStartDate.bottom  = new FormAttachment(wTodate, -2 * margin);
		wAltStartDate.setLayoutData(fdAltStartDate);
		wAltStartDate.addModifyListener(new ModifyListener() { public void modifyText(ModifyEvent arg0) { setFlags(); } });
		wAltStartDateField=new CCombo(shell, SWT.SINGLE | SWT.BORDER);
 		props.setLook(wAltStartDateField);
		wAltStartDateField.setToolTipText(Messages.getString("DimensionLookupDialog.AlternativeStartDateField.Tooltip",Const.CR)); //$NON-NLS-1$ //$NON-NLS-2$		
		FormData fdAltStartDateField=new FormData();
		fdAltStartDateField.left = new FormAttachment(wAltStartDate, 2*margin);
		fdAltStartDateField.right = new FormAttachment(100, 0);
		fdAltStartDateField.bottom  = new FormAttachment(wTodate, -2 * margin);
		wAltStartDateField.setLayoutData(fdAltStartDateField);
		wAltStartDateField.addFocusListener(new FocusListener()
        {
            public void focusLost(org.eclipse.swt.events.FocusEvent e)
            {
            }
        
            public void focusGained(org.eclipse.swt.events.FocusEvent e)
            {
                Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                shell.setCursor(busy);
                getFieldsFromTable();
                shell.setCursor(null);
                busy.dispose();
            }
        }
    );
		
		// Fromdate line
		//
		//  0 [wlFromdate] middle [wFromdate] (100-middle)/3 [wlMinyear] 2*(100-middle)/3 [wMinyear] 100%
		//
		wlFromdate=new Label(shell, SWT.RIGHT);
		wlFromdate.setText(Messages.getString("DimensionLookupDialog.Fromdate.Label")); //$NON-NLS-1$
 		props.setLook(wlFromdate);
		FormData fdlFromdate=new FormData();
		fdlFromdate.left = new FormAttachment(0, 0);
		fdlFromdate.right= new FormAttachment(middle, -margin);
		fdlFromdate.bottom  = new FormAttachment(wAltStartDate, -margin);
		wlFromdate.setLayoutData(fdlFromdate);
		wFromdate=new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFromdate);
		wFromdate.addModifyListener(lsMod);
		FormData fdFromdate=new FormData();
		fdFromdate.left = new FormAttachment(middle, 0);
		fdFromdate.right= new FormAttachment(middle+(100-middle)/3, -margin);
		fdFromdate.bottom  = new FormAttachment(wAltStartDate, -margin);
		wFromdate.setLayoutData(fdFromdate);
		wFromdate.addFocusListener(new FocusListener()
        {
            public void focusLost(org.eclipse.swt.events.FocusEvent e)
            {
            }
        
            public void focusGained(org.eclipse.swt.events.FocusEvent e)
            {
                Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                shell.setCursor(busy);
                getFieldsFromTable();
                shell.setCursor(null);
                busy.dispose();
            }
        }
    );

		// Minyear line
		wlMinyear=new Label(shell, SWT.RIGHT);
		wlMinyear.setText(Messages.getString("DimensionLookupDialog.Minyear.Label")); //$NON-NLS-1$
 		props.setLook(wlMinyear); 		
		FormData fdlMinyear=new FormData();
		fdlMinyear.left  = new FormAttachment(wFromdate, margin);
		fdlMinyear.right = new FormAttachment(middle+2*(100-middle)/3, -margin);
		fdlMinyear.bottom = new FormAttachment(wAltStartDate, -margin);
		wlMinyear.setLayoutData(fdlMinyear);
		wMinyear=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wMinyear);
		wMinyear.addModifyListener(lsMod);
		FormData fdMinyear=new FormData();
		fdMinyear.left = new FormAttachment(wlMinyear, margin);
		fdMinyear.right= new FormAttachment(100, 0);
		fdMinyear.bottom = new FormAttachment(wAltStartDate, -margin);
		wMinyear.setLayoutData(fdMinyear);
		wMinyear.setToolTipText(Messages.getString("DimensionLookupDialog.Minyear.ToolTip")); //$NON-NLS-1$

		// Datefield line
		wlDatefield=new Label(shell, SWT.RIGHT);
		wlDatefield.setText(Messages.getString("DimensionLookupDialog.Datefield.Label")); //$NON-NLS-1$
 		props.setLook(wlDatefield);
		FormData fdlDatefield=new FormData();
		fdlDatefield.left = new FormAttachment(0, 0);
		fdlDatefield.right= new FormAttachment(middle, -margin);
		fdlDatefield.bottom = new FormAttachment(wMinyear, -margin);
		wlDatefield.setLayoutData(fdlDatefield);
		wDatefield=new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wDatefield);
		wDatefield.addModifyListener(lsMod);
		FormData fdDatefield=new FormData();
		fdDatefield.left   = new FormAttachment(middle, 0);
		fdDatefield.bottom = new FormAttachment(wMinyear, -margin);
		fdDatefield.right  = new FormAttachment(100, 0);
		wDatefield.setLayoutData(fdDatefield);
		wDatefield.addFocusListener(new FocusListener()
        {
            public void focusLost(org.eclipse.swt.events.FocusEvent e)
            {
            }
        
            public void focusGained(org.eclipse.swt.events.FocusEvent e)
            {
                Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                shell.setCursor(busy);
                getFields();
                shell.setCursor(null);
                busy.dispose();
            }
        }
    );

		// Version key field:
		wlVersion=new Label(shell, SWT.RIGHT);
		wlVersion.setText(Messages.getString("DimensionLookupDialog.Version.Label")); //$NON-NLS-1$
 		props.setLook(wlVersion);
		FormData fdlVersion=new FormData();
		fdlVersion.left    = new FormAttachment(0, 0);
		fdlVersion.right   = new FormAttachment(middle, -margin);
		fdlVersion.bottom  = new FormAttachment(wDatefield, -margin);
		wlVersion.setLayoutData(fdlVersion);
		wVersion=new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wVersion);
		wVersion.addModifyListener(lsMod);
		FormData fdVersion=new FormData();
		fdVersion.left   = new FormAttachment(middle, 0);
		fdVersion.bottom = new FormAttachment(wDatefield, -margin);
		fdVersion.right  = new FormAttachment(100, 0);
		wVersion.setLayoutData(fdVersion);
		wVersion.addFocusListener(new FocusListener()
        {
            public void focusLost(org.eclipse.swt.events.FocusEvent e)
            {
            }
        
            public void focusGained(org.eclipse.swt.events.FocusEvent e)
            {
                Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                shell.setCursor(busy);
                getFieldsFromTable();
                shell.setCursor(null);
                busy.dispose();
            }
        }
    );

		////////////////////////////////////////////////////
		// The key creation box
		////////////////////////////////////////////////////
		gTechGroup = new Group(shell, SWT.SHADOW_ETCHED_IN);
		gTechGroup.setText(Messages.getString("DimensionLookupDialog.TechGroup.Label")); //$NON-NLS-1$;
		GridLayout gridLayout = new GridLayout(3, false);
		gTechGroup.setLayout(gridLayout);
		FormData fdTechGroup=new FormData();
		fdTechGroup.left   = new FormAttachment(middle, 0);
		fdTechGroup.bottom = new FormAttachment(wVersion, -margin);	
		fdTechGroup.right  = new FormAttachment(100, 0);
		gTechGroup.setBackground(shell.getBackground()); // the default looks ugly
		gTechGroup.setLayoutData(fdTechGroup);

		// Use maximum of table + 1
		wTableMax=new Button(gTechGroup, SWT.RADIO);
 		props.setLook(wTableMax);
 		wTableMax.setSelection(false);
		GridData gdTableMax=new GridData();
		wTableMax.setLayoutData(gdTableMax);
		wTableMax.setToolTipText(Messages.getString("DimensionLookupDialog.TableMaximum.Tooltip",Const.CR)); //$NON-NLS-1$ //$NON-NLS-2$
		wlTableMax=new Label(gTechGroup, SWT.LEFT);
		wlTableMax.setText(Messages.getString("DimensionLookupDialog.TableMaximum.Label")); //$NON-NLS-1$
 		props.setLook(wlTableMax);
		GridData gdlTableMax = new GridData(GridData.FILL_BOTH);
		gdlTableMax.horizontalSpan = 2; gdlTableMax.verticalSpan = 1;
		wlTableMax.setLayoutData(gdlTableMax);
		
		// Sequence Check Button
		wSeqButton=new Button(gTechGroup, SWT.RADIO);
 		props.setLook(wSeqButton);
 		wSeqButton.setSelection(false);
 		GridData gdSeqButton=new GridData();
		wSeqButton.setLayoutData(gdSeqButton);
		wSeqButton.setToolTipText(Messages.getString("DimensionLookupDialog.Sequence.Tooltip",Const.CR)); //$NON-NLS-1$ //$NON-NLS-2$		
		wlSeqButton=new Label(gTechGroup, SWT.LEFT);
		wlSeqButton.setText(Messages.getString("DimensionLookupDialog.Sequence.Label")); //$NON-NLS-1$
 		props.setLook(wlSeqButton); 	
 		GridData gdlSeqButton=new GridData();
		wlSeqButton.setLayoutData(gdlSeqButton);

		wSeq=new Text(gTechGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wSeq);
		wSeq.addModifyListener(lsMod);
		GridData gdSeq=new GridData(GridData.FILL_HORIZONTAL);
		wSeq.setLayoutData(gdSeq);
		wSeq.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent arg0) {
				input.setTechKeyCreation(DimensionLookupMeta.CREATION_METHOD_SEQUENCE);
				wSeqButton.setSelection(true);
				wAutoinc.setSelection(false);
				wTableMax.setSelection(false);				
			}

			public void focusLost(FocusEvent arg0) {
			} 
		});		
		
		// Use an autoincrement field?
		wAutoinc=new Button(gTechGroup, SWT.RADIO);
 		props.setLook(wAutoinc);
 		wAutoinc.setSelection(false);
 		GridData gdAutoinc=new GridData();
		wAutoinc.setLayoutData(gdAutoinc);
		wAutoinc.setToolTipText(Messages.getString("DimensionLookupDialog.AutoincButton.Tooltip",Const.CR)); //$NON-NLS-1$ //$NON-NLS-2$
		wlAutoinc=new Label(gTechGroup, SWT.LEFT);
		wlAutoinc.setText(Messages.getString("DimensionLookupDialog.Autoincrement.Label")); //$NON-NLS-1$
 		props.setLook(wlAutoinc);
 		GridData gdlAutoinc=new GridData();
		wlAutoinc.setLayoutData(gdlAutoinc);

		setTableMax(); 
		setSequence();
		setAutoincUse();		
		
		// Technical key field:
		wlTk=new Label(shell, SWT.RIGHT);
		wlTk.setText(Messages.getString("DimensionLookupDialog.TechnicalKeyField.Label")); //$NON-NLS-1$
 		props.setLook(wlTk);
		FormData fdlTk=new FormData();
		fdlTk.left = new FormAttachment(0, 0);
		fdlTk.right= new FormAttachment(middle, -margin);
		fdlTk.bottom  = new FormAttachment(gTechGroup, -margin);
		
		wlTk.setLayoutData(fdlTk);
		wTk=new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wTk);
		wTk.addModifyListener(lsMod);
		FormData fdTk=new FormData();
		fdTk.left = new FormAttachment(middle, 0);
		//fdTk.top  = new FormAttachment(wTabFolder, margin);
		fdTk.bottom  = new FormAttachment(gTechGroup, -margin);		
		fdTk.right= new FormAttachment(50+middle/2, 0);
		wTk.setLayoutData(fdTk);
		wTk.addFocusListener(new FocusListener()
        {
            public void focusLost(org.eclipse.swt.events.FocusEvent e)
            {
            }
        
            public void focusGained(org.eclipse.swt.events.FocusEvent e)
            {
                Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                shell.setCursor(busy);
                getFieldsFromTable();
                shell.setCursor(null);
                busy.dispose();
            }
        }
    );

		wlTkRename.setText(Messages.getString("DimensionLookupDialog.NewName.Label")); //$NON-NLS-1$
 		props.setLook(wlTkRename);
		FormData fdlTkRename=new FormData();
		fdlTkRename.left = new FormAttachment(50+middle/2, margin);
		fdlTkRename.bottom = new FormAttachment(gTechGroup, -margin);
		wlTkRename.setLayoutData(fdlTkRename);
		wTkRename=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wTkRename);
		wTkRename.addModifyListener(lsMod);
		FormData fdTkRename=new FormData();
		fdTkRename.left = new FormAttachment(wlTkRename, margin);
		fdTkRename.bottom  = new FormAttachment(gTechGroup, -margin);
		fdTkRename.right= new FormAttachment(100, 0);
		wTkRename.setLayoutData(fdTkRename);

		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };

		wStepname.addSelectionListener( lsDef );
        wSchema.addSelectionListener( lsDef );
		wTable.addSelectionListener( lsDef );
		wCommit.addSelectionListener( lsDef );
        wCacheSize.addSelectionListener( lsDef );
		wTk.addSelectionListener( lsDef );
		wTkRename.addSelectionListener( lsDef );
		wSeq.addSelectionListener( lsDef );
		wVersion.addSelectionListener( lsDef );
		wDatefield.addSelectionListener( lsDef );
		wFromdate.addSelectionListener( lsDef );
		wMinyear.addSelectionListener( lsDef );
		wTodate.addSelectionListener( lsDef );
		wMaxyear.addSelectionListener( lsDef );

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		wbTable.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					getTableName();
				}
			}
		);

		wTabFolder.setSelection(0);

		// Set the shell size, based upon previous time...
		setSize();

		getData();
		setTableFieldCombo();
		input.setChanged(backupChanged);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}

	public void setFlags()
	{
		ColumnInfo colinf =new ColumnInfo(Messages.getString("DimensionLookupDialog.ColumnInfo.Type"),      ColumnInfo.COLUMN_TYPE_CCOMBO,  //$NON-NLS-1$
			  input.isUpdate()?
				 DimensionLookupMeta.typeDesc:
				 DimensionLookupMeta.typeDescLookup
		);
		wUpIns.setColumnInfo(2, colinf);

		if (input.isUpdate())
		{
			wUpIns.setColumnText(2, Messages.getString("DimensionLookupDialog.UpdateOrInsertFields.ColumnText.SteamFieldToCompare")); //$NON-NLS-1$
			wUpIns.setColumnText(3, Messages.getString("DimensionLookupDialog.UpdateOrInsertFields.ColumnTextTypeOfDimensionUpdate")); //$NON-NLS-1$
			wUpIns.setColumnToolTip(2, Messages.getString("DimensionLookupDialog.UpdateOrInsertFields.ColumnToolTip")+Const.CR+"Punch Through: Kimball Type I"+Const.CR+"Update: Correct error in last version"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		else
		{
			wUpIns.setColumnText(2, Messages.getString("DimensionLookupDialog.UpdateOrInsertFields.ColumnText.NewNameOfOutputField")); //$NON-NLS-1$
			wUpIns.setColumnText(3, Messages.getString("DimensionLookupDialog.UpdateOrInsertFields.ColumnText.TypeOfReturnField")); //$NON-NLS-1$
			wUpIns.setColumnToolTip(2, Messages.getString("DimensionLookupDialog.UpdateOrInsertFields.ColumnToolTip2")); //$NON-NLS-1$
		}
		wUpIns.optWidth(true);

        // In case of lookup: disable commitsize, etc.
		boolean update = wUpdate.getSelection();
        wlCommit.setEnabled( update );
        wCommit.setEnabled( update );
        wlMinyear.setEnabled( update );
        wMinyear.setEnabled( update );
        wlMaxyear.setEnabled( update );
        wMaxyear.setEnabled( update );
        wlMinyear.setEnabled( update );
        wMinyear.setEnabled( update );
        wlVersion.setEnabled( update );
        wVersion.setEnabled( update );
        wlTkRename.setEnabled( !update );
        wTkRename.setEnabled( !update );
        
        wCreate.setEnabled( update );

        // Set the technical creation key fields correct... then disable 
        // depending on update or not. Then reset if we're updating. It makes
        // sure that the disabled options because of database restrictions
        // will always be properly grayed out.
		setAutoincUse();
		setSequence();
		setTableMax();
                
        // Surpisingly we can't disable these fields as they influence the calculation of the "Unknown" key
        // If we have a MySQL database with Auto-increment for example, the "unknown" is 1.
        // If we have a MySQL database with Table-max the "unknown" is 0.
        //
        
        // gTechGroup.setEnabled( update );
        // wlAutoinc.setEnabled( update );
        // wAutoinc.setEnabled( update );        
        // wlTableMax.setEnabled( update );
        // wTableMax.setEnabled( update );
        // wlSeqButton.setEnabled( update );
        // wSeqButton.setEnabled( update );
        // wSeq.setEnabled( update );      
        
        if ( update )
        {
        	setAutoincUse();
        	setSequence();
        	setTableMax();
        }
        
        // The alternative start date
        //
        wAltStartDate.setEnabled(wUseAltStartDate.getSelection());
        int alternative = DimensionLookupMeta.getStartDateAlternative(wAltStartDate.getText());
        wAltStartDateField.setEnabled( alternative==DimensionLookupMeta.START_DATE_ALTERNATIVE_COLUMN_VALUE ); 
        
        // Caching...
        //
        wlPreloadCache.setEnabled(wUseCache.getSelection() && !wUpdate.getSelection());
        wPreloadCache.setEnabled(wUseCache.getSelection() && !wUpdate.getSelection());

        wlCacheSize.setEnabled(wUseCache.getSelection() && !wPreloadCache.getSelection());
        wCacheSize.setEnabled(wUseCache.getSelection() && !wPreloadCache.getSelection());
        
	}
	
	protected void setComboBoxes()
    {
        // Something was changed in the row.
        //
		final Map<String, Integer> fields = new HashMap<String, Integer>();
        
        // Add the currentMeta fields...
        fields.putAll(inputFields);
        
        Set<String> keySet = fields.keySet();
        List<String> entries = new ArrayList<String>(keySet);
        
        String[] fieldNames= (String[]) entries.toArray(new String[entries.size()]);
        Const.sortStrings(fieldNames);
        ciKey[1].setComboValues(fieldNames);
        ciKey[1].setComboValues(fieldNames);
    }
	public void setAutoincUse()
	{
		boolean enable = (ci == null) || ci.supportsAutoinc();
		wlAutoinc.setEnabled(enable);
		wAutoinc.setEnabled(enable);
		if ( enable == false && 
			 wAutoinc.getSelection() == true )
		{
			wAutoinc.setSelection(false);
			wSeqButton.setSelection(false);
			wTableMax.setSelection(true);
		}		
	}

	public void setTableMax()
	{
		wlTableMax.setEnabled(true);
		wTableMax.setEnabled(true);
	}
	
	public void setSequence()
	{
		boolean seq = (ci == null) || ci.supportsSequences();
		wSeq.setEnabled(seq);
		wlSeqButton.setEnabled(seq);
		wSeqButton.setEnabled(seq);
		if ( seq == false && 
			 wSeqButton.getSelection() == true ) 
		{
		    wAutoinc.setSelection(false);
			wSeqButton.setSelection(false);
			wTableMax.setSelection(true);
		}		
	}    	
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */
	public void getData()
	{
		int i;
		if(log.isDebug()) log.logDebug(toString(), Messages.getString("DimensionLookupDialog.Log.GettingKeyInfo")); //$NON-NLS-1$

		if (input.getKeyStream()!=null)
		for (i=0;i<input.getKeyStream().length;i++)
		{
			TableItem item = wKey.table.getItem(i);
			if (input.getKeyLookup()[i]!=null) item.setText(1, input.getKeyLookup()[i]);
			if (input.getKeyStream()[i]!=null) item.setText(2, input.getKeyStream()[i]);
		}

		if (input.getFieldStream()!=null)
		for (i=0;i<input.getFieldStream().length;i++)
		{
			TableItem item = wUpIns.table.getItem(i);
			if (input.getFieldLookup()[i]!=null) item.setText(1, input.getFieldLookup()[i]);
			if (input.getFieldStream()[i]!=null) item.setText(2, input.getFieldStream()[i]);
			item.setText(3, DimensionLookupMeta.getUpdateType(input.isUpdate(), input.getFieldUpdate()[i]) );
		}

		wUpdate.setSelection( input.isUpdate() );

        if (input.getSchemaName()!=null)      wSchema.setText( input.getSchemaName() );
		if (input.getTableName()!=null)       wTable.setText( input.getTableName() );
		if (input.getKeyField()!=null)        wTk.setText(input.getKeyField());
		if (input.getKeyRename()!=null)       wTkRename.setText(input.getKeyRename());

		wAutoinc.setSelection( input.isAutoIncrement() );

		if (input.getVersionField()!=null)    wVersion.setText(input.getVersionField());
		if (input.getSequenceName()!=null)        wSeq.setText(input.getSequenceName());
		if (input.getDatabaseMeta()!=null)   wConnection.setText(input.getDatabaseMeta().getName());
		else if (transMeta.nrDatabases()==1)
		{
			wConnection.setText( transMeta.getDatabase(0).getName() );
		}
		if (input.getDateField()!=null)    wDatefield.setText(input.getDateField());
		if (input.getDateFrom()!=null)     wFromdate.setText(input.getDateFrom());
		if (input.getDateTo()!=null)       wTodate.setText(input.getDateTo());

		String techKeyCreation = input.getTechKeyCreation(); 
		if ( techKeyCreation == null )  {		    
		    // Determine the creation of the technical key for
			// backwards compatibility. Can probably be removed at
			// version 3.x or so (Sven Boden).
		    DatabaseMeta database = input.getDatabaseMeta(); 
		    if ( database == null || ! database.supportsAutoinc() )  
		    {
 			    input.setAutoIncrement(false);			
		    }		
		    wAutoinc.setSelection(input.isAutoIncrement());
		    
		    wSeqButton.setSelection(input.getSequenceName() != null && input.getSequenceName().length() > 0);
		    if ( input.isAutoIncrement() == false && 
			     (input.getSequenceName() == null || input.getSequenceName().length() <= 0) ) 
		    {
 			    wTableMax.setSelection(true); 			    
		    }
		    
			if ( database != null && database.supportsSequences() && 
				 input.getSequenceName() != null) 
			{
				wSeq.setText(input.getSequenceName());
				input.setAutoIncrement(false);
				wTableMax.setSelection(false);
			}
		}
		else
		{
		    // KETTLE post 2.2 version:
			// The "creation" field now determines the behaviour of the
			// key creation.
			if ( DimensionLookupMeta.CREATION_METHOD_AUTOINC.equals(techKeyCreation))  
			{
			    wAutoinc.setSelection(true);
			}
			else if ( ( DimensionLookupMeta.CREATION_METHOD_SEQUENCE.equals(techKeyCreation)) )
			{
				wSeqButton.setSelection(true);
			}
			else // the rest
			{
				wTableMax.setSelection(true);
				input.setTechKeyCreation(DimensionLookupMeta.CREATION_METHOD_TABLEMAX);
			}
			if ( input.getSequenceName() != null )
			{
    	        wSeq.setText(input.getSequenceName());
			}
		}
				
		wCommit.setText(""+input.getCommitSize()); //$NON-NLS-1$
		
		wUseCache.setSelection(input.getCacheSize()>=0);
		wPreloadCache.setSelection(input.isPreloadingCache());
        if (input.getCacheSize()>=0) wCacheSize.setText(""+input.getCacheSize()); //$NON-NLS-1$

		wMinyear.setText(""+input.getMinYear()); //$NON-NLS-1$
		wMaxyear.setText(""+input.getMaxYear()); //$NON-NLS-1$

		wUpIns.removeEmptyRows();
		wUpIns.setRowNums();
		wUpIns.optWidth(true);
		wKey.removeEmptyRows();
		wKey.setRowNums();
		wKey.optWidth(true);

		ci = transMeta.findDatabase(wConnection.getText());

		// The alternative start date...
		//
		wUseAltStartDate.setSelection(input.isUsingStartDateAlternative());
		if (input.isUsingStartDateAlternative()) {
			wAltStartDate.setText(DimensionLookupMeta.getStartDateAlternativeDesc(input.getStartDateAlternative()));
		}
		wAltStartDateField.setText(Const.NVL(input.getStartDateFieldName(), ""));
		
        setFlags();

		wStepname.selectAll();
	}

	private void cancel()
	{
		stepname=null;
		input.setChanged(backupChanged);
		input.setUpdate( backupUpdate );
		input.setAutoIncrement( backupAutoInc );
		dispose();
	}

	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		getInfo(input);

		stepname = wStepname.getText(); // return value

		if (input.getDatabaseMeta()==null)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(Messages.getString("DimensionLookupDialog.InvalidConnection.DialogMessage")); //$NON-NLS-1$
			mb.setText(Messages.getString("DimensionLookupDialog.InvalidConnection.DialogTitle")); //$NON-NLS-1$
			mb.open();
		}

		dispose();
	}

	private void getInfo(DimensionLookupMeta in)
	{
		in.setUpdate( wUpdate.getSelection() );
		
		//Table ktable = wKey.table;
		int nrkeys = wKey.nrNonEmpty();
		int nrfields = wUpIns.nrNonEmpty();

		in.allocate(nrkeys, nrfields);

		log.logDebug(toString(), Messages.getString("DimensionLookupDialog.Log.FoundKeys",String.valueOf(nrkeys))); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i=0;i<nrkeys;i++)
		{
			TableItem item = wKey.getNonEmpty(i);
			in.getKeyLookup()[i] = item.getText(1);
			in.getKeyStream()[i] = item.getText(2);
		}

		if(log.isDebug()) log.logDebug(toString(), Messages.getString("DimensionLookupDialog.Log.FoundFields",String.valueOf(nrfields))); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i=0;i<nrfields;i++)
		{
			TableItem item        = wUpIns.getNonEmpty(i);
			in.getFieldLookup()[i]  = item.getText(1);
			in.getFieldStream()[i]  = item.getText(2);
			in.getFieldUpdate()[i]  = DimensionLookupMeta.getUpdateType(in.isUpdate(), item.getText(3));
		}

        in.setSchemaName( wSchema.getText() );
		in.setTableName( wTable.getText() );
		in.setKeyField( wTk.getText() );
		in.setKeyRename( wTkRename.getText() );
		if ( wAutoinc.getSelection() == true )  
		{
			in.setTechKeyCreation(DimensionLookupMeta.CREATION_METHOD_AUTOINC);
			in.setAutoIncrement( true );   // for downwards compatibility
			in.setSequenceName( null );
		}
		else if ( wSeqButton.getSelection() == true )
		{
			in.setTechKeyCreation(DimensionLookupMeta.CREATION_METHOD_SEQUENCE);
			in.setAutoIncrement(false);
			in.setSequenceName( wSeq.getText() );
		}
		else  // all the rest
		{
			in.setTechKeyCreation(DimensionLookupMeta.CREATION_METHOD_TABLEMAX);
			in.setAutoIncrement( false );
			in.setSequenceName( null );
		}

		in.setAutoIncrement( wAutoinc.getSelection() );

		if (in.getKeyRename()!=null && in.getKeyRename().equalsIgnoreCase(in.getKeyField()))
			in.setKeyRename( null ); // Don't waste space&time if it's the same

		in.setVersionField( wVersion.getText() );
		in.setDatabaseMeta( transMeta.findDatabase(wConnection.getText()) );
		in.setDateField( wDatefield.getText() );
		in.setDateFrom( wFromdate.getText() );
		in.setDateTo( wTodate.getText() );

		in.setCommitSize( Const.toInt(wCommit.getText(), 0) );
		
        
        if (wUseCache.getSelection()) {
        	in.setCacheSize( Const.toInt(wCacheSize.getText(), -1) );
        } else {
        	in.setCacheSize(-1);
        }
        in.setPreloadingCache(wPreloadCache.getSelection());
        if (wPreloadCache.getSelection()) in.setCacheSize(0);
        
		in.setMinYear( Const.toInt(wMinyear.getText(), Const.MIN_YEAR) );
		in.setMaxYear( Const.toInt(wMaxyear.getText(), Const.MAX_YEAR) );
		
		in.setUsingStartDateAlternative( wUseAltStartDate.getSelection() );
		in.setStartDateAlternative( DimensionLookupMeta.getStartDateAlternative(wAltStartDate.getText()));
		in.setStartDateFieldName( wAltStartDateField.getText() );
	}

	private void getTableName()
	{
		int connr = wConnection.getSelectionIndex();
        if (connr<0) return;
		DatabaseMeta inf = transMeta.getDatabase(connr);

		log.logDebug(toString(), Messages.getString("DimensionLookupDialog.Log.LookingAtConnection")+inf.toString()); //$NON-NLS-1$

		DatabaseExplorerDialog std = new DatabaseExplorerDialog(shell, SWT.NONE, inf, transMeta.getDatabases());
        std.setSelectedSchema(wSchema.getText());
		std.setSelectedTable(wTable.getText());
        std.setSplitSchemaAndTable(true);
		if (std.open() != null)
		{
            wSchema.setText(Const.NVL(std.getSchemaName(), ""));
			wTable.setText(Const.NVL(std.getTableName(), ""));
		}
	}

	private void get()
	{
		if ( wTabFolder.getSelection() == wFieldsTab )
		{
		    if (input.isUpdate()) getUpdate();
		    else getLookup();
		}
		else
		{
			getKeys();
		}
	}

	/**
	 * Get the fields from the previous step and use them as "update fields". 
	 * Only get the the fields which are not yet in use as key, or in 
	 * the field table. Also ignore technical key, version, fromdate, todate.
	 */
	private void getUpdate()
	{
		try
		{
			RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r!=null && !r.isEmpty())
			{
                BaseStepDialog.getFieldsFromPrevious(r, wUpIns, 2, new int[] {1, 2}, new int[] {}, -1, -1, new TableItemInsertListener()
                    {
                        public boolean tableItemInserted(TableItem tableItem, ValueMetaInterface v)
                        {
                            tableItem.setText(3, Messages.getString("DimensionLookupDialog.TableItem.Insert.Label")); //$NON-NLS-1$
                            
                            int idx = wKey.indexOfString(v.getName(), 2);
                            if ( idx<0 &&
                                !v.getName().equalsIgnoreCase(wTk.getText()) &&
                                !v.getName().equalsIgnoreCase(wVersion.getText()) &&
                                !v.getName().equalsIgnoreCase(wFromdate.getText()) &&
                                !v.getName().equalsIgnoreCase(wTodate.getText())
                                )
                            {
                                return true;
                            }
                            return false;
                        }
                    }
                );
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, Messages.getString("DimensionLookupDialog.FailedToGetFields.DialogTitle"), Messages.getString("DimensionLookupDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	private void setTableFieldCombo(){
		Runnable fieldLoader = new Runnable() {
			public void run() {
				//clear
				for (int i = 0; i < tableFieldColumns.size(); i++) {
					ColumnInfo colInfo = (ColumnInfo) tableFieldColumns.get(i);
					colInfo.setComboValues(new String[] {});
				}
				if (!Const.isEmpty(wTable.getText())) {
					DatabaseMeta ci = transMeta.findDatabase(wConnection.getText());
					if (ci != null) {
						Database db = new Database(ci);
						try {
							db.connect();

							String schemaTable = ci	.getQuotedSchemaTableCombination(transMeta.environmentSubstitute(wSchema
											.getText()), transMeta.environmentSubstitute(wTable.getText()));
							RowMetaInterface r = db.getTableFields(schemaTable);
							if (null != r) {
								String[] fieldNames = r.getFieldNames();
								if (null != fieldNames) {
									for (int i = 0; i < tableFieldColumns.size(); i++) {
										ColumnInfo colInfo = (ColumnInfo) tableFieldColumns.get(i);
										colInfo.setComboValues(fieldNames);
									}
								}
							}
						} catch (Exception e) {
							for (int i = 0; i < tableFieldColumns.size(); i++) {
								ColumnInfo colInfo = (ColumnInfo) tableFieldColumns	.get(i);
								colInfo.setComboValues(new String[] {});
							}
							// ignore any errors here. drop downs will not be
							// filled, but no problem for the user
						}
					}
				}
			}
		};
		shell.getDisplay().asyncExec(fieldLoader);
	}
	/**
	 * Get the fields from the table in the database and use them as lookup 
	 * keys. Only get the the fields which are not yet in use as key, or in 
	 * the field table. Also ignore technical key, version, fromdate, todate.
	 */
	private void getLookup()
	{
		DatabaseMeta databaseMeta = transMeta.findDatabase(wConnection.getText());
		if (databaseMeta!=null)
		{
			Database db = new Database(databaseMeta);
			db.shareVariablesWith(transMeta);
			try
			{
				db.connect();
                String schemaTable = databaseMeta.getQuotedSchemaTableCombination(wSchema.getText(), wTable.getText()); 
				RowMetaInterface r = db.getTableFields(schemaTable);
				if (r!=null && !r.isEmpty())
				{
                    BaseStepDialog.getFieldsFromPrevious(r, wUpIns, 2, new int[] { 1, 2 }, new int[] { 3 }, -1, -1, new TableItemInsertListener()
                        {
                            public boolean tableItemInserted(TableItem tableItem, ValueMetaInterface v)
                            {
                                int idx = wKey.indexOfString(v.getName(), 2);
                                if ( idx<0 &&
                                    !v.getName().equalsIgnoreCase(wTk.getText()) &&
                                    !v.getName().equalsIgnoreCase(wVersion.getText()) &&
                                    !v.getName().equalsIgnoreCase(wFromdate.getText()) &&
                                    !v.getName().equalsIgnoreCase(wTodate.getText())
                                    )
                                {
                                    return true;
                                }
                                return false;
                            }
                        }
                    );
				}
			}
			catch(KettleException e)
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setText(Messages.getString("DimensionLookupDialog.ErrorOccurred.DialogTitle")); //$NON-NLS-1$
				mb.setMessage(Messages.getString("DimensionLookupDialog.ErrorOccurred.DialogMessage")+Const.CR+e.getMessage()); //$NON-NLS-1$
				mb.open();
			}
			finally
			{
				db.disconnect();
			}
		}
	}
	 private void getFields()
	 {
		if(!gotPreviousFields)
		{
		 try{
			 String field=wDatefield.getText();
			 RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			 if(r!=null) 
			 {
				 wDatefield.setItems(r.getFieldNames());
				 
			 }
			 if(field!=null) wDatefield.setText(field);
		 	}catch(KettleException ke){
				new ErrorDialog(shell, Messages.getString("DimensionLookupDialog.ErrorGettingFields.Title"),
						 Messages.getString("DimensionLookupDialog.ErrorGettingFields.Message"), ke);
			}
		 	gotPreviousFields=true;
		}
	 }
	 private void getFieldsFromTable(){
			if(!gotTableFields)
			{
				if (!Const.isEmpty(wTable.getText())) {
					DatabaseMeta ci = transMeta.findDatabase(wConnection.getText());
					if (ci != null) {
						Database db = new Database(ci);
						try {
							db.connect();
							String schemaTable = ci	.getQuotedSchemaTableCombination(transMeta.environmentSubstitute(wSchema.getText()), 
									transMeta.environmentSubstitute(wTable.getText()));
							RowMetaInterface r = db.getTableFields(schemaTable);
							if (null != r) {
								String[] fieldNames = r.getFieldNames();
								if (null != fieldNames) {
									// Version
									String version =wVersion.getText();
									wVersion.setItems(fieldNames);
									if(version!=null) wVersion.setText(version);
									// from date
									String fromdate =wFromdate.getText();
									wFromdate.setItems(fieldNames);
									if(fromdate!=null) wFromdate.setText(fromdate);
									// to date
									String todate =wTodate.getText();
									wTodate.setItems(fieldNames);
									if(todate!=null) wTodate.setText(todate);
									// tk
									String tk =wTk.getText();
									wTk.setItems(fieldNames);
									if(tk!=null) wTk.setText(tk);
									// AltStartDateField
									String sd =wAltStartDateField.getText();
									wAltStartDateField.setItems(fieldNames);
									if(sd!=null) wAltStartDateField.setText(sd);
								}
							}
						} catch (Exception e) {
							
							// ignore any errors here. drop downs will not be
							// filled, but no problem for the user
						}
					}
				}
				gotTableFields=true;
			}
		}
	/**
	 * Get the fields from the previous step and use them as "keys". Only 
	 * get the the fields which are not yet in use as key, or in the field 
	 * table. Also ignore technical key, version, fromdate, todate.
	 */
	private void getKeys()
	{
		try
		{
			RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r!=null && !r.isEmpty())
			{
                BaseStepDialog.getFieldsFromPrevious(r, wKey, 2, new int[] {1, 2}, new int[] { 3 }, -1, -1, new TableItemInsertListener()
                {
                    public boolean tableItemInserted(TableItem tableItem, ValueMetaInterface v)
                    {
                        int idx = wKey.indexOfString(v.getName(), 2);
                        if ( idx<0 &&
                            !v.getName().equalsIgnoreCase(wTk.getText()) &&
                            !v.getName().equalsIgnoreCase(wVersion.getText()) &&
                            !v.getName().equalsIgnoreCase(wFromdate.getText()) &&
                            !v.getName().equalsIgnoreCase(wTodate.getText())
                            )
                        {
                            return true;
                        }
                        return false;
                    }
                }
            );

				Table table=wKey.table;
				for (int i=0;i<r.size();i++)
				{
					ValueMetaInterface v = r.getValueMeta(i);
					int idx = wKey.indexOfString(v.getName(), 2);
					int idy = wUpIns.indexOfString(v.getName(), 2);
					if (idx<0 &&
						idy<0 &&
						!v.getName().equalsIgnoreCase(wTk.getText()) &&
						!v.getName().equalsIgnoreCase(wVersion.getText()) &&
						!v.getName().equalsIgnoreCase(wFromdate.getText()) &&
						!v.getName().equalsIgnoreCase(wTodate.getText())
						)
					{
						TableItem ti = new TableItem(table, SWT.NONE);
						ti.setText(1, v.getName());
						ti.setText(2, v.getName());
						ti.setText(3, v.getTypeDesc());
					}
				}
				wKey.removeEmptyRows();
				wKey.setRowNums();
				wKey.optWidth(true);				
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, Messages.getString("DimensionLookupDialog.FailedToGetFields.DialogTitle"), Messages.getString("DimensionLookupDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}		
	}	
	
	// Generate code for create table...
	// Conversions done by Database
	// For Sybase ASE: don't keep everything in lowercase!
	private void create()
	{
		try
		{
			DimensionLookupMeta info = new DimensionLookupMeta();
			getInfo(info);

			String name = stepname;  // new name might not yet be linked to other steps!
			StepMeta stepinfo = new StepMeta(Messages.getString("DimensionLookupDialog.Stepinfo.Title"), name, info); //$NON-NLS-1$
			RowMetaInterface prev = transMeta.getPrevStepFields(stepname);

            String message = null;
            if (Const.isEmpty(info.getKeyField()))
            {
                message = Messages.getString("DimensionLookupDialog.Error.NoTechnicalKeySpecified");
            }
            if (Const.isEmpty(info.getTableName()))
            {
                message = Messages.getString("DimensionLookupDialog.Error.NoTableNameSpecified");
            }
            
            if (message==null)
            {
    			SQLStatement sql = info.getSQLStatements(transMeta, stepinfo, prev);
    			if (!sql.hasError())
    			{
    				if (sql.hasSQL())
    				{
    					SQLEditor sqledit = new SQLEditor(shell, SWT.NONE, info.getDatabaseMeta(), transMeta.getDbCache(), sql.getSQL());
    					sqledit.open();
    				}
    				else
    				{
    					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
    					mb.setMessage(Messages.getString("DimensionLookupDialog.NoSQLNeeds.DialogMessage")); //$NON-NLS-1$
    					mb.setText(Messages.getString("DimensionLookupDialog.NoSQLNeeds.DialogTitle")); //$NON-NLS-1$
    					mb.open();
    				}
    			}
    			else
    			{
    				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
    				mb.setMessage(sql.getError());
    				mb.setText(Messages.getString("DimensionLookupDialog.SQLError.DialogTitle")); //$NON-NLS-1$
    				mb.open();
    			}
            }
            else
            {
                MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
                mb.setMessage(message);
                mb.setText(Messages.getString("System.Dialog.Error.Title")); //$NON-NLS-1$
                mb.open();
            }
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, Messages.getString("DimensionLookupDialog.UnableToBuildSQLError.DialogMessage"), Messages.getString("DimensionLookupDialog.UnableToBuildSQLError.DialogTitle"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public String toString()
	{
		return this.getClass().getName();
	}
}
