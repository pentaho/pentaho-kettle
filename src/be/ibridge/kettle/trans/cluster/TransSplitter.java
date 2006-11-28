package be.ibridge.kettle.trans.cluster;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import be.ibridge.kettle.cluster.ClusterSchema;
import be.ibridge.kettle.cluster.SlaveServer;
import be.ibridge.kettle.core.NotePadMeta;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.trans.TransHopMeta;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.socketreader.SocketReaderMeta;
import be.ibridge.kettle.trans.step.socketwriter.SocketWriterMeta;

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
    
    private TransMeta originalTransformation;
    private Map       serverTransMetaMap;
    private Map       clusterPortMap;
    private Map       clusterStepPortMap;
    private Map       slaveTransMap;
    private TransMeta master;

    public TransSplitter()
    {
        serverTransMetaMap = new Hashtable();
        clusterPortMap = new Hashtable();
        clusterStepPortMap = new Hashtable();
        
        slaveTransMap = new Hashtable();
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

    /**
     * @return the serverTransMetaMap
     */
    public Map getServerTransMetaMap()
    {
        return serverTransMetaMap;
    }

    /**
     * @param serverTransMetaMap the serverTransMetaMap to set
     */
    public void setServerTransMetaMap(Map serverTransMetaMap)
    {
        this.serverTransMetaMap = serverTransMetaMap;
    }
    
    public void generateMasterTransformation() throws KettleException
    {
        // Mixing clusters is not supported at the moment
        // Perform some basic checks on the cluster configuration.
        // 
        checkClusterConfiguration(); 
        
        try
        {
            master = getOriginalCopy(false, null, null);
            
            StepMeta[] originalSteps = originalTransformation.getStepsArray();
            for (int i=0;i<originalSteps.length;i++)
            {
                StepMeta originalStep = originalSteps[i];
                ClusterSchema originalClusterSchema = originalStep.getClusterSchema(); 

                StepMeta copy = (StepMeta) originalStep.clone();
                
                int nrPreviousSteps = originalTransformation.findNrPrevSteps(originalStep);
                for (int p=0;p<nrPreviousSteps;p++)
                {
                    StepMeta previousStep = originalTransformation.findPrevStep(originalStep, p);
                    ClusterSchema previousClusterSchema = previousStep.getClusterSchema();
                    
                    if (originalClusterSchema==null)
                    {
                        if (previousClusterSchema==null)
                        {
                            // No clustering involved here: just add the original step to the master
                            //
                            if (master.findStep(copy.getName())==null) master.addStep(copy);
                        }
                        else
                        {
                            // original step is NOT clustered
                            // Previous step is clustered
                            // --> We read from the slave server using socket readers.
                            //     We need a reader for each slave server in the cluster
                            //
                            
                            // Also add the original step
                            copy.setLocation(copy.getLocation().x+(SPLIT/2), copy.getLocation().y);
                            master.addStep(copy);

                            // Then add the readers
                            int nrSlaves = previousClusterSchema.getSlaveServers().size();
                            for (int s=0;s<nrSlaves;s++)
                            {
                                SlaveServer slaveServer = (SlaveServer) previousClusterSchema.getSlaveServers().get(s);
                                
                                // MASTER
                                SocketReaderMeta socketReaderMeta = new SocketReaderMeta();
                                socketReaderMeta.setHostname(slaveServer.getHostname());
                                socketReaderMeta.setPort(""+getPort(previousClusterSchema, slaveServer, originalStep.getName()));
                                
                                StepMeta readerStep = new StepMeta(getReaderName(originalStep.getName(), slaveServer), socketReaderMeta);
                                readerStep.setLocation(originalStep.getLocation().x-(SPLIT/2), originalStep.getLocation().y + (s*FANOUT*2)-(nrSlaves*FANOUT/2));
                                readerStep.setDraw(originalStep.isDrawn());
                                master.addStep(readerStep);
                                
                                TransHopMeta masterHop = new TransHopMeta(readerStep, copy);
                                master.addTransHop(masterHop);
                                
                                // SLAVE
                                TransMeta slave = getSlaveTransformation(previousClusterSchema, slaveServer);
                                SocketWriterMeta socketWriterMeta = new SocketWriterMeta();
                                socketWriterMeta.setPort(""+getPort(previousClusterSchema, slaveServer, originalStep.getName()));
                                
                                StepMeta writerStep = new StepMeta(getWriterName(originalStep.getName(), slaveServer), socketWriterMeta);
                                writerStep.setLocation(originalStep.getLocation().x, originalStep.getLocation().y);
                                writerStep.setDraw(originalStep.isDrawn());
                                slave.addStep(writerStep);
                                
                                // See if we can add a hop to the previous
                                StepMeta previous = slave.findStep(previousStep.getName());
                                if (previous==null)
                                {
                                    previous = (StepMeta) previousStep.clone();
                                    previous.setLocation(previousStep.getLocation().x+(SPLIT/2), previousStep.getLocation().y);

                                    slave.addStep(previous);
                                }
                                TransHopMeta slaveHop = new TransHopMeta(previous, writerStep);
                                slave.addTransHop(slaveHop);
                                    
                            }
                        }
                    }
                    else
                    {
                        if (previousClusterSchema==null)
                        {
                            // originalStep is clustered
                            // previousStep is not clustered
                            // --> Add a socket writer for each slave server
                            //
                            int nrSlaves = originalClusterSchema.getSlaveServers().size();
                            for (int s=0;s<nrSlaves;s++)
                            {
                                SlaveServer slaveServer = (SlaveServer) originalClusterSchema.getSlaveServers().get(s);
                                
                                // MASTER
                                SocketWriterMeta socketWriterMeta = new SocketWriterMeta();
                                socketWriterMeta.setPort(""+getPort(originalClusterSchema, slaveServer, originalStep.getName()));
                                
                                StepMeta writerStep = new StepMeta(getWriterName( originalStep.getName(), slaveServer ), socketWriterMeta);
                                writerStep.setLocation(originalStep.getLocation().x, originalStep.getLocation().y + (s*FANOUT*2)-(nrSlaves*FANOUT/2));
                                writerStep.setDraw(originalStep.isDrawn());
        
                                master.addStep(writerStep);
                                
                                // The previous step: add a hop to it.
                                // It still has the original name as it is not clustered.
                                // 
                                StepMeta previous = master.findStep(previousStep.getName());
                                if (previous==null)
                                {
                                    previous = (StepMeta) previousStep.clone();
                                    master.addStep(previous); 
                                }
                                TransHopMeta masterHop = new TransHopMeta(previous, writerStep);
                                master.addTransHop(masterHop);
                                
                                // SLAVE
                                TransMeta slave = getSlaveTransformation(originalClusterSchema, slaveServer);
                                
                                SocketReaderMeta socketReaderMeta = new SocketReaderMeta();
                                SlaveServer masterServer = originalClusterSchema.findMaster();
                                if (masterServer==null)
                                {
                                    throw new KettleException("No master server set for cluster schema ["+originalClusterSchema.getName()+"]");
                                }
                                socketReaderMeta.setHostname(masterServer.getHostname());
                                socketReaderMeta.setPort(""+getPort(originalClusterSchema, slaveServer, originalStep.getName()));
                                StepMeta readerStep = new StepMeta(getReaderName(originalStep.getName(), slaveServer ), socketReaderMeta);
                                readerStep.setLocation(originalStep.getLocation().x-(SPLIT/2), originalStep.getLocation().y);
                                readerStep.setDraw(originalStep.isDrawn());
                                slave.addStep(readerStep);
                                
                                // also add the step itself.
                                StepMeta slaveStep = slave.findStep(originalStep.getName());
                                if (slaveStep==null)
                                {
                                    copy.setLocation(originalStep.getLocation().x+(SPLIT/2), originalStep.getLocation().y);
                                    slaveStep = copy;
                                    slave.addStep(slaveStep);
                                }
                                
                                // And a hop from the 
                                TransHopMeta slaveHop = new TransHopMeta(readerStep, slaveStep);
                                slave.addTransHop(slaveHop);
                            }
                        }
                        else
                        {
                            // originalStep is clustered
                            // previousStep is clustered
                            // --> Add original step to the slave transformation(s)
                            //
                            int nrSlaves = originalClusterSchema.getSlaveServers().size();
                            for (int s=0;s<nrSlaves;s++)
                            {
                                SlaveServer slaveServer = (SlaveServer) originalClusterSchema.getSlaveServers().get(s);
                                
                                // SLAVE
                                TransMeta slave = getSlaveTransformation(originalClusterSchema, slaveServer);
                                slave.addStep(copy);
                            }
                        }
                    }
                }
                
                if (nrPreviousSteps==0)
                {
                    if (originalClusterSchema==null)
                    {
                        // Not clustered, simply add the step.
                        if (master.findStep(copy.getName())==null) master.addStep(copy);
                    }
                    else
                    {
                        int nrSlaves = originalClusterSchema.getSlaveServers().size();
                        for (int s=0;s<nrSlaves;s++)
                        {
                            SlaveServer slaveServer = (SlaveServer) originalClusterSchema.getSlaveServers().get(s);
                         
                            // SLAVE
                            TransMeta slave = getSlaveTransformation(originalClusterSchema, slaveServer);
                            slave.addStep(copy);
                        }
                    }
                }
            }
        }
        catch(Exception e)
        {
            throw new KettleException("Unexpected problem while generating master transformation", e);
        }
    }
    
    private void checkClusterConfiguration() throws KettleException
    {
        Map map = new Hashtable();
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
            }
        }
        if (map.size()==0)
        {
            throw new KettleException("No cluster schemas are being used.  As such it is not possible to split and cluster this. transformation.");
        }
        if (map.size()>1)
        {
            throw new KettleException("At this time we don't support the use of multiple cluster schemas in one and the same transformation.");
        }
    }

    public static final String getWriterName(String stepname, SlaveServer slaveServer)
    {
        return stepname+" <writer:"+slaveServer+">";
    }
    
    public static final String getReaderName(String stepname, SlaveServer slaveServer)
    {
        return stepname+" <reader:"+slaveServer+">";
    }

    public static final String getSlaveTransName(String transName, ClusterSchema clusterSchema, SlaveServer slaveServer)
    {
        return transName + " <"+clusterSchema+":"+slaveServer+">";
    }
    
    /**
     * Get the port for the given cluster schema, slave server and step
     * If a port was allocated, that is returned, otherwise a new one is allocated.
     * 
     * @param clusterSchema The cluster schema 
     * @param slaveServer The slave server
     * @param stepname the step name without reader/writer denotion.
     * 
     * @return the port to use for that step/slaveserver/cluster combination
     */
    private int getPort(ClusterSchema clusterSchema, SlaveServer slaveServer, String stepname)
    {
        String key = clusterSchema.getName()+" - "+slaveServer + " - " + stepname;
        int p;
        Integer port = (Integer) clusterStepPortMap.get(key);
        if (port==null)
        {
            p = getNextPort(clusterSchema);
            clusterStepPortMap.put(key, new Integer(p));
        }
        else
        {
            p = port.intValue();
        }
        
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
            p = Integer.parseInt( StringUtil.environmentSubstitute(clusterSchema.getBasePort())  );
        }
        else
        {
            p = port.intValue()+1;
        }
        clusterPortMap.put(clusterSchema, new Integer(p));
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
        String key = clusterSchema.getName()+" - "+slaveServer;
        TransMeta slave = (TransMeta) slaveTransMap.get(key);
        
        if (slave==null)
        {
            slave = getOriginalCopy(true, clusterSchema, slaveServer);
            slaveTransMap.put(key, slave);
        }
        return slave;
    }

    private TransMeta getOriginalCopy(boolean isSlaveTrans, ClusterSchema clusterSchema, SlaveServer slaveServer) throws KettleException
    {
        TransMeta transMeta = new TransMeta();
        if (isSlaveTrans)
        {
            transMeta.setName(getSlaveTransName(originalTransformation.getName(), clusterSchema, slaveServer));
            
            NotePadMeta slaveNote = new NotePadMeta("This is a generated slave transformation.\nIt will be run on slave server: "+slaveServer, 0, 0, -1, -1);
            transMeta.addNote(slaveNote);
        }
        else
        {
            transMeta.setName(originalTransformation.getName());

            NotePadMeta masterNote = new NotePadMeta("This is a generated master transformation.\nIt will be run on server: "+getMasterServer(), 0, 0, -1, -1);
            transMeta.addNote(masterNote);
        }
        transMeta.setClusterSchemas(originalTransformation.getClusterSchemas());
        transMeta.setPartitionSchemas(originalTransformation.getPartitionSchemas());
        transMeta.setDatabases(originalTransformation.getDatabases());

        return transMeta;
    }

    /**
     * @return the master
     */
    public TransMeta getMaster()
    {
        return master;
    }

    /**
     * @return the slaveTransMap
     */
    public Map getSlaveTransMap()
    {
        return slaveTransMap;
    }

    public TransMeta[] getSlaves()
    {
        Collection collection = slaveTransMap.values();
        return (TransMeta[]) collection.toArray(new TransMeta[collection.size()]);
    }
    
    public SlaveServer[] getSlaveTargets()
    {
        Set set = slaveTransMap.keySet();
        return (SlaveServer[]) set.toArray(new SlaveServer[set.size()]);
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
}
