/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.di.connections.vfs.provider;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.junit.After;
import org.junit.Before;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.common.basic.TestBasicConnectionDetails;
import org.pentaho.di.connections.common.basic.TestBasicConnectionProvider;
import org.pentaho.di.connections.common.basic.TestBasicFileProvider;
import org.pentaho.di.connections.vfs.DefaultVFSConnectionFileNameTransformer;
import org.pentaho.di.connections.vfs.VFSConnectionFileNameTransformer;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.bowl.BaseBowl;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.IKettleVFS;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.di.connections.vfs.VFSLookupFilter;
import org.pentaho.metastore.stores.xml.XmlMetaStore;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PvfsResolutionIsolationTest {
  private static final SelectionConfig CONFIG = new SelectionConfig();
  // ConnectionManager shares providers and lookup filters JVM-wide (putIfAbsent), so register them once.
  private static final SelectingProvider PROVIDER = new SelectingProvider();
  private static Object previousProvider;

  @Rule
  public TemporaryFolder metastoreFolder = new TemporaryFolder();

  // Commons VFS caches file systems and file objects process-wide; unique names keep tests independent.
  private final String suffix = UUID.randomUUID().toString().substring( 0, 8 );
  private final String firstName = "first-" + suffix;
  private final String secondName = "second-" + suffix;

  private final List<FileObject> opened = Collections.synchronizedList( new ArrayList<>() );
  private ConnectionManager manager;
  private IKettleVFS vfs;
  private final SelectingProvider provider = PROVIDER;

  @BeforeClass
  public static void initializeKettle() throws Exception {
    KettleClientEnvironment.init();
    // addConnectionProvider is putIfAbsent: other suites in the same JVM may already own this scheme.
    previousProvider = providers().put( TestBasicConnectionProvider.SCHEME, PROVIDER );
    // Production registers this filter (ConnectionLifecycleListener); VFSHelper.getOpts needs it to map URI -> provider.
    ConnectionManager.getInstance().addLookupFilter( new VFSLookupFilter() );
  }

  @AfterClass
  public static void restoreProvider() throws Exception {
    if ( previousProvider != null ) {
      providers().put( TestBasicConnectionProvider.SCHEME, previousProvider );
    } else {
      providers().remove( TestBasicConnectionProvider.SCHEME );
    }
  }

  @SuppressWarnings( "unchecked" )
  private static java.util.Map<String, Object> providers() throws Exception {
    java.lang.reflect.Field field = ConnectionManager.class.getDeclaredField( "connectionProviders" );
    field.setAccessible( true );
    return (java.util.Map<String, Object>) field.get( ConnectionManager.getInstance() );
  }

  @Before
  public void setUp() throws Exception {
    IMetaStore metastore = new XmlMetaStore( metastoreFolder.getRoot().getAbsolutePath() );
    // 10.2 Bowl API: metastore + getConnectionManager(), no SharedObjectsIO/getManager.
    Bowl bowl = new BaseBowl() {
      @Override public IMetaStore getMetastore() {
        return metastore;
      }

      @Override public IMetaStore getExplicitMetastore() {
        return metastore;
      }
    };
    manager = bowl.getConnectionManager();
    provider.reset( firstName, secondName );
    vfs = KettleVFS.getInstance( bowl );

    DefaultFileSystemManager fsm = (DefaultFileSystemManager) KettleVFS.getInstance().getFileSystemManager();
    if ( !fsm.hasProvider( ConnectionFileProvider.SCHEME ) ) {
      fsm.addProvider( ConnectionFileProvider.SCHEME, new ConnectionFileProvider() );
    }
    if ( !fsm.hasProvider( TestBasicFileProvider.SCHEME ) ) {
      fsm.addProvider( TestBasicFileProvider.SCHEME, new TestBasicFileProvider() );
    }

    save( firstName, "/first", "first-token" );
    save( secondName, "/second", "second-token" );
  }

  @After
  public void tearDown() throws Exception {
    for ( FileObject file : opened ) {
      try {
        file.close();
      } catch ( Exception ignored ) {
        // Closing after a connection was deleted can legitimately fail.
      }
    }
  }

  @Test
  public void pdi21035AndPdi20723ParallelResolutionsKeepTheirOwnConnectionOptions() throws Exception {
    Variables sharedSpace = new Variables();
    provider.interleave = true;
    ExecutorService executor = Executors.newFixedThreadPool( 2 );
    try {
      Future<FileObject> first =
        executor.submit( () -> resolve( "pvfs://" + firstName + "/output.txt", sharedSpace ) );
      if ( !provider.firstTranslating.await( 10, TimeUnit.SECONDS ) ) {
        throw new AssertionError( "First resolution never reached URI translation; done=" + first.isDone()
          + ( first.isDone() ? " result=" + first.get() + " class=" + first.get().getClass().getSimpleName() : "" ) );
      }
      Future<FileObject> second =
        executor.submit( () -> resolve( "pvfs://" + secondName + "/output.txt", sharedSpace ) );

      FileSystemOptions firstOptions = resolvedOptions( first.get( 10, TimeUnit.SECONDS ) );
      FileSystemOptions secondOptions = resolvedOptions( second.get( 10, TimeUnit.SECONDS ) );
      assertEquals( "The first URI must use the first connection, not the other thread's credentials",
        firstName, CONFIG.selection( firstOptions ) );
      assertEquals( secondName, CONFIG.selection( secondOptions ) );
      assertEquals( "first-token", CONFIG.credential( firstOptions ) );
      assertEquals( "second-token", CONFIG.credential( secondOptions ) );
    } finally {
      provider.secondTranslating.countDown();
      executor.shutdownNow();
      assertTrue( "Resolution workers did not stop", executor.awaitTermination( 10, TimeUnit.SECONDS ) );
    }
  }

  @Test
  public void pdi21092PvfsDoesNotReplaceCallerConnectionParameter() throws Exception {
    Variables space = new Variables();
    space.setVariable( ConnectionFileSystem.CONNECTION, "application-parameter" );

    resolve( "pvfs://" + firstName + "/input.txt", space );

    assertEquals( "application-parameter", space.getVariable( ConnectionFileSystem.CONNECTION ) );
  }

  @Test
  public void pdi21092RawUriDoesNotInheritPreviouslySelectedPvfsConnection() throws Exception {
    Variables space = new Variables();

    FileObject pvfs = resolve( "pvfs://" + firstName + "/input.txt", space );
    assertEquals( "Fixture precondition: PVFS options carry the connection selection",
      firstName, CONFIG.selection( resolvedOptions( pvfs ) ) );

    FileObject raw = resolve( TestBasicFileProvider.SCHEME + ":///raw/input.txt", space );

    assertNull( "A raw URI must not inherit a prior PVFS connection's options",
      CONFIG.selection( raw.getFileSystem().getFileSystemOptions() ) );
  }

  @Test
  public void pdi20586ChangedVariableIsResolvedOnNextLookup() throws Exception {
    save( firstName, "${root}", "${token}" );
    Variables space = new Variables();
    space.setVariable( "root", "/before" );
    space.setVariable( "token", "before-token" );
    FileObject before = resolve( "pvfs://" + firstName + "/input.txt", space );
    assertEquals( "/before/input.txt", resolved( before ).getName().getPath() );
    assertEquals( "before-token", CONFIG.credential( resolvedOptions( before ) ) );

    space.setVariable( "root", "/after" );
    space.setVariable( "token", "after-token" );
    FileObject after = resolve( "pvfs://" + firstName + "/input.txt", space );

    assertEquals( "/after/input.txt", resolved( after ).getName().getPath() );
    assertEquals( "after-token", CONFIG.credential( resolvedOptions( after ) ) );
  }

  @Test
  public void pdi20586SavedEditIsUsedOnNextLookupOfSamePvfsUri() throws Exception {
    Variables space = new Variables();
    FileObject before = resolve( "pvfs://" + firstName + "/input.txt", space );
    assertEquals( "/first/input.txt", resolved( before ).getName().getPath() );

    save( firstName, "/edited", "edited-token" );
    FileObject after = resolve( "pvfs://" + firstName + "/input.txt", space );

    assertEquals( "/edited/input.txt", resolved( after ).getName().getPath() );
    assertEquals( "edited-token", CONFIG.credential( resolvedOptions( after ) ) );
  }

  @Test
  public void pdi20647DeletedConnectionIsNotUsableThroughFreshLookup() throws Exception {
    Variables space = new Variables();
    FileObject before = resolve( "pvfs://" + firstName + "/input.txt", space );
    assertTrue( before.exists() );

    manager.delete( firstName );

    boolean usable;
    try {
      usable = resolve( "pvfs://" + firstName + "/input.txt", space ).exists();
    } catch ( Exception expected ) {
      usable = false;
    }
    assertFalse( "Fresh lookup must not keep using the deleted connection", usable );
  }

  private void save( String name, String root, String credential ) {
    TestBasicConnectionDetails details = new TestBasicConnectionDetails();
    details.setName( name );
    details.setRootPath( root );
    details.setPassword( credential );
    assertTrue( "Could not save fixture connection " + name, manager.save( details ) );
  }

  private FileObject resolve( String uri, VariableSpace space ) throws Exception {
    FileObject result = vfs.getFileObject( uri, space );
    opened.add( result );
    return result;
  }

  private static FileObject resolved( FileObject file ) {
    return ( (ConnectionFileObject) file ).getResolvedFileObject();
  }

  private static FileSystemOptions resolvedOptions( FileObject file ) {
    return resolved( file ).getFileSystem().getFileSystemOptions();
  }

  private static class SelectingProvider extends TestBasicConnectionProvider {
    private volatile CountDownLatch firstTranslating;
    private volatile CountDownLatch secondTranslating;
    private volatile String firstName;
    private volatile String secondName;
    private volatile boolean interleave;

    void reset( String first, String second ) {
      firstName = first;
      secondName = second;
      firstTranslating = new CountDownLatch( 1 );
      secondTranslating = new CountDownLatch( 1 );
      interleave = false;
    }

    @Override public FileSystemOptions getOpts( TestBasicConnectionDetails details ) {
      FileSystemOptions options = new FileSystemOptions();
      CONFIG.set( options, details.getName(), details.getSpace().environmentSubstitute( details.getPassword() ) );
      return options;
    }

    @Override
    public VFSConnectionFileNameTransformer<TestBasicConnectionDetails> getFileNameTransformer(
      ConnectionManager connectionManager ) {
      return new DefaultVFSConnectionFileNameTransformer<TestBasicConnectionDetails>( connectionManager ) {
        @Override public FileName toProviderFileName( ConnectionFileName name, TestBasicConnectionDetails details )
          throws KettleException {
          if ( interleave ) {
            if ( firstName.equals( details.getName() ) ) {
              firstTranslating.countDown();
              try {
                if ( !secondTranslating.await( 10, TimeUnit.SECONDS ) ) {
                  throw new KettleException( "Second resolution never reached URI translation" );
                }
              } catch ( InterruptedException e ) {
                Thread.currentThread().interrupt();
                throw new KettleException( "Interrupted while coordinating resolutions", e );
              }
            } else if ( secondName.equals( details.getName() ) ) {
              secondTranslating.countDown();
            }
          }
          return super.toProviderFileName( name, details );
        }
      };
    }
  }

  private static class SelectionConfig extends FileSystemConfigBuilder {
    @Override protected Class<? extends FileSystem> getConfigClass() {
      return FileSystem.class;
    }

    void set( FileSystemOptions options, String selection, String credential ) {
      setParam( options, "spike.selection", selection );
      setParam( options, "spike.credential", credential );
    }

    String selection( FileSystemOptions options ) {
      return (String) getParam( options, "spike.selection" );
    }

    String credential( FileSystemOptions options ) {
      return (String) getParam( options, "spike.credential" );
    }
  }
}
