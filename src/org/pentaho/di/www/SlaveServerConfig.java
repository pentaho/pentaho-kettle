package org.pentaho.di.www;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Node;

public class SlaveServerConfig {
	public static final String XML_TAG = "slave_config"; //$NON-NLS-1$
	public static final String XML_TAG_MASTERS = "masters"; //$NON-NLS-1$

	private List<SlaveServer> masters;
	
	private SlaveServer slaveServer;
	
	private boolean reportingToMasters;
	
	private boolean joining;
	
	public SlaveServerConfig() {
		masters=new ArrayList<SlaveServer>();
	}
	
	public SlaveServerConfig(SlaveServer slaveServer) {
		this();
		this.slaveServer = slaveServer;
	}
	
	public SlaveServerConfig(List<SlaveServer> masters, boolean reportingToMasters, SlaveServer slaveServer) {
		this.masters = masters;
		this.reportingToMasters = reportingToMasters;
		this.slaveServer = slaveServer;
	}
	
	public String getXML() {
		
        StringBuffer xml = new StringBuffer();
        
        xml.append(XMLHandler.openTag(XML_TAG));

        for (SlaveServer slaveServer : masters) {
        	xml.append(slaveServer.getXML());
        }
        
        XMLHandler.addTagValue("report_to_masters", reportingToMasters);
        
        if (slaveServer!=null) {
        	xml.append(slaveServer.getXML());
        }

        XMLHandler.addTagValue("joining", joining);

        xml.append(XMLHandler.closeTag(XML_TAG));

        return xml.toString();
	}
	
	public SlaveServerConfig(Node node) {
		this();
		Node mastersNode = XMLHandler.getSubNode(node, XML_TAG_MASTERS);
		int nrMasters = XMLHandler.countNodes(mastersNode, SlaveServer.XML_TAG);
		for (int i=0;i<nrMasters;i++) {
			Node slaveNode = XMLHandler.getSubNodeByNr(mastersNode, SlaveServer.XML_TAG, i);
			SlaveServer master = new SlaveServer(slaveNode);
			masters.add(master);
		}
		reportingToMasters = "Y".equalsIgnoreCase(XMLHandler.getTagValue(node, "report_to_masters"));
		Node slaveNode = XMLHandler.getSubNode(node, SlaveServer.XML_TAG);
		if (slaveNode!=null) {
			slaveServer = new SlaveServer(slaveNode);
		}
		joining = "Y".equalsIgnoreCase(XMLHandler.getTagValue(node, "joining"));
	}

	public SlaveServerConfig(String hostname, int port, boolean joining) {
		this();
		this.joining = joining;
		this.slaveServer = new SlaveServer(hostname+":"+port, hostname, ""+port, null, null);
	}

	/**
	 * @return the list of masters to report back to if the report to masters
	 *         flag is enabled.
	 */
	public List<SlaveServer> getMasters() {
		return masters;
	}

	/**
	 * @param masters the list of masters to set.  It is the list of masters to report back to if the report to masters flag is enabled.
	 */
	public void setMasters(List<SlaveServer> masters) {
		this.masters = masters;
	}

	/**
	 * @return the slave server.<br>
	 *    The user name and password defined in here are used to contact this slave by the masters.
	 */
	public SlaveServer getSlaveServer() {
		return slaveServer;
	}

	/**
	 * @param slaveServer the slave server details to set.<br>
	 *   The user name and password defined in here are used to contact this slave by the masters.
	 */
	public void setSlaveServer(SlaveServer slaveServer) {
		this.slaveServer = slaveServer;
	}

	/**
	 * @return true if this slave reports to the masters
	 */
	public boolean isReportingToMasters() {
		return reportingToMasters;
	}

	/**
	 * @param reportingToMaster set to true if this slave should report to the masters
	 */
	public void setReportingToMasters(boolean reportingToMaster) {
		this.reportingToMasters = reportingToMaster;
	}

	/**
	 * @return true if the webserver needs to join with the webserver threads (wait/block until finished)
	 */
	public boolean isJoining() {
		return joining;
	}

	/**
	 * @param joining Set to true if the webserver needs to join with the webserver threads (wait/block until finished)
	 */
	public void setJoining(boolean joining) {
		this.joining = joining;
	}
	
}
