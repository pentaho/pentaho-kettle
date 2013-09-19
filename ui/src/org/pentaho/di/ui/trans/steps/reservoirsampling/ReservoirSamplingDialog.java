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

package org.pentaho.di.ui.trans.steps.reservoirsampling;

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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.reservoirsampling.ReservoirSamplingMeta;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * The UI class for the ReservoirSampling step
 *
 * @author Mark Hall (mhall{[at]}pentaho.org
 * @version 1.0
 */
public class ReservoirSamplingDialog extends BaseStepDialog
  implements StepDialogInterface {

  private static Class<?> PKG = ReservoirSamplingMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  // various UI bits and pieces
  private Label m_wlStepname;
  private Text m_wStepname;
  private FormData m_fdlStepname;
  private FormData m_fdStepname;

  private Label m_wlSampleSize;
  private TextVar m_wSampleSize;
  private FormData m_fdlSampleSize;
  private FormData m_fdSampleSize;
  

  private Label m_wlSeed;
  private TextVar m_wSeed;
  private FormData m_fdlSeed;
  private FormData m_fdSeed;

  /**
   * meta data for the step. A copy is made so
   * that changes, in terms of choices made by the
   * user, can be detected.
   */
  private ReservoirSamplingMeta m_currentMeta;
  private ReservoirSamplingMeta m_originalMeta;

  public ReservoirSamplingDialog(Shell parent, 
                                 Object in, 
                                 TransMeta tr, 
                                 String sname) {

    super(parent, (BaseStepMeta) in, tr, sname);

    // The order here is important... 
    //m_currentMeta is looked at for changes
    m_currentMeta = (ReservoirSamplingMeta) in;
    m_originalMeta = (ReservoirSamplingMeta) m_currentMeta.clone();
  }

  /**
   * Open the dialog
   *
   * @return the step name
   */
  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = 
      new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);

    props.setLook(shell);
    setShellImage(shell, m_currentMeta);

    // used to listen to a text field (m_wStepname)
    ModifyListener lsMod = new ModifyListener() {
        public void modifyText(ModifyEvent e) {
          m_currentMeta.setChanged();
        }
      };

    changed = m_currentMeta.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout(formLayout);
    shell.setText(BaseMessages.getString(PKG, "ReservoirSamplingDialog.Shell.Title"));

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Stepname line
    m_wlStepname = new Label(shell, SWT.RIGHT);
    m_wlStepname.
      setText(BaseMessages.getString(PKG, "ReservoirSamplingDialog.StepName.Label"));
    props.setLook(m_wlStepname);

    m_fdlStepname = new FormData();
    m_fdlStepname.left = new FormAttachment(0, 0);
    m_fdlStepname.right = new FormAttachment(middle, -margin);
    m_fdlStepname.top = new FormAttachment(0, margin);
    m_wlStepname.setLayoutData(m_fdlStepname);
    m_wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    m_wStepname.setText(stepname);
    props.setLook(m_wStepname);
    m_wStepname.addModifyListener(lsMod);

    // format the text field
    m_fdStepname = new FormData();
    m_fdStepname.left = new FormAttachment(middle, 0);
    m_fdStepname.top = new FormAttachment(0, margin);
    m_fdStepname.right = new FormAttachment(100, 0);
    m_wStepname.setLayoutData(m_fdStepname);

    // Sample size text field
    m_wlSampleSize = new Label(shell, SWT.RIGHT);
    m_wlSampleSize.
      setText(BaseMessages.getString(PKG, "ReservoirSamplingDialog.SampleSize.Label"));
    props.setLook(m_wlSampleSize);
 
    m_fdlSampleSize = new FormData();
    m_fdlSampleSize.left = new FormAttachment(0, 0);
    m_fdlSampleSize.right = new FormAttachment(middle, -margin);
    m_fdlSampleSize.top = new FormAttachment(m_wStepname, margin);
    m_wlSampleSize.setLayoutData(m_fdlSampleSize);
    
    m_wSampleSize = new TextVar(transMeta, shell, 
                                SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(m_wSampleSize);
    m_wSampleSize.addModifyListener(lsMod);
    m_wSampleSize.setText(""+m_originalMeta.getSampleSize());
    m_fdSampleSize=new FormData();
    m_fdSampleSize.left = new FormAttachment(m_wlSampleSize, margin);
    m_fdSampleSize.right = new FormAttachment(100, -margin);
    m_fdSampleSize.top = new FormAttachment(m_wStepname, margin);
    m_wSampleSize.setLayoutData(m_fdSampleSize);    
    
    // Seed text field
    m_wlSeed = new Label(shell, SWT.RIGHT);
    m_wlSeed.
      setText(BaseMessages.getString(PKG, "ReservoirSamplingDialog.Seed.Label"));
    props.setLook(m_wlSeed);
 
    m_fdlSeed = new FormData();
    m_fdlSeed.left = new FormAttachment(0, 0);
    m_fdlSeed.right = new FormAttachment(middle, -margin);
    m_fdlSeed.top = new FormAttachment(m_wSampleSize, margin);
    m_wlSeed.setLayoutData(m_fdlSeed);
  
    m_wSeed = new TextVar(transMeta, shell, 
                          SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(m_wSeed);
    m_wSeed.addModifyListener(lsMod);
    m_wSeed.setText(""+m_originalMeta.getSeed());
    m_fdSeed=new FormData();
    m_fdSeed.left = new FormAttachment(m_wlSeed, margin);
    m_fdSeed.right = new FormAttachment(100, -margin);
    m_fdSeed.top = new FormAttachment(m_wSampleSize, margin);
    m_wSeed.setLayoutData(m_fdSeed);    

    // Some buttons
    wOK = new Button(shell, SWT.PUSH);
    wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
    wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

    setButtonPositions(new Button[] { wOK, wCancel }, margin, m_wSeed);

    // Add listeners
    lsCancel = new Listener() {
        public void handleEvent(Event e) {
          cancel();
        }
      };
    lsOK = new Listener() {
        public void handleEvent(Event e) {
          ok();
        }
      };

    wCancel.addListener(SWT.Selection, lsCancel);
    wOK.addListener(SWT.Selection, lsOK);

    lsDef = new SelectionAdapter() {
        public void widgetDefaultSelected(SelectionEvent e) {
          ok();
        }
      };
    
    m_wStepname.addSelectionListener(lsDef);

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener(new ShellAdapter() {
        public void shellClosed(ShellEvent e) {
          cancel();
        }
      });
    
    // Whenever something changes, set the tooltip to the expanded version:
    m_wSampleSize.addModifyListener(new ModifyListener() {
        public void modifyText(ModifyEvent e) {
          m_wSampleSize.setToolTipText(transMeta.
                                       environmentSubstitute(m_wSampleSize.getText()));
        }
      });
    
    // Whenever something changes, set the tooltip to the expanded version:
    m_wSeed.addModifyListener(new ModifyListener() {
        public void modifyText(ModifyEvent e) {
          m_wSeed.setToolTipText(transMeta.
                                 environmentSubstitute(m_wSeed.getText()));
        }
      });    
    
    // Set the shell size, based upon previous time...
    setSize();

    m_currentMeta.setChanged(changed);
    
    shell.open();

    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
    
    return stepname;
  }

  private void cancel() {
    stepname = null;
    m_currentMeta.setChanged(changed);
    dispose();
  }

  private void ok() {
    if (Const.isEmpty(m_wStepname.getText())) {
      return;
    }
  
    stepname = m_wStepname.getText(); // return value

    m_currentMeta.setSampleSize(m_wSampleSize.getText());
    m_currentMeta.setSeed(m_wSeed.getText());
    System.out.println("OK:" + m_wSampleSize + ":" + m_wSeed);
    if (!m_originalMeta.equals(m_currentMeta)) {
      m_currentMeta.setChanged();
      changed = m_currentMeta.hasChanged();
    }
    
    dispose();
  }
}