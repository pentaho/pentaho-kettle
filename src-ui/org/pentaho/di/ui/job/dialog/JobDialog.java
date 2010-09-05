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
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.parameters.DuplicateParamException;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobEntryLoader;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;
import org.pentaho.di.ui.core.database.dialog.SQLEditor;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
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
	private LogWriter    log;

	private CTabFolder   wTabFolder;
	private FormData     fdTabFolder;

	private CTabItem     wJobTab, wParamTab, wLogTab;

	private PropsUI        props;
		
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

	private Label        wlLogconnection;
	private Button       wbLogconnection;
	private CCombo       wLogconnection;
	private FormData     fdlLogconnection, fdbLogconnection, fdLogconnection;

	private Label        wlLogtable;
	private Text         wLogtable;
	private FormData     fdlLogtable, fdLogtable;

    private Label        wlBatch;
    private Button       wBatch;
    private FormData     fdlBatch, fdBatch;

    private Label        wlBatchTrans;
    private Button       wBatchTrans;
    private FormData     fdlBatchTrans, fdBatchTrans;

    private Label        wlLogfield;
    private Button       wLogfield;
    private FormData     fdlLogfield, fdLogfield;

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

	private RepositoryDirectory newDirectory;
	private boolean directoryChangeAllowed;

	private Label	wlLogSizeLimit;

	private TextVar	wLogSizeLimit;

	public JobDialog(Shell parent, int style, JobMeta jobMeta, Repository rep)
	{
		super(parent, style);
		this.log=LogWriter.getInstance();
		this.jobMeta=jobMeta;
		this.props=PropsUI.getInstance();
		this.rep=rep;
		
        this.newDirectory = null;
        
		directoryChangeAllowed=true;
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
		shell.setText(Messages.getString("JobDialog.JobProperties.ShellText"));

		middle = props.getMiddlePct();
		margin = Const.MARGIN;
		
		wTabFolder = new CTabFolder(shell, SWT.BORDER);
		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
		
		addJobTab();
		addParamTab();
		addLogTab();

		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(0, 0);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);

		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString("System.Button.OK"));
		wOK.setText(BaseMessages.getString("System.Button.OK"));
		wSQL=new Button(shell, SWT.PUSH);
		wSQL.setText(BaseMessages.getString("System.Button.SQL"));
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString("System.Button.Cancel"));

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
	
	private void addJobTab()
	{
		//////////////////////////
		// START OF JOB TAB///
		///
		wJobTab=new CTabItem(wTabFolder, SWT.NONE);
		wJobTab.setText(Messages.getString("JobDialog.JobTab.Label")); //$NON-NLS-1$
        
		Composite wJobComp = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wJobComp);

		FormLayout transLayout = new FormLayout();
		transLayout.marginWidth  = Const.MARGIN;
		transLayout.marginHeight = Const.MARGIN;
		wJobComp.setLayout(transLayout);


		
		// Jobname:
		wlJobname=new Label(wJobComp, SWT.RIGHT);
		wlJobname.setText(Messages.getString("JobDialog.JobName.Label"));
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
		wlJobFilename.setText(Messages.getString("JobDialog.JobFilename.Label"));
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
		wJobFilename.setEnabled(false);


		// Job description:
		Label wlJobdescription = new Label(wJobComp, SWT.RIGHT);
		wlJobdescription.setText(Messages.getString("JobDialog.Jobdescription.Label")); //$NON-NLS-1$
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
		wlExtendeddescription.setText(Messages.getString("JobDialog.Extendeddescription.Label"));
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
		wlJobstatus.setText(Messages.getString("JobDialog.Jobstatus.Label"));
		props.setLook(wlJobstatus);
		fdlJobstatus = new FormData();
		fdlJobstatus.left = new FormAttachment(0, 0);
		fdlJobstatus.right = new FormAttachment(middle, 0);
		fdlJobstatus.top = new FormAttachment(wExtendeddescription, margin*2);
		wlJobstatus.setLayoutData(fdlJobstatus);
		wJobstatus = new CCombo(wJobComp, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		wJobstatus.add(Messages.getString("JobDialog.Draft_Jobstatus.Label"));
		wJobstatus.add(Messages.getString("JobDialog.Production_Jobstatus.Label"));
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
		wlJobversion.setText(Messages.getString("JobDialog.Jobversion.Label")); //$NON-NLS-1$
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
		wlDirectory.setText(Messages.getString("JobDialog.Directory.Label"));
		props.setLook(wlDirectory);
		fdlDirectory=new FormData();
		fdlDirectory.left = new FormAttachment(0, 0);
		fdlDirectory.right= new FormAttachment(middle, -margin);
		fdlDirectory.top  = new FormAttachment(wJobversion, margin);
		wlDirectory.setLayoutData(fdlDirectory);

		wbDirectory=new Button(wJobComp, SWT.PUSH);
		wbDirectory.setToolTipText(Messages.getString("JobDialog.SelectJobFolderFolder.Tooltip"));
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
				RepositoryDirectory directoryFrom = jobMeta.getDirectory();
				if (directoryFrom==null) directoryFrom = new RepositoryDirectory();
				long idDirectoryFrom  = directoryFrom.getID();
				
				SelectDirectoryDialog sdd = new SelectDirectoryDialog(shell, SWT.NONE, rep);
				RepositoryDirectory rd = sdd.open();
				if (rd!=null)
				{
					if (idDirectoryFrom!=rd.getID())
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
		wDirectory.setToolTipText(Messages.getString("JobDialog.Directory.Tooltip"));
		wDirectory.setEditable(false);
		wDirectory.setEnabled(false);
		fdDirectory=new FormData();
		fdDirectory.top  = new FormAttachment(wJobversion, margin);
		fdDirectory.left = new FormAttachment(middle, 0);
		fdDirectory.right= new FormAttachment(wbDirectory, 0);
		wDirectory.setLayoutData(fdDirectory);

		// Create User:
		Label wlCreateUser = new Label(wJobComp, SWT.RIGHT);
		wlCreateUser.setText(Messages.getString("JobDialog.CreateUser.Label")); //$NON-NLS-1$
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
		wlCreateDate.setText(Messages.getString("JobDialog.CreateDate.Label")); //$NON-NLS-1$
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
		wlModUser.setText(Messages.getString("JobDialog.LastModifiedUser.Label")); //$NON-NLS-1$
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
		wlModDate.setText(Messages.getString("JobDialog.LastModifiedDate.Label")); //$NON-NLS-1$
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
        wParamTab.setText(Messages.getString("JobDialog.ParamTab.Label")); //$NON-NLS-1$

        FormLayout paramLayout = new FormLayout ();
        paramLayout.marginWidth  = Const.MARGIN;
        paramLayout.marginHeight = Const.MARGIN;
        
        Composite wParamComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wParamComp);
        wParamComp.setLayout(paramLayout);

        Label wlFields = new Label(wParamComp, SWT.RIGHT);
        wlFields.setText(Messages.getString("JobDialog.Parameters.Label")); //$NON-NLS-1$
        props.setLook(wlFields);
        FormData fdlFields = new FormData();
        fdlFields.left = new FormAttachment(0, 0);
        fdlFields.top  = new FormAttachment(0, 0);
        wlFields.setLayoutData(fdlFields);
        
        final int FieldsCols=3;
        final int FieldsRows=jobMeta.listParameters().length;
        
        ColumnInfo[] colinf=new ColumnInfo[FieldsCols];
        colinf[0]=new ColumnInfo(Messages.getString("JobDialog.ColumnInfo.Parameter.Label"), ColumnInfo.COLUMN_TYPE_TEXT,   false); //$NON-NLS-1$
        colinf[1]=new ColumnInfo(Messages.getString("JobDialog.ColumnInfo.Default.Label"),     ColumnInfo.COLUMN_TYPE_TEXT,   false); //$NON-NLS-1$        
        colinf[2]=new ColumnInfo(Messages.getString("JobDialog.ColumnInfo.Description.Label"),     ColumnInfo.COLUMN_TYPE_TEXT,   false); //$NON-NLS-1$
        
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
		wLogTab.setText(Messages.getString("JobDialog.LogTab.Label")); //$NON-NLS-1$

		FormLayout LogLayout = new FormLayout ();
		LogLayout.marginWidth  = Const.MARGIN;
		LogLayout.marginHeight = Const.MARGIN;
        
		Composite wLogComp = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wLogComp);
		wLogComp.setLayout(LogLayout);

		// Log table connection...
		wlLogconnection=new Label(wLogComp, SWT.RIGHT);
		wlLogconnection.setText(Messages.getString("JobDialog.LogConnection.Label"));
		props.setLook(wlLogconnection);
		fdlLogconnection=new FormData();
		fdlLogconnection.top  = new FormAttachment(wDirectory, margin*4);
		fdlLogconnection.left = new FormAttachment(0, 0);
		fdlLogconnection.right= new FormAttachment(middle, 0);
		wlLogconnection.setLayoutData(fdlLogconnection);

		wbLogconnection=new Button(wLogComp, SWT.PUSH);
		wbLogconnection.setText(BaseMessages.getString("System.Button.Edit"));
		fdbLogconnection=new FormData();
		fdbLogconnection.top   = new FormAttachment(wDirectory, margin*4);
		fdbLogconnection.right = new FormAttachment(100, 0);
		wbLogconnection.setLayoutData(fdbLogconnection);
		wbLogconnection.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				DatabaseMeta databaseMeta = jobMeta.findDatabase(wLogconnection.getText());
				if (databaseMeta==null) databaseMeta=new DatabaseMeta();
				databaseMeta.shareVariablesWith(jobMeta);
				DatabaseDialog cid = new DatabaseDialog(shell, databaseMeta);
				cid.setModalDialog(true);
				if (cid.open()!=null)
				{
					wLogconnection.setText(databaseMeta.getName());
				}
			}
		}
		);

		wLogconnection=new CCombo(wLogComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wLogconnection);
		wLogconnection.setToolTipText(Messages.getString("JobDialog.LogConnection.Tooltip"));
		wLogconnection.addModifyListener(lsMod);
		fdLogconnection=new FormData();
		fdLogconnection.top  = new FormAttachment(wDirectory, margin*4);
		fdLogconnection.left = new FormAttachment(middle, 0);
		fdLogconnection.right= new FormAttachment(wbLogconnection, -margin);
		wLogconnection.setLayoutData(fdLogconnection);

		// populate the combo box...
		for (int i=0;i<jobMeta.nrDatabases();i++)
		{
			DatabaseMeta meta = jobMeta.getDatabase(i);
			wLogconnection.add(meta.getName());
		}
        
		// add a listener
		wLogconnection.addModifyListener(new ModifyListener() { public void modifyText(ModifyEvent e) { setFlags(); } } );

		// Log table...:
		wlLogtable=new Label(wLogComp, SWT.RIGHT);
		wlLogtable.setText(Messages.getString("JobDialog.LogTable.Label"));
		props.setLook(wlLogtable);
		fdlLogtable=new FormData();
		fdlLogtable.left = new FormAttachment(0, 0);
		fdlLogtable.right= new FormAttachment(middle, 0);
		fdlLogtable.top  = new FormAttachment(wLogconnection, margin);
		wlLogtable.setLayoutData(fdlLogtable);
		wLogtable=new Text(wLogComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wLogtable);
		wLogtable.setToolTipText(Messages.getString("JobDialog.LogTable.Tooltip"));
		wLogtable.addModifyListener(lsMod);
		fdLogtable=new FormData();
		fdLogtable.left = new FormAttachment(middle, 0);
		fdLogtable.top  = new FormAttachment(wLogconnection, margin);
		fdLogtable.right= new FormAttachment(100, 0);
		wLogtable.setLayoutData(fdLogtable);

		wlBatch=new Label(wLogComp, SWT.RIGHT);
		wlBatch.setText(Messages.getString("JobDialog.UseBatchID.Label"));
		props.setLook(wlBatch);
		fdlBatch=new FormData();
		fdlBatch.left = new FormAttachment(0, 0);
		fdlBatch.top  = new FormAttachment(wLogtable, margin*3);
		fdlBatch.right= new FormAttachment(middle, -margin);
		wlBatch.setLayoutData(fdlBatch);
		wBatch=new Button(wLogComp, SWT.CHECK);
		props.setLook(wBatch);
		wBatch.setToolTipText(Messages.getString("JobDialog.UseBatchID.Tooltip"));
		fdBatch=new FormData();
		fdBatch.left = new FormAttachment(middle, 0);
		fdBatch.top  = new FormAttachment(wLogtable, margin*3);
		fdBatch.right= new FormAttachment(100, 0);
		wBatch.setLayoutData(fdBatch);

		wlBatchTrans=new Label(wLogComp, SWT.RIGHT);
		wlBatchTrans.setText(Messages.getString("JobDialog.PassBatchID.Label"));
		props.setLook(wlBatchTrans);
		fdlBatchTrans=new FormData();
		fdlBatchTrans.left = new FormAttachment(0, 0);
		fdlBatchTrans.top  = new FormAttachment(wBatch, margin);
		fdlBatchTrans.right= new FormAttachment(middle, -margin);
		wlBatchTrans.setLayoutData(fdlBatchTrans);
		wBatchTrans=new Button(wLogComp, SWT.CHECK);
		props.setLook(wBatchTrans);
		wBatchTrans.setToolTipText(Messages.getString("JobDialog.PassBatchID.Tooltip"));
		fdBatchTrans=new FormData();
		fdBatchTrans.left = new FormAttachment(middle, 0);
		fdBatchTrans.top  = new FormAttachment(wBatch, margin);
		fdBatchTrans.right= new FormAttachment(100, 0);
		wBatchTrans.setLayoutData(fdBatchTrans);

		wlLogfield=new Label(wLogComp, SWT.RIGHT);
		wlLogfield.setText(Messages.getString("JobDialog.UseLogField.Label"));
		props.setLook(wlLogfield);
		fdlLogfield=new FormData();
		fdlLogfield.left = new FormAttachment(0, 0);
		fdlLogfield.top  = new FormAttachment(wBatchTrans, margin);
		fdlLogfield.right= new FormAttachment(middle, -margin);
		wlLogfield.setLayoutData(fdlLogfield);
		wLogfield=new Button(wLogComp, SWT.CHECK);
		props.setLook(wLogfield);
		wLogfield.setToolTipText(Messages.getString("JobDialog.UseLogField.Tooltip"));
		fdLogfield=new FormData();
		fdLogfield.left = new FormAttachment(middle, 0);
		fdLogfield.top  = new FormAttachment(wBatchTrans, margin);
		fdLogfield.right= new FormAttachment(100, 0);
		wLogfield.setLayoutData(fdLogfield);
        wLogfield.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { setFlags(); } });

        // The log size limit
        //
        wlLogSizeLimit = new Label(wLogComp, SWT.RIGHT);
        wlLogSizeLimit.setText(Messages.getString("JobDialog.LogSizeLimit.Label")); //$NON-NLS-1$
        wlLogSizeLimit.setToolTipText(Messages.getString("JobDialog.LogSizeLimit.Tooltip")); //$NON-NLS-1$
        props.setLook(wlLogSizeLimit);
        FormData fdlLogSizeLimit = new FormData();
        fdlLogSizeLimit.left = new FormAttachment(0, 0);
        fdlLogSizeLimit.right= new FormAttachment(middle, -margin);
        fdlLogSizeLimit.top  = new FormAttachment(wLogfield, margin);
        wlLogSizeLimit.setLayoutData(fdlLogSizeLimit);
        wLogSizeLimit=new TextVar(jobMeta, wLogComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wLogSizeLimit.setToolTipText(Messages.getString("JobDialog.LogSizeLimit.Tooltip")); //$NON-NLS-1$
        props.setLook(wLogSizeLimit);
        wLogSizeLimit.addModifyListener(lsMod);
        FormData fdLogSizeLimit = new FormData();
        fdLogSizeLimit.left = new FormAttachment(middle, 0);
        fdLogSizeLimit.top  = new FormAttachment(wLogfield, margin);
        fdLogSizeLimit.right= new FormAttachment(100, 0);
        wLogSizeLimit.setLayoutData(fdLogSizeLimit);

		// Shared objects file
		Label wlSharedObjectsFile = new Label(wLogComp, SWT.RIGHT);
		wlSharedObjectsFile.setText(Messages.getString("JobDialog.SharedObjectsFile.Label"));
		props.setLook(wlSharedObjectsFile);
		FormData fdlSharedObjectsFile = new FormData();
		fdlSharedObjectsFile.left = new FormAttachment(0, 0);
		fdlSharedObjectsFile.right= new FormAttachment(middle, -margin);
		fdlSharedObjectsFile.top  = new FormAttachment(wLogSizeLimit, 4*margin);
		wlSharedObjectsFile.setLayoutData(fdlSharedObjectsFile);
		wSharedObjectsFile=new TextVar(jobMeta, wLogComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wlSharedObjectsFile.setToolTipText(Messages.getString("JobDialog.SharedObjectsFile.Tooltip"));
		wSharedObjectsFile.setToolTipText(Messages.getString("JobDialog.SharedObjectsFile.Tooltip"));
		props.setLook(wSharedObjectsFile);
		FormData fdSharedObjectsFile = new FormData();
		fdSharedObjectsFile.left = new FormAttachment(middle, 0);
		fdSharedObjectsFile.top  = new FormAttachment(wLogSizeLimit, 4*margin);
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
		wLogComp.setLayoutData(fdLogComp);
		  
		wLogComp.layout();
		wLogTab.setControl(wLogComp);
	        
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
		log.logDebug(toString(), "getting transformation info...");
	
		wJobname.setText( Const.NVL(jobMeta.getName(), "") );
		wJobFilename.setText( Const.NVL(jobMeta.getFilename(), "") );
		wJobdescription.setText(  Const.NVL(jobMeta.getDescription(), "") );
		wExtendeddescription.setText( Const.NVL(jobMeta.getExtendedDescription(), "") );
		wJobversion.setText( Const.NVL(jobMeta.getJobversion(), "") );
		wJobstatus.select( jobMeta.getJobstatus() -1);
		
		if (jobMeta.getDirectory()!=null)      wDirectory.setText    ( jobMeta.getDirectory().getPath() );
		
		if (jobMeta.getCreatedUser()!=null)     wCreateUser.setText          ( jobMeta.getCreatedUser() );
		if (jobMeta.getCreatedDate()!=null && jobMeta.getCreatedDate()!=null)     						   
			wCreateDate.setText          ( jobMeta.getCreatedDate().toString() );

		if (jobMeta.getModifiedUser()!=null)     wModUser.setText          ( jobMeta.getModifiedUser() );
		if (jobMeta.getModifiedDate()!=null && 
			jobMeta.getModifiedDate()!=null	)     						   
				wModDate.setText    ( jobMeta.getModifiedDate().toString() );
	

		if (jobMeta.getLogConnection()!=null)  wLogconnection.setText( jobMeta.getLogConnection().getName());
		if (jobMeta.getLogTable()!=null)       wLogtable.setText     ( jobMeta.getLogTable());
        
        wBatch.setSelection(jobMeta.isBatchIdUsed());
        wBatchTrans.setSelection(jobMeta.isBatchIdPassed());
        wLogfield.setSelection(jobMeta.isLogfieldUsed());
		wLogSizeLimit.setText( Const.NVL(jobMeta.getLogSizeLimit(), ""));

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
        
        DatabaseMeta dbMeta = jobMeta.findDatabase(wLogconnection.getText());
        wbLogconnection.setEnabled(dbMeta!=null);
        
        wlLogSizeLimit.setEnabled(wLogfield.getSelection());
        wLogSizeLimit.setEnabled(wLogfield.getSelection());
    }
	
	private void cancel()
	{
		props.setScreen(new WindowProperty(shell));
		jobMeta=null;
		dispose();
	}
	
	private void ok()
	{
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
		jobMeta.setLogConnection( jobMeta.findDatabase(wLogconnection.getText()) );
        jobMeta.setLogTable( wLogtable.getText() );
        
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
				if ( log.isDetailed() ) 
					log.logDetailed(getClass().getName(), "Duplicate parameter '" + item.getText(1) + "' detected.");
			}
		}		        
        
        jobMeta.setUseBatchId( wBatch.getSelection() );
        jobMeta.setBatchIdPassed( wBatchTrans.getSelection() );
        jobMeta.setLogfieldUsed( wLogfield.getSelection() );
        jobMeta.setLogSizeLimit( wLogSizeLimit.getText() );
        jobMeta.setSharedObjectsFile( wSharedObjectsFile.getText() );

        if (newDirectory!=null) 
        {
	        if (directoryChangeAllowed) 
	        {
				RepositoryDirectory dirFrom = jobMeta.getDirectory();
			    long idDirFrom = dirFrom==null?-1L:dirFrom.getID();
	
			    try
				{
				    
					rep.moveJob(jobMeta.getName(), idDirFrom, newDirectory.getID() );
					log.logDetailed(getClass().getName(), "Moved directory to ["+newDirectory.getPath()+"]");
					jobMeta.setDirectory( newDirectory );
					wDirectory.setText(jobMeta.getDirectory().getPath());
				}
				catch(KettleException dbe)
				{
					jobMeta.setDirectory( dirFrom );
			 		
					MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					mb.setText(Messages.getString("JobDialog.Dialog.ErrorChangingDirectory.Title"));
					mb.setMessage(Messages.getString("JobDialog.Dialog.ErrorChangingDirectory.Message"));
					mb.open();
				}
	        }
	        else
	        {
	        	// Just update to the new selected directory...
	        	//
	        	jobMeta.setDirectory( newDirectory );
	        }
        }
        
        jobMeta.setChanged( changed || jobMeta.hasChanged());

		dispose();
	}
	
	// Generate code for create table...
	// Conversions done by Database
	private void sql()
	{
		DatabaseMeta ci = jobMeta.findDatabase(wLogconnection.getText());
		if (ci!=null)
		{
			RowMetaInterface r = Database.getJobLogrecordFields(false, wBatch.getSelection(), wLogfield.getSelection());
			if (r!=null && r.size()>0)
			{
				String tablename = wLogtable.getText();
				if (tablename!=null && tablename.length()>0)
				{
					Database db = new Database(ci);
					db.shareVariablesWith(jobMeta);
					try
					{
                        db.connect();
                        
                        // Get the DDL for the specified tablename and fields...
						String createTable = db.getDDL(tablename, r);
                        
                        if (!Const.isEmpty(createTable))
                        {
    						log.logBasic(toString(), createTable);
    	
    						SQLEditor sqledit = new SQLEditor(shell, SWT.NONE, ci, null, createTable);
    						sqledit.open();
                        }
                        else
                        {
                            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
                            mb.setText(Messages.getString("JobDialog.NoSqlNedds.DialogTitle"));
                            mb.setMessage(Messages.getString("JobDialog.NoSqlNedds.DialogMessage"));
                            mb.open(); 
                        }
					}
					catch(KettleDatabaseException dbe)
					{
						MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
						mb.setMessage(Messages.getString("JobDialog.Dialog.ErrorCreatingSQL.Message")+Const.CR+dbe.getMessage());
						mb.setText(Messages.getString("JobDialog.Dialog.ErrorCreatingSQL.Title"));
						mb.open();
					}
                    finally
                    {
                        db.disconnect();
                    }
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
					mb.setMessage(Messages.getString("JobDialog.Dialog.PleaseEnterALogTable.Message"));
					mb.setText(Messages.getString("JobDialog.Dialog.PleaseEnterALogTable.Title"));
					mb.open(); 
				}
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage(Messages.getString("JobDialog.Dialog.CouldNotFindFieldsToCreateLogTable.Message"));
				mb.setText(Messages.getString("JobDialog.Dialog.CouldNotFindFieldsToCreateLogTable.Title"));
				mb.open(); 
			}
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(Messages.getString("JobDialog.Dialog.SelectCreateValidLogConnection.Message"));
			mb.setText(Messages.getString("JobDialog.Dialog.SelectCreateValidLogConnection.Title"));
			mb.open(); 
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
            String id = JobEntryLoader.getInstance().getJobEntryID(jobEntryInterface);
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