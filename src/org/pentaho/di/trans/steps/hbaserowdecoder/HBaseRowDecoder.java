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

package org.pentaho.di.trans.steps.hbaserowdecoder;

import java.util.List;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.util.Bytes;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.hbase.HBaseRowToKettleTuple;
import org.pentaho.hbase.mapping.HBaseValueMeta;
import org.pentaho.hbase.mapping.Mapping;

/**
 * Step for decoding incoming HBase Result objects using a supplied mapping. 
 * Can be used in a Hadoop MR job for processing tables split by 
 * org.pentaho.hbase.mapred.PentahoTableInputFormat (see the javadoc for this 
 * class for properties that can be set in the job to control the query)
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 */
public class HBaseRowDecoder extends BaseStep implements StepInterface {
  private static Class<?> PKG = HBaseRowDecoderMeta.class;
  
  protected HBaseRowDecoderMeta m_meta;
  protected HBaseRowDecoderData m_data;
      
  public HBaseRowDecoder(StepMeta stepMeta, StepDataInterface stepDataInterface,
      int copyNr, TransMeta transMeta, Trans trans) {
    super(stepMeta, stepDataInterface, copyNr, transMeta, trans);    
  } 
      
  /** The mapping information to use in order to decode HBase column values */
  protected Mapping m_tableMapping;
  
  /** Information from the mapping */
  protected HBaseValueMeta[] m_outputColumns;
  
  /** Index of incoming key value */
  protected int m_keyInIndex = -1;
  
  /** Index of incoming HBase row (Result object) */
  protected int m_resultInIndex = -1;
  
  /** Used when decoding columns to <key, family, column, value, time stamp> tuples */
  protected HBaseRowToKettleTuple m_tupleHandler;
    
  public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)
    throws KettleException {
    
    Object[] inputRow = getRow();
    
    if (inputRow == null) {
      
      setOutputDone();
      return false;
    }
    
    if (first) {
      first = false;
      m_meta = (HBaseRowDecoderMeta)smi;
      m_data = (HBaseRowDecoderData)sdi;
      
      m_tableMapping = m_meta.getMapping();
      
      if (m_tableMapping == null || Const.isEmpty(m_tableMapping.getKeyName())) {
        throw new KettleException(BaseMessages.getString(PKG, 
            "HBaseRowDecoder.Error.NoMappingInfo"));
      }
      
      if (m_tableMapping.isTupleMapping()) {
        m_tupleHandler = new HBaseRowToKettleTuple();
      }
      
      m_outputColumns = new HBaseValueMeta[m_tableMapping.getMappedColumns().keySet().size()];
      int k = 0;
      for (String alias : m_tableMapping.getMappedColumns().keySet()) {
        m_outputColumns[k++] = m_tableMapping.getMappedColumns().get(alias);
      }
      
      m_data.setOutputRowMeta(getInputRowMeta().clone());
      m_meta.getFields(m_data.getOutputRowMeta(), getStepname(), null, null, this);
      
      // check types first
      RowMetaInterface inputMeta = getInputRowMeta();
      String inKey = environmentSubstitute(m_meta.getIncomingKeyField());
      
      m_keyInIndex = inputMeta.indexOfValue(inKey);
      if (m_keyInIndex == -1) {
        throw new KettleException(BaseMessages.getString(PKG, 
            "HBaseRowDecoder.Error.UnableToFindHBaseKey", inKey));
      }
      if (!(inputRow[m_keyInIndex] instanceof org.apache.hadoop.hbase.io.ImmutableBytesWritable)) {        
        throw new KettleException(BaseMessages.getString(PKG, "HBaseRowDecoder.Error.NotImmutableBytesWritable", 
            m_meta.getIncomingKeyField()));
      }
      
      String inResult = environmentSubstitute(m_meta.getIncomingResultField());
      m_resultInIndex = inputMeta.indexOfValue(inResult);
      if (m_resultInIndex == -1) {
        throw new KettleException(BaseMessages.getString(PKG, "HBaseRowDecoder.Error.UnableToFindHBaseRow", 
            inResult));
      }
      if (!(inputRow[m_resultInIndex] instanceof Result)) {
        throw new KettleException(BaseMessages.getString(PKG, "HBaseRowDecoder.Error.NotResult", 
            m_meta.getIncomingResultField()));
      }            
    }
    
    //ImmutableBytesWritable key = (ImmutableBytesWritable)inputRow[m_keyInIndex];
    Result hRow = (Result)inputRow[m_resultInIndex];
    if (inputRow[m_keyInIndex] != null && hRow != null) {
      
      if (m_tableMapping.isTupleMapping()) {
        List<Object[]> hrowToKettleRow = 
          m_tupleHandler.hbaseRowToKettleTupleMode(hRow, m_tableMapping, 
              m_tableMapping.getMappedColumns(), m_data.getOutputRowMeta());
        
        for (Object[] tuple : hrowToKettleRow) {
          putRow(m_data.getOutputRowMeta(), tuple);
        }
      } else {

        Object[] outputRowData = RowDataUtil.allocateRowData(m_outputColumns.length + 1); // + 1 for key

        byte[] rowKey = hRow.getRow();
        Object decodedKey = HBaseValueMeta.decodeKeyValue(rowKey, m_tableMapping);
        outputRowData[0] = decodedKey;

        for (int i = 0; i < m_outputColumns.length; i++) {
          HBaseValueMeta current = m_outputColumns[i];

          String colFamilyName = current.getColumnFamily();
          String qualifier = current.getColumnName();

          KeyValue kv = hRow.getColumnLatest(Bytes.toBytes(colFamilyName), 
              Bytes.toBytes(qualifier));
          Object decodedVal = HBaseValueMeta.decodeColumnValue(kv, current);
          outputRowData[i + 1] = decodedVal;
        }

        // output the row
        putRow(m_data.getOutputRowMeta(), outputRowData);      
      }
    }
        
    return true;
  }
}
