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
import org.pentaho.di.imp.rule.ImportRuleInterface;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.imp.rule.ImportRuleCompositeInterface;

public class ParameterLessImportRuleComposite implements ImportRuleCompositeInterface{

  private Composite composite;

  public Composite getComposite(Composite parent, ImportRuleInterface importRule) {
    composite = new Composite(parent, SWT.NONE);
    PropsUI.getInstance().setLook(composite);
    composite.setLayout(new FillLayout());

    return composite;
  }
  
  public void setCompositeData(ImportRuleInterface importRule) {
  }
  
  public void getCompositeData(ImportRuleInterface importRule) {
  }
}

