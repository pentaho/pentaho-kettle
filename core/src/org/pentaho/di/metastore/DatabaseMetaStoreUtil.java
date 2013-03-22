package org.pentaho.di.metastore;

import java.util.Enumeration;
import java.util.Properties;

import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

public class DatabaseMetaStoreUtil {
  
  public static IMetaStoreElementType populateDatabaseElementType(IMetaStore metaStore) throws MetaStoreException {
    
    // The new type will typically have an ID so all we need to do is give the type a name and a description.
    //
    IMetaStoreElementType elementType = metaStore.newElementType(MetaStoreConst.PENTAHO_NAMESPACE);
    
    // If we didn't get an ID, provide one
    //
    if (elementType.getId()==null) {
      elementType.setId(MetaStoreConst.DATABASE_TYPE_NAME);
    }
    
    // Name and description...
    //
    elementType.setName(MetaStoreConst.DATABASE_TYPE_NAME);
    elementType.setDescription(MetaStoreConst.DATABASE_TYPE_DESCRIPTION);
    return elementType;
  }
  
  public static IMetaStoreElement populateDatabaseElement(IMetaStore metaStore, DatabaseMeta databaseMeta) throws MetaStoreException {
    // If the data type doesn't exist, create it...
    //
    IMetaStoreElementType elementType = metaStore.getElementTypeByName(MetaStoreConst.PENTAHO_NAMESPACE, MetaStoreConst.DATABASE_TYPE_NAME);
    if (elementType==null) {
      elementType = populateDatabaseElementType(metaStore);
    }
    
    // generate a new database element and populate it with metadata
    //
    IMetaStoreElement element = metaStore.newElement(elementType, databaseMeta.getName(), null);

    element.addChild(metaStore.newAttribute(MetaStoreConst.DB_ATTR_ID_PLUGIN_ID, databaseMeta.getPluginId()));
    
    if (element.getId()==null) {
      element.setId(databaseMeta.getName());
    }
    element.setName(databaseMeta.getName());
    
    element.addChild(metaStore.newAttribute(MetaStoreConst.DB_ATTR_ID_DESCRIPTION, databaseMeta.getDescription()));
    element.addChild(metaStore.newAttribute(MetaStoreConst.DB_ATTR_ID_ACCESS_TYPE, databaseMeta.getAccessTypeDesc()));
    element.addChild(metaStore.newAttribute(MetaStoreConst.DB_ATTR_ID_HOSTNAME, databaseMeta.getHostname()));
    element.addChild(metaStore.newAttribute(MetaStoreConst.DB_ATTR_ID_PORT, databaseMeta.getDatabasePortNumberString()));
    element.addChild(metaStore.newAttribute(MetaStoreConst.DB_ATTR_ID_DATABASE_NAME, databaseMeta.getDatabaseName()));
    element.addChild(metaStore.newAttribute(MetaStoreConst.DB_ATTR_ID_USERNAME, databaseMeta.getUsername()));
    element.addChild(metaStore.newAttribute(MetaStoreConst.DB_ATTR_ID_PASSWORD, Encr.encryptPasswordIfNotUsingVariables(databaseMeta.getUsername())));
    element.addChild(metaStore.newAttribute(MetaStoreConst.DB_ATTR_ID_SERVERNAME, databaseMeta.getServername()));
    element.addChild(metaStore.newAttribute(MetaStoreConst.DB_ATTR_ID_DATA_TABLESPACE, databaseMeta.getDataTablespace()));
    element.addChild(metaStore.newAttribute(MetaStoreConst.DB_ATTR_ID_INDEX_TABLESPACE, databaseMeta.getIndexTablespace()));
    
    IMetaStoreAttribute attributesChild = metaStore.newAttribute(MetaStoreConst.DB_ATTR_ID_ATTRIBUTES, null);
    element.addChild(attributesChild);
    
    // Now add a list of all the attributes set on the database connection...
    // 
    Properties attributes = databaseMeta.getAttributes();
    Enumeration<Object> keys = databaseMeta.getAttributes().keys();
    while (keys.hasMoreElements()) {
      String code = (String) keys.nextElement();
      String attribute = (String)attributes.get(code);
      // Add it to the attributes child
      //
      attributesChild.addChild(metaStore.newAttribute(code, attribute));
    }
            
    return element;
  }

  public static DatabaseMeta loadDatabaseMetaFromDatabaseElement(IMetaStoreElement element) throws KettlePluginException {
    DatabaseMeta databaseMeta = new DatabaseMeta();

    // Load the appropriate database plugin (database interface)
    //
    String pluginId = getChildString(element, MetaStoreConst.DB_ATTR_ID_PLUGIN_ID);
    PluginInterface plugin = PluginRegistry.getInstance().getPlugin(DatabasePluginType.class, pluginId);
    DatabaseInterface databaseInterface = (DatabaseInterface) PluginRegistry.getInstance().loadClass(plugin);
    databaseMeta.setDatabaseInterface(databaseInterface);

    databaseMeta.setObjectId(new StringObjectId(element.getId()));
    databaseMeta.setName(element.getName());
    databaseMeta.setDescription(getChildString(element, MetaStoreConst.DB_ATTR_ID_DESCRIPTION));

    String accessTypeString = getChildString(element, MetaStoreConst.DB_ATTR_ID_ACCESS_TYPE);
    databaseMeta.setAccessType(DatabaseMeta.getAccessType(accessTypeString));

    databaseMeta.setHostname(getChildString(element, MetaStoreConst.DB_ATTR_ID_HOSTNAME));
    databaseMeta.setDBPort(getChildString(element, MetaStoreConst.DB_ATTR_ID_PORT));
    databaseMeta.setDBName(getChildString(element, MetaStoreConst.DB_ATTR_ID_DATABASE_NAME));
    databaseMeta.setUsername(getChildString(element, MetaStoreConst.DB_ATTR_ID_USERNAME));
    databaseMeta.setPassword(Encr.decryptPasswordOptionallyEncrypted(getChildString(element, MetaStoreConst.DB_ATTR_ID_PASSWORD)));
    databaseMeta.setServername(getChildString(element, MetaStoreConst.DB_ATTR_ID_SERVERNAME));
    databaseMeta.setDataTablespace(getChildString(element, MetaStoreConst.DB_ATTR_ID_DATA_TABLESPACE));
    databaseMeta.setIndexTablespace(getChildString(element, MetaStoreConst.DB_ATTR_ID_INDEX_TABLESPACE));

    IMetaStoreAttribute attributesChild = element.getChild(MetaStoreConst.DB_ATTR_ID_ATTRIBUTES);
    if (attributesChild!=null) {
      // Now add a list of all the attributes set on the database connection...
      // 
      Properties attributes = databaseMeta.getAttributes();
      for (IMetaStoreAttribute attr : attributesChild.getChildren()) {
        String code = attr.getId();
        String value = getAttributeString(attr);
        attributes.put(code,  value);
      }
    }
    
    return databaseMeta;
  }
  
  public static String getChildString(IMetaStoreAttribute attribute, String id) {
    IMetaStoreAttribute child = attribute.getChild(id);
    if (child==null) {
      return null;
    }
    
    return getAttributeString(child);
  }
  
  public static String getAttributeString(IMetaStoreAttribute attribute) {
    if (attribute.getValue()==null) {
      return null;
    }
    return attribute.getValue().toString();
  }
}
  