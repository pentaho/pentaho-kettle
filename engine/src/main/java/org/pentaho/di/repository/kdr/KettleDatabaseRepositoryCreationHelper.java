// CHECKSTYLE:FileLength:OFF
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

package org.pentaho.di.repository.kdr;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.repository.LongObjectId;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryConnectionDelegate;

public class KettleDatabaseRepositoryCreationHelper {

  private final KettleDatabaseRepository repository;
  private final LogChannelInterface log;
  private final DatabaseMeta databaseMeta;
  private final Database database;

  private final PluginRegistry pluginRegistry;

  public KettleDatabaseRepositoryCreationHelper( KettleDatabaseRepository repository ) {
    this.repository = repository;
    this.databaseMeta = this.repository.getDatabaseMeta();
    this.database = this.repository.getDatabase();

    this.log = repository.getLog();
    this.pluginRegistry = PluginRegistry.getInstance();
  }

  /**
   * Create or upgrade repository tables & fields, populate lookup tables, ...
   *
   * @param monitor
   *          The progress monitor to use, or null if no monitor is present.
   * @param upgrade
   *          True if you want to upgrade the repository, false if you want to create it.
   * @param statements
   *          the list of statements to populate
   * @param dryrun
   *          true if we don't actually execute the statements
   *
   * @throws KettleException
   *           in case something goes wrong!
   */
  public synchronized void createRepositorySchema( ProgressMonitorListener monitor, boolean upgrade,
    List<String> statements, boolean dryrun ) throws KettleException {
    RowMetaInterface table;
    String sql;
    String tablename;
    String schemaTable;
    String indexname;
    String[] keyfield;
    String[] user, pass, code, desc;

    int KEY = 9; // integer, no need for bigint!

    log.logBasic( "Starting to create or modify the repository tables..." );
    String message = ( upgrade ? "Upgrading " : "Creating" ) + " the Kettle repository...";
    if ( monitor != null ) {
      monitor.beginTask( message, 31 );
    }

    repository.connectionDelegate.setAutoCommit( true );

    // ////////////////////////////////////////////////////////////////////////////////
    // R_LOG
    //
    // Log the operations we do in the repository.
    //
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_REPOSITORY_LOG;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table
      .addValueMeta( new ValueMetaInteger(
        KettleDatabaseRepository.FIELD_REPOSITORY_LOG_ID_REPOSITORY_LOG, KEY, 0 ) );
    table.addValueMeta( new ValueMetaString( KettleDatabaseRepository.FIELD_REPOSITORY_LOG_REP_VERSION,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaDate( KettleDatabaseRepository.FIELD_REPOSITORY_LOG_LOG_DATE ) );
    table.addValueMeta( new ValueMetaString( KettleDatabaseRepository.FIELD_REPOSITORY_LOG_LOG_USER,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString( KettleDatabaseRepository.FIELD_REPOSITORY_LOG_OPERATION_DESC,
      KettleDatabaseRepository.REP_STRING_LENGTH, 0 ) );
    sql =
      database.getDDL(
        schemaTable, table, null, false, KettleDatabaseRepository.FIELD_REPOSITORY_LOG_ID_REPOSITORY_LOG,
        false );

    if ( !Utils.isEmpty( sql ) ) {
      statements.add( sql );
      if ( !dryrun ) {
        try {
          if ( log.isDetailed() ) {
            log.logDetailed( "executing SQL statements: " + Const.CR + sql );
          }
          database.execStatements( sql );
          if ( log.isDetailed() ) {
            log.logDetailed( "Created/altered table " + schemaTable );
          }
        } catch ( KettleException dbe ) {
          throw new KettleException( "Unable to create or modify table " + schemaTable, dbe );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }

    if ( !dryrun ) {
      repository.insertLogEntry( ( upgrade ? "Upgrade" : "Creation" ) + " of the Kettle repository" );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    // R_VERSION
    //
    // Let's start with the version table
    //
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_VERSION;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_VERSION_ID_VERSION, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_VERSION_MAJOR_VERSION, 3, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_VERSION_MINOR_VERSION, 3, 0 ) );
    table.addValueMeta( new ValueMetaDate(
      KettleDatabaseRepository.FIELD_VERSION_UPGRADE_DATE, 0, 0 ) );
    table.addValueMeta( new ValueMetaBoolean(
      KettleDatabaseRepository.FIELD_VERSION_IS_UPGRADE, 1, 0 ) );
    sql =
      database
        .getDDL( schemaTable, table, null, false, KettleDatabaseRepository.FIELD_VERSION_ID_VERSION, false );
    boolean create = false;
    if ( !Utils.isEmpty( sql ) ) {
      create = sql.toUpperCase().indexOf( "CREATE TABLE" ) >= 0;
      statements.add( sql );
      if ( !dryrun ) {
        try {
          if ( log.isDetailed() ) {
            log.logDetailed( "executing SQL statements: " + Const.CR + sql );
          }
          database.execStatements( sql );
          if ( log.isDetailed() ) {
            log.logDetailed( "Created/altered table " + schemaTable );
          }
        } catch ( KettleException dbe ) {
          throw new KettleException( "Unable to create or modify table " + schemaTable, dbe );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }

    // Insert an extra record in R_VERSION every time we pass here...
    //
    try {
      // if the table doesn't exist, don't try to grab an ID from it...
      LongObjectId nextId;
      if ( sql.toUpperCase().indexOf( "CREATE TABLE" ) < 0 ) {
        nextId =
          repository.connectionDelegate.getNextID(
            schemaTable, KettleDatabaseRepository.FIELD_VERSION_ID_VERSION );
      } else {
        nextId = new LongObjectId( 1L );
      }
      Object[] data =
        new Object[] {
          nextId.longValue(),
          Long.valueOf( KettleDatabaseRepositoryConnectionDelegate.REQUIRED_MAJOR_VERSION ),
          Long.valueOf( KettleDatabaseRepositoryConnectionDelegate.REQUIRED_MINOR_VERSION ), new Date(),
          Boolean.valueOf( upgrade ), };
      if ( dryrun ) {
        sql = database.getSQLOutput( null, KettleDatabaseRepository.TABLE_R_VERSION, table, data, null );
        statements.add( sql );
      } else {
        database.execStatement( "INSERT INTO "
          + databaseMeta.getQuotedSchemaTableCombination( null, KettleDatabaseRepository.TABLE_R_VERSION )
          + " VALUES(?, ?, ?, ?, ?)", table, data );
      }
    } catch ( KettleException e ) {
      throw new KettleException( "Unable to insert new version log record into " + schemaTable, e );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    // R_DATABASE_TYPE
    //
    // Create table...
    //
    boolean ok_database_type = true;
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_DATABASE_TYPE;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_DATABASE_TYPE_ID_DATABASE_TYPE, KEY, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_DATABASE_TYPE_CODE,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_DATABASE_TYPE_DESCRIPTION,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    sql =
      database.getDDL(
        schemaTable, table, null, false, KettleDatabaseRepository.FIELD_DATABASE_TYPE_ID_DATABASE_TYPE, false );
    create = false;
    if ( !Utils.isEmpty( sql ) ) {
      create = sql.toUpperCase().indexOf( "CREATE TABLE" ) >= 0;
      statements.add( sql );
      if ( !dryrun ) {
        try {
          if ( log.isDetailed() ) {
            log.logDetailed( "executing SQL statements: " + Const.CR + sql );
          }
          database.execStatements( sql );
          if ( log.isDetailed() ) {
            log.logDetailed( "Created/altered table " + schemaTable );
          }
        } catch ( KettleException dbe ) {
          throw new KettleException( "Unable to create or modify table " + schemaTable, dbe );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }

    if ( ok_database_type ) {
      //
      // Populate...
      //
      updateDatabaseTypes( statements, dryrun, create );
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_DATABASE_CONTYPE
    //
    // Create table...
    //
    boolean ok_database_contype = true;
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_DATABASE_CONTYPE;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_DATABASE_CONTYPE_ID_DATABASE_CONTYPE, KEY,
      0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_DATABASE_CONTYPE_CODE,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_DATABASE_CONTYPE_DESCRIPTION,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    sql =
      database.getDDL(
        schemaTable, table, null, false, KettleDatabaseRepository.FIELD_DATABASE_CONTYPE_ID_DATABASE_CONTYPE,
        false );

    if ( !Utils.isEmpty( sql ) ) {
      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }

    // If it's creating the table, go ahead and populate below...
    //
    ok_database_contype = sql.toUpperCase().contains( "CREATE TABLE" );
    if ( ok_database_contype ) {
      //
      // Populate with data...
      //
      code = DatabaseMeta.dbAccessTypeCode;
      desc = DatabaseMeta.dbAccessTypeDesc;

      if ( !dryrun ) {
        database.prepareInsert( table, null, tablename );
      }

      for ( int i = 0; i < code.length; i++ ) {
        RowMetaAndData lookup = null;
        if ( upgrade ) {
          lookup =
            database.getOneRow( "SELECT "
              + repository.quote( KettleDatabaseRepository.FIELD_DATABASE_CONTYPE_ID_DATABASE_CONTYPE )
              + " FROM " + schemaTable + " WHERE "
              + repository.quote( KettleDatabaseRepository.FIELD_DATABASE_CONTYPE_CODE ) + " = '" + code[i]
              + "'" );
        }
        if ( lookup == null ) {
          ObjectId nextid = new LongObjectId( i + 1 );
          if ( !create ) {
            nextid = repository.connectionDelegate.getNextDatabaseConnectionTypeID();
          }

          Object[] tableData = new Object[] { new LongObjectId( nextid ).longValue(), code[i], desc[i], };
          if ( dryrun ) {
            sql = database.getSQLOutput( null, tablename, table, tableData, null );
            statements.add( sql );
          } else {
            database.setValuesInsert( table, tableData );
            database.insertRow();
          }
        }
      }

      try {
        if ( !dryrun ) {
          database.closeInsert();
        }
        if ( log.isDetailed() ) {
          log.logDetailed( "Populated table " + schemaTable );
        }
      } catch ( KettleException dbe ) {
        throw new KettleException( "Unable to close insert after populating table " + schemaTable, dbe );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_NOTE
    //
    // Create table...
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_NOTE;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_NOTE_ID_NOTE, KEY, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_NOTE_VALUE_STR,
      KettleDatabaseRepository.REP_STRING_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_NOTE_GUI_LOCATION_X, 6, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_NOTE_GUI_LOCATION_Y, 6, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_NOTE_GUI_LOCATION_WIDTH, 6, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_NOTE_GUI_LOCATION_HEIGHT, 6, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_NOTE_FONT_NAME,
      KettleDatabaseRepository.REP_STRING_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_NOTE_FONT_SIZE, 6, 0 ) );
    table.addValueMeta( new ValueMetaBoolean(
      KettleDatabaseRepository.FIELD_NOTE_FONT_BOLD, 1, 0 ) );
    table.addValueMeta( new ValueMetaBoolean(
      KettleDatabaseRepository.FIELD_NOTE_FONT_ITALIC, 1, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_NOTE_COLOR_RED, 6, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_NOTE_COLOR_GREEN, 6, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_NOTE_COLOR_BLUE, 6, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_NOTE_BACK_GROUND_COLOR_RED, 6, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_NOTE_BACK_GROUND_COLOR_GREEN, 6, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_NOTE_BACK_GROUND_COLOR_BLUE, 6, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_NOTE_BORDER_COLOR_RED, 6, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_NOTE_BORDER_COLOR_GREEN, 6, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_NOTE_BORDER_COLOR_BLUE, 6, 0 ) );
    table.addValueMeta( new ValueMetaBoolean(
      KettleDatabaseRepository.FIELD_NOTE_DRAW_SHADOW, 1, 0 ) );

    sql = database.getDDL( schemaTable, table, null, false, KettleDatabaseRepository.FIELD_NOTE_ID_NOTE, false );

    if ( !Utils.isEmpty( sql ) ) {
      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_DATABASE
    //
    // Create table...
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_DATABASE;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_DATABASE_ID_DATABASE, KEY, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_DATABASE_NAME,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_DATABASE_ID_DATABASE_TYPE, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_DATABASE_ID_DATABASE_CONTYPE, KEY, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_DATABASE_HOST_NAME,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_DATABASE_DATABASE_NAME,
      KettleDatabaseRepository.REP_STRING_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_DATABASE_PORT, 7, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_DATABASE_USERNAME,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_DATABASE_PASSWORD,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_DATABASE_SERVERNAME,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_DATABASE_DATA_TBS,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_DATABASE_INDEX_TBS,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    sql =
      database.getDDL(
        schemaTable, table, null, false, KettleDatabaseRepository.FIELD_DATABASE_ID_DATABASE, false );

    if ( !Utils.isEmpty( sql ) ) {
      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_DATABASE_ATTRIBUTE
    //
    // Create table...
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_DATABASE_ATTRIBUTE;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_DATABASE_ATTRIBUTE_ID_DATABASE_ATTRIBUTE,
      KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_DATABASE_ATTRIBUTE_ID_DATABASE, KEY, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_DATABASE_ATTRIBUTE_CODE,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_DATABASE_ATTRIBUTE_VALUE_STR,
      KettleDatabaseRepository.REP_STRING_LENGTH, 0 ) );
    sql =
      database.getDDL(
        schemaTable, table, null, false,
        KettleDatabaseRepository.FIELD_DATABASE_ATTRIBUTE_ID_DATABASE_ATTRIBUTE, false );

    if ( !Utils.isEmpty( sql ) ) {
      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
      try {
        indexname = KettleDatabaseRepositoryBase.IDX_R_DATABASE_ATTRIBUTE;
        keyfield =
          new String[] {
            KettleDatabaseRepository.FIELD_DATABASE_ATTRIBUTE_ID_DATABASE,
            KettleDatabaseRepository.FIELD_DATABASE_ATTRIBUTE_CODE, };
        if ( !database.checkIndexExists( schemaTable, keyfield ) ) {
          sql = database.getCreateIndexStatement( schemaTable, indexname, keyfield, false, true, false, false );
          statements.add( sql );
          if ( !dryrun ) {
            if ( log.isDetailed() ) {
              log.logDetailed( "executing SQL statements: " + Const.CR + sql );
            }
            database.execStatements( sql );
            if ( log.isDetailed() ) {
              log.logDetailed( "Created lookup index " + indexname + " on " + schemaTable );
            }
          }
        }
      } catch ( KettleException kdbe ) {
        // Ignore this one: index is not properly detected, it already exists...
      }

    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_DIRECTORY
    //
    // Create table...
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_DIRECTORY;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY_PARENT, KEY, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_DIRECTORY_DIRECTORY_NAME,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    sql =
      database.getDDL(
        schemaTable, table, null, false, KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY, false );

    if ( !Utils.isEmpty( sql ) ) {
      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }

      try {
        indexname = KettleDatabaseRepositoryBase.IDX_R_DIRECTORY;
        keyfield =
          new String[] {
            KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY_PARENT,
            KettleDatabaseRepository.FIELD_DIRECTORY_DIRECTORY_NAME };
        if ( !database.checkIndexExists( schemaTable, keyfield ) ) {
          sql = database.getCreateIndexStatement( schemaTable, indexname, keyfield, false, true, false, false );
          statements.add( sql );
          if ( !dryrun ) {
            if ( log.isDetailed() ) {
              log.logDetailed( "executing SQL statements: " + Const.CR + sql );
            }
            database.execStatements( sql );
            if ( log.isDetailed() ) {
              log.logDetailed( "Created lookup index " + indexname + " on " + schemaTable );
            }
          }
        }
      } catch ( KettleException kdbe ) {
        // Ignore this one: index is not properly detected, it already exists...
      }

    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_TRANSFORMATION
    //
    // Create table...
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_TRANSFORMATION;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANSFORMATION_ID_TRANSFORMATION, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANSFORMATION_ID_DIRECTORY, KEY, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_TRANSFORMATION_NAME,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_TRANSFORMATION_DESCRIPTION,
      KettleDatabaseRepository.REP_STRING_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_TRANSFORMATION_EXTENDED_DESCRIPTION,
      KettleDatabaseRepository.REP_STRING_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_TRANSFORMATION_TRANS_VERSION,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANSFORMATION_TRANS_STATUS, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANSFORMATION_ID_STEP_READ, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANSFORMATION_ID_STEP_WRITE, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANSFORMATION_ID_STEP_INPUT, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANSFORMATION_ID_STEP_OUTPUT, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANSFORMATION_ID_STEP_UPDATE, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANSFORMATION_ID_DATABASE_LOG, KEY, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_TRANSFORMATION_TABLE_NAME_LOG,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaBoolean(
      KettleDatabaseRepository.FIELD_TRANSFORMATION_USE_BATCHID, 1, 0 ) );
    table.addValueMeta( new ValueMetaBoolean(
      KettleDatabaseRepository.FIELD_TRANSFORMATION_USE_LOGFIELD, 1, 0 ) );
    table
      .addValueMeta( new ValueMetaInteger(
        KettleDatabaseRepository.FIELD_TRANSFORMATION_ID_DATABASE_MAXDATE,
        KEY, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_TRANSFORMATION_TABLE_NAME_MAXDATE,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_TRANSFORMATION_FIELD_NAME_MAXDATE,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaNumber(
      KettleDatabaseRepository.FIELD_TRANSFORMATION_OFFSET_MAXDATE, 12, 2 ) );
    table.addValueMeta( new ValueMetaNumber(
      KettleDatabaseRepository.FIELD_TRANSFORMATION_DIFF_MAXDATE, 12, 2 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_TRANSFORMATION_CREATED_USER,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaDate(
      KettleDatabaseRepository.FIELD_TRANSFORMATION_CREATED_DATE, 20, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_TRANSFORMATION_MODIFIED_USER,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaDate(
      KettleDatabaseRepository.FIELD_TRANSFORMATION_MODIFIED_DATE, 20, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANSFORMATION_SIZE_ROWSET, KEY, 0 ) );
    sql =
      database.getDDL(
        schemaTable, table, null, false, KettleDatabaseRepository.FIELD_TRANSFORMATION_ID_TRANSFORMATION,
        false );

    if ( !Utils.isEmpty( sql ) ) {
      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }

    // In case of an update, the added column R_TRANSFORMATION.ID_DIRECTORY == NULL!!!
    //
    if ( database.checkTableExists( schemaTable ) ) {
      sql =
        "SELECT * FROM "
          + schemaTable + " WHERE "
          + repository.quote( KettleDatabaseRepository.FIELD_TRANSFORMATION_ID_DIRECTORY ) + " IS NULL";
      List<Object[]> rows = database.getRows( sql, 1 );
      if ( rows != null && rows.size() > 0 ) {
        sql =
          "UPDATE "
            + schemaTable + " SET "
            + repository.quote( KettleDatabaseRepository.FIELD_TRANSFORMATION_ID_DIRECTORY ) + "=0 WHERE "
            + repository.quote( KettleDatabaseRepository.FIELD_TRANSFORMATION_ID_DIRECTORY ) + " IS NULL";
        statements.add( sql );
        if ( !dryrun ) {
          database.execStatement( sql );
        }
      }
    }

    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_TRANS_ATTRIBUTE
    //
    // Create table...
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_TRANS_ATTRIBUTE;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table
      .addValueMeta( new ValueMetaInteger(
        KettleDatabaseRepository.FIELD_TRANS_ATTRIBUTE_ID_TRANS_ATTRIBUTE,
        KEY, 0 ) );
    table
      .addValueMeta( new ValueMetaInteger(
        KettleDatabaseRepository.FIELD_TRANS_ATTRIBUTE_ID_TRANSFORMATION,
        KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANS_ATTRIBUTE_NR, 6, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_TRANS_ATTRIBUTE_CODE,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANS_ATTRIBUTE_VALUE_NUM, 18, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_TRANS_ATTRIBUTE_VALUE_STR,
      KettleDatabaseRepository.REP_STRING_LENGTH, 0 ) );
    sql =
      database.getDDL(
        schemaTable, table, null, false, KettleDatabaseRepository.FIELD_TRANS_ATTRIBUTE_ID_TRANS_ATTRIBUTE,
        false );

    if ( !Utils.isEmpty( sql ) ) {
      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
      try {
        indexname = KettleDatabaseRepositoryBase.IDX_TRANS_ATTRIBUTE_LOOKUP;
        keyfield =
          new String[] {
            KettleDatabaseRepository.FIELD_TRANS_ATTRIBUTE_ID_TRANSFORMATION,
            KettleDatabaseRepository.FIELD_TRANS_ATTRIBUTE_CODE,
            KettleDatabaseRepository.FIELD_TRANS_ATTRIBUTE_NR };

        if ( !database.checkIndexExists( schemaTable, keyfield ) ) {
          sql = database.getCreateIndexStatement( schemaTable, indexname, keyfield, false, true, false, false );
          statements.add( sql );
          if ( !dryrun ) {
            if ( log.isDetailed() ) {
              log.logDetailed( "executing SQL statements: " + Const.CR + sql );
            }
            database.execStatements( sql );
            if ( log.isDetailed() ) {
              log.logDetailed( "Created lookup index " + indexname + " on " + schemaTable );
            }
          }
        }
      } catch ( KettleException kdbe ) {
        // Ignore this one: index is not properly detected, it already exists...
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_JOB_ATTRIBUTE
    //
    // Create table...
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_JOB_ATTRIBUTE;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOB_ATTRIBUTE_ID_JOB_ATTRIBUTE, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOB_ATTRIBUTE_ID_JOB, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOB_ATTRIBUTE_NR, 6, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_JOB_ATTRIBUTE_CODE,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOB_ATTRIBUTE_VALUE_NUM, 18, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_JOB_ATTRIBUTE_VALUE_STR,
      KettleDatabaseRepository.REP_STRING_LENGTH, 0 ) );
    sql =
      database.getDDL(
        schemaTable, table, null, false, KettleDatabaseRepository.FIELD_JOB_ATTRIBUTE_ID_JOB_ATTRIBUTE, false );

    if ( !Utils.isEmpty( sql ) ) {
      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
      try {
        // PDI-10237
        indexname = KettleDatabaseRepositoryBase.IDX_JOB_ATTRIBUTE_LOOKUP;
        keyfield =
          new String[] {
            KettleDatabaseRepository.FIELD_JOB_ATTRIBUTE_ID_JOB,
            KettleDatabaseRepository.FIELD_JOB_ATTRIBUTE_CODE, KettleDatabaseRepository.FIELD_JOB_ATTRIBUTE_NR };

        if ( !database.checkIndexExists( schemaTable, keyfield ) ) {
          sql = database.getCreateIndexStatement( schemaTable, indexname, keyfield, false, true, false, false );
          statements.add( sql );
          if ( !dryrun ) {
            if ( log.isDetailed() ) {
              log.logDetailed( "executing SQL statements: " + Const.CR + sql );
            }
            database.execStatements( sql );
            if ( log.isDetailed() ) {
              log.logDetailed( "Created lookup index " + indexname + " on " + schemaTable );
            }
          }
        }
      } catch ( KettleException kdbe ) {
        // Ignore this one: index is not properly detected, it already exists...
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_DEPENDENCY
    //
    // Create table...
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_DEPENDENCY;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_DEPENDENCY_ID_DEPENDENCY, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_DEPENDENCY_ID_TRANSFORMATION, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_DEPENDENCY_ID_DATABASE, KEY, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_DEPENDENCY_TABLE_NAME,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_DEPENDENCY_FIELD_NAME,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    sql =
      database.getDDL(
        schemaTable, table, null, false, KettleDatabaseRepository.FIELD_DEPENDENCY_ID_DEPENDENCY, false );

    if ( !Utils.isEmpty( sql ) ) {
      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_PARTITION_SCHEMA
    //
    // Create table...
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_PARTITION_SCHEMA;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_PARTITION_SCHEMA_ID_PARTITION_SCHEMA, KEY,
      0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_PARTITION_SCHEMA_NAME,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table
      .addValueMeta( new ValueMetaBoolean(
        KettleDatabaseRepository.FIELD_PARTITION_SCHEMA_DYNAMIC_DEFINITION,
        1, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_PARTITION_SCHEMA_PARTITIONS_PER_SLAVE,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    sql =
      database.getDDL(
        schemaTable, table, null, false, KettleDatabaseRepository.FIELD_PARTITION_SCHEMA_ID_PARTITION_SCHEMA,
        false );

    if ( !Utils.isEmpty( sql ) ) {
      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_PARTITION
    //
    // Create table...
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_PARTITION;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_PARTITION_ID_PARTITION, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_PARTITION_ID_PARTITION_SCHEMA, KEY, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_PARTITION_PARTITION_ID,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    sql =
      database.getDDL(
        schemaTable, table, null, false, KettleDatabaseRepository.FIELD_PARTITION_ID_PARTITION, false );

    if ( !Utils.isEmpty( sql ) ) {
      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_TRANS_PARTITION_SCHEMA
    //
    // Create table...
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_TRANS_PARTITION_SCHEMA;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANS_PARTITION_SCHEMA_ID_TRANS_PARTITION_SCHEMA,
      KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANS_PARTITION_SCHEMA_ID_TRANSFORMATION,
      KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANS_PARTITION_SCHEMA_ID_PARTITION_SCHEMA,
      KEY, 0 ) );
    sql =
      database.getDDL(
        schemaTable, table, null, false,
        KettleDatabaseRepository.FIELD_TRANS_PARTITION_SCHEMA_ID_TRANS_PARTITION_SCHEMA, false );

    if ( !Utils.isEmpty( sql ) ) {
      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_CLUSTER
    //
    // Create table...
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_CLUSTER;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_CLUSTER_ID_CLUSTER, KEY, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_CLUSTER_NAME,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_CLUSTER_BASE_PORT,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_CLUSTER_SOCKETS_BUFFER_SIZE,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_CLUSTER_SOCKETS_FLUSH_INTERVAL,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaBoolean(
      KettleDatabaseRepository.FIELD_CLUSTER_SOCKETS_COMPRESSED, 0, 0 ) );
    table.addValueMeta( new ValueMetaBoolean(
      KettleDatabaseRepository.FIELD_CLUSTER_DYNAMIC, 0, 0 ) );
    sql =
      database
        .getDDL( schemaTable, table, null, false, KettleDatabaseRepository.FIELD_CLUSTER_ID_CLUSTER, false );

    if ( !Utils.isEmpty( sql ) ) {
      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_SLAVE
    //
    // Create table...
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_SLAVE;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_SLAVE_ID_SLAVE, KEY, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_SLAVE_NAME,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_SLAVE_HOST_NAME,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_SLAVE_PORT,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_SLAVE_WEB_APP_NAME,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_SLAVE_USERNAME,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_SLAVE_PASSWORD,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_SLAVE_PROXY_HOST_NAME,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_SLAVE_PROXY_PORT,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_SLAVE_NON_PROXY_HOSTS,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaBoolean(
      KettleDatabaseRepository.FIELD_SLAVE_MASTER ) );
    sql = database.getDDL( schemaTable, table, null, false, KettleDatabaseRepository.FIELD_SLAVE_ID_SLAVE, false );

    if ( !Utils.isEmpty( sql ) ) {
      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_CLUSTER_SLAVE
    //
    // Create table...
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_CLUSTER_SLAVE;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_CLUSTER_SLAVE_ID_CLUSTER_SLAVE, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_CLUSTER_SLAVE_ID_CLUSTER, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_CLUSTER_SLAVE_ID_SLAVE, KEY, 0 ) );
    sql =
      database.getDDL(
        schemaTable, table, null, false, KettleDatabaseRepository.FIELD_CLUSTER_SLAVE_ID_CLUSTER_SLAVE, false );

    if ( !Utils.isEmpty( sql ) ) {
      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_TRANS_SLAVE
    //
    // Create table...
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_TRANS_SLAVE;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANS_SLAVE_ID_TRANS_SLAVE, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANS_SLAVE_ID_TRANSFORMATION, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANS_SLAVE_ID_SLAVE, KEY, 0 ) );
    sql =
      database.getDDL(
        schemaTable, table, null, false, KettleDatabaseRepository.FIELD_TRANS_SLAVE_ID_TRANS_SLAVE, false );

    if ( !Utils.isEmpty( sql ) ) {
      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_TRANS_CLUSTER
    //
    // Create table...
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_TRANS_CLUSTER;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANS_CLUSTER_ID_TRANS_CLUSTER, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANS_CLUSTER_ID_TRANSFORMATION, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANS_CLUSTER_ID_CLUSTER, KEY, 0 ) );
    sql =
      database.getDDL(
        schemaTable, table, null, false, KettleDatabaseRepository.FIELD_TRANS_CLUSTER_ID_TRANS_CLUSTER, false );

    if ( !Utils.isEmpty( sql ) ) {
      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    //
    // R_TRANS_HOP
    //
    // Create table...
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_TRANS_HOP;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANS_HOP_ID_TRANS_HOP, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANS_HOP_ID_TRANSFORMATION, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANS_HOP_ID_STEP_FROM, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANS_HOP_ID_STEP_TO, KEY, 0 ) );
    table.addValueMeta( new ValueMetaBoolean(
      KettleDatabaseRepository.FIELD_TRANS_HOP_ENABLED, 1, 0 ) );
    sql =
      database.getDDL(
        schemaTable, table, null, false, KettleDatabaseRepository.FIELD_TRANS_HOP_ID_TRANS_HOP, false );

    if ( !Utils.isEmpty( sql ) ) {
      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // /////////////////////////////////////////////////////////////////////////////
    // R_TRANS_STEP_CONDITION
    //
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_TRANS_STEP_CONDITION;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANS_STEP_CONDITION_ID_TRANSFORMATION,
      KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANS_STEP_CONDITION_ID_STEP, KEY, 0 ) );
    table
      .addValueMeta( new ValueMetaInteger(
        KettleDatabaseRepository.FIELD_TRANS_STEP_CONDITION_ID_CONDITION,
        KEY, 0 ) );
    sql = database.getDDL( schemaTable, table, null, false, null, false );

    if ( !Utils.isEmpty( sql ) ) {
      // Doesn't exists: create the table...

      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // /////////////////////////////////////////////////////////////////////////////
    // R_CONDITION
    //
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_CONDITION;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_CONDITION_ID_CONDITION, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_CONDITION_ID_CONDITION_PARENT, KEY, 0 ) );
    table.addValueMeta( new ValueMetaBoolean(
      KettleDatabaseRepository.FIELD_CONDITION_NEGATED, 1, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_CONDITION_OPERATOR,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_CONDITION_LEFT_NAME,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_CONDITION_CONDITION_FUNCTION,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_CONDITION_RIGHT_NAME,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_CONDITION_ID_VALUE_RIGHT, KEY, 0 ) );
    sql =
      database.getDDL(
        schemaTable, table, null, false, KettleDatabaseRepository.FIELD_CONDITION_ID_CONDITION, false );

    if ( !Utils.isEmpty( sql ) ) {
      // Doesn't exist: create the table...

      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // /////////////////////////////////////////////////////////////////////////////
    // R_VALUE
    //
    tablename = KettleDatabaseRepository.TABLE_R_VALUE;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table = new RowMeta();
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_VALUE_ID_VALUE, KEY, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_VALUE_NAME,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_VALUE_VALUE_TYPE,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_VALUE_VALUE_STR,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaBoolean(
      KettleDatabaseRepository.FIELD_VALUE_IS_NULL, 1, 0 ) );
    sql = database.getDDL( schemaTable, table, null, false, KettleDatabaseRepository.FIELD_VALUE_ID_VALUE, false );

    if ( !Utils.isEmpty( sql ) ) {
      // Doesn't exists: create the table...
      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_STEP_TYPE
    //
    // Create table...
    boolean ok_step_type = true;
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_STEP_TYPE;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_STEP_TYPE_ID_STEP_TYPE, KEY, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_STEP_TYPE_CODE,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_STEP_TYPE_DESCRIPTION,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_STEP_TYPE_HELPTEXT,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    sql = database.getDDL( schemaTable, table, null, false, "ID_STEP_TYPE", false );
    create = false;
    if ( !Utils.isEmpty( sql ) ) {
      // Doesn't exists: create the table...

      create = sql.toUpperCase().indexOf( "CREATE TABLE" ) >= 0;
      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }

    if ( ok_step_type ) {
      updateStepTypes( statements, dryrun, create );
      if ( log.isDetailed() ) {
        log.logDetailed( "Populated table " + schemaTable );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_STEP
    //
    // Create table
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_STEP;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_STEP_ID_STEP, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_STEP_ID_TRANSFORMATION, KEY, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_STEP_NAME,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_STEP_DESCRIPTION,
      KettleDatabaseRepository.REP_STRING_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_STEP_ID_STEP_TYPE, KEY, 0 ) );
    table.addValueMeta( new ValueMetaBoolean(
      KettleDatabaseRepository.FIELD_STEP_DISTRIBUTE, 1, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_STEP_COPIES, 3, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_STEP_GUI_LOCATION_X, 6, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_STEP_GUI_LOCATION_Y, 6, 0 ) );
    table.addValueMeta( new ValueMetaBoolean(
      KettleDatabaseRepository.FIELD_STEP_GUI_DRAW, 1, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_STEP_COPIES_STRING,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    sql = database.getDDL( schemaTable, table, null, false, KettleDatabaseRepository.FIELD_STEP_ID_STEP, false );

    if ( !Utils.isEmpty( sql ) ) {
      // Doesn't exists: create the table...

      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_STEP_ATTRIBUTE
    //
    // Create table...
    tablename = KettleDatabaseRepository.TABLE_R_STEP_ATTRIBUTE;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table = new RowMeta();
    table
      .addValueMeta( new ValueMetaInteger(
        KettleDatabaseRepository.FIELD_STEP_ATTRIBUTE_ID_STEP_ATTRIBUTE, KEY,
        0 ) );
    table
      .addValueMeta( new ValueMetaInteger(
        KettleDatabaseRepository.FIELD_STEP_ATTRIBUTE_ID_TRANSFORMATION, KEY,
        0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_STEP_ATTRIBUTE_ID_STEP, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_STEP_ATTRIBUTE_NR, 6, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_STEP_ATTRIBUTE_CODE,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_STEP_ATTRIBUTE_VALUE_NUM, 18, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_STEP_ATTRIBUTE_VALUE_STR,
      KettleDatabaseRepository.REP_STRING_LENGTH, 0 ) );
    sql =
      database.getDDL(
        schemaTable, table, null, false, KettleDatabaseRepository.FIELD_STEP_ATTRIBUTE_ID_STEP_ATTRIBUTE,
        false );

    if ( !Utils.isEmpty( sql ) ) {
      // Doesn't exist: create the table...

      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }

      try {
        indexname = KettleDatabaseRepositoryBase.IDX_R_STEP_ATTRIBUTE;
        keyfield =
          new String[] {
            KettleDatabaseRepository.FIELD_STEP_ATTRIBUTE_ID_STEP,
            KettleDatabaseRepository.FIELD_STEP_ATTRIBUTE_CODE,
            KettleDatabaseRepository.FIELD_STEP_ATTRIBUTE_NR, };
        if ( !database.checkIndexExists( schemaTable, keyfield ) ) {
          sql = database.getCreateIndexStatement( schemaTable, indexname, keyfield, false, true, false, false );
          statements.add( sql );
          if ( !dryrun ) {
            if ( log.isDetailed() ) {
              log.logDetailed( "executing SQL statements: " + Const.CR + sql );
            }
            database.execStatements( sql );
            if ( log.isDetailed() ) {
              log.logDetailed( "Created lookup index " + indexname + " on " + schemaTable );
            }
          }
        }
      } catch ( KettleException kdbe ) {
        // Ignore this one: index is not properly detected, it already exists...
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_STEP_DATABASE
    //
    // Keeps the links between transformation steps and databases.
    // That way investigating dependencies becomes easier to program.
    //
    // Create table...
    tablename = KettleDatabaseRepository.TABLE_R_STEP_DATABASE;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table = new RowMeta();
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_STEP_DATABASE_ID_TRANSFORMATION, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_STEP_DATABASE_ID_STEP, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_STEP_DATABASE_ID_DATABASE, KEY, 0 ) );
    sql = database.getDDL( schemaTable, table, null, false, null, false );

    if ( !Utils.isEmpty( sql ) ) {
      // Doesn't exist: create the table...

      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }

      try {
        indexname = KettleDatabaseRepositoryBase.R_STEP_DATABASE_LU1;
        keyfield = new String[] { KettleDatabaseRepository.FIELD_STEP_DATABASE_ID_TRANSFORMATION, };
        if ( !database.checkIndexExists( schemaTable, keyfield ) ) {
          sql = database.getCreateIndexStatement( schemaTable, indexname, keyfield, false, false, false, false );
          statements.add( sql );
          if ( !dryrun ) {
            if ( log.isDetailed() ) {
              log.logDetailed( "executing SQL statements: " + Const.CR + sql );
            }
            database.execStatements( sql );
            if ( log.isDetailed() ) {
              log.logDetailed( "Created lookup index " + indexname + " on " + schemaTable );
            }
          }
        }
      } catch ( KettleException kdbe ) {
        // Ignore this one: index is not properly detected, it already exists...
      }

      try {
        indexname = KettleDatabaseRepositoryBase.R_STEP_DATABASE_LU2;
        keyfield = new String[] { KettleDatabaseRepository.FIELD_STEP_DATABASE_ID_DATABASE, };
        if ( !database.checkIndexExists( schemaTable, keyfield ) ) {
          sql = database.getCreateIndexStatement( schemaTable, indexname, keyfield, false, false, false, false );
          statements.add( sql );
          if ( !dryrun ) {
            if ( log.isDetailed() ) {
              log.logDetailed( "executing SQL statements: " + Const.CR + sql );
            }
            database.execStatements( sql );
            if ( log.isDetailed() ) {
              log.logDetailed( "Created lookup index " + indexname + " on " + schemaTable );
            }
          }
        }
      } catch ( KettleException kdbe ) {
        // Ignore this one: index is not properly detected, it already exists...
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_TRANS_NOTE
    //
    // Create table...
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_TRANS_NOTE;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANS_NOTE_ID_TRANSFORMATION, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANS_NOTE_ID_NOTE, KEY, 0 ) );
    sql = database.getDDL( schemaTable, table, null, false, null, false );

    if ( !Utils.isEmpty( sql ) ) {
      // Doesn't exist: create the table...

      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_LOGLEVEL
    //
    // Create table...
    boolean ok_loglevel = true;
    tablename = KettleDatabaseRepository.TABLE_R_LOGLEVEL;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table = new RowMeta();
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_LOGLEVEL_ID_LOGLEVEL, KEY, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_LOGLEVEL_CODE,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_LOGLEVEL_DESCRIPTION,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    sql =
      database.getDDL(
        schemaTable, table, null, false, KettleDatabaseRepository.FIELD_LOGLEVEL_ID_LOGLEVEL, false );

    create = false;
    if ( !Utils.isEmpty( sql ) ) {
      // Doesn't exist: create the table...

      create = sql.toUpperCase().indexOf( "CREATE TABLE" ) >= 0;
      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }

    if ( ok_loglevel ) {
      //
      // Populate with data...
      //
      code = LogLevel.logLogLevelCodes();
      desc = LogLevel.getLogLevelDescriptions();

      if ( !dryrun ) {
        database.prepareInsert( table, null, tablename );
      }

      for ( int i = 1; i < code.length; i++ ) {
        RowMetaAndData lookup = null;
        if ( upgrade ) {
          lookup =
            database.getOneRow( "SELECT "
              + repository.quote( KettleDatabaseRepository.FIELD_LOGLEVEL_ID_LOGLEVEL ) + " FROM "
              + schemaTable + " WHERE " + database.getDatabaseMeta().quoteField( "CODE" ) + " = '" + code[i]
              + "'" );
        }
        if ( lookup == null ) {
          ObjectId nextid = new LongObjectId( i );
          if ( !create ) {
            nextid = repository.connectionDelegate.getNextLoglevelID();
          }

          RowMetaAndData tableData = new RowMetaAndData();
          tableData.addValue( new ValueMetaInteger(
            KettleDatabaseRepository.FIELD_LOGLEVEL_ID_LOGLEVEL ), nextid );
          tableData.addValue( new ValueMetaString(
            KettleDatabaseRepository.FIELD_LOGLEVEL_CODE ), code[i] );
          tableData.addValue( new ValueMetaString(
            KettleDatabaseRepository.FIELD_LOGLEVEL_DESCRIPTION ), desc[i] );

          if ( dryrun ) {
            sql = database.getSQLOutput( null, tablename, tableData.getRowMeta(), tableData.getData(), null );
            statements.add( sql );
          } else {
            database.setValuesInsert( tableData.getRowMeta(), tableData.getData() );
            database.insertRow();
          }
        }
      }

      try {
        if ( !dryrun ) {
          database.closeInsert();
        }
        if ( log.isDetailed() ) {
          log.logDetailed( "Populated table " + schemaTable );
        }
      } catch ( KettleException dbe ) {
        throw new KettleException( "Unable to close insert after populating table " + schemaTable, dbe );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_LOG
    //
    // Create table...
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_LOG;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_LOG_ID_LOG, KEY, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_LOG_NAME,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_LOG_ID_LOGLEVEL, KEY, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_LOG_LOGTYPE,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_LOG_FILENAME,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_LOG_FILEEXTENTION,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaBoolean(
      KettleDatabaseRepository.FIELD_LOG_ADD_DATE, 1, 0 ) );
    table.addValueMeta( new ValueMetaBoolean(
      KettleDatabaseRepository.FIELD_LOG_ADD_TIME, 1, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_LOG_ID_DATABASE_LOG, KEY, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_LOG_TABLE_NAME_LOG,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    sql = database.getDDL( schemaTable, table, null, false, KettleDatabaseRepository.FIELD_LOG_ID_LOG, false );

    if ( !Utils.isEmpty( sql ) ) {
      // Doesn't exist: create the table...

      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_JOB
    //
    // Create table...
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_JOB;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOB_ID_JOB, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOB_ID_DIRECTORY, KEY, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_JOB_NAME,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_JOB_DESCRIPTION,
      KettleDatabaseRepository.REP_STRING_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_JOB_EXTENDED_DESCRIPTION,
      KettleDatabaseRepository.REP_STRING_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_JOB_JOB_VERSION,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOB_JOB_STATUS, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOB_ID_DATABASE_LOG, KEY, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_JOB_TABLE_NAME_LOG,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_JOB_CREATED_USER,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaDate(
      KettleDatabaseRepository.FIELD_JOB_CREATED_DATE, 20, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_JOB_MODIFIED_USER,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaDate(
      KettleDatabaseRepository.FIELD_JOB_MODIFIED_DATE, 20, 0 ) );
    table.addValueMeta( new ValueMetaBoolean(
      KettleDatabaseRepository.FIELD_JOB_USE_BATCH_ID, 0, 0 ) );
    table.addValueMeta( new ValueMetaBoolean(
      KettleDatabaseRepository.FIELD_JOB_PASS_BATCH_ID, 0, 0 ) );
    table.addValueMeta( new ValueMetaBoolean(
      KettleDatabaseRepository.FIELD_JOB_USE_LOGFIELD, 0, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_JOB_SHARED_FILE,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) ); // 255 max length for now.

    sql = database.getDDL( schemaTable, table, null, false, KettleDatabaseRepository.FIELD_JOB_ID_JOB, false );
    if ( !Utils.isEmpty( sql ) ) {
      // Doesn't exist: create the table...

      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_JOBENTRY_DATABASE
    //
    // Keeps the links between job entries and databases.
    // That way investigating dependencies becomes easier to program.
    //
    // Create table...
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_JOBENTRY_DATABASE;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOBENTRY_DATABASE_ID_JOB, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOBENTRY_DATABASE_ID_JOBENTRY, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOBENTRY_DATABASE_ID_DATABASE, KEY, 0 ) );
    sql = database.getDDL( schemaTable, table, null, false, KettleDatabaseRepository.FIELD_JOB_ID_JOB, false );
    sql = database.getDDL( schemaTable, table, null, false, null, false );

    if ( !Utils.isEmpty( sql ) ) {
      // Doesn't exist: create the table...

      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }

      try {
        indexname = KettleDatabaseRepositoryBase.R_JOBENTRY_DATABASE_LU1;
        keyfield = new String[] { KettleDatabaseRepository.FIELD_JOBENTRY_DATABASE_ID_JOB, };
        if ( !database.checkIndexExists( schemaTable, keyfield ) ) {
          sql = database.getCreateIndexStatement( schemaTable, indexname, keyfield, false, false, false, false );
          statements.add( sql );
          if ( !dryrun ) {
            if ( log.isDetailed() ) {
              log.logDetailed( "executing SQL statements: " + Const.CR + sql );
            }
            database.execStatements( sql );
            if ( log.isDetailed() ) {
              log.logDetailed( "Created lookup index " + indexname + " on " + schemaTable );
            }
          }
        }
      } catch ( KettleException kdbe ) {
        // Ignore this one: index is not properly detected, it already exists...
      }

      try {
        indexname = KettleDatabaseRepositoryBase.R_JOBENTRY_DATABASE_LU2;
        keyfield = new String[] { KettleDatabaseRepository.FIELD_JOBENTRY_DATABASE_ID_DATABASE, };
        if ( !database.checkIndexExists( schemaTable, keyfield ) ) {
          sql = database.getCreateIndexStatement( schemaTable, indexname, keyfield, false, false, false, false );
          statements.add( sql );
          if ( !dryrun ) {
            if ( log.isDetailed() ) {
              log.logDetailed( "executing SQL statements: " + Const.CR + sql );
            }
            database.execStatements( sql );
            if ( log.isDetailed() ) {
              log.logDetailed( "Created lookup index " + indexname + " on " + schemaTable );
            }
          }
        }
      } catch ( KettleException kdbe ) {
        // Ignore this one: index is not properly detected, it already exists...
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_JOBENTRY_TYPE
    //
    // Create table...
    boolean ok_jobentry_type = true;
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_JOBENTRY_TYPE;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOBENTRY_TYPE_ID_JOBENTRY_TYPE, KEY, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_JOBENTRY_TYPE_CODE,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_JOBENTRY_TYPE_DESCRIPTION,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    sql =
      database.getDDL(
        schemaTable, table, null, false, KettleDatabaseRepository.FIELD_JOBENTRY_TYPE_ID_JOBENTRY_TYPE, false );

    create = false;
    if ( !Utils.isEmpty( sql ) ) {
      // Doesn't exist: create the table...

      create = sql.toUpperCase().indexOf( "CREATE TABLE" ) >= 0;
      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }

    if ( ok_jobentry_type ) {
      //
      // Populate with data...
      //
      updateJobEntryTypes( statements, dryrun, create );
      if ( log.isDetailed() ) {
        log.logDetailed( "Populated table " + schemaTable );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_JOBENTRY
    //
    // Create table...
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_JOBENTRY;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOBENTRY_ID_JOBENTRY, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOBENTRY_ID_JOB, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOBENTRY_ID_JOBENTRY_TYPE, KEY, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_JOBENTRY_NAME,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_JOBENTRY_DESCRIPTION,
      KettleDatabaseRepository.REP_STRING_LENGTH, 0 ) );
    sql =
      database.getDDL(
        schemaTable, table, null, false, KettleDatabaseRepository.FIELD_JOBENTRY_ID_JOBENTRY, false );

    if ( !Utils.isEmpty( sql ) ) {
      // Doesn't exist: create the table...

      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_JOBENTRY_COPY
    //
    // Create table...
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_JOBENTRY_COPY;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOBENTRY_COPY_ID_JOBENTRY_COPY, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOBENTRY_COPY_ID_JOBENTRY, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOBENTRY_COPY_ID_JOB, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOBENTRY_COPY_ID_JOBENTRY_TYPE, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOBENTRY_COPY_NR, 4, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOBENTRY_COPY_GUI_LOCATION_X, 6, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOBENTRY_COPY_GUI_LOCATION_Y, 6, 0 ) );
    table.addValueMeta( new ValueMetaBoolean(
      KettleDatabaseRepository.FIELD_JOBENTRY_COPY_GUI_DRAW, 1, 0 ) );
    table.addValueMeta( new ValueMetaBoolean(
      KettleDatabaseRepository.FIELD_JOBENTRY_COPY_PARALLEL, 1, 0 ) );
    sql =
      database.getDDL(
        schemaTable, table, null, false, KettleDatabaseRepository.FIELD_JOBENTRY_COPY_ID_JOBENTRY_COPY, false );

    if ( !Utils.isEmpty( sql ) ) {
      // Doesn't exist: create the table...

      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_JOBENTRY_ATTRIBUTE
    //
    // Create table...
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_JOBENTRY_ATTRIBUTE;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOBENTRY_ATTRIBUTE_ID_JOBENTRY_ATTRIBUTE,
      KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOBENTRY_ATTRIBUTE_ID_JOB, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOBENTRY_ATTRIBUTE_ID_JOBENTRY, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOBENTRY_ATTRIBUTE_NR, 6, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_JOBENTRY_ATTRIBUTE_CODE,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaNumber(
      KettleDatabaseRepository.FIELD_JOBENTRY_ATTRIBUTE_VALUE_NUM, 13, 2 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_JOBENTRY_ATTRIBUTE_VALUE_STR,
      KettleDatabaseRepository.REP_STRING_LENGTH, 0 ) );
    sql =
      database.getDDL(
        schemaTable, table, null, false,
        KettleDatabaseRepository.FIELD_JOBENTRY_ATTRIBUTE_ID_JOBENTRY_ATTRIBUTE, false );

    if ( !Utils.isEmpty( sql ) ) {
      // Doesn't exist: create the table...

      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }

      try {
        indexname = KettleDatabaseRepositoryBase.R_JOBENTRY_ATTRIBUTE;
        keyfield =
          new String[] {
            KettleDatabaseRepository.FIELD_JOBENTRY_ATTRIBUTE_ID_JOBENTRY_ATTRIBUTE,
            KettleDatabaseRepository.FIELD_JOBENTRY_ATTRIBUTE_CODE,
            KettleDatabaseRepository.FIELD_JOBENTRY_ATTRIBUTE_NR, };

        if ( !database.checkIndexExists( schemaTable, keyfield ) ) {
          sql = database.getCreateIndexStatement( schemaTable, indexname, keyfield, false, true, false, false );
          statements.add( sql );
          if ( !dryrun ) {
            if ( log.isDetailed() ) {
              log.logDetailed( "executing SQL statements: " + Const.CR + sql );
            }
            database.execStatements( sql );
            if ( log.isDetailed() ) {
              log.logDetailed( "Created lookup index " + indexname + " on " + schemaTable );
            }
          }
        }
      } catch ( KettleException kdbe ) {
        // Ignore this one: index is not properly detected, it already exists...
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_JOB_HOP
    //
    // Create table...
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_JOB_HOP;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOB_HOP_ID_JOB_HOP, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOB_HOP_ID_JOB, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOB_HOP_ID_JOBENTRY_COPY_FROM, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOB_HOP_ID_JOBENTRY_COPY_TO, KEY, 0 ) );
    table.addValueMeta( new ValueMetaBoolean(
      KettleDatabaseRepository.FIELD_JOB_HOP_ENABLED, 1, 0 ) );
    table.addValueMeta( new ValueMetaBoolean(
      KettleDatabaseRepository.FIELD_JOB_HOP_EVALUATION, 1, 0 ) );
    table.addValueMeta( new ValueMetaBoolean(
      KettleDatabaseRepository.FIELD_JOB_HOP_UNCONDITIONAL, 1, 0 ) );
    sql =
      database
        .getDDL( schemaTable, table, null, false, KettleDatabaseRepository.FIELD_JOB_HOP_ID_JOB_HOP, false );

    if ( !Utils.isEmpty( sql ) ) {
      // Doesn't exist: create the table...

      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_JOB_NOTE
    //
    // Create table...
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_JOB_NOTE;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOB_NOTE_ID_JOB, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOB_NOTE_ID_NOTE, KEY, 0 ) );
    sql = database.getDDL( schemaTable, table, null, false, null, false );

    if ( !Utils.isEmpty( sql ) ) {
      // Doesn't exist: create the table...

      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    //
    // R_TRANS_LOCK
    //
    // Create table...
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_TRANS_LOCK;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANS_LOCK_ID_TRANS_LOCK, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANS_LOCK_ID_TRANSFORMATION, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_TRANS_LOCK_ID_USER, KEY, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_TRANS_LOCK_LOCK_MESSAGE,
      KettleDatabaseRepository.REP_STRING_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaDate(
      KettleDatabaseRepository.FIELD_TRANS_LOCK_LOCK_DATE, 0, 0 ) );
    sql =
      database.getDDL(
        schemaTable, table, null, false, KettleDatabaseRepository.FIELD_TRANS_LOCK_ID_TRANS_LOCK, false );

    if ( !Utils.isEmpty( sql ) ) {
      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    //
    // R_JOB_LOCK
    //
    // Create table...
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_JOB_LOCK;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOB_LOCK_ID_JOB_LOCK, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOB_LOCK_ID_JOB, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOB_LOCK_ID_USER, KEY, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_JOB_LOCK_LOCK_MESSAGE,
      KettleDatabaseRepository.REP_STRING_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaDate(
      KettleDatabaseRepository.FIELD_JOB_LOCK_LOCK_DATE, 0, 0 ) );
    sql =
      database.getDDL(
        schemaTable, table, null, false, KettleDatabaseRepository.FIELD_JOB_LOCK_ID_JOB_LOCK, false );

    if ( !Utils.isEmpty( sql ) ) {
      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // /////////////////////////////////////////////////////////////////////////////////
    //
    // MetaStore tables...
    //
    // /////////////////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_NAMESPACE
    //
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_NAMESPACE;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_NAMESPACE_ID_NAMESPACE, KEY, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_NAMESPACE_NAME,
      ( database.getDatabaseMeta().getDatabaseInterface().getMaxVARCHARLength() - 1 > 0
        ? database.getDatabaseMeta().getDatabaseInterface().getMaxVARCHARLength() - 1
        : KettleDatabaseRepository.REP_ORACLE_STRING_LENGTH ), 0 ) );

    sql =
      database.getDDL(
        schemaTable, table, null, false, KettleDatabaseRepository.FIELD_NAMESPACE_ID_NAMESPACE, false );

    if ( !Utils.isEmpty( sql ) ) {
      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_ELEMENT_TYPE
    //
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_ELEMENT_TYPE;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_ELEMENT_TYPE_ID_ELEMENT_TYPE, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_ELEMENT_TYPE_ID_NAMESPACE, KEY, 0 ) );
    table.addValueMeta( new ValueMetaString( KettleDatabaseRepository.FIELD_ELEMENT_TYPE_NAME,
      getRepoStringLength(), 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_ELEMENT_TYPE_DESCRIPTION,
      KettleDatabaseRepository.REP_STRING_LENGTH, 0 ) );
    sql =
      database.getDDL(
        schemaTable, table, null, false, KettleDatabaseRepository.FIELD_ELEMENT_TYPE_ID_ELEMENT_TYPE, false );

    if ( !Utils.isEmpty( sql ) ) {
      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_ELEMENT
    //
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_ELEMENT;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_ELEMENT_ID_ELEMENT, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_ELEMENT_ID_ELEMENT_TYPE, KEY, 0 ) );
    table.addValueMeta( new ValueMetaString( KettleDatabaseRepository.FIELD_ELEMENT_NAME,
      getRepoStringLength(), 0 ) );
    sql =
      database
        .getDDL( schemaTable, table, null, false, KettleDatabaseRepository.FIELD_ELEMENT_ID_ELEMENT, false );

    if ( !Utils.isEmpty( sql ) ) {
      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_ELEMENT_ATTRIBUTE
    //
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_ELEMENT_ATTRIBUTE;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }

    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_ELEMENT_ATTRIBUTE_ID_ELEMENT_ATTRIBUTE,
      KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_ELEMENT_ATTRIBUTE_ID_ELEMENT, KEY, 0 ) );
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_ELEMENT_ATTRIBUTE_ID_ELEMENT_ATTRIBUTE_PARENT,
      KEY, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_ELEMENT_ATTRIBUTE_KEY,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_ELEMENT_ATTRIBUTE_VALUE,
      KettleDatabaseRepository.REP_STRING_LENGTH, 0 ) );
    sql =
      database.getDDL(
        schemaTable, table, null, false,
        KettleDatabaseRepository.FIELD_ELEMENT_ATTRIBUTE_ID_ELEMENT_ATTRIBUTE, false );

    if ( !Utils.isEmpty( sql ) ) {
      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }

    // /////////////////////////////////////////////////////////////////////////////////
    //
    // User tables...
    //
    // /////////////////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////////////////
    //
    // R_USER
    //

    // Keep a mapping between the user login and the object id
    //
    Map<String, ObjectId> users = new Hashtable<String, ObjectId>();

    // Create table...
    //
    boolean ok_user = true;
    table = new RowMeta();
    tablename = KettleDatabaseRepository.TABLE_R_USER;
    schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
    if ( monitor != null ) {
      monitor.subTask( "Checking table " + schemaTable );
    }
    table.addValueMeta( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_USER_ID_USER, KEY, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_USER_LOGIN,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_USER_PASSWORD,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_USER_NAME,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaString(
      KettleDatabaseRepository.FIELD_USER_DESCRIPTION,
      KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0 ) );
    table.addValueMeta( new ValueMetaBoolean(
      KettleDatabaseRepository.FIELD_USER_ENABLED, 1, 0 ) );
    sql = database.getDDL( schemaTable, table, null, false, KettleDatabaseRepository.FIELD_USER_ID_USER, false );

    create = false;
    if ( !Utils.isEmpty( sql ) ) {
      // Doesn't exist: create the table...

      create = sql.toUpperCase().indexOf( "CREATE TABLE" ) >= 0;
      statements.add( sql );
      if ( !dryrun ) {
        if ( log.isDetailed() ) {
          log.logDetailed( "executing SQL statements: " + Const.CR + sql );
        }
        database.execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Created or altered table " + schemaTable );
        }
      }
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "Table " + schemaTable + " is OK." );
      }
    }

    if ( ok_user ) {
      //
      // Populate with data...
      //
      user = new String[] { "admin", "guest" };
      pass = new String[] { "admin", "guest" };
      code = new String[] { "Administrator", "Guest account" };
      desc = new String[] { "User manager", "Read-only guest account" };
      // prof = new String[] { "Administrator", "Read-only" };

      if ( !dryrun ) {
        database.prepareInsert( table, null, tablename );
      }

      for ( int i = 0; i < user.length; i++ ) {
        RowMetaAndData lookup = null;
        if ( upgrade ) {
          lookup =
            database.getOneRow( "SELECT "
              + repository.quote( KettleDatabaseRepository.FIELD_USER_ID_USER ) + " FROM " + schemaTable
              + " WHERE " + repository.quote( KettleDatabaseRepository.FIELD_USER_LOGIN ) + " = '" + user[i]
              + "'" );
        }
        if ( lookup == null ) {
          ObjectId nextid = new LongObjectId( i + 1 );
          if ( !create ) {
            nextid = repository.connectionDelegate.getNextUserID();
          }
          String password = Encr.encryptPassword( pass[i] );

          RowMetaAndData tableData = new RowMetaAndData();
          tableData.addValue( new ValueMetaInteger(
            KettleDatabaseRepository.FIELD_USER_ID_USER ), nextid );
          tableData.addValue( new ValueMetaString(
            KettleDatabaseRepository.FIELD_USER_LOGIN ), user[i] );
          tableData.addValue( new ValueMetaString(
            KettleDatabaseRepository.FIELD_USER_PASSWORD ), password );
          tableData.addValue( new ValueMetaString(
            KettleDatabaseRepository.FIELD_USER_NAME ), code[i] );
          tableData.addValue( new ValueMetaString(
            KettleDatabaseRepository.FIELD_USER_DESCRIPTION ), desc[i] );
          tableData.addValue( new ValueMetaBoolean(
            KettleDatabaseRepository.FIELD_USER_ENABLED ), Boolean.TRUE );

          if ( dryrun ) {
            sql = database.getSQLOutput( null, tablename, tableData.getRowMeta(), tableData.getData(), null );
            statements.add( sql );
          } else {
            database.setValuesInsert( tableData );
            database.insertRow();
          }
          users.put( user[i], nextid );
        }
      }

      try {
        if ( !dryrun ) {
          database.closeInsert();
        }
        if ( log.isDetailed() ) {
          log.logDetailed( "Populated table " + schemaTable );
        }
      } catch ( KettleException dbe ) {
        throw new KettleException( "Unable to close insert after populating table " + schemaTable, dbe );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }
    if ( monitor != null ) {
      monitor.done();
    }

    log.logBasic( ( upgrade ? "Upgraded" : "Created" )
      + " " + KettleDatabaseRepository.repositoryTableNames.length + " repository tables." );

  }

  /**
   * Returns max VARCHAR length depending on db interface
   */
  protected int getRepoStringLength() {
    return database.getDatabaseMeta().getDatabaseInterface().getMaxVARCHARLength() - 1 > 0 ? database.getDatabaseMeta()
        .getDatabaseInterface().getMaxVARCHARLength() - 1 : KettleDatabaseRepository.REP_ORACLE_STRING_LENGTH;
  }

  /**
   * Update the list in R_STEP_TYPE using the StepLoader StepPlugin entries
   *
   * @throws KettleException
   *           if the update didn't go as planned.
   */
  public List<String> updateStepTypes( List<String> statements, boolean dryrun, boolean create ) throws KettleException {
    synchronized ( repository ) {

      // We should only do an update if something has changed...
      //
      List<PluginInterface> plugins = pluginRegistry.getPlugins( StepPluginType.class );
      ObjectId[] ids = loadPluginsIds( plugins, create );

      for ( int i = 0, idsLength = ids.length; i < idsLength; i++ ) {
        ObjectId id = ids[ i ];
        if ( id == null ) {
          // Not found, we need to add this one...

          if ( !create ) {
            id = repository.connectionDelegate.getNextStepTypeID();
          } else {
            id = new LongObjectId( i + 1 );
          }

          PluginInterface sp = plugins.get( i );

          RowMetaAndData table = new RowMetaAndData();
          table.addValue( new ValueMetaInteger(
            KettleDatabaseRepository.FIELD_STEP_TYPE_ID_STEP_TYPE ), id );
          table.addValue( new ValueMetaString(
            KettleDatabaseRepository.FIELD_STEP_TYPE_CODE ), sp.getIds()[0] );
          table.addValue( new ValueMetaString(
            KettleDatabaseRepository.FIELD_STEP_TYPE_DESCRIPTION ), sp.getName() );
          table.addValue( new ValueMetaString(
            KettleDatabaseRepository.FIELD_STEP_TYPE_HELPTEXT ), sp.getDescription() );

          if ( dryrun ) {
            String sql =
              database.getSQLOutput( null, KettleDatabaseRepository.TABLE_R_STEP_TYPE, table.getRowMeta(), table
                .getData(), null );
            statements.add( sql );
          } else {
            database.prepareInsert( table.getRowMeta(), null, KettleDatabaseRepository.TABLE_R_STEP_TYPE );
            database.setValuesInsert( table );
            database.insertRow();
            database.closeInsert();
          }
        }
      }
    }
    return statements;
  }

  private ObjectId[] loadPluginsIds( List<PluginInterface> plugins, boolean create ) throws KettleException {
    ObjectId[] ids = new ObjectId[ plugins.size() ];
    if ( create ) {
      return ids;
    }

    Map<String, LongObjectId> stepTypeCodeToIdMap = repository.stepDelegate.getStepTypeCodeToIdMap();
    int index = 0;
    for ( PluginInterface sp : plugins ) {
      ids[index++] = stepTypeCodeToIdMap.get( sp.getIds()[0] );
    }

    return ids;
  }

  /**
   * Update the list in R_DATABASE_TYPE using the database plugin entries
   *
   * @throws KettleException
   *           if the update didn't go as planned.
   */
  public List<String> updateDatabaseTypes( List<String> statements, boolean dryrun, boolean create ) throws KettleException {
    synchronized ( repository ) {

      // We should only do an update if something has changed...
      //
      List<PluginInterface> plugins = pluginRegistry.getPlugins( DatabasePluginType.class );
      for ( int i = 0; i < plugins.size(); i++ ) {
        PluginInterface plugin = plugins.get( i );
        ObjectId id = null;
        if ( !create ) {
          id = repository.databaseDelegate.getDatabaseTypeID( plugin.getIds()[0] );
        }
        if ( id == null ) {
          // Not found, we need to add this one...

          // We need to add this one ...
          id = new LongObjectId( i + 1 );
          if ( !create ) {
            id = repository.connectionDelegate.getNextDatabaseTypeID();
          }

          RowMetaAndData table = new RowMetaAndData();
          table.addValue( new ValueMetaInteger(
            KettleDatabaseRepository.FIELD_DATABASE_TYPE_ID_DATABASE_TYPE ), id );
          table.addValue( new ValueMetaString( KettleDatabaseRepository.FIELD_DATABASE_TYPE_CODE ),
            plugin.getIds()[0] );
          table.addValue( new ValueMetaString(
            KettleDatabaseRepository.FIELD_DATABASE_TYPE_DESCRIPTION ), plugin.getName() );

          if ( dryrun ) {
            String sql =
              database.getSQLOutput(
                null, KettleDatabaseRepository.TABLE_R_DATABASE_TYPE, table.getRowMeta(), table.getData(),
                null );
            statements.add( sql );
          } else {
            database.prepareInsert( table.getRowMeta(), null, KettleDatabaseRepository.TABLE_R_DATABASE_TYPE );
            database.setValuesInsert( table );
            database.insertRow();
            database.closeInsert();
          }
        }
      }
    }
    return statements;
  }

  /**
   * Update the list in R_JOBENTRY_TYPE
   *
   * @param create
   *
   * @exception KettleException
   *              if something went wrong during the update.
   */
  public void updateJobEntryTypes( List<String> statements, boolean dryrun, boolean create ) throws KettleException {
    synchronized ( repository ) {

      // We should only do an update if something has changed...
      PluginRegistry registry = PluginRegistry.getInstance();
      List<PluginInterface> jobPlugins = registry.getPlugins( JobEntryPluginType.class );

      for ( int i = 0; i < jobPlugins.size(); i++ ) {
        PluginInterface jobPlugin = jobPlugins.get( i );
        String type_desc = jobPlugin.getIds()[0];
        String type_desc_long = jobPlugin.getName();
        ObjectId id = null;
        if ( !create ) {
          id = repository.jobEntryDelegate.getJobEntryTypeID( type_desc );
        }
        if ( id == null ) {
          // Not found, we need to add this one...

          // We need to add this one ...
          id = new LongObjectId( i + 1 );
          if ( !create ) {
            id = repository.connectionDelegate.getNextJobEntryTypeID();
          }

          RowMetaAndData table = new RowMetaAndData();
          table.addValue( new ValueMetaInteger(
            KettleDatabaseRepository.FIELD_JOBENTRY_TYPE_ID_JOBENTRY_TYPE ), id );
          table.addValue( new ValueMetaString(
            KettleDatabaseRepository.FIELD_JOBENTRY_TYPE_CODE ), type_desc );
          table.addValue( new ValueMetaString(
            KettleDatabaseRepository.FIELD_JOBENTRY_TYPE_DESCRIPTION ), type_desc_long );

          if ( dryrun ) {
            String sql =
              database.getSQLOutput(
                null, KettleDatabaseRepository.TABLE_R_JOBENTRY_TYPE, table.getRowMeta(), table.getData(),
                null );
            statements.add( sql );
          } else {
            database.prepareInsert( table.getRowMeta(), null, KettleDatabaseRepository.TABLE_R_JOBENTRY_TYPE );
            database.setValuesInsert( table );
            database.insertRow();
            database.closeInsert();
          }
        }
      }
    }
  }

}
