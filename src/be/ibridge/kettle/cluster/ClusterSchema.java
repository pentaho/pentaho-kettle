package be.ibridge.kettle.cluster;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import be.ibridge.kettle.core.ChangedFlag;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.SharedObjectInterface;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.repository.Repository;

/**
 * A cluster schema combines a list of slave servers so that they can be set altogether. 
 * It (can) also contain a number of performance tuning options regarding this cluster.
 * For example options regarding communications with the master node of the nodes themselves come to mind.
 * 
 * @author Matt
 * @since 17-nov-2006
 */
public class ClusterSchema extends ChangedFlag implements Cloneable, SharedObjectInterface
{
    public static final String XML_TAG = "clusterschema";
    
    /** the name of the cluster schema */
    private String name;
    
    /** The list of slave servers we can address */
    private List slaveServers;
    
    /** The data socket port where we start numbering.  The upper limit is the number of remote socket connections. */
    private String basePort;

    private boolean shared;

    /** Size of the buffer for the created socket reader/writers */
    private String socketsBufferSize;
    
    /** Flush outputstreams every X rows */
    private String socketsFlushInterval;
    
    /** flag to compress data over the sockets or not */
    private boolean socketsCompressed;
    
    
    
    public ClusterSchema()
    {
        slaveServers = new ArrayList();
        socketsBufferSize = "2000";
        socketsFlushInterval = "5000";
        socketsCompressed = true;
    }
    
    /**
     * @param name
     * @param slaveServers
     */
    public ClusterSchema(String name, List slaveServers)
    {
        this.name = name;
        this.slaveServers = slaveServers;
    }

    public Object clone() 
    {
        ClusterSchema clusterSchema = new ClusterSchema();
        clusterSchema.setName(name);
        clusterSchema.setBasePort(basePort);
        clusterSchema.setSocketsBufferSize(socketsBufferSize);
        clusterSchema.setSocketsCompressed(socketsCompressed);
        clusterSchema.setSocketsFlushInterval(socketsFlushInterval);
        
        for (int i=0;i<slaveServers.size();i++)
        {
            SlaveServer slaveServer = (SlaveServer) slaveServers.get(i);
            clusterSchema.getSlaveServers().add(slaveServer.clone());
        }
        
        return clusterSchema;
    }
    
    public String toString()
    {
        return name;
    }
    
    public boolean equals(Object obj)
    {
        if (obj==null) return false;
        return name.equals(((ClusterSchema)obj).name);
    }
    
    public int hashCode()
    {
        return name.hashCode();
    }
    
    public String getXML()
    {
        StringBuffer xml = new StringBuffer();
        
        xml.append("        <"+XML_TAG+">"+Const.CR);
        
        xml.append("          "+XMLHandler.addTagValue("name", name));
        xml.append("          "+XMLHandler.addTagValue("base_port", basePort));
        xml.append("          "+XMLHandler.addTagValue("sockets_buffer_size", socketsBufferSize));
        xml.append("          "+XMLHandler.addTagValue("sockets_flush_interval", socketsFlushInterval));
        xml.append("          "+XMLHandler.addTagValue("sockets_compressed", socketsCompressed));
        
        xml.append("          <slaveservers>"+Const.CR);
        for (int i=0;i<slaveServers.size();i++)
        {
            SlaveServer slaveServer = (SlaveServer) slaveServers.get(i);
            xml.append("            "+slaveServer.getXML()).append(Const.CR);
        }
        xml.append("          </slaveservers>"+Const.CR);
        xml.append("        </"+XML_TAG+">"+Const.CR);
        return xml.toString();
    }
    
    public ClusterSchema(Node clusterSchemaNode)
    {
        this();
        
        name = XMLHandler.getTagValue(clusterSchemaNode, "name");
        basePort = XMLHandler.getTagValue(clusterSchemaNode, "base_port");
        socketsBufferSize = XMLHandler.getTagValue(clusterSchemaNode, "sockets_buffer_size");
        socketsFlushInterval = XMLHandler.getTagValue(clusterSchemaNode, "sockets_flush_interval");
        socketsCompressed = "Y".equalsIgnoreCase(  XMLHandler.getTagValue(clusterSchemaNode, "sockets_compressed") );
        
        Node slavesNode = XMLHandler.getSubNode(clusterSchemaNode, "slaveservers");
        int nrSlaves = XMLHandler.countNodes(slavesNode, "slaveserver");
        for (int i=0;i<nrSlaves;i++)
        {
            Node slaveNode = XMLHandler.getSubNodeByNr(slavesNode, "slaveserver", i);
            SlaveServer slaveServer = new SlaveServer(slaveNode);
            slaveServers.add(slaveServer);
        }
    }
    
    public void saveRep(Repository rep, long id_transformation) throws KettleDatabaseException
    {
        long id_cluster_schema = rep.insertClusterSchema(id_transformation, name, basePort, socketsBufferSize, socketsFlushInterval, socketsCompressed);
        
        for (int i=0;i<slaveServers.size();i++)
        {
            SlaveServer slaveServer = (SlaveServer) slaveServers.get(i);
            slaveServer.saveRep(rep, id_transformation, id_cluster_schema);
        }
    }
    
    public ClusterSchema(Repository rep, long id_cluster_schema) throws KettleDatabaseException
    {
        this();
        
        Row row = rep.getClusterSchema(id_cluster_schema);
        
        name = row.getString("SCHEMA_NAME", null);
        basePort = row.getString("BASE_PORT", null);
        socketsBufferSize = row.getString("SOCKETS_BUFFER_SIZE", null);
        socketsFlushInterval = row.getString("SOCKETS_FLUSH_INTERVAL", null);
        socketsCompressed = row.getBoolean("SOCKETS_COMPRESSED", true);
        
        long[] pids = rep.getSlaveServerIDs(id_cluster_schema);
        for (int i=0;i<pids.length;i++)
        {
            SlaveServer slaveServer = new SlaveServer(rep, pids[i]);
            slaveServers.add(slaveServer);
        }
    }
    
    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the slaveServers
     */
    public List getSlaveServers()
    {
        return slaveServers;
    }

    /**
     * @param slaveServers the slaveServers to set
     */
    public void setSlaveServers(List slaveServers)
    {
        this.slaveServers = slaveServers;
    }    
    
    /**
     * @return The slave server strings from this cluster schema
     */
    public String[] getSlaveServerStrings()
    {
        String[] strings = new String[slaveServers.size()];
        for (int i=0;i<strings.length;i++)
        {
            strings[i] = ((SlaveServer)slaveServers.get(i)).toString();
        }
        return strings;
    }

    /**
     * @return the shared
     */
    public boolean isShared()
    {
        return shared;
    }

    /**
     * @param shared the shared to set
     */
    public void setShared(boolean shared)
    {
        this.shared = shared;
    }

    /**
     * @return the basePort
     */
    public String getBasePort()
    {
        return basePort;
    }

    /**
     * @param basePort the basePort to set
     */
    public void setBasePort(String basePort)
    {
        this.basePort = basePort;
    }
    
    public SlaveServer findMaster() throws KettleException
    {
        for (int i=0;i<slaveServers.size();i++)
        {
            SlaveServer slaveServer = (SlaveServer) slaveServers.get(i);
            if (slaveServer.isMaster()) return slaveServer;
        }
        if (slaveServers.size()>0)
        {
            throw new KettleException("No master server defined in cluster schema ["+name+"]");
        }
        throw new KettleException("No slave server(s) defined in cluster schema ["+name+"]");
    }
    
    /**
     * @return The number of slave servers, excluding the master server
     */
    public int findNrSlaves()
    {
        int nr=0;
        for (int i=0;i<slaveServers.size();i++)
        {
            SlaveServer slaveServer = (SlaveServer) slaveServers.get(i);
            if (!slaveServer.isMaster()) nr++;
        }
        return nr;
    }

    /**
     * @return the socketFlushInterval
     */
    public String getSocketsFlushInterval()
    {
        return socketsFlushInterval;
    }

    /**
     * @param socketFlushInterval the socketFlushInterval to set
     */
    public void setSocketsFlushInterval(String socketFlushInterval)
    {
        this.socketsFlushInterval = socketFlushInterval;
    }

    /**
     * @return the socketsBufferSize
     */
    public String getSocketsBufferSize()
    {
        return socketsBufferSize;
    }

    /**
     * @param socketsBufferSize the socketsBufferSize to set
     */
    public void setSocketsBufferSize(String socketsBufferSize)
    {
        this.socketsBufferSize = socketsBufferSize;
    }

    /**
     * @return the socketsCompressed
     */
    public boolean isSocketsCompressed()
    {
        return socketsCompressed;
    }

    /**
     * @param socketsCompressed the socketsCompressed to set
     */
    public void setSocketsCompressed(boolean socketsCompressed)
    {
        this.socketsCompressed = socketsCompressed;
    }

    public SlaveServer findSlaveServer(String slaveServerString)
    {
        for (int i=0;i<slaveServers.size();i++)
        {
            SlaveServer slaveServer = (SlaveServer) slaveServers.get(i);
            if (slaveServer.toString().equalsIgnoreCase(slaveServerString)) return slaveServer;
        }
        return null;
    }
}
