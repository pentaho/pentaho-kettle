/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.ui.spoon.Spoon;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.pentaho.di.core.util.Assert.assertTrue;

@PrepareForTest( { Spoon.class } )
@RunWith( PowerMockRunner.class )
public class PropsUITest {

  @Test
  public void removeRecentRepositoryStringString() throws KettleException {
    final Repository repo = Mockito.mock( Repository.class );
    Mockito.when( repo.getName() ).thenReturn( "Local Connection" );

    PropsUI.removeRecent( (Repository) null, null, null );
    Mockito.verify( repo, Mockito.times( 0 ) ).getObjectInformation( Mockito.any( ObjectId.class ),
      Mockito.eq( RepositoryObjectType.JOB ) );

    PropsUI.removeRecent( repo, "1", null );
    Mockito.verify( repo, Mockito.times( 1 ) ).getObjectInformation( Mockito.any( ObjectId.class ),
      Mockito.eq( RepositoryObjectType.JOB ) );

    PropsUI.removeRecent( repo, "1", RepositoryObjectType.JOB.getTypeDescription() );
    Mockito.verify( repo, Mockito.times( 2 ) ).getObjectInformation( Mockito.any( ObjectId.class ),
      Mockito.eq( RepositoryObjectType.JOB ) );

    PropsUI.removeRecent( repo, "1", RepositoryObjectType.TRANSFORMATION.getTypeDescription() );
    Mockito.verify( repo, Mockito.times( 1 ) ).getObjectInformation( Mockito.any( ObjectId.class ),
      Mockito.eq( RepositoryObjectType.TRANSFORMATION ) );
  }

  @Test
  public void removeRecentStringStringStringMap() throws Exception {
    final String repoName = "Local Connection";
    final String repoFolder = "/home/admin";

    // prep the lastUserRepoFiles
    final Map<String, List<LastUsedFile>> lastUsedRepoFiles = new HashMap();
    final List<LastUsedFile> localRepoUsedFiles = new ArrayList();
    lastUsedRepoFiles.put( repoName, localRepoUsedFiles );
    final LastUsedFile repoFile1 = new LastUsedFile( RepositoryObjectType.TRANSFORMATION.getTypeDescription(),
      "repoFile1", repoFolder, true, repoName, false, 0 );
    final LastUsedFile repoFile2 = new LastUsedFile( RepositoryObjectType.TRANSFORMATION.getTypeDescription(),
      "repoFile2", repoFolder, true, repoName, false, 0 );
    localRepoUsedFiles.add( repoFile1 );
    localRepoUsedFiles.add( repoFile2 );

    // prep the lastUsedFiles
    final List<LastUsedFile> lastUsedFiles = new ArrayList();
    lastUsedFiles.add( repoFile1 );

    PowerMockito.mockStatic( Spoon.class );
    PowerMockito.when( Spoon.getInstance() ).thenReturn( Mockito.mock( Spoon.class ) );

    // null input - there should be no change to the last used file collections
    Whitebox.invokeMethod( PropsUI.class, "removeRecentImpl", (String) null, null, null, null, null );
    assertTrue( lastUsedRepoFiles.size() == 1 );
    assertTrue( lastUsedRepoFiles.get( "Local Connection" ).size() == 2 );
    assertTrue( lastUsedFiles.size() == 1 );

    Whitebox.invokeMethod( PropsUI.class, "removeRecentImpl", repoFolder, "repoFile1", null, null, null );
    assertTrue( lastUsedRepoFiles.size() == 1 );
    assertTrue( lastUsedRepoFiles.get( "Local Connection" ).size() == 2 );
    assertTrue( lastUsedFiles.size() == 1 );

    // remove bad file - there should be no change to the last used file collections
    Whitebox.invokeMethod( PropsUI.class, "removeRecentImpl", repoName, repoFolder, "foo", lastUsedRepoFiles, null );
    assertTrue( lastUsedRepoFiles.size() == 1 );
    assertTrue( lastUsedRepoFiles.get( "Local Connection" ).size() == 2 );
    assertTrue( lastUsedFiles.size() == 1 );

    // remove bad folder - there should be no change to the last used file collections
    Whitebox.invokeMethod( PropsUI.class, "removeRecentImpl", repoName, "foo", "repoFile1", lastUsedRepoFiles,
      lastUsedFiles );
    assertTrue( lastUsedRepoFiles.size() == 1 );
    assertTrue( lastUsedRepoFiles.get( "Local Connection" ).size() == 2 );
    assertTrue( lastUsedFiles.size() == 1 );

    // remove first file - file should be removed form the last used file collections
    Whitebox.invokeMethod( PropsUI.class, "removeRecentImpl", repoName, repoFolder, "repoFile1", lastUsedRepoFiles,
      lastUsedFiles );
    assertTrue( lastUsedRepoFiles.size() == 1 );
    assertTrue( lastUsedRepoFiles.get( "Local Connection" ).size() == 1 );
    assertTrue( lastUsedFiles.size() == 0 );
  }
}
