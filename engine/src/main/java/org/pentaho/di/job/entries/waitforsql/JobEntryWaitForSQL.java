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

package org.pentaho.di.job.entries.waitforsql;

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
import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This defines a Wait for SQL data job entry
 *
 * @author Samatar
 * @since 22-07-2008
 *
 */
public class JobEntryWaitForSQL extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryWaitForSQL.class; // for i18n purposes, needed by Translator2!!

  public boolean isClearResultList;

  public boolean isAddRowsResult;

  public boolean isUseVars;

  public boolean iscustomSQL;

  public String customSQL;

  private DatabaseMeta connection;

  public String tablename;

  public String schemaname;

  private String maximumTimeout; // maximum timeout in seconds
  private String checkCycleTime; // cycle time in seconds
  private boolean successOnTimeout;

  private static final String selectCount = "SELECT count(*) FROM ";

  public static final String[] successConditionsDesc = new String[] {
    BaseMessages.getString( PKG, "JobEntryWaitForSQL.SuccessWhenRowCountEqual.Label" ),
    BaseMessages.getString( PKG, "JobEntryWaitForSQL.SuccessWhenRowCountDifferent.Label" ),
    BaseMessages.getString( PKG, "JobEntryWaitForSQL.SuccessWhenRowCountSmallerThan.Label" ),
    BaseMessages.getString( PKG, "JobEntryWaitForSQL.SuccessWhenRowCountSmallerOrEqualThan.Label" ),
    BaseMessages.getString( PKG, "JobEntryWaitForSQL.SuccessWhenRowCountGreaterThan.Label" ),
    BaseMessages.getString( PKG, "JobEntryWaitForSQL.SuccessWhenRowCountGreaterOrEqual.Label" )

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

  public String rowsCountValue;
  public int successCondition;
  private static String DEFAULT_MAXIMUM_TIMEOUT = "0"; // infinite timeout
  private static String DEFAULT_CHECK_CYCLE_TIME = "60"; // 1 minute

  public JobEntryWaitForSQL( String n ) {
    super( n, "" );
    isClearResultList = true;
    rowsCountValue = "0";
    successCondition = SUCCESS_CONDITION_ROWS_COUNT_GREATER;
    iscustomSQL = false;
    isUseVars = false;
    isAddRowsResult = false;
    customSQL = null;
    schemaname = null;
    tablename = null;
    connection = null;
    maximumTimeout = DEFAULT_MAXIMUM_TIMEOUT;
    checkCycleTime = DEFAULT_CHECK_CYCLE_TIME;
    successOnTimeout = false;
  }

  public JobEntryWaitForSQL() {
    this( "" );
  }

  @Override
  public Object clone() {
    JobEntryWaitForSQL je = (JobEntryWaitForSQL) super.clone();
    return je;
  }

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

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 200 );

    retval.append( super.getXML() );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "connection", connection == null ? null : connection.getName() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "schemaname", schemaname ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "tablename", tablename ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "success_condition", getSuccessConditionCode( successCondition ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "rows_count_value", rowsCountValue ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "is_custom_sql", iscustomSQL ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "is_usevars", isUseVars ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "custom_sql", customSQL ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "add_rows_result", isAddRowsResult ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "maximum_timeout", maximumTimeout ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "check_cycle_time", checkCycleTime ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "success_on_timeout", successOnTimeout ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "clear_result_rows", isClearResultList ) );
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

  public boolean isSuccessOnTimeout() {
    return successOnTimeout;
  }

  public void setSuccessOnTimeout( boolean successOnTimeout ) {
    this.successOnTimeout = successOnTimeout;
  }

  public String getCheckCycleTime() {
    return checkCycleTime;
  }

  public void setCheckCycleTime( String checkCycleTime ) {
    this.checkCycleTime = checkCycleTime;
  }

  public String getMaximumTimeout() {
    return maximumTimeout;
  }

  /**
   * Set how long the job entry may test the connection for a success result
   *
   * @param maximumTimeout Number of seconds to wait for success
   */
  public void setMaximumTimeout( String maximumTimeout ) {
    this.maximumTimeout = maximumTimeout;
  }

  @Override
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
      rowsCountValue = Const.NVL( XMLHandler.getTagValue( entrynode, "rows_count_value" ), "0" );
      iscustomSQL = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "is_custom_sql" ) );
      isUseVars = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "is_usevars" ) );
      customSQL = XMLHandler.getTagValue( entrynode, "custom_sql" );
      isAddRowsResult = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "add_rows_result" ) );
      maximumTimeout = XMLHandler.getTagValue( entrynode, "maximum_timeout" );
      checkCycleTime = XMLHandler.getTagValue( entrynode, "check_cycle_time" );
      successOnTimeout = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "success_on_timeout" ) );
      isClearResultList = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "clear_result_rows" ) );

    } catch ( KettleException e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "JobEntryWaitForSQL.UnableLoadXML" ), e );
    }
  }

  @Override
  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      connection = rep.loadDatabaseMetaFromJobEntryAttribute( id_jobentry, "connection", "id_database", databases );

      schemaname = rep.getJobEntryAttributeString( id_jobentry, "schemaname" );
      tablename = rep.getJobEntryAttributeString( id_jobentry, "tablename" );
      successCondition =
        getSuccessConditionByCode( Const.NVL(
          rep.getJobEntryAttributeString( id_jobentry, "success_condition" ), "" ) );
      rowsCountValue = rep.getJobEntryAttributeString( id_jobentry, "rows_count_value" );
      iscustomSQL = rep.getJobEntryAttributeBoolean( id_jobentry, "is_custom_sql" );
      isUseVars = rep.getJobEntryAttributeBoolean( id_jobentry, "is_usevars" );
      isAddRowsResult = rep.getJobEntryAttributeBoolean( id_jobentry, "add_rows_result" );
      customSQL = rep.getJobEntryAttributeString( id_jobentry, "custom_sql" );
      maximumTimeout = rep.getJobEntryAttributeString( id_jobentry, "maximum_timeout" );
      checkCycleTime = rep.getJobEntryAttributeString( id_jobentry, "check_cycle_time" );
      successOnTimeout = rep.getJobEntryAttributeBoolean( id_jobentry, "success_on_timeout" );
      isClearResultList = rep.getJobEntryAttributeBoolean( id_jobentry, "clear_result_rows" );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( BaseMessages
        .getString( PKG, "JobEntryWaitForSQL.UnableLoadRep", "" + id_jobentry ), dbe );
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

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveDatabaseMetaJobEntryAttribute( id_job, getObjectId(), "connection", "id_database", connection );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "schemaname", schemaname );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "tablename", tablename );
      rep.saveJobEntryAttribute(
        id_job, getObjectId(), "success_condition", getSuccessConditionCode( successCondition ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "rows_count_value", rowsCountValue );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "custom_sql", customSQL );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "is_custom_sql", iscustomSQL );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "is_usevars", isUseVars );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "add_rows_result", isAddRowsResult );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "maximum_timeout", maximumTimeout );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "check_cycle_time", checkCycleTime );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "success_on_timeout", successOnTimeout );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "clear_result_rows", isClearResultList );

    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException(
        BaseMessages.getString( PKG, "JobEntryWaitForSQL.UnableSaveRep", "" + id_job ), dbe );
    }
  }

  public void setDatabase( DatabaseMeta database ) {
    this.connection = database;
  }

  public DatabaseMeta getDatabase() {
    return connection;
  }

  @Override
  public boolean evaluates() {
    return true;
  }

  @Override
  public boolean isUnconditional() {
    return false;
  }

  // Visible for testing purposes
  protected void checkConnection() throws KettleDatabaseException {
    // check connection
    // connect and disconnect
    Database dbchecked = null;
    try {
      dbchecked = new Database( this, connection );
      dbchecked.shareVariablesWith( this );
      dbchecked.connect( parentJob.getTransactionId(), null );
    } finally {
      if ( dbchecked != null ) {
        dbchecked.disconnect();
      }
    }
  }

  @Override
  public Result execute( Result previousResult, int nr ) {
    Result result = previousResult;
    result.setResult( false );
    result.setNrErrors( 1 );
    String realCustomSQL = null;
    String realTablename = environmentSubstitute( tablename );
    String realSchemaname = environmentSubstitute( schemaname );

    if ( connection == null ) {
      logError( BaseMessages.getString( PKG, "JobEntryWaitForSQL.NoDbConnection" ) );
      return result;
    }

    if ( iscustomSQL ) {
      // clear result list rows
      if ( isClearResultList ) {
        result.getRows().clear();
      }

      realCustomSQL = customSQL;
      if ( isUseVars ) {
        realCustomSQL = environmentSubstitute( realCustomSQL );
      }
      if ( log.isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "JobEntryWaitForSQL.Log.EnteredCustomSQL", realCustomSQL ) );
      }

      if ( Utils.isEmpty( realCustomSQL ) ) {
        logError( BaseMessages.getString( PKG, "JobEntryWaitForSQL.Error.NoCustomSQL" ) );
        return result;
      }

    } else {
      if ( Utils.isEmpty( realTablename ) ) {
        logError( BaseMessages.getString( PKG, "JobEntryWaitForSQL.Error.NoTableName" ) );
        return result;
      }
    }

    try {
      // check connection
      // connect and disconnect
      checkConnection();

      // starttime (in seconds)
      long timeStart = System.currentTimeMillis() / 1000;

      int nrRowsLimit = Const.toInt( environmentSubstitute( rowsCountValue ), 0 );
      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntryWaitForSQL.Log.nrRowsLimit", "" + nrRowsLimit ) );
      }

      long iMaximumTimeout =
        Const.toInt( environmentSubstitute( maximumTimeout ), Const.toInt( DEFAULT_MAXIMUM_TIMEOUT, 0 ) );
      long iCycleTime =
        Const.toInt( environmentSubstitute( checkCycleTime ), Const.toInt( DEFAULT_CHECK_CYCLE_TIME, 0 ) );

      //
      // Sanity check on some values, and complain on insanity
      //
      if ( iMaximumTimeout < 0 ) {
        iMaximumTimeout = Const.toInt( DEFAULT_MAXIMUM_TIMEOUT, 0 );
        logBasic( "Maximum timeout invalid, reset to " + iMaximumTimeout );
      }

      if ( iCycleTime < 1 ) {
        // If lower than 1 set to the default
        iCycleTime = Const.toInt( DEFAULT_CHECK_CYCLE_TIME, 1 );
        logBasic( "Check cycle time invalid, reset to " + iCycleTime );
      }

      if ( iMaximumTimeout == 0 ) {
        logBasic( "Waiting indefinitely for SQL data" );
      } else {
        logBasic( "Waiting " + iMaximumTimeout + " seconds for SQL data" );
      }

      boolean continueLoop = true;
      while ( continueLoop && !parentJob.isStopped() ) {
        if ( SQLDataOK( result, nrRowsLimit, realSchemaname, realTablename, realCustomSQL ) ) {
          // SQL data exists, we're happy to exit
          logBasic( "Detected SQL data within timeout" );
          result.setResult( true );
          continueLoop = false;
        } else {
          long now = System.currentTimeMillis() / 1000;

          if ( ( iMaximumTimeout > 0 ) && ( now > ( timeStart + iMaximumTimeout ) ) ) {
            continueLoop = false;

            // SQL data doesn't exist after timeout, either true or false
            if ( isSuccessOnTimeout() ) {
              logBasic( "Didn't detect SQL data before timeout, success" );
              result.setResult( true );
            } else {
              logBasic( "Didn't detect SQL data before timeout, failure" );
              result.setResult( false );
            }
          }
          // sleep algorithm
          long sleepTime = 0;

          if ( iMaximumTimeout == 0 ) {
            sleepTime = iCycleTime;
          } else {
            if ( ( now + iCycleTime ) < ( timeStart + iMaximumTimeout ) ) {
              sleepTime = iCycleTime;
            } else {
              sleepTime = iCycleTime - ( ( now + iCycleTime ) - ( timeStart + iMaximumTimeout ) );
            }
          }
          try {
            if ( sleepTime > 0 ) {
              if ( log.isDetailed() ) {
                logDetailed( "Sleeping " + sleepTime + " seconds before next check for SQL data" );
              }
              Thread.sleep( sleepTime * 1000 );
            }
          } catch ( InterruptedException e ) {
            // something strange happened
            result.setResult( false );
            continueLoop = false;
          }
        }

      }
    } catch ( Exception e ) {
      logBasic( "Exception while waiting for SQL data: " + e.getMessage() );
    }

    if ( result.getResult() ) {
      // Remove error count set at the beginning of the method
      // PDI-15437
      result.setNrErrors( 0 );
    }

    return result;
  }

  protected boolean SQLDataOK( Result result, long nrRowsLimit, String realSchemaName, String realTableName,
    String customSQL ) throws KettleException {
    String countStatement = null;
    long rowsCount = 0;
    boolean successOK = false;
    List<Object[]> ar = null;
    RowMetaInterface rowMeta = null;
    Database db = new Database( this, connection );
    db.shareVariablesWith( this );
    try {
      db.connect( parentJob.getTransactionId(), null );
      if ( iscustomSQL ) {
        countStatement = customSQL;
      } else {
        if ( !Utils.isEmpty( realSchemaName ) ) {
          countStatement =
            selectCount + db.getDatabaseMeta().getQuotedSchemaTableCombination( realSchemaName, realTableName );
        } else {
          countStatement = selectCount + db.getDatabaseMeta().quoteField( realTableName );
        }
      }

      if ( countStatement != null ) {
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobEntryWaitForSQL.Log.RunSQLStatement", countStatement ) );
        }

        if ( iscustomSQL ) {
          ar = db.getRows( countStatement, 0 );
          if ( ar != null ) {
            rowsCount = ar.size();
          } else {
            if ( log.isDebug() ) {
              logDebug( BaseMessages.getString(
                PKG, "JobEntryWaitForSQL.Log.customSQLreturnedNothing", countStatement ) );
            }
          }

        } else {
          RowMetaAndData row = db.getOneRow( countStatement );
          if ( row != null ) {
            rowsCount = row.getInteger( 0 );
          }
        }
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobEntryWaitForSQL.Log.NrRowsReturned", "" + rowsCount ) );
        }

        switch ( successCondition ) {
          case JobEntryWaitForSQL.SUCCESS_CONDITION_ROWS_COUNT_EQUAL:
            successOK = ( rowsCount == nrRowsLimit );
            break;
          case JobEntryWaitForSQL.SUCCESS_CONDITION_ROWS_COUNT_DIFFERENT:
            successOK = ( rowsCount != nrRowsLimit );
            break;
          case JobEntryWaitForSQL.SUCCESS_CONDITION_ROWS_COUNT_SMALLER:
            successOK = ( rowsCount < nrRowsLimit );
            break;
          case JobEntryWaitForSQL.SUCCESS_CONDITION_ROWS_COUNT_SMALLER_EQUAL:
            successOK = ( rowsCount <= nrRowsLimit );
            break;
          case JobEntryWaitForSQL.SUCCESS_CONDITION_ROWS_COUNT_GREATER:
            successOK = ( rowsCount > nrRowsLimit );
            break;
          case JobEntryWaitForSQL.SUCCESS_CONDITION_ROWS_COUNT_GREATER_EQUAL:
            successOK = ( rowsCount >= nrRowsLimit );
            break;
          default:
            break;
        }
      } // end if countStatement!=null
    } catch ( KettleDatabaseException dbe ) {
      logError( BaseMessages.getString( PKG, "JobEntryWaitForSQL.Error.RunningEntry", dbe.getMessage() ) );
    } finally {
      if ( db != null ) {
        if ( isAddRowsResult && iscustomSQL && ar != null ) {
          rowMeta = db.getQueryFields( countStatement, false );
        }
        db.disconnect();
      }
    }

    if ( successOK ) {
      // ad rows to result
      if ( isAddRowsResult && iscustomSQL && ar != null ) {
        List<RowMetaAndData> rows = new ArrayList<RowMetaAndData>();
        for ( int i = 0; i < ar.size(); i++ ) {
          rows.add( new RowMetaAndData( rowMeta, ar.get( i ) ) );
        }
        if ( rows != null ) {
          result.getRows().addAll( rows );
        }
      }
    }
    return successOK;

  }

  @Override
  public DatabaseMeta[] getUsedDatabaseConnections() {
    return new DatabaseMeta[] { connection, };
  }

  @Override
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

}
