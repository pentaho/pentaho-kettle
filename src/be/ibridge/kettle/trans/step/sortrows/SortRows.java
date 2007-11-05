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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.vfs.FileObject;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleFileException;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.core.vfs.KettleVFS;
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
		if (   data.buffer.size()==meta.getSortSize()     // Buffer is full: sort & dump to disk 
		   || (data.files.size()>0 && r==null && data.buffer.size()>0) // No more records: join from disk 
		   )
		{
			// First sort the rows in buffer[]
			quickSort(data.buffer);
			
			// Then write them to disk...
			DataOutputStream dos;
			GZIPOutputStream gzos;
			int p;
			
			try
			{
				FileObject fileObject=KettleVFS.createTempFile(meta.getPrefix(), ".tmp", StringUtil.environmentSubstitute(meta.getDirectory()));
				
				data.files.add(fileObject); // Remember the files!
				OutputStream outputStream = fileObject.getContent().getOutputStream();
				if (meta.getCompress())
				{
					gzos = new GZIPOutputStream(new BufferedOutputStream(outputStream));
					dos=new DataOutputStream(gzos);
				}
				else
				{
					dos = new DataOutputStream(outputStream);
					gzos = null;
				}
			
				// How many records do we have?
				dos.writeInt(data.buffer.size());
                
                for (p=0;p<data.buffer.size();p++)
				{
					if (p==0)
                    {
                        // Save the metadata, keep it in memory
                        data.rowMeta.add( new Row(((Row)data.buffer.get(p))) );
                    }
                    // Just write the data, nothing else
                    ((Row)data.buffer.get(p)).writeData(dos);
				}
				// Close temp-file
				dos.close();  // close data stream
				if (gzos != null)
                {
					gzos.close(); // close gzip stream
                }
                outputStream.close();  // close file stream
			}
			catch(Exception e)
			{
				logError("Error processing temp-file: "+e.toString());
				return false;
			}
			
			data.getBufferIndex=0;
			data.buffer.clear();
		}
		
		
		return true; 
	}
	
	private Row getBuffer()
	{
		int i, f;
		int smallest;
		Row r1, r2;
		Row retval;
		
		// Open all files at once and read one row from each file...
		if (data.files.size()>0 && ( data.dis.size()==0 || data.fis.size()==0 ))
		{
			logBasic("Opening "+data.files.size()+" tmp-files...");
		
			try
			{
				for (f=0;f<data.files.size() && !isStopped();f++)
				{
					FileObject fileObject = (FileObject)data.files.get(f);
                    String filename = KettleVFS.getFilename(fileObject);
					if (log.isDetailed()) logDetailed("Opening tmp-file: ["+filename+"]");
					InputStream fi=fileObject.getContent().getInputStream();
					DataInputStream di;
					data.fis.add(fi);
					if (meta.getCompress())
					{
						GZIPInputStream gzfi = new GZIPInputStream(new BufferedInputStream(fi));
						di =new DataInputStream(gzfi);
						data.gzis.add(gzfi);
					}
                    else
					{
						di=new DataInputStream(fi);
					}
					data.dis.add(di);
					
					// How long is the buffer?
					int buffersize=di.readInt();
					
					if (log.isDetailed()) logDetailed("["+filename+"] expecting "+buffersize+" rows...");
					
					if (buffersize>0)
					{
						// Read a row from each temp-file
                        Row metadata = (Row) data.rowMeta.get(f);
						data.rowbuffer.add(new Row(di, metadata.size(), metadata));    // new row
					}
				}
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
				retval=(Row)data.buffer.get(data.getBufferIndex);
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
				    for (i=0;i<data.rowbuffer.size() && !isStopped();i++)
				    {
					    Row b = (Row)data.rowbuffer.get(i);
					    logRowlevel("--BR#"+i+": "+b.toString());
				    }
				}
				
				smallest=0;
				r1=(Row)data.rowbuffer.get(smallest);
				for (f=1;f<data.rowbuffer.size() && !isStopped();f++)
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
				if (log.isRowLevel()) logRowlevel("Smallest row selected on ["+smallest+"] : "+retval);
				
				// now get another Row for position smallest
				
				FileObject    file = (FileObject)data.files.get(smallest);
				DataInputStream di = (DataInputStream)data.dis.get(smallest); 
				InputStream     fi = (InputStream)data.fis.get(smallest);
				GZIPInputStream gzfi = (meta.getCompress()) ? (GZIPInputStream)data.gzis.get(smallest) : null;

				try
				{
                    Row metadata = (Row) data.rowMeta.get(smallest);
					data.rowbuffer.add(smallest, new Row(di, metadata.size(), metadata));
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
                    data.rowMeta.remove(smallest);
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
			while (r!=null  && !isStopped())
			{
				if (log.isRowLevel()) logRowlevel("Read row: "+r.toString());
				
				putRow(r); // copy row to possible alternate rowset(s).

				r=getBuffer();
			}
			
			setOutputDone(); // signal receiver we're finished.
			return false;
		}

        if (checkFeedback(linesRead)) logBasic("Linenr "+linesRead);

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
			logError("Unexpected error : "+e.toString());
            logError(Const.getStackTracker(e));
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
	public void quickSort(ArrayList elements)
	{
		if (log.isDetailed()) logDetailed("Starting quickSort algorithm..."); 
		if (! elements.isEmpty())
		{ 
            Collections.sort(elements, new Comparator()
                {
                    public int compare(Object o1, Object o2)
                    {
                        return ((Row) o1).compare((Row) o2, data.fieldnrs, meta.getAscending());
                    }
                }
            );
		}
		if (log.isDetailed()) logDetailed("QuickSort algorithm has finished.");
	}
}