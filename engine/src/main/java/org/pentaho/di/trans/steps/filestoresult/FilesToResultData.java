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


package org.pentaho.di.trans.steps.filestoresult;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 26-may-2006
 */
public class FilesToResultData extends BaseStepData implements StepDataInterface {
  public List<ResultFile> filenames;

  public int filenameIndex;

  public RowMetaInterface outputRowMeta;

  public FilesToResultData() {
    super();

    filenames = new ArrayList<ResultFile>();
  }

}
