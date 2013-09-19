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

package org.pentaho.di.ui.imp.rule;

import org.eclipse.swt.widgets.Composite;
import org.pentaho.di.imp.rule.ImportRuleInterface;

public interface ImportRuleCompositeInterface {

  /**
   * Get a composite with all controls on it that will allow a user to edit the import rule settings. 
   * @param parent The parent composite to place the new composite in.
   * @param importRule The import rule to edit
   * @return the new composite
   */
  public Composite getComposite(Composite parent, ImportRuleInterface importRule);
  
  /**
   * Set the import rule data onto the composite
   * 
   * @param importRule the import rule to use
   */
  public void setCompositeData(ImportRuleInterface importRule);
  
  /**
   * Get the data from the composite controls and set them in the import rule provided.
   * @param importRule The import rule to update.
   */
  public void getCompositeData(ImportRuleInterface importRule);
}
