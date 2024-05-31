/*!
 * Copyright 2010 - 2017 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.di.repository.pur;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.RepositoryTestLazySupport;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class PurRepository_DatabaseNames_Test extends RepositoryTestLazySupport {

  private static final String EXISTING_DB = "existing";
  private static final String EXISTING_DB_PATH = getPathForDb( EXISTING_DB );

  public PurRepository_DatabaseNames_Test( Boolean lazyRepo ) {
    super( lazyRepo );
  }

  private static String getPathForDb( String db ) {
    return "/etc/pdi/databases/" + db + RepositoryObjectType.DATABASE.getExtension();
  }

  private PurRepository purRepository;
  private IUnifiedRepository unifiedRepository;

  @Before
  public void setUp() throws Exception {
    RepositoryFile dbFile = file( EXISTING_DB );

    unifiedRepository = mock( IUnifiedRepository.class );
    when( unifiedRepository.getFile( EXISTING_DB_PATH ) ).thenReturn( dbFile );

    PurRepositoryMeta mockMeta = mock( PurRepositoryMeta.class );

    purRepository = new PurRepository();
    purRepository.init( mockMeta );
    purRepository.setTest( unifiedRepository );
  }

  @Test
  public void getDatabaseId_ExactMatch() throws Exception {
    ObjectId databaseID = purRepository.getDatabaseID( EXISTING_DB );
    assertEquals( EXISTING_DB, databaseID.getId() );
  }

  @Test
  public void getDatabaseId_InsensitiveMatch() throws Exception {
    final String lookupName = EXISTING_DB.toUpperCase();
    assertNotSame( lookupName, EXISTING_DB );

    List<RepositoryFile> files = Arrays.asList( file( "a" ), file( EXISTING_DB ), file( "b" ) );
    purRepository = spy( purRepository );
    doReturn( files ).when( purRepository ).getAllFilesOfType( any(),
        eq( RepositoryObjectType.DATABASE ), anyBoolean() );

    ObjectId databaseID = purRepository.getDatabaseID( lookupName );
    assertEquals( EXISTING_DB, databaseID.getId() );
  }

  @Test( expected = KettleException.class )
  public void getDatabaseId_FailsOnRepositoryException() throws Exception {
    when( unifiedRepository.getFile( getPathForDb( "non-existing" ) ) ).thenThrow( new RuntimeException() );
    purRepository.getDatabaseID( "non-existing" );
  }

  private static RepositoryFile file( String name ) {
    return new RepositoryFile.Builder( name + RepositoryObjectType.DATABASE.getExtension() ).title( name ).id( name )
        .build();
  }
}
