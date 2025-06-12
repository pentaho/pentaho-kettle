/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.repository.filerep;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.pentaho.di.core.bowl.DefaultBowl;
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
    KettleVFS.getInstance( DefaultBowl.getInstance() ).getFileObject( virtualFolder ).createFolder();

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
      KettleVFS.getInstance( DefaultBowl.getInstance() ).getFileObject( virtualFolder ).deleteAll();
      // remove residual files
      FileUtils.deleteDirectory( Paths.get( virtualFolder ).getParent().getParent().toFile() );
    } catch ( Exception ignored ) {
      //
    }
  }
}
