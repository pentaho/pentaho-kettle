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

package org.pentaho.di.trans.steps.fileexists;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 03-Juin-2008
 *
 */
public class FileExistsData extends BaseStepData implements StepDataInterface {
  public int indexOfFileename;
  public RowMetaInterface previousRowMeta;
  public RowMetaInterface outputRowMeta;
  public FileObject file;
  public int NrPrevFields;

  public FileExistsData() {
    super();
    indexOfFileename = -1;
    file = null;
  }

}
