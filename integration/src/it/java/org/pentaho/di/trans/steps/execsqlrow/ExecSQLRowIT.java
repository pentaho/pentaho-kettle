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

package org.pentaho.di.trans.steps.execsqlrow;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.RowStepCollector;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.injector.InjectorMeta;

/**
 * Test class for database lookup. H2 is used as database in memory to get an easy playground for database tests. H2
 * does not support all SQL features but it should proof enough for most of our tests.
 *
 * Still to do: - cache testing. - Do not pass rows functionality/eat rows on failed lookup - Fail on multiple rows -
 * Order by - Different comparators
 *
 * @author Sven Boden
 */
public class ExecSQLRowIT {
  static Database database;

  public static final LoggingObjectInterface loggingObject = new SimpleLoggingObject(
    "Exec SQL Row test", LoggingObjectType.GENERAL, null );

  public static final String[] databasesXML = {
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
      + "<connection>" + "<name>db</name>" + "<server>127.0.0.1</server>" + "<type>H2</type>"
      + "<access>Native</access>" + "<database>mem:db</database>" + "<port></port>" + "<username>sa</username>"
      + "<password></password>" + "</connection>", };

  private static String execsqlrow_testtable = "execsqlrow_testtable";

  private static String[] insertStatement = {
    // New rows for the source
    "INSERT INTO " + execsqlrow_testtable + "(ID) VALUES (1)",
    "INSERT INTO " + execsqlrow_testtable + "(ID) VALUES (2)",
    "INSERT INTO " + execsqlrow_testtable + "(ID) VALUES (3)",
    "INSERT INTO " + execsqlrow_testtable + "(ID) VALUES (4)", };

  public static RowMetaInterface createSourceRowMetaInterface() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta = { new ValueMetaInteger( "ID" ) };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rm.addValueMeta( valuesMeta[i] );
    }

    return rm;
  }

  /**
   * Create source table.
   */
  public static void createTables( Database db ) throws Exception {
    String source =
      db.getCreateTableStatement( execsqlrow_testtable, createSourceRowMetaInterface(), null, false, null, true );
    try {
      db.execStatement( source );
    } catch ( KettleException ex ) {
      fail( "failure while creating table " + execsqlrow_testtable + ": " + ex.getMessage() );
    }
  }

  /**
   * Insert data in the source table.
   *
   * @param db
   *          database to use.
   */
  private static void createData( Database db ) throws Exception {
    for ( int idx = 0; idx < insertStatement.length; idx++ ) {
      db.execStatement( insertStatement[idx] );
    }
  }

  public RowMetaInterface createRowMetaInterface() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta = { new ValueMetaString( "SQL" ) };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rm.addValueMeta( valuesMeta[i] );
    }

    return rm;
  }

  /**
   * Create the input rows used for a unit test.
   */
  public List<RowMetaAndData> createDataRows() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createRowMetaInterface();

    Object[] r1 = new Object[] { "delete from " + execsqlrow_testtable + " where id=1" };
    Object[] r2 = new Object[] { "delete from " + execsqlrow_testtable + " where id=2" };
    Object[] r3 = new Object[] { "delete from " + execsqlrow_testtable + " where id=3" };

    list.add( new RowMetaAndData( rm, r1 ) );
    list.add( new RowMetaAndData( rm, r2 ) );
    list.add( new RowMetaAndData( rm, r3 ) );

    return list;
  }

  public RowMetaInterface createResultRowMetaInterface() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta = { new ValueMetaString( "SQL", 30, 0 ) };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rm.addValueMeta( valuesMeta[i] );
    }

    return rm;
  }

  /**
   * Create the result rows for a test.
   */
  public List<RowMetaAndData> createResultDataRows() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createResultRowMetaInterface();

    Object[] r1 = new Object[] { "delete from " + execsqlrow_testtable + " where id=1" };
    Object[] r2 = new Object[] { "delete from " + execsqlrow_testtable + " where id=2" };
    Object[] r3 = new Object[] { "delete from " + execsqlrow_testtable + " where id=3" };

    list.add( new RowMetaAndData( rm, r1 ) );
    list.add( new RowMetaAndData( rm, r2 ) );
    list.add( new RowMetaAndData( rm, r3 ) );

    return list;
  }

  /**
   * Check the 2 lists comparing the rows in order. If they are not the same fail the test.
   */
  public void checkRows( List<RowMetaAndData> rows1, List<RowMetaAndData> rows2 ) {
    int idx = 1;
    if ( rows1.size() != rows2.size() ) {
      fail( "Number of rows is not the same: " + rows1.size() + " and " + rows2.size() );
    }
    Iterator<RowMetaAndData> it1 = rows1.iterator();
    Iterator<RowMetaAndData> it2 = rows2.iterator();

    while ( it1.hasNext() && it2.hasNext() ) {
      RowMetaAndData rm1 = it1.next();
      RowMetaAndData rm2 = it2.next();

      Object[] r1 = rm1.getData();
      Object[] r2 = rm2.getData();

      if ( rm1.size() != rm2.size() ) {
        fail( "row nr " + idx + " is not equal" );
      }
      int[] fields = new int[r1.length];
      for ( int ydx = 0; ydx < r1.length; ydx++ ) {
        fields[ydx] = ydx;
      }
      try {
        if ( rm1.getRowMeta().compare( r1, r2, fields ) != 0 ) {
          fail( "row nr " + idx + " is not equal" );
        }
      } catch ( KettleValueException e ) {
        fail( "row nr " + idx + " is not equal" );
      }

      idx++;
    }
  }

  @BeforeClass
  public static void createDatabase() throws Exception {
    KettleEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "transname" );

    // Add the database connections
    for ( int i = 0; i < databasesXML.length; i++ ) {
      DatabaseMeta databaseMeta = new DatabaseMeta( databasesXML[i] );
      transMeta.addDatabase( databaseMeta );
    }
    DatabaseMeta dbInfo = transMeta.findDatabase( "db" );

    // Execute our setup SQLs in the database.
    database = new Database( loggingObject, dbInfo );
    database.connect();
    createTables( database );
    createData( database );
  }

  /**
   * Basic Test case for Exec SQL Row. This tests a commit size of zero (i.e. autocommit)
   */
  @Test
  public void testExecSQLRow1() throws Exception {
    KettleEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "transname" );

    // Add the database connections
    for ( int i = 0; i < databasesXML.length; i++ ) {
      DatabaseMeta databaseMeta = new DatabaseMeta( databasesXML[i] );
      transMeta.addDatabase( databaseMeta );
    }

    DatabaseMeta dbInfo = transMeta.findDatabase( "db" );
    PluginRegistry registry = PluginRegistry.getInstance();

    //
    // create an injector step...
    //
    String injectorStepname = "injector step";
    InjectorMeta im = new InjectorMeta();

    // Set the information of the injector.

    String injectorPid = registry.getPluginId( StepPluginType.class, im );
    StepMeta injectorStep = new StepMeta( injectorPid, injectorStepname, im );
    transMeta.addStep( injectorStep );

    //
    // create the Exec SQL Row step...
    //
    String stepName = "delete from [" + execsqlrow_testtable + "]";
    ExecSQLRowMeta execsqlmeta = new ExecSQLRowMeta();
    execsqlmeta.setDatabaseMeta( transMeta.findDatabase( "db" ) );
    execsqlmeta.setCommitSize( 0 ); // use Autocommit
    execsqlmeta.setSqlFieldName( "SQL" );

    String execSqlRowId = registry.getPluginId( StepPluginType.class, execsqlmeta );
    StepMeta execSqlRowStep = new StepMeta( execSqlRowId, stepName, execsqlmeta );
    execSqlRowStep.setDescription( "Deletes information from table ["
      + execsqlrow_testtable + "] on database [" + dbInfo + "]" );
    transMeta.addStep( execSqlRowStep );

    TransHopMeta hi = new TransHopMeta( injectorStep, execSqlRowStep );
    transMeta.addTransHop( hi );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );

    trans.prepareExecution( null );

    StepInterface si = trans.getStepInterface( stepName, 0 );
    RowStepCollector rc = new RowStepCollector();
    si.addRowListener( rc );

    RowProducer rp = trans.addRowProducer( injectorStepname, 0 );
    trans.startThreads();

    // add rows
    List<RowMetaAndData> inputList = createDataRows();
    for ( RowMetaAndData rm : inputList ) {
      rp.putRow( rm.getRowMeta(), rm.getData() );
    }
    rp.finished();

    trans.waitUntilFinished();

    List<RowMetaAndData> resultRows = rc.getRowsWritten();
    List<RowMetaAndData> goldRows = createResultDataRows();
    checkRows( goldRows, resultRows );
  }

  /**
   * Basic Test case for Exec SQL Row. This tests a commit size of one (i.e. "simulated" autocommit)
   */
  @Test
  public void testExecSQLRow2() throws Exception {
    KettleEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "transname" );

    // Add the database connections
    for ( int i = 0; i < databasesXML.length; i++ ) {
      DatabaseMeta databaseMeta = new DatabaseMeta( databasesXML[i] );
      transMeta.addDatabase( databaseMeta );
    }

    DatabaseMeta dbInfo = transMeta.findDatabase( "db" );
    PluginRegistry registry = PluginRegistry.getInstance();

    //
    // create an injector step...
    //
    String injectorStepname = "injector step";
    InjectorMeta im = new InjectorMeta();

    // Set the information of the injector.

    String injectorPid = registry.getPluginId( StepPluginType.class, im );
    StepMeta injectorStep = new StepMeta( injectorPid, injectorStepname, im );
    transMeta.addStep( injectorStep );

    //
    // create the Exec SQL Row step...
    //
    String stepName = "delete from [" + execsqlrow_testtable + "]";
    ExecSQLRowMeta execsqlmeta = new ExecSQLRowMeta();
    execsqlmeta.setDatabaseMeta( transMeta.findDatabase( "db" ) );
    execsqlmeta.setCommitSize( 1 );
    execsqlmeta.setSqlFieldName( "SQL" );

    String execSqlRowId = registry.getPluginId( StepPluginType.class, execsqlmeta );
    StepMeta execSqlRowStep = new StepMeta( execSqlRowId, stepName, execsqlmeta );
    execSqlRowStep.setDescription( "Deletes information from table ["
      + execsqlrow_testtable + "] on database [" + dbInfo + "]" );
    transMeta.addStep( execSqlRowStep );

    TransHopMeta hi = new TransHopMeta( injectorStep, execSqlRowStep );
    transMeta.addTransHop( hi );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );

    trans.prepareExecution( null );

    StepInterface si = trans.getStepInterface( stepName, 0 );
    RowStepCollector rc = new RowStepCollector();
    si.addRowListener( rc );

    RowProducer rp = trans.addRowProducer( injectorStepname, 0 );
    trans.startThreads();

    // add rows
    List<RowMetaAndData> inputList = createDataRows();
    for ( RowMetaAndData rm : inputList ) {
      rp.putRow( rm.getRowMeta(), rm.getData() );
    }
    rp.finished();

    trans.waitUntilFinished();

    List<RowMetaAndData> resultRows = rc.getRowsWritten();
    List<RowMetaAndData> goldRows = createResultDataRows();
    checkRows( goldRows, resultRows );
  }

  /**
   * Basic Test case for Exec SQL Row. This tests a commit size of two (i.e. not autocommit and not the input row size)
   */
  @Test
  public void testExecSQLRow3() throws Exception {
    KettleEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "transname" );

    // Add the database connections
    for ( int i = 0; i < databasesXML.length; i++ ) {
      DatabaseMeta databaseMeta = new DatabaseMeta( databasesXML[i] );
      transMeta.addDatabase( databaseMeta );
    }

    DatabaseMeta dbInfo = transMeta.findDatabase( "db" );
    PluginRegistry registry = PluginRegistry.getInstance();

    //
    // create an injector step...
    //
    String injectorStepname = "injector step";
    InjectorMeta im = new InjectorMeta();

    // Set the information of the injector.

    String injectorPid = registry.getPluginId( StepPluginType.class, im );
    StepMeta injectorStep = new StepMeta( injectorPid, injectorStepname, im );
    transMeta.addStep( injectorStep );

    //
    // create the Exec SQL Row step...
    //
    String stepName = "delete from [" + execsqlrow_testtable + "]";
    ExecSQLRowMeta execsqlmeta = new ExecSQLRowMeta();
    execsqlmeta.setDatabaseMeta( transMeta.findDatabase( "db" ) );
    execsqlmeta.setCommitSize( 2 );
    execsqlmeta.setSqlFieldName( "SQL" );

    String execSqlRowId = registry.getPluginId( StepPluginType.class, execsqlmeta );
    StepMeta execSqlRowStep = new StepMeta( execSqlRowId, stepName, execsqlmeta );
    execSqlRowStep.setDescription( "Deletes information from table ["
      + execsqlrow_testtable + "] on database [" + dbInfo + "]" );
    transMeta.addStep( execSqlRowStep );

    TransHopMeta hi = new TransHopMeta( injectorStep, execSqlRowStep );
    transMeta.addTransHop( hi );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );

    trans.prepareExecution( null );

    StepInterface si = trans.getStepInterface( stepName, 0 );
    RowStepCollector rc = new RowStepCollector();
    si.addRowListener( rc );

    RowProducer rp = trans.addRowProducer( injectorStepname, 0 );
    trans.startThreads();

    // add rows
    List<RowMetaAndData> inputList = createDataRows();
    for ( RowMetaAndData rm : inputList ) {
      rp.putRow( rm.getRowMeta(), rm.getData() );
    }
    rp.finished();

    trans.waitUntilFinished();

    List<RowMetaAndData> resultRows = rc.getRowsWritten();
    List<RowMetaAndData> goldRows = createResultDataRows();
    checkRows( goldRows, resultRows );
  }

  /**
   * Basic Test case for Exec SQL Row. This tests a commit size of three (i.e. not autocommit but equal to input row
   * size)
   */
  @Test
  public void testExecSQLRow4() throws Exception {
    KettleEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "transname" );

    // Add the database connections
    for ( int i = 0; i < databasesXML.length; i++ ) {
      DatabaseMeta databaseMeta = new DatabaseMeta( databasesXML[i] );
      transMeta.addDatabase( databaseMeta );
    }

    DatabaseMeta dbInfo = transMeta.findDatabase( "db" );
    PluginRegistry registry = PluginRegistry.getInstance();

    //
    // create an injector step...
    //
    String injectorStepname = "injector step";
    InjectorMeta im = new InjectorMeta();

    // Set the information of the injector.

    String injectorPid = registry.getPluginId( StepPluginType.class, im );
    StepMeta injectorStep = new StepMeta( injectorPid, injectorStepname, im );
    transMeta.addStep( injectorStep );

    //
    // create the Exec SQL Row step...
    //
    String stepName = "delete from [" + execsqlrow_testtable + "]";
    ExecSQLRowMeta execsqlmeta = new ExecSQLRowMeta();
    execsqlmeta.setDatabaseMeta( transMeta.findDatabase( "db" ) );
    execsqlmeta.setCommitSize( 3 );
    execsqlmeta.setSqlFieldName( "SQL" );

    String execSqlRowId = registry.getPluginId( StepPluginType.class, execsqlmeta );
    StepMeta execSqlRowStep = new StepMeta( execSqlRowId, stepName, execsqlmeta );
    execSqlRowStep.setDescription( "Deletes information from table ["
      + execsqlrow_testtable + "] on database [" + dbInfo + "]" );
    transMeta.addStep( execSqlRowStep );

    TransHopMeta hi = new TransHopMeta( injectorStep, execSqlRowStep );
    transMeta.addTransHop( hi );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );

    trans.prepareExecution( null );

    StepInterface si = trans.getStepInterface( stepName, 0 );
    RowStepCollector rc = new RowStepCollector();
    si.addRowListener( rc );

    RowProducer rp = trans.addRowProducer( injectorStepname, 0 );
    trans.startThreads();

    // add rows
    List<RowMetaAndData> inputList = createDataRows();
    for ( RowMetaAndData rm : inputList ) {
      rp.putRow( rm.getRowMeta(), rm.getData() );
    }
    rp.finished();

    trans.waitUntilFinished();

    List<RowMetaAndData> resultRows = rc.getRowsWritten();
    List<RowMetaAndData> goldRows = createResultDataRows();
    checkRows( goldRows, resultRows );
  }

}
