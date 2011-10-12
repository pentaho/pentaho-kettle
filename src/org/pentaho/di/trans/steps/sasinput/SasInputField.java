package org.pentaho.di.trans.steps.sasinput;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

/**
 * This defines a selected list of fields from the input files including 
 *  
 * @author matt
 * @since 10-OCT-2011
 */
public class SasInputField implements XMLInterface, Cloneable {
  private String name;
  private String rename;
  private int    type;
  private int    length;
  private int    precision;
  private String conversionMask;
  private String decimalSymbol;
  private String groupingSymbol;
  private int trimType;
  
  /**
   * @param name
   * @param rename
   * @param type
   * @param conversionMask
   * @param decimalSymbol
   * @param groupingSymbol
   * @param trimType
   */
  public SasInputField(String name, String rename, int type, String conversionMask, String decimalSymbol, String groupingSymbol, int trimType) {
    this.name = name;
    this.rename = rename;
    this.type = type;
    this.conversionMask = conversionMask;
    this.decimalSymbol = decimalSymbol;
    this.groupingSymbol = groupingSymbol;
    this.trimType = trimType;
  }
  
  public SasInputField() {
  }
  
  @Override
  protected SasInputField clone() {
    try {
      return (SasInputField)super.clone();
    } catch(CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getXML() {
    StringBuffer retval=new StringBuffer();
    
    retval.append("    " + XMLHandler.addTagValue("name", name));
    retval.append("    " + XMLHandler.addTagValue("rename", rename));
    retval.append("    " + XMLHandler.addTagValue("type", ValueMeta.getTypeDesc(type)));
    retval.append("    " + XMLHandler.addTagValue("length", length));
    retval.append("    " + XMLHandler.addTagValue("precision", precision));
    retval.append("    " + XMLHandler.addTagValue("conversion_mask", conversionMask));
    retval.append("    " + XMLHandler.addTagValue("decimal", decimalSymbol));
    retval.append("    " + XMLHandler.addTagValue("grouping", groupingSymbol));
    retval.append("    " + XMLHandler.addTagValue("trim_type", ValueMeta.getTrimTypeCode(trimType)));

    return retval.toString();
  }
  
  public void saveRep(Repository rep, ObjectId transformationId, ObjectId stepId, int fieldNr) throws KettleException {
    rep.saveStepAttribute(transformationId, stepId, fieldNr, "field_name", name);
    rep.saveStepAttribute(transformationId, stepId, fieldNr, "field_rename", rename);
    rep.saveStepAttribute(transformationId, stepId, fieldNr, "field_type", ValueMeta.getTypeDesc(type));
    rep.saveStepAttribute(transformationId, stepId, fieldNr, "field_length", length);
    rep.saveStepAttribute(transformationId, stepId, fieldNr, "field_precision", precision);
    rep.saveStepAttribute(transformationId, stepId, fieldNr, "field_conversion_mask", conversionMask);
    rep.saveStepAttribute(transformationId, stepId, fieldNr, "field_decimal", decimalSymbol);
    rep.saveStepAttribute(transformationId, stepId, fieldNr, "field_grouping", groupingSymbol);
    rep.saveStepAttribute(transformationId, stepId, fieldNr, "field_trim_type", ValueMeta.getTrimTypeCode(trimType));
  }
  
  public SasInputField(Repository rep, ObjectId stepId, int fieldNr) throws KettleException {
    name = rep.getStepAttributeString(stepId, fieldNr, "field_name");
    rename = rep.getStepAttributeString(stepId, fieldNr, "field_rename");
    type = ValueMeta.getType(rep.getStepAttributeString(stepId, fieldNr, "field_type"));
    length = (int)rep.getStepAttributeInteger(stepId, fieldNr, "field_length");
    precision = (int)rep.getStepAttributeInteger(stepId, fieldNr, "field_precision");
    conversionMask = rep.getStepAttributeString(stepId, fieldNr, "field_conversion_mask");
    decimalSymbol = rep.getStepAttributeString(stepId, fieldNr, "field_decimal");
    groupingSymbol = rep.getStepAttributeString(stepId, fieldNr, "field_grouping");
    trimType = ValueMeta.getTrimTypeByCode(rep.getStepAttributeString(stepId, fieldNr, "field_trim_type"));
  }
  
  public SasInputField(Node node) throws KettleXMLException {
    name = XMLHandler.getTagValue(node, "name"); //$NON-NLS-1$
    rename = XMLHandler.getTagValue(node, "rename"); //$NON-NLS-1$
    type = ValueMeta.getType(XMLHandler.getTagValue(node, "type")); //$NON-NLS-1$
    length = Const.toInt(XMLHandler.getTagValue(node, "length") , -1);  //$NON-NLS-1$
    precision = Const.toInt(XMLHandler.getTagValue(node, "precision") , -1);  //$NON-NLS-1$
    conversionMask = XMLHandler.getTagValue(node, "conversion_mask"); //$NON-NLS-1$
    decimalSymbol= XMLHandler.getTagValue(node, "decimal"); //$NON-NLS-1$
    groupingSymbol = XMLHandler.getTagValue(node, "grouping"); //$NON-NLS-1$
    trimType = ValueMeta.getTrimTypeByCode(XMLHandler.getTagValue(node, "trim_type")); //$NON-NLS-1$
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
   * @return the rename
   */
  public String getRename() {
    return rename;
  }
  /**
   * @param rename the rename to set
   */
  public void setRename(String rename) {
    this.rename = rename;
  }
  /**
   * @return the type
   */
  public int getType() {
    return type;
  }
  /**
   * @param type the type to set
   */
  public void setType(int type) {
    this.type = type;
  }
  /**
   * @return the conversionMask
   */
  public String getConversionMask() {
    return conversionMask;
  }
  /**
   * @param conversionMask the conversionMask to set
   */
  public void setConversionMask(String conversionMask) {
    this.conversionMask = conversionMask;
  }
  /**
   * @return the decimalSymbol
   */
  public String getDecimalSymbol() {
    return decimalSymbol;
  }
  /**
   * @param decimalSymbol the decimalSymbol to set
   */
  public void setDecimalSymbol(String decimalSymbol) {
    this.decimalSymbol = decimalSymbol;
  }
  /**
   * @return the groupingSymbol
   */
  public String getGroupingSymbol() {
    return groupingSymbol;
  }
  /**
   * @param groupingSymbol the groupingSymbol to set
   */
  public void setGroupingSymbol(String groupingSymbol) {
    this.groupingSymbol = groupingSymbol;
  }
  /**
   * @return the trimType
   */
  public int getTrimType() {
    return trimType;
  }
  /**
   * @param trimType the trimType to set
   */
  public void setTrimType(int trimType) {
    this.trimType = trimType;
  }

  /**
   * @return the precision
   */
  public int getPrecision() {
    return precision;
  }

  /**
   * @param precision the precision to set
   */
  public void setPrecision(int precision) {
    this.precision = precision;
  }

  /**
   * @return the length
   */
  public int getLength() {
    return length;
  }

  /**
   * @param length the length to set
   */
  public void setLength(int length) {
    this.length = length;
  }

  public String getTrimTypeDesc() {
    return ValueMeta.getTrimTypeDesc(trimType);
  }
}
