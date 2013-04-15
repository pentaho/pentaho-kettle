package org.pentaho.di.trans;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.sql.ServiceCacheMethod;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

/**
 * This describes a (transformation) data service to the outside world.
 * It defines the name, picks the step to read from (or to write to), the caching method etc.
 *  
 * @author matt
 *
 */
public class DataServiceMeta {

  public static String XML_TAG = "data-service";

  public static final String DATA_SERVICE_NAME = "name";
  public static final String DATA_SERVICE_STEPNAME = "stepname";
  public static final String DATA_SERVICE_TRANSFORMATION_FILENAME = "transformation_filename";
  public static final String DATA_SERVICE_TRANSFORMATION_REP_PATH = "transformation_rep_path";
  public static final String DATA_SERVICE_TRANSFORMATION_REP_OBJECT_ID = "transformation_rep_object_id";
  public static final String DATA_SERVICE_CACHE_METHOD = "cache_method";
  public static final String DATA_SERVICE_CACHE_MAX_AGE_MINUTES = "cache_max_age_minutes";
  

  protected String name;

  protected String stepname;

  protected String transObjectId; // rep: by reference (1st priority)

  protected String transRepositoryPath; // rep: by name (2nd priority)

  protected String transFilename; // file (3rd priority)

  protected ObjectId objectId;

  protected ServiceCacheMethod cacheMethod;
  
  protected int cacheMaxAgeMinutes;

  public DataServiceMeta() {
    this(null, null, true, false, ServiceCacheMethod.None);
  }

  /**
   * @param name
   * @param stepname
   * @param output
   * @param optimisationAllowed
   */
  public DataServiceMeta(String name, String stepname, boolean output, boolean optimisationAllowed,
      ServiceCacheMethod cacheMethod) {
    this.name = name;
    this.stepname = stepname;
    this.cacheMethod = cacheMethod;
  }
  
  /**
   * Save this object to the metaStore
   * @param metaStore
   * @param attribute
   */
  public void saveToMetaStore(IMetaStore metaStore) throws MetaStoreException {
    DataServiceMetaStoreUtil.createOrUpdateDataServiceElement(metaStore, this);
  }
  
  public DataServiceMeta(IMetaStore metaStore, String dataServiceName) throws MetaStoreException {
    this();
    DataServiceMetaStoreUtil.loadDataService(metaStore, dataServiceName, this);
  }

  public boolean isDefined() {
    return !Const.isEmpty(name) && !Const.isEmpty(stepname);
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the stepname
   */
  public String getStepname() {
    return stepname;
  }

  /**
   * @param stepname the stepname to set
   */
  public void setStepname(String stepname) {
    this.stepname = stepname;
  }

  /**
   * @return the objectId
   */
  public ObjectId getObjectId() {
    return objectId;
  }

  /**
   * @param objectId the objectId to set
   */
  public void setObjectId(ObjectId objectId) {
    this.objectId = objectId;
  }

  /**
   * @return the cacheMethod
   */
  public ServiceCacheMethod getCacheMethod() {
    return cacheMethod;
  }

  /**
   * @param cacheMethod the cacheMethod to set
   */
  public void setCacheMethod(ServiceCacheMethod cacheMethod) {
    this.cacheMethod = cacheMethod;
  }

  public String getTransObjectId() {
    return transObjectId;
  }

  public void setTransObjectId(String transObjectId) {
    this.transObjectId = transObjectId;
  }

  public String getTransRepositoryPath() {
    return transRepositoryPath;
  }

  public void setTransRepositoryPath(String transRepositoryPath) {
    this.transRepositoryPath = transRepositoryPath;
  }

  public String getTransFilename() {
    return transFilename;
  }

  public void setTransFilename(String transFilename) {
    this.transFilename = transFilename;
  }

  public int getCacheMaxAgeMinutes() {
    return cacheMaxAgeMinutes;
  }

  public void setCacheMaxAgeMinutes(int cacheMaxAgeMinutes) {
    this.cacheMaxAgeMinutes = cacheMaxAgeMinutes;
  }
  
  
}
