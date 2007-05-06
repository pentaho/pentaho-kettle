package be.ibridge.kettle.trans.step.blockingstep;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
 *  A step that blocks throughput until the input ends, then it will either output
 *  the last row or the complete input. 
 */
public class BlockingStep extends BaseStep implements StepInterface {

    private BlockingStepMeta meta;
    private BlockingStepData data;
    private Row lastRow;
    
    public BlockingStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
    {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }
        
	private boolean addBuffer(Row r)
	{
		if (r!=null)
		{
			data.buffer.add(r);     // Save row
		}
		
		// Time to write to disk: buffer in core is full!
		if (   data.buffer.size()==meta.getCacheSize()                 // Buffer is full: dump to disk 
		   || (data.files.size()>0 && r==null && data.buffer.size()>0) // No more records: join from disk 
		   )
		{		
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
				logError("Error processing tmp-file: "+e.toString());
				return false;
			}
			
			data.buffer.clear();
		}		
		
		return true; 
	}
	
	private Row getBuffer()
	{
		Row retval;
		
		// Open all files at once and read one row from each file...
		if (data.files.size()>0 && ( data.dis.size()==0 || data.fis.size()==0 ))
		{
			logBasic("Opening tmp-file ...");		
			try
			{
				FileObject fileObject = (FileObject)data.files.get(0);
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
					// Read a row from the temp-file
					Row metadata = (Row) data.rowMeta.get(0);
					data.rowbuffer.add(new Row(di, metadata.size(), metadata));    // new row
				}
			}
			catch(Exception e)
			{
				logError("Error reading back tmp-file : "+e.toString());
                logError(Const.getStackTracker(e));
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
			if (data.rowbuffer.size()==0)
            {
                retval=null;
            }
			else
			{		
				retval=(Row)data.rowbuffer.get(0);
		
				data.rowbuffer.remove(0);
				
				// now get another 
				FileObject    file = (FileObject)data.files.get(0);
				DataInputStream di = (DataInputStream)data.dis.get(0); 
				InputStream     fi = (InputStream)data.fis.get(0);
				GZIPInputStream gzfi = (meta.getCompress()) ? (GZIPInputStream)data.gzis.get(0) : null;

				try
				{
                    Row metadata = (Row)data.rowMeta.get(0);
					data.rowbuffer.add(0, new Row(di, metadata.size(), metadata));
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
						logError("Unable to close/delete file #0 --> "+file.toString());
						setErrors(1);
						stopAll();
						return null;
					}
					
					data.files.remove(0);
					data.dis.remove(0);
					data.fis.remove(0);
					if (gzfi != null) data.gzis.remove(0);
                    data.rowMeta.remove(0);
				}
			}
		}
		return retval;
	}
    
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(BlockingStepMeta)smi;
		data=(BlockingStepData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
		    return true;
		}
		return false;
	} 
    
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
    	
    	boolean err=true;
        Row r=getRow();       // Get row from input rowset & set row busy!
        
        if ( ! meta.isPassAllRows())
        {
        	if (r==null)  // no more input to be expected...
        	{
        		if(lastRow != null) {
        			putRow(lastRow);
        		}
        		setOutputDone();
        		return false;
        	}

        	lastRow = r;
        	return true;
        }
        else
        {
        	//  The mode in which we pass all rows to the output.
        	
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
    		
        	return true;
        }        
    }

    //
    // Run is were the action happens!
    public void run()
    {
        try
        {
        	logBasic("Starting to run...");
            while (processRow(meta, data) && !isStopped());
        }
        catch(Exception e)
        {
            logError("Unexpected error : " + e.toString());
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
}