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

package org.pentaho.di.ui.trans.steps.jobexecutor;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.vfs.FileObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.gui.SpoonInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.jobexecutor.JobExecutorMeta;
import org.pentaho.di.trans.steps.jobexecutor.JobExecutorParameters;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.repository.dialog.SelectObjectDialog;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

public class JobExecutorDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = JobExecutorMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private JobExecutorMeta jobExecutorMeta;

	private Group gJobGroup;

  // File
  //
  private Button                            radioFilename;
  private Button                            wbbFilename;
  private TextVar                           wFilename;

	// Repository by name
	//
  private Button                            radioByName;
  private TextVar                           wJobname, wDirectory;
  private Button                            wbJob;

	// Repository by reference
	//
  private Button                            radioByReference;
  private Button                            wbByReference;
  private TextVar                           wByReference;
	
  // Edit the JobExecutor transformation in Spoon
  //
	private Button wEditJob;

	private CTabFolder wTabFolder;

	private JobMeta executorJobMeta = null;

	protected boolean jobModified;

	private ModifyListener lsMod;

	private int middle;

	private int margin;

  private Button wInheritAll;

  private TableView wJobExecutorParameters;

  private Label wlGroupSize;
  private TextVar wGroupSize;
  private Label wlGroupField;
  private CCombo wGroupField;
  private Label wlGroupTime;
  private TextVar wGroupTime;
  
  private Label wlExecutionResultTarget;
  private Label wlExecutionTimeField;
  private Label wlExecutionResultField;
  private Label wlExecutionNrErrorsField;
  private Label wlExecutionLinesReadField;
  private Label wlExecutionLinesWrittenField;
  private Label wlExecutionLinesInputField;
  private Label wlExecutionLinesOutputField;
  private Label wlExecutionLinesRejectedField;
  private Label wlExecutionLinesUpdatedField;
  private Label wlExecutionLinesDeletedField;
  private Label wlExecutionFilesRetrievedField;
  private Label wlExecutionExitStatusField;
  private Label wlExecutionLogTextField;
  private Label wlExecutionLogChannelIdField;
  private CCombo wExecutionResultTarget;
  private TextVar wExecutionTimeField;
  private TextVar wExecutionResultField;
  private TextVar wExecutionNrErrorsField;
  private TextVar wExecutionLinesReadField;
  private TextVar wExecutionLinesWrittenField;
  private TextVar wExecutionLinesInputField;
  private TextVar wExecutionLinesOutputField;
  private TextVar wExecutionLinesRejectedField;
  private TextVar wExecutionLinesUpdatedField;
  private TextVar wExecutionLinesDeletedField;
  private TextVar wExecutionFilesRetrievedField;
  private TextVar wExecutionExitStatusField;
  private TextVar wExecutionLogTextField;
  private TextVar wExecutionLogChannelIdField;
  
  private ObjectId         referenceObjectId;
  private ObjectLocationSpecificationMethod specificationMethod;

  private ColumnInfo[] parameterColumns;

  private Label wlResultFilesTarget;

  private CCombo wResultFilesTarget;

  private Label wlResultFileNameField;

  private TextVar wResultFileNameField;

  private Label wlResultRowsTarget;

  private CCombo wResultRowsTarget;

  private Label wlResultFields;

  private TableView wResultRowsFields;

  private Button wGetParameters;
 

	public JobExecutorDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta) in, tr, sname);
		jobExecutorMeta = (JobExecutorMeta) in;
		jobModified = false;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
		props.setLook(shell);
		setShellImage(shell, jobExecutorMeta);

		lsMod = new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				jobExecutorMeta.setChanged();
				setFlags();
			}
		};
		changed = jobExecutorMeta.hasChanged();

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "JobExecutorDialog.Shell.Title")); //$NON-NLS-1$

		middle = props.getMiddlePct();
		margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "JobExecutorDialog.Stepname.Label")); //$NON-NLS-1$
		props.setLook(wlStepname);
		fdlStepname = new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right = new FormAttachment(middle, -margin);
		fdlStepname.top = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname = new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top = new FormAttachment(0, margin);
		fdStepname.right = new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);
	
		// Show a group with 2 main options: a transformation in the repository
		// or on file
		//

		// //////////////////////////////////////////////////
		// The key creation box
		// //////////////////////////////////////////////////
		//
		gJobGroup = new Group(shell, SWT.SHADOW_ETCHED_IN);
		gJobGroup.setText(BaseMessages.getString(PKG, "JobExecutorDialog.JobGroup.Label")); //$NON-NLS-1$;
		gJobGroup.setBackground(shell.getBackground()); // the default looks
		// ugly
		FormLayout transGroupLayout = new FormLayout();
		transGroupLayout.marginLeft = margin * 2;
		transGroupLayout.marginTop = margin * 2;
		transGroupLayout.marginRight = margin * 2;
		transGroupLayout.marginBottom = margin * 2;
		gJobGroup.setLayout(transGroupLayout);

		// Radio button: The JobExecutor is in a file
		// 
		radioFilename = new Button(gJobGroup, SWT.RADIO);
		props.setLook(radioFilename);
		radioFilename.setSelection(false);
		radioFilename.setText(BaseMessages.getString(PKG, "JobExecutorDialog.RadioFile.Label")); //$NON-NLS-1$
		radioFilename.setToolTipText(BaseMessages.getString(PKG, "JobExecutorDialog.RadioFile.Tooltip", Const.CR)); //$NON-NLS-1$ //$NON-NLS-2$
		FormData fdFileRadio = new FormData();
		fdFileRadio.left = new FormAttachment(0, 0);
		fdFileRadio.right = new FormAttachment(100, 0);
		fdFileRadio.top = new FormAttachment(0, 0);
		radioFilename.setLayoutData(fdFileRadio);
    radioFilename.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        specificationMethod=ObjectLocationSpecificationMethod.FILENAME;
        setRadioButtons();
      }
    });

		wbbFilename = new Button(gJobGroup, SWT.PUSH | SWT.CENTER); // Browse
		props.setLook(wbbFilename);
		wbbFilename.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
		wbbFilename.setToolTipText(BaseMessages.getString(PKG, "System.Tooltip.BrowseForFileOrDirAndAdd"));
		FormData fdbFilename = new FormData();
		fdbFilename.right = new FormAttachment(100, 0);
		fdbFilename.top = new FormAttachment(radioFilename, margin);
		wbbFilename.setLayoutData(fdbFilename);
		wbbFilename.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e)
			{
				selectFileJob();
			}
		});

		wFilename = new TextVar(transMeta, gJobGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		FormData fdFilename = new FormData();
		fdFilename.left = new FormAttachment(0, 25);
		fdFilename.right = new FormAttachment(wbbFilename, -margin);
		fdFilename.top = new FormAttachment(wbbFilename, 0, SWT.CENTER);
		wFilename.setLayoutData(fdFilename);
		wFilename.addModifyListener(new ModifyListener() { public void modifyText(ModifyEvent e)
			{
        specificationMethod=ObjectLocationSpecificationMethod.FILENAME;
        setRadioButtons();			
      }
		});

		// Radio button: The JobExecutor is in the repository
		// 
		radioByName = new Button(gJobGroup, SWT.RADIO);
		props.setLook(radioByName);
		radioByName.setSelection(false);
		radioByName.setText(BaseMessages.getString(PKG, "JobExecutorDialog.RadioRep.Label")); //$NON-NLS-1$
		radioByName.setToolTipText(BaseMessages.getString(PKG, "JobExecutorDialog.RadioRep.Tooltip", Const.CR)); //$NON-NLS-1$ //$NON-NLS-2$
		FormData fdRepRadio = new FormData();
		fdRepRadio.left = new FormAttachment(0, 0);
		fdRepRadio.right = new FormAttachment(100, 0);
		fdRepRadio.top = new FormAttachment(wbbFilename, 2 * margin);
		radioByName.setLayoutData(fdRepRadio);
    radioByName.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        specificationMethod=ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
        setRadioButtons();
      }
    });
		wbJob = new Button(gJobGroup, SWT.PUSH | SWT.CENTER); // Browse
		props.setLook(wbJob);
		wbJob.setText(BaseMessages.getString(PKG, "JobExecutorDialog.Select.Button"));
		wbJob.setToolTipText(BaseMessages.getString(PKG, "System.Tooltip.BrowseForFileOrDirAndAdd"));
		FormData fdbJob = new FormData();
		fdbJob.right = new FormAttachment(100, 0);
		fdbJob.top = new FormAttachment(radioByName, 2 * margin);
		wbJob.setLayoutData(fdbJob);
		wbJob.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e)
			{
				selectRepositoryJob();
			}
		});

		wDirectory = new TextVar(transMeta, gJobGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wDirectory);
		wDirectory.addModifyListener(lsMod);
		FormData fdJobDir = new FormData();
		fdJobDir.left = new FormAttachment(middle + (100 - middle) / 2, 0);
		fdJobDir.right = new FormAttachment(wbJob, -margin);
		fdJobDir.top = new FormAttachment(wbJob, 0, SWT.CENTER);
		wDirectory.setLayoutData(fdJobDir);
    wDirectory.addModifyListener(new ModifyListener() { public void modifyText(ModifyEvent e) {
        specificationMethod=ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
        setRadioButtons();      
      }
    });

		wJobname = new TextVar(transMeta, gJobGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wJobname);
		wJobname.addModifyListener(lsMod);
		FormData fdJobName = new FormData();
		fdJobName.left = new FormAttachment(0, 25);
		fdJobName.right = new FormAttachment(wDirectory, -margin);
		fdJobName.top = new FormAttachment(wbJob, 0, SWT.CENTER);
		wJobname.setLayoutData(fdJobName);
		wJobname.addModifyListener(new ModifyListener() { public void modifyText(ModifyEvent e) {
        specificationMethod=ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
        setRadioButtons();      
      }
    });
		
    // Radio button: The JobExecutor is in the repository
    // 
    radioByReference = new Button(gJobGroup, SWT.RADIO);
    props.setLook(radioByReference);
    radioByReference.setSelection(false);
    radioByReference.setText(BaseMessages.getString(PKG, "JobExecutorDialog.RadioRepByReference.Label")); //$NON-NLS-1$
    radioByReference.setToolTipText(BaseMessages.getString(PKG, "JobExecutorDialog.RadioRepByReference.Tooltip", Const.CR)); //$NON-NLS-1$ //$NON-NLS-2$
    FormData fdRadioByReference = new FormData();
    fdRadioByReference.left = new FormAttachment(0, 0);
    fdRadioByReference.right = new FormAttachment(100, 0);
    fdRadioByReference.top = new FormAttachment(wJobname, 2 * margin);
    radioByReference.setLayoutData(fdRadioByReference);
    radioByReference.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        specificationMethod=ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE;
        setRadioButtons();
      }
    });

    wbByReference = new Button(gJobGroup, SWT.PUSH | SWT.CENTER);
    props.setLook(wbByReference);
    wbByReference.setImage(GUIResource.getInstance().getImageJobGraph());
    wbByReference.setToolTipText(BaseMessages.getString(PKG, "JobExecutorDialog.SelectJob.Tooltip"));
    FormData fdbByReference = new FormData();
    fdbByReference.top = new FormAttachment(radioByReference, margin);
    fdbByReference.right = new FormAttachment(100, 0);
    wbByReference.setLayoutData(fdbByReference);
    wbByReference.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        selectJobByReference();
      }
    });

    wByReference = new TextVar(transMeta, gJobGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER | SWT.READ_ONLY);
    props.setLook(wByReference);
    wByReference.addModifyListener(lsMod);
    FormData fdByReference = new FormData();
    fdByReference.top = new FormAttachment(radioByReference, margin);
    fdByReference.left = new FormAttachment(0, 25);
    fdByReference.right = new FormAttachment(wbByReference, -margin);
    wByReference.setLayoutData(fdByReference);
    wByReference.addModifyListener(new ModifyListener() { public void modifyText(ModifyEvent e) {
        specificationMethod=ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE;
        setRadioButtons();      
      }
    });
		
		wEditJob = new Button(gJobGroup, SWT.PUSH | SWT.CENTER); // Browse
		props.setLook(wEditJob);
		wEditJob.setText(BaseMessages.getString(PKG, "JobExecutorDialog.Edit.Button"));
		wEditJob.setToolTipText(BaseMessages.getString(PKG, "System.Tooltip.BrowseForFileOrDirAndAdd"));
		FormData fdEditJob = new FormData();
		fdEditJob.left = new FormAttachment(0, 0);
		fdEditJob.right = new FormAttachment(100, 0);
		fdEditJob.top = new FormAttachment(wByReference, 3 * margin);
		wEditJob.setLayoutData(fdEditJob);
		wEditJob.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e)
			{
				editJob();
			}
		});

		FormData fdJobGroup = new FormData();
		fdJobGroup.left = new FormAttachment(0, 0);
		fdJobGroup.top = new FormAttachment(wStepname, 2 * margin);
		fdJobGroup.right = new FormAttachment(100, 0);
		// fdJobGroup.bottom = new FormAttachment(wStepname, 350);
		gJobGroup.setLayoutData(fdJobGroup);
		Control lastControl = gJobGroup;
		
		// 
		// Add a tab folder for the parameters and various input and output
		// streams
		//
		wTabFolder = new CTabFolder(shell, SWT.BORDER);
		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
		wTabFolder.setSimple(false);
		wTabFolder.setUnselectedCloseVisible(true);

		FormData fdTabFolder = new FormData();
		fdTabFolder.left = new FormAttachment(0, 0);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.top = new FormAttachment(lastControl, margin * 2);
		fdTabFolder.bottom = new FormAttachment(100, -75);
		wTabFolder.setLayoutData(fdTabFolder);

		// Some buttons
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);

    // Add the tabs...
    //
    addParametersTab();
    addRowGroupTab();
    addExecutionResultTab();
    addResultRowsTab();
    addResultFilesTab();
    
		// Add listeners
		lsCancel = new Listener()
		{
			public void handleEvent(Event e)
			{
				cancel();
			}
		};
		lsOK = new Listener()
		{
			public void handleEvent(Event e)
			{
				ok();
			}
		};

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener(SWT.Selection, lsOK);

		lsDef = new SelectionAdapter()
		{
			public void widgetDefaultSelected(SelectionEvent e)
			{
				ok();
			}
		};

		wStepname.addSelectionListener(lsDef);
		wFilename.addSelectionListener(lsDef);
		wJobname.addSelectionListener(lsDef);
		wExecutionTimeField.addSelectionListener(lsDef);
		wExecutionResultField.addSelectionListener(lsDef);
		wExecutionNrErrorsField.addSelectionListener(lsDef);
		wExecutionLinesReadField.addSelectionListener(lsDef);
		wExecutionLinesWrittenField.addSelectionListener(lsDef);
		wExecutionLinesInputField.addSelectionListener(lsDef);
		wExecutionLinesOutputField.addSelectionListener(lsDef);
		wExecutionLinesRejectedField.addSelectionListener(lsDef);
		wExecutionLinesUpdatedField.addSelectionListener(lsDef);
		wExecutionLinesDeletedField.addSelectionListener(lsDef);
		wExecutionFilesRetrievedField.addSelectionListener(lsDef);
		wExecutionExitStatusField.addSelectionListener(lsDef);
		wExecutionLogTextField.addSelectionListener(lsDef);
		wExecutionLogChannelIdField.addSelectionListener(lsDef);
		wResultFileNameField.addSelectionListener(lsDef);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(new ShellAdapter()
		{
			public void shellClosed(ShellEvent e)
			{
				cancel();
			}
		});
		


		// Set the shell size, based upon previous time...
		setSize();

		getData();
		jobExecutorMeta.setChanged(changed);
		wTabFolder.setSelection(0);
		
		shell.open();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
				display.sleep();
		}
		return stepname;
	}

  protected void selectJobByReference() {
    if (repository != null) {
      SelectObjectDialog sod = new SelectObjectDialog(shell, repository, true, false);
      sod.open();
      RepositoryElementMetaInterface repositoryObject = sod.getRepositoryObject();
      if (repositoryObject != null) {
        specificationMethod=ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE;
        getByReferenceData(repositoryObject);
        referenceObjectId = repositoryObject.getObjectId();
        setRadioButtons();
      }
    }
  }

  private void selectRepositoryJob()
	{
		try
		{
			SelectObjectDialog sod = new SelectObjectDialog(shell, repository);
			String transName = sod.open();
			RepositoryDirectoryInterface repdir = sod.getDirectory();
			if (transName != null && repdir != null)
			{
				loadRepositoryJob(transName, repdir);
				wJobname.setText(executorJobMeta.getName());
				wDirectory.setText(executorJobMeta.getRepositoryDirectory().getPath());
				wFilename.setText("");
				radioByName.setSelection(true);
				radioFilename.setSelection(false);
				specificationMethod=ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
				setRadioButtons();
			}
		} catch (KettleException ke)
		{
			new ErrorDialog(
					shell,
					BaseMessages.getString(PKG, "JobExecutorDialog.ErrorSelectingObject.DialogTitle"), BaseMessages.getString(PKG, "JobExecutorDialog.ErrorSelectingObject.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private void loadRepositoryJob(String transName, RepositoryDirectoryInterface repdir) throws KettleException
	{
		// Read the transformation...
		//
		executorJobMeta = repository.loadJob(transMeta.environmentSubstitute(transName), repdir, null, null); // reads last version
		executorJobMeta.clearChanged();
	}

  private void selectFileJob() {
    String curFile = wFilename.getText();
    FileObject root = null;

    try {
      root = KettleVFS.getFileObject(curFile != null ? curFile : Const.getUserHomeDirectory());

      VfsFileChooserDialog vfsFileChooser = Spoon.getInstance().getVfsFileChooserDialog(root.getParent(), root);
      FileObject file = vfsFileChooser.open(shell, null, Const.STRING_TRANS_FILTER_EXT, Const.getJobFilterNames(), VfsFileChooserDialog.VFS_DIALOG_OPEN_FILE);
      if (file == null) {
        return;
      }
      String fname = null;

      fname = file.getURL().getFile();

      if (fname != null) {

        loadFileJob(fname);
        wFilename.setText(executorJobMeta.getFilename());
        wJobname.setText(Const.NVL(executorJobMeta.getName(), ""));
        wDirectory.setText("");
        specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
        setRadioButtons();
      }
    } catch (IOException e) {
      new ErrorDialog(shell, 
          BaseMessages.getString(PKG, "JobExecutorDialog.ErrorLoadingJob.DialogTitle"), 
          BaseMessages.getString(PKG, "JobExecutorDialog.ErrorLoadingJob.DialogMessage"), e); //$NON-NLS-1$ //$NON-NLS-2$
    } catch (KettleException e) {
      new ErrorDialog(shell, 
          BaseMessages.getString(PKG, "JobExecutorDialog.ErrorLoadingJob.DialogTitle"), 
          BaseMessages.getString(PKG, "JobExecutorDialog.ErrorLoadingJob.DialogMessage"), e); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

	private void loadFileJob(String fname) throws KettleException
	{
    executorJobMeta = new JobMeta(transMeta.environmentSubstitute(fname), repository);
		executorJobMeta.clearChanged();
	}

	private void editJob()
	{
		// Load the transformation again to make sure it's still there and
		// refreshed
		// It's an extra check to make sure it's still OK...
	  //
		try
		{
			loadJob();

			// If we're still here, jobExecutorMeta is valid.
			//
			SpoonInterface spoon = SpoonFactory.getInstance();
			if (spoon != null) {
				spoon.addJobGraph(executorJobMeta);
			}
		} catch (KettleException e)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "JobExecutorDialog.ErrorShowingJob.Title"),
					BaseMessages.getString(PKG, "JobExecutorDialog.ErrorShowingJob.Message"), e);
		}
	}

	private void loadJob() throws KettleException
	{
	  switch(specificationMethod) {
	  case FILENAME:
      loadFileJob(wFilename.getText());
      break;
	  case REPOSITORY_BY_NAME:
	    String realDirectory = transMeta.environmentSubstitute(wDirectory.getText());
	    String realJobname = transMeta.environmentSubstitute(wJobname.getText());
	    
	    if (Const.isEmpty(realDirectory) || Const.isEmpty(realJobname)) {
	       throw new KettleException(BaseMessages.getString(PKG, "JobExecutorDialog.Exception.NoValidJobExecutorDetailsFound"));
	    }
      RepositoryDirectoryInterface repdir = repository.findDirectory(realDirectory);
      if (repdir == null)
      {
        throw new KettleException(BaseMessages.getString(PKG, "JobExecutorDialog.Exception.UnableToFindRepositoryDirectory)"));
      }
      loadRepositoryJob(realJobname, repdir);
      break;
	  case REPOSITORY_BY_REFERENCE:
      executorJobMeta = repository.loadJob(referenceObjectId, null); // load the last version
      executorJobMeta.clearChanged();
      break;
	  }
	}
	
  public void setActive() {
    radioByName.setEnabled(repository != null);
    radioByReference.setEnabled(repository != null);
    wFilename.setEnabled(radioFilename.getSelection());
    wbbFilename.setEnabled(radioFilename.getSelection());
    wJobname.setEnabled(repository != null && radioByName.getSelection());
    
    wDirectory.setEnabled(repository != null && radioByName.getSelection());
    
    wbJob.setEnabled(repository != null && radioByName.getSelection());

    wByReference.setEnabled(repository != null && radioByReference.getSelection());
    wbByReference.setEnabled(repository != null && radioByReference.getSelection());
  }
  
  protected void setRadioButtons() {
    radioFilename.setSelection(specificationMethod==ObjectLocationSpecificationMethod.FILENAME);
    radioByName.setSelection(specificationMethod==ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME);
    radioByReference.setSelection(specificationMethod==ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE);
    setActive();
  }

  private void getByReferenceData(RepositoryElementMetaInterface transInf) {
    String path = transInf.getRepositoryDirectory().getPath();
    if (!path.endsWith("/"))
      path += "/";
    path += transInf.getName();
    wByReference.setText(path);
  }

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */
	public void getData()
	{
		wStepname.selectAll();

    specificationMethod=jobExecutorMeta.getSpecificationMethod();
    switch(specificationMethod) {
    case FILENAME: 
      wFilename.setText(Const.NVL(jobExecutorMeta.getFileName(), "")); 
      break;
    case REPOSITORY_BY_NAME:
      wDirectory.setText(Const.NVL(jobExecutorMeta.getDirectoryPath(), ""));
      wJobname.setText(Const.NVL(jobExecutorMeta.getJobName(), ""));
      break;
    case REPOSITORY_BY_REFERENCE:
      referenceObjectId = jobExecutorMeta.getJobObjectId();
      wByReference.setText("");
      try {
        if (repository==null) {
          throw new KettleException(BaseMessages.getString(PKG, "JobExecutorDialog.Exception.NotConnectedToRepository.Message"));
        }
        RepositoryObject transInf = repository.getObjectInformation(jobExecutorMeta.getJobObjectId(), RepositoryObjectType.JOB);
        if (transInf != null) {
          getByReferenceData(transInf);
        }
      } catch (KettleException e) {
        new ErrorDialog(shell, 
            BaseMessages.getString(PKG, "JobExecutorDialog.Exception.UnableToReferenceObjectId.Title"), 
            BaseMessages.getString(PKG, "JobExecutorDialog.Exception.UnableToReferenceObjectId.Message"), e);
      }
      break;   
    }
    setRadioButtons();
		
		// TODO: throw in a separate thread.
		//
		try {
		  String[] prevSteps = transMeta.getStepNames();
		  Arrays.sort(prevSteps);
		  wExecutionResultTarget.setItems(prevSteps);
      wResultFilesTarget.setItems(prevSteps);
      wResultRowsTarget.setItems(prevSteps);
		  
		  String[] inputFields = transMeta.getPrevStepFields(stepMeta).getFieldNames();
		  parameterColumns[1].setComboValues(inputFields);
		  wGroupField.setItems(inputFields);
		} catch(Exception e) {
		  log.logError("couldn't get previous step list",e);
		}
		
		
		wGroupSize.setText(Const.NVL(jobExecutorMeta.getGroupSize(), ""));
    wGroupTime.setText(Const.NVL(jobExecutorMeta.getGroupTime(), ""));
    wGroupField.setText(Const.NVL(jobExecutorMeta.getGroupField(), ""));
		
    wExecutionResultTarget.setText(jobExecutorMeta.getExecutionResultTargetStepMeta()==null ? "" : jobExecutorMeta.getExecutionResultTargetStepMeta().getName());
    wExecutionTimeField.setText(Const.NVL(jobExecutorMeta.getExecutionTimeField(), ""));
    wExecutionResultField.setText(Const.NVL(jobExecutorMeta.getExecutionResultField(), ""));
    wExecutionNrErrorsField.setText(Const.NVL(jobExecutorMeta.getExecutionNrErrorsField(), ""));
    wExecutionLinesReadField.setText(Const.NVL(jobExecutorMeta.getExecutionLinesReadField(), ""));
    wExecutionLinesWrittenField.setText(Const.NVL(jobExecutorMeta.getExecutionLinesWrittenField(), ""));
    wExecutionLinesInputField.setText(Const.NVL(jobExecutorMeta.getExecutionLinesInputField(), ""));
    wExecutionLinesOutputField.setText(Const.NVL(jobExecutorMeta.getExecutionLinesOutputField(), ""));
    wExecutionLinesRejectedField.setText(Const.NVL(jobExecutorMeta.getExecutionLinesRejectedField(), ""));
    wExecutionLinesUpdatedField.setText(Const.NVL(jobExecutorMeta.getExecutionLinesUpdatedField(), ""));
    wExecutionLinesDeletedField.setText(Const.NVL(jobExecutorMeta.getExecutionLinesDeletedField(), ""));
    wExecutionFilesRetrievedField.setText(Const.NVL(jobExecutorMeta.getExecutionFilesRetrievedField(), ""));
    wExecutionExitStatusField.setText(Const.NVL(jobExecutorMeta.getExecutionExitStatusField(), ""));
    wExecutionLogTextField.setText(Const.NVL(jobExecutorMeta.getExecutionLogTextField(), ""));
    wExecutionLogChannelIdField.setText(Const.NVL(jobExecutorMeta.getExecutionLogChannelIdField(), ""));

    // result files
    //
    wResultFilesTarget.setText(jobExecutorMeta.getResultFilesTargetStepMeta()==null ? "" : jobExecutorMeta.getResultFilesTargetStepMeta().getName());
    wResultFileNameField.setText(Const.NVL(jobExecutorMeta.getResultFilesFileNameField(), ""));
    
    // Result rows
    //
    wResultRowsTarget.setText(jobExecutorMeta.getResultRowsTargetStepMeta()==null ? "" : jobExecutorMeta.getResultRowsTargetStepMeta().getName());
    for (int i=0;i<jobExecutorMeta.getResultRowsField().length;i++) {
      TableItem item = new TableItem(wResultRowsFields.table, SWT.NONE);
      item.setText(1, Const.NVL(jobExecutorMeta.getResultRowsField()[i], ""));
      item.setText(2, ValueMeta.getTypeDesc(jobExecutorMeta.getResultRowsType()[i]));
      int length=jobExecutorMeta.getResultRowsLength()[i];
      item.setText(3, length<0?"":Integer.toString(length));
      int precision = jobExecutorMeta.getResultRowsPrecision()[i];
      item.setText(4, precision<0?"":Integer.toString(precision));
    }
    wResultRowsFields.removeEmptyRows();
    wResultRowsFields.setRowNums();
    wResultRowsFields.optWidth(true);
    
		wTabFolder.setSelection(0);

		try
		{
			loadJob();
		} catch (Throwable t)
		{

		}
	   
    setFlags();
	}

	private void addParametersTab()
	{
		CTabItem wParametersTab = new CTabItem(wTabFolder, SWT.NONE);
		wParametersTab.setText(BaseMessages.getString(PKG, "JobExecutorDialog.Parameters.Title")); //$NON-NLS-1$
		wParametersTab.setToolTipText(BaseMessages.getString(PKG, "JobExecutorDialog.Parameters.Tooltip")); //$NON-NLS-1$

		Composite wParametersComposite = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wParametersComposite);

		FormLayout parameterTabLayout = new FormLayout();
		parameterTabLayout.marginWidth = Const.FORM_MARGIN;
		parameterTabLayout.marginHeight = Const.FORM_MARGIN;
		wParametersComposite.setLayout(parameterTabLayout);

    // Add a button: get parameters
    //
    wGetParameters = new Button(wParametersComposite, SWT.PUSH);
    wGetParameters.setText(BaseMessages.getString(PKG, "JobExecutorDialog.Parameters.GetParameters"));
    props.setLook(wGetParameters);
    FormData fdGetParameters = new FormData();
    fdGetParameters.bottom = new FormAttachment(100,0);
    fdGetParameters.right = new FormAttachment(100, 0);
    wGetParameters.setLayoutData(fdGetParameters);
    wGetParameters.setSelection(jobExecutorMeta.getParameters().isInheritingAllVariables());
    wGetParameters.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { 
      getParametersFromJob(); 
      } });

		// Add a checkbox: inherit all variables...
		//
		wInheritAll = new Button(wParametersComposite, SWT.CHECK);
		wInheritAll.setText(BaseMessages.getString(PKG, "JobExecutorDialog.Parameters.InheritAll"));
		props.setLook(wInheritAll);
		FormData fdInheritAll = new FormData();
		fdInheritAll.bottom = new FormAttachment(100,0);
		fdInheritAll.left = new FormAttachment(0,0);
		fdInheritAll.right = new FormAttachment(wGetParameters, -margin);
		wInheritAll.setLayoutData(fdInheritAll);
		wInheritAll.setSelection(jobExecutorMeta.getParameters().isInheritingAllVariables());
		
		// Now add a table view with the 3 columns to specify: variable name, input field & optional static input
		//
		parameterColumns = new ColumnInfo[] {
		    new ColumnInfo(BaseMessages.getString(PKG, "JobExecutorDialog.Parameters.column.Variable"), ColumnInfo.COLUMN_TYPE_TEXT, false, false),
		    new ColumnInfo(BaseMessages.getString(PKG, "JobExecutorDialog.Parameters.column.Field"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {}, false),
        new ColumnInfo(BaseMessages.getString(PKG, "JobExecutorDialog.Parameters.column.Input"), ColumnInfo.COLUMN_TYPE_TEXT, false, false),
		};
		parameterColumns[1].setUsingVariables(true);

		JobExecutorParameters parameters = jobExecutorMeta.getParameters();
		wJobExecutorParameters = new TableView(transMeta, wParametersComposite,
				SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER, parameterColumns, parameters.getVariable().length,
				lsMod, props);
		props.setLook(wJobExecutorParameters);
		FormData fdJobExecutors = new FormData();
		fdJobExecutors.left = new FormAttachment(0, 0);
		fdJobExecutors.right = new FormAttachment(100, 0);
		fdJobExecutors.top = new FormAttachment(0, 0);
		fdJobExecutors.bottom = new FormAttachment(wInheritAll, -margin*2);
		wJobExecutorParameters.setLayoutData(fdJobExecutors);

		for (int i = 0; i < parameters.getVariable().length; i++) {
			TableItem tableItem = wJobExecutorParameters.table.getItem(i);
			tableItem.setText(1, Const.NVL(parameters.getVariable()[i], ""));
			tableItem.setText(2, Const.NVL(parameters.getField()[i], ""));
      tableItem.setText(3, Const.NVL(parameters.getInput()[i], ""));
		}
		wJobExecutorParameters.setRowNums();
		wJobExecutorParameters.optWidth(true);

		FormData fdParametersComposite = new FormData();
		fdParametersComposite.left = new FormAttachment(0, 0);
		fdParametersComposite.top = new FormAttachment(0, 0);
		fdParametersComposite.right = new FormAttachment(100, 0);
		fdParametersComposite.bottom = new FormAttachment(100, 0);
		wParametersComposite.setLayoutData(fdParametersComposite);

		wParametersComposite.layout();
		wParametersTab.setControl(wParametersComposite);
	}
	
	 protected void getParametersFromJob() {
    try {
      // Load the job in executorJobMeta
      //
      loadJob();
      
      String[] parameters = executorJobMeta.listParameters();
      for (int i=0;i<parameters.length;i++) {
        String name = parameters[i];
        String desc = executorJobMeta.getParameterDescription(name);
        
        TableItem item = new TableItem(wJobExecutorParameters.table, SWT.NONE);
        item.setText(1, Const.NVL(name, ""));
        item.setText(3, Const.NVL(desc, ""));
      }
      wJobExecutorParameters.removeEmptyRows();
      wJobExecutorParameters.setRowNums();
      wJobExecutorParameters.optWidth(true);
      
    } catch (Exception e) {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "JobExecutorDialog.ErrorLoadingSpecifiedJob.Title"), BaseMessages.getString(PKG, "JobExecutorDialog.ErrorLoadingSpecifiedJob.Message"), e);
    }
     
   }

  private void addRowGroupTab() {

	    final CTabItem wTab = new CTabItem(wTabFolder, SWT.NONE);
	    wTab.setText(BaseMessages.getString(PKG, "JobExecutorDialog.RowGroup.Title")); //$NON-NLS-1$
	    wTab.setToolTipText(BaseMessages.getString(PKG, "JobExecutorDialog.RowGroup.Tooltip")); //$NON-NLS-1$

	    Composite wInputComposite = new Composite(wTabFolder, SWT.NONE);
	    props.setLook(wInputComposite);

	    FormLayout tabLayout = new FormLayout();
	    tabLayout.marginWidth = Const.FORM_MARGIN;
	    tabLayout.marginHeight = Const.FORM_MARGIN;
	    wInputComposite.setLayout(tabLayout);

	    // Group size
	    //
	    wlGroupSize = new Label(wInputComposite, SWT.RIGHT);
	    props.setLook(wlGroupSize);
	    wlGroupSize.setText(BaseMessages.getString(PKG, "JobExecutorDialog.GroupSize.Label")); //$NON-NLS-1$
	    FormData fdlGroupSize = new FormData();
	    fdlGroupSize.top = new FormAttachment(0, 0);
	    fdlGroupSize.left = new FormAttachment(0, 0); // First one in the left
	    fdlGroupSize.right = new FormAttachment(middle, -margin);
	    wlGroupSize.setLayoutData(fdlGroupSize);
	    wGroupSize = new TextVar(transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
	    props.setLook(wGroupSize);
	    wGroupSize.addModifyListener(lsMod);
	    FormData fdGroupSize = new FormData();
	    fdGroupSize.top = new FormAttachment(0, 0);
	    fdGroupSize.left = new FormAttachment(middle, 0); // To the right of
	    fdGroupSize.right = new FormAttachment(100, 0);
	    wGroupSize.setLayoutData(fdGroupSize);
	    Control lastControl = wGroupSize;

	     // Group field
      //
      wlGroupField = new Label(wInputComposite, SWT.RIGHT);
      props.setLook(wlGroupField);
      wlGroupField.setText(BaseMessages.getString(PKG, "JobExecutorDialog.GroupField.Label")); //$NON-NLS-1$
      FormData fdlGroupField = new FormData();
      fdlGroupField.top = new FormAttachment(lastControl, margin);
      fdlGroupField.left = new FormAttachment(0, 0); // First one in the left
      fdlGroupField.right = new FormAttachment(middle, -margin);
      wlGroupField.setLayoutData(fdlGroupField);
      wGroupField = new CCombo(wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
      props.setLook(wGroupField);
      wGroupField.addModifyListener(lsMod);
      FormData fdGroupField = new FormData();
      fdGroupField.top = new FormAttachment(lastControl, margin);
      fdGroupField.left = new FormAttachment(middle, 0); // To the right of
      fdGroupField.right = new FormAttachment(100, 0);
      wGroupField.setLayoutData(fdGroupField);
      lastControl = wGroupField;

      // Group time
      //
      wlGroupTime = new Label(wInputComposite, SWT.RIGHT);
      props.setLook(wlGroupTime);
      wlGroupTime.setText(BaseMessages.getString(PKG, "JobExecutorDialog.GroupTime.Label")); //$NON-NLS-1$
      FormData fdlGroupTime = new FormData();
      fdlGroupTime.top = new FormAttachment(lastControl, margin);
      fdlGroupTime.left = new FormAttachment(0, 0); // First one in the left
      fdlGroupTime.right = new FormAttachment(middle, -margin);
      wlGroupTime.setLayoutData(fdlGroupTime);
      wGroupTime = new TextVar(transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
      props.setLook(wGroupTime);
      wGroupTime.addModifyListener(lsMod);
      FormData fdGroupTime = new FormData();
      fdGroupTime.top = new FormAttachment(lastControl, margin);
      fdGroupTime.left = new FormAttachment(middle, 0); // To the right of
      fdGroupTime.right = new FormAttachment(100, 0);
      wGroupTime.setLayoutData(fdGroupTime);
      // lastControl = wGroupTime;
      
      wTab.setControl(wInputComposite);
	    wTabFolder.setSelection(wTab);
	  }

   private void addExecutionResultTab() {

     final CTabItem wTab = new CTabItem(wTabFolder, SWT.NONE);
     wTab.setText(BaseMessages.getString(PKG, "JobExecutorDialog.ExecutionResults.Title")); //$NON-NLS-1$
     wTab.setToolTipText(BaseMessages.getString(PKG, "JobExecutorDialog.ExecutionResults.Tooltip")); //$NON-NLS-1$
     
     ScrolledComposite scrolledComposite = new ScrolledComposite(wTabFolder, SWT.V_SCROLL | SWT.H_SCROLL);
     scrolledComposite.setLayout(new FillLayout());
     
     Composite wInputComposite = new Composite(scrolledComposite, SWT.NONE);
     props.setLook(wInputComposite);

     FormLayout tabLayout = new FormLayout();
     tabLayout.marginWidth = Const.FORM_MARGIN;
     tabLayout.marginHeight = Const.FORM_MARGIN;
     wInputComposite.setLayout(tabLayout);

     wlExecutionResultTarget = new Label(wInputComposite, SWT.RIGHT);
     props.setLook(wlExecutionResultTarget);
     wlExecutionResultTarget.setText(BaseMessages.getString(PKG, "JobExecutorDialog.ExecutionResultTarget.Label")); //-NLS-1$
     FormData fdlExecutionResultTarget = new FormData();
     fdlExecutionResultTarget.top = new FormAttachment(0, 0);
     fdlExecutionResultTarget.left = new FormAttachment(0, 0); // First one in the left
     fdlExecutionResultTarget.right = new FormAttachment(middle, -margin);
     wlExecutionResultTarget.setLayoutData(fdlExecutionResultTarget);
     wExecutionResultTarget = new CCombo(wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
     props.setLook(wExecutionResultTarget);
     wExecutionResultTarget.addModifyListener(lsMod);
     FormData fdExecutionResultTarget = new FormData();
     fdExecutionResultTarget.top = new FormAttachment(0, 0);
     fdExecutionResultTarget.left = new FormAttachment(middle, 0); // To the right
     fdExecutionResultTarget.right = new FormAttachment(100, 0);
     wExecutionResultTarget.setLayoutData(fdExecutionResultTarget);
     Control lastControl = wExecutionResultTarget;

     // ExecutionTimeField
     //
     wlExecutionTimeField = new Label(wInputComposite, SWT.RIGHT);
     props.setLook(wlExecutionTimeField);
     wlExecutionTimeField.setText(BaseMessages.getString(PKG, "JobExecutorDialog.ExecutionTimeField.Label")); //-NLS-1$
     FormData fdlExecutionTimeField = new FormData();
     fdlExecutionTimeField.top = new FormAttachment(lastControl, margin);
     fdlExecutionTimeField.left = new FormAttachment(0, 0); // First one in the left
     fdlExecutionTimeField.right = new FormAttachment(middle, -margin);
     wlExecutionTimeField.setLayoutData(fdlExecutionTimeField);
     wExecutionTimeField = new TextVar(transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
     props.setLook(wExecutionTimeField);
     wExecutionTimeField.addModifyListener(lsMod);
     FormData fdExecutionTimeField = new FormData();
     fdExecutionTimeField.top = new FormAttachment(lastControl, margin);
     fdExecutionTimeField.left = new FormAttachment(middle, 0); // To the right
     fdExecutionTimeField.right = new FormAttachment(100, 0);
     wExecutionTimeField.setLayoutData(fdExecutionTimeField);
     lastControl = wExecutionTimeField;

     // ExecutionResultField
     //
     wlExecutionResultField = new Label(wInputComposite, SWT.RIGHT);
     props.setLook(wlExecutionResultField);
     wlExecutionResultField.setText(BaseMessages.getString(PKG, "JobExecutorDialog.ExecutionResultField.Label")); //-NLS-1$
     FormData fdlExecutionResultField = new FormData();
     fdlExecutionResultField.top = new FormAttachment(lastControl, margin);
     fdlExecutionResultField.left = new FormAttachment(0, 0); // First one in the left
     fdlExecutionResultField.right = new FormAttachment(middle, -margin);
     wlExecutionResultField.setLayoutData(fdlExecutionResultField);
     wExecutionResultField = new TextVar(transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
     props.setLook(wExecutionResultField);
     wExecutionResultField.addModifyListener(lsMod);
     FormData fdExecutionResultField = new FormData();
     fdExecutionResultField.top = new FormAttachment(lastControl, margin);
     fdExecutionResultField.left = new FormAttachment(middle, 0); // To the right
     fdExecutionResultField.right = new FormAttachment(100, 0);
     wExecutionResultField.setLayoutData(fdExecutionResultField);
     lastControl = wExecutionResultField;

     // ExecutionNrErrorsField
     //
     wlExecutionNrErrorsField = new Label(wInputComposite, SWT.RIGHT);
     props.setLook(wlExecutionNrErrorsField);
     wlExecutionNrErrorsField.setText(BaseMessages.getString(PKG, "JobExecutorDialog.ExecutionNrErrorsField.Label")); //-NLS-1$
     FormData fdlExecutionNrErrorsField = new FormData();
     fdlExecutionNrErrorsField.top = new FormAttachment(lastControl, margin);
     fdlExecutionNrErrorsField.left = new FormAttachment(0, 0); // First one in the left
     fdlExecutionNrErrorsField.right = new FormAttachment(middle, -margin);
     wlExecutionNrErrorsField.setLayoutData(fdlExecutionNrErrorsField);
     wExecutionNrErrorsField = new TextVar(transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
     props.setLook(wExecutionNrErrorsField);
     wExecutionNrErrorsField.addModifyListener(lsMod);
     FormData fdExecutionNrErrorsField = new FormData();
     fdExecutionNrErrorsField.top = new FormAttachment(lastControl, margin);
     fdExecutionNrErrorsField.left = new FormAttachment(middle, 0); // To the right
     fdExecutionNrErrorsField.right = new FormAttachment(100, 0);
     wExecutionNrErrorsField.setLayoutData(fdExecutionNrErrorsField);
     lastControl = wExecutionNrErrorsField;


     // ExecutionLinesReadField
     //
     wlExecutionLinesReadField = new Label(wInputComposite, SWT.RIGHT);
     props.setLook(wlExecutionLinesReadField);
     wlExecutionLinesReadField.setText(BaseMessages.getString(PKG, "JobExecutorDialog.ExecutionLinesReadField.Label")); //-NLS-1$
     FormData fdlExecutionLinesReadField = new FormData();
     fdlExecutionLinesReadField.top = new FormAttachment(lastControl, margin);
     fdlExecutionLinesReadField.left = new FormAttachment(0, 0); // First one in the left
     fdlExecutionLinesReadField.right = new FormAttachment(middle, -margin);
     wlExecutionLinesReadField.setLayoutData(fdlExecutionLinesReadField);
     wExecutionLinesReadField = new TextVar(transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
     props.setLook(wExecutionLinesReadField);
     wExecutionLinesReadField.addModifyListener(lsMod);
     FormData fdExecutionLinesReadField = new FormData();
     fdExecutionLinesReadField.top = new FormAttachment(lastControl, margin);
     fdExecutionLinesReadField.left = new FormAttachment(middle, 0); // To the right
     fdExecutionLinesReadField.right = new FormAttachment(100, 0);
     wExecutionLinesReadField.setLayoutData(fdExecutionLinesReadField);
     lastControl = wExecutionLinesReadField;


     // ExecutionLinesWrittenField
     //
     wlExecutionLinesWrittenField = new Label(wInputComposite, SWT.RIGHT);
     props.setLook(wlExecutionLinesWrittenField);
     wlExecutionLinesWrittenField.setText(BaseMessages.getString(PKG, "JobExecutorDialog.ExecutionLinesWrittenField.Label")); //-NLS-1$
     FormData fdlExecutionLinesWrittenField = new FormData();
     fdlExecutionLinesWrittenField.top = new FormAttachment(lastControl, margin);
     fdlExecutionLinesWrittenField.left = new FormAttachment(0, 0); // First one in the left
     fdlExecutionLinesWrittenField.right = new FormAttachment(middle, -margin);
     wlExecutionLinesWrittenField.setLayoutData(fdlExecutionLinesWrittenField);
     wExecutionLinesWrittenField = new TextVar(transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
     props.setLook(wExecutionLinesWrittenField);
     wExecutionLinesWrittenField.addModifyListener(lsMod);
     FormData fdExecutionLinesWrittenField = new FormData();
     fdExecutionLinesWrittenField.top = new FormAttachment(lastControl, margin);
     fdExecutionLinesWrittenField.left = new FormAttachment(middle, 0); // To the right
     fdExecutionLinesWrittenField.right = new FormAttachment(100, 0);
     wExecutionLinesWrittenField.setLayoutData(fdExecutionLinesWrittenField);
     lastControl = wExecutionLinesWrittenField;


     // ExecutionLinesInputField
     //
     wlExecutionLinesInputField = new Label(wInputComposite, SWT.RIGHT);
     props.setLook(wlExecutionLinesInputField);
     wlExecutionLinesInputField.setText(BaseMessages.getString(PKG, "JobExecutorDialog.ExecutionLinesInputField.Label")); //-NLS-1$
     FormData fdlExecutionLinesInputField = new FormData();
     fdlExecutionLinesInputField.top = new FormAttachment(lastControl, margin);
     fdlExecutionLinesInputField.left = new FormAttachment(0, 0); // First one in the left
     fdlExecutionLinesInputField.right = new FormAttachment(middle, -margin);
     wlExecutionLinesInputField.setLayoutData(fdlExecutionLinesInputField);
     wExecutionLinesInputField = new TextVar(transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
     props.setLook(wExecutionLinesInputField);
     wExecutionLinesInputField.addModifyListener(lsMod);
     FormData fdExecutionLinesInputField = new FormData();
     fdExecutionLinesInputField.top = new FormAttachment(lastControl, margin);
     fdExecutionLinesInputField.left = new FormAttachment(middle, 0); // To the right
     fdExecutionLinesInputField.right = new FormAttachment(100, 0);
     wExecutionLinesInputField.setLayoutData(fdExecutionLinesInputField);
     lastControl = wExecutionLinesInputField;


     // ExecutionLinesOutputField
     //
     wlExecutionLinesOutputField = new Label(wInputComposite, SWT.RIGHT);
     props.setLook(wlExecutionLinesOutputField);
     wlExecutionLinesOutputField.setText(BaseMessages.getString(PKG, "JobExecutorDialog.ExecutionLinesOutputField.Label")); //-NLS-1$
     FormData fdlExecutionLinesOutputField = new FormData();
     fdlExecutionLinesOutputField.top = new FormAttachment(lastControl, margin);
     fdlExecutionLinesOutputField.left = new FormAttachment(0, 0); // First one in the left
     fdlExecutionLinesOutputField.right = new FormAttachment(middle, -margin);
     wlExecutionLinesOutputField.setLayoutData(fdlExecutionLinesOutputField);
     wExecutionLinesOutputField = new TextVar(transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
     props.setLook(wExecutionLinesOutputField);
     wExecutionLinesOutputField.addModifyListener(lsMod);
     FormData fdExecutionLinesOutputField = new FormData();
     fdExecutionLinesOutputField.top = new FormAttachment(lastControl, margin);
     fdExecutionLinesOutputField.left = new FormAttachment(middle, 0); // To the right
     fdExecutionLinesOutputField.right = new FormAttachment(100, 0);
     wExecutionLinesOutputField.setLayoutData(fdExecutionLinesOutputField);
     lastControl = wExecutionLinesOutputField;


     // ExecutionLinesRejectedField
     //
     wlExecutionLinesRejectedField = new Label(wInputComposite, SWT.RIGHT);
     props.setLook(wlExecutionLinesRejectedField);
     wlExecutionLinesRejectedField.setText(BaseMessages.getString(PKG, "JobExecutorDialog.ExecutionLinesRejectedField.Label")); //-NLS-1$
     FormData fdlExecutionLinesRejectedField = new FormData();
     fdlExecutionLinesRejectedField.top = new FormAttachment(lastControl, margin);
     fdlExecutionLinesRejectedField.left = new FormAttachment(0, 0); // First one in the left
     fdlExecutionLinesRejectedField.right = new FormAttachment(middle, -margin);
     wlExecutionLinesRejectedField.setLayoutData(fdlExecutionLinesRejectedField);
     wExecutionLinesRejectedField = new TextVar(transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
     props.setLook(wExecutionLinesRejectedField);
     wExecutionLinesRejectedField.addModifyListener(lsMod);
     FormData fdExecutionLinesRejectedField = new FormData();
     fdExecutionLinesRejectedField.top = new FormAttachment(lastControl, margin);
     fdExecutionLinesRejectedField.left = new FormAttachment(middle, 0); // To the right
     fdExecutionLinesRejectedField.right = new FormAttachment(100, 0);
     wExecutionLinesRejectedField.setLayoutData(fdExecutionLinesRejectedField);
     lastControl = wExecutionLinesRejectedField;


     // ExecutionLinesUpdatedField
     //
     wlExecutionLinesUpdatedField = new Label(wInputComposite, SWT.RIGHT);
     props.setLook(wlExecutionLinesUpdatedField);
     wlExecutionLinesUpdatedField.setText(BaseMessages.getString(PKG, "JobExecutorDialog.ExecutionLinesUpdatedField.Label")); //-NLS-1$
     FormData fdlExecutionLinesUpdatedField = new FormData();
     fdlExecutionLinesUpdatedField.top = new FormAttachment(lastControl, margin);
     fdlExecutionLinesUpdatedField.left = new FormAttachment(0, 0); // First one in the left
     fdlExecutionLinesUpdatedField.right = new FormAttachment(middle, -margin);
     wlExecutionLinesUpdatedField.setLayoutData(fdlExecutionLinesUpdatedField);
     wExecutionLinesUpdatedField = new TextVar(transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
     props.setLook(wExecutionLinesUpdatedField);
     wExecutionLinesUpdatedField.addModifyListener(lsMod);
     FormData fdExecutionLinesUpdatedField = new FormData();
     fdExecutionLinesUpdatedField.top = new FormAttachment(lastControl, margin);
     fdExecutionLinesUpdatedField.left = new FormAttachment(middle, 0); // To the right
     fdExecutionLinesUpdatedField.right = new FormAttachment(100, 0);
     wExecutionLinesUpdatedField.setLayoutData(fdExecutionLinesUpdatedField);
     lastControl = wExecutionLinesUpdatedField;


     // ExecutionLinesDeletedField
     //
     wlExecutionLinesDeletedField = new Label(wInputComposite, SWT.RIGHT);
     props.setLook(wlExecutionLinesDeletedField);
     wlExecutionLinesDeletedField.setText(BaseMessages.getString(PKG, "JobExecutorDialog.ExecutionLinesDeletedField.Label")); //-NLS-1$
     FormData fdlExecutionLinesDeletedField = new FormData();
     fdlExecutionLinesDeletedField.top = new FormAttachment(lastControl, margin);
     fdlExecutionLinesDeletedField.left = new FormAttachment(0, 0); // First one in the left
     fdlExecutionLinesDeletedField.right = new FormAttachment(middle, -margin);
     wlExecutionLinesDeletedField.setLayoutData(fdlExecutionLinesDeletedField);
     wExecutionLinesDeletedField = new TextVar(transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
     props.setLook(wExecutionLinesDeletedField);
     wExecutionLinesDeletedField.addModifyListener(lsMod);
     FormData fdExecutionLinesDeletedField = new FormData();
     fdExecutionLinesDeletedField.top = new FormAttachment(lastControl, margin);
     fdExecutionLinesDeletedField.left = new FormAttachment(middle, 0); // To the right
     fdExecutionLinesDeletedField.right = new FormAttachment(100, 0);
     wExecutionLinesDeletedField.setLayoutData(fdExecutionLinesDeletedField);
     lastControl = wExecutionLinesDeletedField;


     // ExecutionFilesRetrievedField
     //
     wlExecutionFilesRetrievedField = new Label(wInputComposite, SWT.RIGHT);
     props.setLook(wlExecutionFilesRetrievedField);
     wlExecutionFilesRetrievedField.setText(BaseMessages.getString(PKG, "JobExecutorDialog.ExecutionFilesRetrievedField.Label")); //-NLS-1$
     FormData fdlExecutionFilesRetrievedField = new FormData();
     fdlExecutionFilesRetrievedField.top = new FormAttachment(lastControl, margin);
     fdlExecutionFilesRetrievedField.left = new FormAttachment(0, 0); // First one in the left
     fdlExecutionFilesRetrievedField.right = new FormAttachment(middle, -margin);
     wlExecutionFilesRetrievedField.setLayoutData(fdlExecutionFilesRetrievedField);
     wExecutionFilesRetrievedField = new TextVar(transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
     props.setLook(wExecutionFilesRetrievedField);
     wExecutionFilesRetrievedField.addModifyListener(lsMod);
     FormData fdExecutionFilesRetrievedField = new FormData();
     fdExecutionFilesRetrievedField.top = new FormAttachment(lastControl, margin);
     fdExecutionFilesRetrievedField.left = new FormAttachment(middle, 0); // To the right
     fdExecutionFilesRetrievedField.right = new FormAttachment(100, 0);
     wExecutionFilesRetrievedField.setLayoutData(fdExecutionFilesRetrievedField);
     lastControl = wExecutionFilesRetrievedField;


     // ExecutionExitStatusField
     //
     wlExecutionExitStatusField = new Label(wInputComposite, SWT.RIGHT);
     props.setLook(wlExecutionExitStatusField);
     wlExecutionExitStatusField.setText(BaseMessages.getString(PKG, "JobExecutorDialog.ExecutionExitStatusField.Label")); //-NLS-1$
     FormData fdlExecutionExitStatusField = new FormData();
     fdlExecutionExitStatusField.top = new FormAttachment(lastControl, margin);
     fdlExecutionExitStatusField.left = new FormAttachment(0, 0); // First one in the left
     fdlExecutionExitStatusField.right = new FormAttachment(middle, -margin);
     wlExecutionExitStatusField.setLayoutData(fdlExecutionExitStatusField);
     wExecutionExitStatusField = new TextVar(transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
     props.setLook(wExecutionExitStatusField);
     wExecutionExitStatusField.addModifyListener(lsMod);
     FormData fdExecutionExitStatusField = new FormData();
     fdExecutionExitStatusField.top = new FormAttachment(lastControl, margin);
     fdExecutionExitStatusField.left = new FormAttachment(middle, 0); // To the right
     fdExecutionExitStatusField.right = new FormAttachment(100, 0);
     wExecutionExitStatusField.setLayoutData(fdExecutionExitStatusField);
     lastControl = wExecutionExitStatusField;


     // ExecutionLogTextField
     //
     wlExecutionLogTextField = new Label(wInputComposite, SWT.RIGHT);
     props.setLook(wlExecutionLogTextField);
     wlExecutionLogTextField.setText(BaseMessages.getString(PKG, "JobExecutorDialog.ExecutionLogTextField.Label")); //-NLS-1$
     FormData fdlExecutionLogTextField = new FormData();
     fdlExecutionLogTextField.top = new FormAttachment(lastControl, margin);
     fdlExecutionLogTextField.left = new FormAttachment(0, 0); // First one in the left
     fdlExecutionLogTextField.right = new FormAttachment(middle, -margin);
     wlExecutionLogTextField.setLayoutData(fdlExecutionLogTextField);
     wExecutionLogTextField = new TextVar(transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
     props.setLook(wExecutionLogTextField);
     wExecutionLogTextField.addModifyListener(lsMod);
     FormData fdExecutionLogTextField = new FormData();
     fdExecutionLogTextField.top = new FormAttachment(lastControl, margin);
     fdExecutionLogTextField.left = new FormAttachment(middle, 0); // To the right
     fdExecutionLogTextField.right = new FormAttachment(100, 0);
     wExecutionLogTextField.setLayoutData(fdExecutionLogTextField);
     lastControl = wExecutionLogTextField;

     // ExecutionLogChannelIdField
     //
     wlExecutionLogChannelIdField = new Label(wInputComposite, SWT.RIGHT);
     props.setLook(wlExecutionLogChannelIdField);
     wlExecutionLogChannelIdField.setText(BaseMessages.getString(PKG, "JobExecutorDialog.ExecutionLogChannelIdField.Label")); //-NLS-1$
     FormData fdlExecutionLogChannelIdField = new FormData();
     fdlExecutionLogChannelIdField.top = new FormAttachment(lastControl, margin);
     fdlExecutionLogChannelIdField.left = new FormAttachment(0, 0); // First one in the left
     fdlExecutionLogChannelIdField.right = new FormAttachment(middle, -margin);
     wlExecutionLogChannelIdField.setLayoutData(fdlExecutionLogChannelIdField);
     wExecutionLogChannelIdField = new TextVar(transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
     props.setLook(wExecutionLogChannelIdField);
     wExecutionLogChannelIdField.addModifyListener(lsMod);
     FormData fdExecutionLogChannelIdField = new FormData();
     fdExecutionLogChannelIdField.top = new FormAttachment(lastControl, margin);
     fdExecutionLogChannelIdField.left = new FormAttachment(middle, 0); // To the right
     fdExecutionLogChannelIdField.right = new FormAttachment(100, 0);
     wExecutionLogChannelIdField.setLayoutData(fdExecutionLogChannelIdField);
     lastControl = wExecutionLogChannelIdField;
     
     wInputComposite.pack();
     Rectangle bounds = wInputComposite.getBounds();

     scrolledComposite.setContent(wInputComposite);
     scrolledComposite.setExpandHorizontal(true);
     scrolledComposite.setExpandVertical(true);
     scrolledComposite.setMinWidth(bounds.width);
     scrolledComposite.setMinHeight(bounds.height);

     wTab.setControl(scrolledComposite);
     wTabFolder.setSelection(wTab);
   }

   private void addResultFilesTab() {

     final CTabItem wTab = new CTabItem(wTabFolder, SWT.NONE);
     wTab.setText(BaseMessages.getString(PKG, "JobExecutorDialog.ResultFiles.Title")); //$NON-NLS-1$
     wTab.setToolTipText(BaseMessages.getString(PKG, "JobExecutorDialog.ResultFiles.Tooltip")); //$NON-NLS-1$
     
     ScrolledComposite scrolledComposite = new ScrolledComposite(wTabFolder, SWT.V_SCROLL | SWT.H_SCROLL);
     scrolledComposite.setLayout(new FillLayout());
     
     Composite wInputComposite = new Composite(scrolledComposite, SWT.NONE);
     props.setLook(wInputComposite);

     FormLayout tabLayout = new FormLayout();
     tabLayout.marginWidth = Const.FORM_MARGIN;
     tabLayout.marginHeight = Const.FORM_MARGIN;
     wInputComposite.setLayout(tabLayout);

     wlResultFilesTarget = new Label(wInputComposite, SWT.RIGHT);
     props.setLook(wlResultFilesTarget);
     wlResultFilesTarget.setText(BaseMessages.getString(PKG, "JobExecutorDialog.ResultFilesTarget.Label")); //-NLS-1$
     FormData fdlResultFilesTarget = new FormData();
     fdlResultFilesTarget.top = new FormAttachment(0, 0);
     fdlResultFilesTarget.left = new FormAttachment(0, 0); // First one in the left
     fdlResultFilesTarget.right = new FormAttachment(middle, -margin);
     wlResultFilesTarget.setLayoutData(fdlResultFilesTarget);
     wResultFilesTarget = new CCombo(wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
     props.setLook(wResultFilesTarget);
     wResultFilesTarget.addModifyListener(lsMod);
     FormData fdResultFilesTarget = new FormData();
     fdResultFilesTarget.top = new FormAttachment(0, 0);
     fdResultFilesTarget.left = new FormAttachment(middle, 0); // To the right
     fdResultFilesTarget.right = new FormAttachment(100, 0);
     wResultFilesTarget.setLayoutData(fdResultFilesTarget);
     Control lastControl = wResultFilesTarget;

     // ResultFileNameField
     //
     wlResultFileNameField = new Label(wInputComposite, SWT.RIGHT);
     props.setLook(wlResultFileNameField);
     wlResultFileNameField.setText(BaseMessages.getString(PKG, "JobExecutorDialog.ResultFileNameField.Label")); //-NLS-1$
     FormData fdlResultFileNameField = new FormData();
     fdlResultFileNameField.top = new FormAttachment(lastControl, margin);
     fdlResultFileNameField.left = new FormAttachment(0, 0); // First one in the left
     fdlResultFileNameField.right = new FormAttachment(middle, -margin);
     wlResultFileNameField.setLayoutData(fdlResultFileNameField);
     wResultFileNameField = new TextVar(transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
     props.setLook(wResultFileNameField);
     wResultFileNameField.addModifyListener(lsMod);
     FormData fdResultFileNameField = new FormData();
     fdResultFileNameField.top = new FormAttachment(lastControl, margin);
     fdResultFileNameField.left = new FormAttachment(middle, 0); // To the right
     fdResultFileNameField.right = new FormAttachment(100, 0);
     wResultFileNameField.setLayoutData(fdResultFileNameField);
     lastControl = wResultFileNameField;

     wInputComposite.pack();
     Rectangle bounds = wInputComposite.getBounds();

     scrolledComposite.setContent(wInputComposite);
     scrolledComposite.setExpandHorizontal(true);
     scrolledComposite.setExpandVertical(true);
     scrolledComposite.setMinWidth(bounds.width);
     scrolledComposite.setMinHeight(bounds.height);

     wTab.setControl(scrolledComposite);
     wTabFolder.setSelection(wTab);
   }

   private void addResultRowsTab() {

     final CTabItem wTab = new CTabItem(wTabFolder, SWT.NONE);
     wTab.setText(BaseMessages.getString(PKG, "JobExecutorDialog.ResultRows.Title")); //$NON-NLS-1$
     wTab.setToolTipText(BaseMessages.getString(PKG, "JobExecutorDialog.ResultRows.Tooltip")); //$NON-NLS-1$
     
     ScrolledComposite scrolledComposite = new ScrolledComposite(wTabFolder, SWT.V_SCROLL | SWT.H_SCROLL);
     scrolledComposite.setLayout(new FillLayout());
     
     Composite wInputComposite = new Composite(scrolledComposite, SWT.NONE);
     props.setLook(wInputComposite);

     FormLayout tabLayout = new FormLayout();
     tabLayout.marginWidth = Const.FORM_MARGIN;
     tabLayout.marginHeight = Const.FORM_MARGIN;
     wInputComposite.setLayout(tabLayout);

     wlResultRowsTarget = new Label(wInputComposite, SWT.RIGHT);
     props.setLook(wlResultRowsTarget);
     wlResultRowsTarget.setText(BaseMessages.getString(PKG, "JobExecutorDialog.ResultRowsTarget.Label")); //-NLS-1$
     FormData fdlResultRowsTarget = new FormData();
     fdlResultRowsTarget.top = new FormAttachment(0, 0);
     fdlResultRowsTarget.left = new FormAttachment(0, 0); // First one in the left
     fdlResultRowsTarget.right = new FormAttachment(middle, -margin);
     wlResultRowsTarget.setLayoutData(fdlResultRowsTarget);
     wResultRowsTarget = new CCombo(wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
     props.setLook(wResultRowsTarget);
     wResultRowsTarget.addModifyListener(lsMod);
     FormData fdResultRowsTarget = new FormData();
     fdResultRowsTarget.top = new FormAttachment(0, 0);
     fdResultRowsTarget.left = new FormAttachment(middle, 0); // To the right
     fdResultRowsTarget.right = new FormAttachment(100, 0);
     wResultRowsTarget.setLayoutData(fdResultRowsTarget);
     Control lastControl = wResultRowsTarget;

     wlResultFields=new Label(wInputComposite, SWT.NONE);
     wlResultFields.setText(BaseMessages.getString(PKG, "JobExecutorDialog.ResultFields.Label")); //$NON-NLS-1$
     props.setLook(wlResultFields);
     FormData fdlResultFields = new FormData();
     fdlResultFields.left  = new FormAttachment(0, 0);
     fdlResultFields.top   = new FormAttachment(lastControl, margin);
     wlResultFields.setLayoutData(fdlResultFields);
     
     int nrRows= (jobExecutorMeta.getResultRowsField()!=null?jobExecutorMeta.getResultRowsField().length:1);
     
     ColumnInfo[] ciResultFields=new ColumnInfo[] {
      new ColumnInfo(BaseMessages.getString(PKG, "JobExecutorDialog.ColumnInfo.Field"),  ColumnInfo.COLUMN_TYPE_TEXT,   false, false), //$NON-NLS-1$
      new ColumnInfo(BaseMessages.getString(PKG, "JobExecutorDialog.ColumnInfo.Type"),   ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getTypes()), //$NON-NLS-1$
      new ColumnInfo(BaseMessages.getString(PKG, "JobExecutorDialog.ColumnInfo.Length"), ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
      new ColumnInfo(BaseMessages.getString(PKG, "JobExecutorDialog.ColumnInfo.Length"), ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
     };
     
     wResultRowsFields=new TableView(transMeta, wInputComposite, 
                           SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, 
                           ciResultFields, 
                           nrRows,  
                           lsMod,
                           props
                           );

     FormData fdResultFields = new FormData();
     fdResultFields.left  = new FormAttachment(0, 0);
     fdResultFields.top   = new FormAttachment(wlResultFields, margin);
     fdResultFields.right = new FormAttachment(100, 0);
     fdResultFields.bottom= new FormAttachment(100, 0);
     wResultRowsFields.setLayoutData(fdResultFields);
     
     wInputComposite.pack();
     Rectangle bounds = wInputComposite.getBounds();

     scrolledComposite.setContent(wInputComposite);
     scrolledComposite.setExpandHorizontal(true);
     scrolledComposite.setExpandVertical(true);
     scrolledComposite.setMinWidth(bounds.width);
     scrolledComposite.setMinHeight(bounds.height);

     wTab.setControl(scrolledComposite);
     wTabFolder.setSelection(wTab);
   }

  private void setFlags() {
	  // Enable/disable fields...
    //
    if (wlGroupSize==null || wlGroupSize==null || wlGroupField==null || wGroupField==null || wlGroupTime==null || wGroupTime==null) {
      return;
    }
    boolean enableSize = Const.toInt(transMeta.environmentSubstitute(wGroupSize.getText()), -1)>=0;
    boolean enableField = !Const.isEmpty(wGroupField.getText());
    // boolean enableTime = Const.toInt(transMeta.environmentSubstitute(wGroupTime.getText()), -1)>0;
    
    wlGroupSize.setEnabled(true);
    wGroupSize.setEnabled(true);
    wlGroupField.setEnabled(!enableSize);
    wGroupField.setEnabled(!enableSize);
    wlGroupTime.setEnabled(!enableSize && !enableField);
    wGroupTime.setEnabled(!enableSize && !enableField);
  }

	private void cancel()
	{
		stepname = null;
		jobExecutorMeta.setChanged(changed);
		dispose();
	}

	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		stepname = wStepname.getText(); // return value

		try
		{
			loadJob();
		} catch (KettleException e)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "JobExecutorDialog.ErrorLoadingSpecifiedJob.Title"), BaseMessages.getString(PKG, "JobExecutorDialog.ErrorLoadingSpecifiedJob.Message"), e);
		}
		
    jobExecutorMeta.setSpecificationMethod(specificationMethod);
    switch(specificationMethod) {
    case FILENAME:
      jobExecutorMeta.setFileName(wFilename.getText());
      jobExecutorMeta.setDirectoryPath(null);
      jobExecutorMeta.setJobName(null);
      jobExecutorMeta.setJobObjectId(null);
      break;
    case REPOSITORY_BY_NAME:
      jobExecutorMeta.setDirectoryPath(wDirectory.getText());
      jobExecutorMeta.setJobName(wJobname.getText());
      jobExecutorMeta.setFileName(null);
      jobExecutorMeta.setJobObjectId(null);
      break;
    case REPOSITORY_BY_REFERENCE:
      jobExecutorMeta.setFileName(null);
      jobExecutorMeta.setDirectoryPath(null);
      jobExecutorMeta.setJobName(null);
      jobExecutorMeta.setJobObjectId(referenceObjectId);
      break;
    }

		// Load the information on the tabs, optionally do some
		// verifications...
		// 
		collectInformation();

		// Set the input steps for input mappings
		jobExecutorMeta.searchInfoAndTargetSteps(transMeta.getSteps());

		jobExecutorMeta.setChanged(true);

		dispose();
	}

	private void collectInformation()
	{
		// The parameters...
	  //
	  JobExecutorParameters parameters = jobExecutorMeta.getParameters();
	  
    int nrLines = wJobExecutorParameters.nrNonEmpty();
    String variables[] = new String[nrLines];
    String fields[] = new String[nrLines];
    String input[] = new String[nrLines];
    parameters.setVariable(variables);
    parameters.setField(fields);
    parameters.setInput(input);
    for (int i = 0; i < nrLines; i++) {
      TableItem item = wJobExecutorParameters.getNonEmpty(i);
      variables[i] = item.getText(1);
      fields[i] = item.getText(2);
      input[i] = item.getText(3);
    }
    parameters.setInheritingAllVariables(wInheritAll.getSelection());
    
    // The group definition
    //
    jobExecutorMeta.setGroupSize(wGroupSize.getText());
    jobExecutorMeta.setGroupField(wGroupField.getText());
    jobExecutorMeta.setGroupTime(wGroupTime.getText());
    
    jobExecutorMeta.setExecutionResultTargetStepMeta(transMeta.findStep(wExecutionResultTarget.getText()));
    jobExecutorMeta.setExecutionTimeField(wExecutionTimeField.getText());
    jobExecutorMeta.setExecutionResultField(wExecutionResultField.getText());
    jobExecutorMeta.setExecutionNrErrorsField(wExecutionNrErrorsField.getText());
    jobExecutorMeta.setExecutionLinesReadField(wExecutionLinesReadField.getText());
    jobExecutorMeta.setExecutionLinesWrittenField(wExecutionLinesWrittenField.getText());
    jobExecutorMeta.setExecutionLinesInputField(wExecutionLinesInputField.getText());
    jobExecutorMeta.setExecutionLinesOutputField(wExecutionLinesOutputField.getText());
    jobExecutorMeta.setExecutionLinesRejectedField(wExecutionLinesRejectedField.getText());
    jobExecutorMeta.setExecutionLinesUpdatedField(wExecutionLinesUpdatedField.getText());
    jobExecutorMeta.setExecutionLinesDeletedField(wExecutionLinesDeletedField.getText());
    jobExecutorMeta.setExecutionFilesRetrievedField(wExecutionFilesRetrievedField.getText());
    jobExecutorMeta.setExecutionExitStatusField(wExecutionExitStatusField.getText());
    jobExecutorMeta.setExecutionLogTextField(wExecutionLogTextField.getText());
    jobExecutorMeta.setExecutionLogChannelIdField(wExecutionLogChannelIdField.getText());
    
    jobExecutorMeta.setResultFilesTargetStepMeta(transMeta.findStep(wResultFilesTarget.getText()));
    jobExecutorMeta.setResultFilesFileNameField(wResultFileNameField.getText());
    
    // Result row info
    //
    jobExecutorMeta.setResultRowsTargetStepMeta(transMeta.findStep(wResultRowsTarget.getText()));
    int nrFields = wResultRowsFields.nrNonEmpty();
    jobExecutorMeta.setResultRowsField(new String[nrFields]);
    jobExecutorMeta.setResultRowsType(new int[nrFields]);
    jobExecutorMeta.setResultRowsLength(new int[nrFields]);
    jobExecutorMeta.setResultRowsPrecision(new int[nrFields]);
    
    for (int i=0;i<nrFields;i++) {
      TableItem item = wResultRowsFields.getNonEmpty(i);
      jobExecutorMeta.getResultRowsField()[i] = item.getText(1);
      jobExecutorMeta.getResultRowsType()[i] = ValueMeta.getType(item.getText(2));
      jobExecutorMeta.getResultRowsLength()[i] = Const.toInt(item.getText(3), -1);
      jobExecutorMeta.getResultRowsPrecision()[i] = Const.toInt(item.getText(4), -1);
    }

	}
}
