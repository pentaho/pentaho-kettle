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


package org.pentaho.di.trans.steps.sftpput;

import org.pentaho.di.job.entries.sftp.SFTPClient;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Send file to SFTP host.
 *
 * @author Samatar Hassan
 * @since 30-April-2012
 */
public class SFTPPutData extends BaseStepData implements StepDataInterface {

  // SFTP connection
  public SFTPClient sftpclient;
  // Index Of sourcefilename field
  public int indexOfSourceFileFieldName;
  // index of remote directory
  public int indexOfRemoteDirectory;
  // Index of movetofolder
  public int indexOfMoveToFolderFieldName;
  // index of remote filename
  public int indexOfRemoteFilename;

  public SFTPPutData() {
    super();
    this.indexOfSourceFileFieldName = -1;
    this.indexOfRemoteDirectory = -1;
    this.indexOfMoveToFolderFieldName = -1;
    this.indexOfRemoteFilename = -1;
  }

}
