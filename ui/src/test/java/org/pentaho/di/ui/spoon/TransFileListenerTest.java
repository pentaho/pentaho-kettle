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

package org.pentaho.di.ui.spoon;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.jobexecutor.JobExecutorMeta;
import org.pentaho.di.trans.steps.transexecutor.TransExecutorMeta;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


public class TransFileListenerTest {

  TransFileListener transFileListener;

  @Before
  public void setUp() {
    transFileListener = new TransFileListener();
  }

  @Test
  public void testAccepts() throws Exception {
    assertFalse( transFileListener.accepts( null ) );
    assertFalse( transFileListener.accepts( "NoDot" ) );
    assertTrue( transFileListener.accepts( "Trans.ktr" ) );
    assertTrue( transFileListener.accepts( ".ktr" ) );
  }

  @Test
  public void testAcceptsXml() throws Exception {
    assertFalse( transFileListener.acceptsXml( null ) );
    assertFalse( transFileListener.acceptsXml( "" ) );
    assertFalse( transFileListener.acceptsXml( "Transformation" ) );
    assertTrue( transFileListener.acceptsXml( "transformation" ) );
  }

  @Test
  public void testGetFileTypeDisplayNames() throws Exception {
    String[] names = transFileListener.getFileTypeDisplayNames( null );
    assertNotNull( names );
    assertEquals( 2, names.length );
    assertEquals( "Transformations", names[0] );
    assertEquals( "XML", names[1] );
  }

  @Test
  public void testGetRootNodeName() throws Exception {
    assertEquals( "transformation", transFileListener.getRootNodeName() );
  }

  @Test
  public void testGetSupportedExtensions() throws Exception {
    String[] extensions = transFileListener.getSupportedExtensions();
    assertNotNull( extensions );
    assertEquals( 2, extensions.length );
    assertEquals( "ktr", extensions[0] );
    assertEquals( "xml", extensions[1] );
  }

  @Test
  public void testProcessLinkedTransWithFilename() {
    TransExecutorMeta transExecutorMeta = spy( new TransExecutorMeta() );
    transExecutorMeta.setFileName( "/path/to/Transformation2.ktr" );
    transExecutorMeta.setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
    StepMeta transExecutorStep = mock( StepMeta.class );
    when( transExecutorStep.getStepID() ).thenReturn( "TransExecutor" );
    when( transExecutorStep.getStepMetaInterface() ).thenReturn( transExecutorMeta );

    TransMeta parent = mock( TransMeta.class );
    when( parent.getSteps() ).thenReturn( Arrays.asList( transExecutorStep ) );

    TransMeta result = transFileListener.processLinkedTrans( parent );

    boolean found = false;
    for ( StepMeta stepMeta : result.getSteps() ) {
      if ( stepMeta.getStepID().equalsIgnoreCase( "TransExecutor" ) ) {
        found = true;
        TransExecutorMeta resultExecMeta = (TransExecutorMeta) stepMeta.getStepMetaInterface();
        assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, resultExecMeta.getSpecificationMethod() );
        assertEquals( resultExecMeta.getDirectoryPath(), "/path/to" );
        assertEquals( resultExecMeta.getTransName(), "Transformation2" );
      }
    }
    assertTrue( found );
  }

  @Test
  public void testProcessLinkedTransWithNoFilename() {
    TransExecutorMeta transExecutorMeta = spy( new TransExecutorMeta() );
    transExecutorMeta.setFileName( null );
    transExecutorMeta.setDirectoryPath( "/path/to" );
    transExecutorMeta.setTransName( "Transformation2" );
    transExecutorMeta.setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
    StepMeta transExecutorStep = mock( StepMeta.class );
    when( transExecutorStep.getStepID() ).thenReturn( "TransExecutor" );
    when( transExecutorStep.getStepMetaInterface() ).thenReturn( transExecutorMeta );

    TransMeta parent = mock( TransMeta.class );
    when( parent.getSteps() ).thenReturn( Arrays.asList( transExecutorStep ) );

    TransMeta result = transFileListener.processLinkedTrans( parent );

    boolean found = false;
    for ( StepMeta stepMeta : result.getSteps() ) {
      if ( stepMeta.getStepID().equalsIgnoreCase( "TransExecutor" ) ) {
        found = true;
        TransExecutorMeta resultExecMeta = (TransExecutorMeta) stepMeta.getStepMetaInterface();
        assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, resultExecMeta.getSpecificationMethod() );
        assertEquals( resultExecMeta.getDirectoryPath(), "/path/to" );
        assertEquals( resultExecMeta.getTransName(), "Transformation2" );
      }
    }
    assertTrue( found );
  }

  @Test
  public void testProcessLinkedJobsWithFilename() {
    JobExecutorMeta jobExecutorMeta = spy( new JobExecutorMeta() );
    jobExecutorMeta.setFileName( "/path/to/Job1.kjb" );
    jobExecutorMeta.setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
    StepMeta jobExecutorStep = mock( StepMeta.class );
    when( jobExecutorStep.getStepID() ).thenReturn( "JobExecutor" );
    when( jobExecutorStep.getStepMetaInterface() ).thenReturn( jobExecutorMeta );

    TransMeta parent = mock( TransMeta.class );
    when( parent.getSteps() ).thenReturn( Arrays.asList( jobExecutorStep ) );

    TransMeta result = transFileListener.processLinkedJobs( parent );

    boolean found = false;
    for ( StepMeta stepMeta : result.getSteps() ) {
      if ( stepMeta.getStepID().equalsIgnoreCase( "JobExecutor" ) ) {
        found = true;
        JobExecutorMeta resultExecMeta = (JobExecutorMeta) stepMeta.getStepMetaInterface();
        assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, resultExecMeta.getSpecificationMethod() );
        assertEquals( resultExecMeta.getDirectoryPath(), "/path/to" );
        assertEquals( resultExecMeta.getJobName(), "Job1" );
      }
    }
    assertTrue( found );
  }

  @Test
  public void testProcessLinkedJobsWithNoFilename() {
    JobExecutorMeta jobExecutorMeta = spy( new JobExecutorMeta() );
    jobExecutorMeta.setFileName( null );
    jobExecutorMeta.setDirectoryPath( "/path/to" );
    jobExecutorMeta.setJobName( "Job1" );
    jobExecutorMeta.setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
    StepMeta transExecutorStep = mock( StepMeta.class );
    when( transExecutorStep.getStepID() ).thenReturn( "JobExecutor" );
    when( transExecutorStep.getStepMetaInterface() ).thenReturn( jobExecutorMeta );

    TransMeta parent = mock( TransMeta.class );
    when( parent.getSteps() ).thenReturn( Arrays.asList( transExecutorStep ) );

    TransMeta result = transFileListener.processLinkedJobs( parent );

    boolean found = false;
    for ( StepMeta stepMeta : result.getSteps() ) {
      if ( stepMeta.getStepID().equalsIgnoreCase( "JobExecutor" ) ) {
        found = true;
        JobExecutorMeta resultExecMeta = (JobExecutorMeta) stepMeta.getStepMetaInterface();
        assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, resultExecMeta.getSpecificationMethod() );
        assertEquals( resultExecMeta.getDirectoryPath(), "/path/to" );
        assertEquals( resultExecMeta.getJobName(), "Job1" );
      }
    }
    assertTrue( found );
  }
}
