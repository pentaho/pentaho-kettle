/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
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
package org.pentaho.di.ui.trans.steps.infobrightoutput;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.InfobrightDatabaseMeta;
import org.pentaho.di.core.database.MySQLDatabaseMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.infobrightoutput.InfobrightLoaderMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import com.infobright.etl.model.DataFormat;
import com.infobright.io.InfobrightNamedPipeLoader;

/**
 * Dialog box for the Infobright loader.
 */
public class InfobrightLoaderDialog extends BaseStepDialog implements StepDialogInterface {
  
  private static Class<?> PKG = InfobrightLoaderMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
  
  private int middle;
  
  private CCombo serverConnection;
  private CCombo dataFormatSelect;

  protected BaseStepMeta input;
  
  private ModifyListener lsMod;

  private InfobrightLoaderMeta	meta;

  private TextVar	targetSchemaText;

  private TextVar	targetTableText;
  
  private CCombo   charsetSelect;
  
  private TextVar   agentPortText;
  
  private TextVar   debugFileText;

  /**
   * @param parent
   * @param in
   * @param tr
   * @param sname
   */
  public InfobrightLoaderDialog(Shell parent, Object in, TransMeta tr, String sname) {
    super(parent, (BaseStepMeta) in, tr, sname);
    input = (BaseStepMeta) in;
    meta = (InfobrightLoaderMeta) in;
    
    lsMod = new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        input.setChanged();
      }
    };
  }
  
  /**
   * {@inheritDoc}
   * @see org.pentaho.di.trans.step.StepDialogInterface#open()
   */
  public String open() {
    shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
    props.setLook(shell);
    
    setShellImage(shell, (StepMetaInterface) input);

    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout(formLayout);
    shell.setText(BaseMessages.getString(PKG, "InfobrightLoaderDialog.Shell.Title"));

    middle = props.getMiddlePct();
     
    int margin = Const.MARGIN;

    /************************************** Step name line ***************************/
    // label
    wlStepname = new Label(shell, SWT.RIGHT);
    wlStepname.setText(BaseMessages.getString(PKG, "InfobrightLoaderDialog.Stepname.Label"));
    wlStepname.setLayoutData(standardLabelSpacing(null));
    props.setLook(wlStepname);

    // text entry
    wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wStepname.setText(stepname);
    wStepname.addModifyListener(lsMod);
    wStepname.setLayoutData(standardInputSpacing(null));
    props.setLook(wStepname);

    Control lastControl = addDbConnectionInputs();
    lastControl = addCustomInputs(lastControl);
    
    addVerticalPadding(2 * margin);
    
    /********************************** OK and Cancel buttons **************************/
    addDefaultButtons(margin, lastControl);
    
    getData();
    input.setChanged(changed);

    shell.open();
    while (!shell.isDisposed()) {
      Display display = getParent().getDisplay();

      if (!display.readAndDispatch())
        display.sleep();
    }
    return stepname;
  }

  /**
   * Adds db connection text boxes for input
   * @return the last control specified
   */
  protected Control addDbConnectionInputs() {
	List<String> ibConnections = new ArrayList<String>();
	for (DatabaseMeta dbMeta : transMeta.getDatabases()) {
		if (dbMeta.getDatabaseInterface() instanceof MySQLDatabaseMeta || dbMeta.getDatabaseInterface() instanceof InfobrightDatabaseMeta) {
			ibConnections.add(dbMeta.getName());
		}
	}
	serverConnection = addStandardSelect(BaseMessages.getString(PKG, "InfobrightLoaderDialog.Connection.Label"), wStepname, ibConnections.toArray(new String[ibConnections.size()]));
	
    return serverConnection;
  }

  /**
   * Adds any custom inputs
   * @param prevControl
   * @return the last control
   */
  protected Control addCustomInputs(Control prevControl) {
	String[] dataformats = new String[DataFormat.values().length];
	int i = 0;
	for (DataFormat format : DataFormat.values()) {
	  dataformats[i++] = format.getDisplayText();
	}

	Map<String,Charset> availableCharsetsMap = Charset.availableCharsets();
	String[] availableCharsets = new String[availableCharsetsMap.size()];
	int j = 0;
	for (String charsetName : availableCharsetsMap.keySet()) {
	  availableCharsets[j++] = charsetName;
	}
    dataFormatSelect = addStandardSelect(BaseMessages.getString(PKG, "InfobrightLoaderDialog.Dataformat.Label"), prevControl, dataformats);
    targetSchemaText = addStandardTextVar(BaseMessages.getString(PKG, "InfobrightLoaderDialog.TargetSchema.Label"), dataFormatSelect);
    targetTableText = addStandardTextVar(BaseMessages.getString(PKG, "InfobrightLoaderDialog.TargetTable.Label"), targetSchemaText);
    charsetSelect = addStandardSelect(BaseMessages.getString(PKG, "InfobrightLoaderDialog.Charset.Label"), targetTableText, availableCharsets);
    agentPortText = addStandardTextVar(BaseMessages.getString(PKG, "InfobrightLoaderDialog.AgentPort.Label"), charsetSelect);
    debugFileText = addStandardTextVar(BaseMessages.getString(PKG, "InfobrightLoaderDialog.DebugFile.Label"), agentPortText);
    return debugFileText;
  }

  protected CCombo addStandardSelect(String labelMessageKey, Control prevControl, String[] choices) {
    int vertPad = verticalPadding;
    addStandardLabel(labelMessageKey, prevControl);
    verticalPadding = vertPad;
    CCombo combo = new CCombo(shell, SWT.BORDER);
    combo.setItems(choices);
    combo.addModifyListener(lsMod);
    combo.setLayoutData(standardInputSpacing(prevControl));
    return combo;
  }
  
  protected TextVar addStandardTextVar(String labelMessageKey, Control prevControl) {
    int vertPad = verticalPadding;
    addStandardLabel(labelMessageKey, prevControl);
    verticalPadding = vertPad;
    TextVar targetControl = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    targetControl.addModifyListener(lsMod);
    targetControl.setLayoutData(standardInputSpacing(prevControl));
    return targetControl;
  }
  
  protected Button addStandardCheckBox(String labelMessageKey, Control prevControl) {
    addStandardLabel(labelMessageKey, prevControl);
    Button targetControl = new Button(shell, SWT.CHECK);
    targetControl.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        input.setChanged();
      }
    }
    );
    targetControl.setLayoutData(standardInputSpacing(prevControl));
    return targetControl;
  }
  
  private Label addStandardLabel(String messageString, Control previousControl) {
    Label label = new Label(shell, SWT.RIGHT);
    label.setText(messageString);
    label.setLayoutData(standardLabelSpacing(previousControl));
    props.setLook(label);
    return label;
  }
  
  private int verticalPadding = 0;
  private void addVerticalPadding(int amount) {
    verticalPadding += amount;
  }
  
  private FormData standardLabelSpacing(Control control) {
    return standardSpacing(control, true);
  }
  
  private FormData standardInputSpacing(Control control) {
    return standardSpacing(control, false);
  }
  
  private FormData standardSpacing(Control control, boolean isLabel) {
    FormData fd = new FormData();
    
    if (isLabel)
      fd.left = new FormAttachment(0, 0);
    else  
      fd.left = new FormAttachment(middle, 0);
    
    if (isLabel)
      fd.right = new FormAttachment(middle, -Const.MARGIN);
    else
      fd.right = new FormAttachment(100, 0);
    
    if (control != null)
      fd.top = new FormAttachment(control, Const.MARGIN+verticalPadding);
    else
      fd.top = new FormAttachment(0, Const.MARGIN+verticalPadding);      
    
    verticalPadding = 0;
    return fd;
  }
  
  private void addDefaultButtons(int margin, Control lastControl) {
    // Some buttons
    wOK = new Button(shell, SWT.PUSH);
    wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
    wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

    setButtonPositions(new Button[] { wOK, wCancel }, margin, lastControl);

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
    wStepname.addSelectionListener(lsDef);

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e) {
        cancel();
      }
    });

    // Set the shell size, based upon previous time...
    setSize();    
  }
  
  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    wStepname.selectAll();
    if (meta.getDatabaseMeta()!=null) {
    	serverConnection.setText(meta.getDatabaseMeta().getName());
    }
    dataFormatSelect.setText(meta.getInfobrightProductType());
    targetSchemaText.setText(Const.NVL(meta.getSchemaName(), ""));
    targetTableText.setText(Const.NVL(meta.getTablename(), ""));
    charsetSelect.setText(meta.getCharset().name());
    agentPortText.setText(Integer.toString(meta.getAgentPort()));
    debugFileText.setText(Const.NVL(meta.getDebugFile(), ""));
  }

  protected void cancel() {
    stepname = null;
    input.setChanged(changed);
    dispose();
  }

  protected void ok() {
    try {
      stepname = wStepname.getText(); // return value
      meta.setDatabaseMeta( transMeta.findDatabase(serverConnection.getText()) );
      meta.setSchemaName(targetSchemaText.getText());
      meta.setTablename(targetTableText.getText());
      meta.setDataFormat(DataFormat.valueForDisplayName(dataFormatSelect.getText()));
      String charsetName = charsetSelect.getText();
      Charset charset2 = ("".equals(charsetName) ?
          InfobrightNamedPipeLoader.DEFAULT_CHARSET :
          Charset.forName(charsetName));
      meta.setCharset(charset2);
      String agentPortStr = agentPortText.getText().trim();
      if ("".equals(agentPortStr)) {
        meta.setAgentPort(InfobrightNamedPipeLoader.AGENT_DEFAULT_PORT);
      } else {
        meta.setAgentPort(Integer.parseInt(agentPortStr));
      }
      meta.setDebugFile(debugFileText.getText());
      dispose();
    } catch(Exception e) {
        new ErrorDialog(shell, BaseMessages.getString(PKG,
            "InfobrightLoaderDialog.Illegal.Dialog.Settings.Title"),
          BaseMessages.getString(PKG, "InfobrightLoaderDialog.Illegal.Dialog.Settings.Message"), e);
    }
  }

  public BaseStepMeta getInput() {
    return input;
  }
}
