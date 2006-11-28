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
 
package be.ibridge.kettle.trans.step.socketreader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleEOFException;
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

public class SocketReader extends BaseStep implements StepInterface
{
	public static final String STRING_FINISHED = "Finished";
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
            Row r;
            
            if (first)
            {
                // Connect to the server socket (started during init)
                
                try
                {
                    int port = Integer.parseInt( StringUtil.environmentSubstitute(meta.getPort()) );
                    
                    data.socket       = new Socket(StringUtil.environmentSubstitute(meta.getHostname()), port);
                    data.outputStream = new DataOutputStream(new GZIPOutputStream(data.socket.getOutputStream()));
                    data.inputStream  = new DataInputStream(new GZIPInputStream(data.socket.getInputStream()));
                }
                catch(Exception e)
                {
                    logError("Error initialising step: "+e.toString());
                    logError(Const.getStackTracker(e));
                    throw new KettleException("Unable to open socket to server "+StringUtil.environmentSubstitute(meta.getHostname())+" port "+StringUtil.environmentSubstitute(meta.getPort()), e);
                }
                
                data.row = new Row(data.inputStream); // This is the metadata
                first=false;
            }
            r = new Row(data.inputStream, data.row.size(), data.row);
            linesInput++;
            
            if (checkFeedback(linesInput)) logBasic(Messages.getString("SocketReader.Log.LineNumber")+linesInput); //$NON-NLS-1$
            
            putRow(r);
        }
        catch(KettleEOFException e)
        {
            // Send "Finished" message back to server.
            try
            {
                logBasic("Sending finished string to writer (we have read all the data)");
                data.outputStream.writeUTF(STRING_FINISHED);
                logBasic("Finished string was sent.");
                
                logBasic("Waiting a few seconds before finishing this step.");
                try
                {
                    // Allow a few seconds for the server to read this message and draw it's own conclusions.
                    // If we don't sleep, this will fall through to the dispose() method and 
                    // kill all connections, including the output stream.  
                    // I'm sure there is a more ellegant way of doing this, but for now we wait a bit :-)
                    //
                    Thread.sleep(20000); 
                }
                catch(InterruptedException ie)
                {
                    
                }
            }
            catch(IOException ioe)
            {
                logError("Unable to send 'finished' message back to server: "+ioe.toString());
                setErrors(1);
            }
            
            setOutputDone(); // finished reading.
            return false;
        }
        catch (Exception e)
        {
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
        logBasic("Closing input stream.");
        try { data.inputStream.close(); } catch(IOException e) {}
        logBasic("Closing output stream.");
        try { data.outputStream.close(); } catch(IOException e) {}
        logBasic("Closing socket.");
        try { data.socket.close(); } catch(IOException e) {}
    }
	
	//
	// Run is were the action happens!
	public void run()
	{
		try
		{
			logBasic(Messages.getString("SocketReader.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("SocketReader.Log.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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
