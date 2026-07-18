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
package org.pentaho.di.shared;

import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.trans.steps.loadsave.MemoryRepository;
import org.pentaho.di.trans.steps.loadsave.MemoryRepositoryExtended;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.w3c.dom.Node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests that verify the write-through cache in {@link RepositorySharedObjectsIO}.
 * <p>
 * Parameterized over both plain {@link MemoryRepository} and {@link MemoryRepositoryExtended} so that
 * both the standard-repository and RepositoryExtended code paths are exercised.
 * <p>
 * A fresh repository instance is created before each test to guarantee full isolation.
 */
@RunWith( Parameterized.class )
public class RepositorySharedObjectsIOCacheTest {

  /**
   * Class of the repository to instantiate for each test. Using a class reference rather than a
   * pre-built instance ensures every {@code @Before} gets a brand-new, clean repository.
   */
  private final Class<? extends MemoryRepository> repClass;
  private MemoryRepository rep;
  private RepositorySharedObjectsIO shared;

  public RepositorySharedObjectsIOCacheTest( Class<? extends MemoryRepository> repClass ) {
    this.repClass = repClass;
  }

  @Parameterized.Parameters
  public static List<Object[]> repositories() {
    ArrayList<Object[]> reps = new ArrayList<>();
    reps.add( new Object[] { MemoryRepository.class } );
    reps.add( new Object[] { MemoryRepositoryExtended.class } );
    return reps;
  }

  @BeforeClass
  public static void init() throws Exception {
    KettleClientEnvironment.init();
  }

  @Before
  public void setup() throws Exception {
    rep = repClass.getDeclaredConstructor().newInstance();
    shared = new RepositorySharedObjectsIO( rep, Collections::emptyList );
  }

  // region Cache hit on repeated reads

  /**
   * Verifies that once the cache is populated, subsequent reads are served from the cache and do
   * not reflect changes made directly to the backing repository.
   */
  @Test
  public void testCacheIsUsedOnRepeatedReads() throws Exception {
    // First read – populates the cache (empty repository)
    Map<String, Node> firstRead = shared.getSharedObjects( SharedObjectsIO.SharedObjectType.CONNECTION.getName() );
    assertEquals( 0, firstRead.size() );

    // Add a database directly to the backing repository, bypassing the cache
    DatabaseMeta directDb = buildDb( "directDb", "directSchema" );
    rep.save( directDb, null, null );

    // Second read – should return the stale cached result, not the new repository entry
    Map<String, Node> secondRead = shared.getSharedObjects( SharedObjectsIO.SharedObjectType.CONNECTION.getName() );
    assertEquals( "Cache should be used; directly-added entry must not be visible", 0, secondRead.size() );
  }

  // endregion

  // region Cache updated on save

  /**
   * Verifies that after a save via {@link RepositorySharedObjectsIO#saveSharedObject}, the cached result
   * immediately reflects the saved object without requiring another round-trip to the repository.
   */
  @Test
  public void testSaveUpdatesCache() throws Exception {
    // Warm up the cache with an empty read
    shared.getSharedObjects( SharedObjectsIO.SharedObjectType.CONNECTION.getName() );

    // Save through the IO layer – should update the cache
    DatabaseMeta db = buildDb( "myDb", "mySchema" );
    shared.saveSharedObject( SharedObjectsIO.SharedObjectType.CONNECTION.getName(), db.getName(), db.toNode() );

    // Add a second database directly to the backing repository (bypassing cache)
    DatabaseMeta directDb = buildDb( "directDb", "directSchema" );
    rep.save( directDb, null, null );

    // Read – should see only the saved object (from cache), not the directly-added one
    Map<String, Node> result = shared.getSharedObjects( SharedObjectsIO.SharedObjectType.CONNECTION.getName() );
    assertEquals( "Only the write-through entry should be visible", 1, result.size() );
    assertNotNull( result.get( "myDb" ) );
    assertNull( "Directly-added entry should not be visible via cached read", result.get( "directDb" ) );
  }

  /**
   * Verifies that re-saving an existing object (update) replaces the old cache entry.
   */
  @Test
  public void testSaveUpdatesCacheOnUpdate() throws Exception {
    // Warm up cache
    shared.getSharedObjects( SharedObjectsIO.SharedObjectType.CONNECTION.getName() );

    DatabaseMeta db = buildDb( "myDb", "originalSchema" );
    shared.saveSharedObject( SharedObjectsIO.SharedObjectType.CONNECTION.getName(), db.getName(), db.toNode() );

    // Update the same object
    db.setDBName( "updatedSchema" );
    shared.saveSharedObject( SharedObjectsIO.SharedObjectType.CONNECTION.getName(), db.getName(), db.toNode() );

    Map<String, Node> result = shared.getSharedObjects( SharedObjectsIO.SharedObjectType.CONNECTION.getName() );
    assertEquals( 1, result.size() );
    DatabaseMeta readBack = new DatabaseMeta( result.get( "myDb" ) );
    assertEquals( "Cache should reflect the updated value", "updatedSchema", readBack.getDatabaseName() );
  }

  // endregion

  // region Cache updated on delete

  /**
   * Verifies that after a delete, the cached result no longer contains the deleted entry.
   */
  @Test
  public void testDeleteUpdatesCache() throws Exception {
    // Warm up cache
    shared.getSharedObjects( SharedObjectsIO.SharedObjectType.CONNECTION.getName() );

    DatabaseMeta db = buildDb( "myDb", "mySchema" );
    shared.saveSharedObject( SharedObjectsIO.SharedObjectType.CONNECTION.getName(), db.getName(), db.toNode() );

    // Confirm it is visible
    assertNotNull( shared.getSharedObject( SharedObjectsIO.SharedObjectType.CONNECTION.getName(), "myDb" ) );

    // Delete through the IO layer
    shared.delete( SharedObjectsIO.SharedObjectType.CONNECTION.getName(), "myDb" );

    // Re-add the same object directly to the backing repository (bypassing cache)
    rep.save( db, null, null );

    // Read – cache should reflect the delete and not expose the directly-added entry
    Map<String, Node> result = shared.getSharedObjects( SharedObjectsIO.SharedObjectType.CONNECTION.getName() );
    assertEquals( "Deleted entry must not be present in cache", 0, result.size() );
  }

  // endregion

  // region clearCache

  /**
   * Verifies that {@link RepositorySharedObjectsIO#clearCache()} evicts all local cache entries so that
   * the next read reflects the current state of the backing repository.
   */
  @Test
  public void testClearCacheAllowsFreshReadsFromRepo() throws Exception {
    // Warm up the cache (empty)
    shared.getSharedObjects( SharedObjectsIO.SharedObjectType.CONNECTION.getName() );

    // Add a database directly to the backing repository (bypassing the cache)
    DatabaseMeta directDb = buildDb( "directDb", "directSchema" );
    rep.save( directDb, null, null );

    // Before clearCache: directly-added entry is not visible
    assertEquals( 0, shared.getSharedObjects( SharedObjectsIO.SharedObjectType.CONNECTION.getName() ).size() );

    // Clear the local cache
    shared.clearCache();

    // After clearCache: next read re-fetches from the repository and sees the new entry
    Map<String, Node> result = shared.getSharedObjects( SharedObjectsIO.SharedObjectType.CONNECTION.getName() );
    assertEquals( "After clearCache a fresh repository read should be performed", 1, result.size() );
    assertNotNull( result.get( "directDb" ) );
  }

  /**
   * Verifies that {@link RepositorySharedObjectsIO#clearCache()} evicts all types, not just one.
   */
  @Test
  public void testClearCacheEvictsAllTypes() throws Exception {
    // Warm up the cache for multiple types
    shared.getSharedObjects( SharedObjectsIO.SharedObjectType.CONNECTION.getName() );
    shared.getSharedObjects( SharedObjectsIO.SharedObjectType.SLAVESERVER.getName() );

    // Directly add to both types in the backing repository
    DatabaseMeta directDb = buildDb( "directDb", "directSchema" );
    rep.save( directDb, null, null );

    // Before clearCache: changes not visible
    assertEquals( 0, shared.getSharedObjects( SharedObjectsIO.SharedObjectType.CONNECTION.getName() ).size() );
    assertEquals( 0, shared.getSharedObjects( SharedObjectsIO.SharedObjectType.SLAVESERVER.getName() ).size() );

    shared.clearCache();

    // After clearCache: both types re-fetched from repository
    assertEquals( 1, shared.getSharedObjects( SharedObjectsIO.SharedObjectType.CONNECTION.getName() ).size() );
  }

  // endregion

  // region clear(type)

  /**
   * Verifies that {@link RepositorySharedObjectsIO#clear(String)} removes the cache entry for the
   * given type so that a subsequent direct repository addition becomes visible on the next read.
   */
  @Test
  public void testClearTypeInvalidatesCacheForThatType() throws Exception {
    // Warm up cache and save an entry
    shared.getSharedObjects( SharedObjectsIO.SharedObjectType.CONNECTION.getName() );
    DatabaseMeta db = buildDb( "myDb", "mySchema" );
    shared.saveSharedObject( SharedObjectsIO.SharedObjectType.CONNECTION.getName(), db.getName(), db.toNode() );
    assertEquals( 1, shared.getSharedObjects( SharedObjectsIO.SharedObjectType.CONNECTION.getName() ).size() );

    // clear() removes from the backing store and the cache
    shared.clear( SharedObjectsIO.SharedObjectType.CONNECTION.getName() );

    // Directly add a new database to the repository (bypassing cache)
    DatabaseMeta newDb = buildDb( "newDb", "newSchema" );
    rep.save( newDb, null, null );

    // The next read should miss the (now-absent) cache entry and fetch fresh from the repository
    Map<String, Node> result = shared.getSharedObjects( SharedObjectsIO.SharedObjectType.CONNECTION.getName() );
    assertEquals( "Cache for CONNECTION should have been invalidated by clear()", 1, result.size() );
    assertNotNull( result.get( "newDb" ) );
  }

  // endregion

  // region helpers

  private static DatabaseMeta buildDb( String name, String dbName ) {
    DatabaseMeta db = new DatabaseMeta();
    db.setName( name );
    db.setDBName( dbName );
    return db;
  }

  // endregion
}
