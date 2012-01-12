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

package org.pentaho.hbase.mapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Class encapsulating meta data on a table in HBase. A mapping
 * consists of meta data on the key of the table and a subset of
 * the columns. Since the key has no name in HBase, the user is
 * required to provide a name/alias. Keys may be of type String,
 * Integer, Long or Date. Integer, long and date may further be
 * defined to be unsigned or signed. Keys in HBase (like everything
 * else) are just stored as arrays of bytes and are ordered 
 * lexicographically as such. Since two's complement is used to
 * represent integers/longs it is necessary to flip the sign bit *before*
 * storing the key in order to ensure that negative numbers sort before
 * positive ones in two's complement. Of course this has to be reversed
 * when reading key values.<p>
 * 
 * Columns in HBase are uniquely identified by their name (qualifier)
 * and column family that they belong to. The user must supply these
 * two bits of information for a column to be mapped. An alias may
 * optionally be supplied for a column. Columns may be of type
 * String, Integer, Long, Float, Double, Date, Boolean, BigNumber,
 * Serializable and Binary. This is nearly the same as Kettle's
 * ValueMeta set of types, with the exception that we make the
 * distinction between integer/long and float/double (see
 * HBaseValueMeta). While this distinction doesn't matter for reading 
 * from HBase it is necessary for consistency sake when writing 
 * to a HBase table that perhaps was not created/loaded by Kettle. 
 * Boolean is decoded by trying as a string first (e.g T, F, YES, NO etc.)
 * and then, if that fails, as a number. BigNumber is decoded by 
 * trying to parse from a string first and then, if that fails, by
 * deserializing as a BigNumber object. 
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 *
 */
public class Mapping {
  protected String m_tableName;
  protected String m_mappingName;
  
  protected String m_keyName;
  
  public enum KeyType {
    // both date types are longs - Date uses sign bit flipping to ensure correct sort
    // order when there are "negative" dates (i.e. dates prior to the epoch). Unsigned
    // date uses a raw long (makes the assumption that all dates in use are positive, i.e
    // >= the epoch)
    STRING("String"), INTEGER("Integer"), UNSIGNED_INTEGER("UnsignedInteger"), 
    LONG("Long"), UNSIGNED_LONG("UnsignedLong"), 
    DATE("Date"), UNSIGNED_DATE("UnsignedDate");    
    
    private final String m_stringVal;
    
    KeyType(String name) {
      m_stringVal = name;
    }
    
    public String toString() {
      return m_stringVal;
    }
  }
  
  protected KeyType m_keyType = KeyType.STRING;
  
  /** Fast lookup by alias */
  protected Map<String, HBaseValueMeta> m_mappedColumnsByAlias = 
    new HashMap<String, HBaseValueMeta>();
  
  /** Fast lookup by column family,column name */
  protected Map<String, HBaseValueMeta> m_mappedColumnsByFamilyCol = 
    new HashMap<String, HBaseValueMeta>();
  
  /**
   * Constructor.
   * 
   * @param tableName the name of the table that this mapping applies
   * to
   * @param mappingName the name of the mapping
   */
  public Mapping(String tableName, String mappingName) {
    this(tableName, mappingName, null, null);
  }
  
  /**
   * Constructor.
   * 
   * @param tableName the name of the table that this mapping applies
   * to
   * @param mappingName the name of the mapping
   * @param keyName the name for the key
   * @param keyType the type of the key
   */
  public Mapping(String tableName, String mappingName, 
      String keyName, KeyType keyType) {
    
    m_tableName = tableName;
    m_mappingName = mappingName;

    m_keyName = keyName;
    m_keyType = keyType;
  }
  
  /*public Mapping(String tableName, String mappingName, 
      String keyName, KeyType keyType, Map<String, HBaseValueMeta> cols) {
    this(tableName, mappingName, keyName, keyType);
    
    setMappedColumns(cols);
  }*/
  
  /**
   * Add a column to this mapping
   * 
   * @param column the column to add
   * @return the alias for this column (this may be different than
   * the alias set in the column passed in if that one already exists
   * in the mapping).
   * @throws Exception if the family, qualifier pair (which uniquely
   * identifieds a column) already exists in this mapping. 
   */
  public String addMappedColumn(HBaseValueMeta column) throws Exception {
    
    // each <column family,column name> tuple can only be in the
    // mapping once!
    if (m_mappedColumnsByFamilyCol.get(column.getColumnFamily() 
        + "," + column.getColumnName()) != null) {
      throw new Exception("\"" + column.getColumnFamily() + "," 
          + column.getColumnName() + "\" is already mapped in mapping \"" 
          + m_mappingName + "\"");
    }
    
    m_mappedColumnsByFamilyCol.put(column.getColumnFamily() 
        + "," + column.getColumnName(), column);
    
    String alias = column.getAlias();
    
    // automatically adjust alias if it already exists
    if (m_mappedColumnsByAlias.get(alias) != null) {
      // this alias is already in use
      if (alias.lastIndexOf('_') <= 0) {
        alias += "_1";
      } else {
        // try to parse whatever comes after as an integer
        String tail = alias.substring(alias.lastIndexOf('_') + 1, alias.length());
        try {
          int copy = Integer.parseInt(tail);
          copy++;
          alias = alias.substring(0, alias.lastIndexOf('_') + 1);
          alias += "" + copy;
        } catch (NumberFormatException e) {
          // just append a new underscored number
          alias += "_1";
        }
      }
      
      column.setAlias(alias);
    }
    
    m_mappedColumnsByAlias.put(alias, column);
    
    return alias;
  }  
  
  /**
   * Set the name of the table that this mapping applies to
   * 
   * @param tableName the name of the table that backs this mapping
   */
  public void setTableName(String tableName) {
    m_tableName = tableName;
  }
  
  /**
   * Get the name of the table that backs this mapping
   * 
   * @return the name of the table that backs thsi mapping
   */
  public String getTableName() {
    return m_tableName;
  }
  
  /**
   * Set the name of this mapping
   * 
   * @param mappingName the name of this mapping
   */
  public void setMappingName(String mappingName) {
    m_mappingName = mappingName;
  }
  
  /**
   * Get the name of this mapping
   * 
   * @return the name of this mapping
   */
  public String getMappingName() {
    return m_mappingName;
  }
  
  /**
   * Set the name to use for the key of the table backed by this
   * mapping.
   * 
   * @param keyName the name to use for the key.
   */
  public void setKeyName(String keyName) {
    m_keyName = keyName;
  }
  
  /**
   * Get the name that this mapping uses for the key of
   * table backed by the mapping.
   * 
   * @return the name of the key.
   */
  public String getKeyName() {
    return m_keyName;
  }
  
  /**
   * Set the type for the key
   * 
   * @param type the type of the key.
   */
  public void setKeyType(KeyType type) {
    m_keyType = type;
  }
  
  /**
   * Set the type of the key as a string
   * 
   * @param type the type of the key as a string
   * @throws Exception if the type is unknown
   */
  public void setKeyTypeAsString(String type) throws Exception {
    boolean found = false;
    for (KeyType k : KeyType.values()) {
      if (k.toString().equalsIgnoreCase(type)) {
        m_keyType = k;
        found = true;
        break;
      }
    }
    
    if (!found) {
      throw new Exception("Unknown key type: " + type);
    }
  }
  
  /**
   * Get the type of the key
   * 
   * @return the type of the key
   */
  public KeyType getKeyType() {
    return m_keyType;
  }  
  
  /**
   * Set the columns mapped by this mapping
   * 
   * @param cols a Map of column information
   */
  public void setMappedColumns(Map<String, HBaseValueMeta> cols) {
    m_mappedColumnsByAlias = cols;
  }
  
  /**
   * Get the columns mapped by this mapping
   * 
   * @return a Map, keyed by alias, of the columns mapped by this
   * mapping.
   */
  public Map<String, HBaseValueMeta> getMappedColumns() {
    return m_mappedColumnsByAlias;
  }
  
  /**
   * Returns a textual description of this mapping
   * 
   * @return a textual description of this mapping
   */
  public String toString() {
    Set<String> aliases = m_mappedColumnsByAlias.keySet();
    boolean first = true;
    StringBuffer result = new StringBuffer();
    result.append("Mapping \"" + getMappingName() + "\" on table \""
        + getTableName() + "\":\n\n");
    
    result.append("\tKEY (" + getKeyName() + "): " + getKeyType().toString());
    result.append("\n\n");
    
    if (aliases.size() > 0) {
      for (String alias : aliases) {
        HBaseValueMeta vm = m_mappedColumnsByAlias.get(alias);
        if (first) {
          
          first = false;
        }
        
        result.append("\t\"" + alias + "\" (" + vm.getColumnFamily() + 
            HBaseValueMeta.SEPARATOR + vm.getColumnName() + "): ");
        if (vm.getStorageType() == ValueMetaInterface.STORAGE_TYPE_INDEXED) {
          Object[] labels = vm.getIndex();
          result.append("{");
          for (int i = 0; i < labels.length; i++) {
            if (i == labels.length - 1) {
              result.append(labels[i].toString().trim()).append("}\n");
            } else {
              result.append(labels[i].toString().trim()).append(",");
            }
          }
        } else {
          result.append(ValueMetaInterface.typeCodes[vm.getType()]).append("\n");
        }
      }
    }
    
    return result.toString();
  }
}
