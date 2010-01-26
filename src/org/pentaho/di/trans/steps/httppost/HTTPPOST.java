 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Samatar Hassan and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Samatar Hassan.
 * The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 
package org.pentaho.di.trans.steps.httppost;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;



/**
 * Make a HTTP Post call
 *  
 * @author Samatar
 * @since 15-jan-2009
 *
 */

public class HTTPPOST extends BaseStep implements StepInterface
{
	
	private static Class<?> PKG = HTTPPOSTMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private static final String CONTENT_TYPE = "Content-type";
	private static final String CONTENT_TYPE_TEXT_XML = "text/xml";
	
	private HTTPPOSTMeta meta;
	private HTTPPOSTData data;
	
	public HTTPPOST(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	

	
	private Object[] callHTTPPOST(Object[] rowData) throws KettleException
    {
		// get dynamic url ?
		if(meta.isUrlInField()) data.realUrl=data.inputRowMeta.getString(rowData,data.indexOfUrlField);
 
      	try
        {
      		if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "HTTPPOST.Log.ConnectingToURL",data.realUrl));
      		
            // Prepare HTTP POST
            // 
            HttpClient HTTPPOSTclient = new HttpClient();
            PostMethod post = new PostMethod(data.realUrl);
            //post.setFollowRedirects(false); 
            
            // Specify content type and encoding
            // If content encoding is not explicitly specified
            // ISO-8859-1 is assumed
            if(!data.contentTypeHeaderOverwrite) {  // can be overwritten now
	            if(Const.isEmpty(data.realEncoding)) {
	            	post.setRequestHeader(CONTENT_TYPE, CONTENT_TYPE_TEXT_XML);
	            	if(log.isDebug()) log.logDebug(toString(), BaseMessages.getString(PKG, "HTTPPOST.Log.HeaderValue",CONTENT_TYPE,CONTENT_TYPE_TEXT_XML));
	            } else {
	            	post.setRequestHeader(CONTENT_TYPE, CONTENT_TYPE_TEXT_XML+"; "+data.realEncoding);
	            	if(log.isDebug()) log.logDebug(toString(), BaseMessages.getString(PKG, "HTTPPOST.Log.HeaderValue",CONTENT_TYPE,CONTENT_TYPE_TEXT_XML+"; "+data.realEncoding));
	            }
            }

            // HEADER PARAMETERS
            if(data.useHeaderParameters) {
	            // set header parameters that we want to send 
		        for (int i=0;i<data.header_parameters_nrs.length;i++)
		        {
	        		post.addRequestHeader(data.headerParameters[i].getName(),
	        				data.inputRowMeta.getString(rowData,data.header_parameters_nrs[i]));
	        		if(log.isDebug()) log.logDebug(toString(), BaseMessages.getString(PKG, "HTTPPOST.Log.HeaderValue",data.headerParameters[i].getName(),data.inputRowMeta.getString(rowData,data.header_parameters_nrs[i])));
		        }
	        }
            
            // BODY PARAMETERS
            if(data.useBodyParameters) {
	            // set body parameters that we want to send 
		        for (int i=0;i<data.body_parameters_nrs.length;i++)
		        {
	        		data.bodyParameters[i].setValue(data.inputRowMeta.getString(rowData,data.body_parameters_nrs[i]));
	        		if(log.isDebug()) log.logDebug(toString(), BaseMessages.getString(PKG, "HTTPPOST.Log.BodyValue",data.bodyParameters[i].getName(),data.inputRowMeta.getString(rowData,data.body_parameters_nrs[i])));
		        }
		        post.setRequestBody(data.bodyParameters);
	        }
            
            

            // QUERY PARAMETERS
            if(data.useQueryParameters)
            {
            	 for (int i=0;i<data.query_parameters_nrs.length;i++)
 		         {
 		        	data.queryParameters[i].setValue(data.inputRowMeta.getString(rowData,data.query_parameters_nrs[i]));
 		        	if(log.isDebug()) log.logDebug(toString(), BaseMessages.getString(PKG, "HTTPPOST.Log.QueryValue",data.queryParameters[i].getName(),data.inputRowMeta.getString(rowData,data.query_parameters_nrs[i])));
 		         }
            	 post.setQueryString(data.queryParameters); 
            }

            // Set request entity?
            if(data.indexOfRequestEntity>=0)
            {
            	String tmp=data.inputRowMeta.getString(rowData,data.indexOfRequestEntity);
                // Request content will be retrieved directly
                // from the input stream
                // Per default, the request content needs to be buffered
                // in order to determine its length.
                // Request body buffering can be avoided when
                // content length is explicitly specified
            	
            	if(meta.isPostAFile())
            	{
     		       File input = new File(tmp);
     		       post.setRequestEntity(new InputStreamRequestEntity(new FileInputStream(input), input.length()));
            	}
            	else
            	{
            		post.setRequestEntity(new InputStreamRequestEntity(new ByteArrayInputStream(tmp.getBytes()), tmp.length())); 
            	}
            }
            
            // Execute request
            // 
            InputStreamReader inputStreamReader=null;
            Object[] newRow = null;
            try
            {
            	// Execute the POST method
                int statusCode = HTTPPOSTclient.executeMethod(post);
                
                // Display status code
                if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "HTTPPOST.Log.ResponseCode",""+statusCode));
                String body=null;
                if( statusCode != -1 )
                {
                	 // guess encoding
                    String encoding = post.getResponseHeader("Content-Type").getValue().replaceFirst("^.*;\\s*charset\\s*=\\s*","").trim(); 
                    if (encoding == null) encoding = "ISO-8859-1"; 
                    
                    if(log.isDebug()) log.logDebug(toString(), BaseMessages.getString(PKG, "HTTPPOST.Log.Encoding",encoding));

	                // the response
                    inputStreamReader = new InputStreamReader(post.getResponseBodyAsStream(),encoding); 
                    StringBuffer bodyBuffer = new StringBuffer(); 
                     
                    int c;
                    while ( (c=inputStreamReader.read())!=-1) {
                    	bodyBuffer.append((char)c);
                    }
                    inputStreamReader.close(); 
	                
	                // Display response
	                body = bodyBuffer.toString();
	                
	                if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "HTTPPOST.Log.ResponseBody",body));
                }
                int returnFieldsOffset=data.inputRowMeta.size();
                if (!Const.isEmpty(meta.getFieldName())) {
                	newRow=RowDataUtil.addValueData(rowData, returnFieldsOffset, body);
                	returnFieldsOffset++;
                }
                
                if (!Const.isEmpty(meta.getResultCodeFieldName())) {
                	newRow=RowDataUtil.addValueData(rowData, returnFieldsOffset, new Long(statusCode));
                }                
            }
            finally
            {
            	if(inputStreamReader!=null) inputStreamReader.close(); 
                // Release current connection to the connection pool once you are done
            	post.releaseConnection();
            }
            return newRow;
        }
        catch(Exception e)
        {
            throw new KettleException(BaseMessages.getString(PKG, "HTTPPOST.Error.CanNotReadURL",data.realUrl), e);

        }
    }
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(HTTPPOSTMeta)smi;
		data=(HTTPPOSTData)sdi;
		
		Object[] r=getRow();       // Get row from input rowset & set row busy!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		if ( first )
		{
			first=false;
			data.inputRowMeta = getInputRowMeta();
			data.outputRowMeta=getInputRowMeta().clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			
			if(meta.isUrlInField())
			{
				if(Const.isEmpty(meta.getUrlField()))
				{
					logError(BaseMessages.getString(PKG, "HTTPPOST.Log.NoField"));
					throw new KettleException(BaseMessages.getString(PKG, "HTTPPOST.Log.NoField"));
				}
				
				// cache the position of the field			
				if (data.indexOfUrlField<0)
				{	
					String realUrlfieldName=environmentSubstitute(meta.getUrlField());
					data.indexOfUrlField =data.inputRowMeta.indexOfValue((realUrlfieldName));
					if (data.indexOfUrlField<0)
					{
						// The field is unreachable !
						logError(BaseMessages.getString(PKG, "HTTPPOST.Log.ErrorFindingField",realUrlfieldName)); 
						throw new KettleException(BaseMessages.getString(PKG, "HTTPPOST.Exception.ErrorFindingField",realUrlfieldName)); 
					}
				}
			}else
			{
				data.realUrl=environmentSubstitute(meta.getUrl());
			}
			// set body parameters
			int nrargs=meta.getArgumentField().length;
			if(nrargs>0)
			{
				data.useBodyParameters=false;
				data.useHeaderParameters=false;
				data.contentTypeHeaderOverwrite=false;
				int nrheader=0;
				int nrbody=0;
				for (int i=0;i<nrargs;i++) // split into body / header
				{
					if (meta.getArgumentHeader()[i]) {
						data.useHeaderParameters=true; // at least one header parameter
						nrheader++;
					} else {
						data.useBodyParameters=true; // at least one body parameter
						nrbody++;
					}
				}
				data.header_parameters_nrs=new int[nrheader];
				data.headerParameters = new NameValuePair[nrheader];
				data.body_parameters_nrs=new int[nrbody];
				data.bodyParameters = new NameValuePair[nrbody];
				int posHeader=0;
				int posBody=0;				
				for (int i=0;i<nrargs;i++)
				{
					int fieldIndex=data.inputRowMeta.indexOfValue(meta.getArgumentField()[i]);
					if (fieldIndex<0)
					{
						logError(BaseMessages.getString(PKG, "HTTPPOST.Log.ErrorFindingField")+meta.getArgumentField()[i]+"]"); //$NON-NLS-1$ //$NON-NLS-2$
						throw new KettleStepException(BaseMessages.getString(PKG, "HTTPPOST.Exception.CouldnotFindField",meta.getArgumentField()[i])); //$NON-NLS-1$ //$NON-NLS-2$
					}
					if (meta.getArgumentHeader()[i]) {
						data.header_parameters_nrs[posHeader]=fieldIndex;
						data.headerParameters[posHeader]= new NameValuePair(environmentSubstitute(meta.getArgumentParameter()[i]),
								data.outputRowMeta.getString(r,data.header_parameters_nrs[posHeader]));
						posHeader++;
						if (CONTENT_TYPE.equalsIgnoreCase(meta.getArgumentField()[i])) {
							data.contentTypeHeaderOverwrite=true; // Content-type will be overwritten
						}
					} else {
						data.body_parameters_nrs[posBody]=fieldIndex;
						data.bodyParameters[posBody]= new NameValuePair(environmentSubstitute(meta.getArgumentParameter()[i]),
								data.outputRowMeta.getString(r,data.body_parameters_nrs[posBody]));
						posBody++;
					}
				}
			}
			// set query parameters
			int nrQuery=meta.getQueryField().length;
			if(nrQuery>0)
			{
				data.useQueryParameters=true;
				data.query_parameters_nrs=new int[nrQuery];
				data.queryParameters = new NameValuePair[nrQuery];
				for (int i=0;i<nrQuery;i++)
				{
					data.query_parameters_nrs[i]=data.inputRowMeta.indexOfValue(meta.getQueryField()[i]);
					if (data.query_parameters_nrs[i]<0)
					{
						logError(BaseMessages.getString(PKG, "HTTPPOST.Log.ErrorFindingField")+meta.getQueryField()[i]+"]"); //$NON-NLS-1$ //$NON-NLS-2$
						throw new KettleStepException(BaseMessages.getString(PKG, "HTTPPOST.Exception.CouldnotFindField",meta.getQueryField()[i])); //$NON-NLS-1$ //$NON-NLS-2$
					}
					data.queryParameters[i]= new NameValuePair(environmentSubstitute(meta.getQueryParameter()[i]),
							data.outputRowMeta.getString(r,data.query_parameters_nrs[i]));
				}
			}
			// set request entity?
			if(!Const.isEmpty(meta.getRequestEntity()))
			{
				data.indexOfRequestEntity=data.inputRowMeta.indexOfValue(environmentSubstitute(meta.getRequestEntity()));
				if (data.indexOfRequestEntity<0)
				{
					throw new KettleStepException(BaseMessages.getString(PKG, "HTTPPOST.Exception.CouldnotFindRequestEntityField",meta.getRequestEntity())); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			data.realEncoding=environmentSubstitute(meta.getEncoding());	
		} // end if first
		
		try
		{
	        Object[] outputRowData = callHTTPPOST(r);
        	putRow(data.outputRowMeta, outputRowData);  // copy row to output rowset(s);
			
            if (checkFeedback(getLinesRead())) 
            {
            	if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "HTTPPOST.LineNumber")+getLinesRead()); //$NON-NLS-1$
            }
		}
		catch(KettleException e)
		{
			 boolean sendToErrorRow=false;
			 String errorMessage = null;
			 
			if (getStepMeta().isDoingErrorHandling())
			{
		         sendToErrorRow = true;
		         errorMessage = e.toString();
			}
			else
			{
				logError(BaseMessages.getString(PKG, "HTTPPOST.ErrorInStepRunning")+e.getMessage()); //$NON-NLS-1$
				setErrors(1);
                logError(Const.getStackTracker(e));
				stopAll();
				setOutputDone();  // signal end to receiver(s)
				return false;
			}

			if (sendToErrorRow)
			{
			   // Simply add this row to the error row
			   putError(getInputRowMeta(), r, 1, errorMessage, null, "HTTPPOST001");
			}

		}
			
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(HTTPPOSTMeta)smi;
		data=(HTTPPOSTData)sdi;

		if (super.init(smi, sdi))
		{
		    return true;
		}
		return false;
	}
		
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (HTTPPOSTMeta)smi;
	    data = (HTTPPOSTData)sdi;
	    
	    super.dispose(smi, sdi);
	}
}
