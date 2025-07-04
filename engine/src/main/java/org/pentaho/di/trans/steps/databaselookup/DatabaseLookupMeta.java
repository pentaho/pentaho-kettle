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


package org.pentaho.di.trans.steps.databaselookup;

import java.util.Arrays;
import java.util.List;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.ProvidesModelerMeta;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
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

public class DatabaseLookupMeta extends BaseStepMeta implements StepMetaInterface,
    ProvidesModelerMeta {
  private static Class<?> PKG = DatabaseLookupMeta.class; // for i18n purposes, needed by Translator2!!

  private static final String TAG_CACHE = "cache";
  private static final String TAG_CACHE_LOAD_ALL = "cache_load_all";
  private static final String TAG_CACHE_SIZE = "cache_size";
  private static final String TAG_CONDITION = "condition";
  private static final String TAG_CONNECTION = "connection";
  private static final String TAG_DEFAULT = "default";
  private static final String TAG_EAT_ROW_ON_FAILURE = "eat_row_on_failure";
  private static final String TAG_FAIL_ON_MULTIPLE = "fail_on_multiple";
  private static final String TAG_FIELD = "field";
  private static final String TAG_KEY = "key";
  private static final String TAG_LOOKUP = "lookup";
  private static final String TAG_LOOKUP_KEY_CONDITION = "lookup_key_condition";
  private static final String TAG_LOOKUP_KEY_FIELD = "lookup_key_field";
  private static final String TAG_LOOKUP_KEY_NAME = "lookup_key_name";
  private static final String TAG_LOOKUP_KEY_NAME2 = "lookup_key_name2";
  private static final String TAG_LOOKUP_ORDERBY = "lookup_orderby";
  private static final String TAG_LOOKUP_SCHEMA = "lookup_schema";
  private static final String TAG_LOOKUP_TABLE = "lookup_table";
  private static final String TAG_NAME = "name";
  private static final String TAG_NAME2 = "name2";
  private static final String TAG_ORDERBY = "orderby";
  private static final String TAG_RENAME = "rename";
  private static final String TAG_RETURN_VALUE_DEFAULT = "return_value_default";
  private static final String TAG_RETURN_VALUE_NAME = "return_value_name";
  private static final String TAG_RETURN_VALUE_RENAME = "return_value_rename";
  private static final String TAG_RETURN_VALUE_TYPE = "return_value_type";
  private static final String TAG_SCHEMA = "schema";
  private static final String TAG_TABLE = "table";
  private static final String TAG_TYPE = "type";
  private static final String TAG_VALUE = "value";
  private static final String SPACES8 = "        ";
  private static final String SPACES6 = "      ";
  private static final String SPACES4 = "    ";

  public static final String[] conditionStrings = new String[] {
    "=", "<>", "<", "<=", ">", ">=", "LIKE", "BETWEEN", "IS NULL", "IS NOT NULL", };

  public static final int CONDITION_EQ = 0;
  public static final int CONDITION_NE = 1;
  public static final int CONDITION_LT = 2;
  public static final int CONDITION_LE = 3;
  public static final int CONDITION_GT = 4;
  public static final int CONDITION_GE = 5;
  public static final int CONDITION_LIKE = 6;
  public static final int CONDITION_BETWEEN = 7;
  public static final int CONDITION_IS_NULL = 8;
  public static final int CONDITION_IS_NOT_NULL = 9;

  /** what's the lookup schema name? */
  private String schemaName;

  /** what's the lookup table? */
  private String tablename;

  /** database connection */
  private DatabaseMeta databaseMeta;

  /** which field in input stream to compare with? */
  private String[] streamKeyField1;

  /** Extra field for between... */
  private String[] streamKeyField2;

  /** Comparator: =, <>, BETWEEN, ... */
  private String[] keyCondition;

  /** field in table */
  private String[] tableKeyField;

  /** return these field values after lookup */
  private String[] returnValueField;

  /** new name for value ... */
  private String[] returnValueNewName;

  /** default value in case not found... */
  private String[] returnValueDefault;

  /** type of default value */
  private int[] returnValueDefaultType;

  /** order by clause... */
  private String orderByClause;

  /** Cache values we look up --> faster */
  private boolean cached;

  /** Limit the cache size to this! */
  private int cacheSize;

  /** Flag to make it load all data into the cache at startup */
  private boolean loadingAllDataInCache;

  /** Have the lookup fail if multiple results were found, renders the orderByClause useless */
  private boolean failingOnMultipleResults;

  /** Have the lookup eat the incoming row when nothing gets found */
  private boolean eatingRowOnLookupFailure;

  public DatabaseLookupMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the cached.
   */
  public boolean isCached() {
    return cached;
  }

  /**
   * @param cached
   *          The cached to set.
   */
  public void setCached( boolean cached ) {
    this.cached = cached;
  }

  /**
   * @return Returns the cacheSize.
   */
  public int getCacheSize() {
    return cacheSize;
  }

  /**
   * @param cacheSize
   *          The cacheSize to set.
   */
  public void setCacheSize( int cacheSize ) {
    this.cacheSize = cacheSize;
  }

  /**
   * @return Returns the database.
   */
  @Override
  public DatabaseMeta getDatabaseMeta() {
    return databaseMeta;
  }

  @Override
  public String getTableName() {
    return tablename;
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
   * @return Returns the orderByClause.
   */
  public String getOrderByClause() {
    return orderByClause;
  }

  /**
   * @param orderByClause
   *          The orderByClause to set.
   */
  public void setOrderByClause( String orderByClause ) {
    this.orderByClause = orderByClause;
  }

  /**
   * @return Returns the returnValueDefault.
   */
  public String[] getReturnValueDefault() {
    return returnValueDefault;
  }

  /**
   * @param returnValueDefault
   *          The returnValueDefault to set.
   */
  public void setReturnValueDefault( String[] returnValueDefault ) {
    this.returnValueDefault = returnValueDefault;
  }

  /**
   * @return Returns the returnValueDefaultType.
   */
  public int[] getReturnValueDefaultType() {
    return returnValueDefaultType;
  }

  /**
   * @param returnValueDefaultType
   *          The returnValueDefaultType to set.
   */
  public void setReturnValueDefaultType( int[] returnValueDefaultType ) {
    this.returnValueDefaultType = returnValueDefaultType;
  }

  /**
   * @return Returns the returnValueField.
   */
  public String[] getReturnValueField() {
    return returnValueField;
  }

  /**
   * @param returnValueField
   *          The returnValueField to set.
   */
  public void setReturnValueField( String[] returnValueField ) {
    this.returnValueField = returnValueField;
  }

  /**
   * @return Returns the returnValueNewName.
   */
  public String[] getReturnValueNewName() {
    return returnValueNewName;
  }

  /**
   * @param returnValueNewName
   *          The returnValueNewName to set.
   */
  public void setReturnValueNewName( String[] returnValueNewName ) {
    this.returnValueNewName = returnValueNewName;
  }

  /**
   * @return Returns the streamKeyField1.
   */
  public String[] getStreamKeyField1() {
    return streamKeyField1;
  }

  /**
   * @param streamKeyField1
   *          The streamKeyField1 to set.
   */
  public void setStreamKeyField1( String[] streamKeyField1 ) {
    this.streamKeyField1 = streamKeyField1;
  }

  /**
   * @return Returns the streamKeyField2.
   */
  public String[] getStreamKeyField2() {
    return streamKeyField2;
  }

  /**
   * @param streamKeyField2
   *          The streamKeyField2 to set.
   */
  public void setStreamKeyField2( String[] streamKeyField2 ) {
    this.streamKeyField2 = streamKeyField2;
  }

  /**
   * @return Returns the tableKeyField.
   */
  public String[] getTableKeyField() {
    return tableKeyField;
  }

  /**
   * @param tableKeyField
   *          The tableKeyField to set.
   */
  public void setTableKeyField( String[] tableKeyField ) {
    this.tableKeyField = tableKeyField;
  }

  /**
   * @return Returns the tablename.
   */
  public String getTablename() {
    return tablename;
  }

  /**
   * @param tablename
   *          The tablename to set.
   */
  public void setTablename( String tablename ) {
    this.tablename = tablename;
  }

  /**
   * @return Returns the failOnMultipleResults.
   */
  public boolean isFailingOnMultipleResults() {
    return failingOnMultipleResults;
  }

  /**
   * @param failOnMultipleResults
   *          The failOnMultipleResults to set.
   */
  public void setFailingOnMultipleResults( boolean failOnMultipleResults ) {
    this.failingOnMultipleResults = failOnMultipleResults;
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    streamKeyField1 = null;
    returnValueField = null;

    readData( stepnode, databases );
  }

  public void allocate( int nrKeys, int nrValues ) {
    streamKeyField1 = new String[ nrKeys ];
    tableKeyField = new String[ nrKeys ];
    keyCondition = new String[ nrKeys ];
    streamKeyField2 = new String[ nrKeys ];
    returnValueField = new String[ nrValues ];
    returnValueNewName = new String[ nrValues ];
    returnValueDefault = new String[ nrValues ];
    returnValueDefaultType = new int[ nrValues ];
  }

  @Override
  public Object clone() {
    DatabaseLookupMeta retval = (DatabaseLookupMeta) super.clone();

    int nrKeys = streamKeyField1.length;
    int nrValues = returnValueField.length;

    retval.allocate( nrKeys, nrValues );

    if ( nrKeys != 0 ) {
      System.arraycopy( streamKeyField1, 0, retval.streamKeyField1, 0, nrKeys );
      System.arraycopy( tableKeyField, 0, retval.tableKeyField, 0, nrKeys );
      System.arraycopy( keyCondition, 0, retval.keyCondition, 0, nrKeys );
      System.arraycopy( streamKeyField2, 0, retval.streamKeyField2, 0, nrKeys );
    }

    if ( nrValues != 0 ) {
      System.arraycopy( returnValueField, 0, retval.returnValueField, 0, nrValues );
      System.arraycopy( returnValueNewName, 0, retval.returnValueNewName, 0, nrValues );
      System.arraycopy( returnValueDefault, 0, retval.returnValueDefault, 0, nrValues );
      System.arraycopy( returnValueDefaultType, 0, retval.returnValueDefaultType, 0, nrValues );
    }

    return retval;
  }

  private void readData( Node stepnode, List<DatabaseMeta> databases ) throws KettleXMLException {
    try {
      String con = XMLHandler.getTagValue( stepnode, TAG_CONNECTION );
      databaseMeta = DatabaseMeta.findDatabase( databases, con );
      cached = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_CACHE ) );
      loadingAllDataInCache = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_CACHE_LOAD_ALL ) );
      cacheSize = Const.toInt( XMLHandler.getTagValue( stepnode, TAG_CACHE_SIZE ), 0 );
      schemaName = XMLHandler.getTagValue( stepnode, TAG_LOOKUP, TAG_SCHEMA );
      tablename = XMLHandler.getTagValue( stepnode, TAG_LOOKUP, TAG_TABLE );

      Node lookup = XMLHandler.getSubNode( stepnode, TAG_LOOKUP );

      int nrKeys = XMLHandler.countNodes( lookup, TAG_KEY );
      int nrValues = XMLHandler.countNodes( lookup, TAG_VALUE );

      allocate( nrKeys, nrValues );

      for ( int i = 0; i < nrKeys; i++ ) {
        Node knode = XMLHandler.getSubNodeByNr( lookup, TAG_KEY, i );

        streamKeyField1[ i ] = XMLHandler.getTagValue( knode, TAG_NAME );
        tableKeyField[ i ] = XMLHandler.getTagValue( knode, TAG_FIELD );
        keyCondition[ i ] = XMLHandler.getTagValue( knode, TAG_CONDITION );
        if ( keyCondition[ i ] == null ) {
          keyCondition[ i ] = "=";
        }
        streamKeyField2[ i ] = XMLHandler.getTagValue( knode, TAG_NAME2 );
      }

      for ( int i = 0; i < nrValues; i++ ) {
        Node vnode = XMLHandler.getSubNodeByNr( lookup, TAG_VALUE, i );

        returnValueField[ i ] = XMLHandler.getTagValue( vnode, TAG_NAME );
        returnValueNewName[ i ] = XMLHandler.getTagValue( vnode, TAG_RENAME );
        if ( returnValueNewName[ i ] == null ) {
          returnValueNewName[ i ] = returnValueField[ i ]; // default: the same name!
        }
        returnValueDefault[ i ] = XMLHandler.getTagValue( vnode, TAG_DEFAULT );
        returnValueDefaultType[ i ] = ValueMetaFactory.getIdForValueMeta( XMLHandler.getTagValue( vnode, TAG_TYPE ) );
        if ( returnValueDefaultType[ i ] < 0 ) {
          returnValueDefaultType[ i ] = ValueMetaInterface.TYPE_STRING;
        }
      }
      orderByClause = XMLHandler.getTagValue( lookup, TAG_ORDERBY ); // Optional, can by null
      failingOnMultipleResults = "Y".equalsIgnoreCase( XMLHandler.getTagValue( lookup, TAG_FAIL_ON_MULTIPLE ) );
      eatingRowOnLookupFailure = "Y".equalsIgnoreCase( XMLHandler.getTagValue( lookup, TAG_EAT_ROW_ON_FAILURE ) );
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "DatabaseLookupMeta.ERROR0001.UnableToLoadStepFromXML" ), e );
    }
  }

  @Override
  public void setDefault() {
    streamKeyField1 = null;
    returnValueField = null;
    databaseMeta = null;
    cached = false;
    cacheSize = 0;
    schemaName = "";
    tablename = BaseMessages.getString( PKG, "DatabaseLookupMeta.Default.TableName" );

    allocate( 0, 0 );

    orderByClause = "";
    failingOnMultipleResults = false;
    eatingRowOnLookupFailure = false;
  }

  @Override
  public void getFields( Bowl bowl, RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
      VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    if ( Utils.isEmpty( info ) || info[0] == null ) { // null or length 0 : no info from database
      for ( int i = 0; i < getReturnValueNewName().length; i++ ) {
        try {
          ValueMetaInterface v =
              ValueMetaFactory.createValueMeta( getReturnValueNewName()[ i ], getReturnValueDefaultType()[ i ] );
          v.setOrigin( name );
          row.addValueMeta( v );
        } catch ( Exception e ) {
          throw new KettleStepException( e );
        }
      }
    } else {
      for ( int i = 0; i < returnValueNewName.length; i++ ) {
        ValueMetaInterface v = info[0].searchValueMeta( returnValueField[ i ] );
        if ( v != null ) {
          ValueMetaInterface copy = v.clone(); // avoid renaming other value meta - PDI-9844
          copy.setName( returnValueNewName[ i ] );
          copy.setOrigin( name );
          row.addValueMeta( copy );
        }
      }
    }
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 500 );

    retval.append( SPACES4 ).append(
        XMLHandler.addTagValue( TAG_CONNECTION, databaseMeta == null ? "" : databaseMeta.getName() ) );
    retval.append( SPACES4 ).append( XMLHandler.addTagValue( TAG_CACHE, cached ) );
    retval.append( SPACES4 ).append( XMLHandler.addTagValue( TAG_CACHE_LOAD_ALL, loadingAllDataInCache ) );
    retval.append( SPACES4 ).append( XMLHandler.addTagValue( TAG_CACHE_SIZE, cacheSize ) );
    retval.append( "    <lookup>" ).append( Const.CR );
    retval.append( SPACES6 ).append( XMLHandler.addTagValue( TAG_SCHEMA, schemaName ) );
    retval.append( SPACES6 ).append( XMLHandler.addTagValue( TAG_TABLE, tablename ) );
    retval.append( SPACES6 ).append( XMLHandler.addTagValue( TAG_ORDERBY, orderByClause ) );
    retval.append( SPACES6 ).append( XMLHandler.addTagValue( TAG_FAIL_ON_MULTIPLE, failingOnMultipleResults ) );
    retval.append( SPACES6 ).append( XMLHandler.addTagValue( TAG_EAT_ROW_ON_FAILURE, eatingRowOnLookupFailure ) );

    for ( int i = 0; i < streamKeyField1.length; i++ ) {
      retval.append( "      <key>" ).append( Const.CR );
      retval.append( SPACES8 ).append( XMLHandler.addTagValue( TAG_NAME, streamKeyField1[ i ] ) );
      retval.append( SPACES8 ).append( XMLHandler.addTagValue( TAG_FIELD, tableKeyField[ i ] ) );
      retval.append( SPACES8 ).append( XMLHandler.addTagValue( TAG_CONDITION, keyCondition[ i ] ) );
      retval.append( SPACES8 ).append( XMLHandler.addTagValue( TAG_NAME2, streamKeyField2[ i ] ) );
      retval.append( "      </key>" ).append( Const.CR );
    }

    for ( int i = 0; i < returnValueField.length; i++ ) {
      retval.append( "      <value>" ).append( Const.CR );
      retval.append( SPACES8 ).append( XMLHandler.addTagValue( TAG_NAME, returnValueField[ i ] ) );
      retval.append( SPACES8 ).append( XMLHandler.addTagValue( TAG_RENAME, returnValueNewName[ i ] ) );
      retval.append( SPACES8 ).append( XMLHandler.addTagValue( TAG_DEFAULT, returnValueDefault[ i ] ) );
      retval.append( SPACES8 ).append(
          XMLHandler.addTagValue( TAG_TYPE, ValueMetaFactory.getValueMetaName( returnValueDefaultType[ i ] ) ) );
      retval.append( "      </value>" ).append( Const.CR );
    }

    retval.append( "    </lookup>" ).append( Const.CR );

    return retval.toString();
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      databaseMeta = rep.loadDatabaseMetaFromStepAttribute( id_step, "id_connection", databases );

      cached = rep.getStepAttributeBoolean( id_step, TAG_CACHE );
      loadingAllDataInCache = rep.getStepAttributeBoolean( id_step, TAG_CACHE_LOAD_ALL );
      cacheSize = (int) rep.getStepAttributeInteger( id_step, TAG_CACHE_SIZE );
      schemaName = rep.getStepAttributeString( id_step, TAG_LOOKUP_SCHEMA );
      tablename = rep.getStepAttributeString( id_step, TAG_LOOKUP_TABLE );
      orderByClause = rep.getStepAttributeString( id_step, TAG_LOOKUP_ORDERBY );
      failingOnMultipleResults = rep.getStepAttributeBoolean( id_step, TAG_FAIL_ON_MULTIPLE );
      eatingRowOnLookupFailure = rep.getStepAttributeBoolean( id_step, TAG_EAT_ROW_ON_FAILURE );

      int nrKeys = rep.countNrStepAttributes( id_step, TAG_LOOKUP_KEY_FIELD );
      int nrValues = rep.countNrStepAttributes( id_step, TAG_RETURN_VALUE_NAME );

      allocate( nrKeys, nrValues );

      for ( int i = 0; i < nrKeys; i++ ) {
        streamKeyField1[ i ] = rep.getStepAttributeString( id_step, i, TAG_LOOKUP_KEY_NAME );
        tableKeyField[ i ] = rep.getStepAttributeString( id_step, i, TAG_LOOKUP_KEY_FIELD );
        keyCondition[ i ] = rep.getStepAttributeString( id_step, i, TAG_LOOKUP_KEY_CONDITION );
        streamKeyField2[ i ] = rep.getStepAttributeString( id_step, i, TAG_LOOKUP_KEY_NAME2 );
      }

      for ( int i = 0; i < nrValues; i++ ) {
        returnValueField[ i ] = rep.getStepAttributeString( id_step, i, TAG_RETURN_VALUE_NAME );
        returnValueNewName[ i ] = rep.getStepAttributeString( id_step, i, TAG_RETURN_VALUE_RENAME );
        returnValueDefault[ i ] = rep.getStepAttributeString( id_step, i, TAG_RETURN_VALUE_DEFAULT );
        returnValueDefaultType[ i ] =
          ValueMetaFactory.getIdForValueMeta( rep.getStepAttributeString( id_step, i, TAG_RETURN_VALUE_TYPE ) );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "DatabaseLookupMeta.ERROR0002.UnexpectedErrorReadingFromTheRepository" ), e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId idTransformation, ObjectId idStep ) throws KettleException {
    try {
      rep.saveDatabaseMetaStepAttribute( idTransformation, idStep, "id_connection", databaseMeta );
      rep.saveStepAttribute( idTransformation, idStep, TAG_CACHE, cached );
      rep.saveStepAttribute( idTransformation, idStep, TAG_CACHE_LOAD_ALL, loadingAllDataInCache );
      rep.saveStepAttribute( idTransformation, idStep, TAG_CACHE_SIZE, cacheSize );
      rep.saveStepAttribute( idTransformation, idStep, TAG_LOOKUP_SCHEMA, schemaName );
      rep.saveStepAttribute( idTransformation, idStep, TAG_LOOKUP_TABLE, tablename );
      rep.saveStepAttribute( idTransformation, idStep, TAG_LOOKUP_ORDERBY, orderByClause );
      rep.saveStepAttribute( idTransformation, idStep, TAG_FAIL_ON_MULTIPLE, failingOnMultipleResults );
      rep.saveStepAttribute( idTransformation, idStep, TAG_EAT_ROW_ON_FAILURE, eatingRowOnLookupFailure );

      for ( int i = 0; i < streamKeyField1.length; i++ ) {
        rep.saveStepAttribute( idTransformation, idStep, i, TAG_LOOKUP_KEY_NAME, streamKeyField1[ i ] );
        rep.saveStepAttribute( idTransformation, idStep, i, TAG_LOOKUP_KEY_FIELD, tableKeyField[ i ] );
        rep.saveStepAttribute( idTransformation, idStep, i, TAG_LOOKUP_KEY_CONDITION, keyCondition[ i ] );
        rep.saveStepAttribute( idTransformation, idStep, i, TAG_LOOKUP_KEY_NAME2, streamKeyField2[ i ] );
      }

      for ( int i = 0; i < returnValueField.length; i++ ) {
        rep.saveStepAttribute( idTransformation, idStep, i, TAG_RETURN_VALUE_NAME, returnValueField[ i ] );
        rep.saveStepAttribute( idTransformation, idStep, i, TAG_RETURN_VALUE_RENAME, returnValueNewName[ i ] );
        rep.saveStepAttribute( idTransformation, idStep, i, TAG_RETURN_VALUE_DEFAULT, returnValueDefault[ i ] );
        rep.saveStepAttribute( idTransformation, idStep, i, TAG_RETURN_VALUE_TYPE, ValueMetaFactory
          .getValueMetaName( returnValueDefaultType[ i ] ) );
      }

      // Also, save the step-database relationship!
      if ( databaseMeta != null ) {
        rep.insertStepDatabase( idTransformation, idStep, databaseMeta.getObjectId() );
      }
    } catch ( Exception e ) {
      throw new KettleException(
        BaseMessages.getString( PKG, "DatabaseLookupMeta.ERROR0003.UnableToSaveStepToRepository" ) + idStep, e );
    }
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
      RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
      Repository repository, IMetaStore metaStore ) {
    CheckResult cr;
    String errorMessage = "";

    if ( databaseMeta != null ) {
      try ( Database db = new Database( loggingObject, databaseMeta ) ) {
        db.shareVariablesWith( transMeta );
        databases = new Database[] { db }; // Keep track of this one for cancelQuery

        db.connect();

        if ( !Utils.isEmpty( tablename ) ) {
          boolean first = true;
          boolean errorFound = false;
          errorMessage = "";

          String schemaTable =
            databaseMeta.getQuotedSchemaTableCombination( db.environmentSubstitute( schemaName ), db
              .environmentSubstitute( tablename ) );
          RowMetaInterface r = db.getTableFields( schemaTable );

          if ( r != null ) {
            // Check the keys used to do the lookup...

            for ( String keyField : tableKeyField ) {
              ValueMetaInterface v = r.searchValueMeta( keyField );
              if ( v == null ) {
                if ( first ) {
                  first = false;
                  errorMessage +=
                    BaseMessages.getString( PKG, "DatabaseLookupMeta.Check.MissingCompareFieldsInLookupTable" )
                      + Const.CR;
                }
                errorFound = true;
                errorMessage += "\t\t" + keyField + Const.CR;
              }
            }
            if ( errorFound ) {
              cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, errorMessage, stepMeta );
            } else {
              cr =
                new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
                  PKG, "DatabaseLookupMeta.Check.AllLookupFieldsFoundInTable" ), stepMeta );
            }
            remarks.add( cr );

            // Also check the returned values!

            for ( String returnField : returnValueField ) {
              ValueMetaInterface v = r.searchValueMeta( returnField );
              if ( v == null ) {
                if ( first ) {
                  first = false;
                  errorMessage +=
                    BaseMessages.getString( PKG, "DatabaseLookupMeta.Check.MissingReturnFieldsInLookupTable" )
                      + Const.CR;
                }
                errorFound = true;
                errorMessage += "\t\t" + returnField + Const.CR;
              }
            }
            if ( errorFound ) {
              cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, errorMessage, stepMeta );
            } else {
              cr =
                new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
                  PKG, "DatabaseLookupMeta.Check.AllReturnFieldsFoundInTable" ), stepMeta );
            }
            remarks.add( cr );

          } else {
            errorMessage = BaseMessages.getString( PKG, "DatabaseLookupMeta.Check.CouldNotReadTableInfo" );
            cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, errorMessage, stepMeta );
            remarks.add( cr );
          }
        }

        // Look up fields in the input stream <prev>
        if ( prev != null && prev.size() > 0 ) {
          boolean first = true;
          errorMessage = "";
          boolean errorFound = false;

          for ( String streamKeyField : streamKeyField1 ) {
            ValueMetaInterface v = prev.searchValueMeta( streamKeyField );
            if ( v == null ) {
              if ( first ) {
                first = false;
                errorMessage +=
                  BaseMessages.getString( PKG, "DatabaseLookupMeta.Check.MissingFieldsNotFoundInInput" )
                    + Const.CR;
              }
              errorFound = true;
              errorMessage += "\t\t" + streamKeyField + Const.CR;
            }
          }
          if ( errorFound ) {
            cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, errorMessage, stepMeta );
          } else {
            cr =
              new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
                PKG, "DatabaseLookupMeta.Check.AllFieldsFoundInInput" ), stepMeta );
          }
          remarks.add( cr );
        } else {
          errorMessage =
            BaseMessages.getString( PKG, "DatabaseLookupMeta.Check.CouldNotReadFromPreviousSteps" ) + Const.CR;
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, errorMessage, stepMeta );
          remarks.add( cr );
        }
      } catch ( KettleDatabaseException dbe ) {
        errorMessage =
          BaseMessages.getString( PKG, "DatabaseLookupMeta.Check.DatabaseErrorWhileChecking" )
            + dbe.getMessage();
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, errorMessage, stepMeta );
        remarks.add( cr );
      }
    } else {
      errorMessage = BaseMessages.getString( PKG, "DatabaseLookupMeta.Check.MissingConnectionError" );
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, errorMessage, stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "DatabaseLookupMeta.Check.StepIsReceivingInfoFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "DatabaseLookupMeta.Check.NoInputReceivedFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    }
  }

  @Override
  public RowMetaInterface getTableFields() {
    RowMetaInterface fields = null;
    if ( databaseMeta != null ) {
      try ( Database db = new Database( loggingObject, databaseMeta ) ) {
      databases = new Database[] { db }; // Keep track of this one for cancelQuery
        db.connect();
        String tableName = databaseMeta.environmentSubstitute( tablename );
        String schemaTable = databaseMeta.getQuotedSchemaTableCombination( schemaName, tableName );
        fields = db.getTableFields( schemaTable );

      } catch ( KettleDatabaseException dbe ) {
        logError( BaseMessages.getString( PKG, "DatabaseLookupMeta.ERROR0004.ErrorGettingTableFields" )
          + dbe.getMessage() );
      }
    }
    return fields;
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
      TransMeta transMeta, Trans trans ) {
    return new DatabaseLookup( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new DatabaseLookupData();
  }

  @Override
  public void analyseImpact( List<DatabaseImpact> impact, TransMeta transMeta, StepMeta stepInfo,
      RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, Repository repository,
      IMetaStore metaStore ) {
    // The keys are read-only...
    String remarkKey = BaseMessages.getString( PKG, "DatabaseLookupMeta.Impact.Key" );
    for ( int i = 0; i < streamKeyField1.length; i++ ) {
      ValueMetaInterface v = prev.searchValueMeta( streamKeyField1[ i ] );
      impact.add( new DatabaseImpact(
        DatabaseImpact.TYPE_IMPACT_READ, transMeta.getName(), stepInfo.getName(), databaseMeta
        .getDatabaseName(), tablename, tableKeyField[ i ], streamKeyField1[ i ], v != null
        ? v.getOrigin() : "?", Const.EMPTY_STRING, remarkKey ) );
    }

    // The Return fields are read-only too...
    String remarkReturnValue = BaseMessages.getString( PKG, "DatabaseLookupMeta.Impact.ReturnValue" );
    for ( String returnValue : returnValueField ) {
      impact.add( new DatabaseImpact( DatabaseImpact.TYPE_IMPACT_READ, transMeta.getName(), stepInfo.getName(),
        databaseMeta.getDatabaseName(), tablename, returnValue, Const.EMPTY_STRING, Const.EMPTY_STRING,
        Const.EMPTY_STRING, remarkReturnValue ) );
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

  /**
   * @return Returns the eatingRowOnLookupFailure.
   */
  public boolean isEatingRowOnLookupFailure() {
    return eatingRowOnLookupFailure;
  }

  /**
   * @param eatingRowOnLookupFailure
   *          The eatingRowOnLookupFailure to set.
   */
  public void setEatingRowOnLookupFailure( boolean eatingRowOnLookupFailure ) {
    this.eatingRowOnLookupFailure = eatingRowOnLookupFailure;
  }

  /**
   * @return the schemaName
   */
  @Override
  public String getSchemaName() {
    return schemaName;
  }

  @Override
  public String getMissingDatabaseConnectionInformationMessage() {
    return null;
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
   * @return the loadingAllDataInCache
   */
  public boolean isLoadingAllDataInCache() {
    return loadingAllDataInCache;
  }

  /**
   * @param loadingAllDataInCache
   *          the loadingAllDataInCache to set
   */
  public void setLoadingAllDataInCache( boolean loadingAllDataInCache ) {
    this.loadingAllDataInCache = loadingAllDataInCache;
  }

  @Override
  public RowMeta getRowMeta( StepDataInterface stepData ) {
    return (RowMeta) ( (DatabaseLookupData) stepData ).returnMeta;
  }

  @Override
  public List<String> getDatabaseFields() {
    return Arrays.asList( returnValueField );
  }

  @Override
  public List<String> getStreamFields() {
    return Arrays.asList( returnValueNewName );
  }
}
