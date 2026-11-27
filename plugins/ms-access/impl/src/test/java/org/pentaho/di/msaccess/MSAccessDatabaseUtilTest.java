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

package org.pentaho.di.msaccess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pentaho.di.core.exception.KettleException;

import com.healthmarketscience.jackcess.ColumnBuilder;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.TableBuilder;

public class MSAccessDatabaseUtilTest {
  private static final String COLUMN = "Data";

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  @Test
  public void openRefusesToResolveLinkedTable() throws Exception {
    File secret = createDatabaseWithRow( "secret.mdb", "Secret", "TOP-SECRET" );
    File attack = createDatabaseWithRow( "attack.mdb", "Normal", "harmless" );
    
    try ( Database db = MSAccessDatabaseUtil.open( attack, false ) ) {
      db.createLinkedTable( "EvilLinked", secret.getAbsolutePath(), "Secret" );
    }

    for ( boolean readOnly : new boolean[] { true, false } ) {
      try ( Database db = MSAccessDatabaseUtil.open( attack, readOnly ) ) {
        try {
          MSAccessDatabaseUtil.getTable( db, "EvilLinked" );
          fail( "Linked table should not be resolved" );
        } catch ( KettleException e ) {
          assertTrue( e.getCause() instanceof IOException );
          assertTrue( e.getMessage(), e.getMessage().contains( "Unable to read table EvilLinked" ) );
          assertTrue( e.getMessage(), e.getMessage().contains( "Linked tables are not supported" ) );
          assertTrue( e.getMessage(), e.getMessage().contains( secret.getAbsolutePath() ) );
        }

        Table normal = MSAccessDatabaseUtil.getTable( db, "Normal" );
        assertEquals( "harmless", normal.getNextRow().getString( COLUMN ) );
      }
    }
  }

  @Test
  public void createDisablesLinkedDatabases() throws Exception {
    try ( Database db = MSAccessDatabaseUtil.create( new File( tmp.getRoot(), "new.mdb" ),
      Database.FileFormat.V2000 ) ) {
      assertSame( MSAccessDatabaseUtil.LINKED_DATABASES_DISABLED, db.getLinkResolver() );
    }
  }

  private File createDatabaseWithRow( String fileName, String tableName, String value ) throws IOException {
    File file = new File( tmp.getRoot(), fileName );
    
    try ( Database db = MSAccessDatabaseUtil.create( file, Database.FileFormat.V2000 ) ) {
      new TableBuilder( tableName )
        .addColumn( new ColumnBuilder( COLUMN, DataType.TEXT ) )
        .toTable( db )
        .addRow( value );
    }
    
    return file;
  }
}
