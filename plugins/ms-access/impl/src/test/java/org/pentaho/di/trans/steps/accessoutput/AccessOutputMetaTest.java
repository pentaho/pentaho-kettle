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


package org.pentaho.di.trans.steps.accessoutput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.ColumnBuilder;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.TableBuilder;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

public class AccessOutputMetaTest {
  LoadSaveTester loadSaveTester;
  Class<AccessOutputMeta> testMetaClass = AccessOutputMeta.class;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setUpLoadSave() throws Exception {
    PluginRegistry.init( false );
    List<String> attributes =
      Arrays.asList( "filename", "fileCreated", "tablename", "tableCreated", "tableTruncated", "commitSize", "addToResultFiles", "DoNotOpenNewFileInit" );

    Map<String, String> getterMap = new HashMap<String, String>();
    Map<String, String> setterMap = new HashMap<String, String>();

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
      new LoadSaveTester( testMetaClass, attributes, getterMap, setterMap, attrValidatorMap, typeValidatorMap );
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  @Test
  public void testDefaults() {
    AccessOutputMeta stepMeta = new AccessOutputMeta();
    stepMeta.setDefault();
    assertTrue( stepMeta.isFileCreated() );
    assertTrue( stepMeta.isTableCreated() );
    assertTrue( stepMeta.isAddToResultFiles() );
    assertEquals( 500, stepMeta.getCommitSize() );
    assertFalse( stepMeta.isTableTruncated() );
    assertFalse( stepMeta.isDoNotOpenNewFileInit() );
  }

  @Test
  public void testGetRequiredFieldsReturnsLayoutFromAccessTable() throws Exception {
    File databaseFile = File.createTempFile( "AccessOutputMetaLayout", ".mdb" );
    databaseFile.delete();
    databaseFile.deleteOnExit();

    try ( Database database = new DatabaseBuilder( databaseFile ).setFileFormat( Database.FileFormat.V2000 ).create() ) {
      new TableBuilder( "Users" )
        .addColumn( new ColumnBuilder( "id", DataType.LONG ) )
        .addColumn( new ColumnBuilder( "name", DataType.TEXT ).setLength( 50 ) )
        .addColumn( new ColumnBuilder( "active", DataType.BOOLEAN ) )
        .toTable( database );
    }

    AccessOutputMeta meta = new AccessOutputMeta();
    meta.setFilename( databaseFile.getAbsolutePath() );
    meta.setTablename( "Users" );

    RowMetaInterface fields = meta.getRequiredFields( new Variables() );

    assertEquals( 3, fields.size() );
    assertEquals( "id", fields.getValueMeta( 0 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, fields.getValueMeta( 0 ).getType() );
    assertEquals( "name", fields.getValueMeta( 1 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_STRING, fields.getValueMeta( 1 ).getType() );
    assertEquals( "active", fields.getValueMeta( 2 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_BOOLEAN, fields.getValueMeta( 2 ).getType() );
  }

  @Test
  public void testGetColumnsAndCreateObjectsForRowHandleConvertedTypes() throws Exception {
    RowMetaInterface rowMeta = new RowMeta();

    ValueMetaInteger tinyInteger = new ValueMetaInteger( "tinyInteger" );
    tinyInteger.setLength( 2 );
    rowMeta.addValueMeta( tinyInteger );

    ValueMetaInteger smallInteger = new ValueMetaInteger( "smallInteger" );
    smallInteger.setLength( 4 );
    rowMeta.addValueMeta( smallInteger );

    ValueMetaInteger longInteger = new ValueMetaInteger( "longInteger" );
    longInteger.setLength( 9 );
    rowMeta.addValueMeta( longInteger );

    ValueMetaString longString = new ValueMetaString( "longString" );
    longString.setLength( 300 );
    rowMeta.addValueMeta( longString );

    File databaseFile = File.createTempFile( "AccessOutputMetaColumns", ".mdb" );
    databaseFile.delete();
    databaseFile.deleteOnExit();

    try ( Database database = new DatabaseBuilder( databaseFile ).setFileFormat( Database.FileFormat.V2000 ).create() ) {
      Table table = new TableBuilder( "ColumnTypes" ).addColumns( AccessOutputMeta.getColumns( rowMeta ) ).toTable( database );
      List<? extends Column> columns = table.getColumns();

      assertEquals( DataType.BYTE, columns.get( 0 ).getType() );
      assertEquals( DataType.INT, columns.get( 1 ).getType() );
      assertEquals( DataType.LONG, columns.get( 2 ).getType() );
      assertEquals( DataType.MEMO, columns.get( 3 ).getType() );
    }

    Object[] convertedObjects = AccessOutputMeta.createObjectsForRow( rowMeta, new Object[] { 1L, 2L, 3L, "value" } );

    assertNotNull( convertedObjects );
    assertEquals( Byte.class, convertedObjects[0].getClass() );
    assertEquals( Short.class, convertedObjects[1].getClass() );
    assertEquals( Long.class, convertedObjects[2].getClass() );
    assertEquals( String.class, convertedObjects[3].getClass() );
  }
}
