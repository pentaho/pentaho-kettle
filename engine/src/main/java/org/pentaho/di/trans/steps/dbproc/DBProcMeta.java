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

package org.pentaho.di.trans.steps.dbproc;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaFactory;
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
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/*
 * Created on 26-apr-2003
 *
 */

public class DBProcMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = DBProcMeta.class; // for i18n purposes, needed by Translator2!!

  /** database connection */
  private DatabaseMeta database;

  /** proc.-name to be called */
  private String procedure;

  /** function arguments */
  private String[] argument;

  /** IN / OUT / INOUT */
  private String[] argumentDirection;

  /** value type for OUT */
  private int[] argumentType;

  /** function result: new value name */
  private String resultName;

  /** function result: new value type */
  private int resultType;

  /** The flag to set auto commit on or off on the connection */
  private boolean autoCommit;

  public DBProcMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the argument.
   */
  public String[] getArgument() {
    return argument;
  }

  /**
   * @param argument
   *          The argument to set.
   */
  public void setArgument( String[] argument ) {
    this.argument = argument;
  }

  /**
   * @return Returns the argumentDirection.
   */
  public String[] getArgumentDirection() {
    return argumentDirection;
  }

  /**
   * @param argumentDirection
   *          The argumentDirection to set.
   */
  public void setArgumentDirection( String[] argumentDirection ) {
    this.argumentDirection = argumentDirection;
  }

  /**
   * @return Returns the argumentType.
   */
  public int[] getArgumentType() {
    return argumentType;
  }

  /**
   * @param argumentType
   *          The argumentType to set.
   */
  public void setArgumentType( int[] argumentType ) {
    this.argumentType = argumentType;
  }

  /**
   * @return Returns the database.
   */
  public DatabaseMeta getDatabase() {
    return database;
  }

  /**
   * @param database
   *          The database to set.
   */
  public void setDatabase( DatabaseMeta database ) {
    this.database = database;
  }

  /**
   * @return Returns the procedure.
   */
  public String getProcedure() {
    return procedure;
  }

  /**
   * @param procedure
   *          The procedure to set.
   */
  public void setProcedure( String procedure ) {
    this.procedure = procedure;
  }

  /**
   * @return Returns the resultName.
   */
  public String getResultName() {
    return resultName;
  }

  /**
   * @param resultName
   *          The resultName to set.
   */
  public void setResultName( String resultName ) {
    this.resultName = resultName;
  }

  /**
   * @return Returns the resultType.
   */
  public int getResultType() {
    return resultType;
  }

  /**
   * @param resultType
   *          The resultType to set.
   */
  public void setResultType( int resultType ) {
    this.resultType = resultType;
  }

  /**
   * @return Returns the autoCommit.
   */
  public boolean isAutoCommit() {
    return autoCommit;
  }

  /**
   * @param autoCommit
   *          The autoCommit to set.
   */
  public void setAutoCommit( boolean autoCommit ) {
    this.autoCommit = autoCommit;
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode, databases );
  }

  public void allocate( int nrargs ) {
    argument = new String[nrargs];
    argumentDirection = new String[nrargs];
    argumentType = new int[nrargs];
  }

  public Object clone() {
    DBProcMeta retval = (DBProcMeta) super.clone();
    int nrargs = argument.length;

    retval.allocate( nrargs );

    System.arraycopy( argument, 0, retval.argument, 0, nrargs );
    System.arraycopy( argumentDirection, 0, retval.argumentDirection, 0, nrargs );
    System.arraycopy( argumentType, 0, retval.argumentType, 0, nrargs );

    return retval;
  }

  public void setDefault() {
    int i;
    int nrargs;

    database = null;

    nrargs = 0;

    allocate( nrargs );

    for ( i = 0; i < nrargs; i++ ) {
      argument[i] = "arg" + i;
      argumentDirection[i] = "IN";
      argumentType[i] = ValueMetaInterface.TYPE_NUMBER;
    }

    resultName = "result";
    resultType = ValueMetaInterface.TYPE_NUMBER;
    autoCommit = true;
  }

  @Override
  public void getFields( RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

    if ( !Utils.isEmpty( resultName ) ) {
      ValueMetaInterface v;
      try {
        v = ValueMetaFactory.createValueMeta( resultName, resultType );
        v.setOrigin( name );
        r.addValueMeta( v );
      } catch ( KettlePluginException e ) {
        throw new KettleStepException( e );
      }
    }

    for ( int i = 0; i < argument.length; i++ ) {
      if ( argumentDirection[i].equalsIgnoreCase( "OUT" ) ) {
        ValueMetaInterface v;
        try {
          v = ValueMetaFactory.createValueMeta( argument[i], argumentType[i] );
          v.setOrigin( name );
          r.addValueMeta( v );
        } catch ( KettlePluginException e ) {
          throw new KettleStepException( e );
        }
      }
    }

    return;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 500 );

    retval
      .append( "    " ).append( XMLHandler.addTagValue( "connection", database == null ? "" : database.getName() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "procedure", procedure ) );
    retval.append( "    <lookup>" ).append( Const.CR );

    for ( int i = 0; i < argument.length; i++ ) {
      retval.append( "      <arg>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "name", argument[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "direction", argumentDirection[i] ) );
      retval.append( "        " ).append(
        XMLHandler.addTagValue( "type", ValueMetaFactory.getValueMetaName( argumentType[i] ) ) );
      retval.append( "      </arg>" ).append( Const.CR );
    }

    retval.append( "    </lookup>" ).append( Const.CR );

    retval.append( "    <result>" ).append( Const.CR );
    retval.append( "      " ).append( XMLHandler.addTagValue( "name", resultName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "type",
      ValueMetaFactory.getValueMetaName( resultType ) ) );
    retval.append( "    </result>" ).append( Const.CR );

    retval.append( "    " ).append( XMLHandler.addTagValue( "auto_commit", autoCommit ) );

    return retval.toString();
  }

  private void readData( Node stepnode, List<? extends SharedObjectInterface> databases ) throws KettleXMLException {
    try {
      int i;
      int nrargs;

      String con = XMLHandler.getTagValue( stepnode, "connection" );
      database = DatabaseMeta.findDatabase( databases, con );
      procedure = XMLHandler.getTagValue( stepnode, "procedure" );

      Node lookup = XMLHandler.getSubNode( stepnode, "lookup" );
      nrargs = XMLHandler.countNodes( lookup, "arg" );

      allocate( nrargs );

      for ( i = 0; i < nrargs; i++ ) {
        Node anode = XMLHandler.getSubNodeByNr( lookup, "arg", i );

        argument[i] = XMLHandler.getTagValue( anode, "name" );
        argumentDirection[i] = XMLHandler.getTagValue( anode, "direction" );
        argumentType[i] = ValueMetaFactory.getIdForValueMeta( XMLHandler.getTagValue( anode, "type" ) );
      }

      resultName = XMLHandler.getTagValue( stepnode, "result", "name" ); // Optional, can be null
                                                                         //
      resultType = ValueMetaFactory.getIdForValueMeta( XMLHandler.getTagValue( stepnode, "result", "type" ) );
      autoCommit = !"N".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "auto_commit" ) );
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "DBProcMeta.Exception.UnableToReadStepInfo" ), e );
    }
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      database = rep.loadDatabaseMetaFromStepAttribute( id_step, "id_connection", databases );
      procedure = rep.getStepAttributeString( id_step, "procedure" );

      int nrargs = rep.countNrStepAttributes( id_step, "arg_name" );
      allocate( nrargs );

      for ( int i = 0; i < nrargs; i++ ) {
        argument[i] = rep.getStepAttributeString( id_step, i, "arg_name" );
        argumentDirection[i] = rep.getStepAttributeString( id_step, i, "arg_direction" );
        argumentType[i] = ValueMetaFactory.getIdForValueMeta( rep.getStepAttributeString( id_step, i, "arg_type" ) );
      }

      resultName = rep.getStepAttributeString( id_step, "result_name" );
      resultType = ValueMetaFactory.getIdForValueMeta( rep.getStepAttributeString( id_step, "result_type" ) );
      autoCommit = rep.getStepAttributeBoolean( id_step, 0, "auto_commit", true );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "DBProcMeta.Exception.UnexpectedErrorReadingStepInfo" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveDatabaseMetaStepAttribute( id_transformation, id_step, "id_connection", database );
      rep.saveStepAttribute( id_transformation, id_step, "procedure", procedure );

      for ( int i = 0; i < argument.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "arg_name", argument[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "arg_direction", argumentDirection[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "arg_type",
          ValueMetaFactory.getValueMetaName( argumentType[i] ) );
      }

      rep.saveStepAttribute( id_transformation, id_step, "result_name", resultName );
      rep.saveStepAttribute( id_transformation, id_step, "result_type",
        ValueMetaFactory.getValueMetaName( resultType ) );
      rep.saveStepAttribute( id_transformation, id_step, "auto_commit", autoCommit );

      // Also, save the step-database relationship!
      if ( database != null ) {
        rep.insertStepDatabase( id_transformation, id_step, database.getObjectId() );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "DBProcMeta.Exception.UnableToSaveStepInfo" )
        + id_step, e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;
    String error_message = "";

    if ( database != null ) {
      Database db = new Database( transMeta, database );
      try {
        db.connect();

        // Look up fields in the input stream <prev>
        if ( prev != null && prev.size() > 0 ) {
          boolean first = true;
          error_message = "";
          boolean error_found = false;

          for ( int i = 0; i < argument.length; i++ ) {
            ValueMetaInterface v = prev.searchValueMeta( argument[i] );
            if ( v == null ) {
              if ( first ) {
                first = false;
                error_message +=
                  BaseMessages.getString( PKG, "DBProcMeta.CheckResult.MissingArguments" ) + Const.CR;
              }
              error_found = true;
              error_message += "\t\t" + argument[i] + Const.CR;
            } else {
              // Argument exists in input stream: same type?

              if ( v.getType() != argumentType[i] && !( v.isNumeric() && ValueMetaBase.isNumeric( argumentType[i] ) ) ) {
                error_found = true;
                error_message +=
                  "\t\t"
                    + argument[i]
                    + BaseMessages.getString(
                      PKG, "DBProcMeta.CheckResult.WrongTypeArguments", v.getTypeDesc(),
                      ValueMetaFactory.getValueMetaName( argumentType[i] ) ) + Const.CR;
              }
            }
          }
          if ( error_found ) {
            cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
          } else {
            cr =
              new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
                PKG, "DBProcMeta.CheckResult.AllArgumentsOK" ), stepMeta );
          }
          remarks.add( cr );
        } else {
          error_message = BaseMessages.getString( PKG, "DBProcMeta.CheckResult.CouldNotReadFields" ) + Const.CR;
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
          remarks.add( cr );
        }
      } catch ( KettleException e ) {
        error_message = BaseMessages.getString( PKG, "DBProcMeta.CheckResult.ErrorOccurred" ) + e.getMessage();
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      }
    } else {
      error_message = BaseMessages.getString( PKG, "DBProcMeta.CheckResult.InvalidConnection" );
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "DBProcMeta.CheckResult.ReceivingInfoFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "DBProcMeta.CheckResult.NoInpuReceived" ), stepMeta );
      remarks.add( cr );
    }

  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new DBProc( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new DBProcData();
  }

  public DatabaseMeta[] getUsedDatabaseConnections() {
    if ( database != null ) {
      return new DatabaseMeta[] { database };
    } else {
      return super.getUsedDatabaseConnections();
    }
  }

  public boolean supportsErrorHandling() {
    return true;
  }

}
