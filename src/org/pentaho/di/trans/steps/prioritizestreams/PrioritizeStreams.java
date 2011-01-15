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


package org.pentaho.di.trans.steps.prioritizestreams;


import org.pentaho.di.core.RowSet;
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


/**
 * Prioritize INPUT Streams.
 * 
 * @author Samatar
 * @since 30-06-2008
 */

public class PrioritizeStreams extends BaseStep implements StepInterface
{
	private static Class<?> PKG = PrioritizeStreamsMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private PrioritizeStreamsMeta meta;
	private PrioritizeStreamsData data;
	
	public PrioritizeStreams(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(PrioritizeStreamsMeta)smi;
		data=(PrioritizeStreamsData)sdi;

		if(first)  {
			first=false;
			if(meta.getStepName()==null || meta.getStepName().length>0)  {
				data.stepnrs=meta.getStepName().length;
				data.rowSets= new RowSet[data.stepnrs];
				
				for(int i=0; i<data.stepnrs; i++) {
					data.rowSets[i]=findInputRowSet(meta.getStepName()[i]);
					if(i>0) {
						// Compare layout of first stream with the current stream
						checkInputLayoutValid(data.rowSets[0].getRowMeta(), data.rowSets[i].getRowMeta());
					}
				}
			}else  {
				// error
				throw new KettleException(BaseMessages.getString(PKG, "PrioritizeStreams.Error.NotInputSteps"));
			}
			data.currentRowSet=data.rowSets[0];
			// Take the row Meta from the first stream
	    	data.outputRowMeta = data.currentRowSet.getRowMeta();
		} // end if first
		

		Object[] input=getOneRow();
		
		while(input==null && data.stepnr<data.stepnrs-1 && !isStopped()) {
			input=getOneRow();
		}
		
		if (input==null) {
			// no more input to be expected...
			setOutputDone();
			return false;
		}
		
		 putRow(data.outputRowMeta, input);

		return true;
	}
	private Object[] getOneRow() throws KettleException {
		Object[] input = getRowFrom(data.currentRowSet);
		if(input==null)  {
			if(data.stepnr<data.stepnrs-1)  {
				// read rows from the next step
				data.stepnr++;
				data.currentRowSet=data.rowSets[data.stepnr];	
				input = getRowFrom(data.currentRowSet);
			}
		} 
		return input;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(PrioritizeStreamsMeta)smi;
		data=(PrioritizeStreamsData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
			data.stepnr=0;
		    return true;
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
		data.currentRowSet=null;
		data.rowSets=null;
	    super.dispose(smi, sdi);
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
