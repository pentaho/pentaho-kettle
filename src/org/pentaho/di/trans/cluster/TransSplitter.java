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
package org.pentaho.di.trans.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.trans.SlaveStepCopyPartitionDistribution;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RemoteStep;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepPartitioningMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.socketreader.SocketReaderMeta;
import org.pentaho.di.trans.steps.socketwriter.SocketWriterMeta;

/**
 * This class takes care of the separation of the original transformation into pieces that run on the different slave servers in the clusters used.
 * 
 * @author Matt
 *
 */
public class TransSplitter
{
    private static final int FANOUT = 30;
    private static final int SPLIT  = 120;
    
    private TransMeta  originalTransformation;
    private Map<ClusterSchema,Integer>        clusterPortMap;
    private Map<String,Integer>        clusterStepPortMap;
    private Map<SlaveServer,TransMeta>        slaveTransMap;
    private TransMeta  master;
    private StepMeta[] referenceSteps;
    private Map<SlaveServer,Map<PartitionSchema,List<String>>> slaveServerPartitionsMap;
    private Map<TransMeta,Map<StepMeta,String>> slaveStepPartitionFlag;

    /** Decide in advance which step / copy in which slave server is going to server which partition.  It's not a guessing game. */
    private SlaveStepCopyPartitionDistribution slaveStepCopyPartitionDistribution = new SlaveStepCopyPartitionDistribution();
	private int socketsBufferSize;
	private boolean compressingSocketStreams;
    
    public TransSplitter()
    {
        clear();
    }
    
    private void clear() {
    	clusterPortMap = new Hashtable<ClusterSchema,Integer>();
        clusterStepPortMap = new Hashtable<String,Integer>();
        
        slaveTransMap = new Hashtable<SlaveServer,TransMeta>();
        slaveStepPartitionFlag = new Hashtable<TransMeta,Map<StepMeta,String>>();
    }


	/**
     * @param originalTransformation
     */
    public TransSplitter(TransMeta originalTransformation)
    {
        this();
        this.originalTransformation = originalTransformation;
    }

    /**
     * @return the originalTransformation
     */
    public TransMeta getOriginalTransformation()
    {
        return originalTransformation;
    }

    /**
     * @param originalTransformation the originalTransformation to set
     */
    public void setOriginalTransformation(TransMeta originalTransformation)
    {
        this.originalTransformation = originalTransformation;
    }

    
    private void checkClusterConfiguration() throws KettleException
    {
        Map<String,ClusterSchema> map = new Hashtable<String,ClusterSchema>();
        StepMeta[] steps = originalTransformation.getStepsArray();
        for (int i=0;i<steps.length;i++)
        {
            ClusterSchema clusterSchema = steps[i].getClusterSchema(); 
            if (clusterSchema!=null)
            {
                map.put(steps[i].getClusterSchema().getName(), steps[i].getClusterSchema());
                
                if (clusterSchema.findMaster()==null)
                {
                    throw new KettleException("No master server was specified in cluster schema ["+clusterSchema+"]");
                }
                
                // Grab the 
                socketsBufferSize = Const.toInt( originalTransformation.environmentSubstitute(clusterSchema.getSocketsBufferSize()), 50000  );
                compressingSocketStreams = clusterSchema.isSocketsCompressed();
            }
        }
        if (map.size()==0)
        {
            throw new KettleException("No cluster schemas are being used.  As such it is not possible to split and cluster this transformation.");
        }
        if (map.size()>1)
        {
            throw new KettleException("At this time we don't support the use of multiple cluster schemas in one and the same transformation.");
        }
    }

    private String getWriterName(String stepname, ClusterSchema clusterSchema, SlaveServer slaveServer)
    {
        return "Writer : "+getPort(clusterSchema, slaveServer, stepname);
    }
    
    private String getReaderName(String stepname, ClusterSchema clusterSchema, SlaveServer slaveServer)
    {
        return "Reader : "+getPort(clusterSchema, slaveServer, stepname);
    }

    private String getSlaveTransName(String transName, ClusterSchema clusterSchema, SlaveServer slaveServer)
    {
        return transName + " ("+clusterSchema+":"+slaveServer.getName()+")";
    }
    
    /**
     * Get the port for the given cluster schema, slave server and step
     * If a port was allocated, that is returned, otherwise a new one is allocated.
     * 
     * @param clusterSchema The cluster schema 
     * @param serverSocketSlaveServer The slave server
     * @param serverSocketStep the step name without reader/writer denotion.
     * 
     * @return the port to use for that step/slaveserver/cluster combination
     */
    private int getPort(ClusterSchema clusterSchema, SlaveServer serverSocketSlaveServer, String serverSocketStep)
    {
        String key = clusterSchema.getName()+" - "+serverSocketSlaveServer + " - " + serverSocketStep + " - ";
        int p;
        Integer port = (Integer) clusterStepPortMap.get(key);
        if (port==null)
        {
            p = getNextPort(clusterSchema);
            clusterStepPortMap.put(key, Integer.valueOf(p));
        }
        else
        {
            p = port.intValue();
        }
        
        // System.out.println("Port for ["+key+"] is : "+p);
        
        return p;
    }
    
    /**
     * Allocates a new port for the cluster.  This port has to be unique in the master, so it has to increase with each call.
     * @param clusterSchema the cluster schema to allocate for.  Each clusterSchema should have it's own range.
     * @return the next port number
     */
    private int getNextPort(ClusterSchema clusterSchema)
    {
        int p;
        Integer port = (Integer) clusterPortMap.get(clusterSchema);
        if (port==null)
        {
            p = Integer.parseInt( clusterSchema.environmentSubstitute(clusterSchema.getBasePort())  );
        }
        else
        {
            p = port.intValue()+1;
        }
        clusterPortMap.put(clusterSchema, Integer.valueOf(p));
        return p;
    }
    
    /**
     * Create or get a slave transformation for the specified cluster & slave server
     * @param clusterSchema the cluster schema to reference
     * @param slaveServer the slave server to reference
     * @return
     */
    private TransMeta getSlaveTransformation(ClusterSchema clusterSchema, SlaveServer slaveServer) throws KettleException
    {
        TransMeta slave = (TransMeta) slaveTransMap.get(slaveServer);
        if (slave==null)
        {
            slave = getOriginalCopy(true, clusterSchema, slaveServer);
            slaveTransMap.put(slaveServer, slave);
        }
        return slave;
    }

    private TransMeta getOriginalCopy(boolean isSlaveTrans, ClusterSchema clusterSchema, SlaveServer slaveServer) throws KettleException
    {
        TransMeta transMeta = new TransMeta();
        transMeta.setSlaveTransformation(true);
        
        if (isSlaveTrans)
        {
            transMeta.setName(getSlaveTransName(originalTransformation.getName(), clusterSchema, slaveServer));
            
            NotePadMeta slaveNote = new NotePadMeta("This is a generated slave transformation.\nIt will be run on slave server: "+slaveServer, 0, 0, -1, -1);
            transMeta.addNote(slaveNote);
            
            // add the slave partitioning schema's here.
            for (int i=0;i<referenceSteps.length;i++)
            {
                StepMeta stepMeta = referenceSteps[i];
                verifySlavePartitioningConfiguration(transMeta, stepMeta, clusterSchema, slaveServer);
            }
        }
        else
        {
            transMeta.setName(originalTransformation.getName()+" (master)");

            NotePadMeta masterNote = new NotePadMeta("This is a generated master transformation.\nIt will be run on server: "+getMasterServer(), 0, 0, -1, -1);
            transMeta.addNote(masterNote);
        }
        
        // Copy the cluster schemas
        //
        for (ClusterSchema schema : originalTransformation.getClusterSchemas()) {
            transMeta.getClusterSchemas().add((ClusterSchema) schema.clone());
        }
        
        transMeta.setDatabases(originalTransformation.getDatabases());

        // Feedback
        transMeta.setFeedbackShown(originalTransformation.isFeedbackShown());
        transMeta.setFeedbackSize(originalTransformation.getFeedbackSize());
        
        // Priority management
        transMeta.setUsingThreadPriorityManagment(originalTransformation.isUsingThreadPriorityManagment());

        // Unique connections
        transMeta.setUsingUniqueConnections(originalTransformation.isUsingUniqueConnections());
        
        // Also set the logging details...
        transMeta.setReadStep( originalTransformation.getReadStep() );
        transMeta.setWriteStep( originalTransformation.getWriteStep() );
        transMeta.setInputStep( originalTransformation.getInputStep() );
        transMeta.setOutputStep( originalTransformation.getOutputStep() );
        transMeta.setRejectedStep( originalTransformation.getRejectedStep() );

        transMeta.setLogConnection( originalTransformation.getLogConnection() );
        transMeta.setLogTable( originalTransformation.getLogTable() );
        transMeta.setBatchIdUsed( originalTransformation.isBatchIdUsed() );
        transMeta.setLogfieldUsed( originalTransformation.isLogfieldUsed() );
        
        return transMeta;
    }
    
    private void verifySlavePartitioningConfiguration(TransMeta slave, StepMeta stepMeta, ClusterSchema clusterSchema, SlaveServer slaveServer)
    {
        Map<StepMeta,String> stepPartitionFlag = slaveStepPartitionFlag.get(slave); 
        if (stepPartitionFlag==null)
        {
            stepPartitionFlag = new Hashtable<StepMeta,String>();
            slaveStepPartitionFlag.put(slave, stepPartitionFlag);
        }
        if (stepPartitionFlag.get(stepMeta)!=null) return; // already done;
        
        StepPartitioningMeta partitioningMeta = stepMeta.getStepPartitioningMeta();
        if (partitioningMeta!=null && partitioningMeta.getMethodType()!=StepPartitioningMeta.PARTITIONING_METHOD_NONE && partitioningMeta.getPartitionSchema()!=null)
        {
            // Find the schemaPartitions map to use
            Map<PartitionSchema,List<String>> schemaPartitionsMap = slaveServerPartitionsMap.get(slaveServer);
            if (schemaPartitionsMap!=null)
            {
                PartitionSchema partitionSchema = partitioningMeta.getPartitionSchema();
                List<String> partitionsList = schemaPartitionsMap.get(partitionSchema);
                if (partitionsList!=null) 
                {
                    // We found a list of partitions, now let's create a new partition schema with this data.
                    String targetSchemaName = createSlavePartitionSchemaName(partitionSchema.getName());
                    PartitionSchema targetSchema = slave.findPartitionSchema(targetSchemaName);
                    if (targetSchema==null)
                    {
                        targetSchema = new PartitionSchema(targetSchemaName, partitionsList);
                        slave.getPartitionSchemas().add(targetSchema); // add it to the slave if it doesn't exist.
                    }
                }
            }
        }
        
        stepPartitionFlag.put(stepMeta, "Y"); // is done.
    }

    public static String createSlavePartitionSchemaName(String name) {
		return name;
	}

    private static final String STRING_TARGET_PARTITION_NAME_SUFFIX = " (target)";
    
    public static String createTargetPartitionSchemaName(String name) {
		return name + STRING_TARGET_PARTITION_NAME_SUFFIX;
	}
    
    public static String createPartitionSchemaNameFromTarget(String targetName) {
    	if (targetName.endsWith(STRING_TARGET_PARTITION_NAME_SUFFIX)) {
    		return targetName.substring(0, targetName.length()-STRING_TARGET_PARTITION_NAME_SUFFIX.length());
    	}
    	else {
    		return targetName;
    	}
    }

	/**
     * @return the master
     */
    public TransMeta getMaster()
    {
        return master;
    }

    /**
     * @return the slaveTransMap : the mapping between a slaveServer and the transformation
     *
     */
    public Map<SlaveServer, TransMeta> getSlaveTransMap()
    {
        return slaveTransMap;
    }

    
    public TransMeta[] getSlaves()
    {
        Collection<TransMeta> collection = slaveTransMap.values();
        return collection.toArray(new TransMeta[collection.size()]);
    }
    
    public SlaveServer[] getSlaveTargets()
    {
        Set<SlaveServer> set = slaveTransMap.keySet();
        return set.toArray(new SlaveServer[set.size()]);
    }
    
    public SlaveServer getMasterServer() throws KettleException
    {
        StepMeta[] steps = originalTransformation.getStepsArray();
        for (int i=0;i<steps.length;i++)
        {
            ClusterSchema clusterSchema = steps[i].getClusterSchema(); 
            if (clusterSchema!=null)
            {
                return clusterSchema.findMaster();
            }
        }
        throw new KettleException("No master server could be found in the original transformation");
    }
    
    
    public void splitOriginalTransformation() throws KettleException
    {
    	clear();
        // Mixing clusters is not supported at the moment
        // Perform some basic checks on the cluster configuration.
        // 
        findUsedOriginalSteps();
        checkClusterConfiguration(); 
        generateSlavePartitionSchemas();
        
        try
        {
        	SlaveServer masterServer = getMasterServer();
            master = getOriginalCopy(false, null, null);
            
            for (int r=0;r<referenceSteps.length;r++)
            {
                StepMeta referenceStep = referenceSteps[r];
                ClusterSchema referenceClusterSchema = referenceStep.getClusterSchema(); 

                int nrPreviousSteps = originalTransformation.findNrPrevSteps(referenceStep);
                for (int p=0;p<nrPreviousSteps;p++)
                {
                    StepMeta previousStep = originalTransformation.findPrevStep(referenceStep, p);

                    ClusterSchema previousClusterSchema = previousStep.getClusterSchema();
                    if (referenceClusterSchema==null) {
                        if (previousClusterSchema==null)
                        {
                            // No clustering involved here: just add the reference step to the master
                            //
                            StepMeta target = master.findStep(referenceStep.getName());
                            if (target==null)
                            {
                                target = (StepMeta) referenceStep.clone();
                                master.addStep(target);
                            }
                            
                            StepMeta source = master.findStep(previousStep.getName());
                            if (source==null)
                            {
                                source = (StepMeta) previousStep.clone();
                                master.addStep(source);
                            }
                            
                            // Add a hop too...
                            //
                            TransHopMeta masterHop = new TransHopMeta(source, target);
                            master.addTransHop(masterHop);
                        }
                        else
                        {
                            // reference step is NOT clustered
                            // Previous step is clustered
                            // --> We read from the slave server using socket readers.
                            //     We need a reader for each slave server in the cluster
                            //
                            
                            // Also add the reference step to the master. (cloned)
                            //
                            StepMeta target = master.findStep(referenceStep.getName());
                            if (target==null) {
                                target = (StepMeta) referenceStep.clone();
                                target.setLocation(target.getLocation().x, target.getLocation().y);
                                master.addStep(target);
                            }
                            
                            // Then add the remote input/output steps to master and slave 
                            // 
                            int nrSlaves = previousClusterSchema.getSlaveServers().size();
                            for (int slaveNr=0;slaveNr<nrSlaves;slaveNr++)
                            {
                                SlaveServer slaveServer = (SlaveServer) previousClusterSchema.getSlaveServers().get(slaveNr);
                                
                                if (!slaveServer.isMaster())
                                {
                                	// MASTER: add remote input steps to the master step.  That way it can receive data over sockets.
                                	// 
                                	int port = getPort(previousClusterSchema, slaveServer, previousStep.getName() + " - " + referenceStep.getName());
                                	
                                	// Default: we send/receive to/from copy 0 of the remote step.
                                	//
                                    RemoteStep remoteSlaveStep = new RemoteStep(slaveServer.getHostname(), masterServer.getHostname(), Integer.toString(port), previousStep.getName(), 0, target.getName(), 0, slaveServer.getName(), socketsBufferSize, compressingSocketStreams);
                                	target.getRemoteInputSteps().add(remoteSlaveStep);
                                	
                                    // SLAVE : add remote output steps to the previous step
                                	//
                                	TransMeta slave = getSlaveTransformation(previousClusterSchema, slaveServer);

                                    // See if we can add a link to the previous using the Remote Steps concept.
                                	//
                                    StepMeta previous = slave.findStep(previousStep.getName());
                                    if (previous==null)
                                    {
                                        previous = addSlaveCopy(slave, previousStep, slaveServer);
                                    }
                                    
                                    // Default: we send/receive to/from copy 0 of the remote step.
                                	//
                                    RemoteStep remoteMasterStep = new RemoteStep(masterServer.getHostname(), slaveServer.getHostname(), Integer.toString(port), previous.getName(), 0, target.getName(), 0, masterServer.getName(), socketsBufferSize, compressingSocketStreams);
                                    previous.getRemoteOutputSteps().add(remoteMasterStep);

                                    // TODO: verify partitioning logic.
                                    // Make sure the data finds its way back to the master.
                                    //
                                }
                            }
                        }
                    }
                    else
                    {
                        if (previousClusterSchema==null)
                        {
                            // reference step is clustered
                            // previous step is not clustered
                            // --> Add a socket writer for each slave server
                            //
                            int nrSlaves = referenceClusterSchema.getSlaveServers().size();
                            for (int s=0;s<nrSlaves;s++)
                            {
                                SlaveServer slaveServer = (SlaveServer) referenceClusterSchema.getSlaveServers().get(s);
                                if (!slaveServer.isMaster())
                                {
                                	// MASTER : add remote output step to the previous step
                                	//
                                    StepMeta masterStep = master.findStep(previousStep.getName());
                                    if (masterStep==null) {
                                        masterStep = (StepMeta) previousStep.clone();
                                        masterStep.setLocation(previousStep.getLocation().x, previousStep.getLocation().y);
                                        master.addStep(masterStep); 
                                    }
                                    
                                    // SLAVE : add remote input step to the reference slave step...
                                    //
                                    TransMeta slave = getSlaveTransformation(referenceClusterSchema, slaveServer);
                                    
                                    // also add the step itself.
                                    StepMeta slaveStep = slave.findStep(referenceStep.getName());
                                    if (slaveStep==null)
                                    {
                                        slaveStep = addSlaveCopy(slave, referenceStep, slaveServer);
                                    }
                                    
                                    // Verify the partitioning for this slave step.
                        			// It's running in 1 or more copies depending on the number of partitions
                        			// Get the number of target partitions...
                        			//
                        			Map<PartitionSchema, List<String>> partitionsMap = slaveServerPartitionsMap.get(slaveServer);
                        			StepPartitioningMeta targetStepPartitioningMeta = referenceStep.getStepPartitioningMeta();
                        			PartitionSchema targetPartitionSchema = targetStepPartitioningMeta.getPartitionSchema();
                        			int nrOfTargetPartitions = 1;
                        			if (slaveStep.isPartitioned() && targetPartitionSchema!=null) {
                            			List<String> targetPartitionsList = partitionsMap.get(targetPartitionSchema);
                            			nrOfTargetPartitions = targetPartitionsList.size();
                        			}

                        			// Add the required remote input and output steps to make the partitioning a reality.
                        			//
                        			for (int targetCopyNr=0;targetCopyNr<nrOfTargetPartitions;targetCopyNr++) {
                        				// Default: we send from copy 0 to the remote step. 
                        				// TODO: add support for steps running multiple copies or partitioned on the master. (needs a loop above this with a sourceCopyNr
                                    	//
                        				int port = getPort(referenceClusterSchema, slaveServer, slaveServer+"-"+masterStep.getName()+".0"+" --> "+slaveStep.getName()+"."+targetCopyNr);

                                        RemoteStep remoteMasterStep = new RemoteStep(slaveServer.getHostname(), masterServer.getHostname(), Integer.toString(port), masterStep.getName(), 0, referenceStep.getName(), targetCopyNr, slaveServer.getName(), socketsBufferSize, compressingSocketStreams);
                                        masterStep.getRemoteOutputSteps().add(remoteMasterStep);

                                        RemoteStep remoteSlaveStep = new RemoteStep(masterServer.getHostname(), slaveServer.getHostname(), Integer.toString(port), masterStep.getName(), 0, referenceStep.getName(), targetCopyNr, masterServer.getName(), socketsBufferSize, compressingSocketStreams);
                                        slaveStep.getRemoteInputSteps().add(remoteSlaveStep);
                                        
                                        // OK, create a partition number for the target step in the partition distribution...
                            			//
                                        if (slaveStep.isPartitioned()) {
                                        	slaveStepCopyPartitionDistribution.addPartition(slaveServer.getName(), targetPartitionSchema.getName(), targetCopyNr);
                                        }
                        			}
                        			
                        			// Also set the target partitioning on the master step.
                        			// A copy of the original reference step partitioning schema
                        			//
                        			if (targetStepPartitioningMeta.isPartitioned()) {
                        				
                        				// Set the target partitioning schema for the source step (master)
                        				//
                        				StepPartitioningMeta stepPartitioningMeta = targetStepPartitioningMeta.clone();
                        				PartitionSchema partitionSchema = stepPartitioningMeta.getPartitionSchema();
                        				partitionSchema.setName( createTargetPartitionSchemaName(partitionSchema.getName())) ;
                        				
                        				if (partitionSchema.isDynamicallyDefined()) {
                        					// Expand the cluster definition to: nrOfSlaves*nrOfPartitionsPerSlave...
                        					//
                        					partitionSchema.expandPartitionsDynamically(referenceClusterSchema.findNrSlaves(), originalTransformation);
                        				}
                        				masterStep.setTargetStepPartitioningMeta( stepPartitioningMeta );
                    					master.addOrReplacePartitionSchema(partitionSchema);
                        				
                    					// Now set the partitioning schema for the slave step...
                    					// For the slave step, we only should those partition IDs that are interesting for the current slave...
                        				//
                        				stepPartitioningMeta = targetStepPartitioningMeta.clone();
                        				partitionSchema = stepPartitioningMeta.getPartitionSchema();
                        				partitionSchema.setName( createSlavePartitionSchemaName(partitionSchema.getName())) ;
                        				
                        				if (partitionSchema.isDynamicallyDefined()) {
                        					// Expand the cluster definition to: nrOfSlaves*nrOfPartitionsPerSlave...
                        					//
                        					partitionSchema.expandPartitionsDynamically(referenceClusterSchema.findNrSlaves(), originalTransformation);
                        				}
                        				
                        				partitionSchema.retainPartitionsForSlaveServer(referenceClusterSchema.findNrSlaves(), getSlaveServerNumber(referenceClusterSchema, slaveServer));
                    					slave.addOrReplacePartitionSchema( partitionSchema );
                        			}
                                }
                            }
                        }
                        else
                        {
                            // reference step is clustered
                            // previous step is clustered
                            // --> Add reference step to the slave transformation(s)
                            //
                        	int nrSlaves = referenceClusterSchema.getSlaveServers().size();
                            for (int slaveNr=0;slaveNr<nrSlaves;slaveNr++)
                            {
                                SlaveServer slaveServer = referenceClusterSchema.getSlaveServers().get(slaveNr);
                                if (!slaveServer.isMaster())
                                {
                                    // SLAVE
                                    TransMeta slave = getSlaveTransformation(referenceClusterSchema, slaveServer);
                                    
                                    // This is the target step
                                    //
                                    StepMeta target = slave.findStep(referenceStep.getName());
                                    if (target==null)
                                    {
                                        target = addSlaveCopy(slave, referenceStep, slaveServer);
                                    }
                                    
                                    // This is the source step
                                    //
                                    StepMeta source = slave.findStep(previousStep.getName());
                                    if (source==null)
                                    {
                                        source = addSlaveCopy(slave, previousStep, slaveServer);
                                    }
                                    
                                    // Add a hop between source and target
                                    //
                                    TransHopMeta slaveHop = new TransHopMeta(source, target);
                                    slave.addTransHop(slaveHop);
                                    
                                    // Verify the partitioning
                                    // That means is this case that it is possible that
                                    // 
                                    // 1) the number of partitions is larger than the number of slaves
                                    // 2) the partitioning method might change requiring the source step to do re-partitioning.
                                    // 
                                    // We need to provide the source step with the information to re-partition correctly.
                                    //
                                    // Case 1: both source and target are partitioned on the same partition schema.
                                    //
                                    StepPartitioningMeta sourceStepPartitioningMeta = previousStep.getStepPartitioningMeta();
                                    StepPartitioningMeta targetStepPartitioningMeta = referenceStep.getStepPartitioningMeta();
                                    
                                    /*
                                    try {
                                    	if (previousStep.isPartitioned() && referenceStep.isPartitioned() && 
                                        		sourceStepPartitioningMeta.equals(targetStepPartitioningMeta)) {
                                    		// TODO: remove this block.
                                    	}
                                    }
                                    catch(NullPointerException e) {
                                    	throw e;
                                    }
                                    */
                                    
                                    if (previousStep.isPartitioned() && referenceStep.isPartitioned() && 
                                    		sourceStepPartitioningMeta.equals(targetStepPartitioningMeta)) {
                                    	
                                    	// Just divide the partitions over the available slaves...
                                    	// set the appropriate partition schema for both step...
                                    	//
                        				StepPartitioningMeta stepPartitioningMeta = sourceStepPartitioningMeta.clone();
                        				PartitionSchema partitionSchema = stepPartitioningMeta.getPartitionSchema();
                        				partitionSchema.setName( createSlavePartitionSchemaName(partitionSchema.getName())) ;
                        				
                        				if (partitionSchema.isDynamicallyDefined()) {
                        					partitionSchema.expandPartitionsDynamically(referenceClusterSchema.findNrSlaves(), originalTransformation);
                        				}
                        				partitionSchema.retainPartitionsForSlaveServer(referenceClusterSchema.findNrSlaves(), getSlaveServerNumber(referenceClusterSchema, slaveServer));
                        				
                        				source.setStepPartitioningMeta(stepPartitioningMeta);
                        				target.setStepPartitioningMeta(stepPartitioningMeta);
                        				slave.addOrReplacePartitionSchema(partitionSchema);
                                    }

                                    // Case 2: both source and target are partitioned on a different partition schema.
                                    // Case 3: source is not partitioned, target is partitioned.
                                    //
                                    //  --> This means that we're re-partitioning!!
                                    //
                                    else if ( (!previousStep.isPartitioned() && referenceStep.isPartitioned() ) || 
                                    		(previousStep.isPartitioned() && referenceStep.isPartitioned() && !sourceStepPartitioningMeta.equals(target.getStepPartitioningMeta()))
                                    	  ) {
                                    	
                                    	PartitionSchema targetPartitionSchema = targetStepPartitioningMeta.getPartitionSchema();
                                    	PartitionSchema sourcePartitionSchema = sourceStepPartitioningMeta.getPartitionSchema();
                                    	
                                    	// Since the source step is running clustered, it's running in a number of slaves, one copy each.
                                    	// All these source steps need to be able to talk to all the other target steps...
                                    	// If there are N slaves, there are N source steps and N target steps.
                                    	// We need to add N-1 remote output and input steps to the source and target step.
                                    	// This leads to Nx(N-1) extra data paths.
                                    	//
                                    	// Let's see if we can find them...
                                    	//
                            			for (int partSlaveNr=0;partSlaveNr<referenceClusterSchema.getSlaveServers().size();partSlaveNr++) {
                                    		SlaveServer partSlaveServer = referenceClusterSchema.getSlaveServers().get(partSlaveNr);
                                    		if (!partSlaveServer.isMaster()) {
                                    			
                                    			// It's running in 1 or more copies depending on the number of partitions
                                    			// Get the number of target partitions...
                                    			//
                                    			Map<PartitionSchema, List<String>> partitionsMap = slaveServerPartitionsMap.get(partSlaveServer);
                                    			int nrOfTargetPartitions = 1;
                                    			if (target.isPartitioned() && targetPartitionSchema!=null) {
	                                    			List<String> targetPartitionsList = partitionsMap.get(targetPartitionSchema);
	                                    			nrOfTargetPartitions = targetPartitionsList.size();
                                    			}
                                    			
                                    			// Get the number of source partitions...
                                    			//
                                    			int nrOfSourcePartitions = 1;
                                    			if (source.isPartitioned() && sourcePartitionSchema!=null) {
	                                    			List<String> sourcePartitionsList = partitionsMap.get(sourcePartitionSchema);
	                                    			nrOfSourcePartitions = sourcePartitionsList.size();
                                    			}

                                    			// We can't just specify 0 as the target/source copy number. At runtime we could have multiple copies of a step running for multiple partitions.
                                    			// Those ports will have to be allocated too.
                                    			//
                                    			// The port: steps A-->B
                                    			// A is not clustered and so is B
                                    			// So the data has to be re-partitioned.
                                    			//
                                    			// A0->B1, A0->B2, A0->B3, A0->B4
                                    			// A1->B0, A1->B2, A1->B3, A1->B4
                                    			// A2->B0, A2->B1, A2->B3, A2->B4
                                    			// A3->B0, A3->B1, A3->B2, A3->B4
                                    			// A4->B0, A4->B1, A4->B2, A4->B3
                                    			//
                                    			// Where the 0 in A0 specifies the source slave server
                                    			// Where the 0 in B0 specified the target slave server
                                    			//
                                    			// So all in all, we need to allocate Nx(N-1) ports. 
                                    			//
                                    			for (int sourceCopyNr=0;sourceCopyNr<nrOfSourcePartitions;sourceCopyNr++) {
                                        			for (int targetCopyNr=0;targetCopyNr<nrOfTargetPartitions;targetCopyNr++) {
                                    				
                                            			if (!slaveServer.equals(partSlaveServer)) {
	                                        				// We hit only get the remote steps, NOT the local ones.
	                                        				// That's why it's OK to generate all combinations.
	                                        				//
			                                    			int outPort = getPort(referenceClusterSchema, slaveServer, partSlaveServer+"-"+source.getName()+"."+sourceCopyNr+" --> "+target.getName()+"."+targetCopyNr);
			                                    			RemoteStep remoteOutputStep = new RemoteStep( slaveServer.getHostname(), partSlaveServer.getHostname(), Integer.toString(outPort), source.getName(), sourceCopyNr, target.getName(), targetCopyNr, partSlaveServer.getName(), socketsBufferSize, compressingSocketStreams);
			                                    			source.getRemoteOutputSteps().add(remoteOutputStep);
			
			                                    			// OK, so the source step is sending rows out on the reserved ports
			                                    			// What we need to do now is link all the OTHER slaves up to them.
			                                    			//
			                                    			int inPort = getPort(referenceClusterSchema, partSlaveServer, slaveServer+"-"+source.getName()+"."+sourceCopyNr+" --> "+target.getName()+"."+targetCopyNr);
			                                    			RemoteStep remoteInputStep = new RemoteStep( partSlaveServer.getHostname(), slaveServer.getHostname(), Integer.toString(inPort), source.getName(), sourceCopyNr, target.getName(), targetCopyNr, partSlaveServer.getName(), socketsBufferSize, compressingSocketStreams );
			                                    			target.getRemoteInputSteps().add(remoteInputStep);
                                            			}
		                                    			// OK, save the partition number for the target step in the partition distribution...
		                                    			//
		                                    			slaveStepCopyPartitionDistribution.addPartition(partSlaveServer.getName(), targetPartitionSchema.getName(), targetCopyNr);
                                        			}
                                    			}
	                                    			
                                    			// Set the target partitioning schema on the source step so that we can use that in the transformation...
                                    			// On the one hand we can't keep all partitions, otherwise the slave transformations start up N copies for N partitions.
                                    			// On the other hand we need the information to repartition.
                                    			//
                                    			
                                    			if (sourceStepPartitioningMeta.isPartitioned()) {
                                    				// Set the correct partitioning schema for the source step.
                                    				//
                                    				// Set the target partitioning schema for the target step (slave)
                                    				//
                                    				StepPartitioningMeta stepPartitioningMeta = sourceStepPartitioningMeta.clone();
                                    				PartitionSchema partitionSchema = stepPartitioningMeta.getPartitionSchema();
                                    				partitionSchema.setName( createSlavePartitionSchemaName(partitionSchema.getName())) ;
                                    				
                                    				if (partitionSchema.isDynamicallyDefined()) {
                                    					// Expand the cluster definition to: nrOfSlaves*nrOfPartitionsPerSlave...
                                    					//
                                    					partitionSchema.expandPartitionsDynamically(referenceClusterSchema.findNrSlaves(), originalTransformation);
                                    				}
                                    				
                                    				partitionSchema.retainPartitionsForSlaveServer(referenceClusterSchema.findNrSlaves(), getSlaveServerNumber(referenceClusterSchema, slaveServer));
                                    				
                                    				source.setStepPartitioningMeta( stepPartitioningMeta );
                                					slave.addOrReplacePartitionSchema(partitionSchema);
                                    				
                                    			}
                                    			
                                    			if (targetStepPartitioningMeta.isPartitioned()) {

                                    				// Set the target partitioning schema for the target step (slave)
                                    				//
                                    				StepPartitioningMeta stepPartitioningMeta = targetStepPartitioningMeta.clone();
                                    				PartitionSchema partitionSchema = stepPartitioningMeta.getPartitionSchema();
                                    				partitionSchema.setName( createSlavePartitionSchemaName(partitionSchema.getName())) ;
                                    				
                                    				if (partitionSchema.isDynamicallyDefined()) {
                                    					partitionSchema.expandPartitionsDynamically(referenceClusterSchema.findNrSlaves(), originalTransformation);
                                    				}
                                    				partitionSchema.retainPartitionsForSlaveServer(referenceClusterSchema.findNrSlaves(), getSlaveServerNumber(referenceClusterSchema, slaveServer));
                                    				
                                    				target.setStepPartitioningMeta( stepPartitioningMeta );
                                					slave.addOrReplacePartitionSchema(partitionSchema);
                                    			}
                                    			
                                    			// If we're re-partitioning, set the target step partitioning meta data on the source step
                                    			//
                                    			if (!sourceStepPartitioningMeta.isPartitioned() || !sourceStepPartitioningMeta.equals(targetStepPartitioningMeta) ) {
                                    				
                                    				// Not partitioned means the target is partitioned.
                                    				// Set the target partitioning on the source...
                                    				
                                    				// Set the correct partitioning schema for the source step.
                                    				//
                                    				// Set the target partitioning schema for the target step (slave)
                                    				//
                                    				StepPartitioningMeta stepPartitioningMeta = targetStepPartitioningMeta.clone();
                                    				PartitionSchema partitionSchema = stepPartitioningMeta.getPartitionSchema();
                                    				partitionSchema.setName( createTargetPartitionSchemaName(partitionSchema.getName())) ;
                                    				
                                    				if (partitionSchema.isDynamicallyDefined()) {
                                    					// Expand the cluster definition to: nrOfSlaves*nrOfPartitionsPerSlave...
                                    					//
                                    					partitionSchema.expandPartitionsDynamically(referenceClusterSchema.findNrSlaves(), originalTransformation);
                                    				}
                                    				
                                    				source.setTargetStepPartitioningMeta( stepPartitioningMeta );
                                					slave.addOrReplacePartitionSchema(partitionSchema);
                                    			}
                                    		}
                                    	}
                                    }
                                }
                            }
                        }
                    }
                }
                
                if (nrPreviousSteps==0)
                {
                    if (referenceClusterSchema==null)
                    {
                        // Not clustered, simply add the step.
                        if (master.findStep(referenceStep.getName())==null) {
                        	master.addStep((StepMeta) referenceStep.clone());
                        }
                    }
                    else
                    {
                        int nrSlaves = referenceClusterSchema.getSlaveServers().size();
                        for (int s=0;s<nrSlaves;s++)
                        {
                            SlaveServer slaveServer = (SlaveServer) referenceClusterSchema.getSlaveServers().get(s);

                            if (!slaveServer.isMaster())
                            {
                                // SLAVE
                                TransMeta slave = getSlaveTransformation(referenceClusterSchema, slaveServer);
                                if (slave.findStep(referenceStep.getName())==null)
                                {
                                	addSlaveCopy(slave, referenceStep, slaveServer);
                                }
                            }
                        }
                    }
                }
            }
             
            // This block of code, this loop, checks the informational steps. (yellow hops).
            // 
            for (int i=0;i<referenceSteps.length;i++)
            {
                StepMeta originalStep = referenceSteps[i];
                ClusterSchema originalClusterSchema = originalStep.getClusterSchema(); 

                // Also take care of the info steps...
                // For example: StreamLookup, Table Input, etc.
                //
                StepMeta infoSteps[] = originalTransformation.getInfoStep(originalStep);
                for (int p=0;infoSteps!=null && p<infoSteps.length;p++)
                {
                    StepMeta infoStep = infoSteps[p];
                    
                    ClusterSchema infoClusterSchema = infoStep.getClusterSchema();
                    if (originalClusterSchema==null)
                    {
                        if (infoClusterSchema==null)
                        {
                            // No clustering involved here: just add a link between the reference step and the infostep
                            //
                            StepMeta target = master.findStep(originalStep.getName());
                            StepMeta source = master.findStep(infoStep.getName());
                            
                            // Add a hop too...
                            TransHopMeta masterHop = new TransHopMeta(source, target);
                            master.addTransHop(masterHop);
                        }
                        else
                        {
                            // reference step is NOT clustered
                            // Previous step is clustered
                            // --> We read from the slave server using socket readers.
                            //     We need a reader for each slave server in the cluster
                            //     On top of that we need to merge the data from all these steps using a dummy. (to make sure)
                        	//     That dummy needs to feed into Merge Join
                        	//
                            int nrSlaves = infoClusterSchema.getSlaveServers().size();
                            for (int s=0;s<nrSlaves;s++)
                            {
                                SlaveServer slaveServer = (SlaveServer) infoClusterSchema.getSlaveServers().get(s);
                                
                                if (!slaveServer.isMaster())
                                {
                                	////////////////////////////////////////////////////////////////////////////////////////////
                                    // On the SLAVES: add a socket writer...
                                	//
                                	TransMeta slave = getSlaveTransformation(infoClusterSchema, slaveServer);
                                    
                                	SocketWriterMeta socketWriterMeta = new SocketWriterMeta();
                                	int port = getPort(infoClusterSchema, slaveServer, infoStep.getName()+" - "+originalStep.getName());
                                	socketWriterMeta.setPort(""+port);
                                	socketWriterMeta.setBufferSize(infoClusterSchema.getSocketsBufferSize());
                                	socketWriterMeta.setFlushInterval(infoClusterSchema.getSocketsFlushInterval());
                                	socketWriterMeta.setCompressed(infoClusterSchema.isSocketsCompressed());
                                	
                                    StepMeta writerStep = new StepMeta(getWriterName( infoStep.getName(), infoClusterSchema, slaveServer ), socketWriterMeta);
                                    writerStep.setLocation(infoStep.getLocation().x+50, infoStep.getLocation().y + 50);
                                    writerStep.setDraw(true);
                                    
                                    slave.addStep(writerStep);
                                    
                                    // We also need to add a hop between infoStep and the new writer step
                                    //
                                    TransHopMeta slaveHop =new TransHopMeta(infoStep, writerStep);
                                    if (slave.findTransHop(slaveHop)==null) slave.addTransHop(slaveHop);
                                	
                                    ////////////////////////////////////////////////////////////////////////////////////////////
                                    // On the MASTER : add a socket reader and a dummy step to merge the data...
                                	//
                                    SocketReaderMeta socketReaderMeta = new SocketReaderMeta();
                                    socketReaderMeta.setPort(""+port);
                                    socketReaderMeta.setBufferSize(infoClusterSchema.getSocketsBufferSize());
                                    socketReaderMeta.setCompressed(infoClusterSchema.isSocketsCompressed());
                                    
                                    StepMeta readerStep = new StepMeta(getReaderName(infoStep.getName(), infoClusterSchema, slaveServer ), socketReaderMeta);
                                    readerStep.setLocation(infoStep.getLocation().x, infoStep.getLocation().y+(s*FANOUT*2)-(nrSlaves*FANOUT/2));
                                    readerStep.setDraw(true);
                                    
                                    master.addStep(readerStep);
                                    
                                    // Also add a single dummy step in the master that will merge the data from the slave transformations.
                                    // 
                                    String dummyName = infoStep.getName();
                                    StepMeta dummyStep = master.findStep(dummyName);
                                    if (dummyStep==null) 
                                    {
                                    	DummyTransMeta dummy = new DummyTransMeta();
                                    	dummyStep = new StepMeta(dummyName, dummy);
                                    	dummyStep.setLocation(infoStep.getLocation().x+(SPLIT/2), infoStep.getLocation().y);
                                    	dummyStep.setDraw(true);
                                    	dummyStep.setDescription("This step merges the data from the various data streams coming from the slave transformations.\nIt does that right before it hits the step that reads from a specific (info) step.");
                                    	
                                    	master.addStep(dummyStep);
                                    	
                                    	// Now we need a hop from the dummy merge step to the actual target step (original step)
                                    	//
                                    	StepMeta masterTargetStep = master.findStep(originalStep.getName());
                                    	TransHopMeta targetHop = new TransHopMeta(dummyStep, masterTargetStep);
                                    	master.addTransHop(targetHop);
                                    	
                                    	// Set the master target step as an info step... (use the cloned copy)
                                		//
                                        String infoStepNames[] = masterTargetStep.getStepMetaInterface().getInfoSteps();
                                        if (infoStepNames!=null)
                                        {
                                            StepMeta is[] = new StepMeta[infoStepNames.length];
                                            for (int n=0;n<infoStepNames.length;n++)
                                            {
                                                is[n] = slave.findStep(infoStepNames[n]); // OK, info steps moved to the slave steps
                                                if (infoStepNames[n].equals(infoStep.getName()))  
                                                {
                                                    // We want to replace this one with the reader step: that's where we source from now
                                                    infoSteps[n] = readerStep;
                                                }
                                            }
                                            masterTargetStep.getStepMetaInterface().setInfoSteps(infoSteps);
                                        }
                                    }
                                    
                                    // Add a hop between the reader step and the dummy
                                    //
                                    TransHopMeta mergeHop = new TransHopMeta(readerStep, dummyStep);
                                    if (master.findTransHop(mergeHop)==null) 
                                    {
                                    	master.addTransHop(mergeHop);
                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        if (infoClusterSchema==null)
                        {
                            // reference step is clustered
                            // info step is not clustered
                            // --> Add a socket writer for each slave server
                            //
                            int nrSlaves = originalClusterSchema.getSlaveServers().size();
                            for (int s=0;s<nrSlaves;s++)
                            {
                                SlaveServer slaveServer = (SlaveServer) originalClusterSchema.getSlaveServers().get(s);
                                
                                if (!slaveServer.isMaster())
                                {
                                    // MASTER
                                    SocketWriterMeta socketWriterMeta = new SocketWriterMeta();
                                    socketWriterMeta.setPort(""+getPort(originalClusterSchema, slaveServer, infoStep.getName()+" - "+originalStep.getName()));
                                    socketWriterMeta.setBufferSize(originalClusterSchema.getSocketsBufferSize());
                                    socketWriterMeta.setFlushInterval(originalClusterSchema.getSocketsFlushInterval());
                                    socketWriterMeta.setCompressed(originalClusterSchema.isSocketsCompressed());

                                    StepMeta writerStep = new StepMeta(getWriterName( originalStep.getName(), originalClusterSchema, slaveServer ), socketWriterMeta);
                                    writerStep.setLocation(originalStep.getLocation().x, originalStep.getLocation().y + (s*FANOUT*2)-(nrSlaves*FANOUT/2));
                                    writerStep.setDraw(originalStep.isDrawn());
            
                                    master.addStep(writerStep);
                                    
                                    // The previous step: add a hop to it.
                                    // It still has the original name as it is not clustered.
                                    // 
                                    StepMeta previous = master.findStep(infoStep.getName());
                                    if (previous==null)
                                    {
                                        previous = (StepMeta) infoStep.clone();
                                        master.addStep(previous); 
                                    }
                                    TransHopMeta masterHop = new TransHopMeta(previous, writerStep);
                                    master.addTransHop(masterHop);
                                    
                                    // SLAVE
                                    TransMeta slave = getSlaveTransformation(originalClusterSchema, slaveServer);
                                    
                                    SocketReaderMeta socketReaderMeta = new SocketReaderMeta();
                                    socketReaderMeta.setHostname(masterServer.getHostname());
                                    socketReaderMeta.setPort(""+getPort(originalClusterSchema, slaveServer, infoStep.getName()+" - "+originalStep.getName() ));
                                    socketReaderMeta.setBufferSize(originalClusterSchema.getSocketsBufferSize());
                                    socketReaderMeta.setCompressed(originalClusterSchema.isSocketsCompressed());
                                    
                                    StepMeta readerStep = new StepMeta(getReaderName(originalStep.getName(), originalClusterSchema, slaveServer ), socketReaderMeta);
                                    readerStep.setLocation(originalStep.getLocation().x-(SPLIT/2), originalStep.getLocation().y);
                                    readerStep.setDraw(originalStep.isDrawn());
                                    slave.addStep(readerStep);
                                    
                                    // also add the step itself.
                                    StepMeta slaveStep = slave.findStep(originalStep.getName());
                                    if (slaveStep==null)
                                    {
                                        slaveStep = addSlaveCopy(slave, originalStep, slaveServer);
                                    }
                                    
                                    // And a hop from the 
                                    TransHopMeta slaveHop = new TransHopMeta(readerStep, slaveStep);
                                    slave.addTransHop(slaveHop);
                                    
                                    // 
                                    // Now we have to explain to the slaveStep that it has to source from previous
                                    // 
                                    String infoStepNames[] = slaveStep.getStepMetaInterface().getInfoSteps();
                                    if (infoStepNames!=null)
                                    {
                                        StepMeta is[] = new StepMeta[infoStepNames.length];
                                        for (int n=0;n<infoStepNames.length;n++)
                                        {
                                            is[n] = slave.findStep(infoStepNames[n]); // OK, info steps moved to the slave steps
                                            if (infoStepNames[n].equals(infoStep.getName()))  
                                            {
                                                // We want to replace this one with the reader step: that's where we source from now
                                                infoSteps[n] = readerStep;
                                            }
                                        }
                                        slaveStep.getStepMetaInterface().setInfoSteps(infoSteps);
                                    }
                                }
                            }
                        }
                        else
                        {
                            /*
                            // reference step is clustered
                            // previous step is clustered
                            // --> Add reference step to the slave transformation(s)
                            //
                             */
                            // 
                            // Now we have to explain to the slaveStep that it has to source from previous
                            //
                            int nrSlaves = originalClusterSchema.getSlaveServers().size();
                            for (int s=0;s<nrSlaves;s++)
                            {
                                SlaveServer slaveServer = (SlaveServer) originalClusterSchema.getSlaveServers().get(s);
                                if (!slaveServer.isMaster())
                                {
                                    TransMeta slave = getSlaveTransformation(originalClusterSchema, slaveServer);
                                    StepMeta slaveStep = slave.findStep(originalStep.getName());
                                    String infoStepNames[] = slaveStep.getStepMetaInterface().getInfoSteps();
                                    if (infoStepNames!=null)
                                    {
                                        StepMeta is[] = new StepMeta[infoStepNames.length];
                                        for (int n=0;n<infoStepNames.length;n++)
                                        {
                                            is[n] = slave.findStep(infoStepNames[n]); // OK, info steps moved to the slave steps
                                            
                                            // Hang on... is there a hop to the previous step?
                                            if (slave.findTransHop(is[n], slaveStep)==null)
                                            {
                                                TransHopMeta infoHop = new TransHopMeta(is[n], slaveStep);
                                                slave.addTransHop(infoHop);
                                            }
                                        }
                                        slaveStep.getStepMetaInterface().setInfoSteps(infoSteps);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Also add the original list of partition schemas to the slave step copy partition distribution...
            //
            slaveStepCopyPartitionDistribution.setOriginalPartitionSchemas(originalTransformation.getPartitionSchemas());
            for (SlaveStepCopyPartitionDistribution.SlaveStepCopy slaveStepCopy : slaveStepCopyPartitionDistribution.getDistribution().keySet()) {
            	int partition = slaveStepCopyPartitionDistribution.getPartition(slaveStepCopy.getSlaveServerName(), slaveStepCopy.getPartitionSchemaName(), slaveStepCopy.getStepCopyNr());
            	System.out.println("slave step copy: slaveServer="+slaveStepCopy.getSlaveServerName()+", partition schema="+slaveStepCopy.getPartitionSchemaName()+", copynr="+slaveStepCopy.getStepCopyNr()+" ---> partition="+partition);
            }
            
            // Get a hold of all the slave transformations & the master...
            // Assign the SAME slave-step-copy-partition distribution to all of them.
            // That way the slave transformations can figure out where to send data.
            // 
            // Also clear the changed flag.
            //
            for (TransMeta transMeta : slaveTransMap.values()) {
            	transMeta.setSlaveStepCopyPartitionDistribution(slaveStepCopyPartitionDistribution);
            	transMeta.clearChanged();
            }
        	master.setSlaveStepCopyPartitionDistribution(slaveStepCopyPartitionDistribution);
            master.clearChanged();
            
            // We're absolutely done here...
        }
        catch(Exception e)
        {
            throw new KettleException("Unexpected problem while generating master transformation", e);
        }
    }
    
    private int getSlaveServerNumber(ClusterSchema clusterSchema, SlaveServer slaveServer) {
    	int index = 0;
    	for (SlaveServer check : clusterSchema.getSlaveServers()) {
    		if (!check.isMaster()) {
    			if (check.equals(slaveServer)) {
    				return index;
    			}
        		index++;
    		}
    	}
    	return -1;
    }

    /**
     * Create a copy of a step from the original transformation for use in the a slave transformation.
     * If the step is partitioned, the partitioning will be changed to "schemaName (slave)" 
     * 
     * @param stepMeta The step to copy / clone.
     * @return a copy of the specified step for use in a slave transformation.
     */
    private StepMeta addSlaveCopy(TransMeta transMeta, StepMeta stepMeta, SlaveServer slaveServer) {
		StepMeta copy = (StepMeta) stepMeta.clone();
		if (copy.isPartitioned()) {
			StepPartitioningMeta stepPartitioningMeta = copy.getStepPartitioningMeta();
			PartitionSchema partitionSchema = stepPartitioningMeta.getPartitionSchema();
			String slavePartitionSchemaName = createSlavePartitionSchemaName(partitionSchema.getName());
			PartitionSchema slaveSchema = transMeta.findPartitionSchema(slavePartitionSchemaName);
			if (slaveSchema!=null) {
				stepPartitioningMeta.setPartitionSchema(slaveSchema);
			}
			// Always just start a single copy on the slave server...
			// Otherwise the confusion w.r.t. to partitioning & re-partitioning would be complete.
			//
			copy.setCopies(1);
		}

		// Remove the clustering information on the slave transformation step
		// We don't need it anymore, it only confuses.
		//
		copy.setClusterSchema(null); 
		
		transMeta.addStep(copy);
		return copy;
	}

	private void findUsedOriginalSteps()
    {
        List<StepMeta> transHopSteps = originalTransformation.getTransHopSteps(false);
        referenceSteps = transHopSteps.toArray(new StepMeta[transHopSteps.size()]);
    }
    
    /**
     * We want to devide the available partitions over the slaves.
     * Let's create a hashtable that contains the partition schema's
     * Since we can only use a single cluster, we can divide them all over a single set of slave servers. 
     * 
     * @throws KettleException
     */
    private void generateSlavePartitionSchemas() throws KettleException
    {
        slaveServerPartitionsMap = new Hashtable<SlaveServer,Map<PartitionSchema,List<String>>>();
        
        for (int i=0;i<referenceSteps.length;i++)
        {
            StepMeta stepMeta = referenceSteps[i];
            StepPartitioningMeta stepPartitioningMeta = stepMeta.getStepPartitioningMeta();
            
            if (stepPartitioningMeta==null) continue;
            if (stepPartitioningMeta.getMethodType()==StepPartitioningMeta.PARTITIONING_METHOD_NONE) continue;
            
            ClusterSchema clusterSchema = stepMeta.getClusterSchema();
            if (clusterSchema==null) continue;
            
            // Make a copy of the partition schema because we might change the object.
            // Let's not alter the original transformation.
            // The match is done on name, and the name is preserved in this case, so it should be safe to do so.
            // Also, all cloned steps re-match with the cloned schema name afterwards...
            //
            PartitionSchema partitionSchema = (PartitionSchema) stepPartitioningMeta.getPartitionSchema().clone();

            int nrSlaves = clusterSchema.findNrSlaves();
            if (nrSlaves==0) continue; // no slaves: ignore this situation too

            // Change the partitioning layout dynamically if the user requested this...
            //
            if (partitionSchema.isDynamicallyDefined())
            {
            	partitionSchema.expandPartitionsDynamically(nrSlaves, originalTransformation);
            }
            
            int nrPartitions = partitionSchema.getPartitionIDs().size();
            
            if (nrPartitions<nrSlaves)
            {
                throw new KettleException("It doesn't make sense to have a partitioned, clustered step with less partitions ("+nrPartitions+") than that there are slave servers ("+nrSlaves+")");
            }

            int slaveServerNr=0;
            for (int p=0;p<nrPartitions;p++)
            {
                String partitionId = partitionSchema.getPartitionIDs().get(p);
                
                SlaveServer slaveServer = clusterSchema.getSlaveServers().get(slaveServerNr);
                
                // Skip the master...
                //
                if (slaveServer.isMaster())
                {
                    slaveServerNr++;
                    if (slaveServerNr>=clusterSchema.getSlaveServers().size()) slaveServerNr=0; // re-start
                    slaveServer = (SlaveServer) clusterSchema.getSlaveServers().get(slaveServerNr);
                }

                Map<PartitionSchema,List<String>> schemaPartitionsMap = slaveServerPartitionsMap.get(slaveServer);
                if (schemaPartitionsMap==null)
                {
                    // Add the schema-partitions map to the the slave server
                	//
                    schemaPartitionsMap = new HashMap<PartitionSchema,List<String>>();
                    slaveServerPartitionsMap.put(slaveServer, schemaPartitionsMap);
                }
                
                // See if we find a list of partitions
                //
                List<String> partitions = schemaPartitionsMap.get(partitionSchema);
                if (partitions==null)
                {
                    partitions = new ArrayList<String>();
                    schemaPartitionsMap.put(partitionSchema, partitions);
                }
                
                // Add the partition ID to the appropriate list
                //
                if (partitions.indexOf(partitionId)<0)
                {
                    partitions.add(partitionId);
                }

                // Switch to next slave.
                slaveServerNr++;
                if (slaveServerNr>=clusterSchema.getSlaveServers().size()) slaveServerNr=0; // re-start
            }
        }
        // System.out.println("We have "+(slaveServerPartitionsMap.size())+" entries in the slave server partitions map");
    }
 
}
