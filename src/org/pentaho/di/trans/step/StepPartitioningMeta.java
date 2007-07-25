package org.pentaho.di.trans.step;

import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.HashPartitioner;
import org.pentaho.di.trans.ModPartitioner;
import org.pentaho.di.trans.NoPartitioner;
import org.pentaho.di.trans.Partitioner;
import org.w3c.dom.Node;


public class StepPartitioningMeta implements XMLInterface, Cloneable
{
    public static final int PARTITIONING_METHOD_NONE    = 0;
    public static final int PARTITIONING_METHOD_MOD     = 1;
    public static final int PARTITIONING_METHOD_MIRROR  = 2;
    public static final int PARTITIONING_METHOD_HASH  = 3;
    
    public static final String[] methodCodes        = new String[] { "none", "Mod", "Mirror", "Hash" };
    public static final String[] methodDescriptions = new String[] { "None", "Remainder of division", "Mirror to all partitions", "Generate a hash code" };

    private int             method;
    private String          fieldName;

    private String          partitionSchemaName; // to allow delayed binding...
    private PartitionSchema partitionSchema;
    
    private Partitioner partitioner;
    
    private boolean hasChanged = false;
    
    public StepPartitioningMeta()
    {
        method = PARTITIONING_METHOD_NONE;
        partitionSchema = new PartitionSchema();
        hasChanged = false;
    }
    
    /**
     * @param method
     * @param fieldName
     */
    public StepPartitioningMeta(int method, String fieldName, PartitionSchema partitionSchema)
    {
    	setMethod( method );
        this.fieldName = fieldName;
        this.partitionSchema = partitionSchema;
        hasChanged = false;
    }
    
    public Object clone()
    {
       StepPartitioningMeta stepPartitioningMeta = new StepPartitioningMeta(method, fieldName, partitionSchema!=null ? (PartitionSchema) partitionSchema.clone() : null);
       stepPartitioningMeta.partitionSchemaName = partitionSchemaName;
       return stepPartitioningMeta;
    }
    
    /**
     * @return true if the partition schema names are the same.
     */
    @Override
    public boolean equals(Object obj) {
    	StepPartitioningMeta meta = (StepPartitioningMeta) obj;
    	return partitionSchemaName.equalsIgnoreCase(meta.partitionSchemaName);
    }
    
    @Override
    public String toString() {
    	switch(method) {
    	case PARTITIONING_METHOD_MOD : return getMethodDescription()+" : "+fieldName+"@"+partitionSchema.getName();
    	case PARTITIONING_METHOD_HASH : return getMethodDescription()+" : "+fieldName+"@"+partitionSchema.getName();
    	default: return getMethodDescription();
    	}
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
        hasChanged = true;
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
        createPartitioner(method);
        hasChanged = true;
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
    	this();
    	setMethod( getMethod( XMLHandler.getTagValue(partitioningMethodNode, "method") ) );
        fieldName = XMLHandler.getTagValue(partitioningMethodNode, "field_name");
        partitionSchemaName = XMLHandler.getTagValue(partitioningMethodNode, "schema_name");
        hasChanged = false;
        if (method!=PARTITIONING_METHOD_NONE && Const.isEmpty(partitionSchemaName)) {
        	throw new RuntimeException("bohoo!");
        }
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
/*
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
*/
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
        hasChanged = true;
    }
    
    /**
     * Set the partitioning schema after loading from XML or repository
     * @param partitionSchemas the list of partitioning schemas
     */
    public void setPartitionSchemaAfterLoading(List<PartitionSchema> partitionSchemas) throws KettleException
    {
        partitionSchema=null; // sorry, not found!
        
        for (int i=0;i<partitionSchemas.size() && partitionSchema==null;i++)
        {
            PartitionSchema schema = partitionSchemas.get(i);
            if (schema.getName().equalsIgnoreCase(partitionSchemaName))
            {
                partitionSchema = schema; // found!
            }
        }
        
        if (method!=PARTITIONING_METHOD_NONE && partitionSchema==null) {
        	String message = "Unable to set partition schema for name ["+partitionSchemaName+"], method: "+getMethodDescription()+Const.CR;
            for (int i=0;i<partitionSchemas.size() && partitionSchema==null;i++)
            {
                PartitionSchema schema = partitionSchemas.get(i);
                message+="  --> "+schema.getName()+Const.CR;
            }
        	throw new KettleException(message);
        }
    }

    /**
     * Saves partitioning properties in the repository for the given step.
     * @param rep the repository to save in
     * @param id_transformation the ID of the transformation
     * @param id_step the ID of the step
     * @throws KettleDatabaseException In case anything goes wrong
     */
    public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
    {
        rep.saveStepAttribute(id_transformation, id_step, "PARTITIONING_SCHEMA",    partitionSchema!=null?partitionSchema.getName():""); // selected schema
        rep.saveStepAttribute(id_transformation, id_step, "PARTITIONING_METHOD",    getMethodCode());          // method of partitioning  
        rep.saveStepAttribute(id_transformation, id_step, "PARTITIONING_FIELDNAME", fieldName);               // The fieldname to partition on 
    }
    
    public void createPartitioner( int method ) {
        switch ( method ) {
        case PARTITIONING_METHOD_MOD: partitioner = new ModPartitioner(this); break;
        case PARTITIONING_METHOD_HASH: partitioner = new HashPartitioner(this); break;
        case PARTITIONING_METHOD_NONE: partitioner = new NoPartitioner(this); break;
        default: partitioner = null;
        }
    }
    
    public StepPartitioningMeta(Repository rep, long id_step) throws KettleException
    {
    	this();
        partitionSchemaName = rep.getStepAttributeString(id_step, "PARTITIONING_SCHEMA");
        String methodCode   = rep.getStepAttributeString(id_step, "PARTITIONING_METHOD");
        setMethod( getMethod(methodCode) );
        fieldName           = rep.getStepAttributeString(id_step, "PARTITIONING_FIELDNAME");
        hasChanged = true;
    }
    /*
    public boolean isMethodMod()
    {
        return method==PARTITIONING_METHOD_MOD;
    }
    */
    public boolean isMethodMirror()
    {
        return method==PARTITIONING_METHOD_MIRROR;
    }

    public int getPartition(RowMetaInterface rowMeta, Object[] row) throws KettleException
    {
    	if( partitioner != null ) {
    		return partitioner.getPartition(rowMeta, row);
    	}
    	return 0;
    }
    
	public Partitioner getPartitioner() {
		return partitioner;
	}

	public void setPartitioner(Partitioner partitioner) {
		this.partitioner = partitioner;
	}

	public boolean hasChanged() {
		return hasChanged;
	}

	public void hasChanged(boolean hasChanged) {
		this.hasChanged = hasChanged;
	}
}
