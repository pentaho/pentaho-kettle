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
 * Created on 19-jun-2003
 *
 */

package org.pentaho.di.ui.trans.steps.selectvalues;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.SourceToTargetMapping;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.selectvalues.SelectMetadataChange;
import org.pentaho.di.trans.steps.selectvalues.SelectValuesMeta;
import org.pentaho.di.ui.core.dialog.EnterMappingDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;



/**
 * Dialog for the Select Values step. 
 */
public class SelectValuesDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = SelectValuesMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private CTabFolder   wTabFolder;
	private FormData     fdTabFolder;
	
	private CTabItem     wSelectTab, wRemoveTab, wMetaTab;

	private Composite    wSelectComp, wRemoveComp, wMetaComp;
	private FormData     fdSelectComp, fdRemoveComp, fdMetaComp;
	
	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;
	
	private Label        wlUnspecified;
	private Button       wUnspecified;
	private FormData     fdlUnspecified, fdUnspecified;
	
	private Label        wlRemove;
	private TableView    wRemove;
	private FormData     fdlRemove, fdRemove;

	private Label        wlMeta;
	private TableView    wMeta;
	private FormData     fdlMeta, fdMeta;
	
	private Button       wGetSelect, wGetRemove, wGetMeta, wDoMapping;
	private FormData     fdGetSelect, fdGetRemove, fdGetMeta;

	private SelectValuesMeta input;
	
	private List<ColumnInfo> fieldColumns = new ArrayList<ColumnInfo>();
	
	private String[] charsets = null;
	
	/**
	 * Fields from previous step
	 */
	private RowMetaInterface prevFields;

	/**
	 * Previous fields are read asynchonous because this might take some time
	 * and the user is able to do other things, where he will not need the previous fields
	 */
	private boolean bPreviousFieldsLoaded = false;
	
    private Map<String, Integer> inputFields;
	
	public SelectValuesDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(SelectValuesMeta)in;
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
		changed = input.hasChanged();

		lsGet      = new Listener() { public void handleEvent(Event e) { get();    } };

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "SelectValuesDialog.Shell.Label")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "SelectValuesDialog.Stepname.Label")); //$NON-NLS-1$
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

		// The folders!
		wTabFolder = new CTabFolder(shell, SWT.BORDER);
 		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
		
		//////////////////////////
		// START OF SELECT TAB ///
		//////////////////////////
		
		wSelectTab=new CTabItem(wTabFolder, SWT.NONE);
		wSelectTab.setText(BaseMessages.getString(PKG, "SelectValuesDialog.SelectTab.TabItem")); //$NON-NLS-1$
		
		wSelectComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wSelectComp);

		FormLayout selectLayout = new FormLayout();
		selectLayout.marginWidth  = margin;
		selectLayout.marginHeight = margin;
		wSelectComp.setLayout(selectLayout);

		wlUnspecified=new Label(wSelectComp, SWT.RIGHT);
		wlUnspecified.setText(BaseMessages.getString(PKG, "SelectValuesDialog.Unspecified.Label")); //$NON-NLS-1$
 		props.setLook(wlUnspecified);
		fdlUnspecified=new FormData();
		fdlUnspecified.left = new FormAttachment(0, 0);
		fdlUnspecified.right = new FormAttachment(middle, 0);
		fdlUnspecified.bottom = new FormAttachment(100, 0);
		wlUnspecified.setLayoutData(fdlUnspecified);

		wUnspecified=new Button(wSelectComp, SWT.CHECK);
		props.setLook(wUnspecified);
		fdUnspecified=new FormData();
		fdUnspecified.left = new FormAttachment(middle, margin);
		fdUnspecified.right = new FormAttachment(100, 0);
		fdUnspecified.bottom = new FormAttachment(100, 0);
		wUnspecified.setLayoutData(fdUnspecified);

		
		wlFields=new Label(wSelectComp, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "SelectValuesDialog.Fields.Label")); //$NON-NLS-1$
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(0, 0);
		wlFields.setLayoutData(fdlFields);
		
		final int FieldsCols=4;
		final int FieldsRows=input.getSelectName().length;
		
		ColumnInfo[] colinf=new ColumnInfo[FieldsCols];
		colinf[0]=new ColumnInfo(BaseMessages.getString(PKG, "SelectValuesDialog.ColumnInfo.Fieldname"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[]{BaseMessages.getString(PKG, "SelectValuesDialog.ColumnInfo.Loading")},   false ); //$NON-NLS-1$
		colinf[1]=new ColumnInfo(BaseMessages.getString(PKG, "SelectValuesDialog.ColumnInfo.RenameTo"), ColumnInfo.COLUMN_TYPE_TEXT,   false ); //$NON-NLS-1$
		colinf[2]=new ColumnInfo(BaseMessages.getString(PKG, "SelectValuesDialog.ColumnInfo.Length"),    ColumnInfo.COLUMN_TYPE_TEXT,   false ); //$NON-NLS-1$
		colinf[3]=new ColumnInfo(BaseMessages.getString(PKG, "SelectValuesDialog.ColumnInfo.Precision"), ColumnInfo.COLUMN_TYPE_TEXT,   false ); //$NON-NLS-1$

		fieldColumns.add(colinf[0]);
		wFields=new TableView(transMeta, wSelectComp, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
						      colinf, 
						      FieldsRows,  
						      lsMod,
						      props
						      );

		wGetSelect = new Button(wSelectComp, SWT.PUSH);
		wGetSelect.setText(BaseMessages.getString(PKG, "SelectValuesDialog.GetSelect.Button")); //$NON-NLS-1$
		wGetSelect.addListener(SWT.Selection, lsGet);
		fdGetSelect = new FormData();
		fdGetSelect.right = new FormAttachment(100, 0);
		fdGetSelect.top   = new FormAttachment(wlFields, margin);
		wGetSelect.setLayoutData(fdGetSelect);

		wDoMapping = new Button(wSelectComp, SWT.PUSH);
		wDoMapping.setText(BaseMessages.getString(PKG, "SelectValuesDialog.DoMapping.Button")); //$NON-NLS-1$

		wDoMapping.addListener(SWT.Selection, new Listener() { 	public void handleEvent(Event arg0) { generateMappings();}});

		fdGetSelect = new FormData();
		fdGetSelect.right = new FormAttachment(100, 0);
		fdGetSelect.top   = new FormAttachment(wGetSelect, 0);
		wDoMapping.setLayoutData(fdGetSelect);

		fdFields=new FormData();
		fdFields.left = new FormAttachment(0, 0);
		fdFields.top  = new FormAttachment(wlFields, margin);
		fdFields.right  = new FormAttachment(wGetSelect, -margin);
		fdFields.bottom = new FormAttachment(wUnspecified, -margin);
		wFields.setLayoutData(fdFields);

		fdSelectComp=new FormData();
		fdSelectComp.left  = new FormAttachment(0, 0);
		fdSelectComp.top   = new FormAttachment(0, 0);
		fdSelectComp.right = new FormAttachment(100, 0);
		fdSelectComp.bottom= new FormAttachment(100, 0);
		wSelectComp.setLayoutData(fdSelectComp);
	
		wSelectComp.layout();
		wSelectTab.setControl(wSelectComp);

		/////////////////////////////////////////////////////////////
		/// END OF SELECT TAB
		/////////////////////////////////////////////////////////////

		/////////////////////////////////////////////////////////////
		// START OF REMOVE TAB 
		/////////////////////////////////////////////////////////////
		wRemoveTab=new CTabItem(wTabFolder, SWT.NONE);
		wRemoveTab.setText(BaseMessages.getString(PKG, "SelectValuesDialog.RemoveTab.TabItem")); //$NON-NLS-1$

		FormLayout contentLayout = new FormLayout ();
		contentLayout.marginWidth  = margin;
		contentLayout.marginHeight = margin;
		
		wRemoveComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wRemoveComp);
		wRemoveComp.setLayout(contentLayout);

		wlRemove=new Label(wRemoveComp, SWT.NONE);
		wlRemove.setText(BaseMessages.getString(PKG, "SelectValuesDialog.Remove.Label")); //$NON-NLS-1$
 		props.setLook(wlRemove);
		fdlRemove=new FormData();
		fdlRemove.left = new FormAttachment(0, 0);
		fdlRemove.top  = new FormAttachment(0, 0);
		wlRemove.setLayoutData(fdlRemove);

		final int RemoveCols=1;
		final int RemoveRows=input.getDeleteName().length;
		
		ColumnInfo[] colrem=new ColumnInfo[RemoveCols];
		colrem[0]=new ColumnInfo(BaseMessages.getString(PKG, "SelectValuesDialog.ColumnInfo.Fieldname"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[]{BaseMessages.getString(PKG, "SelectValuesDialog.ColumnInfo.Loading")},  false ); //$NON-NLS-1$
		fieldColumns.add(colrem[0]);
		wRemove=new TableView(transMeta, wRemoveComp, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
						      colrem, 
						      RemoveRows,  
						      lsMod,
						      props
						      );

		wGetRemove = new Button(wRemoveComp, SWT.PUSH);
		wGetRemove.setText(BaseMessages.getString(PKG, "SelectValuesDialog.GetRemove.Button")); //$NON-NLS-1$
		wGetRemove.addListener(SWT.Selection, lsGet);
		fdGetRemove = new FormData();
		fdGetRemove.right = new FormAttachment(100, 0);
		fdGetRemove.top   = new FormAttachment(50, 0);
		wGetRemove.setLayoutData(fdGetRemove);

		fdRemove=new FormData();
		fdRemove.left = new FormAttachment(0, 0);
		fdRemove.top  = new FormAttachment(wlRemove, margin);
		fdRemove.right  = new FormAttachment(wGetRemove, -margin);
		fdRemove.bottom = new FormAttachment(100, 0);
		wRemove.setLayoutData(fdRemove);
		
		fdRemoveComp = new FormData();
		fdRemoveComp.left  = new FormAttachment(0, 0);
		fdRemoveComp.top   = new FormAttachment(0, 0);
		fdRemoveComp.right = new FormAttachment(100, 0);
		fdRemoveComp.bottom= new FormAttachment(100, 0);
		wRemoveComp.setLayoutData(fdRemoveComp);

		wRemoveComp.layout();
		wRemoveTab.setControl(wRemoveComp);

		/////////////////////////////////////////////////////////////
		/// END OF REMOVE TAB
		/////////////////////////////////////////////////////////////

		
		//////////////////////////
		// START OF META TAB  ///
		//////////////////////////
		
		wMetaTab=new CTabItem(wTabFolder, SWT.NONE);
		wMetaTab.setText(BaseMessages.getString(PKG, "SelectValuesDialog.MetaTab.TabItem")); //$NON-NLS-1$
		
		wMetaComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wMetaComp);

		FormLayout metaLayout = new FormLayout();
		metaLayout.marginWidth  = margin;
		metaLayout.marginHeight = margin;
		wMetaComp.setLayout(metaLayout);
		
		wlMeta=new Label(wMetaComp, SWT.NONE);
		wlMeta.setText(BaseMessages.getString(PKG, "SelectValuesDialog.Meta.Label")); //$NON-NLS-1$
 		props.setLook(wlMeta);
		fdlMeta=new FormData();
		fdlMeta.left = new FormAttachment(0, 0);
		fdlMeta.top  = new FormAttachment(0, 0);
		wlMeta.setLayoutData(fdlMeta);
		
		final int MetaRows=input.getMeta().length;
		
		ColumnInfo[] colmeta=new ColumnInfo[] {
			new ColumnInfo(BaseMessages.getString(PKG, "SelectValuesDialog.ColumnInfo.Fieldname"),     ColumnInfo.COLUMN_TYPE_CCOMBO,   new String[]{BaseMessages.getString(PKG, "SelectValuesDialog.ColumnInfo.Loading")}, false ), //$NON-NLS-1$ //$NON-NLS-2$
			new ColumnInfo(BaseMessages.getString(PKG, "SelectValuesDialog.ColumnInfo.Renameto"),      ColumnInfo.COLUMN_TYPE_TEXT,     false ), //$NON-NLS-1$
			new ColumnInfo(BaseMessages.getString(PKG, "SelectValuesDialog.ColumnInfo.Type"),          ColumnInfo.COLUMN_TYPE_CCOMBO,   ValueMeta.getAllTypes(), false), //$NON-NLS-1$
			new ColumnInfo(BaseMessages.getString(PKG, "SelectValuesDialog.ColumnInfo.Length"),        ColumnInfo.COLUMN_TYPE_TEXT,     false ), //$NON-NLS-1$
			new ColumnInfo(BaseMessages.getString(PKG, "SelectValuesDialog.ColumnInfo.Precision"),     ColumnInfo.COLUMN_TYPE_TEXT,     false ), //$NON-NLS-1$
			new ColumnInfo(BaseMessages.getString(PKG, "SelectValuesDialog.ColumnInfo.Storage.Label"), ColumnInfo.COLUMN_TYPE_CCOMBO,   new String[] {BaseMessages.getString(PKG, "System.Combo.Yes"), BaseMessages.getString(PKG, "System.Combo.No"), } ), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			new ColumnInfo(BaseMessages.getString(PKG, "SelectValuesDialog.ColumnInfo.Format"),        ColumnInfo.COLUMN_TYPE_FORMAT,   3), //$NON-NLS-1$
			new ColumnInfo(BaseMessages.getString(PKG, "SelectValuesDialog.ColumnInfo.DateLenient"),   ColumnInfo.COLUMN_TYPE_CCOMBO,   new String[] {BaseMessages.getString(PKG, "System.Combo.Yes"), BaseMessages.getString(PKG, "System.Combo.No"), } ), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			new ColumnInfo(BaseMessages.getString(PKG, "SelectValuesDialog.ColumnInfo.Encoding"),      ColumnInfo.COLUMN_TYPE_CCOMBO,   getCharsets(), false), //$NON-NLS-1$
			new ColumnInfo(BaseMessages.getString(PKG, "SelectValuesDialog.ColumnInfo.Decimal"),       ColumnInfo.COLUMN_TYPE_TEXT,     false), //$NON-NLS-1$
			new ColumnInfo(BaseMessages.getString(PKG, "SelectValuesDialog.ColumnInfo.Grouping"),      ColumnInfo.COLUMN_TYPE_TEXT,     false), //$NON-NLS-1$
			new ColumnInfo(BaseMessages.getString(PKG, "SelectValuesDialog.ColumnInfo.Currency"),      ColumnInfo.COLUMN_TYPE_TEXT,     false), //$NON-NLS-1$
		};
		colmeta[5].setToolTip(BaseMessages.getString(PKG, "SelectValuesDialog.ColumnInfo.Storage.Tooltip")); //$NON-NLS-1$
		fieldColumns.add(colmeta[0]);
		wMeta=new TableView(transMeta, wMetaComp, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
						      colmeta, 
						      MetaRows,  
						      lsMod,
						      props
						      );

		wGetMeta = new Button(wMetaComp, SWT.PUSH);
		wGetMeta.setText(BaseMessages.getString(PKG, "SelectValuesDialog.GetMeta.Button")); //$NON-NLS-1$
		wGetMeta.addListener(SWT.Selection, lsGet);
		fdGetMeta = new FormData();
		fdGetMeta.right = new FormAttachment(100, 0);
		fdGetMeta.top   = new FormAttachment(50, 0);
		wGetMeta.setLayoutData(fdGetMeta);

		fdMeta=new FormData();
		fdMeta.left = new FormAttachment(0, 0);
		fdMeta.top  = new FormAttachment(wlMeta, margin);
		fdMeta.right  = new FormAttachment(wGetMeta, -margin);
		fdMeta.bottom = new FormAttachment(100, 0);
		wMeta.setLayoutData(fdMeta);

		fdMetaComp=new FormData();
		fdMetaComp.left  = new FormAttachment(0, 0);
		fdMetaComp.top   = new FormAttachment(0, 0);
		fdMetaComp.right = new FormAttachment(100, 0);
		fdMetaComp.bottom= new FormAttachment(100, 0);
		wMetaComp.setLayoutData(fdMetaComp);
	
		wMetaComp.layout();
		wMetaTab.setControl(wMetaComp);

		/////////////////////////////////////////////////////////////
		/// END OF META TAB
		/////////////////////////////////////////////////////////////

		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(wStepname, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);

		/////////////////////////////////////////////////////////////
		/// END OF TAB FOLDER
		/////////////////////////////////////////////////////////////


		
		
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wTabFolder);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
				
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		
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
                        prevFields=row;
                        // Remember these fields...
                        for (int i=0;i<row.size();i++)
                        {
                            inputFields.put(row.getValueMeta(i).getName(), Integer.valueOf(i));
                        }
                        setComboBoxes();
                    }
                    catch(KettleException e)
                    {
                    	logError(BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"));
                    }
                }
            }
        };
        new Thread(runnable).start();
		
		// Set the shell size, based upon previous time...
		setSize();

		getData();
		input.setChanged(changed);

		setComboValues();

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}

	private void setComboValues() {
		Runnable fieldLoader = new Runnable() {
			public void run() {
				try {
					prevFields = transMeta.getPrevStepFields(stepname);
				} catch (KettleException e) {
					prevFields = new RowMeta();
					String msg = BaseMessages.getString(PKG, "SelectValuesDialog.DoMapping.UnableToFindInput");
					logError(msg);
				}
				String[] prevStepFieldNames = prevFields.getFieldNames();
				Arrays.sort(prevStepFieldNames);
				bPreviousFieldsLoaded = true;
				for (int i = 0; i < fieldColumns.size(); i++) {
					ColumnInfo colInfo = (ColumnInfo) fieldColumns.get(i);
					colInfo.setComboValues(prevStepFieldNames);
				}
			}
		};
		shell.getDisplay().asyncExec(fieldLoader);
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{	
		wTabFolder.setSelection(0); // Default
		
		/*
		 * Select fields
		 */
		if (input.getSelectName()!=null && input.getSelectName().length>0)
		{
			for (int i=0;i<input.getSelectName().length;i++)
			{
				TableItem item = wFields.table.getItem(i);
				if (input.getSelectName()[i]!=null) 
					item.setText(1, input.getSelectName()     [i]);
				if (input.getSelectRename()[i]!=null && !input.getSelectRename()[i].equals(input.getSelectName()[i]))
					item.setText(2, input.getSelectRename()   [i]);
				item.setText(3, input.getSelectLength()   [i]<0?"":""+input.getSelectLength()   [i]); //$NON-NLS-1$ //$NON-NLS-2$
				item.setText(4, input.getSelectPrecision()[i]<0?"":""+input.getSelectPrecision()[i]); //$NON-NLS-1$ //$NON-NLS-2$
			}
			wFields.setRowNums();
			wFields.optWidth(true);
			wTabFolder.setSelection(0);
		}
		wUnspecified.setSelection( input.isSelectingAndSortingUnspecifiedFields() );
		
		/*
		 * Remove certain fields...
		 */
		if (input.getDeleteName()!=null && input.getDeleteName().length>0) 
		{
			for (int i=0;i<input.getDeleteName().length;i++)
			{
				TableItem item = wRemove.table.getItem(i);
				if (input.getDeleteName()[i]!=null)  item.setText(1, input.getDeleteName()     [i]);
			}
			wRemove.setRowNums();
			wRemove.optWidth(true);
			wTabFolder.setSelection(1);
		}

		/*
		 * Change the meta-data of certain fields
		 */
		if (!Const.isEmpty(input.getMeta()))
		{
			for (int i=0;i<input.getMeta().length;i++)
			{
				SelectMetadataChange change = input.getMeta()[i];
				
				TableItem item = wMeta.table.getItem(i);
				item.setText( 1, Const.NVL(change.getName(), ""));
				if (change.getRename()!=null && !change.getRename().equals(change.getName()))
				{
					item.setText(2, change.getRename());
				}
				item.setText( 3, ValueMeta.getTypeDesc( change.getType()) );
				item.setText( 4, change.getLength()   <0?"":""+change.getLength()); //$NON-NLS-1$ //$NON-NLS-2$
				item.setText( 5, change.getPrecision()<0?"":""+change.getPrecision()); //$NON-NLS-1$ //$NON-NLS-2$
				item.setText( 6, change.getStorageType()==ValueMetaInterface.STORAGE_TYPE_NORMAL?BaseMessages.getString(PKG, "System.Combo.Yes"):BaseMessages.getString(PKG, "System.Combo.No")); //$NON-NLS-1$ //$NON-NLS-2$
				item.setText( 7, Const.NVL(change.getConversionMask(), ""));
				item.setText( 8, change.isDateFormatLenient()?BaseMessages.getString(PKG, "System.Combo.Yes"):BaseMessages.getString(PKG, "System.Combo.No")); //$NON-NLS-1$ //$NON-NLS-2$
				item.setText( 9, Const.NVL(change.getEncoding(), ""));
				item.setText( 10, Const.NVL(change.getDecimalSymbol(), ""));
				item.setText( 11, Const.NVL(change.getGroupingSymbol(), ""));
				item.setText( 12, Const.NVL(change.getCurrencySymbol(), ""));
			}
			wMeta.setRowNums();
			wMeta.optWidth(true);
			wTabFolder.setSelection(2);
		}

		wStepname.setFocus();
		wStepname.selectAll();
	}
	
  private String[] getCharsets()
  {
      if (charsets == null)
      {
          Collection<Charset> charsetCol = Charset.availableCharsets().values();
          charsets = new String[charsetCol.size()];
          int i=0;
          for (Charset charset : charsetCol) {
            charsets[i++] = charset.displayName();
          }
      }
      return charsets;
  }
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		stepname = wStepname.getText(); // return value
		
		// copy info to meta class (input)

		int nrfields = wFields.nrNonEmpty();
		int nrremove = wRemove.nrNonEmpty();
		int nrmeta   = wMeta.nrNonEmpty();
		
		input.allocate(nrfields, nrremove, nrmeta);
		
		for (int i=0;i<nrfields;i++)
		{
			TableItem item = wFields.getNonEmpty(i);
			input.getSelectName()        [i] = item.getText(1);
			input.getSelectRename()      [i] = item.getText(2);
			if (input.getSelectRename()[i]==null || input.getSelectName()[i].length()==0)
				input.getSelectRename()[i] = input.getSelectName()[i];
			input.getSelectLength()      [i] = Const.toInt(item.getText(3), -2);
			input.getSelectPrecision()   [i] = Const.toInt(item.getText(4), -2);
			
			if (input.getSelectLength()   [i]<-2) input.getSelectLength()   [i]=-2;
			if (input.getSelectPrecision()[i]<-2) input.getSelectPrecision()[i]=-2;
		}
		input.setSelectingAndSortingUnspecifiedFields( wUnspecified.getSelection() );

		for (int i=0;i<nrremove;i++)
		{
			TableItem item = wRemove.getNonEmpty(i);
			input.getDeleteName()        [i] = item.getText(1);
		}

		for (int i=0;i<nrmeta;i++)
		{
			SelectMetadataChange change = new SelectMetadataChange(input);
			input.getMeta()[i] = change;
			
			TableItem item = wMeta.getNonEmpty(i);
			
			change.setName(item.getText(1));
			change.setRename(item.getText(2));
			if (Const.isEmpty(change.getRename()))
			{
				change.setRename(change.getName());
			}
			change.setType(ValueMeta.getType(item.getText(3)) );
			
			change.setLength(Const.toInt(item.getText(4), -2));
			change.setPrecision(Const.toInt(item.getText(5), -2));
			
			if (change.getLength()<-2) change.setLength(-2);
			if (change.getPrecision()<-2) change.setPrecision(-2);
			if (BaseMessages.getString(PKG, "System.Combo.Yes").equalsIgnoreCase(item.getText(6))) 
			{
				change.setStorageType(ValueMetaInterface.STORAGE_TYPE_NORMAL);
			}
			
			change.setConversionMask(item.getText(7));
			// If DateFormatLenient is anything but Yes (including blank) then it is false
			change.setDateFormatLenient(item.getText(8).equalsIgnoreCase(BaseMessages.getString(PKG, "System.Combo.Yes"))?true:false);
			change.setEncoding(item.getText(9));
			change.setDecimalSymbol(item.getText(10));
			change.setGroupingSymbol(item.getText(11));
			change.setCurrencySymbol(item.getText(12));
		}
		dispose();
	}

	private void get()
	{
		try
		{
            RowMetaInterface r = transMeta.getPrevStepFields(stepname);
            if (r!=null && !r.isEmpty())
            {
    			switch (wTabFolder.getSelectionIndex())
    			{
    			case 0 : BaseStepDialog.getFieldsFromPrevious(r, wFields, 1, new int[] { 1 }, new int[] {}, -1, -1, null); break;
                case 1 : BaseStepDialog.getFieldsFromPrevious(r, wRemove, 1, new int[] { 1 }, new int[] {}, -1, -1, null); break;
                case 2 : BaseStepDialog.getFieldsFromPrevious(r, wMeta, 1, new int[] { 1 }, new int[] {}, 4, 5, null); break;
    			}
            }
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "SelectValuesDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "SelectValuesDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	/**
	 * Reads in the fields from the previous steps and from the ONE next step and opens an 
	 * EnterMappingDialog with this information. After the user did the mapping, those information 
	 * is put into the Select/Rename table.
	 */
	private void generateMappings() {
		if (!bPreviousFieldsLoaded) {
			MessageDialog.openError(shell, BaseMessages.getString(PKG, "SelectValuesDialog.ColumnInfo.Loading"), BaseMessages.getString(PKG, "SelectValuesDialog.ColumnInfo.Loading"));
			return;
		}
		if ((wRemove.getItemCount() > 0) || (wMeta.getItemCount() > 0)) {
			for (int i = 0; i < wRemove.getItemCount(); i++) {
				String[] columns = wRemove.getItem(i);
				for (int a = 0; a < columns.length; a++) {
					if (columns[a].length() > 0) {
						MessageDialog.openError(shell, 
								BaseMessages.getString(PKG, "SelectValuesDialog.DoMapping.NoDeletOrMetaTitle"),
								BaseMessages.getString(PKG, "SelectValuesDialog.DoMapping.NoDeletOrMeta"));
						return;
					}
				}
			}
			for (int i = 0; i < wMeta.getItemCount(); i++) {
				String[] columns = wMeta.getItem(i);
				for (int a = 0; a < columns.length; a++) {
					String col = columns[a];
					if (col.length() > 0) {
						MessageDialog.openError(shell, 
								BaseMessages.getString(PKG, "SelectValuesDialog.DoMapping.NoDeletOrMetaTitle"),
								BaseMessages.getString(PKG, "SelectValuesDialog.DoMapping.NoDeletOrMeta"));
						return;
					}
				}
			}
		}

		RowMetaInterface nextStepRequiredFields = null;

		StepMeta stepMeta = new StepMeta(stepname, input);
		List<StepMeta> nextSteps = transMeta.findNextSteps(stepMeta);
		if (nextSteps.size()== 0 || nextSteps.size()> 1) {
			MessageDialog
					.openError(shell,
							BaseMessages.getString(PKG, "SelectValuesDialog.DoMapping.NoNextStepTitle"),
							BaseMessages.getString(PKG, "SelectValuesDialog.DoMapping.NoNextStep"));
			return;
		}
		StepMeta outputStepMeta = nextSteps.get(0);
		StepMetaInterface stepMetaInterface = outputStepMeta
				.getStepMetaInterface();
		try {
			nextStepRequiredFields = stepMetaInterface.getRequiredFields(transMeta);
		} catch (KettleException e) {
			logError(BaseMessages.getString(PKG, "SelectValuesDialog.DoMapping.UnableToFindOutput"));
			nextStepRequiredFields = new RowMeta();
		}

		String[] inputNames = new String[prevFields.size()];
		for (int i = 0; i < prevFields.size(); i++) {
			ValueMetaInterface value = prevFields.getValueMeta(i);
			inputNames[i] = value.getName()+
			     EnterMappingDialog.STRING_ORIGIN_SEPARATOR+value.getOrigin()+")";
		}

		String[] outputNames = new String[nextStepRequiredFields.size()];
		for(int i=0; i<nextStepRequiredFields.size(); i++){
			outputNames[i] = nextStepRequiredFields.getValueMeta(i).getName();
		}
		
		String[] selectName = new String[wFields.getItemCount()];
		String[] selectRename = new String[wFields.getItemCount()];
		for (int i = 0; i < wFields.getItemCount(); i++) {
			selectName[i] = wFields.getItem(i, 1);
			selectRename[i] = wFields.getItem(i, 2);
		}
		
		List<SourceToTargetMapping> mappings = new ArrayList<SourceToTargetMapping>();
		StringBuffer missingFields = new StringBuffer();
		for (int i = 0; i < selectName.length; i++) {
			String valueName = selectName[i];
			String valueRename = selectRename[i];
			int inIndex = prevFields.indexOfValue(valueName);
			if (inIndex < 0) {
				missingFields.append(Const.CR + "   " + valueName+" --> " + valueRename);
				continue;
			}
			if (null==valueRename || valueRename.equals("")){
				valueRename = valueName;
			}
			int outIndex = nextStepRequiredFields.indexOfValue(valueRename);
			if (outIndex < 0) {
				missingFields.append(Const.CR + "   " + valueName+" --> " + valueRename);
				continue;
			}
			SourceToTargetMapping mapping = new SourceToTargetMapping(inIndex,
					outIndex);
			mappings.add(mapping);
		}
		// show a confirm dialog if some misconfiguration was found
		if (missingFields.length()>0){
			MessageDialog.setDefaultImage(GUIResource.getInstance().getImageSpoon());
			boolean goOn = MessageDialog.openConfirm(shell,
					BaseMessages.getString(PKG, "SelectValuesDialog.DoMapping.SomeFieldsNotFoundTitle"), 
					BaseMessages.getString(PKG, "SelectValuesDialog.DoMapping.SomeFieldsNotFound", missingFields.toString()));
			if (!goOn) {
				return;
			}
		}
		EnterMappingDialog d = new EnterMappingDialog(SelectValuesDialog.this.shell,inputNames, outputNames, mappings);
		mappings = d.open();

		// mappings == null if the user pressed cancel
		//
		if (mappings!=null) {
			wFields.table.removeAll();
			wFields.table.setItemCount(mappings.size());
			for (int i = 0; i < mappings.size(); i++) {
				SourceToTargetMapping mapping = (SourceToTargetMapping) mappings.get(i);
				TableItem item = wFields.table.getItem(i);
				item.setText(1, prevFields.getValueMeta(mapping.getSourcePosition()).getName());
				item.setText(2, outputNames[mapping.getTargetPosition()]);
			}
			wFields.setRowNums();
			wFields.optWidth(true);
			wTabFolder.setSelection(0);
		}
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

        String fieldNames[] = (String[]) entries.toArray(new String[entries.size()]);

        Const.sortStrings(fieldNames);
        
        bPreviousFieldsLoaded = true;
        for (int i = 0; i < fieldColumns.size(); i++) 
        {
			ColumnInfo colInfo = (ColumnInfo) fieldColumns.get(i);
			colInfo.setComboValues(fieldNames);
		}
    }
}