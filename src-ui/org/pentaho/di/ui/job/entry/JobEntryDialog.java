/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
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
package org.pentaho.di.ui.job.entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;

public class JobEntryDialog extends Dialog {
	private static Class<?> PKG = StepInterface.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static final LoggingObjectInterface loggingObject = new SimpleLoggingObject("Job entry dialog", LoggingObjectType.JOBENTRYDIALOG, null);

	protected JobEntryInterface jobEntryInt;
	protected Repository rep;
	protected JobMeta jobMeta;
	protected Shell shell;
	protected PropsUI props;
    protected Shell parent;
	
    public JobEntryDialog(Shell parent, JobEntryInterface jobEntry, Repository rep, JobMeta jobMeta)
    {
        super(parent, SWT.NONE);
        props = PropsUI.getInstance();

        this.jobEntryInt = jobEntry;
        this.rep = rep;
        this.jobMeta = jobMeta;
        this.shell=parent;
        
    }
 
  public CCombo addConnectionLine(Composite parent, Control previous, int middle, int margin) {
    return addConnectionLine(parent, previous, middle, margin, new Label(parent, SWT.RIGHT), new Button(parent, SWT.PUSH), new Button(parent, SWT.PUSH));
  }
  
  public CCombo addConnectionLine(Composite parent, Control previous, int middle, int margin, final Label wlConnection,
      final Button wbnConnection, final Button wbeConnection) {
    final CCombo wConnection;
    final FormData fdlConnection, fdbConnection, fdeConnection, fdConnection;

    wConnection = new CCombo(parent, SWT.BORDER | SWT.READ_ONLY);
    props.setLook(wConnection);

    addDatabases(wConnection);

    wlConnection.setText(BaseMessages.getString(PKG, "BaseStepDialog.Connection.Label")); //$NON-NLS-1$
    props.setLook(wlConnection);
    fdlConnection = new FormData();
    fdlConnection.left = new FormAttachment(0, 0);
    fdlConnection.right = new FormAttachment(middle, -margin);
    if (previous != null)
      fdlConnection.top = new FormAttachment(previous, margin);
    else
      fdlConnection.top = new FormAttachment(0, 0);
    wlConnection.setLayoutData(fdlConnection);

    // 
    // NEW button
    //
    wbnConnection.setText(BaseMessages.getString(PKG, "BaseStepDialog.NewConnectionButton.Label")); //$NON-NLS-1$
    wbnConnection.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        DatabaseMeta databaseMeta = new DatabaseMeta();
        databaseMeta.shareVariablesWith(jobMeta);
        
        DatabaseDialog cid = new DatabaseDialog(shell, databaseMeta);
        cid.setModalDialog(true);
        if (cid.open() != null) {
        	jobMeta.addDatabase(databaseMeta);
          wConnection.removeAll();
          addDatabases(wConnection);
          selectDatabase(wConnection, databaseMeta.getName());
        }
        
      }
    });
    fdbConnection = new FormData();
    fdbConnection.right = new FormAttachment(100, 0);
    if (previous != null)
      fdbConnection.top = new FormAttachment(previous, margin);
    else
      fdbConnection.top = new FormAttachment(0, 0);
    wbnConnection.setLayoutData(fdbConnection);

    //
    // Edit button
    //
    wbeConnection.setText(BaseMessages.getString(PKG, "BaseStepDialog.EditConnectionButton.Label")); //$NON-NLS-1$
    wbeConnection.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        DatabaseMeta databaseMeta = jobMeta.findDatabase(wConnection.getText());
        if (databaseMeta != null) {
          databaseMeta.shareVariablesWith(jobMeta);
          DatabaseDialog cid = new DatabaseDialog(shell, databaseMeta);
          cid.setModalDialog(true);
          if (cid.open() != null) {
            wConnection.removeAll();
            addDatabases(wConnection);
            selectDatabase(wConnection, databaseMeta.getName());
          }
        }
      }
    });
    fdeConnection = new FormData();
    fdeConnection.right = new FormAttachment(wbnConnection, -margin);
    if (previous != null)
      fdeConnection.top = new FormAttachment(previous, margin);
    else
      fdeConnection.top = new FormAttachment(0, 0);
    wbeConnection.setLayoutData(fdeConnection);

    //
    // what's left of the line: combo box
    //
    fdConnection = new FormData();
    fdConnection.left = new FormAttachment(middle, 0);
    if (previous != null)
      fdConnection.top = new FormAttachment(previous, margin);
    else
      fdConnection.top = new FormAttachment(0, 0);
    fdConnection.right = new FormAttachment(wbeConnection, -margin);
    wConnection.setLayoutData(fdConnection);

    return wConnection;
  }
  
  public void addDatabases(CCombo wConnection) {
    for (int i = 0; i < jobMeta.nrDatabases(); i++) {
      DatabaseMeta ci = jobMeta.getDatabase(i);
	  wConnection.add(ci.getName());
    }
  }

  public void selectDatabase(CCombo wConnection, String name) {
    int idx = wConnection.indexOf(name);
    if (idx >= 0) {
      wConnection.select(idx);
    }
  }


}
