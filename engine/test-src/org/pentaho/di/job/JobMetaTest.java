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

import org.junit.Test;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.repository.Repository;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JobMetaTest {

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
    doThrow( new RuntimeException() ).when( brokenJobEntryMock ).lookupRepositoryReferences( any( Repository.class ) );

    JobEntryCopy jobEntryCopy1 = mock( JobEntryCopy.class );
    when( jobEntryCopy1.getEntry() ).thenReturn( jobEntryMock );
    jobMetaMock.addJobEntry( 0, jobEntryCopy1 );

    JobEntryCopy jobEntryCopy2 = mock( JobEntryCopy.class );
    when( jobEntryCopy2.getEntry() ).thenReturn( brokenJobEntryMock );
    jobMetaMock.addJobEntry( 1, jobEntryCopy2 );

    JobEntryCopy jobEntryCopy3 = mock( JobEntryCopy.class );
    when( jobEntryCopy3.getEntry() ).thenReturn( jobEntryMock );
    jobMetaMock.addJobEntry( 2, jobEntryCopy3 );

    jobMetaMock.lookupRepositoryReferences( any( Repository.class ) );

  }
}
