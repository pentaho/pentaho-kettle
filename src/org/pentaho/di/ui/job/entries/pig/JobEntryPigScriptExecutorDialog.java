/*
 * Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.ui.job.entries.pig;

import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.pig.JobEntryPigScriptExecutor;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.hadoop.jobconf.HadoopConfigurer;
import org.pentaho.hadoop.jobconf.HadoopConfigurerFactory;

/**
 * Job entry dialog for the PigScriptExecutor -  job entry that executes 
 * a Pig script either on a hadoop cluster or locally.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 */
public class JobEntryPigScriptExecutorDialog extends JobEntryDialog implements
    JobEntryDialogInterface {
  
  private static final Class<?> PKG = JobEntryPigScriptExecutor.class;
  
  public static final String PIG_FILE_EXT = ".pig";
  
  private Display m_display;
  private boolean m_backupChanged;
  
  private Text m_wName;
  
  private CCombo m_distroCombo;

  private Label m_hdfsLab;
  private TextVar m_hdfsHostname;
  private Label m_hdfsPortLab;
  private TextVar m_hdfsPort;
  
  private Label m_jobTrackerLab;
  private TextVar m_jobTrackerHostname;
  private Label m_jobTrackerPortLab;
  private TextVar m_jobTrackerPort;
  
  private TextVar m_pigScriptText;
  private Button m_pigScriptBrowseBut;
  
  private Button m_enableBlockingBut;
  
  private Button m_localExecutionBut;
  
  private TableView m_scriptParams;
  
  protected JobEntryPigScriptExecutor m_jobEntry;
  
  /**
   * Constructor.
   * 
   * @param parent parent shell
   * @param jobEntryInt the job entry that this dialog edits
   * @param rep a repository
   * @param jobMeta job meta data
   */
  public JobEntryPigScriptExecutorDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta) {
    super(parent, jobEntryInt, rep, jobMeta);
    m_jobEntry = (JobEntryPigScriptExecutor) jobEntryInt;
  }

  public JobEntryInterface open() {
    
    Shell parent = getParent();
    m_display = parent.getDisplay();

    shell = new Shell(parent, props.getJobsDialogStyle());
    props.setLook(shell);
    JobDialog.setShellImage(shell, m_jobEntry);

    ModifyListener lsMod = new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_jobEntry.setChanged();
      }
    };
    
    m_backupChanged = m_jobEntry.hasChanged();
    
    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout(formLayout);
    shell.setText("Pig script executor");

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;
    
    // Name line
    Label nameLineL = new Label(shell, SWT.RIGHT);
    nameLineL.setText(BaseMessages.getString(PKG, "JobEntryDialog.Title"));
    props.setLook(nameLineL);
    FormData fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(0, 0);
    fd.right = new FormAttachment(middle, -margin);
    nameLineL.setLayoutData(fd);
    
    m_wName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(m_wName);
    m_wName.addModifyListener(lsMod);
    fd = new FormData();
    fd.top = new FormAttachment(0, 0);
    fd.left = new FormAttachment(middle, 0);
    fd.right = new FormAttachment(100, 0);
    m_wName.setLayoutData(fd);
    
    // distro line
    Label distroLab = new Label(shell, SWT.RIGHT);
    props.setLook(distroLab);
    distroLab.setText(BaseMessages.getString(PKG, 
    "JobEntryPigScriptExecutor.HadoopDistribution.Label"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_wName, margin);
    fd.right = new FormAttachment(middle, -margin);
    distroLab.setLayoutData(fd);
    
    m_distroCombo = new CCombo(shell, SWT.BORDER);
    props.setLook(m_distroCombo);
    m_distroCombo.setEditable(false);
    
    try {
      // auto detected first
      HadoopConfigurer auto = HadoopConfigurerFactory.locateConfigurer();
      if (auto != null) {
        m_distroCombo.add(auto.distributionName());
      } else {
        List<HadoopConfigurer> available = HadoopConfigurerFactory.getAvailableConfigurers();
        for (HadoopConfigurer config : available) {
          m_distroCombo.add(config.distributionName());
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(m_wName, margin);
    fd.right = new FormAttachment(100, 0);
    m_distroCombo.setLayoutData(fd);
    
    // hdfs line
    m_hdfsLab = new Label(shell, SWT.RIGHT);
    props.setLook(m_hdfsLab);
    m_hdfsLab.setText(BaseMessages.getString(PKG, 
        "JobEntryPigScriptExecutor.HDFSHostname.Label"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_distroCombo, margin);
    fd.right = new FormAttachment(middle, -margin);
    m_hdfsLab.setLayoutData(fd);
    
    m_hdfsHostname = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(m_hdfsHostname);
    m_hdfsHostname.addModifyListener(lsMod);
    
    // set the tool tip to the contents with any env variables expanded
    m_hdfsHostname.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_hdfsHostname.setToolTipText(jobMeta.environmentSubstitute(m_hdfsHostname.getText()));
      }
    });
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_distroCombo, margin);
    fd.left = new FormAttachment(middle, 0);
    m_hdfsHostname.setLayoutData(fd);
    
    // hdfs port line
    m_hdfsPortLab = new Label(shell, SWT.RIGHT);
    props.setLook(m_hdfsPortLab);
    m_hdfsPortLab.setText(BaseMessages.getString(PKG, 
        "JobEntryPigScriptExecutor.HDFSPort.Label"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_hdfsHostname, margin);
    fd.right = new FormAttachment(middle, -margin);
    m_hdfsPortLab.setLayoutData(fd);
    
    m_hdfsPort = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(m_hdfsPort);
    m_hdfsPort.addModifyListener(lsMod);
    // set the tool tip to the contents with any env variables expanded
    m_hdfsPort.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_hdfsPort.setToolTipText(jobMeta.environmentSubstitute(m_hdfsPort.getText()));
      }
    });
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_hdfsHostname, margin);
    fd.left = new FormAttachment(middle, 0);
    m_hdfsPort.setLayoutData(fd);
    
    // job tracker line
    m_jobTrackerLab = new Label(shell, SWT.RIGHT);
    props.setLook(m_jobTrackerLab);
    m_jobTrackerLab.setText(BaseMessages.getString(PKG, 
        "JobEntryPigScriptExecutor.JobtrackerHostname.Label"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_hdfsPort, margin);
    fd.right = new FormAttachment(middle, -margin);
    m_jobTrackerLab.setLayoutData(fd);
    
    m_jobTrackerHostname = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(m_jobTrackerHostname);
    m_jobTrackerHostname.addModifyListener(lsMod);
    // set the tool tip to the contents with any env variables expanded
    m_jobTrackerHostname.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_jobTrackerHostname.
          setToolTipText(jobMeta.environmentSubstitute(m_jobTrackerHostname.getText()));
      }
    });
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_hdfsPort, margin);
    fd.left = new FormAttachment(middle, 0);
    m_jobTrackerHostname.setLayoutData(fd);
    
    m_jobTrackerPortLab = new Label(shell, SWT.RIGHT);
    props.setLook(m_jobTrackerPortLab);
    m_jobTrackerPortLab.setText(BaseMessages.getString(PKG, 
        "JobEntryPigScriptExecutor.JobtrackerPort.Label"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_jobTrackerHostname, margin);
    fd.right = new FormAttachment(middle, -margin);
    m_jobTrackerPortLab.setLayoutData(fd);
    
    m_jobTrackerPort = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(m_jobTrackerPort);
    m_jobTrackerPort.addModifyListener(lsMod);
    // set the tool tip to the contents with any env variables expanded
    m_jobTrackerPort.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_jobTrackerPort.
          setToolTipText(jobMeta.environmentSubstitute(m_jobTrackerPort.getText()));
      }
    });
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_jobTrackerHostname, margin);
    fd.left = new FormAttachment(middle, 0);
    m_jobTrackerPort.setLayoutData(fd);
    
    // script file line
    Label scriptFileLab = new Label(shell, SWT.RIGHT);
    props.setLook(scriptFileLab);
    scriptFileLab.setText(BaseMessages.getString(PKG, 
        "JobEntryPigScriptExecutor.PigScript.Label"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_jobTrackerPort, margin);
    fd.right = new FormAttachment(middle, -margin);
    scriptFileLab.setLayoutData(fd);
    
    m_pigScriptBrowseBut = new Button(shell, SWT.PUSH | SWT.CENTER);
    props.setLook(m_pigScriptBrowseBut);
    m_pigScriptBrowseBut.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_jobTrackerPort, 0);
    m_pigScriptBrowseBut.setLayoutData(fd);
    m_pigScriptBrowseBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        openDialog();
      }
    });
    
    m_pigScriptText = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(m_pigScriptText);
    m_pigScriptText.addModifyListener(lsMod);
    m_pigScriptText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_pigScriptText.setToolTipText(jobMeta.environmentSubstitute(m_pigScriptText.getText()));
      }
    });
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(m_jobTrackerPort, margin);
    fd.right = new FormAttachment(m_pigScriptBrowseBut, -margin);
    m_pigScriptText.setLayoutData(fd);
    
    // blocking line
    Label enableBlockingLab = new Label(shell, SWT.RIGHT);
    props.setLook(enableBlockingLab);
    enableBlockingLab.setText(BaseMessages.getString(PKG, 
        "JobEntryPigScriptExecutor.EnableBlocking.Label"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_pigScriptText, margin);
    fd.right = new FormAttachment(middle, -margin);
    enableBlockingLab.setLayoutData(fd);
    
    m_enableBlockingBut = new Button(shell, SWT.CHECK);
    props.setLook(m_enableBlockingBut);
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(m_pigScriptText, margin);
    m_enableBlockingBut.setLayoutData(fd);
    m_enableBlockingBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        m_jobEntry.setChanged();
      }
    });
    
    // local execution line
    Label localExecutionLab = new Label(shell, SWT.RIGHT);
    props.setLook(localExecutionLab);
    localExecutionLab.setText(BaseMessages.getString(PKG, 
        "JobEntryPigScriptExecutor.LocalExecution.Label"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_enableBlockingBut, margin);
    fd.right = new FormAttachment(middle, -margin);
    localExecutionLab.setLayoutData(fd);
    
    m_localExecutionBut = new Button(shell, SWT.CHECK);
    props.setLook(m_localExecutionBut);
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(m_enableBlockingBut, margin);
    m_localExecutionBut.setLayoutData(fd);
    m_localExecutionBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        m_jobEntry.setChanged();
        setEnabledStatus();
      }
    });
    
    // script parameters -----------------
    Group paramsGroup = new Group(shell, SWT.SHADOW_ETCHED_IN);
    paramsGroup.setText(BaseMessages.getString(PKG, 
        "JobEntryPigScriptExecutor.ScriptParameters.Label"));
    FormLayout paramsLayout = new FormLayout();
    paramsGroup.setLayout(paramsLayout);
    props.setLook(paramsGroup);
    
    fd = new FormData();
    fd.top = new FormAttachment(m_localExecutionBut, margin);
    fd.right = new FormAttachment(100, -margin);
    fd.left = new FormAttachment(0, 0);    
    fd.bottom = new FormAttachment(100, -margin*10);
    paramsGroup.setLayoutData(fd);
    
    ColumnInfo[] colinf = new ColumnInfo[] {
        new ColumnInfo(BaseMessages.getString(PKG, 
              "JobEntryPigScriptExecutor.ScriptParameters.ParamterName.Label"), 
            ColumnInfo.COLUMN_TYPE_TEXT, false),
            new ColumnInfo(BaseMessages.getString(PKG, 
                "JobEntryPigScriptExecutor.ScriptParameters.ParamterValue.Label"), 
                ColumnInfo.COLUMN_TYPE_TEXT, false)
    };
    
    m_scriptParams = new TableView(jobMeta, paramsGroup,
        SWT.FULL_SELECTION | SWT.MULTI, colinf, 1, lsMod, props);
    
    fd = new FormData();
    fd.top = new FormAttachment(0, margin);
    fd.right = new FormAttachment(100, -margin);
    fd.left = new FormAttachment(0, 0);
    fd.bottom = new FormAttachment(100, -margin);
    m_scriptParams.setLayoutData(fd);
    
    
    // ---- buttons ------------------------
    Button wOK = new Button(shell, SWT.PUSH);
    wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
    Button wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

    BaseStepDialog.
      positionBottomButtons(shell, new Button[] { wOK, wCancel }, 
          margin, paramsGroup);
    
    // Add listeners
    Listener lsCancel = new Listener() {
      public void handleEvent(Event e) {
        cancel();
      }
    };
    Listener lsOK = new Listener() {
      public void handleEvent(Event e) {
        ok();
      }
    };

    wOK.addListener(SWT.Selection, lsOK);
    wCancel.addListener(SWT.Selection, lsCancel);

    SelectionAdapter lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected(SelectionEvent e) {
        ok();
      }
    };
    m_wName.addSelectionListener(lsDef);
        
    
    
    
    // Detect [X] or ALT-F4 or something that kills this window...
    shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e) {
        //cancel();
      }
    });

    getData();

    BaseStepDialog.setSize(shell);

    shell.open();
    props.setDialogSize(shell, "JobTransDialogSize");
    while (!shell.isDisposed()) {
      if (!m_display.readAndDispatch())
        m_display.sleep();
    }
    
    return m_jobEntry;
  }
  
  private void cancel() {
    m_jobEntry.setChanged(m_backupChanged);

    m_jobEntry = null;
    dispose();
  }
  
  /**
   * Dispose this dialog
   */
  public void dispose() {
    WindowProperty winprop = new WindowProperty(shell);
    props.setScreen(winprop);
    shell.dispose();
  }
  
  protected void setEnabledStatus() {
    boolean local = m_localExecutionBut.getSelection();
    m_hdfsLab.setEnabled(!local);
    m_hdfsHostname.setEnabled(!local);
    m_hdfsPortLab.setEnabled(!local);
    m_hdfsPort.setEnabled(!local);
    m_jobTrackerLab.setEnabled(!local);
    m_jobTrackerHostname.setEnabled(!local);
    m_jobTrackerPortLab.setEnabled(!local);
    m_jobTrackerPort.setEnabled(!local);
  }
  
  protected void openDialog() {
    FileDialog openDialog = new FileDialog(shell, SWT.OPEN);
    openDialog.setFilterExtensions(new String[] {"*" + PIG_FILE_EXT, "*"});
    openDialog.setFilterNames(new String[] {"Pig script files", "All files"});
    
    //String prevName = jobMeta.environmentSubstitute(m_pigScriptText.getText());
    String parentFolder = null;
    
    try {
      parentFolder = KettleVFS.
        getFilename(KettleVFS.getFileObject(jobMeta.
            environmentSubstitute(jobMeta.getFilename())));
      
      if (!Const.isEmpty(parentFolder)) {
        openDialog.setFileName(parentFolder);
      }
    } catch (Exception ex) {
      
    }
    
    if (openDialog.open() != null) {
      m_pigScriptText.setText(openDialog.getFilterPath() 
          + System.getProperty("file.separator")
          + openDialog.getFileName());
    }    
  }
  
  protected void getData() {
    m_wName.setText(Const.NVL(m_jobEntry.getName(), ""));
    m_distroCombo.setText(m_jobEntry.getHadoopDistribution());
    if (!Const.isEmpty(m_jobEntry.getHDFSHostname())) {
      m_hdfsHostname.setText(m_jobEntry.getHDFSHostname());
    }
    if (!Const.isEmpty(m_jobEntry.getHDFSPort())) {
      m_hdfsPort.setText(m_jobEntry.getHDFSPort());
    }
    if (!Const.isEmpty(m_jobEntry.getJobTrackerHostname())) {
      m_jobTrackerHostname.setText(m_jobEntry.getJobTrackerHostname());
    }
    if (!Const.isEmpty(m_jobEntry.getJobTrackerPort())) {
      m_jobTrackerPort.setText(m_jobEntry.getJobTrackerPort());
    }     
    
    m_pigScriptText.setText(Const.NVL(m_jobEntry.getScriptFilename(), ""));
    m_enableBlockingBut.setSelection(m_jobEntry.getEnableBlocking());
    m_localExecutionBut.setSelection(m_jobEntry.getLocalExecution());
    
    HashMap<String, String> params = m_jobEntry.getScriptParameters();
    if (params.size() > 0) {
      for (String name : params.keySet()) {
        String value = params.get(name);
        TableItem item = new TableItem(m_scriptParams.table, SWT.NONE);
        item.setText(1, name);
        item.setText(2, value);
      }
    }
    
    m_scriptParams.removeEmptyRows();
    m_scriptParams.setRowNums();
    m_scriptParams.optWidth(true);
    
    setEnabledStatus();
  }
  
  protected void ok() {
    if (Const.isEmpty(m_wName.getText())) {
      MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
      mb.setText(BaseMessages.getString(PKG, "System.StepJobEntryNameMissing.Title"));
      mb.setMessage(BaseMessages.getString(PKG, "System.JobEntryNameMissing.Msg"));
      mb.open();
      return;
    }
    
    m_jobEntry.setName(m_wName.getText());
    m_jobEntry.setHadoopDistribution(m_distroCombo.getText());
    
    m_jobEntry.setHDFSHostname(m_hdfsHostname.getText());
    m_jobEntry.setHDFSPort(m_hdfsPort.getText());
    m_jobEntry.setJobTrackerHostname(m_jobTrackerHostname.getText());
    m_jobEntry.setJobTrackerPort(m_jobTrackerPort.getText());
    m_jobEntry.setScriptFilename(m_pigScriptText.getText());
    m_jobEntry.setEnableBlocking(m_enableBlockingBut.getSelection());
    m_jobEntry.setLocalExecution(m_localExecutionBut.getSelection());
    
    int numNonEmpty = m_scriptParams.nrNonEmpty();
    HashMap<String, String> params = new HashMap<String, String>();
    if (numNonEmpty > 0) {      
      for (int i = 0; i < numNonEmpty; i++) {
        TableItem item = m_scriptParams.getNonEmpty(i);
        String name = item.getText(1).trim();
        String value = item.getText(2).trim();
        
        params.put(name, value);
      }      
    }
    
    m_jobEntry.setScriptParameters(params);
    
    m_jobEntry.setChanged();
    dispose();    
  }
}
