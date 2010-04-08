/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.www;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;

public class CarteSingleton {

  private static Class<?> PKG = Carte.class; // for i18n purposes, needed by
  // Translator2!! $NON-NLS-1$
  private static CarteSingleton carte;

  private LogChannelInterface log;
  private TransformationMap transformationMap;
  private JobMap jobMap;
  private List<SlaveServerDetection> detections;
  private SocketRepository socketRepository;

  private CarteSingleton(SlaveServerConfig config) throws KettleException {
    KettleEnvironment.init();
    CentralLogStore.init(config.getMaxLogLines(), config.getMaxLogTimeoutMinutes());

    this.log = new LogChannel("Carte");
    transformationMap = new TransformationMap();
    jobMap = new JobMap();
    detections = new ArrayList<SlaveServerDetection>();
    socketRepository = new SocketRepository(log);

    SlaveServer slaveServer = config.getSlaveServer();
    String hostname = slaveServer.getHostname();
    int port = WebServer.PORT;
    if (!Const.isEmpty(slaveServer.getPort())) {
      try {
        port = Integer.parseInt(slaveServer.getPort());
      } catch (Exception e) {
        log.logError(BaseMessages.getString(PKG, "Carte.Error.CanNotPartPort", slaveServer.getHostname(), "" + port), e);
      }
    }

    // TODO: see if we need to keep doing this on a periodic basis.
    // The master might be dead or not alive yet at the time we send this
    // message.
    // Repeating the registration over and over every few minutes might
    // harden this sort of problems.
    //
    if (config.isReportingToMasters()) {
      final SlaveServer client = new SlaveServer("Dynamic slave [" + hostname + ":" + port + "]", hostname, "" + port, slaveServer.getUsername(), slaveServer
          .getPassword());
      for (final SlaveServer master : config.getMasters()) {
        // Here we use the username/password specified in the slave
        // server section of the configuration.
        // This doesn't have to be the same pair as the one used on the
        // master!
        //
        try {
          SlaveServerDetection slaveServerDetection = new SlaveServerDetection(client);
          master.sendXML(slaveServerDetection.getXML(), RegisterSlaveServlet.CONTEXT_PATH + "/");
          log.logBasic("Registered this slave server to master slave server [" + master.toString() + "] on address [" + master.getServerAndPort() + "]");
        } catch (Exception e) {
          log.logError("Unable to register to master slave server [" + master.toString() + "] on address [" + master.getServerAndPort() + "]");
        }
      }
    }
  }

  public static CarteSingleton getInstance() {
    try {
      if (carte == null) {
        String hostname = "localhost";
        String port = "8881";
        SlaveServer slaveServer = new SlaveServer(hostname + ":" + port, hostname, port, null, null);
        SlaveServerConfig config = new SlaveServerConfig();
        config.setSlaveServer(slaveServer);
        carte = new CarteSingleton(config);
        
        Trans trans = Carte.generateTestTransformation();
        carte.getTransformationMap().addTransformation(trans.getName(), trans, new TransConfiguration(trans.getTransMeta(), new TransExecutionConfiguration()));
        
        return carte;
      } else {
        return carte;
      }
    } catch (KettleException ke) {
      throw new RuntimeException(ke);
    }
  }

  public TransformationMap getTransformationMap() {
    return transformationMap;
  }

  public void setTransformationMap(TransformationMap transformationMap) {
    this.transformationMap = transformationMap;
  }

  public JobMap getJobMap() {
    return jobMap;
  }

  public void setJobMap(JobMap jobMap) {
    this.jobMap = jobMap;
  }

  public List<SlaveServerDetection> getDetections() {
    return detections;
  }

  public void setDetections(List<SlaveServerDetection> detections) {
    this.detections = detections;
  }

  public SocketRepository getSocketRepository() {
    return socketRepository;
  }

  public void setSocketRepository(SocketRepository socketRepository) {
    this.socketRepository = socketRepository;
  }

}
