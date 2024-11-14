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


package org.pentaho.di.trans.steps.processfiles;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 03-Juin-2008
 *
 */
public class ProcessFilesData extends BaseStepData implements StepDataInterface {
  public int indexOfSourceFilename;
  public int indexOfTargetFilename;
  public FileObject sourceFile;
  public FileObject targetFile;

  public ProcessFilesData() {
    super();
    indexOfSourceFilename = -1;
    indexOfTargetFilename = -1;
    sourceFile = null;
    targetFile = null;
  }
}
