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


package org.pentaho.hbase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.hbase.mapping.HBaseValueMeta;
import org.pentaho.hbase.mapping.Mapping;

/**
 * Class for decoding HBase rows to a <key, family, column, value, time stamp> 
 * Kettle row format.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 */
public class HBaseRowToKettleTuple {
  
  /** Holds a set of tuples (Kettle rows) - one for each column from an HBase row */  
  protected List<Object[]> m_decodedTuples;
  
  /** Index in the Kettle row format of the key column */
  protected int m_keyIndex = -1;
  
  /** Index in the Kettle row format of the family column */
  protected int m_familyIndex = -1;
  
  /** Index in the Kettle row format of the column name column */
  protected int m_colNameIndex = -1;
  
  /** Index in the Kettle row format of the column value column */
  protected int m_valueIndex = -1;
  
  /** Index in the Kettle row format of the time stamp column */
  protected int m_timestampIndex = -1;
  
  /** List of (optional) byte array encoded user-specified column families to extract column values for */
  protected List<byte[]> m_userSpecifiedFamilies;
  
  /** List of (optional) human-readable user-specified column families to extract column values for */
  protected List<String> m_userSpecifiedFamiliesHumanReadable;
  
  protected List<HBaseValueMeta> m_tupleColsFromAliasMap;  
  
  public void reset() {
    m_decodedTuples = null;
    
    m_keyIndex = -1;
    m_familyIndex = -1;
    m_colNameIndex = -1;
    m_valueIndex = -1;
    m_timestampIndex = -1;
    m_userSpecifiedFamilies = null;
    m_userSpecifiedFamiliesHumanReadable = null;
    
    m_tupleColsFromAliasMap = null;
    m_decodedTuples = null;
  }
  
  /**
   * Convert an HBase row to (potentially) multiple Kettle rows in tuple 
   * format.
   * 
   * @param hRow an HBase row
   * @param mapping the mapping information to use (must be a "tuple" mapping)
   * @param tupleColsMappedByAlias the meta data for each of the tuple columns the user 
   * has opted to have output
   * @param outputRowMeta the outgoing Kettle row format
   * @return a list of Kettle rows in tuple format
   * @throws KettleException if a problem occurs
   */
  public List<Object[]> hbaseRowToKettleTupleMode(Result hRow, Mapping mapping,
      Map<String, HBaseValueMeta> tupleColsMappedByAlias, 
      RowMetaInterface outputRowMeta) throws KettleException {
    
    if (m_decodedTuples == null) {
      m_tupleColsFromAliasMap = new ArrayList<HBaseValueMeta>();
      // add the key first - type (or name for that matter) 
      // is not important as this is just a dummy placeholder
      // here so that indexes into m_tupleColsFromAliasMap align with the output row meta
      // format
      HBaseValueMeta keyMeta = new HBaseValueMeta(mapping.getKeyName() + 
          HBaseValueMeta.SEPARATOR + "dummy", 
          ValueMetaInterface.TYPE_INTEGER, 0, 0);
      m_tupleColsFromAliasMap.add(keyMeta);
      
      for (String alias : tupleColsMappedByAlias.keySet()) {
        m_tupleColsFromAliasMap.add(tupleColsMappedByAlias.get(alias));
      }
    }    
    
    return hbaseRowToKettleTupleMode(hRow, mapping, m_tupleColsFromAliasMap, 
        outputRowMeta);    
  }
  
  /**
   * Convert an HBase row to (potentially) multiple Kettle rows in tuple 
   * format.
   * 
   * @param hRow an HBase row
   * @param mapping the mapping information to use (must be a "tuple" mapping)
   * @param tupleCols the meta data for each of the tuple columns the user 
   * has opted to have output
   * @param outputRowMeta the outgoing Kettle row format
   * @return a list of Kettle rows in tuple format
   * @throws KettleException if a problem occurs
   */
  public List<Object[]> hbaseRowToKettleTupleMode(Result hRow, Mapping mapping, 
      List<HBaseValueMeta> tupleCols, RowMetaInterface outputRowMeta) throws KettleException {
    
    if (m_decodedTuples == null) {
      m_decodedTuples = new ArrayList<Object[]>();
      m_keyIndex = outputRowMeta.indexOfValue(mapping.getKeyName());
      m_familyIndex = outputRowMeta.indexOfValue("Family");
      m_colNameIndex = outputRowMeta.indexOfValue("Column");
      m_valueIndex = outputRowMeta.indexOfValue("Value");
      m_timestampIndex = outputRowMeta.indexOfValue("Timestamp");
      
      if (!Const.isEmpty(mapping.getTupleFamilies())) {
        String[] familiesS = mapping.getTupleFamilies().split(HBaseValueMeta.SEPARATOR);
        m_userSpecifiedFamilies = new ArrayList<byte[]>();
        m_userSpecifiedFamiliesHumanReadable = new ArrayList<String>();
        
        for (String family : familiesS) {
          m_userSpecifiedFamiliesHumanReadable.add(family);
          m_userSpecifiedFamilies.add(Bytes.toBytes(family.trim()));
        }
      }
    } else {
      m_decodedTuples.clear();
    }
    
    byte[] rawKey = hRow.getRow();
    Object decodedKey = HBaseValueMeta.decodeKeyValue(rawKey, mapping);
    
    NavigableMap<byte[],NavigableMap<byte[],NavigableMap<Long,byte[]>>> rowData = 
      hRow.getMap();
    
    if (!Const.isEmpty(mapping.getTupleFamilies())) {   
      int i = 0;
      for (byte[] family : m_userSpecifiedFamilies) {
        NavigableMap<byte[], NavigableMap<Long, byte[]>> colMap = rowData.get(family);
        for (byte[] colName : colMap.keySet()) {
          NavigableMap<Long, byte[]> valuesByTimestamp = colMap.get(colName);
          
          Object[] newTuple = RowDataUtil.allocateRowData(outputRowMeta.size());
          
          // row key
          if (m_keyIndex != -1) {
            newTuple[m_keyIndex] = decodedKey;
          }
          
          // get value of most recent column value
          Map.Entry<Long, byte[]> mostRecentColVal = valuesByTimestamp.lastEntry();
          
          // store the timestamp
          if (m_timestampIndex != -1) {
            newTuple[m_timestampIndex] = mostRecentColVal.getKey();
          }
          
          // column name
          if (m_colNameIndex != -1) {
            HBaseValueMeta colNameMeta = tupleCols.get(m_colNameIndex);
            Object decodedColName = HBaseValueMeta.decodeColumnValue(colName, colNameMeta);
            newTuple[m_colNameIndex] = decodedColName;
          }
          
          // column value
          if (m_valueIndex != -1) {            
            HBaseValueMeta colValueMeta = tupleCols.get(m_valueIndex);
            Object decodedValue = HBaseValueMeta.
              decodeColumnValue(mostRecentColVal.getValue(), colValueMeta);
            newTuple[m_valueIndex] = decodedValue;
          }          
          
          // column family
          if (m_familyIndex != -1) {
            newTuple[m_familyIndex] = m_userSpecifiedFamiliesHumanReadable.get(i);
          }
          
          m_decodedTuples.add(newTuple);
        }
        i++;
      }
    } else {
      // process all column families
      for (byte[] family : rowData.keySet()) {
        
        // column family
        Object decodedFamily = null;
        if (m_familyIndex != -1) {
          HBaseValueMeta colFamMeta = tupleCols.get(m_familyIndex);
          decodedFamily = HBaseValueMeta.decodeColumnValue(family, colFamMeta);
        }
        
        NavigableMap<byte[], NavigableMap<Long, byte[]>> colMap = rowData.get(family);
        for (byte[] colName : colMap.keySet()) {
          NavigableMap<Long, byte[]> valuesByTimestamp = colMap.get(colName);
          
          Object[] newTuple = RowDataUtil.allocateRowData(outputRowMeta.size());
          
          // row key
          if (m_keyIndex != -1) {
            newTuple[m_keyIndex] = decodedKey;
          }
          
          // get value of most recent column value
          Map.Entry<Long, byte[]> mostRecentColVal = valuesByTimestamp.lastEntry();
          
          // store the timestamp
          if (m_timestampIndex != -1) {
            newTuple[m_timestampIndex] = mostRecentColVal.getKey();
          }
          
          // column name
          if (m_colNameIndex != -1) {
            HBaseValueMeta colNameMeta = tupleCols.get(m_colNameIndex);
            Object decodedColName = HBaseValueMeta.decodeColumnValue(colName, colNameMeta);
            newTuple[m_colNameIndex] = decodedColName;
          }
          
          // column value
          if (m_valueIndex != -1) {            
            HBaseValueMeta colValueMeta = tupleCols.get(m_valueIndex);
            Object decodedValue = HBaseValueMeta.
              decodeColumnValue(mostRecentColVal.getValue(), colValueMeta);
            newTuple[m_valueIndex] = decodedValue;
          }          
          
          // column family
          if (m_familyIndex != -1) {
            newTuple[m_familyIndex] = decodedFamily;
          }
          
          m_decodedTuples.add(newTuple);
        }
      }
    }
       
    return m_decodedTuples;
  }
}