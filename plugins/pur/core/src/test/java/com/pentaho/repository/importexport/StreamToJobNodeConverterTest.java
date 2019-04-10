/*!
 * Copyright 2019 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.pentaho.repository.importexport;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.platform.api.repository2.unified.ConverterException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

import java.io.IOException;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.pentaho.di.core.util.Assert.assertTrue;

public class StreamToJobNodeConverterTest {

  @Test
  public void testConvertJobWithMissingPlugins() throws IOException, KettleException {
    RepositoryFile repositoryFile = new RepositoryFile.Builder( "test file" ).build();
    IUnifiedRepository pur = mock( IUnifiedRepository.class );
    when( pur.getFileById( "MissingEntries.ktr" ) ).thenReturn( repositoryFile );
    JobMeta jobMeta = new JobMeta();

    Repository repository = mock( Repository.class );
    when( repository.loadJob( any( StringObjectId.class ), anyString() ) ).thenReturn( jobMeta );

    StreamToJobNodeConverter jobNodeConverter = new StreamToJobNodeConverter( pur );
    jobNodeConverter = spy( jobNodeConverter );
    doReturn( repository ).when( jobNodeConverter ).connectToRepository();

    try {
      jobNodeConverter.convert( getClass().getResource( "MissingEntries.kjb" ).openStream(), "UTF-8", "application/vnd.pentaho.transformation" );
    } catch ( ConverterException e ) {
      assertTrue( e.getMessage().contains( "MissingPlugin" ) );
      return;
    }
    fail();
  }
}
