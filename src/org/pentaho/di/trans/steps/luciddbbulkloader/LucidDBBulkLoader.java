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

package org.pentaho.di.trans.steps.luciddbbulkloader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
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
 * Performs a bulk load to a LucidDB table.
 *
 * Based on Sven Boden's Oracle Bulk Loader step
 * 
 * @author matt
 * @since  14-nov-2008
 */
public class LucidDBBulkLoader extends BaseStep implements StepInterface
{
	private LucidDBBulkLoaderMeta meta;
	private LucidDBBulkLoaderData data;
	
	public LucidDBBulkLoader(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	/**
	 * Create the command line for a LucidDB process depending on the meta
	 * information supplied.
	 * 
	 * @param meta The meta data to create the command line from
	 * @param password Use the real password or not
	 * 
	 * @return The string to execute.
	 * 
	 * @throws KettleException Upon any exception
	 */
	public String createCommandLine(LucidDBBulkLoaderMeta meta, boolean password) throws KettleException
	{
	   StringBuffer sb = new StringBuffer(300);
	   
	   if ( !Const.isEmpty(meta.getClientPath()) )
	   {
		   try
		   {
	           FileObject fileObject = KettleVFS.getFileObject(environmentSubstitute(meta.getClientPath()));
  	      	   String psqlexec = KettleVFS.getFilename(fileObject);
		       sb.append(psqlexec);
  	       }
	       catch ( IOException ex )
	       {
	           throw new KettleException("Error retrieving mclient application string", ex);
	       }		       
	   }
	   else
	   {
		   throw new KettleException("No mclient application specified");
	   }

	   // JDBC URL
	   //
	   sb.append(" -u '"+meta.getDatabaseMeta().getURL()+"'");

	   // Driver class
	   //
	   sb.append(" -d '"+meta.getDatabaseMeta().getDriverClass()+"'");

	   // username
	   //
	   if (!Const.isEmpty(meta.getDatabaseMeta().getUsername())) {
		   sb.append(" -n "+meta.getDatabaseMeta().getUsername());
	   }

	   // password
	   //
	   if (!Const.isEmpty(meta.getDatabaseMeta().getPassword())) {
		   sb.append(" -p "+meta.getDatabaseMeta().getPassword());
	   }

	   return sb.toString(); 
	}
	
	public boolean execute(LucidDBBulkLoaderMeta meta, boolean wait) throws KettleException
	{
        Runtime rt = Runtime.getRuntime();

        try  
        {
        	String tableName = environmentSubstitute(meta.getTableName());
        	
        	// 1) Set up the FIFO folder, create the directory and path to it... 
        	//
        	String fifoVfsDirectory = environmentSubstitute(meta.getFifoDirectory());
        	FileObject directory = KettleVFS.getFileObject(fifoVfsDirectory);
        	directory.createFolder();
        	String fifoDirectory = KettleVFS.getFilename(directory);
        	
        	// 2) Create the FIFO file using the "mkfifo" command...
        	//    Make sure to log all the possible output, also from STDERR
        	//
        	data.fifoFilename = KettleVFS.getFilename(directory)+Const.FILE_SEPARATOR+tableName+".csv";
        	data.bcpFilename = KettleVFS.getFilename(directory)+Const.FILE_SEPARATOR+tableName+".bcp";
        	
        	File fifoFile = new File(data.fifoFilename);
        	if (!fifoFile.exists()) {
	        	String mkFifoCmd = "mkfifo "+data.fifoFilename+"";
	        	logBasic("Creating FIFO file using this command : "+mkFifoCmd);
	        	Process mkFifoProcess = rt.exec(mkFifoCmd);
	        	StreamLogger errorLogger = new StreamLogger(mkFifoProcess.getErrorStream(), "mkFifoError");
	        	StreamLogger outputLogger = new StreamLogger(mkFifoProcess.getInputStream(), "mkFifoOuptut");
	        	new Thread(errorLogger).start();
	        	new Thread(outputLogger).start();
	        	int result = mkFifoProcess.waitFor();
	        	if (result!=0) {
	        		throw new Exception("Return code "+result+" received from statement : "+mkFifoCmd);
	        	}
        	}
        	            
        	// 3) Execute the client tool (sqllineClient) from LucidDB
        	//    Again, we make sure to log all the possible output, also from STDERR
        	//
        	String cmd = createCommandLine(meta, true);
        	
        	logBasic("Executing command: "+cmd);
            data.mClientlProcess = rt.exec(cmd);
            data.errorLogger = new StreamLogger(data.mClientlProcess.getErrorStream(), "ERROR");
            data.outputLogger = new StreamLogger(data.mClientlProcess.getInputStream(), "OUTPUT");
            data.bulkOutputStream = data.mClientlProcess.getOutputStream();
            new Thread(data.errorLogger).start();
            new Thread(data.outputLogger).start();                              

            // 4) Now we are ready to create the LucidDB FIFO server that will handle the actual bulk loading.
            //
            String fifoServerStatement = "";
            fifoServerStatement += "create or replace server "+meta.getFifoServerName()+Const.CR;
            fifoServerStatement += "foreign data wrapper sys_file_wrapper"+Const.CR;
            fifoServerStatement += "options ("+Const.CR;
            fifoServerStatement += "directory '"+fifoDirectory+"',"+Const.CR;
            fifoServerStatement += "file_extension 'csv',"+Const.CR;
            fifoServerStatement += "with_header 'no',"+Const.CR;
            fifoServerStatement += "num_rows_scan '0',"+Const.CR;
            fifoServerStatement += "lenient 'no');"+Const.CR;
            
            logBasic("Creating LucidDB fifo_server with the following command: "+fifoServerStatement);
            data.bulkOutputStream.write(fifoServerStatement.getBytes());
            data.bulkOutputStream.flush();
            
            // 5) Now we also need to create a bulk loader file .bcp
            //
            createBulkLoadConfigFile(data.bcpFilename);
            
            // 6) execute the actual load command!
            //    This will actually block until the load is done in the separate execution thread.
            //
            executeLoadCommand(tableName);
            
        	// 7) We have to write rows to the FIFO file later on.
            //
        	data.fifoStream = new BufferedOutputStream( new FileOutputStream( new File(data.fifoFilename) ) );
        }
        catch ( Exception ex )
        {
        	throw new KettleException(ex);
        }
        
        return true;
	}

	private void executeLoadCommand(String tableName) throws KettleException {
		String loadCommand = "";
		loadCommand += "insert into "+data.schemaTable+Const.CR;
		loadCommand += "select * from "+meta.getFifoServerName()+".\"DEFAULT\"."+tableName+";"+Const.CR;

		try {
			data.bulkOutputStream.write(loadCommand.getBytes());
			data.bulkOutputStream.flush();
			logBasic("Executed load command : "+Const.CR+loadCommand);
		}
		catch(IOException e) {
			throw new KettleException("Unable to execute the bulk load command on LucidDB", e);
		}
	}

	private void createBulkLoadConfigFile(String bcpFilename) throws KettleException {
		File bcpFile = new File(bcpFilename);
		FileWriter writer = null;
		
		try {
			writer = new FileWriter(bcpFile);
			
			// The first Line is the version number, usually 9.0
			//
			writer.write("9.0"+Const.CR);
	
			// The second line contains the number of columns...
			//
			writer.write(meta.getFieldTable().length+Const.CR);
			
			// The next block lists the columns from 1..N where N is the number of columns...
			//
			for (int i=0;i<meta.getFieldTable().length;i++) {
				ValueMetaInterface field = getInputRowMeta().getValueMeta(data.keynrs[i]);
				
				// Col 1 : the column number (i+1)
				//
				writer.write(Integer.toString(i+1)+" ");
	
				// Col 2 : the data type
				//
				String dataType=null;
				switch(field.getType()) {
				case ValueMetaInterface.TYPE_STRING : dataType="SQLVARCHAR"; break;
				case ValueMetaInterface.TYPE_BIGNUMBER: dataType="SQLREAL"; break;
				case ValueMetaInterface.TYPE_NUMBER : dataType="SQLREAL"; break;
				case ValueMetaInterface.TYPE_INTEGER : dataType="SQLBIGINT"; break;
				case ValueMetaInterface.TYPE_DATE : dataType="SQLTIMESTAMP"; break;
				case ValueMetaInterface.TYPE_BOOLEAN : dataType="SQLCHAR"; break;
				default : dataType="SQLVARCHAR"; break;
				}
				writer.write(dataType+" ");
				
				// Col 3 : an ignored column (start position or something like that)
				//
				writer.write("0 ");
				
				// Col 4 : the data length, just put the length metadata in here
				//
				writer.write(""+field.getLength()+" ");
				
				// Col 5 : The separator is also ignored, we're going to put a tab in here, like in the sample
				//
				writer.write("\"\\t\" "); // "\t"
				
				// Col 6 : the column number again...
				//
				writer.write(Integer.toString(i+1)+" ");
	
				// Col 7 : The identifier
				//
				writer.write(meta.getFieldTable()[i]+" ");
				
				// Col 8 : Collation / Format : leave it empty/default at the time being
				//
				writer.write("\"\" "); // ""
				
				// Newline to finish
				//
				writer.write(Const.CR);
			}
		}
		catch(Exception e) {
			throw new KettleException("Unable to create BCP control file", e);
		}
		finally {
			// That's it, close shop
			//
			if (writer!=null) {
				try {
					writer.close();
				} catch (IOException e) {
					throw new KettleException("Unable to close BCP file '"+bcpFilename+"'", e);
				}
			}
		}
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(LucidDBBulkLoaderMeta)smi;
		data=(LucidDBBulkLoaderData)sdi;

		try
		{
			Object[] r=getRow();  // Get row from input rowset & set row busy!
			if (r==null)          // no more input to be expected...
			{
				setOutputDone();
				
				// Close the fifo file...
				//
				data.fifoStream.close();
				data.fifoStream=null;

				// Close the output stream...
				//
				data.bulkOutputStream.flush();
				data.bulkOutputStream.close();
				data.bulkOutputStream=null;
				
                // wait for the client process to finish and check for any error...
				//
            	int exitVal = data.mClientlProcess.waitFor();
				logBasic(Messages.getString("LucidDBBulkLoader.Log.ExitValuePsqlPath", "" + exitVal)); //$NON-NLS-1$
	            data.mClientlProcess=null;
	            
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

				// execute the client statement...
				//
				execute(meta, true);
			}
			
			writeRowToBulk(getInputRowMeta(), r);
			putRow(getInputRowMeta(), r);
			incrementLinesOutput();

			return true;
		}
		catch(Exception e)
		{
			logError(Messages.getString("LucidDBBulkLoader.Log.ErrorInStep"), e); //$NON-NLS-1$
			setErrors(1);
			stopAll();
			setOutputDone();  // signal end to receiver(s)
			return false;
		} 
	}

	private void writeRowToBulk(RowMetaInterface rowMeta, Object[] r) throws KettleException {

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
		    		data.fifoStream.write(data.separator);
				}
				
	    		int index = data.keynrs[i];
	    		ValueMetaInterface valueMeta = rowMeta.getValueMeta(index);
	    		Object valueData = r[index];
	    		
	    		if (valueData!=null) {
		    		switch(valueMeta.getType()) {
		    		case ValueMetaInterface.TYPE_STRING :
		    			data.fifoStream.write(data.quote);
		    			if (valueMeta.isStorageBinaryString() && meta.getFieldFormatOk()[i]) {
		    				// We had a string, just dump it back.
		    				data.fifoStream.write((byte[])valueData);
		    			} else {
		    				data.fifoStream.write(valueMeta.getString(valueData).getBytes());
		    			}
		    			data.fifoStream.write(data.quote);
		    			break;
		    		case ValueMetaInterface.TYPE_INTEGER:
		    			if (valueMeta.isStorageBinaryString() && meta.getFieldFormatOk()[i]) {
		    				data.fifoStream.write((byte[])valueData);
		    			} else {
		    				data.fifoStream.write(Long.toString(valueMeta.getInteger(valueData)).getBytes());
		    			}
		    			break;
		    		case ValueMetaInterface.TYPE_DATE:
		    			// Keep the data format as indicated.
		    			//
		    			if (valueMeta.isStorageBinaryString() && meta.getFieldFormatOk()[i]) {
		    				data.fifoStream.write((byte[])valueData);
		    			} else {
		    				Date date = valueMeta.getDate(valueData);
		    				// Convert it to the LucidDB date format "yyyy/MM/dd HH:mm:ss"
		    				//
		    				data.fifoStream.write(data.bulkDateMeta.getString(date).getBytes());
		    			}
		    			break;
		    		case ValueMetaInterface.TYPE_BOOLEAN:
		    			if (valueMeta.isStorageBinaryString() && meta.getFieldFormatOk()[i]) {
		    				data.fifoStream.write((byte[])valueData);
		    			} else {
		    				data.fifoStream.write(Boolean.toString(valueMeta.getBoolean(valueData)).getBytes());
		    			}
		    			break;
		    		case ValueMetaInterface.TYPE_NUMBER:
		    			if (valueMeta.isStorageBinaryString() && meta.getFieldFormatOk()[i]) {
		    				data.fifoStream.write((byte[])valueData);
		    			} else {
		    				data.fifoStream.write(Double.toString(valueMeta.getNumber(valueData)).getBytes());
		    			}
		    			break;
		    		case ValueMetaInterface.TYPE_BIGNUMBER:
		    			if (valueMeta.isStorageBinaryString() && meta.getFieldFormatOk()[i]) {
		    				data.fifoStream.write((byte[])valueData);
		    			} else {
		    				data.fifoStream.write(valueMeta.getString(valueData).getBytes());
		    			}
		    			break;
		    		}
	    		}
	    	}
			
			// finally write a newline
			//
    		data.fifoStream.write(data.newline);
    	}
    	catch(Exception e)
    	{
    		throw new KettleException("Error serializing rows of data to the fifo file", e);
    	}
		
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(LucidDBBulkLoaderMeta)smi;
		data=(LucidDBBulkLoaderData)sdi;

		if (super.init(smi, sdi))
		{			
			data.quote = "\"".getBytes();
			data.separator = ",".getBytes();
			data.newline = Const.CR.getBytes();

			data.bulkDateMeta = new ValueMeta("dateMeta", ValueMetaInterface.TYPE_DATE);
			data.bulkDateMeta.setConversionMask("yyyy-MM-dd HH:mm:ss");
			data.bulkDateMeta.setStringEncoding(meta.getEncoding());

			data.bulkNumberMeta = new ValueMeta("numberMeta", ValueMetaInterface.TYPE_NUMBER);
			data.bulkNumberMeta.setConversionMask("#.#");
			data.bulkNumberMeta.setGroupingSymbol(",");
			data.bulkNumberMeta.setDecimalSymbol(".");
			data.bulkNumberMeta.setStringEncoding(meta.getEncoding());

			data.bufferSize = Const.toInt(environmentSubstitute(meta.getBufferSize()), 100000);
			
			// Allocate the buffer
			// 
			data.rowBuffer = new byte[data.bufferSize][];
			data.bufferIndex = 0;
			
			// Schema-table combination...
			data.schemaTable = meta.getDatabaseMeta().getSchemaTableCombination(environmentSubstitute(meta.getSchemaName()), environmentSubstitute(meta.getTableName()));
			
			return true;
		}
		return false;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (LucidDBBulkLoaderMeta)smi;
	    data = (LucidDBBulkLoaderData)sdi;

	    // Close the output streams if still needed.
	    //
	    try {
	    	if (data.fifoStream!=null) data.fifoStream.close();
	    	if (data.bulkOutputStream!=null) data.bulkOutputStream.close();
	    	if (data.mClientlProcess!=null) {
	    		int exitValue = data.mClientlProcess.waitFor();
	    		logDetailed("Exit value for the mclient process was : "+exitValue);
	    	}
	    }
	    catch(Exception e) {
	    	setErrors(1L);
	    	logError("Unexpected error encountered while finishing the client process", e);
	    }
	    
	    super.dispose(smi, sdi);
	}
	
	//
	// Run is were the action happens!
	//
	public void run()
	{
		BaseStep.runStepThread(this, meta, data);
	}
}