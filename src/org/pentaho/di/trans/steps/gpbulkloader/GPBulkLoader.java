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

package org.pentaho.di.trans.steps.gpbulkloader;

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
 * @author Luke Lonergan
 * @since  28-mar-2008
 */
public class GPBulkLoader extends BaseStep implements StepInterface
{
	private static Class<?> PKG = GPBulkLoaderMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	Process psqlProcess = null;
	
	private GPBulkLoaderMeta meta;
	private GPBulkLoaderData data;
	private GPBulkDataOutput output = null;
		
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
	
	
	public GPBulkLoader(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
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
	public String getControlFileContents(GPBulkLoaderMeta meta, RowMetaInterface rm, Object[] r) throws KettleException
	{
		DatabaseMeta dm = meta.getDatabaseMeta();
		String inputName = "'" + environmentSubstitute(meta.getDataFile()) + "'";

		//if ( GPBulkLoaderMeta.METHOD_AUTO_CONCURRENT.equals(meta.getLoadMethod()) )
		//{
		//	// if loading is concurrent, the filename has to be a * as sqlldr will
		//	// read from stdin.
		//	inputName = "*";
		//}

		String loadAction = meta.getLoadAction();

		StringBuffer contents = new StringBuffer(500);

        String tableName = dm.getQuotedSchemaTableCombination(
            environmentSubstitute(meta.getSchemaName()),
            environmentSubstitute(meta.getTableName()));

        // Create a Postgres / Greenplum COPY string for use with a psql client
        if (loadAction.equalsIgnoreCase("truncate")) {
            contents.append(loadAction + " ");
            contents.append(tableName + ";");
            contents.append(Const.CR);
        }
        contents.append("\\COPY ");
        // Table name

        contents.append(tableName);

        // Names of columns

        contents.append(" ( ");

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

        contents.append(" ) ");

        // The "FROM" filename
        contents.append(" FROM ");
        contents.append(inputName);

        // The "FORMAT" clause
        contents.append(" WITH CSV ");

        // The single row error handling
        contents.append("LOG ERRORS INTO ");
        contents.append(tableName + "_errors ");
        contents.append(" SEGMENT REJECT LIMIT ");
        contents.append(meta.getMaxErrors());

//		contents.append("  ERRORS=\'").append(meta.getMaxErrors()).append("\'").append(Const.CR);

//		contents.append("LOAD DATA").append(Const.CR).append(
//		                "INFILE ").append(inputName).append(Const.CR).append(
//		                "INTO TABLE ").append(dm.getQuotedSchemaTableCombination(environmentSubstitute(meta.getSchemaName()),
//		            		                                                     environmentSubstitute(meta.getTableName()))).append(
//		                Const.CR).append(loadAction).append(Const.CR).append(
//                        "FIELDS TERMINATED BY ',' ENCLOSED BY '\"'").append(Const.CR).append(
//                        "(");
//
//		String streamFields[] = meta.getFieldStream();
//		String tableFields[] = meta.getFieldTable();
//		String dateMask[] = meta.getDateMask();
//
//		if ( streamFields == null || streamFields.length == 0 )
//		{
//			throw new KettleException("No fields defined to load to database");
//		}
//
//		for (int i = 0; i < streamFields.length; i++)
//		{
//			if ( i!=0 ) contents.append(", ").append(Const.CR);
//			contents.append(dm.quoteField(tableFields[i]));
//
//			int pos = rm.indexOfValue(streamFields[i]);
//			if (pos<0)
//			{
//				throw new KettleException("Could not find field " +
//						                  streamFields[i] + " in stream");
//			}
//			ValueMetaInterface v = rm.getValueMeta(pos);
//			switch ( v.getType() )
//			{
//			    case ValueMetaInterface.TYPE_STRING:
//			    	if ( v.getLength() > 255 )
//			    	{
//			    		contents.append(" CHAR(").append(v.getLength()).append(")");
//			    	}
//			    	else
//			    	{
//			    		contents.append(" CHAR");
//			    	}
//			    	break;
//			    case ValueMetaInterface.TYPE_INTEGER:
//			    case ValueMetaInterface.TYPE_NUMBER:
//			    case ValueMetaInterface.TYPE_BIGNUMBER:
//			    	break;
//			    case ValueMetaInterface.TYPE_DATE:
//			    	if ( GPBulkLoaderMeta.DATE_MASK_DATE.equals(dateMask[i]) )
//			    	{
//			    	    contents.append(" DATE 'yyyy-mm-dd'");
//			    	}
//			    	else if ( GPBulkLoaderMeta.DATE_MASK_DATETIME.equals(dateMask[i]) )
//			    	{
//			    		contents.append(" TIMESTAMP 'yyyy-mm-dd hh24:mi:ss.ff'");
//			    	}
//			    	else
//			    	{
//			    		// If not specified the default is date.
//			    		contents.append(" DATE 'yyyy-mm-dd'");
//			    	}
//			    	break;
//			    case ValueMetaInterface.TYPE_BINARY:
//			    	contents.append(" ENCLOSED BY '<startlob>' AND '<endlob>'");
//			    	break;
//			}
//		}
//		contents.append(")");

		//if ( GPBulkLoaderMeta.METHOD_AUTO_CONCURRENT.equals(meta.getLoadMethod()) )
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
	public void createControlFile(String filename, Object[] row, GPBulkLoaderMeta meta) throws KettleException
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
	public String createCommandLine(GPBulkLoaderMeta meta, boolean password) throws KettleException
	{
	   StringBuffer sb = new StringBuffer(300);
	   
	   if ( meta.getPsqlpath() != null )
	   {
		   try
		   {
	           FileObject fileObject = KettleVFS.getFileObject(environmentSubstitute(meta.getPsqlpath()), getTransMeta());
  	      	   String psqlexec = KettleVFS.getFilename(fileObject);
		       sb.append('\'').append(psqlexec).append('\'');
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
		   
	           sb.append(" -n -f ");
	           sb.append('\'').append(KettleVFS.getFilename(fileObject)).append('\'');
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
	   
		       sb.append(" -o ");
		       sb.append('\'').append(KettleVFS.getFilename(fileObject)).append('\'');
		   }
		   catch ( Exception ex )
		   {
		       throw new KettleException("Error retrieving logfile string", ex);
		   }
	   }


       DatabaseMeta dm = meta.getDatabaseMeta();
       if ( dm != null )
       {
           String user = Const.NVL(dm.getUsername(), "");

// Passwords will not work for now because we can't get them to the command line without assuming UNIX and using
// an environment variable
           String pass = Const.NVL(dm.getPassword(), "");
           if (password && ! pass.equalsIgnoreCase("") ) {
               throw new KettleException("Passwords are not supported directly, try configuring your connection for trusted access using pg_hba.conf");
           }
//           if ( ! password )
//           {
//        	   pass = "******";
//           }
//           String dns  = Const.NVL(dm.getDatabaseName(), "");
//           sb.append(" -U ").append(environmentSubstitute(user)).append("/").append(environmentSubstitute(pass));

           sb.append(" -U ").append(environmentSubstitute(user));

           //Hostname and portname
           String hostname  = Const.NVL(dm.getHostname(), "");
           String portnum  = Const.NVL(dm.getDatabasePortNumberString(), "");
           sb.append(" -h ");
           sb.append(hostname);
           sb.append(" -p ");
           sb.append(portnum);

           // Database Name
           String dns  = Const.NVL(dm.getDatabaseName(), "");
           sb.append(" -d ");
           String overrideName = meta.getDbNameOverride();
           if ( Const.isEmpty(Const.rtrim(overrideName)) )
           {
               sb.append(environmentSubstitute(dns));
           }
           else
           {
        	   // if the database name override is filled in, do that one.
        	   sb.append(environmentSubstitute(overrideName));
           }
       }
	   else
	   {
		   throw new KettleException("No connection specified");
	   }

	   return sb.toString(); 
	}
	
	public boolean execute(GPBulkLoaderMeta meta, boolean wait) throws KettleException
	{
        Runtime rt = Runtime.getRuntime();

        try  
        {
            psqlProcess = rt.exec(createCommandLine(meta, true));
            // any error message?
            StreamLogger errorLogger = new 
                StreamLogger(psqlProcess.getErrorStream(), "ERROR");
        
            // any output?
            StreamLogger outputLogger = new 
                StreamLogger(psqlProcess.getInputStream(), "OUTPUT");
            
            // kick them off
            errorLogger.start();
            outputLogger.start();                              

            if ( wait ) 
            {
                // any error???
            	int exitVal = psqlProcess.waitFor();
				logBasic(BaseMessages.getString(PKG, "GPBulkLoader.Log.ExitValuePsqlPath", "" + exitVal)); //$NON-NLS-1$
            }
        }
        catch ( Exception ex )
        {
        	// Don't throw the message upwards, the message contains the password.
        	throw new KettleException("Error while executing psql \'" + createCommandLine(meta, false) + "\'");
        }
        
        return true;
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(GPBulkLoaderMeta)smi;
		data=(GPBulkLoaderData)sdi;

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
					if ( GPBulkLoaderMeta.METHOD_AUTO_END.equals(loadMethod))
					{
						execute(meta, true);
					}
				//	else if ( GPBulkLoaderMeta.METHOD_AUTO_CONCURRENT.equals(meta.getLoadMethod()) )
				//	{
				//		try 
				//		{
				//			if ( psqlProcess != null )
				//			{
				//				int exitVal = psqlProcess.waitFor();
				//				logBasic(BaseMessages.getString(PKG, "GPBulkLoader.Log.ExitValueSqlldr", "" + exitVal)); //$NON-NLS-1$
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
					output = new GPBulkDataOutput(meta);

				//	if ( GPBulkLoaderMeta.METHOD_AUTO_CONCURRENT.equals(meta.getLoadMethod()) )
				//	{
				//		execute(meta, false);
				//	}
					output.open(this, psqlProcess);
				}
				output.writeLine(getInputRowMeta(), r);
			}
			putRow(getInputRowMeta(), r);
			incrementLinesOutput();
		
		}
		catch(KettleException e)
		{
			logError(BaseMessages.getString(PKG, "GPBulkLoader.Log.ErrorInStep")+e.getMessage()); //$NON-NLS-1$
			setErrors(1);
			stopAll();
			setOutputDone();  // signal end to receiver(s)
			return false;
		} 

		return true;
	}

    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(GPBulkLoaderMeta)smi;
		data=(GPBulkLoaderData)sdi;
		
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
	    meta = (GPBulkLoaderMeta)smi;
	    data = (GPBulkLoaderData)sdi;

	    super.dispose(smi, sdi);
	    
	    if ( !preview && meta.isEraseFiles() )
	    {
	       // Erase the created cfg/dat files if requested. We don't erase
	       // the rest of the files because it would be "stupid" to erase them
	       // right after creation. If you don't want them, don't fill them in.
	       FileObject fileObject = null;
	       
	       String method = meta.getLoadMethod();
	       if (  // GPBulkLoaderMeta.METHOD_AUTO_CONCURRENT.equals(method) ||
	    		 GPBulkLoaderMeta.METHOD_AUTO_END.equals(method))
	       {
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
		       }
		   }

	       if (  GPBulkLoaderMeta.METHOD_AUTO_END.equals(method) )
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
	       
	       if (  GPBulkLoaderMeta.METHOD_MANUAL.equals(method) )
	       {
	           logBasic("Deletion of files is not compatible with \'manual load method\'");	   
	       }
	    }
	}

	public String toString()
	{
		return this.getClass().getName();
	}	
}