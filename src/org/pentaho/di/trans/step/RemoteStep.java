package org.pentaho.di.trans.step;

import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.w3c.dom.Node;


public class RemoteStep implements Cloneable, XMLInterface {

	public static final String XML_TAG = "remotestep";
	
	/** The host name or IP address to read from or to write to */
	private String hostname;
	
	/** The port to read input data from or to write output data to */
	private String port;

	/**
	 * @param hostname
	 * @param port
	 */
	public RemoteStep(String hostname, String port) {
		super();
		this.hostname = hostname;
		this.port = port;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	public String getXML() {
		StringBuffer xml = new StringBuffer();
		xml.append(XMLHandler.openTag(XML_TAG));
		
		xml.append(XMLHandler.addTagValue("hostname", hostname, false));
		xml.append(XMLHandler.addTagValue("port", port, false));
		
		xml.append(XMLHandler.closeTag(XML_TAG));
		return xml.toString();
	}
	
	public RemoteStep(Node node) {
		hostname = XMLHandler.getTagValue(node, "hostname");
		port = XMLHandler.getTagValue(node, "port");
	}
	
	@Override
	public String toString() {
		return hostname+":"+port;
	}
	
	@Override
	public boolean equals(Object obj) {
		return toString().equalsIgnoreCase(obj.toString());
	}

	/**
	 * @return the host name
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * @param hostname the host name to set
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	/**int
	 * @return the port
	 */
	public String getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(String port) {
		this.port = port;
	}	
}
