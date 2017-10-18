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

package org.pentaho.di.trans.steps.sql;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.DatabaseImpact;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/*******************************************************************************
 * Contains meta-data to execute arbitrary SQL, optionally each row again.
 *
 * Created on 10-sep-2005
 */

public class ExecSQLMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = ExecSQLMeta.class; // for i18n purposes, needed by Translator2!!

  private DatabaseMeta databaseMeta;

  private String sql;

  private boolean executedEachInputRow;

  private String[] arguments;

  private String updateField;

  private String insertField;

  private String deleteField;

  private String readField;

  private boolean singleStatement;

  private boolean replaceVariables;

  private boolean quoteString;

  private boolean setParams;

  public ExecSQLMeta() {
    super();
  }

  /**
   * @return Returns the true if we have to set params.
   */
  public boolean isParams() {
    return this.setParams;
  }

  /**
   * @param value
   *          set true if we have to set params.
   */
  public void setParams( boolean value ) {
    this.setParams = value;
  }

  /**
   * @return Returns the database.
   */
  public DatabaseMeta getDatabaseMeta() {
    return databaseMeta;
  }

  /**
   * @param database
   *          The database to set.
   */
  public void setDatabaseMeta( DatabaseMeta database ) {
    this.databaseMeta = database;
  }

  /**
   * @return Returns the sql.
   */
  public String getSql() {
    return sql;
  }

  /**
   * @param sql
   *          The sql to set.
   */
  public void setSql( String sql ) {
    this.sql = sql;
  }

  /**
   * @return Returns the arguments.
   */
  public String[] getArguments() {
    return arguments;
  }

  /**
   * @param arguments
   *          The arguments to set.
   */
  public void setArguments( String[] arguments ) {
    this.arguments = arguments;
  }

  /**
   * @return Returns the executedEachInputRow.
   */
  public boolean isExecutedEachInputRow() {
    return executedEachInputRow;
  }

  /**
   * @param executedEachInputRow
   *          The executedEachInputRow to set.
   */
  public void setExecutedEachInputRow( boolean executedEachInputRow ) {
    this.executedEachInputRow = executedEachInputRow;
  }

  /**
   * @return Returns the deleteField.
   */
  public String getDeleteField() {
    return deleteField;
  }

  /**
   * @param deleteField
   *          The deleteField to set.
   */
  public void setDeleteField( String deleteField ) {
    this.deleteField = deleteField;
  }

  /**
   * @return Returns the insertField.
   */
  public String getInsertField() {
    return insertField;
  }

  /**
   * @param insertField
   *          The insertField to set.
   */
  public void setInsertField( String insertField ) {
    this.insertField = insertField;
  }

  /**
   * @return Returns the readField.
   */
  public String getReadField() {
    return readField;
  }

  /**
   * @param readField
   *          The readField to set.
   */
  public void setReadField( String readField ) {
    this.readField = readField;
  }

  /**
   * @return Returns the updateField.
   */
  public String getUpdateField() {
    return updateField;
  }

  /**
   * @param updateField
   *          The updateField to set.
   */
  public void setUpdateField( String updateField ) {
    this.updateField = updateField;
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode, databases );
  }

  public Object clone() {
    ExecSQLMeta retval = (ExecSQLMeta) super.clone();
    int nrArgs = arguments.length;
    retval.allocate( nrArgs );
    System.arraycopy( arguments, 0, retval.arguments, 0, nrArgs );
    return retval;
  }

  public void allocate( int nrargs ) {
    arguments = new String[nrargs];
  }

  private void readData( Node stepnode, List<? extends SharedObjectInterface> databases ) throws KettleXMLException {
    try {
      String con = XMLHandler.getTagValue( stepnode, "connection" );
      databaseMeta = DatabaseMeta.findDatabase( databases, con );
      String eachRow = XMLHandler.getTagValue( stepnode, "execute_each_row" );
      executedEachInputRow = "Y".equalsIgnoreCase( eachRow );
      singleStatement = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "single_statement" ) );
      replaceVariables = "Y".equals( XMLHandler.getTagValue( stepnode, "replace_variables" ) );
      quoteString = "Y".equals( XMLHandler.getTagValue( stepnode, "quoteString" ) );
      setParams = "Y".equals( XMLHandler.getTagValue( stepnode, "set_params" ) );
      sql = XMLHandler.getTagValue( stepnode, "sql" );

      insertField = XMLHandler.getTagValue( stepnode, "insert_field" );
      updateField = XMLHandler.getTagValue( stepnode, "update_field" );
      deleteField = XMLHandler.getTagValue( stepnode, "delete_field" );
      readField = XMLHandler.getTagValue( stepnode, "read_field" );

      Node argsnode = XMLHandler.getSubNode( stepnode, "arguments" );
      int nrArguments = XMLHandler.countNodes( argsnode, "argument" );
      allocate( nrArguments );
      for ( int i = 0; i < nrArguments; i++ ) {
        Node argnode = XMLHandler.getSubNodeByNr( argsnode, "argument", i );
        arguments[i] = XMLHandler.getTagValue( argnode, "name" );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "ExecSQLMeta.Exception.UnableToLoadStepInfoFromXML" ), e );
    }
  }

  public void setDefault() {
    databaseMeta = null;
    sql = "";
    arguments = new String[0];
  }

  public void getFields( RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    RowMetaAndData add =
      ExecSQL.getResultRow( new Result(), getUpdateField(), getInsertField(), getDeleteField(), getReadField() );

    r.mergeRowMeta( add.getRowMeta() );
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    retval
      .append( "    " ).append(
        XMLHandler.addTagValue( "connection", databaseMeta == null ? "" : databaseMeta.getName() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "execute_each_row", executedEachInputRow ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "single_statement", singleStatement ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "replace_variables", replaceVariables ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "quoteString", quoteString ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "sql", sql ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "set_params", setParams ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "insert_field", insertField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "update_field", updateField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "delete_field", deleteField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "read_field", readField ) );

    retval.append( "    <arguments>" ).append( Const.CR );
    for ( int i = 0; i < arguments.length; i++ ) {
      retval
        .append( "       <argument>" ).append( XMLHandler.addTagValue( "name", arguments[i], false ) ).append(
          "</argument>" ).append( Const.CR );
    }
    retval.append( "    </arguments>" ).append( Const.CR );

    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      databaseMeta = rep.loadDatabaseMetaFromStepAttribute( id_step, "id_connection", databases );
      executedEachInputRow = rep.getStepAttributeBoolean( id_step, "execute_each_row" );
      singleStatement = rep.getStepAttributeBoolean( id_step, "single_statement" );
      replaceVariables = rep.getStepAttributeBoolean( id_step, "replace_variables" );
      quoteString = rep.getStepAttributeBoolean( id_step, "quoteString" );
      sql = rep.getStepAttributeString( id_step, "sql" );
      setParams = rep.getStepAttributeBoolean( id_step, "set_params" );
      insertField = rep.getStepAttributeString( id_step, "insert_field" );
      updateField = rep.getStepAttributeString( id_step, "update_field" );
      deleteField = rep.getStepAttributeString( id_step, "delete_field" );
      readField = rep.getStepAttributeString( id_step, "read_field" );

      int nrargs = rep.countNrStepAttributes( id_step, "arg_name" );
      allocate( nrargs );

      for ( int i = 0; i < nrargs; i++ ) {
        arguments[i] = rep.getStepAttributeString( id_step, i, "arg_name" );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "ExecSQLMeta.Exception.UnexpectedErrorReadingStepInfo" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveDatabaseMetaStepAttribute( id_transformation, id_step, "id_connection", databaseMeta );
      rep.saveStepAttribute( id_transformation, id_step, "sql", sql );
      rep.saveStepAttribute( id_transformation, id_step, "execute_each_row", executedEachInputRow );
      rep.saveStepAttribute( id_transformation, id_step, "single_statement", singleStatement );
      rep.saveStepAttribute( id_transformation, id_step, "replace_variables", replaceVariables );
      rep.saveStepAttribute( id_transformation, id_step, "quoteString", quoteString );
      rep.saveStepAttribute( id_transformation, id_step, "set_params", setParams );
      rep.saveStepAttribute( id_transformation, id_step, "insert_field", insertField );
      rep.saveStepAttribute( id_transformation, id_step, "update_field", updateField );
      rep.saveStepAttribute( id_transformation, id_step, "delete_field", deleteField );
      rep.saveStepAttribute( id_transformation, id_step, "read_field", readField );

      // Also, save the step-database relationship!
      if ( databaseMeta != null ) {
        rep.insertStepDatabase( id_transformation, id_step, databaseMeta.getObjectId() );
      }

      for ( int i = 0; i < arguments.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "arg_name", arguments[i] );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "ExecSQLMeta.Exception.UnableToSaveStepInfo" )
        + id_step, e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;

    if ( databaseMeta != null ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "ExecSQLMeta.CheckResult.ConnectionExists" ), stepMeta );
      remarks.add( cr );

      Database db = new Database( loggingObject, databaseMeta );
      db.shareVariablesWith( transMeta );
      databases = new Database[] { db }; // keep track of it for
      // cancelling purposes...

      try {
        db.connect();
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "ExecSQLMeta.CheckResult.DBConnectionOK" ), stepMeta );
        remarks.add( cr );

        if ( sql != null && sql.length() != 0 ) {
          cr =
            new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
              PKG, "ExecSQLMeta.CheckResult.SQLStatementEntered" ), stepMeta );
          remarks.add( cr );
        } else {
          cr =
            new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
              PKG, "ExecSQLMeta.CheckResult.SQLStatementMissing" ), stepMeta );
          remarks.add( cr );
        }
      } catch ( KettleException e ) {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "ExecSQLMeta.CheckResult.ErrorOccurred" )
            + e.getMessage(), stepMeta );
        remarks.add( cr );
      } finally {
        db.disconnect();
      }
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "ExecSQLMeta.CheckResult.ConnectionNeeded" ), stepMeta );
      remarks.add( cr );
    }

    // If it's executed each row, make sure we have input
    if ( executedEachInputRow ) {
      if ( input.length > 0 ) {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "ExecSQLMeta.CheckResult.StepReceivingInfoOK" ), stepMeta );
        remarks.add( cr );
      } else {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "ExecSQLMeta.CheckResult.NoInputReceivedError" ), stepMeta );
        remarks.add( cr );
      }
    } else {
      if ( input.length > 0 ) {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "ExecSQLMeta.CheckResult.SQLOnlyExecutedOnce" ), stepMeta );
        remarks.add( cr );
      } else {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "ExecSQLMeta.CheckResult.InputReceivedOKForSQLOnlyExecuteOnce" ), stepMeta );
        remarks.add( cr );
      }
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new ExecSQL( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new ExecSQLData();
  }

  public void analyseImpact( List<DatabaseImpact> impact, TransMeta transMeta, StepMeta stepMeta, RowMeta prev,
    String[] input, String[] output, RowMeta info ) throws KettleStepException {
    DatabaseImpact ii =
      new DatabaseImpact(
        DatabaseImpact.TYPE_IMPACT_READ_WRITE, transMeta.getName(), stepMeta.getName(),
        databaseMeta.getDatabaseName(),
        BaseMessages.getString( PKG, "ExecSQLMeta.DatabaseMeta.Unknown.Label" ),
        BaseMessages.getString( PKG, "ExecSQLMeta.DatabaseMeta.Unknown2.Label" ),
        BaseMessages.getString( PKG, "ExecSQLMeta.DatabaseMeta.Unknown3.Label" ), stepMeta.getName(), sql,
        BaseMessages.getString( PKG, "ExecSQLMeta.DatabaseMeta.Title" ) );
    impact.add( ii );
  }

  public DatabaseMeta[] getUsedDatabaseConnections() {
    if ( databaseMeta != null ) {
      return new DatabaseMeta[] { databaseMeta };
    } else {
      return super.getUsedDatabaseConnections();
    }
  }

  /**
   * @return Returns the variableReplacementActive.
   */
  public boolean isReplaceVariables() {
    return replaceVariables;
  }

  /**
   * @param variableReplacementActive
   *          The variableReplacementActive to set.
   */
  public void setVariableReplacementActive( boolean variableReplacementActive ) {
    this.replaceVariables = variableReplacementActive;
  }

  public boolean isQuoteString() {
    return quoteString;
  }

  public void setQuoteString( boolean quoteString ) {
    this.quoteString = quoteString;
  }

  public boolean supportsErrorHandling() {
    return true;
  }

  /**
   * @return the singleStatement
   */
  public boolean isSingleStatement() {
    return singleStatement;
  }

  /**
   * @param singleStatement
   *          the singleStatement to set
   */
  public void setSingleStatement( boolean singleStatement ) {
    this.singleStatement = singleStatement;
  }
}
