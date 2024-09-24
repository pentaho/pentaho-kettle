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

package org.pentaho.di.trans.steps.getsubfolders;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 18-July-2008
 */
public class GetSubFoldersData extends BaseStepData implements StepDataInterface {

  public Object[] previous_row;

  public RowMetaInterface outputRowMeta;

  public FileInputList files;

  public boolean isLastFile;

  public int filenr;

  public int filessize;

  public FileObject file;

  public long rownr;

  public int totalpreviousfields;

  public int indexOfFoldernameField;

  public RowMetaInterface inputRowMeta;

  public Object[] readrow;

  public int nrStepFields;

  public GetSubFoldersData() {
    super();
    previous_row = null;
    filenr = 0;
    filessize = 0;
    file = null;
    totalpreviousfields = 0;
    indexOfFoldernameField = -1;
    readrow = null;
    nrStepFields = 0;
  }

}
