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
 
 
package org.pentaho.di.trans.steps.sort;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Sort the rows in the input-streams based on certain criteria
 * 
 * @author Matt
 * @since 29-apr-2003
 */
public class SortRows extends BaseStep implements StepInterface
{
	private SortRowsMeta meta;
	private SortRowsData data;
	
	public SortRows(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		
		meta=(SortRowsMeta)getStepMeta().getStepMetaInterface();
		data=(SortRowsData)stepDataInterface;
	}
	
	private boolean addBuffer(RowMetaInterface rowMeta, Object[] r) throws KettleException
	{
		if (r!=null)
		{
			// Do we need to convert binary string keys?
			//
			for (int i=0;i<data.fieldnrs.length;i++)
			{
				if (data.convertKeysToNative[i])
				{
					int index = data.fieldnrs[i];
					r[index] = rowMeta.getValueMeta(index).convertBinaryStringToNativeType((byte[]) r[index]);
				}
			}

			// Save row
			// 
			data.buffer.add( r );     
		}
		if (data.files.size()==0 && r==null) // No more records: sort buffer
		{
			quickSort(data.buffer);
		}
		
		// Check the free memory every 1000 rows...
		//
		data.freeCounter++;
		if (data.sortSize<=0 && data.freeCounter>=1000)
		{
			data.freeMemoryPct = Const.getPercentageFreeMemory();
			data.freeCounter=0;
			
			if (log.isDetailed())
			{
				data.memoryReporting++;
				if (data.memoryReporting>=10)
				{
					logDetailed("Available memory : "+data.freeMemoryPct+"%");
					data.memoryReporting=0;
				}
			}
		}
		
		boolean doSort = data.buffer.size()==data.sortSize; // Buffer is full: sort & dump to disk
		doSort |= data.files.size()>0 && r==null && data.buffer.size()>0; // No more records: join from disk 
		doSort |= data.freeMemoryPctLimit>0 && data.freeMemoryPct<data.freeMemoryPctLimit && data.buffer.size()>=data.minSortSize;
		
		// time to sort the buffer and write the data to disk...
		//
		if ( doSort )
		{
			// First sort the rows in buffer[]
			quickSort(data.buffer);
			
			// Then write them to disk...
			DataOutputStream dos;
			GZIPOutputStream gzos;
			int p;
            Object[] previousRow = null;
			
			try
			{
				FileObject fileObject=KettleVFS.createTempFile(meta.getPrefix(), ".tmp", environmentSubstitute(meta.getDirectory()), getTransMeta());
				
				data.files.add(fileObject); // Remember the files!
				OutputStream outputStream = KettleVFS.getOutputStream(fileObject,false);
				if (data.compressFiles)
				{
					gzos = new GZIPOutputStream(new BufferedOutputStream(outputStream));
					dos=new DataOutputStream(gzos);
				}
				else
				{
					dos = new DataOutputStream(new BufferedOutputStream(outputStream, 500000));
					gzos = null;
				}
                
                // Just write the data, nothing else
                if (meta.isOnlyPassingUniqueRows())
                {
                    int index=0;
                    while (index<data.buffer.size())
                    {
                        Object[] row = data.buffer.get(index);
                        if (previousRow!=null)
                        {
                            int result = data.outputRowMeta.compare(row, previousRow, data.fieldnrs);
                            if (result==0)
                            {
                                data.buffer.remove(index); // remove this duplicate element as requested
                                if (log.isRowLevel()) logRowlevel("Duplicate row removed: "+data.outputRowMeta.getString(row));
                            }
                            else
                            {
                                index++;
                            }
                        }
                        else
                        {
                            index++;
                        }
                        previousRow = row; 
                    }
                }
			
				// How many records do we have left?
				data.bufferSizes.add( data.buffer.size() );
                
                for (p=0;p<data.buffer.size();p++)
				{
                    data.outputRowMeta.writeData(dos, data.buffer.get(p));
				}
                
                if (data.sortSize<0)
                {
                	if (data.buffer.size()>data.minSortSize)
                	{
                		data.minSortSize=data.buffer.size(); // if we did it once, we can do it again.
                		
                		// Memory usage goes up over time, even with garbage collection
                		// We need pointers, file handles, etc.
                		// As such, we're going to lower the min sort size a bit
                		//
                		data.minSortSize = (int)Math.round((double)data.minSortSize * 0.90);
                	}
                }
                
                // Clear the list
                data.buffer.clear();
                
				// Close temp-file
				dos.close();  // close data stream
				if (gzos != null)
                {
					gzos.close(); // close gzip stream
                }
                outputStream.close();  // close file stream
                
                // How much memory do we have left?
                //
                data.freeMemoryPct = Const.getPercentageFreeMemory();
    			data.freeCounter=0;
    			if (data.sortSize<=0)
    			{
    				if (log.isDetailed()) logDetailed("Available memory : "+data.freeMemoryPct+"%");
    			}
    			
			}
			catch(Exception e)
			{
				throw new KettleException("Error processing temp-file!", e);
			}
			
            data.getBufferIndex=0;
		}
		
		
		return true; 
	}
	
	private Object[] getBuffer() throws KettleValueException
	{
		Object[] retval;
		
		// Open all files at once and read one row from each file...
		if (data.files.size()>0 && ( data.dis.size()==0 || data.fis.size()==0 ))
		{
			if(log.isBasic()) logBasic("Opening "+data.files.size()+" tmp-files...");
		
			try
			{
				for (int f=0;f<data.files.size() && !isStopped();f++)
				{
					FileObject fileObject = (FileObject)data.files.get(f);
                    String filename = KettleVFS.getFilename(fileObject);
					if (log.isDetailed()) logDetailed("Opening tmp-file: ["+filename+"]");
					InputStream fi=KettleVFS.getInputStream(fileObject);
					DataInputStream di;
					data.fis.add(fi);
					if (data.compressFiles)
					{
						GZIPInputStream gzfi = new GZIPInputStream(new BufferedInputStream(fi));
						di =new DataInputStream(gzfi);
						data.gzis.add(gzfi);
					}
                    else
					{
						di=new DataInputStream(new BufferedInputStream(fi, 50000));
					}
					data.dis.add(di);
					
					// How long is the buffer?
					int buffersize=data.bufferSizes.get(f);
					
					if (log.isDetailed()) logDetailed("["+filename+"] expecting "+buffersize+" rows...");
					
					if (buffersize>0)
					{
						Object[] row = (Object [])data.outputRowMeta.readData(di);
                        data.rowbuffer.add( row );    // new row from input stream
                        data.tempRows.add( new RowTempFile(row,f) );
					}
				}
				
				// Sort the data row buffer
				Collections.sort(data.tempRows, data.comparator);
			}
			catch(Exception e)
			{
				logError("Error reading back tmp-files : "+e.toString());
                logError(Const.getStackTracker(e));
			}
		}
		
		if (data.files.size()==0)
		{
			if (data.getBufferIndex<data.buffer.size())
			{
				retval=(Object[])data.buffer.get(data.getBufferIndex);
				data.getBufferIndex++;
			}
			else
			{
				retval=null;
			}
		}
		else
		{
			if (data.rowbuffer.size()==0)
            {
                retval=null;
            }
			else
			{
				// We now have "filenr" rows waiting: which one is the smallest?
				//
				if (log.isRowLevel())
				{
				    for (int i=0;i<data.rowbuffer.size() && !isStopped();i++)
				    {
					    Object[] b = (Object[])data.rowbuffer.get(i);
					    logRowlevel("--BR#"+i+": "+data.outputRowMeta.getString(b));
				    }
				}
				
				RowTempFile rowTempFile = data.tempRows.remove(0);
				retval = rowTempFile.row;
				int smallest = rowTempFile.fileNumber;
				
				// now get another Row for position smallest
				
				FileObject    file = (FileObject)data.files.get(smallest);
				DataInputStream di = (DataInputStream)data.dis.get(smallest); 
				InputStream     fi = (InputStream)data.fis.get(smallest);
				GZIPInputStream gzfi = (data.compressFiles) ? (GZIPInputStream)data.gzis.get(smallest) : null;

				try
				{
					Object[] row2 = (Object [])data.outputRowMeta.readData(di);
					RowTempFile extra = new RowTempFile(row2, smallest);
					
					int index = Collections.binarySearch(data.tempRows, extra, data.comparator);
					if (index < 0)
					{
						data.tempRows.add(index*(-1) - 1, extra);
					}
					else
					{
						data.tempRows.add(index, extra);
					}
				}
				catch(KettleFileException fe) // empty file or EOF mostly
				{
					try
					{
						di.close();
						fi.close();
						if (gzfi != null) gzfi.close();
						file.delete();
					}
					catch(IOException e)
					{
						logError("Unable to close/delete file #"+smallest+" --> "+file.toString());
						setErrors(1);
						stopAll();
						return null;
					}
					
					data.files.remove(smallest);
					data.dis.remove(smallest);
					data.fis.remove(smallest);

					if (gzfi != null) data.gzis.remove(smallest);
					
					// Also update all file numbers in in data.tempRows if they are larger than smallest.
					//
					for (RowTempFile rtf : data.tempRows)
					{
						if (rtf.fileNumber>smallest) rtf.fileNumber--;
					}
					
				} 
				catch (SocketTimeoutException e) 
				{
					throw new KettleValueException(e); // should never happen on local files
				}
			}
		}
		return retval;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		boolean err=true;
		int i;
		
		Object[] r=getRow();   // get row from rowset, wait for our turn, indicate busy!

		// initialize 
		if (first && r!=null)
		{
			first=false;
			data.convertKeysToNative = new boolean[meta.getFieldName().length];
			data.fieldnrs=new int[meta.getFieldName().length];
			for (i=0;i<meta.getFieldName().length;i++)
			{
				data.fieldnrs[i]=getInputRowMeta().indexOfValue( meta.getFieldName()[i] );
				if (data.fieldnrs[i]<0)
				{
					logError("Sort field ["+meta.getFieldName()[i]+"] not found!");
					setOutputDone();
					return false;
				}
				data.convertKeysToNative[i] = getInputRowMeta().getValueMeta(data.fieldnrs[i]).isStorageBinaryString();
 			}
            
            // Metadata
            data.outputRowMeta = getInputRowMeta().clone();
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
		}
		
		err=addBuffer(getInputRowMeta(), r);
		if (!err) 
		{
			setOutputDone(); // signal receiver we're finished.
			return false;
		}		
		
		if (r==null)  // no more input to be expected...
		{
			// Now we can start the output!
			r=getBuffer();
			Object[] previousRow = null;
			while (r!=null  && !isStopped())
			{
				if (log.isRowLevel()) logRowlevel("Read row: "+getInputRowMeta().getString(r));
				
				// Do another verification pass for unique rows...
				//
				if (meta.isOnlyPassingUniqueRows())
				{
					if (previousRow!=null)
					{
						// See if this row is the same as the previous one as far as the keys are concerned.
						// If so, we don't put forward this row.
                        int result = data.outputRowMeta.compare(r, previousRow, data.fieldnrs);
                        if (result!=0)
                        {
    						putRow(data.outputRowMeta, r); // copy row to possible alternate rowset(s).
                        }
					}
					else
					{
						putRow(data.outputRowMeta, r); // copy row to possible alternate rowset(s).
					}
					previousRow = r;
				}
				else
				{
					putRow(data.outputRowMeta, r); // copy row to possible alternate rowset(s).
				}

				r=getBuffer();
			}
			
			setOutputDone(); // signal receiver we're finished.
			return false;
		}

        if (checkFeedback(getLinesRead())) 
        {
        	if(log.isBasic()) logBasic("Linenr "+getLinesRead());
        }

		return true;
	}
	
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(SortRowsMeta)smi;
		data=(SortRowsData)sdi;
		
		if (super.init(smi, sdi))
		{
			data.sortSize = Const.toInt(environmentSubstitute(meta.getSortSize()), -1);
			data.freeMemoryPctLimit = Const.toInt(meta.getFreeMemoryLimit(), -1);
			if (data.sortSize<=0 && data.freeMemoryPctLimit<=0)
			{
				// Prefer the memory limit as it should never fail
				//
				data.freeMemoryPctLimit = 25;
			}
						
			if (data.sortSize>0)
			{
				data.buffer = new ArrayList<Object[]>(data.sortSize);
			}
			else
			{
				data.buffer = new ArrayList<Object[]>(5000);
			}

            data.compressFiles = getBooleanValueOfVariable(meta.getCompressFilesVariable(), meta.getCompressFiles());
            
            data.comparator = new Comparator<RowTempFile>(){
            	public int compare(RowTempFile o1, RowTempFile o2)
            	{
            		try
            		{
            			return data.outputRowMeta.compare(o1.row, o2.row, data.fieldnrs);
            		}
            		catch(KettleValueException e)
            		{
            			logError("Error comparing rows: "+e.toString());
                    	return 0;
            		}
            	}
            };

		    // Add init code here.
            
            if (data.sortSize>0)
            {
                data.rowbuffer=new ArrayList<Object[]>(data.sortSize);
            }
            else
            {
                data.rowbuffer=new ArrayList<Object[]>();
            }
            data.tempRows  = new ArrayList<RowTempFile>();
            
            data.minSortSize = 5000;
            
		    return true;
		}
		return false;
	}

	/** 
	 * Sort the entire vector, if it is not empty.
	 */
	public void quickSort(List<Object[]> elements)
	{
		if (log.isDetailed()) logDetailed("Starting quickSort algorithm..."); 
		if (elements.size()>0)
		{ 
            Collections.sort(elements, new Comparator<Object[]>()
                {
                    public int compare(Object[] o1, Object[] o2)
                    {
                        Object[] r1 = (Object[]) o1;
                        Object[] r2 = (Object[]) o2;
                        
                        try
                        {
                            return data.outputRowMeta.compare(r1, r2, data.fieldnrs);
                        }
                        catch(KettleValueException e)
                        {
                            logError("Error comparing rows: "+e.toString());
                            return 0;
                        }
                    }
                }
            );
            long nrConversions = 0L;
            for (ValueMetaInterface valueMeta : data.outputRowMeta.getValueMetaList())
            {
            	nrConversions+=valueMeta.getNumberOfBinaryStringConversions();
            	valueMeta.setNumberOfBinaryStringConversions(0L);
            }
            if(log.isDetailed()) logDetailed("The number of binary string to data type conversions done in this sort block is "+nrConversions);
		}
		if (log.isDetailed()) logDetailed("QuickSort algorithm has finished.");
	}
	
	//
	// Run is were the action happens!
	public void run()
	{
    	BaseStep.runStepThread(this, meta, data);
	}	
}