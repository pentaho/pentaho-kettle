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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

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

  @Deprecated
  // is this used?
  public int nr_repeats;

  public int nrInputFields;

  @Deprecated
  public int recordnr;

  @Deprecated
  public int nrrecords;
  /**
   * last row read
   */
  public Object[] readrow;
  public int totalpreviousfields;

  public int filenr;

  @Deprecated //used?
  public FileInputStream fr;
  @Deprecated //used?
  public BufferedInputStream is;

  @Deprecated //used?
  public String itemElement;
  @Deprecated //used?
  public int itemCount;
  @Deprecated //used?
  public int itemPosition;

  /**
   * output row counter
   */
  public long rownr;
  public int indexSourceField;

  @Deprecated
  public JsonReader jsonReader;
  @Deprecated
  public List<NJSONArray> resultList;

  @Deprecated
  public String stringToParse;

  public Iterator<InputStream> inputs;
  public IJsonReader reader;
  public RowSet readerRowSet;
  public BitSet repeatedFields;

  public JsonInputData() {
    super();
    nr_repeats = 0;
    previousRow = null;
    filenr = 0;

    fr = null;
    is = null;
    indexSourceField = -1;

    nrInputFields = -1;
    recordnr = 0;
    nrrecords = 0;

    readrow = null;
    totalpreviousfields = 0;
  }

}
