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


package org.pentaho.di.trans.steps.delete;

import java.util.List;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionDeep;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
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
import org.pentaho.di.trans.step.*;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This class takes care of deleting values in a table using a certain condition and values for input.
 *
 * @author Tom, Matt
 * @since 28-March-2006
 */
@InjectionSupported( localizationPrefix = "Delete.Injection.", groups = "FIELDS" )
public class DeleteMeta extends BaseDatabaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = DeleteMeta.class; // for i18n purposes, needed by Translator2!!

  private static final String TAG_4_SPACES = "    ";
  private static final String TAG_6_SPACES = "      ";
  private static final String TAG_8_SPACES = "        ";
  private static final String TAG_CONNECTION = "connection";
  private static final String TAG_COMMIT = "commit";
  private static final String TAG_LOOKUP = "lookup";
  private static final String TAG_SCHEMA = "schema";
  private static final String TAG_TABLE = "table";
  private static final String TAG_KEY = "key";
  private static final String TAG_FIELD = "field";
  private static final String TAG_NAME = "name";
  private static final String TAG_NAME2 = "name2";
  private static final String TAG_CONDITION = "condition";
  private static final String TAG_KEY_FIELD = "key_field";
  private static final String TAG_KEY_NAME = "key_name";
  private static final String TAG_KEY_NAME2 = "key_name2";
  private static final String TAG_KEY_CONDITION = "key_condition";
  private static final String TAG_ID_CONNECTION = "id_connection";

  private DatabaseMeta databaseMeta;

  /** The target schema name */
  @Injection( name = "TARGET_SCHEMA", required = true )
  private String schemaName;

  /** The lookup table name */
  @Injection( name = "TARGET_TABLE", required = true )
  private String tableName;

  /** database connection */
  @Injection( name = "CONNECTIONNAME", required = true )
  public void metaSetConnection( String connectionName ) throws KettleException {
    setConnection( connectionName );
  }

  @InjectionDeep
  private KeyFields[] keyFields = {};

  /** Commit size for inserts/updates */
  @Injection( name = "COMMIT_SIZE" )
  private String commitSize;

  public DeleteMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the commitSize.
   */
  public String getCommitSizeVar() {
    return commitSize;
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

  public KeyFields[] getKeyFields() {
    return keyFields;
  }

  public void setKeyFields( KeyFields[] keyFields ) {
    this.keyFields = keyFields;
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

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode, databases );
  }

  public void allocate( int nrkeys ) {
    keyFields = new KeyFields[nrkeys];
    for ( int i = 0; i < nrkeys; i++ ) {
      keyFields[i] = new KeyFields();
    }
  }

  public Object clone() {
    DeleteMeta retval = (DeleteMeta) super.clone();
    int nrkeys = keyFields.length;

    retval.allocate( nrkeys );

    for ( int i = 0; i < nrkeys; i++ ) {
      retval.keyFields[i] = (KeyFields) keyFields[i].clone();
    }

    return retval;
  }

  private void readData( Node stepnode, List<DatabaseMeta> databases ) throws KettleXMLException {
    try {
      String csize;
      int nrkeys;

      String con = XMLHandler.getTagValue( stepnode, TAG_CONNECTION );
      databaseMeta = DatabaseMeta.findDatabase( databases, con );
      csize = XMLHandler.getTagValue( stepnode, TAG_COMMIT );
      commitSize = ( csize != null ) ? csize : "0";
      schemaName = XMLHandler.getTagValue( stepnode, TAG_LOOKUP, TAG_SCHEMA );
      tableName = XMLHandler.getTagValue( stepnode, TAG_LOOKUP, TAG_TABLE );

      Node lookup = XMLHandler.getSubNode( stepnode, TAG_LOOKUP );
      nrkeys = XMLHandler.countNodes( lookup, TAG_KEY );

      allocate( nrkeys );

      for ( int i = 0; i < nrkeys; i++ ) {
        Node knode = XMLHandler.getSubNodeByNr( lookup, TAG_KEY, i );

        keyFields[i].setKeyStream( XMLHandler.getTagValue( knode, TAG_NAME ) );
        keyFields[i].setKeyLookup( XMLHandler.getTagValue( knode, TAG_FIELD ) );
        keyFields[i].setKeyCondition( XMLHandler.getTagValue( knode, TAG_CONDITION ) );
        if ( keyFields[i].getKeyCondition() == null ) {
          keyFields[i].setKeyCondition( "=" );
        }
        keyFields[i].setKeyStream2( XMLHandler.getTagValue( knode, TAG_NAME2 ) );
      }

    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
              PKG, "DeleteMeta.Exception.UnableToReadStepInfoFromXML" ), e );
    }
  }

  public void setDefault() {
    databaseMeta = null;
    commitSize = "100";
    schemaName = "";
    tableName = BaseMessages.getString( PKG, "DeleteMeta.DefaultTableName.Label" );

    int nrkeys = 0;

    allocate( nrkeys );
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 500 );

    retval.append( TAG_4_SPACES ).append(
                    XMLHandler.addTagValue( TAG_CONNECTION, databaseMeta == null ? "" : databaseMeta.getName() ) );
    retval.append( TAG_4_SPACES ).append( XMLHandler.addTagValue( TAG_COMMIT, commitSize ) );
    retval.append( TAG_4_SPACES + "<lookup>" ).append( Const.CR );
    retval.append( TAG_6_SPACES ).append( XMLHandler.addTagValue( TAG_SCHEMA, schemaName ) );
    retval.append( TAG_6_SPACES ).append( XMLHandler.addTagValue( TAG_TABLE, tableName ) );

    for ( int i = 0; i < keyFields.length; i++ ) {
      retval.append( TAG_6_SPACES + "<key>" ).append( Const.CR );
      retval.append( TAG_8_SPACES ).append( XMLHandler.addTagValue( TAG_NAME, keyFields[i].getKeyStream() ) );
      retval.append( TAG_8_SPACES ).append( XMLHandler.addTagValue( TAG_FIELD, keyFields[i].getKeyLookup() ) );
      retval.append( TAG_8_SPACES ).append( XMLHandler.addTagValue( TAG_CONDITION, keyFields[i].getKeyCondition() ) );
      retval.append( TAG_8_SPACES ).append( XMLHandler.addTagValue( TAG_NAME2, keyFields[i].getKeyStream2() ) );
      retval.append( TAG_6_SPACES + "</key>" ).append( Const.CR );
    }

    retval.append( TAG_4_SPACES + "</lookup>" ).append( Const.CR );

    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      databaseMeta = rep.loadDatabaseMetaFromStepAttribute( id_step, TAG_ID_CONNECTION, databases );

      commitSize = rep.getStepAttributeString( id_step, TAG_COMMIT );
      if ( commitSize == null ) {
        long comSz = -1;
        try {
          comSz = rep.getStepAttributeInteger( id_step, TAG_COMMIT );
        } catch ( Exception ex ) {
          commitSize = "100";
        }
        if ( comSz >= 0 ) {
          commitSize = Long.toString( comSz );
        }
      }
      schemaName = rep.getStepAttributeString( id_step, TAG_SCHEMA );
      tableName = rep.getStepAttributeString( id_step, TAG_TABLE );

      int nrkeys = rep.countNrStepAttributes( id_step, TAG_KEY_FIELD );

      allocate( nrkeys );

      for ( int i = 0; i < nrkeys; i++ ) {
        keyFields[i].setKeyStream( rep.getStepAttributeString( id_step, i, TAG_KEY_NAME ) );
        keyFields[i].setKeyLookup( rep.getStepAttributeString( id_step, i, TAG_KEY_FIELD ) );
        keyFields[i].setKeyCondition( rep.getStepAttributeString( id_step, i, TAG_KEY_CONDITION ) );
        keyFields[i].setKeyStream2( rep.getStepAttributeString( id_step, i, TAG_KEY_NAME2 ) );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
              PKG, "DeleteMeta.Exception.UnexpectedErrorInReadingStepInfo" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveDatabaseMetaStepAttribute( id_transformation, id_step, TAG_ID_CONNECTION, databaseMeta );
      rep.saveStepAttribute( id_transformation, id_step, TAG_COMMIT, commitSize );
      rep.saveStepAttribute( id_transformation, id_step, TAG_SCHEMA, schemaName );
      rep.saveStepAttribute( id_transformation, id_step, TAG_TABLE, tableName );

      for ( int i = 0; i < keyFields.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, TAG_KEY_NAME, keyFields[i].getKeyStream() );
        rep.saveStepAttribute( id_transformation, id_step, i, TAG_KEY_FIELD, keyFields[i].getKeyLookup() );
        rep.saveStepAttribute( id_transformation, id_step, i, TAG_KEY_CONDITION, keyFields[i].getKeyCondition() );
        rep.saveStepAttribute( id_transformation, id_step, i, TAG_KEY_NAME2, keyFields[i].getKeyStream2() );
      }

      // Also, save the step-database relationship!
      if ( databaseMeta != null ) {
        rep.insertStepDatabase( id_transformation, id_step, databaseMeta.getObjectId() );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "DeleteMeta.Exception.UnableToSaveStepInfo" )
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
                          PKG, "DeleteMeta.CheckResult.TablenameOK" ), stepMeta );
          remarks.add( cr );

          boolean first = true;
          boolean error_found = false;
          error_message = "";

          // Check fields in table
          RowMetaInterface r = db.getTableFieldsMeta( schemaName, tableName );
          if ( r != null ) {
            cr =
                    new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
                            PKG, "DeleteMeta.CheckResult.VisitTableSuccessfully" ), stepMeta );
            remarks.add( cr );

            for ( int i = 0; i < keyFields.length; i++ ) {
              String lufield = keyFields[i].getKeyLookup();

              ValueMetaInterface v = r.searchValueMeta( lufield );
              if ( v == null ) {
                if ( first ) {
                  first = false;
                  error_message +=
                          BaseMessages.getString( PKG, "DeleteMeta.CheckResult.MissingCompareFieldsInTargetTable" )
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
                              PKG, "DeleteMeta.CheckResult.FoundLookupFields" ), stepMeta );
            }
            remarks.add( cr );
          } else {
            error_message = BaseMessages.getString( PKG, "DeleteMeta.CheckResult.CouldNotReadTableInfo" );
            cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
            remarks.add( cr );
          }
        }

        // Look up fields in the input stream <prev>
        if ( prev != null && prev.size() > 0 ) {
          cr =
                  new CheckResult(
                          CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
                          PKG, "DeleteMeta.CheckResult.ConnectedStepSuccessfully", String.valueOf( prev.size() ) ),
                          stepMeta );
          remarks.add( cr );

          boolean first = true;
          error_message = "";
          boolean error_found = false;

          for ( int i = 0; i < keyFields.length; i++ ) {
            ValueMetaInterface v = prev.searchValueMeta( keyFields[i].getKeyStream() );
            if ( v == null ) {
              if ( first ) {
                first = false;
                error_message += BaseMessages.getString( PKG, "DeleteMeta.CheckResult.MissingFields" ) + Const.CR;
              }
              error_found = true;
              error_message += "\t\t"
                      + ( keyFields[i].getKeyStream() == null ? "" : keyFields[i].getKeyStream() ) + Const.CR;
            }
          }
          for ( int i = 0; i < keyFields.length; i++ ) {
            if ( keyFields[i].getKeyStream2() != null ) {
              ValueMetaInterface v = prev.searchValueMeta( keyFields[i].getKeyStream2() );
              if ( v == null ) {
                if ( first ) {
                  first = false;
                  error_message +=
                          BaseMessages.getString( PKG, "DeleteMeta.CheckResult.MissingFields2" ) + Const.CR;
                }
                error_found = true;
                error_message += "\t\t" + keyFields[i].getKeyStream2() + Const.CR;
              }
            }
          }
          if ( error_found ) {
            cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
          } else {
            cr =
                    new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
                            PKG, "DeleteMeta.CheckResult.AllFieldsFound" ), stepMeta );
          }
          remarks.add( cr );

          // How about the fields to insert/update the table with?
          first = true;
          error_found = false;
          error_message = "";
        } else {
          error_message = BaseMessages.getString( PKG, "DeleteMeta.CheckResult.MissingFields3" ) + Const.CR;
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
          remarks.add( cr );
        }
      } catch ( KettleException e ) {
        error_message = BaseMessages.getString( PKG, "DeleteMeta.CheckResult.DatabaseError" ) + e.getMessage();
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      } finally {
        db.close();
      }
    } else {
      error_message = BaseMessages.getString( PKG, "DeleteMeta.CheckResult.InvalidConnection" );
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
              new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
                      PKG, "DeleteMeta.CheckResult.StepReceivingInfo" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
              new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
                      PKG, "DeleteMeta.CheckResult.NoInputReceived" ), stepMeta );
      remarks.add( cr );
    }
  }

  public SQLStatement getSQLStatements( TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
                                        Repository repository, IMetaStore metaStore ) {
    SQLStatement retval = new SQLStatement( stepMeta.getName(), databaseMeta, null ); // default: nothing to do!

    if ( databaseMeta != null ) {
      if ( prev != null && prev.size() > 0 ) {
        if ( !Utils.isEmpty( tableName ) ) {
          Database db = new Database( loggingObject, databaseMeta );
          db.shareVariablesWith( transMeta );
          try {
            db.connect();

            String schemaTable = databaseMeta.getQuotedSchemaTableCombination( schemaName, tableName );
            String cr_table = db.getDDL( schemaTable, prev, null, false, null, true );

            String cr_index = "";
            String[] idx_fields = null;

            if ( keyFields != null && keyFields.length > 0 && keyFields[0].getKeyLookup() != null ) {
              idx_fields = new String[ keyFields.length];
              for ( int i = 0; i < keyFields.length; i++ ) {
                idx_fields[i] = keyFields[i].getKeyLookup();
              }
            } else {
              retval.setError( BaseMessages.getString( PKG, "DeleteMeta.CheckResult.KeyFieldsRequired" ) );
            }

            // Key lookup dimensions...
            if ( idx_fields != null && idx_fields.length > 0 && !db.checkIndexExists( schemaTable, idx_fields ) ) {
              String indexname = "idx_" + tableName + "_lookup";
              cr_index =
                      db.getCreateIndexStatement(
                              schemaName, tableName, indexname, idx_fields, false, false, false, true );
            }

            String sql = cr_table + cr_index;
            if ( sql.length() == 0 ) {
              retval.setSQL( null );
            } else {
              retval.setSQL( sql );
            }
          } catch ( KettleException e ) {
            retval.setError( BaseMessages.getString( PKG, "DeleteMeta.Returnvalue.ErrorOccurred" )
                    + e.getMessage() );
          }
        } else {
          retval.setError( BaseMessages.getString( PKG, "DeleteMeta.Returnvalue.NoTableDefinedOnConnection" ) );
        }
      } else {
        retval.setError( BaseMessages.getString( PKG, "DeleteMeta.Returnvalue.NoReceivingAnyFields" ) );
      }
    } else {
      retval.setError( BaseMessages.getString( PKG, "DeleteMeta.Returnvalue.NoConnectionDefined" ) );
    }

    return retval;
  }

  public void analyseImpact( List<DatabaseImpact> impact, TransMeta transMeta, StepMeta stepMeta,
                             RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, Repository repository,
                             IMetaStore metaStore ) throws KettleStepException {
    if ( prev != null ) {
      // Lookup: we do a lookup on the natural keys
      for ( int i = 0; i < keyFields.length; i++ ) {
        ValueMetaInterface v = prev.searchValueMeta( keyFields[i].getKeyStream() );

        DatabaseImpact ii =
                new DatabaseImpact(
                        DatabaseImpact.TYPE_IMPACT_DELETE, transMeta.getName(), stepMeta.getName(), databaseMeta
                        .getDatabaseName(), tableName, keyFields[i].getKeyLookup(), keyFields[i].getKeyStream(),
                        v != null ? v.getOrigin() : "?", "", "Type = " + v.toStringMeta() );
        impact.add( ii );
      }
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
                                Trans trans ) {
    return new Delete( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  public StepDataInterface getStepData() {
    return new DeleteData();
  }

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

  public boolean supportsErrorHandling() {
    return true;
  }

  public void setConnection( String connectionName ) throws KettleException {
    // Get the databases from the Database connection list this is required by
    // MDI injection of the connection name
    databaseMeta = DatabaseMeta.findDatabase( getParentStepMeta().getParentTransMeta().getDatabases(), connectionName );
    if ( databaseMeta == null ) {
      String errMsg = BaseMessages.getString( PKG, "DeleteMeta.Exception.ConnectionUndefined",
              getParentStepMeta().getName(), connectionName );
      logError( errMsg );
      throw new KettleException( errMsg );
    }
  }

  public static class KeyFields implements Cloneable {
    /** field in table */
    @Injection( name = "TABLE_NAME_FIELD", group = "FIELDS", required = true )
    private String keyLookup;

    /** Comparator: =, <>, BETWEEN, ... */
    @Injection( name = "COMPARATOR", group = "FIELDS", required = true )
    private String keyCondition;

    @Injection( name = "STREAM_FIELDNAME_1", group = "FIELDS", required = false )
    private String keyStream;

    /** Extra field for between... */
    @Injection( name = "STREAM_FIELDNAME_2", group = "FIELDS", required = false )
    private String keyStream2;

    public String getKeyStream() {
      return keyStream;
    }

    public void setKeyStream( String keyStream ) {
      this.keyStream = keyStream;
    }

    public String getKeyLookup() {
      return keyLookup;
    }

    public void setKeyLookup( String keyLookup ) {
      this.keyLookup = keyLookup;
    }

    public String getKeyCondition() {
      return keyCondition;
    }

    public void setKeyCondition( String keyCondition ) {
      this.keyCondition = keyCondition;
    }

    public String getKeyStream2() {
      return keyStream2;
    }

    public void setKeyStream2( String keyStream2 ) {
      this.keyStream2 = keyStream2;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ( ( keyLookup == null ) ? 0 : keyLookup.hashCode() );
      result = prime * result + ( ( keyCondition == null ) ? 0 : keyCondition.hashCode() );
      result = prime * result + ( ( keyStream == null ) ? 0 : keyStream.hashCode() );
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
      KeyFields other = (KeyFields) obj;
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
        return super.clone();
      } catch ( CloneNotSupportedException e ) {
        throw new RuntimeException( e );
      }
    }
  }
}
