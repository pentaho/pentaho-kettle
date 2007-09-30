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
 
package be.ibridge.kettle.trans.step.http;

import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.httpclient.methods.GetMethod;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * Retrieves values from a database by calling database stored procedures or functions
 *  
 * @author Matt
 * @since 26-apr-2003
 *
 */

public class HTTP extends BaseStep implements StepInterface
{
	private HTTPMeta meta;
	private HTTPData data;
	
	public HTTP(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private void execHttp(Row row) throws KettleException
	{
        if (first)
		{
			first=false;
			data.argnrs=new int[meta.getArgumentField().length];
			
			for (int i=0;i<meta.getArgumentField().length;i++)
			{
				data.argnrs[i]=row.searchValueIndex(meta.getArgumentField()[i]);
				if (data.argnrs[i]<0)
				{
					logError(Messages.getString("HTTP.Log.ErrorFindingField")+meta.getArgumentField()[i]+"]"); //$NON-NLS-1$ //$NON-NLS-2$
					throw new KettleStepException(Messages.getString("HTTP.Exception.CouldnotFindField",meta.getArgumentField()[i])); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}

        Value result = callHttpService(row);
        row.addValue(result);
	}
	
	private Value callHttpService(Row row) throws KettleException
    {
      	String url = determineUrl(row);
      	try
        {
            logDetailed("Connecting to : ["+url+"]");
            
            // Prepare HTTP get
            // 
            HttpClient httpclient = new HttpClient();
            HttpMethod method = new GetMethod(url);

            // Execute request
            // 
            try
            {
                int result = httpclient.executeMethod(method);
                
                // The status code
                log.logDebug(toString(), "Response status code: " + result);
                
                // the response
                InputStream inputStream = method.getResponseBodyAsStream();
                StringBuffer bodyBuffer = new StringBuffer();
                int c;
                while ( (c=inputStream.read())!=-1) bodyBuffer.append((char)c);
                inputStream.close();
                
                String body = bodyBuffer.toString();
                log.logDebug(toString(), "Response body: "+body);
                
                return new Value(meta.getFieldName(), body);
            }
            finally
            {
                // Release current connection to the connection pool once you are done
                method.releaseConnection();
            }
        }
        catch(Exception e)
        {
            throw new KettleException("Unable to get result from specified URL :"+url, e);
        }
    }

    private String determineUrl(Row row) throws KettleException
    {
    	try
    	{
	        StringBuffer url = new StringBuffer(meta.getUrl()); // the base URL
	        
	        for (int i=0;i<data.argnrs.length;i++)
	        {
	            if (i==0 && url.indexOf("?")<0)
	            {
	                url.append('?');
	            }
	            else
	            {
	                url.append('&');
	            }
	        	url.append(URIUtil.encodeWithinQuery(meta.getArgumentParameter()[i]));
	        	url.append('=');
				url.append(URIUtil.encodeWithinQuery(row.getValue(data.argnrs[i]).toString(false)));
	        }
	        return url.toString();
	    }
	    catch(Exception e)
	    {
	        throw new KettleException("Unable to create URL.", e);
	    }
    }

    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(HTTPMeta)smi;
		data=(HTTPData)sdi;
		
		 boolean sendToErrorRow=false;
		 String errorMessage = null;

		Row r=getRow();       // Get row from input rowset & set row busy!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		    
		try
		{
			execHttp(r); // add new values to the row
			putRow(r);  // copy row to output rowset(s);
				
            if (checkFeedback(linesRead)) logBasic(Messages.getString("HTTP.LineNumber")+linesRead); //$NON-NLS-1$
		}
		catch(KettleException e)
		{
			if (getStepMeta().isDoingErrorHandling())
			{
		          sendToErrorRow = true;
		          errorMessage = e.toString();
			}
			else
			{
				logError(Messages.getString("HTTP.ErrorInStepRunning")+e.getMessage()); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				setOutputDone();  // signal end to receiver(s)
				return false;
			}
			

			if (sendToErrorRow)
			{
			   // Simply add this row to the error row
			   putError(r, 1, errorMessage, null, "HTTP001");
			}

		}
			
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(HTTPMeta)smi;
		data=(HTTPData)sdi;

		if (super.init(smi, sdi))
		{
		    return true;
		}
		return false;
	}
		
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (HTTPMeta)smi;
	    data = (HTTPData)sdi;
	    
	    super.dispose(smi, sdi);
	}

	//
	// Run is were the action happens!
	public void run()
	{
		logBasic(Messages.getString("HTTP.Log.StartingToRun")); //$NON-NLS-1$
		
		try
		{
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("HTTP.Log.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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
	
	public String toString()
	{
		return this.getClass().getName();
	}
}
