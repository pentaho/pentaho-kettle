/**
 * The Pentaho proprietary code is licensed under the terms and conditions
 * of the software license agreement entered into between the entity licensing
 * such code and Pentaho Corporation. 
 */
package org.pentaho.di.trans.steps.monetdbagilemart;

import org.pentaho.di.core.TableManager;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.monetdbbulkloader.MonetDBBulkLoader;
import org.pentaho.di.trans.steps.monetdbbulkloader.MonetDBBulkLoaderData;
import org.pentaho.di.trans.steps.monetdbbulkloader.MonetDBBulkLoaderMeta;
import org.pentaho.di.trans.steps.tableagilemart.AgileMartUtil;

public class MonetDBAgileMart extends MonetDBBulkLoader implements TableManager {

	private static Class<?> PKG = MonetDBAgileMartMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	private long rowLimit = -1;
	private long bufferLimit = -1;
	
	public MonetDBAgileMart(StepMeta stepMeta,
			StepDataInterface stepDataInterface, int copyNr,
			TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		
		// TODO - pull "AgileBI" from configuration
		String connection = MonetDBAgileMartMeta.getStringProperty("AgileBIDatabase", "AgileBI");
		((MonetDBAgileMartMeta) stepMeta.getStepMetaInterface()).setDatabaseMeta( transMeta.findDatabase(connection) );
		
	}

	@Override
	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		
		MonetDBBulkLoaderMeta stepMeta=(MonetDBBulkLoaderMeta)smi;
		stepMeta.setAutoSchema(true);
		stepMeta.setAutoStringWidths(true);
		stepMeta.setTruncate(true);
		return super.init(smi, sdi);
	}	
	
	@Override
	public boolean execute(MonetDBBulkLoaderMeta meta, boolean wait) throws KettleException {

		rowLimit = ((MonetDBAgileMartMeta) meta).getRowLimit();
		rowsWritten = 0;
		MonetDBBulkLoaderData data = getData();
		bufferLimit = data.bufferSize;
		if( rowLimit < bufferLimit ) {
			// shrink the buffer limit down to the row limit
			bufferLimit = rowLimit;
		}
   		if (log.isDetailed()) logDetailed("MonetDBAgileMart Truncate flag: "+meta.isTruncate() );
   		if (log.isDetailed()) logDetailed("MonetDBAgileMart Auto Adjust Schema flag: "+meta.isAutoSchema() );
   		if (log.isDetailed()) logDetailed("MonetDBAgileMart Auto String Length flag: "+meta.isAutoStringWidths() );
		
		if (log.isDetailed()) logDetailed("Creating commands" );
		if(  meta.isAutoSchema() || meta.isTruncate() ) {
	        try 
	        {
        		Runtime rt = Runtime.getRuntime();
            	String cmd = createCommandLine(meta, false);
	            if( meta.isAutoSchema() ) {
	            	autoAdjustSchema( meta, rt, cmd );
	            } else {
	                if( meta.isTruncate() ) {
	                	truncateTable( rt, cmd );
	                }
	            }
	        }
	        catch ( Exception ex )
	        {
	           	throw new KettleException("Error while generating MonetDB commands", ex);
	        }
		}
		if( !meta.isAutoSchema() ) {
			meta.updateFields(getTransMeta(), getStepname(), data);
		}
   		
		return super.execute(meta, wait);
	}

	@Override
    protected void writeRowToMonetDB(RowMetaInterface rowMeta, Object[] r) throws KettleException {
		if( rowsWritten >= rowLimit ) {
			// we are done, ignore any new rows
			AgileMartUtil util = new AgileMartUtil();
			util.updateMetadata( getMeta(), rowsWritten );
			throw new MonetDBRowLimitException("Row limit exceeded");
		}
		MonetDBBulkLoaderData data = getData();
		if (bufferLimit==data.bufferIndex || log.isDebug() ) {
    		writeBufferToMonetDB();
    		if( (rowLimit - rowsWritten) <  bufferLimit ) {
    			// shrink the buffer limit down for the last one
    			bufferLimit = rowLimit - rowsWritten;
    		}
    		if( rowsWritten >= rowLimit ) {
    			// we are done, stop the transformation
    			throw new MonetDBRowLimitException("Row limit exceeded");
    		}
    	}
		addRowToBuffer(rowMeta, r);
		rowsWritten++;
    }
	
	@Override
	public void setRowLimit(long rowLimit) {
		((MonetDBAgileMartMeta) getMeta()).setRowLimit( rowLimit );
	}	

	/**
	 * Write the current buffer to the mclient. This is called when a data load is cancelled
	 * @return
	 */
	public boolean flush() {
		try {
			writeBufferToMonetDB();
			return true;
		} catch (KettleException e) {
			MonetDBBulkLoaderMeta meta = getMeta();
			setMessage( BaseMessages.getString(PKG, "MonetDBAgileMart.Log.FlushError", meta.getTableName(), this.getMessage() ) );
			log.logError(BaseMessages.getString(PKG, "MonetDBAgileMart.Log.FlushError", meta.getTableName(), this.getMessage() ), e);
		}
		return false;
	}
	
	public boolean truncateTable( ) {
		MonetDBBulkLoaderMeta meta = getMeta();
		try {
			Runtime rt = Runtime.getRuntime();
			String cmd = createCommandLine(meta, false);
			truncateTable( rt, cmd );
			return true;
		} catch (KettleException e) {
			setMessage( BaseMessages.getString(PKG, "MonetDBAgileMart.Log.TruncateError", meta.getTableName(), this.getMessage() ) );
			log.logError(BaseMessages.getString(PKG, "MonetDBAgileMart.Log.TruncateError", meta.getTableName(), this.getMessage() ), e);
		}
		return false;
	}	
	
	@Override
	public void setTableName(String tableName) {
		MonetDBBulkLoaderMeta meta = getMeta();
		meta.setTableName(tableName);
	}

	@Override
	public boolean adjustSchema() {

		MonetDBBulkLoaderMeta meta = getMeta();
		try {
			Runtime rt = Runtime.getRuntime();
			String cmd = createCommandLine(meta, false);
			autoAdjustSchema( meta, rt, cmd );
			return true;
		} catch (KettleException e) {
			setMessage( BaseMessages.getString(PKG, "MonetDBAgileMart.Log.SchemaError", meta.getTableName(), this.getMessage() ) );
			log.logError(BaseMessages.getString(PKG, "MonetDBAgileMart.Log.SchemaError", meta.getTableName(), this.getMessage() ), e);
		}
		return false;
	}
	
	public boolean dropTable( )  {
		MonetDBBulkLoaderMeta meta = getMeta();
		try {
			Runtime rt = Runtime.getRuntime();
			String cmd = createCommandLine(meta, false);
			dropTable( rt, cmd );
			return true;
		} catch (KettleException e) {
			setMessage( BaseMessages.getString(PKG, "MonetDBAgileMart.Log.DropError", meta.getTableName(), this.getMessage() ) );
			log.logError(BaseMessages.getString(PKG, "MonetDBAgileMart.Log.DropError", meta.getTableName(), this.getMessage() ), e);
		}
		return false;
	}
	
	
}
