/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.job;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.IdNotFoundException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.exception.LookupReferencesException;
import org.pentaho.di.core.listeners.ContentChangedListener;
import org.pentaho.di.job.entries.empty.JobEntryEmpty;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.repository.Repository;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

public class JobMetaTest {
  private JobMeta jm;
  private JobEntryEmpty je1;
  private JobEntryEmpty je2;
  private JobEntryEmpty je4;

  @Before
  public void setUp() {
    jm = new JobMeta();

    je1 = new JobEntryEmpty();
    je1.setName( "je1" );
    JobEntryCopy copy1 = new JobEntryCopy( je1 );

    je2 = new JobEntryEmpty();
    je2.setName( "je2" );
    JobEntryCopy copy2 = new JobEntryCopy( je2 );
    JobHopMeta hop = new JobHopMeta( copy1, copy2 );
    jm.addJobHop( hop );

    JobEntryEmpty je3 = new JobEntryEmpty();
    je3.setName( "je3" );
    copy2 = new JobEntryCopy( je3 );
    hop = new JobHopMeta( copy1, copy2 );
    jm.addJobHop( hop );

    je4 = new JobEntryEmpty();
    je4.setName( "je4" );
    copy1 = new JobEntryCopy( je3 );
    copy2 = new JobEntryCopy( je4 );
    hop = new JobHopMeta( copy1, copy2 );
    jm.addJobHop( hop );

  }

  @Test
  public void testPathExist() throws KettleXMLException, IOException, URISyntaxException {
    Assert.assertTrue( jm.isPathExist( je1, je4 ) );
  }

  @Test
  public void testPathNotExist() throws KettleXMLException, IOException, URISyntaxException {
    Assert.assertFalse( jm.isPathExist( je2, je4 ) );
  }

  @Test
  public void testContentChangeListener() throws Exception {
    ContentChangedListener listener = mock( ContentChangedListener.class );
    jm.addContentChangedListener( listener );

    jm.setChanged();
    jm.setChanged( true );

    verify( listener, times( 2 ) ).contentChanged( same( jm ) );

    jm.clearChanged();
    jm.setChanged( false );

    verify( listener, times( 2 ) ).contentSafe( same( jm ) );

    jm.removeContentChangedListener( listener );
    jm.setChanged();
    jm.setChanged( true );

    verifyNoMoreInteractions( listener );
  }

  @Test
  public void testLookupRepositoryReferences() throws Exception {
    JobMeta jobMetaMock = mock( JobMeta.class );
    doCallRealMethod().when( jobMetaMock ).lookupRepositoryReferences( any( Repository.class ) );
    doCallRealMethod().when( jobMetaMock ).addJobEntry( anyInt(), any( JobEntryCopy.class ) );
    doCallRealMethod().when( jobMetaMock ).clear();

    jobMetaMock.clear();

    JobEntryTrans jobEntryMock = mock( JobEntryTrans.class );
    when( jobEntryMock.hasRepositoryReferences() ).thenReturn( true );

    JobEntryTrans brokenJobEntryMock = mock( JobEntryTrans.class );
    when( brokenJobEntryMock.hasRepositoryReferences() ).thenReturn( true );
    doThrow( mock( IdNotFoundException.class ) ).when( brokenJobEntryMock ).lookupRepositoryReferences( any(
        Repository.class ) );

    JobEntryCopy jobEntryCopy1 = mock( JobEntryCopy.class );
    when( jobEntryCopy1.getEntry() ).thenReturn( jobEntryMock );
    jobMetaMock.addJobEntry( 0, jobEntryCopy1 );

    JobEntryCopy jobEntryCopy2 = mock( JobEntryCopy.class );
    when( jobEntryCopy2.getEntry() ).thenReturn( brokenJobEntryMock );
    jobMetaMock.addJobEntry( 1, jobEntryCopy2 );

    JobEntryCopy jobEntryCopy3 = mock( JobEntryCopy.class );
    when( jobEntryCopy3.getEntry() ).thenReturn( jobEntryMock );
    jobMetaMock.addJobEntry( 2, jobEntryCopy3 );

    Repository repo = mock( Repository.class );
    try {
      jobMetaMock.lookupRepositoryReferences( repo );
      Assert.fail( "no exception for broken entry" );
    } catch ( LookupReferencesException e ) {
      // ok
    }

    verify( jobEntryMock, times( 2 ) ).lookupRepositoryReferences( any( Repository.class ) );

  }
}
