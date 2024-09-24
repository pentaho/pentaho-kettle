/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
