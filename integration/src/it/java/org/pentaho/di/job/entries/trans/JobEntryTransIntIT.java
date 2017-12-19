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

package org.pentaho.di.job.entries.trans;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.commons.vfs2.FileObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.TestUtilities;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobHopMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.special.JobEntrySpecial;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransTestFactory;
import org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta;
import org.pentaho.di.utils.TestUtils;

public class JobEntryTransIntIT {

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init( false );
  }

  private String createPDI14676Transformation() throws IOException, KettleException {
    // Setup Transformation
    String rowGenStepName = "Generate Rows";
    RowGeneratorMeta rowGenMeta = new RowGeneratorMeta();
    rowGenMeta.setRowLimit( String.valueOf( Integer.MAX_VALUE ) );
    rowGenMeta.setNeverEnding( true );
    rowGenMeta.setIntervalInMs( "0" );
    rowGenMeta.allocate( 0 );

    TransMeta tMeta = TransTestFactory.generateTestTransformation( new Variables(), rowGenMeta, rowGenStepName );
    
    // Remove the Injector step, as it's not needed for this transformation
    TransHopMeta hopToRemove = tMeta.findTransHop( tMeta.findStep( TransTestFactory.INJECTOR_STEPNAME ), tMeta.findStep( rowGenStepName ) );
    tMeta.removeTransHop( tMeta.indexOfTransHop( hopToRemove ) );
    tMeta.removeStep( tMeta.indexOfStep( tMeta.findStep( TransTestFactory.INJECTOR_STEPNAME ) ) );

    // Write transformation to temp file, for use within a job
    String transFilename = TestUtilities.createEmptyTempFile( this.getClass().getSimpleName() + "_PDI14676_", ".ktr" );
    FileObject transFile = TestUtils.getFileObject( transFilename );
    OutputStream outStream = transFile.getContent().getOutputStream();
    PrintWriter pw = new PrintWriter( outStream );
    pw.write( tMeta.getXML() );
    pw.close();
    outStream.close();
    return transFilename;
  }

  /*
   * Tests whether the job can force a transformation to stop, when the job is asked to stop.
   * A timeout parameter is required, to avoid a failed unit test from running forever.
   */
  @Test(timeout=30000)
  public void testPDI14676() throws KettleException, IOException, InterruptedException {

    String transFilename = createPDI14676Transformation();

    // Setup Job
    JobEntrySpecial startEntry = new JobEntrySpecial( "Start", true, false );
    JobEntryCopy startCopy = new JobEntryCopy( startEntry );
    startCopy.setLocation( 50, 50 );
    startCopy.setDrawn();

    JobEntryTrans transEntry = new JobEntryTrans( "PDI-13676 example" );
    transEntry.setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
    transEntry.setFileName( transFilename );
    JobEntryCopy transCopy = new JobEntryCopy( transEntry );
    transCopy.setLocation( 200, 50 );
    transCopy.setDrawn();

    JobMeta jobMeta = new JobMeta();
    jobMeta.addJobEntry( startCopy );
    jobMeta.addJobEntry( transCopy );
    jobMeta.addJobHop( new JobHopMeta( startCopy, transCopy ) );

    // Run job
    Job jobInstance = new Job( null, jobMeta );
    jobInstance.start();

    // Allow job startup time
    while ( !jobInstance.isActive() ) {
      if ( jobInstance.isStopped() || jobInstance.isFinished() ) {
        break;
      }
      Thread.sleep( 10 );
    }

    // Let the job run for a short period
    Thread.sleep( 300 );

    assertFalse( jobInstance.isStopped() );
    assertFalse( jobInstance.isFinished() );

    // Tell the job to stop.
    jobInstance.stopAll();
    assertTrue( jobInstance.isStopped() );

    // Allow the job's thread to stop and be cleaned up
    while ( !jobInstance.isFinished() || jobInstance.isActive() ) {
      Thread.sleep( 10 );      
    }

    // Ensure that the job and the thread have both stopped
    assertTrue( jobInstance.isFinished() );
    assertFalse( jobInstance.isAlive() );
  }
}
