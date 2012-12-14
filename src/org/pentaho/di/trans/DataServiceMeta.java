package org.pentaho.di.trans;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.repository.ObjectId;
import org.w3c.dom.Node;

/**
 * This describes a (transformation) data service to the outside world.
 * It defines the name, picks the step to read from (or to write to), the caching method etc.
 *  
 * @author matt
 *
 */
public class DataServiceMeta implements XMLInterface {
  
  public static String XML_TAG = "data-service";  

  private static final String PROPERTY_NAME = "name";
  private static final String PROPERTY_STEPNAME = "stepname";
  private static final String PROPERTY_OUTPUT = "output";
  private static final String PROPERTY_OPTIMISATION_ALLOWED = "optimisation_allowed";
  private static final String PROPERTY_CACHE_METHOD = "cache_method";

  protected String name;
  protected String stepname;
  protected boolean output;
  protected boolean optimizationAllowed;
  
  protected ObjectId objectId;
  
  protected ServiceCacheMethod cacheMethod;
  
  public DataServiceMeta() {
    this(null, null, true, false, ServiceCacheMethod.None);
  }
  
  /**
   * @param name
   * @param stepname
   * @param output
   * @param optimisationAllowed
   */
  public DataServiceMeta(String name, String stepname, boolean output, boolean optimisationAllowed, ServiceCacheMethod cacheMethod) {
    this.name = name;
    this.stepname = stepname;
    this.output = output;
    this.optimizationAllowed = optimisationAllowed;
    this.cacheMethod = cacheMethod;
  }
  
  public boolean isDefined() {
    return !Const.isEmpty(name) && !Const.isEmpty(stepname);
  }
  
  @Override
  public String getXML() throws KettleException {
    StringBuilder xml = new StringBuilder();
    
    xml.append(XMLHandler.openTag(XML_TAG));
    xml.append(XMLHandler.addTagValue(PROPERTY_NAME, name));
    xml.append(XMLHandler.addTagValue(PROPERTY_STEPNAME, stepname));
    xml.append(XMLHandler.addTagValue(PROPERTY_OUTPUT, output));
    xml.append(XMLHandler.addTagValue(PROPERTY_OPTIMISATION_ALLOWED, optimizationAllowed));
    xml.append(XMLHandler.addTagValue(PROPERTY_CACHE_METHOD, cacheMethod.name()));
    xml.append(XMLHandler.closeTag(XML_TAG));
    
    return xml.toString();
  }
  
  public DataServiceMeta(Node node) {
    name = XMLHandler.getTagValue(node, PROPERTY_NAME);
    stepname = XMLHandler.getTagValue(node, PROPERTY_STEPNAME);
    output = "Y".equalsIgnoreCase(XMLHandler.getTagValue(node, PROPERTY_OUTPUT));
    optimizationAllowed = "Y".equalsIgnoreCase(XMLHandler.getTagValue(node, PROPERTY_OPTIMISATION_ALLOWED));
    cacheMethod = ServiceCacheMethod.getMethodByName(XMLHandler.getTagValue(node, PROPERTY_CACHE_METHOD));
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
   * @return the output
   */
  public boolean isOutput() {
    return output;
  }
  /**
   * @param output the output to set
   */
  public void setOutput(boolean output) {
    this.output = output;
  }
  /**
   * @return the optimizationAllowed
   */
  public boolean isOptimizationAllowed() {
    return optimizationAllowed;
  }
  /**
   * @param optimizationAllowed the optimizationAllowed to set
   */
  public void setOptimizationAllowed(boolean optimizationAllowed) {
    this.optimizationAllowed = optimizationAllowed;
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
}
