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


package org.pentaho.di.base;


import org.junit.BeforeClass;
import org.junit.matchers.JUnitMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.shared.DatabaseManagementInterface;
import org.pentaho.di.shared.MemorySharedObjectsIO;
import org.pentaho.di.shared.SharedObjectInterface;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Andrey Khayrutdinov
 */
public abstract class PrivateDatabasesTestTemplate<T extends AbstractMeta> {

  @BeforeClass
  public static void setupFirst() throws Exception {
    KettleEnvironment.init();
    DefaultBowl.getInstance().setSharedObjectsIO( new MemorySharedObjectsIO() );
    DefaultBowl.getInstance().clearManagers();
  }

  protected void doTest_OnePrivate_TwoSharedAllExport() throws Exception {
    T meta = createMeta();
    DatabaseMeta privateMeta = createDatabase( "privateMeta" );
    meta.getDatabaseManagementInterface().add( privateMeta );
    System.setProperty( Const.STRING_ONLY_USED_DB_TO_XML, "N" );

    String xml = toXml( meta );

    DatabaseMeta meta1 = createDatabase( "meta1" );
    DatabaseMeta meta2 = createDatabase( "meta2" );
    DatabaseManagementInterface dbmgr = getBowlDbMrb();
    dbmgr.add( meta1 );
    dbmgr.add( meta2 );

    T loaded = fromXml( xml );

    List<String> loadedDbs = Arrays.asList( loaded.getDatabaseNames() );
    assertEquals( 3, loadedDbs.size() );
    assertThat( loadedDbs, JUnitMatchers.hasItems( "meta1", "meta2", "privateMeta" ) );

    Set<String> privateDatabases = loaded.getPrivateDatabases();
    assertNotNull( privateDatabases );
    assertEquals( 1, privateDatabases.size() );
    assertTrue( privateDatabases.contains( "privateMeta" ) );
    System.setProperty( Const.STRING_ONLY_USED_DB_TO_XML, "Y" );
  }

  protected void doTest_OnePrivate_TwoSharedOnlyUsed() throws Exception {
    T meta = createMeta();
    DatabaseMeta privateMeta = createDatabase( "privateMeta" );
    meta.getDatabaseManagementInterface().add( privateMeta );

    String xml = toXml( meta );

    DatabaseMeta meta1 = createDatabase( "meta1" );
    meta1.setShared( true );
    DatabaseMeta meta2 = createDatabase( "meta2" );
    meta2.setShared( true );

    T loaded = fromXml( xml );

    List<String> loadedDbs = Arrays.asList( loaded.getDatabaseNames() );
    assertEquals( 2, loadedDbs.size() );
    assertThat( loadedDbs, JUnitMatchers.hasItems( "meta1", "meta2" ) );

    Set<String> privateDatabases = loaded.getPrivateDatabases();
    assertNotNull( privateDatabases );
    assertEquals( 0, privateDatabases.size() );
    assertFalse( privateDatabases.contains( "privateMeta" ) );
  }

  protected void doTest_NoPrivate() throws Exception {
    T meta = createMeta();
    String xml = toXml( meta );

    T loaded = fromXml( xml );

    Set<String> privateDatabases = loaded.getPrivateDatabases();
    assertNotNull( privateDatabases );
    assertTrue( privateDatabases.isEmpty() );
  }

  protected void doTest_OnePrivate_NoSharedExportAll() throws Exception {
    T meta = createMeta();
    DatabaseMeta privateMeta = createDatabase( "privateMeta" );
    meta.getDatabaseManagementInterface().add( privateMeta );
    System.setProperty( Const.STRING_ONLY_USED_DB_TO_XML, "N" );
    String xml = toXml( meta );

    T loaded = fromXml( xml );

    List<String> loadedDbs = Arrays.asList( loaded.getDatabaseNames() );
    assertTrue( loadedDbs.contains( "privateMeta" ) );

    Set<String> privateDatabases = loaded.getPrivateDatabases();
    assertNotNull( privateDatabases );
    assertEquals( 1, privateDatabases.size() );
    assertTrue( privateDatabases.contains( privateMeta.getName() ) );
    System.setProperty( Const.STRING_ONLY_USED_DB_TO_XML, "Y" );
  }

  protected void doTest_OnePrivate_NoSharedOnlyUsed() throws Exception {
    T meta = createMeta();
    DatabaseMeta privateMeta = createDatabase( "privateMeta" );
    meta.getDatabaseManagementInterface().add( privateMeta );
    String xml = toXml( meta );

    T loaded = fromXml( xml );
    List<String> loadedDbs = Arrays.asList( loaded.getDatabaseNames() );
    assertFalse( loadedDbs.contains( "privateMeta" ) );

    Set<String> privateDatabases = loaded.getPrivateDatabases();
    assertNotNull( privateDatabases );
    assertEquals( 0, privateDatabases.size() );
    assertFalse( privateDatabases.contains( privateMeta.getName() ) );
  }


  protected DatabaseMeta createDatabase( String name ) {
    DatabaseMeta db = new DatabaseMeta();
    db.setName( name );
    db.getDatabaseInterface().setDatabaseName( name );
    return db;
  }

  private DatabaseManagementInterface getBowlDbMrb() throws KettleException {
    return DefaultBowl.getInstance().getManager( DatabaseManagementInterface.class );
  }

  protected abstract T createMeta();

  protected abstract T fromXml( String xml ) throws Exception;

  protected abstract String toXml( T meta ) throws Exception;
}
