package org.pentaho.database.util;

import java.util.Properties;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.di.core.database.BaseDatabaseMeta;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.PartitionDatabaseMeta;

public class DatabaseUtil {
  
  public static DatabaseMeta convertToDatabaseMeta(IDatabaseConnection conn) {

    DatabaseMeta meta = new DatabaseMeta();

    Properties props = new Properties();
    
    for (String key : conn.getExtraOptions().keySet()) {
      props.put(BaseDatabaseMeta.ATTRIBUTE_PREFIX_EXTRA_OPTION + key, conn.getExtraOptions().get(key));
    }

    for (String key : conn.getConnectionPoolingProperties().keySet()) {
      props.put(BaseDatabaseMeta.ATTRIBUTE_POOLING_PARAMETER_PREFIX + key, conn.getConnectionPoolingProperties().get(key));
    }
    
    for (String key : conn.getAttributes().keySet()) {
      props.put(key, conn.getAttributes().get(key));
    }
    
    meta.setAttributes(props);

    meta.setDatabaseType(conn.getDatabaseType().getShortName());
    meta.setName(conn.getName());
    meta.setAccessType(conn.getAccessType().ordinal());
    meta.setHostname(conn.getHostname());
    meta.setDBName(conn.getDatabaseName());
    meta.setDBPort(conn.getDatabasePort());
    meta.setUsername(conn.getUsername());
    meta.setPassword(conn.getPassword());

    
    meta.setServername(conn.getInformixServername());
    meta.setDataTablespace(conn.getDataTablespace());
    meta.setIndexTablespace(conn.getIndexTablespace());
    
    // addl
    if (conn.getConnectSql() != null) {
      meta.setConnectSQL(conn.getConnectSql());
    }
    
    meta.setInitialPoolSize(conn.getInitialPoolSize());
    meta.setMaximumPoolSize(conn.getMaximumPoolSize());
    
    if (conn.getSqlServerInstance() != null) {
      meta.setSQLServerInstance(conn.getSqlServerInstance());
    }
    meta.setForcingIdentifiersToLowerCase(conn.isForcingIdentifiersToLowerCase());
    meta.setForcingIdentifiersToUpperCase(conn.isForcingIdentifiersToUpperCase());
    meta.setPartitioned(conn.isPartitioned());
    meta.setQuoteAllFields(conn.isQuoteAllFields());
    meta.setStreamingResults(conn.isStreamingResults());
    meta.setUsingConnectionPool(conn.isUsingConnectionPool());
    meta.setUsingDoubleDecimalAsSchemaTableSeparator(conn.isUsingDoubleDecimalAsSchemaTableSeparator());
    
    if (conn.getPartitioningInformation() != null) {
      PartitionDatabaseMeta pdmetas[] = new PartitionDatabaseMeta[conn.getPartitioningInformation().size()];
      
      //TODO
      int c = 0;
      for (org.pentaho.database.model.PartitionDatabaseMeta pdmeta : conn.getPartitioningInformation()) {
        pdmetas[c++] = convertToPartitionDatabaseMeta(pdmeta);
      }
      meta.setPartitioningInformation(pdmetas);
    }
      
    
    if (conn.getChanged()) {
      meta.setChanged();
    }
    
    return meta;
  }
  
  public static PartitionDatabaseMeta convertToPartitionDatabaseMeta(org.pentaho.database.model.PartitionDatabaseMeta pdm) {
    PartitionDatabaseMeta pdmeta = new PartitionDatabaseMeta();
    pdmeta.setDatabaseName(pdm.getDatabaseName());
    pdmeta.setHostname(pdm.getHostname());
    pdmeta.setPartitionId(pdm.getPartitionId());
    pdmeta.setPassword(pdm.getPassword());
    pdmeta.setPort(pdm.getPort());
    pdmeta.setUsername(pdm.getUsername());
    return pdmeta;
  }
}
