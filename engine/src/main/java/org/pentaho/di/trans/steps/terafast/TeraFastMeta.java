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

package org.pentaho.di.trans.steps.terafast;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.AbstractStepMeta;
import org.pentaho.di.core.util.BooleanPluginProperty;
import org.pentaho.di.core.util.GenericStepData;
import org.pentaho.di.core.util.IntegerPluginProperty;
import org.pentaho.di.core.util.PluginMessages;
import org.pentaho.di.core.util.StringListPluginProperty;
import org.pentaho.di.core.util.StringPluginProperty;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.metastore.api.IMetaStore;

/**
 * @author <a href="mailto:michael.gugerell@aschauer-edv.at">Michael Gugerell(asc145)</a>
 *
 */
public class TeraFastMeta extends AbstractStepMeta {

  public static final PluginMessages MESSAGES = PluginMessages.getMessages( TeraFastMeta.class );

  /**
   * Default fast load path.
   */
  public static final String DEFAULT_FASTLOAD_PATH = "/usr/bin/fastload";

  /**
   * Default data file.
   */
  public static final String DEFAULT_DATA_FILE = "${Internal.Step.CopyNr}.dat";

  public static final String DEFAULT_TARGET_TABLE = "${TARGET_TABLE}_${RUN_ID}";

  /**
   * Default session.
   */
  public static final int DEFAULT_SESSIONS = 2;

  public static final boolean DEFAULT_TRUNCATETABLE = true;

  public static final boolean DEFAULT_VARIABLE_SUBSTITUTION = true;

  /**
   * Default error limit.
   */
  public static final int DEFAULT_ERROR_LIMIT = 25;

  /* custom xml values */
  private static final String FASTLOADPATH = "fastload_path";

  private static final String CONTROLFILE = "controlfile_path";

  private static final String DATAFILE = "datafile_path";

  private static final String LOGFILE = "logfile_path";

  private static final String SESSIONS = "sessions";

  private static final String ERRORLIMIT = "error_limit";

  private static final String USECONTROLFILE = "use_control_file";

  private static final String TARGETTABLE = "target_table";

  private static final String TRUNCATETABLE = "truncate_table";

  private static final String TABLE_FIELD_LIST = "table_field_list";

  private static final String STREAM_FIELD_LIST = "stream_field_list";

  private static final String VARIABLE_SUBSTITUTION = "variable_substitution";

  /** available options. **/
  private StringPluginProperty fastloadPath;

  private StringPluginProperty controlFile;

  private StringPluginProperty dataFile;

  private StringPluginProperty logFile;

  private IntegerPluginProperty sessions;

  private IntegerPluginProperty errorLimit;

  private BooleanPluginProperty useControlFile;

  private BooleanPluginProperty variableSubstitution;

  private BooleanPluginProperty truncateTable;

  private StringPluginProperty targetTable;

  private StringListPluginProperty tableFieldList;

  private StringListPluginProperty streamFieldList;

  /**
     *
     */
  public TeraFastMeta() {
    super();
    this.fastloadPath = this.getPropertyFactory().createString( FASTLOADPATH );
    this.controlFile = this.getPropertyFactory().createString( CONTROLFILE );
    this.dataFile = this.getPropertyFactory().createString( DATAFILE );
    this.logFile = this.getPropertyFactory().createString( LOGFILE );
    this.sessions = this.getPropertyFactory().createInteger( SESSIONS );
    this.errorLimit = this.getPropertyFactory().createInteger( ERRORLIMIT );
    this.targetTable = this.getPropertyFactory().createString( TARGETTABLE );
    this.useControlFile = this.getPropertyFactory().createBoolean( USECONTROLFILE );
    this.truncateTable = this.getPropertyFactory().createBoolean( TRUNCATETABLE );
    this.tableFieldList = this.getPropertyFactory().createStringList( TABLE_FIELD_LIST );
    this.streamFieldList = this.getPropertyFactory().createStringList( STREAM_FIELD_LIST );
    this.variableSubstitution = this.getPropertyFactory().createBoolean( VARIABLE_SUBSTITUTION );
  }

  /**
   * {@inheritDoc}
   *
   * @see org.pentaho.di.trans.step.StepMetaInterface#check(java.util.List, org.pentaho.di.trans.TransMeta,
   *      org.pentaho.di.trans.step.StepMeta, org.pentaho.di.core.row.RowMetaInterface, java.lang.String[],
   *      java.lang.String[], org.pentaho.di.core.row.RowMetaInterface)
   */
  public void check( final List<CheckResultInterface> remarks, final TransMeta transmeta, final StepMeta stepMeta,
    final RowMetaInterface prev, final String[] input, final String[] output, final RowMetaInterface info,
    VariableSpace space, Repository repository, IMetaStore metaStore ) {
    CheckResult checkResult;
    try {
      RowMetaInterface tableFields = getRequiredFields( transmeta );
      checkResult =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, MESSAGES
          .getString( "TeraFastMeta.Message.ConnectionEstablished" ), stepMeta );
      remarks.add( checkResult );

      checkResult =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, MESSAGES
          .getString( "TeraFastMeta.Message.TableExists" ), stepMeta );
      remarks.add( checkResult );

      boolean error = false;
      for ( String field : this.tableFieldList.getValue() ) {
        if ( tableFields.searchValueMeta( field ) == null ) {
          error = true;
          checkResult =
            new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, MESSAGES
              .getString( "TeraFastMeta.Exception.TableFieldNotFound" ), stepMeta );
          remarks.add( checkResult );
        }
      }
      if ( !error ) {
        checkResult =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, MESSAGES
            .getString( "TeraFastMeta.Message.AllTableFieldsFound" ), stepMeta );
        remarks.add( checkResult );
      }
      if ( prev != null && prev.size() > 0 ) {
        // step mode. step receiving input
        checkResult =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, MESSAGES
            .getString( "TeraFastMeta.Message.StepInputDataFound" ), stepMeta );
        remarks.add( checkResult );

        error = false;
        for ( String field : this.streamFieldList.getValue() ) {
          if ( prev.searchValueMeta( field ) == null ) {
            error = true;
            checkResult =
              new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, MESSAGES
                .getString( "TeraFastMeta.Exception.StreamFieldNotFound" ), stepMeta );
            remarks.add( checkResult );
          }
        }
        if ( !error ) {
          checkResult =
            new CheckResult( CheckResultInterface.TYPE_RESULT_OK, MESSAGES
              .getString( "TeraFastMeta.Message.AllStreamFieldsFound" ), stepMeta );
          remarks.add( checkResult );
        }
      }
      // else { job mode. no input rows. pentaho doesn't seem to allow to check jobs. Default Warning: Step is not
      // in transformation.
    } catch ( KettleDatabaseException e ) {
      checkResult =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, MESSAGES
          .getString( "TeraFastMeta.Exception.ConnectionFailed" ), stepMeta );
      remarks.add( checkResult );
    } catch ( KettleException e ) {
      checkResult = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, e.getMessage(), stepMeta );
      remarks.add( checkResult );
    }
  }

  /**
   * @param space
   *          the variableSpace to be used.
   * @return the quoted and resolved schema table name.
   * @throws KettleException
   *           if no table specified.
   */
  public String getQuotedSchemaTableName( final VariableSpace space ) throws KettleException {
    final String realTableName = space.environmentSubstitute( this.targetTable.getValue() );
    if ( StringUtils.isEmpty( realTableName ) ) {
      throw new KettleException( MESSAGES.getString( "TeraFastMeta.Exception.TableNotSpecified" ) );
    }
    return this.getDbMeta().getQuotedSchemaTableCombination( StringUtils.EMPTY, realTableName );
  }

  /**
   * @return the database.
   * @throws KettleException
   *           if an error occurs.
   */
  public Database connectToDatabase() throws KettleException {
    if ( this.getDbMeta() != null ) {
      Database db = new Database( loggingObject, this.getDbMeta() );
      db.connect();
      return db;
    }
    throw new KettleException( MESSAGES.getString( "TeraFastMeta.Exception.ConnectionNotDefined" ) );
  }

  /**
   * {@inheritDoc}
   *
   * @see org.pentaho.di.trans.step.StepMetaInterface#getStep(org.pentaho.di.trans.step.StepMeta,
   *      org.pentaho.di.trans.step.StepDataInterface, int, org.pentaho.di.trans.TransMeta, org.pentaho.di.trans.Trans)
   */
  public StepInterface getStep( final StepMeta stepMeta, final StepDataInterface stepDataInterface, final int cnr,
    final TransMeta transMeta, final Trans disp ) {
    return new TeraFast( stepMeta, stepDataInterface, cnr, transMeta, disp );
  }

  /**
   * {@inheritDoc}
   *
   * @see org.pentaho.di.trans.step.StepMetaInterface#getStepData()
   */
  @Override
  public StepDataInterface getStepData() {
    return new GenericStepData();
  }

  /**
   * {@inheritDoc}
   *
   * @see org.pentaho.di.trans.step.StepMetaInterface#setDefault()
   */
  public void setDefault() {
    this.fastloadPath.setValue( DEFAULT_FASTLOAD_PATH );
    this.dataFile.setValue( DEFAULT_DATA_FILE );
    this.sessions.setValue( DEFAULT_SESSIONS );
    this.errorLimit.setValue( DEFAULT_ERROR_LIMIT );
    this.truncateTable.setValue( DEFAULT_TRUNCATETABLE );
    this.variableSubstitution.setValue( DEFAULT_VARIABLE_SUBSTITUTION );
    this.targetTable.setValue( DEFAULT_TARGET_TABLE );
    this.useControlFile.setValue( true );
  }

  /**
   * {@inheritDoc}
   *
   * @see org.pentaho.di.trans.step.BaseStepMeta#getFields(org.pentaho.di.core.row.RowMetaInterface, java.lang.String,
   *      org.pentaho.di.core.row.RowMetaInterface[], org.pentaho.di.trans.step.StepMeta,
   *      org.pentaho.di.core.variables.VariableSpace)
   */
  @Override
  public void getFields( final RowMetaInterface inputRowMeta, final String name, final RowMetaInterface[] info,
    final StepMeta nextStep, final VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    // Default: nothing changes to rowMeta
  }

  /**
   * {@inheritDoc}
   *
   * @see org.pentaho.di.trans.step.BaseStepMeta#getRequiredFields(org.pentaho.di.core.variables.VariableSpace)
   */
  @Override
  public RowMetaInterface getRequiredFields( final VariableSpace space ) throws KettleException {
    if ( !this.useControlFile.getValue() ) {
      final Database database = connectToDatabase();
      database.shareVariablesWith( space );

      final String schemaTable = getQuotedSchemaTableName( space );
      RowMetaInterface fields = database.getTableFields( schemaTable );
      database.disconnect();
      if ( fields == null ) {
        throw new KettleException( MESSAGES.getString( "TeraFastMeta.Exception.TableNotFound" ) );
      }
      return fields;
    }
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.pentaho.di.trans.step.BaseStepMeta#clone()
   */
  @Override
  public Object clone() {
    return super.clone();
  }

  /**
   * @return the fastloadPath
   */
  public StringPluginProperty getFastloadPath() {
    return this.fastloadPath;
  }

  /**
   * @param fastloadPath
   *          the fastloadPath to set
   */
  public void setFastloadPath( final StringPluginProperty fastloadPath ) {
    this.fastloadPath = fastloadPath;
  }

  /**
   * @return the controlFile
   */
  public StringPluginProperty getControlFile() {
    return this.controlFile;
  }

  /**
   * @param controlFile
   *          the controlFile to set
   */
  public void setControlFile( final StringPluginProperty controlFile ) {
    this.controlFile = controlFile;
  }

  /**
   * @return the dataFile
   */
  public StringPluginProperty getDataFile() {
    return this.dataFile;
  }

  /**
   * @param dataFile
   *          the dataFile to set
   */
  public void setDataFile( final StringPluginProperty dataFile ) {
    this.dataFile = dataFile;
  }

  /**
   * @return the logFile
   */
  public StringPluginProperty getLogFile() {
    return this.logFile;
  }

  /**
   * @param logFile
   *          the logFile to set
   */
  public void setLogFile( final StringPluginProperty logFile ) {
    this.logFile = logFile;
  }

  /**
   * @return the sessions
   */
  public IntegerPluginProperty getSessions() {
    return this.sessions;
  }

  /**
   * @param sessions
   *          the sessions to set
   */
  public void setSessions( final IntegerPluginProperty sessions ) {
    this.sessions = sessions;
  }

  /**
   * @return the errorLimit
   */
  public IntegerPluginProperty getErrorLimit() {
    return this.errorLimit;
  }

  /**
   * @param errorLimit
   *          the errorLimit to set
   */
  public void setErrorLimit( final IntegerPluginProperty errorLimit ) {
    this.errorLimit = errorLimit;
  }

  /**
   * @return the useControlFile
   */
  public BooleanPluginProperty getUseControlFile() {
    return this.useControlFile;
  }

  /**
   * @param useControlFile
   *          the useControlFile to set
   */
  public void setUseControlFile( final BooleanPluginProperty useControlFile ) {
    this.useControlFile = useControlFile;
  }

  /**
   * @return the targetTable
   */
  public StringPluginProperty getTargetTable() {
    return this.targetTable;
  }

  /**
   * @param targetTable
   *          the targetTable to set
   */
  public void setTargetTable( final StringPluginProperty targetTable ) {
    this.targetTable = targetTable;
  }

  /**
   * @return the truncateTable
   */
  public BooleanPluginProperty getTruncateTable() {
    return this.truncateTable;
  }

  /**
   * @param truncateTable
   *          the truncateTable to set
   */
  public void setTruncateTable( final BooleanPluginProperty truncateTable ) {
    this.truncateTable = truncateTable;
  }

  /**
   * @return the tableFieldList
   */
  public StringListPluginProperty getTableFieldList() {
    return this.tableFieldList;
  }

  /**
   * @param tableFieldList
   *          the tableFieldList to set
   */
  public void setTableFieldList( final StringListPluginProperty tableFieldList ) {
    this.tableFieldList = tableFieldList;
  }

  /**
   * @return the streamFieldList
   */
  public StringListPluginProperty getStreamFieldList() {
    return this.streamFieldList;
  }

  /**
   * @param streamFieldList
   *          the streamFieldList to set
   */
  public void setStreamFieldList( final StringListPluginProperty streamFieldList ) {
    this.streamFieldList = streamFieldList;
  }

  /**
   * @return the variableSubstitution
   */
  public BooleanPluginProperty getVariableSubstitution() {
    return this.variableSubstitution;
  }

  /**
   * @param variableSubstitution
   *          the variableSubstitution to set
   */
  public void setVariableSubstitution( BooleanPluginProperty variableSubstitution ) {
    this.variableSubstitution = variableSubstitution;
  }

}
