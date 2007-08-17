package org.pentaho.di.trans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.xml.XMLHandler;
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
		private String stepName;
		private int    stepCopyNr;
		
		/**
		 * @param slaveServerName
		 * @param stepName
		 * @param stepCopyNr
		 */
		public SlaveStepCopy(String slaveServerName, String stepName, int stepCopyNr) {
			super();
			this.slaveServerName = slaveServerName;
			this.stepName = stepName;
			this.stepCopyNr = stepCopyNr;
		}
		
		public String toString() {
			return slaveServerName+"/"+stepName+"."+stepCopyNr;
		}
		
		public boolean equals(Object obj) {
			SlaveStepCopy copy = (SlaveStepCopy) obj;
			return slaveServerName.equals(copy.slaveServerName) && stepName.equals(copy.stepName) && stepCopyNr==stepCopyNr;
		}
		
		public int hashCode() {
			return slaveServerName.hashCode() ^ stepName.hashCode() ^ new Integer(stepCopyNr).hashCode();
		}

		public int compareTo(SlaveStepCopy o) {
			int cmp = slaveServerName.compareTo(o.slaveServerName);
			if (cmp!=0) return cmp;
			cmp = stepName.compareTo(o.stepName);
			if (cmp!=0) return cmp;
			return stepCopyNr-o.stepCopyNr;
		}
	}

	public static final String XML_TAG = "slave-step-copy-partition-distribution";
	
	private Map<SlaveStepCopy, Integer> distribution;
	
	public SlaveStepCopyPartitionDistribution() {
		distribution = new Hashtable<SlaveStepCopy, Integer>();
	}
	
	public void addPartition(String slaveServerName, String stepName, int stepCopyNr, int partitionNr) {
		distribution.put(new SlaveStepCopy(slaveServerName, stepName, stepCopyNr), partitionNr);
	}
	
	private int getPartition(SlaveStepCopy slaveStepCopy) {
		Integer integer = distribution.get(slaveStepCopy);
		if (integer==null) return -1;
		return integer;
	}
	
	public int getPartition(String slaveServerName, String stepName, int stepCopyNr) {
		return getPartition(new SlaveStepCopy(slaveServerName, stepName, stepCopyNr));
	}
	
	public String getXML() {
		StringBuffer xml = new StringBuffer();
		
		xml.append( XMLHandler.openTag(XML_TAG) ).append(Const.CR);
		
		List<SlaveStepCopy> list = new ArrayList<SlaveStepCopy>(distribution.keySet());
		Collections.sort(list);
		
		for (SlaveStepCopy copy : list) {
			int partition = getPartition(copy);
			
			xml.append("  ").append(XMLHandler.openTag("entry") );
			xml.append("  ").append(XMLHandler.addTagValue("slavename", copy.slaveServerName, false) );
			xml.append("  ").append(XMLHandler.addTagValue("stepname", copy.stepName, false) );
			xml.append("  ").append(XMLHandler.addTagValue("stepcopy", copy.stepCopyNr, false) );
			xml.append("  ").append(XMLHandler.addTagValue("partition", partition, false) );
			
			xml.append(XMLHandler.closeTag("entry") ).append(Const.CR);
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
			String stepName = XMLHandler.getTagValue(entryNode, "stepname");
			int stepCopyNr = Const.toInt( XMLHandler.getTagValue(entryNode, "stepcopy"), -1);
			int partitionNr = Const.toInt( XMLHandler.getTagValue(entryNode, "partition"), -1);
			
			System.out.println("Read SlaveStepCopyPartitionDistribution entry: slave="+slaveServerName+", step="+stepName+", copy="+stepCopyNr+", partition="+partitionNr);
			addPartition(slaveServerName, stepName, stepCopyNr, partitionNr);
		}
	}
	
	public Map<SlaveStepCopy, Integer> getDistribution() {
		return distribution;
	}
}
