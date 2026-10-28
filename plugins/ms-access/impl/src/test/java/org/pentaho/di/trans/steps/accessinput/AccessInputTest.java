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

package org.pentaho.di.trans.steps.accessinput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransTestFactory;

import com.healthmarketscience.jackcess.ColumnBuilder;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.TableBuilder;

public class AccessInputTest {

  private static final String STEP_NAME = "Access Input";
  private static final String COLUMN = "Data";
  private static final String SECRET_VALUE = "TOP-SECRET";

  @ClassRule
  public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  @BeforeClass
  public static void before() throws KettleException {
    KettleEnvironment.init( false );
  }

  @Test
  public void linkedTableIsNotResolved() throws Exception {
    File secret = createDatabase( "secret.mdb", "Secret", SECRET_VALUE );
    File attack = createDatabase( "attack.mdb", "Normal", "harmless" );
    try ( Database db = DatabaseBuilder.open( attack ) ) {
      db.createLinkedTable( "EvilLinked", secret.getAbsolutePath(), "Secret" );
    }

    try {
      List<RowMetaAndData> rows = readTable( attack, "EvilLinked" );
      fail( "Linked table was resolved and read " + rows.size() + " row(s) from " + secret + ": " + rows );
    } catch ( KettleException expected ) {
      // the step must refuse to follow the link
    }
  }

  @Test
  public void regularTableIsStillRead() throws Exception {
    File db = createDatabase( "plain.mdb", "Normal", "harmless" );

    List<RowMetaAndData> rows = readTable( db, "Normal" );

    assertEquals( 1, rows.size() );
    assertEquals( "harmless", rows.get( 0 ).getString( COLUMN, null ) );
  }

  private File createDatabase( String fileName, String tableName, String value ) throws IOException {
    File file = new File( tmp.getRoot(), fileName );
    try ( Database db = new DatabaseBuilder( file ).setFileFormat( Database.FileFormat.V2000 ).create() ) {
      Table table = new TableBuilder( tableName )
        .addColumn( new ColumnBuilder( COLUMN, DataType.TEXT ) )
        .toTable( db );
      table.addRow( value );
    }
    return file;
  }

  private List<RowMetaAndData> readTable( File file, String tableName ) throws KettleException {
    AccessInputField field = new AccessInputField( COLUMN );
    field.setColumn( COLUMN );
    field.setType( ValueMetaInterface.TYPE_STRING );

    AccessInputMeta meta = new AccessInputMeta();
    meta.allocateFiles( 1 );
    meta.setFileName( new String[] { file.getAbsolutePath() } );
    meta.setTableName( tableName );
    meta.allocateFields( 1 );
    meta.setInputFields( new AccessInputField[] { field } );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, meta, STEP_NAME );
    TransHopMeta injectHop = transMeta.findTransHop( transMeta.findStep( TransTestFactory.INJECTOR_STEPNAME ),
      transMeta.findStep( STEP_NAME ) );
    transMeta.removeTransHop( transMeta.indexOfTransHop( injectHop ) );

    return TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, STEP_NAME,
      TransTestFactory.DUMMY_STEPNAME, new ArrayList<>() );
  }
}
