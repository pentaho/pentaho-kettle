/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.job.entries.job;

import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.logging.LoggingRegistry;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.job.JobEntryJob;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.PropsUI;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * @author Vadim_Polynkov
 */
@RunWith( PowerMockRunner.class )
@PrepareForTest( { PropsUI.class, LoggingRegistry.class } )
public class JobEntryJobDialogTest {

  private static final String FILE_NAME =  "TestJob.kjb";

  private JobEntryJobDialog dialog;
  private JobEntryJob job = mock( JobEntryJob.class );

  @Before
  public void setUp() {
    mockStatic( PropsUI.class );
    when( PropsUI.getInstance() ).thenReturn( mock( PropsUI.class ) );

    LoggingRegistry logging = mock( LoggingRegistry.class );
    doReturn( null ).when( logging ).registerLoggingSource( anyObject() );

    mockStatic( LoggingRegistry.class );
    when( LoggingRegistry.getInstance() ).thenReturn( logging );

    dialog = spy( new JobEntryJobDialog( mock( Shell.class ), job, mock( Repository.class ), mock( JobMeta.class ) ) );
    doReturn( "My Job" ).when( dialog ).getName();
    doNothing().when( dialog ).getInfo( job );
    doNothing().when( dialog ).getData();
    doNothing().when( dialog ).dispose();
  }

  @Test
  public void testEntryName() {
    assertEquals( "${Internal.Entry.Current.Directory}/" + FILE_NAME, dialog.getEntryName( FILE_NAME ) );
  }

  @Test
  public void testSetChanged_OK() {
    doReturn( "/path/job.kjb" ).when( dialog ).getPath();

    dialog.ok();
    verify( job, times( 1 ) ).setChanged();
  }

  @Test
  public void testSpecificationMethod_ConnectedRepositoryByName() {
    doReturn( "/path/job.kjb" ).when( dialog ).getPath();

    dialog.ok();
    verify( job, times( 1 ) ).setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
  }

  @Test
  public void testSpecificationMethod_ConnectedFilename() {
    doReturn( "file:///path/job.kjb" ).when( dialog ).getPath();

    dialog.ok();
    verify( job, times( 1 ) ).setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
  }

  @Test
  public void testSpecificationMethod_ConnectedFilenameZip() {
    doReturn( "zip:file:///path/job.kjb" ).when( dialog ).getPath();

    dialog.ok();
    verify( job, times( 1 ) ).setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
  }

  @Test
  public void testSpecificationMethod_ConnectedFilenameHDFS() {
    doReturn( "hdfs://path/job.kjb" ).when( dialog ).getPath();

    dialog.ok();
    verify( job, times( 1 ) ).setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
  }

  @Test
  public void testSpecificationMethod_NotConnectedFilename() {
    JobEntryJobDialog nc = spy( new JobEntryJobDialog( mock( Shell.class ), job, null, mock( JobMeta.class ) ) );
    doReturn( "My Job" ).when( nc ).getName();
    doReturn( "/path/job.kjb" ).when( nc ).getPath();
    doNothing().when( nc ).getInfo( job );
    doNothing().when( nc ).getData();
    doNothing().when( nc ).dispose();

    nc.ok();
    verify( job, times( 1 ) ).setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
  }
}
