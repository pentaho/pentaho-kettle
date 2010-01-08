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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.partition.PartitionSchema;
import org.w3c.dom.Node;

/**
 * This class keeps track of which step copy in which transformation is responsible for handling a certain partition nr.
 * This distribution is created BEFORE the slave transformations are sent to the slave servers.
 * As such, it will be easy to link a step copy on a certain slave server uniquely to a certain partition.
 * That is to say, it will be done the same way accross the complete cluster.
 *  
 * @author matt
 *
 */
public class SlaveStepCopyPartitionDistribution {
	
	public class SlaveStepCopy implements Comparable<SlaveStepCopy> {
		private String slaveServerName;
		private String partitionSchemaName;
		private int    stepCopyNr;
		
		/**
		 * @param slaveServerName
		 * @param partitionSchemaName
		 * @param stepCopyNr
		 */
		public SlaveStepCopy(String slaveServerName, String partitionSchemaName, int stepCopyNr) {
			super();
			this.slaveServerName = slaveServerName;
			this.partitionSchemaName = partitionSchemaName;
			this.stepCopyNr = stepCopyNr;
		}
		
		public String toString() {
			return slaveServerName+"/"+partitionSchemaName+"."+stepCopyNr;
		}
		
		public boolean equals(Object obj) {
			SlaveStepCopy copy = (SlaveStepCopy) obj;
			return slaveServerName.equals(copy.slaveServerName) && partitionSchemaName.equals(copy.partitionSchemaName) && stepCopyNr==copy.stepCopyNr;
		}
		
		public int hashCode() {
			return slaveServerName.hashCode() ^ partitionSchemaName.hashCode() ^ Integer.valueOf(stepCopyNr).hashCode();
		}

		public int compareTo(SlaveStepCopy o) {
			int cmp = slaveServerName.compareTo(o.slaveServerName);
			if (cmp!=0) return cmp;
			cmp = partitionSchemaName.compareTo(o.partitionSchemaName);
			if (cmp!=0) return cmp;
			return stepCopyNr-o.stepCopyNr;
		}

		/**
		 * @return the slaveServerName
		 */
		public String getSlaveServerName() {
			return slaveServerName;
		}

		/**
		 * @param slaveServerName the slaveServerName to set
		 */
		public void setSlaveServerName(String slaveServerName) {
			this.slaveServerName = slaveServerName;
		}

		/**
		 * @return the partition schema name
		 */
		public String getPartitionSchemaName() {
			return partitionSchemaName;
		}

		/**
		 * @param partitionSchemaName the partition schema name to set
		 */
		public void setStepName(String partitionSchemaName) {
			this.partitionSchemaName = partitionSchemaName;
		}

		/**
		 * @return the stepCopyNr
		 */
		public int getStepCopyNr() {
			return stepCopyNr;
		}

		/**
		 * @param stepCopyNr the stepCopyNr to set
		 */
		public void setStepCopyNr(int stepCopyNr) {
			this.stepCopyNr = stepCopyNr;
		}
	}

	public static final String XML_TAG = "slave-step-copy-partition-distribution";
	
	private Map<SlaveStepCopy, Integer> distribution;
	private List<PartitionSchema> originalPartitionSchemas;

	public SlaveStepCopyPartitionDistribution() {
		distribution = new Hashtable<SlaveStepCopy, Integer>();
	}
	
	/**
	 * Add a partition number to the distribution for re-use at runtime.
	 * @param slaveServerName
	 * @param partitionSchemaName
	 * @param stepCopyNr
	 * @param partitionNr
	 */
	public void addPartition(String slaveServerName, String partitionSchemaName, int stepCopyNr, int partitionNr) {
		distribution.put(new SlaveStepCopy(slaveServerName, partitionSchemaName, stepCopyNr), partitionNr);
	}
	
	/**
	 * Add a partition number to the distribution if it doesn't already exist.
	 * 
	 * @param slaveServerName
	 * @param partitionSchemaName
	 * @param stepCopyNr
	 * @return The found or created partition number
	 */
	public int addPartition(String slaveServerName, String partitionSchemaName, int stepCopyNr) {
		Integer partitionNr = distribution.get(new SlaveStepCopy(slaveServerName, partitionSchemaName, stepCopyNr));
		if (partitionNr==null) {
			// Not found: add it.
			//
			int nr = 0;
			for (SlaveStepCopy slaveStepCopy : distribution.keySet()) {
				if (slaveStepCopy.partitionSchemaName.equals(partitionSchemaName)) {
					nr++;
				}
			}
			partitionNr=Integer.valueOf(nr);
			addPartition(slaveServerName, partitionSchemaName, stepCopyNr, nr);
		}
		return partitionNr.intValue();
	}
	
	private int getPartition(SlaveStepCopy slaveStepCopy) {
		Integer integer = distribution.get(slaveStepCopy);
		if (integer==null) return -1;
		return integer;
	}
	
	public int getPartition(String slaveServerName, String partitionSchemaName, int stepCopyNr) {
		return getPartition(new SlaveStepCopy(slaveServerName, partitionSchemaName, stepCopyNr));
	}
	
	public String getXML() {
		StringBuilder xml = new StringBuilder(200);
		
		xml.append( XMLHandler.openTag(XML_TAG) ).append(Const.CR);
		
		List<SlaveStepCopy> list = new ArrayList<SlaveStepCopy>(distribution.keySet());
		Collections.sort(list);
		
		for (SlaveStepCopy copy : list) {
			int partition = getPartition(copy);
			
			xml.append("  ").append(XMLHandler.openTag("entry") );
			xml.append("  ").append(XMLHandler.addTagValue("slavename", copy.slaveServerName, false) );
			xml.append("  ").append(XMLHandler.addTagValue("partition_schema_name", copy.partitionSchemaName, false) );
			xml.append("  ").append(XMLHandler.addTagValue("stepcopy", copy.stepCopyNr, false) );
			xml.append("  ").append(XMLHandler.addTagValue("partition", partition, false) );
			
			xml.append(XMLHandler.closeTag("entry") ).append(Const.CR);
		}
		
		if (originalPartitionSchemas!=null) {
			xml.append("  ").append(XMLHandler.openTag("original-partition-schemas") );
			for (PartitionSchema partitionSchema : originalPartitionSchemas) {
				xml.append(partitionSchema.getXML());
			}
			xml.append("  ").append(XMLHandler.closeTag("original-partition-schemas") );
		}

		xml.append( XMLHandler.closeTag(XML_TAG) ).append(Const.CR);

		return xml.toString();
	}
	
	public SlaveStepCopyPartitionDistribution(Node node) {
		this();
		
		int nrEntries = XMLHandler.countNodes(node, "entry");
		for (int i=0;i<nrEntries;i++) {
			Node entryNode = XMLHandler.getSubNodeByNr(node, "entry", i);
			String slaveServerName = XMLHandler.getTagValue(entryNode, "slavename");
			String partitionSchemaName = XMLHandler.getTagValue(entryNode, "partition_schema_name");
			int stepCopyNr = Const.toInt( XMLHandler.getTagValue(entryNode, "stepcopy"), -1);
			int partitionNr = Const.toInt( XMLHandler.getTagValue(entryNode, "partition"), -1);
			
			addPartition(slaveServerName, partitionSchemaName, stepCopyNr, partitionNr);
		}
		
		Node originalPartitionSchemasNode = XMLHandler.getSubNode(node, "original-partition-schemas");
		if (originalPartitionSchemasNode!=null) {
			originalPartitionSchemas = new ArrayList<PartitionSchema>();
			int nrSchemas = XMLHandler.countNodes(originalPartitionSchemasNode, PartitionSchema.XML_TAG);
			for (int i=0;i<nrSchemas;i++) {
				Node schemaNode = XMLHandler.getSubNodeByNr(originalPartitionSchemasNode, PartitionSchema.XML_TAG, i);
				PartitionSchema originalPartitionSchema = new PartitionSchema(schemaNode);
				originalPartitionSchemas.add(originalPartitionSchema);
			}
		}
	}
	
	public Map<SlaveStepCopy, Integer> getDistribution() {
		return distribution;
	}

	/**
	 * @return the originalPartitionSchemas
	 */
	public List<PartitionSchema> getOriginalPartitionSchemas() {
		return originalPartitionSchemas;
	}

	/**
	 * @param originalPartitionSchemas the originalPartitionSchemas to set
	 */
	public void setOriginalPartitionSchemas(List<PartitionSchema> originalPartitionSchemas) {
		this.originalPartitionSchemas = originalPartitionSchemas;
	}
}
