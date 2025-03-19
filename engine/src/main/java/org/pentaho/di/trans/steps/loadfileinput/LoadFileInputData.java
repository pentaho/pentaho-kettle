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


package org.pentaho.di.trans.steps.loadfileinput;

import java.util.Date;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 21-06-2007
 */
public class LoadFileInputData extends BaseStepData implements StepDataInterface {
  public RowMetaInterface outputRowMeta;
  public RowMetaInterface convertRowMeta;
  public Object[] previousRow;
  public int nr_repeats;

  public FileInputList files;
  public boolean last_file;
  public FileObject file;
  public int filenr;

  public long rownr;
  public int indexOfFilenameField;
  public int totalpreviousfields;
  public int nrInputFields;

  public Object[] readrow;

  public byte[] filecontent;

  public long fileSize;

  public RowMetaInterface inputRowMeta;
  public String filename;
  public String shortFilename;
  public String path;
  public String extension;
  public boolean hidden;
  public Date lastModificationDateTime;
  public String uriName;
  public String rootUriName;

  public LoadFileInputData() {
    super();

    nr_repeats = 0;
    previousRow = null;
    filenr = 0;

    totalpreviousfields = 0;
    indexOfFilenameField = -1;
    nrInputFields = -1;

    readrow = null;
    fileSize = 0;
  }
}
