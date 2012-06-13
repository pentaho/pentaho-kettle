/*******************************************************************************
 *
 * Pentaho Big Data
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

package org.pentaho.di.trans.steps.avroinput;

import java.io.IOException;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
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
 * Class providing an input step for reading data from an Avro serialized file. 
 * Handles both container files (where the schema is serialized into the file) and 
 * schemaless files. In the case of the later, the user must supply a schema in order 
 * to read objects from the file. In the case of the former, a schema can be optionally 
 * supplied.
 * 
 * Currently supports Avro records, arrays, maps and primitive types. Paths use the "dot" 
 * notation and "$" indicates the root of the object. Arrays and maps are accessed via "[]" 
 * and differ only in that array elements are accessed via zero-based integer indexes and 
 * map values are accessed by string keys.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 */
public class AvroInput extends BaseStep implements StepInterface {
  
  protected AvroInputMeta m_meta;
  protected AvroInputData m_data;  
  
  public AvroInput(StepMeta stepMeta, StepDataInterface stepDataInterface, 
      int copyNr, TransMeta transMeta, Trans trans) {
    
    super(stepMeta, stepDataInterface, copyNr, transMeta, trans);    
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.di.trans.step.BaseStep#processRow(org.pentaho.di.trans.step.StepMetaInterface, org.pentaho.di.trans.step.StepDataInterface)
   */
  public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) 
    throws KettleException {
    
    Object[] currentInputRow = getRow();
    
    
    if (first) {
      first = false;
      
      m_data = (AvroInputData)sdi;
      m_meta = (AvroInputMeta)smi;
      
      if (Const.isEmpty(m_meta.getFilename()) && !m_meta.getAvroInField()) {
        throw new KettleException(BaseMessages.
            getString(AvroInputMeta.PKG, "AvroInput.Error.NoAvroFileSpecified"));
      }                  

      String readerSchema = m_meta.getSchemaFilename();
      readerSchema = environmentSubstitute(readerSchema);
      String avroFieldName = m_meta.getAvroFieldName();
      avroFieldName = environmentSubstitute(avroFieldName);
      
      // setup the output row meta
      RowMetaInterface outRowMeta = null;
      outRowMeta = getInputRowMeta();
      if (outRowMeta != null) {
        outRowMeta = outRowMeta.clone();
      } else {
        outRowMeta = new RowMeta();
      }

      int newFieldOffset = outRowMeta.size();
      m_data.setOutputRowMeta(outRowMeta);
      m_meta.getFields(m_data.getOutputRowMeta(), getStepname(), null, null, this);
      
      // initialize substitution fields
      if (m_meta.getLookupFields() != null && m_meta.getLookupFields().size() > 0) {
        for (AvroInputMeta.LookupField f : m_meta.getLookupFields()) {
          f.init(getInputRowMeta(), this);
        }
      }
      
      if (m_meta.getAvroInField()) {
        if (m_meta.getLookupFields() != null && m_meta.getLookupFields().size() > 0) {
//          System.out.println("---------- about to initialize from field decoding....");
        }
        // initialize for reading from a field
        m_data.initializeFromFieldDecoding(avroFieldName, readerSchema, 
            m_meta.getAvroFields(), m_meta.getAvroIsJsonEncoded(), newFieldOffset);
        if (m_meta.getLookupFields() != null && m_meta.getLookupFields().size() > 0) {
//          System.out.println("Initialization done...");
        }
      } else {
        // initialize for reading from a file
        FileObject fileObject = KettleVFS.getFileObject(m_meta.getFilename(), getTransMeta());      
        m_data.establishFileType(fileObject, readerSchema, m_meta.getAvroFields(), 
            m_meta.getAvroIsJsonEncoded(), newFieldOffset);
      }
    }    
    
    if (!m_meta.getAvroInField()) {
      currentInputRow = null;
    } else {
      if (currentInputRow != null) {
        // set variables lookup values
        if (m_meta.getLookupFields() != null && m_meta.getLookupFields().size() > 0) {
          for (AvroInputMeta.LookupField f : m_meta.getLookupFields()) {
            f.setVariable(this, currentInputRow);
          }
        }
      }
    }
        
    Object [][] outputRow = null;
    try {
      outputRow = m_data.avroObjectToKettle(currentInputRow, this);
    } catch (Exception ex) {
      if (getStepMeta().isDoingErrorHandling()) {
        String errorDescriptions = "An error occurred while decoding an avro object: " 
          + ex.getMessage();
        String errorFields = "";
        RowMetaInterface rowMeta = null;
        Object[] currentRow = new Object[0];
        if (m_meta.getAvroInField()) {
          errorFields += m_meta.getAvroFieldName();
          rowMeta = getInputRowMeta();
          currentRow = currentInputRow;
        } else {
          errorFields = "Data read from file";
          rowMeta = m_data.getOutputRowMeta();
        }
        putError(rowMeta, currentRow, 1, errorDescriptions, errorFields, "AvroInput001");
        
        if (checkFeedback(getProcessed())) {
          logBasic(BaseMessages.getString(AvroInputMeta.PKG, "AvroInput.Message.CheckFeedback", 
              getProcessed()));
//          logBasic("Read " + getProcessed() + " rows from Avro file");
        }
        
        return true;
      } else {
        throw new KettleException(ex.getMessage(), ex);
      }
    }
    if (outputRow != null) {
      // there may be more than one row if the paths contain an array/map expansion
      for (int i = 0; i < outputRow.length; i++) {
        putRow(m_data.getOutputRowMeta(), outputRow[i]);


        if (log.isRowLevel()) {
          log.logRowlevel(toString(), "Outputted row #" + getProcessed() 
              + " : " + outputRow);
        }
      }
    } else {
      if (!m_meta.getAvroInField()) {
        try {
          logBasic(BaseMessages.getString(AvroInputMeta.PKG, 
          "AvroInput.Message.ClosingFile"));
          m_data.close();
        } catch (IOException ex) {
          throw new KettleException(ex.getMessage(), ex);
        }
      }
      setOutputDone();
      return false;
    }    
    
    if (checkFeedback(getProcessed())) {
      logBasic(BaseMessages.getString(AvroInputMeta.PKG, "AvroInput.Message.CheckFeedback", 
          getProcessed()));
//      logBasic("Read " + getProcessed() + " rows from Avro file");
    }
    
    return true;
  }  
  
  /* (non-Javadoc)
   * @see org.pentaho.di.trans.step.BaseStep#setStopped(boolean)
   */
  public void setStopped(boolean stopped) {
    if (isStopped() && stopped == true) {
      return;
    }
    
    super.setStopped(stopped);
    
    if (stopped && !m_meta.getAvroInField()) {
      try {
        logBasic(BaseMessages.getString(AvroInputMeta.PKG, 
          "AvroInput.Message.ClosingFile"));
        m_data.close();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }
}
