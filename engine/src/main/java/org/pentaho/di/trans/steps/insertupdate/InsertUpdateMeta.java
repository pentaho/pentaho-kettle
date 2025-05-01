/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.insertupdate;

import java.util.Arrays;
import java.util.List;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.injection.AfterInjection;
import org.pentaho.di.core.injection.InjectionDeep;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.ProvidesModelerMeta;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.DatabaseImpact;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;
import org.pentaho.di.trans.step.utils.RowMetaUtils;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/*
 * Created on 26-apr-2003
 *
 */
@InjectionSupported( localizationPrefix = "InsertUpdateMeta.Injection.", groups = { "KEYS", "UPDATES" } )
public class InsertUpdateMeta extends BaseDatabaseStepMeta implements StepMetaInterface, ProvidesModelerMeta {
  private static Class<?> PKG = InsertUpdateMeta.class; // for i18n purposes, needed by Translator2!!

  private List<? extends SharedObjectInterface> databases;

  /**
   * what's the lookup schema?
   */
  @Injection( name = "SCHEMA_NAME", required = true )
  private String schemaName;

  /**
   * what's the lookup table?
   */
  @Injection( name = "TABLE_NAME", required = true )
  private String tableName;

  /**
   * database connection
   */
  private DatabaseMeta databaseMeta;

  @InjectionDeep
  private KeyField[] keyFields = {};

  @InjectionDeep
  private UpdateField[] updateFields = {};

  /**
   * Commit size for inserts/updates
   */
  @Injection( name = "COMMIT_SIZE" )
  private String commitSize;

  /**
   * Bypass any updates
   */
  @Injection( name = "DO_NOT" )
  private boolean updateBypassed;

  @Injection( name = "CONNECTIONNAME", required = true )
  public void setConnection( String connectionName ) {
    databaseMeta = DatabaseMeta.findDatabase( databases, connectionName );
  }

  /**
   * @return Returns the commitSize.
   * @deprecated use public String getCommitSizeVar() instead
   */
  @Deprecated
  public int getCommitSize() {
    return Integer.parseInt( commitSize );
  }

  /**
   * @return Returns the commitSize.
   */
  public String getCommitSizeVar() {
    return commitSize;
  }

  /**
   * @param vs -
   *           variable space to be used for searching variable value
   *           usually "this" for a calling step
   * @return Returns the commitSize.
   */
  public int getCommitSize( VariableSpace vs ) {
    //this happens when the step is created via API and no setDefaults was called
    commitSize = ( commitSize == null ) ? "0" : commitSize;
    return Integer.parseInt( vs.environmentSubstitute( commitSize ) );
  }

  /**
   * @param commitSize The commitSize to set.
   * @deprecated use public void setCommitSize( String commitSize ) instead
   */
  @Deprecated
  public void setCommitSize( int commitSize ) {
    this.commitSize = Integer.toString( commitSize );
  }

  /**
   * @param commitSize The commitSize to set.
   */
  public void setCommitSize( String commitSize ) {
    this.commitSize = commitSize;
  }

  /**
   * @return Returns the database.
   */
  public DatabaseMeta getDatabaseMeta() {
    return databaseMeta;
  }

  /**
   * @param database The database to set.
   */
  public void setDatabaseMeta( DatabaseMeta database ) {
    this.databaseMeta = database;
  }

  public KeyField[] getKeyFields() {
    return keyFields;
  }

  public void setKeyFields( KeyField[] keyFields ) {
    this.keyFields = keyFields;
  }

  public UpdateField[] getUpdateFields() {
    return updateFields;
  }

  public void setUpdateFields( UpdateField[] updateFields ) {
    this.updateFields = updateFields;
  }

  /**
   * @return Returns the tableName.
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * @param tableName The tableName to set.
   */
  public void setTableName( String tableName ) {
    this.tableName = tableName;
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode, databases );
  }

  public void allocate( int nrkeys, int nrvalues ) {
    keyFields = new KeyField[ nrkeys ];
    for ( int i = 0; i < nrkeys; i++ ) {
      keyFields[ i ] = new KeyField();
    }
    updateFields = new UpdateField[ nrvalues ];
    for ( int i = 0; i < nrvalues; i++ ) {
      updateFields[ i ] = new UpdateField();
    }
  }

  public Object clone() {
    InsertUpdateMeta retval = (InsertUpdateMeta) super.clone();
    int nrkeys = keyFields.length;
    int nrvalues = updateFields.length;

    retval.allocate( nrkeys, nrvalues );

    for ( int i = 0; i < nrkeys; i++ ) {
      retval.keyFields[ i ] = (KeyField) this.keyFields[ i ].clone();
    }

    for ( int i = 0; i < nrvalues; i++ ) {
      retval.updateFields[ i ] = (UpdateField) this.updateFields[ i ].clone();
    }

    return retval;
  }

  private void readData( Node stepnode, List<? extends SharedObjectInterface> databases ) throws KettleXMLException {
    this.databases = databases;
    try {
      String csize;
      int nrkeys, nrvalues;

      String con = XMLHandler.getTagValue( stepnode, "connection" );
      databaseMeta = DatabaseMeta.findDatabase( databases, con );
      csize = XMLHandler.getTagValue( stepnode, "commit" );
      commitSize = ( csize != null ) ? csize : "0";
      schemaName = XMLHandler.getTagValue( stepnode, "lookup", "schema" );
      tableName = XMLHandler.getTagValue( stepnode, "lookup", "table" );
      updateBypassed = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "update_bypassed" ) );

      Node lookup = XMLHandler.getSubNode( stepnode, "lookup" );
      nrkeys = XMLHandler.countNodes( lookup, "key" );
      nrvalues = XMLHandler.countNodes( lookup, "value" );

      allocate( nrkeys, nrvalues );

      for ( int i = 0; i < nrkeys; i++ ) {
        Node knode = XMLHandler.getSubNodeByNr( lookup, "key", i );

        keyFields[ i ].setKeyStream( XMLHandler.getTagValue( knode, "name" ) );
        keyFields[ i ].setKeyLookup( XMLHandler.getTagValue( knode, "field" ) );
        keyFields[ i ].setKeyCondition( XMLHandler.getTagValue( knode, "condition" ) );
        if ( keyFields[ i ].getKeyCondition() == null ) {
          keyFields[ i ].setKeyCondition( "=" );
        }
        keyFields[ i ].setKeyStream2( XMLHandler.getTagValue( knode, "name2" ) );
      }

      for ( int i = 0; i < nrvalues; i++ ) {
        Node vnode = XMLHandler.getSubNodeByNr( lookup, "value", i );

        updateFields[ i ].setUpdateLookup( XMLHandler.getTagValue( vnode, "name" ) );
        updateFields[ i ].setUpdateStream( XMLHandler.getTagValue( vnode, "rename" ) );
        if ( updateFields[ i ].getUpdateStream() == null ) {
          updateFields[ i ].setUpdateStream( updateFields[ i ].getUpdateLookup() ); // default: the same name!
        }
        String updateValue = XMLHandler.getTagValue( vnode, "update" );
        if ( updateValue == null ) {
          // default TRUE
          updateFields[ i ].setUpdate( Boolean.TRUE );
        } else {
          if ( updateValue.equalsIgnoreCase( "Y" ) ) {
            updateFields[ i ].setUpdate( Boolean.TRUE );
          } else {
            updateFields[ i ].setUpdate( Boolean.FALSE );
          }
        }
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "InsertUpdateMeta.Exception.UnableToReadStepInfoFromXML" ), e );
    }
  }

  public void setDefault() {
    databaseMeta = null;
    commitSize = "100";
    schemaName = "";
    tableName = BaseMessages.getString( PKG, "InsertUpdateMeta.DefaultTableName" );

    int nrkeys = 0;
    int nrvalues = 0;

    allocate( nrkeys, nrvalues );

    for ( int i = 0; i < nrkeys; i++ ) {
      keyFields[ i ].setKeyLookup( "age" );
      keyFields[ i ].setKeyCondition( "BETWEEN" );
      keyFields[ i ].setKeyStream( "age_from" );
      keyFields[ i ].setKeyStream2( "age_to" );
    }


    for ( int i = 0; i < nrvalues; i++ ) {
      updateFields[ i ].setUpdateLookup( BaseMessages.getString( PKG, "InsertUpdateMeta.ColumnName.ReturnField" ) + i );
      updateFields[ i ].setUpdateStream( BaseMessages.getString( PKG, "InsertUpdateMeta.ColumnName.NewName" ) + i );
      updateFields[ i ].setUpdate( Boolean.TRUE );
    }
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 400 );

    retval
      .append( "    " ).append(
        XMLHandler.addTagValue( "connection", databaseMeta == null ? "" : databaseMeta.getName() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "commit", commitSize ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "update_bypassed", updateBypassed ) );
    retval.append( "    <lookup>" ).append( Const.CR );
    retval.append( "      " ).append( XMLHandler.addTagValue( "schema", schemaName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "table", tableName ) );

    for ( int i = 0; i < keyFields.length; i++ ) {
      retval.append( "      <key>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "name", keyFields[ i ].getKeyStream() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "field", keyFields[ i ].getKeyLookup() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "condition", keyFields[ i ].getKeyCondition() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "name2", keyFields[ i ].getKeyStream2() ) );
      retval.append( "      </key>" ).append( Const.CR );
    }

    for ( int i = 0; i < updateFields.length; i++ ) {
      retval.append( "      <value>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "name", updateFields[ i ].getUpdateLookup() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "rename", updateFields[ i ].getUpdateStream() ) );
      retval.append( "        " )
        .append( XMLHandler.addTagValue( "update", updateFields[ i ].getUpdate().booleanValue() ) );
      retval.append( "      </value>" ).append( Const.CR );
    }

    retval.append( "    </lookup>" ).append( Const.CR );

    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
    throws KettleException {
    this.databases = databases;
    try {
      databaseMeta = rep.loadDatabaseMetaFromStepAttribute( id_step, "id_connection", databases );

      commitSize = rep.getStepAttributeString( id_step, "commit" );
      if ( commitSize == null ) {
        long comSz = -1;
        try {
          comSz = rep.getStepAttributeInteger( id_step, "commit" );
        } catch ( Exception ex ) {
          commitSize = "100";
        }
        if ( comSz >= 0 ) {
          commitSize = Long.toString( comSz );
        }
      }
      schemaName = rep.getStepAttributeString( id_step, "schema" );
      tableName = rep.getStepAttributeString( id_step, "table" );
      updateBypassed = rep.getStepAttributeBoolean( id_step, "update_bypassed" );

      int nrkeys = rep.countNrStepAttributes( id_step, "key_field" );
      int nrvalues = rep.countNrStepAttributes( id_step, "value_name" );

      allocate( nrkeys, nrvalues );

      for ( int i = 0; i < nrkeys; i++ ) {
        keyFields[ i ].setKeyStream( rep.getStepAttributeString( id_step, i, "key_name" ) );
        keyFields[ i ].setKeyLookup( rep.getStepAttributeString( id_step, i, "key_field" ) );
        keyFields[ i ].setKeyCondition( rep.getStepAttributeString( id_step, i, "key_condition" ) );
        keyFields[ i ].setKeyStream2( rep.getStepAttributeString( id_step, i, "key_name2" ) );
      }

      for ( int i = 0; i < nrvalues; i++ ) {
        updateFields[ i ].setUpdateLookup( rep.getStepAttributeString( id_step, i, "value_name" ) );
        updateFields[ i ].setUpdateStream( rep.getStepAttributeString( id_step, i, "value_rename" ) );
        updateFields[ i ].setUpdate(
          Boolean.valueOf( rep.getStepAttributeBoolean( id_step, i, "value_update", true ) ) );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "InsertUpdateMeta.Exception.UnexpectedErrorReadingStepInfoFromRepository" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
    throws KettleException {
    try {
      rep.saveDatabaseMetaStepAttribute( id_transformation, id_step, "id_connection", databaseMeta );
      rep.saveStepAttribute( id_transformation, id_step, "commit", commitSize );
      rep.saveStepAttribute( id_transformation, id_step, "schema", schemaName );
      rep.saveStepAttribute( id_transformation, id_step, "table", tableName );
      rep.saveStepAttribute( id_transformation, id_step, "update_bypassed", updateBypassed );

      for ( int i = 0; i < keyFields.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "key_name", keyFields[ i ].getKeyStream() );
        rep.saveStepAttribute( id_transformation, id_step, i, "key_field", keyFields[ i ].keyLookup );
        rep.saveStepAttribute( id_transformation, id_step, i, "key_condition", keyFields[ i ].getKeyCondition() );
        rep.saveStepAttribute( id_transformation, id_step, i, "key_name2", keyFields[ i ].getKeyStream2() );
      }

      for ( int i = 0; i < updateFields.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "value_name", updateFields[ i ].getUpdateLookup() );
        rep.saveStepAttribute( id_transformation, id_step, i, "value_rename", updateFields[ i ].getUpdateStream() );
        rep.saveStepAttribute( id_transformation, id_step, i, "value_update",
          updateFields[ i ].getUpdate().booleanValue() );
      }

      // Also, save the step-database relationship!
      if ( databaseMeta != null ) {
        rep.insertStepDatabase( id_transformation, id_step, databaseMeta.getObjectId() );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "InsertUpdateMeta.Exception.UnableToSaveStepInfoToRepository" )
        + id_step, e );
    }
  }

  @Override
  public void getFields( Bowl bowl, RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep,
                         VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    // Default: nothing changes to rowMeta
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
                     RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
                     Repository repository, IMetaStore metaStore ) {
    CheckResult cr;
    String error_message = "";

    if ( databaseMeta != null ) {
      Database db = new Database( loggingObject, databaseMeta );
      db.shareVariablesWith( transMeta );
      try {
        db.connect();

        if ( !Utils.isEmpty( tableName ) ) {
          cr =
            new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
              PKG, "InsertUpdateMeta.CheckResult.TableNameOK" ), stepMeta );
          remarks.add( cr );

          boolean first = true;
          boolean error_found = false;
          error_message = "";

          // Check fields in table
          RowMetaInterface r = db.getTableFieldsMeta( schemaName, tableName );
          if ( r != null ) {
            cr =
              new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
                PKG, "InsertUpdateMeta.CheckResult.TableExists" ), stepMeta );
            remarks.add( cr );

            for ( int i = 0; i < keyFields.length; i++ ) {
              String lufield = keyFields[ i ].getKeyLookup();

              ValueMetaInterface v = r.searchValueMeta( lufield );
              if ( v == null ) {
                if ( first ) {
                  first = false;
                  error_message +=
                    BaseMessages.getString(
                      PKG, "InsertUpdateMeta.CheckResult.MissingCompareFieldsInTargetTable" )
                      + Const.CR;
                }
                error_found = true;
                error_message += "\t\t" + lufield + Const.CR;
              }
            }
            if ( error_found ) {
              cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
            } else {
              cr =
                new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
                  PKG, "InsertUpdateMeta.CheckResult.AllLookupFieldsFound" ), stepMeta );
            }
            remarks.add( cr );

            // How about the fields to insert/update in the table?
            first = true;
            error_found = false;
            error_message = "";

            for ( int i = 0; i < updateFields.length; i++ ) {
              String lufield = updateFields[ i ].getUpdateLookup();

              ValueMetaInterface v = r.searchValueMeta( lufield );
              if ( v == null ) {
                if ( first ) {
                  first = false;
                  error_message +=
                    BaseMessages.getString(
                      PKG, "InsertUpdateMeta.CheckResult.MissingFieldsToUpdateInTargetTable" )
                      + Const.CR;
                }
                error_found = true;
                error_message += "\t\t" + lufield + Const.CR;
              }
            }
            if ( error_found ) {
              cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
            } else {
              cr =
                new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
                  PKG, "InsertUpdateMeta.CheckResult.AllFieldsToUpdateFoundInTargetTable" ), stepMeta );
            }
            remarks.add( cr );
          } else {
            error_message = BaseMessages.getString( PKG, "InsertUpdateMeta.CheckResult.CouldNotReadTableInfo" );
            cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
            remarks.add( cr );
          }
        }

        // Look up fields in the input stream <prev>
        if ( prev != null && prev.size() > 0 ) {
          cr =
            new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
              PKG, "InsertUpdateMeta.CheckResult.StepReceivingDatas", prev.size() + "" ), stepMeta );
          remarks.add( cr );

          boolean first = true;
          error_message = "";
          boolean error_found = false;

          for ( int i = 0; i < keyFields.length; i++ ) {
            ValueMetaInterface v = prev.searchValueMeta( keyFields[ i ].getKeyStream() );
            if ( v == null ) {
              if ( first ) {
                first = false;
                error_message +=
                  BaseMessages.getString( PKG, "InsertUpdateMeta.CheckResult.MissingFieldsInInput" ) + Const.CR;
              }
              error_found = true;
              error_message += "\t\t" + keyFields[ i ].getKeyStream() + Const.CR;
            }
          }
          for ( int i = 0; i < keyFields.length; i++ ) {
            if ( keyFields[ i ].getKeyStream2() != null && keyFields[ i ].getKeyStream2().length() > 0 ) {
              ValueMetaInterface v = prev.searchValueMeta( keyFields[ i ].getKeyStream2() );
              if ( v == null ) {
                if ( first ) {
                  first = false;
                  error_message +=
                    BaseMessages.getString( PKG, "InsertUpdateMeta.CheckResult.MissingFieldsInInput" )
                      + Const.CR;
                }
                error_found = true;
                error_message += "\t\t" + keyFields[ i ].getKeyStream() + Const.CR;
              }
            }
          }
          if ( error_found ) {
            cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
          } else {
            cr =
              new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
                PKG, "InsertUpdateMeta.CheckResult.AllFieldsFoundInInput" ), stepMeta );
          }
          remarks.add( cr );

          // How about the fields to insert/update the table with?
          first = true;
          error_found = false;
          error_message = "";

          for ( int i = 0; i < updateFields.length; i++ ) {
            String lufield = updateFields[ i ].getUpdateStream();

            ValueMetaInterface v = prev.searchValueMeta( lufield );
            if ( v == null ) {
              if ( first ) {
                first = false;
                error_message +=
                  BaseMessages.getString( PKG, "InsertUpdateMeta.CheckResult.MissingInputStreamFields" )
                    + Const.CR;
              }
              error_found = true;
              error_message += "\t\t" + lufield + Const.CR;
            }
          }
          if ( error_found ) {
            cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
          } else {
            cr =
              new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
                PKG, "InsertUpdateMeta.CheckResult.AllFieldsFoundInInput2" ), stepMeta );
          }
          remarks.add( cr );
        } else {
          error_message =
            BaseMessages.getString( PKG, "InsertUpdateMeta.CheckResult.MissingFieldsInInput3" ) + Const.CR;
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
          remarks.add( cr );
        }
      } catch ( KettleException e ) {
        error_message =
          BaseMessages.getString( PKG, "InsertUpdateMeta.CheckResult.DatabaseErrorOccurred" ) + e.getMessage();
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      } finally {
        db.disconnect();
      }
    } else {
      error_message = BaseMessages.getString( PKG, "InsertUpdateMeta.CheckResult.InvalidConnection" );
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "InsertUpdateMeta.CheckResult.StepReceivingInfoFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "InsertUpdateMeta.CheckResult.NoInputError" ), stepMeta );
      remarks.add( cr );
    }
  }

  public SQLStatement getSQLStatements( TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
                                        Repository repository, IMetaStore metaStore ) throws KettleStepException {
    SQLStatement retval = new SQLStatement( stepMeta.getName(), databaseMeta, null ); // default: nothing to do!

    if ( databaseMeta != null ) {
      if ( prev != null && prev.size() > 0 ) {
        String[] keyLookupAsStringArray = new String[ keyFields.length ];
        for ( int i = 0; i < keyFields.length; i++ ) {
          keyLookupAsStringArray[ i ] = keyFields[ i ].getKeyLookup();
        }
        String[] keyStreamAsStringArray = new String[ keyFields.length ];
        for ( int i = 0; i < keyFields.length; i++ ) {
          keyStreamAsStringArray[ i ] = keyFields[ i ].getKeyStream();
        }
        String[] updateLookupAsStringArray = new String[ updateFields.length ];
        for ( int i = 0; i < updateFields.length; i++ ) {
          updateLookupAsStringArray[ i ] = updateFields[ i ].getUpdateLookup();
        }
        String[] updateStreamAsStringArray = new String[ updateFields.length ];
        for ( int i = 0; i < updateFields.length; i++ ) {
          updateStreamAsStringArray[ i ] = updateFields[ i ].getUpdateStream();
        }
        // Copy the row
        RowMetaInterface tableFields = RowMetaUtils.getRowMetaForUpdate( prev, keyLookupAsStringArray,
          keyStreamAsStringArray, updateLookupAsStringArray, updateStreamAsStringArray );

        if ( !Utils.isEmpty( tableName ) ) {
          Database db = new Database( loggingObject, databaseMeta );
          db.shareVariablesWith( transMeta );
          try {
            db.connect();

            String schemaTable = databaseMeta.getQuotedSchemaTableCombination( schemaName, tableName );
            String cr_table = db.getDDL( schemaTable, tableFields, null, false, null, true );

            String cr_index = "";
            String[] idx_fields = null;

            if ( keyFields != null && keyFields.length > 0 ) {
              idx_fields = new String[ keyFields.length ];
              for ( int i = 0; i < keyFields.length; i++ ) {
                idx_fields[ i ] = keyFields[ i ].getKeyLookup();
              }
            } else {
              retval.setError( BaseMessages.getString( PKG, "InsertUpdateMeta.CheckResult.MissingKeyFields" ) );
            }

            // Key lookup dimensions...
            if ( idx_fields != null
              && idx_fields.length > 0 && !db.checkIndexExists( schemaName, tableName, idx_fields ) ) {
              String indexname = "idx_" + tableName + "_lookup";
              cr_index =
                db.getCreateIndexStatement( schemaTable, indexname, idx_fields, false, false, false, true );
            }

            String sql = cr_table + cr_index;
            if ( sql.length() == 0 ) {
              retval.setSQL( null );
            } else {
              retval.setSQL( sql );
            }
          } catch ( KettleException e ) {
            retval.setError( BaseMessages.getString( PKG, "InsertUpdateMeta.ReturnValue.ErrorOccurred" )
              + e.getMessage() );
          }
        } else {
          retval
            .setError( BaseMessages.getString( PKG, "InsertUpdateMeta.ReturnValue.NoTableDefinedOnConnection" ) );
        }
      } else {
        retval.setError( BaseMessages.getString( PKG, "InsertUpdateMeta.ReturnValue.NotReceivingAnyFields" ) );
      }
    } else {
      retval.setError( BaseMessages.getString( PKG, "InsertUpdateMeta.ReturnValue.NoConnectionDefined" ) );
    }

    return retval;
  }

  public void analyseImpact( List<DatabaseImpact> impact, TransMeta transMeta, StepMeta stepMeta,
                             RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info,
                             Repository repository,
                             IMetaStore metaStore ) throws KettleStepException {
    if ( prev != null ) {
      // Lookup: we do a lookup on the natural keys
      for ( int i = 0; i < keyFields.length; i++ ) {
        ValueMetaInterface v = prev.searchValueMeta( keyFields[ i ].getKeyStream() );
        String keyFieldsOriginValue;
        String keyFieldsRemark;
        if ( v != null ) {
          keyFieldsOriginValue = v.getOrigin();
          keyFieldsRemark = "Type = " + v.toStringMeta();
        } else {
          keyFieldsOriginValue = "?";
          keyFieldsRemark = "Type = ";
        }

        DatabaseImpact ii =
          new DatabaseImpact(
            DatabaseImpact.TYPE_IMPACT_READ, transMeta.getName(), stepMeta.getName(), databaseMeta
            .getDatabaseName(), tableName, keyFields[ i ].getKeyLookup(), keyFields[ i ].getKeyStream(),
            keyFieldsOriginValue, "", keyFieldsRemark );
        impact.add( ii );
      }

      // Insert update fields : read/write
      for ( int i = 0; i < updateFields.length; i++ ) {
        ValueMetaInterface v = prev.searchValueMeta( updateFields[ i ].getUpdateStream() );
        String updateFieldsOriginValue;
        String updateFieldsRemark;
        if ( v != null ) {
          updateFieldsOriginValue = v.getOrigin();
          updateFieldsRemark = "Type = " + v.toStringMeta();
        } else {
          updateFieldsOriginValue = "?";
          updateFieldsRemark = "Type = ";
        }

        DatabaseImpact ii =
          new DatabaseImpact(
            DatabaseImpact.TYPE_IMPACT_READ_WRITE, transMeta.getName(), stepMeta.getName(), databaseMeta
            .getDatabaseName(), tableName, updateFields[ i ].getUpdateLookup(), updateFields[ i ].getUpdateStream(),
            updateFieldsOriginValue, "", updateFieldsRemark );
        impact.add( ii );
      }
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
                                TransMeta transMeta, Trans trans ) {
    return new InsertUpdate( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new InsertUpdateData();
  }

  public DatabaseMeta[] getUsedDatabaseConnections() {
    if ( databaseMeta != null ) {
      return new DatabaseMeta[] { databaseMeta };
    } else {
      return super.getUsedDatabaseConnections();
    }
  }

  /**
   * @return Returns the updateBypassed.
   */
  public boolean isUpdateBypassed() {
    return updateBypassed;
  }

  /**
   * @param updateBypassed The updateBypassed to set.
   */
  public void setUpdateBypassed( boolean updateBypassed ) {
    this.updateBypassed = updateBypassed;
  }

  public RowMetaInterface getRequiredFields( VariableSpace space ) throws KettleException {
    String realSchemaName = space.environmentSubstitute( schemaName );
    String realTableName = space.environmentSubstitute( tableName );

    if ( databaseMeta != null ) {
      Database db = new Database( loggingObject, databaseMeta );
      try {
        db.connect();

        if ( !Utils.isEmpty( realTableName ) ) {
          // Check if this table exists...
          if ( db.checkTableExists( realSchemaName, realTableName ) ) {
            return db.getTableFieldsMeta( realSchemaName, realTableName );
          } else {
            throw new KettleException( BaseMessages.getString( PKG, "InsertUpdateMeta.Exception.TableNotFound" ) );
          }
        } else {
          throw new KettleException( BaseMessages.getString( PKG, "InsertUpdateMeta.Exception.TableNotSpecified" ) );
        }
      } catch ( Exception e ) {
        throw new KettleException(
          BaseMessages.getString( PKG, "InsertUpdateMeta.Exception.ErrorGettingFields" ), e );
      } finally {
        db.disconnect();
      }
    } else {
      throw new KettleException( BaseMessages.getString( PKG, "InsertUpdateMeta.Exception.ConnectionNotDefined" ) );
    }

  }

  /**
   * @return the schemaName
   */
  public String getSchemaName() {
    return schemaName;
  }

  @Override public String getMissingDatabaseConnectionInformationMessage() {
    return null;
  }

  /**
   * @param schemaName the schemaName to set
   */
  public void setSchemaName( String schemaName ) {
    this.schemaName = schemaName;
  }

  public boolean supportsErrorHandling() {
    return true;
  }

  @Override public RowMeta getRowMeta( StepDataInterface stepData ) {
    return (RowMeta) ( (InsertUpdateData) stepData ).insertRowMeta;
  }

  @Override public List<String> getDatabaseFields() {
    String[] updateLookup = new String[ updateFields.length ];
    for ( int i = 0; i < updateFields.length; i++ ) {
      updateLookup[ i ] = updateFields[ i ].getUpdateLookup();
    }
    return Arrays.asList( updateLookup );
  }

  @Override public List<String> getStreamFields() {

    String[] updateStream = new String[ updateFields.length ];
    for ( int i = 0; i < updateFields.length; i++ ) {
      updateStream[ i ] = updateFields[ i ].getUpdateStream();
    }
    return Arrays.asList( updateStream );
  }

  /**
   * If we use injection we can have different arrays lengths.
   * We need synchronize them for consistency behavior with UI
   */
  @AfterInjection
  public void afterInjectionSynchronization() {

  }

  public static class UpdateField implements Cloneable {
    /**
     * Field value to update after lookup
     */
    @Injection( name = "UPDATE_LOOKUP", group = "UPDATES", required = true )
    private String updateLookup;

    /**
     * Stream name to update value with
     */
    @Injection( name = "UPDATE_STREAM", group = "UPDATES", required = true )
    private String updateStream;

    /**
     * boolean indicating if field needs to be updated
     */
    @Injection( name = "UPDATE_FLAG", group = "UPDATES", required = true )
    private Boolean update;

    /**
     * @return Returns the updateLookup.
     */
    public String getUpdateLookup() {
      return updateLookup;
    }

    /**
     * @param updateLookup The updateLookup to set.
     */
    public void setUpdateLookup( String updateLookup ) {
      this.updateLookup = updateLookup;
    }

    /**
     * @return Returns the updateStream.
     */
    public String getUpdateStream() {
      return updateStream;
    }

    /**
     * @param updateStream The updateStream to set.
     */
    public void setUpdateStream( String updateStream ) {
      this.updateStream = updateStream;
    }

    public Boolean getUpdate() {
      return update;
    }

    public void setUpdate( Boolean update ) {
      this.update = update;
    }

    public UpdateField() {
      updateLookup = null;
    }

    @Override
    public boolean equals( Object obj ) {
      if ( this == obj ) {
        return true;
      }
      if ( obj == null ) {
        return false;
      }
      if ( getClass() != obj.getClass() ) {
        return false;
      }
      UpdateField other = (UpdateField) obj;
      if ( updateStream == null ) {
        if ( other.updateStream != null ) {
          return false;
        }
      } else if ( !updateStream.equals( other.updateStream ) ) {
        return false;
      }
      if ( updateLookup == null ) {
        if ( other.updateLookup != null ) {
          return false;
        }
      } else if ( !updateLookup.equals( other.updateLookup ) ) {
        return false;
      }
      if ( update == null ) {
        if ( other.update != null ) {
          return false;
        }
      } else if ( !update.equals( other.update ) ) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ( ( updateStream == null ) ? 0 : updateStream.hashCode() );
      result = prime * result + ( ( updateLookup == null ) ? 0 : updateLookup.hashCode() );
      result = prime * result + ( ( update == null ) ? 0 : update.hashCode() );
      return result;
    }

    @Override
    public Object clone() {
      try {
        return (UpdateField) super.clone();
      } catch ( CloneNotSupportedException e ) {
        throw new RuntimeException( e );
      }
    }
  }

  public static class KeyField implements Cloneable {

    /**
     * which field in input stream to compare with?
     */
    @Injection( name = "KEY_STREAM", group = "KEYS", required = false )
    private String keyStream;

    /**
     * field in table
     */
    @Injection( name = "KEY_LOOKUP", group = "KEYS", required = true )
    private String keyLookup;

    /**
     * Comparator: =, <>, BETWEEN, ...
     */
    @Injection( name = "KEY_CONDITION", group = "KEYS", required = true )
    private String keyCondition;

    /**
     * Extra field for between...
     */
    @Injection( name = "KEY_STREAM2", group = "KEYS", required = false )
    private String keyStream2;

    public KeyField() {
      this.keyStream = null;
    }

    /**
     * @return Returns the keyStream.
     */
    public String getKeyStream() {
      return keyStream;
    }

    /**
     * @param keyStream The keyStream to set.
     */
    public void setKeyStream( String keyStream ) {
      this.keyStream = keyStream;
    }

    /**
     * @return Returns the keyLookup.
     */
    public String getKeyLookup() {
      return keyLookup;
    }

    /**
     * @param keyLookup The keyLookup to set.
     */
    public void setKeyLookup( String keyLookup ) {
      this.keyLookup = keyLookup;
    }

    /**
     * @return Returns the keyCondition.
     */
    public String getKeyCondition() {
      return keyCondition;
    }

    /**
     * @param keyCondition The keyCondition to set.
     */
    public void setKeyCondition( String keyCondition ) {
      this.keyCondition = keyCondition;
    }

    /**
     * @return Returns the keyStream2.
     */
    public String getKeyStream2() {
      return keyStream2;
    }

    /**
     * @param keyStream2 The keyStream2 to set.
     */
    public void setKeyStream2( String keyStream2 ) {
      this.keyStream2 = keyStream2;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ( ( keyStream == null ) ? 0 : keyStream.hashCode() );
      result = prime * result + ( ( keyLookup == null ) ? 0 : keyLookup.hashCode() );
      result = prime * result + ( ( keyCondition == null ) ? 0 : keyCondition.hashCode() );
      result = prime * result + ( ( keyStream2 == null ) ? 0 : keyStream2.hashCode() );
      return result;
    }

    @Override
    public boolean equals( Object obj ) {
      if ( this == obj ) {
        return true;
      }
      if ( obj == null ) {
        return false;
      }
      if ( getClass() != obj.getClass() ) {
        return false;
      }
      KeyField other = (KeyField) obj;
      if ( keyStream == null ) {
        if ( other.keyStream != null ) {
          return false;
        }
      } else if ( !keyStream.equals( other.keyStream ) ) {
        return false;
      }
      if ( keyLookup == null ) {
        if ( other.keyLookup != null ) {
          return false;
        }
      } else if ( !keyLookup.equals( other.keyLookup ) ) {
        return false;
      }
      if ( keyCondition == null ) {
        if ( other.keyCondition != null ) {
          return false;
        }
      } else if ( !keyCondition.equals( other.keyCondition ) ) {
        return false;
      }
      if ( keyStream2 == null ) {
        if ( other.keyStream2 != null ) {
          return false;
        }
      } else if ( !keyStream2.equals( other.keyStream2 ) ) {
        return false;
      }
      return true;
    }

    @Override
    public Object clone() {
      try {
        return (KeyField) super.clone();
      } catch ( CloneNotSupportedException e ) {
        throw new RuntimeException( e );
      }
    }
  }
}
