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

package org.pentaho.di.trans.steps.hbaseinput;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.filter.WritableByteArrayComparable;
import org.apache.hadoop.hbase.util.Bytes;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.hbase.mapping.DeserializedBooleanComparator;
import org.pentaho.hbase.mapping.DeserializedNumericComparator;
import org.pentaho.hbase.mapping.HBaseValueMeta;
import org.pentaho.hbase.mapping.Mapping;
import org.pentaho.hbase.mapping.MappingAdmin;

/**
 * Class providing an input step for reading data from an HBase table
 * according to meta data mapping info stored in a separate HBase table
 * called "pentaho_mappings". See org.pentaho.hbase.mapping.Mapping for
 * details on the meta data format.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 *
 */
public class HBaseInput extends BaseStep implements StepInterface {
  
  protected HBaseInputMeta m_meta;
  protected HBaseInputData m_data;  

  public HBaseInput(StepMeta stepMeta, StepDataInterface stepDataInterface,
      int copyNr, TransMeta transMeta, Trans trans) {
    
    super(stepMeta, stepDataInterface, copyNr, transMeta, trans);    
  }
  
  /** Configuration object for connecting to HBase */
  protected Configuration m_connection;
  
  /** The mapping admin object for interacting with mapping information */
  protected MappingAdmin m_mappingAdmin;  
  
  /** The mapping information to use in order to decode HBase column values */
  protected Mapping m_tableMapping;
  
  /** Information from the mapping */
  protected Map<String, HBaseValueMeta> m_columnsMappedByAlias;
  
  /** User-selected columns from the mapping (null indicates output all columns) */
  protected List<HBaseValueMeta> m_userOutputColumns;
  
  /** Table object for the table to read from */
  protected HTable m_sourceTable;
  
  /** Scanner over the result rows */
  protected ResultScanner m_resultSet;
  
  public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) 
    throws KettleException {
    
    if (first) {
      first = false;
      m_meta = (HBaseInputMeta)smi;
      m_data = (HBaseInputData)sdi;
      
      // Get the connection to HBase
      try {
        m_connection = HBaseInputData.
        getHBaseConnection(environmentSubstitute(m_meta.getZookeeperHosts()),
            environmentSubstitute(m_meta.getZookeeperPort()),
            HBaseInputData.
              stringToURL(environmentSubstitute(m_meta.getCoreConfigURL())), 
            HBaseInputData.
              stringToURL(environmentSubstitute(m_meta.getDefaultConfigURL())));
      } catch (IOException ex) {
        throw new KettleException("Unable to obtain a connection to HBase.", ex);
      }
      try {
        m_mappingAdmin = new MappingAdmin(m_connection);
      } catch (Exception ex) {
        throw new KettleException("Unable to create a MappingAdmin connection", ex);
      }
      
      // check on the existence and readiness of the target table
      HBaseAdmin admin = null;
      try {
        admin = new HBaseAdmin(m_connection);
      } catch (Exception ex) {
        throw new KettleException("Unable to obtain a connection to HBase.", ex);
      }

      String sourceName = environmentSubstitute(m_meta.getSourceTableName());
      if (StringUtil.isEmpty(sourceName)) {
        throw new KettleException(BaseMessages.getString(getClass(), "HBaseInput.TableName.Missing"));
      }
      try {
        if (!admin.tableExists(sourceName)) {
          throw new KettleException("Source table \"" + sourceName + "\" does not exist!");
        }
      
        if (admin.isTableDisabled(sourceName) || !admin.isTableAvailable(sourceName)) {
          throw new KettleException("Source table \"" + sourceName + "\" is not available!");          
        }
      } catch (IOException ex) {
        throw new KettleException("A problem occurred when trying to check " +
                        "availablility/readyness of source table \"" 
            + sourceName + "\"", ex);
      }
      
      // Get mapping details for the source table
      try {
        m_tableMapping = m_mappingAdmin.getMapping(
            environmentSubstitute(m_meta.getSourceTableName()), 
            environmentSubstitute(m_meta.getSourceMappingName()));
        m_columnsMappedByAlias = m_tableMapping.getMappedColumns();
      } catch (IOException ex) {
        throw new KettleException(ex.getMessage(), ex);
      }
      
      // conversion mask to use for user specified key values in range scan.
      // This can come from user-specified field information OR it can be
      // provided in the keyStart/keyStop values by suffixing the value with
      // "@converionMask"
      String dateOrNumberConversionMaskForKey = null;
      
      // if there are any user-chosen output fields in the meta data then
      // check them against table mapping. All selected fields must be present
      // in the mapping
      m_userOutputColumns = m_meta.getOutputFields();
      if (m_userOutputColumns != null && m_userOutputColumns.size() > 0) {
        for (HBaseValueMeta vm : m_userOutputColumns) {
          if (!vm.isKey()) {
            if (m_columnsMappedByAlias.get(vm.getAlias()) == null) {
              throw new KettleException("Unable to find user-selected column " +
                  "\"" + vm.getAlias() + "\" in the table mapping \""
                  + m_tableMapping.getTableName() + HBaseValueMeta.SEPARATOR 
                  + m_tableMapping.getMappingName() + "\"");
            }
          } else {
            dateOrNumberConversionMaskForKey = vm.getConversionMask();
          }
        }
      }
            
      // Set up the scan
      Scan s = null;
      if (Const.isEmpty(m_meta.getKeyStartValue()) && 
          Const.isEmpty(m_meta.getKeyStopValue())) {
        s = new Scan(); // scan all rows
      } else {
        byte[] keyLowerBound = null;
        String keyStartS = environmentSubstitute(m_meta.getKeyStartValue());
        String convM = dateOrNumberConversionMaskForKey;
        
        if (m_tableMapping.getKeyType() == Mapping.KeyType.BINARY) {
          // assume we have a hex encoded string
          keyLowerBound = HBaseValueMeta.encodeKeyValue(keyStartS, m_tableMapping.getKeyType());
        } else if (m_tableMapping.getKeyType() != Mapping.KeyType.STRING) {
          // allow a conversion mask in the start key field to override any specified for
          // the key in the user specified fields
          String[] parts = keyStartS.split("@");
          if (parts.length == 2) {
            keyStartS = parts[0];
            convM = parts[1];
          }
          
          if (!Const.isEmpty(convM) && convM.length() > 0) {
            
            if (m_tableMapping.getKeyType() == Mapping.KeyType.DATE ||
                m_tableMapping.getKeyType() == Mapping.KeyType.UNSIGNED_DATE) {
              SimpleDateFormat sdf = new SimpleDateFormat();
              sdf.applyPattern(convM);
              try {
                Date d = sdf.parse(keyStartS);
                keyLowerBound = HBaseValueMeta.encodeKeyValue(d, m_tableMapping.getKeyType());
              } catch (ParseException e) {
                throw new KettleException("Unable to parse lower bound key value \""
                    + keyStartS + "\"");
              }
            } else {
              // Number type
              // Double/Float or Long/Integer              
              DecimalFormat df = new DecimalFormat();
              df.applyPattern(convM);
              Number num = null;
              try {
                num = df.parse(keyStartS);
                keyLowerBound = HBaseValueMeta.encodeKeyValue(num, m_tableMapping.getKeyType());
              } catch (ParseException e) {
                throw new KettleException("Unable to parse lower bound key value \""
                    + keyStartS + "\"");
              }
            }
          } else {
            // just try it as a string
            keyLowerBound = HBaseValueMeta.
              encodeKeyValue(keyStartS, m_tableMapping.getKeyType());
          }
        } else {          
          // it is a string
          keyLowerBound = HBaseValueMeta.
            encodeKeyValue(keyStartS, m_tableMapping.getKeyType());
        }
        
        if (Const.isEmpty(m_meta.getKeyStopValue())) {
          s = new Scan(keyLowerBound);
        } else {
          byte[] keyUpperBound = null;
          String keyStopS = environmentSubstitute(m_meta.getKeyStopValue());
          convM = dateOrNumberConversionMaskForKey;
          
          if (m_tableMapping.getKeyType() == Mapping.KeyType.BINARY) {
            // assume we have a hex encoded string
            keyUpperBound = HBaseValueMeta.encodeKeyValue(keyStopS, m_tableMapping.getKeyType());
          } else if (m_tableMapping.getKeyType() != Mapping.KeyType.STRING) {
            
            // allow a conversion mask in the stop key field to override any specified for
            // the key in the user specified fields
            String[] parts = keyStopS.split("@");
            if (parts.length == 2) {
              keyStopS = parts[0];
              convM = parts[1];
            }
              
            if (!Const.isEmpty(convM) && convM.length() > 0) {
              if (m_tableMapping.getKeyType() == Mapping.KeyType.DATE ||
                  m_tableMapping.getKeyType() == Mapping.KeyType.UNSIGNED_DATE) {
                SimpleDateFormat sdf = new SimpleDateFormat();
                sdf.applyPattern(convM);
                try {
                  Date d = sdf.parse(keyStopS);
                  keyUpperBound = HBaseValueMeta.encodeKeyValue(d, m_tableMapping.getKeyType());
                } catch (ParseException e) {
                  throw new KettleException("Unable to parse upper bound key value \""
                      + keyStopS + "\"", e);
                }
              } else {
                // Number type
                // Double/Float or Long/Integer              
                DecimalFormat df = new DecimalFormat();
                df.applyPattern(convM);
                Number num = null;
                try {
                  num = df.parse(keyStopS);
                  keyUpperBound = HBaseValueMeta.encodeKeyValue(num, m_tableMapping.getKeyType());
                } catch (ParseException e) {
                  throw new KettleException("Unable to parse upper bound key value \""
                      + keyStopS + "\"", e);
                }
              }              
            } else {
              // just try it as a string
              keyUpperBound = HBaseValueMeta.
                encodeKeyValue(keyStopS, m_tableMapping.getKeyType());
            }
          } else {
            // it is a string
            keyUpperBound = HBaseValueMeta.
              encodeKeyValue(keyStopS, m_tableMapping.getKeyType());
          }
          s = new Scan(keyLowerBound, keyUpperBound);
        }  
      }
      
      // set any user-specified scanner caching
      if (!Const.isEmpty(m_meta.getScannerCacheSize())) {
        String temp = environmentSubstitute(m_meta.getScannerCacheSize());
        int sc = Integer.parseInt(temp);        
        s.setCaching(sc);
        logBasic("Set scanner caching to " + sc + " rows.");
      }
      
      // LIMIT THE SCAN TO JUST THE COLUMNS IN THE MAPPING
      // User-selected output columns?
      if (m_userOutputColumns != null && m_userOutputColumns.size() > 0) {
        for (HBaseValueMeta currentCol : m_userOutputColumns) {
          if (!currentCol.isKey()) {
            String colFamilyName = currentCol.getColumnFamily();
            String qualifier = currentCol.getColumnName();
            
            boolean binaryColName = false;
            if (qualifier.startsWith("@@@binary@@@")) {              
              qualifier = qualifier.replace("@@@binary@@@", "");
              binaryColName = true;
            }
            
            s.addColumn(Bytes.toBytes(colFamilyName), (binaryColName) ? 
                Bytes.toBytesBinary(qualifier) : Bytes.toBytes(qualifier));
          }
        }
      } else {
/*        // all the columns in the mapping
        Set<String> aliasSet = m_columnsMappedByAlias.keySet();

        for (String name : aliasSet) {
          HBaseValueMeta currentCol = m_columnsMappedByAlias.get(name);
          String colFamilyName = currentCol.getColumnFamily();
          String qualifier = currentCol.getColumnName();
          s.addColumn(Bytes.toBytes(colFamilyName), Bytes.toBytes(qualifier));
        } */
      }
      
                  
      // set any filters
      if (m_meta.getColumnFilters() != null && m_meta.getColumnFilters().size() > 0) {
        FilterList fl = null;
        if (m_meta.getMatchAnyFilter()) {
          fl = new FilterList(FilterList.Operator.MUST_PASS_ONE);
        } else {
          fl = new FilterList(FilterList.Operator.MUST_PASS_ALL);
        }
        
        for (ColumnFilter cf : m_meta.getColumnFilters()) {
          String fieldAliasS = environmentSubstitute(cf.getFieldAlias());
          HBaseValueMeta mappedCol = m_columnsMappedByAlias.get(fieldAliasS);
          if (mappedCol == null) {
            throw new KettleException("Column filter \"" + fieldAliasS 
                + "\" is not in the mapping!");
          }
          
          // check the type (if set in the ColumnFilter) against the type
          // of this field in the mapping
          String fieldTypeS = environmentSubstitute(cf.getFieldType());
          if (!Const.isEmpty(fieldTypeS)) {
            if (!mappedCol.getHBaseTypeDesc().equalsIgnoreCase(fieldTypeS)) {
              throw new KettleException("Type (" + fieldTypeS 
                  + ") of column filter for \""
                  + fieldAliasS + "\" does not match type specified " 
                  + "for this field in the mapping (" 
                  + mappedCol.getHBaseTypeDesc() + ")");
            }
          }
          
          CompareFilter.CompareOp comp = null;
          byte[] family = Bytes.toBytes(mappedCol.getColumnFamily());
          byte[] qualifier = Bytes.toBytes(mappedCol.getColumnName());
          ColumnFilter.ComparisonType op = cf.getComparisonOperator();
          
          switch (op) {
          case EQUAL:
            comp = CompareFilter.CompareOp.EQUAL;
            break;
          case NOT_EQUAL:
            comp = CompareFilter.CompareOp.NOT_EQUAL;
            break;
          case GREATER_THAN:
            comp = CompareFilter.CompareOp.GREATER;
            break;
          case GREATER_THAN_OR_EQUAL:
            comp = CompareFilter.CompareOp.GREATER_OR_EQUAL;
            break;
          case LESS_THAN:
            comp = CompareFilter.CompareOp.LESS;
            break;
          case LESS_THAN_OR_EQUAL:
            comp = CompareFilter.CompareOp.LESS_OR_EQUAL;
            break;            
          }
          
          String comparisonString = cf.getConstant().trim();
          comparisonString = environmentSubstitute(comparisonString);
          
          if (comp != null) {

            byte[] comparisonRaw = null;
            
            // do the numeric comparison stuff
            if (mappedCol.isNumeric()) {
              
              // Double/Float or Long/Integer              
              DecimalFormat df = new DecimalFormat();
              String formatS = environmentSubstitute(cf.getFormat());
              if (!Const.isEmpty(formatS)) {
                df.applyPattern(formatS);
              }
              
              Number num = null;
              try {
                num = df.parse(comparisonString);
              } catch (ParseException e) {
                throw new KettleException(e.getMessage(), e);
              }
              
              if (mappedCol.isInteger()) {
                if (!mappedCol.getIsLongOrDouble()) {               
                  comparisonRaw = Bytes.toBytes(num.intValue());
                } else {
                  comparisonRaw = Bytes.toBytes(num.longValue());
                }
              } else {
                if (!mappedCol.getIsLongOrDouble()) {
                  comparisonRaw = Bytes.toBytes(num.floatValue());
                } else {
                  comparisonRaw = Bytes.toBytes(num.doubleValue());
                }
              }                            

              if (!cf.getSignedComparison()) {
                //    comparisonRaw = Bytes.toBytes(compI);
                SingleColumnValueFilter scf = 
                  new SingleColumnValueFilter(family, qualifier, comp, comparisonRaw);
                scf.setFilterIfMissing(true);
                fl.addFilter(scf);
              } else {
                // custom comparator for signed comparison
                DeserializedNumericComparator comparator = null;
                if (mappedCol.isInteger()) {
                  if (mappedCol.getIsLongOrDouble()) {
                    comparator = new DeserializedNumericComparator(mappedCol.isInteger(),
                        mappedCol.getIsLongOrDouble(), num.longValue());
                  } else {
                    comparator = new DeserializedNumericComparator(mappedCol.isInteger(), 
                        mappedCol.getIsLongOrDouble(), num.intValue());
                  }
                } else {
                  if (mappedCol.getIsLongOrDouble()) {
                    comparator = new DeserializedNumericComparator(mappedCol.isInteger(), 
                        mappedCol.getIsLongOrDouble(), num.doubleValue());
                  } else {
                    comparator = new DeserializedNumericComparator(mappedCol.isInteger(), 
                        mappedCol.getIsLongOrDouble(), num.floatValue());
                  }
                }
                SingleColumnValueFilter scf = 
                  new SingleColumnValueFilter(family, qualifier, comp, comparator);
                scf.setFilterIfMissing(true);
                fl.addFilter(scf);                
              }        
            } else if (mappedCol.isDate()) {
              SimpleDateFormat sdf = new SimpleDateFormat();
              String formatS = environmentSubstitute(cf.getFormat());
              if (!Const.isEmpty(formatS)) {
                sdf.applyPattern(formatS);
              }
              Date d = null;
              try {
                d = sdf.parse(comparisonString);
              } catch (ParseException e) {
                throw new KettleException(e.getMessage(), e);
              }
              
              long dateAsMillis = d.getTime();
              if (!cf.getSignedComparison()) {
                comparisonRaw = Bytes.toBytes(dateAsMillis);
                
                SingleColumnValueFilter scf = 
                  new SingleColumnValueFilter(family, qualifier, comp, comparisonRaw);
                scf.setFilterIfMissing(true);
                fl.addFilter(scf);
              } else {
                // custom comparator for signed comparison
                DeserializedNumericComparator comparator = 
                  new DeserializedNumericComparator(true,
                    true, dateAsMillis);
                SingleColumnValueFilter scf = 
                  new SingleColumnValueFilter(family, qualifier, comp, comparator);
                scf.setFilterIfMissing(true);
                fl.addFilter(scf);
              }
            } else if (mappedCol.isBoolean()) {
              
              // temporarily encode it so that we can use the utility routine in HBaseValueMeta
              byte[] tempEncoded = Bytes.toBytes(comparisonString);
              Boolean decodedB = HBaseValueMeta.decodeBoolFromString(tempEncoded);
              // skip if we can't parse the comparison value
              if (decodedB == null) {
                continue;
              }
            
              // System.err.println("::::::::::::: Here " + decodedB);
              DeserializedBooleanComparator comparator = 
                new DeserializedBooleanComparator(decodedB.booleanValue());
              SingleColumnValueFilter scf = 
                new SingleColumnValueFilter(family, qualifier, comp, comparator);
              scf.setFilterIfMissing(true);
              fl.addFilter(scf);
            }
          } else {
            WritableByteArrayComparable comparator = null;
            comp = CompareFilter.CompareOp.EQUAL;
            if (cf.getComparisonOperator() == ColumnFilter.ComparisonType.SUBSTRING) {
              comparator = new SubstringComparator(comparisonString);
            } else {
              comparator = new RegexStringComparator(comparisonString);
            }
            
            SingleColumnValueFilter scf = 
              new SingleColumnValueFilter(family, qualifier, comp, comparator);
            scf.setFilterIfMissing(true);
            fl.addFilter(scf);
          }
        }
        
        if (fl != null && fl.getFilters().size() > 0) {
          s.setFilter(fl);
        }
      }
      
      try {
        m_sourceTable = new HTable(m_connection, m_tableMapping.getTableName());
        m_resultSet = m_sourceTable.getScanner(s); 
      } catch (IOException e) {
        throw new KettleException(e.getMessage(), e);
      }

      // set up the output fields (using the mapping)
      m_data.setOutputRowMeta(new RowMeta());
      m_meta.getFields(m_data.getOutputRowMeta(), getStepname(), null, null, this);
    }
    
    Result r = null;
    try {
      r = m_resultSet.next();
    } catch (IOException e) {
      throw new KettleException(e.getMessage(), e);
    }
    
    if (r == null) {
      m_resultSet.close();
      try {
        m_sourceTable.close();
      } catch (IOException e) {
        throw new KettleException("Problem closing connection to HBase table: " 
            + e.getMessage(), e);
      }
      setOutputDone();
      return false;
    }    
    
    int size = (m_userOutputColumns != null && m_userOutputColumns.size() > 0)
      ? m_userOutputColumns.size()
      : m_tableMapping.getMappedColumns().keySet().size() + 1; // + 1 for the key
    Object[] outputRowData = RowDataUtil.allocateRowData(size);     
    
    
    // User-selected output columns?
    if (m_userOutputColumns != null && m_userOutputColumns.size() > 0) {
      for (HBaseValueMeta currentCol : m_userOutputColumns) {
        if (currentCol.isKey()) {
          byte[] rawKey = r.getRow();
          Object decodedKey = HBaseValueMeta.decodeKeyValue(rawKey, m_tableMapping);
          int keyIndex = m_data.getOutputRowMeta().indexOfValue(currentCol.getAlias());
          outputRowData[keyIndex] = decodedKey;
        } else {
          String colFamilyName = currentCol.getColumnFamily();
          String qualifier = currentCol.getColumnName();
          
          System.out.println("Processing qualifier: " + qualifier);
          
          boolean binaryColName = false;
          if (qualifier.startsWith("@@@binary@@@")) {
            qualifier = qualifier.replace("@@@binary@@@", "");
            // assume hex encoded
            binaryColName = true;
          }

          KeyValue kv = r.getColumnLatest(Bytes.toBytes(colFamilyName), 
              (binaryColName) ? Bytes.toBytesBinary(qualifier) : Bytes.toBytes(qualifier));
          
          if (kv == null) {
            System.out.println("Unable to look up qualifier " + qualifier);
          }
          
          int outputIndex = m_data.getOutputRowMeta().indexOfValue(currentCol.getAlias());
          if (outputIndex < 0) {
            throw new KettleException("HBase column \"" + currentCol.getAlias() 
                + "\" doesn't seem to be defined in the output");
          }

          Object decodedVal = HBaseValueMeta.decodeColumnValue(kv, currentCol);      
          outputRowData[outputIndex] = decodedVal;
        }
      }
    } else {

      // all the columns in the mapping
      
      // do the key first
      byte[] rawKey = r.getRow();
      Object decodedKey = HBaseValueMeta.decodeKeyValue(rawKey, m_tableMapping);
      int keyIndex = m_data.getOutputRowMeta().indexOfValue(m_tableMapping.getKeyName());
      outputRowData[keyIndex] = decodedKey;
      
      Set<String> aliasSet = m_columnsMappedByAlias.keySet();

      for (String name : aliasSet) {
        HBaseValueMeta currentCol = m_columnsMappedByAlias.get(name);
        String colFamilyName = currentCol.getColumnFamily();
        String qualifier = currentCol.getColumnName();
        
        boolean binaryColName = false;
        if (qualifier.startsWith("@@@binary@@@")) {
          qualifier = qualifier.replace("@@@binary@@@", "");
          // assume hex encoded
          binaryColName = true;
        }

        KeyValue kv = r.getColumnLatest(Bytes.toBytes(colFamilyName), 
            (binaryColName) ? Bytes.toBytesBinary(qualifier) : Bytes.toBytes(qualifier));        
        
        int outputIndex = m_data.getOutputRowMeta().indexOfValue(name);
        if (outputIndex < 0) {
          throw new KettleException("HBase column \"" + name + "\" doesn't seem " +
          "to be defined in the output");
        }

        Object decodedVal = HBaseValueMeta.decodeColumnValue(kv, currentCol);      
        outputRowData[outputIndex] = decodedVal;
      }
    }
    
    // output the row
    putRow(m_data.getOutputRowMeta(), outputRowData);    
    return true;
  }
  
  // TODO handle error rows (see CsvInput). Unexpected lengths of values in columns
  // perhaps could be sent to the error stream somehow.
  
  // Do we need this??
  /*
  public boolean isWaitingForData() {
    return true;
  } */
  
  /* (non-Javadoc)
   * @see org.pentaho.di.trans.step.BaseStep#setStopped(boolean)
   */
  public void setStopped(boolean stopped) {
    if (isStopped() && stopped == true) {
      return;
    }
    super.setStopped(stopped);

    if (stopped) {
      if (m_resultSet != null) {
        logBasic("Closing connection...");
        m_resultSet.close();
        try {
          m_sourceTable.close();
        } catch (IOException e) {
          logError("A problem occurred while closing connection to HBase: " 
              + e.getMessage(), e);
        }
      }
    }
  }
  
}