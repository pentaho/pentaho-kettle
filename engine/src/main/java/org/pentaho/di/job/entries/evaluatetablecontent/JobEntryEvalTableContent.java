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

package org.pentaho.di.job.entries.evaluatetablecontent;

import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This defines a Table content evaluation job entry
 *
 * @author Samatar
 * @since 22-07-2008
 *
 */
public class JobEntryEvalTableContent extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryEvalTableContent.class; // for i18n purposes, needed by Translator2!!

  private boolean addRowsResult;
  private boolean clearResultList;
  private boolean useVars;
  private boolean useCustomSQL;
  private String customSQL;
  private DatabaseMeta connection;
  private String tablename;
  private String schemaname;
  private String limit;
  private int successCondition;

  private static final String selectCount = "SELECT count(*) FROM ";

  public static final String[] successConditionsDesc = new String[] {
    BaseMessages.getString( PKG, "JobEntryEvalTableContent.SuccessWhenRowCountEqual.Label" ),
    BaseMessages.getString( PKG, "JobEntryEvalTableContent.SuccessWhenRowCountDifferent.Label" ),
    BaseMessages.getString( PKG, "JobEntryEvalTableContent.SuccessWhenRowCountSmallerThan.Label" ),
    BaseMessages.getString( PKG, "JobEntryEvalTableContent.SuccessWhenRowCountSmallerOrEqualThan.Label" ),
    BaseMessages.getString( PKG, "JobEntryEvalTableContent.SuccessWhenRowCountGreaterThan.Label" ),
    BaseMessages.getString( PKG, "JobEntryEvalTableContent.SuccessWhenRowCountGreaterOrEqual.Label" )

  };
  public static final String[] successConditionsCode = new String[] {
    "rows_count_equal", "rows_count_different", "rows_count_smaller", "rows_count_smaller_equal",
    "rows_count_greater", "rows_count_greater_equal" };

  public static final int SUCCESS_CONDITION_ROWS_COUNT_EQUAL = 0;
  public static final int SUCCESS_CONDITION_ROWS_COUNT_DIFFERENT = 1;
  public static final int SUCCESS_CONDITION_ROWS_COUNT_SMALLER = 2;
  public static final int SUCCESS_CONDITION_ROWS_COUNT_SMALLER_EQUAL = 3;
  public static final int SUCCESS_CONDITION_ROWS_COUNT_GREATER = 4;
  public static final int SUCCESS_CONDITION_ROWS_COUNT_GREATER_EQUAL = 5;

  public JobEntryEvalTableContent( String n ) {
    super( n, "" );
    limit = "0";
    successCondition = SUCCESS_CONDITION_ROWS_COUNT_GREATER;
    useCustomSQL = false;
    useVars = false;
    addRowsResult = false;
    clearResultList = true;
    customSQL = null;
    schemaname = null;
    tablename = null;
    connection = null;
  }

  public JobEntryEvalTableContent() {
    this( "" );
  }

  public Object clone() {
    JobEntryEvalTableContent je = (JobEntryEvalTableContent) super.clone();
    return je;
  }

  /**
   * @deprecated due to typo. Use {@link #getSuccessCondition()} instead.
   * @return the successCondition
   */
  @Deprecated
  public int getSuccessCobdition() {
    return successCondition;
  }

  /**
   * @return the successCondition
   */
  public int getSuccessCondition() {
    return successCondition;
  }

  public static int getSuccessConditionByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < successConditionsDesc.length; i++ ) {
      if ( successConditionsDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }

    // If this fails, try to match using the code.
    return getSuccessConditionByCode( tt );
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 200 );

    retval.append( super.getXML() );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "connection", connection == null ? null : connection.getName() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "schemaname", schemaname ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "tablename", tablename ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "success_condition", getSuccessConditionCode( successCondition ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "limit", limit ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "is_custom_sql", useCustomSQL ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "is_usevars", useVars ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "custom_sql", customSQL ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "add_rows_result", addRowsResult ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "clear_result_rows", clearResultList ) );

    return retval.toString();
  }

  private static String getSuccessConditionCode( int i ) {
    if ( i < 0 || i >= successConditionsCode.length ) {
      return successConditionsCode[0];
    }
    return successConditionsCode[i];
  }

  private static int getSucessConditionByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < successConditionsCode.length; i++ ) {
      if ( successConditionsCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public static String getSuccessConditionDesc( int i ) {
    if ( i < 0 || i >= successConditionsDesc.length ) {
      return successConditionsDesc[0];
    }
    return successConditionsDesc[i];
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      String dbname = XMLHandler.getTagValue( entrynode, "connection" );
      connection = DatabaseMeta.findDatabase( databases, dbname );
      schemaname = XMLHandler.getTagValue( entrynode, "schemaname" );
      tablename = XMLHandler.getTagValue( entrynode, "tablename" );
      successCondition =
        getSucessConditionByCode( Const.NVL( XMLHandler.getTagValue( entrynode, "success_condition" ), "" ) );
      limit = Const.NVL( XMLHandler.getTagValue( entrynode, "limit" ), "0" );
      useCustomSQL = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "is_custom_sql" ) );
      useVars = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "is_usevars" ) );
      customSQL = XMLHandler.getTagValue( entrynode, "custom_sql" );
      addRowsResult = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "add_rows_result" ) );
      clearResultList = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "clear_result_rows" ) );

    } catch ( KettleException e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "JobEntryEvalTableContent.UnableLoadXML" ), e );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      connection = rep.loadDatabaseMetaFromJobEntryAttribute( id_jobentry, "connection", "id_database", databases );

      schemaname = rep.getJobEntryAttributeString( id_jobentry, "schemaname" );
      tablename = rep.getJobEntryAttributeString( id_jobentry, "tablename" );
      successCondition =
        getSuccessConditionByCode( Const.NVL(
          rep.getJobEntryAttributeString( id_jobentry, "success_condition" ), "" ) );
      limit = rep.getJobEntryAttributeString( id_jobentry, "limit" );
      useCustomSQL = rep.getJobEntryAttributeBoolean( id_jobentry, "is_custom_sql" );
      useVars = rep.getJobEntryAttributeBoolean( id_jobentry, "is_usevars" );
      addRowsResult = rep.getJobEntryAttributeBoolean( id_jobentry, "add_rows_result" );
      clearResultList = rep.getJobEntryAttributeBoolean( id_jobentry, "clear_result_rows" );

      customSQL = rep.getJobEntryAttributeString( id_jobentry, "custom_sql" );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobEntryEvalTableContent.UnableLoadRep", ""
        + id_jobentry ), dbe );
    }
  }

  private static int getSuccessConditionByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < successConditionsCode.length; i++ ) {
      if ( successConditionsCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveDatabaseMetaJobEntryAttribute( id_job, getObjectId(), "connection", "id_database", connection );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "schemaname", schemaname );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "tablename", tablename );
      rep.saveJobEntryAttribute(
        id_job, getObjectId(), "success_condition", getSuccessConditionCode( successCondition ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "limit", limit );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "custom_sql", customSQL );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "is_custom_sql", useCustomSQL );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "is_usevars", useVars );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "add_rows_result", addRowsResult );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "clear_result_rows", clearResultList );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobEntryEvalTableContent.UnableSaveRep", ""
        + id_job ), dbe );
    }
  }

  public void setDatabase( DatabaseMeta database ) {
    this.connection = database;
  }

  public DatabaseMeta getDatabase() {
    return connection;
  }

  public boolean evaluates() {
    return true;
  }

  public boolean isUnconditional() {
    return false;
  }

  public Result execute( Result previousResult, int nr ) {
    Result result = previousResult;
    result.setResult( false );

    // see PDI-10270, PDI-10644 for details
    boolean oldBehavior =
      "Y".equalsIgnoreCase( getVariable( Const.KETTLE_COMPATIBILITY_SET_ERROR_ON_SPECIFIC_JOB_ENTRIES, "N" ) );

    String countSQLStatement = null;
    long rowsCount = 0;
    long errCount = 0;

    boolean successOK = false;

    int nrRowsLimit = Const.toInt( environmentSubstitute( limit ), 0 );
    if ( log.isDetailed() ) {
      logDetailed( BaseMessages.getString( PKG, "JobEntryEvalTableContent.Log.nrRowsLimit", "" + nrRowsLimit ) );
    }

    if ( connection != null ) {
      Database db = new Database( this, connection );
      db.shareVariablesWith( this );
      try {
        db.connect( parentJob.getTransactionId(), null );

        if ( useCustomSQL ) {
          String realCustomSQL = customSQL;
          if ( useVars ) {
            realCustomSQL = environmentSubstitute( realCustomSQL );
          }
          if ( log.isDebug() ) {
            logDebug( BaseMessages.getString( PKG, "JobEntryEvalTableContent.Log.EnteredCustomSQL", realCustomSQL ) );
          }

          if ( !Utils.isEmpty( realCustomSQL ) ) {
            countSQLStatement = realCustomSQL;
          } else {
            errCount++;
            logError( BaseMessages.getString( PKG, "JobEntryEvalTableContent.Error.NoCustomSQL" ) );
          }

        } else {
          String realTablename = environmentSubstitute( tablename );
          String realSchemaname = environmentSubstitute( schemaname );

          if ( !Utils.isEmpty( realTablename ) ) {
            if ( !Utils.isEmpty( realSchemaname ) ) {
              countSQLStatement =
                selectCount
                  + db.getDatabaseMeta().getQuotedSchemaTableCombination( realSchemaname, realTablename );
            } else {
              countSQLStatement = selectCount + db.getDatabaseMeta().quoteField( realTablename );
            }
          } else {
            errCount++;
            logError( BaseMessages.getString( PKG, "JobEntryEvalTableContent.Error.NoTableName" ) );
          }
        }

        if ( countSQLStatement != null ) {
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString(
              PKG, "JobEntryEvalTableContent.Log.RunSQLStatement", countSQLStatement ) );
          }

          if ( useCustomSQL ) {
            if ( clearResultList ) {
              result.getRows().clear();
            }

            List<Object[]> ar = db.getRows( countSQLStatement, 0 );
            if ( ar != null ) {
              rowsCount = ar.size();

              // ad rows to result
              RowMetaInterface rowMeta = db.getQueryFields( countSQLStatement, false );

              List<RowMetaAndData> rows = new ArrayList<RowMetaAndData>();
              for ( int i = 0; i < ar.size(); i++ ) {
                rows.add( new RowMetaAndData( rowMeta, ar.get( i ) ) );
              }
              if ( addRowsResult && useCustomSQL ) {
                if ( rows != null ) {
                  result.getRows().addAll( rows );
                }
              }
            } else {
              if ( log.isDebug() ) {
                logDebug( BaseMessages.getString(
                  PKG, "JobEntryEvalTableContent.Log.customSQLreturnedNothing", countSQLStatement ) );
              }
            }

          } else {
            RowMetaAndData row = db.getOneRow( countSQLStatement );
            if ( row != null ) {
              rowsCount = row.getInteger( 0 );
            }
          }
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "JobEntryEvalTableContent.Log.NrRowsReturned", ""
              + rowsCount ) );
          }
          switch ( successCondition ) {
            case JobEntryEvalTableContent.SUCCESS_CONDITION_ROWS_COUNT_EQUAL:
              successOK = ( rowsCount == nrRowsLimit );
              break;
            case JobEntryEvalTableContent.SUCCESS_CONDITION_ROWS_COUNT_DIFFERENT:
              successOK = ( rowsCount != nrRowsLimit );
              break;
            case JobEntryEvalTableContent.SUCCESS_CONDITION_ROWS_COUNT_SMALLER:
              successOK = ( rowsCount < nrRowsLimit );
              break;
            case JobEntryEvalTableContent.SUCCESS_CONDITION_ROWS_COUNT_SMALLER_EQUAL:
              successOK = ( rowsCount <= nrRowsLimit );
              break;
            case JobEntryEvalTableContent.SUCCESS_CONDITION_ROWS_COUNT_GREATER:
              successOK = ( rowsCount > nrRowsLimit );
              break;
            case JobEntryEvalTableContent.SUCCESS_CONDITION_ROWS_COUNT_GREATER_EQUAL:
              successOK = ( rowsCount >= nrRowsLimit );
              break;
            default:
              break;
          }

          if ( !successOK && oldBehavior ) {
            errCount++;
          }
        } // end if countSQLStatement!=null
      } catch ( KettleException dbe ) {
        errCount++;
        logError( BaseMessages.getString( PKG, "JobEntryEvalTableContent.Error.RunningEntry", dbe.getMessage() ) );
      } finally {
        if ( db != null ) {
          db.disconnect();
        }
      }
    } else {
      errCount++;
      logError( BaseMessages.getString( PKG, "JobEntryEvalTableContent.NoDbConnection" ) );
    }

    result.setResult( successOK );
    result.setNrLinesRead( rowsCount );
    result.setNrErrors( errCount );

    return result;
  }

  public DatabaseMeta[] getUsedDatabaseConnections() {
    return new DatabaseMeta[] { connection, };
  }

  public List<ResourceReference> getResourceDependencies( JobMeta jobMeta ) {
    List<ResourceReference> references = super.getResourceDependencies( jobMeta );
    if ( connection != null ) {
      ResourceReference reference = new ResourceReference( this );
      reference.getEntries().add( new ResourceEntry( connection.getHostname(), ResourceType.SERVER ) );
      reference.getEntries().add( new ResourceEntry( connection.getDatabaseName(), ResourceType.DATABASENAME ) );
      references.add( reference );
    }
    return references;
  }

  @Override
  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    JobEntryValidatorUtils.andValidator().validate( this, "WaitForSQL", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
  }

  public boolean isAddRowsResult() {
    return addRowsResult;
  }

  public void setAddRowsResult( boolean addRowsResult ) {
    this.addRowsResult = addRowsResult;
  }

  public boolean isClearResultList() {
    return clearResultList;
  }

  public void setClearResultList( boolean clearResultList ) {
    this.clearResultList = clearResultList;
  }

  public boolean isUseVars() {
    return useVars;
  }

  public void setUseVars( boolean useVars ) {
    this.useVars = useVars;
  }

  public boolean isUseCustomSQL() {
    return useCustomSQL;
  }

  public void setUseCustomSQL( boolean useCustomSQL ) {
    this.useCustomSQL = useCustomSQL;
  }

  public String getCustomSQL() {
    return customSQL;
  }

  public void setCustomSQL( String customSQL ) {
    this.customSQL = customSQL;
  }

  public DatabaseMeta getConnection() {
    return connection;
  }

  public void setConnection( DatabaseMeta connection ) {
    this.connection = connection;
  }

  public String getTablename() {
    return tablename;
  }

  public void setTablename( String tablename ) {
    this.tablename = tablename;
  }

  public String getSchemaname() {
    return schemaname;
  }

  public void setSchemaname( String schemaname ) {
    this.schemaname = schemaname;
  }

  public String getLimit() {
    return limit;
  }

  public void setLimit( String limit ) {
    this.limit = limit;
  }

  public void setSuccessCondition( int successCondition ) {
    this.successCondition = successCondition;
  }

}
