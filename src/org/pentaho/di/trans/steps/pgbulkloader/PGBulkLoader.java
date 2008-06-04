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

package org.pentaho.di.trans.steps.pgbulkloader;

//
// The "designer" notes of the PostgreSQL bulkloader:
// ----------------------------------------------
//
// Let's see how fast we can push data down the tube with the use of COPY FROM STDIN 
//
// 

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.StreamLogger;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * Performs a bulk load to a postgres table.
 *
 * Based on (copied from) Sven Boden's Oracle Bulk Loader step
 * 
 * @author matt
 * @since  28-mar-2008
 */
public class PGBulkLoader extends BaseStep implements StepInterface
{
	private PGBulkLoaderMeta meta;
	private PGBulkLoaderData data;
		
	/*
	 * Local copy of the transformation "preview" property. We only forward
	 * the rows upon previewing, we don't do any of the real stuff.
	 */
	private boolean preview = false;
	
	
	public PGBulkLoader(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
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
	public String getControlFileContents(PGBulkLoaderMeta meta, RowMetaInterface rm, Object[] r) throws KettleException
	{
		DatabaseMeta dm = meta.getDatabaseMeta();
		// String inputName = "'" + environmentSubstitute(meta.getDataFile()) + "'";

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
        contents.append(" FROM /tmp/load0.dat"); // FIFO file

        // The "FORMAT" clause
        contents.append(" WITH CSV DELIMITER AS ';' QUOTE AS '\"'");

		return contents.toString();
	}
	
	/**
	 * Create a control file.
	 * 
	 * @param filename
	 * @param meta
	 * @throws KettleException
	 */
	public void createControlFile(String filename, Object[] row, PGBulkLoaderMeta meta) throws KettleException
	{
		File controlFile = new File(filename);
		FileWriter fw = null;
		
		try
		{
			controlFile.createNewFile();
			fw = new FileWriter(controlFile);
 	        fw.write(getControlFileContents(meta, getInputRowMeta(), row));	  
 	        fw.write(Const.CR);
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
	public String createCommandLine(PGBulkLoaderMeta meta, boolean password) throws KettleException
	{
	   StringBuffer sb = new StringBuffer(300);
	   
	   if ( meta.getPsqlpath() != null )
	   {
		   try
		   {
	           FileObject fileObject = KettleVFS.getFileObject(environmentSubstitute(meta.getPsqlpath()));
  	      	   String psqlexec = KettleVFS.getFilename(fileObject);
		       sb.append(psqlexec);
  	       }
	       catch ( IOException ex )
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
	           FileObject fileObject = KettleVFS.getFileObject(environmentSubstitute(meta.getControlFile()));   
		   
	           sb.append(" -f ");
	           sb.append(KettleVFS.getFilename(fileObject));
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
	   
	   /*
	   if ( meta.getLogFile() != null )
	   {
		   try 
		   {
		       FileObject fileObject = KettleVFS.getFileObject(environmentSubstitute(meta.getLogFile()));   
	   
		       sb.append(" -o ");
		       sb.append('\'').append(KettleVFS.getFilename(fileObject)).append('\'');
		   }
		   catch ( IOException ex )
		   {
		       throw new KettleException("Error retrieving logfile string", ex);
		   }
	   }
	   */

       DatabaseMeta dm = meta.getDatabaseMeta();
       if ( dm != null )
       {
           String user = Const.NVL(dm.getUsername(), "");

		   // Passwords will not work for now because we can't get them to the command line without assuming UNIX and using
		   // an environment variable
           
           /*
           String pass = Const.NVL(dm.getPassword(), "");
           if (password && ! pass.equalsIgnoreCase("") ) {
               throw new KettleException("Passwords are not supported directly, try configuring your connection for trusted access using pg_hba.conf");
           }
           */

           sb.append(" -U ").append(environmentSubstitute(user));

           /*
           //Hostname and portname
           String hostname  = Const.NVL(dm.getHostname(), "");
           String portnum  = Const.NVL(dm.getDatabasePortNumberString(), "");
           sb.append(" -h ");
           sb.append(hostname);
           sb.append(" -p ");
           sb.append(portnum);
		*/
           
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
	
	public boolean execute(PGBulkLoaderMeta meta, boolean wait) throws KettleException
	{
        Runtime rt = Runtime.getRuntime();

        try  
        {
        	String cmd = createCommandLine(meta, true);
        	logBasic("Executing command: "+cmd);
            data.psqlProcess = rt.exec(cmd);
            
            // any error message?
            //
            data.errorLogger = new StreamLogger(data.psqlProcess.getErrorStream(), "ERROR");
        
            // any output?
            data.outputLogger = new StreamLogger(data.psqlProcess.getInputStream(), "OUTPUT");
            
            // Where do we send the data to?
            //
            data.pgOutputStream = new BufferedOutputStream(new FileOutputStream(new File("/tmp/load0.dat")), 5000000); // FIFO file
            
            // kick them off
            new Thread(data.errorLogger).start();
            new Thread(data.outputLogger).start();                              

            // OK, now we need to feed the data into the pgOutputStream
            //
            
        }
        catch ( Exception ex )
        {
        	throw new KettleException("Error while executing psql : " + createCommandLine(meta, false), ex);
        }
        
        return true;
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(PGBulkLoaderMeta)smi;
		data=(PGBulkLoaderData)sdi;

		try
		{
			Object[] r=getRow();  // Get row from input rowset & set row busy!
			
			if (r==null)          // no more input to be expected...
			{
				setOutputDone();

				// Close the output stream...
				//
				data.pgOutputStream.flush();
				data.pgOutputStream.close();
				
                // wait for the pgsql process to finish and check for any error...
				//
            	int exitVal = data.psqlProcess.waitFor();
				logBasic(Messages.getString("GPBulkLoader.Log.ExitValuePsqlPath", "" + exitVal)); //$NON-NLS-1$
	            
				return false;
			}
			
			if (first)
			{
				first=false;
				createControlFile(environmentSubstitute(meta.getControlFile()), r, meta);
				
				// Cache field indexes.
				//
				data.keynrs = new int[meta.getFieldStream().length];
				for (int i=0;i<data.keynrs.length;i++) {
					data.keynrs[i] = getInputRowMeta().indexOfValue(meta.getFieldStream()[i]);
				}

				// execute the psql statement...
				//
				execute(meta, true);
			}
			
			writeRowToPostgres(getInputRowMeta(), r);
			
			putRow(getInputRowMeta(), r);
			incrementLinesOutput();
			
			return true;
		}
		catch(Exception e)
		{
			logError(Messages.getString("GPBulkLoader.Log.ErrorInStep"), e); //$NON-NLS-1$
			setErrors(1);
			stopAll();
			setOutputDone();  // signal end to receiver(s)
			return false;
		} 
	}

    private void writeRowToPostgres(RowMetaInterface rowMeta, Object[] r) throws KettleException {
		
    	try {
	    	// So, we have this output stream to which we can write CSV data to.
	    	// Basically, what we need to do is write the binary data (from strings to it as part of this proof of concept)
	    	//
    		// Let's assume the data is in the correct format here too.
			//
			for (int i=0;i<data.keynrs.length;i++) {
				if (i>0) {
		    		// Write a separator 
		    		//
		    		data.pgOutputStream.write(data.separator);
				}
				
	    		int index = data.keynrs[i];
	    		ValueMetaInterface valueMeta = rowMeta.getValueMeta(index);
	    		Object valueData = r[index];
	    		
	    		if (valueData!=null) {
		    		switch(valueMeta.getType()) {
		    		case ValueMetaInterface.TYPE_STRING :
		    			data.pgOutputStream.write(data.quote);
		    			if (valueMeta.isStorageBinaryString()) {
		    				// lazy conversion.  For this test, just dump the data to the output stream.
		    				//
		    				
		    				data.pgOutputStream.write((byte[])valueData);
		    			} else {
		    				data.pgOutputStream.write(valueMeta.getString(valueData).getBytes());
		    			}
		    			data.pgOutputStream.write(data.quote);
		    			break;
		    		case ValueMetaInterface.TYPE_INTEGER:
		    			if (valueMeta.isStorageBinaryString()) {
		    				data.pgOutputStream.write((byte[])valueData);
		    			} else {
		    				data.pgOutputStream.write(Long.toString(valueMeta.getInteger(valueData)).getBytes());
		    			}
		    			break;
		    		case ValueMetaInterface.TYPE_DATE:
		    			break;
		    			// TODO
		    		case ValueMetaInterface.TYPE_BOOLEAN:
		    			if (valueMeta.isStorageBinaryString()) {
		    				data.pgOutputStream.write((byte[])valueData);
		    			} else {
		    				data.pgOutputStream.write(Double.toString(valueMeta.getNumber(valueData)).getBytes());
		    			}
		    			break;
		    		case ValueMetaInterface.TYPE_NUMBER:
		    			if (valueMeta.isStorageBinaryString()) {
		    				data.pgOutputStream.write((byte[])valueData);
		    			} else {
		    				data.pgOutputStream.write(Double.toString(valueMeta.getNumber(valueData)).getBytes());
		    			}
		    			break;
		    		}
	    		}
	    	}
			
			// Now write a newline
			//
			data.pgOutputStream.write(data.newline);
    	}
    	catch(Exception e)
    	{
    		throw new KettleException("Error serializing rows of data to the psql command", e);
    	}
		
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(PGBulkLoaderMeta)smi;
		data=(PGBulkLoaderData)sdi;
		
		Trans trans = getTrans();
		preview = trans.isPreview();
		
		if (super.init(smi, sdi))
		{			
			data.quote = "\"".getBytes();
			data.separator = ";".getBytes();
			data.newline = Const.CR.getBytes();

			return true;
		}
		return false;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (PGBulkLoaderMeta)smi;
	    data = (PGBulkLoaderData)sdi;

	    super.dispose(smi, sdi);
	    
	    if ( !preview && meta.isEraseFiles() )
	    {
	       // Erase the created cfg/dat files if requested. We don't erase
	       // the rest of the files because it would be "stupid" to erase them
	       // right after creation. If you don't want them, don't fill them in.
	       FileObject fileObject = null;
	       
	       String method = meta.getLoadMethod();
	       if (  // GPBulkLoaderMeta.METHOD_AUTO_CONCURRENT.equals(method) ||
	    		 PGBulkLoaderMeta.METHOD_AUTO_END.equals(method))
	       {
	 	       if ( meta.getControlFile() != null )
		       {
			       try
			       {
		               fileObject = KettleVFS.getFileObject(environmentSubstitute(meta.getControlFile()));			   
		               fileObject.delete();
		               fileObject.close();
	  	           }
		           catch ( IOException ex )
		           {
		               logError("Error deleting control file \'" + KettleVFS.getFilename(fileObject) + "\': " + ex.getMessage());
		           }
		       }
		   }

	       if (  PGBulkLoaderMeta.METHOD_AUTO_END.equals(method) )
	       {
	    	   // In concurrent mode the data is written to the control file.
	 	       if ( meta.getDataFile() != null )
		       {
			       try
			       {
		               fileObject = KettleVFS.getFileObject(environmentSubstitute(meta.getDataFile()));			   
		               fileObject.delete();
		               fileObject.close();
	  	           }
		           catch ( IOException ex )
		           {
		               logError("Error deleting data file \'" + KettleVFS.getFilename(fileObject) + "\': " + ex.getMessage());
		           }
		       }
	       }	       
	       
	       if (  PGBulkLoaderMeta.METHOD_MANUAL.equals(method) )
	       {
	           logBasic("Deletion of files is not compatible with \'manual load method\'");	   
	       }
	    }
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