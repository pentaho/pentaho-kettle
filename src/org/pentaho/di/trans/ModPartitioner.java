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
package org.pentaho.di.trans;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
import org.w3c.dom.Node;

public class ModPartitioner extends BasePartitioner {

	private String fieldName; 
	protected int partitionColumnIndex = -1;
	
	public ModPartitioner(  ) {
		super( );
	}

	public Partitioner getInstance() 
	{
		Partitioner partitioner = new ModPartitioner();
		partitioner.setId( getId() );
		partitioner.setDescription( getDescription() );
		return partitioner;
	}
	
	public ModPartitioner clone()
	{
		ModPartitioner modPartitioner = (ModPartitioner) super.clone();
		modPartitioner.fieldName = fieldName;
		
		return modPartitioner;
	}

	public String getDialogClassName()
	{
		return "org.pentaho.di.ui.trans.dialog.ModPartitionerDialog";
	}

		public int getPartition(RowMetaInterface rowMeta, Object[] row ) throws KettleException
		{
			init(rowMeta);

	        if (partitionColumnIndex < 0)
	        {
	            partitionColumnIndex = rowMeta.indexOfValue(fieldName);
	            if (partitionColumnIndex < 0) { 
	            	throw new KettleStepException("Unable to find partitioning field name [" + fieldName + "] in the output row..." + rowMeta); 
	            }
	        }

	        long value;
	        
	        ValueMetaInterface valueMeta = rowMeta.getValueMeta(partitionColumnIndex);
	        Object valueData = row[partitionColumnIndex];
	        
	        switch(valueMeta.getType()) {
	        case ValueMetaInterface.TYPE_INTEGER: 
	        	Long longValue = rowMeta.getInteger(row, partitionColumnIndex); 
	        	if (longValue==null) {
	        		value = valueMeta.hashCode(valueData);
	        	}
	        	else {
	        		value = longValue.longValue();
	        	}
	        default: 
	        	value = valueMeta.hashCode(valueData);
	        }
	        
	        /*
	        value = rowMeta.getInteger(row, partitionColumnIndex);
	        */
            
            int targetLocation = (int)( Math.abs(value) % nrPartitions);

			return targetLocation;
		}

		public String getDescription() {
			String description = "Mod partitioner";
			if (!Const.isEmpty(fieldName)) {
				description+="(" + fieldName + ")";
			}
			return description;
		}

		public String getXML()
		{
	        StringBuilder xml = new StringBuilder(150);
	        xml.append("           ").append(XMLHandler.addTagValue("field_name", fieldName));
	        return xml.toString();
		}

		public void loadXML(Node partitioningMethodNode)
			throws KettleXMLException
		{
	        fieldName = XMLHandler.getTagValue(partitioningMethodNode, "field_name");
		}

	    public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
	    {
	        rep.saveStepAttribute(id_transformation, id_step, "PARTITIONING_FIELDNAME", fieldName);               // The fieldname to partition on 
	    }

	    public void loadRep(Repository rep, ObjectId id_step) throws KettleException
	    {
	        fieldName = rep.getStepAttributeString(id_step, "PARTITIONING_FIELDNAME");
	    }

		public String getFieldName() {
			return fieldName;
		}

		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}

}
