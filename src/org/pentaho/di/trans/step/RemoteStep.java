package org.pentaho.di.trans.step;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleEOFException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.w3c.dom.Node;


/**
 * Defines and handles communication to and from remote steps.
 *  
 *  TODO: add compression as a parameter/option
 *	TODO add buffer size as a parameter
 *
 * @author Matt
 *
 */
public class RemoteStep implements Cloneable, XMLInterface, Comparable<RemoteStep> {

	public static final String XML_TAG = "remotestep";
	
	/** The host name or IP address to read from or to write to */
	private String hostname;
	
	/** The port to read input data from or to write output data to */
	private String port;

	private ServerSocket serverSocket;
	private Socket socket;
	
	private boolean first;

	private DataOutputStream outputStream;

    public AtomicBoolean stopped = new AtomicBoolean(false);

	private BaseStep baseStep;

	private DataInputStream inputStream;

	private String sourceStep;

	private String targetStep;

	private String targetSlaveServerName;

	/**
	 * @param hostname
	 * @param port
	 */
	public RemoteStep(String hostname, String port, String sourceStep, String targetStep, String targetSlaveServerName) {
		super();
		this.hostname = hostname;
		this.port = port;
		this.sourceStep = sourceStep;
		this.targetStep = targetStep;
		this.targetSlaveServerName = targetSlaveServerName;
	}
	
	@Override
	public Object clone() {
		try {
			return super.clone();
		}
		catch(CloneNotSupportedException e) {
			return null;
		}
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

	public int compareTo(RemoteStep remoteStep) {
		return toString().compareTo(remoteStep.toString());
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

	public void openServerSocket(VariableSpace space) throws IOException {
		int portNumber = Integer.parseInt( space.environmentSubstitute(port) );
        serverSocket = new ServerSocket(portNumber);
	}

	/**
	 * @return the serverSocket that is created by the open server socket method.
	 */
	public ServerSocket getServerSocket() {
		return serverSocket;
	}

	/**
	 * @return the socket
	 */
	public Socket getSocket() {
		return socket;
	}

	/**
	 * @param socket the socket to set
	 */
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	/**
	 * Open a socket for writing.
	 * 
	 * @param maxRowsetSize
	 * @return
	 * @throws IOException
	 */
	public RowSet openWriterSocket(final BaseStep baseStep) throws IOException {
		this.baseStep = baseStep;
		socket = serverSocket.accept();
        outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(), 5000));
		first=true;
		
		// Create an output row set: to be added to BaseStep.outputRowSets
		//
		final RowSet rowSet = new RowSet(baseStep.getTransMeta().getSizeRowset());
		
		// TODO: verify the case with multiple step copies running on the slaves
		rowSet.setThreadNameFromToCopy(sourceStep, 0, targetStep, 0);  
		rowSet.setRemoteSlaveServerName(targetSlaveServerName);
		
		// Start a thread that will read out the output row set and send the data over the wire...
		// This will make everything else transparent, copying, distributing, including partitioning, etc.
		//
		Runnable runnable = new Runnable() {
		
			public void run() {
				try {
				
					// get a row of data...
					Object[] rowData = baseStep.getRowFrom(rowSet);
					
					while (rowData!=null && !baseStep.isStopped()) {
						sendRow(rowSet.getRowMeta(), rowData);
						rowData = baseStep.getRowFrom(rowSet);
					}
					
				} catch (Exception e) {
					LogWriter.getInstance().logError(baseStep.toString(), "Error writing to remote step", e);
					baseStep.setErrors(1);
					baseStep.stopAll();
				}
				finally {
					try {
						socket.close();
					} catch (IOException e) {
						LogWriter.getInstance().logError(baseStep.toString(), "Error closing client socket to remote step", e);
						baseStep.setErrors(1);
						baseStep.stopAll();
					}
					try {
						serverSocket.close();
					} catch (IOException e) {
						LogWriter.getInstance().logError(baseStep.toString(), "Error closing client socket to remote step", e);
						baseStep.setErrors(1);
						baseStep.stopAll();
					}
				}
			}
		};
		// Fire off this in the background...
		new Thread(runnable).start();
		
		// Return the rowSet to be added to the output row set of baseStep 
		//
		return rowSet;
	}
	
	private void sendRow(RowMetaInterface rowMeta, Object[] rowData) throws KettleFileException {
		if (first) {
			rowMeta.writeMeta(outputStream);
			baseStep.linesOutput++;
			first=false;
		}
		rowMeta.writeData(outputStream, rowData);
	}
	
	public RowSet openReaderSocket(final BaseStep baseStep) throws IOException {
		final RowSet rowSet = new RowSet(baseStep.getTransMeta().getSizeRowset());
		
		// TODO: verify the case with multiple step copies running on the slaves
		rowSet.setThreadNameFromToCopy(sourceStep, 0, targetStep, 0);
		
		this.baseStep = baseStep;
		
		int portNumber = Integer.parseInt( baseStep.environmentSubstitute(port) );
		
		// Link back to the remote input step...
		//
		socket = new Socket(baseStep.environmentSubstitute(hostname), portNumber);
        inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream(), 5000));

        Runnable runnable = new Runnable() {
			public void run() {
				// First read the row meta data from the socket...
				try {
					RowMetaInterface rowMeta = new RowMeta(inputStream);
					Object[] rowData = rowMeta.readData(inputStream);
					while (rowData!=null && !baseStep.isStopped()) {
						baseStep.linesInput++;
						rowSet.putRow(rowMeta, rowData);
						rowData = rowMeta.readData(inputStream);
					}
				}
				catch(KettleEOFException e) {
					// Nothing, we're simply done reading...
				} catch (KettleFileException e) {
					LogWriter.getInstance().logError(baseStep.toString(), "Error reading from client socket to remote step", e);
					baseStep.setErrors(1);
					baseStep.stopAll();
				}
				finally {
					try {
						socket.close();
					} catch (IOException e) {
						LogWriter.getInstance().logError(baseStep.toString(), "Error closing client socket to remote step", e);
						baseStep.setErrors(1);
						baseStep.stopAll();
					}
				}
				rowSet.setDone(); // signal baseStep that nothing else comes from this step.
			}
		};
		new Thread(runnable).start();
		
		return rowSet;
	}

	/**
	 * @return the sourceStep
	 */
	public String getSourceStep() {
		return sourceStep;
	}

	/**
	 * @param sourceStep the sourceStep to set
	 */
	public void setSourceStep(String sourceStep) {
		this.sourceStep = sourceStep;
	}

	/**
	 * @return the targetStep
	 */
	public String getTargetStep() {
		return targetStep;
	}

	/**
	 * @param targetStep the targetStep to set
	 */
	public void setTargetStep(String targetStep) {
		this.targetStep = targetStep;
	}

	/**
	 * @return the targetSlaveServerName
	 */
	public String getTargetSlaveServerName() {
		return targetSlaveServerName;
	}

	/**
	 * @param targetSlaveServerName the targetSlaveServerName to set
	 */
	public void setTargetSlaveServerName(String targetSlaveServerName) {
		this.targetSlaveServerName = targetSlaveServerName;
	}
}
