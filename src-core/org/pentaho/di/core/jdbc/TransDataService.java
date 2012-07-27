package org.pentaho.di.core.jdbc;

import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.w3c.dom.Node;

/**
 * This class describes a transformation data service containing a name, a description as well as a transformation to give the data, the step from which to read.
 * Later we will also add additional information about this service like the database cache location for this service, retention period and so on.
 *  
 * @author matt
 *
 */
public class TransDataService implements XMLInterface {
  private String name;
  
  private String fileName;
  private String repositoryName;
  private String repositoryId;
  
  private String serviceStepName;

  public TransDataService() {
    this(null, null, null, null, null);
  }
  
  /**
   * @param name
   */
  public TransDataService(String name) {
    this(name, null, null, null, null);
  }

  /**
   * @param name
   * @param fileName
   * @param repositoryName
   * @param repositoryId
   * @param serviceStepName
   */
  public TransDataService(String name, String fileName, String repositoryName, String repositoryId, String serviceStepName) {
    this.name = name;
    this.fileName = fileName;
    this.repositoryName = repositoryName;
    this.repositoryId = repositoryId;
    this.serviceStepName = serviceStepName;
  }

  public TransDataService(Node serviceNode) {
    this.name = XMLHandler.getTagValue(serviceNode, "name");
    this.fileName = XMLHandler.getTagValue(serviceNode, "filename");
    this.repositoryName = XMLHandler.getTagValue(serviceNode, "repository_name");
    this.repositoryId = XMLHandler.getTagValue(serviceNode, "repository_object_id");
    this.serviceStepName = XMLHandler.getTagValue(serviceNode, "service_step");
  }


  public String getXML() {
    StringBuilder xml = new StringBuilder();
    xml.append(XMLHandler.addTagValue("name", name));
    xml.append(XMLHandler.addTagValue("filename", fileName));
    xml.append(XMLHandler.addTagValue("repository_name", repositoryName));
    xml.append(XMLHandler.addTagValue("repository_object_id", repositoryId));
    xml.append(XMLHandler.addTagValue("service_step", serviceStepName));
    return xml.toString();
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
   * @return the fileName
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * @param fileName the fileName to set
   */
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  /**
   * @return the repositoryName
   */
  public String getRepositoryName() {
    return repositoryName;
  }

  /**
   * @param repositoryName the repositoryName to set
   */
  public void setRepositoryName(String repositoryName) {
    this.repositoryName = repositoryName;
  }

  /**
   * @return the repositoryId
   */
  public String getRepositoryId() {
    return repositoryId;
  }

  /**
   * @param repositoryId the repositoryId to set
   */
  public void setRepositoryId(String repositoryId) {
    this.repositoryId = repositoryId;
  }

  /**
   * @return the serviceStepName
   */
  public String getServiceStepName() {
    return serviceStepName;
  }

  /**
   * @param serviceStepName the serviceStepName to set
   */
  public void setServiceStepName(String serviceStepName) {
    this.serviceStepName = serviceStepName;
  }
  
  
}
