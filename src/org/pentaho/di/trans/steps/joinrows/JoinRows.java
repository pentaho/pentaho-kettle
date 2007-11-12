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
 
 
package org.pentaho.di.trans.steps.joinrows;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * Performs a cartesian product between 2 or more input streams.
 * 
 * @author Matt
 * @since 29-apr-2003
 */
public class JoinRows extends BaseStep implements StepInterface
{
	private JoinRowsMeta meta;
	private JoinRowsData data;

	public JoinRows(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
		
	/*
	 * Allocate input streams and create the temporary files...
	 * 
	 */
	@SuppressWarnings("unchecked")
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(JoinRowsMeta)smi;
		data=(JoinRowsData)sdi;

		if (super.init(smi, sdi))
		{
			try
			{
				// Start with the caching of the data, write later...
				data.caching=true;
				
				// Start at file 1, skip 0 for speed!
				data.filenr=1;
				
				// See if a main step is supplied: in that case move the corresponding rowset to position 0
				for (int i=0;i<inputRowSets.size();i++)
				{
				    RowSet rs = (RowSet) inputRowSets.get(i);
				    if (rs.getOriginStepName().equalsIgnoreCase(meta.getMainStepname()))
				    {
				        // swap this one and position 0...
                        // That means, the main stream is always stream 0 --> easy!
                        //
				        RowSet zero = (RowSet)inputRowSets.get(0);
				        inputRowSets.set(0, rs);
				        inputRowSets.set(i, zero);
				    }
				}
				
				//** INPUT SIDE **
				data.file             = new File               [inputRowSets.size()];
				data.fileInputStream  = new FileInputStream    [inputRowSets.size()];
				data.dataInputStream  = new DataInputStream    [inputRowSets.size()];
				data.size             = new int                [inputRowSets.size()];
				data.fileRowMeta      = new RowMetaInterface   [inputRowSets.size()];
				data.joinrow          = new Object             [inputRowSets.size()][];
				data.rs               = new RowSet             [inputRowSets.size()];
				data.cache            = new List               [inputRowSets.size()];
				data.position         = new int                [inputRowSets.size()];
				data.fileOutputStream = new FileOutputStream   [inputRowSets.size()];
				data.dataOutputStream = new DataOutputStream   [inputRowSets.size()];
				data.restart          = new boolean            [inputRowSets.size()];
				
				for (int i=1;i<inputRowSets.size();i++)
				{
                    String directoryName = environmentSubstitute(meta.getDirectory());
					data.file[i]=File.createTempFile(meta.getPrefix(), ".tmp", new File(directoryName)); //$NON-NLS-1$
					data.file[i].deleteOnExit();
					
					data.size[i]     = 0;
					data.rs[i]       = (RowSet)inputRowSets.get(i);
					data.cache[i]    = null;
					// data.row[i]      = null;
					data.position[i] = 0;
					
					data.dataInputStream[i]  = null;
					data.dataOutputStream[i] = null;
					
					data.joinrow[i] = null;
					data.restart[i] = false;
				}
				
				return true;
			}
			catch(IOException e)
			{
				logError(Messages.getString("JoinRows.Log.ErrorCreatingTemporaryFiles")+e.toString()); //$NON-NLS-1$
			}
		}
				
		return false;
	}
    
    /**
     * Get a row of data from the indicated rowset or buffer (memory/disk) 
     * @param filenr The rowset or buffer to read a row from
     * @return a row of data
     * @throws KettleException in case something goes wrong 
     */
	public Object[] getRowData(int filenr) throws KettleException
	{
		data.restart[filenr] = false;
					 
		Object[] rowData = null;
        
		// Do we read from the first rowset or a file?
		if (filenr==0)
		{
			// Rowset 0:
			RowSet rowSet = getInputRowSets().get(0);
            rowData = getRowFrom(rowSet); 
            if (rowData!=null)
            {
                data.fileRowMeta[0] = rowSet.getRowMeta();
            }
			
			if (log.isRowLevel()) logRowlevel(Messages.getString("JoinRows.Log.ReadRowFromStream")+(rowData==null?"<null>":rowData.toString())); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else
		{
			if (data.cache[filenr]==null)
			{
				// See if we need to open the file?
				if (data.dataInputStream[filenr]==null)
				{
					try
					{
						data.fileInputStream[filenr] = new FileInputStream(data.file[filenr]);
						data.dataInputStream[filenr] = new DataInputStream(data.fileInputStream[filenr]);
					}
					catch(FileNotFoundException fnfe)
					{
						logError(Messages.getString("JoinRows.Log.UnableToFindOrOpenTemporaryFile")+data.file[filenr]+"] : "+fnfe.toString()); //$NON-NLS-1$ //$NON-NLS-2$
						setErrors(1);
						stopAll();
						return null;
					}
				}
				
				// Read a row from the temporary file
				
				if (data.size[filenr]==0)
				{
					logBasic(Messages.getString("JoinRows.Log.NoRowsComingFromStep")+data.rs[filenr].getOriginStepName()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
					return null;
				}
				
				try
				{
					rowData = data.fileRowMeta[filenr].readData(data.dataInputStream[filenr]);
				}
				catch(KettleFileException e)
				{
					logError(Messages.getString("JoinRows.Log.UnableToReadDataFromTempFile")+filenr+" ["+data.file[filenr]+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					setErrors(1);
					stopAll();
					return null;
				} 
				catch (SocketTimeoutException e) 
				{
					logError(Messages.getString("JoinRows.Log.UnableToReadDataFromTempFile")+filenr+" ["+data.file[filenr]+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					setErrors(1);
					stopAll();
					return null;
				}
				if (log.isRowLevel()) logRowlevel(Messages.getString("JoinRows.Log.ReadRowFromFile")+filenr+" : "+rowData); //$NON-NLS-1$ //$NON-NLS-2$

				data.position[filenr]++;
				
				// If the file is at the end, close it.
				// The file will then be re-opened if needed later on.
				if (data.position[filenr]>=data.size[filenr])
				{
					try
					{
						data.dataInputStream[filenr].close();
						data.fileInputStream[filenr].close();
	
						data.dataInputStream[filenr]=null;
						data.fileInputStream[filenr]=null;
						
						data.position[filenr]=0;
						data.restart[filenr]=true;  // indicate that we restarted.
					}
					catch(IOException ioe)
					{
						logError(Messages.getString("JoinRows.Log.UnableToCloseInputStream")+data.file[filenr]+"] : "+ioe.toString()); //$NON-NLS-1$ //$NON-NLS-2$
						setErrors(1);
						stopAll();
						return null;
					}
				}
			}
			else
			{
				if (data.size[filenr]==0)
				{
					logBasic(Messages.getString("JoinRows.Log.NoRowsComingFromStep")+data.rs[filenr].getOriginStepName()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
					return null;
				}
				rowData = (Object[]) data.cache[filenr].get(data.position[data.filenr]);
			
                // Don't forget to clone the data to protect it against data alteration downstream.
                //
                rowData = data.fileRowMeta[filenr].cloneRow(rowData);
                
				data.position[filenr]++;
				
				// If the file is at the end, close it.
				// The file will then be re-opened if needed later on.
				if (data.position[filenr]>=data.size[filenr])
				{
					data.position[filenr]=0;
					data.restart[filenr]=true;  // indicate that we restarted.
				}
			}
		}
		
		return rowData;
	}
			
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(JoinRowsMeta)smi;
		data=(JoinRowsData)sdi;

		if (data.caching)
		{
			  ///////////////////////////////
			 // Read from  input channels //
			///////////////////////////////
			
			if (data.filenr>=data.file.length)
			{
				// Switch the mode to reading back from the data cache
				data.caching=false;
				
				// Start back at filenr = 0
				data.filenr=0;
				
				return true;
			}
			
			// We need to open a new outputstream
			if (data.dataOutputStream[data.filenr]==null)
			{
				try
				{
					// Open the temp file
					data.fileOutputStream[data.filenr] = new FileOutputStream(data.file[data.filenr]);

					// Open the data output stream...
					data.dataOutputStream[data.filenr] = new DataOutputStream(data.fileOutputStream[data.filenr]);
				}
				catch(FileNotFoundException fnfe)
				{
					logError(Messages.getString("JoinRows.Log.UnableToOpenOutputstream")+data.file[data.filenr].toString()+"] : "+fnfe.toString()); //$NON-NLS-1$ //$NON-NLS-2$
					stopAll();
					setErrors(1);
					return false;
				}				
			}

	    	// Read a line from the appropriate rowset...
			RowSet rowSet = data.rs[data.filenr];
	    	Object[] rowData = getRowFrom(rowSet);
	    	if (rowData!=null) // We read a row from one of the input streams...
	    	{
                if (data.fileRowMeta[data.filenr]==null)
	    		{
		    		// The first row is used as meta-data, clone it for safety
                    data.fileRowMeta[data.filenr] = rowSet.getRowMeta().clone();
	    		}

                data.fileRowMeta[data.filenr].writeData(data.dataOutputStream[data.filenr], rowData);
	    		data.size[data.filenr]++;

	    		if (log.isRowLevel()) logRowlevel(rowData.toString());
	    		
	    		//
	    		// Perhaps we want to cache this data??
	    		//
	    		if (data.size[data.filenr]<=meta.getCacheSize())
	    		{
	    			if (data.cache[data.filenr]==null) data.cache[data.filenr]=new ArrayList<Object[]>();
	    			
	    			// Add this row to the cache!
	    			data.cache[data.filenr].add(rowData);
	    		}
	    		else
	    		{
	    			// we can't cope with this many rows: reset the cache...
	    			if (log.isDetailed()) logDetailed(Messages.getString("JoinRows.Log.RowsFound",meta.getCacheSize()+"",data.rs[data.filenr].getOriginStepName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    			data.cache[data.filenr]=null;
	    		}

	    	}
	    	else // No more rows found on rowset!!
	    	{
	    		// Close outputstream.
	    		try
				{
	    			data.dataOutputStream[data.filenr].close();
	    			data.fileOutputStream[data.filenr].close();
	    			data.dataOutputStream[data.filenr]=null;
	    			data.fileOutputStream[data.filenr]=null;
	   			}
	    		catch(IOException ioe)
				{
	    			logError(Messages.getString("JoinRows.Log.ErrorInClosingOutputStream")+data.filenr+" : ["+data.file[data.filenr].toString()+"] : "+ioe.toString()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
	    		
	    		// Advance to the next file/input-stream...
	    		data.filenr++;
	    	}
		}
		else
		{
			  //////////////////////////
			 // Write to the output! //
			//////////////////////////
			
			// Read one row and store it in joinrow[]
			//
			data.joinrow[data.filenr] = getRowData(data.filenr);
			if (data.joinrow[data.filenr]==null) // 100 x 0 = 0 : don't output when one of the input streams has no rows.
			{                                    // If this is filenr #0, it's fine too!
				setOutputDone();
				return false;
			}
			
			//
			// OK, are we at the last file yet?
			// If so, we can output one row in the cartesian product.
			// Otherwise, go to the next file to get an extra row. 
			//
			if (data.filenr>=data.file.length-1)
			{
                if (data.outputRowMeta==null)
                {
                    data.outputRowMeta = createOutputRowMeta(data.fileRowMeta);
                }
                
				// Stich the output row together
				Object[] sum = new Object[data.outputRowMeta.size()];
                int sumIndex=0;
				for (int f=0;f<=data.filenr;f++)
				{
                    for (int c=0;c<data.fileRowMeta[f].size();c++)
                    {
                        sum[sumIndex] = data.joinrow[f][c];
                        sumIndex++;
                    }
				}
				
				if (meta.getCondition()!=null && !meta.getCondition().isEmpty())
				{
				    // Test the specified condition...
				    if (meta.getCondition().evaluate(data.outputRowMeta, sum)) 
                    {
                        putRow(data.outputRowMeta, sum);
                    }
				}
				else
				{
					// Put it out where it belongs!
				    putRow(data.outputRowMeta, sum);
				}

				// Did we reach the last position in the last file?
				// This means that position[] is at 0!
				// Possible we have to do this multiple times.
				// 
				while (data.restart[data.filenr])
				{
					// Get row from the previous file
					data.filenr--;
				}
			}
			else
			{
				data.filenr++;
			}
		}
		return true;
	}

    private RowMetaInterface createOutputRowMeta(RowMetaInterface[] fileRowMeta)
    {
        RowMetaInterface outputRowMeta = new RowMeta();
        for (int i=0;i<data.fileRowMeta.length;i++)
        {
            outputRowMeta.mergeRowMeta(data.fileRowMeta[i]);
        }
        return outputRowMeta;
    }

    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(JoinRowsMeta)smi;
		data=(JoinRowsData)sdi;

		// Remove the temporary files...
		for (int i=1;i<data.file.length;i++)
		{
			data.file[i].delete();
		}
		
		super.dispose(meta, data);
	}
	

	//
	// Run is were the action happens!
	//
	public void run()
	{
		try
		{
			logBasic(Messages.getString("System.Log.StartingToRun")); //$NON-NLS-1$
			
			while (processRow(meta, data) && !isStopped());
		}
		catch(Throwable t)
		{
			logError(Messages.getString("System.Log.UnexpectedError")+" : "); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Const.getStackTracker(t));
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