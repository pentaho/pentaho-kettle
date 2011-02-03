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

package org.pentaho.di.trans.steps.gpload;

//
// The "designer" notes of the Greenplum bulkloader:
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

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * Performs a bulk load to an Greenplum table.
 *
 * Based on (copied from) Sven Boden's Oracle Bulk Loader step
 * @author Luke Lonergan, Matt Casters, Sean Flatley
 * @since  28-mar-2008, 17-dec-2010
 */
public class GPLoad extends BaseStep implements StepInterface
{
	private static Class<?> PKG = GPLoadMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	Process gploadProcess = null;
	
	private GPLoadMeta meta;
	protected GPLoadData data;
	private GPLoadDataOutput output = null;
		
	/*
	 * Local copy of the transformation "preview" property. We only forward
	 * the rows upon previewing, we don't do any of the real stuff.
	 */
	private boolean preview = false;
  
  //
  // This class continually reads from the stream, and sends it to the log
  // if the logging level is at least basic level.
  //
  final private class StreamLogger extends Thread
  {
    private InputStream input;
    private String type;
    
    StreamLogger(InputStream is, String type) {
      this.input = is;
      this.type = type + ">"; //$NON-NLS-1$
    }

    public void run() {
      try
      {
        final BufferedReader br = new BufferedReader(new InputStreamReader(input));
        String line;
        while ( (line = br.readLine()) != null)
        {
          // Only perform the concatenation if at basic level. Otherwise,
          // this just reads from the stream.
          if (log.isBasic()) { 
            logBasic(type + line);
          }
        }
      } 
      catch (IOException ioe)
      {
          ioe.printStackTrace();  
      }
      
    }
    
  }
	
	
	public GPLoad(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
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
	public String getControlFileContents(GPLoadMeta meta, RowMetaInterface rm, Object[] r) throws KettleException
	{
		DatabaseMeta dm = meta.getDatabaseMeta();

		StringBuffer contents = new StringBuffer(500);
		
		// Source: GP Admin Guide 3.3.6, page 635:
		//
		contents.append("VERSION: 1.0.0.1").append(Const.CR);
        contents.append("DATABASE: ").append(environmentSubstitute(dm.getDatabaseName())).append(Const.CR);
        contents.append("USER: ").append(environmentSubstitute(dm.getUsername())).append(Const.CR);
        contents.append("HOST: ").append(environmentSubstitute(dm.getHostname())).append(Const.CR);
        contents.append("PORT: ").append(environmentSubstitute(dm.getDatabasePortNumberString())).append(Const.CR);
        contents.append("GPLOAD:").append(Const.CR);
        contents.append("   INPUT:").append(Const.CR);
        
        contents.append("    - SOURCE: ").append(Const.CR);

        // TODO: Stream to a temporary file and then bulk load OR optionally stream to a named pipe (like MySQL bulk loader)
        // TODO: allow LOCAL_HOSTNAME/PORT/PORT_RANGE to be specified
        //
        String inputName = "'" + environmentSubstitute(meta.getDataFile()) + "'";
        contents.append("        FILE: ").append('[').append(inputName).append(']').append(Const.CR);
        
        
        // COLUMNS is optional, takes the existing fields in the table
        // contents.append("    - COLUMNS:").append(Const.CR);
        
        // See also page 155 for formatting information & escaping
        //
        contents.append("    - FORMAT: TEXT").append(Const.CR);
        contents.append("    - DELIMITER: '").append(environmentSubstitute(meta.getDelimiter())).append("'").append(Const.CR);
        
        // TODO: implement escape character, null_as
        //
        // contents.append("    - ESCAPE: '").append(environmentSubstitute(meta.getEscapeCharacter)).append("'").append(Const.CR);
        
        contents.append("    - QUOTE: '").append(environmentSubstitute(meta.getEnclosure())).append("'").append(Const.CR);
        contents.append("    - HEADER: FALSE").append(Const.CR);
        
        // TODO: implement database encoding support
        // contents.append("    - ENCODING: ").append(Const.CR);
        
        contents.append("    - ERROR_LIMIT: ").append(meta.getMaxErrors()).append(Const.CR);
        
        if (!Const.isEmpty(meta.getErrorTableName())) {
           contents.append("    - ERROR_TABLE: ").append(meta.getErrorTableName()).append(Const.CR);
        }
        
        contents.append("   OUTPUT:").append(Const.CR);

        String tableName = dm.getQuotedSchemaTableCombination(
            environmentSubstitute(meta.getSchemaName()),
            environmentSubstitute(meta.getTableName()));

        contents.append("    - TABLE: ").append(tableName).append(Const.CR);
        contents.append("    - MODE: ").append(meta.getLoadAction()).append(Const.CR);

        // TODO: add support for MATCH_COLUMNS, UPDATE_COLUMN, UPDATE_CONDITION, MAPPING
        // TODO: add suport for BEFORE and AFTER SQL

        /*
           String streamFields[] = meta.getFieldStream();
    		String tableFields[] = meta.getFieldTable();
    
    		if ( streamFields == null || streamFields.length == 0 )
    		{
    			throw new KettleException("No fields defined to load to database");
    		}
    
    		for (int i = 0; i < streamFields.length; i++)
    		{
    			if ( i!=0 ) contents.append(", ");
    			contents.append(dm.quoteField(tableFields[i]));
            }
        */

		return contents.toString();
	}
	
	/**
	 * Create a control file.
	 * 
	 * @param filename
	 * @param meta
	 * @throws KettleException
	 */
	public void createControlFile(String filename, Object[] row, GPLoadMeta meta) throws KettleException
	{
		File controlFile = new File(filename);
		FileWriter fw = null;
		
		try
		{
			controlFile.createNewFile();
			fw = new FileWriter(controlFile);
 	        fw.write(getControlFileContents(meta, getInputRowMeta(), row));	        
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
	 * Create the command line for a psql process depending on the meta
	 * information supplied.
	 * 
	 * @param meta The meta data to create the command line from
	 * @param password Use the real password or not
	 * 
	 * @return The string to execute.
	 * 
	 * @throws KettleException Upon any exception
	 */
	public String createCommandLine(GPLoadMeta meta, boolean password) throws KettleException
	{
	   StringBuffer sb = new StringBuffer(300);
	   
	   if ( meta.getGploadPath() != null )
	   {
		   try
		   {
	           FileObject fileObject = KettleVFS.getFileObject(environmentSubstitute(meta.getGploadPath()), getTransMeta());
  	      	   String psqlexec = KettleVFS.getFilename(fileObject);
		       //sb.append('\'').append(psqlexec).append('\'');
  	      	   sb.append(psqlexec);
  	       }
	       catch ( Exception ex )
	       {
	           throw new KettleException("Error retrieving sqlldr string", ex);
	       }		       
	   }
	   else
	   {
		   throw new KettleException("No psql application specified");
	   }

	   if ( meta.getControlFile() != null )
	   {
		   try
		   {
	           FileObject fileObject = KettleVFS.getFileObject(environmentSubstitute(meta.getControlFile()), getTransMeta());   
		   
	           sb.append(" -f ");
	           //sb.append('\'').append(KettleVFS.getFilename(fileObject)).append('\'');
	           sb.append(KettleVFS.getFilename(fileObject));
  	       }
	       catch ( Exception ex )
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
		       FileObject fileObject = KettleVFS.getFileObject(environmentSubstitute(meta.getLogFile()), getTransMeta());   
	   
		       sb.append(" -l ");
		       sb.append('\'').append(KettleVFS.getFilename(fileObject)).append('\'');
		   }
		   catch ( Exception ex )
		   {
		       throw new KettleException("Error retrieving logfile string", ex);
		   }
	   }

	   // hostname, port and so on are passed through the control file
	   //

	   return sb.toString(); 
	}
	
	public boolean execute(GPLoadMeta meta, boolean wait) throws KettleException
	{
        Runtime rt = Runtime.getRuntime();

        try  
        {
            gploadProcess = rt.exec(createCommandLine(meta, true));
            // any error message?
            StreamLogger errorLogger = new 
                StreamLogger(gploadProcess.getErrorStream(), "ERROR");
        
            // any output?
            StreamLogger outputLogger = new 
                StreamLogger(gploadProcess.getInputStream(), "OUTPUT");
            
            // kick them off
            errorLogger.start();
            outputLogger.start();                              

            if ( wait ) 
            {
                // any error???
            	int exitVal = gploadProcess.waitFor();
				logBasic(BaseMessages.getString(PKG, "GPLoad.Log.ExitValuePsqlPath", "" + exitVal)); //$NON-NLS-1$
            }
        }
        catch ( Exception ex )
        {
        	// Don't throw the message upwards, the message contains the password.
        	throw new KettleException("Error while executing \'" + createCommandLine(meta, false) + "\'");
        }
        
        return true;
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(GPLoadMeta)smi;
		data=(GPLoadData)sdi;

		try
		{
			Object[] r=getRow();  // Get row from input rowset & set row busy!
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
					if ( GPLoadMeta.METHOD_AUTO_END.equals(loadMethod))
					{
						execute(meta, true);
					}
				//	else if ( GPLoadMeta.METHOD_AUTO_CONCURRENT.equals(meta.getLoadMethod()) )
				//	{
				//		try 
				//		{
				//			if ( psqlProcess != null )
				//			{
				//				int exitVal = psqlProcess.waitFor();
				//				logBasic(BaseMessages.getString(PKG, "GPLoad.Log.ExitValueSqlldr", "" + exitVal)); //$NON-NLS-1$
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
					createControlFile(environmentSubstitute(meta.getControlFile()), r, meta);
					output = new GPLoadDataOutput(meta, log.getLogLevel());

				//	if ( GPLoadMeta.METHOD_AUTO_CONCURRENT.equals(meta.getLoadMethod()) )
				//	{
				//		execute(meta, false);
				//	}
					output.open(this, gploadProcess);
				}
				output.writeLine(getInputRowMeta(), r);
			}
			putRow(getInputRowMeta(), r);
			incrementLinesOutput();
		
		}
		catch(KettleException e)
		{
			logError(BaseMessages.getString(PKG, "GPLoad.Log.ErrorInStep")+e.getMessage()); //$NON-NLS-1$
			setErrors(1);
			stopAll();
			setOutputDone();  // signal end to receiver(s)
			return false;
		} 

		return true;
	}

    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(GPLoadMeta)smi;
		data=(GPLoadData)sdi;
		
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
	    meta = (GPLoadMeta)smi;
	    data = (GPLoadData)sdi;

	    super.dispose(smi, sdi);
	    
	    if ( !preview && meta.isEraseFiles() )
	    {
	       // Erase the created cfg/dat files if requested. We don't erase
	       // the rest of the files because it would be "stupid" to erase them
	       // right after creation. If you don't want them, don't fill them in.
	       FileObject fileObject = null;
	       
	       String method = meta.getLoadMethod();
	       if (  // GPLoadMeta.METHOD_AUTO_CONCURRENT.equals(method) ||
	    		 GPLoadMeta.METHOD_AUTO_END.equals(method))
	       { /*
	 	       if ( meta.getControlFile() != null )
		       {
			       try
			       {
		               fileObject = KettleVFS.getFileObject(environmentSubstitute(meta.getControlFile()), getTransMeta());			   
		               fileObject.delete();
		               fileObject.close();
	  	           }
		           catch ( Exception ex )
		           {
		               logError("Error deleting control file \'" + KettleVFS.getFilename(fileObject) + "\': " + ex.getMessage());
		           }
		       }*/
		   }

	       if (  GPLoadMeta.METHOD_AUTO_END.equals(method) )
	       {
	    	   // In concurrent mode the data is written to the control file.
	 	       if ( meta.getDataFile() != null )
		       {
			       try
			       {
		               fileObject = KettleVFS.getFileObject(environmentSubstitute(meta.getDataFile()), getTransMeta());			   
		               fileObject.delete();
		               fileObject.close();
	  	           }
		           catch ( Exception ex )
		           {
		               logError("Error deleting data file \'" + KettleVFS.getFilename(fileObject) + "\': " + ex.getMessage(), ex);
		           }
		       }
	       }	       
	       
	       if (  GPLoadMeta.METHOD_MANUAL.equals(method) )
	       {
	           logBasic("Deletion of files is not compatible with \'manual load method\'");	   
	       }
	    }
	}	
}