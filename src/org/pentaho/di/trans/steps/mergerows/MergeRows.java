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

package org.pentaho.di.trans.steps.mergerows;

import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleRowException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
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
 * Merge rows from 2 sorted streams to detect changes.
 * Use this as feed for a dimension in case you have no time stamps in your source system.
 * 
 * @author Matt
 * @since 19-dec-2005
 */
public class MergeRows extends BaseStep implements StepInterface
{
	private static Class<?> PKG = MergeRowsMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private static final String VALUE_IDENTICAL  = "identical"; //$NON-NLS-1$ 
    private static final String VALUE_CHANGED    = "changed"; //$NON-NLS-1$
    private static final String VALUE_NEW        = "new"; //$NON-NLS-1$
    private static final String VALUE_DELETED    = "deleted"; //$NON-NLS-1$ 
    
	private MergeRowsMeta meta;
	private MergeRowsData data;
	
	public MergeRows(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(MergeRowsMeta)smi;
		data=(MergeRowsData)sdi;

        if (first)
        {
            first = false;
            
            // Find the appropriate RowSet 
            //
            List<StreamInterface> infoStreams = meta.getStepIOMeta().getInfoStreams();
            
            data.oneRowSet = findInputRowSet(infoStreams.get(0).getStepname());
            data.twoRowSet = findInputRowSet(infoStreams.get(1).getStepname());
            
    		data.one=getRowFrom(data.oneRowSet);
            data.two=getRowFrom(data.twoRowSet);
            
            try
            {
                checkInputLayoutValid(data.oneRowSet.getRowMeta(), data.twoRowSet.getRowMeta());
            }
            catch(KettleRowException e)
            {
            	throw new KettleException(BaseMessages.getString(PKG, "MergeRows.Exception.InvalidLayoutDetected"), e);
            }            

            if (data.one!=null)
            {
                // Find the key indexes:
                data.keyNrs = new int[meta.getKeyFields().length];
                for (int i=0;i<data.keyNrs.length;i++)
                {
                    data.keyNrs[i] = data.oneRowSet.getRowMeta().indexOfValue(meta.getKeyFields()[i]);
                    if (data.keyNrs[i]<0)
                    {
                        String message = BaseMessages.getString(PKG, "MergeRows.Exception.UnableToFindFieldInReferenceStream",meta.getKeyFields()[i]);  //$NON-NLS-1$ //$NON-NLS-2$
                        logError(message);
                        throw new KettleStepException(message);
                    }
                }
            }

            if (data.two!=null)
            {
                data.valueNrs = new int[meta.getValueFields().length];
                for (int i=0;i<data.valueNrs.length;i++)
                {
                    data.valueNrs[i] = data.twoRowSet.getRowMeta().indexOfValue(meta.getValueFields()[i]);
                    if (data.valueNrs[i]<0)
                    {
                        String message = BaseMessages.getString(PKG, "MergeRows.Exception.UnableToFindFieldInReferenceStream",meta.getValueFields()[i]);  //$NON-NLS-1$ //$NON-NLS-2$
                        logError(message);
                        throw new KettleStepException(message);
                    }
                }
            }
        }

        if (log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "MergeRows.Log.DataInfo",data.one+"")+data.two); //$NON-NLS-1$ //$NON-NLS-2$

        if (data.one==null && data.two==null)
        {
            setOutputDone();
            return false;
        }
        
        if (data.outputRowMeta==null)
        {
            data.outputRowMeta = new RowMeta();
            if (data.one!=null)
            {
                meta.getFields(data.outputRowMeta, getStepname(), new RowMetaInterface[] { data.oneRowSet.getRowMeta() }, null, this);
            }
            else
            {
                meta.getFields(data.outputRowMeta, getStepname(), new RowMetaInterface[] { data.twoRowSet.getRowMeta() }, null, this);
            }
        }

        Object[] outputRow;
        int outputIndex;
        String flagField = null;
        
        if (data.one==null && data.two!=null) // Record 2 is flagged as new!
        {
            outputRow = data.two;
            outputIndex = data.twoRowSet.getRowMeta().size();
            flagField = VALUE_NEW;

            // Also get a next row from compare rowset...
            data.two=getRowFrom(data.twoRowSet);
        }
        else
        if (data.one!=null && data.two==null) // Record 1 is flagged as deleted!
        {
            outputRow = data.one;
            outputIndex = data.oneRowSet.getRowMeta().size();
            flagField = VALUE_DELETED;
            
            // Also get a next row from reference rowset...
            data.one=getRowFrom(data.oneRowSet);
        }
        else  // OK, Here is the real start of the compare code!
        {
            int compare = data.oneRowSet.getRowMeta().compare(data.one, data.two, data.keyNrs);
            if (compare==0)  // The Key matches, we CAN compare the two rows...
            {
                int compareValues = data.oneRowSet.getRowMeta().compare(data.one, data.two, data.valueNrs);
                if (compareValues==0)
                {
                    outputRow = data.one;
                    outputIndex = data.oneRowSet.getRowMeta().size();
                    flagField = VALUE_IDENTICAL;
                }
                else
                {
                    // Return the compare (most recent) row
                    //
                    outputRow = data.two;
                    outputIndex = data.twoRowSet.getRowMeta().size();
                    flagField = VALUE_CHANGED;
                }

                // Get a new row from both streams...
                data.one=getRowFrom(data.oneRowSet);
                data.two=getRowFrom(data.twoRowSet);
            }
            else 
            {
                if (compare<0) // one < two
                {
                    outputRow = data.one;
                    outputIndex = data.oneRowSet.getRowMeta().size();
                    flagField = VALUE_DELETED;

                    data.one=getRowFrom(data.oneRowSet);
                }
                else
                {
                    outputRow = data.two;
                    outputIndex = data.twoRowSet.getRowMeta().size();
                    flagField = VALUE_NEW;

                    data.two=getRowFrom(data.twoRowSet);
                }
            }
        }
        
        // send the row to the next steps...
        putRow(data.outputRowMeta, RowDataUtil.addValueData(outputRow, outputIndex, flagField));

        if (checkFeedback(getLinesRead())) 
        {
        	if(log.isBasic()) logBasic(BaseMessages.getString(PKG, "MergeRows.LineNumber")+getLinesRead()); //$NON-NLS-1$
        }

		return true;
	}

	/**
     * @see StepInterface#init( org.pentaho.di.trans.step.StepMetaInterface , org.pentaho.di.trans.step.StepDataInterface)
     */
    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
		meta=(MergeRowsMeta)smi;
		data=(MergeRowsData)sdi;

        if (super.init(smi, sdi))
        {
            List<StreamInterface> infoStreams = meta.getStepIOMeta().getInfoStreams();

            if (infoStreams.get(0).getStepMeta()!=null ^ infoStreams.get(1).getStepMeta()!=null)
            {
                logError(BaseMessages.getString(PKG, "MergeRows.Log.BothTrueAndFalseNeeded")); //$NON-NLS-1$
            }
            else
            {
                return true;
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

	//
	// Run is were the action happens!
	public void run()
	{
    	BaseStep.runStepThread(this, meta, data);
	}
}