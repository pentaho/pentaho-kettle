package org.pentaho.di.trans.step;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleEOFException;
import org.pentaho.di.core.exception.KettleException;
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

	private static final long TIMEOUT_IN_SECONDS = 30;
	
	private static LogWriter log = LogWriter.getInstance();

	/** The target or source slave server with which we're exchanging data */
	private String targetSlaveServerName;
	
	/** The target or source hostname */
	private String hostname;

	/** The target or source port number for the data socket */
	private String port;
	
	private ServerSocket serverSocket;
	private Socket socket;
	
	private boolean first;

	private DataOutputStream outputStream;

    public AtomicBoolean stopped = new AtomicBoolean(false);

	private BaseStep baseStep;

	private DataInputStream inputStream;

	private String sourceStep;
	
	private int sourceStepCopyNr;

	private String targetStep;
	
	private int targetStepCopyNr;
	
	

	/**
	 * @param hostname
	 * @param port
	 */
	public RemoteStep(String hostname, String port, String sourceStep, int sourceStepCopyNr, String targetStep, int targetStepCopyNr, String targetSlaveServerName) {
		super();
		this.hostname = hostname;
		this.port = port;
		this.sourceStep = sourceStep;
		this.sourceStepCopyNr = sourceStepCopyNr;
		this.targetStep = targetStep;
		this.targetStepCopyNr = targetStepCopyNr;
		
		this.targetSlaveServerName = targetSlaveServerName;
		
		if (sourceStep.equals(targetStep) && sourceStepCopyNr==targetStepCopyNr) {
			throw new RuntimeException("The source and target step/copy can't be the same for a remote step definition.");
		}
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

		xml.append(XMLHandler.addTagValue("source_step_name", sourceStep, false));
		xml.append(XMLHandler.addTagValue("source_step_copy", sourceStepCopyNr, false));
		xml.append(XMLHandler.addTagValue("target_step_name", targetStep, false));
		xml.append(XMLHandler.addTagValue("target_step_copy", targetStepCopyNr, false));

		xml.append(XMLHandler.addTagValue("target_slave_server_name", targetSlaveServerName, false));

		xml.append(XMLHandler.closeTag(XML_TAG));
		return xml.toString();
	}
	
	public RemoteStep(Node node) {
		
		hostname = XMLHandler.getTagValue(node, "hostname");
		port     = XMLHandler.getTagValue(node, "port");
		
		sourceStep       = XMLHandler.getTagValue(node, "source_step_name");
		sourceStepCopyNr = Integer.parseInt(XMLHandler.getTagValue(node, "source_step_copy"));
		targetStep       = XMLHandler.getTagValue(node, "target_step_name");
		targetStepCopyNr = Integer.parseInt(XMLHandler.getTagValue(node, "target_step_copy"));
		
		targetSlaveServerName = XMLHandler.getTagValue(node, "target_slave_server_name");
	}
	
	@Override
	public String toString() {
		return hostname+":"+port+" ("+targetSlaveServerName+" : "+sourceStep+"."+sourceStepCopyNr+" --> "+targetStep+"."+targetStepCopyNr+")"; // "  -  "+sourceStep+"."+sourceStepCopyNr+" --> "+targetStep+"."+targetStepCopyNr+")";
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
		
		System.out.println("Server socket accepted for port ["+ port +"]");
		
        outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(), 5000));
		first=true;
		
		// Create an output row set: to be added to BaseStep.outputRowSets
		//
		final RowSet rowSet = new RowSet(baseStep.getTransMeta().getSizeRowset());
		
		// Set the details for the source and target step as well as the target slave server.
		// This will help us determine the pre-calculated partition nr later in the game. (putRow())
		//
		rowSet.setThreadNameFromToCopy(sourceStep, sourceStepCopyNr, targetStep, targetStepCopyNr);  
		rowSet.setRemoteSlaveServerName(targetSlaveServerName);
		
		// Start a thread that will read out the output row set and send the data over the wire...
		// This will make everything else transparent, copying, distributing, including partitioning, etc.
		//
		Runnable runnable = new Runnable() {
		
			public void run() {
				try {
				
					// get a row of data...
					//
					Object[] rowData = baseStep.getRowFrom(rowSet); 
					
					// Send that row to the remote step
					//
					while (rowData!=null && !baseStep.isStopped()) {
						sendRow(rowSet.getRowMeta(), rowData);
						synchronized(rowSet) { rowData = baseStep.getRowFrom(rowSet); }
					}
					
				} catch (Exception e) {
					LogWriter.getInstance().logError(baseStep.toString(), "Error writing to remote step", e);
					LogWriter.getInstance().logError(baseStep.toString(), Const.getStackTracker(e));
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
		
		// Fire this off in the in a separate thread...
		//
		new Thread(runnable).start();
		
		// Return the rowSet to be added to the output row set of baseStep 
		//
		return rowSet;
	}
	
	private void sendRow(RowMetaInterface rowMeta, Object[] rowData) throws KettleFileException {
		if (first) {
			rowMeta.writeMeta(outputStream);
			first=false;
		}
		rowMeta.writeData(outputStream, rowData);

		baseStep.linesOutput++;
	}
	
	public RowSet openReaderSocket(final BaseStep baseStep) throws IOException, KettleException {
		
		final RowSet rowSet = new RowSet(baseStep.getTransMeta().getSizeRowset());
		
		// TODO: verify the case with multiple step copies running on the slaves
		rowSet.setThreadNameFromToCopy(sourceStep, sourceStepCopyNr, targetStep, targetStepCopyNr);
		
		this.baseStep = baseStep;
		
		int portNumber = Integer.parseInt( baseStep.environmentSubstitute(port) );
		String realHostname = baseStep.environmentSubstitute(hostname);

		// Connect to the server socket (started during BaseStep.init())
        // Because the accept() call on the server socket can be called after we reached this code
        // it is best to build in a retry loop with a time-out here.
        // 
        long startTime = System.currentTimeMillis();
        boolean connected=false;
        KettleException lastException=null;
		
        //// timeout with retry until connected
        while ( !connected && (TIMEOUT_IN_SECONDS > (System.currentTimeMillis()-startTime)/1000) && !baseStep.isStopped())
        {
        	try {
				socket = new Socket(realHostname, portNumber);
				connected=true;
		        inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream(), 5000));
		        lastException=null;
        	}
        	catch(Exception e) {
                lastException=new KettleException("Unable to open socket to server "+realHostname+" port "+portNumber, e);
        	}
	        if (lastException!=null) // Sleep for a second
	        {
	            try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					throw new KettleException("Interrupted while trying to connect to server socket: "+e.toString());
				}
	        }
        }

        // See if all was OK...
        if (lastException!=null)
        {
            log.logError(toString(), "Error initialising step: "+lastException.toString());
            log.logError(toString(), Const.getStackTracker(lastException));
            throw lastException;
        }
        else
        {
            if (inputStream==null) throw new KettleException("Unable to connect to the SocketWriter in the "+TIMEOUT_IN_SECONDS+"s timeout period.");
        }
        

        // Create a thread to take care of the reading from the client socket.
        // The rows read will be put in a RowSet buffer.
        // That buffer will hand over the rows to the step that has this RemoteStep object defined 
        // as a remote input step.
        //
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
					//
				} catch (Exception e) {
					LogWriter.getInstance().logError(baseStep.toString(), "Error reading from client socket to remote step", e);
					LogWriter.getInstance().logError(baseStep.toString(), Const.getStackTracker(e));
					baseStep.setErrors(1);
					baseStep.stopAll();
				}
				finally {
					try {
						inputStream.close();
					} catch (IOException e) {
						LogWriter.getInstance().logError(baseStep.toString(), "Error closing input stream, used for reading from remote step", e);
						baseStep.setErrors(1);
						baseStep.stopAll();
					}
					try {
						socket.close();
					} catch (IOException e) {
						LogWriter.getInstance().logError(baseStep.toString(), "Error closing client socket connection to remote step", e);
						baseStep.setErrors(1);
						baseStep.stopAll();
					}
				}
				// signal baseStep that nothing else comes from this step.
				//
				rowSet.setDone(); 
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

	/**
	 * @return the sourceStepCopyNr
	 */
	public int getSourceStepCopyNr() {
		return sourceStepCopyNr;
	}

	/**
	 * @param sourceStepCopyNr the sourceStepCopyNr to set
	 */
	public void setSourceStepCopyNr(int sourceStepCopyNr) {
		this.sourceStepCopyNr = sourceStepCopyNr;
	}

	/**
	 * @return the targetStepCopyNr
	 */
	public int getTargetStepCopyNr() {
		return targetStepCopyNr;
	}

	/**
	 * @param targetStepCopyNr the targetStepCopyNr to set
	 */
	public void setTargetStepCopyNr(int targetStepCopyNr) {
		this.targetStepCopyNr = targetStepCopyNr;
	}
}
