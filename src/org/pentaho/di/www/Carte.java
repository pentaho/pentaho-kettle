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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class Carte
{
	private static Class<?> PKG = Carte.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private WebServer webServer;
	private SlaveServerConfig config;
	private LogChannelInterface log;

	public Carte(final SlaveServerConfig config) throws Exception {
		this.config = config;

		this.log = new LogChannel("Carte");
		
		boolean allOK=true;
		
        final TransformationMap transformationMap = new TransformationMap();
        final JobMap jobMap = new JobMap();
        List<SlaveServerDetection> detections = Collections.synchronizedList(new ArrayList<SlaveServerDetection>());
        SocketRepository socketRepository = new SocketRepository(log);
        
        Trans trans = generateTestTransformation();
        transformationMap.addTransformation(trans.getName(), trans, new TransConfiguration(trans.getTransMeta(), new TransExecutionConfiguration()));
        
    	SlaveServer slaveServer = config.getSlaveServer();
        
        String hostname = slaveServer.getHostname();
        int port = WebServer.PORT;
        if (!Const.isEmpty(slaveServer.getPort()))
        {
            try
            {
                port = Integer.parseInt(slaveServer.getPort());
            }
            catch(Exception e)
            {
                log.logError(BaseMessages.getString(PKG, "Carte.Error.CanNotPartPort", slaveServer.getHostname(), ""+port), e);
                allOK=false;
            }
        }

        // TODO: see if we need to keep doing this on a periodic basis.
        // The master might be dead or not alive yet at the time we send this message.
        // Repeating the registration over and over every few minutes might harden this sort of problems.
        //
        if (config.isReportingToMasters()) {
        	final SlaveServer client = new SlaveServer("Dynamic slave ["+hostname+":"+port+"]", hostname, ""+port, slaveServer.getUsername(), slaveServer.getPassword());
	        for (final SlaveServer master : config.getMasters()) {
	        	// Here we use the username/password specified in the slave server section of the configuration.
	        	// This doesn't have to be the same pair as the one used on the master!
	        	//
	        	try {
		        	SlaveServerDetection slaveServerDetection = new SlaveServerDetection(client);
		        	master.sendXML(slaveServerDetection.getXML(), RegisterSlaveServlet.CONTEXT_PATH+"/");
	        		log.logBasic("Registered this slave server to master slave server ["+master.toString()+"] on address ["+master.getServerAndPort()+"]");
	        	} catch(Exception e) {
	        		log.logError("Unable to register to master slave server ["+master.toString()+"] on address ["+master.getServerAndPort()+"]");
	        		allOK=false;
	        	}
	        }
        }
        
        // If we need to time out finished or idle objects, we should create a timer in the background to clean
        //
        if (config.getObjectTimeoutMinutes()>0) {

        	log.logBasic("Installing timer to purge stale objects after "+config.getObjectTimeoutMinutes()+" minutes.");
        	
    		Timer timer = new Timer();

    		final AtomicBoolean busy = new AtomicBoolean(false);
    		TimerTask timerTask = new TimerTask() {
    			public void run() {
    				if (!busy.get()) {
    					busy.set(true);

    					try {
	    					// Check all transformations...
	    					//
	    					for (CarteObjectEntry entry : transformationMap.getTransformationObjects()) {
	    						Trans trans = transformationMap.getTransformation(entry);
	    						
	    						// See if the transformation is finished or stopped.
	    						//
	    						if (trans!=null && (trans.isFinished() || trans.isStopped()) && trans.getLogDate()!=null) {
	    							// check the last log time
	    							//
	    							int diffInMinutes = (int)Math.floor( (System.currentTimeMillis() - trans.getLogDate().getTime())/60000 );
	    							if (diffInMinutes>=config.getObjectTimeoutMinutes()) {
	    								// Let's remove this from the transformation map...
	    								//
	    								transformationMap.removeTransformation(entry);
    									transformationMap.deallocateServerSocketPorts(entry);
	    							}
	    						}
	    					}
	    					
	    					// And the jobs...
	    					//
	    					for (CarteObjectEntry entry : jobMap.getJobObjects()) {
	    						Job job = jobMap.getJob(entry);
	    						
	    						// See if the job is finished or stopped.
	    						//
	    						if (job!=null && (job.isFinished() || job.isStopped()) && job.getLogDate()!=null) {
	    							// check the last log time
	    							//
	    							int diffInMinutes = (int)Math.floor( (System.currentTimeMillis() - job.getLogDate().getTime())/60000 );
	    							if (diffInMinutes>=config.getObjectTimeoutMinutes()) {
	    								// Let's remove this from the job map...
	    								//
	    								jobMap.removeJob(entry);
	    							}
	    						}
	    					}
	    					
    					} finally {
    						busy.set(false);
    					}
    				}
    			}
    		};

    		// Search for stale objects every minute:
    		//
    		timer.schedule(timerTask, 60000, 60000);
        }
        
        if (allOK) {
        	this.webServer = new WebServer(log, transformationMap, jobMap, socketRepository, detections, hostname, port, config.isJoining());
        }
	}
	
    public static void main(String[] args) throws Exception
    {
    	// Load from an xml file that describes the complete configuration...
    	//
    	SlaveServerConfig config = null;
    	if (args.length==1 && !Const.isEmpty(args[0])) {
    		FileObject file = KettleVFS.getFileObject(args[0]);
    		Document document = XMLHandler.loadXMLFile(file);
    		Node configNode = XMLHandler.getSubNode(document, SlaveServerConfig.XML_TAG); 
    		config = new SlaveServerConfig(new LogChannel("Slave server config"), configNode);
    	}
    	if (args.length==2  && !Const.isEmpty(args[0])  && !Const.isEmpty(args[1])) {
    		String hostname = args[0];
    		String port = args[1];
    		SlaveServer slaveServer = new SlaveServer(hostname+":"+port, hostname, port, null, null);
    		
    		config = new SlaveServerConfig();
    		config.setSlaveServer(slaveServer);
    	}
    	
    	// Nothing configured: show the usage
    	//
        if (config==null)
        {
            System.err.println(BaseMessages.getString(PKG, "Carte.Usage.Text"));
            System.err.println();

            System.err.println(BaseMessages.getString(PKG, "Carte.Usage.Example") + ": Carte 127.0.0.1 8080");
            System.err.println(BaseMessages.getString(PKG, "Carte.Usage.Example") + ": Carte 192.168.1.221 8081");
            System.err.println();
            System.err.println(BaseMessages.getString(PKG, "Carte.Usage.Example") + ": Carte /foo/bar/carte-config.xml");
            System.err.println(BaseMessages.getString(PKG, "Carte.Usage.Example") + ": Carte http://www.example.com/carte-config.xml");

            System.exit(1);
        }
        
        runCarte(config);
    }

    public static void runCarte(SlaveServerConfig config) throws Exception {
    	init();
    	       
    	CentralLogStore.init(config.getMaxLogLines(), config.getMaxLogTimeoutMinutes());

        // Join with the process: block
        //
        config.setJoining(true);
        
        new Carte(config);
	}

	private static void init() throws Exception
    {
	    KettleEnvironment.init();
	    LogWriter.getInstance( LogWriter.LOG_LEVEL_BASIC );
	}
    
    public static Trans generateTestTransformation()
    {
        RowGeneratorMeta A = new RowGeneratorMeta();
        A.allocate(3);
        A.setRowLimit("100000000");

        A.getFieldName()[0]   = "ID"; 
        A.getFieldType()[0]   = ValueMeta.getTypeDesc(ValueMetaInterface.TYPE_INTEGER);
        A.getFieldLength()[0] = 7; 
        A.getValue()[0]       = "1234"; 
        
        A.getFieldName()[1]   = "Name"; 
        A.getFieldType()[1]   = ValueMeta.getTypeDesc(ValueMetaInterface.TYPE_STRING);
        A.getFieldLength()[1] = 35; 
        A.getValue()[1]       = "Some name"; 

        A.getFieldName()[2]   = "Last updated"; 
        A.getFieldType()[2]   = ValueMeta.getTypeDesc(ValueMetaInterface.TYPE_DATE);
        A.getFieldFormat()[2] = "yyyy/MM/dd"; 
        A.getValue()[2]       = "2006/11/13"; 

        TransMeta transMeta = TransPreviewFactory.generatePreviewTransformation(null, A, "A");
        transMeta.setName("Row generator test");
        transMeta.setSizeRowset(2500);
        transMeta.setFeedbackSize(50000);
        transMeta.setUsingThreadPriorityManagment(false);

        return new Trans(transMeta);
        
    }

	/**
	 * @return the webServer
	 */
	public WebServer getWebServer() {
		return webServer;
	}

	/**
	 * @param webServer the webServer to set
	 */
	public void setWebServer(WebServer webServer) {
		this.webServer = webServer;
	}

	/**
	 * @return the slave server (Carte) configuration
	 */
	public SlaveServerConfig getConfig() {
		return config;
	}

	/**
	 * @param config the slave server (Carte) configuration
	 */
	public void setConfig(SlaveServerConfig config) {
		this.config = config;
	}

}
