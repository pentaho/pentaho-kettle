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

package org.pentaho.di.ui.repository.dialog;

import java.util.Date;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.imp.ImportRules;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.IRepositoryImporter;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryImportFeedbackInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * Takes care of displaying a dialog that will handle the wait while we are
 * importing a backup file from XML...
 * 
 * @author Matt
 * @since 03-jun-2005
 */
public class RepositoryImportProgressDialog extends Dialog implements ProgressMonitorListener, RepositoryImportFeedbackInterface {
  private static Class<?>              PKG           = RepositoryImportProgressDialog.class; 

  private Shell                        shell, parent;
  private Display                      display;
  private PropsUI                      props;

  private Label                        wLabel;
  private Text                         wLogging;
  private Button                       wClose;

  private boolean                      askOverwrite  = true;

  private String fileDirectory;
  private String[] filenames;
  private RepositoryDirectoryInterface baseDirectory;
  private Repository rep;
  private String versionComment;

  private ImportRules importRules;

  public RepositoryImportProgressDialog(Shell parent, int style, Repository rep, String fileDirectory, String[] filenames, RepositoryDirectoryInterface baseDirectory, String versionComment) {
    this(parent, style, rep, fileDirectory, filenames, baseDirectory, versionComment, new ImportRules());
  }

  public RepositoryImportProgressDialog(Shell parent, int style, Repository rep, String fileDirectory, String[] filenames, RepositoryDirectoryInterface baseDirectory, String versionComment, ImportRules importRules) {
    super(parent, style);

    this.props = PropsUI.getInstance();
    this.parent = parent;

    this.rep = rep;
    this.fileDirectory = fileDirectory;
    this.filenames = filenames;
    this.baseDirectory = baseDirectory;
    this.versionComment = versionComment;
    this.importRules = importRules;
  }

  public void open() {
    display = parent.getDisplay();

    shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
    props.setLook(shell);

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setText(BaseMessages.getString(PKG, "RepositoryImportDialog.Title"));
    shell.setImage(GUIResource.getInstance().getImageSpoon());
    shell.setLayout(formLayout);

    // 
    // The task line...
    // ////////////////////////////////////////////////////////////////

    wLabel = new Label(shell, SWT.LEFT);
    props.setLook(wLabel);

    FormData fdLabel = new FormData();
    fdLabel.left = new FormAttachment(0, 0);
    fdLabel.top = new FormAttachment(0, 0);
    fdLabel.right = new FormAttachment(100, 0);
    wLabel.setLayoutData(fdLabel);

    //
    // The close button...
    // ////////////////////////////////////////////////////////////////

    // Buttons
    wClose = new Button(shell, SWT.PUSH);
    wClose.setText(BaseMessages.getString(PKG, "System.Button.Close"));

    BaseStepDialog.positionBottomButtons(shell, new Button[] { wClose }, Const.MARGIN, (Control) null);

    wClose.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        dispose();
      }
    });

    // 
    // Then the logging...
    // ////////////////////////////////////////////////////////////////

    wLogging = new Text(shell, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
    props.setLook(wLabel);

    FormData fdLogging = new FormData();
    fdLogging.left = new FormAttachment(0, 0);
    fdLogging.top = new FormAttachment(wLabel, Const.MARGIN);
    fdLogging.right = new FormAttachment(100, 0);
    fdLogging.bottom = new FormAttachment(wClose, -Const.MARGIN);
    wLogging.setLayoutData(fdLogging);

    display.asyncExec(new Runnable() {
      public void run() {
        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter() {
          public void shellClosed(ShellEvent e) {
            dispose();
          }
        });
      }
    });

    BaseStepDialog.setSize(shell, 1024, 768, true);

    shell.open();

    display.asyncExec(new Runnable() {
      public void run() {
        IRepositoryImporter importer = rep.getImporter();
        importer.setImportRules(importRules);
        importer.importAll(RepositoryImportProgressDialog.this, fileDirectory, filenames, baseDirectory, false, false, versionComment);
      }
    });

    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }
  }

  public void dispose() {
    props.setScreen(new WindowProperty(shell));
    shell.dispose();
  }
  
  public void setLabel(String labelText) {
    wLabel.setText(labelText); 
  }
  
  public void updateDisplay() {
    shell.getDisplay().update();
  }
  
  public void showError(String title, String message, Exception e) {
    new ErrorDialog(shell, title, message, e);
  }
  
  public boolean transOverwritePrompt(TransMeta transMeta) {
    MessageDialogWithToggle md = new MessageDialogWithToggle(shell, BaseMessages.getString(PKG, "RepositoryImportDialog.OverwriteTrans.Title"), null, BaseMessages.getString(PKG, "RepositoryImportDialog.OverwriteTrans.Message", transMeta.getName()),
        MessageDialog.QUESTION, new String[] { BaseMessages.getString(PKG, "System.Button.Yes"), BaseMessages.getString(PKG, "System.Button.No") }, 1, BaseMessages.getString(PKG, "RepositoryImportDialog.DontAskAgain.Label"), !askOverwrite);
    MessageDialogWithToggle.setDefaultImage(GUIResource.getInstance().getImageSpoon());
    int answer = md.open();
    
    askOverwrite = !md.getToggleState();
    
    return (answer & 0xFF) == 0;
  }
  
  public boolean jobOverwritePrompt(JobMeta jobMeta) {
    MessageDialogWithToggle md = new MessageDialogWithToggle(shell, BaseMessages.getString(PKG, "RepositoryImportDialog.OverwriteJob.Title"), null, BaseMessages.getString(PKG, "RepositoryImportDialog.OverwriteJob.Message", jobMeta.getName()),
        MessageDialog.QUESTION, new String[] { BaseMessages.getString(PKG, "System.Button.Yes"), BaseMessages.getString(PKG, "System.Button.No") }, 1, BaseMessages.getString(PKG, "RepositoryImportDialog.DontAskAgain.Label"), !askOverwrite);
    MessageDialogWithToggle.setDefaultImage(GUIResource.getInstance().getImageSpoon());
    int answer = md.open();
    askOverwrite = !md.getToggleState();

    return (answer & 0xFF) == 0;
  }

  public void addLog(String line) {
    StringBuffer rest = new StringBuffer(wLogging.getText());
    rest.append(XMLHandler.date2string(new Date())).append(" : ");
    rest.append(line).append(Const.CR);
    wLogging.setText(rest.toString());
    wLogging.setSelection(rest.length()); // make it scroll
  }
  
  public boolean askContinueOnErrorQuestion(String title, String message) {
    MessageBox mb = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
    mb.setMessage(message);
    mb.setText(title);
    int answer = mb.open();
    return answer != SWT.NO;
  }

  public void beginTask(String message, int nrWorks) {
    addLog(message);
  }

  public void done() {
  }

  public boolean isCanceled() {
    return false;
  }

  public void setTaskName(String taskName) {
    addLog(taskName);
  }

  public void subTask(String message) {
    addLog(message);
  }

  public void worked(int nrWorks) {
  }
  
  @Override
  public boolean isAskingOverwriteConfirmation() {
    return askOverwrite;
  }
}
