/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.repository.filerep;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.repository.RepositoryDirectoryInterface;

import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Andrey Khayrutdinov
 */
public abstract class KettleFileRepositoryTestBase {

  protected KettleFileRepository repository;
  protected RepositoryDirectoryInterface tree;

  protected String virtualFolder;

  @Before
  public void setUp() throws Exception {
    KettleEnvironment.init();

    virtualFolder = "ram://file-repo/" + UUID.randomUUID();
    KettleVFS.getFileObject( virtualFolder ).createFolder();

    KettleFileRepositoryMeta repositoryMeta =
      new KettleFileRepositoryMeta( "KettleFileRepository", "FileRep", "File repository", virtualFolder );
    repository = new KettleFileRepository();
    repository.init( repositoryMeta );

    // Test connecting... (no security needed)
    //
    repository.connect( null, null );
    assertTrue( repository.isConnected() );

    // Test loading the directory tree
    //
    tree = repository.loadRepositoryDirectoryTree();
    assertNotNull( tree );
  }

  @After
  public void tearDown() throws Exception {
    try {
      KettleVFS.getFileObject( virtualFolder ).deleteAll();
      // remove residual files
      FileUtils.deleteDirectory( Paths.get( virtualFolder ).getParent().getParent().toFile() );
    } catch ( Exception ignored ) {
      //
    }
  }
}
