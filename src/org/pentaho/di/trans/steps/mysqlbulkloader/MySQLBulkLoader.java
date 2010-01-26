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

package org.pentaho.di.trans.steps.mysqlbulkloader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.Date;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.DBCache;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.StreamLogger;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * Performs a streaming bulk load to a MySQL table.
 *
 * Based on Sven Boden's Oracle Bulk Loader step
 * 
 * @author matt
 * @since  14-apr-2009
 */
public class MySQLBulkLoader extends BaseStep implements StepInterface
{
	private static Class<?> PKG = MySQLBulkLoaderMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private MySQLBulkLoaderMeta meta;
	private MySQLBulkLoaderData data;
    
	public MySQLBulkLoader(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean execute(MySQLBulkLoaderMeta meta) throws KettleException
	{
        Runtime rt = Runtime.getRuntime();

        try  
        {
        	// 1) Create the FIFO file using the "mkfifo" command...
        	//    Make sure to log all the possible output, also from STDERR
        	//
        	data.fifoFilename = environmentSubstitute(meta.getFifoFileName());
        	
        	File fifoFile = new File(data.fifoFilename);
        	if (!fifoFile.exists()) {
        		// MKFIFO!
        		//
	        	String mkFifoCmd = "mkfifo "+data.fifoFilename;
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

	        	String chmodCmd = "chmod 666 "+data.fifoFilename;
	        	logBasic("Setting FIFO file permissings using this command : "+chmodCmd);
	        	Process chmodProcess = rt.exec(chmodCmd);
	        	errorLogger = new StreamLogger(log, chmodProcess.getErrorStream(), "chmodError");
	        	outputLogger = new StreamLogger(log, chmodProcess.getInputStream(), "chmodOuptut");
	        	new Thread(errorLogger).start();
	        	new Thread(outputLogger).start();
	        	result = chmodProcess.waitFor();
	        	if (result!=0) {
	        		throw new Exception("Return code "+result+" received from statement : "+chmodCmd);
	        	}
        	}

        	// 2) Make a connection to MySQL for sending SQL commands
            // (Also, we need a clear cache for getting up-to-date target metadata)
            DBCache.getInstance().clear(meta.getDatabaseMeta().getName());

			data.db = new Database(this, meta.getDatabaseMeta());
			data.db.shareVariablesWith(this);
			// Connect to the database
            if (getTransMeta().isUsingUniqueConnections())
            {
                synchronized (getTrans())
                {
                    data.db.connect(getTrans().getThreadName(), getPartitionID());
                }
            } else
            {
                data.db.connect(getPartitionID());
            }

            logBasic("Connected to MySQL");

            // 3) Now we are ready to run the load command...
            //
            executeLoadCommand();            
        }
        catch ( Exception ex )
        {
        	throw new KettleException(ex);
        }
        
        return true;
	}

	private void executeLoadCommand() throws Exception {
		
        String loadCommand = "";
        loadCommand += "LOAD DATA INFILE '"+environmentSubstitute(meta.getFifoFileName())+"' ";
        loadCommand += "INTO TABLE "+data.schemaTable+" ";
        if (meta.isReplacingData()) {
        	loadCommand += "REPLACE ";
        } else if (meta.isIgnoringErrors()) {
        	loadCommand += "IGNORE ";
        }
        if (!Const.isEmpty(meta.getEncoding())) {
        	loadCommand += "CHARACTER SET "+meta.getEncoding()+" ";
        }
        String delStr = meta.getDelimiter();
        if ("\t".equals(delStr)) delStr="\\t";
        
        loadCommand += "FIELDS TERMINATED BY '"+delStr+"' ";
        if (!Const.isEmpty(meta.getEnclosure())) {
        	loadCommand += "OPTIONALLY ENCLOSED BY '"+meta.getEnclosure()+"' ";
        }
        loadCommand += "ESCAPED BY '"+meta.getEscapeChar()+("\\".equals(meta.getEscapeChar())?meta.getEscapeChar():"")+"' ";
        loadCommand += ";"+Const.CR;
        
        logBasic("Starting the MySQL bulk Load in a separate thread : "+loadCommand);
        data.sqlRunner = new SqlRunner(data, loadCommand);
        data.sqlRunner.start();
        
        // Ready to start writing rows to the FIFO file now...
        //
    	data.fifoStream = new BufferedOutputStream( new FileOutputStream( data.fifoFilename ), 1000 );
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(MySQLBulkLoaderMeta)smi;
		data=(MySQLBulkLoaderData)sdi;

		try
		{
			Object[] r=getRow();  // Get row from input rowset & set row busy!
			if (r==null)          // no more input to be expected...
			{
				setOutputDone();
				
				closeOutput();
	            
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
				
				data.bulkFormatMeta = new ValueMetaInterface[data.keynrs.length];
				for (int i=0;i<data.keynrs.length;i++) {
					ValueMetaInterface sourceMeta = getInputRowMeta().getValueMeta(data.keynrs[i]);
					if (sourceMeta.isDate()) {
						if (meta.getFieldFormatType()[i]==MySQLBulkLoaderMeta.FIELD_FORMAT_TYPE_DATE) {
							data.bulkFormatMeta[i] = data.bulkDateMeta.clone();
						} else if (meta.getFieldFormatType()[i]==MySQLBulkLoaderMeta.FIELD_FORMAT_TYPE_TIMESTAMP) {
							data.bulkFormatMeta[i] = data.bulkTimestampMeta.clone(); // default to timestamp
						}
					} else if (sourceMeta.isNumeric() && meta.getFieldFormatType()[i]==MySQLBulkLoaderMeta.FIELD_FORMAT_TYPE_NUMBER) {
						data.bulkFormatMeta[i] = data.bulkNumberMeta.clone();
					} 
					
					if (data.bulkFormatMeta[i]==null && !sourceMeta.isStorageBinaryString()) {
						data.bulkFormatMeta[i] = sourceMeta.clone();
					}
				}

				// execute the client statement...
				//
				execute(meta);
			}

			// Every nr of rows we re-start the bulk load process to allow indexes etc to fit into the MySQL server memory
			// Performance could degrade if we don't do this.
			//
			if (data.bulkSize>0 && getLinesOutput()>0 && (getLinesOutput()%data.bulkSize)==0) {
				closeOutput();
				executeLoadCommand();
			}

			writeRowToBulk(getInputRowMeta(), r);
			putRow(getInputRowMeta(), r);
			incrementLinesOutput();

			return true;
		}
		catch(Exception e)
		{
			logError(BaseMessages.getString(PKG, "MySQLBulkLoader.Log.ErrorInStep"), e); //$NON-NLS-1$
			setErrors(1);
			stopAll();
			setOutputDone();  // signal end to receiver(s)
			return false;
		} 
	}

	private void closeOutput() throws Exception 
	{
		// Close the fifo file...
		//
		data.fifoStream.close();
		data.fifoStream=null;

        // wait for the INSERT statement to finish and check for any
        // error and/or warning...
        data.sqlRunner.join();
        SqlRunner sqlRunner = data.sqlRunner;
        data.sqlRunner = null;
        sqlRunner.checkExcn();
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
		    			if (valueMeta.isStorageBinaryString() && meta.getFieldFormatType()[i]==MySQLBulkLoaderMeta.FIELD_FORMAT_TYPE_OK) {
		    				// We had a string, just dump it back.
		    				data.fifoStream.write((byte[])valueData);
		    			} else {
		    				String string = valueMeta.getString(valueData);
		    				if (string!=null) {
			    				if (meta.getFieldFormatType()[i]==MySQLBulkLoaderMeta.FIELD_FORMAT_TYPE_STRING_ESCAPE) {
			    					string = Const.replace(string, meta.getEnclosure(), meta.getEscapeChar()+meta.getEnclosure());
			    				}
			    				data.fifoStream.write(string.getBytes());
		    				}
		    			}
		    			data.fifoStream.write(data.quote);
		    			break;
		    		case ValueMetaInterface.TYPE_INTEGER:
	    				if (valueMeta.isStorageBinaryString() && data.bulkFormatMeta[i]==null) {
	    					data.fifoStream.write( valueMeta.getBinaryString(valueData) );
	    				} else {
		    				Long integer = valueMeta.getInteger(valueData);
		    				if (integer!=null) {
		    					data.fifoStream.write(data.bulkFormatMeta[i].getString(integer).getBytes());
		    				}
	    				}
		    			break;
		    		case ValueMetaInterface.TYPE_DATE:
	    				if (valueMeta.isStorageBinaryString() && data.bulkFormatMeta[i]==null) {
	    					data.fifoStream.write( valueMeta.getBinaryString(valueData) );
	    				} else {
		    				Date date = valueMeta.getDate(valueData);
		    				if (date!=null) {
		    					data.fifoStream.write(data.bulkFormatMeta[i].getString(date).getBytes());
		    				}
	    				}
		    			break;
		    		case ValueMetaInterface.TYPE_BOOLEAN:
	    				if (valueMeta.isStorageBinaryString() && data.bulkFormatMeta[i]==null) {
	    					data.fifoStream.write( valueMeta.getBinaryString(valueData) );
	    				} else {
		    				Boolean b= valueMeta.getBoolean(valueData);
		    				if (b!=null) {
		    					data.fifoStream.write(data.bulkFormatMeta[i].getString(b).getBytes());
		    				}
	    				}
		    			break;
		    		case ValueMetaInterface.TYPE_NUMBER:
		    			if (valueMeta.isStorageBinaryString() && data.bulkFormatMeta[i]==null) {
		    				data.fifoStream.write((byte[])valueData);
		    			} else {
		    				Double d = valueMeta.getNumber(valueData);
		    				if (d!=null) {
		    					data.fifoStream.write(data.bulkFormatMeta[i].getString(d).getBytes());
		    				}
		    			}
		    			break;
		    		case ValueMetaInterface.TYPE_BIGNUMBER:
		    			if (valueMeta.isStorageBinaryString() && data.bulkFormatMeta[i]==null) {
		    				data.fifoStream.write((byte[])valueData);
		    			} else {
		    				BigDecimal bn = valueMeta.getBigNumber(valueData);
		    				if (bn!=null) {
		    					data.fifoStream.write(data.bulkFormatMeta[i].getString(bn).getBytes());
		    				}
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
		meta=(MySQLBulkLoaderMeta)smi;
		data=(MySQLBulkLoaderData)sdi;

		if (super.init(smi, sdi))
		{			
			if (Const.isEmpty(meta.getEnclosure())) {
				data.quote = new byte[] { };
			} else {
				data.quote = meta.getEnclosure().getBytes();
			}
			if (Const.isEmpty(meta.getDelimiter())) {
				data.separator = "\t".getBytes();
			} else {
				data.separator = meta.getDelimiter().getBytes();
			}
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
						
			data.bulkSize = Const.toLong(environmentSubstitute(meta.getBulkSize()), -1L);
			
			// Schema-table combination...
			data.schemaTable = meta.getDatabaseMeta().getSchemaTableCombination(environmentSubstitute(meta.getSchemaName()), environmentSubstitute(meta.getTableName()));
			
			return true;
		}
		return false;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (MySQLBulkLoaderMeta)smi;
	    data = (MySQLBulkLoaderData)sdi;

	    // Close the output streams if still needed.
	    //
	    try {
	    	if (data.fifoStream!=null) {
	    		data.fifoStream.close();
	    	}

            // Stop the SQL execution thread
	    	//
            if (data.sqlRunner!= null) {
                data.sqlRunner.join();
                data.sqlRunner = null;
            }
            // Release the database connection
            //
            if (data.db!=null) {
                data.db.disconnect();
                data.db = null;
            }
            
            // remove the fifo file...
            //
            try {
            	if (data.fifoFilename!=null) {
            		new File(data.fifoFilename).delete();
            	}
            } catch(Exception e) {
            	logError("Unable to delete FIFO file : "+data.fifoFilename, e);
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
        private MySQLBulkLoaderData data;
        
        private String loadCommand;

        private Exception ex;
        
        SqlRunner(MySQLBulkLoaderData data, String loadCommand)
        {
            this.data = data;
            this.loadCommand = loadCommand;
        }
        
        public void run()
        {
            try {
                data.db.execStatement(loadCommand);
            } catch (Exception ex) {
                this.ex = ex;
            }
        }

        void checkExcn() throws Exception
        {
            // This is called from the main thread context to rethrow any saved
            // excn.
            if (ex != null) {
                throw ex;
            }
        }
    }
}
