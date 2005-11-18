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
 
 
package be.ibridge.kettle.trans.step.sortrows;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleFileException;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


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
	
	private boolean addBuffer(Row r)
	{
		if (r!=null)
		{
			data.buffer.add(r);     // Save row
		}
		if (data.files.size()==0 && r==null) // No more records: sort buffer
		{
			quickSort(data.buffer);
		}
		
		// time to write to disk: buffer is full!
		if (   data.buffer.size()==Const.SORT_SIZE     // Buffer is full: sort & dump to disk 
		   || (data.files.size()>0 && r==null && data.buffer.size()>0) // No more records: join from disk 
		   )
		{
			// First sort the rows in buffer[]
			quickSort(data.buffer);
			
			// Then write them to disk...
			File             fil;
			FileOutputStream fos;
			DataOutputStream dos;
			int p;
			
			try
			{
				fil=File. createTempFile(meta.getPrefix(), ".tmp", new File(Const.replEnv(meta.getDirectory())));
				fil.deleteOnExit();
				data.files.add(fil);   // Remember the files!
				
				fos=new FileOutputStream(fil);
				dos=new DataOutputStream(fos);
			
				// How many records do we have?
				dos.writeInt(data.buffer.size());
			
				for (p=0;p<data.buffer.size();p++)
				{
					((Row)data.buffer.get(p)).write(dos);
				}
				// Close temp-file
				dos.close();  // close data stream
				fos.close();  // close file stream
			}
			catch(Exception e)
			{
				logError("Error processing temp-file: "+e.toString());
				return false;
			}
			
			data.buffer.removeAllElements();
		}
		
		
		return true; 
	}
	
	private Row getBuffer()
	{
		int i, f;
		int smallest;
		Row r1, r2;
		Row retval;
		String filename;
		
		debug="start";
		
		// Open all files at once and read one row from each file...
		if (data.files.size()>0 && ( data.dis.size()==0 || data.fis.size()==0 ))
		{
			debug="initialize";
			logBasic("Opening "+data.files.size()+" tmp-files...");
		
			try
			{
				for (f=0;f<data.files.size();f++)
				{
					filename=((File)data.files.get(f)).toString();
					debug="opening file "+filename;
					logDetailed("Opening tmp-file: ["+filename+"]");
					FileInputStream fi=new FileInputStream( (File)data.files.get(f) );
					DataInputStream di=new DataInputStream( fi );
					data.fis.add(fi);
					data.dis.add(di);
					
					// How long is the buffer?
					int buffersize=di.readInt();
					debug="buffersize = "+buffersize;
					
					logDetailed("["+filename+"] expecting "+buffersize+" rows...");
					
					if (buffersize>0)
					{
						// Read a row from each temp-file
						data.rowbuffer.add(new Row(di));    // new row
					}
				}
			}
			catch(Exception e)
			{
				logError("Error reading back tmp-files in step ["+debug+"] : "+e.toString());
			}
		}
		
		if (data.files.size()==0)
		{
			if (data.buffer.size()>0)
			{
				retval=(Row)data.buffer.get(0);
				data.buffer.remove(0);
			}
			else
			{
				retval=null;
			}
		}
		else
		{
			if (data.rowbuffer.size()==0) retval=null;
			else
			{
				// We now have "filenr" rows waiting: which one is the smallest?
				//
				for (i=0;i<data.rowbuffer.size();i++)
				{
					Row b = (Row)data.rowbuffer.get(i);
					logRowlevel("--BR#"+i+": "+b.toString());
				}
				//
				
				smallest=0;
				r1=(Row)data.rowbuffer.get(smallest);
				for (f=1;f<data.rowbuffer.size();f++)
				{
					r2=(Row)data.rowbuffer.get(f);
					
					if (r2!=null && r2.compare(r1, data.fieldnrs, meta.getAscending())<0)
					{
						smallest=f;
						r1=(Row)data.rowbuffer.get(smallest);
					}
				}
				retval=r1;
		
				data.rowbuffer.remove(smallest);
				logRowlevel("Smallest row selected on ["+smallest+"] : "+retval);
				
				// now get another Row for position smallest
				
				File          file = (File)data.files.get(smallest);
				DataInputStream di = (DataInputStream)data.dis.get(smallest); 
				FileInputStream fi = (FileInputStream)data.fis.get(smallest);
				 
				try
				{
					data.rowbuffer.add(smallest, new Row(di));
				}
				catch(KettleFileException fe) // empty file or EOF mostly
				{
					try
					{
						di.close();
						fi.close();
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
				}
			}
		}
		return retval;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		Row r=null;
		boolean err=true;
		int i;
		
		r=getRow();   // get row from rowset, wait for our turn, indicate busy!

		// initialize 
		if (first && r!=null)
		{
			first=false;
			data.fieldnrs=new int[meta.getFieldName().length];
			for (i=0;i<meta.getFieldName().length;i++)
			{
				data.fieldnrs[i]=r.searchValueIndex( meta.getFieldName()[i] );
				if (data.fieldnrs[i]<0)
				{
					logError("Sort field ["+meta.getFieldName()[i]+"] not found!");
					setOutputDone();
					return false;
				}
 			}
		}
		
		err=addBuffer(r);
		if (!err) 
		{
			setOutputDone(); // signal receiver we're finished.
			return false;
		}		
		
		if (r==null)  // no more input to be expected...
		{
			// Now we can start the output!
			r=getBuffer();
			while (r!=null)
			{
				logRowlevel("Read row: "+r.toString());
				
				putRow(r); // copy row to possible alternate rowset(s).

				r=getBuffer();
			}
			
			setOutputDone(); // signal receiver we're finished.
			return false;
		}

		if ((linesRead>0) && (linesRead%Const.ROWS_UPDATE)==0) logBasic("Linenr "+linesRead);

		return true;
	}
	
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(SortRowsMeta)smi;
		data=(SortRowsData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
		    return true;
		}
		return false;
	}
			
	//
	// Run is were the action happens!
	//
	public void run()
	{
		try
		{
			logBasic("Starting to run...");
			while (processRow(meta, data)  && !isStopped());
		}
		catch(Exception e)
		{
			logError("Unexpected error in '"+debug+"' : "+e.toString());
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
	
	
	/** Sort the entire vector, if it is not empty
	 */
	public synchronized void quickSort(Vector elements)
	{
		logDetailed("Starting quickSort algorithm..."); 
		if (! elements.isEmpty())
		{ 
			this.quickSort(elements, 0, elements.size()-1);
		}
		logDetailed("QuickSort algorithm has finished.");
	}


	/**
	 * QuickSort.java by Henk Jan Nootenboom, 9 Sep 2002
	 * Copyright 2002-2003 SUMit. All Rights Reserved.
	 *
	 * Algorithm designed by prof C. A. R. Hoare, 1962
	 * See http://www.sum-it.nl/en200236.html
	 * for algorithm improvement by Henk Jan Nootenboom, 2002.
	 *
	 * Recursive Quicksort, sorts (part of) a Vector by
	 *  1.  Choose a pivot, an element used for comparison
	 *  2.  dividing into two parts:
	 *      - less than-equal pivot
	 *      - and greater than-equal to pivot.
	 *      A element that is equal to the pivot may end up in any part.
	 *      See www.sum-it.nl/en200236.html for the theory behind this.
	 *  3. Sort the parts recursively until there is only one element left.
	 *
	 * www.sum-it.nl/QuickSort.java this source code
	 * www.sum-it.nl/quicksort.php3 demo of this quicksort in a java applet
	 *
	 * Permission to use, copy, modify, and distribute this java source code
	 * and its documentation for NON-COMMERCIAL or COMMERCIAL purposes and
	 * without fee is hereby granted.
	 * See http://www.sum-it.nl/security/index.html for copyright laws.
	 */
	  private synchronized void quickSort(Vector elements, int lowIndex, int highIndex)
	  { 
	  	int lowToHighIndex;
		int highToLowIndex;
		int pivotIndex;
		Row pivotValue;  // values are Strings in this demo, change to suit your application
		Row lowToHighValue;
		Row highToLowValue;
		Row parking;
		int newLowIndex;
		int newHighIndex;
		int compareResult;

		lowToHighIndex = lowIndex;
		highToLowIndex = highIndex;
		/** Choose a pivot, remember it's value
		 *  No special action for the pivot element itself.
		 *  It will be treated just like any other element.
		 */
		pivotIndex = (lowToHighIndex + highToLowIndex) / 2;
		pivotValue = (Row)elements.elementAt(pivotIndex);

		/** Split the Vector in two parts.
		 *
		 *  The lower part will be lowIndex - newHighIndex,
		 *  containing elements <= pivot Value
		 *
		 *  The higher part will be newLowIndex - highIndex,
		 *  containting elements >= pivot Value
		 * 
		 */
		newLowIndex = highIndex + 1;
		newHighIndex = lowIndex - 1;
		// loop until low meets high
		while ((newHighIndex + 1) < newLowIndex) // loop until partition complete
		{ // loop from low to high to find a candidate for swapping
		  lowToHighValue = (Row)elements.elementAt(lowToHighIndex);
		  while (lowToHighIndex < newLowIndex
			& lowToHighValue.compare(pivotValue, data.fieldnrs, meta.getAscending())<0 )
		  { 
		  	newHighIndex = lowToHighIndex; // add element to lower part
			lowToHighIndex ++;
			lowToHighValue = (Row)elements.elementAt(lowToHighIndex);
		  }

		  // loop from high to low find other candidate for swapping
		  highToLowValue = (Row)elements.elementAt(highToLowIndex);
		  while (newHighIndex <= highToLowIndex
			& (highToLowValue.compare(pivotValue, data.fieldnrs, meta.getAscending())>0)
			)
		  { 
		  	newLowIndex = highToLowIndex; // add element to higher part
			highToLowIndex --;
			highToLowValue = (Row)elements.elementAt(highToLowIndex);
		  }

		  // swap if needed
		  if (lowToHighIndex == highToLowIndex) // one last element, may go in either part
		  { 
		  	newHighIndex = lowToHighIndex; // move element arbitrary to lower part
		  }
		  else if (lowToHighIndex < highToLowIndex) // not last element yet
		  { 
		  	compareResult = lowToHighValue.compare(highToLowValue, data.fieldnrs, meta.getAscending());
			if (compareResult >= 0) // low >= high, swap, even if equal
			{ 
			  parking = lowToHighValue;
			  elements.setElementAt(highToLowValue, lowToHighIndex);
			  elements.setElementAt(parking, highToLowIndex);

			  newLowIndex = highToLowIndex;
			  newHighIndex = lowToHighIndex;

			  lowToHighIndex ++;
			  highToLowIndex --;
			}
		  }
		}

		// Continue recursion for parts that have more than one element
		if (lowIndex < newHighIndex)
		{ 
			this.quickSort(elements, lowIndex, newHighIndex); // sort lower subpart
		}
		if (newLowIndex < highIndex)
		{ 
			this.quickSort(elements, newLowIndex, highIndex); // sort higher subpart
		}
	  }
}
