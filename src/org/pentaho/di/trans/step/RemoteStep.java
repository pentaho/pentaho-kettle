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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.BlockingRowSet;
import org.pentaho.di.core.exception.KettleEOFException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.www.SocketRepository;
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
	
	/** The target or source slave server with which we're exchanging data */
	private String targetSlaveServerName;
	
	/** The target or source host name */
	private String hostname;
	
	/** The remote host name */
	private String remoteHostname;

	/** The target or source port number for the data socket */
	private String port;
	
	private ServerSocket serverSocket;
	private Socket socket;
	
	private DataOutputStream outputStream;

    public AtomicBoolean stopped = new AtomicBoolean(false);

	private BaseStep baseStep;

	private DataInputStream inputStream;

	private String sourceStep;
	
	private int sourceStepCopyNr;

	private String targetStep;
	
	private int targetStepCopyNr;
	
	private int bufferSize;
	private boolean compressingStreams;
	
	private GZIPOutputStream gzipOutputStream;

	private String	sourceSlaveServerName;

	/**
	 * @param hostname
	 * @param port
	 */
	public RemoteStep(String hostname, String remoteHostname, String port, String sourceStep, int sourceStepCopyNr, String targetStep, int targetStepCopyNr, String sourceSlaveServerName, String targetSlaveServerName, int bufferSize, boolean compressingStreams) {
		super();
		this.hostname = hostname;
		this.remoteHostname = remoteHostname;
		this.port = port;
		this.sourceStep = sourceStep;
		this.sourceStepCopyNr = sourceStepCopyNr;
		this.targetStep = targetStep;
		this.targetStepCopyNr = targetStepCopyNr;
		this.bufferSize = bufferSize;
		this.compressingStreams = compressingStreams;
		
		this.sourceSlaveServerName = sourceSlaveServerName;
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
		xml.append(XMLHandler.addTagValue("remote_hostname", remoteHostname, false));
		xml.append(XMLHandler.addTagValue("port", port, false));
		xml.append(XMLHandler.addTagValue("buffer_size", bufferSize, false));
		xml.append(XMLHandler.addTagValue("compressed_streams", compressingStreams, false));

		xml.append(XMLHandler.addTagValue("source_step_name", sourceStep, false));
		xml.append(XMLHandler.addTagValue("source_step_copy", sourceStepCopyNr, false));
		xml.append(XMLHandler.addTagValue("target_step_name", targetStep, false));
		xml.append(XMLHandler.addTagValue("target_step_copy", targetStepCopyNr, false));

		xml.append(XMLHandler.addTagValue("source_slave_server_name", sourceSlaveServerName, false));
		xml.append(XMLHandler.addTagValue("target_slave_server_name", targetSlaveServerName, false));


		
		xml.append(XMLHandler.closeTag(XML_TAG));
		return xml.toString();
	}
	
	public RemoteStep(Node node) {
		
		hostname = XMLHandler.getTagValue(node, "hostname");
		remoteHostname = XMLHandler.getTagValue(node, "remote_hostname");
		port     = XMLHandler.getTagValue(node, "port");
		bufferSize = Integer.parseInt(XMLHandler.getTagValue(node, "buffer_size"));
		compressingStreams = "Y".equalsIgnoreCase( XMLHandler.getTagValue(node, "compressed_streams") );

		sourceStep       = XMLHandler.getTagValue(node, "source_step_name");
		sourceStepCopyNr = Integer.parseInt(XMLHandler.getTagValue(node, "source_step_copy"));
		targetStep       = XMLHandler.getTagValue(node, "target_step_name");
		targetStepCopyNr = Integer.parseInt(XMLHandler.getTagValue(node, "target_step_copy"));
		
		sourceSlaveServerName = XMLHandler.getTagValue(node, "source_slave_server_name");
		targetSlaveServerName = XMLHandler.getTagValue(node, "target_slave_server_name");
	}
	
	@Override
	public String toString() {
		return hostname+":"+port+" ("+sourceSlaveServerName+"/"+sourceStep+"."+sourceStepCopyNr+" --> "+targetSlaveServerName+"/"+targetStep+"."+targetStepCopyNr+")"; // "  -  "+sourceStep+"."+sourceStepCopyNr+" --> "+targetStep+"."+targetStepCopyNr+")";
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

	public synchronized void openServerSocket(BaseStep baseStep) throws IOException {
		this.baseStep = baseStep;
		int portNumber = Integer.parseInt( baseStep.environmentSubstitute(port) );
		
		SocketRepository socketRepository = baseStep.getSocketRepository();
		serverSocket = socketRepository.openServerSocket(portNumber, baseStep.getTransMeta().getName()+" - "+baseStep.toString());
		
		// Add this socket to the steps server socket list
		// That way, the socket can be closed during transformation cleanup
		// That is called when the cluster has finished processing.
		//
		baseStep.getServerSockets().add(serverSocket);
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
	 * @return the RowSet created that will accept the rows for the remote step
	 * @throws IOException
	 */
	public synchronized BlockingRowSet openWriterSocket() throws IOException {

		// Create an output row set: to be added to BaseStep.outputRowSets
		//
		final BlockingRowSet rowSet = new BlockingRowSet(baseStep.getTransMeta().getSizeRowset());
		
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
					// Accept the socket, create a connection
					// This blocks until something comes through...
					//
					socket = serverSocket.accept();
					
					// Create the output stream...
					if (compressingStreams) {
						gzipOutputStream = new GZIPOutputStream(socket.getOutputStream(), 50000);
				        outputStream = new DataOutputStream(new BufferedOutputStream(gzipOutputStream, bufferSize));
					}
					else {
				        outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(), bufferSize));
					}
					
					baseStep.logBasic("Server socket accepted for port ["+ port +"], reading from server "+targetSlaveServerName);

					// get a row of data...
					//
					Object[] rowData = baseStep.getRowFrom(rowSet); 
					if (rowData!=null) {
						rowSet.getRowMeta().writeMeta(outputStream);
					}
					
					// Send that row to the remote step
					//
					while (rowData!=null && !baseStep.isStopped()) {
						// It's too confusing to count these twice, so decrement
						baseStep.decrementLinesRead();
						baseStep.decrementLinesWritten(); 
						
						// Write the row to the remote step via the output stream....
						//
						rowSet.getRowMeta().writeData(outputStream, rowData);
						baseStep.incrementLinesOutput();

						if (baseStep.log.isDebug()) baseStep.logDebug("Sent row to port "+port+" : "+rowSet.getRowMeta().getString(rowData));
						rowData = baseStep.getRowFrom(rowSet);
					}
					
					if (compressingStreams) {
						outputStream.flush();
						gzipOutputStream.finish();
					}
					else {
						outputStream.flush();
					}
					
				} catch (Exception e) {
					baseStep.logError("Error writing to remote step", e);
					baseStep.setErrors(1);
					baseStep.stopAll();
				}
				finally {

					// shut down the output stream, we've sent everything...
					//
					try {
						if (socket!=null) {
							socket.shutdownInput();
							socket.shutdownOutput();
							socket.close();
						}
					} catch (IOException e) {
						baseStep.logError("Error closing output socket to remote step", e);
						baseStep.setErrors(1);
						baseStep.stopAll();
					}
					
					// Now we can't close the server socket.
					// This would immediately kill all the remaining data on the client side.
					// The close of the server socket will happen when all the transformation in the cluster have finished.
					// Then Trans.cleanup() will be called.
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
		
	private Object[] getRowOfData(RowMetaInterface rowMeta) throws KettleFileException
	{
		Object[] rowData = null;

		while (!baseStep.isStopped() && rowData==null) {
			try {
				rowData = rowMeta.readData(inputStream);
			}
			catch(SocketTimeoutException e)
			{
				rowData = null; // try again.
			}
		}

		return rowData;
	}
	
	public synchronized BlockingRowSet openReaderSocket(final BaseStep baseStep) throws IOException, KettleException {
		this.baseStep = baseStep;
		
		final BlockingRowSet rowSet = new BlockingRowSet(baseStep.getTransMeta().getSizeRowset());
		
		// Make sure we handle the case with multiple step copies running on a slave...
		//
		rowSet.setThreadNameFromToCopy(sourceStep, sourceStepCopyNr, targetStep, targetStepCopyNr);
		rowSet.setRemoteSlaveServerName(targetSlaveServerName);
		
		final int portNumber = Integer.parseInt( baseStep.environmentSubstitute(port) );
		final String realHostname = baseStep.environmentSubstitute(hostname);

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
				socket = new Socket();
				socket.setReuseAddress(true);
				
				baseStep.logDetailed("Step variable MASTER_HOST : ["+baseStep.getVariable("MASTER_HOST")+"]");
				baseStep.logDetailed("Opening client (reader) socket to server ["+Const.NVL(realHostname, "")+":"+port+"]");
				socket.connect(new InetSocketAddress(realHostname, portNumber), 5000);
				
				connected=true;
				
                if (compressingStreams)
                {
                    inputStream  = new DataInputStream(new BufferedInputStream(new GZIPInputStream(socket.getInputStream()), bufferSize));
                }
                else
                {
                    inputStream  = new DataInputStream(new BufferedInputStream(socket.getInputStream(), bufferSize));
                }
                		
		        lastException=null;
        	}
        	catch(Exception e) {
                lastException=new KettleException("Unable to open socket to server "+realHostname+" port "+portNumber, e);
        	}
	        if (lastException!=null) // Sleep for a while
	        {
	            try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					if (socket!=null) {
						socket.shutdownInput();
						socket.shutdownOutput();
						socket.close();
						baseStep.logDetailed("Closed connection to server socket to read rows from remote step on server "+realHostname+" port "+portNumber+" - Local port="+socket.getLocalPort());
					}

					throw new KettleException("Interrupted while trying to connect to server socket: "+e.toString());
				}
	        }
        }

        // See if all was OK...
        if (lastException!=null)
        {

        	baseStep.logError("Error initialising step: "+lastException.toString());
			if (socket!=null) {
				socket.shutdownInput();
				socket.shutdownOutput();
				socket.close();
				baseStep.logDetailed("Closed connection to server socket to read rows from remote step on server "+realHostname+" port "+portNumber+" - Local port="+socket.getLocalPort());
			}
            throw lastException;
        }
        else
        {
            if (inputStream==null) throw new KettleException("Unable to connect to the SocketWriter in the "+TIMEOUT_IN_SECONDS+"s timeout period.");
        }
        
        baseStep.logDetailed("Opened connection to server socket to read rows from remote step on server "+realHostname+" port "+portNumber+" - Local port="+socket.getLocalPort());

        // Create a thread to take care of the reading from the client socket.
        // The rows read will be put in a RowSet buffer.
        // That buffer will hand over the rows to the step that has this RemoteStep object defined 
        // as a remote input step.
        //
        Runnable runnable = new Runnable() {
			public void run() {
				try {
					
					// First read the row meta data from the socket...
					//
					RowMetaInterface rowMeta=null;
					while (!baseStep.isStopped() && rowMeta==null) {
						try {
							rowMeta = new RowMeta(inputStream);
						}
						catch(SocketTimeoutException e) {
							rowMeta=null;
						}
					}
					
					if (rowMeta==null) {
						throw new KettleEOFException(); // leave now.
					}
					
					// And a first row of data...
					//
					Object[] rowData = getRowOfData(rowMeta);
					
					// Now get the data itself, row by row...
					//
					while (rowData!=null && !baseStep.isStopped()) {
						baseStep.incrementLinesInput();
						baseStep.decrementLinesRead();

						if (baseStep.log.isDebug()) baseStep.logDebug("Received row from remote step: "+rowMeta.getString(rowData));

						baseStep.putRowTo(rowMeta, rowData, rowSet);
						baseStep.decrementLinesWritten();
						rowData = getRowOfData(rowMeta);
					}
				}
				catch(KettleEOFException e) {
					// Nothing, we're simply done reading...
					//
					if (baseStep.log.isDebug()) baseStep.logDebug("Finished reading from remote step on server "+hostname+" port "+portNumber);

				} catch (Exception e) {
					baseStep.logError("Error reading from client socket to remote step", e);
					baseStep.setErrors(1);
					baseStep.stopAll();
				}
				finally {
					// Close the socket
					try {
						if (socket!=null) {
							socket.shutdownInput();
							socket.shutdownOutput();
							socket.close();
							baseStep.logDetailed("Closed connection to server socket to read rows from remote step on server "+realHostname+" port "+portNumber+" - Local port="+socket.getLocalPort());
						}
					} catch (IOException e) {
						baseStep.logError("Error closing client socket connection to remote step", e);
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

	/**
	 * @return the bufferSize
	 */
	public int getBufferSize() {
		return bufferSize;
	}

	/**
	 * @param bufferSize the bufferSize to set
	 */
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	/**
	 * @return the compressingStreams
	 */
	public boolean isCompressingStreams() {
		return compressingStreams;
	}

	/**
	 * @param compressingStreams the compressingStreams to set
	 */
	public void setCompressingStreams(boolean compressingStreams) {
		this.compressingStreams = compressingStreams;
	}

	/**
	 * @return the remoteHostname
	 */
	public String getRemoteHostname() {
		return remoteHostname;
	}

	/**
	 * @param remoteHostname the remoteHostname to set
	 */
	public void setRemoteHostname(String remoteHostname) {
		this.remoteHostname = remoteHostname;
	}

	/**
	 * @return the sourceSlaveServer name
	 */
	public String getSourceSlaveServerName() {
		return sourceSlaveServerName;
	}

	/**
	 * @param sourceSlaveServername the sourceSlaveServerName to set
	 */
	public void setSourceSlaveServerName(String sourceSlaveServerName) {
		this.sourceSlaveServerName = sourceSlaveServerName;
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			if (socket!=null) {
				socket.shutdownInput();
				socket.shutdownOutput();
				socket.close();
			}
			if (serverSocket!=null) {
				serverSocket.close();
			}
		} catch (IOException e) {
		} finally {
			super.finalize();
		}
	}
}
