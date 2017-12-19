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

package org.pentaho.di.trans.steps.accessoutput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransTestFactory;
import org.pentaho.di.trans.steps.accessinput.AccessInputField;
import org.pentaho.di.trans.steps.accessinput.AccessInputMeta;

public class AccessOutputTest {

  private static final String TABLE_NAME = "Users";
  private static final String FIELD_NAME = "UserName";

  @BeforeClass
  public static void before() throws KettleException {
    KettleEnvironment.init( false );
  }

  List<RowMetaAndData> getTestRowMetaAndData( String[] value ) {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();
    Object[] row = new Object[ value.length ];
    RowMetaInterface rm = new RowMeta();
    for ( int i = 0; i < value.length; i++ ) {
      rm.addValueMeta( new ValueMetaString( FIELD_NAME ) );
      row[ i ] = value[ i ];
    }
    list.add( new RowMetaAndData( rm, row ) );
    return list;
  }

  private static AccessInputField[] getInputFields( List<RowMetaAndData> in ) {
    RowMetaAndData rmd = in.get( 0 );
    RowMetaInterface rm = rmd.getRowMeta();
    List<AccessInputField> retval = new ArrayList<AccessInputField>();
    for ( ValueMetaInterface value : rm.getValueMetaList() ) {
      AccessInputField field = new AccessInputField( value.getName() );
      field.setColumn( value.getName() );
      field.setType( value.getType() );
      retval.add( field );
    }
    return retval.toArray( new AccessInputField[retval.size()] );
  }

  private void checkResult( String fileName, String tableName, List<RowMetaAndData> expected ) throws KettleException, IOException {
    assertNotNull( fileName );
    assertNotNull( tableName );
    assertTrue( expected.size() > 0 );

    final String stepName = "My Access Input Step";
    AccessInputMeta stepMeta = new AccessInputMeta();
    stepMeta.allocateFiles( 1 );
    stepMeta.setFileName( new String[] { fileName } );
    stepMeta.setTableName( tableName );
    stepMeta.allocateFields( 1 );
    stepMeta.setInputFields( getInputFields( expected ) );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, stepMeta, stepName );

    TransHopMeta injectHop = transMeta.findTransHop( transMeta.findStep( TransTestFactory.INJECTOR_STEPNAME ),
      transMeta.findStep( stepName ) );
    transMeta.removeTransHop( transMeta.indexOfTransHop( injectHop ) );

    List<RowMetaAndData> ret =
      TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, stepName,
        TransTestFactory.DUMMY_STEPNAME, new ArrayList<RowMetaAndData>() );

    assertEquals( "Expected and actual row size should match", expected.size(), ret.size() );
    for ( int i = 0; i < expected.size(); i++ ) {
      assertEquals( "Checking Row #" + i, expected.get( i ), ret.get( i ) );
    }
  }

  @Test
  public void testProcessRows() throws IOException, KettleException {
    final String stepName = "My Access Output Step";

    File dbFile = File.createTempFile( "AccessOutputTestProcessRows", ".mdb" );
    dbFile.delete();
    AccessOutputMeta stepMeta = new AccessOutputMeta();
    stepMeta.setDefault();
    stepMeta.setFilename( dbFile.getAbsolutePath() );
    stepMeta.setTablename( TABLE_NAME );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, stepMeta, stepName );
    List<RowMetaAndData> inputList = getTestRowMetaAndData( new String[] { "Alice" } );
    TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, stepName,
      TransTestFactory.DUMMY_STEPNAME, inputList );

    dbFile.deleteOnExit();
    checkResult( dbFile.getAbsolutePath(), TABLE_NAME, inputList );
  }

  @Test
  public void testTruncateTable() throws IOException, KettleException {
    final String stepName = "My Access Output Step";

    File dbFile = File.createTempFile( "AccessOutputTestTruncateTable", ".mdb" );
    dbFile.delete();
    AccessOutputMeta stepMeta = new AccessOutputMeta();
    stepMeta.setDefault();
    stepMeta.setFilename( dbFile.getAbsolutePath() );
    stepMeta.setTablename( TABLE_NAME );
    stepMeta.setTableTruncated( true );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, stepMeta, stepName );
    List<RowMetaAndData> inputList = getTestRowMetaAndData( new String[] { "Alice" } );

    for ( int i = 0; i < 3; i++ ) {
      TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, stepName,
        TransTestFactory.DUMMY_STEPNAME, inputList );
    }

    dbFile.deleteOnExit();
    checkResult( dbFile.getAbsolutePath(), TABLE_NAME, inputList );
  }

  @Test
  public void testNoTruncateTable() throws IOException, KettleException {
    final String stepName = "My Access Output Step";

    File dbFile = File.createTempFile( "AccessOutputTestNoTruncateTable", ".mdb" );
    dbFile.delete();
    AccessOutputMeta stepMeta = new AccessOutputMeta();
    stepMeta.setDefault();
    stepMeta.setFilename( dbFile.getAbsolutePath() );
    stepMeta.setTablename( TABLE_NAME );
    stepMeta.setTableTruncated( false );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, stepMeta, stepName );
    List<RowMetaAndData> inputList = getTestRowMetaAndData( new String[] { "Alice" } );

    List<RowMetaAndData> expected = new ArrayList<RowMetaAndData>();

    // Execute the transformation 3 times, we should have more rows than just a single execution
    for ( int i = 0; i < 3; i++ ) {
      TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, stepName,
        TransTestFactory.DUMMY_STEPNAME, inputList );
      expected.add( inputList.get( 0 ).clone() );
    }

    dbFile.deleteOnExit();
    checkResult( dbFile.getAbsolutePath(), TABLE_NAME, expected );
  }
}
