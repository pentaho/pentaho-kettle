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
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;


import java.util.Collections;

import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.repository.LongObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.RepositoryObjectType;

public class UIRepositoryDirectoryTest {

  @Test
  public void testGetRepositoryObjectsFilled() throws Exception {
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
    String dirTest = "dirTest";
    dir.setName( dirTest );
    String[] dirNames = { dirTest };
    when( repo.getDirectoryNames( any() ) ).thenReturn( dirNames );
    UIRepositoryDirectory uiDir = new UIRepositoryDirectory( root, null, repo );
    UIRepositoryObjects objects = uiDir.getRepositoryObjects();
    assertNotNull( objects );
    uiDir = new UIRepositoryDirectory( dir, uiDir, repo );
    objects = uiDir.getRepositoryObjects();
    assertEquals( 1, objects.size() );
  }

  @Test
  public void testGetRepositoryObjectsNulled() throws Exception {
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

    String dirTest = "dirTest";
    dir.setName( dirTest );
    String[] dirNames = { dirTest };
    when( repo.getDirectoryNames( any() ) ).thenReturn( dirNames );
    UIRepositoryDirectory uiDir = new UIRepositoryDirectory( root, null, repo );
    UIRepositoryObjects objects = uiDir.getRepositoryObjects();
    assertNotNull( objects );
    uiDir = new UIRepositoryDirectory( dir, uiDir, repo );
    objects = uiDir.getRepositoryObjects();
    assertEquals( 1, objects.size() );
  }

  @Test
  public void testRefresh() throws Exception {
    RepositoryDirectory rd = Mockito.mock( RepositoryDirectory.class );
    Mockito.when( rd.getObjectId() ).thenReturn( new LongObjectId( 0L ) );
    UIRepositoryDirectory uiDir = new UIRepositoryDirectory( rd, null, null );
    uiDir.populateChildren();
    uiDir.getRepositoryObjects();
    uiDir.clear();
    Mockito.verify( rd ).getChildren();
    Mockito.verify( rd ).getRepositoryObjects();
  }

}
