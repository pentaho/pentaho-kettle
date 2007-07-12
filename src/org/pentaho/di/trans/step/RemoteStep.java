package org.pentaho.di.trans.step;


public class RemoteStep implements Cloneable {

	/** The host name or IP address to read from or to write to */
	private String hostname;
	
	/** The port to read input data from or to write output data to */
	private int port;

	/**
	 * @param hostname
	 * @param port
	 */
	public RemoteStep(String hostname, int port) {
		super();
		this.hostname = hostname;
		this.port = port;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
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

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}	
}
