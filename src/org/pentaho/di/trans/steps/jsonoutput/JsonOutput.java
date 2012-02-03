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

package org.pentaho.di.trans.steps.jsonoutput;



import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.commons.vfs.FileObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.jsonoutput.JsonOutputMeta;

/**
 * Converts input rows to one or more XML files.
 * 
 * @author Matt
 * @since 14-jan-2006
 */
public class JsonOutput extends BaseStep implements StepInterface
{
	private static Class<?> PKG = JsonOutput.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private JsonOutputMeta meta;
    private JsonOutputData data;
     
    private interface CompatibilityFactory  {
       public void execute(Object[] row) throws KettleException;
    }
    
    @SuppressWarnings("unchecked")
    private class CompatibilityMode implements CompatibilityFactory {
       public void execute(Object[] row) throws KettleException {
       
          for (int i=0;i<data.nrFields;i++) {
             JsonOutputField outputField = meta.getOutputFields()[i];
             
             ValueMetaInterface v = data.inputRowMeta.getValueMeta(data.fieldIndexes[i]);
             
             // Create a new object with specified fields
             JSONObject jo = new JSONObject();

             switch (v.getType()) {
                case ValueMeta.TYPE_BOOLEAN:
                   jo.put(outputField.getElementName(), data.inputRowMeta.getBoolean(row, data.fieldIndexes[i]));
                   break;
                case ValueMeta.TYPE_INTEGER:
                   jo.put(outputField.getElementName(), data.inputRowMeta.getInteger(row, data.fieldIndexes[i]));
                   break;
                case ValueMeta.TYPE_NUMBER:
                   jo.put(outputField.getElementName(), data.inputRowMeta.getNumber(row, data.fieldIndexes[i]));
                   break;
                case ValueMeta.TYPE_BIGNUMBER:
                   jo.put(outputField.getElementName(), data.inputRowMeta.getBigNumber(row, data.fieldIndexes[i]));
                   break;
                default:
                   jo.put(outputField.getElementName(), data.inputRowMeta.getString(row, data.fieldIndexes[i]));
                   break;
              }
              data.ja.add(jo);     
          }

          data.nrRow++;
          
          if(data.nrRowsInBloc>0) {
             System.out.println("data.nrRow%data.nrRowsInBloc = "+ data.nrRow%data.nrRowsInBloc);
             if(data.nrRow%data.nrRowsInBloc==0) {
                // We can now output an object
                System.out.println("outputting the row.");
                outPutRow(row);
             }
          }
       }
    }
    
    @SuppressWarnings("unchecked")
    private class FixedMode implements CompatibilityFactory {
       public void execute(Object[] row) throws KettleException {
          
          // Create a new object with specified fields
          JSONObject jo = new JSONObject();
                  
          for (int i=0;i<data.nrFields;i++) {
              JsonOutputField outputField = meta.getOutputFields()[i];
              
              ValueMetaInterface v = data.inputRowMeta.getValueMeta(data.fieldIndexes[i]);
              
              switch (v.getType()) {
                 case ValueMeta.TYPE_BOOLEAN:
                    jo.put(outputField.getElementName(), data.inputRowMeta.getBoolean(row, data.fieldIndexes[i]));
                    break;
                 case ValueMeta.TYPE_INTEGER:
                    jo.put(outputField.getElementName(), data.inputRowMeta.getInteger(row, data.fieldIndexes[i]));
                    break;
                 case ValueMeta.TYPE_NUMBER:
                    jo.put(outputField.getElementName(), data.inputRowMeta.getNumber(row, data.fieldIndexes[i]));
                    break;
                 case ValueMeta.TYPE_BIGNUMBER:
                    jo.put(outputField.getElementName(), data.inputRowMeta.getBigNumber(row, data.fieldIndexes[i]));
                    break;
                 default:
                    jo.put(outputField.getElementName(), data.inputRowMeta.getString(row, data.fieldIndexes[i]));
                    break;
              }           
          }
          data.ja.add(jo);

          data.nrRow++;
          
          if(data.nrRowsInBloc > 0) {
             System.out.println("data.nrRow%data.nrRowsInBloc = "+ data.nrRow%data.nrRowsInBloc);
             if(data.nrRow%data.nrRowsInBloc==0) {
                // We can now output an object
                System.out.println("outputting the row.");
                outPutRow(row);
             }
          }
       }  
    }
    
    private CompatibilityFactory compatibilityFactory;
    
    public JsonOutput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
    {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
        
        //   Here we decide whether or not to build the structure in
        //   compatible mode or fixed mode
        JsonOutputMeta jsonOutputMeta = (JsonOutputMeta)(stepMeta.getStepMetaInterface());
        if (jsonOutputMeta.isCompatibilityMode()) {
           compatibilityFactory = new CompatibilityMode();
        }
        else {
           compatibilityFactory = new FixedMode();
        }
    }
    
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
       
       meta=(JsonOutputMeta)smi;
       data=(JsonOutputData)sdi;

       Object[] r = getRow();       // This also waits for a row to be finished.
       if (r==null)  {
         // no more input to be expected...
        if(!data.rowsAreSafe) {
           // Let's output the remaining unsafe data
               outPutRow(r);
           }
           
           setOutputDone();
           return false;
       }
       
       if (first)  {
        first=false;
        data.inputRowMeta=getInputRowMeta();
        data.inputRowMetaSize=data.inputRowMeta.size();
        if(data.outputValue) {
           data.outputRowMeta = data.inputRowMeta.clone();
           meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
        }
        
        // Cache the field name indexes
        //
        data.nrFields=meta.getOutputFields().length;
        data.fieldIndexes = new int[data.nrFields];
        for (int i=0;i<data.nrFields;i++) {
           data.fieldIndexes[i] = data.inputRowMeta.indexOfValue(meta.getOutputFields()[i].getFieldName());
           if (data.fieldIndexes[i]<0) {
              throw new KettleException(BaseMessages.getString(PKG, "JsonOutput.Exception.FieldNotFound")); //$NON-NLS-1$
           }
           JsonOutputField field = meta.getOutputFields()[i];
           field.setElementName(environmentSubstitute(field.getElementName()));
        }
       }
       
       data.rowsAreSafe=false;
       compatibilityFactory.execute(r);
       
     if(data.writeToFile && !data.outputValue) {
        putRow(data.inputRowMeta,r ); // in case we want it go further...
        incrementLinesOutput();
     }
       return true;
    }    
        
   @SuppressWarnings("unchecked")
	private void outPutRow(Object[] rowData) throws KettleStepException {
    	// We can now output an object
		data.jg = new JSONObject();
		data.jg.put(data.realBlocName, data.ja);
		String value = data.jg.toJSONString();
		
		if(data.outputValue) {
			Object[] outputRowData = RowDataUtil.addValueData(rowData, data.inputRowMetaSize, value);
			incrementLinesOutput();
			putRow(data.outputRowMeta, outputRowData);
		}
		
		if(data.writeToFile) {
			// Open a file
			if (!openNewFile()) {
				throw new KettleStepException(BaseMessages.getString(PKG, "JsonOutput.Error.OpenNewFile", buildFilename()));
			}
			// Write data to file
			try {
				data.writer.write(value);
			}catch(Exception e) {
				throw new KettleStepException(BaseMessages.getString(PKG, "JsonOutput.Error.Writing"), e);
			}
			// Close file
			closeFile();
		}
        // Data are safe
        data.rowsAreSafe=true;
        data.ja = new JSONArray();
    }

    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta=(JsonOutputMeta)smi;
        data=(JsonOutputData)sdi;
        if(super.init(smi, sdi)) {
        	
        	data.writeToFile = (meta.getOperationType() != JsonOutputMeta.OPERATION_TYPE_OUTPUT_VALUE);
        	data.outputValue = (meta.getOperationType() != JsonOutputMeta.OPERATION_TYPE_WRITE_TO_FILE);
        	
        	if(data.outputValue) {
        		// We need to have output field name
        		if(Const.isEmpty(environmentSubstitute(meta.getOutputValue()))) {
        			logError(BaseMessages.getString(PKG, "JsonOutput.Error.MissingOutputFieldName"));
    				stopAll();
    				setErrors(1);
        			return false;
        		}
        	}
        	if(data.writeToFile) {
        		// We need to have output field name
        		if(!meta.isServletOutput() && Const.isEmpty(meta.getFileName())) {
        			logError(BaseMessages.getString(PKG, "JsonOutput.Error.MissingTargetFilename"));
    				stopAll();
    				setErrors(1);
        			return false;
        		}
        		if(!meta.isDoNotOpenNewFileInit()) {
					if (!openNewFile()) {
						logError(BaseMessages.getString(PKG, "JsonOutput.Error.OpenNewFile", buildFilename()));
						stopAll();
						setErrors(1);
	        			return false;
					}
				}
        		

        	}
            data.realBlocName = Const.NVL(environmentSubstitute(meta.getJsonBloc()), "");
            data.nrRowsInBloc = Const.toInt(environmentSubstitute(meta.getNrRowsInBloc()), 0);
        	return true;
        }


        return false;
    }
    
    public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
        meta=(JsonOutputMeta)smi;
        data=(JsonOutputData)sdi;
        if(data.ja!=null) data.ja=null;
        if(data.jg!=null) data.jg=null;
        closeFile();
        super.dispose(smi, sdi);
        
    }   
    private void createParentFolder(String filename) throws KettleStepException {
    	if(!meta.isCreateParentFolder()) return;
		// Check for parent folder
		FileObject parentfolder=null;
		try {
			// Get parent folder
    		parentfolder=KettleVFS.getFileObject(filename, getTransMeta()).getParent();	    		
    		if(!parentfolder.exists())	{
    			if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "JsonOutput.Error.ParentFolderNotExist", parentfolder.getName()));
    			parentfolder.createFolder();
    			if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "JsonOutput.Log.ParentFolderCreated"));
    		}
		}catch (Exception e) {
			throw new KettleStepException(BaseMessages.getString(PKG, "JsonOutput.Error.ErrorCreatingParentFolder", parentfolder.getName()));
		} finally {
         	if ( parentfolder != null ){
         		try  {
         			parentfolder.close();
         		}catch ( Exception ex ) {};
         	}
         }
    }
    public boolean openNewFile()
	{
		if(data.writer!=null) return true;
		boolean retval=false;
		
		try {
         
		  if (meta.isServletOutput()) {
		    data.writer = getTrans().getServletPrintWriter();
		  } else {
  			String filename = buildFilename();
  			createParentFolder(filename);
  			if (meta.AddToResult()) {
  				// Add this to the result file names...
  				ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, KettleVFS.getFileObject(filename, getTransMeta()), getTransMeta().getName(), getStepname());
  				resultFile.setComment(BaseMessages.getString(PKG, "JsonOutput.ResultFilenames.Comment"));
  	            addResultFile(resultFile);
  			}
  			    
              OutputStream outputStream;
              OutputStream fos = KettleVFS.getOutputStream(filename, getTransMeta(), meta.isFileAppended());
              outputStream=fos;
  
              if (!Const.isEmpty(meta.getEncoding())) {
                  data.writer = new OutputStreamWriter(new BufferedOutputStream(outputStream, 5000), environmentSubstitute(meta.getEncoding()));
              } else {
                  data.writer = new OutputStreamWriter(new BufferedOutputStream(outputStream, 5000));
              }
              
              if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "JsonOutput.FileOpened", filename));
              
              data.splitnr++;
		  }
			
			retval=true;
            
		} catch(Exception e) {
			logError(BaseMessages.getString(PKG, "JsonOutput.Error.OpeningFile", e.toString()));
		}

		return retval;
	}
    public String buildFilename() {
		return meta.buildFilename(environmentSubstitute(meta.getFileName()),  getCopy(), data.splitnr);
	}
	
    private boolean closeFile()
	{
		if(data.writer==null) return true;
    	boolean retval=false;
		
		try
		{
			data.writer.close();
			data.writer=null;
			retval=true;
		}
		catch(Exception e)
		{
			logError(BaseMessages.getString(PKG, "JsonOutput.Error.ClosingFile", e.toString()));
			setErrors(1);
			retval = false;
		}

		return retval;
	} 
}