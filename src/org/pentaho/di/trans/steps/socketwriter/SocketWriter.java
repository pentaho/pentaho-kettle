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
 
package org.pentaho.di.trans.steps.socketwriter;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.util.zip.GZIPOutputStream;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * Write data to a TCP/IP socket read by SocketReader.
 * The data being sent over the socket is one serialized Row object including metadata and then a series of serialized rows, data only. 
 * 
 * This part of the SocketWriter/SocketRead pair contains the ServerSocket.
 * 
 * @author Matt
 * @since 1-dec-2006
 */
public class SocketWriter extends BaseStep implements StepInterface
{
	private static Class<?> PKG = SocketWriterMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private SocketWriterMeta meta;
	private SocketWriterData data;
	
	public SocketWriter(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(SocketWriterMeta)smi;
		data=(SocketWriterData)sdi;

        try
        {
            if (first)
            {
                int bufferSize = Const.toInt( environmentSubstitute(meta.getBufferSize()), 1000);
                
                data.clientSocket = data.serverSocket.accept(); 
                
                if (meta.isCompressed())
                {
                    data.outputStream = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(data.clientSocket.getOutputStream()), bufferSize));
                }
                else
                {
                    data.outputStream = new DataOutputStream(new BufferedOutputStream(data.clientSocket.getOutputStream(), bufferSize));
                }
                
                data.flushInterval = Const.toInt( environmentSubstitute(meta.getFlushInterval()), 4000);
            }
        }
        catch (Exception e)
        {
            logError("Error accepting from socket : "+e.toString());
            logError("Stack trace: "+Const.CR+Const.getStackTracker(e));
            
            setErrors(1);
            stopAll();
            setOutputDone();
            return false;
        }
        
		Object[] r=getRow();    // get row, set busy!
        // Input rowMeta is automatically set, available when needed
        
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
        
        try
        {
            if (first)
            {
                getInputRowMeta().writeMeta(data.outputStream);
                first=false;
            }
            getInputRowMeta().writeData(data.outputStream, r);
            incrementLinesOutput();
            
            // flush every X rows
            if (getLinesOutput()>0 && data.flushInterval>0 && (getLinesOutput()%data.flushInterval)==0) data.outputStream.flush();

        }
        catch (Exception e)
        {
            logError("Error writing to socket : "+e.toString());
            logError("Failing row : "+getInputRowMeta().getString(r));
            logError("Stack trace: "+Const.CR+Const.getStackTracker(e));
            
            setErrors(1);
            stopAll();
            setOutputDone();
            return false;
        }

        if (checkFeedback(getLinesRead())) logBasic(BaseMessages.getString(PKG, "SocketWriter.Log.LineNumber")+getLinesRead()); //$NON-NLS-1$
			
		return true;
	}

    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(SocketWriterMeta)smi;
		data=(SocketWriterData)sdi;
		
		if (super.init(smi, sdi))
		{
            try
            {
                int port = Integer.parseInt( environmentSubstitute(meta.getPort()) );
                data.serverSocket = getTrans().getSocketRepository().openServerSocket(port, getTransMeta().getName()+" - "+this.toString());
                
                return true;
            }
            catch(Exception e)
            {
                logError("Error creating server socket: "+e.toString());
                logError(Const.getStackTracker(e));
            }
		}
		return false;
	}
    
    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
        // Ignore errors, we don't care
        // If we are here, it means all work is done
        // It's a lot of work to keep it all in sync for now we don't need to do that.
        // 
        try { data.outputStream.close(); } catch(Exception e) {}
        try { data.clientSocket.close(); } catch(Exception e) {}
        try { data.serverSocket.close(); } catch(Exception e) {}
        
        super.dispose(smi, sdi);
    }

}