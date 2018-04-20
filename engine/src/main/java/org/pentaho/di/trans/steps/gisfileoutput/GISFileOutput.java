package org.pentaho.di.trans.steps.gisfileoutput;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.geospatial.GeotoolsWriter;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;

import java.io.IOException;

/**
 * Reads data from an GIS file.
 * 
 * @author jmathieu, etdub
 * @since 29-sep-2008
 */
public class GISFileOutput extends BaseStep implements StepInterface {

	private static Class<?> PKG = GISFileOutput.class;

	private final String DEFAULT_ENC = "ISO-8859-1";
	
	private GISFileOutputMeta meta;
	private GISFileOutputData data;

	public GISFileOutput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private boolean createWriter(Object[] r){
		if(data.file!=null)
			closeFile();
		try {
			data.gtWriter = new GeotoolsWriter(data.file.getURL(), data.charset);
			data.gtWriter.open();
		} catch (FileSystemException e) {
			logError("Cannot get file URL.", e);
			return false;
		} catch (KettleException e) {
			logError("Could not open writer.", e);
			return false;
		}
		try {
			data.gtWriter.createSimpleFeatureType(data.outputRowMeta, r);
		} catch (KettleException e) {
			logError("Could not create feature type.", e);
			return false;
		}
		return true;
	}
	
	private Object[] getRowPreviousStep() throws KettleException {
		try {
			return !meta.isFileNameInField()?getRow():getRowFrom(data.rowSet);
		} catch (Exception e) {
			throw new KettleException("Cannot create file object.", e);
		} 
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)throws KettleException {
		meta = (GISFileOutputMeta) smi;
		data = (GISFileOutputData) sdi;

		Object[] r = getRowPreviousStep();  
		
		if(r==null){
			setOutputDone();
			return false;
		}else{
			if (first) {	
				first = false;	
				if(!meta.isFileNameInField()){
					data.outputRowMeta = getInputRowMeta().clone();
					createWriter(r);
				}else				
					data.outputRowMeta  = data.rowSet.getRowMeta();						
			}			
				
			if(meta.isFileNameInField() && openFile(r[data.outputRowMeta.indexOfValue(meta.getFileNameField())].toString()))
				createWriter(r);	

			if (!writeRowToFile(r)){
				setErrors(1);
				stopAll();
				return false;
			}
			
			putRow(data.outputRowMeta, r);
			
	        if (checkFeedback(getLinesOutput()) && log.isBasic()) 
	        	logBasic("linenr "+getLinesOutput());
			
			return true;
		}	
	}
	
	public boolean openFile(String fileName){
		try {
			FileObject fo;
			try {
				fo = KettleVFS.getFileObject(fileName);
			} catch (Exception e) {
				logError("Cannot create file object.", e);
				return false;
			}
			if(data.file==null || !data.file.getURL().equals(fo.getURL())){
				data.file = fo;
				return true;
			}			
		}catch (FileSystemException e){
			logError("Cannot open/create file ", e);
			return false;
		}
		return false;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi){
		meta=(GISFileOutputMeta)smi;
		data=(GISFileOutputData)sdi;
		if (super.init(smi, sdi)){
			if(!meta.isFileNameInField()){
				try {						
					data.file = KettleVFS.getFileObject(environmentSubstitute(meta.getFileName()));
				} catch (Exception e) {
					logError("Cannot create file object.", e);
					return false;
				}
			}else{
				try {
					data.rowSet = findInputRowSet(meta.getAcceptingStepName());
				} catch (KettleStepException e) {
					logError("Could not retrieve filenames from previous step.", e);
					return false;
				}
			}
			data.charset =	Const.isEmpty(meta.getGisFileCharset())?DEFAULT_ENC:meta.getGisFileCharset();
			return true;
		}
		return false;
	}

	private boolean writeRowToFile(Object[] r){	
		try {
			data.gtWriter.putRow(r);
		} catch (KettleException e) {
			logError("Could not write row.", e);
			return false;
		}
		return true;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi){
		closeFile();
		super.dispose(smi, sdi);
	}

	private void closeFile() {
		try {
			if(data.file!=null)
				data.file.close();
		} catch (FileSystemException e) {
			logError("Error closing file", e);
		}
		if(data.gtWriter!=null){
			incrementLinesOutput();	
			data.gtWriter.close();
		}
	}

	// Run is were the action happens!
//	public void run() {
//		try {
//			logBasic(BaseMessages.getString(PKG,"GISFileOutput.Log.StartingToRun")); //$NON-NLS-1$
//			while (!isStopped() && processRow(meta, data));
//		}catch (Exception e) {
//			logError(BaseMessages.getString(PKG,"GISFileOutput.Log.Error.UnexpectedError") + " : " + e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
//			logError(Const.getStackTracker(e));
//			setErrors(1);
//			stopAll();
//		}finally {
//			dispose(meta, data);
//			markStop();
//			logSummary();
//		}
//	}
}