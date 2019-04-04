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

package org.pentaho.di.trans.steps.salesforcedelete;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.steps.salesforce.SalesforceStepData;

import com.sforce.soap.partner.DeleteResult;

/*
 * @author Samatar
 * @since 10-06-2007
 */
public class SalesforceDeleteData extends SalesforceStepData {
  public RowMetaInterface inputRowMeta;
  public RowMetaInterface outputRowMeta;

  public DeleteResult[] deleteResult;

  public String[] deleteId;
  public Object[][] outputBuffer;
  public int iBufferPos;

  public int indexOfKeyField;

  public SalesforceDeleteData() {
    super();

    deleteResult = null;
    iBufferPos = 0;
    indexOfKeyField = -1;
  }
}
