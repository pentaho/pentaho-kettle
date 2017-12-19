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
