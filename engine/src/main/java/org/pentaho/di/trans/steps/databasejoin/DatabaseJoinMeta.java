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

package org.pentaho.di.trans.steps.databasejoin;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaNone;
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

public class DatabaseJoinMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = DatabaseJoinMeta.class; // for i18n purposes, needed by Translator2!!

  /** database connection */
  private DatabaseMeta databaseMeta;

  /** SQL Statement */
  private String sql;

  /** Number of rows to return (0=ALL) */
  private int rowLimit;

  /**
   * false: don't return rows where nothing is found true: at least return one source row, the rest is NULL
   */
  private boolean outerJoin;

  /** Fields to use as parameters (fill in the ? markers) */
  private String[] parameterField;

  /** Type of the paramenters */
  private int[] parameterType;

  /**
   * false: don't replave variable in scrip true: replace variable in script
   */
  private boolean replacevars;

  public DatabaseJoinMeta() {
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
   * @return Returns the parameterField.
   */
  public String[] getParameterField() {
    return parameterField;
  }

  /**
   * @param parameterField
   *          The parameterField to set.
   */
  public void setParameterField( String[] parameterField ) {
    this.parameterField = parameterField;
  }

  /**
   * @return Returns the parameterType.
   */
  public int[] getParameterType() {
    return parameterType;
  }

  /**
   * @param parameterType
   *          The parameterType to set.
   */
  public void setParameterType( int[] parameterType ) {
    this.parameterType = parameterType;
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

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    parameterField = null;
    parameterType = null;
    outerJoin = false;
    replacevars = false;
    readData( stepnode, databases );
  }

  public void allocate( int nrparam ) {
    parameterField = new String[nrparam];
    parameterType = new int[nrparam];
  }

  @Override
  public Object clone() {
    DatabaseJoinMeta retval = (DatabaseJoinMeta) super.clone();

    int nrparam = parameterField.length;

    retval.allocate( nrparam );

    System.arraycopy( parameterField, 0, retval.parameterField, 0, nrparam );
    System.arraycopy( parameterType, 0, retval.parameterType, 0, nrparam );

    return retval;
  }

  private void readData( Node stepnode, List<DatabaseMeta> databases ) throws KettleXMLException {
    try {
      String con = XMLHandler.getTagValue( stepnode, "connection" );
      databaseMeta = DatabaseMeta.findDatabase( databases, con );
      sql = XMLHandler.getTagValue( stepnode, "sql" );
      outerJoin = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "outer_join" ) );
      replacevars = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "replace_vars" ) );
      rowLimit = Const.toInt( XMLHandler.getTagValue( stepnode, "rowlimit" ), 0 );

      Node param = XMLHandler.getSubNode( stepnode, "parameter" );
      int nrparam = XMLHandler.countNodes( param, "field" );

      allocate( nrparam );

      for ( int i = 0; i < nrparam; i++ ) {
        Node pnode = XMLHandler.getSubNodeByNr( param, "field", i );
        parameterField[i] = XMLHandler.getTagValue( pnode, "name" );
        String ptype = XMLHandler.getTagValue( pnode, "type" );
        parameterType[i] = ValueMetaFactory.getIdForValueMeta( ptype );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages
        .getString( PKG, "DatabaseJoinMeta.Exception.UnableToLoadStepInfo" ), e );
    }
  }

  @Override
  public void setDefault() {
    databaseMeta = null;
    rowLimit = 0;
    sql = "";
    outerJoin = false;
    parameterField = null;
    parameterType = null;
    outerJoin = false;
    replacevars = false;

    int nrparam = 0;

    allocate( nrparam );

    for ( int i = 0; i < nrparam; i++ ) {
      parameterField[i] = "param" + i;
      parameterType[i] = ValueMetaInterface.TYPE_NUMBER;
    }
  }

  public RowMetaInterface getParameterRow( RowMetaInterface fields ) {
    RowMetaInterface param = new RowMeta();

    if ( fields != null ) {
      for ( int i = 0; i < parameterField.length; i++ ) {
        ValueMetaInterface v = fields.searchValueMeta( parameterField[i] );
        if ( v != null ) {
          param.addValueMeta( v );
        }
      }
    }
    return param;
  }

  @Override
  public void getFields( RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

    if ( databaseMeta == null ) {
      return;
    }

    Database db = new Database( loggingObject, databaseMeta );
    databases = new Database[] { db }; // Keep track of this one for cancelQuery

    // Which fields are parameters?
    // info[0] comes from the database connection.
    //
    RowMetaInterface param = getParameterRow( row );

    // First try without connecting to the database... (can be S L O W)
    // See if it's in the cache...
    //
    RowMetaInterface add = null;
    try {
      add = db.getQueryFields( space.environmentSubstitute( sql ), true, param, new Object[param.size()] );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleStepException( BaseMessages.getString(
        PKG, "DatabaseJoinMeta.Exception.UnableToDetermineQueryFields" )
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
      //
      try {
        db.connect();
        add = db.getQueryFields( space.environmentSubstitute( sql ), true, param, new Object[param.size()] );
        for ( int i = 0; i < add.size(); i++ ) {
          ValueMetaInterface v = add.getValueMeta( i );
          v.setOrigin( name );
        }
        row.addRowMeta( add );
        db.disconnect();
      } catch ( KettleDatabaseException dbe ) {
        throw new KettleStepException( BaseMessages.getString(
          PKG, "DatabaseJoinMeta.Exception.ErrorObtainingFields" ), dbe );
      }
    }
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    retval
      .append( "    " ).append(
        XMLHandler.addTagValue( "connection", databaseMeta == null ? "" : databaseMeta.getName() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "rowlimit", rowLimit ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "sql", sql ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "outer_join", outerJoin ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "replace_vars", replacevars ) );
    retval.append( "    <parameter>" ).append( Const.CR );
    for ( int i = 0; i < parameterField.length; i++ ) {
      retval.append( "      <field>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "name", parameterField[i] ) );
      retval.append( "        " ).append(
        XMLHandler.addTagValue( "type", ValueMetaFactory.getValueMetaName( parameterType[i] ) ) );
      retval.append( "      </field>" ).append( Const.CR );
    }
    retval.append( "    </parameter>" ).append( Const.CR );

    return retval.toString();
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      databaseMeta = rep.loadDatabaseMetaFromStepAttribute( id_step, "id_connection", databases );
      rowLimit = (int) rep.getStepAttributeInteger( id_step, "rowlimit" );
      sql = rep.getStepAttributeString( id_step, "sql" );
      outerJoin = rep.getStepAttributeBoolean( id_step, "outer_join" );
      replacevars = rep.getStepAttributeBoolean( id_step, "replace_vars" );

      int nrparam = rep.countNrStepAttributes( id_step, "parameter_field" );

      allocate( nrparam );

      for ( int i = 0; i < nrparam; i++ ) {
        parameterField[i] = rep.getStepAttributeString( id_step, i, "parameter_field" );
        String stype = rep.getStepAttributeString( id_step, i, "parameter_type" );
        parameterType[i] = ValueMetaFactory.getIdForValueMeta( stype );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "DatabaseJoinMeta.Exception.UnexpectedErrorReadingStepInfo" ), e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveDatabaseMetaStepAttribute( id_transformation, id_step, "id_connection", databaseMeta );
      rep.saveStepAttribute( id_transformation, id_step, "rowlimit", rowLimit );
      rep.saveStepAttribute( id_transformation, id_step, "sql", sql );
      rep.saveStepAttribute( id_transformation, id_step, "outer_join", outerJoin );
      rep.saveStepAttribute( id_transformation, id_step, "replace_vars", replacevars );

      for ( int i = 0; i < parameterField.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "parameter_field", parameterField[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "parameter_type", ValueMetaFactory
            .getValueMetaName( parameterType[i] ) );
      }

      // Also, save the step-database relationship!
      if ( databaseMeta != null ) {
        rep.insertStepDatabase( id_transformation, id_step, databaseMeta.getObjectId() );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "DatabaseJoinMeta.Exception.UnableToSaveStepInfo" )
        + id_step, e );
    }
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {

    CheckResult cr;
    String error_message = "";

    if ( databaseMeta != null ) {
      Database db = new Database( loggingObject, databaseMeta );
      databases = new Database[] { db }; // Keep track of this one for cancelQuery

      try {
        db.connect();
        if ( sql != null && sql.length() != 0 ) {
          RowMetaInterface param = getParameterRow( prev );

          error_message = "";

          RowMetaInterface r =
            db.getQueryFields( transMeta.environmentSubstitute( sql ), true, param, new Object[param.size()] );
          if ( r != null ) {
            cr =
              new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
                PKG, "DatabaseJoinMeta.CheckResult.QueryOK" ), stepMeta );
            remarks.add( cr );
          } else {
            error_message = BaseMessages.getString( PKG, "DatabaseJoinMeta.CheckResult.InvalidDBQuery" );
            cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
            remarks.add( cr );
          }

          int q = db.countParameters( transMeta.environmentSubstitute( sql ) );
          if ( q != parameterField.length ) {
            error_message =
              BaseMessages.getString( PKG, "DatabaseJoinMeta.CheckResult.DismatchBetweenParametersAndQuestion" )
                + Const.CR;
            error_message +=
              BaseMessages.getString( PKG, "DatabaseJoinMeta.CheckResult.DismatchBetweenParametersAndQuestion2" )
                + q + Const.CR;
            error_message +=
              BaseMessages.getString( PKG, "DatabaseJoinMeta.CheckResult.DismatchBetweenParametersAndQuestion3" )
                + parameterField.length;

            cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
            remarks.add( cr );
          } else {
            cr =
              new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
                PKG, "DatabaseJoinMeta.CheckResult.NumberOfParamCorrect" )
                + q + ")", stepMeta );
            remarks.add( cr );
          }
        }

        // Look up fields in the input stream <prev>
        if ( prev != null && prev.size() > 0 ) {
          boolean first = true;
          error_message = "";
          boolean error_found = false;

          for ( int i = 0; i < parameterField.length; i++ ) {
            ValueMetaInterface v = prev.searchValueMeta( parameterField[i] );
            if ( v == null ) {
              if ( first ) {
                first = false;
                error_message +=
                  BaseMessages.getString( PKG, "DatabaseJoinMeta.CheckResult.MissingFields" ) + Const.CR;
              }
              error_found = true;
              error_message += "\t\t" + parameterField[i] + Const.CR;
            }
          }
          if ( error_found ) {
            cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
          } else {
            cr =
              new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
                PKG, "DatabaseJoinMeta.CheckResult.AllFieldsFound" ), stepMeta );
          }
          remarks.add( cr );
        } else {
          error_message =
            BaseMessages.getString( PKG, "DatabaseJoinMeta.CheckResult.CounldNotReadFields" ) + Const.CR;
          cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
          remarks.add( cr );
        }
      } catch ( KettleException e ) {
        error_message =
          BaseMessages.getString( PKG, "DatabaseJoinMeta.CheckResult.ErrorOccurred" ) + e.getMessage();
        cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      } finally {
        db.disconnect();
      }
    } else {
      error_message = BaseMessages.getString( PKG, "DatabaseJoinMeta.CheckResult.InvalidConnection" );
      cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "DatabaseJoinMeta.CheckResult.ReceivingInfo" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "DatabaseJoinMeta.CheckResult.NoInputReceived" ), stepMeta );
      remarks.add( cr );
    }

  }

  @Override
  public RowMetaInterface getTableFields() {
    // Build a dummy parameter row...
    //
    RowMetaInterface param = new RowMeta();
    for ( int i = 0; i < parameterField.length; i++ ) {
      ValueMetaInterface v;
      try {
        v = ValueMetaFactory.createValueMeta( parameterField[i], parameterType[i] );
      } catch ( KettlePluginException e ) {
        v = new ValueMetaNone( parameterField[i] );
      }
      param.addValueMeta( v );
    }

    RowMetaInterface fields = null;
    if ( databaseMeta != null ) {
      Database db = new Database( loggingObject, databaseMeta );
      databases = new Database[] { db }; // Keep track of this one for cancelQuery

      try {
        db.connect();
        fields =
          db.getQueryFields( databaseMeta.environmentSubstitute( sql ), true, param, new Object[param.size()] );
      } catch ( KettleDatabaseException dbe ) {
        logError( BaseMessages.getString( PKG, "DatabaseJoinMeta.Log.DatabaseErrorOccurred" ) + dbe.getMessage() );
      } finally {
        db.disconnect();
      }
    }
    return fields;
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
    Trans trans ) {
    return new DatabaseJoin( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new DatabaseJoinData();
  }

  @Override
  public void analyseImpact( List<DatabaseImpact> impact, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, Repository repository,
    IMetaStore metaStore ) throws KettleStepException {

    // Find the lookupfields...
    //
    RowMetaInterface out = prev.clone();
    getFields( out, stepMeta.getName(), new RowMetaInterface[] { info, }, null, transMeta, repository, metaStore );

    if ( out != null ) {
      for ( int i = 0; i < out.size(); i++ ) {
        ValueMetaInterface outvalue = out.getValueMeta( i );
        DatabaseImpact di =
          new DatabaseImpact(
            DatabaseImpact.TYPE_IMPACT_READ, transMeta.getName(), stepMeta.getName(),
            databaseMeta.getDatabaseName(), "", outvalue.getName(), outvalue.getName(), stepMeta.getName(),
            transMeta.environmentSubstitute( sql ),
            BaseMessages.getString( PKG, "DatabaseJoinMeta.DatabaseImpact.Title" ) );
        impact.add( di );

      }
    }
  }

  @Override
  public DatabaseMeta[] getUsedDatabaseConnections() {
    if ( databaseMeta != null ) {
      return new DatabaseMeta[] { databaseMeta };
    } else {
      return super.getUsedDatabaseConnections();
    }
  }

  @Override
  public boolean supportsErrorHandling() {
    return true;
  }
}
