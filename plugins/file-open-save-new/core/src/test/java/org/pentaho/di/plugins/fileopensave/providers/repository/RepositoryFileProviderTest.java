/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.di.plugins.fileopensave.providers.repository;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.plugins.fileopensave.controllers.RepositoryBrowserController;
import org.pentaho.di.plugins.fileopensave.providers.repository.model.RepositoryTree;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.spoon.Spoon;

import java.lang.reflect.Field;
import java.util.function.Supplier;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RepositoryFileProviderTest {

  private RepositoryFileProvider fileProvider;
  private Spoon spoon;

  @BeforeClass
  public static void setUpClass() throws KettleException {
    // The provider logs load failures, which requires the central log store to be initialized.
    if ( !KettleClientEnvironment.isInitialized() ) {
      KettleClientEnvironment.init();
    }
  }

  @Before
  public void setUp() throws Exception {
    fileProvider = new RepositoryFileProvider();
    spoon = mock( Spoon.class );
    setSpoonSupplier( fileProvider, () -> spoon );
    RepositoryBrowserController.repository = null;
    RepositoryFileProvider.repository = null;
  }

  @After
  public void tearDown() {
    RepositoryBrowserController.repository = null;
    RepositoryFileProvider.repository = null;
  }

  /**
   * The provider resolves its repository through a {@link Spoon} supplier, so the supplier is
   * replaced to keep the test free of a running Spoon instance.
   */
  private void setSpoonSupplier( RepositoryFileProvider provider, Supplier<Spoon> supplier ) throws Exception {
    Field field = RepositoryFileProvider.class.getDeclaredField( "spoonSupplier" );
    field.setAccessible( true );
    field.set( provider, supplier );
  }

  @Test
  public void getTreeReturnsAnEmptyTreeWhenNoRepositoryIsConnected() {
    // Regression for PDI-20652: getTree() dereferenced the result of loadDirectoryTree() directly,
    // so a null tree threw an NPE that surfaced as the open dialog silently failing to appear.
    when( spoon.getRepository() ).thenReturn( null );

    RepositoryTree tree = fileProvider.getTree( null );

    assertNotNull( tree );
    assertTrue( tree.getChildren().isEmpty() );
  }

  @Test
  public void getTreeReturnsAnEmptyTreeWhenTheDirectoryTreeCannotBeLoaded() throws Exception {
    // Error path: a repository that denies access causes loadDirectoryTree() to return null.
    // getTree() must still hand back a usable tree so the dialog can open and report the failure.
    Repository repository = mock( Repository.class );
    when( spoon.getRepository() ).thenReturn( repository );
    when( repository.loadRepositoryDirectoryTree() )
      .thenThrow( new RuntimeException( "access denied while getting file with path \"/\"" ) );

    RepositoryTree tree = fileProvider.getTree( null );

    assertNotNull( tree );
    assertTrue( tree.getChildren().isEmpty() );
  }

  @Test
  public void loadDirectoryTreeReturnsNullWhenNoRepositoryIsConnected() {
    when( spoon.getRepository() ).thenReturn( null );

    assertNull( fileProvider.loadDirectoryTree() );
  }

  @Test
  public void loadDirectoryTreeReturnsNullWhenTheRepositoryDeniesAccess() throws Exception {
    Repository repository = mock( Repository.class );
    when( spoon.getRepository() ).thenReturn( repository );
    when( repository.loadRepositoryDirectoryTree() )
      .thenThrow( new RuntimeException( "access denied while getting file with path \"/\"" ) );

    assertNull( fileProvider.loadDirectoryTree() );
  }
}
