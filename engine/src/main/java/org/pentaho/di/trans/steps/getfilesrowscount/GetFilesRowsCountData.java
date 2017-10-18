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

package org.pentaho.di.trans.steps.getfilesrowscount;

import java.io.InputStream;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar Hassan
 * @since 06-Sept-2007
 */
public class GetFilesRowsCountData extends BaseStepData implements StepDataInterface {
  public String thisline;
  public RowMetaInterface outputRowMeta;
  public RowMetaInterface convertRowMeta;
  public Object[] previousRow;

  public FileInputList files;
  public boolean last_file;
  public FileObject file;
  public long filenr;

  public InputStream fr;
  public long rownr;
  public int fileFormatType;
  public StringBuilder lineStringBuilder;
  public int totalpreviousfields;
  public int indexOfFilenameField;
  public Object[] readrow;
  public RowMetaInterface inputRowMeta;
  public char separator;

  public boolean foundData;

  /**
   *
   */
  public GetFilesRowsCountData() {
    super();
    previousRow = null;
    thisline = null;
    previousRow = null;

    fr = null;
    lineStringBuilder = new StringBuilder( 256 );
    totalpreviousfields = 0;
    indexOfFilenameField = -1;
    readrow = null;
    separator = '\n';
    foundData = false;
  }
}
