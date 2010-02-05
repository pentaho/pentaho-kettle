 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 
package org.pentaho.di.trans.steps.socketreader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleEOFException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * Read data from a TCP/IP socket supplied by SocketWriter.
 * The data coming over the socket is one serialized Row object including metadata and then a series of serialized rows, data only. 
 * 
 * @author Matt
 * @since 01-dec-2006
 */
public class SocketReader extends BaseStep implements StepInterface
{
	private static Class<?> PKG = SocketReaderMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static final String STRING_FINISHED = "Finished";
    private static final int TIMEOUT_IN_SECONDS = 30;
    private SocketReaderMeta meta;
	private SocketReaderData data;
	
	public SocketReader(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(SocketReaderMeta)smi;
		data=(SocketReaderData)sdi;

        try
        {
            Object[] r;
            
            if (first)
            {
                // Connect to the server socket (started during init)
                // Because the accept() call on the server socket can be called after we reached this code
                // it is best to build in a retry loop with a time-out here.
                // 
                long startTime = System.currentTimeMillis();
                boolean connected=false;
                KettleException lastException=null;
                
                //// timeout with retry until connected
                while ( !connected && (TIMEOUT_IN_SECONDS > (System.currentTimeMillis()-startTime)/1000) && !isStopped())
                {
                    try
                    {
                        int port = Integer.parseInt( environmentSubstitute(meta.getPort()) );
                        int bufferSize = Integer.parseInt( environmentSubstitute(meta.getBufferSize()));
                        
                        data.socket = new Socket(environmentSubstitute(meta.getHostname()), port);
                        connected=true;

                        if (meta.isCompressed())
                        {
                            data.outputStream = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(data.socket.getOutputStream()), bufferSize));
                            data.inputStream  = new DataInputStream(new BufferedInputStream(new GZIPInputStream(data.socket.getInputStream()), bufferSize));
                        }
                        else
                        {
                            data.outputStream = new DataOutputStream(new BufferedOutputStream(data.socket.getOutputStream(), bufferSize));
                            data.inputStream  = new DataInputStream(new BufferedInputStream(data.socket.getInputStream(), bufferSize));
                        }
                        lastException=null;
                    }
                    catch(Exception e)
                    {
                        lastException=new KettleException("Unable to open socket to server "+environmentSubstitute(meta.getHostname())+" port "+environmentSubstitute(meta.getPort()), e);
                    }
                    
                    if (lastException!=null) // Sleep for a second
                    {
                        Thread.sleep(1000);
                    }
                }
                
                if (lastException!=null)
                {
                    logError("Error initialising step: "+lastException.toString());
                    logError(Const.getStackTracker(lastException));
					if (data.socket!=null) {
						data.socket.shutdownInput();
						data.socket.shutdownOutput();
						data.socket.close();
						logError("Closed connection to data socket to "+environmentSubstitute(meta.getHostname())+" port "+environmentSubstitute(meta.getPort()));
					}

                    throw lastException;
                }
                else
                {
                    if (data.inputStream==null) throw new KettleException("Unable to connect to the SocketWriter in the "+TIMEOUT_IN_SECONDS+"s timeout period.");
                }
                
                
                data.rowMeta = new RowMeta(data.inputStream); // This is the metadata
                first=false;
            }
            r = data.rowMeta.readData(data.inputStream);
            
            incrementLinesInput();
            
            if (checkFeedback(getLinesInput())) logBasic(BaseMessages.getString(PKG, "SocketReader.Log.LineNumber")+getLinesInput()); //$NON-NLS-1$
            
            putRow(data.rowMeta, r);
        }
        catch(KettleEOFException e)
        {
            setOutputDone(); // finished reading.
            return false;
        }
        catch (Exception e)
        {
			if (data.socket!=null) {
				try {
					data.socket.shutdownInput();
					data.socket.shutdownOutput();
					data.socket.close();
					logError("Closed connection to data socket to "+environmentSubstitute(meta.getHostname())+" port "+environmentSubstitute(meta.getPort()));
				} catch (IOException e1) {
					logError("Failed to close connection to data socket to "+environmentSubstitute(meta.getHostname())+" port "+environmentSubstitute(meta.getPort()));
				}
			}
            throw new KettleException(e);
        }
        
		return true;
	}

    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(SocketReaderMeta)smi;
		data=(SocketReaderData)sdi;
		
		if (super.init(smi, sdi))
		{
            return true;
		}
		return false;
	}
    
    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
        // Ignore errors, we don't care
        // If we are here, it means all work is done
        // It's a lot of work to keep it all in sync for now we don't need to do that.
        // 
    	try { data.inputStream.close(); } catch(Exception e) {}
    	try { data.outputStream.close(); } catch(Exception e) {}
    	try {
    		data.socket.shutdownInput();
    		data.socket.shutdownOutput();
    		data.socket.close();
    	} catch(Exception e) {}

        super.dispose(smi, sdi);
    }

}