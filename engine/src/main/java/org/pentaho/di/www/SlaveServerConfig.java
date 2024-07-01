/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.www;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;
import org.w3c.dom.Node;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.function.Supplier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SlaveServerConfig {
  public static final String XML_TAG = "slave_config";
  public static final String XML_TAG_MASTERS = "masters";

  public static final String XML_TAG_REPOSITORY = "repository";
  public static final String XML_TAG_SEQUENCES = "sequences";
  public static final String XML_TAG_AUTOSEQUENCE = "autosequence";
  public static final String XML_TAG_AUTO_CREATE = "autocreate";
  public static final String XML_TAG_JETTY_OPTIONS = "jetty_options";
  public static final String XML_TAG_ACCEPTORS = "acceptors";
  public static final String XML_TAG_ACCEPT_QUEUE_SIZE = "acceptQueueSize";
  public static final String XML_TAG_LOW_RES_MAX_IDLE_TIME = "lowResourcesMaxIdleTime";

  private List<SlaveServer> masters;

  private SlaveServer slaveServer;

  private boolean reportingToMasters;

  private boolean joining;

  private int maxLogLines;

  private int maxLogTimeoutMinutes;

  private int objectTimeoutMinutes;

  private String filename;

  private List<DatabaseMeta> databases;
  private List<SlaveSequence> slaveSequences;

  private SlaveSequence autoSequence;

  private boolean automaticCreationAllowed;

  private Repository repository;
  private RepositoryMeta repositoryMeta;
  private String repositoryId;
  private String repositoryUsername;
  private String repositoryPassword;

  private Supplier<IMetaStore> metaStoreSupplier;

  private String passwordFile;

  public SlaveServerConfig() {
    masters = new ArrayList<SlaveServer>();
    databases = new ArrayList<DatabaseMeta>();
    slaveSequences = new ArrayList<SlaveSequence>();
    automaticCreationAllowed = false;
    metaStoreSupplier = () -> {
      IMetaStore metastore = MetaStoreConst.getDefaultMetastoreSupplier().get();
      if ( metastore != null ) {
        return metastore;
      }
      LogChannel.GENERAL.logError( "Unable to open local Pentaho meta store from [" + MetaStoreConst.getDefaultPentahoMetaStoreLocation() + "]");

      MemoryMetaStore memoryStore = new MemoryMetaStore();
      memoryStore.setName( "Memory metastore" );
      return memoryStore;
    };
    passwordFile = null; // force lookup by server in ~/.kettle or local folder
  }

  public SlaveServerConfig( SlaveServer slaveServer ) {
    this();
    this.slaveServer = slaveServer;
  }

  public SlaveServerConfig( List<SlaveServer> masters, boolean reportingToMasters, SlaveServer slaveServer ) {
    this.masters = masters;
    this.reportingToMasters = reportingToMasters;
    this.slaveServer = slaveServer;
  }

  public String getXML() {

    StringBuilder xml = new StringBuilder();

    xml.append( XMLHandler.openTag( XML_TAG ) );

    for ( SlaveServer slaveServer : masters ) {
      xml.append( slaveServer.getXML() );
    }

    XMLHandler.addTagValue( "report_to_masters", reportingToMasters );

    if ( slaveServer != null ) {
      xml.append( slaveServer.getXML() );
    }

    XMLHandler.addTagValue( "joining", joining );
    XMLHandler.addTagValue( "max_log_lines", maxLogLines );
    XMLHandler.addTagValue( "max_log_timeout_minutes", maxLogTimeoutMinutes );
    XMLHandler.addTagValue( "object_timeout_minutes", objectTimeoutMinutes );

    xml.append( XMLHandler.openTag( XML_TAG_SEQUENCES ) );
    for ( SlaveSequence slaveSequence : slaveSequences ) {
      xml.append( XMLHandler.openTag( SlaveSequence.XML_TAG ) );
      xml.append( slaveSequence.getXML() );
      xml.append( XMLHandler.closeTag( SlaveSequence.XML_TAG ) );
    }
    xml.append( XMLHandler.closeTag( XML_TAG_SEQUENCES ) );

    if ( autoSequence != null ) {
      xml.append( XMLHandler.openTag( XML_TAG_AUTOSEQUENCE ) );
      xml.append( autoSequence.getXML() );
      xml.append( XMLHandler.addTagValue( XML_TAG_AUTO_CREATE, automaticCreationAllowed ) );
      xml.append( XMLHandler.closeTag( XML_TAG_AUTOSEQUENCE ) );
    }

    if ( repositoryMeta != null ) {
      xml.append( XMLHandler.openTag( XML_TAG_REPOSITORY ) );
      xml.append( "  " ).append( XMLHandler.addTagValue( "id", repositoryMeta.getId() ) );
      xml.append( "  " ).append( XMLHandler.addTagValue( "username", repositoryUsername ) );
      xml.append( "  " ).append(
        XMLHandler.addTagValue( "password", Encr.encryptPasswordIfNotUsingVariables( repositoryPassword ) ) );
      xml.append( XMLHandler.closeTag( XML_TAG_REPOSITORY ) );
    }

    xml.append( XMLHandler.closeTag( XML_TAG ) );

    return xml.toString();
  }

  public SlaveServerConfig( LogChannelInterface log, Node node ) throws KettleXMLException {
    this();
    Node slaveNode = XMLHandler.getSubNode( node, SlaveServer.XML_TAG );
    if ( slaveNode != null ) {
      slaveServer = new SlaveServer( slaveNode );
      checkNetworkInterfaceSetting( log, slaveNode, slaveServer );
    }

    Node mastersNode = XMLHandler.getSubNode( node, XML_TAG_MASTERS );
    int nrMasters = XMLHandler.countNodes( mastersNode, SlaveServer.XML_TAG );
    for ( int i = 0; i < nrMasters; i++ ) {
      Node masterSlaveNode = XMLHandler.getSubNodeByNr( mastersNode, SlaveServer.XML_TAG, i );
      SlaveServer masterSlaveServer = new SlaveServer( masterSlaveNode );
      checkNetworkInterfaceSetting( log, masterSlaveNode, masterSlaveServer );
      masterSlaveServer.setSslMode( slaveServer.isSslMode() );
      masters.add( masterSlaveServer );
    }

    reportingToMasters = "Y".equalsIgnoreCase( XMLHandler.getTagValue( node, "report_to_masters" ) );

    joining = "Y".equalsIgnoreCase( XMLHandler.getTagValue( node, "joining" ) );
    maxLogLines = Const.toInt( XMLHandler.getTagValue( node, "max_log_lines" ), 0 );
    maxLogTimeoutMinutes = Const.toInt( XMLHandler.getTagValue( node, "max_log_timeout_minutes" ), 0 );
    objectTimeoutMinutes = Const.toInt( XMLHandler.getTagValue( node, "object_timeout_minutes" ), 0 );

    // Read sequence information
    //
    List<Node> dbNodes = XMLHandler.getNodes( node, DatabaseMeta.XML_TAG );
    for ( Node dbNode : dbNodes ) {
      databases.add( new DatabaseMeta( dbNode ) );
    }

    Node sequencesNode = XMLHandler.getSubNode( node, "sequences" );
    List<Node> seqNodes = XMLHandler.getNodes( sequencesNode, SlaveSequence.XML_TAG );
    for ( Node seqNode : seqNodes ) {
      slaveSequences.add( new SlaveSequence( seqNode, databases ) );
    }

    Node autoSequenceNode = XMLHandler.getSubNode( node, XML_TAG_AUTOSEQUENCE );
    if ( autoSequenceNode != null ) {
      autoSequence = new SlaveSequence( autoSequenceNode, databases );
      automaticCreationAllowed =
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( autoSequenceNode, XML_TAG_AUTO_CREATE ) );
    }

    // Set Jetty Options
    setUpJettyOptions( node );

    Node repositoryNode = XMLHandler.getSubNode( node, XML_TAG_REPOSITORY );
    repositoryId = XMLHandler.getTagValue( repositoryNode, "name" );
    repositoryUsername = XMLHandler.getTagValue( repositoryNode, "username" );
    repositoryPassword = XMLHandler.getTagValue( repositoryNode, "password" );
  }

  /** Set up jetty options to the system properties
   * @param node
   */
  protected void setUpJettyOptions( Node node ) {
    Map<String, String> jettyOptions = parseJettyOptions( node );

    if ( jettyOptions != null && jettyOptions.size() > 0 ) {
      for ( Entry<String, String> jettyOption : jettyOptions.entrySet() ) {
        System.setProperty( jettyOption.getKey(), jettyOption.getValue() );
      }
    }
  }

  /**
   * Read and parse jetty options
   *
   * @param node
   *          that contains jetty options nodes
   * @return map of not empty jetty options
   */
  protected Map<String, String> parseJettyOptions( Node node ) {

    Map<String, String> jettyOptions = null;

    Node jettyOptionsNode = XMLHandler.getSubNode( node, XML_TAG_JETTY_OPTIONS );

    if ( jettyOptionsNode != null ) {

      jettyOptions = new HashMap<String, String>();
      if ( XMLHandler.getTagValue( jettyOptionsNode, XML_TAG_ACCEPTORS ) != null ) {
        jettyOptions.put( Const.KETTLE_CARTE_JETTY_ACCEPTORS, XMLHandler.getTagValue( jettyOptionsNode, XML_TAG_ACCEPTORS ) );
      }
      if ( XMLHandler.getTagValue( jettyOptionsNode, XML_TAG_ACCEPT_QUEUE_SIZE ) != null ) {
        jettyOptions.put( Const.KETTLE_CARTE_JETTY_ACCEPT_QUEUE_SIZE, XMLHandler.getTagValue( jettyOptionsNode,
            XML_TAG_ACCEPT_QUEUE_SIZE ) );
      }
      if ( XMLHandler.getTagValue( jettyOptionsNode, XML_TAG_LOW_RES_MAX_IDLE_TIME ) != null ) {
        jettyOptions.put( Const.KETTLE_CARTE_JETTY_RES_MAX_IDLE_TIME, XMLHandler.getTagValue( jettyOptionsNode,
            XML_TAG_LOW_RES_MAX_IDLE_TIME ) );
      }
    }
    return jettyOptions;
  }

  private void openRepository( String repositoryId ) throws KettleException {
    try {

      RepositoriesMeta repositoriesMeta = new RepositoriesMeta();
      repositoriesMeta.readData();
      repositoryMeta = repositoriesMeta.findRepository( repositoryId );
      if ( repositoryMeta == null ) {
        throw new KettleException( "Unable to find repository: " + repositoryId );
      }
      PluginRegistry registry = PluginRegistry.getInstance();
      repository = registry.loadClass( RepositoryPluginType.class, repositoryMeta, Repository.class );
      repository.init( repositoryMeta );
      repository.connect( repositoryUsername, repositoryPassword );

      LogChannel.GENERAL.logBasic( "Connected to repository '" + repository.getName() + "'" );

    } catch ( Exception e ) {
      throw new KettleException( "Unable to open repository connection", e );
    }
  }

  public void readAutoSequences() throws KettleException {
    if ( autoSequence == null ) {
      return;
    }

    Database database = null;

    try {
      DatabaseMeta databaseMeta = autoSequence.getDatabaseMeta();
      LoggingObjectInterface loggingInterface =
        new SimpleLoggingObject( "auto-sequence", LoggingObjectType.GENERAL, null );
      database = new Database( loggingInterface, databaseMeta );
      database.connect();
      String schemaTable =
        databaseMeta.getQuotedSchemaTableCombination( autoSequence.getSchemaName(), autoSequence.getTableName() );
      String seqField = databaseMeta.quoteField( autoSequence.getSequenceNameField() );
      String valueField = databaseMeta.quoteField( autoSequence.getValueField() );

      String sql = "SELECT " + seqField + ", " + valueField + " FROM " + schemaTable;
      List<Object[]> rows = database.getRows( sql, 0 );
      RowMetaInterface rowMeta = database.getReturnRowMeta();
      for ( Object[] row : rows ) {
        // Automatically create a new sequence for each sequence found...
        //
        String sequenceName = rowMeta.getString( row, seqField, null );
        if ( !Utils.isEmpty( sequenceName ) ) {
          Long value = rowMeta.getInteger( row, valueField, null );
          if ( value != null ) {
            SlaveSequence slaveSequence =
              new SlaveSequence( sequenceName, value, databaseMeta, autoSequence.getSchemaName(), autoSequence
                .getTableName(), autoSequence.getSequenceNameField(), autoSequence.getValueField() );

            slaveSequences.add( slaveSequence );

            LogChannel.GENERAL.logBasic( "Automatically created slave sequence '"
              + slaveSequence.getName() + "' with start value " + slaveSequence.getStartValue() );
          }
        }
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unable to automatically configure slave sequences", e );
    } finally {
      if ( database != null ) {
        database.disconnect();
      }
    }
  }

  private void checkNetworkInterfaceSetting( LogChannelInterface log, Node slaveNode, SlaveServer slaveServer ) {
    // See if we need to grab the network interface to use and then override the host name
    //
    String networkInterfaceName = XMLHandler.getTagValue( slaveNode, "network_interface" );
    if ( !Utils.isEmpty( networkInterfaceName ) ) {
      // OK, so let's try to get the IP address for this network interface...
      //
      try {
        String newHostname = Const.getIPAddress( networkInterfaceName );
        if ( newHostname != null ) {
          slaveServer.setHostname( newHostname );
          // Also change the name of the slave...
          //
          slaveServer.setName( slaveServer.getName() + "-" + newHostname );
          log.logBasic( "Hostname for slave server ["
            + slaveServer.getName() + "] is set to [" + newHostname + "], information derived from network "
            + networkInterfaceName );
        }
      } catch ( SocketException e ) {
        log.logError( "Unable to get the IP address for network interface "
          + networkInterfaceName + " for slave server [" + slaveServer.getName() + "]", e );
      }
    }

  }

  public SlaveServerConfig( String hostname, int port, boolean joining ) {
    this();
    this.joining = joining;
    this.slaveServer = new SlaveServer( hostname + ":" + port, hostname, "" + port, null, null );
  }

  /**
   * @return the list of masters to report back to if the report to masters flag is enabled.
   */
  public List<SlaveServer> getMasters() {
    return masters;
  }

  /**
   * @param masters
   *          the list of masters to set. It is the list of masters to report back to if the report to masters flag is
   *          enabled.
   */
  public void setMasters( List<SlaveServer> masters ) {
    this.masters = masters;
  }

  /**
   * @return the slave server.<br>
   *         The user name and password defined in here are used to contact this slave by the masters.
   */
  public SlaveServer getSlaveServer() {
    return slaveServer;
  }

  /**
   * @param slaveServer
   *          the slave server details to set.<br>
   *          The user name and password defined in here are used to contact this slave by the masters.
   */
  public void setSlaveServer( SlaveServer slaveServer ) {
    this.slaveServer = slaveServer;
  }

  /**
   * @return true if this slave reports to the masters
   */
  public boolean isReportingToMasters() {
    return reportingToMasters;
  }

  /**
   * @param reportingToMaster
   *          set to true if this slave should report to the masters
   */
  public void setReportingToMasters( boolean reportingToMaster ) {
    this.reportingToMasters = reportingToMaster;
  }

  /**
   * @return true if the webserver needs to join with the webserver threads (wait/block until finished)
   */
  public boolean isJoining() {
    return joining;
  }

  /**
   * @param joining
   *          Set to true if the webserver needs to join with the webserver threads (wait/block until finished)
   */
  public void setJoining( boolean joining ) {
    this.joining = joining;
  }

  /**
   * @return the maxLogLines
   */
  public int getMaxLogLines() {
    return maxLogLines;
  }

  /**
   * @param maxLogLines
   *          the maxLogLines to set
   */
  public void setMaxLogLines( int maxLogLines ) {
    this.maxLogLines = maxLogLines;
  }

  /**
   * @return the maxLogTimeoutMinutes
   */
  public int getMaxLogTimeoutMinutes() {
    return maxLogTimeoutMinutes;
  }

  /**
   * @param maxLogTimeoutMinutes
   *          the maxLogTimeoutMinutes to set
   */
  public void setMaxLogTimeoutMinutes( int maxLogTimeoutMinutes ) {
    this.maxLogTimeoutMinutes = maxLogTimeoutMinutes;
  }

  /**
   * @return the objectTimeoutMinutes
   */
  public int getObjectTimeoutMinutes() {
    return objectTimeoutMinutes;
  }

  /**
   * @param objectTimeoutMinutes
   *          the objectTimeoutMinutes to set
   */
  public void setObjectTimeoutMinutes( int objectTimeoutMinutes ) {
    this.objectTimeoutMinutes = objectTimeoutMinutes;
  }

  /**
   * @return the filename
   */
  public String getFilename() {
    return filename;
  }

  /**
   * @param filename
   *          the filename to set
   */
  public void setFilename( String filename ) {
    this.filename = filename;
  }

  /**
   * @return the databases
   */
  public List<DatabaseMeta> getDatabases() {
    return databases;
  }

  /**
   * @param databases
   *          the databases to set
   */
  public void setDatabases( List<DatabaseMeta> databases ) {
    this.databases = databases;
  }

  /**
   * @return the slaveSequences
   */
  public List<SlaveSequence> getSlaveSequences() {
    return slaveSequences;
  }

  /**
   * @param slaveSequences
   *          the slaveSequences to set
   */
  public void setSlaveSequences( List<SlaveSequence> slaveSequences ) {
    this.slaveSequences = slaveSequences;
  }

  /**
   * @return the autoSequence
   */
  public SlaveSequence getAutoSequence() {
    return autoSequence;
  }

  /**
   * @param autoSequence
   *          the autoSequence to set
   */
  public void setAutoSequence( SlaveSequence autoSequence ) {
    this.autoSequence = autoSequence;
  }

  /**
   * @return the automaticCreationAllowed
   */
  public boolean isAutomaticCreationAllowed() {
    return automaticCreationAllowed;
  }

  /**
   * @param automaticCreationAllowed
   *          the automaticCreationAllowed to set
   */
  public void setAutomaticCreationAllowed( boolean automaticCreationAllowed ) {
    this.automaticCreationAllowed = automaticCreationAllowed;
  }

  /**
   * @return the repository, loaded lazily
   */
  public Repository getRepository() throws KettleException {

    if ( !Utils.isEmpty( repositoryId ) && repository == null ) {
      openRepository( repositoryId );
    }

    return repository;
  }

  /**
   * @param repository
   *          the repository to set
   */
  public void setRepository( Repository repository ) {
    this.repository = repository;
  }

  /**
   * @return the repositoryUsername
   */
  public String getRepositoryUsername() {
    return repositoryUsername;
  }

  /**
   * @param repositoryUsername
   *          the repositoryUsername to set
   */
  public void setRepositoryUsername( String repositoryUsername ) {
    this.repositoryUsername = repositoryUsername;
  }

  /**
   * @return the repositoryPassword
   */
  public String getRepositoryPassword() {
    return repositoryPassword;
  }

  /**
   * @param repositoryPassword
   *          the repositoryPassword to set
   */
  public void setRepositoryPassword( String repositoryPassword ) {
    this.repositoryPassword = repositoryPassword;
  }

  public IMetaStore getMetaStore() {
    return metaStoreSupplier == null ? null : metaStoreSupplier.get();
  }

  public Supplier<IMetaStore> getMetastoreSupplier() {
    return metaStoreSupplier;
  }

  /**
   * Should generally be used only for tests.
   *
   *
   * @param metastoreSupplier
   */
  public void setMetastoreSupplier( Supplier<IMetaStore> metastoreSupplier ) {
    this.metaStoreSupplier = metastoreSupplier;
  }

  public String getPasswordFile() {
    return passwordFile;
  }

  public void setPasswordFile( String passwordFile ) {
    this.passwordFile = passwordFile;
  }

  public String getRepositoryId() {
    return repositoryId;
  }

  public void setRepositoryId( String repositoryId ) {
    this.repositoryId = repositoryId;
  }

}
