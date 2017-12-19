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

package org.pentaho.di.trans.steps.execsqlrow;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.sql.ExecSQL;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/***
 * Contains meta-data to execute arbitrary SQL from a specified field.
 *
 * Created on 10-sep-2008
 */

public class ExecSQLRowMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = ExecSQLRowMeta.class; // for i18n purposes, needed by Translator2!!

  private DatabaseMeta databaseMeta;
  private String sqlField;

  private String updateField;
  private String insertField;
  private String deleteField;
  private String readField;

  /** Commit size for inserts/updates */
  private int commitSize;

  private boolean sqlFromfile;

  /** Send SQL as single statement **/
  private boolean sendOneStatement;

  public ExecSQLRowMeta() {
    super();
  }

  /**
   * @return Returns the sqlFromfile.
   */
  public boolean IsSendOneStatement() {
    return sendOneStatement;
  }

  /**
   * @param sendOneStatement
   *          The sendOneStatement to set.
   */
  public void SetSendOneStatement( boolean sendOneStatement ) {
    this.sendOneStatement = sendOneStatement;
  }

  /**
   * @return Returns the sqlFromfile.
   */
  public boolean isSqlFromfile() {
    return sqlFromfile;
  }

  /**
   * @param sqlFromfile
   *          The sqlFromfile to set.
   */
  public void setSqlFromfile( boolean sqlFromfile ) {
    this.sqlFromfile = sqlFromfile;
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
   * @return Returns the sqlField.
   */
  public String getSqlFieldName() {
    return sqlField;
  }

  /**
   * @param sql
   *          The sqlField to sqlField.
   */
  public void setSqlFieldName( String sqlField ) {
    this.sqlField = sqlField;
  }

  /**
   * @return Returns the commitSize.
   */
  public int getCommitSize() {
    return commitSize;
  }

  /**
   * @param commitSize
   *          The commitSize to set.
   */
  public void setCommitSize( int commitSize ) {
    this.commitSize = commitSize;
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
    ExecSQLRowMeta retval = (ExecSQLRowMeta) super.clone();
    return retval;
  }

  private void readData( Node stepnode, List<? extends SharedObjectInterface> databases ) throws KettleXMLException {
    try {
      String csize;
      String con = XMLHandler.getTagValue( stepnode, "connection" );
      databaseMeta = DatabaseMeta.findDatabase( databases, con );
      csize = XMLHandler.getTagValue( stepnode, "commit" );
      commitSize = Const.toInt( csize, 0 );
      sqlField = XMLHandler.getTagValue( stepnode, "sql_field" );

      insertField = XMLHandler.getTagValue( stepnode, "insert_field" );
      updateField = XMLHandler.getTagValue( stepnode, "update_field" );
      deleteField = XMLHandler.getTagValue( stepnode, "delete_field" );
      readField = XMLHandler.getTagValue( stepnode, "read_field" );
      sqlFromfile = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "sqlFromfile" ) );

      sendOneStatement =
        "Y".equalsIgnoreCase( Const.NVL( XMLHandler.getTagValue( stepnode, "sendOneStatement" ), "Y" ) );
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "ExecSQLRowMeta.Exception.UnableToLoadStepInfoFromXML" ), e );
    }
  }

  public void setDefault() {
    sqlFromfile = false;
    commitSize = 1;
    databaseMeta = null;
    sqlField = null;
    sendOneStatement = true;
  }

  public void getFields( RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    RowMetaAndData add =
      ExecSQL.getResultRow( new Result(), getUpdateField(), getInsertField(), getDeleteField(), getReadField() );

    r.mergeRowMeta( add.getRowMeta() );
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );
    retval.append( "    " ).append( XMLHandler.addTagValue( "commit", commitSize ) );
    retval
      .append( "    " ).append(
        XMLHandler.addTagValue( "connection", databaseMeta == null ? "" : databaseMeta.getName() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "sql_field", sqlField ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "insert_field", insertField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "update_field", updateField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "delete_field", deleteField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "read_field", readField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "sqlFromfile", sqlFromfile ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "sendOneStatement", sendOneStatement ) );
    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      databaseMeta = rep.loadDatabaseMetaFromStepAttribute( id_step, "id_connection", databases );
      commitSize = (int) rep.getStepAttributeInteger( id_step, "commit" );
      sqlField = rep.getStepAttributeString( id_step, "sql_field" );

      insertField = rep.getStepAttributeString( id_step, "insert_field" );
      updateField = rep.getStepAttributeString( id_step, "update_field" );
      deleteField = rep.getStepAttributeString( id_step, "delete_field" );
      readField = rep.getStepAttributeString( id_step, "read_field" );
      sqlFromfile = rep.getStepAttributeBoolean( id_step, "sqlFromfile" );

      String sendOneStatementString = rep.getStepAttributeString( id_step, "sendOneStatement" );
      if ( Utils.isEmpty( sendOneStatementString ) ) {
        sendOneStatement = true;
      } else {
        sendOneStatement = rep.getStepAttributeBoolean( id_step, "sendOneStatement" );
      }

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "ExecSQLRowMeta.Exception.UnexpectedErrorReadingStepInfo" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveDatabaseMetaStepAttribute( id_transformation, id_step, "id_connection", databaseMeta );
      rep.saveStepAttribute( id_transformation, id_step, "commit", commitSize );
      rep.saveStepAttribute( id_transformation, id_step, "sql_field", sqlField );

      rep.saveStepAttribute( id_transformation, id_step, "insert_field", insertField );
      rep.saveStepAttribute( id_transformation, id_step, "update_field", updateField );
      rep.saveStepAttribute( id_transformation, id_step, "delete_field", deleteField );
      rep.saveStepAttribute( id_transformation, id_step, "read_field", readField );

      // Also, save the step-database relationship!
      if ( databaseMeta != null ) {
        rep.insertStepDatabase( id_transformation, id_step, databaseMeta.getObjectId() );
      }

      rep.saveStepAttribute( id_transformation, id_step, "sqlFromfile", sqlFromfile );
      rep.saveStepAttribute( id_transformation, id_step, "sendOneStatement", sendOneStatement );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "ExecSQLRowMeta.Exception.UnableToSaveStepInfo" )
        + id_step, e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;

    if ( databaseMeta != null ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "ExecSQLRowMeta.CheckResult.ConnectionExists" ), stepMeta );
      remarks.add( cr );

      Database db = new Database( loggingObject, databaseMeta );
      databases = new Database[] { db }; // keep track of it for cancelling purposes...

      try {
        db.connect();
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "ExecSQLRowMeta.CheckResult.DBConnectionOK" ), stepMeta );
        remarks.add( cr );

        if ( sqlField != null && sqlField.length() != 0 ) {
          cr =
            new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
              PKG, "ExecSQLRowMeta.CheckResult.SQLFieldNameEntered" ), stepMeta );
        } else {
          cr =
            new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
              PKG, "ExecSQLRowMeta.CheckResult.SQLFieldNameMissing" ), stepMeta );
        }
        remarks.add( cr );
      } catch ( KettleException e ) {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "ExecSQLRowMeta.CheckResult.ErrorOccurred" )
            + e.getMessage(), stepMeta );
        remarks.add( cr );
      } finally {
        db.disconnect();
      }
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "ExecSQLRowMeta.CheckResult.ConnectionNeeded" ), stepMeta );
      remarks.add( cr );
    }

    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "ExecSQLRowMeta.CheckResult.StepReceivingInfoOK" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "ExecSQLRowMeta.CheckResult.NoInputReceivedError" ), stepMeta );
      remarks.add( cr );
    }

  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new ExecSQLRow( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new ExecSQLRowData();
  }

  public DatabaseMeta[] getUsedDatabaseConnections() {
    if ( databaseMeta != null ) {
      return new DatabaseMeta[] { databaseMeta };
    } else {
      return super.getUsedDatabaseConnections();
    }
  }

  public boolean supportsErrorHandling() {
    return true;
  }

}
