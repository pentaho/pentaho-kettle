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
package org.pentaho.di.trans.step;

import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Partitioner;
import org.pentaho.di.trans.StepLoader;
import org.w3c.dom.Node;


public class StepPartitioningMeta implements XMLInterface, Cloneable
{
    public static final int PARTITIONING_METHOD_NONE     = 0;
    public static final int PARTITIONING_METHOD_MIRROR   = 1;
    public static final int PARTITIONING_METHOD_SPECIAL  = 2;
    
    public static final String[] methodCodes        = new String[] { "none", "Mirror", };
    public static final String[] methodDescriptions = new String[] { "None", "Mirror to all partitions" };

    private int				methodType;
    private String          method;

    private String          partitionSchemaName; // to allow delayed binding...
    private PartitionSchema partitionSchema;
    
    private Partitioner partitioner;
    
    private boolean hasChanged = false;
    
    public StepPartitioningMeta()
    {
        method = "none";
        methodType = PARTITIONING_METHOD_NONE;
        partitionSchema = new PartitionSchema();
        hasChanged = false;
    }
    
    /**
     * @param method
     * @param fieldName
     */
    public StepPartitioningMeta(String method, PartitionSchema partitionSchema)
    {
    	setMethod( method );
        this.partitionSchema = partitionSchema;
        hasChanged = false;
    }
    
    public StepPartitioningMeta clone()
    {
       StepPartitioningMeta stepPartitioningMeta = new StepPartitioningMeta(method, partitionSchema!=null ? (PartitionSchema) partitionSchema.clone() : null);
       stepPartitioningMeta.partitionSchemaName = partitionSchemaName;
       stepPartitioningMeta.setMethodType(methodType);
       stepPartitioningMeta.setPartitioner(partitioner == null? null : partitioner.clone());
       return stepPartitioningMeta;
    }
    
    /**
     * @return true if the partition schema names are the same.
     */
    @Override
    public boolean equals(Object obj) {
    	if (obj==null) return false;
    	StepPartitioningMeta meta = (StepPartitioningMeta) obj;
    	return partitionSchemaName.equalsIgnoreCase(meta.partitionSchemaName);
    }
    
    @Override
    public String toString() {
    	
    	String description;
    	
    	if (partitioner!=null) {
    		description = partitioner.getDescription();
    	}
    	else {
        	description = getMethodDescription();
    	}
    	if (partitionSchema!=null) {
    		description += " / "+partitionSchema.toString();
    	}
    	
    	return description;
    }

    /**
     * @return the partitioningMethod
     */
    public int getMethodType()
    {
        return methodType;
    }

    /**
     * @param method the partitioning method to set
     */
    public void setMethod(String method)
    {
    	if( !method.equals(this.method) )
    	{
            this.method = method;
            createPartitioner(method);
            hasChanged = true;
    	}
    }

    public String getXML()
    {
        StringBuffer xml = new StringBuffer(150);

        xml.append("         <partitioning>").append(Const.CR);
        xml.append("           ").append(XMLHandler.addTagValue("method",    getMethodCode()));
        xml.append("           ").append(XMLHandler.addTagValue("schema_name", partitionSchema!=null?partitionSchema.getName():""));
        if( partitioner != null ) 
        {
        	xml.append( partitioner.getXML() );
        }
        xml.append("           </partitioning>").append(Const.CR);
        
        return xml.toString();
    }
    
    public StepPartitioningMeta(Node partitioningMethodNode) throws KettleXMLException
    {
    	this();
    	setMethod( getMethod( XMLHandler.getTagValue(partitioningMethodNode, "method") ) );
        partitionSchemaName = XMLHandler.getTagValue(partitioningMethodNode, "schema_name");
        hasChanged = false;
        if( partitioner != null ) 
        {
        	partitioner.loadXML(partitioningMethodNode);
        }
        if (methodType!=PARTITIONING_METHOD_NONE && Const.isEmpty(partitionSchemaName)) {
        	throw new RuntimeException("bohoo!");
        }
    }
    
    public String getMethodCode()
    {
    	if( methodType == PARTITIONING_METHOD_SPECIAL) 
    	{
    		if( partitioner != null )
    		{
    			return partitioner.getId();
    		} else {
    			return methodCodes[PARTITIONING_METHOD_NONE];
    		}
    	}
        return methodCodes[methodType];
    }

    public String getMethodDescription()
    {
    	if( methodType != PARTITIONING_METHOD_SPECIAL )
    	{
            return methodDescriptions[methodType];
    	}
    	else 
    	{
    		return partitioner.getDescription();
    	}
    }

    public String getMethod( ) {
    	return method;
    }

    public static final String getMethod(String description)
    {
    	if (Const.isEmpty(description)) return methodCodes[PARTITIONING_METHOD_NONE];
    	
        for (int i=0;i<methodDescriptions.length;i++)
        {
            if (methodDescriptions[i].equalsIgnoreCase(description)){
            	return methodCodes[i];
            }
        }
        
        for (int i=0;i<methodCodes.length;i++)
        {
            if (methodCodes[i].equalsIgnoreCase(description)) return methodCodes[i];
        }
        
        if( StepLoader.getPartitioner( description ) != null ) {
        	return description;
        }
        return methodCodes[PARTITIONING_METHOD_NONE];
    }

    public static final int getMethodType(String description)
    {
        for (int i=0;i<methodDescriptions.length;i++)
        {
            if (methodDescriptions[i].equalsIgnoreCase(description)){
            	return i;
            }
        }
        
        for (int i=0;i<methodCodes.length;i++)
        {
            if (methodCodes[i].equalsIgnoreCase(description)) return i;
        }
        
        if( StepLoader.getPartitioner( description ) != null ) {
        	return PARTITIONING_METHOD_SPECIAL;
        }
        return PARTITIONING_METHOD_NONE;
    }

    public boolean isPartitioned()
    {
        return methodType!=PARTITIONING_METHOD_NONE;
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
        
        if (methodType!=PARTITIONING_METHOD_NONE && partitionSchema==null) {
        	String message = "Unable to set partition schema for name ["+partitionSchemaName+"], method: "+getMethodDescription()+Const.CR;
        	message += "This is the list of available partition schema:"+Const.CR;
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
    }
    
    public void createPartitioner( String method ) {
    	methodType = getMethodType(method);
        switch ( methodType ) {
        case PARTITIONING_METHOD_SPECIAL: {
        	partitioner = StepLoader.getPartitioner( method ).getInstance();
        	break;
        }
        case PARTITIONING_METHOD_NONE:
        default: partitioner = null;
        }
        if( partitioner != null ) 
        {
        	partitioner.setMeta(this);
        }
    }
    
    public StepPartitioningMeta(Repository rep, long id_step) throws KettleException
    {
    	this();
        partitionSchemaName = rep.getStepAttributeString(id_step, "PARTITIONING_SCHEMA");
        String methodCode   = rep.getStepAttributeString(id_step, "PARTITIONING_METHOD");
        setMethod( getMethod(methodCode) );
        if( partitioner != null ) {
        	partitioner.loadRep( rep, id_step);
        }
        hasChanged = true;
    }

    public boolean isMethodMirror()
    {
        return methodType==PARTITIONING_METHOD_MIRROR;
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

	public void setMethodType(int methodType) {
		this.methodType = methodType;
	}
}
