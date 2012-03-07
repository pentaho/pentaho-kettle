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
  
  public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) 
    throws KettleException {
    
    if (first) {
      first = false;
      
      m_data = (AvroInputData)sdi;
      m_meta = (AvroInputMeta)smi;
      
      if (Const.isEmpty(m_meta.getFilename())) {
        throw new KettleException(BaseMessages.
            getString(AvroInputMeta.PKG, "AvroInput.Error.NoAvroFileSpecified"));
      }            
      
      FileObject fileObject = KettleVFS.getFileObject(m_meta.getFilename(), getTransMeta());      

      String readerSchema = m_meta.getSchemaFilename();
      readerSchema = environmentSubstitute(readerSchema);

      m_data.establishFileType(fileObject, readerSchema, m_meta.getAvroFields(), 
          m_meta.getAvroFileIsJsonEncoded());
      
      // setup the output row meta
      m_data.setOutputRowMeta(new RowMeta());
      m_meta.getFields(m_data.getOutputRowMeta(), getStepname(), null, null, this);      
    }
    
    Object[] outputRow = m_data.avroObjectToKettle();
    if (outputRow != null) {
      putRow(m_data.getOutputRowMeta(), outputRow);
      
      if (log.isRowLevel()) {
        log.logRowlevel(toString(), "Outputted row #" + getProcessed() 
            + " : " + outputRow);
      }
    } else {
      try {
        logBasic(BaseMessages.getString(AvroInputMeta.PKG, 
            "AvroInput.Message.ClosingFile"));
        m_data.close();
      } catch (IOException ex) {
        throw new KettleException(ex.getMessage(), ex);
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
  
  public void setStopped(boolean stopped) {
    super.setStopped(stopped);
    
    if (stopped) {
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
