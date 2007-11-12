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
package org.pentaho.di.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.changed.ChangedFlag;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.SharedObjectInterface;
import org.w3c.dom.Node;

/**
 * A cluster schema combines a list of slave servers so that they can be set altogether. 
 * It (can) also contain a number of performance tuning options regarding this cluster.
 * For example options regarding communications with the master node of the nodes themselves come to mind.
 * 
 * @author Matt
 * @since 17-nov-2006
 */
public class ClusterSchema extends ChangedFlag implements Cloneable, SharedObjectInterface, VariableSpace
{
    public static final String XML_TAG = "clusterschema"; //$NON-NLS-1$
    
    /** the name of the cluster schema */
    private String name;
    
    /** The list of slave servers we can address */
    private List<SlaveServer> slaveServers;
    
    /** The data socket port where we start numbering.  The upper limit is the number of remote socket connections. */
    private String basePort;

    private boolean shared;

    /** Size of the buffer for the created socket reader/writers */
    private String socketsBufferSize;
    
    /** Flush outputstreams every X rows */
    private String socketsFlushInterval;
    
    /** flag to compress data over the sockets or not */
    private boolean socketsCompressed;
    
    private VariableSpace variables = new Variables();
    
    private long id;
    
    public ClusterSchema()
    {
        id=-1L;
        slaveServers = new ArrayList<SlaveServer>();
        socketsBufferSize = "2000"; //$NON-NLS-1$
        socketsFlushInterval = "5000"; //$NON-NLS-1$
        socketsCompressed = true;
        basePort = "40000"; //$NON-NLS-1$
    }
    
    /**
     * @param name
     * @param slaveServers
     */
    public ClusterSchema(String name, List<SlaveServer> slaveServers)
    {
        this.name = name;
        this.slaveServers = slaveServers;
    }

    public Object clone() 
    {
        ClusterSchema clusterSchema = new ClusterSchema();
        clusterSchema.replaceMeta(this);
        return clusterSchema;
    }
    

    public void replaceMeta(ClusterSchema clusterSchema)
    {
        this.name = clusterSchema.name;
        this.basePort = clusterSchema.basePort;
        this.socketsBufferSize = clusterSchema.socketsBufferSize;
        this.socketsCompressed = clusterSchema.socketsCompressed;
        this.socketsFlushInterval = clusterSchema.socketsFlushInterval;
        
        this.slaveServers.clear();
        this.slaveServers.addAll(clusterSchema.slaveServers); // no clone() of the slave server please!
        
        this.shared = clusterSchema.shared;
        this.id = clusterSchema.id;
        this.setChanged(true);
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
        StringBuffer xml = new StringBuffer(500);
        
        xml.append("        <").append(XML_TAG).append(">").append(Const.CR); //$NON-NLS-1$ //$NON-NLS-2$
        
        xml.append("          ").append(XMLHandler.addTagValue("name", name)); //$NON-NLS-1$ //$NON-NLS-2$
        xml.append("          ").append(XMLHandler.addTagValue("base_port", basePort)); //$NON-NLS-1$ //$NON-NLS-2$
        xml.append("          ").append(XMLHandler.addTagValue("sockets_buffer_size", socketsBufferSize)); //$NON-NLS-1$ //$NON-NLS-2$
        xml.append("          ").append(XMLHandler.addTagValue("sockets_flush_interval", socketsFlushInterval)); //$NON-NLS-1$ //$NON-NLS-2$
        xml.append("          ").append(XMLHandler.addTagValue("sockets_compressed", socketsCompressed)); //$NON-NLS-1$ //$NON-NLS-2$
        
        xml.append("          <slaveservers>").append(Const.CR); //$NON-NLS-1$
        for (int i=0;i<slaveServers.size();i++)
        {
            SlaveServer slaveServer = slaveServers.get(i);
            xml.append("            ").append(XMLHandler.addTagValue("name", slaveServer.getName())); //$NON-NLS-1$ //$NON-NLS-2$
        }
        xml.append("          </slaveservers>").append(Const.CR); //$NON-NLS-1$
        xml.append("        </").append(XML_TAG).append(">").append(Const.CR); //$NON-NLS-1$ //$NON-NLS-2$
        return xml.toString();
    }
    
    public ClusterSchema(Node clusterSchemaNode, List<SlaveServer> referenceSlaveServers)
    {
        this();
        
        name = XMLHandler.getTagValue(clusterSchemaNode, "name"); //$NON-NLS-1$
        basePort = XMLHandler.getTagValue(clusterSchemaNode, "base_port"); //$NON-NLS-1$
        socketsBufferSize = XMLHandler.getTagValue(clusterSchemaNode, "sockets_buffer_size"); //$NON-NLS-1$
        socketsFlushInterval = XMLHandler.getTagValue(clusterSchemaNode, "sockets_flush_interval"); //$NON-NLS-1$
        socketsCompressed = "Y".equalsIgnoreCase(  XMLHandler.getTagValue(clusterSchemaNode, "sockets_compressed") ); //$NON-NLS-1$ //$NON-NLS-2$
        
        Node slavesNode = XMLHandler.getSubNode(clusterSchemaNode, "slaveservers"); //$NON-NLS-1$
        int nrSlaves = XMLHandler.countNodes(slavesNode, "name"); //$NON-NLS-1$
        for (int i=0;i<nrSlaves;i++)
        {
            Node serverNode = XMLHandler.getSubNodeByNr(slavesNode, "name", i); //$NON-NLS-1$
            String serverName = XMLHandler.getNodeValue(serverNode);
            SlaveServer slaveServer = SlaveServer.findSlaveServer(referenceSlaveServers, serverName);
            if (slaveServer!=null) 
            {
                slaveServers.add(slaveServer);
            }
        }
    }

    public void saveRep(Repository rep) throws KettleException
    {
        saveRep(rep, -1L, false);
    }

    public void saveRep(Repository rep, long id_transformation, boolean isUsedByTransformation) throws KettleException
    {
        setId(rep.getClusterID(name));
        if (getId()<0)
        {
            // Save the cluster
            setId(rep.insertCluster(this));
        }
        else
        {
            rep.delClusterSlaves(getId());
        }
        
        // Also save the used slave server references.
        for (int i=0;i<slaveServers.size();i++)
        {
            SlaveServer slaveServer = slaveServers.get(i);
            if (slaveServer.getId()<0) // oops, not yet saved!
            {
                slaveServer.saveRep(rep, id_transformation, isUsedByTransformation);
            }
            rep.insertClusterSlave(this, slaveServer);
        }
        
        // Save a link to the transformation to keep track of the use of this partition schema
        // Only save it if it's really used by the transformation
        if (isUsedByTransformation)
        {
            rep.insertTransformationCluster(id_transformation, getId());
        }
    }
    
    public ClusterSchema(Repository rep, long id_cluster_schema, List<SlaveServer> slaveServers) throws KettleException
    {
        this();
        
        setId(id_cluster_schema);
        
        RowMetaAndData row = rep.getClusterSchema(id_cluster_schema);
        
        name = row.getString("NAME", null); //$NON-NLS-1$
        basePort = row.getString("BASE_PORT", null); //$NON-NLS-1$
        socketsBufferSize = row.getString("SOCKETS_BUFFER_SIZE", null); //$NON-NLS-1$
        socketsFlushInterval = row.getString("SOCKETS_FLUSH_INTERVAL", null); //$NON-NLS-1$
        socketsCompressed = row.getBoolean("SOCKETS_COMPRESSED", true); //$NON-NLS-1$
        
        long[] pids = rep.getSlaveIDs(id_cluster_schema);
        for (int i=0;i<pids.length;i++)
        {
            SlaveServer slaveServer = new SlaveServer(rep, pids[i]);
            SlaveServer reference = SlaveServer.findSlaveServer(slaveServers, slaveServer.getName());
            if (reference!=null) 
                this.slaveServers.add(reference);
            else 
                this.slaveServers.add(slaveServer);
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
    public List<SlaveServer> getSlaveServers()
    {
        return slaveServers;
    }

    /**
     * @param slaveServers the slaveServers to set
     */
    public void setSlaveServers(List<SlaveServer> slaveServers)
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
            strings[i] = (slaveServers.get(i)).toString();
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
            SlaveServer slaveServer = slaveServers.get(i);
            if (slaveServer.isMaster()) return slaveServer;
        }
        if (slaveServers.size()>0)
        {
            throw new KettleException(Messages.getString("ClusterSchema.NoMasterServerDefined", name)); //$NON-NLS-1$
        }
        throw new KettleException(Messages.getString("ClusterSchema.NoSlaveServerDefined", name)); //$NON-NLS-1$
    }
    
    /**
     * @return The number of slave servers, excluding the master server
     */
    public int findNrSlaves()
    {
        int nr=0;
        for (int i=0;i<slaveServers.size();i++)
        {
            SlaveServer slaveServer = slaveServers.get(i);
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

    public SlaveServer findSlaveServer(String slaveServerName)
    {
        for (int i=0;i<slaveServers.size();i++)
        {
            SlaveServer slaveServer = slaveServers.get(i);
            if (slaveServer.getName().equalsIgnoreCase(slaveServerName)) return slaveServer;
        }
        return null;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }
    
	public void copyVariablesFrom(VariableSpace space) 
	{
		variables.copyVariablesFrom(space);		
	}

	public String environmentSubstitute(String aString) 
	{
		return variables.environmentSubstitute(aString);
	}

	public String[] environmentSubstitute(String aString[]) 
	{
		return variables.environmentSubstitute(aString);
	}		

	public VariableSpace getParentVariableSpace() 
	{
		return variables.getParentVariableSpace();
	}
	
	public void setParentVariableSpace(VariableSpace parent) 
	{
		variables.setParentVariableSpace(parent);
	}

	public String getVariable(String variableName, String defaultValue) 
	{
		return variables.getVariable(variableName, defaultValue);
	}

	public String getVariable(String variableName) 
	{
		return variables.getVariable(variableName);
	}
	
	public boolean getBooleanValueOfVariable(String variableName, boolean defaultValue) {
		if (!Const.isEmpty(variableName))
		{
			String value = environmentSubstitute(variableName);
			if (!Const.isEmpty(value))
			{
				return ValueMeta.convertStringToBoolean(value);
			}
		}
		return defaultValue;
	}

	public void initializeVariablesFrom(VariableSpace parent) 
	{
		variables.initializeVariablesFrom(parent);	
	}

	public String[] listVariables() 
	{
		return variables.listVariables();
	}

	public void setVariable(String variableName, String variableValue) 
	{
		variables.setVariable(variableName, variableValue);		
	}

	public void shareVariablesWith(VariableSpace space) 
	{
		variables = space;		
	}

	public void injectVariables(Map<String,String> prop) 
	{
		variables.injectVariables(prop);		
	}    
}