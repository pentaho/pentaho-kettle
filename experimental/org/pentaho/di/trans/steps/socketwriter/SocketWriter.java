 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
 
package org.pentaho.di.trans.steps.socketwriter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.exception.KettleException;
import org.pentaho.di.core.util.StringUtil;


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
                int bufferSize = Const.toInt( StringUtil.environmentSubstitute(meta.getBufferSize()), 1000);
                
                data.clientSocket = data.serverSocket.accept(); 
                
                if (meta.isCompressed())
                {
                    data.outputStream = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(data.clientSocket.getOutputStream()), bufferSize));
                    data.inputStream = new DataInputStream(new BufferedInputStream(new GZIPInputStream(data.clientSocket.getInputStream()), bufferSize));
                }
                else
                {
                    data.outputStream = new DataOutputStream(new BufferedOutputStream(data.clientSocket.getOutputStream(), bufferSize));
                    data.inputStream = new DataInputStream(new BufferedInputStream(data.clientSocket.getInputStream(), bufferSize));
                }
                
                data.flushInterval = Const.toInt( StringUtil.environmentSubstitute(meta.getFlushInterval()), 4000);
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
            linesOutput++;
            
            // flush every X rows
            if (linesOutput>0 && data.flushInterval>0 && (linesOutput%data.flushInterval)==0) data.outputStream.flush();

        }
        catch (Exception e)
        {
            logError("Error writing to socket : "+e.toString());
            logError("Failing row : "+r);
            logError("Stack trace: "+Const.CR+Const.getStackTracker(e));
            
            setErrors(1);
            stopAll();
            setOutputDone();
            return false;
        }

        if (checkFeedback(linesRead)) logBasic(Messages.getString("SocketWriter.Log.LineNumber")+linesRead); //$NON-NLS-1$
			
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
                int port = Integer.parseInt( StringUtil.environmentSubstitute(meta.getPort()) );
                data.serverSocket = new ServerSocket(port);
                
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
        try { data.inputStream.close();  } catch(Exception e) {}
        try { data.clientSocket.close(); } catch(Exception e) {}
        try { data.serverSocket.close(); } catch(Exception e) {}
        
        super.dispose(smi, sdi);
    }
	
	//
	// Run is were the action happens!
	public void run()
	{
		try
		{
			logBasic(Messages.getString("SocketWriter.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("SocketWriter.Log.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Const.getStackTracker(e));
            setErrors(1);
			stopAll();
		}
		finally
		{
			dispose(meta, data);
			logSummary();
			markStop();
		}
	}
}
