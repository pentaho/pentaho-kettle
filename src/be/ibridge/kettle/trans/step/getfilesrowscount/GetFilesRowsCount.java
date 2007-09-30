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
 

package be.ibridge.kettle.trans.step.getfilesrowscount;

import java.io.BufferedInputStream;
import java.io.InputStreamReader;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;

import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.ResultFile;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.vfs.KettleVFS;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;

/**
 * Read all files, count rows number
 * 
 * @author Samatar
 * @since 24-05-2007
 */
public class GetFilesRowsCount extends BaseStep implements StepInterface
{
	private GetFilesRowsCountMeta meta;
	private GetFilesRowsCountData data;
	
	private static final int BUFFER_SIZE_INPUT_STREAM = 500;
	
	public GetFilesRowsCount(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		char separator='\n';
		
    	if (data.filenr >= data.files.size())
        {
            setOutputDone();
            return false;
        }
        
        if (first)
        {           
            first = false;
            
            if((meta.getRowSeparatorFormat().equals("CUSTOM")) && (Const.isEmpty(meta.getRowSeparator())))
            {
            	log.logError(Messages.getString("GetFilesRowsCount.Error.NoSeparator.Title"), Messages.getString("GetFilesRowsCount.Error.NoSeparator.Msg"));
            	setErrors(1);
                stopAll(); 
            }
            else
            {
	            if (meta.getRowSeparatorFormat().equals("CR"))
	    		{
	    			separator='\n';
	    			if (log.isDetailed()) log.logDetailed(Messages.getString("GetFilesRowsCount.Log.Separator.Title"), Messages.getString("GetFilesRowsCount.Log.Separatoris.Infos") + " \\n");
	    		}
	            else if (meta.getRowSeparatorFormat().equals("LF"))
	    		{
	    			separator='\r';
	    			if (log.isDetailed()) log.logDetailed(Messages.getString("GetFilesRowsCount.Log.Separator.Title"), Messages.getString("GetFilesRowsCount.Log.Separatoris.Infos") + " \\r");
	    		}
	            else if (meta.getRowSeparatorFormat().equals("TAB"))
	    		{
	            	separator='\t';
	            	if (log.isDetailed()) log.logDetailed(Messages.getString("GetFilesRowsCount.Log.Separator.Title"), Messages.getString("GetFilesRowsCount.Log.Separatoris.Infos") + " \\t");
	    		}
	            else
	    		{
	            	
	            	separator=StringUtil.environmentSubstitute(meta.getRowSeparator()).charAt(0);
	            	 
	            	if (log.isDetailed()) log.logDetailed(Messages.getString("GetFilesRowsCount.Log.Separator.Title"), Messages.getString("GetFilesRowsCount.Log.Separatoris.Infos") + " " +separator);
	    		}
            }
        }	
		
		for (int i=0;i<data.files.size();i++)
		{	
			data.file = (FileObject) data.files.get(i);
	    	
			
			logBasic(Messages.getString("GetFilesRowsCount.Log.OpeningFile", data.file.toString()));
	    	
			// Fetch files and process each one
			try 
			{
				if (data.file.getType() == FileType.FILE)	
				{
					data.fr = KettleVFS.getInputStream(data.file);
					data.isr = new InputStreamReader(new BufferedInputStream(data.fr, BUFFER_SIZE_INPUT_STREAM));
						
					int c = 0;				
					data.lineStringBuffer.setLength(0);
					
					 while (c >= 0)
	                 {
					     c = data.isr.read();

	                     if (c == separator)
	                     {
	                         // Move Row number pointer ahead
	                       	 data.rownr ++;	                        	
	    				 }	                        
	                 }
					 data.filesnr++;
				}				
			}
			catch (Exception e)
			{
				logError(Messages.getString("GetFilesRowsCount.Log.UnableToOpenFile", ""+data.filenr, data.file.toString(), e.toString()));
				stopAll();
				setErrors(1);
			}
			
			if (log.isDetailed()) logDetailed(Messages.getString("GetFilesRowsCount.Log.FileOpened", data.file.toString()));
		
			// Add this to the result file names...
			ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, data.file, getTransMeta().getName(), getStepname());
			resultFile.setComment(Messages.getString("GetFilesRowsCount.Log.FileAddedResult"));
			addResultFile(resultFile);
	    	
			// Move file pointer ahead!
			data.filenr++;
		
		}
    		
		
		linesInput++;
		data.rownr--;
		
		if (log.isDetailed()) log.logDetailed(Messages.getString("GetFilesRowsCount.Log.TotalRows"),Messages.getString("GetFilesRowsCount.Log.TotalFiles", ""+data.rownr,""+data.filesnr));
		
		// Create new row				
		
		Row r = new Row();
		
		Value countrow = new Value(StringUtil.environmentSubstitute(meta.getRowsCountFieldName()), Value.VALUE_TYPE_INTEGER);
		countrow.setValue(data.rownr);
		countrow.setLength(9);
		r.addValue(countrow);
		
		if (meta.includeCountFiles())
		{
			Value countfiles = new Value(StringUtil.environmentSubstitute(meta.getFilesCountFieldName()), Value.VALUE_TYPE_INTEGER);
			countfiles.setValue(data.filesnr);
			countfiles.setLength(9);
			r.addValue(countfiles);
		
		}

        // Output row
        putRow(r);
		
		if ((linesInput > 0) && (linesInput % Const.ROWS_UPDATE) == 0) logBasic("linenr " + linesInput);
        return true;  // This is the end of this step. 
	}		

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(GetFilesRowsCountMeta)smi;
		data=(GetFilesRowsCountData)sdi;
		
		if (super.init(smi, sdi))
		{
			data.files = meta.getFiles().getFiles();
			if (data.files==null || data.files.size()==0)
			{
				logError(Messages.getString("GetFilesRowsCount.Log.NoFiles"));
				return false;
			}
            
			data.rownr = 1L;
			data.filesnr = 0;
			
			return true;
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (GetFilesRowsCountMeta)smi;
		data = (GetFilesRowsCountData)sdi;

		super.dispose(smi, sdi);
	}
	
	//
	// Run is were the action happens!
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