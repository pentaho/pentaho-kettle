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

import java.math.BigDecimal;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.StreamLogger;
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
 * Performs a bulk load to a postgres table.
 *
 * Based on (copied from) Sven Boden's Oracle Bulk Loader step
 * 
 * @author matt
 * @since  28-mar-2008
 */
public class PGBulkLoader extends BaseStep implements StepInterface
{
	private static Class<?> PKG = PGBulkLoaderMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private PGBulkLoaderMeta meta;
	private PGBulkLoaderData data;	
	
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
	public String getCopyCommand(RowMetaInterface rm, Object[] r) throws KettleException
	{
		DatabaseMeta dm = meta.getDatabaseMeta();

		String loadAction = meta.getLoadAction();

		StringBuffer contents = new StringBuffer(500);

        String tableName = dm.getQuotedSchemaTableCombination(
            environmentSubstitute(meta.getSchemaName()),
            environmentSubstitute(meta.getTableName()));
        
        // Set the date style...
        //
        // contents.append("SET DATESTYLE ISO;"); // This is the default but we set it anyway...
        // contents.append(Const.CR);

        // Create a Postgres / Greenplum COPY string for use with a psql client
        if (loadAction.equalsIgnoreCase("truncate")) {
            contents.append("TRUNCATE TABLE ");
            contents.append(tableName + ";");
            contents.append(Const.CR);
        }
        contents.append("COPY ");
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
        contents.append(" FROM STDIN"); // FIFO file

        // The "FORMAT" clause
        contents.append(" WITH CSV DELIMITER AS '").append(meta.getDelimiter()).append("' QUOTE AS '").append(meta.getEnclosure()).append("'");
        contents.append(";").append(Const.CR);

		return contents.toString();
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
	           FileObject fileObject = KettleVFS.getFileObject(environmentSubstitute(meta.getPsqlpath()), getTransMeta());
  	      	   String psqlexec = Const.optionallyQuoteStringByOS(KettleVFS.getFilename(fileObject));
		       sb.append(psqlexec);
  	       }
	       catch ( KettleFileException ex )
	       {
	           throw new KettleException("Error retrieving sqlldr string", ex);
	       }		       
	   }
	   else
	   {
		   if(isDetailed()) logDetailed( "psql defaults to system path");
		   sb.append("psql");
	   }

       DatabaseMeta dm = meta.getDatabaseMeta();
       if ( dm != null )
       {

		   // Note: Passwords are not supported directly, try configuring your connection for trusted access using pg_hba.conf
           //

           // The username
           // 
    	   String user = Const.NVL(dm.getUsername(), "");
           sb.append(" -U ").append(environmentSubstitute(user));

           // Hostname and portname
           //
           String hostname = environmentSubstitute(Const.NVL(dm.getHostname(), ""));
           String portnum = environmentSubstitute(Const.NVL(dm.getDatabasePortNumberString(), ""));
           sb.append(" -h ");
           sb.append(hostname);
           sb.append(" -p ");
           sb.append(portnum);
           
           // Database Name
           // 
           String dns  = environmentSubstitute(Const.NVL(dm.getDatabaseName(), ""));
           sb.append(" ");
           
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
            data.errorLogger = new StreamLogger(log, data.psqlProcess.getErrorStream(), "ERROR {0}");
        
            // any output?
            data.outputLogger = new StreamLogger(log, data.psqlProcess.getInputStream(), "OUTPUT {0}");
            
            // Where do we send the data to? --> To STDIN of the psql process
            //
            data.pgOutputStream = data.psqlProcess.getOutputStream();
            
            // kick them off
            new Thread(data.errorLogger).start();
            new Thread(data.outputLogger).start();                              

            // OK, from here on, we need to feed in the COPY command followed by the data into the pgOutputStream
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
				logBasic(BaseMessages.getString(PKG, "GPBulkLoader.Log.ExitValuePsqlPath", "" + exitVal)); //$NON-NLS-1$
	            
				return false;
			}
			
			if (first)
			{
				first=false;
				
				// Cache field indexes.
				//
				data.keynrs = new int[meta.getFieldStream().length];
				for (int i=0;i<data.keynrs.length;i++) {
					data.keynrs[i] = getInputRowMeta().indexOfValue(meta.getFieldStream()[i]);
				}

				// execute the psql statement...
				//
				execute(meta, true);
				
				String copyCmd = getCopyCommand(getInputRowMeta(), r);
				logBasic("Launching command: "+copyCmd);
				data.pgOutputStream.write(copyCmd.getBytes());

				// Write rows of data hereafter...
				//
			}
			
			writeRowToPostgres(getInputRowMeta(), r);
			
			putRow(getInputRowMeta(), r);
			incrementLinesOutput();
			
			return true;
		}
		catch(Exception e)
		{
			logError(BaseMessages.getString(PKG, "GPBulkLoader.Log.ErrorInStep"), e); //$NON-NLS-1$
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

		    			// No longer dump the bytes for a Lazy Conversion;
		    			// We need to escape the quote characters in every string
		    			String quoteStr = new String(data.quote); 
		    			String escapedString = valueMeta.getString(valueData).replace(quoteStr,quoteStr+quoteStr); 
		    			data.pgOutputStream.write(escapedString.getBytes());

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
		    			// Format the date in the right format.
		    			// 
		    			switch(data.dateFormatChoices[i]) {
		    			// Pass the data along in the format chosen by the user OR in binary format...
		    			//
		    			case PGBulkLoaderMeta.NR_DATE_MASK_PASS_THROUGH:
			    			{
				    			if (valueMeta.isStorageBinaryString()) {
				    				data.pgOutputStream.write((byte[])valueData);
				    			} else {
				    				String dateString = valueMeta.getString(valueData);
				    				if (dateString!=null) {
				    					data.pgOutputStream.write(dateString.getBytes());
				    				}
				    			}
			    			}
			    			break;
			    		// Convert to a "YYYY/MM/DD" format
			    	    //
		    			case PGBulkLoaderMeta.NR_DATE_MASK_DATE:
			    			{
			    				String dateString = data.dateMeta.getString(valueMeta.getDate(valueData));
			    				if (dateString!=null) {
			    					data.pgOutputStream.write(dateString.getBytes());
			    				}
			    			}
		    			break;
			    		// Convert to a "YYYY/MM/DD HH:MM:SS" (ISO) format
			    	    //
		    			case PGBulkLoaderMeta.NR_DATE_MASK_DATETIME:
			    			{
			    				String dateTimeString = data.dateTimeMeta.getString(valueMeta.getDate(valueData));
			    				if (dateTimeString!=null) {
			    					data.pgOutputStream.write(dateTimeString.getBytes());
			    				}
			    			}
		    			break;
		    			}
		    			break;
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
		    		case ValueMetaInterface.TYPE_BIGNUMBER:
		    			if (valueMeta.isStorageBinaryString()) {
		    				data.pgOutputStream.write((byte[])valueData);
		    			} else {
		    				BigDecimal big = valueMeta.getBigNumber(valueData);
		    				if (big!=null) {
		    					data.pgOutputStream.write(big.toString().getBytes());
		    				}
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
		
		if (super.init(smi, sdi))
		{			
			if (meta.getEnclosure()!=null) data.quote = meta.getEnclosure().getBytes(); else data.quote = new byte[] {}; 
			if (meta.getDelimiter()!=null) data.separator = meta.getDelimiter().getBytes(); else data.separator = new byte[] {};
			data.newline = Const.CR.getBytes();

			data.dateFormatChoices = new int[meta.getFieldStream().length];
			for (int i=0;i<data.dateFormatChoices.length;i++) {
				if (Const.isEmpty(meta.getDateMask()[i])) {
					data.dateFormatChoices[i] = PGBulkLoaderMeta.NR_DATE_MASK_PASS_THROUGH;
				} else if (meta.getDateMask()[i].equalsIgnoreCase(PGBulkLoaderMeta.DATE_MASK_DATE)) {
					data.dateFormatChoices[i] = PGBulkLoaderMeta.NR_DATE_MASK_DATE;
				} else if (meta.getDateMask()[i].equalsIgnoreCase(PGBulkLoaderMeta.DATE_MASK_DATETIME)) {
					data.dateFormatChoices[i] = PGBulkLoaderMeta.NR_DATE_MASK_DATETIME;
				}  else { // The default : just pass it along...
					data.dateFormatChoices[i] = PGBulkLoaderMeta.NR_DATE_MASK_PASS_THROUGH;
				}

			}
			return true;
		}
		return false;
	}
	
}