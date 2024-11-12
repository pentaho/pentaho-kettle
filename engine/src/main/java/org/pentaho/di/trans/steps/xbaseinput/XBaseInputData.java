/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.xbaseinput;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Provides data for the XBaseInput step.
 *
 * @author Matt
 * @since 20-jan-2005
 */
public class XBaseInputData extends BaseStepData implements StepDataInterface {
  public XBase xbi;
  public RowMetaInterface fields;
  public int fileNr;
  public FileObject file_dbf;
  public FileInputList files;
  public RowMetaInterface outputRowMeta;

  public XBaseInputData() {
    super();

    xbi = null;
    fields = null;
  }

}
