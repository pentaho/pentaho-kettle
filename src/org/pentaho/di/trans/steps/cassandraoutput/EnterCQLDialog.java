/* Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
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

package org.pentaho.di.trans.steps.cassandraoutput;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.StyledTextComp;
import org.pentaho.di.ui.spoon.job.JobGraph;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.steps.tableinput.SQLValuesHighlight;

/**
 * Provides a popup dialog for editing CQL commands.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 */
public class EnterCQLDialog extends Dialog {
  
  private static Class<?> PKG = EnterCQLDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
  
  protected String m_title;
  
  protected String m_originalCQL;
  protected String m_currentCQL;
  
  protected Shell m_parent;
  protected Shell m_shell;
  
  protected Button m_ok;
  protected Button m_cancel;
  protected Listener m_lsCancel;
  protected Listener m_lsOK;
  
//  protected SelectionAdapter m_lsDef;
  
  protected PropsUI m_props;
  
  protected StyledTextComp m_cqlText;
  
  protected TransMeta m_transMeta;
  
  protected ModifyListener m_lsMod;

  public EnterCQLDialog(Shell parent, TransMeta transMeta, ModifyListener lsMod, 
      String title, String cql) {
    super(parent, SWT.NONE);
    
    m_parent = parent;
    m_props = PropsUI.getInstance();
    m_title = title;
    m_originalCQL = cql;
    m_transMeta = transMeta;
    m_lsMod = lsMod;
  }
  
  public String open() {
    
    Display display = m_parent.getDisplay();
    
    m_shell = new Shell(m_parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN | SWT.APPLICATION_MODAL);
    m_props.setLook(m_shell);
    m_shell.setImage(GUIResource.getInstance().getImageSpoon());
    
    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    m_shell.setLayout(formLayout);
    m_shell.setText(m_title);

    int margin = Const.MARGIN;
    
    m_cqlText = new StyledTextComp(m_transMeta, m_shell, 
        SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, "");
    m_props.setLook(m_cqlText, m_props.WIDGET_STYLE_FIXED);
    
    m_cqlText.setText(m_originalCQL);
    m_currentCQL = m_originalCQL;
    
    FormData fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(0, 0);
    fd.right = new FormAttachment(100, -2 * margin);
    fd.bottom = new FormAttachment(100, -50);
    m_cqlText.setLayoutData(fd);
    m_cqlText.addModifyListener(m_lsMod);
    m_cqlText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_cqlText.setToolTipText(m_transMeta.environmentSubstitute(m_cqlText.getText()));
      }
    });
    
    // Text Highlighting
    m_cqlText.addLineStyleListener(new SQLValuesHighlight());
    
    // Some buttons
    m_ok = new Button(m_shell, SWT.PUSH);
    m_ok.setText(BaseMessages.getString(PKG, "System.Button.OK"));
    m_cancel = new Button(m_shell, SWT.PUSH);
    m_cancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
    
    BaseStepDialog.positionBottomButtons(m_shell, new Button[] { m_ok, m_cancel }, margin, null);
    
    // Add listeners
    m_lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
    m_lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
    
    m_ok.addListener(SWT.Selection, m_lsOK);
    m_cancel.addListener(SWT.Selection, m_lsCancel);
    
    // Detect [X] or ALT-F4 or something that kills this window...
    m_shell.addShellListener( new ShellAdapter() { public void shellClosed(ShellEvent e) { checkCancel(e); } } );
    
    BaseStepDialog.setSize(m_shell);
    m_shell.open();
    
    while (!m_shell.isDisposed()) {
      if (!display.readAndDispatch()) display.sleep();
    }
    
    return m_currentCQL;    
  }
  
  public void dispose() {
    m_props.setScreen(new WindowProperty(m_shell));
    m_shell.dispose();
  }
  
  protected void ok() {
    m_currentCQL = m_cqlText.getText();
    dispose();
  }
  
  protected void cancel() {
    m_currentCQL = m_originalCQL;
    dispose();
  }
  
  public void checkCancel(ShellEvent e) {
    String newText = m_cqlText.getText();
    if (!newText.equals(m_originalCQL)) {
      int save = JobGraph.showChangedWarning(m_shell, m_title);
      if (save == SWT.CANCEL) {
        e.doit = false;
      } else if (save == SWT.YES) {
        ok();
      } else {
        cancel();
      }
    } else {
      cancel();
    }
  }
}
