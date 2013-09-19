package org.pentaho.di.metastore;

import java.io.File;

import org.pentaho.di.core.Const;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.stores.xml.XmlMetaStore;

public class MetaStoreConst {

  public static final String DB_ATTR_ID_DESCRIPTION = "description";
  public static final String DB_ATTR_ID_PLUGIN_ID = "plugin_id";
  public static final String DB_ATTR_ID_ACCESS_TYPE = "access_type";
  public static final String DB_ATTR_ID_HOSTNAME = "hostname";
  public static final String DB_ATTR_ID_PORT = "port";
  public static final String DB_ATTR_ID_DATABASE_NAME = "database_name";
  public static final String DB_ATTR_ID_USERNAME = "username";
  public static final String DB_ATTR_ID_PASSWORD = "password";
  public static final String DB_ATTR_ID_SERVERNAME = "servername";
  public static final String DB_ATTR_ID_DATA_TABLESPACE = "data_tablespace";
  public static final String DB_ATTR_ID_INDEX_TABLESPACE = "index_tablespace";
  
  // Extra information for 3rd party tools, not used by Kettle
  //
  public static final String DB_ATTR_DRIVER_CLASS = "driver_class";
  public static final String DB_ATTR_JDBC_URL = "jdbc_url";
  
  public static final String DB_ATTR_ID_ATTRIBUTES = "attributes";

  public static final String getDefaultPentahoMetaStoreLocation() {
    return System.getProperty("user.home")+File.separator+".pentaho";
  }
  
  public static IMetaStore openLocalPentahoMetaStore() throws MetaStoreException {
    String rootFolder = System.getProperty(Const.PENTAHO_METASTORE_FOLDER);
    if (Const.isEmpty(rootFolder)) {
      rootFolder = getDefaultPentahoMetaStoreLocation();
    }
    File rootFolderFile = new File(rootFolder);
    if (!rootFolderFile.exists()) {
      rootFolderFile.mkdirs();
    }
    XmlMetaStore metaStore = new XmlMetaStore(rootFolder);
    metaStore.setName(Const.PENTAHO_METASTORE_NAME);
    return metaStore;
  }
}
