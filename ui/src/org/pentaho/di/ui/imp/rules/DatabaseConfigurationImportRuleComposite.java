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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.imp.rule.ImportRuleInterface;
import org.pentaho.di.imp.rules.DatabaseConfigurationImportRule;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;
import org.pentaho.di.ui.imp.rule.ImportRuleCompositeInterface;

public class DatabaseConfigurationImportRuleComposite implements ImportRuleCompositeInterface{

  private Button button;
  private Composite composite;
  private DatabaseConfigurationImportRule rule;
  private Label label; 
  private DatabaseMeta databaseMeta;
  
  public Composite getComposite(Composite parent, ImportRuleInterface importRule) {
    rule = (DatabaseConfigurationImportRule) importRule;
    databaseMeta = rule.getDatabaseMeta();
    PropsUI props = PropsUI.getInstance();
    
    composite = new Composite(parent, SWT.NONE);
    props.setLook(composite);
    composite.setLayout(new FillLayout());

    label = new Label(composite, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    props.setLook(label);
    label.setText("Database configuration : (not configured)");

    button = new Button(composite, SWT.PUSH);
    button.setText("Edit...");
    button.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent event) { editDatabase(); } });
    
    return composite;
  }
  
 public void setCompositeData(ImportRuleInterface importRule) {
    if (databaseMeta!=null) {
      label.setText("Database configuration: "+databaseMeta.getName());
    }
  }
  
  public void getCompositeData(ImportRuleInterface importRule) {
    rule.setDatabaseMeta(databaseMeta);
  }

  protected void editDatabase() {
    DatabaseMeta editMeta;
    if (databaseMeta == null) {
      editMeta = new DatabaseMeta();
    } else {
      editMeta = (DatabaseMeta) databaseMeta.clone();
    }
    DatabaseDialog databaseDialog = new DatabaseDialog(composite.getShell(), editMeta);
    if (databaseDialog.open()!=null) {
      databaseMeta = editMeta;
      setCompositeData(rule);
    }
  }


}

