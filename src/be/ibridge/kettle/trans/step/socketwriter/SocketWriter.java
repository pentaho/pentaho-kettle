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
 
package be.ibridge.kettle.trans.step.socketwriter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * Do nothing.  Pass all input data to the next steps.
 * 
 * @author Matt
 * @since 2-jun-2003
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

		Row r=getRow();    // get row, set busy!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
        
        try
        {
            if (first)
            {
                data.clientSocket = data.serverSocket.accept(); 
                data.outputStream = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(data.clientSocket.getOutputStream()), 100000));
                data.inputStream = new DataInputStream(new BufferedInputStream(new GZIPInputStream(data.clientSocket.getInputStream()), 1000000));
                
                r.write(data.outputStream);
                first=false;
            }
            r.writeData(data.outputStream);
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
                
                data.serverSocket       = new ServerSocket(port);
                
    		    // Add init code here.
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
        try { data.inputStream.close(); } catch(Exception e) {}
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
