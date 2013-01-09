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

package org.pentaho.di.trans.steps.luciddbbulkloader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.DBCache;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
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
	private static Class<?> PKG = LucidDBBulkLoaderMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private LucidDBBulkLoaderMeta meta;
	private LucidDBBulkLoaderData data;
    // private SqlRunner sqlRunner;
    
	public LucidDBBulkLoader(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
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
        	FileObject directory = KettleVFS.getFileObject(fifoVfsDirectory, getTransMeta());
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
	        	StreamLogger errorLogger = new StreamLogger(log, mkFifoProcess.getErrorStream(), "mkFifoError");
	        	StreamLogger outputLogger = new StreamLogger(log, mkFifoProcess.getInputStream(), "mkFifoOuptut");
	        	new Thread(errorLogger).start();
	        	new Thread(outputLogger).start();
	        	int result = mkFifoProcess.waitFor();
	        	if (result!=0) {
	        		throw new Exception("Return code "+result+" received from statement : "+mkFifoCmd);
	        	}
        	}

        	// 3) Make a connection to LucidDB for sending SQL commands
            // (Also, we need a clear cache for getting up-to-date target metadata)
            DBCache.getInstance().clear(meta.getDatabaseMeta().getName());
            if(meta.getDatabaseMeta()==null) {
        		logError(BaseMessages.getString(PKG, "LuciDBBulkLoader.Init.ConnectionMissing", getStepname()));
        		return false;
        	}
			data.db = new Database(this, meta.getDatabaseMeta());
			data.db.shareVariablesWith(this);
			// Connect to the database
            if (getTransMeta().isUsingUniqueConnections())
            {
                synchronized (getTrans())
                {
                    data.db.connect(getTrans().getTransactionId(), getPartitionID());
                }
            } else
            {
                data.db.connect(getPartitionID());
            }

            logBasic("Connected to LucidDB");

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
            data.db.execStatements(fifoServerStatement);
            
            // 5) Set the error limit in the LucidDB session 
            // REVIEW jvs 13-Dec-2008:  is this guaranteed to retain the same
            // connection?
            String errorMaxStatement = "";
            errorMaxStatement += "alter session set \"errorMax\" = " + meta.getMaxErrors() + ";" + Const.CR;
            logBasic("Setting error limit in LucidDB session with the following command: " + errorMaxStatement);
            data.db.execStatements(errorMaxStatement);
            
            // 6) Now we also need to create a bulk loader file .bcp
            //
            createBulkLoadConfigFile(data.bcpFilename);
            
            // 7) execute the actual load command!
            //    This will actually block until the load is done in the
            // separate execution thread; see notes in executeLoadCommand
            // on why it's important for this to occur BEFORE
            // opening our end of the FIFO.
            //
            executeLoadCommand(tableName);
            
        	// 8) We have to write rows to the FIFO file later on.
        	data.fifoStream = new BufferedOutputStream( new FileOutputStream( fifoFile ));
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
		loadCommand += "select * from "+meta.getFifoServerName()+".\"DEFAULT\"."+tableName+Const.CR;

        // NOTE jvs 13-Dec-2008: We prepare the SQL before spawning the thread
        // to execute it.  The reason is that if a SQL validation exception
        // occurs during preparation (e.g. due to datatype mismatch), we don't
        // even want to open our end of the FIFO, otherwise we can get stuck
        // since the server is never going to open its end until execution,
        // which ain't gonna happen in that case.

        logBasic("Preparing load command : "+Const.CR+loadCommand);
        PreparedStatement ps = data.db.prepareSQL(loadCommand);
        
        data.sqlRunner = new SqlRunner(data, ps);
        data.sqlRunner.start();
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

            RowMetaInterface targetFieldMeta = meta.getRequiredFields(this);
			
            data.bulkFormatMeta = new ValueMetaInterface[meta.getFieldTable().length];
            
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
				case ValueMetaInterface.TYPE_NUMBER : dataType="SQLFLT8"; break;
				case ValueMetaInterface.TYPE_INTEGER : dataType="SQLBIGINT"; break;
				case ValueMetaInterface.TYPE_DATE :
                    // Use the actual datatypes in the target table to
                    // determine how to create the control file column
                    // definition for date/time fields.
                    if (targetFieldMeta.getValueMetaList().get(i).getOriginalColumnType() == Types.DATE) {
                        data.bulkFormatMeta[i] = data.bulkDateMeta;
                        dataType="SQLDATE";
                    } else {
                        data.bulkFormatMeta[i] = data.bulkTimestampMeta;
                        dataType="SQLTIMESTAMP";
                    }
                    break;
                    // REVIEW jvs 13-Dec-2008:  enable boolean support?
				case ValueMetaInterface.TYPE_BOOLEAN : dataType="SQLCHAR"; break;
				default : dataType="SQLVARCHAR"; break;
				}
				writer.write(dataType+" ");
				
				// Col 3 : an ignored column (start position or something like that)
				//
				writer.write("0 ");
				
				// Col 4 : the data length, just put the length metadata in here
				//
                if (field.getLength() == -1) {
                    writer.write("1000 ");
                } else {
                    writer.write(""+field.getLength()+" ");
                }
				
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

                // wait for the INSERT statement to finish and check for any
                // error and/or warning...
                data.sqlRunner.join();
                SqlRunner sqlRunner = data.sqlRunner;
                data.sqlRunner = null;
                for (String warning : sqlRunner.warnings) {
                    // REVIEW jvs 13-Dec-2008:  It would be nice if there were
                    // a logWarning instead?
                    logError(" (WARNING) " + warning);
                }
                sqlRunner.checkExcn();

                // If there was no fatal exception, but there were warnings,
                // retrieve the rejected row count
                if (!sqlRunner.warnings.isEmpty()) {
                    ResultSet rs = data.db.openQuery("SELECT PARAM_VALUE FROM SYS_ROOT.USER_SESSION_PARAMETERS WHERE PARAM_NAME='lastRowsRejected'");
                    try {
                        rs.next();
                        setLinesRejected(rs.getInt(1));
                    } finally {
                        rs.close();
                    }
                }
	            
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
			logError(BaseMessages.getString(PKG, "LucidDBBulkLoader.Log.ErrorInStep"), e); //$NON-NLS-1$
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
		    			// REVIEW jvs 13-Dec-2008:  Is it OK to ignore
		    			// FieldFormatOk like this?
		    			/*if (false && valueMeta.isStorageBinaryString() && meta.getFieldFormatOk()[i]) {
		    				data.fifoStream.write((byte[])valueData);
		    			} else {
		    			*/
		    				Date date = valueMeta.getDate(valueData);
		    				// Convert it to the ISO timestamp format
		    				// "yyyy-MM-dd HH:mm:ss" // or date format
		    				// "yyyy-MM-dd" as appropriate, since LucidDB
		    				// follows SQL:2003 here
		    				data.fifoStream.write(data.bulkFormatMeta[i].getString(date).getBytes());
		    			// }
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

			data.bulkTimestampMeta = new ValueMeta("timestampMeta", ValueMetaInterface.TYPE_DATE);
			data.bulkTimestampMeta.setConversionMask("yyyy-MM-dd HH:mm:ss");
			data.bulkTimestampMeta.setStringEncoding(meta.getEncoding());

			data.bulkDateMeta = new ValueMeta("dateMeta", ValueMetaInterface.TYPE_DATE);
			data.bulkDateMeta.setConversionMask("yyyy-MM-dd");
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
			data.schemaTable = meta.getDatabaseMeta().getQuotedSchemaTableCombination(environmentSubstitute(meta.getSchemaName()), environmentSubstitute(meta.getTableName()));
			
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

            // Stop the SQL execution thread
            if (data.sqlRunner!= null) {
                data.sqlRunner.join();
                data.sqlRunner = null;
            }
            // And finally, release the database connection
            if (data.db!=null) {
                data.db.disconnect();
                data.db = null;
            }
	    }
	    catch(Exception e) {
	    	setErrors(1L);
	    	logError("Unexpected error encountered while closing the client connection", e);
	    }
	    
	    super.dispose(smi, sdi);
	}
	
    static class SqlRunner extends Thread
    {
        private LucidDBBulkLoaderData data;
        
        private PreparedStatement ps;

        private SQLException ex;

        List<String> warnings;
        
        SqlRunner(LucidDBBulkLoaderData data, PreparedStatement ps)
        {
            this.data = data;
            this.ps = ps;
            warnings = new ArrayList<String>();
        }
        
        public void run()
        {
            try {
                // TODO jvs 12-Dec-2008:  cross-check result against actual number
                // of rows sent.
                ps.executeUpdate();

                // Pump out any warnings and save them.
                SQLWarning warning = ps.getWarnings();
                while (warning != null) {
                    warnings.add(warning.getMessage());
                    warning = warning.getNextWarning();
                }
            } catch (SQLException ex) {
                this.ex = ex;
            } finally {
                try {
                    data.db.closePreparedStatement(ps);
                } catch (KettleException ke) {
                    // not much we can do with this
                } finally {
                    ps = null;
                }
            }
        }

        void checkExcn() throws SQLException
        {
            // This is called from the main thread context to rethrow any saved
            // excn.
            if (ex != null) {
                throw ex;
            }
        }
    }
}
