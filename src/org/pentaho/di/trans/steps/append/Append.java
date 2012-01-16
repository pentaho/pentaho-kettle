/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.append;

import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleRowException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;


/**
 * Read all rows from a hop until the end, and then read the rows from another hop.
 * 
 * @author Sven Boden
 * @since  3-june-2007
 */
public class Append extends BaseStep implements StepInterface
{   
	private static Class<?> PKG = Append.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private AppendMeta meta;
	private AppendData data;
	
	public Append(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(AppendMeta)smi;
		data=(AppendData)sdi;
		
    	Object[] input = null;
    	if ( data.processHead )
    	{
		    input = getRowFrom(data.headRowSet);
		    
		    if ( input == null )
		    {
		    	// Switch to tail processing		    
	        	data.processHead = false;
	        	data.processTail = true;
		    }
		    else 
		    {
		    	if (data.outputRowMeta==null) {
		    		data.outputRowMeta = data.headRowSet.getRowMeta();
		    	}
		    }
		    	
    	}
    	
    	if ( data.processTail )
    	{
    		input = getRowFrom(data.tailRowSet);
		    if ( input == null )
		    {
	            setOutputDone();
	            return false;
		    }
	    	if (data.outputRowMeta==null) {
	    		data.outputRowMeta = data.tailRowSet.getRowMeta();
	    	}
	    	
	    	if ( data.firstTail )
	    	{
	    		data.firstTail = false;
	    		
    		    // Check here for the layout (which has to be the same) when we
	    		// read the first row of the tail.
                try
                {
                    checkInputLayoutValid(data.headRowSet.getRowMeta(), data.tailRowSet.getRowMeta());
                }
                catch(KettleRowException e)
                {
            	    throw new KettleException(BaseMessages.getString(PKG, "Append.Exception.InvalidLayoutDetected"), e);
                }
	    	}	    	
    	}

    	if ( input != null )
    	{
            putRow(data.outputRowMeta, input);
    	}

        if (checkFeedback(getLinesRead())) 
        {
        	if(log.isBasic()) logBasic(BaseMessages.getString(PKG, "AppendRows.LineNumber")+getLinesRead()); //$NON-NLS-1$
        }

		return true;
	}

	/**
     * @see StepInterface#init( org.pentaho.di.trans.step.StepMetaInterface , org.pentaho.di.trans.step.StepDataInterface)
     */
    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
		meta=(AppendMeta)smi;
		data=(AppendData)sdi;

        if (super.init(smi, sdi))
        {
        	data.processHead = true;
        	data.processTail = false;
        	data.firstTail = true;
        	
            List<StreamInterface> infoStreams = meta.getStepIOMeta().getInfoStreams();
            StreamInterface headStream = infoStreams.get(0);
            StreamInterface tailStream = infoStreams.get(1);

            if (headStream.getStepname()==null || tailStream.getStepname()==null)
            {
                logError(BaseMessages.getString(PKG, "AppendRows.Log.BothHopsAreNeeded")); //$NON-NLS-1$
            }
            else
            {
            	try {
	            	data.headRowSet = findInputRowSet(headStream.getStepname());
	            	data.tailRowSet = findInputRowSet(tailStream.getStepname());
	                return true;
            	}
            	catch(Exception e) {
            		logError(e.getMessage());
            		return false;
            	}
            }            
        }
        return false;
    }
    
    /**
     * Checks whether 2 template rows are compatible for the mergestep. 
     * 
     * @param referenceRow Reference row
     * @param compareRow Row to compare to
     * 
     * @return true when templates are compatible.
     * @throws KettleRowException in case there is a compatibility error.
     */
    protected void checkInputLayoutValid(RowMetaInterface referenceRowMeta, RowMetaInterface compareRowMeta) throws KettleRowException
    {
        if (referenceRowMeta!=null && compareRowMeta!=null)
        {
            BaseStep.safeModeChecking(referenceRowMeta, compareRowMeta);
        }
    }    

}