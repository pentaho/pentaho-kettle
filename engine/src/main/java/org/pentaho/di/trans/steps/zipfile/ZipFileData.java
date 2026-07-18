/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.trans.steps.zipfile;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 03-Juin-2008
 *
 */
public class ZipFileData extends BaseStepData implements StepDataInterface {
  public RowMetaInterface outputRowMeta;

  public int indexOfSourceFilename;
  public int indexOfZipFilename;
  public FileObject sourceFile;
  public FileObject zipFile;

  public int indexOfBaseFolder;
  public String baseFolder;

  public int indexOfMoveToFolder;

  public ZipFileData() {
    super();
    indexOfSourceFilename = -1;
    indexOfZipFilename = -1;
    sourceFile = null;
    zipFile = null;
    indexOfBaseFolder = -1;
    baseFolder = null;
    indexOfMoveToFolder = -1;
  }

}
