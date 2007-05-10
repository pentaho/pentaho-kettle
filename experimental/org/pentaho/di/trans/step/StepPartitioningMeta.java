package org.pentaho.di.trans.step;

import java.util.List;

import org.pentaho.di.partition.PartitionSchema;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.XMLInterface;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.repository.Repository;

public class StepPartitioningMeta implements XMLInterface, Cloneable
{
    public static final int PARTITIONING_METHOD_NONE    = 0;
    public static final int PARTITIONING_METHOD_MOD     = 1;
    public static final int PARTITIONING_METHOD_MIRROR  = 2;
    
    public static final String[] methodCodes        = new String[] { "none", "Mod", "Mirror" };
    public static final String[] methodDescriptions = new String[] { "None", "Remainder of division", "Mirror to all partitions" };

    private int             method;
    private String          fieldName;

    private String          partitionSchemaName; // to allow delayed binding...
    private PartitionSchema partitionSchema;
    
    public StepPartitioningMeta()
    {
        method = PARTITIONING_METHOD_NONE;
    }
    
    /**
     * @param method
     * @param fieldName
     */
    public StepPartitioningMeta(int method, String fieldName, PartitionSchema partitionSchema)
    {
        super();
        this.method = method;
        this.fieldName = fieldName;
        this.partitionSchema = partitionSchema;
    }
    
    public Object clone()
    {
       return new StepPartitioningMeta(method, fieldName, partitionSchema!=null ? (PartitionSchema) partitionSchema.clone() : null);
    }

    /**
     * @return the partitionColumn
     */
    public String getFieldName()
    {
        return fieldName;
    }

    /**
     * @param fieldName the field name to set
     */
    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    /**
     * @return the partitioningMethod
     */
    public int getMethod()
    {
        return method;
    }

    /**
     * @param method the partitioning method to set
     */
    public void setMethod(int method)
    {
        this.method = method;
    }

    public String getXML()
    {
        StringBuffer xml = new StringBuffer(150);

        xml.append("         <partitioning>").append(Const.CR);
        xml.append("           ").append(XMLHandler.addTagValue("method",    getMethodCode()));
        xml.append("           ").append(XMLHandler.addTagValue("field_name", fieldName));
        xml.append("           ").append(XMLHandler.addTagValue("schema_name", partitionSchema!=null?partitionSchema.getName():""));
        xml.append("           </partitioning>").append(Const.CR);
        
        return xml.toString();
    }
    
    public StepPartitioningMeta(Node partitioningMethodNode)
    {
        method = getMethod( XMLHandler.getTagValue(partitioningMethodNode, "method") );
        fieldName = XMLHandler.getTagValue(partitioningMethodNode, "field_name");
        partitionSchemaName = XMLHandler.getTagValue(partitioningMethodNode, "schema_name");
    }
    
    public String getMethodCode()
    {
        return methodCodes[method];
    }

    public String getMethodDescription()
    {
        return methodDescriptions[method];
    }

    public static final int getMethod(String description)
    {
        for (int i=0;i<methodDescriptions.length;i++)
        {
            if (methodDescriptions[i].equalsIgnoreCase(description)) return i;
        }
        
        for (int i=0;i<methodCodes.length;i++)
        {
            if (methodCodes[i].equalsIgnoreCase(description)) return i;
        }
        return PARTITIONING_METHOD_NONE;
    }

    public boolean isPartitioned()
    {
        return method!=PARTITIONING_METHOD_NONE;
    }

    public int getPartitionNr(Long value, int nrPartitions)
    {
        int nr = 0;
        switch(method)
        {
        case PARTITIONING_METHOD_MOD:
            nr = (int)(value.longValue() % nrPartitions);
            break;
        }
        return nr;
    }

    /**
     * @return the partitionSchema
     */
    public PartitionSchema getPartitionSchema()
    {
        return partitionSchema;
    }

    /**
     * @param partitionSchema the partitionSchema to set
     */
    public void setPartitionSchema(PartitionSchema partitionSchema)
    {
        this.partitionSchema = partitionSchema;
    }
    
    /**
     * Set the partitioning schema after loading from XML or repository
     * @param partitionSchemas the list of partitioning schemas
     */
    public void setPartitionSchemaAfterLoading(List partitionSchemas)
    {
        partitionSchema=null; // sorry, not found!
        
        for (int i=0;i<partitionSchemas.size() && partitionSchema==null;i++)
        {
            PartitionSchema schema = (PartitionSchema) partitionSchemas.get(i);
            if (schema.getName().equalsIgnoreCase(partitionSchemaName))
            {
                partitionSchema = schema; // found!
            }
        }
    }

    /**
     * Saves partitioning properties in the repository for the given step.
     * @param rep the repository to save in
     * @param id_transformation the ID of the transformation
     * @param id_step the ID of the step
     * @throws KettleDatabaseException In case anything goes wrong
     */
    public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleDatabaseException
    {
        rep.saveStepAttribute(id_transformation, id_step, "PARTITIONING_SCHEMA",    partitionSchema!=null?partitionSchema.getName():""); // selected schema
        rep.saveStepAttribute(id_transformation, id_step, "PARTITIONING_METHOD",    getMethodCode());          // method of partitioning  
        rep.saveStepAttribute(id_transformation, id_step, "PARTITIONING_FIELDNAME", fieldName);               // The fieldname to partition on 
    }
    
    public StepPartitioningMeta(Repository rep, long id_step) throws KettleDatabaseException
    {
        partitionSchemaName = rep.getStepAttributeString(id_step, "PARTITIONING_SCHEMA");
        String methodCode   = rep.getStepAttributeString(id_step, "PARTITIONING_METHOD");
        method = getMethod(methodCode);
        fieldName           = rep.getStepAttributeString(id_step, "PARTITIONING_FIELDNAME");
    }
    
    public boolean isMethodMod()
    {
        return method==PARTITIONING_METHOD_MOD;
    }
    
    public boolean isMethodMirror()
    {
        return method==PARTITIONING_METHOD_MIRROR;
    }
}
