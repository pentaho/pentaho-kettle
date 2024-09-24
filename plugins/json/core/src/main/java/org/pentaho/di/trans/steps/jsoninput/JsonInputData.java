/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.trans.steps.jsoninput;

import java.io.InputStream;
import java.util.BitSet;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.file.BaseFileInputStepData;
import org.pentaho.di.trans.steps.jsoninput.reader.CloseableIterator;
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

  public CloseableIterator<InputStream> inputs;
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
