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



package org.pentaho.di.trans.steps.changefileencoding;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 03-Juin-2008
 *
 */
public class ChangeFileEncodingData extends BaseStepData implements StepDataInterface {
  public int indexOfFileename;
  public int indexOfTargetFileename;
  public FileObject sourceFile;

  public String sourceEncoding;
  public String targetEncoding;

  public RowMetaInterface inputRowMeta;

  public ChangeFileEncodingData() {
    super();
    indexOfFileename = -1;
    indexOfTargetFileename = -1;
    sourceFile = null;
    sourceEncoding = null;
    targetEncoding = null;
  }

}
