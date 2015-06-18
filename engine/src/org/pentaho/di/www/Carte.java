/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs.FileObject;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class Carte {
  private static Class<?> PKG = Carte.class; // for i18n purposes, needed by Translator2!!

  private WebServer webServer;
  private SlaveServerConfig config;
  private boolean allOK;

  public Carte( final SlaveServerConfig config ) throws Exception {
    this( config, null );
  }

  public Carte( final SlaveServerConfig config, Boolean joinOverride ) throws Exception {
    this.config = config;

    allOK = true;

    CarteSingleton.setSlaveServerConfig( config );
    LogChannelInterface log = CarteSingleton.getInstance().getLog();

    final TransformationMap transformationMap = CarteSingleton.getInstance().getTransformationMap();
    transformationMap.setSlaveServerConfig( config );
    final JobMap jobMap = CarteSingleton.getInstance().getJobMap();
    jobMap.setSlaveServerConfig( config );
    List<SlaveServerDetection> detections = Collections.synchronizedList( new ArrayList<SlaveServerDetection>() );
    SocketRepository socketRepository = CarteSingleton.getInstance().getSocketRepository();

    SlaveServer slaveServer = config.getSlaveServer();

    String hostname = slaveServer.getHostname();
    int port = WebServer.PORT;
    if ( !Const.isEmpty( slaveServer.getPort() ) ) {
      try {
        port = Integer.parseInt( slaveServer.getPort() );
      } catch ( Exception e ) {
        log.logError( BaseMessages.getString( PKG, "Carte.Error.CanNotPartPort", slaveServer.getHostname(), ""
          + port ), e );
        allOK = false;
      }
    }

    // TODO: see if we need to keep doing this on a periodic basis.
    // The master might be dead or not alive yet at the time we send this message.
    // Repeating the registration over and over every few minutes might harden this sort of problems.
    //
    Properties masterProperties = null;
    if ( config.isReportingToMasters() ) {
      String propertiesMaster = slaveServer.getPropertiesMasterName();
      for ( final SlaveServer master : config.getMasters() ) {
        // Here we use the username/password specified in the slave server section of the configuration.
        // This doesn't have to be the same pair as the one used on the master!
        //
        try {
          SlaveServerDetection slaveServerDetection = new SlaveServerDetection( slaveServer.getClient() );
          master.sendXML( slaveServerDetection.getXML(), RegisterSlaveServlet.CONTEXT_PATH + "/" );
          log.logBasic( "Registered this slave server to master slave server [" + master.toString() + "] on address ["
              + master.getServerAndPort() + "]" );
        } catch ( Exception e ) {
          log.logError( "Unable to register to master slave server [" + master.toString() + "] on address ["
              + master.getServerAndPort() + "]" );
          allOK = false;
        }
        try {
          if ( !StringUtils.isBlank( propertiesMaster ) && propertiesMaster.equalsIgnoreCase( master.getName() ) ) {
            if ( masterProperties != null ) {
              log.logError( "More than one primary master server. Master name is " + propertiesMaster );
            } else {
              masterProperties = master.getKettleProperties();
              log.logBasic( "Got properties from master server [" + master.toString() + "], address ["
                  + master.getServerAndPort() + "]" );
            }
          }
        } catch ( Exception e ) {
          log.logError( "Unable to get properties from master server [" + master.toString() + "], address ["
              + master.getServerAndPort() + "]" );
          allOK = false;
        }
      }
    }
    if ( masterProperties != null ) {
      EnvUtil.applyKettleProperties( masterProperties, slaveServer.isOverrideExistingProperties() );
    }

    // If we need to time out finished or idle objects, we should create a timer in the background to clean
    // this is done automatically now
    // CarteSingleton.installPurgeTimer(config, log, transformationMap, jobMap);

    if ( allOK ) {
      boolean shouldJoin = config.isJoining();
      if ( joinOverride != null ) {
        shouldJoin = joinOverride;
      }

      this.webServer =
          new WebServer( log, transformationMap, jobMap, socketRepository, detections, hostname, port, shouldJoin,
              config.getPasswordFile(), slaveServer.getSslConfig() );
    }
  }

  public static void main( String[] args ) throws Exception {
    KettleEnvironment.init();
    KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.CARTE );

    // Load from an xml file that describes the complete configuration...
    //
    SlaveServerConfig config = null;
    if ( args.length == 1 && !Const.isEmpty( args[0] ) ) {
      FileObject file = KettleVFS.getFileObject( args[0] );
      Document document = XMLHandler.loadXMLFile( file );
      Node configNode = XMLHandler.getSubNode( document, SlaveServerConfig.XML_TAG );
      config = new SlaveServerConfig( new LogChannel( "Slave server config" ), configNode );
      if ( config.getAutoSequence() != null ) {
        config.readAutoSequences();
      }
      config.setFilename( args[0] );
    }
    if ( args.length == 2 && !Const.isEmpty( args[0] ) && !Const.isEmpty( args[1] ) ) {
      String hostname = args[0];
      String port = args[1];
      SlaveServer slaveServer = new SlaveServer( hostname + ":" + port, hostname, port, null, null );

      config = new SlaveServerConfig();
      config.setSlaveServer( slaveServer );
    }

    // Nothing configured: show the usage
    //
    if ( config == null ) {
      System.err.println( BaseMessages.getString( PKG, "Carte.Usage.Text" ) );
      System.err.println();

      System.err.println( BaseMessages.getString( PKG, "Carte.Usage.Example" ) + ": Carte 127.0.0.1 8080" );
      System.err.println( BaseMessages.getString( PKG, "Carte.Usage.Example" ) + ": Carte 192.168.1.221 8081" );
      System.err.println();
      System.err.println( BaseMessages.getString( PKG, "Carte.Usage.Example" )
        + ": Carte /foo/bar/carte-config.xml" );
      System.err.println( BaseMessages.getString( PKG, "Carte.Usage.Example" )
        + ": Carte http://www.example.com/carte-config.xml" );

      System.exit( 1 );
    }

    runCarte( config );
  }

  public static void runCarte( SlaveServerConfig config ) throws Exception {
    KettleLogStore.init( config.getMaxLogLines(), config.getMaxLogTimeoutMinutes() );

    config.setJoining( true );

    Carte carte = new Carte( config, false );
    CarteSingleton.setCarte( carte );

    carte.getWebServer().join();
  }

  public static Trans generateTestTransformation() {
    RowGeneratorMeta A = new RowGeneratorMeta();
    A.allocate( 3 );
    A.setRowLimit( "100000000" );

    ( A.getFieldName() )[0] = "ID";
    ( A.getFieldType() )[0] = ValueMeta.getTypeDesc( ValueMetaInterface.TYPE_INTEGER );
    ( A.getFieldLength() )[0] = 7;
    ( A.getValue() )[0] = "1234";

    ( A.getFieldName() )[1] = "Name";
    ( A.getFieldType() )[1] = ValueMeta.getTypeDesc( ValueMetaInterface.TYPE_STRING );
    ( A.getFieldLength() )[1] = 35;
    ( A.getValue() )[1] = "Some name";

    ( A.getFieldName() )[2] = "Last updated";
    ( A.getFieldType() )[2] = ValueMeta.getTypeDesc( ValueMetaInterface.TYPE_DATE );
    ( A.getFieldFormat() )[2] = "yyyy/MM/dd";
    ( A.getValue() )[2] = "2006/11/13";

    TransMeta transMeta = TransPreviewFactory.generatePreviewTransformation( null, A, "A" );
    transMeta.setName( "Row generator test" );
    transMeta.setSizeRowset( 2500 );
    transMeta.setFeedbackSize( 50000 );
    transMeta.setUsingThreadPriorityManagment( false );

    return new Trans( transMeta );
  }

  /**
   * @return the webServer
   */
  public WebServer getWebServer() {
    return webServer;
  }

  /**
   * @param webServer
   *          the webServer to set
   */
  public void setWebServer( WebServer webServer ) {
    this.webServer = webServer;
  }

  /**
   * @return the slave server (Carte) configuration
   */
  public SlaveServerConfig getConfig() {
    return config;
  }

  /**
   * @param config
   *          the slave server (Carte) configuration
   */
  public void setConfig( SlaveServerConfig config ) {
    this.config = config;
  }
}
