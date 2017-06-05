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

package org.pentaho.di.trans.steps.combinationlookup;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.metastore.api.IMetaStore;

import junit.framework.TestCase;

/**
 * Test class for combination lookup/update. HSQL is used as database in memory to get an easy playground for database
 * tests. H2 does not support all SQL features but it should proof enough for most of our tests.
 *
 * @author Sven Boden
 */
public class CombinationLookupTest extends TestCase {
  public static final String[] databasesXML = {
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
      + "<connection>" + "<name>lookup</name>" + "<server>127.0.0.1</server>" + "<type>H2</type>"
      + "<access>Native</access>" + "<database>mem:db</database>" + "<port></port>" + "<username>sa</username>"
      + "<password></password>" + "</connection>", };

  private static String target_table = "type1_dim";
  private static String source_table = "source";

  private static String[] insertStatement = {
    // New rows for the target
    "INSERT INTO "
      + source_table + "(ORDNO, DLR_CD, DLR_NM, DLR_DESC)"
      + "VALUES (1, 'BE010001', 'Frijters', 'Frijters NV');",
    "INSERT INTO "
      + source_table + "(ORDNO, DLR_CD, DLR_NM, DLR_DESC)"
      + "VALUES (2, 'BE010002', 'Sebrechts', 'Sebrechts NV');",
    "INSERT INTO "
      + source_table + "(ORDNO, DLR_CD, DLR_NM, DLR_DESC)"
      + "VALUES (3, 'DE010003', 'Gelden', 'Gelden Distribution Center');",

    // Existing business key
    "INSERT INTO "
      + source_table + "(ORDNO, DLR_CD, DLR_NM, DLR_DESC)"
      + "VALUES (4, 'BE010001', 'Frijters', 'Frijters BVBA');",

    // New row again
    "INSERT INTO "
      + source_table + "(ORDNO, DLR_CD, DLR_NM, DLR_DESC)"
      + "VALUES (5, 'DE010004', 'Germania', 'German Distribution Center');" };

  @Override
  @Before
  public void setUp() throws Exception {
    KettleEnvironment.init();
  }

  public RowMetaInterface createTargetRowMetaInterface() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta =
    {
      new ValueMetaInteger( "ID", 8, 0 ),
      new ValueMetaString( "DLR_CD", 8, 0 ),
      new ValueMetaString( "DLR_NM", 30, 0 ),
      new ValueMetaString( "DLR_DESC", 30, 0 ), };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rm.addValueMeta( valuesMeta[i] );
    }

    return rm;
  }

  public RowMetaInterface createSourceRowMetaInterface() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta =
    {
      new ValueMetaInteger( "ORDNO", 8, 0 ),
      new ValueMetaString( "DLR_CD", 8, 0 ),
      new ValueMetaString( "DLR_NM", 30, 0 ),
      new ValueMetaString( "DLR_DESC", 30, 0 ), };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rm.addValueMeta( valuesMeta[i] );
    }

    return rm;
  }

  /**
   * Create source and target table.
   */
  public void createTables( Database db ) throws Exception {
    String target =
      db.getCreateTableStatement( target_table, createTargetRowMetaInterface(), null, false, null, true );
    try {
      db.execStatement( target );
    } catch ( KettleException ex ) {
      fail( "failure while creating table " + target_table + ": " + ex.getMessage() );
    }

    String source =
      db.getCreateTableStatement( source_table, createSourceRowMetaInterface(), null, false, null, true );
    try {
      db.execStatement( source );
    } catch ( KettleException ex ) {
      fail( "failure while creating table " + source_table + ": " + ex.getMessage() );
    }
  }

  /**
   * Insert data in the source table.
   *
   * @param db
   *          database to use.
   */
  private void createData( Database db ) throws Exception {
    for ( int idx = 0; idx < insertStatement.length; idx++ ) {
      db.execStatement( insertStatement[idx] );
    }
  }

  /**
   * Check the results in the target dimension table.
   *
   * @param db
   *          database to use.
   */
  public void checkResults( Database db ) throws Exception {
    String query = "SELECT ID, DLR_CD, DLR_NM, DLR_DESC FROM " + target_table + " ORDER BY ID";

    String[] correctResults =
    { "1|BE010001|null|null", "2|BE010002|null|null", "3|DE010003|null|null", "4|DE010004|null|null", };

    ResultSet rs = db.openQuery( query );
    int idx = 0;
    while ( rs.next() ) {
      int id = rs.getInt( "ID" );
      String dlr_cd = rs.getString( "DLR_CD" );
      String dlr_nm = rs.getString( "DLR_NM" );
      String dlr_desc = rs.getString( "DLR_DESC" );
      String result = id + "|" + dlr_cd + "|" + dlr_nm + "|" + dlr_desc;
      if ( idx > correctResults.length ) {
        fail( "more rows returned than expected" );
      }
      if ( !result.equals( correctResults[idx] ) ) {
        fail( "row " + ( idx + 1 ) + " is different than expected" );
      }
      idx++;
    }
    if ( idx < correctResults.length ) {
      fail( "less rows returned than expected" );
    }
  }

  public void testUseDefaultSchemaName() throws Exception {
    String schemaName = "";
    String tableName = "tableName";
    String schemaTable = "default.tableName";
    String technicalKeyField = "technicalKeyField";

    DatabaseMeta databaseMeta = spy( new DatabaseMeta( databasesXML[0] ) {
      @Override
      public String getFieldDefinition( ValueMetaInterface v, String tk, String pk, boolean use_autoinc ) {
        return "someValue";
      }
    } );
    when( databaseMeta.getQuotedSchemaTableCombination( schemaName, tableName ) ).thenReturn( schemaTable );

    CombinationLookupMeta clm = new CombinationLookupMeta();
    clm.setTechnicalKeyField( technicalKeyField );
    clm.setKeyLookup( new String[] { "keyLookup1", "keyLookup2" } );
    clm.setDatabaseMeta( databaseMeta );
    clm.setTablename( tableName );
    clm.setSchemaName( schemaName );

    StepMeta stepMeta = mock( StepMeta.class );

    RowMetaInterface rowMetaInterface = mock( RowMetaInterface.class );
    when( rowMetaInterface.size() ).thenReturn( 1 );

    Repository repository = mock( Repository.class );
    IMetaStore metaStore = mock( IMetaStore.class );

    SQLStatement sqlStatement =
        clm.getSQLStatements( new TransMeta(), stepMeta, rowMetaInterface, repository, metaStore );

    String sql = sqlStatement.getSQL();
    Assert.assertTrue( StringUtils.countMatches( sql, schemaTable ) == 3 );
  }

  /**
   * Test case for Combination lookup/update.
   */
  public void testCombinationLookup() throws Exception {
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

    DatabaseMeta lookupDBInfo = transMeta.findDatabase( "lookup" );

    // Execute our setup SQLs in the database.
    Database lookupDatabase = new Database( transMeta, lookupDBInfo );
    lookupDatabase.connect();
    createTables( lookupDatabase );
    createData( lookupDatabase );

    PluginRegistry registry = PluginRegistry.getInstance();

    //
    // create the source step...
    //
    String fromstepname = "read from [" + source_table + "]";
    TableInputMeta tii = new TableInputMeta();
    tii.setDatabaseMeta( transMeta.findDatabase( "lookup" ) );
    String selectSQL = "SELECT " + Const.CR;
    selectSQL += "DLR_CD, DLR_NM, DLR_DESC ";
    selectSQL += "FROM " + source_table + " ORDER BY ORDNO;";
    tii.setSQL( selectSQL );

    String fromstepid = registry.getPluginId( StepPluginType.class, tii );
    StepMeta fromstep = new StepMeta( fromstepid, fromstepname, tii );
    fromstep.setLocation( 150, 100 );
    fromstep.setDraw( true );
    fromstep.setDescription( "Reads information from table ["
      + source_table + "] on database [" + lookupDBInfo + "]" );
    transMeta.addStep( fromstep );

    //
    // create the combination lookup/update step...
    //
    String lookupstepname = "lookup from [lookup]";
    CombinationLookupMeta clm = new CombinationLookupMeta();
    String[] lookupKey = { "DLR_CD" };
    clm.setTablename( target_table );
    clm.setKeyField( lookupKey );
    clm.setKeyLookup( lookupKey );
    clm.setTechnicalKeyField( "ID" );
    clm.setTechKeyCreation( CombinationLookupMeta.CREATION_METHOD_TABLEMAX );
    clm.setDatabaseMeta( lookupDBInfo );

    String lookupstepid = registry.getPluginId( StepPluginType.class, clm );
    StepMeta lookupstep = new StepMeta( lookupstepid, lookupstepname, clm );
    lookupstep.setDescription( "Looks up information from table [lookup] on database [" + lookupDBInfo + "]" );
    transMeta.addStep( lookupstep );

    TransHopMeta hi = new TransHopMeta( fromstep, lookupstep );
    transMeta.addTransHop( hi );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );
    trans.execute( null );

    trans.waitUntilFinished();

    checkResults( lookupDatabase );
  }
}
