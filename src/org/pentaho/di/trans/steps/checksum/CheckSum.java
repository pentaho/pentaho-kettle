/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Samatar Hassan 
 * The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans.steps.checksum;

import java.security.MessageDigest;
import java.util.zip.CRC32;
import java.util.zip.Adler32;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * Caculate a checksum for each row.
 * 
 * @author Samatar
 * @since 30-06-2008
 */

public class CheckSum extends BaseStep implements StepInterface
{
	private CheckSumMeta meta;
	private CheckSumData data;
	
	public CheckSum(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(CheckSumMeta)smi;
		data=(CheckSumData)sdi;

		Object[] r=getRow();    // get row, set busy!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		
		if(first)
		{
			first=false;
			
			data.outputRowMeta = getInputRowMeta().clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			
			if(meta.getFieldName()==null || meta.getFieldName().length>0)
			{
				data.fieldnrs=new int[meta.getFieldName().length];
				
				for (int i=0;i<meta.getFieldName().length;i++)
				{
					data.fieldnrs[i]=getInputRowMeta().indexOfValue( meta.getFieldName()[i] );
					if (data.fieldnrs[i]<0)
					{
						logError(Messages.getString("CheckSum.Log.CanNotFindField",meta.getFieldName()[i]));
						throw new KettleException(Messages.getString("CheckSum.Log.CanNotFindField",meta.getFieldName()[i]));
					}
	 			}
			}else
			{
				data.fieldnrs=new int[r.length];
				for(int i=0;i<r.length;i++)
				{
					data.fieldnrs[i]=i;
				}
			}
			data.fieldnr=data.fieldnrs.length;

		} // end if first
		
        boolean sendToErrorRow=false;
        String errorMessage = null;
        Object[] outputRowData=null;
        
		try{
			if(meta.getCheckSumType().equals("ADLER32") || meta.getCheckSumType().equals("CRC32"))
			{
				// get checksum 
				Long checksum=calculCheckSum(r);
				outputRowData =RowDataUtil.addValueData(r, getInputRowMeta().size(),checksum);
			}else
			{
				// get checksum 
				String checkSum=createCheckSum(r);
				outputRowData =RowDataUtil.addValueData(r, getInputRowMeta().size(),checkSum);
			}
			
			 //	add new values to the row.
			putRow(data.outputRowMeta, outputRowData);  // copy row to output rowset(s);
		}
	   catch(Exception e)
        {
        	if (getStepMeta().isDoingErrorHandling())
        	{
                  sendToErrorRow = true;
                  errorMessage = e.toString();
        	}
        	else
        	{
	            logError(Messages.getString("CheckSum.ErrorInStepRunning")+e.getMessage()); //$NON-NLS-1$
	            setErrors(1);
	            stopAll();
	            setOutputDone();  // signal end to receiver(s)
	            return false;
        	}
        	if (sendToErrorRow)
        	{
        	   // Simply add this row to the error row
        	   putError(getInputRowMeta(), r, 1, errorMessage, meta.getResultFieldName(), "CheckSum001");
        	}
        }
		return true;
	}
	private String createCheckSum(Object[] r) throws Exception
	{
		String retval=null;
		StringBuffer Buff = new StringBuffer();
    	
    	// Loop through fields
		for(int i=0;i<data.fieldnr;i++)
		{	
			String fieldvalue=getInputRowMeta().getString(r,data.fieldnrs[i]);
			Buff.append(fieldvalue);
		}

		MessageDigest digest = MessageDigest.getInstance("MD5");
		digest.update(Buff.toString().getBytes());
		byte[] hash = digest.digest();

		retval= getString(hash);
	
		return retval;
	}
	private static String getString( byte[] bytes ) {
        StringBuffer sb = new StringBuffer();
        for( int i=0; i<bytes.length; i++ ) {
            byte b = bytes[ i ];
            sb.append( ( int )( 0x00FF & b ) );
            if( i+1 <bytes.length ) {
                sb.append( "-" );
            }
        }
        return sb.toString();
    }
	private Long calculCheckSum(Object[] r) throws Exception
	{
		Long retval;
		StringBuffer Buff = new StringBuffer();
    	
    	// Loop through fields
		for(int i=0;i<data.fieldnr;i++)
		{	
			String fieldvalue=getInputRowMeta().getString(r,data.fieldnrs[i]);
			Buff.append(fieldvalue);
		}

		if(meta.getCheckSumType().equals("CRC32"))
    	{
    		CRC32 crc32= new CRC32();
    		crc32.update(Buff.toString().getBytes());
    		retval =new Long(crc32.getValue()); 
		}else
    	{
    		Adler32 adler32= new java.util.zip.Adler32();
    		adler32.update(Buff.toString().getBytes());
    		retval =new Long(adler32.getValue()); 
    	}
		 
		return retval;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(CheckSumMeta)smi;
		data=(CheckSumData)sdi;
		
		if (super.init(smi, sdi))
		{
        	if(Const.isEmpty(meta.getResultFieldName()))
        	{
        		log.logError(toString(), Messages.getString("CheckSum.Error.ResultFieldMissing"));
        		return false;
        	}
		    return true;
		}
		return false;
	}
	
	//
	// Run is were the action happens!
	public void run()
	{
    	BaseStep.runStepThread(this, meta, data);
	}
}
