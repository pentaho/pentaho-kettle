package org.pentaho.di.trans.sql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.row.RowMetaInterface;

public class TransDataCache {
  
  private Map<String, RowMetaInterface> rowMetaMap;
  private Map<String, List<Object[]>> rowDataMap;
  
  private static TransDataCache cache;
  
  public static TransDataCache getInstance() {
    if (cache==null) {
      cache = new TransDataCache();
    }
    return cache; 
  }
  
  private TransDataCache() {
    rowMetaMap = new HashMap<String, RowMetaInterface>();
    rowDataMap = new HashMap<String, List<Object[]>>();
  }

  public void store(String serviceName, RowMetaInterface rowMeta, List<Object[]> rowData) {
    rowMetaMap.put(serviceName, rowMeta);
    rowDataMap.put(serviceName, rowData);
  }
  
  public RowMetaInterface retrieveRowMeta(String serviceName) {
    return rowMetaMap.get(serviceName);
  }
  
  public List<Object[]> retrieveRowData(String serviceName) {
    return rowDataMap.get(serviceName);
  }
  
  public void remove(String serviceName) {
    rowMetaMap.remove(serviceName);
    rowDataMap.remove(serviceName);
  }
}
