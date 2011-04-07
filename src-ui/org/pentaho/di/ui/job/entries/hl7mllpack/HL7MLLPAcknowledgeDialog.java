/* 
 * Copyright (c) 2009 kJube.  All rights reserved. 
 */

package org.pentaho.di.ui.job.entries.hl7mllpack;

import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.hl7mllpack.HL7MLLPAcknowledge;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.LabelText;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * This dialog allows you to edit the HL7 MPPL Input job entry.
 * 
 * @since 24-03-2011
 * @author matt
 */

public class HL7MLLPAcknowledgeDialog extends JobEntryDialog implements JobEntryDialogInterface {
  private static Class<?>  PKG = HL7MLLPAcknowledge.class; // for i18n purposes,
                                                     // needed by Translator2!!
                                                     // $NON-NLS-1$

  private LabelText        wName;

  private LabelTextVar     wServer;
  private LabelTextVar     wPort;
  private LabelTextVar     wVariable;

  private Button           wOK, wCancel;

  private HL7MLLPAcknowledge     jobEntry;

  private Shell            shell;

  private SelectionAdapter lsDef;

  private boolean          changed;

  public HL7MLLPAcknowledgeDialog(Shell parent, JobEntryInterface jobEntry, Repository rep, JobMeta jobMeta) {
    super(parent, jobEntry, rep, jobMeta);
    this.jobEntry = (HL7MLLPAcknowledge) jobEntry;

    if (this.jobEntry.getName() == null)
      this.jobEntry.setName(BaseMessages.getString(PKG, "HL7MLLPAcknowledgeDialog.Name.Default"));
  }

  public JobEntryInterface open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell(parent, props.getJobsDialogStyle());
    props.setLook(shell);
    JobDialog.setShellImage(shell, jobEntry);

    ModifyListener lsMod = new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        jobEntry.setChanged();
      }
    };
    changed = jobEntry.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout(formLayout);
    shell.setText(BaseMessages.getString(PKG, "HL7MLLPAcknowledgeDialog.Title"));

    int margin = Const.MARGIN;

    // Job entry name line
    wName = new LabelText(shell, BaseMessages.getString(PKG, "HL7MLLPAcknowledgeDialog.Name.Label"), BaseMessages.getString(PKG, "HL7MLLPAcknowledgeDialog.Name.Tooltip"));
    wName.addModifyListener(lsMod);
    FormData fdName = new FormData();
    fdName.top = new FormAttachment(0, 0);
    fdName.left = new FormAttachment(0, 0);
    fdName.right = new FormAttachment(100, 0);
    wName.setLayoutData(fdName);
    Control lastControl = wName;

    // the server
    //
    wServer = new LabelTextVar(jobMeta, shell, BaseMessages.getString(PKG, "HL7MLLPAcknowledgeDialog.Server.Label"), BaseMessages.getString(PKG, "HL7MLLPAcknowledgeDialog.Server.Tooltip"));
    props.setLook(wServer);
    wServer.addModifyListener(lsMod);
    FormData fdBatchIdSchema = new FormData();
    fdBatchIdSchema.left = new FormAttachment(0, 0);
    fdBatchIdSchema.top = new FormAttachment(lastControl, margin);
    fdBatchIdSchema.right = new FormAttachment(100, 0);
    wServer.setLayoutData(fdBatchIdSchema);
    lastControl = wServer;

    // the port
    //
    wPort = new LabelTextVar(jobMeta, shell, BaseMessages.getString(PKG, "HL7MLLPAcknowledgeDialog.Port.Label"), BaseMessages.getString(PKG, "HL7MLLPAcknowledgeDialog.Port.Tooltip"));
    props.setLook(wPort);
    wPort.addModifyListener(lsMod);
    FormData fdPort = new FormData();
    fdPort.left = new FormAttachment(0, 0);
    fdPort.top = new FormAttachment(lastControl, margin);
    fdPort.right = new FormAttachment(100, 0);
    wPort.setLayoutData(fdPort);
    lastControl = wPort;

    // the variable
    //
    wVariable = new LabelTextVar(jobMeta, shell, BaseMessages.getString(PKG, "HL7MLLPAcknowledgeDialog.Variable.Label"), BaseMessages.getString(PKG, "HL7MLLPAcknowledgeDialog.Variable.Tooltip"));
    props.setLook(wVariable);
    wVariable.addModifyListener(lsMod);
    FormData fdVariable = new FormData();
    fdVariable.left = new FormAttachment(0, 0);
    fdVariable.top = new FormAttachment(lastControl, margin);
    fdVariable.right = new FormAttachment(100, 0);
    wVariable.setLayoutData(fdVariable);
    lastControl = wVariable;

    wOK = new Button(shell, SWT.PUSH);
    wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
    wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

    BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, lastControl);

    // Add listeners
    //
    wCancel.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e) {
        cancel();
      }
    });
    wOK.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e) {
        ok();
      }
    });

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected(SelectionEvent e) {
        ok();
      }
    };

    wName.addSelectionListener(lsDef);
    wServer.addSelectionListener(lsDef);
    wPort.addSelectionListener(lsDef);
    wVariable.addSelectionListener(lsDef);

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e) {
        cancel();
      }
    });

    getData();

    BaseStepDialog.setSize(shell);

    shell.open();
    props.setDialogSize(shell, "HL7MLLPAcknowledgeDialog.DialogSize");
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }
    return jobEntry;
  }

  public void dispose() {
    WindowProperty winprop = new WindowProperty(shell);
    props.setScreen(winprop);
    shell.dispose();
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    wName.setText(Const.NVL(jobEntry.getName(), ""));
    wName.getTextWidget().selectAll();
    wServer.setText(Const.NVL(jobEntry.getServer(), ""));
    wPort.setText(Const.NVL(jobEntry.getPort(), ""));
    wVariable.setText(Const.NVL(jobEntry.getVariableName(), ""));
  }

  private void cancel() {
    jobEntry.setChanged(changed);
    jobEntry = null;
    dispose();
  }

  private void ok() {
    jobEntry.setName(wName.getText());

    getInfo(jobEntry);

    dispose();
  }

  private void getInfo(HL7MLLPAcknowledge entry) {
    entry.setServer(wServer.getText());
    entry.setPort(wPort.getText());
    entry.setVariableName(wVariable.getText());
  }
}