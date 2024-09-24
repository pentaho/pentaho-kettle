/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.www.service.zip;

import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.unzip.JobEntryUnZip;

/**
 * Simple class to handle zip file operations. This class is a wrapper to call Kettle transformation/job entries.
 */
public class ZipServiceKettle implements ZipService {

  /**
   * {@inheritDoc}
   */
  @Override
  public void extract( String zipFile, String destinationDirectory ) throws KettleException {

    JobEntryUnZip jobEntryUnZip = instantiateJobEntryUnZip();

    execute( jobEntryUnZip, zipFile, destinationDirectory );

  }

  /**
   *
   * @param jobEntryUnZip instance of object.
   * @param zipFile zip file path.
   * @param destinationDirectory destination directory to extract contents of <code>zipFile</code>.
   */
  protected void execute( JobEntryUnZip jobEntryUnZip, String zipFile, String destinationDirectory ) {

    setValues( jobEntryUnZip, zipFile, destinationDirectory );
    // NOTE not checking result, it will always return negative in this scenario
    // JobEntryUnZip expects other components to be present, does not effect unzipping proces
    jobEntryUnZip.execute( new Result(), 1 );

  }

  /**
   * Populate <code>jobEntryUnZip</code> with the bare minimum values to run.
   * @param jobEntryUnZip instance of object.
   * @param zipFile zip file path.
   * @param destinationDirectory destination directory to extract contents of <code>zipFile</code>.
   */
  protected void setValues( JobEntryUnZip jobEntryUnZip, String zipFile, String destinationDirectory ) {

    jobEntryUnZip.setZipFilename( zipFile );
    jobEntryUnZip.setWildcardSource( "" );
    jobEntryUnZip.setWildcardExclude( "" );
    jobEntryUnZip.setSourceDirectory( destinationDirectory );
    jobEntryUnZip.setMoveToDirectory( "" );

  }


  /**
   * Create object {@link JobEntryUnZip}.
   * @return instance of object.
   */
  protected JobEntryUnZip instantiateJobEntryUnZip() {

    JobEntryUnZip jobEntryUnZip = new JobEntryUnZip( "ZipServiceKettle" ); // Generic name
    JobMeta jobMeta = instantiateJobMeta();
    jobEntryUnZip.setParentJobMeta( jobMeta );
    return jobEntryUnZip;

  }

  /**
   * Create object {@link JobMeta}.
   * @return instance of object.
   */
  protected JobMeta instantiateJobMeta() {
    return new JobMeta();
  }

}
