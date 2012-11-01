/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package org.pentaho.di.trans.steps.monetdbbulkloader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.MonetDBDatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
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
import org.pentaho.di.trans.steps.monetdbagilemart.MonetDBRowLimitException;
import org.pentaho.di.trans.steps.tableagilemart.AgileMartUtil;


/**
 * Performs a bulk load to a MonetDB table.
 *
 * Based on (copied from) Sven Boden's Oracle Bulk Loader step
 * 
 * @author matt
 * @since  22-aug-2008
 */
public class MonetDBBulkLoader extends BaseStep implements StepInterface
{
	private static Class<?> PKG = MonetDBBulkLoaderMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private MonetDBBulkLoaderMeta meta;
	private MonetDBBulkLoaderData data;
	private String message;
	private TransMeta localTransMeta;
	protected long rowsWritten = -1;
	private AgileMartUtil util = new AgileMartUtil();
	
	public String getMessage() {
		return message;
	}

	public MonetDBBulkLoader(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		localTransMeta = transMeta;
	}

	protected void setMessage( String message ) {
		this.message = message;
	}
	
	protected MonetDBBulkLoaderMeta getMeta() {
		return meta;
	}
	
	protected String escapeOsPath( String path, boolean isWindows ) {
		
		StringBuffer sb = new StringBuffer();
		
		// should be done with a regex
		for( int i=0; i<path.length(); i++ ) {
			char c = path.charAt(i);
			if( c == ' ') {
				sb.append( isWindows ? "^ " : "\\ " );
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	/**
	 * Create the command line for a psql process depending on the meta
	 * information supplied.
	 * 
	 * @param meta The meta data to create the command line from
	 * 
	 * @return The string to execute.
	 * 
	 * @throws KettleException Upon any exception
	 */
	public String createCommandLine(MonetDBBulkLoaderMeta meta, boolean lSql) throws KettleException
	{
		   StringBuffer sb = new StringBuffer(300);
	   
	   String osName = System.getProperty("os.name");
	   boolean isWindows = osName.toLowerCase().indexOf("windows") != -1;
	   if ( !Const.isEmpty(meta.getMClientPath()) )
	   {
		   try
		   {
	           FileObject fileObject = KettleVFS.getFileObject(environmentSubstitute(meta.getMClientPath()), getTransMeta());
  	      	   String psqlexec = KettleVFS.getFilename(fileObject);
  	      	   psqlexec = escapeOsPath( psqlexec, isWindows );
		       sb.append(psqlexec);
  	       }
	       catch ( KettleFileException ex )
	       {
	           throw new KettleException("Error retrieving mclient application string", ex);
	       }		       
	   }
	   else
	   {
		   throw new KettleException("No mclient application specified");
	   }

	   String enclosure = isWindows ? "\"" : "";
	   
	   if( isWindows ) {
		   sb.append(" /STARTED-FROM-MENU");
	   }
	   
	   // Add standard options to the mclient command:
	   //
	   if( lSql ) {
		   sb.append(" -lsql");
	   }
	   // See if the encoding is set...
	   //
	   if ( !Const.isEmpty(meta.getEncoding()))
	   {
		   sb.append(" ").append(enclosure).append("--encoding=");
		   sb.append(environmentSubstitute(meta.getEncoding())).append(enclosure);
	   }
	   
//	   if ( !Const.isEmpty(meta.getLogFile()))
//	   {
//		   try 
//		   {
//		       FileObject fileObject = KettleVFS.getFileObject(environmentSubstitute(meta.getLogFile()), getTransMeta());   
//	    	   sb.append(" ").append(enclosure).append("--log=");
//		       sb.append('\'').append(KettleVFS.getFilename(fileObject)).append('\'').append(enclosure);
//		   }
//		   catch ( KettleFileException ex )
//		   {
//		       throw new KettleException("Error retrieving logfile string", ex);
//		   }
//	   }
	   
       DatabaseMeta dm = meta.getDatabaseMeta();
       if ( dm != null )
       {
           String hostname = environmentSubstitute(Const.NVL(dm.getHostname(), ""));
           String portnum  = environmentSubstitute(Const.NVL(dm.getDatabasePortNumberString(), ""));
           String dbname   = environmentSubstitute(Const.NVL(dm.getDatabaseName(), ""));

           if (!Const.isEmpty(hostname)) {
        	   sb.append(" ").append(enclosure).append("--host=").append(hostname).append(enclosure);
           }
           if (!Const.isEmpty(portnum) && Const.toInt(portnum, -1)>0) {
        	   sb.append(" ").append(enclosure).append("--port=").append(portnum).append(enclosure);
           }
           if (!Const.isEmpty(dbname)) {
        	   sb.append(" ").append(enclosure).append("--database=").append(dbname).append(enclosure);
           }
       }
	   else
	   {
		   throw new KettleException("No connection specified");
	   }

	   return sb.toString(); 
	}
	
	public boolean execute(MonetDBBulkLoaderMeta meta, boolean wait) throws KettleException
	{
		Runtime rt = Runtime.getRuntime();
		if (log.isDetailed()) logDetailed("Started execute" );

    	String cmdLSql = null;
		if (log.isDetailed()) logDetailed("Creating commands" );
        try 
        {
        	cmdLSql = createCommandLine(meta, true);
        }
        catch ( Exception ex )
        {
           	throw new KettleException("Error while generating MonetDB commands", ex);
        }
		if (log.isDetailed()) logDetailed("Created command: "+cmdLSql );

            try  
        {

       		if (log.isDetailed()) logDetailed("Auto String Length flag: "+meta.isAutoStringWidths() );
        	
        	logBasic("Executing command: "+cmdLSql);
			ProcessHolder holder = startMClient( rt, cmdLSql );

			if( !holder.isRunning ) {
				message = holder.message;
				throw new KettleException("An error occurred writing data to the mclient process: "+message);
			}
        	
        	data.mClientlProcess = holder.process;
            
            // any error message?
            //
            data.errorLogger = new StreamLogger(log, data.mClientlProcess.getErrorStream(), "ERROR");
        
            // any output?
            data.outputLogger = new StreamLogger(log, data.mClientlProcess.getInputStream(), "OUTPUT");
            
            // Where do we send the data to?  --> To STDIN of the mclient process
            //
            data.monetOutputStream = data.mClientlProcess.getOutputStream();
            
            // kick them off
            new Thread(data.errorLogger).start();
            new Thread(data.outputLogger).start();                              

            // OK, from here on, we need to feed the COPY INTO command followed by the data into the monetOutputStream
            //
        }
        catch ( Exception ex )
        {
        	throw new KettleException("Error while executing mclient : " + cmdLSql, ex);
        }
        
        return true;
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(MonetDBBulkLoaderMeta)smi;
		data=(MonetDBBulkLoaderData)sdi;

		try
		{
			Object[] r=getRow();  // Get row from input rowset & set row busy!
			if (r==null)          // no more input to be expected...
			{
				setOutputDone();

	    		writeBufferToMonetDB();
				// Close the output stream...
				//
	    		if( data.monetOutputStream != null ) {
	    			data.monetOutputStream.flush();
	    			data.monetOutputStream.close();
	                // wait for the mclient process to finish and check for any error...
					//
	            	int exitVal = data.mClientlProcess.waitFor();
					logBasic(BaseMessages.getString(PKG, "MonetDBBulkLoader.Log.ExitValuePsqlPath", "" + exitVal)); //$NON-NLS-1$
	    		}
	    		util.updateMetadata( meta, rowsWritten );
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
			}
			
			writeRowToMonetDB(getInputRowMeta(), r);
			putRow(getInputRowMeta(), r);
			incrementLinesOutput();

			return true;
		}
    catch(MonetDBRowLimitException me) {
      // we need to stop processing and clean up
      logDebug(me.getMessage());
      stopAll();
      setOutputDone();
      return true;
    } catch(Exception e) {
			logError(BaseMessages.getString(PKG, "MonetDBBulkLoader.Log.ErrorInStep"), e); //$NON-NLS-1$
			setErrors(1);
			stopAll();
			setOutputDone();  // signal end to receiver(s)
			return false;
		} 
	}

    protected void writeRowToMonetDB(RowMetaInterface rowMeta, Object[] r) throws KettleException {
    	if (data.bufferIndex==data.bufferSize || log.isDebug() ) {
    		writeBufferToMonetDB();
    	}
		addRowToBuffer(rowMeta, r);
    }

	protected void addRowToBuffer(RowMetaInterface rowMeta, Object[] r) throws KettleException {

    	ByteArrayOutputStream line = new ByteArrayOutputStream(25000);
    	
    	try {
	    	// So, we have this output stream to which we can write CSV data to.
	    	// Basically, what we need to do is write the binary data (from strings to it as part of this proof of concept)
	    	//
    		// The data format required is essentially:
    		//
    		for (int i=0;i<data.keynrs.length;i++) {
				if (i>0) {
		    		// Write a separator 
		    		//
		    		line.write(data.separator);
				}
				
	    		int index = data.keynrs[i];
	    		ValueMetaInterface valueMeta = rowMeta.getValueMeta(index);
	    		Object valueData = r[index];
	    		
	    		if (valueData!=null) {
		    		switch(valueMeta.getType()) {
		    		case ValueMetaInterface.TYPE_STRING :
		    			line.write(data.quote);
		    			// we have to convert to strings to escape '\'s
	    				String str = valueMeta.getString(valueData);
	    				if( str == null ) {
	    					line.write("null".getBytes());
	    				} else {
		    				// escape any backslashes
			    			str = str.replace("\\", "\\\\");
                str = str.replace("\"", "\\\"");
                if(meta.isAutoStringWidths()) {
			    				int len = valueMeta.getLength();
			    				if( len < 1 ) {
			    					len = MonetDBDatabaseMeta.DEFAULT_VARCHAR_LENGTH;
			    				}
			    				if( str.length() > len ) {
			    					// TODO log this event
			    					str = str.substring(0, len);
			    				}
			    				line.write(str.getBytes(meta.getEncoding()));
			    			} else {
			    				line.write(str.getBytes(meta.getEncoding()));
			    			}
	    				}
		    			line.write(data.quote);
		    			break;
		    		case ValueMetaInterface.TYPE_INTEGER:
		    			if (valueMeta.isStorageBinaryString() && meta.getFieldFormatOk()[i]) {
		    				line.write((byte[])valueData);
		    			} else {
		    				Long value = valueMeta.getInteger(valueData);
		    				if( value == null ) {
		    					line.write("null".getBytes());
		    				} else {
		    					line.write(Long.toString(value).getBytes());
		    				}
		    			}
		    			break;
		    		case ValueMetaInterface.TYPE_DATE:
		    			// Keep the data format as indicated.
		    			//
		    			if (valueMeta.isStorageBinaryString() && meta.getFieldFormatOk()[i]) {
		    				line.write((byte[])valueData);
		    			} else {
		    				Date value = valueMeta.getDate(valueData);
		    				// Convert it to the MonetDB date format "yyyy/MM/dd HH:mm:ss"
		    				if( value == null ) {
		    					line.write("null".getBytes());
		    				} else {
		    					line.write(data.monetDateMeta.getString(value).getBytes());
		    				}
		    			}
		    			break;
		    		case ValueMetaInterface.TYPE_BOOLEAN:
	    				{
	    					Boolean value = valueMeta.getBoolean(valueData);
		    				if( value == null ) {
		    					line.write("null".getBytes());
		    				} else {
		    					if( value.booleanValue() ) {
		    						line.write("Y".getBytes());
		    					} else {
		    						line.write("N".getBytes());
		    					}
		    				}
	    				}
		    			break;
		    		case ValueMetaInterface.TYPE_NUMBER:
		    			if (valueMeta.isStorageBinaryString() && meta.getFieldFormatOk()[i]) {
		    				line.write((byte[])valueData);
		    			} else {
		    				Double value = valueMeta.getNumber(valueData);
		    				if( value == null ) {
		    					line.write("null".getBytes());
		    				} else {
		    					line.write(Double.toString(value).getBytes());
		    				}
		    			}
		    			break;
		    		case ValueMetaInterface.TYPE_BIGNUMBER:
		    			if (valueMeta.isStorageBinaryString() && meta.getFieldFormatOk()[i]) {
		    				line.write((byte[])valueData);
		    			} else {
		    				String value = valueMeta.getString(valueData);
		    				if( value == null ) {
		    					line.write("null".getBytes());
		    				} else {
		    					line.write(value.getBytes());
		    				}
		    			}
		    			break;
		    		}
	    		} else {
    				line.write("null".getBytes());
	    		}
	    	}
			
			// finally write a newline
			//
			line.write(data.newline);
			if( log.isDebug() ) log.logDebug( new String(line.toByteArray()) );
			
			// Now that we have the line, grab the content and store it in the buffer...
			//
			data.rowBuffer[data.bufferIndex] = line.toByteArray();
			data.bufferIndex++;
    	}
    	catch(Exception e)
    	{
    		throw new KettleException("Error serializing rows of data to the psql command", e);
    	}
		
	}
	
	public void truncateTable( Runtime rt, String mClientCmd ) throws KettleException {
		
    	try {
		   if (log.isDetailed()) logDetailed("attempting to truncate table" );
			ProcessHolder holder = startMClient( rt, mClientCmd );
			if(!holder.isRunning ) {
				message = holder.message;
				throw new KettleException("An error occurred writing data to the mclient process: "+message);
			}
		  	
		  	String cmd;
		  	cmd = meta.getDatabaseMeta().getTruncateTableStatement(null, data.schemaTable)+";";		  	
		  	
		  	if (log.isDetailed()) logDetailed("Trying: "+cmd);
		  	holder.stdIn.write(cmd.getBytes());
		   
		  	holder.stdIn.flush();
		  	holder.stdIn.close();
		    // wait for the process to finish and check for any error...

		   int exitVal = holder.process.waitFor();
 		   byte buffer[] = new byte[4096];
 		  holder.stdOut.read(buffer);
 		   message = new String( buffer );
		   logBasic(BaseMessages.getString(PKG, "MonetDBBulkLoader.Log.ExitValuePsqlPath", "" + exitVal)); //$NON-NLS-1$
		   if( exitVal != 0 ) {
			   logError(BaseMessages.getString(PKG, "MonetDBBulkLoader.Log.ExitMessage",message)); //$NON-NLS-1$
		   } else {
			   logDebug(BaseMessages.getString(PKG, "MonetDBBulkLoader.Log.ExitMessage",message)); //$NON-NLS-1$
		   }
 		   if( exitVal != 0 ) {
	     		throw new KettleException("An error occurred executing a statement");
		   }
	
 		   // try to update the metadata registry
 		   util.updateMetadata( meta, -1 );
		  	if (log.isDetailed()) logDetailed("Successfull: "+cmd);
	    
    	}
    	catch(Exception e) {
    		throw new KettleException("An error occurred writing data to the mclient process", e);
    	}		
	}

	protected ProcessHolder startMClient( Runtime rt, String command ) {
		ProcessHolder holder = new ProcessHolder();
		holder.isRunning = true;
    	try {
    		
		   holder.process = rt.exec(command);
		   holder.stdIn = holder.process.getOutputStream();
		   holder.stdOut = holder.process.getInputStream();
		   holder.stdErr = holder.process.getErrorStream();

		  	try {
				int exitValue = holder.process.exitValue();
				// if we get here, mclient has terminated
				byte buffer[] = new byte[4096];
				holder.stdErr.read(buffer);
				holder.message = new String(buffer);
				holder.isRunning = false;
		  	} catch (Exception e) {
		  		// mclient is still running, this is a good thing
		  	}
    	} catch (Exception e) {
    		log.logError("Could not execute MonetDB mclient command: "+command);
    	}
	  	return holder;
	}
	
	public void dropTable( Runtime rt, String mClientCmd ) throws KettleException {
		
		if (log.isDetailed()) logDetailed("attempting to truncate table" );

		ProcessHolder holder = startMClient( rt, mClientCmd );
		if(!holder.isRunning ) {
			message = holder.message;
			throw new KettleException("An error occurred writing data to the mclient process: "+message);
		}
		  	
		try {
			   // this will fail if the table does not exist
		  	String cmd;
		  	cmd = "drop table " + data.schemaTable+";";		  	
		  	
		  	if (log.isDetailed()) logDetailed("Trying: "+cmd);
		  	holder.stdIn.write(cmd.getBytes());
		   
		  	holder.stdIn.flush();
		  	holder.stdIn.close();
		    // wait for the process to finish and check for any error...

		   int exitVal = holder.process.waitFor();
 		   byte buffer[] = new byte[4096];
 		  holder.stdOut.read(buffer);
 		   message = new String(buffer);
		   logBasic(BaseMessages.getString(PKG, "MonetDBBulkLoader.Log.ExitValuePsqlPath", "" + exitVal)); //$NON-NLS-1$
		   logDebug(BaseMessages.getString(PKG, "MonetDBBulkLoader.Log.ExitMessage",message)); //$NON-NLS-1$
 		   // try to update the metadata registry
 		   util.updateMetadata( meta, -1 );
		  	if (log.isDetailed()) logDetailed("Successfull: "+cmd);

    	}
    	catch(Exception e) {
    		throw new KettleException("An error occurred writing data to the mclient process", e);
    	}		
	}	
	
	public void autoAdjustSchema( MonetDBBulkLoaderMeta meta, Runtime rt, String mClientCmd )  throws KettleException {
		
		ProcessHolder holder = null;
    	try {
 		   if (log.isDetailed()) logDetailed("Attempting to auto adjust table structure" );
 		
 		   // monetDB cannot alter table column definitions
 		   dropTable(rt, mClientCmd);
			mClientCmd = createCommandLine(meta, false);
 		   
			holder = startMClient( rt, mClientCmd );
			if(!holder.isRunning ) {
				message = holder.message;
				throw new KettleException("An error occurred writing data to the mclient process: "+message);
			}
 		  	
		   if (log.isDetailed()) logDetailed("getTransMeta: "+getTransMeta() );
   		   if (log.isDetailed()) logDetailed("getStepname: "+getStepname() );
   		   SQLStatement statement = meta.getTableDdl(getTransMeta(), getStepname(), true, data, true);
   		   if (log.isDetailed()) logDetailed("Statement: "+statement );
  		   if (log.isDetailed() && statement != null) logDetailed("Statement has SQL: "+statement.hasSQL() );
    		
  		   if(statement != null && statement.hasSQL()) {
    			String cmd = statement.getSQL();
    			this.message = "";
     		  	if (log.isDetailed()) logDetailed("Trying: "+cmd);
     		  	holder.stdIn.write(cmd.getBytes());
    			holder.stdIn.flush();
    			holder.stdIn.close();
     		    // wait for the process to finish and check for any error...
     		  	try {
          		   int exitVal = holder.process.waitFor();
         		   byte buffer[] = new byte[4096];
         		  holder.stdOut.read(buffer);
         		   this.message = new String(buffer);
         		   logBasic(BaseMessages.getString(PKG, "MonetDBBulkLoader.Log.ExitValuePsqlPath", "" + exitVal)); //$NON-NLS-1$
        		   if( exitVal != 0 ) {
        			   logError(BaseMessages.getString(PKG, "MonetDBBulkLoader.Log.ExitMessage",message)); //$NON-NLS-1$
        		   } else {
        			   logDebug(BaseMessages.getString(PKG, "MonetDBBulkLoader.Log.ExitMessage",message)); //$NON-NLS-1$
        		   }
         		   if( exitVal != 0 ) {
         	     		throw new KettleException("An error occurred executing a statement");
         		   }
     		  	} catch(Exception e) {
     	     		// can we get an error message
     	     		if( holder != null && holder.stdOut != null ) {
     	      		   byte buffer[] = new byte[4096];
     	     		   try {
     	     			  holder.stdOut.read(buffer);
     		     		   this.message = new String(buffer);
     		     		   logError(BaseMessages.getString(PKG, "MonetDBBulkLoader.Log.ExitMessage", new String(buffer))); //$NON-NLS-1$
     	     		   } catch (IOException e1) {
     					// well we tried
     	     		   }
     	     		}
     	     		throw new KettleException("An error occurred writing data to the mclient process", e);
     	     	}

  		   } else {
  			   this.message = statement.getError();
  			   logError(statement.getError());
  	     		throw new KettleException("An error occurred creating SQL statement");
  		   }
 		  	 		   
     	}
     	catch(Exception e) {
     		throw new KettleException("An error occurred writing data to the mclient process", e);
     	}		
		if (log.isDetailed()) logDetailed("Successfull");
	}
		
    protected void writeBufferToMonetDB() throws KettleException {
    	if (data.bufferIndex==0) return;
    	
    	try {
	    	// first write the COPY INTO command...
	    	//
    		StringBuffer cmdBuff = new StringBuffer();
    		cmdBuff.append( "COPY " )
    		.append(data.bufferIndex)
    		.append(" RECORDS INTO ")
    		.append(data.schemaTable)
    		.append(" FROM STDIN USING DELIMITERS '")
    		.append(new String(data.separator))
    		.append("','\\n','")
    		.append(new String(data.quote))
    		.append("';");
    		String cmd = cmdBuff.toString();
	    	if (log.isDetailed()) logDetailed(cmd);
	    	data.monetOutputStream.write(cmd.getBytes());
	    	
	    	for (int i=0;i<data.bufferIndex;i++) {
	    		data.monetOutputStream.write(data.rowBuffer[i]);
		    	if (log.isRowLevel()) logRowlevel(new String(data.rowBuffer[i]));
	    	}
	    	
	    	// Also write an empty row
	    	//
//	    	data.monetOutputStream.write(Const.CR.getBytes());
	    	if (log.isRowLevel()) logRowlevel(Const.CR);
	    	
	    	// reset the buffer pointer...
	    	//
	    	data.bufferIndex=0;
    	}
    	catch(Exception e) {
    		throw new KettleException("An error occurred writing data to the mclient process", e);
    	}
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(MonetDBBulkLoaderMeta)smi;
		data=(MonetDBBulkLoaderData)sdi;

		if (super.init(smi, sdi))
		{			
			data.quote = "\"".getBytes();
			data.separator = "|".getBytes();
			data.newline = Const.CR.getBytes();

			data.monetDateMeta = new ValueMeta("dateMeta", ValueMetaInterface.TYPE_DATE);
			data.monetDateMeta.setConversionMask("yyyy/MM/dd HH:mm:ss");
			data.monetDateMeta.setStringEncoding(meta.getEncoding());

			data.monetNumberMeta = new ValueMeta("numberMeta", ValueMetaInterface.TYPE_NUMBER);
			data.monetNumberMeta.setConversionMask("#.#");
			data.monetNumberMeta.setGroupingSymbol(",");
			data.monetNumberMeta.setDecimalSymbol(".");
			data.monetNumberMeta.setStringEncoding(meta.getEncoding());

			data.bufferSize = Const.toInt(environmentSubstitute(meta.getBufferSize()), 100000);
			
			// Allocate the buffer
			// 
			data.rowBuffer = new byte[data.bufferSize][];
			data.bufferIndex = 0;
			
			//
			String connectionName = meta.getDbConnectionName();
			if (!Const.isEmpty(connectionName) && connectionName.startsWith("${") && connectionName.endsWith("}")) {
				meta.setDatabaseMeta(localTransMeta.findDatabase(environmentSubstitute(connectionName)));	
			}
			
			// Schema-table combination...
			data.schemaTable = meta.getDatabaseMeta(this).getQuotedSchemaTableCombination(
			    environmentSubstitute(meta.getSchemaName()), 
			    environmentSubstitute(meta.getTableName())
			  );
			
			return true;
		}
		return false;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (MonetDBBulkLoaderMeta)smi;
	    data = (MonetDBBulkLoaderData)sdi;

	    // Close the mclient output stream
	    //
	    try {
	    	if( data.monetOutputStream != null ) {
	    		data.monetOutputStream.close();
		    	int exitVal = data.mClientlProcess.waitFor();
				   logBasic(BaseMessages.getString(PKG, "MonetDBBulkLoader.Log.ExitValuePsqlPath", "" + exitVal)); //$NON-NLS-1$
				   if( exitVal != 0 ) {
					   logError(BaseMessages.getString(PKG, "MonetDBBulkLoader.Log.ExitMessage",message)); //$NON-NLS-1$
				   } else {
					   logDebug(BaseMessages.getString(PKG, "MonetDBBulkLoader.Log.ExitMessage",message)); //$NON-NLS-1$
				   }
		 		   if( exitVal != 0 ) {
			     		throw new KettleException("An error occurred executing a statement");
				   }
	    	}
	    }
	    catch(Exception e) {
	    	setErrors(1L);
	    	logError("Unexpected error encountered while finishing the mclient process", e);
	    }
	    
	    super.dispose(smi, sdi);
	}

	protected MonetDBBulkLoaderData getData() {
		return this.data;
	}

	private class ProcessHolder {
		Process process;
	  	boolean isRunning = true;
	  	OutputStream stdIn = null;
	  	InputStream stdOut = null;
	  	InputStream stdErr = null;
	  	String message;
		
	}
	
}

