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

package org.pentaho.di.trans.steps.gettablenames;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Return tables name list from Database connection *
 *
 * @author Samatar
 * @since 03-Juin-2008
 *
 */

public class GetTableNames extends BaseStep implements StepInterface {
  private static Class<?> PKG = GetTableNamesMeta.class; // for i18n purposes, needed by Translator2!!

  private GetTableNamesMeta meta;
  private GetTableNamesData data;

  public GetTableNames( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  /**
   * Build an empty row based on the meta-data...
   *
   * @return
   */

  private Object[] buildEmptyRow() {
    Object[] rowData = RowDataUtil.allocateRowData( data.outputRowMeta.size() );

    return rowData;
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (GetTableNamesMeta) smi;
    data = (GetTableNamesData) sdi;

    if ( meta.isDynamicSchema() ) {
      // Grab one row from previous step ...
      data.readrow = getRow();

      if ( data.readrow == null ) {
        setOutputDone();
        return false;
      }
    }

    if ( first ) {
      first = false;

      if ( meta.isDynamicSchema() ) {
        data.inputRowMeta = getInputRowMeta();
        data.outputRowMeta = data.inputRowMeta.clone();
        // Get total previous fields
        data.totalpreviousfields = data.inputRowMeta.size();

        // Check is filename field is provided
        if ( Utils.isEmpty( meta.getSchemaFieldName() ) ) {
          logError( BaseMessages.getString( PKG, "GetTableNames.Log.NoSchemaField" ) );
          throw new KettleException( BaseMessages.getString( PKG, "GetTableNames.Log.NoSchemaField" ) );
        }

        // cache the position of the field
        if ( data.indexOfSchemaField < 0 ) {
          data.indexOfSchemaField = data.inputRowMeta.indexOfValue( meta.getSchemaFieldName() );
          if ( data.indexOfSchemaField < 0 ) {
            // The field is unreachable !
            logError( BaseMessages.getString( PKG, "GetTableNames.Log.ErrorFindingField" )
              + "[" + meta.getSchemaFieldName() + "]" );
            throw new KettleException( BaseMessages.getString(
              PKG, "GetTableNames.Exception.CouldnotFindField", meta.getSchemaFieldName() ) );
          }
        }

      } else {
        data.outputRowMeta = new RowMeta();
      }

      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

    }

    if ( meta.isDynamicSchema() ) {
      // Get value of dynamic schema ...
      data.realSchemaName = data.inputRowMeta.getString( data.readrow, data.indexOfSchemaField );
    }

    Object[] outputRow = buildEmptyRow();
    if ( meta.isDynamicSchema() ) {
      System.arraycopy( data.readrow, 0, outputRow, 0, data.readrow.length );
    }

    // Catalogs
    if ( meta.isIncludeCatalog() ) {
      String ObjectType = BaseMessages.getString( PKG, "GetTableNames.ObjectType.Catalog" );
      // Views
      String[] catalogsNames = data.db.getCatalogs();
      int nr = catalogsNames.length;

      for ( int i = 0; i < nr && !isStopped(); i++ ) {

        // Clone current input row
        Object[] outputRowCatalog = outputRow.clone();

        int outputIndex = data.totalpreviousfields;

        String catalogName = catalogsNames[i];
        outputRowCatalog[outputIndex++] = catalogName;

        if ( !Utils.isEmpty( data.realObjectTypeFieldName ) ) {
          outputRowCatalog[outputIndex++] = ObjectType;
        }
        if ( !Utils.isEmpty( data.realIsSystemObjectFieldName ) ) {
          outputRowCatalog[outputIndex++] = Boolean.valueOf( data.db.isSystemTable( catalogName ) );
        }
        if ( !Utils.isEmpty( data.realSQLCreationFieldName ) ) {
          outputRowCatalog[outputIndex++] = null;
        }
        data.rownr++;
        putRow( data.outputRowMeta, outputRowCatalog ); // copy row to output rowset(s);

        if ( checkFeedback( getLinesRead() ) ) {
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "GetTableNames.LineNumber", "" + getLinesRead() ) );
          }
        }
        if ( log.isRowLevel() ) {
          logRowlevel( BaseMessages.getString( PKG, "GetTableNames.Log.PutoutRow", data.outputRowMeta
            .getString( outputRowCatalog ) ) );
        }
      }
    }

    // Schemas
    if ( meta.isIncludeSchema() ) {
      String ObjectType = BaseMessages.getString( PKG, "GetTableNamesDialog.ObjectType.Schema" );
      // Views
      String[] schemaNames = new String[] {};
      if ( !Utils.isEmpty( data.realSchemaName ) ) {
        schemaNames = new String[] { data.realSchemaName };
      } else {
        schemaNames = data.db.getSchemas();
      }
      int nr = schemaNames.length;
      for ( int i = 0; i < nr && !isStopped(); i++ ) {

        // Clone current input row
        Object[] outputRowSchema = outputRow.clone();

        int outputIndex = data.totalpreviousfields;

        String schemaName = schemaNames[i];
        outputRowSchema[outputIndex++] = schemaName;

        if ( !Utils.isEmpty( data.realObjectTypeFieldName ) ) {
          outputRowSchema[outputIndex++] = ObjectType;
        }
        if ( !Utils.isEmpty( data.realIsSystemObjectFieldName ) ) {
          outputRowSchema[outputIndex++] = Boolean.valueOf( data.db.isSystemTable( schemaName ) );
        }
        if ( !Utils.isEmpty( data.realSQLCreationFieldName ) ) {
          outputRowSchema[outputIndex++] = null;
        }
        data.rownr++;
        putRow( data.outputRowMeta, outputRowSchema ); // copy row to output rowset(s);

        if ( checkFeedback( getLinesRead() ) ) {
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "GetTableNames.LineNumber", "" + getLinesRead() ) );
          }
        }
        if ( log.isRowLevel() ) {
          logRowlevel( BaseMessages.getString( PKG, "GetTableNames.Log.PutoutRow", data.outputRowMeta
            .getString( outputRowSchema ) ) );
        }
      }
    }

    if ( meta.isIncludeTable() ) {
      // Tables
      String[] tableNames = data.db.getTablenames( data.realSchemaName, meta.isAddSchemaInOut() );

      String ObjectType = BaseMessages.getString( PKG, "GetTableNamesDialog.ObjectType.Table" );
      int nr = tableNames.length;
      for ( int i = 0; i < nr && !isStopped(); i++ ) {
        Object[] outputRowTable = outputRow.clone();

        int outputIndex = data.totalpreviousfields;

        String tableName = tableNames[i];
        outputRowTable[outputIndex++] = tableName;

        if ( !Utils.isEmpty( data.realObjectTypeFieldName ) ) {
          outputRowTable[outputIndex++] = ObjectType;
        }
        if ( !Utils.isEmpty( data.realIsSystemObjectFieldName ) ) {
          outputRowTable[outputIndex++] = Boolean.valueOf( data.db.isSystemTable( tableName ) );
        }
        // Get primary key
        String pk = null;
        String[] pkc = data.db.getPrimaryKeyColumnNames( tableName );
        if ( pkc != null && pkc.length == 1 ) {
          pk = pkc[0];
          pkc = null;
        }
        // return sql creation
        // handle simple primary key (one field)
        String sql =
          data.db
            .getCreateTableStatement( tableName, data.db.getTableFields( tableName ), null, false, pk, true );

        if ( pkc != null ) {
          // add composite primary key (several fields in primary key)
          int IndexOfLastClosedBracket = sql.lastIndexOf( ")" );
          if ( IndexOfLastClosedBracket > -1 ) {
            sql = sql.substring( 0, IndexOfLastClosedBracket );
            sql += ", PRIMARY KEY (";
            for ( int k = 0; k < pkc.length; k++ ) {
              if ( k > 0 ) {
                sql += ", ";
              }
              sql += pkc[k];
            }
            sql += ")" + Const.CR + ")" + Const.CR + ";";
          }
        }
        if ( !Utils.isEmpty( data.realSQLCreationFieldName ) ) {
          outputRowTable[outputIndex++] = sql;
        }

        data.rownr++;
        putRow( data.outputRowMeta, outputRowTable ); // copy row to output rowset(s);

        if ( checkFeedback( getLinesRead() ) ) {
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "GetTableNames.LineNumber", "" + getLinesRead() ) );
          }
        }
        if ( log.isRowLevel() ) {
          logRowlevel( BaseMessages.getString( PKG, "GetTableNames.Log.PutoutRow", data.outputRowMeta
            .getString( outputRowTable ) ) );
        }
      }
    }

    // Views
    if ( meta.isIncludeView() ) {
      try {
        String[] viewNames = data.db.getViews( data.realSchemaName, meta.isAddSchemaInOut() );
        String ObjectType = BaseMessages.getString( PKG, "GetTableNamesDialog.ObjectType.View" );
        int nr = viewNames.length;
        for ( int i = 0; i < nr && !isStopped(); i++ ) {
          Object[] outputRowView = outputRow.clone();
          int outputIndex = data.totalpreviousfields;

          String viewName = viewNames[i];
          outputRowView[outputIndex++] = viewName;

          if ( !Utils.isEmpty( data.realObjectTypeFieldName ) ) {
            outputRowView[outputIndex++] = ObjectType;
          }
          if ( !Utils.isEmpty( data.realIsSystemObjectFieldName ) ) {
            outputRowView[outputIndex++] = Boolean.valueOf( data.db.isSystemTable( viewName ) );
          }

          if ( !Utils.isEmpty( data.realSQLCreationFieldName ) ) {
            outputRowView[outputIndex++] = null;
          }
          data.rownr++;
          putRow( data.outputRowMeta, outputRowView ); // copy row to output rowset(s);

          if ( checkFeedback( getLinesRead() ) ) {
            if ( log.isDetailed() ) {
              logDetailed( BaseMessages.getString( PKG, "GetTableNames.LineNumber", "" + getLinesRead() ) );
            }
          }
          if ( log.isRowLevel() ) {
            logRowlevel( BaseMessages.getString( PKG, "GetTableNames.Log.PutoutRow", data.outputRowMeta
              .getString( outputRowView ) ) );
          }
        }
      } catch ( Exception e ) {
        // Ignore
      }
    }
    if ( meta.isIncludeProcedure() ) {
      String[] procNames = data.db.getProcedures();
      String ObjectType = BaseMessages.getString( PKG, "GetTableNamesDialog.ObjectType.Procedure" );
      int nr = procNames.length;
      for ( int i = 0; i < nr && !isStopped(); i++ ) {
        Object[] outputRowProc = outputRow.clone();
        int outputIndex = data.totalpreviousfields;

        String procName = procNames[i];
        outputRowProc[outputIndex++] = procName;

        if ( !Utils.isEmpty( data.realObjectTypeFieldName ) ) {
          outputRowProc[outputIndex++] = ObjectType;
        }
        if ( !Utils.isEmpty( data.realIsSystemObjectFieldName ) ) {
          outputRowProc[outputIndex++] = Boolean.valueOf( data.db.isSystemTable( procName ) );
        }
        if ( !Utils.isEmpty( data.realSQLCreationFieldName ) ) {
          outputRowProc[outputIndex++] = null;
        }
        data.rownr++;
        putRow( data.outputRowMeta, outputRowProc ); // copy row to output rowset(s);

        if ( checkFeedback( getLinesRead() ) ) {
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "GetTableNames.LineNumber", "" + getLinesRead() ) );
          }
        }
        if ( log.isRowLevel() ) {
          logRowlevel( BaseMessages.getString( PKG, "GetTableNames.Log.PutoutRow", data.outputRowMeta
            .getString( outputRowProc ) ) );
        }
      }
    }
    if ( meta.isIncludeSynonym() ) {
      String[] Synonyms = data.db.getSynonyms( data.realSchemaName, meta.isAddSchemaInOut() );
      String ObjectType = BaseMessages.getString( PKG, "GetTableNamesDialog.ObjectType.Synonym" );

      int nr = Synonyms.length;
      for ( int i = 0; i < nr && !isStopped(); i++ ) {
        Object[] outputRowSyn = outputRow.clone();
        int outputIndex = data.totalpreviousfields;

        String Synonym = Synonyms[i];

        outputRowSyn[outputIndex++] = Synonym;

        if ( !Utils.isEmpty( data.realObjectTypeFieldName ) ) {
          outputRowSyn[outputIndex++] = ObjectType;
        }
        if ( !Utils.isEmpty( data.realIsSystemObjectFieldName ) ) {
          outputRowSyn[outputIndex++] = Boolean.valueOf( data.db.isSystemTable( Synonym ) );
        }
        if ( !Utils.isEmpty( data.realSQLCreationFieldName ) ) {
          outputRowSyn[outputIndex++] = null;
        }
        data.rownr++;
        putRow( data.outputRowMeta, outputRowSyn ); // copy row to output rowset(s);

        if ( checkFeedback( getLinesRead() ) ) {
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "GetTableNames.LineNumber", "" + getLinesRead() ) );
          }
        }

        if ( log.isRowLevel() ) {
          logRowlevel( BaseMessages.getString( PKG, "GetTableNames.Log.PutoutRow", data.outputRowMeta
            .getString( outputRowSyn ) ) );
        }
      }
    }

    if ( !meta.isDynamicSchema() ) {
      setOutputDone();
      return false;
    } else {
      return true;
    }
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (GetTableNamesMeta) smi;
    data = (GetTableNamesData) sdi;

    if ( super.init( smi, sdi ) ) {
      if ( Utils.isEmpty( meta.getTablenameFieldName() ) ) {
        logError( BaseMessages.getString( PKG, "GetTableNames.Error.TablenameFieldNameMissing" ) );
        return false;
      }
      String realSchemaName = environmentSubstitute( meta.getSchemaName() );
      if ( !Utils.isEmpty( realSchemaName ) ) {
        data.realSchemaName = realSchemaName;
      }
      data.realTableNameFieldName = environmentSubstitute( meta.getTablenameFieldName() );
      data.realObjectTypeFieldName = environmentSubstitute( meta.getObjectTypeFieldName() );
      data.realIsSystemObjectFieldName = environmentSubstitute( meta.isSystemObjectFieldName() );
      data.realSQLCreationFieldName = environmentSubstitute( meta.getSQLCreationFieldName() );
      if ( !meta.isIncludeCatalog()
        && !meta.isIncludeSchema() && !meta.isIncludeTable() && !meta.isIncludeView()
        && !meta.isIncludeProcedure() && !meta.isIncludeSynonym() ) {
        logError( BaseMessages.getString( PKG, "GetTableNames.Error.includeAtLeastOneType" ) );
        return false;
      }

      try {
        // Create the output row meta-data
        data.outputRowMeta = new RowMeta();
        meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore ); // get the
                                                                                                      // metadata
                                                                                                      // populated
      } catch ( Exception e ) {
        logError( "Error initializing step: " + e.toString() );
        logError( Const.getStackTracker( e ) );
        return false;
      }

      data.db = new Database( this, meta.getDatabase() );
      data.db.shareVariablesWith( this );
      try {
        if ( getTransMeta().isUsingUniqueConnections() ) {
          synchronized ( getTrans() ) {
            data.db.connect( getTrans().getTransactionId(), getPartitionID() );
          }
        } else {
          data.db.connect( getPartitionID() );
        }

        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "GetTableNames.Log.ConnectedToDB" ) );
        }

        return true;
      } catch ( KettleException e ) {
        logError( BaseMessages.getString( PKG, "GetTableNames.Log.DBException" ) + e.getMessage() );
        if ( data.db != null ) {
          data.db.disconnect();
        }
      }
    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (GetTableNamesMeta) smi;
    data = (GetTableNamesData) sdi;
    if ( data.db != null ) {
      data.db.disconnect();
      data.db = null;
    }
    super.dispose( smi, sdi );
  }

}
