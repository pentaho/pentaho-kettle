/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.getrepositorynames;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.filerep.KettleFileRepository;
import org.pentaho.di.repository.filerep.KettleFileRepositoryMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.utils.TestUtils;

public class GetRepositoryNamesTest {

  String baseDirName;
  RepositoryMeta repoMeta;
  Repository repo;

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Before
  public void setup() throws KettleException {
    baseDirName = TestUtils.createTempDir();
    repoMeta = new KettleFileRepositoryMeta( UUID.randomUUID().toString(), UUID.randomUUID().toString(),
      UUID.randomUUID().toString(), baseDirName );
    repo = new KettleFileRepository();
    repo.init( repoMeta );
    repo.connect( null, null );

    // Populate
    RepositoryDirectoryInterface ROOT_DIR = repo.findDirectory( "/" );
    RepositoryDirectoryInterface DIR1 = new RepositoryDirectory( ROOT_DIR, "dir1" );
    repo.saveRepositoryDirectory( DIR1 );
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "Trans1" );
    transMeta.setRepositoryDirectory( DIR1 );
    repo.save( transMeta, null, null );
  }

  @After
  public void cleanup() {
    if ( repo != null ) {
      repo.disconnect();
    }
    FileObject baseDir = TestUtils.getFileObject( baseDirName );
    if ( baseDir != null ) {
      try {
        baseDir.delete();
      } catch ( FileSystemException ignored ) {
        // Ignore, couldn't cleanup after tests
      }
    }
  }

  @Test
  public void testGetRepoList() throws KettleException {
    GetRepositoryNamesMeta meta = new GetRepositoryNamesMeta();
    meta.setDirectory( new String[]{ "/" } );
    meta.setNameMask( new String[]{ ".*" } );
    meta.setExcludeNameMask( new String[]{ "" } );
    meta.setIncludeSubFolders( new boolean[]{ true } );
    StepMeta stepMeta = new StepMeta( "GetRepoNamesStep", meta );

    TransMeta transMeta = new TransMeta();
    transMeta.setRepository( repo );
    transMeta.addStep( stepMeta );

    GetRepositoryNamesData data = (GetRepositoryNamesData) meta.getStepData();
    GetRepositoryNames step = new GetRepositoryNames( stepMeta, data, 0, transMeta, new Trans( transMeta ) );
    step.init( meta, data );
    assertNotNull( data.list );
    assertEquals( 1, data.list.size() );
    assertEquals( "Trans1", data.list.get( 0 ).getName() );
    assertEquals( "/dir1", data.list.get( 0 ).getRepositoryDirectory().getPath() );
  }

  @Test
  public void testGetRepoListVariables() throws KettleException {
    VariableSpace vars = new Variables();
    vars.setVariable( "DirName", "/dir1" );
    vars.setVariable( "IncludeMask", ".*" );
    vars.setVariable( "ExcludeMask", "" );

    GetRepositoryNamesMeta meta = new GetRepositoryNamesMeta();
    meta.setDirectory( new String[]{ "${DirName}" } );
    meta.setNameMask( new String[]{ "${IncludeMask}" } );
    meta.setExcludeNameMask( new String[]{ "${ExcludeMask}" } );
    meta.setIncludeSubFolders( new boolean[]{ true } );
    StepMeta stepMeta = new StepMeta( "GetRepoNamesStep", meta );

    TransMeta transMeta = new TransMeta( vars );
    transMeta.setRepository( repo );
    transMeta.addStep( stepMeta );

    GetRepositoryNamesData data = (GetRepositoryNamesData) meta.getStepData();
    GetRepositoryNames step = new GetRepositoryNames( stepMeta, data, 0, transMeta, new Trans( transMeta ) );
    step.shareVariablesWith( vars ); // Required, as we're calling init() directly, instead of using Trans
    step.init( meta, data );

    assertNotNull( data.list );
    assertEquals( 1, data.list.size() );
    assertEquals( "Trans1", data.list.get( 0 ).getName() );
    assertEquals( "/dir1", data.list.get( 0 ).getRepositoryDirectory().getPath() );
  }
}
