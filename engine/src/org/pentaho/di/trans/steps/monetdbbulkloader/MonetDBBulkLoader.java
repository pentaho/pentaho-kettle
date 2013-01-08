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
import java.util.Date;
import java.util.List;

import nl.cwi.monetdb.mcl.io.BufferedMCLReader;
import nl.cwi.monetdb.mcl.io.BufferedMCLWriter;
import nl.cwi.monetdb.mcl.net.MapiSocket;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.MonetDBDatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
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

	public boolean execute(MonetDBBulkLoaderMeta meta, boolean wait) throws KettleException
	{
		if (log.isDetailed()) logDetailed("Started execute" );

        try
        {

       		if (log.isDetailed()) logDetailed("Auto String Length flag: "+meta.isAutoStringWidths() );
        	
          MapiSocket mserver = getMonetDBConnection();
          data.mserver = mserver;

          data.in = mserver.getReader();
          data.out = mserver.getWriter();

          String error = data.in.waitForPrompt();
          if(error != null) {
            throw new KettleException("Error while connecting to MonetDB for bulk loading : " + error);
			    }
        	
          data.outputLogger = new StreamLogger(log, mserver.getInputStream(), "OUTPUT");

        }
        catch ( Exception ex )
        {
        	throw new KettleException(ex);
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
        try {
	    	  writeBufferToMonetDB();
          data.out.flush();
        } catch (KettleException ke) {
          throw ke;
        } finally {
          data.mserver.close();
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

			// Now that we have the line, grab the content and store it in the buffer...
			//
			data.rowBuffer[data.bufferIndex] = line.toString(); //line.toByteArray();
			data.bufferIndex++;
    	}
    	catch(Exception e)
    	{
    		throw new KettleException("Error serializing rows of data to the psql command", e);
    	}
		
	}
	
	public void truncate() throws KettleException {
    String cmd;
    String table = data.schemaTable;
    cmd = meta.getDatabaseMeta().getTruncateTableStatement(null, table)+";";

    try {
      executeSql(cmd);
    } catch (Exception e) {
      throw new KettleException("Error while truncating table " + table, e);
		}

    // try to update the metadata registry
    util.updateMetadata( meta, -1 );
    if (log.isDetailed()) logDetailed("Successfull: "+cmd);

  }

  public void drop() throws KettleException {
    try {
      executeSql("drop table " + data.schemaTable);
    } catch (Exception e) {
      throw new KettleException("Error while dropping table " + data.schemaTable, e);
    }

  }

	public void autoAdjustSchema( MonetDBBulkLoaderMeta meta )  throws KettleException {
		
 		   if (log.isDetailed()) logDetailed("Attempting to auto adjust table structure" );

    drop();

		   if (log.isDetailed()) logDetailed("getTransMeta: "+getTransMeta() );
   		   if (log.isDetailed()) logDetailed("getStepname: "+getStepname() );
   		   SQLStatement statement = meta.getTableDdl(getTransMeta(), getStepname(), true, data, true);
   		   if (log.isDetailed()) logDetailed("Statement: "+statement );
  		   if (log.isDetailed() && statement != null) logDetailed("Statement has SQL: "+statement.hasSQL() );
    		
  		   if(statement != null && statement.hasSQL()) {
    			String cmd = statement.getSQL();
     		  try {
            executeSql(cmd);
          } catch (Exception e) {
            throw new KettleException("Error while creating table " + data.schemaTable, e);
     	    }
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
    		.append("','" + Const.CR + "','")
    		.append(new String(data.quote))
    		.append("';");
    		String cmd = cmdBuff.toString();
	    	if (log.isDetailed()) logDetailed(cmd);

        data.out.write('s');
        data.out.write(cmdBuff.toString());
        data.out.newLine();

	    	for (int i=0;i<data.bufferIndex;i++) {
          String buffer = data.rowBuffer[i];
          data.out.write(buffer);
		    	if (log.isRowLevel()) logRowlevel(buffer);
	    	}
	    	
        // wait for the prompt
        String error = data.in.waitForPrompt();
        if(error != null) {
          throw new KettleException("Error loading data: " + error);
        }
        // write an empty line, forces the flush of the stream
        data.out.writeLine("");

        // again...
        error = data.in.waitForPrompt();
        if(error != null) {
          throw new KettleException("Error loading data: " + error);
        }
        data.out.writeLine("");

        // and again, making sure we commit all the records
        error = data.in.waitForPrompt();
        if(error != null) {
          throw new KettleException("Error loading data: " + error);
        }

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
			data.rowBuffer = new String[data.bufferSize]; //new byte[data.bufferSize][];
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

	    super.dispose(smi, sdi);
	}

	protected MonetDBBulkLoaderData getData() {
		return this.data;
	}

  protected MapiSocket getMonetDBConnection() throws Exception {
    if(this.meta == null) {
      throw new KettleException("No metadata available to determine connection information from.");
    }
    DatabaseMeta dm = meta.getDatabaseMeta();
    String hostname = environmentSubstitute(Const.NVL(dm.getHostname(), ""));
    String portnum  = environmentSubstitute(Const.NVL(dm.getDatabasePortNumberString(), ""));
    String user = environmentSubstitute(Const.NVL(dm.getUsername(), ""));
    String password = environmentSubstitute(Const.NVL(dm.getPassword(), ""));
    String db = environmentSubstitute(Const.NVL(dm.getDatabaseName(), ""));

    MapiSocket mserver = getMonetDBConnection(hostname, Integer.valueOf(portnum), user, password, db, log);
    return mserver;

  }
  protected static MapiSocket getMonetDBConnection(String host, int port, String user, String password, String db) throws Exception {
    return getMonetDBConnection(host, port, user, password, db, null);
  }

  protected static MapiSocket getMonetDBConnection(String host, int port, String user, String password, String db, LogChannelInterface log) throws Exception {
    MapiSocket mserver = new MapiSocket();
    mserver.setDatabase(db);
    mserver.setLanguage("sql");

//    mserver.debug("mserver-instaview.debug.log");

    try {

      List<?> warnings = mserver.connect(host, port, user, password);
      if(warnings != null) {
        for (Object warning : warnings) {
          if(log != null) {
            log.logBasic("MonetDB connection warning: " + warning);
          }
        }
      } else {
        if (log != null) {
          log.logDebug("Successful MapiSocket connection to MonetDB established.");
        }
      }
      return mserver;

    } catch (Exception e) {
      throw e;
    }
  }

  protected void executeSql(String query) throws Exception {
    if(this.meta == null) {
      throw new KettleException("No metadata available to determine connection information from.");
    }
    DatabaseMeta dm = meta.getDatabaseMeta();
    String hostname = environmentSubstitute(Const.NVL(dm.getHostname(), ""));
    String portnum  = environmentSubstitute(Const.NVL(dm.getDatabasePortNumberString(), ""));
    String user = environmentSubstitute(Const.NVL(dm.getUsername(), ""));
    String password = environmentSubstitute(Const.NVL(dm.getPassword(), ""));
    String db = environmentSubstitute(Const.NVL(dm.getDatabaseName(), ""));

    executeSql(query, hostname, Integer.valueOf(portnum), user, password, db);

  }

  protected static void executeSql(String query, String host, int port, String user, String password, String db) throws Exception {
    MapiSocket mserver = null;
    try {
      mserver = getMonetDBConnection(host, port, user, password, db);

      BufferedMCLReader in = mserver.getReader();
      BufferedMCLWriter out = mserver.getWriter();

      String error = in.waitForPrompt();
      if(error != null) {
        throw new Exception("ERROR waiting for input reader: " + error);
      }

      // the leading 's' is essential, since it is a protocol
      // marker that should not be omitted, likewise the
      // trailing semicolon
      out.write('s');
      System.out.println(query);
      out.write(query);
      out.write(';');
      out.newLine();

      out.writeLine("");

      String line = null;
      while( (line = in.readLine()) != null  ) {
        int type = in.getLineType();

        // read till we get back to the prompt
        if (type == BufferedMCLReader.PROMPT) {
          break;
        }

        switch(type) {
          case BufferedMCLReader.ERROR:
            System.err.println(line);
            break;
          case BufferedMCLReader.RESULT:
            System.out.println(line);
            break;
          default:
            // unknown, header, ...
            break;
        }
      }

    } catch (Exception e) {
      throw e;
    } finally {
      mserver.close();
    }

  }

}

