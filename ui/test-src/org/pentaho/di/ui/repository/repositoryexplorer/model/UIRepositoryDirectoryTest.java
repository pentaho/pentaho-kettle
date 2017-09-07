/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017 by Pentaho : http://www.pentaho.com
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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.repository.LongObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.RepositoryExtended;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.UserInfo;

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
    UIRepositoryDirectory uiDir = new UIRepositoryDirectory( root, null, repo );
    UIRepositoryObjects objects = uiDir.getRepositoryObjects();
    assertNotNull( objects );
    uiDir = new UIRepositoryDirectory( dir, uiDir, repo );
    objects = uiDir.getRepositoryObjects();
    assertEquals( 1, objects.size() );
    verifyZeroInteractions( repo );
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

    UIRepositoryDirectory uiDir = new UIRepositoryDirectory( root, null, repo );
    UIRepositoryObjects objects = uiDir.getRepositoryObjects();
    assertNotNull( objects );
    uiDir = new UIRepositoryDirectory( dir, uiDir, repo );
    objects = uiDir.getRepositoryObjects();
    assertEquals( 1, objects.size() );
  }

  @Test
  public void testRefresh() throws Exception {
    RepositoryDirectory root = new RepositoryDirectory();
    LongObjectId rootObjectId = new LongObjectId( 0L );
    root.setObjectId( rootObjectId );
    RepositoryDirectory dir = new RepositoryDirectory();
    dir.setObjectId( new LongObjectId( 1L ) );
    root.addSubdirectory( dir );

    RepositoryExtended repo = mock( RepositoryExtended.class );
    UserInfo userInfo = new UserInfo();
    userInfo.setAdmin( true );
    Mockito.when( repo.getUserInfo() ).thenReturn( userInfo );
    RepositoryDirectory rd = Mockito.mock( RepositoryDirectory.class );
    Mockito.when( rd.findDirectory( Mockito.eq( rootObjectId ) ) )
      .thenReturn( Mockito.mock( RepositoryDirectory.class ) );
    Mockito.when( repo.loadRepositoryDirectoryTree( "/", "*.ktr|*.kjb", -1, true, true, true ) ).thenReturn( rd );

    UIRepositoryDirectory uiDir = new UIRepositoryDirectory( root, null, repo );
    uiDir.refresh();
    Mockito.verify( repo ).loadRepositoryDirectoryTree( "/", "*.ktr|*.kjb", -1, true, true, true );
  }

}
