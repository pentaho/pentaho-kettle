/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.jsoninput;

import java.io.InputStream;
import java.util.BitSet;
import java.util.Iterator;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.fileinput.BaseFileInputStepData;
import org.pentaho.di.trans.steps.jsoninput.reader.IJsonReader;

/**
 * @author Samatar
 * @since 21-06-2010
 */
public class JsonInputData extends BaseFileInputStepData implements StepDataInterface {
  public Object[] previousRow;
  public RowMetaInterface inputRowMeta;

  public boolean hasFirstRow;

  public int nrInputFields;

  /**
   * last row read
   */
  public Object[] readrow;
  public int totalpreviousfields;

  public int filenr;

  /**
   * output row counter
   */
  public long rownr;
  public int indexSourceField;

  public Iterator<InputStream> inputs;
  public IJsonReader reader;
  public RowSet readerRowSet;
  public BitSet repeatedFields;

  public JsonInputData() {
    super();
    nr_repeats = 0;
    previousRow = null;
    filenr = 0;

    indexSourceField = -1;

    nrInputFields = -1;

    readrow = null;
    totalpreviousfields = 0;
  }

}
