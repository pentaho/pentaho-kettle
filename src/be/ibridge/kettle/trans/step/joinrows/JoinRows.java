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
 
 
package be.ibridge.kettle.trans.step.joinrows;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.RowSet;
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
				        RowSet zero = (RowSet)inputRowSets.get(0);
				        inputRowSets.set(0, rs);
				        inputRowSets.set(i, zero);
				    }
				}
				
				//** INPUT SIDE **
				data.file             = new File            [inputRowSets.size()];
				data.fileInputStream  = new FileInputStream [inputRowSets.size()];
				data.dataInputStream  = new DataInputStream [inputRowSets.size()];
				data.size             = new int             [inputRowSets.size()];
				data.row              = new Row             [inputRowSets.size()];
				data.joinrow          = new Row             [inputRowSets.size()];
				data.rs               = new RowSet          [inputRowSets.size()];
				data.cache            = new ArrayList       [inputRowSets.size()];
				data.position         = new int             [inputRowSets.size()];
				data.fileOutputStream = new FileOutputStream[inputRowSets.size()];
				data.dataOutputStream = new DataOutputStream[inputRowSets.size()];
				data.restart          = new boolean         [inputRowSets.size()];
				
				for (int i=1;i<inputRowSets.size();i++)
				{
                    String directoryName = Const.replEnv(meta.getDirectory());
					data.file[i]=File.createTempFile(meta.getPrefix(), ".tmp", new File(directoryName));
					data.file[i].deleteOnExit();
					
					data.size[i]     = 0;
					data.rs[i]       = (RowSet)inputRowSets.get(i);
					data.cache[i]    = null;
					data.row[i]      = null;
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
				logError("Error creating temporary file(s) : "+e.toString());
			}
		}
				
		return false;
	}
    
	public Row getRowData(int filenr)
	{
		data.restart[filenr] = false;
					 
		Row r = null;
		// Do we read from the first rowset or a file?
		if (filenr==0)
		{
			debug="Get row from rowset #"+filenr;
			
			// Rowset 0:
			r = getRowFrom(0);
			
			logRowlevel("Read row from stream #0 : "+(r==null?"<null>":r.toString()));
		}
		else
		{
			if (data.cache[filenr]==null)
			{
				debug="Get row from file #"+filenr;
				// See if we need to open the file?
				if (data.dataInputStream[filenr]==null)
				{
					try
					{
		    			debug="Open file input stream #"+filenr;
						data.fileInputStream[filenr] = new FileInputStream(data.file[filenr]);
		    			debug="Open data input stream #"+filenr;
						data.dataInputStream[filenr] = new DataInputStream(data.fileInputStream[filenr]);
					}
					catch(FileNotFoundException fnfe)
					{
						logError("Unable to find/open temporary file ["+data.file[filenr]+"] : "+fnfe.toString());
						setErrors(1);
						stopAll();
						return null;
					}
				}
				
				debug="Read row from the data input stream #"+filenr;
				// Read a row from the temporary file
				
				if (data.size[filenr]==0)
				{
					logBasic("No rows coming from step ["+data.rs[filenr].getOriginStepName()+"]");
					return null;
				}
				
				try
				{
					r = new Row(data.dataInputStream[filenr], data.row[filenr].size(), data.row[filenr]);
				}
				catch(KettleFileException e)
				{
					logError("Unable to read data from temporary file #"+filenr+" ["+data.file[filenr]+"]");
					setErrors(1);
					stopAll();
					return null;
				}
				logRowlevel("Read row from file #"+filenr+" : "+r);

				data.position[filenr]++;
				
				// If the file is at the end, close it.
				// The file will then be re-opened if needed later on.
				if (data.position[filenr]>=data.size[filenr])
				{
	    			debug="Close stream #"+filenr;
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
						logError("Unable to close input stream to file ["+data.file[filenr]+"] : "+ioe.toString());
						setErrors(1);
						stopAll();
						return null;
					}
				}
			}
			else
			{
				debug="Get row from cache #"+filenr+" size="+data.cache[filenr].size();

				if (data.size[filenr]==0)
				{
					logBasic("No rows coming from step ["+data.rs[filenr].getOriginStepName()+"]");
					return null;
				}
				r = (Row)data.cache[filenr].get(data.position[data.filenr]);
			
				data.position[filenr]++;
				
				// If the file is at the end, close it.
				// The file will then be re-opened if needed later on.
				if (data.position[filenr]>=data.size[filenr])
				{
					debug="Position=0, restart=true for filenr="+filenr;
					data.position[filenr]=0;
					data.restart[filenr]=true;  // indicate that we restarted.
				}
			}
		}
		
		return r;
	}
			
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(JoinRowsMeta)smi;
		data=(JoinRowsData)sdi;

		debug="data.caching="+data.caching;
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
				debug="Opening file outputstream #"+data.filenr;
				try
				{
					// Open the temp file
					debug="Opening file outputstream #"+data.filenr+" file==null?"+(data.file[data.filenr]==null);
					data.fileOutputStream[data.filenr] = new FileOutputStream(data.file[data.filenr]);

					// Open the data output stream...
					debug="Opening data outputstream #"+data.filenr;
					data.dataOutputStream[data.filenr] = new DataOutputStream(data.fileOutputStream[data.filenr]);
				}
				catch(FileNotFoundException fnfe)
				{
					logError("Unable to open outputstream to temporary file ["+data.file[data.filenr].toString()+"] : "+fnfe.toString());
					stopAll();
					setErrors(1);
					return false;
				}				
			}

	    	// Read a line from the appropriate rowset...
			String fromStep = data.rs[data.filenr].getOriginStepName();
			debug="Get row from step ["+fromStep+"]";
	    	Row r = getRowFrom(fromStep);
	    	
	    	if (r!=null) // We read a row from one of the input streams...
	    	{
	    		if (data.row[data.filenr]==null)
	    		{
		    		// The first row is used as meta-data:
					data.row[data.filenr] = new Row(r); // "Clone" the row, so it becomes independend.
	    		}

				debug="Write the data to temp file #"+data.filenr;
	    		r.writeData(data.dataOutputStream[data.filenr]);
	    		data.size[data.filenr]++;

	    		logRowlevel(r.toString());
	    		
	    		//
	    		// Perhaps we want to cache this data??
	    		//
	    		if (data.size[data.filenr]<=meta.getCacheSize())
	    		{
	    			if (data.cache[data.filenr]==null) data.cache[data.filenr]=new ArrayList();
	    			
	    			// Add this row to the cache!
	    			data.cache[data.filenr].add(r);
	    		}
	    		else
	    		{
	    			// we can't cope with this many rows: reset the cache...
	    			logDetailed("More then "+meta.getCacheSize()+" rows found: clearing cache from step ["+data.rs[data.filenr].getOriginStepName()+"]");
	    			data.cache[data.filenr]=null;
	    		}

	    	}
	    	else // No more rows found on rowset!!
	    	{
				debug="Close outputstream #"+data.filenr;
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
	    			logError("Error closing outputstream #"+data.filenr+" : ["+data.file[data.filenr].toString()+"] : "+ioe.toString());
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
				debug="Stich together #"+data.filenr;
				// Stich the output row together
				Row sum = new Row();
				for (int i=0;i<=data.filenr;i++)
				{
					sum.addRow(data.joinrow[i]);
				}
				
				if (meta.getCondition()!=null && !meta.getCondition().isEmpty())
				{
				    // Test the specified condition...
				    if (meta.getCondition().evaluate(sum)) putRow(sum);
				}
				else
				{
					// Put it out where it belongs!
				    putRow(sum);
				}

				// Did we reach the last position in the last file?
				// This means that position[] is at 0!
				// Possible we have to do this multiple times.
				// 
				while (data.restart[data.filenr])
				{
					debug="Go back to file #"+(data.filenr-1);
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
			logBasic("Starting to run...");
			while (processRow(meta, data) && !isStopped());
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
		
}