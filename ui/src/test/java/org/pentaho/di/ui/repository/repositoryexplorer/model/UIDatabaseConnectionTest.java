/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.repository.repositoryexplorer.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryElementMetaInterface;

public class UIDatabaseConnectionTest {

  @Test
  public void testDefaults() {
    UIDatabaseConnection uiconn = new UIDatabaseConnection();
    assertNull( uiconn.getName() );
    assertNull( uiconn.getType() );
    assertNull( uiconn.getDisplayName() );
    assertNull( uiconn.getDatabaseMeta() );

    DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    when( dbMeta.getName() ).thenReturn( "TestDb" );
    when( dbMeta.getPluginId() ).thenReturn( "MYSQL" );
    when( dbMeta.getDisplayName() ).thenReturn( "TestDbDisplay" );

    Repository repo = mock( Repository.class );
    uiconn = new UIDatabaseConnection( dbMeta, repo );
    assertEquals( "TestDb", uiconn.getName() );
    assertEquals( "MYSQL", uiconn.getType() );
    assertEquals( "TestDbDisplay", uiconn.getDisplayName() );
    assertSame( dbMeta, uiconn.getDatabaseMeta() );
  }

  @Test
  public void testModifiedDate() {
    final Long timestamp = 100000L;
    SimpleDateFormat sdf = new SimpleDateFormat( "d MMM yyyy HH:mm:ss z" );

    UIDatabaseConnection uiconn = new UIDatabaseConnection();
    RepositoryElementMetaInterface repoMeta = mock( RepositoryElementMetaInterface.class );
    when( repoMeta.getModifiedDate() ).thenReturn( new Date( timestamp ) );

    uiconn.setRepositoryElementMetaInterface( repoMeta );
    assertEquals( sdf.format( new Date( timestamp ) ), uiconn.getDateModified() );
  }

  @Test
  public void testModifiedDateIsNull() {
    final Long timestamp = 100000L;
    SimpleDateFormat sdf = new SimpleDateFormat( "d MMM yyyy HH:mm:ss z" );

    UIDatabaseConnection uiconn = new UIDatabaseConnection();
    RepositoryElementMetaInterface repoMeta = mock( RepositoryElementMetaInterface.class );
    when( repoMeta.getModifiedDate() ).thenReturn( null );

    uiconn.setRepositoryElementMetaInterface( repoMeta );
    assertEquals( null, uiconn.getDateModified() );
  }
}
