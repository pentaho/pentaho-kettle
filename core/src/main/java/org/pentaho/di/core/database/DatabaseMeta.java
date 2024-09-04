// CHECKSTYLE:FileLength:OFF
/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.database;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.ExecutorUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.shared.SharedObjectBase;
import org.pentaho.di.shared.SharedObjectInterface;
import org.w3c.dom.Node;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * This class defines the database specific parameters for a certain database type. It also provides static information
 * regarding a number of well known databases.
 *
 * @author Matt
 * @since 18-05-2003
 *
 */
public class DatabaseMeta extends SharedObjectBase implements Cloneable, XMLInterface, SharedObjectInterface,
  VariableSpace, RepositoryElementInterface {
  private static Class<?> PKG = Database.class; // for i18n purposes, needed by Translator2!!

  public static final String XML_TAG = "connection";

  public static final RepositoryObjectType REPOSITORY_ELEMENT_TYPE = RepositoryObjectType.DATABASE;

  private static final String DROP_TABLE_STATEMENT = "DROP TABLE IF EXISTS ";

  // Comparator for sorting databases alphabetically by name
  public static final Comparator<DatabaseMeta> comparator = new Comparator<DatabaseMeta>() {
    @Override
    public int compare( DatabaseMeta dbm1, DatabaseMeta dbm2 ) {
      return dbm1.getName().compareToIgnoreCase( dbm2.getName() );
    }
  };

  private DatabaseInterface databaseInterface;

  private static volatile Future<Map<String, DatabaseInterface>> allDatabaseInterfaces;

  static {
    init();
  }

  public static void init() {
    PluginRegistry.getInstance().addPluginListener( DatabasePluginType.class,
      new org.pentaho.di.core.plugins.PluginTypeListener() {
        @Override public void pluginAdded( Object serviceObject ) {
          clearDatabaseInterfacesMap();
        }

        @Override public void pluginRemoved( Object serviceObject ) {
          clearDatabaseInterfacesMap();

        }

        @Override public void pluginChanged( Object serviceObject ) {
          clearDatabaseInterfacesMap();
        }
      } );
  }

  private VariableSpace variables = new Variables();

  private ObjectRevision objectRevision;

  private boolean readOnly = false;

  private boolean needUpdate = false;

  /**
   * Indicates that the connections doesn't point to a type of database yet.
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_NONE = 0;

  /**
   * Connection to a MySQL database
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_MYSQL = 1;

  /**
   * Connection to an Oracle database
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_ORACLE = 2;

  /**
   * Connection to an AS/400 (IBM iSeries) DB400 database
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_AS400 = 3;

  /**
   * Connection to a Microsoft SQL Server database
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_MSSQL = 5;

  /**
   * Connection to an IBM DB2 database
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_DB2 = 6;

  /**
   * Connection to a PostgreSQL database
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_POSTGRES = 7;

  /**
   * Connection to an Intersystems Cache database
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_CACHE = 8;

  /**
   * Connection to an IBM Informix database
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_INFORMIX = 9;

  /**
   * Connection to a Sybase ASE database
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_SYBASE = 10;

  /**
   * Connection to a Gupta SQLBase database
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_GUPTA = 11;

  /**
   * Connection to a FireBird database
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_FIREBIRD = 13;

  /**
   * Connection to a SAP DB database
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_SAPDB = 14;

  /**
   * Connection to a Hypersonic java database
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_HYPERSONIC = 15;

  /**
   * Connection to a generic database
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_GENERIC = 16;

  /**
   * Connection to an SAP R/3 system
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_SAPR3 = 17;

  /**
   * Connection to an Ingress database
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_INGRES = 18;

  /**
   * Connection to a Borland Interbase database
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_INTERBASE = 19;

  /**
   * Connection to an ExtenDB database
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_EXTENDB = 20;

  /**
   * Connection to a Teradata database
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_TERADATA = 21;

  /**
   * Connection to an Oracle RDB database
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_ORACLE_RDB = 22;

  /**
   * Connection to an H2 database
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_H2 = 23;

  /**
   * Connection to a Netezza database
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_NETEZZA = 24;

  /**
   * Connection to an IBM UniVerse database
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_UNIVERSE = 25;

  /**
   * Connection to a SQLite database
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_SQLITE = 26;

  /**
   * Connection to a BMC Remedy Action Request System
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_REMEDY_AR_SYSTEM = 28;

  /**
   * Connection to a Palo MOLAP Server
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_PALO = 29;

  /**
   * Connection to a SybaseIQ ASE database
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_SYBASEIQ = 30;

  /**
   * Connection to a Greenplum database
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_GREENPLUM = 31;

  /**
   * Connection to a MonetDB database
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_MONETDB = 32;

  /**
   * Connection to a KingbaseES database
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_KINGBASEES = 33;

  /**
   * Connection to a Vertica database
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_VERTICA = 34;

  /**
   * Connection to a Neoview database
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_NEOVIEW = 35;

  /**
   * Connection to a LucidDB database
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_LUCIDDB = 36;

  /**
   * Connection to an Infobright database
   *
   * @deprecated
   */
  @Deprecated
  public static final int TYPE_DATABASE_INFOBRIGHT = 37;

  /**
   * Connect natively through JDBC thin driver to the database.
   */
  public static final int TYPE_ACCESS_NATIVE = 0;

  /**
   * Connect to the database using ODBC.
   * @deprecated
   */
  public static final int TYPE_ACCESS_ODBC = 1;

  /**
   * Connect to the database using OCI. (Oracle only)
   */
  public static final int TYPE_ACCESS_OCI = 2;

  /**
   * Connect to the database using plugin specific method. (SAP ERP)
   */
  public static final int TYPE_ACCESS_PLUGIN = 3;

  /**
   * Connect to the database using JNDI.
   */
  public static final int TYPE_ACCESS_JNDI = 4;

  /**
   * Short description of the access type, used in XML and the repository.
   */
  public static final String[] dbAccessTypeCode = { "Native", "ODBC (deprecated)", "OCI", "Plugin", "JNDI", ",", };

  /**
   * Longer description for user interactions.
   */
  public static final String[] dbAccessTypeDesc = {
    "Native (JDBC)", "ODBC (deprecated)", "OCI", "Plugin specific access method", "JNDI", "Custom", };

  /**
   * Use this length in a String value to indicate that you want to use a CLOB in stead of a normal text field.
   */
  public static final int CLOB_LENGTH = 9999999;

  /**
   * The value to store in the attributes so that an empty value doesn't get lost...
   */
  public static final String EMPTY_OPTIONS_STRING = "><EMPTY><";

  /**
   * Construct a new database connections. Note that not all these parameters are not always mandatory.
   *
   * @param name
   *          The database name
   * @param type
   *          The type of database
   * @param access
   *          The type of database access
   * @param host
   *          The hostname or IP address
   * @param db
   *          The database name
   * @param port
   *          The port on which the database listens.
   * @param user
   *          The username
   * @param pass
   *          The password
   */
  public DatabaseMeta( String name, String type, String access, String host, String db, String port, String user,
    String pass ) {
    setValues( name, type, access, host, db, port, user, pass );
    addOptions();
  }

  /**
   * Create an empty database connection
   *
   */
  public DatabaseMeta() {
    setDefault();
    addOptions();
  }

  /**
   * Set default values for an Oracle database.
   *
   */
  public void setDefault() {
    setValues( "", "Oracle", "Native", "", "", "1521", "", "" );
  }

  /**
   * Add a list of common options for some databases.
   *
   */
  public void addOptions() {
    databaseInterface.addDefaultOptions();
    setSupportsBooleanDataType( true );
    setSupportsTimestampDataType( true );
  }

  /**
   * @return the system dependend database interface for this database metadata definition
   */
  public DatabaseInterface getDatabaseInterface() {
    return databaseInterface;
  }

  /**
   * Set the system dependend database interface for this database metadata definition
   *
   * @param databaseInterface
   *          the system dependend database interface
   */
  public void setDatabaseInterface( DatabaseInterface databaseInterface ) {
    this.databaseInterface = databaseInterface;
  }

  /**
   * Search for the right type of DatabaseInterface object and clone it.
   *
   * @param databaseType
   *          the type of DatabaseInterface to look for (description)
   * @return The requested DatabaseInterface
   *
   * @throws KettleDatabaseException
   *           when the type could not be found or referenced.
   */
  public static final DatabaseInterface getDatabaseInterface( String databaseType ) throws KettleDatabaseException {
    DatabaseInterface di = findDatabaseInterface( databaseType );
    if ( di == null ) {
      throw new KettleDatabaseException( BaseMessages.getString(
        PKG, "DatabaseMeta.Error.DatabaseInterfaceNotFound", databaseType ) );
    }
    return (DatabaseInterface) di.clone();
  }

  /**
   * Search for the right type of DatabaseInterface object and return it.
   *
   * @param databaseTypeDesc
   *          the type of DatabaseInterface to look for (id or description)
   * @return The requested DatabaseInterface
   *
   * @throws KettleDatabaseException
   *           when the type could not be found or referenced.
   */
  private static final DatabaseInterface findDatabaseInterface( String databaseTypeDesc ) throws KettleDatabaseException {
    PluginRegistry registry = PluginRegistry.getInstance();
    PluginInterface plugin = registry.getPlugin( DatabasePluginType.class, databaseTypeDesc );
    if ( plugin == null ) {
      plugin = registry.findPluginWithName( DatabasePluginType.class, databaseTypeDesc );
    }

    if ( plugin == null ) {
      throw new KettleDatabaseException( "database type with plugin id ["
        + databaseTypeDesc + "] couldn't be found!" );
    }

    return getDatabaseInterfacesMap().get( plugin.getIds()[0] );
  }

  /**
   * Returns the database ID of this database connection if a repository was used before.
   *
   * @return the ID of the db connection.
   */
  @Override
  public ObjectId getObjectId() {
    return databaseInterface.getObjectId();
  }

  @Override
  public void setObjectId( ObjectId id ) {
    databaseInterface.setObjectId( id );
  }

  @Override
  public Object clone() {
    return deepClone( false );
  }

  public Object deepClone( boolean cloneUpdateFlag ) {
    DatabaseMeta databaseMeta = new DatabaseMeta();
    databaseMeta.replaceMeta( this, cloneUpdateFlag );
    databaseMeta.setObjectId( null );
    return databaseMeta;
  }

  public void replaceMeta( DatabaseMeta databaseMeta, boolean cloneUpdateFlag ) {
    this.setValues(
      databaseMeta.getName(), databaseMeta.getPluginId(), databaseMeta.getAccessTypeDesc(), databaseMeta
        .getHostname(), databaseMeta.getDatabaseName(), databaseMeta.getDatabasePortNumberString(),
      databaseMeta.getUsername(), databaseMeta.getPassword() );
    this.setServername( databaseMeta.getServername() );
    this.setDataTablespace( databaseMeta.getDataTablespace() );
    this.setIndexTablespace( databaseMeta.getIndexTablespace() );

    this.databaseInterface = (DatabaseInterface) databaseMeta.databaseInterface.clone();

    this.setObjectId( databaseMeta.getObjectId() );
    setChanged( true );
    if ( cloneUpdateFlag ) {
      setNeedUpdate( databaseMeta.isNeedUpdate() );
    }
  }

  public void replaceMeta( DatabaseMeta databaseMeta ) {
    replaceMeta( databaseMeta, false );
  }

  public void setValues( String name, String type, String access, String host, String db, String port,
    String user, String pass ) {
    try {
      databaseInterface = getDatabaseInterface( type );
    } catch ( KettleDatabaseException kde ) {
      throw new RuntimeException( "Database type not found!", kde );
    }

    setName( name );
    setAccessType( getAccessType( access ) );
    setHostname( host );
    setDBName( db );
    setDBPort( port );
    setUsername( user );
    setPassword( pass );
    setServername( null );
    setChanged( false );
  }

  public void setDatabaseType( String type ) {
    DatabaseInterface oldInterface = databaseInterface;

    try {
      databaseInterface = getDatabaseInterface( type );
    } catch ( KettleDatabaseException kde ) {
      throw new RuntimeException( "Database type [" + type + "] not found!", kde );
    }

    setName( oldInterface.getName() );
    setDisplayName( oldInterface.getDisplayName() );
    setAccessType( oldInterface.getAccessType() );
    setHostname( oldInterface.getHostname() );
    setDBName( oldInterface.getDatabaseName() );
    setDBPort( oldInterface.getDatabasePortNumberString() );
    setUsername( oldInterface.getUsername() );
    setPassword( oldInterface.getPassword() );
    setServername( oldInterface.getServername() );
    setDataTablespace( oldInterface.getDataTablespace() );
    setIndexTablespace( oldInterface.getIndexTablespace() );
    setChanged( oldInterface.isChanged() );
  }

  public void setValues( DatabaseMeta info ) {
    databaseInterface = (DatabaseInterface) info.databaseInterface.clone();
  }

  /**
   * Sets the name of the database connection. This name should be unique in a transformation and in general in a single
   * repository.
   *
   * @param name
   *          The name of the database connection
   */
  @Override
  public void setName( String name ) {
    databaseInterface.setName( name );
  }

  /**
   * Returns the name of the database connection
   *
   * @return The name of the database connection
   */
  @Override
  public String getName() {
    return databaseInterface.getName();
  }

  public void setDisplayName( String displayName ) {
    databaseInterface.setDisplayName( displayName );
  }

  /**
   * Returns the name of the database connection
   *
   * @return The name of the database connection
   */
  public String getDisplayName() {
    return databaseInterface.getDisplayName();
  }

  /**
   * Returns the type of database, one of
   * <p>
   * TYPE_DATABASE_MYSQL
   * <p>
   * TYPE_DATABASE_ORACLE
   * <p>
   * TYPE_DATABASE_...
   * <p>
   *
   * @return the database type
   * @Deprecated public int getDatabaseType() { return databaseInterface.getDatabaseType(); }
   */

  /**
   * The plugin ID of the database interface
   */
  public String getPluginId() {
    return databaseInterface.getPluginId();
  }

  /*
   * Sets the type of database.
   *
   * @param db_type The database type public void setDatabaseType(int db_type) { databaseInterface this.databaseType =
   * db_type; }
   */

  /**
   * Return the type of database access. One of
   * <p>
   * TYPE_ACCESS_NATIVE
   * <p>
   * TYPE_ACCESS_OCI
   * <p>
   *
   * @return The type of database access.
   */
  public int getAccessType() {
    return databaseInterface.getAccessType();
  }

  /**
   * Set the type of database access.
   *
   * @param access_type
   *          The access type.
   */
  public void setAccessType( int access_type ) {
    databaseInterface.setAccessType( access_type );
  }

  /**
   * Returns a short description of the type of database.
   *
   * @return A short description of the type of database.
   * @deprecated This is actually the plugin ID
   */
  @Deprecated
  public String getDatabaseTypeDesc() {
    return getPluginId();
  }

  /**
   * Gets you a short description of the type of database access.
   *
   * @return A short description of the type of database access.
   */
  public String getAccessTypeDesc() {
    return dbAccessTypeCode[getAccessType()];
  }

  /**
   * Return the hostname of the machine on which the database runs.
   *
   * @return The hostname of the database.
   */
  public String getHostname() {
    return databaseInterface.getHostname();
  }

  /**
   * Sets the hostname of the machine on which the database runs.
   *
   * @param hostname
   *          The hostname of the machine on which the database runs.
   */
  public void setHostname( String hostname ) {
    databaseInterface.setHostname( hostname );
  }

  /**
   * Return the port on which the database listens as a String. Allows for parameterisation.
   *
   * @return The database port.
   */
  public String getDatabasePortNumberString() {
    return databaseInterface.getDatabasePortNumberString();
  }

  /**
   * Sets the port on which the database listens.
   *
   * @param db_port
   *          The port number on which the database listens
   */
  public void setDBPort( String db_port ) {
    databaseInterface.setDatabasePortNumberString( db_port );
  }

  /**
   * Return the name of the database.
   *
   * @return The database name.
   */
  public String getDatabaseName() {
    return databaseInterface.getDatabaseName();
  }

  /**
   * Set the name of the database.
   *
   * @param databaseName
   *          The new name of the database
   */
  public void setDBName( String databaseName ) {
    databaseInterface.setDatabaseName( databaseName );
  }

  /**
   * Get the username to log into the database on this connection.
   *
   * @return The username to log into the database on this connection.
   */
  public String getUsername() {
    return databaseInterface.getUsername();
  }

  /**
   * Sets the username to log into the database on this connection.
   *
   * @param username
   *          The username
   */
  public void setUsername( String username ) {
    databaseInterface.setUsername( username );
  }

  /**
   * Get the password to log into the database on this connection.
   *
   * @return the password to log into the database on this connection.
   */
  public String getPassword() {
    return databaseInterface.getPassword();
  }

  /**
   * Sets the password to log into the database on this connection.
   *
   * @param password
   *          the password to log into the database on this connection.
   */
  public void setPassword( String password ) {
    databaseInterface.setPassword( password );
  }

  /**
   * @param servername
   *          the Informix servername
   */
  public void setServername( String servername ) {
    databaseInterface.setServername( servername );
  }

  /**
   * @return the Informix servername
   */
  public String getServername() {
    return databaseInterface.getServername();
  }

  public String getDataTablespace() {
    return databaseInterface.getDataTablespace();
  }

  public void setDataTablespace( String data_tablespace ) {
    databaseInterface.setDataTablespace( data_tablespace );
  }

  public String getIndexTablespace() {
    return databaseInterface.getIndexTablespace();
  }

  public void setIndexTablespace( String index_tablespace ) {
    databaseInterface.setIndexTablespace( index_tablespace );
  }

  public boolean isNeedUpdate() {
    return needUpdate;
  }

  public void setNeedUpdate( boolean needUpdate ) {
    this.needUpdate = needUpdate;
  }

  public void setChanged() {
    setChanged( true );
    setNeedUpdate( true );
  }

  public void setChanged( boolean ch ) {
    databaseInterface.setChanged( ch );
  }

  public boolean hasChanged() {
    return databaseInterface.isChanged();
  }

  public void clearChanged() {
    databaseInterface.setChanged( false );
  }

  @Override
  public String toString() {
    return getDisplayName();
  }

  /**
   * @return The extra attributes for this database connection
   */
  public Properties getAttributes() {
    return databaseInterface.getAttributes();
  }

  /**
   * Set extra attributes on this database connection
   *
   * @param attributes
   *          The extra attributes to set on this database connection.
   */
  public void setAttributes( Properties attributes ) {
    databaseInterface.setAttributes( attributes );
  }

  /**
   * Constructs a new database using an XML string snippet. It expects the snippet to be enclosed in
   * <code>connection</code> tags.
   *
   * @param xml
   *          The XML string to parse
   * @throws KettleXMLException
   *           in case there is an XML parsing error
   */
  public DatabaseMeta( String xml ) throws KettleXMLException {
    this( XMLHandler.getSubNode( XMLHandler.loadXMLString( xml ), "connection" ) );
  }

  /**
   * Reads the information from an XML Node into this new database connection.
   *
   * @param con
   *          The Node to read the data from
   * @throws KettleXMLException
   */
  public DatabaseMeta( Node con ) throws KettleXMLException {
    this();

    try {
      String type = XMLHandler.getTagValue( con, "type" );
      try {
        databaseInterface = getDatabaseInterface( type );

      } catch ( KettleDatabaseException kde ) {
        throw new KettleXMLException( "Unable to create new database interface", kde );
      }
      
      setDefaultAttributesValues();

      setName( XMLHandler.getTagValue( con, "name" ) );
      setDisplayName( getName() );
      setHostname( XMLHandler.getTagValue( con, "server" ) );
      String acc = XMLHandler.getTagValue( con, "access" );
      setAccessType( getAccessType( acc ) );

      setDBName( XMLHandler.getTagValue( con, "database" ) );

      // The DB port is read here too for backward compatibility! getName()
      //
      setDBPort( XMLHandler.getTagValue( con, "port" ) );
      setUsername( XMLHandler.getTagValue( con, "username" ) );
      setPassword( Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( con, "password" ) ) );
      setServername( XMLHandler.getTagValue( con, "servername" ) );
      setDataTablespace( XMLHandler.getTagValue( con, "data_tablespace" ) );
      setIndexTablespace( XMLHandler.getTagValue( con, "index_tablespace" ) );

      setReadOnly( Boolean.valueOf( XMLHandler.getTagValue( con, "read_only" ) ) );

      // Also, read the database attributes...
      Node attrsnode = XMLHandler.getSubNode( con, "attributes" );
      if ( attrsnode != null ) {
        List<Node> attrnodes = XMLHandler.getNodes( attrsnode, "attribute" );
        for ( Node attrnode : attrnodes ) {
          String code = XMLHandler.getTagValue( attrnode, "code" );
          String attribute = XMLHandler.getTagValue( attrnode, "attribute" );
          if ( code != null && attribute != null ) {
            databaseInterface.addAttribute( code, attribute );
          }
          getDatabasePortNumberString();
        }
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load database connection info from XML node", e );
    }
  }

  /**
   * Initialize every attribute
   */
  private void setDefaultAttributesValues() {
    setConnectSQL( "" );
    setInitialPoolSizeString( "" );
    setMaximumPoolSizeString( "" );
    setUsingConnectionPool( false );
    setForcingIdentifiersToLowerCase( false );
    setForcingIdentifiersToUpperCase( false );
    setQuoteAllFields( false );
    setUsingDoubleDecimalAsSchemaTableSeparator( false );
    setSupportsBooleanDataType( false );
    setSupportsTimestampDataType( false );
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 250 );

    retval.append( "  <" ).append( XML_TAG ).append( '>' ).append( Const.CR );
    retval.append( "    " ).append( XMLHandler.addTagValue( "name", getName() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "server", getHostname() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "type", getPluginId() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "access", getAccessTypeDesc() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "database", getDatabaseName() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "port", getDatabasePortNumberString() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "username", getUsername() ) );
    retval.append( "    " ).append(
      XMLHandler.addTagValue( "password", Encr.encryptPasswordIfNotUsingVariables( getPassword() ) ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "servername", getServername() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "data_tablespace", getDataTablespace() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "index_tablespace", getIndexTablespace() ) );

    // only write the tag out if it is set to true
    if ( isReadOnly() ) {
      retval.append( "    " ).append( XMLHandler.addTagValue( "read_only", Boolean.toString( isReadOnly() ) ) );
    }

    retval.append( "    <attributes>" ).append( Const.CR );

    List<String> list = new ArrayList<>();
    Set<Object> keySet = getAttributes().keySet();
    for ( Object object : keySet ) {
      list.add( (String) object );
    }
    Collections.sort( list ); // Sort the entry-sets to make sure we can compare XML strings: if the order is different,
                              // the XML is different.

    for ( Iterator<String> iter = list.iterator(); iter.hasNext(); ) {
      String code = iter.next();
      String attribute = getAttributes().getProperty( code );
      if ( !Utils.isEmpty( attribute ) ) {
        retval.append( "      <attribute>"
          + XMLHandler.addTagValue( "code", code, false )
          + XMLHandler.addTagValue( "attribute", attribute, false ) + "</attribute>" + Const.CR );
      }
    }
    retval.append( "    </attributes>" ).append( Const.CR );

    retval.append( "  </" + XML_TAG + ">" ).append( Const.CR );
    return retval.toString();
  }

  @Override
  public int hashCode() {
    return getName().hashCode(); // name of connection is unique!
  }

  @Override
  public boolean equals( Object obj ) {
    return obj instanceof DatabaseMeta && getName().equals( ( (DatabaseMeta) obj ).getName() );
  }

  public String getURL() throws KettleDatabaseException {
    return getURL( null );
  }

  public String getURL( String partitionId ) throws KettleDatabaseException {
    // First see if we're not doing any JNDI...
    //
    /*
     * This doesn't make much sense here - we check but do nothing? if ( getAccessType() == TYPE_ACCESS_JNDI ) { // We
     * can't really determine the URL here. // // }
     */
    String baseUrl;
    String hostname;
    String port;
    String databaseName;

    if ( isPartitioned() && !Utils.isEmpty( partitionId ) ) {
      // Get the cluster information...
      PartitionDatabaseMeta partition = getPartitionMeta( partitionId );
      hostname = environmentSubstitute( partition.getHostname() );
      port = environmentSubstitute( partition.getPort() );
      databaseName = environmentSubstitute( partition.getDatabaseName() );
    } else {
      hostname = environmentSubstitute( getHostname() );
      port = environmentSubstitute( getDatabasePortNumberString() );
      databaseName = environmentSubstitute( getDatabaseName() );
    }
    baseUrl = databaseInterface.getURL( environmentSubstitute( hostname ), environmentSubstitute( port ),
      environmentSubstitute( databaseName ) );
    String url =  environmentSubstitute( baseUrl );

    if ( databaseInterface.supportsOptionsInURL() ) {
      Map<String, String> extraOptions = getExtraOptions();
      databaseInterface.putOptionalOptions( extraOptions );
      url = appendExtraOptions( url, extraOptions );
    }
    // else {
    // We need to put all these options in a Properties file later (Oracle & Co.)
    // This happens at connect time...
    // }

    return url;
  }

  protected String appendExtraOptions( String url, Map<String, String> extraOptions ) {
    if ( extraOptions.isEmpty() ) {
      return url;
    }

    StringBuilder urlBuilder = new StringBuilder( url );

    final String optionIndicator = getExtraOptionIndicator();
    final String optionSeparator = getExtraOptionSeparator();
    final String valueSeparator = getExtraOptionValueSeparator();

    Iterator<String> iterator = extraOptions.keySet().iterator();
    boolean first = true;
    while ( iterator.hasNext() ) {
      String typedParameter = iterator.next();
      int dotIndex = typedParameter.indexOf( '.' );
      if ( dotIndex == -1 ) {
        continue;
      }

      final String value = extraOptions.get( typedParameter );
      if ( Utils.isEmpty( value ) || value.equals( EMPTY_OPTIONS_STRING ) ) {
        // skip this science no value is provided
        continue;
      }

      final String typeCode = typedParameter.substring( 0, dotIndex );
      final String parameter = typedParameter.substring( dotIndex + 1 );

      // Only add to the URL if it's the same database type code,
      // or underlying database is the same for both id's, and any subset of
      // connection settings for one database is valid for another
      boolean dbForBothDbInterfacesIsSame = false;
      try {
        DatabaseInterface primaryDb = getDbInterface( typeCode );
        dbForBothDbInterfacesIsSame = databaseForBothDbInterfacesIsTheSame( primaryDb, getDatabaseInterface() );
      } catch ( KettleDatabaseException e ) {
        getGeneralLogger().logError(
          "DatabaseInterface with " + typeCode + " database type is not found! Parameter " + parameter
            + "won't be appended to URL" );
      }
      if ( dbForBothDbInterfacesIsSame ) {
        if ( first && url.indexOf( valueSeparator ) == -1 ) {
          urlBuilder.append( optionIndicator );
        } else {
          urlBuilder.append( optionSeparator );
        }

        urlBuilder.append( environmentSubstitute( parameter ) ).append( valueSeparator ).append( environmentSubstitute( value ) );
        first = false;
      }
    }

    return urlBuilder.toString();
  }

  /**
   * This method is designed to identify whether the actual database for two database connection types is the same.
   * This situation can occur in two cases:
   * 1. plugin id of {@code primary} is the same as plugin id of {@code secondary}
   * 2. {@code secondary} is a descendant {@code primary} (with any deepness).
   */
  protected boolean databaseForBothDbInterfacesIsTheSame( DatabaseInterface primary, DatabaseInterface secondary ) {
    if ( primary == null || secondary == null ) {
      throw new IllegalArgumentException( "DatabaseInterface shouldn't be null!" );
    }

    if ( primary.getPluginId() == null || secondary.getPluginId() == null ) {
      return false;
    }

    if ( primary.getPluginId().equals( secondary.getPluginId() ) ) {
      return true;
    }

    return primary.getClass().isAssignableFrom( secondary.getClass() );
  }

  public Properties getConnectionProperties() {
    Properties properties = new Properties();

    Map<String, String> map = getExtraOptions();
    if ( map.size() > 0 ) {
      Iterator<String> iterator = map.keySet().iterator();
      while ( iterator.hasNext() ) {
        String typedParameter = iterator.next();
        int dotIndex = typedParameter.indexOf( '.' );
        if ( dotIndex >= 0 ) {
          String typeCode = typedParameter.substring( 0, dotIndex );
          String parameter = typedParameter.substring( dotIndex + 1 );
          String value = map.get( typedParameter );

          // Only add to the URL if it's the same database type code...
          //
          if ( databaseInterface.getPluginId().equals( typeCode ) ) {
            if ( value != null && value.equals( EMPTY_OPTIONS_STRING ) ) {
              value = "";
            }
            properties.put( parameter, environmentSubstitute( Const.NVL( value, "" ) ) );
          }
        }
      }
    }

    return properties;
  }

  public String getExtraOptionIndicator() {
    return getDatabaseInterface().getExtraOptionIndicator();
  }

  /**
   * @return The extra option separator in database URL for this platform (usually this is semicolon ; )
   */
  public String getExtraOptionSeparator() {
    return getDatabaseInterface().getExtraOptionSeparator();
  }

  /**
   * @return The extra option value separator in database URL for this platform (usually this is the equal sign = )
   */
  public String getExtraOptionValueSeparator() {
    return getDatabaseInterface().getExtraOptionValueSeparator();
  }

  /**
   * Add an extra option to the attributes list
   *
   * @param databaseTypeCode
   *          The database type code for which the option applies
   * @param option
   *          The option to set
   * @param value
   *          The value of the option
   */
  public void addExtraOption( String databaseTypeCode, String option, String value ) {
    databaseInterface.addExtraOption( databaseTypeCode, option, value );
  }

  public void applyDefaultOptions( DatabaseInterface databaseInterface ) {
    final Map<String, String> extraOptions = getExtraOptions();

    final Map<String, String> defaultOptions = databaseInterface.getDefaultOptions();
    for ( Map.Entry<String, String> entry : defaultOptions.entrySet() ) {
      String option = entry.getKey();
      String value = entry.getValue();
      String[] split = option.split( "[.]", 2 );
      if ( !extraOptions.containsKey( option ) && split.length == 2 ) {
        addExtraOption( split[0], split[1], value );
      }
    }
  }

  /**
   * @deprecated because the same database can support transactions or not. It all depends on the database setup.
   *             Therefor, we look at the database metadata DatabaseMetaData.supportsTransactions() in stead of this.
   * @return true if the database supports transactions
   */
  @Deprecated
  public boolean supportsTransactions() {
    return databaseInterface.supportsTransactions();
  }

  public boolean supportsAutoinc() {
    return databaseInterface.supportsAutoInc();
  }

  public boolean supportsSequences() {
    return databaseInterface.supportsSequences();
  }

  public String getSQLSequenceExists( String sequenceName ) {
    return databaseInterface.getSQLSequenceExists( sequenceName );
  }

  public boolean supportsBitmapIndex() {
    return databaseInterface.supportsBitmapIndex();
  }

  public boolean supportsSetLong() {
    return databaseInterface.supportsSetLong();
  }

  /**
   * @return true if the database supports schemas
   */
  public boolean supportsSchemas() {
    return databaseInterface.supportsSchemas();
  }

  /**
   * @return true if the database supports catalogs
   */
  public boolean supportsCatalogs() {
    return databaseInterface.supportsCatalogs();
  }

  /**
   *
   * @return true when the database engine supports empty transaction. (for example Informix does not on a non-ANSI
   *         database type!)
   */
  public boolean supportsEmptyTransactions() {
    return databaseInterface.supportsEmptyTransactions();
  }

  /**
   * See if this database supports the setCharacterStream() method on a PreparedStatement.
   *
   * @return true if we can set a Stream on a field in a PreparedStatement. False if not.
   */
  public boolean supportsSetCharacterStream() {
    return databaseInterface.supportsSetCharacterStream();
  }

  /**
   * Get the maximum length of a text field for this database connection. This includes optional CLOB, Memo and Text
   * fields. (the maximum!)
   *
   * @return The maximum text field length for this database type. (mostly CLOB_LENGTH)
   */
  public int getMaxTextFieldLength() {
    return databaseInterface.getMaxTextFieldLength();
  }

  public static final int getAccessType( String dbaccess ) {
    int i;

    if ( dbaccess == null ) {
      return TYPE_ACCESS_NATIVE;
    }

    for ( i = 0; i < dbAccessTypeCode.length; i++ ) {
      if ( dbAccessTypeCode[i].equalsIgnoreCase( dbaccess ) ) {
        return i;
      }
    }
    for ( i = 0; i < dbAccessTypeDesc.length; i++ ) {
      if ( dbAccessTypeDesc[i].equalsIgnoreCase( dbaccess ) ) {
        return i;
      }
    }

    return TYPE_ACCESS_NATIVE;
  }

  public static final String getAccessTypeDesc( int dbaccess ) {
    if ( dbaccess < 0 ) {
      return null;
    }
    if ( dbaccess > dbAccessTypeCode.length ) {
      return null;
    }

    return dbAccessTypeCode[dbaccess];
  }

  public static final String getAccessTypeDescLong( int dbaccess ) {
    if ( dbaccess < 0 ) {
      return null;
    }
    if ( dbaccess > dbAccessTypeDesc.length ) {
      return null;
    }

    return dbAccessTypeDesc[dbaccess];
  }

  public static final DatabaseInterface[] getDatabaseInterfaces() {
    List<DatabaseInterface> list = new ArrayList<>( getDatabaseInterfacesMap().values() );
    return list.toArray( new DatabaseInterface[list.size()] );
  }

  /**
   * Clear the database interfaces map. The map is cached by getDatabaseInterfacesMap(), but in some instances it may
   * need to be reloaded (such as adding/updating Database plugins). After calling clearDatabaseInterfacesMap(), the
   * next call to getDatabaseInterfacesMap() will reload the map.
   */
  public static final void clearDatabaseInterfacesMap() {
    allDatabaseInterfaces = null;
  }

  private static final Future<Map<String, DatabaseInterface>> createDatabaseInterfacesMap() {
    return ExecutorUtil.getExecutor().submit( new Callable<Map<String, DatabaseInterface>>() {
      private Map<String, DatabaseInterface> doCreate() {
        LogChannelInterface log = LogChannel.GENERAL;
        PluginRegistry registry = PluginRegistry.getInstance();

        List<PluginInterface> plugins = registry.getPlugins( DatabasePluginType.class );
        HashMap<String, DatabaseInterface> tmpAllDatabaseInterfaces = new HashMap<>();
        for ( PluginInterface plugin : plugins ) {
          try {
            DatabaseInterface databaseInterface = (DatabaseInterface) registry.loadClass( plugin );
            databaseInterface.setPluginId( plugin.getIds()[ 0 ] );
            databaseInterface.setPluginName( plugin.getName() );
            tmpAllDatabaseInterfaces.put( plugin.getIds()[ 0 ], databaseInterface );
          } catch ( KettlePluginException cnfe ) {
            // System.out.println( "Could not create connection entry for " + plugin.getName() + ".  " + cnfe.getCause().getClass().getName() );
            log.logError( "Could not create connection entry for "
              + plugin.getName() + ".  " + cnfe.getCause().getClass().getName() );
            if ( log.isDebug() ) {
              log.logDebug( "Debug-Error loading plugin: " + plugin, cnfe );
            }
          } catch ( Exception e ) {
            log.logError( "Error loading plugin: " + plugin, e );
          }
        }
        return Collections.unmodifiableMap( tmpAllDatabaseInterfaces );
      }

      @Override public Map<String, DatabaseInterface> call() throws Exception {
        return doCreate();
      }
    } );
  }

  public static final Map<String, DatabaseInterface> getDatabaseInterfacesMap() {
    Future<Map<String, DatabaseInterface>> allDatabaseInterfaces = DatabaseMeta.allDatabaseInterfaces;
    while ( allDatabaseInterfaces == null ) {
      DatabaseMeta.allDatabaseInterfaces = createDatabaseInterfacesMap();
      allDatabaseInterfaces = DatabaseMeta.allDatabaseInterfaces;
    }
    try {
      return allDatabaseInterfaces.get();
    } catch ( Exception e ) {
      clearDatabaseInterfacesMap();
      // doCreate() above doesn't declare any exceptions so anything that comes out SHOULD be a runtime exception
      if ( e instanceof RuntimeException ) {
        throw (RuntimeException) e;
      } else {
        throw new RuntimeException( e );
      }
    }
  }

  public static final int[] getAccessTypeList( String dbTypeDesc ) {
    try {
      DatabaseInterface di = findDatabaseInterface( dbTypeDesc );
      return di.getAccessTypeList();
    } catch ( KettleDatabaseException kde ) {
      return null;
    }
  }

  public static final int getPortForDBType( String strtype, String straccess ) {
    try {
      DatabaseInterface di = getDatabaseInterface( strtype );
      di.setAccessType( getAccessType( straccess ) );
      return di.getDefaultDatabasePort();
    } catch ( KettleDatabaseException kde ) {
      return -1;
    }
  }

  public int getDefaultDatabasePort() {
    return databaseInterface.getDefaultDatabasePort();
  }

  public int getNotFoundTK( boolean use_autoinc ) {
    return databaseInterface.getNotFoundTK( use_autoinc );
  }

  public String getDriverClass() {
    return environmentSubstitute( databaseInterface.getDriverClass() );
  }

  public String stripCR( String sbsql ) {
    if ( sbsql == null ) {
      return null;
    }
    return stripCR( new StringBuilder( sbsql ) );
  }

  public String stripCR( StringBuffer sbsql ) {
    // DB2 Can't handle \n in SQL Statements...
    if ( !supportsNewLinesInSQL() ) {
      // Remove CR's
      for ( int i = sbsql.length() - 1; i >= 0; i-- ) {
        if ( sbsql.charAt( i ) == '\n' || sbsql.charAt( i ) == '\r' ) {
          sbsql.setCharAt( i, ' ' );
        }
      }
    }

    return sbsql.toString();
  }

  public String stripCR( StringBuilder sbsql ) {
    // DB2 Can't handle \n in SQL Statements...
    if ( !supportsNewLinesInSQL() ) {
      // Remove CR's
      for ( int i = sbsql.length() - 1; i >= 0; i-- ) {
        if ( sbsql.charAt( i ) == '\n' || sbsql.charAt( i ) == '\r' ) {
          sbsql.setCharAt( i, ' ' );
        }
      }
    }

    return sbsql.toString();
  }

  public String getSeqNextvalSQL( String sequenceName ) {
    return databaseInterface.getSQLNextSequenceValue( sequenceName );
  }

  public String getSQLCurrentSequenceValue( String sequenceName ) {
    return databaseInterface.getSQLCurrentSequenceValue( sequenceName );
  }

  public boolean isFetchSizeSupported() {
    return databaseInterface.isFetchSizeSupported();
  }

  /**
   * Indicates the need to insert a placeholder (0) for auto increment fields.
   *
   * @return true if we need a placeholder for auto increment fields in insert statements.
   */
  public boolean needsPlaceHolder() {
    return databaseInterface.needsPlaceHolder();
  }

  public String getFunctionSum() {
    return databaseInterface.getFunctionSum();
  }

  public String getFunctionAverage() {
    return databaseInterface.getFunctionAverage();
  }

  public String getFunctionMaximum() {
    return databaseInterface.getFunctionMaximum();
  }

  public String getFunctionMinimum() {
    return databaseInterface.getFunctionMinimum();
  }

  public String getFunctionCount() {
    return databaseInterface.getFunctionCount();
  }

  /**
   * Check the database connection parameters and give back an array of remarks
   *
   * @return an array of remarks Strings
   */
  public String[] checkParameters() {
    ArrayList<String> remarks = new ArrayList<>();

    if ( getDatabaseInterface() == null ) {
      remarks.add( BaseMessages.getString( PKG, "DatabaseMeta.BadInterface" ) );
    }

    if ( getName() == null || getName().length() == 0 ) {
      remarks.add( BaseMessages.getString( PKG, "DatabaseMeta.BadConnectionName" ) );
    }

    if ( !isPartitioned()
      && ( ( (BaseDatabaseMeta) getDatabaseInterface() ).requiresName()
      && !( getDatabaseInterface() instanceof GenericDatabaseMeta ) ) ) {
      if ( getDatabaseName() == null || getDatabaseName().length() == 0 ) {
        remarks.add( BaseMessages.getString( PKG, "DatabaseMeta.BadDatabaseName" ) );
      }
    }

    return remarks.toArray( new String[ remarks.size() ] );
  }

  /**
   * This is now replaced with getQuotedSchemaTableCombination(), enforcing the use of the quoteFields call
   *
   * @param schemaName
   * @param tableName
   * @return
   * @deprecated please use getQuotedSchemaTableCombination()
   */
  @Deprecated
  public String getSchemaTableCombination( String schemaName, String tableName ) {
    return getQuotedSchemaTableCombination( schemaName, tableName );
  }

  /**
   * Calculate the schema-table combination, usually this is the schema and table separated with a dot. (schema.table)
   *
   * @param schemaName
   *          the schema-name or null if no schema is used.
   * @param tableName
   *          the table name
   * @return the schemaname-tablename combination
   */
  public String getQuotedSchemaTableCombination( String schemaName, String tableName ) {
    if ( Utils.isEmpty( schemaName ) ) {
      if ( Utils.isEmpty( getPreferredSchemaName() ) ) {
        return quoteField( environmentSubstitute( tableName ) ); // no need to look further
      } else {
        return databaseInterface.getSchemaTableCombination(
          quoteField( environmentSubstitute( getPreferredSchemaName() ) ),
          quoteField( environmentSubstitute( tableName ) ) );
      }
    } else {
      return databaseInterface.getSchemaTableCombination(
        quoteField( environmentSubstitute( schemaName ) ), quoteField( environmentSubstitute( tableName ) ) );
    }
  }

  public boolean isClob( ValueMetaInterface v ) {
    boolean retval = true;

    if ( v == null || v.getLength() < DatabaseMeta.CLOB_LENGTH ) {
      retval = false;
    } else {
      return true;
    }
    return retval;
  }

  public String getFieldDefinition( ValueMetaInterface v, String tk, String pk, boolean use_autoinc ) {
    return getFieldDefinition( v, tk, pk, use_autoinc, true, true );
  }

  public String getFieldDefinition( ValueMetaInterface v, String tk, String pk, boolean use_autoinc,
    boolean add_fieldname, boolean add_cr ) {

    String definition =
      v.getDatabaseColumnTypeDefinition( databaseInterface, tk, pk, use_autoinc, add_fieldname, add_cr );
    if ( !Utils.isEmpty( definition ) ) {
      return definition;
    }

    return databaseInterface.getFieldDefinition( v, tk, pk, use_autoinc, add_fieldname, add_cr );
  }

  public String getLimitClause( int nrRows ) {
    return databaseInterface.getLimitClause( nrRows );
  }

  /**
   * @param tableName
   *          The table or schema-table combination. We expect this to be quoted properly already!
   * @return the SQL for to get the fields of this table.
   */
  public String getSQLQueryFields( String tableName ) {
    return databaseInterface.getSQLQueryFields( tableName );
  }

  public String getAddColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean use_autoinc,
    String pk, boolean semicolon ) {
    String retval = databaseInterface.getAddColumnStatement( tablename, v, tk, use_autoinc, pk, semicolon );
    retval += Const.CR;
    if ( semicolon ) {
      retval += ";" + Const.CR;
    }
    return retval;
  }

  public String getDropColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean use_autoinc,
    String pk, boolean semicolon ) {
    String retval = databaseInterface.getDropColumnStatement( tablename, v, tk, use_autoinc, pk, semicolon );
    retval += Const.CR;
    if ( semicolon ) {
      retval += ";" + Const.CR;
    }
    return retval;
  }

  public String getModifyColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean use_autoinc,
    String pk, boolean semicolon ) {
    String retval = databaseInterface.getModifyColumnStatement( tablename, v, tk, use_autoinc, pk, semicolon );
    retval += Const.CR;
    if ( semicolon ) {
      retval += ";" + Const.CR;
    }

    return retval;
  }

  /**
   * @return an array of reserved words for the database type...
   */
  public String[] getReservedWords() {
    return databaseInterface.getReservedWords();
  }

  /**
   * @return true if reserved words need to be double quoted ("password", "select", ...)
   */
  public boolean quoteReservedWords() {
    return databaseInterface.quoteReservedWords();
  }

  /**
   * @return The start quote sequence, mostly just double quote, but sometimes [, ...
   */
  public String getStartQuote() {
    return databaseInterface.getStartQuote();
  }

  /**
   * @return The end quote sequence, mostly just double quote, but sometimes ], ...
   */
  public String getEndQuote() {
    return databaseInterface.getEndQuote();
  }

  /**
   * Returns a quoted field if this is needed: contains spaces, is a reserved word, ...
   *
   * @param field
   *          The fieldname to check for quoting
   * @return The quoted field (if this is needed.
   */
  public String quoteField( String field ) {
    if ( Utils.isEmpty( field ) ) {
      return null;
    }

    if ( isForcingIdentifiersToLowerCase() ) {
      field = field.toLowerCase();
    } else if ( isForcingIdentifiersToUpperCase() ) {
      field = field.toUpperCase();
    }

    // If the field already contains quotes, we don't touch it anymore, just return the same string...
    if ( field.indexOf( getStartQuote() ) >= 0 || field.indexOf( getEndQuote() ) >= 0 ) {
      return field;
    }

    if ( isReservedWord( field ) && quoteReservedWords() ) {
      return handleCase( getStartQuote() + field + getEndQuote() );
    } else {
      if ( databaseInterface.isQuoteAllFields()
        || hasSpacesInField( field ) || hasSpecialCharInField( field ) || hasDotInField( field ) ) {
        return getStartQuote() + field + getEndQuote();
      } else {
        return field;
      }
    }
  }

  private String handleCase( String field ) {
    if ( preserveReservedCase() ) {
      return field;
    } else {
      if ( databaseInterface.isDefaultingToUppercase() ) {
        return field.toUpperCase();
      } else {
        return field.toLowerCase();
      }
    }
  }

  /**
   * Determines whether or not this field is in need of quoting:<br>
   * - When the fieldname contains spaces<br>
   * - When the fieldname is a reserved word<br>
   *
   * @param fieldname
   *          the fieldname to check if there is a need for quoting
   * @return true if the fieldname needs to be quoted.
   */
  public boolean isInNeedOfQuoting( String fieldname ) {
    return isReservedWord( fieldname ) || hasSpacesInField( fieldname );
  }

  /**
   * Returns true if the string specified is a reserved word on this database type.
   *
   * @param word
   *          The word to check
   * @return true if word is a reserved word on this database.
   */
  public boolean isReservedWord( String word ) {
    String[] reserved = getReservedWords();
    if ( Const.indexOfString( word, reserved ) >= 0 ) {
      return true;
    }
    return false;
  }

  /**
   * Detects if a field has spaces in the name. We need to quote the field in that case.
   *
   * @param fieldname
   *          The fieldname to check for spaces
   * @return true if the fieldname contains spaces
   */
  public boolean hasSpacesInField( String fieldname ) {
    if ( fieldname == null ) {
      return false;
    }
    if ( fieldname.indexOf( ' ' ) >= 0 ) {
      return true;
    }
    return false;
  }

  /**
   * Detects if a field has spaces in the name. We need to quote the field in that case.
   *
   * @param fieldname
   *          The fieldname to check for spaces
   * @return true if the fieldname contains spaces
   */
  public boolean hasSpecialCharInField( String fieldname ) {
    if ( fieldname == null ) {
      return false;
    }
    if ( fieldname.indexOf( '/' ) >= 0 ) {
      return true;
    }
    if ( fieldname.indexOf( '-' ) >= 0 ) {
      return true;
    }
    if ( fieldname.indexOf( '+' ) >= 0 ) {
      return true;
    }
    if ( fieldname.indexOf( ',' ) >= 0 ) {
      return true;
    }
    if ( fieldname.indexOf( '*' ) >= 0 ) {
      return true;
    }
    if ( fieldname.indexOf( '(' ) >= 0 ) {
      return true;
    }
    if ( fieldname.indexOf( ')' ) >= 0 ) {
      return true;
    }
    if ( fieldname.indexOf( '{' ) >= 0 ) {
      return true;
    }
    if ( fieldname.indexOf( '}' ) >= 0 ) {
      return true;
    }
    if ( fieldname.indexOf( '[' ) >= 0 ) {
      return true;
    }
    if ( fieldname.indexOf( ']' ) >= 0 ) {
      return true;
    }
    if ( fieldname.indexOf( '%' ) >= 0 ) {
      return true;
    }
    if ( fieldname.indexOf( '@' ) >= 0 ) {
      return true;
    }
    if ( fieldname.indexOf( '?' ) >= 0 ) {
      return true;
    }
    return false;
  }

  public boolean hasDotInField( String fieldname ) {
    if ( fieldname == null ) {
      return false;
    }
    if ( fieldname.indexOf( '.' ) >= 0 ) {
      return true;
    }
    return false;
  }

  /**
   * Checks the fields specified for reserved words and quotes them.
   *
   * @param fields
   *          the list of fields to check
   * @return true if one or more values have a name that is a reserved word on this database type.
   */
  public boolean replaceReservedWords( RowMetaInterface fields ) {
    boolean hasReservedWords = false;
    for ( int i = 0; i < fields.size(); i++ ) {
      ValueMetaInterface v = fields.getValueMeta( i );
      if ( isReservedWord( v.getName() ) ) {
        hasReservedWords = true;
        v.setName( quoteField( v.getName() ) );
      }
    }
    return hasReservedWords;
  }

  /**
   * Checks the fields specified for reserved words
   *
   * @param fields
   *          the list of fields to check
   * @return The nr of reserved words for this database.
   */
  public int getNrReservedWords( RowMetaInterface fields ) {
    int nrReservedWords = 0;
    for ( int i = 0; i < fields.size(); i++ ) {
      ValueMetaInterface v = fields.getValueMeta( i );
      if ( isReservedWord( v.getName() ) ) {
        nrReservedWords++;
      }
    }
    return nrReservedWords;
  }

  /**
   * @return a list of types to get the available tables
   */
  public String[] getTableTypes() {
    return databaseInterface.getTableTypes();
  }

  /**
   * @return a list of types to get the available views
   */
  public String[] getViewTypes() {
    return databaseInterface.getViewTypes();
  }

  /**
   * @return a list of types to get the available synonyms
   */
  public String[] getSynonymTypes() {
    return databaseInterface.getSynonymTypes();
  }

  /**
   * @return true if we need to supply the schema-name to getTables in order to get a correct list of items.
   */
  public boolean useSchemaNameForTableList() {
    return databaseInterface.useSchemaNameForTableList();
  }

  /**
   * @return true if the database supports views
   */
  public boolean supportsViews() {
    return databaseInterface.supportsViews();
  }

  /**
   * @return true if the database supports synonyms
   */
  public boolean supportsSynonyms() {
    return databaseInterface.supportsSynonyms();
  }

  /**
   *
   * @return The SQL on this database to get a list of stored procedures.
   */
  public String getSQLListOfProcedures() {
    return databaseInterface.getSQLListOfProcedures();
  }

  /**
   * @param tableName
   *          The tablename to be truncated
   * @return The SQL statement to remove all rows from the specified statement, if possible without using transactions
   */
  public String getTruncateTableStatement( String schema, String tableName ) {
    return databaseInterface.getTruncateTableStatement( getQuotedSchemaTableCombination( schema, tableName ) );
  }

  /**
   * @return true if the database rounds floating point numbers to the right precision. For example if the target field
   *         is number(7,2) the value 12.399999999 is converted into 12.40
   */
  public boolean supportsFloatRoundingOnUpdate() {
    return databaseInterface.supportsFloatRoundingOnUpdate();
  }

  /**
   * @param tableNames
   *          The names of the tables to lock
   * @return The SQL commands to lock database tables for write purposes. null is returned in case locking is not
   *         supported on the target database.
   */
  public String getSQLLockTables( String[] tableNames ) {
    return databaseInterface.getSQLLockTables( tableNames );
  }

  /**
   * @param tableNames
   *          The names of the tables to unlock
   * @return The SQL commands to unlock databases tables. null is returned in case locking is not supported on the
   *         target database.
   */
  public String getSQLUnlockTables( String[] tableNames ) {
    return databaseInterface.getSQLUnlockTables( tableNames );
  }

  /**
   * @return a feature list for the chosen database type.
   *
   */
  public List<RowMetaAndData> getFeatureSummary() {
    List<RowMetaAndData> list = new ArrayList<>();
    RowMetaAndData r = null;
    final String par = "Parameter";
    final String val = "Value";

    ValueMetaInterface testValue = new ValueMetaString( "FIELD" );
    testValue.setLength( 30 );

    if ( databaseInterface != null ) {
      // Type of database
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "Database type" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, getPluginId() );
      list.add( r );
      // Type of access
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "Access type" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, getAccessTypeDesc() );
      list.add( r );
      // Name of database
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "Database name" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, getDatabaseName() );
      list.add( r );
      // server host name
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "Server hostname" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, getHostname() );
      list.add( r );
      // Port number
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "Service port" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, getDatabasePortNumberString() );
      list.add( r );
      // Username
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "Username" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, getUsername() );
      list.add( r );
      // Informix server
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "Informix server name" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, getServername() );
      list.add( r );
      // Other properties...
      Enumeration<Object> keys = getAttributes().keys();
      while ( keys.hasMoreElements() ) {
        String key = (String) keys.nextElement();
        String value = getAttributes().getProperty( key );
        r = new RowMetaAndData();
        r.addValue( par, ValueMetaInterface.TYPE_STRING, "Extra attribute [" + key + "]" );
        r.addValue( val, ValueMetaInterface.TYPE_STRING, value );
        list.add( r );
      }

      // driver class
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "Driver class" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, getDriverClass() );
      list.add( r );
      // URL
      String pwd = getPassword();
      setPassword( "password" ); // Don't give away the password in the URL!
      String url = "";
      try {
        url = getURL();
      } catch ( Exception e ) {
        url = "";
      } // SAP etc.
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "URL" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, url );
      list.add( r );
      setPassword( pwd );
      // SQL: Next sequence value
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "SQL: next sequence value" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, getSeqNextvalSQL( "SEQUENCE" ) );
      list.add( r );
      // is set fetch size supported
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "supported: set fetch size" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, isFetchSizeSupported() ? "Y" : "N" );
      list.add( r );
      // needs place holder for auto increment
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "auto increment field needs placeholder" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, needsPlaceHolder() ? "Y" : "N" );
      list.add( r );
      // Sum function
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "SUM aggregate function" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, getFunctionSum() );
      list.add( r );
      // Avg function
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "AVG aggregate function" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, getFunctionAverage() );
      list.add( r );
      // Minimum function
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "MIN aggregate function" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, getFunctionMinimum() );
      list.add( r );
      // Maximum function
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "MAX aggregate function" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, getFunctionMaximum() );
      list.add( r );
      // Count function
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "COUNT aggregate function" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, getFunctionCount() );
      list.add( r );
      // Schema-table combination
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "Schema / Table combination" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, getQuotedSchemaTableCombination( "SCHEMA", "TABLE" ) );
      list.add( r );
      // Limit clause
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "LIMIT clause for 100 rows" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, getLimitClause( 100 ) );
      list.add( r );
      // add column statement
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "Add column statement" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, getAddColumnStatement(
        "TABLE", testValue, null, false, null, false ) );
      list.add( r );
      // drop column statement
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "Drop column statement" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, getDropColumnStatement(
        "TABLE", testValue, null, false, null, false ) );
      list.add( r );
      // Modify column statement
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "Modify column statement" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, getModifyColumnStatement(
        "TABLE", testValue, null, false, null, false ) );
      list.add( r );

      // List of reserved words
      String reserved = "";
      if ( getReservedWords() != null ) {
        for ( int i = 0; i < getReservedWords().length; i++ ) {
          reserved += ( i > 0 ? ", " : "" ) + getReservedWords()[i];
        }
      }
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "List of reserved words" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, reserved );
      list.add( r );

      // Quote reserved words?
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "Quote reserved words?" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, quoteReservedWords() ? "Y" : "N" );
      list.add( r );
      // Start Quote
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "Start quote for reserved words" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, getStartQuote() );
      list.add( r );
      // End Quote
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "End quote for reserved words" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, getEndQuote() );
      list.add( r );

      // List of table types
      String types = "";
      String[] slist = getTableTypes();
      if ( slist != null ) {
        for ( int i = 0; i < slist.length; i++ ) {
          types += ( i > 0 ? ", " : "" ) + slist[i];
        }
      }
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "List of JDBC table types" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, types );
      list.add( r );

      // List of view types
      types = "";
      slist = getViewTypes();
      if ( slist != null ) {
        for ( int i = 0; i < slist.length; i++ ) {
          types += ( i > 0 ? ", " : "" ) + slist[i];
        }
      }
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "List of JDBC view types" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, types );
      list.add( r );

      // List of synonym types
      types = "";
      slist = getSynonymTypes();
      if ( slist != null ) {
        for ( int i = 0; i < slist.length; i++ ) {
          types += ( i > 0 ? ", " : "" ) + slist[i];
        }
      }
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "List of JDBC synonym types" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, types );
      list.add( r );

      // Use schema-name to get list of tables?
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "use schema name to get table list?" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, useSchemaNameForTableList() ? "Y" : "N" );
      list.add( r );
      // supports view?
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "supports views?" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, supportsViews() ? "Y" : "N" );
      list.add( r );
      // supports synonyms?
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "supports synonyms?" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, supportsSynonyms() ? "Y" : "N" );
      list.add( r );
      // SQL: get list of procedures?
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "SQL: list of procedures" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, getSQLListOfProcedures() );
      list.add( r );
      // SQL: get truncate table statement?
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "SQL: truncate table" );
      String truncateStatement = getTruncateTableStatement( null, "TABLE" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, truncateStatement != null
        ? truncateStatement : "Not supported by this database type" );
      list.add( r );
      // supports float rounding on update?
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "supports floating point rounding on update/insert" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, supportsFloatRoundingOnUpdate() ? "Y" : "N" );
      list.add( r );
      // supports time stamp to date conversion
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "supports timestamp-date conversion" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, supportsTimeStampToDateConversion() ? "Y" : "N" );
      list.add( r );
      // supports batch updates
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "supports batch updates" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, supportsBatchUpdates() ? "Y" : "N" );
      list.add( r );
      // supports boolean values
      r = new RowMetaAndData();
      r.addValue( par, ValueMetaInterface.TYPE_STRING, "supports boolean data type" );
      r.addValue( val, ValueMetaInterface.TYPE_STRING, supportsBooleanDataType() ? "Y" : "N" );
      list.add( r );
    }

    return list;
  }

  /**
   * @return true if the database result sets support getTimeStamp() to retrieve date-time. (Date)
   */
  public boolean supportsTimeStampToDateConversion() {
    return databaseInterface.supportsTimeStampToDateConversion();
  }

  /**
   * @return true if the database JDBC driver supports batch updates For example Interbase doesn't support this!
   */
  public boolean supportsBatchUpdates() {
    return databaseInterface.supportsBatchUpdates();
  }

  /**
   * @return true if the database supports a boolean, bit, logical, ... datatype
   */
  public boolean supportsBooleanDataType() {
    return databaseInterface.supportsBooleanDataType();
  }

  /**
   *
   * @param b
   *          Set to true if the database supports a boolean, bit, logical, ... datatype
   */
  public void setSupportsBooleanDataType( boolean b ) {
    databaseInterface.setSupportsBooleanDataType( b );
  }

  /**
   * @return true if the database supports the Timestamp data type (nanosecond precision and all)
   */
  public boolean supportsTimestampDataType() {
    return databaseInterface.supportsTimestampDataType();
  }

  /**
   *
   * @param b
   *          Set to true if the database supports the Timestamp data type (nanosecond precision and all)
   */
  public void setSupportsTimestampDataType( boolean b ) {
    databaseInterface.setSupportsTimestampDataType( b );
  }

  /**
   * @return true if reserved words' case should be preserved
   */
  public boolean preserveReservedCase() {
    return databaseInterface.preserveReservedCase();
  }

  /**
   * @return true if reserved words' case should be preserved
   */
  public void setPreserveReservedCase( boolean b ) {
    databaseInterface.setPreserveReservedCase( b );
  }

  /**
   * Changes the names of the fields to their quoted equivalent if this is needed
   *
   * @param fields
   *          The row of fields to change
   */
  public void quoteReservedWords( RowMetaInterface fields ) {
    for ( int i = 0; i < fields.size(); i++ ) {
      ValueMetaInterface v = fields.getValueMeta( i );
      v.setName( quoteField( v.getName() ) );
    }
  }

  /**
   * @return a map of all the extra URL options you want to set.
   */
  public Map<String, String> getExtraOptions() {
    return databaseInterface.getExtraOptions();
  }

  /**
   * @return true if the database supports connection options in the URL, false if they are put in a Properties object.
   */
  public boolean supportsOptionsInURL() {
    return databaseInterface.supportsOptionsInURL();
  }

  /**
   * @return extra help text on the supported options on the selected database platform.
   */
  public String getExtraOptionsHelpText() {
    return databaseInterface.getExtraOptionsHelpText();
  }

  /**
   * @return true if the database JDBC driver supports getBlob on the resultset. If not we must use getBytes() to get
   *         the data.
   */
  public boolean supportsGetBlob() {
    return databaseInterface.supportsGetBlob();
  }

  /**
   * @return The SQL to execute right after connecting
   */
  public String getConnectSQL() {
    return databaseInterface.getConnectSQL();
  }

  /**
   * @param sql
   *          The SQL to execute right after connecting
   */
  public void setConnectSQL( String sql ) {
    databaseInterface.setConnectSQL( sql );
  }

  /**
   * @return true if the database supports setting the maximum number of return rows in a resultset.
   */
  public boolean supportsSetMaxRows() {
    return databaseInterface.supportsSetMaxRows();
  }

  /**
   * Verify the name of the database and if required, change it if it already exists in the list of databases.
   *
   * @param databases
   *          the databases to check against.
   * @param oldname
   *          the old name of the database
   * @return the new name of the database connection
   */
  public String verifyAndModifyDatabaseName( List<DatabaseMeta> databases, String oldname ) {
    String name = getName();
    if ( name.equalsIgnoreCase( oldname ) ) {
      return name; // nothing to see here: move along!
    }

    int nr = 2;
    while ( DatabaseMeta.findDatabase( databases, getName() ) != null ) {
      setName( name + " " + nr );
      setDisplayName( name + " " + nr );
      nr++;
    }
    return getName();
  }

  /**
   * @return true if we want to use a database connection pool
   */
  public boolean isUsingConnectionPool() {
    return databaseInterface.isUsingConnectionPool();
  }

  /**
   * @param usePool
   *          true if we want to use a database connection pool
   */
  public void setUsingConnectionPool( boolean usePool ) {
    databaseInterface.setUsingConnectionPool( usePool );
  }

  /**
   * @return the maximum pool size
   */
  public int getMaximumPoolSize() {
    return Const.toInt(
      environmentSubstitute( getMaximumPoolSizeString() ), ConnectionPoolUtil.defaultMaximumNrOfConnections );
  }

  /**
   * @return the maximum pool size variable name
   */
  public String getMaximumPoolSizeString() {
    return databaseInterface.getMaximumPoolSizeString();
  }

  /**
   * @param maximumPoolSize
   *          the maximum pool size
   */
  public void setMaximumPoolSize( int maximumPoolSize ) {
    databaseInterface.setMaximumPoolSize( maximumPoolSize );
  }

  /**
   * @param maximumPoolSize
   *          the maximum pool size variable name
   */
  public void setMaximumPoolSizeString( String maximumPoolSize ) {
    databaseInterface.setMaximumPoolSizeString( maximumPoolSize );
  }

  /**
   * @return the initial pool size
   */
  public int getInitialPoolSize() {
    return Const.toInt(
      environmentSubstitute( getInitialPoolSizeString() ), ConnectionPoolUtil.defaultInitialNrOfConnections );
  }

  /**
   * @return the initial pool size variable name
   */
  public String getInitialPoolSizeString() {
    return databaseInterface.getInitialPoolSizeString();
  }

  /**
   * @param initalPoolSize
   *          the initial pool size
   */
  public void setInitialPoolSize( int initalPoolSize ) {
    databaseInterface.setInitialPoolSize( initalPoolSize );
  }

  /**
    * @param initalPoolSize
   *          the initial pool size variable name
   */
  public void setInitialPoolSizeString( String initalPoolSize ) {
    databaseInterface.setInitialPoolSizeString( initalPoolSize );
  }

  /**
   * @return true if the connection contains partitioning information
   */
  public boolean isPartitioned() {
    return databaseInterface.isPartitioned();
  }

  /**
   * @param partitioned
   *          true if the connection is set to contain partitioning information
   */
  public void setPartitioned( boolean partitioned ) {
    databaseInterface.setPartitioned( partitioned );
  }

  /**
   * @return the available partition/host/databases/port combinations in the cluster
   */
  public PartitionDatabaseMeta[] getPartitioningInformation() {
    if ( !isPartitioned() ) {
      return new PartitionDatabaseMeta[] {};
    }
    return databaseInterface.getPartitioningInformation();
  }

  /**
   * @param partitionInfo
   *          the available partition/host/databases/port combinations in the cluster
   */
  public void setPartitioningInformation( PartitionDatabaseMeta[] partitionInfo ) {
    databaseInterface.setPartitioningInformation( partitionInfo );
  }

  /**
   * Finds the partition metadata for the given partition iD
   *
   * @param partitionId
   *          The partition ID to look for
   * @return the partition database metadata or null if nothing was found.
   */
  public PartitionDatabaseMeta getPartitionMeta( String partitionId ) {
    PartitionDatabaseMeta[] partitionInfo = getPartitioningInformation();
    for ( int i = 0; i < partitionInfo.length; i++ ) {
      if ( partitionInfo[i].getPartitionId().equals( partitionId ) ) {
        return partitionInfo[i];
      }
    }
    return null;
  }

  public Properties getConnectionPoolingProperties() {
    return databaseInterface.getConnectionPoolingProperties();
  }

  public void setConnectionPoolingProperties( Properties properties ) {
    databaseInterface.setConnectionPoolingProperties( properties );
  }

  public String getSQLTableExists( String tablename ) {
    return databaseInterface.getSQLTableExists( tablename );
  }

  public String getSQLColumnExists( String columnname, String tablename ) {
    return databaseInterface.getSQLColumnExists( columnname, tablename );
  }

  public boolean needsToLockAllTables() {
    return databaseInterface.needsToLockAllTables();
  }

  /**
   * @return true if the database is streaming results (normally this is an option just for MySQL).
   */
  public boolean isStreamingResults() {
    return databaseInterface.isStreamingResults();
  }

  /**
   * @param useStreaming
   *          true if we want the database to stream results (normally this is an option just for MySQL).
   */
  public void setStreamingResults( boolean useStreaming ) {
    databaseInterface.setStreamingResults( useStreaming );
  }

  /**
   * @return true if all fields should always be quoted in db
   */
  public boolean isQuoteAllFields() {
    return databaseInterface.isQuoteAllFields();
  }

  /**
   * @param quoteAllFields
   *          true if all fields in DB should be quoted.
   */
  public void setQuoteAllFields( boolean quoteAllFields ) {
    databaseInterface.setQuoteAllFields( quoteAllFields );
  }

  /**
   * @return true if all identifiers should be forced to lower case
   */
  public boolean isForcingIdentifiersToLowerCase() {
    return databaseInterface.isForcingIdentifiersToLowerCase();
  }

  /**
   * @param forceLowerCase
   *          true if all identifiers should be forced to lower case
   */
  public void setForcingIdentifiersToLowerCase( boolean forceLowerCase ) {
    databaseInterface.setForcingIdentifiersToLowerCase( forceLowerCase );
  }

  /**
   * @return true if all identifiers should be forced to upper case
   */
  public boolean isForcingIdentifiersToUpperCase() {
    return databaseInterface.isForcingIdentifiersToUpperCase();
  }

  /**
   * @param forceUpperCase
   *          true if all identifiers should be forced to upper case
   */
  public void setForcingIdentifiersToUpperCase( boolean forceUpperCase ) {
    databaseInterface.setForcingIdentifiersToUpperCase( forceUpperCase );
  }

  /**
   * Find a database with a certain name in an arraylist of databases.
   *
   * @param databases
   *          The ArrayList of databases
   * @param dbname
   *          The name of the database connection
   * @return The database object if one was found, null otherwise.
   */
  public static final DatabaseMeta findDatabase( List<? extends SharedObjectInterface> databases, String dbname ) {
    if ( databases == null || dbname == null ) {
      return null;
    }

    for ( int i = 0; i < databases.size(); i++ ) {
      DatabaseMeta ci = (DatabaseMeta) databases.get( i );
      if ( ci.getName().trim().equalsIgnoreCase( dbname.trim() ) ) {
        return ci;
      }
    }
    return null;
  }

  public static int indexOfName( String[] databaseNames, String name ) {
    if ( databaseNames == null || name == null ) {
      return -1;
    }

    for ( int i = 0; i < databaseNames.length; i++ ) {
      String databaseName = databaseNames[ i ];
      if ( name.equalsIgnoreCase( databaseName ) ) {
        return i;
      }
    }

    return -1;
  }

  /**
   * Find a database with a certain ID in an arraylist of databases.
   *
   * @param databases
   *          The ArrayList of databases
   * @param id
   *          The id of the database connection
   * @return The database object if one was found, null otherwise.
   */
  public static final DatabaseMeta findDatabase( List<DatabaseMeta> databases, ObjectId id ) {
    if ( databases == null ) {
      return null;
    }

    for ( DatabaseMeta ci : databases ) {
      if ( ci.getObjectId() != null && ci.getObjectId().equals( id ) ) {
        return ci;
      }
    }
    return null;
  }

  @Override
  public void copyVariablesFrom( VariableSpace space ) {
    variables.copyVariablesFrom( space );
  }

  @Override
  public String environmentSubstitute( String aString ) {
    return variables.environmentSubstitute( aString );
  }

  @Override
  public String[] environmentSubstitute( String[] aString ) {
    return variables.environmentSubstitute( aString );
  }

  @Override
  public String fieldSubstitute( String aString, RowMetaInterface rowMeta, Object[] rowData ) throws KettleValueException {
    return variables.fieldSubstitute( aString, rowMeta, rowData );
  }

  @Override
  public VariableSpace getParentVariableSpace() {
    return variables.getParentVariableSpace();
  }

  @Override
  public void setParentVariableSpace( VariableSpace parent ) {
    variables.setParentVariableSpace( parent );
  }

  @Override
  public String getVariable( String variableName, String defaultValue ) {
    return variables.getVariable( variableName, defaultValue );
  }

  @Override
  public String getVariable( String variableName ) {
    return variables.getVariable( variableName );
  }

  @Override
  public boolean getBooleanValueOfVariable( String variableName, boolean defaultValue ) {
    if ( !Utils.isEmpty( variableName ) ) {
      String value = environmentSubstitute( variableName );
      if ( !Utils.isEmpty( value ) ) {
        return ValueMetaBase.convertStringToBoolean( value );
      }
    }
    return defaultValue;
  }

  @Override
  public void initializeVariablesFrom( VariableSpace parent ) {
    variables.initializeVariablesFrom( parent );
  }

  @Override
  public String[] listVariables() {
    return variables.listVariables();
  }

  @Override
  public void setVariable( String variableName, String variableValue ) {
    variables.setVariable( variableName, variableValue );
  }

  @Override
  public void shareVariablesWith( VariableSpace space ) {
    variables = space;
  }

  @Override
  public void injectVariables( Map<String, String> prop ) {
    variables.injectVariables( prop );
  }

  /**
   * @return the SQL Server instance
   */
  public String getSQLServerInstance() {
    // This is also covered/persisted by JDBC option MS SQL Server / instancename / <somevalue>
    // We want to return <somevalue>
    // --> MSSQL.instancename
    return getExtraOptions().get( getPluginId() + ".instance" );
  }

  /**
   * @param instanceName
   *          the SQL Server instance
   */
  public void setSQLServerInstance( String instanceName ) {
    // This is also covered/persisted by JDBC option MS SQL Server / instancename / <somevalue>
    // We want to return set <somevalue>
    // --> MSSQL.instancename
    if ( ( instanceName != null ) && ( instanceName.length() > 0 ) ) {
      addExtraOption( getPluginId(), "instance", instanceName );
    }
  }

  /**
   * @return true if the Microsoft SQL server uses two decimals (..) to separate schema and table (default==false).
   */
  public boolean isUsingDoubleDecimalAsSchemaTableSeparator() {
    return databaseInterface.isUsingDoubleDecimalAsSchemaTableSeparator();
  }

  /**
   * @param useDoubleDecimalSeparator
   *          true if we want the database to stream results (normally this is an option just for MySQL).
   */
  public void setUsingDoubleDecimalAsSchemaTableSeparator( boolean useDoubleDecimalSeparator ) {
    databaseInterface.setUsingDoubleDecimalAsSchemaTableSeparator( useDoubleDecimalSeparator );
  }

  /**
   * @return true if this database needs a transaction to perform a query (auto-commit turned off).
   */
  public boolean isRequiringTransactionsOnQueries() {
    return databaseInterface.isRequiringTransactionsOnQueries();
  }

  public String testConnection() {

    StringBuilder report = new StringBuilder();

    // If the plug-in needs to provide connection information, we ask the DatabaseInterface...
    //
    try {
      DatabaseFactoryInterface factory = getDatabaseFactory();
      return factory.getConnectionTestReport( this );
    } catch ( ClassNotFoundException e ) {
      report
        .append( BaseMessages.getString( PKG, "BaseDatabaseMeta.TestConnectionReportNotImplemented.Message" ) )
        .append( Const.CR );
      report.append( BaseMessages.getString( PKG, "DatabaseMeta.report.ConnectionError", getName() )
        + e.toString() + Const.CR );
      report.append( Const.getStackTracker( e ) + Const.CR );
    } catch ( Exception e ) {
      report.append( BaseMessages.getString( PKG, "DatabaseMeta.report.ConnectionError", getName() )
        + e.toString() + Const.CR );
      report.append( Const.getStackTracker( e ) + Const.CR );
    }
    return report.toString();
  }

  public DatabaseTestResults testConnectionSuccess() {

    StringBuilder report = new StringBuilder();
    DatabaseTestResults databaseTestResults = new DatabaseTestResults();

    // If the plug-in needs to provide connection information, we ask the DatabaseInterface...
    //
    try {
      DatabaseFactoryInterface factory = getDatabaseFactory();
      databaseTestResults = factory.getConnectionTestResults( this );
    } catch ( ClassNotFoundException e ) {
      report
        .append( BaseMessages.getString( PKG, "BaseDatabaseMeta.TestConnectionReportNotImplemented.Message" ) )
        .append( Const.CR );
      report.append( BaseMessages.getString( PKG, "DatabaseMeta.report.ConnectionError", getName() )
        + e.toString() + Const.CR );
      report.append( Const.getStackTracker( e ) + Const.CR );
      databaseTestResults.setMessage( report.toString() );
      databaseTestResults.setSuccess( false );
    } catch ( Exception e ) {
      report.append( BaseMessages.getString( PKG, "DatabaseMeta.report.ConnectionError", getName() )
        + e.toString() + Const.CR );
      report.append( Const.getStackTracker( e ) + Const.CR );
      databaseTestResults.setMessage( report.toString() );
      databaseTestResults.setSuccess( false );
    }
    return databaseTestResults;
  }

  public DatabaseFactoryInterface getDatabaseFactory() throws Exception {
    PluginRegistry registry = PluginRegistry.getInstance();
    PluginInterface plugin = registry.getPlugin( DatabasePluginType.class, databaseInterface.getPluginId() );
    if ( plugin == null ) {
      throw new KettleDatabaseException( "database type with plugin id ["
        + databaseInterface.getPluginId() + "] couldn't be found!" );
    }

    ClassLoader loader = registry.getClassLoader( plugin );

    Class<?> clazz = Class.forName( databaseInterface.getDatabaseFactoryName(), true, loader );
    return (DatabaseFactoryInterface) clazz.newInstance();
  }

  public String getPreferredSchemaName() {
    return databaseInterface.getPreferredSchemaName();
  }

  public void setPreferredSchemaName( String preferredSchemaName ) {
    databaseInterface.setPreferredSchemaName( preferredSchemaName );
  }

  /**
   * Not used in this case, simply return root /
   */
  @Override
  public RepositoryDirectoryInterface getRepositoryDirectory() {
    return new RepositoryDirectory();
  }

  @Override
  public void setRepositoryDirectory( RepositoryDirectoryInterface repositoryDirectory ) {
    throw new RuntimeException( "Setting a directory on a database connection is not supported" );
  }

  @Override
  public RepositoryObjectType getRepositoryElementType() {
    return REPOSITORY_ELEMENT_TYPE;
  }

  @Override
  public ObjectRevision getObjectRevision() {
    return objectRevision;
  }

  @Override
  public void setObjectRevision( ObjectRevision objectRevision ) {
    this.objectRevision = objectRevision;
  }

  @Override
  public String getDescription() {
    // NOT USED
    return null;
  }

  @Override
  public void setDescription( String description ) {
    // NOT USED
  }

  public boolean supportsSequenceNoMaxValueOption() {
    return databaseInterface.supportsSequenceNoMaxValueOption();
  }

  public boolean requiresCreateTablePrimaryKeyAppend() {
    return databaseInterface.requiresCreateTablePrimaryKeyAppend();
  }

  public boolean requiresCastToVariousForIsNull() {
    return databaseInterface.requiresCastToVariousForIsNull();
  }

  public boolean isDisplaySizeTwiceThePrecision() {
    return databaseInterface.isDisplaySizeTwiceThePrecision();
  }

  public boolean supportsPreparedStatementMetadataRetrieval() {
    return databaseInterface.supportsPreparedStatementMetadataRetrieval();
  }

  public boolean isSystemTable( String tableName ) {
    return databaseInterface.isSystemTable( tableName );
  }

  private boolean supportsNewLinesInSQL() {
    return databaseInterface.supportsNewLinesInSQL();
  }

  public String getSQLListOfSchemas() {
    return databaseInterface.getSQLListOfSchemas( this );
  }

  public int getMaxColumnsInIndex() {
    return databaseInterface.getMaxColumnsInIndex();
  }

  public boolean supportsErrorHandlingOnBatchUpdates() {
    return databaseInterface.supportsErrorHandlingOnBatchUpdates();
  }

  /**
   * Get the SQL to insert a new empty unknown record in a dimension.
   *
   * @param schemaTable
   *          the schema-table name to insert into
   * @param keyField
   *          The key field
   * @param versionField
   *          the version field
   * @return the SQL to insert the unknown record into the SCD.
   */
  public String getSQLInsertAutoIncUnknownDimensionRow( String schemaTable, String keyField, String versionField ) {
    return databaseInterface.getSQLInsertAutoIncUnknownDimensionRow( schemaTable, keyField, versionField );
  }

  /**
   * @return true if this is a relational database you can explore. Return false for SAP, PALO, etc.
   */
  public boolean isExplorable() {
    return databaseInterface.isExplorable();
  }

  /**
   *
   * @return The SQL on this database to get a list of sequences.
   */
  public String getSQLListOfSequences() {
    return databaseInterface.getSQLListOfSequences();
  }

  public String quoteSQLString( String string ) {
    return databaseInterface.quoteSQLString( string );
  }

  /**
   * @see DatabaseInterface#generateColumnAlias(int, String)
   */
  public String generateColumnAlias( int columnIndex, String suggestedName ) {
    return databaseInterface.generateColumnAlias( columnIndex, suggestedName );
  }

  public boolean isMySQLVariant() {
    return databaseInterface.isMySQLVariant();
  }

  public Long getNextBatchId( Database ldb, String schemaName, String tableName, String fieldName ) throws KettleDatabaseException {
    return databaseInterface.getNextBatchId( this, ldb, schemaName, tableName, fieldName );
  }

  public Object getValueFromResultSet( ResultSet rs, ValueMetaInterface val, int i ) throws KettleDatabaseException {
    return databaseInterface.getValueFromResultSet( rs, val, i );
  }

  /**
   * Marker used to determine if the DatabaseMeta should be allowed to be modified/saved. It does NOT prevent object
   * modification.
   *
   * @return
   */
  public boolean isReadOnly() {
    return readOnly;
  }

  /**
   * Sets the marker used to determine if the DatabaseMeta should be allowed to be modified/saved. Setting to true does
   * NOT prevent object modification.
   *
   * @return
   */
  public void setReadOnly( boolean readOnly ) {
    this.readOnly = readOnly;
  }

  public String getSequenceNoMaxValueOption() {
    return databaseInterface.getSequenceNoMaxValueOption();
  }

  /**
   * @return true if the database supports autoGeneratedKeys
   */
  public boolean supportsAutoGeneratedKeys() {
    return databaseInterface.supportsAutoGeneratedKeys();
  }


  /**
   * Customizes the ValueMetaInterface defined in the base
   *
   * @return String the create table statement
   */
  public String getCreateTableStatement() {
    return databaseInterface.getCreateTableStatement();
  }

  /**
   * Forms the drop table statement specific for a certain RDBMS.
   *
   * @param tableName Name of the table to drop
   * @return Drop table statement specific for the current database
   * @see <a href="http://jira.pentaho.com/browse/BISERVER-13024">BISERVER-13024</a>
   */
  public String getDropTableIfExistsStatement( String tableName ) {
    if ( databaseInterface instanceof DatabaseInterfaceExtended ) {
      return ( (DatabaseInterfaceExtended) databaseInterface ).getDropTableIfExistsStatement( tableName );
    }
    // A fallback statement in case somehow databaseInterface is of an old version.
    // This is the previous, and in fact, buggy implementation. See BISERVER-13024.
    return DROP_TABLE_STATEMENT + tableName;
  }

  /**
   * For testing
   */
  protected LogChannelInterface getGeneralLogger() {
    return LogChannel.GENERAL;
  }

  /**
   * For testing
   */
  protected DatabaseInterface getDbInterface( String typeCode ) throws KettleDatabaseException {
    return getDatabaseInterface( typeCode );
  }

  public ResultSet getTables( DatabaseMetaData databaseMetaData, String schema, String table,
                              String[] tableTypesToGet ) throws SQLException {
    return databaseInterface.getTables( databaseMetaData, this, schema, table, tableTypesToGet );
  }

  public ResultSet getSchemas( DatabaseMetaData databaseMetaData ) throws SQLException {
    return databaseInterface.getSchemas( databaseMetaData, this );
  }

  public String getNamedCluster() {
    return databaseInterface.getNamedCluster();
  }

  public void setNamedCluster( String namedCluster ) {
    databaseInterface.setNamedCluster( namedCluster );
  }
}
