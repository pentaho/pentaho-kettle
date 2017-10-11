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
