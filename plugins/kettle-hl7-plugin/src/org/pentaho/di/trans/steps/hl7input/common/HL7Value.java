package org.pentaho.di.trans.steps.hl7input.common;

import org.pentaho.di.core.Const;

public class HL7Value {
  private String version;
  private String parentGroup;
  private String groupName;
  private String structureName;
  private String structureNumber;
  private String fieldName;
  private String coordinate;
  private String dataType;
  private String description;
  
  private String value;

  /**
   * @param version
   * @param parentGroup
   * @param groupName
   * @param structureName
   * @param fieldName
   * @param dataType
   * @param description
   * @param value
   */
  public HL7Value(String version, String parentGroup, String groupName, String structureName, String structureNumber, String fieldName, String coordinate, String dataType, String description, String value) {
    this.version = version;
    this.parentGroup = parentGroup;
    this.groupName = groupName;
    this.structureName = structureName;
    this.structureNumber = structureNumber;
    this.fieldName = fieldName;
    this.coordinate = coordinate;
    this.dataType = dataType;
    this.description = description;
    this.value = value;
  }
  
  @Override
  public String toString() {
    return version+" ("+parentGroup+") "+groupName+" "+structureName+" "+structureNumber+" : "+fieldName+" "+coordinate+" ("+dataType+" "+description+") : "+Const.NVL(value, "");
  }

  /**
   * @return the version
   */
  public String getVersion() {
    return version;
  }

  /**
   * @param version the version to set
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * @return the parentGroup
   */
  public String getParentGroup() {
    return parentGroup;
  }

  /**
   * @param parentGroup the parentGroup to set
   */
  public void setParentGroup(String parentGroup) {
    this.parentGroup = parentGroup;
  }

  /**
   * @return the groupName
   */
  public String getGroupName() {
    return groupName;
  }

  /**
   * @param groupName the groupName to set
   */
  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  /**
   * @return the structureName
   */
  public String getStructureName() {
    return structureName;
  }

  /**
   * @param structureName the structureName to set
   */
  public void setStructureName(String structureName) {
    this.structureName = structureName;
  }

  /**
   * @return the fieldName
   */
  public String getFieldName() {
    return fieldName;
  }

  /**
   * @param fieldName the fieldName to set
   */
  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  /**
   * @return the dataType
   */
  public String getDataType() {
    return dataType;
  }

  /**
   * @param dataType the dataType to set
   */
  public void setDataType(String dataType) {
    this.dataType = dataType;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * @param value the value to set
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * @return the structureNumber
   */
  public String getStructureNumber() {
    return structureNumber;
  }

  /**
   * @param structureNumber the structureNumber to set
   */
  public void setStructureNumber(String structureNumber) {
    this.structureNumber = structureNumber;
  }

  /**
   * @return the coordinate
   */
  public String getCoordinate() {
    return coordinate;
  }

  /**
   * @param coordinate the coordinate to set
   */
  public void setCoordinate(String coordinate) {
    this.coordinate = coordinate;
  }
}
