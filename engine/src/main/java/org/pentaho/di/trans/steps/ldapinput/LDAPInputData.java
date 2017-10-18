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

package org.pentaho.di.trans.steps.ldapinput;

import java.util.HashSet;

import javax.naming.directory.Attributes;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar Hassan
 * @since 21-09-2007
 */
public class LDAPInputData extends BaseStepData implements StepDataInterface {
  public String thisline;
  public RowMetaInterface outputRowMeta;
  public RowMetaInterface convertRowMeta;
  public Object[] previousRow;
  public int nr_repeats;
  public long rownr;
  public String multi_valuedFieldSeparator;
  public int nrfields;
  public HashSet<String> attributesBinary;
  public LDAPConnection connection;
  public String staticFilter;
  public String staticSearchBase;
  public String[] attrReturned;
  public Object[] readRow;
  public int indexOfSearchBaseField;
  public int indexOfFilterField;
  public Attributes attributes;
  public int nrIncomingFields;
  public boolean dynamic;

  public LDAPInputData() {
    super();
    previousRow = null;
    thisline = null;
    nr_repeats = 0;
    previousRow = null;
    multi_valuedFieldSeparator = null;
    nrfields = 0;
    staticFilter = null;
    staticSearchBase = null;
    indexOfSearchBaseField = -1;
    indexOfFilterField = -1;
    attributes = null;
    nrIncomingFields = 0;
    dynamic = false;
  }

}
