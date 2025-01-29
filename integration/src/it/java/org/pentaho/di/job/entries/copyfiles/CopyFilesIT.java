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


package org.pentaho.di.job.entries.copyfiles;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.TestUtilities;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.job.Job;

public class CopyFilesIT {

  @BeforeClass
  public static void setUpBeforeClass() {
    KettleLogStore.init();
  }

  /**
   * Creates a Result and logs that fact.
   *
   * @return
   */
  private static Result createStartJobEntryResult() {

    Result startResult = new Result();
    startResult.setLogText( TestUtilities.now() + " - START - Starting job entry\r\n " );
    return startResult;

  }

  /**
   * Tests copying a folder contents. The folders used are created in the Java's temp location using unique folder and
   * file names.
   *
   * @throws Exception
   */
  @Test
  public void testLocalFileCopy() throws Exception {

    String sourceFolder = TestUtilities.createTempFolder( "testLocalFileCopy_source" );
    String destinationFolder = TestUtilities.createTempFolder( "testLocalFileCopy_destination" );

    if ( Utils.isEmpty( sourceFolder ) || Utils.isEmpty( destinationFolder ) ) {
      fail( "Could not create the source and/or destination folder(s)." );
    }

    // create a text file named testLocalFileCopy with a delimiter of ;
    TestUtilities.writeTextFile( sourceFolder, "testLocalFileCopy", ";" );

    // the parent job
    Job parentJob = new Job();

    // Set up the job entry to do wildcard copy
    JobEntryCopyFiles jobEntry = new JobEntryCopyFiles( "Job entry copy files" );
    jobEntry.source_filefolder = new String[] { sourceFolder };
    jobEntry.destination_filefolder = new String[] { destinationFolder };
    jobEntry.wildcard = new String[] { "" };
    jobEntry.setParentJob( parentJob );

    // Check the result for errors.
    Result result = jobEntry.execute( createStartJobEntryResult(), 1 );
    if ( result.getNrErrors() != 0 ) {
      fail( result.getLogText() );
    }
  }
}
