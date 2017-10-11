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

package org.pentaho.di.trans.steps.tableagilemart;

import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.DBCache;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.TableManager;
import org.pentaho.di.core.database.MonetDBDatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.tableoutput.TableOutput;
import org.pentaho.di.trans.steps.tableoutput.TableOutputData;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;

public class TableAgileMart extends TableOutput implements TableManager {

  private static Class<?> PKG = TableAgileMartMeta.class; // for i18n purposes, needed by Translator2!!

  private String message = null;

  private long rowLimit = -1;
  private long rowsWritten = -1;
  private AgileMartUtil util = new AgileMartUtil();

  public TableAgileMart( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    boolean result = super.init( smi, sdi );
    rowsWritten = 0;
    util.setLog( log );
    return result;
  }

  @Override
  public boolean adjustSchema() {
    TableOutputMeta meta = getMeta();
    TableOutputData data = getData();
    TransMeta transMeta = getTransMeta();
    StepMeta stepMeta = meta.getParentStepMeta();
    DBCache dbcache = transMeta.getDbCache();
    StringBuilder messageBuffer = new StringBuilder();

    try {
      RowMetaInterface prev = transMeta.getPrevStepFields( stepMeta.getName() );
      if ( log.isDetailed() ) {
        logDetailed( "Attempting to auto adjust table structure" );
      }

      if ( log.isDetailed() ) {
        logDetailed( "getTransMeta: " + getTransMeta() );
      }
      if ( log.isDetailed() ) {
        logDetailed( "getStepname: " + getStepname() );
      }

      SQLStatement statement = meta.getSQLStatements( transMeta, stepMeta, prev, repository, metaStore );

      if ( log.isDetailed() ) {
        logDetailed( "Statement: " + statement );
      }
      if ( log.isDetailed() && statement != null ) {
        logDetailed( "Statement has SQL: " + statement.hasSQL() );
      }

      if ( statement != null && statement.hasSQL() ) {
        String sql = statement.getSQL();
        if ( log.isDetailed() ) {
          logDetailed( "Trying: " + sql );
        }

        try {
          log.logDetailed( "Executing SQL: " + Const.CR + sql );
          data.db.execStatement( sql );

          // Clear the database cache, in case we're using one...
          if ( dbcache != null ) {
            dbcache.clear( data.databaseMeta.getName() );
          }
        } catch ( Exception dbe ) {
          String error = BaseMessages.getString( PKG, "SQLEditor.Log.SQLExecError", sql, dbe.toString() );
          messageBuffer.append( error ).append( Const.CR );
          return false;
        }

        if ( log.isDetailed() ) {
          logDetailed( "Successfull: " + sql );
        }
      } else if ( statement.getError() == null ) {
        // there were no changes to be made
        return true;
      } else {
        this.message = statement.getError();
        logError( statement.getError() );
        return false;
      }
    } catch ( Exception e ) {
      logError( "An error ocurred trying to adjust the table schema", e );
    }
    return true;
  }

  protected Object[] writeToTable( RowMetaInterface rowMeta, Object[] r ) throws KettleException {
    // see if we need to truncate any string fields
    try {
      int index = 0;
      List<ValueMetaInterface> valueMetas = rowMeta.getValueMetaList();
      for ( ValueMetaInterface valueMeta : valueMetas ) {
        Object valueData = r[index];
        if ( valueData != null ) {
          if ( valueMeta.getType() == ValueMetaInterface.TYPE_STRING ) {
            String str = valueMeta.getString( valueData );
            int len = valueMeta.getLength();
            if ( len < 1 ) {
              len = MonetDBDatabaseMeta.DEFAULT_VARCHAR_LENGTH;
            }
            if ( str.length() > len ) {
              // TODO log this event
              str = str.substring( 0, len );
            }
            r[index] = str;
          }
        }
        index++;
      }
    } catch ( Exception e ) {
      throw new KettleException( "Error serializing rows of data to the psql command", e );
    }
    return super.writeToTable( rowMeta, r );
  }

  @Override
  public boolean flush() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    if ( rowLimit != -1 && rowsWritten >= rowLimit ) {
      // we are done, ignore any new rows
      TableOutputMeta meta = (TableOutputMeta) smi;
      util.updateMetadata( meta, rowsWritten );
      throw new KettleException( "Row limit exceeded" );
    }
    boolean result = super.processRow( smi, sdi );
    if ( result ) {
      rowsWritten++;
    } else {
      TableOutputMeta meta = (TableOutputMeta) smi;
      util.updateMetadata( meta, rowsWritten );
    }
    return result;
  }

  @Override
  public boolean dropTable() {
    TableOutputMeta meta = getMeta();
    TableOutputData data = getData();
    String schema = meta.getSchemaName();
    String table = meta.getTableName();
    if ( schema != null && !schema.equals( "" ) ) {
      table = schema + "." + table;
    }
    String sql = "drop table " + table + ";";
    try {
      Result result = data.db.execStatement( sql );
      int status = result.getExitStatus();
      if ( status == 0 ) {
        util.updateMetadata( meta, -1 );
      }
      return status == 0;
    } catch ( KettleDatabaseException e ) {
      message = "Could not drop table: " + table;
      logError( message, e );
    }
    return false;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public void setRowLimit( long rowLimit ) {
    this.rowLimit = rowLimit;
  }

  @Override
  public void setTableName( String tableName ) {
    getMeta().setTableName( tableName );
  }

  @Override
  public boolean truncateTable() {
    TableOutputMeta meta = getMeta();
    TableOutputData data = getData();
    try {
      data.db.truncateTable( environmentSubstitute( meta.getSchemaName() ), environmentSubstitute( meta
        .getTableName() ) );
      util.updateMetadata( meta, -1 );
      return true;
    } catch ( KettleDatabaseException e ) {
      message = "Could not truncate table: " + meta.getTableName();
      logError( message, e );
    }
    return false;
  }

}
