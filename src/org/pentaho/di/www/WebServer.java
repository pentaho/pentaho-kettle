/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
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

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.plus.jaas.JAASUserRealm;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;



public class WebServer
{
	private static Class<?> PKG = WebServer.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private LogChannelInterface log;
    
    public  static final int PORT = 80;

    private Server             server;
    
    private TransformationMap  transformationMap;
	private JobMap             jobMap;
	private List<SlaveServerDetection>  detections;
	private SocketRepository   socketRepository;
	
    private String hostname;
    private int port;

    private Timer slaveMonitoringTimer;

    public WebServer(LogChannelInterface log, TransformationMap transformationMap, JobMap jobMap, SocketRepository socketRepository, List<SlaveServerDetection> detections, String hostname, int port, boolean join) throws Exception
    {
    	this.log = log;
        this.transformationMap = transformationMap;
        this.jobMap = jobMap;
        this.socketRepository = socketRepository;
        this.detections = detections;
        this.hostname = hostname;
        this.port = port;

        startServer();
        
        // Start the monitoring of the registered slave servers...
        //
        startSlaveMonitoring();
        
        if (join) {
            server.join();
        }
    }

	public WebServer(LogChannelInterface log, TransformationMap transformationMap, JobMap jobMap, SocketRepository socketRepository, List<SlaveServerDetection> slaveServers, String hostname, int port) throws Exception
    {
      this(log, transformationMap, jobMap, socketRepository, slaveServers, hostname, port, true);
    }

    public Server getServer()
    {
        return server;
    }

    public void startServer() throws Exception
    {
        server = new Server();

        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__BASIC_AUTH);;
        constraint.setRoles( new String[] { Constraint.ANY_ROLE } );
        constraint.setAuthenticate(true);
        
        ConstraintMapping constraintMapping = new ConstraintMapping();
        constraintMapping.setConstraint(constraint);
        constraintMapping.setPathSpec("/*");

        // Set up the security handler, optionally with JAAS
        //
        SecurityHandler securityHandler = new SecurityHandler();
        
        if(System.getProperty("loginmodulename") != null && System.getProperty("java.security.auth.login.config") != null){
        	JAASUserRealm jaasRealm = new JAASUserRealm("Kettle");
        	jaasRealm.setLoginModuleName(System.getProperty("loginmodulename"));
        	securityHandler.setUserRealm(jaasRealm);
        } else {
        	// See if there is a kettle.pwd file in the KETTLE_HOME directory:
        	//
        	File homePwdFile = new File(Const.getKettleCartePasswordFile());
        	if (homePwdFile.exists()) {
        		securityHandler.setUserRealm(new HashUserRealm("Kettle", Const.getKettleCartePasswordFile()));
        	} else {
        		securityHandler.setUserRealm(new HashUserRealm("Kettle", Const.getKettleLocalCartePasswordFile()));
        	}
        }
        
        securityHandler.setConstraintMappings(new ConstraintMapping[]{constraintMapping});
               
        // Add all the servlets...
        //
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        
        // Root
        //
        Context root = new Context(contexts, GetRootServlet.CONTEXT_PATH, Context.SESSIONS);
        GetRootServlet rootServlet = new GetRootServlet();
        rootServlet.setJettyMode(true);
        root.addServlet(new ServletHolder(rootServlet), "/*");
        
        // Carte Status
        //
        Context status = new Context(contexts, GetStatusServlet.CONTEXT_PATH, Context.SESSIONS);
        status.addServlet(new ServletHolder(new GetStatusServlet(transformationMap, jobMap)), "/*");

        // Trans status
        //
        Context transStatus = new Context(contexts, GetTransStatusServlet.CONTEXT_PATH, Context.SESSIONS);
        transStatus.addServlet(new ServletHolder(new GetTransStatusServlet(transformationMap)), "/*");

        // Prepare execution
        //
        Context prepareExecution = new Context(contexts, PrepareExecutionTransServlet.CONTEXT_PATH, Context.SESSIONS);
        prepareExecution.addServlet(new ServletHolder(new PrepareExecutionTransServlet(transformationMap)), "/*");
        
        // Start execution
        //
        Context startExecution = new Context(contexts, StartExecutionTransServlet.CONTEXT_PATH, Context.SESSIONS);
        startExecution.addServlet(new ServletHolder(new StartExecutionTransServlet(transformationMap)), "/*");

        // Start transformation
        //
        Context startTrans = new Context(contexts, StartTransServlet.CONTEXT_PATH, Context.SESSIONS);
        startTrans.addServlet(new ServletHolder(new StartTransServlet(transformationMap)), "/*");

        // Pause/Resume transformation
        //
        Context pauseTrans = new Context(contexts, PauseTransServlet.CONTEXT_PATH, Context.SESSIONS);
        pauseTrans.addServlet(new ServletHolder(new PauseTransServlet(transformationMap)), "/*");

        // Stop transformation
        //
        Context stopTrans = new Context(contexts, StopTransServlet.CONTEXT_PATH, Context.SESSIONS);
        stopTrans.addServlet(new ServletHolder(new StopTransServlet(transformationMap)), "/*");

        // Transformation cleanup
        //
        Context cleanupTrans = new Context(contexts, CleanupTransServlet.CONTEXT_PATH, Context.SESSIONS);
        cleanupTrans.addServlet(new ServletHolder(new CleanupTransServlet(transformationMap)), "/*");

        // Add transformation
        //
        Context addTrans = new Context(contexts, AddTransServlet.CONTEXT_PATH, Context.SESSIONS);
        addTrans.addServlet(new ServletHolder(new AddTransServlet(transformationMap, socketRepository)), "/*");

        // Remove Transformation
        //
        Context removeTrans = new Context(contexts, RemoveTransServlet.CONTEXT_PATH, Context.SESSIONS);
        removeTrans.addServlet(new ServletHolder(new RemoveTransServlet(transformationMap)), "/*");

        // Step port reservation
        //
        Context getPort = new Context(contexts, AllocateServerSocketServlet.CONTEXT_PATH, Context.SESSIONS);
        getPort.addServlet(new ServletHolder(new AllocateServerSocketServlet(transformationMap)), "/*");

        // Port listing information 
        //
        Context listPorts = new Context(contexts, ListServerSocketServlet.CONTEXT_PATH, Context.SESSIONS);
        listPorts.addServlet(new ServletHolder(new ListServerSocketServlet(transformationMap)), "/*");

        // Sniff transformation step
        //
        Context sniffStep = new Context(contexts, SniffStepServlet.CONTEXT_PATH, Context.SESSIONS);
        sniffStep.addServlet(new ServletHolder(new SniffStepServlet(transformationMap)), "/*");

        // execute transformation
        //
        Context executeTrans = new Context(contexts, ExecuteTransServlet.CONTEXT_PATH, Context.SESSIONS);
        executeTrans .addServlet(new ServletHolder(new ExecuteTransServlet(transformationMap)), "/*");

        ///////////////////////////////////////////////////////////////////////////////////////////////////////
        //
        // The job handlers...
        //
        
        // Start job
        //
        Context startJob = new Context(contexts, StartJobServlet.CONTEXT_PATH, Context.SESSIONS);
        startJob.addServlet(new ServletHolder(new StartJobServlet(jobMap)), "/*");
        
        // Stop transformation
        //
        Context stopJob = new Context(contexts, StopJobServlet.CONTEXT_PATH, Context.SESSIONS);
        stopJob.addServlet(new ServletHolder(new StopJobServlet(jobMap)), "/*");

        // Trans status
        //
        Context jobStatus = new Context(contexts, GetJobStatusServlet.CONTEXT_PATH, Context.SESSIONS);
        jobStatus.addServlet(new ServletHolder(new GetJobStatusServlet(jobMap)), "/*");

        // Add job
        //
        Context addJob= new Context(contexts, AddJobServlet.CONTEXT_PATH, Context.SESSIONS);
        addJob.addServlet(new ServletHolder(new AddJobServlet(jobMap, socketRepository)), "/*");

        // Remove Job
        //
        Context removeJob = new Context(contexts, RemoveJobServlet.CONTEXT_PATH, Context.SESSIONS);
        removeJob.addServlet(new ServletHolder(new RemoveJobServlet(jobMap)), "/*");

        
        ///////////////////////////////////////////////////////////////////////////////////////////////////////
        //
        /// Cluster management
        //
        
        // Register a new slave on the master
        //
        Context registerSlave= new Context(contexts, RegisterSlaveServlet.CONTEXT_PATH, Context.SESSIONS);
        registerSlave.addServlet(new ServletHolder(new RegisterSlaveServlet(detections)), "/*");

        // Get list of registered slave servers
        //
        Context getSlaves= new Context(contexts, GetSlavesServlet.CONTEXT_PATH, Context.SESSIONS);
        getSlaves.addServlet(new ServletHolder(new GetSlavesServlet(detections)), "/*");


        ///////////////////////////////////////////////////////////////////////////////////////////////////////
        //
        /// Resources
        //

        // Add export 
        //
        Context addExport = new Context(contexts, AddExportServlet.CONTEXT_PATH, Context.SESSIONS);
        addExport.addServlet(new ServletHolder(new AddExportServlet(jobMap, transformationMap)), "/*");
        
        

        server.setHandlers(new Handler[] { securityHandler, contexts });

        // Start execution
        createListeners();
        
        server.start();
    }

    public void stopServer() {
		try {
			if (server != null) {
			  
			  // Stop the monitoring timer
			  //
			  if (slaveMonitoringTimer!=null) {
			    slaveMonitoringTimer.cancel();
			    slaveMonitoringTimer = null;
			  }
			  
				// Clean up all the server sockets...
				//
				socketRepository.closeAll();

				// Stop the server...
				//
				server.stop();
			}
		} catch (Exception e) {
			log.logError(BaseMessages.getString(PKG, "WebServer.Error.FailedToStop.Title"), BaseMessages.getString(PKG, "WebServer.Error.FailedToStop.Msg", "" + e));
		}
	}
    
    private void createListeners() 
    {
        SocketConnector connector = new SocketConnector();
        connector.setPort(port);
        connector.setHost(hostname);
        connector.setName(BaseMessages.getString(PKG, "WebServer.Log.KettleHTTPListener",hostname));
        log.logBasic(BaseMessages.getString(PKG, "WebServer.Log.CreateListener",hostname,""+port));

        server.setConnectors( new Connector[] { connector });
    }

    /**
     * @return the hostname
     */
    public String getHostname()
    {
        return hostname;
    }

    /**
     * @param hostname the hostname to set
     */
    public void setHostname(String hostname)
    {
        this.hostname = hostname;
    }

	/**
	 * @return the slave server detections
	 */
	public List<SlaveServerDetection> getDetections() {
		return detections;
	}

	/**
	 * This method registers a timer to check up on all the registered slave servers every X seconds.<br>
	 */
    private void startSlaveMonitoring() {
  		slaveMonitoringTimer = new Timer("WebServer Timer");
  		TimerTask timerTask = new TimerTask() {
		
			public void run() {
				for (SlaveServerDetection slaveServerDetection : detections) {
					SlaveServer slaveServer = slaveServerDetection.getSlaveServer();
					
					// See if we can get a status...
					//
					try {
						// TODO: consider making this lighter or retaining more information...
						slaveServer.getStatus(); // throws the exception
						slaveServerDetection.setActive(true);
						slaveServerDetection.setLastActiveDate(new Date());
					} catch(Exception e) {
						slaveServerDetection.setActive(false);
						slaveServerDetection.setLastInactiveDate(new Date());
						
						// TODO: kick it out after a configurable period of time...
					}
				}
			}
		};
		slaveMonitoringTimer.schedule(timerTask, 20000, 20000);
	}

	/**
	 * @return the socketRepository
	 */
	public SocketRepository getSocketRepository() {
		return socketRepository;
	}

	/**
	 * @param socketRepository the socketRepository to set
	 */
	public void setSocketRepository(SocketRepository socketRepository) {
		this.socketRepository = socketRepository;
	}
}

