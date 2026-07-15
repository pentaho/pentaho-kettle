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


package org.pentaho.di.trans.steps.accessoutput;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;

import com.healthmarketscience.jackcess.ColumnBuilder;
import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.CursorBuilder;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.TableBuilder;

/**
 * @author Matt
 * @since 24-jan-2005
 */
public class AccessOutputData extends BaseStepData {
  public Database db;
  public Table table;
  public List<Object[]> rows;
  public RowMetaInterface outputRowMeta;
  public boolean oneFileOpened;

  public AccessOutputData() {
    super();
    rows = new ArrayList<>();
    oneFileOpened = false;
  }

  void createDatabase( File databaseFile ) throws IOException {
    db = new DatabaseBuilder( databaseFile ).setFileFormat( Database.FileFormat.V2000 ).create();
  }

  void openDatabase( File databaseFile ) throws IOException {
    db = DatabaseBuilder.open( databaseFile );
  }

  void closeDatabase() throws IOException {
    db.close();
  }

  void createTable( String tableName, RowMetaInterface rowMeta ) throws IOException {
    List<ColumnBuilder> columns = AccessOutputMeta.getColumns( rowMeta );
    table = new TableBuilder( tableName ).addColumns( columns ).toTable( db );
  }

  void addRowToTable( Object... row ) throws IOException {
    table.addRow( row );
  }

  void addRowsToTable( List<Object[]> rows ) throws IOException {
    table.addRows( rows );
  }

  void truncateTable() throws IOException {
    if ( table == null ) {
      return;
    }
    Cursor tableRows = CursorBuilder.createCursor( table );
    while ( tableRows.moveToNextRow() ) {
      tableRows.deleteCurrentRow();
    }
  }
}
