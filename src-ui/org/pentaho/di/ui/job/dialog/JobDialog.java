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
 
package org.pentaho.di.ui.job.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.DBCache;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.ChannelLogTable;
import org.pentaho.di.core.logging.JobEntryLogTable;
import org.pentaho.di.core.logging.JobLogTable;
import org.pentaho.di.core.logging.LogStatus;
import org.pentaho.di.core.logging.LogTableField;
import org.pentaho.di.core.logging.LogTableInterface;
import org.pentaho.di.core.parameters.DuplicateParamException;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;
import org.pentaho.di.ui.core.database.dialog.SQLEditor;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.FieldDisabledListener;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.repository.dialog.SelectDirectoryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


/**
 * Allows you to edit the Job settings.  Just pass a JobInfo object.
 * 
 * @author Matt Casters
 * @since  02-jul-2003
 */
public class JobDialog extends Dialog
{
	private static Class<?> PKG = JobDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    public static final int	LOG_INDEX_JOB	       = 0;
    public static final int	LOG_INDEX_JOB_ENTRY	   = 1;
    public static final int	LOG_INDEX_CHANNEL      = 2;

	private CTabFolder   wTabFolder;
	private FormData     fdTabFolder;

	private CTabItem     wJobTab, wParamTab, wLogTab, wSettingsTab;

	private PropsUI      props;
		
	private Label        wlJobname;
	private Text         wJobname;
    private FormData     fdlJobname, fdJobname;

	private Label        wlJobFilename;
	private Text         wJobFilename;
    private FormData     fdlJobFilename, fdJobFilename;

    private Label        wlDirectory;
	private Text         wDirectory;
	private Button       wbDirectory;
    private FormData     fdlDirectory, fdbDirectory, fdDirectory;    

	private Button       wbLogconnection;
	private ComboVar     wLogconnection;

	private TextVar      wLogSchema;

    private Label        wlBatchTrans;
    private Button       wBatchTrans;
    private FormData     fdlBatchTrans, fdBatchTrans;

	private Button wOK, wSQL, wCancel;
	private Listener lsOK, lsSQL, lsCancel;

	private JobMeta jobMeta;
	private Shell  shell;
	private Repository rep;
	
	private SelectionAdapter lsDef;
	
	private ModifyListener lsMod;
	private boolean changed;
    
    private TextVar wSharedObjectsFile;
    private boolean sharedObjectsFileChanged;

    // param tab
	private TableView    wParamFields;

	// Job description
	private Text         wJobdescription;

	// Extended description
	private Label wlExtendeddescription;
	private Text wExtendeddescription;
	private FormData fdlExtendeddescription, fdExtendeddescription;

	// Job Status
	private Label    wlJobstatus;
	private CCombo   wJobstatus;
	private FormData fdlJobstatus, fdJobstatus;

	// Job version
	private Text         wJobversion;

	private int middle;
	private int margin;

	// Job creation
	private Text         wCreateUser;
	private Text         wCreateDate;

	// Job modification
	private Text         wModUser;
	private Text         wModDate;

	private RepositoryDirectoryInterface newDirectory;
	private boolean directoryChangeAllowed;

	private TextVar	wLogSizeLimit;

	private List	wLogTypeList;

	private Composite	wLogOptionsComposite;

	private int	previousLogTableIndex = -1;

	private TableView	wOptionFields;

	private TextVar	wLogTimeout;

	private Composite	wLogComp;
	
	private TextVar	wLogTable;

	private TextVar	wLogInterval;

	private JobLogTable jobLogTable;
	private JobEntryLogTable jobEntryLogTable;
	private ChannelLogTable channelLogTable;
	
	private DatabaseDialog databaseDialog;
  


	public JobDialog(Shell parent, int style, JobMeta jobMeta, Repository rep)
	{
		super(parent, style);
		this.jobMeta=jobMeta;
		this.props=PropsUI.getInstance();
		this.rep=rep;
		
        this.newDirectory = null;
        
		directoryChangeAllowed=true;
		
		jobLogTable = (JobLogTable) jobMeta.getJobLogTable().clone();
		channelLogTable = (ChannelLogTable) jobMeta.getChannelLogTable().clone();
		jobEntryLogTable = (JobEntryLogTable) jobMeta.getJobEntryLogTable().clone();
	}
	

	public JobMeta open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
		props.setLook(shell);
		shell.setImage((Image) GUIResource.getInstance().getImageJobGraph());
		
		lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				changed = true;
			}
		};

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "JobDialog.JobProperties.ShellText"));

		middle = props.getMiddlePct();
		margin = Const.MARGIN;
		
		wTabFolder = new CTabFolder(shell, SWT.BORDER);
		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
		
		addJobTab();
		addParamTab();
		addSettingsTab();
		addLogTab();

		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(0, 0);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);

		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		wSQL=new Button(shell, SWT.PUSH);
		wSQL.setText(BaseMessages.getString(PKG, "System.Button.SQL"));
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

		//BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wSQL, wCancel }, margin, wSharedObjectsFile);
		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wSQL, wCancel }, Const.MARGIN, null);
		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsSQL      = new Listener() { public void handleEvent(Event e) { sql();    } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wSQL.addListener   (SWT.Selection, lsSQL   );
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wJobname.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		wTabFolder.setSelection(0);
		getData();
		BaseStepDialog.setSize(shell);		

		changed=false;
			
		shell.open();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch()) display.sleep();
		}
		return jobMeta;
	}


  private DatabaseDialog getDatabaseDialog(){
    if(databaseDialog != null){
      return databaseDialog;
    }
    databaseDialog = new DatabaseDialog(shell);
    return databaseDialog;
  }
	
	private void addJobTab()
	{
		//////////////////////////
		// START OF JOB TAB///
		///
		wJobTab=new CTabItem(wTabFolder, SWT.NONE);
		wJobTab.setText(BaseMessages.getString(PKG, "JobDialog.JobTab.Label")); //$NON-NLS-1$
        
		Composite wJobComp = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wJobComp);

		FormLayout transLayout = new FormLayout();
		transLayout.marginWidth  = Const.MARGIN;
		transLayout.marginHeight = Const.MARGIN;
		wJobComp.setLayout(transLayout);


		
		// Jobname:
		wlJobname=new Label(wJobComp, SWT.RIGHT);
		wlJobname.setText(BaseMessages.getString(PKG, "JobDialog.JobName.Label"));
		props.setLook(wlJobname);
		fdlJobname=new FormData();
		fdlJobname.left = new FormAttachment(0, 0);
		fdlJobname.right= new FormAttachment(middle, -margin);
		fdlJobname.top  = new FormAttachment(0, margin);
		wlJobname.setLayoutData(fdlJobname);
		wJobname=new Text(wJobComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(		wJobname);
		wJobname.addModifyListener(lsMod);
		fdJobname=new FormData();
		fdJobname.left = new FormAttachment(middle, 0);
		fdJobname.top  = new FormAttachment(0, margin);
		fdJobname.right= new FormAttachment(100, 0);
		wJobname.setLayoutData(fdJobname);

		// JobFilename:
		wlJobFilename=new Label(wJobComp, SWT.RIGHT);
		wlJobFilename.setText(BaseMessages.getString(PKG, "JobDialog.JobFilename.Label"));
		props.setLook(wlJobFilename);
		fdlJobFilename=new FormData();
		fdlJobFilename.left = new FormAttachment(0, 0);
		fdlJobFilename.right= new FormAttachment(middle, -margin);
		fdlJobFilename.top  = new FormAttachment(wJobname, margin);
		wlJobFilename.setLayoutData(fdlJobFilename);
		wJobFilename=new Text(wJobComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(		wJobFilename);
		wJobFilename.addModifyListener(lsMod);
		fdJobFilename=new FormData();
		fdJobFilename.left = new FormAttachment(middle, 0);
		fdJobFilename.top  = new FormAttachment(wJobname, margin);
		fdJobFilename.right= new FormAttachment(100, 0);
		wJobFilename.setLayoutData(fdJobFilename);
		wJobFilename.setEditable(false);
		wJobFilename.setBackground(GUIResource.getInstance().getColorLightGray());

		// Job description:
		Label wlJobdescription = new Label(wJobComp, SWT.RIGHT);
		wlJobdescription.setText(BaseMessages.getString(PKG, "JobDialog.Jobdescription.Label")); //$NON-NLS-1$
		props.setLook(wlJobdescription);
		FormData fdlJobdescription = new FormData();
		fdlJobdescription.left = new FormAttachment(0, 0);
		fdlJobdescription.right= new FormAttachment(middle, -margin);
		fdlJobdescription.top  = new FormAttachment(wJobFilename, margin);
		wlJobdescription.setLayoutData(fdlJobdescription);
		wJobdescription=new Text(wJobComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wJobdescription);
		wJobdescription.addModifyListener(lsMod);
		FormData fdJobdescription = new FormData();
		fdJobdescription.left = new FormAttachment(middle, 0);
		fdJobdescription.top  = new FormAttachment(wJobFilename, margin);
		fdJobdescription.right= new FormAttachment(100, 0);
		wJobdescription.setLayoutData(fdJobdescription);
        

		// Transformation Extended description
		wlExtendeddescription = new Label(wJobComp, SWT.RIGHT);
		wlExtendeddescription.setText(BaseMessages.getString(PKG, "JobDialog.Extendeddescription.Label"));
		props.setLook(wlExtendeddescription);
		fdlExtendeddescription = new FormData();
		fdlExtendeddescription.left = new FormAttachment(0, 0);
		fdlExtendeddescription.top = new FormAttachment(wJobdescription, margin);
		fdlExtendeddescription.right = new FormAttachment(middle, -margin);
		wlExtendeddescription.setLayoutData(fdlExtendeddescription);

		wExtendeddescription = new Text(wJobComp, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		props.setLook(wExtendeddescription,Props.WIDGET_STYLE_FIXED);
		wExtendeddescription.addModifyListener(lsMod);
		fdExtendeddescription = new FormData();
		fdExtendeddescription.left = new FormAttachment(middle, 0);
		fdExtendeddescription.top = new FormAttachment(wJobdescription, margin);
		fdExtendeddescription.right = new FormAttachment(100, 0);
		fdExtendeddescription.bottom =new FormAttachment(50, -margin);
		wExtendeddescription.setLayoutData(fdExtendeddescription);

		//Trans Status
		wlJobstatus = new Label(wJobComp, SWT.RIGHT);
		wlJobstatus.setText(BaseMessages.getString(PKG, "JobDialog.Jobstatus.Label"));
		props.setLook(wlJobstatus);
		fdlJobstatus = new FormData();
		fdlJobstatus.left = new FormAttachment(0, 0);
		fdlJobstatus.right = new FormAttachment(middle, 0);
		fdlJobstatus.top = new FormAttachment(wExtendeddescription, margin*2);
		wlJobstatus.setLayoutData(fdlJobstatus);
		wJobstatus = new CCombo(wJobComp, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		wJobstatus.add(BaseMessages.getString(PKG, "JobDialog.Draft_Jobstatus.Label"));
		wJobstatus.add(BaseMessages.getString(PKG, "JobDialog.Production_Jobstatus.Label"));
		wJobstatus.add("");
		wJobstatus.select(-1); // +1: starts at -1

		props.setLook(wJobstatus);
		fdJobstatus= new FormData();
		fdJobstatus.left = new FormAttachment(middle, 0);
		fdJobstatus.top = new FormAttachment(wExtendeddescription, margin*2);
		fdJobstatus.right = new FormAttachment(100, 0);
		wJobstatus.setLayoutData(fdJobstatus);


		// Job version:
		Label wlJobversion = new Label(wJobComp, SWT.RIGHT);
		wlJobversion.setText(BaseMessages.getString(PKG, "JobDialog.Jobversion.Label")); //$NON-NLS-1$
		props.setLook(wlJobversion);
		FormData fdlJobversion = new FormData();
		fdlJobversion.left = new FormAttachment(0, 0);
		fdlJobversion.right= new FormAttachment(middle, -margin);
		fdlJobversion.top  = new FormAttachment(wJobstatus, margin);
		wlJobversion.setLayoutData(fdlJobversion);
		wJobversion=new Text(wJobComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wJobversion);
		wJobversion.addModifyListener(lsMod);
		FormData fdJobversion = new FormData();
		fdJobversion.left = new FormAttachment(middle, 0);
		fdJobversion.top  = new FormAttachment(wJobstatus, margin);
		fdJobversion.right= new FormAttachment(100, 0);
		wJobversion.setLayoutData(fdJobversion);


		// Directory:
		wlDirectory=new Label(wJobComp, SWT.RIGHT);
		wlDirectory.setText(BaseMessages.getString(PKG, "JobDialog.Directory.Label"));
		props.setLook(wlDirectory);
		fdlDirectory=new FormData();
		fdlDirectory.left = new FormAttachment(0, 0);
		fdlDirectory.right= new FormAttachment(middle, -margin);
		fdlDirectory.top  = new FormAttachment(wJobversion, margin);
		wlDirectory.setLayoutData(fdlDirectory);

		wbDirectory=new Button(wJobComp, SWT.PUSH);
		wbDirectory.setToolTipText(BaseMessages.getString(PKG, "JobDialog.SelectJobFolderFolder.Tooltip"));
		wbDirectory.setImage(GUIResource.getInstance().getImageArrow());
		props.setLook(wbDirectory);
		fdbDirectory=new FormData();
		fdbDirectory.top  = new FormAttachment(wJobversion, 0);
		fdbDirectory.right= new FormAttachment(100, 0);
		wbDirectory.setLayoutData(fdbDirectory);
		wbDirectory.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
			  RepositoryDirectoryInterface directoryFrom = jobMeta.getRepositoryDirectory();
				if (directoryFrom==null) directoryFrom = new RepositoryDirectory();
				ObjectId idDirectoryFrom  = directoryFrom.getObjectId();
				
				SelectDirectoryDialog sdd = new SelectDirectoryDialog(shell, SWT.NONE, rep);
				RepositoryDirectoryInterface rd = sdd.open();
				if (rd!=null)
				{
					if (idDirectoryFrom!=rd.getObjectId())
					{
                        // We need to change this in the repository as well!!
                        // We do this when the user pressed OK
                        newDirectory = rd;
                        wDirectory.setText(rd.getPath());
					}
					else
					{
						// Same directory!
					}
				}
			}
		});

		wDirectory=new Text(wJobComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wDirectory);
		wDirectory.setToolTipText(BaseMessages.getString(PKG, "JobDialog.Directory.Tooltip"));
		wDirectory.setEditable(false);
		wDirectory.setEnabled(false);
		fdDirectory=new FormData();
		fdDirectory.top  = new FormAttachment(wJobversion, margin);
		fdDirectory.left = new FormAttachment(middle, 0);
		fdDirectory.right= new FormAttachment(wbDirectory, 0);
		wDirectory.setLayoutData(fdDirectory);

		// Create User:
		Label wlCreateUser = new Label(wJobComp, SWT.RIGHT);
		wlCreateUser.setText(BaseMessages.getString(PKG, "JobDialog.CreateUser.Label")); //$NON-NLS-1$
		props.setLook(wlCreateUser);
		FormData fdlCreateUser = new FormData();
		fdlCreateUser.left = new FormAttachment(0, 0);
		fdlCreateUser.right= new FormAttachment(middle, -margin);
		fdlCreateUser.top  = new FormAttachment(wDirectory, margin);
		wlCreateUser.setLayoutData(fdlCreateUser);
		wCreateUser=new Text(wJobComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wCreateUser);
		wCreateUser.setEditable(false);
		wCreateUser.addModifyListener(lsMod);
		FormData fdCreateUser = new FormData();
		fdCreateUser.left = new FormAttachment(middle, 0);
		fdCreateUser.top  = new FormAttachment(wDirectory, margin);
		fdCreateUser.right= new FormAttachment(100, 0);
		wCreateUser.setLayoutData(fdCreateUser);

		// Created Date:
		Label wlCreateDate = new Label(wJobComp, SWT.RIGHT);
		wlCreateDate.setText(BaseMessages.getString(PKG, "JobDialog.CreateDate.Label")); //$NON-NLS-1$
		props.setLook(wlCreateDate);
		FormData fdlCreateDate = new FormData();
		fdlCreateDate.left = new FormAttachment(0, 0);
		fdlCreateDate.right= new FormAttachment(middle, -margin);
		fdlCreateDate.top  = new FormAttachment(wCreateUser, margin);
		wlCreateDate.setLayoutData(fdlCreateDate);
		wCreateDate=new Text(wJobComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wCreateDate);
		wCreateDate.setEditable(false);
		wCreateDate.addModifyListener(lsMod);
		FormData fdCreateDate = new FormData();
		fdCreateDate.left = new FormAttachment(middle, 0);
		fdCreateDate.top  = new FormAttachment(wCreateUser, margin);
		fdCreateDate.right= new FormAttachment(100, 0);
		wCreateDate.setLayoutData(fdCreateDate);
    

		// Modified User:
		Label wlModUser = new Label(wJobComp, SWT.RIGHT);
		wlModUser.setText(BaseMessages.getString(PKG, "JobDialog.LastModifiedUser.Label")); //$NON-NLS-1$
		props.setLook(wlModUser);
		FormData fdlModUser = new FormData();
		fdlModUser.left = new FormAttachment(0, 0);
		fdlModUser.right= new FormAttachment(middle, -margin);
		fdlModUser.top  = new FormAttachment(wCreateDate, margin);
		wlModUser.setLayoutData(fdlModUser);
		wModUser=new Text(wJobComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wModUser);
		wModUser.setEditable(false);
		wModUser.addModifyListener(lsMod);
		FormData fdModUser = new FormData();
		fdModUser.left = new FormAttachment(middle, 0);
		fdModUser.top  = new FormAttachment(wCreateDate, margin);
		fdModUser.right= new FormAttachment(100, 0);
		wModUser.setLayoutData(fdModUser);

		// Modified Date:
		Label wlModDate = new Label(wJobComp, SWT.RIGHT);
		wlModDate.setText(BaseMessages.getString(PKG, "JobDialog.LastModifiedDate.Label")); //$NON-NLS-1$
		props.setLook(wlModDate);
		FormData fdlModDate = new FormData();
		fdlModDate.left = new FormAttachment(0, 0);
		fdlModDate.right= new FormAttachment(middle, -margin);
		fdlModDate.top  = new FormAttachment(wModUser, margin);
		wlModDate.setLayoutData(fdlModDate);
		wModDate=new Text(wJobComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wModDate);
		wModDate.setEditable(false);
		wModDate.addModifyListener(lsMod);
		FormData fdModDate = new FormData();
		fdModDate.left = new FormAttachment(middle, 0);
		fdModDate.top  = new FormAttachment(wModUser, margin);
		fdModDate.right= new FormAttachment(100, 0);
		wModDate.setLayoutData(fdModDate);


		FormData fdJobComp = new FormData();
		fdJobComp.left  = new FormAttachment(0, 0);
		fdJobComp.top   = new FormAttachment(0, 0);
		fdJobComp.right = new FormAttachment(100, 0);
		fdJobComp.bottom= new FormAttachment(100, 0);

		wJobComp.setLayoutData(fdJobComp);
		wJobTab.setControl(wJobComp);
        
		/////////////////////////////////////////////////////////////
		/// END OF JOB TAB
		/////////////////////////////////////////////////////////////
	}

    private void addParamTab()
    {
        //////////////////////////
        // START OF PARAM TAB
        ///
        wParamTab=new CTabItem(wTabFolder, SWT.NONE);
        wParamTab.setText(BaseMessages.getString(PKG, "JobDialog.ParamTab.Label")); //$NON-NLS-1$

        FormLayout paramLayout = new FormLayout ();
        paramLayout.marginWidth  = Const.MARGIN;
        paramLayout.marginHeight = Const.MARGIN;
        
        Composite wParamComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wParamComp);
        wParamComp.setLayout(paramLayout);

        Label wlFields = new Label(wParamComp, SWT.RIGHT);
        wlFields.setText(BaseMessages.getString(PKG, "JobDialog.Parameters.Label")); //$NON-NLS-1$
        props.setLook(wlFields);
        FormData fdlFields = new FormData();
        fdlFields.left = new FormAttachment(0, 0);
        fdlFields.top  = new FormAttachment(0, 0);
        wlFields.setLayoutData(fdlFields);
        
        final int FieldsCols=3;
        final int FieldsRows=jobMeta.listParameters().length;
        
        ColumnInfo[] colinf=new ColumnInfo[FieldsCols];
        colinf[0]=new ColumnInfo(BaseMessages.getString(PKG, "JobDialog.ColumnInfo.Parameter.Label"), ColumnInfo.COLUMN_TYPE_TEXT,   false); //$NON-NLS-1$
        colinf[1]=new ColumnInfo(BaseMessages.getString(PKG, "JobDialog.ColumnInfo.Default.Label"),     ColumnInfo.COLUMN_TYPE_TEXT,   false); //$NON-NLS-1$        
        colinf[2]=new ColumnInfo(BaseMessages.getString(PKG, "JobDialog.ColumnInfo.Description.Label"),     ColumnInfo.COLUMN_TYPE_TEXT,   false); //$NON-NLS-1$
        
        wParamFields=new TableView(jobMeta, wParamComp, 
                              SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
                              colinf, 
                              FieldsRows,  
                              lsMod,
                              props
                              );
       
        FormData fdFields = new FormData();
        fdFields.left  = new FormAttachment(0, 0);
        fdFields.top   = new FormAttachment(wlFields, margin);
        fdFields.right = new FormAttachment(100, 0);
        fdFields.bottom= new FormAttachment(100, 0);
        wParamFields.setLayoutData(fdFields);

        FormData fdDepComp = new FormData();
        fdDepComp.left  = new FormAttachment(0, 0);
        fdDepComp.top   = new FormAttachment(0, 0);
        fdDepComp.right = new FormAttachment(100, 0);
        fdDepComp.bottom= new FormAttachment(100, 0);
        wParamComp.setLayoutData(fdDepComp);
        
        wParamComp.layout();
        wParamTab.setControl(wParamComp);

        /////////////////////////////////////////////////////////////
        /// END OF PARAM TAB
        /////////////////////////////////////////////////////////////
    }	
		
    private void addLogTab()
    {
        //////////////////////////
        // START OF LOG TAB///
        ///
        wLogTab=new CTabItem(wTabFolder, SWT.NONE);
        wLogTab.setText(BaseMessages.getString(PKG, "JobDialog.LogTab.Label")); //$NON-NLS-1$

        FormLayout LogLayout = new FormLayout ();
        LogLayout.marginWidth  = Const.MARGIN;
        LogLayout.marginHeight = Const.MARGIN;
        
        wLogComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wLogComp);
        wLogComp.setLayout(LogLayout);
        
        // Add a log type List on the left hand side...
        //
        wLogTypeList = new List(wLogComp, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        props.setLook(wLogTypeList);
        
        wLogTypeList.add(BaseMessages.getString(PKG, "JobDialog.LogTableType.Job")); // Index 0
        wLogTypeList.add(BaseMessages.getString(PKG, "JobDialog.LogTableType.JobEntry")); // Index 1
        wLogTypeList.add(BaseMessages.getString(PKG, "JobDialog.LogTableType.LoggingChannels")); // Index 3
        
        FormData fdLogTypeList = new FormData();
        fdLogTypeList.left = new FormAttachment(0, 0);
        fdLogTypeList.top  = new FormAttachment(0, 0);
        fdLogTypeList.right= new FormAttachment(middle/2, 0);
        fdLogTypeList.bottom= new FormAttachment(100, 0);
        wLogTypeList.setLayoutData(fdLogTypeList);
        
        wLogTypeList.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent arg0) {
        		showLogTypeOptions(wLogTypeList.getSelectionIndex());
        	}
		});
        
        // On the right side we see a dynamic area : a composite...
        //
        wLogOptionsComposite = new Composite(wLogComp, SWT.BORDER);

        FormLayout logOptionsLayout = new FormLayout ();
        logOptionsLayout.marginWidth  = Const.MARGIN;
        logOptionsLayout.marginHeight = Const.MARGIN;
        wLogOptionsComposite.setLayout(logOptionsLayout);
        
        props.setLook(wLogOptionsComposite);
        FormData fdLogOptionsComposite = new FormData();
        fdLogOptionsComposite.left = new FormAttachment(wLogTypeList, margin);
        fdLogOptionsComposite.top  = new FormAttachment(0, 0);
        fdLogOptionsComposite.right= new FormAttachment(100, 0);
        fdLogOptionsComposite.bottom= new FormAttachment(100, 0);
        wLogOptionsComposite.setLayoutData(fdLogOptionsComposite);

        FormData fdLogComp = new FormData();
        fdLogComp.left  = new FormAttachment(0, 0);
        fdLogComp.top   = new FormAttachment(0, 0);
        fdLogComp.right = new FormAttachment(100, 0);
        fdLogComp.bottom= new FormAttachment(100, 0);
        wLogComp.setLayoutData(fdLogComp);
    
        wLogComp.layout();
        wLogTab.setControl(wLogComp);
        
        /////////////////////////////////////////////////////////////
        /// END OF LOG TAB
        /////////////////////////////////////////////////////////////
    }	
    
    private void showLogTypeOptions(int index) {
    	
    	if (index!=previousLogTableIndex) {
    		
    		// Remember the that was entered data...
    		//
			switch(previousLogTableIndex) {
			case LOG_INDEX_JOB       : getJobLogTableOptions(); break;
			case LOG_INDEX_CHANNEL   : getChannelLogTableOptions(); break;
			case LOG_INDEX_JOB_ENTRY : getJobEntryLogTableOptions(); break;
			default: break;
			}
    		
    		// clean the log options composite...
    		//
    		for (Control control : wLogOptionsComposite.getChildren()) {
    			control.dispose();
    		}
	    	
			switch(index) {
			case LOG_INDEX_JOB       : showJobLogTableOptions(); break;
			case LOG_INDEX_CHANNEL   : showChannelLogTableOptions(); break;
			case LOG_INDEX_JOB_ENTRY : showJobEntryLogTableOptions(); break;
			default: break;
			}
    	}
	}

	private void getJobLogTableOptions() {
		
		if (previousLogTableIndex==LOG_INDEX_JOB) {
			// The connection...
			//
			jobLogTable.setConnectionName( wLogconnection.getText() );
			jobLogTable.setSchemaName( wLogSchema.getText() );
			jobLogTable.setTableName( wLogTable.getText() );
			jobLogTable.setLogInterval( wLogInterval.getText() );
			jobLogTable.setLogSizeLimit( wLogSizeLimit.getText() );
			jobLogTable.setTimeoutInDays( wLogTimeout.getText() );
			
			for (int i=0;i<jobLogTable.getFields().size();i++) {
				TableItem item = wOptionFields.table.getItem(i);
				
				LogTableField field = jobLogTable.getFields().get(i);
				field.setEnabled(item.getChecked());
				field.setFieldName(item.getText(1));
			}
		}
	}
	
	private Control addDBSchemaTableLogOptions(LogTableInterface logTable) {

		// Log table connection...
		//
        Label wlLogconnection = new Label(wLogOptionsComposite, SWT.RIGHT);
        wlLogconnection.setText(BaseMessages.getString(PKG, "JobDialog.LogConnection.Label")); //$NON-NLS-1$
        props.setLook(wlLogconnection);
        FormData fdlLogconnection = new FormData();
        fdlLogconnection.left = new FormAttachment(0, 0);
        fdlLogconnection.right= new FormAttachment(middle, -margin);
        fdlLogconnection.top  = new FormAttachment(0, 0);
        wlLogconnection.setLayoutData(fdlLogconnection);

        wbLogconnection=new Button(wLogOptionsComposite, SWT.PUSH);
        wbLogconnection.setText(BaseMessages.getString(PKG, "JobDialog.LogconnectionButton.Label")); //$NON-NLS-1$
        wbLogconnection.addSelectionListener(new SelectionAdapter() 
        {
            public void widgetSelected(SelectionEvent e) 
            {
                DatabaseMeta databaseMeta = new DatabaseMeta();
                databaseMeta.shareVariablesWith(jobMeta);
                
                getDatabaseDialog().setDatabaseMeta(databaseMeta);
                
                if (getDatabaseDialog().open()!=null)
                {
                    jobMeta.addDatabase(getDatabaseDialog().getDatabaseMeta());
                    wLogconnection.add(getDatabaseDialog().getDatabaseMeta().getName());
                    wLogconnection.select(wLogconnection.getItemCount()-1);
                }
            }
        });
        FormData fdbLogconnection = new FormData();
        fdbLogconnection.right= new FormAttachment(100, 0);
        fdbLogconnection.top  = new FormAttachment(0, 0);
        wbLogconnection.setLayoutData(fdbLogconnection);

        wLogconnection=new ComboVar(jobMeta, wLogOptionsComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wLogconnection);
        wLogconnection.addModifyListener(lsMod);
        FormData fdLogconnection = new FormData();
        fdLogconnection.left = new FormAttachment(middle, 0);
        fdLogconnection.top  = new FormAttachment(0, 0);
        fdLogconnection.right= new FormAttachment(wbLogconnection, -margin);
        wLogconnection.setLayoutData(fdLogconnection);
        wLogconnection.setItems(jobMeta.getDatabaseNames());
        wLogconnection.setText(Const.NVL(logTable.getConnectionName(), ""));
        wLogconnection.setToolTipText(BaseMessages.getString(PKG, "JobDialog.LogConnection.Tooltip", logTable.getConnectionNameVariable()));

        // Log schema ...
        //
        Label wlLogSchema = new Label(wLogOptionsComposite, SWT.RIGHT);
        wlLogSchema.setText(BaseMessages.getString(PKG, "JobDialog.LogSchema.Label")); //$NON-NLS-1$
        props.setLook(wlLogSchema);
        FormData fdlLogSchema = new FormData();
        fdlLogSchema.left = new FormAttachment(0, 0);
        fdlLogSchema.right= new FormAttachment(middle, -margin);
        fdlLogSchema.top  = new FormAttachment(wLogconnection, margin);
        wlLogSchema.setLayoutData(fdlLogSchema);
        wLogSchema=new TextVar(jobMeta, wLogOptionsComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wLogSchema);
        wLogSchema.addModifyListener(lsMod);
        FormData fdLogSchema = new FormData();
        fdLogSchema.left = new FormAttachment(middle, 0);
        fdLogSchema.top  = new FormAttachment(wLogconnection, margin);
        fdLogSchema.right= new FormAttachment(100, 0);
        wLogSchema.setLayoutData(fdLogSchema);
        wLogSchema.setText(Const.NVL(logTable.getSchemaName(), ""));
        wLogSchema.setToolTipText(BaseMessages.getString(PKG, "JobDialog.LogSchema.Tooltip", logTable.getSchemaNameVariable()));

        // Log table...
        //
        Label wlLogtable = new Label(wLogOptionsComposite, SWT.RIGHT);
        wlLogtable.setText(BaseMessages.getString(PKG, "JobDialog.Logtable.Label")); //$NON-NLS-1$
        props.setLook(wlLogtable);
        FormData fdlLogtable = new FormData();
        fdlLogtable.left = new FormAttachment(0, 0);
        fdlLogtable.right= new FormAttachment(middle, -margin);
        fdlLogtable.top  = new FormAttachment(wLogSchema, margin);
        wlLogtable.setLayoutData(fdlLogtable);
        wLogTable=new TextVar(jobMeta, wLogOptionsComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wLogTable);
        wLogTable.addModifyListener(lsMod);
        FormData fdLogtable = new FormData();
        fdLogtable.left = new FormAttachment(middle, 0);
        fdLogtable.top  = new FormAttachment(wLogSchema, margin);
        fdLogtable.right= new FormAttachment(100, 0);
        wLogTable.setLayoutData(fdLogtable);
        wLogTable.setText(Const.NVL(logTable.getTableName(), ""));
        wLogTable.setToolTipText(BaseMessages.getString(PKG, "JobDialog.LogTable.Tooltip", logTable.getTableNameVariable()));

        return wLogTable;

	}

	private void showJobLogTableOptions() {
		previousLogTableIndex=LOG_INDEX_JOB;
		
		addDBSchemaTableLogOptions(jobLogTable);
		
        // Log interval...
        //
        Label wlLogInterval = new Label(wLogOptionsComposite, SWT.RIGHT);
        wlLogInterval.setText(BaseMessages.getString(PKG, "JobDialog.LogInterval.Label")); //$NON-NLS-1$
        props.setLook(wlLogInterval);
        FormData fdlLogInterval = new FormData();
        fdlLogInterval.left = new FormAttachment(0, 0);
        fdlLogInterval.right= new FormAttachment(middle, -margin);
        fdlLogInterval.top  = new FormAttachment(wLogTable, margin);
        wlLogInterval.setLayoutData(fdlLogInterval);
        wLogInterval=new TextVar(jobMeta, wLogOptionsComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wLogInterval);
        wLogInterval.addModifyListener(lsMod);
        FormData fdLogInterval = new FormData();
        fdLogInterval.left = new FormAttachment(middle, 0);
        fdLogInterval.top  = new FormAttachment(wLogTable, margin);
        fdLogInterval.right= new FormAttachment(100, 0);
        wLogInterval.setLayoutData(fdLogInterval);
        wLogInterval.setText(Const.NVL(jobLogTable.getLogInterval(), ""));

        // The log timeout in days
        //
        Label wlLogTimeout = new Label(wLogOptionsComposite, SWT.RIGHT);
        wlLogTimeout.setText(BaseMessages.getString(PKG, "JobDialog.LogTimeout.Label")); //$NON-NLS-1$
        wlLogTimeout.setToolTipText(BaseMessages.getString(PKG, "JobDialog.LogTimeout.Tooltip")); //$NON-NLS-1$
        props.setLook(wlLogTimeout);
        FormData fdlLogTimeout = new FormData();
        fdlLogTimeout.left = new FormAttachment(0, 0);
        fdlLogTimeout.right= new FormAttachment(middle, -margin);
        fdlLogTimeout.top  = new FormAttachment(wLogInterval, margin);
        wlLogTimeout.setLayoutData(fdlLogTimeout);
        wLogTimeout=new TextVar(jobMeta, wLogOptionsComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wLogTimeout.setToolTipText(BaseMessages.getString(PKG, "JobDialog.LogTimeout.Tooltip")); //$NON-NLS-1$
        props.setLook(wLogTimeout);
        wLogTimeout.addModifyListener(lsMod);
        FormData fdLogTimeout = new FormData();
        fdLogTimeout.left = new FormAttachment(middle, 0);
        fdLogTimeout.top  = new FormAttachment(wLogInterval, margin);
        fdLogTimeout.right= new FormAttachment(100, 0);
        wLogTimeout.setLayoutData(fdLogTimeout);
        wLogTimeout.setText(Const.NVL(jobLogTable.getTimeoutInDays(), ""));

        // The log size limit
        //
        Label wlLogSizeLimit = new Label(wLogOptionsComposite, SWT.RIGHT);
        wlLogSizeLimit.setText(BaseMessages.getString(PKG, "JobDialog.LogSizeLimit.Label")); //$NON-NLS-1$
        wlLogSizeLimit.setToolTipText(BaseMessages.getString(PKG, "JobDialog.LogSizeLimit.Tooltip")); //$NON-NLS-1$
        props.setLook(wlLogSizeLimit);
        FormData fdlLogSizeLimit = new FormData();
        fdlLogSizeLimit.left = new FormAttachment(0, 0);
        fdlLogSizeLimit.right= new FormAttachment(middle, -margin);
        fdlLogSizeLimit.top  = new FormAttachment(wLogTimeout, margin);
        wlLogSizeLimit.setLayoutData(fdlLogSizeLimit);
        wLogSizeLimit=new TextVar(jobMeta, wLogOptionsComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wLogSizeLimit.setToolTipText(BaseMessages.getString(PKG, "JobDialog.LogSizeLimit.Tooltip")); //$NON-NLS-1$
        props.setLook(wLogSizeLimit);
        wLogSizeLimit.addModifyListener(lsMod);
        FormData fdLogSizeLimit = new FormData();
        fdLogSizeLimit.left = new FormAttachment(middle, 0);
        fdLogSizeLimit.top  = new FormAttachment(wLogTimeout, margin);
        fdLogSizeLimit.right= new FormAttachment(100, 0);
        wLogSizeLimit.setLayoutData(fdLogSizeLimit);
        wLogSizeLimit.setText(Const.NVL(jobLogTable.getLogSizeLimit(), ""));
        
        // Add the fields grid...
        //
		Label wlFields = new Label(wLogOptionsComposite, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "JobDialog.TransLogTable.Fields.Label")); //$NON-NLS-1$
 		props.setLook(wlFields);
		FormData fdlFields = new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(wLogSizeLimit, margin*2);
		wlFields.setLayoutData(fdlFields);
		
		final java.util.List<LogTableField> fields = jobLogTable.getFields();
		final int nrRows=fields.size();
		
		ColumnInfo[] colinf=new ColumnInfo[] {
			new ColumnInfo(BaseMessages.getString(PKG, "JobDialog.TransLogTable.Fields.FieldName"), ColumnInfo.COLUMN_TYPE_TEXT, false ), //$NON-NLS-1$
			new ColumnInfo(BaseMessages.getString(PKG, "JobDialog.TransLogTable.Fields.Description"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
		};
		
		wOptionFields=new TableView(jobMeta, wLogOptionsComposite, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.CHECK, // add a check to the left... 
						      colinf, 
						      nrRows,  
						      true,
						      lsMod,
						      props
						      );      
		
		wOptionFields.setSortable(false);
		
		for (int i=0;i<fields.size();i++) {
			LogTableField field = fields.get(i);
			TableItem item = wOptionFields.table.getItem(i);
			item.setChecked(field.isEnabled());
			item.setText(new String[] { "", Const.NVL(field.getFieldName(), ""), Const.NVL(field.getDescription(), "") });
		}
		
		wOptionFields.table.getColumn(0).setText(BaseMessages.getString(PKG, "JobDialog.TransLogTable.Fields.Enabled"));
        
		FormData fdOptionFields = new FormData();
		fdOptionFields.left = new FormAttachment(0, 0);
		fdOptionFields.top  = new FormAttachment(wlFields, margin);
		fdOptionFields.right  = new FormAttachment(100, 0);
		fdOptionFields.bottom = new FormAttachment(100, 0);
		wOptionFields.setLayoutData(fdOptionFields);
		
		wOptionFields.optWidth(true);

		wOptionFields.layout();
		wLogOptionsComposite.layout(true, true);
		wLogComp.layout(true, true);
	}
	
    
	
	private void getChannelLogTableOptions() {
		
		if (previousLogTableIndex==LOG_INDEX_CHANNEL) {
			// The connection...
			//
			channelLogTable.setConnectionName( wLogconnection.getText() );
			channelLogTable.setSchemaName( wLogSchema.getText() );
			channelLogTable.setTableName( wLogTable.getText() );
			channelLogTable.setTimeoutInDays( wLogTimeout.getText() );
			
			for (int i=0;i<channelLogTable.getFields().size();i++) {
				TableItem item = wOptionFields.table.getItem(i);
				
				LogTableField field = channelLogTable.getFields().get(i);
				field.setEnabled(item.getChecked());
				field.setFieldName(item.getText(1));
			}
		}
	}

	
	private void showChannelLogTableOptions() {
		previousLogTableIndex=LOG_INDEX_CHANNEL;

		addDBSchemaTableLogOptions(channelLogTable);
		
        // The log timeout in days
        //
        Label wlLogTimeout = new Label(wLogOptionsComposite, SWT.RIGHT);
        wlLogTimeout.setText(BaseMessages.getString(PKG, "JobDialog.LogTimeout.Label")); //$NON-NLS-1$
        wlLogTimeout.setToolTipText(BaseMessages.getString(PKG, "JobDialog.LogTimeout.Tooltip")); //$NON-NLS-1$
        props.setLook(wlLogTimeout);
        FormData fdlLogTimeout = new FormData();
        fdlLogTimeout.left = new FormAttachment(0, 0);
        fdlLogTimeout.right= new FormAttachment(middle, -margin);
        fdlLogTimeout.top  = new FormAttachment(wLogTable, margin);
        wlLogTimeout.setLayoutData(fdlLogTimeout);
        wLogTimeout=new TextVar(jobMeta, wLogOptionsComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wLogTimeout.setToolTipText(BaseMessages.getString(PKG, "JobDialog.LogTimeout.Tooltip")); //$NON-NLS-1$
        props.setLook(wLogTimeout);
        wLogTimeout.addModifyListener(lsMod);
        FormData fdLogTimeout = new FormData();
        fdLogTimeout.left = new FormAttachment(middle, 0);
        fdLogTimeout.top  = new FormAttachment(wLogTable, margin);
        fdLogTimeout.right= new FormAttachment(100, 0);
        wLogTimeout.setLayoutData(fdLogTimeout);
        wLogTimeout.setText(Const.NVL(channelLogTable.getTimeoutInDays(), ""));
        
        // Add the fields grid...
        //
		Label wlFields = new Label(wLogOptionsComposite, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "JobDialog.TransLogTable.Fields.Label")); //$NON-NLS-1$
 		props.setLook(wlFields);
		FormData fdlFields = new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(wLogTimeout, margin*2);
		wlFields.setLayoutData(fdlFields);
		
		final java.util.List<LogTableField> fields = channelLogTable.getFields();
		final int nrRows=fields.size();
		
		ColumnInfo[] colinf=new ColumnInfo[] {
			new ColumnInfo(BaseMessages.getString(PKG, "JobDialog.TransLogTable.Fields.FieldName"), ColumnInfo.COLUMN_TYPE_TEXT, false ), //$NON-NLS-1$
			new ColumnInfo(BaseMessages.getString(PKG, "JobDialog.TransLogTable.Fields.Description"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
		};
		
		FieldDisabledListener disabledListener = new FieldDisabledListener() {
			
			public boolean isFieldDisabled(int rowNr) {
				if (rowNr>=0 && rowNr<fields.size()) {
					LogTableField field = fields.get(rowNr);
					return field.isSubjectAllowed();
				} else {
					return true;
				}
			}
		};
		
		colinf[1].setDisabledListener(disabledListener);
		
		wOptionFields=new TableView(jobMeta, wLogOptionsComposite, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.CHECK, // add a check to the left... 
						      colinf, 
						      nrRows,  
						      true,
						      lsMod,
						      props
						      );      
		
		wOptionFields.setSortable(false);
		
		for (int i=0;i<fields.size();i++) {
			LogTableField field = fields.get(i);
			TableItem item = wOptionFields.table.getItem(i);
			item.setChecked(field.isEnabled());
			item.setText(new String[] { "", Const.NVL(field.getFieldName(), ""), Const.NVL(field.getDescription(), "") });
		}
		
		wOptionFields.table.getColumn(0).setText(BaseMessages.getString(PKG, "JobDialog.TransLogTable.Fields.Enabled"));
        
		FormData fdOptionFields = new FormData();
		fdOptionFields.left = new FormAttachment(0, 0);
		fdOptionFields.top  = new FormAttachment(wlFields, margin);
		fdOptionFields.right  = new FormAttachment(100, 0);
		fdOptionFields.bottom = new FormAttachment(100, 0);
		wOptionFields.setLayoutData(fdOptionFields);
		
		wOptionFields.optWidth(true);

		wOptionFields.layout();
		wLogOptionsComposite.layout(true, true);
		wLogComp.layout(true, true);
	}

	private void getJobEntryLogTableOptions() {
		
		if (previousLogTableIndex==LOG_INDEX_JOB_ENTRY) {
			// The connection...
			//
			jobEntryLogTable.setConnectionName( wLogconnection.getText() );
			jobEntryLogTable.setSchemaName( wLogSchema.getText() );
			jobEntryLogTable.setTableName( wLogTable.getText() );
			jobEntryLogTable.setTimeoutInDays( wLogTimeout.getText() );
			
			for (int i=0;i<jobEntryLogTable.getFields().size();i++) {
				TableItem item = wOptionFields.table.getItem(i);
				
				LogTableField field = jobEntryLogTable.getFields().get(i);
				field.setEnabled(item.getChecked());
				field.setFieldName(item.getText(1));
			}
		}
	}
	
	private void showJobEntryLogTableOptions() {
		previousLogTableIndex=LOG_INDEX_JOB_ENTRY;

		addDBSchemaTableLogOptions(jobEntryLogTable);
		
        // The log timeout in days
        //
        Label wlLogTimeout = new Label(wLogOptionsComposite, SWT.RIGHT);
        wlLogTimeout.setText(BaseMessages.getString(PKG, "JobDialog.LogTimeout.Label")); //$NON-NLS-1$
        wlLogTimeout.setToolTipText(BaseMessages.getString(PKG, "JobDialog.LogTimeout.Tooltip")); //$NON-NLS-1$
        props.setLook(wlLogTimeout);
        FormData fdlLogTimeout = new FormData();
        fdlLogTimeout.left = new FormAttachment(0, 0);
        fdlLogTimeout.right= new FormAttachment(middle, -margin);
        fdlLogTimeout.top  = new FormAttachment(wLogTable, margin);
        wlLogTimeout.setLayoutData(fdlLogTimeout);
        wLogTimeout=new TextVar(jobMeta, wLogOptionsComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wLogTimeout.setToolTipText(BaseMessages.getString(PKG, "JobDialog.LogTimeout.Tooltip")); //$NON-NLS-1$
        props.setLook(wLogTimeout);
        wLogTimeout.addModifyListener(lsMod);
        FormData fdLogTimeout = new FormData();
        fdLogTimeout.left = new FormAttachment(middle, 0);
        fdLogTimeout.top  = new FormAttachment(wLogTable, margin);
        fdLogTimeout.right= new FormAttachment(100, 0);
        wLogTimeout.setLayoutData(fdLogTimeout);
        wLogTimeout.setText(Const.NVL(jobEntryLogTable.getTimeoutInDays(), ""));
        
        
        // Add the fields grid...
        //
		Label wlFields = new Label(wLogOptionsComposite, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "JobDialog.TransLogTable.Fields.Label")); //$NON-NLS-1$
 		props.setLook(wlFields);
		FormData fdlFields = new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(wLogTimeout, margin*2);
		wlFields.setLayoutData(fdlFields);
		
		final java.util.List<LogTableField> fields = jobEntryLogTable.getFields();
		final int nrRows=fields.size();
		
		ColumnInfo[] colinf=new ColumnInfo[] {
			new ColumnInfo(BaseMessages.getString(PKG, "JobDialog.TransLogTable.Fields.FieldName"), ColumnInfo.COLUMN_TYPE_TEXT, false ), //$NON-NLS-1$
			new ColumnInfo(BaseMessages.getString(PKG, "JobDialog.TransLogTable.Fields.Description"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
		};
		
		FieldDisabledListener disabledListener = new FieldDisabledListener() {
			
			public boolean isFieldDisabled(int rowNr) {
				if (rowNr>=0 && rowNr<fields.size()) {
					LogTableField field = fields.get(rowNr);
					return field.isSubjectAllowed();
				} else {
					return true;
				}
			}
		};
		
		colinf[1].setDisabledListener(disabledListener);
		
		wOptionFields=new TableView(jobMeta, wLogOptionsComposite, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.CHECK, // add a check to the left... 
						      colinf, 
						      nrRows,  
						      true,
						      lsMod,
						      props
						      );      
		
		wOptionFields.setSortable(false);
		
		for (int i=0;i<fields.size();i++) {
			LogTableField field = fields.get(i);
			TableItem item = wOptionFields.table.getItem(i);
			item.setChecked(field.isEnabled());
			item.setText(new String[] { "", Const.NVL(field.getFieldName(), ""), Const.NVL(field.getDescription(), "") });
		}
		
		wOptionFields.table.getColumn(0).setText(BaseMessages.getString(PKG, "JobDialog.TransLogTable.Fields.Enabled"));
        
		FormData fdOptionFields = new FormData();
		fdOptionFields.left = new FormAttachment(0, 0);
		fdOptionFields.top  = new FormAttachment(wlFields, margin);
		fdOptionFields.right  = new FormAttachment(100, 0);
		fdOptionFields.bottom = new FormAttachment(100, 0);
		wOptionFields.setLayoutData(fdOptionFields);
		
		wOptionFields.optWidth(true);

		wOptionFields.layout();
		wLogOptionsComposite.layout(true, true);
		wLogComp.layout(true, true);
	}


    
    
    
    
    
    
    
	private void addSettingsTab()
	{
		//////////////////////////
		// START OF SETTINGS TAB///
		///
		wSettingsTab=new CTabItem(wTabFolder, SWT.NONE);
		wSettingsTab.setText(BaseMessages.getString(PKG, "JobDialog.SettingsTab.Label")); //$NON-NLS-1$

		FormLayout LogLayout = new FormLayout ();
		LogLayout.marginWidth  = Const.MARGIN;
		LogLayout.marginHeight = Const.MARGIN;
        
		Composite wSettingsComp = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wSettingsComp);
		wSettingsComp.setLayout(LogLayout);

		wlBatchTrans=new Label(wSettingsComp, SWT.RIGHT);
		wlBatchTrans.setText(BaseMessages.getString(PKG, "JobDialog.PassBatchID.Label"));
		props.setLook(wlBatchTrans);
		fdlBatchTrans=new FormData();
		fdlBatchTrans.left = new FormAttachment(0, 0);
		fdlBatchTrans.top  = new FormAttachment(0, margin);
		fdlBatchTrans.right= new FormAttachment(middle, -margin);
		wlBatchTrans.setLayoutData(fdlBatchTrans);
		wBatchTrans=new Button(wSettingsComp, SWT.CHECK);
		props.setLook(wBatchTrans);
		wBatchTrans.setToolTipText(BaseMessages.getString(PKG, "JobDialog.PassBatchID.Tooltip"));
		fdBatchTrans=new FormData();
		fdBatchTrans.left = new FormAttachment(middle, 0);
		fdBatchTrans.top  = new FormAttachment(0, margin);
		fdBatchTrans.right= new FormAttachment(100, 0);
		wBatchTrans.setLayoutData(fdBatchTrans);

		// Shared objects file
		Label wlSharedObjectsFile = new Label(wSettingsComp, SWT.RIGHT);
		wlSharedObjectsFile.setText(BaseMessages.getString(PKG, "JobDialog.SharedObjectsFile.Label"));
		props.setLook(wlSharedObjectsFile);
		FormData fdlSharedObjectsFile = new FormData();
		fdlSharedObjectsFile.left = new FormAttachment(0, 0);
		fdlSharedObjectsFile.right= new FormAttachment(middle, -margin);
		fdlSharedObjectsFile.top  = new FormAttachment(wBatchTrans, 4*margin);
		wlSharedObjectsFile.setLayoutData(fdlSharedObjectsFile);
		wSharedObjectsFile=new TextVar(jobMeta, wSettingsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wlSharedObjectsFile.setToolTipText(BaseMessages.getString(PKG, "JobDialog.SharedObjectsFile.Tooltip"));
		wSharedObjectsFile.setToolTipText(BaseMessages.getString(PKG, "JobDialog.SharedObjectsFile.Tooltip"));
		props.setLook(wSharedObjectsFile);
		FormData fdSharedObjectsFile = new FormData();
		fdSharedObjectsFile.left = new FormAttachment(middle, 0);
		fdSharedObjectsFile.top  = new FormAttachment(wBatchTrans, 4*margin);
		fdSharedObjectsFile.right= new FormAttachment(100, 0);
		wSharedObjectsFile.setLayoutData(fdSharedObjectsFile);
		wSharedObjectsFile.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent arg0)
			{
				sharedObjectsFileChanged = true;
			}
		}
		);

		FormData fdLogComp = new FormData();
		fdLogComp.left  = new FormAttachment(0, 0);
		fdLogComp.top   = new FormAttachment(0, 0);
		fdLogComp.right = new FormAttachment(100, 0);
		fdLogComp.bottom= new FormAttachment(100, 0);
		wSettingsComp.setLayoutData(fdLogComp);
		  
		wSettingsComp.layout();
		wSettingsTab.setControl(wSettingsComp);
	        
		/////////////////////////////////////////////////////////////
		/// END OF LOG TAB
		/////////////////////////////////////////////////////////////
	}

	
	public void dispose()
	{
		WindowProperty winprop = new WindowProperty(shell);
		props.setScreen(winprop);
		shell.dispose();
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		wJobname.setText( Const.NVL(jobMeta.getName(), "") );
		wJobFilename.setText( Const.NVL(jobMeta.getFilename(), "") );
		wJobdescription.setText(  Const.NVL(jobMeta.getDescription(), "") );
		wExtendeddescription.setText( Const.NVL(jobMeta.getExtendedDescription(), "") );
		wJobversion.setText( Const.NVL(jobMeta.getJobversion(), "") );
		wJobstatus.select( jobMeta.getJobstatus() -1);
		
		wLogTypeList.select(LOG_INDEX_JOB);
		showJobLogTableOptions();
		
		if (jobMeta.getRepositoryDirectory()!=null)      wDirectory.setText    ( jobMeta.getRepositoryDirectory().getPath() );
		
		if (jobMeta.getCreatedUser()!=null)     wCreateUser.setText          ( jobMeta.getCreatedUser() );
		if (jobMeta.getCreatedDate()!=null && jobMeta.getCreatedDate()!=null)     						   
			wCreateDate.setText          ( jobMeta.getCreatedDate().toString() );

		if (jobMeta.getModifiedUser()!=null)     wModUser.setText          ( jobMeta.getModifiedUser() );
		if (jobMeta.getModifiedDate()!=null && 
			jobMeta.getModifiedDate()!=null	)     						   
				wModDate.setText    ( jobMeta.getModifiedDate().toString() );
	
		wBatchTrans.setSelection(jobMeta.isBatchIdPassed());

		// The named parameters
		String[] parameters = jobMeta.listParameters();
		for (int idx=0;idx<parameters.length;idx++)
		{
			TableItem item = wParamFields.table.getItem(idx);
			
			String description;
			try {
				description = jobMeta.getParameterDescription(parameters[idx]);
			} catch (UnknownParamException e) {
				description = "";
			}
			String defValue;
			try {
				defValue = jobMeta.getParameterDefault(parameters[idx]);
			} catch (UnknownParamException e) {
				defValue = "";
			}
						
			item.setText(1, parameters[idx]);
			item.setText(2, Const.NVL(defValue, ""));
			item.setText(3, Const.NVL(description, ""));
		}        
		wParamFields.setRowNums();
		wParamFields.optWidth(true);
        
        wSharedObjectsFile.setText(Const.NVL(jobMeta.getSharedObjectsFile(), ""));
        sharedObjectsFileChanged=false;
        
        setFlags();
	}
    
    public void setFlags()
    {
        wbDirectory.setEnabled(rep!=null);
        // wDirectory.setEnabled(rep!=null);
        wlDirectory.setEnabled(rep!=null);
        
        // DatabaseMeta dbMeta = jobMeta.findDatabase(wLogconnection.getText());
        // wbLogconnection.setEnabled(dbMeta!=null);
        
        // wlLogSizeLimit.setEnabled(wLogfield.getSelection());
        // wLogSizeLimit.setEnabled(wLogfield.getSelection());
    }
	
	private void cancel()
	{
		props.setScreen(new WindowProperty(shell));
		jobMeta=null;
		dispose();
	}
	
	private void getLogInfo() {
		getJobLogTableOptions();
		getChannelLogTableOptions();
		getJobEntryLogTableOptions();
	}
	
	private void ok()
	{
		getLogInfo();
		
		jobMeta.setJobLogTable(jobLogTable);
		jobMeta.setChannelLogTable(channelLogTable);
		jobMeta.setJobEntryLogTable(jobEntryLogTable);

		jobMeta.setName( wJobname.getText() );
		jobMeta.setDescription(wJobdescription.getText());
		jobMeta.setExtendedDescription(wExtendeddescription.getText()  );
		jobMeta.setJobversion(wJobversion.getText() );
		if ( wJobstatus.getSelectionIndex() != 2 )
		{
			// Saving the index as meta data is in fact pretty bad, but since
			// it was already in ...
		    jobMeta.setJobstatus( wJobstatus.getSelectionIndex() + 1 );
		}
		else
		{
		    jobMeta.setJobstatus( -1  );
		}
		        
		// Clear and add parameters
		jobMeta.eraseParameters();
    	int nrNonEmptyFields = wParamFields.nrNonEmpty(); 
		for (int i=0;i<nrNonEmptyFields;i++)
		{
			TableItem item = wParamFields.getNonEmpty(i);

			try {
				jobMeta.addParameterDefinition(item.getText(1), item.getText(2), item.getText(3));
			} catch (DuplicateParamException e) {
				// Ignore the duplicate parameter.
			}
		}		        
        
        jobMeta.setBatchIdPassed( wBatchTrans.getSelection() );
        jobMeta.setSharedObjectsFile( wSharedObjectsFile.getText() );

        if (newDirectory!=null) 
        {
	        if (directoryChangeAllowed) 
	        {
	          RepositoryDirectoryInterface dirFrom = jobMeta.getRepositoryDirectory();
	
			    try
				{
					ObjectId newId = rep.renameJob(jobMeta.getObjectId(), newDirectory, jobMeta.getName() );
					jobMeta.setObjectId(newId);
					jobMeta.setRepositoryDirectory( newDirectory );
					wDirectory.setText(jobMeta.getRepositoryDirectory().getPath());
				}
				catch(KettleException dbe)
				{
					jobMeta.setRepositoryDirectory( dirFrom );
			 		
					MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					mb.setText(BaseMessages.getString(PKG, "JobDialog.Dialog.ErrorChangingDirectory.Title"));
					mb.setMessage(BaseMessages.getString(PKG, "JobDialog.Dialog.ErrorChangingDirectory.Message"));
					mb.open();
				}
	        }
	        else
	        {
	        	// Just update to the new selected directory...
	        	//
	        	jobMeta.setRepositoryDirectory( newDirectory );
	        }
        }
        
        jobMeta.setChanged( changed || jobMeta.hasChanged());

		dispose();
	}
	
	/** 
	 * Generates code for create table...
	 * Conversions done by Database
	 */
	private void sql()
	{
		getLogInfo();

		try {
	
			for (LogTableInterface logTable : new LogTableInterface[] { jobLogTable, jobEntryLogTable, channelLogTable, } ) {
				if (logTable.getDatabaseMeta()!=null && !Const.isEmpty(logTable.getTableName())) {
					// OK, we have something to work with!
					//
					Database db = null;
					try {
						db = new Database(jobMeta, logTable.getDatabaseMeta());
						db.shareVariablesWith(jobMeta);
						db.connect();
						
						RowMetaInterface fields = logTable.getLogRecord(LogStatus.START, null, null).getRowMeta();
                  String schemaTable = logTable.getDatabaseMeta().getSchemaTableCombination(db.environmentSubstitute(logTable.getSchemaName()), db.environmentSubstitute(logTable.getTableName()));
						String createTable = db.getDDL(schemaTable, fields);
						
						if (!Const.isEmpty(createTable))
						{
							String comments="-- "+logTable.getLogTableType()+Const.CR;
							comments+="--"+Const.CR+Const.CR;
							
							SQLEditor sqledit = new SQLEditor(shell, SWT.NONE, logTable.getDatabaseMeta(), DBCache.getInstance(), comments+createTable);
							sqledit.open();
						}
						else
						{
							MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
							mb.setText(BaseMessages.getString(PKG, "JobDialog.NoSqlNedds.DialogTitle")); //$NON-NLS-1$
							mb.setMessage(logTable.getLogTableType()+Const.CR+Const.CR+BaseMessages.getString(PKG, "JobDialog.NoSqlNedds.DialogMessage")); //$NON-NLS-1$
							mb.open(); 
						}
						
					} finally { 
						if (db!=null) {
							db.disconnect();
						}
					}
				}
			}
		} catch(Exception e) {
			new ErrorDialog(shell, BaseMessages.getString(PKG, "JobDialog.Dialog.ErrorCreatingSQL.Title"),BaseMessages.getString(PKG, "JobDialog.Dialog.ErrorCreatingSQL.Message"), e);
		}
	}
    public boolean isSharedObjectsFileChanged()
    {
        return sharedObjectsFileChanged;
    }

	public String toString()
	{
		return this.getClass().getName();
	}
    
    public static final void setShellImage(Shell shell, JobEntryInterface jobEntryInterface)
    {
        try
        {
            String id = PluginRegistry.getInstance().getPluginId(JobEntryPluginType.class, jobEntryInterface);
            if (id!=null)
            {
                shell.setImage((Image) GUIResource.getInstance().getImagesJobentries().get(id));
            }
        }
        catch(Throwable e)
        {
        }
    }
    
	public void setDirectoryChangeAllowed(boolean directoryChangeAllowed) {
		this.directoryChangeAllowed = directoryChangeAllowed;
	}
}