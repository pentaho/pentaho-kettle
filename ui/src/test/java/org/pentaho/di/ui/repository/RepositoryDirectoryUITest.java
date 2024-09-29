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


package org.pentaho.di.ui.repository;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

import java.util.Collections;

import org.junit.Test;
import org.pentaho.di.repository.LongObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.RepositoryObjectType;

public class RepositoryDirectoryUITest {

  @Test
  public void testLoadRepositoryObjectsFilled() throws Exception {
    RepositoryDirectory root = new RepositoryDirectory();
    root.setObjectId( new LongObjectId( 0L ) );
    RepositoryDirectory dir = new RepositoryDirectory();
    dir.setObjectId( new LongObjectId( 1L ) );
    root.addSubdirectory( dir );
    RepositoryElementMetaInterface meta = mock( RepositoryElementMetaInterface.class );
    when( meta.getObjectType() ).thenReturn( RepositoryObjectType.TRANSFORMATION );

    root.setRepositoryObjects( Collections.emptyList() );
    dir.setRepositoryObjects( Collections.singletonList( meta ) );
    Repository repo = mock( Repository.class );

    assertTrue( RepositoryDirectoryUI.loadRepositoryObjects( root, true, true, repo ).isEmpty() );
    assertEquals( 1, RepositoryDirectoryUI.loadRepositoryObjects( dir, true, true, repo ).size() );

    verifyNoMoreInteractions( repo );
  }

  @Test
  public void testLoadRepositoryObjectsNulled() throws Exception {
    RepositoryDirectory root = new RepositoryDirectory();
    root.setObjectId( new LongObjectId( 0L ) );
    RepositoryDirectory dir = new RepositoryDirectory();
    dir.setObjectId( new LongObjectId( 1L ) );
    root.addSubdirectory( dir );
    RepositoryElementMetaInterface meta = mock( RepositoryElementMetaInterface.class );
    when( meta.getObjectType() ).thenReturn( RepositoryObjectType.TRANSFORMATION );

    Repository repo = mock( Repository.class );
    when( repo.getJobAndTransformationObjects( dir.getObjectId(), false ) ).thenReturn(
        Collections.singletonList( meta ) );
    when( repo.getJobAndTransformationObjects( root.getObjectId(), false ) ).thenReturn( Collections.emptyList() );

    assertTrue( RepositoryDirectoryUI.loadRepositoryObjects( root, true, true, repo ).isEmpty() );
    assertEquals( 1, RepositoryDirectoryUI.loadRepositoryObjects( dir, true, true, repo ).size() );
  }

}
