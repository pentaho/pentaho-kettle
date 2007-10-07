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
 
package be.ibridge.kettle.trans.step.sqlfileoutput;


import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStreamWriter;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.ResultFile;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.core.vfs.KettleVFS;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;
import org.apache.commons.vfs.FileObject;
import be.ibridge.kettle.trans.step.sqlfileoutput.Messages;




/**
 * Writes rows to a database table.
 * 
 * @author Matt
 * @since 6-apr-2003
 */
public class SQLFileOutput extends BaseStep implements StepInterface
{
	private SQLFileOutputMeta meta;
	private SQLFileOutputData data;
	
	String schemaTable;
	String schemaName;
	String tableName;
        
		
	public SQLFileOutput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(SQLFileOutputMeta)smi;
		data=(SQLFileOutputData)sdi;
		
		
		Row r;
		boolean sendToErrorRow=false;
		String errorMessage = null;
		
		r=getRow();    // this also waits for a previous step to be finished.
		
		if ( r!=null && linesOutput>0 && meta.getSplitEvery()>0 && ((linesOutput+1)%meta.getSplitEvery())==0)   
		{
			
			// Done with this part or with everything.
			closeFile();
			
			// Not finished: open another file...
			if (r!=null)
			{
				if (!openNewFile())
				{
					logError("Unable to open new file (split #"+data.splitnr+"...");
					setErrors(1);
					return false;
				}

				
			}
		
		}
		
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}

		try
		{
 
	        if (linesOutput==0)
	        {
		        // Add creation table once to the top
		        if (meta.createTable())
		        {
		            String cr_table = data.db.getDDLCreationTable(schemaTable , r);	            
		            
		            if (log.isDetailed()) logDetailed("Output statement : "+cr_table);
			        // Write to file
		            data.writer.write(cr_table+ Const.CR + Const.CR) ;
		        }
		        
    
	            // Truncate table
	            if (meta.truncateTable())
	            {
		            // Write to file
	            	 data.writer.write(data.db.getDDLTruncateTable(schemaName, tableName+ ";" + Const.CR + Const.CR));
	            			
	            }
	            
	        }
	        
		}
		catch(Exception e)
		{
			throw new KettleStepException(e.getMessage());
		}
		
		try
		{
	        String sql = data.db.getSQLOutput(schemaName, tableName, r, meta.getDateFormat()) + ";" ;
	        
	        // Do we start a new line for this statement ?
	        if (meta.StartNewLine())  sql =sql + Const.CR;	
	        
	        
	        
	        if (log.isDetailed()) logDetailed("Output statement : "+sql);
	            
	        try
	        {
		         // Write to file
		         data.writer.write(sql.toCharArray()) ;
	        }
	        catch(Exception e)
			{
	        	throw new KettleStepException(e.getMessage());
			}
            if (!r.isIgnored())
            {
                putRow(r); // in case we want it go further...
                linesOutput++;
            }

            if (checkFeedback(linesRead)) logBasic("linenr "+linesRead);
		}
		catch(KettleException e)
		{
			
			if (getStepMeta().isDoingErrorHandling())
	        {
                sendToErrorRow = true;
                errorMessage = e.toString();
	        }
	        else
	        {
			
				logError(Messages.getString("SQLFileOutputMeta.Log.ErrorInStep")+e.getMessage()); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				setOutputDone();  // signal end to receiver(s)
				return false;
	        }
			 if (sendToErrorRow)
	         {
				 // Simply add this row to the error row
	             putError(r, 1, errorMessage, null, "SFO001");
	             r.setIgnore();
	         }
			
	
		}	
		
		
		return true;
	}
	public String buildFilename()
	{
		return meta.buildFilename(getCopy(), data.splitnr);
		

	}
	
	public boolean openNewFile()
	{
		boolean retval=false;
		data.writer=null;
		
		try
		{
         
			String filename = buildFilename();
			if (meta.AddToResult())
			{
				// Add this to the result file names...
				ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, KettleVFS.getFileObject(filename), getTransMeta().getName(), getStepname());
				resultFile.setComment("This file was created with a text file output step");
	            addResultFile(resultFile);
			}
            OutputStream outputStream;
            
            log.logDetailed(toString(), "Opening output stream in nocompress mode");
            OutputStream fos = KettleVFS.getOutputStream(filename, meta.isFileAppended());
            outputStream=fos;
			
            log.logBasic(toString(), "Opening output stream in default encoding");
            data.writer = new OutputStreamWriter(new BufferedOutputStream(outputStream, 5000));
        
            if (!Const.isEmpty(meta.getEncoding()))
            {
                log.logBasic(toString(), "Opening output stream in encoding: "+meta.getEncoding());
                data.writer = new OutputStreamWriter(new BufferedOutputStream(outputStream, 5000), StringUtil.environmentSubstitute(meta.getEncoding()));
            }
            else
            {
                log.logBasic(toString(), "Opening output stream in default encoding");
                data.writer = new OutputStreamWriter(new BufferedOutputStream(outputStream, 5000));
            }
            
            logDetailed("Opened new file with name ["+filename+"]");
            
            data.splitnr++;
			
			retval=true;
            
		}
		catch(Exception e)
		{
			logError("Error opening new file : "+e.toString());
		}
		// System.out.println("end of newFile(), splitnr="+splitnr);

		//data.splitnr++;

		return retval;
	}
	
	private boolean closeFile()
	{
		boolean retval=false;
		
		try
		{
			logDebug("Closing output stream");
			data.writer.close();
			logDebug("Closed output stream");
			data.writer = null;
		
			
			logDebug("Closing normal file ..");
		
            if (data.fos!=null)
            {
                data.fos.close();
                data.fos=null;
            }
		
			//System.out.println("Closed file...");

			retval=true;
		}
		catch(Exception e)
		{
			logError("Exception trying to close file: " + e.toString());
			setErrors(1);
			retval = false;
		}

		return retval;
	}

    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(SQLFileOutputMeta)smi;
		data=(SQLFileOutputData)sdi;

		if (super.init(smi, sdi))
		{
			try
			{
				
				if (meta.getDatabaseMeta() == null)
				 {
		            throw new KettleStepException("The connection is not defined (empty)");
		        }	
				
				
             
				data.db=new Database(meta.getDatabaseMeta());
				
                      
				logBasic("Connected to database ["+meta.getDatabaseMeta()+"]");
	
				
				if(meta.isCreateParentFolder())
				{
					// Check for parent folder
					FileObject parentfolder=null;
		    		try
		    		{
		    			// Get parent folder
		    			String filename=StringUtil.environmentSubstitute(meta.getFileName());
			    		parentfolder=KettleVFS.getFileObject(filename).getParent();	    		
			    		if(!parentfolder.exists())	
			    		{
			    			log.logBasic("Folder parent", "Folder parent " + parentfolder.getName() + " does not exist !");
			    			parentfolder.createFolder();
			    			log.logBasic("Folder parent", "Folder parent was created.");
			    		}
		    		}
		    		catch (Exception e) {
		    			logError("Couldn't created parent folder "+ parentfolder.getName());
		    			setErrors(1L);
						stopAll();
		    		}
		    		 finally {
		             	if ( parentfolder != null )
		             	{
		             		try  {
		             			parentfolder.close();
		             		}
		             		catch ( Exception ex ) {};
		             	}
		             }		
				}		
				
				
				
				if (!openNewFile())
				{
					logError("Couldn't open file [" + buildFilename() + "]");
					setErrors(1L);
					stopAll();
				}
               
				tableName  = StringUtil.environmentSubstitute(meta.getTablename()); 
				schemaName  = StringUtil.environmentSubstitute(meta.getSchemaName()); 
			   
				if (Const.isEmpty(tableName))
		        {
		            throw new KettleStepException("The tablename is not defined (empty)");
		        }
		          
		        schemaTable = data.db.getDatabaseMeta().getQuotedSchemaTableCombination(schemaName, tableName);

				
				
			}
			catch(Exception e)
			{
				logError("An error occurred intialising this step: "+e.getMessage());
				stopAll();
				setErrors(1);
			}
			
			return true;
		}
		return false;
	}
		
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(SQLFileOutputMeta)smi;
		data=(SQLFileOutputData)sdi;

		try
		{
		
            for (int i=0;i<data.batchBuffer.size();i++)
            {
                Row row = (Row) data.batchBuffer.get(i);
                putRow(row);
                linesOutput++;
            }
            // Clear the buffer
            data.batchBuffer.clear();  
            
            closeFile();
		}
		
		catch(Exception dbe)
		{
			logError("Unexpected error committing the database connection: "+dbe.toString());
            logError(Const.getStackTracker(dbe));
			setErrors(1);
			stopAll();
		}
		finally
        {
            setOutputDone();

		    data.db.disconnect();
            super.dispose(smi, sdi);
        }        
	}
	

	/**
	 * Run is were the action happens!
	 */
	public void run()
	{
		try
		{
			logBasic("Starting to run...");
			while (processRow(meta, data) && !isStopped());
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
}
