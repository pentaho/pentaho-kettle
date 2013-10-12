/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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
    if ( cache == null ) {
      cache = new TransDataCache();
    }
    return cache;
  }

  private TransDataCache() {
    rowMetaMap = new HashMap<String, RowMetaInterface>();
    rowDataMap = new HashMap<String, List<Object[]>>();
  }

  public void store( String serviceName, RowMetaInterface rowMeta, List<Object[]> rowData ) {
    rowMetaMap.put( serviceName, rowMeta );
    rowDataMap.put( serviceName, rowData );
  }

  public RowMetaInterface retrieveRowMeta( String serviceName ) {
    return rowMetaMap.get( serviceName );
  }

  public List<Object[]> retrieveRowData( String serviceName ) {
    return rowDataMap.get( serviceName );
  }

  public void remove( String serviceName ) {
    rowMetaMap.remove( serviceName );
    rowDataMap.remove( serviceName );
  }
}
