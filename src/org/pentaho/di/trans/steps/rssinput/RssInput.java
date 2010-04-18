/*************************************************************************************** 
 * Copyright (C) 2007 Samatar.  All rights reserved. 
 * This software was developed by Samatar and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is Samatar.  
 * The Initial Developer is Samatar.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/


package org.pentaho.di.trans.steps.rssinput;

import it.sauronsoftware.feed4j.FeedParser;
import it.sauronsoftware.feed4j.bean.FeedItem;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.ui.database.Messages;


/**
 * Read data from RSS and writes these to one or more output streams.
 * 
 * @author Samatar
 * @since 13-10-2007
 */
public class RssInput extends BaseStep implements StepInterface
{
	private static Class<?> PKG = RssInput.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private RssInputMeta meta;
	private RssInputData data;
	
	public RssInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private boolean readNextUrl()
	{
		try
		{
			if(meta.urlInField())
			{
				 data.readrow= getRow();  // Grab another row ...
				 if(data.readrow==null)
				 {
		            if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "RssInput.Log.FinishedProcessing"));
		            return false; 
				 }
				 if(first)
				 {
					first=false;
					data.inputRowMeta = getInputRowMeta();
		            data.outputRowMeta = data.inputRowMeta.clone();
		            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
		            
		            // Get total previous fields
		            data.totalpreviousfields=data.inputRowMeta.size();

					// Create convert meta-data objects that will contain Date & Number formatters
		            data.convertRowMeta = data.outputRowMeta.clone();
		            for (int i=0;i<data.convertRowMeta.size();i++) data.convertRowMeta.getValueMeta(i).setType(ValueMetaInterface.TYPE_STRING);
		  
		            // For String to <type> conversions, we allocate a conversion meta data row as well...
					//
					data.convertRowMeta = data.outputRowMeta.clone();
					for (int i=0;i<data.convertRowMeta.size();i++) {
						data.convertRowMeta.getValueMeta(i).setType(ValueMetaInterface.TYPE_STRING);            
					}
					
					// Check is URL field is provided
					if (Const.isEmpty(meta.getUrlFieldname()))
					{
						logError(BaseMessages.getString(PKG, "RssInput.Log.UrlFieldNameMissing"));
						throw new KettleException(BaseMessages.getString(PKG, "RssInput.Log.UrlFieldNameMissing"));
					}
					
					// cache the position of the field			
					if (data.indexOfUrlField<0)
					{	
						data.indexOfUrlField =data.inputRowMeta.indexOfValue(meta.getUrlFieldname());
						if (data.indexOfUrlField<0)
						{
							// The field is unreachable !
							logError(BaseMessages.getString(PKG, "RssInput.Log.ErrorFindingField")+ "[" + meta.getUrlFieldname()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
							throw new KettleException(BaseMessages.getString(PKG, "RssInput.Exception.ErrorFindingField",meta.getUrlFieldname())); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}		
						
				}
				// get URL field value
				data.currenturl= data.inputRowMeta.getString(data.readrow,data.indexOfUrlField);
				 
			}else{
				if(data.last_url) return false;
	            if (data.urlnr>=data.urlsize) // finished processing!
	            {
	            	if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "RssInput.Log.FinishedProcessing"));
	                return false;
	            }
				// Is this the last url?
	            data.last_url = ( data.urlnr==data.urlsize-1);
				data.currenturl =environmentSubstitute(meta.getUrl()[data.urlnr]) ;
			}
            

			if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "RssInput.Log.ReadingUrl", data.currenturl));
			
			URL rss = new URL(data.currenturl);
			data.feed = FeedParser.parse(rss);
			data.itemssize = data.feed.getItemCount();
			
			// Move url pointer ahead!
			data.urlnr++;
			data.itemsnr=0;

			if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "RssInput.Log.UrlReaded", data.currenturl,data.itemssize));

		}
		catch(Exception e)
		{
			logError(BaseMessages.getString(PKG, "RssInput.Log.UnableToReadUrl", ""+data.urlnr, data.currenturl, e.toString()));
			stopAll();
			setErrors(1);
			logError(Const.getStackTracker(e));
			return false;
		}
		return true;
	}
	
	private Object[] getOneRow()  throws KettleException
	{

		if(meta.urlInField())
		{
			while ((data.itemsnr>=data.itemssize))
			{
		        if (!readNextUrl())
		        {
		            return null;
		        }
			} 
		}else
		{
			while ((data.itemsnr>=data.itemssize) || data.feed==null)
			{
		        if (!readNextUrl())
		        {
		            return null;
		        }
			}
		}
		
		// Create new row
		Object[] outputRowData = buildEmptyRow();
			
		if (data.readrow!=null) System.arraycopy(data.readrow, 0, outputRowData, 0, data.readrow.length);
				
		try{
			
			// Get item
			FeedItem item = data.feed.getItem(data.itemsnr);

			if((Const.isEmpty(meta.getRealReadFrom()) 
					|| (!Const.isEmpty(meta.getRealReadFrom()) 
							&& item.getPubDate().compareTo(data.readfromdatevalide)>0))) 
			{
						
				// Execute for each Input field...
				for (int j=0;j<meta.getInputFields().length;j++)
				{
					RssInputField RSSInputField = meta.getInputFields()[j];
							
					String valueString=null;
					switch (RSSInputField.getColumn())
					{
						case RssInputField.COLUMN_TITLE:
							valueString=item.getTitle();
							break;
						case RssInputField.COLUMN_LINK:
							valueString=item.getLink()== null ? "" :item.getLink().toString();
							break;
						case RssInputField.COLUMN_DESCRIPTION_AS_TEXT:
							valueString=item.getDescriptionAsText();
							break;
						case RssInputField.COLUMN_DESCRIPTION_AS_HTML:
							valueString=item.getDescriptionAsHTML();
							break;
						case RssInputField.COLUMN_COMMENTS:
							valueString=item.getComments()== null ? "": item.getComments().toString();
							break;
						case RssInputField.COLUMN_GUID:
							valueString=item.getGUID();
							break;
						case RssInputField.COLUMN_PUB_DATE:
							valueString=item.getPubDate()== null ? "":DateFormat.getInstance().format(item.getPubDate());
							break;
						default:
							break;
					}

							
					// Do trimming
					switch (RSSInputField.getTrimType())
					{
						case RssInputField.TYPE_TRIM_LEFT:
							valueString = Const.ltrim(valueString);
							break;
						case RssInputField.TYPE_TRIM_RIGHT:
							valueString = Const.rtrim(valueString);
							break;
						case RssInputField.TYPE_TRIM_BOTH:
							valueString = Const.trim(valueString);
							break;
						default:
							break;
					}
					
							
					// Do conversions
					//
					ValueMetaInterface targetValueMeta = data.outputRowMeta.getValueMeta(data.totalpreviousfields+j);
					ValueMetaInterface sourceValueMeta = data.convertRowMeta.getValueMeta(data.totalpreviousfields+j);
					outputRowData[data.totalpreviousfields+j] = targetValueMeta.convertData(sourceValueMeta, valueString);

					// Do we need to repeat this field if it is null?
					if (meta.getInputFields()[j].isRepeated())
					{
						if (data.previousRow!=null && Const.isEmpty(valueString))
						{
							outputRowData[data.totalpreviousfields+j] = data.previousRow[data.totalpreviousfields+j];
						}
					}
							
				} // end of loop over fields ...
						
				int rowIndex = data.nrInputFields;
				
				// See if we need to add the url to the row...
				 if (meta.includeUrl()) {
					outputRowData[data.totalpreviousfields+rowIndex++] = data.currenturl;
				}
				 // See if we need to add the row number to the row...  
				if (meta.includeRowNumber())
		        {
		            outputRowData[data.totalpreviousfields+rowIndex++] = new Long(data.rownr);
		        }
				
				RowMetaInterface irow = getInputRowMeta();
				
				data.previousRow = irow==null?outputRowData:(Object[])irow.cloneRow(outputRowData); // copy it to make
				// surely the next step doesn't change it in between...
				
				data.rownr++; 
				
				putRow(data.outputRowMeta, outputRowData);  // copy row to output rowset(s);

				 if (meta.getRowLimit()>0 && data.rownr>meta.getRowLimit())  // limit has been reached: stop now.
			     {
					 return null;	
			     }	
			}
			data.itemsnr++;
		}
		catch(Exception e)
		{
			throw new KettleException(e);

		} 
		return outputRowData;
	}
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		 Object[] outputRowData=null;

		try
		{
			 // Grab a row
			 outputRowData=getOneRow();
			 if (outputRowData==null)
		     {
		        setOutputDone();  // signal end to receiver(s)
		        return false; // end of data or error.
		     }
		}
		catch(Exception e)
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
				logError(BaseMessages.getString(PKG, "RssInput.Exception.Run",e.toString()));
				logError(Const.getStackTracker(e));
				setErrors(1);
				throw new KettleException(e);
		
			}
			if (sendToErrorRow)
			{
			   // Simply add this row to the error row
			   putError(getInputRowMeta(), outputRowData, 1, errorMessage, null, "RssInput001");
			}

		}

		 return true;
	
	}
	/**
	 * Build an empty row based on the meta-data.
	 * 
	 * @return
	 */
	private Object[] buildEmptyRow()
	{
       Object[] rowData = RowDataUtil.allocateRowData(data.outputRowMeta.size());

	    return rowData;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(RssInputMeta)smi;
		data=(RssInputData)sdi;
		
		if (super.init(smi, sdi))
		{
			if (meta.includeRowNumber() && Const.isEmpty(meta.getRowNumberField()))
		    {
				logError(Messages.getString("RssInput.Error.RowNumberFieldMissing"));
				return false;
		    }
			if (meta.includeUrl() && Const.isEmpty(meta.geturlField()))
		    {
				logError(Messages.getString("RssInput.Error.UrlFieldMissing"));
				return false;
		    }
			
			if(!Const.isEmpty(meta.getReadFrom()))
			{
				// Let's check validity of the read from date
				try
				{
					SimpleDateFormat fdrss = new SimpleDateFormat("yyyy-MM-dd");
					fdrss.setLenient(false);
					data.readfromdatevalide = fdrss.parse(meta.getRealReadFrom());	
				}
				catch (Exception e)
				{
					logError("can not validate ''Read From date'' : " + environmentSubstitute(meta.getReadFrom()));
					return false;
				}	
			}
			if(meta.urlInField())
			{
				if (meta.getUrl()==null && meta.getUrl().length==0)
			    {
					logError(BaseMessages.getString(PKG, "RssInput.Log.UrlMissing"));
					return false;
				}
				
			}else{
				data.urlsize=meta.getUrl().length;
				try{
					// Create the output row meta-data
		            data.outputRowMeta = new RowMeta();
	
					meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
					
					// Create convert meta-data objects that will contain Date & Number formatters
		            data.convertRowMeta = data.outputRowMeta.clone();
		            for (int i=0;i<data.convertRowMeta.size();i++) data.convertRowMeta.getValueMeta(i).setType(ValueMetaInterface.TYPE_STRING);
		  
		            // For String to <type> conversions, we allocate a conversion meta data row as well...
					//
					data.convertRowMeta = data.outputRowMeta.clone();
					for (int i=0;i<data.convertRowMeta.size();i++) {
						data.convertRowMeta.getValueMeta(i).setType(ValueMetaInterface.TYPE_STRING);            
					}
				}
				catch(Exception e)
				{
					logError("Error initializing step: "+e.toString());
					logError(Const.getStackTracker(e));
					return false;
				}
			
			}
			
			data.rownr = 1L;
			data.urlnr=0;
			
			data.nrInputFields=meta.getInputFields().length;
			
			return true;
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(RssInputMeta)smi;
		data=(RssInputData)sdi;
		if(data.feed!=null) data.feed=null;

		super.dispose(smi, sdi);
	}
	
}