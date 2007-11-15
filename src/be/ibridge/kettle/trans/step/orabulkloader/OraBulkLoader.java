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

package be.ibridge.kettle.trans.step.orabulkloader;

//
// The "designer" notes of the Oracle bulkloader:
// ----------------------------------------------
//
// - "Enclosed" is used in the loader instead of "optionally enclosed" as optionally
//   encloses kind of destroys the escaping.
// - A Boolean is output as Y and N (as in the text output step e.g.). If people don't
//   like this they can first convert the boolean value to something else before loading
//   it.
// - Filters (besides data and datetime) are not supported as it slows down.
//
// 

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.commons.vfs.FileObject;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.vfs.KettleVFS;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;
import be.ibridge.kettle.core.database.DatabaseMeta;

/**
 * Performs a bulk load to an oracle table.
 *
 * @author Sven Boden
 * @since  20-feb-2007
 */
public class OraBulkLoader extends BaseStep implements StepInterface
{
	Process sqlldrProcess = null;
	
	private OraBulkLoaderMeta meta;
	private OraBulkLoaderData data;
	private OraBulkDataOutput output = null;
		
	/*
	 * Local copy of the transformation "preview" property. We only forward
	 * the rows upon previewing, we don't do any of the real stuff.
	 */
	private boolean preview = false;
	
	class StreamGobbler extends Thread
	{
	    private InputStream is;
	    private String type;
	    private OutputStream os;
	    
	    StreamGobbler(InputStream is, String type)
	    {
	        this(is, type, null);
	    }
	    
	    StreamGobbler(InputStream is, String type, OutputStream redirect)
	    {
	        this.is = is;
	        this.type = type;
	        this.os = redirect;
	    }
	    
	    public void run()
	    {
	        try
	        {
	            PrintWriter pw = null;
	            if (os != null)
	                pw = new PrintWriter(os);
	                
	            InputStreamReader isr = new InputStreamReader(is);
	            BufferedReader br = new BufferedReader(isr);
	            String line=null;
	            while ( (line = br.readLine()) != null)
	            {
	                if (pw != null)
	                    pw.println(line);
	                logBasic(type + ">" + line);    
	            }
	            if (pw != null)
	                pw.flush();
	        } 
	        catch (IOException ioe)
	        {
	            ioe.printStackTrace();  
	        }
	    }
	}
	
	public OraBulkLoader(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	/**
	 * Get the contents of the control file as specified in the meta object
	 * 
	 * @param meta the meta object to model the control file after
	 * 
	 * @return a string containing the control file contents
	 */
	public String getControlFileContents(OraBulkLoaderMeta meta, Row r) throws KettleException
	{
		DatabaseMeta dm = meta.getDatabaseMeta();
		String inputName = "'" + StringUtil.environmentSubstitute(meta.getDataFile()) + "'";
		
		//if ( OraBulkLoaderMeta.METHOD_AUTO_CONCURRENT.equals(meta.getLoadMethod()) )
		//{
		//	// if loading is concurrent, the filename has to be a * as sqlldr will
		//	// read from stdin.		
		//	inputName = "*";
		//}
		
		String loadAction = meta.getLoadAction();

		StringBuffer contents = new StringBuffer(500); 
		contents.append("OPTIONS(").append(Const.CR);
		contents.append("  ERRORS=\'").append(meta.getMaxErrors()).append("\'").append(Const.CR);

 	    if ( meta.getCommitSize() != 0 && 
 	    		! (meta.isDirectPath() && getStepMeta().getCopies() > 1 ))
		{
 	       // For the second part of the above expressions: ROWS is not supported
 	       // in parallel mode (by sqlldr).
		   contents.append("  , ROWS=\'").append(meta.getCommitSize()).append("\'").append(Const.CR);
		}
 	    
   	    if ( meta.getBindSize() != 0 )
   	    {
		   contents.append("  , BINDSIZE=\'").append(meta.getBindSize()).append("\'").append(Const.CR);
		}
	
	    if ( meta.getReadSize() != 0 )
	    {
		   contents.append("  , READSIZE=\'").append(meta.getReadSize()).append("\'").append(Const.CR);
		}
	
 	    contents.append(")").append(Const.CR);
		 	    
		contents.append("LOAD DATA").append(Const.CR).append(
		                "INFILE ").append(inputName).append(Const.CR).append(
		                "INTO TABLE ").append(dm.getQuotedSchemaTableCombination(StringUtil.environmentSubstitute(meta.getSchemaName()),
		            		                                                     StringUtil.environmentSubstitute(meta.getTableName()))).append(
		                Const.CR).append(loadAction).append(Const.CR).append(		                    
                        "FIELDS TERMINATED BY ',' ENCLOSED BY '\"'").append(Const.CR).append(
                        "(");

		String fields[] = meta.getFieldTable();
		String dateMask[] = meta.getDateMask();
		
		if ( fields == null || fields.length == 0 )
		{
			throw new KettleException("No fields defined to load to database");
		}
		
		for (int i = 0; i < fields.length; i++)
		{
			if ( i!=0 ) contents.append(", ").append(Const.CR);
			contents.append(dm.quoteField(fields[i]));
			
			int pos = r.searchValueIndex(fields[i]);
			if (pos<0)
			{
				throw new KettleException("Could not find field " + 
						                  meta.getFieldStream()[i] + " in stream");
			}
			Value v = r.getValue(pos);
			switch ( v.getType() )
			{
			    case Value.VALUE_TYPE_STRING:
			    	if ( v.getLength() > 255 )
			    	{
			    		contents.append(" CHAR(").append(v.getLength()).append(")");
			    	}
			    	else
			    	{
			    		contents.append(" CHAR");
			    	}			    	
			    	break;
			    case Value.VALUE_TYPE_INTEGER:
			    case Value.VALUE_TYPE_NUMBER:
			    case Value.VALUE_TYPE_BIGNUMBER:
			    	break;
			    case Value.VALUE_TYPE_DATE:			    	
			    	if ( OraBulkLoaderMeta.DATE_MASK_DATE.equals(dateMask[i]) )
			    	{
			    	    contents.append(" DATE 'yyyy-mm-dd'");	
			    	}
			    	else if ( OraBulkLoaderMeta.DATE_MASK_DATETIME.equals(dateMask[i]) )
			    	{
			    		contents.append(" TIMESTAMP 'yyyy-mm-dd hh24:mi:ss.ff'");
			    	}			    	
			    	else
			    	{
			    		// If not specified the default is date.
			    		contents.append(" DATE 'yyyy-mm-dd'");
			    	}
			    	break;
			    case Value.VALUE_TYPE_BINARY:
			    	contents.append(" ENCLOSED BY '<startlob>' AND '<endlob>'");
			    	break;
			}
		}
		contents.append(")");
		
		//if ( OraBulkLoaderMeta.METHOD_AUTO_CONCURRENT.equals(meta.getLoadMethod()) )
		//{
		//	contents.append(Const.CR).append("BEGINDATA").append(Const.CR);
		//}
		
		return contents.toString();
	}
	
	/**
	 * Create a control file.
	 * 
	 * @param filename
	 * @param meta
	 * @throws KettleException
	 */
	public void createControlFile(String filename, Row row, OraBulkLoaderMeta meta) throws KettleException
	{
		File controlFile = new File(filename);
		FileWriter fw = null;
		
		try
		{
			controlFile.createNewFile();
			fw = new FileWriter(controlFile);
 	        fw.write(getControlFileContents(meta, row));	        
		}
		catch ( IOException ex )
		{
		    throw new KettleException(ex.getMessage(), ex);
		}
		finally
		{
			try  {
			    if ( fw != null )
				    fw.close();
			}
			catch ( Exception ex ) {}
		}
	}
	
	/**
	 * Create the command line for an sqlldr process depending on the meta
	 * information supplied.
	 * 
	 * @param meta The meta data to create the command line from
	 * @param password Use the real password or not
	 * 
	 * @return The string to execute.
	 * 
	 * @throws KettleException Upon any exception
	 */
	public String createCommandLine(OraBulkLoaderMeta meta, boolean password) throws KettleException
	{
	   StringBuffer sb = new StringBuffer(300);
	   
	   if ( meta.getSqlldr() != null )
	   {
		   try
		   {
	           FileObject fileObject = KettleVFS.getFileObject(StringUtil.environmentSubstitute(meta.getSqlldr()));   
  	      	   String sqlldr = KettleVFS.getFilename(fileObject);
		       sb.append(sqlldr);
  	       }
	       catch ( IOException ex )
	       {
	           throw new KettleException("Error retrieving sqlldr string", ex);
	       }		       
	   }
	   else
	   {
		   throw new KettleException("No sqlldr application specified");
	   }

	   if ( meta.getControlFile() != null )
	   {
		   try
		   {
	           FileObject fileObject = KettleVFS.getFileObject(StringUtil.environmentSubstitute(meta.getControlFile()));   
		   
	           sb.append(" control=\'");
	           sb.append(KettleVFS.getFilename(fileObject));
		       sb.append("\'");
  	       }
	       catch ( IOException ex )
	       {
	           throw new KettleException("Error retrieving controlfile string", ex);
	       }		   
	   }
	   else
	   {
		   throw new KettleException("No control file specified");
	   }
	   	   
	   if ( meta.getLogFile() != null )
	   {
		   try 
		   {
		       FileObject fileObject = KettleVFS.getFileObject(StringUtil.environmentSubstitute(meta.getLogFile()));   
	   
		       sb.append(" log=\'");
		       sb.append(KettleVFS.getFilename(fileObject));
		       sb.append("\'");
		   }
		   catch ( IOException ex )
		   {
		       throw new KettleException("Error retrieving logfile string", ex);
		   }
	   }

	   if ( meta.getBadFile() != null )
	   {
		   try
		   {
	           FileObject fileObject = KettleVFS.getFileObject(StringUtil.environmentSubstitute(meta.getBadFile()));   
		   
	           sb.append(" bad=\'");
	           sb.append(KettleVFS.getFilename(fileObject));		  		   
		       sb.append("\'");
		   }
		   catch ( IOException ex )
		   {
		       throw new KettleException("Error retrieving badfile string", ex);
		   }		       
	   }
	   
	   if ( meta.getDiscardFile() != null )
	   {
		   try
		   {
	           FileObject fileObject = KettleVFS.getFileObject(StringUtil.environmentSubstitute(meta.getDiscardFile()));   
		   
	           sb.append(" discard=\'");
	           sb.append(KettleVFS.getFilename(fileObject));		  		   
		       sb.append("\'");
		   }
		   catch ( IOException ex )
		   {
		       throw new KettleException("Error retrieving discardfile string", ex);
		   }		       		       
	   }
		
       DatabaseMeta dm = meta.getDatabaseMeta();
       if ( dm != null )
       {
           String user = Const.NVL(dm.getUsername(), "");
           String pass = Const.NVL(dm.getPassword(), "");
           if ( ! password )
           {
        	   pass = "******";
           }
           String dns  = Const.NVL(dm.getDatabaseName(), "");
           sb.append(" userid=").append(StringUtil.environmentSubstitute(user)).append("/").append(StringUtil.environmentSubstitute(pass)).append("@");
           sb.append(StringUtil.environmentSubstitute(dns));           
       }
	   else
	   {
		   throw new KettleException("No connection specified");
	   }

	   if ( meta.isDirectPath() )
	   {
           sb.append(" DIRECT=TRUE");
           
           if ( getStepMeta().getCopies() > 1 )
           {
        	   sb.append(" PARALLEL=TRUE");
           }
	   }

	   return sb.toString(); 
	}
	
	public boolean execute(OraBulkLoaderMeta meta, boolean wait) throws KettleException
	{
        Runtime rt = Runtime.getRuntime();

        try  
        {
            sqlldrProcess = rt.exec(createCommandLine(meta, true));
            // any error message?
            StreamGobbler errorGobbler = new 
                StreamGobbler(sqlldrProcess.getErrorStream(), "ERROR");            
        
            // any output?
            StreamGobbler outputGobbler = new 
                StreamGobbler(sqlldrProcess.getInputStream(), "OUTPUT");
            
            // kick them off
            errorGobbler.start();
            outputGobbler.start();                              

            if ( wait ) 
            {
                // any error???
            	int exitVal = sqlldrProcess.waitFor();
				logBasic(Messages.getString("OraBulkLoader.Log.ExitValueSqlldr", "" + exitVal)); //$NON-NLS-1$
            }
        }
        catch ( Exception ex )
        {
        	// Don't throw the message upwards, the message contains the password.
        	throw new KettleException("Error while executing sqlldr \'" + createCommandLine(meta, false) + "\'");
        }
        
        return true;
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(OraBulkLoaderMeta)smi;
		data=(OraBulkLoaderData)sdi;		

		try
		{
			Row r=getRow();       // Get row from input rowset & set row busy!
			if (r==null)          // no more input to be expected...
			{
				setOutputDone();

				if ( ! preview )
				{
					if ( output != null )
					{
						// Close the output
						try  {
							output.close();
						}
						catch ( IOException e )
						{
							throw new KettleException("Error while closing output", e); 
						}

						output = null;
					}

					String loadMethod = meta.getLoadMethod();
					if ( OraBulkLoaderMeta.METHOD_AUTO_END.equals(loadMethod))
					{
						execute(meta, true);
					}
				//	else if ( OraBulkLoaderMeta.METHOD_AUTO_CONCURRENT.equals(meta.getLoadMethod()) )
				//	{
				//		try 
				//		{
				//			if ( sqlldrProcess != null )
				//			{
				//				int exitVal = sqlldrProcess.waitFor();
				//				logBasic(Messages.getString("OraBulkLoader.Log.ExitValueSqlldr", "" + exitVal)); //$NON-NLS-1$
				//			}
				//			else
				//			{
				//				throw new KettleException("Internal error: no sqlldr process running");
				//			}
				//		}
				//		catch ( Exception ex )
				//		{
				//			throw new KettleException("Error while executing sqlldr", ex);
				//		}
				//	}
				}			
				return false;
			}

			if ( ! preview )
			{
				if (first)
				{
					first=false;
					createControlFile(StringUtil.environmentSubstitute(meta.getControlFile()), r, meta);
					output = new OraBulkDataOutput(meta);			

				//	if ( OraBulkLoaderMeta.METHOD_AUTO_CONCURRENT.equals(meta.getLoadMethod()) )
				//	{
				//		execute(meta, false);
				//	}
					output.open(sqlldrProcess);
				}
				output.writeLine(r);
			}
			putRow(r);
			linesOutput++;
		
		}
		catch(KettleException e)
		{
			logError(Messages.getString("OraBulkLoader.Log.ErrorInStep")+e.getMessage()); //$NON-NLS-1$
			setErrors(1);
			stopAll();
			setOutputDone();  // signal end to receiver(s)
			return false;
		} 

		return true;
	}

    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(OraBulkLoaderMeta)smi;
		data=(OraBulkLoaderData)sdi;
		
		Trans trans = getTrans();
		preview = trans.isPreview();
		
		if (super.init(smi, sdi))
		{			
			return true;
		}
		return false;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (OraBulkLoaderMeta)smi;
	    data = (OraBulkLoaderData)sdi;

	    super.dispose(smi, sdi);
	    
	    if ( !preview && meta.isEraseFiles() )
	    {
	       // Erase the created cfg/dat files if requested. We don't erase
	       // the rest of the files because it would be "stupid" to erase them
	       // right after creation. If you don't want them, don't fill them in.
	       FileObject fileObject = null;
	       
	       String method = meta.getLoadMethod();
	       if (  // OraBulkLoaderMeta.METHOD_AUTO_CONCURRENT.equals(method) || 
	    		 OraBulkLoaderMeta.METHOD_AUTO_END.equals(method))
	       {
	 	       if ( meta.getControlFile() != null )
		       {
			       try
			       {
		               fileObject = KettleVFS.getFileObject(StringUtil.environmentSubstitute(meta.getControlFile()));			   
		               fileObject.delete();
		               fileObject.close();
	  	           }
		           catch ( IOException ex )
		           {
		               logError("Error deleting control file \'" + KettleVFS.getFilename(fileObject) + "\': " + ex.getMessage());
		           }
		       }
		   }

	       if (  OraBulkLoaderMeta.METHOD_AUTO_END.equals(method) )
	       {
	    	   // In concurrent mode the data is written to the control file.
	 	       if ( meta.getDataFile() != null )
		       {
			       try
			       {
		               fileObject = KettleVFS.getFileObject(StringUtil.environmentSubstitute(meta.getDataFile()));			   
		               fileObject.delete();
		               fileObject.close();
	  	           }
		           catch ( IOException ex )
		           {
		               logError("Error deleting data file \'" + KettleVFS.getFilename(fileObject) + "\': " + ex.getMessage());
		           }
		       }
	       }	       
	       
	       if (  OraBulkLoaderMeta.METHOD_MANUAL.equals(method) )
	       {
	           logBasic("Deletion of files is not compatible with \'manual load method\'");	   
	       }
	    }
	}

	//
	// Run is were the action happens!
	//
	public void run()
	{
		try
		{
			logBasic(Messages.getString("OraBulkLoader.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("OraBulkLoader.Log.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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

	public String toString()
	{
		return this.getClass().getName();
	}
}