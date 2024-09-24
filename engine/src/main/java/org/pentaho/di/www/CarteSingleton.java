/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.LoggingRegistry;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.trans.Trans;

public class CarteSingleton {

  private static Class<?> PKG = Carte.class; // for i18n purposes, needed by Translator2!!

  private static SlaveServerConfig slaveServerConfig;
  private static CarteSingleton carteSingleton;
  private static Carte carte;

  private LogChannelInterface log;

  private TransformationMap transformationMap;
  private JobMap jobMap;
  private List<SlaveServerDetection> detections;
  private SocketRepository socketRepository;

  private CarteSingleton( SlaveServerConfig config ) throws KettleException {
    KettleEnvironment.init();
    KettleLogStore.init( config.getMaxLogLines(), config.getMaxLogTimeoutMinutes() );

    this.log = new LogChannel( "Carte" );
    transformationMap = new TransformationMap();
    transformationMap.setSlaveServerConfig( config );
    jobMap = new JobMap();
    jobMap.setSlaveServerConfig( config );
    detections = new ArrayList<SlaveServerDetection>();
    socketRepository = new SocketRepository( log );

    installPurgeTimer( config, log, transformationMap, jobMap );

    SlaveServer slaveServer = config.getSlaveServer();
    if ( slaveServer != null ) {
      int port = WebServer.PORT;
      if ( !Utils.isEmpty( slaveServer.getPort() ) ) {
        try {
          port = Integer.parseInt( slaveServer.getPort() );
        } catch ( Exception e ) {
          log.logError( BaseMessages.getString( PKG, "Carte.Error.CanNotPartPort", slaveServer.getHostname(), ""
            + port ), e );
        }
      }

      // TODO: see if we need to keep doing this on a periodic basis.
      // The master might be dead or not alive yet at the time we send this
      // message.
      // Repeating the registration over and over every few minutes might
      // harden this sort of problems.
      //
      if ( config.isReportingToMasters() ) {
        String hostname = slaveServer.getHostname();
        final SlaveServer client =
          new SlaveServer( "Dynamic slave [" + hostname + ":" + port + "]", hostname, "" + port, slaveServer
            .getUsername(), slaveServer.getPassword() );
        for ( final SlaveServer master : config.getMasters() ) {
          // Here we use the username/password specified in the slave
          // server section of the configuration.
          // This doesn't have to be the same pair as the one used on the
          // master!
          //
          try {
            SlaveServerDetection slaveServerDetection = new SlaveServerDetection( client );
            master.sendXML( slaveServerDetection.getXML(), RegisterSlaveServlet.CONTEXT_PATH + "/" );
            log.logBasic( "Registered this slave server to master slave server ["
              + master.toString() + "] on address [" + master.getServerAndPort() + "]" );
          } catch ( Exception e ) {
            log.logError( "Unable to register to master slave server ["
              + master.toString() + "] on address [" + master.getServerAndPort() + "]" );
          }
        }
      }
    }
  }

  public static void installPurgeTimer( final SlaveServerConfig config, final LogChannelInterface log,
    final TransformationMap transformationMap, final JobMap jobMap ) {

    final int objectTimeout;
    String systemTimeout = EnvUtil.getSystemProperty( Const.KETTLE_CARTE_OBJECT_TIMEOUT_MINUTES, null );

    // The value specified in XML takes precedence over the environment variable!
    //
    if ( config.getObjectTimeoutMinutes() > 0 ) {
      objectTimeout = config.getObjectTimeoutMinutes();
    } else if ( !Utils.isEmpty( systemTimeout ) ) {
      objectTimeout = Const.toInt( systemTimeout, 1440 );
    } else {
      objectTimeout = 24 * 60; // 1440 : default is a one day time-out
    }

    // If we need to time out finished or idle objects, we should create a timer
    // in the background to clean
    //
    if ( objectTimeout > 0 ) {

      log.logBasic( "Installing timer to purge stale objects after " + objectTimeout + " minutes." );

      Timer timer = new Timer( "CartePurgeTimer", true );

      final AtomicBoolean busy = new AtomicBoolean( false );
      TimerTask timerTask = new TimerTask() {
        public void run() {
          if ( busy.compareAndSet( false, true ) ) {

            try {
              // Check all transformations...
              //
              for ( CarteObjectEntry entry : transformationMap.getTransformationObjects() ) {
                Trans trans = transformationMap.getTransformation( entry );

                // See if the transformation is finished or stopped.
                //
                if ( trans != null && ( trans.isFinished() || trans.isStopped() ) && trans.getLogDate() != null ) {
                  // check the last log time
                  //
                  int diffInMinutes =
                    (int) Math.floor( ( System.currentTimeMillis() - trans.getLogDate().getTime() ) / 60000 );
                  if ( diffInMinutes >= objectTimeout ) {

                    // Let's remove this from the transformation map...
                    transformationMap.removeTransformation( entry );

                    // Remove the logging information from the log registry & central log store
                    KettleLogStore.discardLines( trans.getLogChannelId(), false );

                    log.logMinimal( "Cleaned up transformation "
                      + entry.getName() + " with id " + entry.getId() + " from " + trans.getLogDate()
                      + ", diff=" + diffInMinutes );
                  }
                }
              }

              // And the jobs...
              //
              for ( CarteObjectEntry entry : jobMap.getJobObjects() ) {
                Job job = jobMap.getJob( entry );

                // See if the job is finished or stopped.
                //
                if ( job != null && ( job.isFinished() || job.isStopped() ) && job.getLogDate() != null ) {
                  // check the last log time
                  //
                  int diffInMinutes =
                    (int) Math.floor( ( System.currentTimeMillis() - job.getLogDate().getTime() ) / 60000 );
                  if ( diffInMinutes >= objectTimeout ) {
                    // Let's remove this from the job map...
                    //
                    String id = jobMap.getJob( entry ).getLogChannelId();
                    LoggingRegistry.getInstance().removeLogChannelFileWriterBuffer( id );

                    jobMap.removeJob( entry );

                    // Remove the logging information from the log registry & central log store
                    KettleLogStore.discardLines( job.getLogChannelId(), false );


                    log.logMinimal( "Cleaned up job "
                      + entry.getName() + " with id " + entry.getId() + " from " + job.getLogDate() );
                  }
                }
              }

            } finally {
              busy.set( false );
            }
          }
        }
      };

      // Search for stale objects every 20 seconds:
      //
      timer.schedule( timerTask, 20000, 20000 );
    }
  }

  public static CarteSingleton getInstance() {
    try {
      if ( carteSingleton == null ) {
        if ( slaveServerConfig == null ) {
          slaveServerConfig = new SlaveServerConfig();
          SlaveServer slaveServer = new SlaveServer();
          slaveServerConfig.setSlaveServer( slaveServer );
        }

        carteSingleton = new CarteSingleton( slaveServerConfig );

        String carteObjectId = UUID.randomUUID().toString();
        SimpleLoggingObject servletLoggingObject =
          new SimpleLoggingObject( "CarteSingleton", LoggingObjectType.CARTE, null );
        servletLoggingObject.setContainerObjectId( carteObjectId );
        servletLoggingObject.setLogLevel( LogLevel.BASIC );

        return carteSingleton;
      } else {
        return carteSingleton;
      }
    } catch ( KettleException ke ) {
      throw new RuntimeException( ke );
    }
  }

  public TransformationMap getTransformationMap() {
    return transformationMap;
  }

  public void setTransformationMap( TransformationMap transformationMap ) {
    this.transformationMap = transformationMap;
  }

  public JobMap getJobMap() {
    return jobMap;
  }

  public void setJobMap( JobMap jobMap ) {
    this.jobMap = jobMap;
  }

  public List<SlaveServerDetection> getDetections() {
    return detections;
  }

  public void setDetections( List<SlaveServerDetection> detections ) {
    this.detections = detections;
  }

  public SocketRepository getSocketRepository() {
    return socketRepository;
  }

  public void setSocketRepository( SocketRepository socketRepository ) {
    this.socketRepository = socketRepository;
  }

  public static SlaveServerConfig getSlaveServerConfig() {
    return slaveServerConfig;
  }

  public static void setSlaveServerConfig( SlaveServerConfig slaveServerConfig ) {
    CarteSingleton.slaveServerConfig = slaveServerConfig;
  }

  public static void setCarte( Carte carte ) {
    CarteSingleton.carte = carte;
  }

  public static Carte getCarte() {
    return CarteSingleton.carte;
  }

  public LogChannelInterface getLog() {
    return log;
  }
}
