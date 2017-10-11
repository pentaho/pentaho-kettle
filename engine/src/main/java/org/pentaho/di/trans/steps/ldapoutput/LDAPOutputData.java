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

package org.pentaho.di.trans.steps.ldapoutput;

import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.ldapinput.LDAPConnection;

/**
 * @author Samatar Hassan
 * @since 21-09-2007
 */
public class LDAPOutputData extends BaseStepData implements StepDataInterface {
  public LDAPConnection connection;
  public int indexOfDNField;
  public int[] fieldStream;
  public String[] fieldsAttribute;
  public int nrfields;
  public int nrfieldsToUpdate;
  public String separator;
  public String[] attributes;
  public String[] attributesToUpdate;

  public int[] fieldStreamToUpdate;
  public String[] fieldsAttributeToUpdate;

  public int indexOfOldDNField;
  public int indexOfNewDNField;

  public LDAPOutputData() {
    super();
    this.indexOfDNField = -1;
    this.nrfields = 0;
    this.separator = null;
    this.fieldStreamToUpdate = null;
    this.fieldsAttributeToUpdate = null;
    this.attributesToUpdate = null;
    this.nrfieldsToUpdate = 0;
    this.indexOfOldDNField = -1;
    this.indexOfNewDNField = -1;
  }

}
