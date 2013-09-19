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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.imp.rule.ImportRuleInterface;
import org.pentaho.di.imp.rules.TransformationHasDescriptionImportRule;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.imp.rule.ImportRuleCompositeInterface;

public class TransformationHasDescriptionImportRuleComposite implements ImportRuleCompositeInterface{

  private Text text;
  private Composite composite;

  public Composite getComposite(Composite parent, ImportRuleInterface importRule) {
    PropsUI props = PropsUI.getInstance();
    
    composite = new Composite(parent, SWT.NONE);
    props.setLook(composite);
    composite.setLayout(new FillLayout());

    Label label = new Label(composite, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    props.setLook(label);
    label.setText("Minimum length: ");

    text = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    props.setLook(text);
    
    return composite;
  }
  
  public void setCompositeData(ImportRuleInterface importRule) {
    TransformationHasDescriptionImportRule rule = (TransformationHasDescriptionImportRule) importRule;
    text.setText(Integer.toString(rule.getMinLength()));
  }
  
  public void getCompositeData(ImportRuleInterface importRule) {
    TransformationHasDescriptionImportRule rule = (TransformationHasDescriptionImportRule) importRule;
    rule.setMinLength(Const.toInt(text.getText(), 0));
  }
}

