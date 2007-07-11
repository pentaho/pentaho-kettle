package org.pentaho.di.trans.step;

import org.pentaho.di.core.row.RowMetaInterface;

public class StepServerSocket {
	
	private int port;
	private RowMetaInterface inputRowMeta;
	
	public StepServerSocket(int port) {
		this.port = port;
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

	/**
	 * @return the inputRowMeta
	 */
	public RowMetaInterface getInputRowMeta() {
		return inputRowMeta;
	}

	/**
	 * @param inputRowMeta the inputRowMeta to set
	 */
	public void setInputRowMeta(RowMetaInterface inputRowMeta) {
		this.inputRowMeta = inputRowMeta;
	} 
	
	
}
