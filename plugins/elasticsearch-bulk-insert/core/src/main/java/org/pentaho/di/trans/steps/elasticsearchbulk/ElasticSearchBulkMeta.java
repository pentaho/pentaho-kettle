/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.elasticsearchbulk;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionDeep;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

@Step( id = "ElasticSearchBulk", i18nPackageName = "org.pentaho.di.trans.steps.elasticsearch",
        name = "ElasticSearchBulk.TypeLongDesc.ElasticSearchBulk",
        description = "ElasticSearchBulk.TypeTooltipDesc.ElasticSearchBulk",
        categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Deprecated", image = "ESB.svg",
        documentationUrl = "Products/ElasticSearch_Bulk_Insert" )
@InjectionSupported( localizationPrefix = "ElasticSearchBulk.Injection." )
public class ElasticSearchBulkMeta extends BaseStepMeta implements StepMetaInterface {

  private static Class<?> PKG = ElasticSearchBulkMeta.class; // for i18n purposes

  /**
   * Serialization aids
   */
  private static class Dom {
    static final String TAG_GENERAL = "general";

    static final String TAG_INDEX = "index";
    static final String TAG_TYPE = "type";
    static final String TAG_IS_JSON = "isJson";
    static final String TAG_JSON_FIELD = "jsonField";

    static final String TAG_ID_IN_FIELD = "idField";
    static final String TAG_OVERWRITE_IF_EXISTS = "overwriteIfExists";

    static final String TAG_ID_OUT_FIELD = "idOutputField";
    static final String TAG_USE_OUTPUT = "useOutput";
    static final String TAG_STOP_ON_ERROR = "stopOnError";
    static final String TAG_TIMEOUT = "timeout";
    static final String TAG_TIMEOUT_UNIT = "timeoutUnit";
    static final String TAG_BATCH_SIZE = "batchSize";

    static final String TAG_FIELDS = "fields";
    static final String TAG_FIELD = "field";

    static final String TAG_NAME = "columnName";
    static final String TAG_TARGET = "targetName";

    static final String TAG_SERVERS = "servers";
    static final String TAG_SERVER = "server";
    static final String TAG_SERVER_ADDRESS = "address";
    static final String TAG_SERVER_PORT = "port";

    public static final String TAG_SETTINGS = "settings";
    public static final String TAG_SETTING = "setting";
    public static final String TAG_SETTING_NAME = "name";
    public static final String TAG_SETTING_VALUE = "value";

    static final String INDENT = "  ";
  }

  public static final int DEFAULT_BATCH_SIZE = 50000;
  public static final Long DEFAULT_TIMEOUT = 10L;
  public static final TimeUnit DEFAULT_TIMEOUT_UNIT = TimeUnit.SECONDS;
  public static final int DEFAULT_PORT = 9300;

  // ///////////
  // FIELDS //
  // /////////

  @Injection( name = "INDEX_NAME" )
  private String index = null;
  @Injection( name = "INDEX_TYPE" )
  private String type = null;
  @Injection( name = "JSON_INPUT" )
  private boolean isJsonInsert = false;
  @Injection( name = "JSON_FIELD" )
  private String jsonField = null;

  @Injection( name = "ID_FIELD" )
  private String idInField = null;
  @Injection( name = "OVERWRITE_IF_EXIST" )
  private boolean overWriteIfSameId = false;
  @Injection( name = "ID_OUTPUT_FIELD" )
  private String idOutField = null;
  @Injection( name = "USE_OUTPUT" )
  private boolean useOutput = false;
  @Injection( name = "STOP_ON_ERROR" )
  private boolean stopOnError = true;

  @Injection( name = "BATCH_SIZE" )
  private String batchSize;
  @Injection( name = "TIMEOUT_VALUE" )
  private String timeout;
  @Injection( name = "TIMEOUT_UNIT" )
  private TimeUnit timeoutUnit;

  @InjectionDeep( prefix = "SERVER" )
  List<Server> servers = new ArrayList<>();
  @InjectionDeep( prefix = "FIELD" )
  List<Field> fields = new ArrayList<>();
  @InjectionDeep( prefix = "SETTING" )
  List<Setting> settings = new ArrayList<>();
  // private List<InetSocketTransportAddress> servers = new ArrayList<InetSocketTransportAddress>();

  /**
   * fields to use in json generation
   */
  //private Map<String, String> fields = new HashMap<String, String>();
  //private Map<String, String> settings = new HashMap<String, String>();

  // ////////////////////
  // GETTERS/SETTERS //
  // //////////////////
  public String getJsonField() {
    return jsonField;
  }

  public void setJsonField( String jsonField ) {
    this.jsonField = StringUtils.isBlank( jsonField ) ? null : jsonField;
  }

  public String getIdOutField() {
    return idOutField;
  }

  public void setIdOutField( String idField ) {
    this.idOutField = StringUtils.isBlank( idField ) ? null : idField;
  }

  public boolean isJsonInsert() {
    return isJsonInsert;
  }

  public void setJsonInsert( boolean isJsonInsert ) {
    this.isJsonInsert = isJsonInsert;
  }

  public String getIndex() {
    return index;
  }

  public void setIndex( String index ) {
    this.index = index;
  }

  public String getType() {
    return type;
  }

  public void setType( String type ) {
    this.type = type;
  }

  public void setIdInField( String idInField ) {
    this.idInField = idInField;
  }

  public String getIdInField() {
    return idInField;
  }

  public void setOverWriteIfSameId( boolean overWriteIfSameId ) {
    this.overWriteIfSameId = overWriteIfSameId;
  }

  public boolean isOverWriteIfSameId() {
    return overWriteIfSameId;
  }

  public boolean isUseOutput() {
    return useOutput;
  }

  public void setUseOutput( boolean value ) {
    useOutput = value;
  }

  public boolean isStopOnError() {
    return stopOnError;
  }

  public void setStopOnError( boolean stopOnError ) {
    this.stopOnError = stopOnError;
  }

  public List<Field> getFields() {
    return Collections.unmodifiableList( fields );
  }

  public Map<String, String> getFieldsMap() {
    Map<String, String> result = new TreeMap<>();
    for ( Field f : fields ) {
      result.put( f.name, f.targetName );
    }
    return Collections.unmodifiableMap( result );
  }

  public void setFieldsMap( Map<String, String> values ) {
    clearFields();
    for ( String k : values.keySet() ) {
      addField( k, values.get( k ) );
    }
  }

  public void clearFields() {
    this.fields.clear();
  }

  public void addField( String inputName, String nameInJson ) {
    Field f = new Field();
    f.name = inputName;
    f.targetName = StringUtils.isBlank( nameInJson ) ? inputName : nameInJson;
    this.fields.add( f );
  }

  public List<Server> getServers() {
    return Collections.unmodifiableList( servers );
  }

  public void setServers( List<Server> values ) {
    servers.clear();
    servers.addAll( values );
  }

  public void clearServers() {
    servers.clear();
  }

  public void addServer( String addr, int port ) {
    Server s = new Server();
    s.address = addr;
    s.port = port;
    servers.add( s );
  }

  public Map<String, String> getSettingsMap() {
    Map<String, String> result = new TreeMap<>();
    for ( Setting s : settings ) {
      result.put( s.setting, s.value );
    }
    return Collections.unmodifiableMap( result );
  }

  public void setSettingsMap( Map<String, String> values ) {
    clearSettings();
    for ( String k : values.keySet() ) {
      addSetting( k, values.get( k ) );
    }
  }

  public void clearSettings() {
    settings.clear();
  }

  public void addSetting( String property, String value ) {
    if ( StringUtils.isNotBlank( property ) ) {
      Setting s = new Setting();
      s.setting = property;
      s.value = value;
      settings.add( s );
    }
  }

  /**
   * @param value The batch size to set
   */
  public void setBatchSize( String value ) {
    this.batchSize = value;
  }

  /**
   * @return Returns the batchSize.
   */
  public String getBatchSize() {
    return this.batchSize;
  }

  public int getBatchSizeInt( VariableSpace vars ) {
    return Const.toInt( vars.environmentSubstitute( this.batchSize ), DEFAULT_BATCH_SIZE );
  }

  /**
   * @return Returns the TimeOut.
   */
  public String getTimeOut() {
    return timeout;
  }

  /**
   * @param TimeOut The TimeOut to set.
   */
  public void setTimeOut( String TimeOut ) {
    this.timeout = TimeOut;
  }

  public TimeUnit getTimeoutUnit() {
    return timeoutUnit != null ? timeoutUnit : DEFAULT_TIMEOUT_UNIT;
  }

  public void setTimeoutUnit( TimeUnit timeoutUnit ) {
    this.timeoutUnit = timeoutUnit;
  }

  // ////////////////
  // CONSTRUCTOR //
  // //////////////

  public ElasticSearchBulkMeta() {
    super(); // allocate BaseStepMeta
  }

  public Object clone() {
    ElasticSearchBulkMeta retval = (ElasticSearchBulkMeta) super.clone();

    return retval;
  }

  public void setDefault() {
    batchSize = "" + DEFAULT_BATCH_SIZE;
    timeoutUnit = DEFAULT_TIMEOUT_UNIT;
    index = "twitter";
    type = "tweet";

    isJsonInsert = false;
    jsonField = null;
    idOutField = null;
    useOutput = false;
    stopOnError = true;
  }

  /* This function adds meta data to the rows being pushed out */
  public void getFields( RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep,
                         VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    if ( StringUtils.isNotBlank( this.getIdOutField() ) ) {
      ValueMetaInterface valueMeta = new ValueMetaString( space.environmentSubstitute( this.getIdOutField() ) );
      valueMeta.setOrigin( name );
      // add if doesn't exist
      if ( !r.exists( valueMeta ) ) {
        r.addValueMeta( valueMeta );
      }
    }
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    try {

      Node general = XMLHandler.getSubNode( stepnode, Dom.TAG_GENERAL );

      batchSize = XMLHandler.getTagValue( general, Dom.TAG_BATCH_SIZE );
      timeout = XMLHandler.getTagValue( general, Dom.TAG_TIMEOUT );
      String timeoutStr = XMLHandler.getTagValue( general, Dom.TAG_TIMEOUT_UNIT );
      try {
        timeoutUnit = TimeUnit.valueOf( timeoutStr );
      } catch ( Exception e ) {
        timeoutUnit = DEFAULT_TIMEOUT_UNIT;
      }

      setIndex( XMLHandler.getTagValue( general, Dom.TAG_INDEX ) );
      setType( XMLHandler.getTagValue( general, Dom.TAG_TYPE ) );

      setJsonInsert( parseBool( XMLHandler.getTagValue( general, Dom.TAG_IS_JSON ) ) );
      setJsonField( XMLHandler.getTagValue( general, Dom.TAG_JSON_FIELD ) );
      setIdInField( XMLHandler.getTagValue( general, Dom.TAG_ID_IN_FIELD ) );
      setOverWriteIfSameId( parseBool( XMLHandler.getTagValue( general, Dom.TAG_OVERWRITE_IF_EXISTS ) ) );

      setIdOutField( XMLHandler.getTagValue( general, Dom.TAG_ID_OUT_FIELD ) );
      setUseOutput( parseBool( XMLHandler.getTagValue( general, Dom.TAG_USE_OUTPUT ) ) );
      setStopOnError( parseBool( XMLHandler.getTagValue( general, Dom.TAG_STOP_ON_ERROR ) ) );

      // Fields
      Node fields = XMLHandler.getSubNode( stepnode, Dom.TAG_FIELDS );
      int nrFields = XMLHandler.countNodes( fields, Dom.TAG_FIELD );
      this.clearFields();
      for ( int i = 0; i < nrFields; i++ ) {
        Node fNode = XMLHandler.getSubNodeByNr( fields, Dom.TAG_FIELD, i );

        String colName = XMLHandler.getTagValue( fNode, Dom.TAG_NAME );
        String targetName = XMLHandler.getTagValue( fNode, Dom.TAG_TARGET );

        this.addField( colName, targetName );
      }

      // Servers
      Node servers = XMLHandler.getSubNode( stepnode, Dom.TAG_SERVERS );
      int nrServers = XMLHandler.countNodes( servers, Dom.TAG_SERVER );
      this.clearServers();
      for ( int i = 0; i < nrServers; i++ ) {
        Node sNode = XMLHandler.getSubNodeByNr( servers, Dom.TAG_SERVER, i );

        String addr = XMLHandler.getTagValue( sNode, Dom.TAG_SERVER_ADDRESS );
        String portStr = XMLHandler.getTagValue( sNode, Dom.TAG_SERVER_PORT );

        int port = DEFAULT_PORT;
        try {
          port = Integer.parseInt( portStr );
        } catch ( NumberFormatException nfe ) {
          // use default
        }
        this.addServer( addr, port );
      }

      // Settings
      Node settings = XMLHandler.getSubNode( stepnode, Dom.TAG_SETTINGS );
      int nrSettings = XMLHandler.countNodes( settings, Dom.TAG_SETTING );
      this.clearSettings();
      for ( int i = 0; i < nrSettings; i++ ) {
        Node sNode = XMLHandler.getSubNodeByNr( settings, Dom.TAG_SETTING, i );

        String name = XMLHandler.getTagValue( sNode, Dom.TAG_SETTING_NAME );
        String value = XMLHandler.getTagValue( sNode, Dom.TAG_SETTING_VALUE );

        this.addSetting( name, value );
      }

    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  private static boolean parseBool( String val ) {
    return "Y".equals( val );
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer();
    Indentation indent = new Indentation();

    indent.incr().incr();

    // General
    retval.append( indent.toString() ).append( XMLHandler.openTag( Dom.TAG_GENERAL ) ).append( Const.CR );
    indent.incr();

    retval.append( indent.toString() + XMLHandler.addTagValue( Dom.TAG_INDEX, getIndex() ) );
    retval.append( indent.toString() + XMLHandler.addTagValue( Dom.TAG_TYPE, getType() ) );

    retval.append( indent.toString() + XMLHandler.addTagValue( Dom.TAG_BATCH_SIZE, batchSize ) );
    retval.append( indent.toString() + XMLHandler.addTagValue( Dom.TAG_TIMEOUT, timeout ) );
    retval.append( indent.toString() ).append( XMLHandler.addTagValue( Dom.TAG_TIMEOUT_UNIT, timeoutUnit.toString() ) );

    retval.append( indent.toString() + XMLHandler.addTagValue( Dom.TAG_IS_JSON, isJsonInsert() ) );
    if ( getJsonField() != null ) {
      retval.append( indent.toString() + XMLHandler.addTagValue( Dom.TAG_JSON_FIELD, getJsonField() ) );
    }
    if ( getIdOutField() != null ) {
      retval.append( indent.toString() + XMLHandler.addTagValue( Dom.TAG_ID_OUT_FIELD, getIdOutField() ) );
    }

    if ( getIdInField() != null ) {
      retval.append( indent.toString() + XMLHandler.addTagValue( Dom.TAG_ID_IN_FIELD, getIdInField() ) );
    }

    retval.append( indent.toString() + XMLHandler.addTagValue( Dom.TAG_OVERWRITE_IF_EXISTS, isOverWriteIfSameId() ) );

    retval.append( indent.toString() + XMLHandler.addTagValue( Dom.TAG_USE_OUTPUT, useOutput ) );
    retval.append( indent.toString() + XMLHandler.addTagValue( Dom.TAG_STOP_ON_ERROR, stopOnError ) );

    indent.decr();
    retval.append( indent.toString() ).append( XMLHandler.closeTag( Dom.TAG_GENERAL ) ).append( Const.CR );

    // Fields
    retval.append( indent.toString() ).append( XMLHandler.openTag( Dom.TAG_FIELDS ) ).append( Const.CR );
    indent.incr();
    for ( Field f : fields ) {
      retval.append( indent.toString() ).append( XMLHandler.openTag( Dom.TAG_FIELD ) ).append( Const.CR );
      indent.incr();
      retval.append( indent.toString() ).append( XMLHandler.addTagValue( Dom.TAG_NAME, f.name ) );
      retval.append( indent.toString() ).append( XMLHandler.addTagValue( Dom.TAG_TARGET, f.targetName ) );
      indent.decr();
      retval.append( indent.toString() ).append( XMLHandler.closeTag( Dom.TAG_FIELD ) ).append( Const.CR );
    }
    indent.decr();
    retval.append( indent.toString() ).append( XMLHandler.closeTag( Dom.TAG_FIELDS ) ).append( Const.CR );

    // Servers
    retval.append( indent.toString() ).append( XMLHandler.openTag( Dom.TAG_SERVERS ) ).append( Const.CR );
    indent.incr();
    for ( Server istAddr : servers ) {
      retval.append( indent.toString() ).append( XMLHandler.openTag( Dom.TAG_SERVER ) ).append( Const.CR );
      indent.incr();
      retval.append( indent.toString() ).append( XMLHandler.addTagValue( Dom.TAG_SERVER_ADDRESS, istAddr.address ) );
      retval.append( indent.toString() ).append( XMLHandler.addTagValue( Dom.TAG_SERVER_PORT, istAddr.port ) );
      indent.decr();
      retval.append( indent.toString() ).append( XMLHandler.closeTag( Dom.TAG_SERVER ) ).append( Const.CR );
    }
    indent.decr();
    retval.append( indent.toString() ).append( XMLHandler.closeTag( Dom.TAG_SERVERS ) ).append( Const.CR );

    // Settings
    retval.append( indent.toString() ).append( XMLHandler.openTag( Dom.TAG_SETTINGS ) ).append( Const.CR );
    indent.incr();
    for ( Setting s : settings ) {
      retval.append( indent.toString() ).append( XMLHandler.openTag( Dom.TAG_SETTING ) ).append( Const.CR );
      indent.incr();
      retval.append( indent.toString() ).append( XMLHandler.addTagValue( Dom.TAG_SETTING_NAME, s.setting ) );
      retval.append( indent.toString() ).append( XMLHandler.addTagValue( Dom.TAG_SETTING_VALUE, s.value ) );
      indent.decr();
      retval.append( indent.toString() ).append( XMLHandler.closeTag( Dom.TAG_SETTING ) ).append( Const.CR );
    }
    indent.decr();
    retval.append( indent.toString() ).append( XMLHandler.closeTag( Dom.TAG_SETTINGS ) ).append( Const.CR );

    return retval.toString();
  }

  private static class Indentation {

    private static String indentUnit = Dom.INDENT;
    private String indent = "";
    private int indentLevel = 0;

    public Indentation incr() {
      indentLevel++;
      indent += indentUnit;
      return this;
    }

    public Indentation decr() {
      if ( --indentLevel >= 0 ) {
        indent = indent.substring( 0, indent.length() - indentUnit.length() );
      }
      return this;
    }

    public String toString() {
      return indent;
    }

  }

  private static String joinRepAttr( String... args ) {
    return StringUtils.join( args, "_" );
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
          throws KettleException {
    try {

      setIndex( rep.getStepAttributeString( id_step, joinRepAttr( Dom.TAG_GENERAL, Dom.TAG_INDEX ) ) );
      setType( rep.getStepAttributeString( id_step, joinRepAttr( Dom.TAG_GENERAL, Dom.TAG_TYPE ) ) );

      setBatchSize( rep.getStepAttributeString( id_step, joinRepAttr( Dom.TAG_GENERAL, Dom.TAG_BATCH_SIZE ) ) );
      setTimeOut( rep.getStepAttributeString( id_step, joinRepAttr( Dom.TAG_GENERAL, Dom.TAG_TIMEOUT ) ) );
      String timeoutStr = rep.getStepAttributeString( id_step, joinRepAttr( Dom.TAG_GENERAL, Dom.TAG_TIMEOUT_UNIT ) );
      try {
        timeoutUnit = TimeUnit.valueOf( timeoutStr );
      } catch ( Exception e ) {
        timeoutUnit = DEFAULT_TIMEOUT_UNIT;
      }

      setJsonInsert( rep.getStepAttributeBoolean( id_step, joinRepAttr( Dom.TAG_GENERAL, Dom.TAG_IS_JSON ) ) );
      setJsonField( ( rep.getStepAttributeString( id_step, joinRepAttr( Dom.TAG_GENERAL, Dom.TAG_JSON_FIELD ) ) ) );

      setIdInField( ( rep.getStepAttributeString( id_step, joinRepAttr( Dom.TAG_GENERAL, Dom.TAG_ID_IN_FIELD ) ) ) );
      setOverWriteIfSameId( rep.getStepAttributeBoolean( id_step, joinRepAttr( Dom.TAG_GENERAL,
              Dom.TAG_OVERWRITE_IF_EXISTS ) ) );

      setIdOutField( ( rep.getStepAttributeString( id_step, joinRepAttr( Dom.TAG_GENERAL, Dom.TAG_ID_OUT_FIELD ) ) ) );

      setUseOutput( rep.getStepAttributeBoolean( id_step, joinRepAttr( Dom.TAG_GENERAL, Dom.TAG_USE_OUTPUT ) ) );
      setStopOnError( rep.getStepAttributeBoolean( id_step, joinRepAttr( Dom.TAG_GENERAL, Dom.TAG_STOP_ON_ERROR ) ) );

      // Fields
      clearFields();
      int fieldsNr = rep.countNrStepAttributes( id_step, joinRepAttr( Dom.TAG_FIELD, Dom.TAG_NAME ) );
      for ( int i = 0; i < fieldsNr; i++ ) {
        String name = rep.getStepAttributeString( id_step, i, joinRepAttr( Dom.TAG_FIELD, Dom.TAG_NAME ) );
        String target = rep.getStepAttributeString( id_step, i, joinRepAttr( Dom.TAG_FIELD, Dom.TAG_TARGET ) );
        addField( name, target );
      }

      // Servers
      clearServers();
      int serversNr = rep.countNrStepAttributes( id_step, joinRepAttr( Dom.TAG_SERVER, Dom.TAG_SERVER_ADDRESS ) );
      for ( int i = 0; i < serversNr; i++ ) {
        String addr = rep.getStepAttributeString( id_step, i, joinRepAttr( Dom.TAG_SERVER, Dom.TAG_SERVER_ADDRESS ) );
        int port = (int) rep.getStepAttributeInteger( id_step, i, joinRepAttr( Dom.TAG_SERVER, Dom.TAG_SERVER_PORT ) );
        addServer( addr, port );
      }

      // Settings
      clearSettings();
      int settingsNr = rep.countNrStepAttributes( id_step, joinRepAttr( Dom.TAG_SETTING, Dom.TAG_SETTING_NAME ) );
      for ( int i = 0; i < settingsNr; i++ ) {
        String name = rep.getStepAttributeString( id_step, i, joinRepAttr( Dom.TAG_SETTING, Dom.TAG_SETTING_NAME ) );
        String value = rep.getStepAttributeString( id_step, i, joinRepAttr( Dom.TAG_SETTING, Dom.TAG_SETTING_VALUE ) );
        addSetting( name, value );
      }

    } catch ( Exception e ) {
      throw new KettleException(
              BaseMessages.getString( PKG, "ElasticSearchBulkMeta.Exception.ErrorReadingRepository" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
          throws KettleException {
    try {

      rep.saveStepAttribute( id_transformation, id_step, joinRepAttr( Dom.TAG_GENERAL, Dom.TAG_INDEX ), getIndex() );
      rep.saveStepAttribute( id_transformation, id_step, joinRepAttr( Dom.TAG_GENERAL, Dom.TAG_TYPE ), getType() );

      rep.saveStepAttribute( id_transformation, id_step, joinRepAttr( Dom.TAG_GENERAL, Dom.TAG_BATCH_SIZE ),
              batchSize );
      rep.saveStepAttribute( id_transformation, id_step, joinRepAttr( Dom.TAG_GENERAL, Dom.TAG_TIMEOUT ),
              getTimeOut() );
      rep.saveStepAttribute( id_transformation, id_step, joinRepAttr( Dom.TAG_GENERAL, Dom.TAG_TIMEOUT_UNIT ),
              getTimeoutUnit().toString() );

      rep.saveStepAttribute( id_transformation, id_step, joinRepAttr( Dom.TAG_GENERAL, Dom.TAG_IS_JSON ),
              isJsonInsert() );
      rep.saveStepAttribute( id_transformation, id_step, joinRepAttr( Dom.TAG_GENERAL, Dom.TAG_JSON_FIELD ),
              getJsonField() );

      rep.saveStepAttribute( id_transformation, id_step, joinRepAttr( Dom.TAG_GENERAL, Dom.TAG_ID_IN_FIELD ),
              getIdInField() );
      rep.saveStepAttribute( id_transformation, id_step, joinRepAttr( Dom.TAG_GENERAL, Dom.TAG_OVERWRITE_IF_EXISTS ),
              isOverWriteIfSameId() );

      rep.saveStepAttribute( id_transformation, id_step, joinRepAttr( Dom.TAG_GENERAL, Dom.TAG_ID_OUT_FIELD ),
              getIdOutField() );

      rep.saveStepAttribute( id_transformation, id_step, joinRepAttr( Dom.TAG_GENERAL, Dom.TAG_USE_OUTPUT ),
              isUseOutput() );
      rep.saveStepAttribute( id_transformation, id_step, joinRepAttr( Dom.TAG_GENERAL, Dom.TAG_STOP_ON_ERROR ),
              isStopOnError() );

      // Fields
      for ( int i = 0; i < fields.size(); i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, joinRepAttr( Dom.TAG_FIELD, Dom.TAG_NAME ), fields.get(
                i ).name );
        rep.saveStepAttribute( id_transformation, id_step, i, joinRepAttr( Dom.TAG_FIELD, Dom.TAG_TARGET ), fields.get(
                i ).targetName );
      }

      // Servers
      for ( int i = 0; i < servers.size(); i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, joinRepAttr( Dom.TAG_SERVER, Dom.TAG_SERVER_ADDRESS ),
                servers.get( i ).address );
        rep.saveStepAttribute( id_transformation, id_step, i, joinRepAttr( Dom.TAG_SERVER, Dom.TAG_SERVER_PORT ),
                servers.get( i ).port );
      }

      // Settings
      for ( int i = 0; i < settings.size(); i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, joinRepAttr( Dom.TAG_SETTING, Dom.TAG_SETTING_NAME ),
                settings.get( i ).setting );
        rep.saveStepAttribute( id_transformation, id_step, i, joinRepAttr( Dom.TAG_SETTING, Dom.TAG_SETTING_VALUE ),
                settings.get( i ).value );
      }

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG,
              "ElasticSearchBulkMeta.Exception.ErrorSavingToRepository", "" + id_step ), e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
                     String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
                     IMetaStore metaStore ) {

    checkBasicRequiredFields( remarks, stepMeta );

    checkInputFields( remarks, prev, stepMeta );

  }

  private void checkBasicRequiredFields( List<CheckResultInterface> remarks, StepMeta stepMeta ) {
    checkRequiredString( remarks, stepMeta, getIndex(), BaseMessages.getString( PKG,
            "ElasticSearchBulkDialog.Index.Label" ) );
    checkRequiredString( remarks, stepMeta, getType(), BaseMessages.getString( PKG,
            "ElasticSearchBulkDialog.Type.Label" ) );
    checkRequiredString( remarks, stepMeta, getBatchSize(), BaseMessages.getString( PKG,
            "ElasticSearchBulkDialog.BatchSize.Label" ) );
  }

  private void checkRequiredString( List<CheckResultInterface> remarks, StepMeta stepMeta, String value,
                                    String fieldName ) {
    if ( StringUtils.isBlank( value ) ) {
      remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
              "ElasticSearchBulkMeta.CheckResult.MissingRequired", fieldName ), stepMeta ) );
    } else {
      remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
              "ElasticSearchBulkMeta.CheckResult.RequiredOK", fieldName, value ), stepMeta ) );
    }
  }

  private void checkInputFields( List<CheckResultInterface> remarks, RowMetaInterface prev, StepMeta stepMeta ) {

    if ( prev != null && prev.size() > 0 ) {
      if ( isJsonInsert() ) { // JSON
        if ( StringUtils.isBlank( getJsonField() ) ) { // jsonField not set
          String jsonFieldLabel = BaseMessages.getString( PKG, "ElasticSearchBulkDialog.JsonField.Label" );
          String isJsonLabel = BaseMessages.getString( PKG, "ElasticSearchBulkDialog.IsJson.Label" );
          remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
                  "ElasticSearchBulkMeta.CheckResult.MissingRequiredDependent", jsonFieldLabel, isJsonLabel ),
                  stepMeta ) );
        } else if ( prev.indexOfValue( getJsonField() ) < 0 ) { // jsonField not in input
          remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
                  "ElasticSearchBulkMeta.CheckResult.MissingInput", getJsonField() ), stepMeta ) );
        }
      } else { // not JSON
        for ( Field f : fields ) {
          if ( prev.indexOfValue( f.name ) < 0 ) { // fields not found
            remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
                    "ElasticSearchBulkMeta.CheckResult.MissingInput", f.name ), stepMeta ) );
          }
        }
      }
    } else { // no input
      remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
              "ElasticSearchBulkMeta.CheckResult.NoInput" ), stepMeta ) );
    }

  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
                                Trans trans ) {
    return new ElasticSearchBulk( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new ElasticSearchBulkData();
  }

  public boolean supportsErrorHandling() {
    return true;
  }

  public static class Server {
    @Injection( name = "ADDRESS" )
    public String address;
    @Injection( name = "PORT" )
    public int port;

    public String getAddress() {
      return address;
    }

    public int getPort() {
      return port;
    }

  }

  public static class Field {
    @Injection( name = "NAME" )
    public String name;
    @Injection( name = "TARGET_NAME" )
    public String targetName;
  }

  public static class Setting {
    @Injection( name = "NAME" )
    public String setting;
    @Injection( name = "VALUE" )
    public String value;
  }
}
