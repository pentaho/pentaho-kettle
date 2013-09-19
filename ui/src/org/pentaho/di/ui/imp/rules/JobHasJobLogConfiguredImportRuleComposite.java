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

package org.pentaho.di.ui.imp.rules;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.imp.rule.ImportRuleInterface;
import org.pentaho.di.imp.rules.JobHasJobLogConfiguredImportRule;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.imp.rule.ImportRuleCompositeInterface;
             
public class JobHasJobLogConfiguredImportRuleComposite implements ImportRuleCompositeInterface{

  private Text schemaText;
  private Composite composite;
  private Text tableText;
  private Text connectionText; 

  public Composite getComposite(Composite parent, ImportRuleInterface importRule) {
    PropsUI props = PropsUI.getInstance();
    
    composite = new Composite(parent, SWT.BACKGROUND);
    props.setLook(composite);
    
    FormLayout formLayout = new FormLayout ();
    formLayout.marginWidth  = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;
    composite.setLayout(formLayout);

    // Schema input field...
    //
    Label schemaLabel = new Label(composite, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    props.setLook(schemaLabel);
    schemaLabel.setText("Schema ");
    FormData fdSchemaLabel = new FormData();
    fdSchemaLabel.left = new FormAttachment(0,0);
    fdSchemaLabel.top = new FormAttachment(0,0);
    schemaLabel.setLayoutData(fdSchemaLabel);
    
    schemaText = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    props.setLook(schemaText);
    FormData fdSchemaText = new FormData();
    fdSchemaText.left = new FormAttachment(schemaLabel, Const.MARGIN);
    fdSchemaText.top = new FormAttachment(0,0);
    fdSchemaText.right = new FormAttachment(schemaLabel, 150);
    schemaText.setLayoutData(fdSchemaText);
    
    // Table name input field...
    //
    Label tableLabel = new Label(composite, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    props.setLook(tableLabel);
    tableLabel.setText("Table ");
    FormData fdTableLabel = new FormData();
    fdTableLabel.left = new FormAttachment(schemaText, Const.MARGIN);
    fdTableLabel.top = new FormAttachment(0, 0);
    tableLabel.setLayoutData(fdTableLabel);
    
    tableText = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    props.setLook(tableText);
    FormData fdTableText = new FormData();
    fdTableText.left = new FormAttachment(tableLabel, Const.MARGIN);
    fdTableText.top = new FormAttachment(0,0);
    fdTableText.right = new FormAttachment(tableLabel, 150);
    tableText.setLayoutData(fdTableText);

    // Connection name input field...
    //
    Label connectionLabel = new Label(composite, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    props.setLook(connectionLabel);
    connectionLabel.setText("Connection ");
    FormData fdConnectionLabel = new FormData();
    fdConnectionLabel.left = new FormAttachment(tableText, Const.MARGIN);
    fdConnectionLabel.top = new FormAttachment(0, 0);
    connectionLabel.setLayoutData(fdConnectionLabel);
    
    connectionText = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    props.setLook(connectionText);
    FormData fdConnectionText = new FormData();
    fdConnectionText.left = new FormAttachment(connectionLabel, Const.MARGIN);
    fdConnectionText.top = new FormAttachment(0, 0);
    fdConnectionText.right = new FormAttachment(connectionLabel, 200);
    connectionText.setLayoutData(fdConnectionText);
    
    return composite;
  }
  
  public void setCompositeData(ImportRuleInterface importRule) {
    JobHasJobLogConfiguredImportRule rule = (JobHasJobLogConfiguredImportRule) importRule;
    schemaText.setText(Const.NVL(rule.getSchemaName(), ""));
    tableText.setText(Const.NVL(rule.getTableName(), ""));
    connectionText.setText(Const.NVL(rule.getConnectionName(), ""));
  }
  
  public void getCompositeData(ImportRuleInterface importRule) {
    JobHasJobLogConfiguredImportRule rule = (JobHasJobLogConfiguredImportRule) importRule;
    rule.setSchemaName(schemaText.getText());
    rule.setTableName(tableText.getText());
    rule.setConnectionName(connectionText.getText());
  }
}

