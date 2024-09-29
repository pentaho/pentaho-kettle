/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.trans.steps.accessoutput;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

/**
 * @author Matt
 * @since 24-jan-2005
 */
public class AccessOutputData extends BaseStepData implements StepDataInterface {
  public Database db;
  public Table table;
  public List<Object[]> rows;
  public RowMetaInterface outputRowMeta;
  public boolean oneFileOpened;

  public AccessOutputData() {
    super();
    rows = new ArrayList<Object[]>();
    oneFileOpened = false;
  }

  void createDatabase( File databaseFile ) throws IOException {
    db = Database.create( databaseFile );
  }

  void openDatabase( File databaseFile ) throws IOException {
    db = Database.open( databaseFile );
  }

  void closeDatabase() throws IOException {
    db.close();
  }

  void createTable( String tableName, RowMetaInterface rowMeta ) throws IOException {
    List<Column> columns = AccessOutputMeta.getColumns( rowMeta );
    db.createTable( tableName, columns );
    table = db.getTable( tableName );
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
    Cursor tableRows = Cursor.createCursor( table );
    while ( tableRows.moveToNextRow() ) {
      tableRows.deleteCurrentRow();
    }
  }
}
