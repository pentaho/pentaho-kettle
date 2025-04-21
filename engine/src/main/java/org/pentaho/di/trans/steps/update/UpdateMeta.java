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


package org.pentaho.di.trans.steps.update;

import com.google.common.primitives.Ints;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.AfterInjection;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.util.Utils;
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

import java.util.List;

/*
 * Created on 26-apr-2003
 *
 */
@InjectionSupported( localizationPrefix = "UpdateMeta.Injection.", groups = { "KEYS", "UPDATES" } )
public class UpdateMeta extends BaseDatabaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = UpdateMeta.class; // for i18n purposes, needed by Translator2!!

  private List<DatabaseMeta> databases;

  /** The lookup table name */
  @Injection( name = "SCHEMA_NAME" )
  private String schemaName;

  /** The lookup table name */
  @Injection( name = "TABLE_NAME" )
  private String tableName;

  /** database connection */
  private DatabaseMeta databaseMeta;

  /** which field in input stream to compare with? */
  @Injection( name = "KEY_STREAM", group = "KEYS" )
  private String[] keyStream;

  /** field in table */
  @Injection( name = "KEY_LOOKUP", group = "KEYS" )
  private String[] keyLookup;

  /** Comparator: =, <>, BETWEEN, ... */
  @Injection( name = "KEY_CONDITION", group = "KEYS" )
  private String[] keyCondition;

  /** Extra field for between... */
  @Injection( name = "KEY_STREAM2", group = "KEYS" )
  private String[] keyStream2;

  /** Field value to update after lookup */
  @Injection( name = "UPDATE_LOOKUP", group = "UPDATES" )
  private String[] updateLookup;

  /** Stream name to update value with */
  @Injection( name = "UPDATE_STREAM", group = "UPDATES" )
  private String[] updateStream;

  /** Commit size for inserts/updates */
  @Injection( name = "COMMIT_SIZE" )
  private String commitSize;

  /** update errors are ignored if this flag is set to true */
  @Injection( name = "IGNORE_LOOKUP_FAILURE" )
  private boolean errorIgnored;

  /** adds a boolean field to the output indicating success of the update */
  @Injection( name = "FLAG_FIELD" )
  private String ignoreFlagField;

  /** adds a boolean field to skip lookup and directly update selected fields */
  @Injection( name = "SKIP_LOOKUP" )
  private boolean skipLookup;

  /** Flag to indicate the use of batch updates, enabled by default but disabled for backward compatibility */
  @Injection( name = "BATCH_UPDATE" )
  private boolean useBatchUpdate;

  @Injection( name = "CONNECTIONNAME" )
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
    // this happens when the step is created via API and no setDefaults was called
    commitSize = ( commitSize == null ) ? "0" : commitSize;
    return Integer.parseInt( vs.environmentSubstitute( commitSize ) );
  }

  /**
   * @param commitSize
   *          The commitSize to set.
   *          @deprecated use public void setCommitSize( String commitSize ) instead
   */
  @Deprecated
  public void setCommitSize( int commitSize ) {
    this.commitSize = Integer.toString( commitSize );
  }

  /**
   * @param commitSize
   *          The commitSize to set.
   */
  public void setCommitSize( String commitSize ) {
    this.commitSize = commitSize;
  }

  /**
   * @return Returns the skipLookup.
   */
  public boolean isSkipLookup() {
    return skipLookup;
  }

  /**
   * @param skipLookup
   *          The skipLookup to set.
   */
  public void setSkipLookup( boolean skipLookup ) {
    this.skipLookup = skipLookup;
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
   * @return Returns the keyCondition.
   */
  public String[] getKeyCondition() {
    return keyCondition;
  }

  /**
   * @param keyCondition
   *          The keyCondition to set.
   */
  public void setKeyCondition( String[] keyCondition ) {
    this.keyCondition = keyCondition;
  }

  /**
   * @return Returns the keyLookup.
   */
  public String[] getKeyLookup() {
    return keyLookup;
  }

  /**
   * @param keyLookup
   *          The keyLookup to set.
   */
  public void setKeyLookup( String[] keyLookup ) {
    this.keyLookup = keyLookup;
  }

  /**
   * @return Returns the keyStream.
   */
  public String[] getKeyStream() {
    return keyStream;
  }

  /**
   * @param keyStream
   *          The keyStream to set.
   */
  public void setKeyStream( String[] keyStream ) {
    this.keyStream = keyStream;
  }

  /**
   * @return Returns the keyStream2.
   */
  public String[] getKeyStream2() {
    return keyStream2;
  }

  /**
   * @param keyStream2
   *          The keyStream2 to set.
   */
  public void setKeyStream2( String[] keyStream2 ) {
    this.keyStream2 = keyStream2;
  }

  /**
   * @return Returns the tableName.
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * @param tableName
   *          The tableName to set.
   */
  public void setTableName( String tableName ) {
    this.tableName = tableName;
  }

  /**
   * @return Returns the updateLookup.
   */
  public String[] getUpdateLookup() {
    return updateLookup;
  }

  /**
   * @param updateLookup
   *          The updateLookup to set.
   */
  public void setUpdateLookup( String[] updateLookup ) {
    this.updateLookup = updateLookup;
  }

  /**
   * @return Returns the updateStream.
   */
  public String[] getUpdateStream() {
    return updateStream;
  }

  /**
   * @param updateStream
   *          The updateStream to set.
   */
  public void setUpdateStream( String[] updateStream ) {
    this.updateStream = updateStream;
  }

  /**
   * @return Returns the ignoreError.
   */
  public boolean isErrorIgnored() {
    return errorIgnored;
  }

  /**
   * @param ignoreError
   *          The ignoreError to set.
   */
  public void setErrorIgnored( boolean ignoreError ) {
    this.errorIgnored = ignoreError;
  }

  /**
   * @return Returns the ignoreFlagField.
   */
  public String getIgnoreFlagField() {
    return ignoreFlagField;
  }

  /**
   * @param ignoreFlagField
   *          The ignoreFlagField to set.
   */
  public void setIgnoreFlagField( String ignoreFlagField ) {
    this.ignoreFlagField = ignoreFlagField;
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    this.databases = databases;
    readData( stepnode, databases );
  }

  public void allocate( int nrkeys, int nrvalues ) {
    keyStream = new String[nrkeys];
    keyLookup = new String[nrkeys];
    keyCondition = new String[nrkeys];
    keyStream2 = new String[nrkeys];
    updateLookup = new String[nrvalues];
    updateStream = new String[nrvalues];
  }

  @Override
  public Object clone() {
    UpdateMeta retval = (UpdateMeta) super.clone();
    int nrkeys = keyStream.length;
    int nrvalues = updateLookup.length;

    retval.allocate( nrkeys, nrvalues );

    System.arraycopy( keyStream, 0, retval.keyStream, 0, nrkeys );
    System.arraycopy( keyLookup, 0, retval.keyLookup, 0, nrkeys );
    System.arraycopy( keyCondition, 0, retval.keyCondition, 0, nrkeys );
    System.arraycopy( keyStream2, 0, retval.keyStream2, 0, nrkeys );

    System.arraycopy( updateLookup, 0, retval.updateLookup, 0, nrvalues );
    System.arraycopy( updateStream, 0, retval.updateStream, 0, nrvalues );
    return retval;
  }

  private void readData( Node stepnode, List<DatabaseMeta> databases ) throws KettleXMLException {
    try {
      String csize;
      int nrkeys, nrvalues;

      String con = XMLHandler.getTagValue( stepnode, "connection" );
      databaseMeta = DatabaseMeta.findDatabase( databases, con );
      csize = XMLHandler.getTagValue( stepnode, "commit" );
      commitSize = ( csize == null ) ? "0" : csize;
      useBatchUpdate = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "use_batch" ) );
      skipLookup = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "skip_lookup" ) );
      errorIgnored = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "error_ignored" ) );
      ignoreFlagField = XMLHandler.getTagValue( stepnode, "ignore_flag_field" );
      schemaName = XMLHandler.getTagValue( stepnode, "lookup", "schema" );
      tableName = XMLHandler.getTagValue( stepnode, "lookup", "table" );

      Node lookup = XMLHandler.getSubNode( stepnode, "lookup" );
      nrkeys = XMLHandler.countNodes( lookup, "key" );
      nrvalues = XMLHandler.countNodes( lookup, "value" );

      allocate( nrkeys, nrvalues );

      for ( int i = 0; i < nrkeys; i++ ) {
        Node knode = XMLHandler.getSubNodeByNr( lookup, "key", i );

        keyStream[i] = XMLHandler.getTagValue( knode, "name" );
        keyLookup[i] = XMLHandler.getTagValue( knode, "field" );
        keyCondition[i] = XMLHandler.getTagValue( knode, "condition" );
        if ( keyCondition[i] == null ) {
          keyCondition[i] = "=";
        }
        keyStream2[i] = XMLHandler.getTagValue( knode, "name2" );
      }

      for ( int i = 0; i < nrvalues; i++ ) {
        Node vnode = XMLHandler.getSubNodeByNr( lookup, "value", i );

        updateLookup[i] = XMLHandler.getTagValue( vnode, "name" );
        updateStream[i] = XMLHandler.getTagValue( vnode, "rename" );
        if ( updateStream[i] == null ) {
          updateStream[i] = updateLookup[i]; // default: the same name!
        }
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "UpdateMeta.Exception.UnableToReadStepInfoFromXML" ), e );
    }
  }

  @Override
  public void setDefault() {
    skipLookup = false;
    keyStream = null;
    updateLookup = null;
    databaseMeta = null;
    commitSize = "100";
    schemaName = "";
    tableName = BaseMessages.getString( PKG, "UpdateMeta.DefaultTableName" );

    int nrkeys = 0;
    int nrvalues = 0;

    allocate( nrkeys, nrvalues );

    for ( int i = 0; i < nrkeys; i++ ) {
      keyLookup[i] = "age";
      keyCondition[i] = "BETWEEN";
      keyStream[i] = "age_from";
      keyStream2[i] = "age_to";
    }

    for ( int i = 0; i < nrvalues; i++ ) {
      updateLookup[i] = BaseMessages.getString( PKG, "UpdateMeta.ColumnName.ReturnField" ) + i;
      updateStream[i] = BaseMessages.getString( PKG, "UpdateMeta.ColumnName.NewName" ) + i;
    }
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval
      .append( "    " + XMLHandler.addTagValue( "connection", databaseMeta == null ? "" : databaseMeta.getName() ) );
    retval.append( "    " + XMLHandler.addTagValue( "skip_lookup", skipLookup ) );
    retval.append( "    " + XMLHandler.addTagValue( "commit", commitSize ) );
    retval.append( "    " + XMLHandler.addTagValue( "use_batch", useBatchUpdate ) );
    retval.append( "    " + XMLHandler.addTagValue( "error_ignored", errorIgnored ) );
    retval.append( "    " + XMLHandler.addTagValue( "ignore_flag_field", ignoreFlagField ) );
    retval.append( "    <lookup>" + Const.CR );
    retval.append( "      " + XMLHandler.addTagValue( "schema", schemaName ) );
    retval.append( "      " + XMLHandler.addTagValue( "table", tableName ) );

    for ( int i = 0; i < keyStream.length; i++ ) {
      retval.append( "      <key>" + Const.CR );
      retval.append( "        " + XMLHandler.addTagValue( "name", keyStream[i] ) );
      retval.append( "        " + XMLHandler.addTagValue( "field", keyLookup[i] ) );
      retval.append( "        " + XMLHandler.addTagValue( "condition", keyCondition[i] ) );
      retval.append( "        " + XMLHandler.addTagValue( "name2", keyStream2[i] ) );
      retval.append( "        </key>" + Const.CR );
    }

    for ( int i = 0; i < updateLookup.length; i++ ) {
      retval.append( "      <value>" + Const.CR );
      retval.append( "        " + XMLHandler.addTagValue( "name", updateLookup[i] ) );
      retval.append( "        " + XMLHandler.addTagValue( "rename", updateStream[i] ) );
      retval.append( "        </value>" + Const.CR );
    }

    retval.append( "      </lookup>" + Const.CR );

    return retval.toString();
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    this.databases = databases;
    try {
      databaseMeta = rep.loadDatabaseMetaFromStepAttribute( id_step, "id_connection", databases );
      skipLookup = rep.getStepAttributeBoolean( id_step, "skip_lookup" );
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
      useBatchUpdate = rep.getStepAttributeBoolean( id_step, "use_batch" );
      schemaName = rep.getStepAttributeString( id_step, "schema" );
      tableName = rep.getStepAttributeString( id_step, "table" );

      errorIgnored = rep.getStepAttributeBoolean( id_step, "error_ignored" );
      ignoreFlagField = rep.getStepAttributeString( id_step, "ignore_flag_field" );

      int nrKeyName = rep.countNrStepAttributes( id_step, "key_name" );
      int nrKeyField = rep.countNrStepAttributes( id_step, "key_field" );
      int nrKeyCondition = rep.countNrStepAttributes( id_step, "key_condition" );
      int nrKeyName2 = rep.countNrStepAttributes( id_step, "key_name2" );

      int nrkeys = Ints.max( nrKeyName, nrKeyCondition, nrKeyField, nrKeyName2 );

      int nrValueName = rep.countNrStepAttributes( id_step, "value_name" );
      int nrValueRename = rep.countNrStepAttributes( id_step, "value_rename" );

      int nrvalues = Ints.max( nrValueName, nrValueRename );

      allocate( nrkeys, nrvalues );

      for ( int i = 0; i < nrkeys; i++ ) {
        keyStream[i] = rep.getStepAttributeString( id_step, i, "key_name" );
        keyLookup[i] = rep.getStepAttributeString( id_step, i, "key_field" );
        keyCondition[i] = rep.getStepAttributeString( id_step, i, "key_condition" );
        keyStream2[i] = rep.getStepAttributeString( id_step, i, "key_name2" );
      }

      for ( int i = 0; i < nrvalues; i++ ) {
        updateLookup[i] = rep.getStepAttributeString( id_step, i, "value_name" );
        updateStream[i] = rep.getStepAttributeString( id_step, i, "value_rename" );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "UpdateMeta.Exception.UnexpectedErrorReadingStepInfoFromRepository" ), e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveDatabaseMetaStepAttribute( id_transformation, id_step, "id_connection", databaseMeta );
      rep.saveStepAttribute( id_transformation, id_step, "skip_lookup", skipLookup );
      rep.saveStepAttribute( id_transformation, id_step, "commit", commitSize );
      rep.saveStepAttribute( id_transformation, id_step, "use_batch", useBatchUpdate );
      rep.saveStepAttribute( id_transformation, id_step, "schema", schemaName );
      rep.saveStepAttribute( id_transformation, id_step, "table", tableName );

      rep.saveStepAttribute( id_transformation, id_step, "error_ignored", errorIgnored );
      rep.saveStepAttribute( id_transformation, id_step, "ignore_flag_field", ignoreFlagField );

      for ( int i = 0; i < keyStream.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "key_name", keyStream[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "key_field", keyLookup[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "key_condition", keyCondition[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "key_name2", keyStream2[i] );
      }

      for ( int i = 0; i < updateLookup.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "value_name", updateLookup[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "value_rename", updateStream[i] );
      }

      // Also, save the step-database relationship!
      if ( databaseMeta != null ) {
        rep.insertStepDatabase( id_transformation, id_step, databaseMeta.getObjectId() );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "UpdateMeta.Exception.UnableToSaveStepInfoToRepository" )
        + id_step, e );
    }
  }

  @Override
  public void getFields( Bowl bowl, RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    if ( ignoreFlagField != null && ignoreFlagField.length() > 0 ) {
      ValueMetaInterface v = new ValueMetaBoolean( ignoreFlagField );
      v.setOrigin( name );

      row.addValueMeta( v );
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
      db.shareVariablesWith( transMeta );
      try {
        db.connect();

        if ( !Utils.isEmpty( tableName ) ) {
          cr =
            new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
              PKG, "UpdateMeta.CheckResult.TableNameOK" ), stepMeta );
          remarks.add( cr );

          boolean first = true;
          boolean error_found = false;
          error_message = "";

          // Check fields in table
          RowMetaInterface r = db.getTableFieldsMeta( schemaName, tableName );
          if ( r != null ) {
            cr =
              new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
                PKG, "UpdateMeta.CheckResult.TableExists" ), stepMeta );
            remarks.add( cr );

            for ( int i = 0; i < keyLookup.length; i++ ) {
              String lufield = keyLookup[i];

              ValueMetaInterface v = r.searchValueMeta( lufield );
              if ( v == null ) {
                if ( first ) {
                  first = false;
                  error_message +=
                    BaseMessages.getString( PKG, "UpdateMeta.CheckResult.MissingCompareFieldsInTargetTable" )
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
                  PKG, "UpdateMeta.CheckResult.AllLookupFieldsFound" ), stepMeta );
            }
            remarks.add( cr );

            // How about the fields to insert/update in the table?
            first = true;
            error_found = false;
            error_message = "";

            for ( int i = 0; i < updateLookup.length; i++ ) {
              String lufield = updateLookup[i];

              ValueMetaInterface v = r.searchValueMeta( lufield );
              if ( v == null ) {
                if ( first ) {
                  first = false;
                  error_message +=
                    BaseMessages.getString( PKG, "UpdateMeta.CheckResult.MissingFieldsToUpdateInTargetTable" )
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
                  PKG, "UpdateMeta.CheckResult.AllFieldsToUpdateFoundInTargetTable" ), stepMeta );
            }
            remarks.add( cr );
          } else {
            error_message = BaseMessages.getString( PKG, "UpdateMeta.CheckResult.CouldNotReadTableInfo" );
            cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
            remarks.add( cr );
          }
        }

        // Look up fields in the input stream <prev>
        if ( prev != null && prev.size() > 0 ) {
          cr =
            new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
              PKG, "UpdateMeta.CheckResult.StepReceivingDatas", prev.size() + "" ), stepMeta );
          remarks.add( cr );

          boolean first = true;
          error_message = "";
          boolean error_found = false;

          for ( int i = 0; i < keyStream.length; i++ ) {
            ValueMetaInterface v = prev.searchValueMeta( keyStream[i] );
            if ( v == null ) {
              if ( first ) {
                first = false;
                error_message +=
                  BaseMessages.getString( PKG, "UpdateMeta.CheckResult.MissingFieldsInInput" ) + Const.CR;
              }
              error_found = true;
              error_message += "\t\t" + keyStream[i] + Const.CR;
            }
          }
          for ( int i = 0; i < keyStream2.length; i++ ) {
            if ( keyStream2[i] != null && keyStream2[i].length() > 0 ) {
              ValueMetaInterface v = prev.searchValueMeta( keyStream2[i] );
              if ( v == null ) {
                if ( first ) {
                  first = false;
                  error_message +=
                    BaseMessages.getString( PKG, "UpdateMeta.CheckResult.MissingFieldsInInput2" ) + Const.CR;
                }
                error_found = true;
                error_message += "\t\t" + keyStream[i] + Const.CR;
              }
            }
          }
          if ( error_found ) {
            cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
          } else {
            cr =
              new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
                PKG, "UpdateMeta.CheckResult.AllFieldsFoundInInput" ), stepMeta );
          }
          remarks.add( cr );

          // How about the fields to insert/update the table with?
          first = true;
          error_found = false;
          error_message = "";

          for ( int i = 0; i < updateStream.length; i++ ) {
            String lufield = updateStream[i];

            ValueMetaInterface v = prev.searchValueMeta( lufield );
            if ( v == null ) {
              if ( first ) {
                first = false;
                error_message +=
                  BaseMessages.getString( PKG, "UpdateMeta.CheckResult.MissingInputStreamFields" ) + Const.CR;
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
                PKG, "UpdateMeta.CheckResult.AllFieldsFoundInInput2" ), stepMeta );
          }
          remarks.add( cr );
        } else {
          error_message = BaseMessages.getString( PKG, "UpdateMeta.CheckResult.MissingFieldsInInput3" ) + Const.CR;
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
          remarks.add( cr );
        }
      } catch ( KettleException e ) {
        error_message =
          BaseMessages.getString( PKG, "UpdateMeta.CheckResult.DatabaseErrorOccurred" ) + e.getMessage();
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      } finally {
        db.close();
      }
    } else {
      error_message = BaseMessages.getString( PKG, "UpdateMeta.CheckResult.InvalidConnection" );
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "UpdateMeta.CheckResult.StepReceivingInfoFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "UpdateMeta.CheckResult.NoInputError" ), stepMeta );
      remarks.add( cr );
    }
  }

  @Override
  public SQLStatement getSQLStatements( TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
    Repository repository, IMetaStore metaStore ) throws KettleStepException  {
    SQLStatement retval = new SQLStatement( stepMeta.getName(), databaseMeta, null ); // default: nothing to do!

    if ( databaseMeta != null ) {
      if ( prev != null && prev.size() > 0 ) {
        // Copy the row
        RowMetaInterface tableFields = RowMetaUtils.getRowMetaForUpdate( prev, keyLookup, keyStream,
            updateLookup, updateStream );
        if ( !Utils.isEmpty( tableName ) ) {
          String schemaTable = databaseMeta.getQuotedSchemaTableCombination( schemaName, tableName );

          Database db = new Database( loggingObject, databaseMeta );
          db.shareVariablesWith( transMeta );
          try {
            db.connect();

            if ( getIgnoreFlagField() != null && getIgnoreFlagField().length() > 0 ) {
              prev.addValueMeta( new ValueMetaBoolean( getIgnoreFlagField() ) );
            }

            String cr_table = db.getDDL( schemaTable, tableFields, null, false, null, true );

            String cr_index = "";
            String[] idx_fields = null;

            if ( keyLookup != null && keyLookup.length > 0 ) {
              idx_fields = new String[keyLookup.length];
              for ( int i = 0; i < keyLookup.length; i++ ) {
                idx_fields[i] = keyLookup[i];
              }
            } else {
              retval.setError( BaseMessages.getString( PKG, "UpdateMeta.CheckResult.MissingKeyFields" ) );
            }

            // Key lookup dimensions...
            if ( idx_fields != null
              && idx_fields.length > 0 && !db.checkIndexExists( schemaTable, idx_fields ) ) {
              String indexname = "idx_" + tableName + "_lookup";
              cr_index =
                db.getCreateIndexStatement(
                  schemaTable, indexname, idx_fields, false, false, false, true );
            }

            String sql = cr_table + cr_index;
            if ( sql.length() == 0 ) {
              retval.setSQL( null );
            } else {
              retval.setSQL( sql );
            }
          } catch ( KettleException e ) {
            retval.setError( BaseMessages.getString( PKG, "UpdateMeta.ReturnValue.ErrorOccurred" )
              + e.getMessage() );
          }
        } else {
          retval.setError( BaseMessages.getString( PKG, "UpdateMeta.ReturnValue.NoTableDefinedOnConnection" ) );
        }
      } else {
        retval.setError( BaseMessages.getString( PKG, "UpdateMeta.ReturnValue.NotReceivingAnyFields" ) );
      }
    } else {
      retval.setError( BaseMessages.getString( PKG, "UpdateMeta.ReturnValue.NoConnectionDefined" ) );
    }

    return retval;
  }

  @Override
  public void analyseImpact( List<DatabaseImpact> impact, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, Repository repository,
    IMetaStore metaStore ) throws KettleStepException {
    if ( prev != null ) {
      // Lookup: we do a lookup on the natural keys
      for ( int i = 0; i < keyLookup.length; i++ ) {
        ValueMetaInterface v = prev.searchValueMeta( keyStream[i] );

        DatabaseImpact ii =
          new DatabaseImpact(
            DatabaseImpact.TYPE_IMPACT_READ, transMeta.getName(), stepMeta.getName(), databaseMeta
              .getDatabaseName(), tableName, keyLookup[i], keyStream[i],
            v != null ? v.getOrigin() : "?", "", "Type = " + v.toStringMeta() );
        impact.add( ii );
      }

      // Update fields : read/write
      for ( int i = 0; i < updateLookup.length; i++ ) {
        ValueMetaInterface v = prev.searchValueMeta( updateStream[i] );

        DatabaseImpact ii =
          new DatabaseImpact(
            DatabaseImpact.TYPE_IMPACT_UPDATE, transMeta.getName(), stepMeta.getName(), databaseMeta
              .getDatabaseName(), tableName, updateLookup[i], updateStream[i], v != null
              ? v.getOrigin() : "?", "", "Type = " + v.toStringMeta() );
        impact.add( ii );
      }
    }
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
    Trans trans ) {
    return new Update( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new UpdateData();
  }

  @Override
  public DatabaseMeta[] getUsedDatabaseConnections() {
    if ( databaseMeta != null ) {
      return new DatabaseMeta[] { databaseMeta };
    } else {
      return super.getUsedDatabaseConnections();
    }
  }

  /**
   * @return the schemaName
   */
  public String getSchemaName() {
    return schemaName;
  }

  /**
   * @param schemaName
   *          the schemaName to set
   */
  public void setSchemaName( String schemaName ) {
    this.schemaName = schemaName;
  }

  @Override
  public boolean supportsErrorHandling() {
    return true;
  }

  /**
   * @return the useBatchUpdate
   */
  public boolean useBatchUpdate() {
    return useBatchUpdate;
  }

  /**
   * @param useBatchUpdate
   *          the useBatchUpdate to set
   */
  public void setUseBatchUpdate( boolean useBatchUpdate ) {
    this.useBatchUpdate = useBatchUpdate;
  }

  /**
   * If we use injection we can have different arrays lengths.
   * We need synchronize them for consistency behavior with UI
   */
  @AfterInjection
  public void afterInjectionSynchronization() {
    int nrFields = ( keyStream == null ) ? -1 : keyStream.length;
    if ( nrFields <= 0 ) {
      return;
    }
    String[][] rtn = Utils.normalizeArrays( nrFields, keyLookup, keyCondition, keyStream2 );
    keyLookup = rtn[ 0 ];
    keyCondition = rtn[ 1 ];
    keyStream2 = rtn[ 2 ];

    nrFields = updateLookup.length;
    rtn = Utils.normalizeArrays( nrFields, updateStream );
    updateStream = rtn[ 0 ];
  }

}
