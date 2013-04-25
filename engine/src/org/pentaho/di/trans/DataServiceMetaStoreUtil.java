package org.pentaho.di.trans;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.sql.ServiceCacheMethod;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.exceptions.MetaStoreNamespaceExistsException;
import org.pentaho.metastore.util.MetaStoreUtil;
import org.pentaho.metastore.util.PentahoDefaults;
import org.pentaho.pms.util.Const;

public class DataServiceMetaStoreUtil extends MetaStoreUtil {
  
  private static String namespace = PentahoDefaults.NAMESPACE;
 
  /**
   * This method creates a new Element Type in the meta store corresponding encapsulating the Kettle Data Service meta data. 
   * @param metaStore The store to create the element type in
   * @return The existing or new element type
   * @throws MetaStoreException 
   * @throws MetaStoreNamespaceExistsException 
   */
  public static IMetaStoreElementType createDataServiceElementTypeIfNeeded(IMetaStore metaStore) throws MetaStoreException {
    
    if (!metaStore.namespaceExists(namespace)) {
      metaStore.createNamespace(namespace);
    }
    
    IMetaStoreElementType elementType = metaStore.getElementType(namespace, PentahoDefaults.KETTLE_DATA_SERVICE_ELEMENT_TYPE_NAME);
    if (elementType==null) {
      elementType = metaStore.newElementType(namespace);
      elementType.setName(PentahoDefaults.KETTLE_DATA_SERVICE_ELEMENT_TYPE_NAME);
      elementType.setDescription(PentahoDefaults.KETTLE_DATA_SERVICE_ELEMENT_TYPE_DESCRIPTION);
      metaStore.createElementType(namespace, elementType);
    }
    
    return elementType;
  }
  
  public static IMetaStoreElement createOrUpdateDataServiceElement(IMetaStore metaStore, DataServiceMeta dataServiceMeta) throws MetaStoreException {
    
    IMetaStoreElementType elementType = createDataServiceElementTypeIfNeeded(metaStore);
    
    IMetaStoreElement oldElement = metaStore.getElementByName(namespace, elementType, dataServiceMeta.getName());
    IMetaStoreElement element = metaStore.newElement(); 
    populateDataServiceElement(metaStore, element, dataServiceMeta);

    if (oldElement==null) {
      metaStore.createElement(namespace, elementType, element);
    } else {
      metaStore.updateElement(namespace, elementType, oldElement.getId(), element);
    }
    
    return element;
  }

  private static void populateDataServiceElement(IMetaStore metaStore, IMetaStoreElement element, DataServiceMeta dataServiceMeta) throws MetaStoreException {
    element.setName(dataServiceMeta.getName());
    element.addChild(metaStore.newAttribute(DataServiceMeta.DATA_SERVICE_STEPNAME, dataServiceMeta.getStepname()));
    element.addChild(metaStore.newAttribute(DataServiceMeta.DATA_SERVICE_TRANSFORMATION_FILENAME, dataServiceMeta.getTransFilename()));
    element.addChild(metaStore.newAttribute(DataServiceMeta.DATA_SERVICE_TRANSFORMATION_REP_PATH, dataServiceMeta.getTransRepositoryPath()));
    element.addChild(metaStore.newAttribute(DataServiceMeta.DATA_SERVICE_TRANSFORMATION_REP_OBJECT_ID, dataServiceMeta.getTransObjectId()));
    element.addChild(metaStore.newAttribute(DataServiceMeta.DATA_SERVICE_TRANSFORMATION_REP_OBJECT_ID, dataServiceMeta.getTransObjectId()));
    
  }

  public static DataServiceMeta loadDataService(IMetaStore metaStore, String dataServiceName, DataServiceMeta meta) throws MetaStoreException {
    IMetaStoreElementType elementType = createDataServiceElementTypeIfNeeded(metaStore);
    IMetaStoreElement element = metaStore.getElementByName(namespace, elementType, dataServiceName);
    if (element!=null) {
      return elementToDataService(element, meta);
    } 
    
    throw new MetaStoreException("Data service '"+dataServiceName+"' could not be found");
 }
  
  private static DataServiceMeta elementToDataService(IMetaStoreElement element) {
    DataServiceMeta dataService = new DataServiceMeta();
    return elementToDataService(element, dataService);
  }
  
  private static DataServiceMeta elementToDataService(IMetaStoreElement element, DataServiceMeta meta) {
    meta.setName(element.getName());
    meta.setStepname(getChildString(element, DataServiceMeta.DATA_SERVICE_STEPNAME));
    meta.setTransFilename(getChildString(element, DataServiceMeta.DATA_SERVICE_TRANSFORMATION_FILENAME));
    meta.setTransRepositoryPath(getChildString(element, DataServiceMeta.DATA_SERVICE_TRANSFORMATION_REP_PATH));
    String transObjectIdString = getChildString(element, DataServiceMeta.DATA_SERVICE_TRANSFORMATION_REP_OBJECT_ID);
    meta.setTransObjectId(Const.isEmpty(transObjectIdString) ? null : new StringObjectId(transObjectIdString));
    meta.setCacheMaxAgeMinutes(Const.toInt(getChildString(element, DataServiceMeta.DATA_SERVICE_CACHE_MAX_AGE_MINUTES), 0));
    meta.setCacheMethod(ServiceCacheMethod.getMethodByName(getChildString(element, DataServiceMeta.DATA_SERVICE_CACHE_METHOD)));
    return meta;
  }

  public static List<DataServiceMeta> getDataServices(IMetaStore metaStore) throws MetaStoreException {
    IMetaStoreElementType elementType = createDataServiceElementTypeIfNeeded(metaStore);
    List<DataServiceMeta> dataServices = new ArrayList<DataServiceMeta>();
    for (IMetaStoreElement element : metaStore.getElements(namespace,  elementType)) {
      dataServices.add(elementToDataService(element));
    }
    return dataServices;
  }
  
}
