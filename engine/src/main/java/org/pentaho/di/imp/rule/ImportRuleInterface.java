/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.imp.rule;

import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.w3c.dom.Node;

public interface ImportRuleInterface extends Cloneable {

  /**
   * @return The import rule plugin ID
   */
  public String getId();

  /**
   * @param string
   *          Sets the plugin ID of this import rule
   */
  public void setId( String string );

  /**
   * Validate the rule against a subject (transformation, job, database, ...)
   *
   * @param subject
   *          The subject to validate against
   * @return The feedback list consists of a series of approvals and/or warnings and/or errors.
   */
  public List<ImportValidationFeedback> verifyRule( Object subject );

  /**
   * @return True if this rule is enabled. When not enabled this rule will not be looked at. Neither an approval nor an
   *         error will be given during validation.
   */
  public boolean isEnabled();

  /**
   * @param enabled
   *          Enables or disables this rule. When a rule is not enabled it will not be looked at. Neither an approval
   *          nor an error will be given during validation.
   */
  public void setEnabled( boolean enabled );

  /**
   * @return true if only one rule is needed in a whole set of rules. Adding another would not help. Returns false in
   *         case you can add this rule multiple times with different settings.
   */
  public boolean isUnique();

  public void loadXML( Node ruleNode ) throws KettleException;

  public String getXML();

  public String getCompositeClassName();

  public ImportRuleInterface clone();

}
