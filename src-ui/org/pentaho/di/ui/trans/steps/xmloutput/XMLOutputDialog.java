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

package org.pentaho.di.ui.trans.steps.xmloutput;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.xmloutput.Messages;
import org.pentaho.di.trans.steps.xmloutput.XMLField;
import org.pentaho.di.trans.steps.xmloutput.XMLOutputMeta;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;


public class XMLOutputDialog extends BaseStepDialog implements StepDialogInterface
{
	private CTabFolder   wTabFolder;
	private FormData     fdTabFolder;
	
	private CTabItem     wFileTab, wContentTab, wFieldsTab;

	private FormData     fdFileComp, fdContentComp, fdFieldsComp;

	private Label        wlFilename;
	private Button       wbFilename;
	private TextVar      wFilename;
	private FormData     fdlFilename, fdbFilename, fdFilename;

	private Label        wlExtension;
	private Text         wExtension;
	private FormData     fdlExtension, fdExtension;

	private Label        wlAddStepnr;
	private Button       wAddStepnr;
	private FormData     fdlAddStepnr, fdAddStepnr;

	private Label        wlAddDate;
	private Button       wAddDate;
	private FormData     fdlAddDate, fdAddDate;

	private Label        wlAddTime;
	private Button       wAddTime;
	private FormData     fdlAddTime, fdAddTime;

	private Button       wbShowFiles;
	private FormData     fdbShowFiles;

	private Label        wlZipped;
	private Button       wZipped;
	private FormData     fdlZipped, fdZipped;
	
    private Label        wlEncoding;
    private CCombo       wEncoding;
    private FormData     fdlEncoding, fdEncoding;

    private Label        wlMainElement;
    private CCombo       wMainElement;
    private FormData     fdlMainElement, fdMainElement;

    private Label        wlRepeatElement;
    private CCombo       wRepeatElement;
    private FormData     fdlRepeatElement, fdRepeatElement;

	private Label        wlSplitEvery;
	private Text         wSplitEvery;
	private FormData     fdlSplitEvery, fdSplitEvery;

	private TableView    wFields;
	private FormData     fdFields;

	private XMLOutputMeta input;
	
    private Button       wMinWidth;
    private Listener     lsMinWidth;
    private boolean      gotEncodings = false; 
    
	public XMLOutputDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(XMLOutputMeta)in;
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
		shell.setText(Messages.getString("XMLOutputDialog.DialogTitle"));
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("System.Label.StepName"));
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

		wTabFolder = new CTabFolder(shell, SWT.BORDER);
 		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
		
		//////////////////////////
		// START OF FILE TAB///
		///
		wFileTab=new CTabItem(wTabFolder, SWT.NONE);
		wFileTab.setText(Messages.getString("XMLOutputDialog.FileTab.Tab"));
		
		Composite wFileComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wFileComp);

		FormLayout fileLayout = new FormLayout();
		fileLayout.marginWidth  = 3;
		fileLayout.marginHeight = 3;
		wFileComp.setLayout(fileLayout);

		// Filename line
		wlFilename=new Label(wFileComp, SWT.RIGHT);
		wlFilename.setText(Messages.getString("XMLOutputDialog.Filename.Label"));
 		props.setLook(wlFilename);
		fdlFilename=new FormData();
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.top  = new FormAttachment(0, margin);
		fdlFilename.right= new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);

		wbFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbFilename);
		wbFilename.setText(Messages.getString("XMLOutputDialog.Browse.Button"));
		fdbFilename=new FormData();
		fdbFilename.right= new FormAttachment(100, 0);
		fdbFilename.top  = new FormAttachment(0, 0);
		wbFilename.setLayoutData(fdbFilename);

		wFilename=new TextVar(transMeta, wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		fdFilename=new FormData();
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.top  = new FormAttachment(0, margin);
		fdFilename.right= new FormAttachment(wbFilename, -margin);
		wFilename.setLayoutData(fdFilename);

		// Extension line
		wlExtension=new Label(wFileComp, SWT.RIGHT);
		wlExtension.setText(Messages.getString("XMLOutputDialog.Extension.Label"));
 		props.setLook(wlExtension);
		fdlExtension=new FormData();
		fdlExtension.left = new FormAttachment(0, 0);
		fdlExtension.top  = new FormAttachment(wFilename, margin);
		fdlExtension.right= new FormAttachment(middle, -margin);
		wlExtension.setLayoutData(fdlExtension);
		wExtension=new Text(wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wExtension.setText("");
 		props.setLook(wExtension);
		wExtension.addModifyListener(lsMod);
		fdExtension=new FormData();
		fdExtension.left = new FormAttachment(middle, 0);
		fdExtension.top  = new FormAttachment(wFilename, margin);
		fdExtension.right= new FormAttachment(100, 0);
		wExtension.setLayoutData(fdExtension);

		// Create multi-part file?
		wlAddStepnr=new Label(wFileComp, SWT.RIGHT);
		wlAddStepnr.setText(Messages.getString("XMLOutputDialog.AddStepNr.Label"));
 		props.setLook(wlAddStepnr);
		fdlAddStepnr=new FormData();
		fdlAddStepnr.left = new FormAttachment(0, 0);
		fdlAddStepnr.top  = new FormAttachment(wExtension, margin);
		fdlAddStepnr.right= new FormAttachment(middle, -margin);
		wlAddStepnr.setLayoutData(fdlAddStepnr);
		wAddStepnr=new Button(wFileComp, SWT.CHECK);
 		props.setLook(wAddStepnr);
		fdAddStepnr=new FormData();
		fdAddStepnr.left = new FormAttachment(middle, 0);
		fdAddStepnr.top  = new FormAttachment(wExtension, margin);
		fdAddStepnr.right= new FormAttachment(100, 0);
		wAddStepnr.setLayoutData(fdAddStepnr);
		wAddStepnr.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setChanged();
				}
			}
		);

		// Create multi-part file?
		wlAddDate=new Label(wFileComp, SWT.RIGHT);
		wlAddDate.setText(Messages.getString("XMLOutputDialog.AddDate.Label"));
 		props.setLook(wlAddDate);
		fdlAddDate=new FormData();
		fdlAddDate.left = new FormAttachment(0, 0);
		fdlAddDate.top  = new FormAttachment(wAddStepnr, margin);
		fdlAddDate.right= new FormAttachment(middle, -margin);
		wlAddDate.setLayoutData(fdlAddDate);
		wAddDate=new Button(wFileComp, SWT.CHECK);
 		props.setLook(wAddDate);
		fdAddDate=new FormData();
		fdAddDate.left = new FormAttachment(middle, 0);
		fdAddDate.top  = new FormAttachment(wAddStepnr, margin);
		fdAddDate.right= new FormAttachment(100, 0);
		wAddDate.setLayoutData(fdAddDate);
		wAddDate.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setChanged();
				}
			}
		);
		// Create multi-part file?
		wlAddTime=new Label(wFileComp, SWT.RIGHT);
		wlAddTime.setText(Messages.getString("XMLOutputDialog.AddTime.Label"));
 		props.setLook(wlAddTime);
		fdlAddTime=new FormData();
		fdlAddTime.left = new FormAttachment(0, 0);
		fdlAddTime.top  = new FormAttachment(wAddDate, margin);
		fdlAddTime.right= new FormAttachment(middle, -margin);
		wlAddTime.setLayoutData(fdlAddTime);
		wAddTime=new Button(wFileComp, SWT.CHECK);
 		props.setLook(wAddTime);
		fdAddTime=new FormData();
		fdAddTime.left = new FormAttachment(middle, 0);
		fdAddTime.top  = new FormAttachment(wAddDate, margin);
		fdAddTime.right= new FormAttachment(100, 0);
		wAddTime.setLayoutData(fdAddTime);
		wAddTime.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setChanged();
				}
			}
		);

		wbShowFiles=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbShowFiles);
		wbShowFiles.setText(Messages.getString("XMLOutputDialog.ShowFiles.Button"));
		fdbShowFiles=new FormData();
		fdbShowFiles.left = new FormAttachment(middle, 0);
		fdbShowFiles.top  = new FormAttachment(wAddTime, margin*2);
		wbShowFiles.setLayoutData(fdbShowFiles);
		wbShowFiles.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					XMLOutputMeta tfoi = new XMLOutputMeta();
					getInfo(tfoi);
					String files[] = tfoi.getFiles(transMeta);
					if (files!=null && files.length>0)
					{
						EnterSelectionDialog esd = new EnterSelectionDialog(shell, files, Messages.getString("XMLOutputDialog.OutputFiles.DialogTitle"), Messages.getString("XMLOutputDialog.OutputFiles.DialogMessage"));
						esd.setViewOnly();
						esd.open();
					}
					else
					{
						MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
						mb.setMessage(Messages.getString("XMLOutputDialog.NoFilesFound.DialogMessage"));
						mb.setText(Messages.getString("System.Dialog.Error.Title"));
						mb.open(); 
					}
				}
			}
		);


		
		fdFileComp=new FormData();
		fdFileComp.left  = new FormAttachment(0, 0);
		fdFileComp.top   = new FormAttachment(0, 0);
		fdFileComp.right = new FormAttachment(100, 0);
		fdFileComp.bottom= new FormAttachment(100, 0);
		wFileComp.setLayoutData(fdFileComp);
	
		wFileComp.layout();
		wFileTab.setControl(wFileComp);

		/////////////////////////////////////////////////////////////
		/// END OF FILE TAB
		/////////////////////////////////////////////////////////////

		//////////////////////////
		// START OF CONTENT TAB///
		///
		wContentTab=new CTabItem(wTabFolder, SWT.NONE);
		wContentTab.setText(Messages.getString("XMLOutputDialog.ContentTab.TabTitle"));

		FormLayout contentLayout = new FormLayout ();
		contentLayout.marginWidth  = 3;
		contentLayout.marginHeight = 3;
		
		Composite wContentComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wContentComp);
		wContentComp.setLayout(contentLayout);



		wlZipped=new Label(wContentComp, SWT.RIGHT);
		wlZipped.setText(Messages.getString("XMLOutputDialog.Zipped.Label"));
 		props.setLook(wlZipped);
		fdlZipped=new FormData();
		fdlZipped.left = new FormAttachment(0, 0);
		fdlZipped.top  = new FormAttachment(0, 0);
		fdlZipped.right= new FormAttachment(middle, -margin);
		wlZipped.setLayoutData(fdlZipped);
		wZipped=new Button(wContentComp, SWT.CHECK );
 		props.setLook(wZipped);
		fdZipped=new FormData();
		fdZipped.left = new FormAttachment(middle, 0);
		fdZipped.top  = new FormAttachment(0, 0);
		fdZipped.right= new FormAttachment(100, 0);
		wZipped.setLayoutData(fdZipped);
		wZipped.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setChanged();
				}
			}
		);

        wlEncoding=new Label(wContentComp, SWT.RIGHT);
        wlEncoding.setText(Messages.getString("XMLOutputDialog.Encoding.Label"));
        props.setLook(wlEncoding);
        fdlEncoding=new FormData();
        fdlEncoding.left = new FormAttachment(0, 0);
        fdlEncoding.top  = new FormAttachment(wZipped, margin);
        fdlEncoding.right= new FormAttachment(middle, -margin);
        wlEncoding.setLayoutData(fdlEncoding);
        wEncoding=new CCombo(wContentComp, SWT.BORDER | SWT.READ_ONLY);
        wEncoding.setEditable(true);
        props.setLook(wEncoding);
        wEncoding.addModifyListener(lsMod);
        fdEncoding=new FormData();
        fdEncoding.left = new FormAttachment(middle, 0);
        fdEncoding.top  = new FormAttachment(wZipped, margin);
        fdEncoding.right= new FormAttachment(100, 0);
        wEncoding.setLayoutData(fdEncoding);
        wEncoding.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    setEncodings();
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );

        wlMainElement=new Label(wContentComp, SWT.RIGHT);
        wlMainElement.setText(Messages.getString("XMLOutputDialog.MainElement.Label"));
        props.setLook(wlMainElement);
        fdlMainElement=new FormData();
        fdlMainElement.left = new FormAttachment(0, 0);
        fdlMainElement.top  = new FormAttachment(wEncoding, margin);
        fdlMainElement.right= new FormAttachment(middle, -margin);
        wlMainElement.setLayoutData(fdlMainElement);
        wMainElement=new CCombo(wContentComp, SWT.BORDER | SWT.READ_ONLY);
        wMainElement.setEditable(true);
        props.setLook(wMainElement);
        wMainElement.addModifyListener(lsMod);
        fdMainElement=new FormData();
        fdMainElement.left = new FormAttachment(middle, 0);
        fdMainElement.top  = new FormAttachment(wEncoding, margin);
        fdMainElement.right= new FormAttachment(100, 0);
        wMainElement.setLayoutData(fdMainElement);

        wlRepeatElement=new Label(wContentComp, SWT.RIGHT);
        wlRepeatElement.setText(Messages.getString("XMLOutputDialog.RepeatElement.Label"));
        props.setLook(wlRepeatElement);
        fdlRepeatElement=new FormData();
        fdlRepeatElement.left = new FormAttachment(0, 0);
        fdlRepeatElement.top  = new FormAttachment(wMainElement, margin);
        fdlRepeatElement.right= new FormAttachment(middle, -margin);
        wlRepeatElement.setLayoutData(fdlRepeatElement);
        wRepeatElement=new CCombo(wContentComp, SWT.BORDER | SWT.READ_ONLY);
        wRepeatElement.setEditable(true);
        props.setLook(wRepeatElement);
        wRepeatElement.addModifyListener(lsMod);
        fdRepeatElement=new FormData();
        fdRepeatElement.left = new FormAttachment(middle, 0);
        fdRepeatElement.top  = new FormAttachment(wMainElement, margin);
        fdRepeatElement.right= new FormAttachment(100, 0);
        wRepeatElement.setLayoutData(fdRepeatElement);

		wlSplitEvery=new Label(wContentComp, SWT.RIGHT);
		wlSplitEvery.setText(Messages.getString("XMLOutputDialog.SplitEvery.Label"));
 		props.setLook(wlSplitEvery);
		fdlSplitEvery=new FormData();
		fdlSplitEvery.left = new FormAttachment(0, 0);
		fdlSplitEvery.top  = new FormAttachment(wRepeatElement, margin);
		fdlSplitEvery.right= new FormAttachment(middle, -margin);
		wlSplitEvery.setLayoutData(fdlSplitEvery);
		wSplitEvery=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wSplitEvery);
		wSplitEvery.addModifyListener(lsMod);
		fdSplitEvery=new FormData();
		fdSplitEvery.left = new FormAttachment(middle, 0);
		fdSplitEvery.top  = new FormAttachment(wRepeatElement, margin);
		fdSplitEvery.right= new FormAttachment(100, 0);
		wSplitEvery.setLayoutData(fdSplitEvery);

		fdContentComp = new FormData();
		fdContentComp.left  = new FormAttachment(0, 0);
		fdContentComp.top   = new FormAttachment(0, 0);
		fdContentComp.right = new FormAttachment(100, 0);
		fdContentComp.bottom= new FormAttachment(100, 0);
		wContentComp.setLayoutData(fdContentComp);

		wContentComp.layout();
		wContentTab.setControl(wContentComp);

		/////////////////////////////////////////////////////////////
		/// END OF CONTENT TAB
		/////////////////////////////////////////////////////////////

		// Fields tab...
		//
		wFieldsTab = new CTabItem(wTabFolder, SWT.NONE);
		wFieldsTab.setText(Messages.getString("XMLOutputDialog.FieldsTab.TabTitle"));
		
		FormLayout fieldsLayout = new FormLayout ();
		fieldsLayout.marginWidth  = Const.FORM_MARGIN;
		fieldsLayout.marginHeight = Const.FORM_MARGIN;
		
		Composite wFieldsComp = new Composite(wTabFolder, SWT.NONE);
		wFieldsComp.setLayout(fieldsLayout);
 		props.setLook(wFieldsComp);

		wGet=new Button(wFieldsComp, SWT.PUSH);
		wGet.setText(Messages.getString("XMLOutputDialog.Get.Button"));
		wGet.setToolTipText(Messages.getString("XMLOutputDialog.Get.Tooltip"));

		wMinWidth =new Button(wFieldsComp, SWT.PUSH);
		wMinWidth.setText(Messages.getString("XMLOutputDialog.MinWidth.Label"));
		wMinWidth.setToolTipText(Messages.getString("XMLOutputDialog.MinWidth.Tooltip"));

		setButtonPositions(new Button[] { wGet, wMinWidth}, margin, null);

		final int FieldsRows=input.getOutputFields().length;
		
		// Prepare a list of possible formats...
		String dats[] = Const.getDateFormats();
		String nums[] = Const.getNumberFormats();
		int totsize = dats.length + nums.length;
		String formats[] = new String[totsize];
		for (int x=0;x<dats.length;x++) formats[x] = dats[x];
		for (int x=0;x<nums.length;x++) formats[dats.length+x] = nums[x];
		
		ColumnInfo[] colinf=new ColumnInfo[]
          {
    		new ColumnInfo(Messages.getString("XMLOutputDialog.Fieldname.Column"),   ColumnInfo.COLUMN_TYPE_TEXT,   false),
            new ColumnInfo(Messages.getString("XMLOutputDialog.ElementName.Column"), ColumnInfo.COLUMN_TYPE_TEXT,   false),
    		new ColumnInfo(Messages.getString("XMLOutputDialog.Type.Column"),        ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getTypes() ),
    		new ColumnInfo(Messages.getString("XMLOutputDialog.Format.Column"),      ColumnInfo.COLUMN_TYPE_CCOMBO, formats),
    		new ColumnInfo(Messages.getString("XMLOutputDialog.Length.Column"),      ColumnInfo.COLUMN_TYPE_TEXT,   false),
    		new ColumnInfo(Messages.getString("XMLOutputDialog.Precision.Column"),   ColumnInfo.COLUMN_TYPE_TEXT,   false),
    		new ColumnInfo(Messages.getString("XMLOutputDialog.Currency.Column"),    ColumnInfo.COLUMN_TYPE_TEXT,   false),
    		new ColumnInfo(Messages.getString("XMLOutputDialog.Decimal.Column"),     ColumnInfo.COLUMN_TYPE_TEXT,   false),
    		new ColumnInfo(Messages.getString("XMLOutputDialog.Group.Column"),       ColumnInfo.COLUMN_TYPE_TEXT,   false),
    		new ColumnInfo(Messages.getString("XMLOutputDialog.Null.Column"),        ColumnInfo.COLUMN_TYPE_TEXT,   false)
          };
		
		wFields=new TableView(transMeta, wFieldsComp, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
						      colinf, 
						      FieldsRows,  
						      lsMod,
							  props
						      );

		fdFields=new FormData();
		fdFields.left  = new FormAttachment(0, 0);
		fdFields.top   = new FormAttachment(0, 0);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom= new FormAttachment(wGet, -margin);
		wFields.setLayoutData(fdFields);

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
		fdTabFolder.top   = new FormAttachment(wStepname, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);
		
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK"));
		
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel"));

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wTabFolder);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();       } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();      } };
		lsMinWidth    = new Listener() { public void handleEvent(Event e) { setMinimalWidth(); } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();   } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wGet.addListener   (SWT.Selection, lsGet   );
		wMinWidth.addListener (SWT.Selection, lsMinWidth );
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wFilename.addSelectionListener( lsDef );
	
		// Whenever something changes, set the tooltip to the expanded version:
		wFilename.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wFilename.setToolTipText(transMeta.environmentSubstitute( wFilename.getText() ) );
				}
			}
		);
		

		wbFilename.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e) 
				{
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					dialog.setFilterExtensions(new String[] {"*.txt", "*.csv", "*"});
					if (wFilename.getText()!=null)
					{
						dialog.setFileName(transMeta.environmentSubstitute(wFilename.getText()));
					}
					dialog.setFilterNames(new String[] {Messages.getString("System.FileType.TextFiles"), Messages.getString("System.FileType.CSVFiles"), Messages.getString("System.FileType.AllFiles")});
					if (dialog.open()!=null)
					{
						wFilename.setText(dialog.getFilterPath()+System.getProperty("file.separator")+dialog.getFileName());
					}
				}
			}
		);
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		lsResize = new Listener() 
		{
			public void handleEvent(Event event) 
			{
				Point size = shell.getSize();
				wFields.setSize(size.x-10, size.y-50);
				wFields.table.setSize(size.x-10, size.y-50);
				wFields.redraw();
			}
		};
		shell.addListener(SWT.Resize, lsResize);

		wTabFolder.setSelection(0);
		
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
	
    private void setEncodings()
    {
        // Encoding of the text file:
        if (!gotEncodings)
        {
            gotEncodings = true;
            
            wEncoding.removeAll();
            List<Charset> values = new ArrayList<Charset>(Charset.availableCharsets().values());
            for (int i=0;i<values.size();i++)
            {
                Charset charSet = (Charset)values.get(i);
                wEncoding.add( charSet.displayName() );
            }
            
            // Now select the default!
            String defEncoding = Const.getEnvironmentVariable("file.encoding", "UTF-8");
            int idx = Const.indexOfString(defEncoding, wEncoding.getItems() );
            if (idx>=0) wEncoding.select( idx );
        }
    }


    /**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		if (input.getFileName()      != null) wFilename.setText(input.getFileName());
		if (input.getExtension()     != null) wExtension.setText(input.getExtension());
        if (input.getEncoding()      != null) wEncoding.setText(input.getEncoding());
        if (input.getMainElement()   != null) wMainElement.setText(input.getMainElement());
        if (input.getRepeatElement() != null) wRepeatElement.setText(input.getRepeatElement());
        
		wSplitEvery.setText(""+input.getSplitEvery());

		wZipped.setSelection(input.isZipped());
		wAddDate.setSelection(input.isDateInFilename());
		wAddTime.setSelection(input.isTimeInFilename());
		wAddStepnr.setSelection(input.isStepNrInFilename());
		
		log.logDebug(toString(), Messages.getString("XMLOutputDialog.Log.GettingFieldsInfo"));
		
		for (int i=0;i<input.getOutputFields().length;i++)
		{
		    XMLField field = input.getOutputFields()[i];

			TableItem item = wFields.table.getItem(i);
			if (field.getFieldName()!=null) item.setText(1, field.getFieldName());
            if (field.getElementName()!=null) item.setText(2, field.getElementName());
			item.setText(3, field.getTypeDesc());
			if (field.getFormat()!=null) item.setText(4, field.getFormat());
			if (field.getLength()!=-1) item.setText(5, ""+field.getLength());
			if (field.getPrecision()!=-1) item.setText(6, ""+field.getPrecision());
			if (field.getCurrencySymbol()!=null) item.setText(7, field.getCurrencySymbol());
			if (field.getDecimalSymbol()!=null) item.setText(8, field.getDecimalSymbol());
			if (field.getGroupingSymbol()!=null) item.setText(9, field.getGroupingSymbol());
			if (field.getNullString()!=null) item.setText(10, field.getNullString());
		}
		
		wFields.optWidth(true);
		wStepname.selectAll();
	}
	
	private void cancel()
	{
		stepname=null;
		
		input.setChanged(backupChanged);

		dispose();
	}
	
	private void getInfo(XMLOutputMeta tfoi)
	{
		tfoi.setFileName(   wFilename.getText() );
        tfoi.setEncoding( wEncoding.getText() );
        tfoi.setMainElement( wMainElement.getText() );
        tfoi.setRepeatElement( wRepeatElement.getText() );
		tfoi.setExtension(  wExtension.getText() );
		tfoi.setSplitEvery( Const.toInt(wSplitEvery.getText(), 0) );

		tfoi.setStepNrInFilename( wAddStepnr.getSelection() );
		tfoi.setDateInFilename( wAddDate.getSelection() );
		tfoi.setTimeInFilename( wAddTime.getSelection() );
		tfoi.setZipped( wZipped.getSelection() );

		//Table table = wFields.table;
		
		int nrfields = wFields.nrNonEmpty();

		tfoi.allocate(nrfields);
		
		for (int i=0;i<nrfields;i++)
		{
		    XMLField field = new XMLField();
		    
			TableItem item = wFields.getNonEmpty(i);
			field.setFieldName( item.getText(1) );
            field.setElementName( item.getText(2) );
            
            if (field.getFieldName().equals(field.getElementName())) field.setElementName("");
            
			field.setType( item.getText(3) );
			field.setFormat( item.getText(4) );
			field.setLength( Const.toInt(item.getText(5), -1) );
			field.setPrecision( Const.toInt(item.getText(6), -1) );
			field.setCurrencySymbol( item.getText(7) );
			field.setDecimalSymbol( item.getText(8) );
			field.setGroupingSymbol( item.getText(9) );
			field.setNullString( item.getText(10) );
			
			tfoi.getOutputFields()[i]  = field;
		}
	}
	
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		stepname = wStepname.getText(); // return value
		
		getInfo(input);
		
		dispose();
	}
	
	private void get()
	{
		try
		{
			RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r!=null)
			{
                TableItemInsertListener listener = new TableItemInsertListener()
                    {
                        public boolean tableItemInserted(TableItem tableItem, ValueMetaInterface v)
                        {
                            if (v.isNumber())
                            {
                                if (v.getLength()>0)
                                {
                                    int le=v.getLength();
                                    int pr=v.getPrecision();
                                    
                                    if (v.getPrecision()<=0)
                                    {
                                        pr=0;
                                    }
                                    
                                    String mask=" ";
                                    for (int m=0;m<le-pr;m++)
                                    {
                                        mask+="0";
                                    }
                                    if (pr>0) mask+=".";
                                    for (int m=0;m<pr;m++)
                                    {
                                        mask+="0";
                                    }
                                    tableItem.setText(4, mask);
                                }
                            }
                            return true;
                        }
                    };
                BaseStepDialog.getFieldsFromPrevious(r, wFields, 1, new int[] { 1, 2 }, new int[] { 3 }, 5, 6, listener);
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, Messages.getString("System.Dialog.GetFieldsFailed.Title"), Messages.getString("System.Dialog.GetFieldsFailed.Message"), ke);
		}

	}
	
	/**
	 * Sets the output width to minimal width...
	 *
	 */
	public void setMinimalWidth()
	{
    	int nrNonEmptyFields = wFields.nrNonEmpty(); 
		for (int i=0;i<nrNonEmptyFields;i++)
		{
			TableItem item = wFields.getNonEmpty(i);
			
			item.setText(5, "");
			item.setText(6, "");
			
			int type = ValueMeta.getType(item.getText(2));
			switch(type)
			{
			case ValueMeta.TYPE_STRING:  item.setText(4, ""); break;
			case ValueMeta.TYPE_INTEGER: item.setText(4, "0"); break;
			case ValueMeta.TYPE_NUMBER: item.setText(4, "0.#####"); break;
			case ValueMeta.TYPE_DATE: break;
			default: break;
			}
		}
		wFields.optWidth(true);
	}

	public String toString()
	{
		return this.getClass().getName();
	}
}
