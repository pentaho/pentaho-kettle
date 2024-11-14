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
