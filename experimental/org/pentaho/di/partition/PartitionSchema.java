package org.pentaho.di.partition;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.ChangedFlag;
import org.pentaho.di.core.Const;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.exception.KettleException;

/**
 * A partition schema allow you to partition a step according into a number of partitions that run independendly.
 * It allows us to "map" 
 * 
 * @author Matt
 */
public class PartitionSchema extends ChangedFlag implements Cloneable, SharedObjectInterface
{
    public static final String XML_TAG = "partitionschema";

    private String   name;

    private String[] partitionIDs;
    private boolean shared;
    
    private long id;

    public PartitionSchema()
    {
        partitionIDs=new String[] {};
    }
    
    /**
     * @param name
     * @param partitionIDs
     */
    public PartitionSchema(String name, String[] partitionIDs)
    {
        this.name = name;
        this.partitionIDs = partitionIDs;
    }

    public Object clone()
    {
        PartitionSchema partitionSchema = new PartitionSchema();
        partitionSchema.replaceMeta(this);
        partitionSchema.setId(-1L);
        return partitionSchema;
    }

    public void replaceMeta(PartitionSchema partitionSchema)
    {
        this.name = partitionSchema.name;
        this.partitionIDs = partitionSchema.partitionIDs;
        
        // this.shared = partitionSchema.shared;
        this.setId(partitionSchema.id);
        this.setChanged(true);
    }
    
    public String toString()
    {
        return name;
    }
    
    public boolean equals(Object obj)
    {
        if (obj==null) return false;
        return name.equals(((PartitionSchema)obj).name);
    }

    public int hashCode()
    {
        return name.hashCode();
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
     * @return the partitionIDs
     */
    public String[] getPartitionIDs()
    {
        return partitionIDs;
    }

    /**
     * @param partitionIDs the partitionIDs to set
     */
    public void setPartitionIDs(String[] partitionIDs)
    {
        this.partitionIDs = partitionIDs;
    }

    public String getXML()
    {
        StringBuffer xml = new StringBuffer(200);
        
        xml.append("        <").append(XML_TAG).append(">").append(Const.CR);
        xml.append("          ").append(XMLHandler.addTagValue("name", name));
        for (int i=0;i<partitionIDs.length;i++)
        {
            xml.append("          <partition>");
            xml.append("            ").append(XMLHandler.addTagValue("id", partitionIDs[i]));
            xml.append("          </partition>");
        }
        xml.append("        </").append(XML_TAG).append(">").append(Const.CR);
        return xml.toString();
    }
    
    public PartitionSchema(Node partitionSchemaNode)
    {
        name = XMLHandler.getTagValue(partitionSchemaNode, "name");
        
        int nrIDs = XMLHandler.countNodes(partitionSchemaNode, "partition");
        partitionIDs = new String[nrIDs];
        for (int i=0;i<nrIDs;i++)
        {
            Node partitionNode = XMLHandler.getSubNodeByNr(partitionSchemaNode, "partition", i);
            partitionIDs[i] = XMLHandler.getTagValue(partitionNode, "id");
        }
    }

    
    public void saveRep(Repository rep) throws KettleException
    {
        saveRep(rep, -1L, false);
    }

    public void saveRep(Repository rep, long id_transformation, boolean isUsedByTransformation) throws KettleException
    {
        // see if this partitioning schema is already in the repository...
        setId( rep.getPartitionSchemaID(name) );
        if (getId()<0)
        {
            setId(rep.insertPartitionSchema(this));
        }
        else
        {
            rep.updatePartitionSchema(this);
            rep.delPartitions(getId());
        }
        
        // Save the cluster-partition relationships
        //
        for (int i=0;i<partitionIDs.length;i++)
        {
            rep.insertPartition(getId(), partitionIDs[i]);
        }
        
        // Save a link to the transformation to keep track of the use of this partition schema
        // Otherwise, we shouldn't bother with this
        //
        if (isUsedByTransformation)
        {
            rep.insertTransformationPartitionSchema(id_transformation, getId());
        }
    }
    
    public PartitionSchema(Repository rep, long id_partition_schema) throws KettleException
    {
        this();
        
        setId(id_partition_schema);
        
        RowMetaAndData row = rep.getPartitionSchema(id_partition_schema);
        
        name = row.getString("NAME", null);
        
        long[] pids = rep.getPartitionIDs(id_partition_schema);
        partitionIDs = new String[pids.length];
        for (int i=0;i<pids.length;i++)
        {
            partitionIDs[i] = rep.getPartition(pids[i]).getString("PARTITION_ID", null);
        }
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
     * @return the id
     */
    public long getId()
    {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id)
    {
        this.id = id;
    }
}