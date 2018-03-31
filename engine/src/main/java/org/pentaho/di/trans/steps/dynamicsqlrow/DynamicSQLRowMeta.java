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

package org.pentaho.di.trans.steps.dynamicsqlrow;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
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

public class DynamicSQLRowMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = DynamicSQLRowMeta.class; // for i18n purposes, needed by Translator2!!

  /** database connection */
  private DatabaseMeta databaseMeta;

  /** SQL Statement */
  private String sql;

  private String sqlfieldname;

  /** Number of rows to return (0=ALL) */
  private int rowLimit;

  /**
   * false: don't return rows where nothing is found true: at least return one source row, the rest is NULL
   */
  private boolean outerJoin;

  private boolean replacevars;

  public boolean queryonlyonchange;

  public DynamicSQLRowMeta() {
    super(); // allocate BaseStepMeta
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
   * @return Returns the outerJoin.
   */
  public boolean isOuterJoin() {
    return outerJoin;
  }

  /**
   * @param outerJoin
   *          The outerJoin to set.
   */
  public void setOuterJoin( boolean outerJoin ) {
    this.outerJoin = outerJoin;
  }

  /**
   * @return Returns the replacevars.
   */
  public boolean isVariableReplace() {
    return replacevars;
  }

  /**
   * @param replacevars
   *          The replacevars to set.
   */
  public void setVariableReplace( boolean replacevars ) {
    this.replacevars = replacevars;
  }

  /**
   * @return Returns the queryonlyonchange.
   */
  public boolean isQueryOnlyOnChange() {
    return queryonlyonchange;
  }

  /**
   * @param queryonlyonchange
   *          The queryonlyonchange to set.
   */
  public void setQueryOnlyOnChange( boolean queryonlyonchange ) {
    this.queryonlyonchange = queryonlyonchange;
  }

  /**
   * @return Returns the rowLimit.
   */
  public int getRowLimit() {
    return rowLimit;
  }

  /**
   * @param rowLimit
   *          The rowLimit to set.
   */
  public void setRowLimit( int rowLimit ) {
    this.rowLimit = rowLimit;
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
   * @return Returns the sqlfieldname.
   */
  public String getSQLFieldName() {
    return sqlfieldname;
  }

  /**
   * @param sql
   *          The sqlfieldname to set.
   */
  public void setSQLFieldName( String sqlfieldname ) {
    this.sqlfieldname = sqlfieldname;
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode, databases );
  }

  public Object clone() {
    DynamicSQLRowMeta retval = (DynamicSQLRowMeta) super.clone();

    return retval;
  }

  private void readData( Node stepnode, List<DatabaseMeta> databases ) throws KettleXMLException {
    try {
      String con = XMLHandler.getTagValue( stepnode, "connection" );
      databaseMeta = DatabaseMeta.findDatabase( databases, con );
      sql = XMLHandler.getTagValue( stepnode, "sql" );
      outerJoin = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "outer_join" ) );
      replacevars = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "replace_vars" ) );
      queryonlyonchange = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "query_only_on_change" ) );

      rowLimit = Const.toInt( XMLHandler.getTagValue( stepnode, "rowlimit" ), 0 );
      sqlfieldname = XMLHandler.getTagValue( stepnode, "sql_fieldname" );

    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "DynamicSQLRowMeta.Exception.UnableToLoadStepInfo" ), e );
    }
  }

  public void setDefault() {
    databaseMeta = null;
    rowLimit = 0;
    sql = "";
    outerJoin = false;
    replacevars = false;
    sqlfieldname = null;
    queryonlyonchange = false;
  }

  public void getFields( RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

    if ( databaseMeta == null ) {
      return;
    }

    Database db = new Database( loggingObject, databaseMeta );
    databases = new Database[] { db }; // Keep track of this one for cancelQuery

    // First try without connecting to the database... (can be S L O W)
    // See if it's in the cache...
    RowMetaInterface add = null;
    String realSQL = sql;
    if ( replacevars ) {
      realSQL = space.environmentSubstitute( realSQL );
    }
    try {
      add = db.getQueryFields( realSQL, false );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleStepException( BaseMessages.getString(
        PKG, "DynamicSQLRowMeta.Exception.UnableToDetermineQueryFields" )
        + Const.CR + sql, dbe );
    }

    if ( add != null ) { // Cache hit, just return it this...
      for ( int i = 0; i < add.size(); i++ ) {
        ValueMetaInterface v = add.getValueMeta( i );
        v.setOrigin( name );
      }
      row.addRowMeta( add );
    } else {
      // No cache hit, connect to the database, do it the hard way...
      try {
        db.connect();
        add = db.getQueryFields( realSQL, false );
        for ( int i = 0; i < add.size(); i++ ) {
          ValueMetaInterface v = add.getValueMeta( i );
          v.setOrigin( name );
        }
        row.addRowMeta( add );
        db.disconnect();
      } catch ( KettleDatabaseException dbe ) {
        throw new KettleStepException( BaseMessages.getString(
          PKG, "DynamicSQLRowMeta.Exception.ErrorObtainingFields" ), dbe );
      }
    }
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval
      .append( "    " + XMLHandler.addTagValue( "connection", databaseMeta == null ? "" : databaseMeta.getName() ) );
    retval.append( "    " + XMLHandler.addTagValue( "rowlimit", rowLimit ) );
    retval.append( "    " + XMLHandler.addTagValue( "sql", sql ) );
    retval.append( "    " + XMLHandler.addTagValue( "outer_join", outerJoin ) );
    retval.append( "    " + XMLHandler.addTagValue( "replace_vars", replacevars ) );
    retval.append( "    " + XMLHandler.addTagValue( "sql_fieldname", sqlfieldname ) );
    retval.append( "    " + XMLHandler.addTagValue( "query_only_on_change", queryonlyonchange ) );

    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      databaseMeta = rep.loadDatabaseMetaFromStepAttribute( id_step, "id_connection", databases );
      rowLimit = (int) rep.getStepAttributeInteger( id_step, "rowlimit" );
      sql = rep.getStepAttributeString( id_step, "sql" );
      outerJoin = rep.getStepAttributeBoolean( id_step, "outer_join" );
      replacevars = rep.getStepAttributeBoolean( id_step, "replace_vars" );
      sqlfieldname = rep.getStepAttributeString( id_step, "sql_fieldname" );
      queryonlyonchange = rep.getStepAttributeBoolean( id_step, "query_only_on_change" );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "DynamicSQLRowMeta.Exception.UnexpectedErrorReadingStepInfo" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveDatabaseMetaStepAttribute( id_transformation, id_step, "id_connection", databaseMeta );
      rep.saveStepAttribute( id_transformation, id_step, "rowlimit", rowLimit );
      rep.saveStepAttribute( id_transformation, id_step, "sql", sql );
      rep.saveStepAttribute( id_transformation, id_step, "outer_join", outerJoin );
      rep.saveStepAttribute( id_transformation, id_step, "replace_vars", replacevars );
      rep.saveStepAttribute( id_transformation, id_step, "sql_fieldname", sqlfieldname );
      rep.saveStepAttribute( id_transformation, id_step, "query_only_on_change", queryonlyonchange );

      // Also, save the step-database relationship!
      if ( databaseMeta != null ) {
        rep.insertStepDatabase( id_transformation, id_step, databaseMeta.getObjectId() );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "DynamicSQLRowMeta.Exception.UnableToSaveStepInfo" )
        + id_step, e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;
    String error_message = "";

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "DynamicSQLRowMeta.CheckResult.ReceivingInfo" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "DynamicSQLRowMeta.CheckResult.NoInputReceived" ), stepMeta );
      remarks.add( cr );
    }

    // Check for SQL field
    if ( Utils.isEmpty( sqlfieldname ) ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "DynamicSQLRowMeta.CheckResult.SQLFieldNameMissing" ), stepMeta );
      remarks.add( cr );
    } else {
      ValueMetaInterface vfield = prev.searchValueMeta( sqlfieldname );
      if ( vfield == null ) {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "DynamicSQLRowMeta.CheckResult.SQLFieldNotFound", sqlfieldname ), stepMeta );
      } else {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "DynamicSQLRowMeta.CheckResult.SQLFieldFound", sqlfieldname, vfield.getOrigin() ), stepMeta );
      }
      remarks.add( cr );
    }

    if ( databaseMeta != null ) {
      Database db = new Database( loggingObject, databaseMeta );
      databases = new Database[] { db }; // Keep track of this one for cancelQuery

      try {
        db.connect();
        if ( sql != null && sql.length() != 0 ) {

          error_message = "";

          RowMetaInterface r = db.getQueryFields( sql, true );
          if ( r != null ) {
            cr =
              new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
                PKG, "DynamicSQLRowMeta.CheckResult.QueryOK" ), stepMeta );
            remarks.add( cr );
          } else {
            error_message = BaseMessages.getString( PKG, "DynamicSQLRowMeta.CheckResult.InvalidDBQuery" );
            cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
            remarks.add( cr );
          }
        }
      } catch ( KettleException e ) {
        error_message =
          BaseMessages.getString( PKG, "DynamicSQLRowMeta.CheckResult.ErrorOccurred" ) + e.getMessage();
        cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      } finally {
        db.disconnect();
      }
    } else {
      error_message = BaseMessages.getString( PKG, "DynamicSQLRowMeta.CheckResult.InvalidConnection" );
      cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
    Trans trans ) {
    return new DynamicSQLRow( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  public StepDataInterface getStepData() {
    return new DynamicSQLRowData();
  }

  public void analyseImpact( List<DatabaseImpact> impact, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, Repository repository,
    IMetaStore metaStore ) throws KettleStepException {

    RowMetaInterface out = prev.clone();
    getFields( out, stepMeta.getName(), new RowMetaInterface[] { info, }, null, transMeta, repository, metaStore );
    if ( out != null ) {
      for ( int i = 0; i < out.size(); i++ ) {
        ValueMetaInterface outvalue = out.getValueMeta( i );
        DatabaseImpact di =
          new DatabaseImpact(
            DatabaseImpact.TYPE_IMPACT_READ, transMeta.getName(), stepMeta.getName(), databaseMeta
              .getDatabaseName(), "", outvalue.getName(), outvalue.getName(), stepMeta.getName(), sql,
            BaseMessages.getString( PKG, "DynamicSQLRowMeta.DatabaseImpact.Title" ) );
        impact.add( di );

      }
    }
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
