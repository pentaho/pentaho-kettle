/*******************************************************************************
 *
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.hbaseinput;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

public class ColumnFilter {
  
  public static enum ComparisonType {
    
    EQUAL("="), GREATER_THAN(">"), GREATER_THAN_OR_EQUAL(">="),
    LESS_THAN("<"), LESS_THAN_OR_EQUAL("<="), NOT_EQUAL("!="),
    SUBSTRING("Substring"), REGEX("Regular expression");
    
    private final String m_stringVal;
    
    ComparisonType(String name) {
      m_stringVal = name;
    }
    
    public String toString() {
      return m_stringVal;
    }
  }
  
  /** The hbase column to apply this filter to */
  protected String m_fieldAlias;
  
  /** The comparison operator */
  protected ComparisonType m_comparison;
  
  /** Whether the comparison constant or the column values involve signed numbers */
  protected boolean m_signedComparison;
  
  /** Holds the constant to compare to (or substring/regex) */
  protected String m_constant;
  
  /** Holds the formatting string for dates and numbers */
  protected String m_format;
  
  /** 
   * Holds the type of the field at the time this filter was defined - allows
   * easy checking at runtime in case the user has switched mappings but not
   * updated their filters.
   */
  protected String m_fieldType;
  
  public ColumnFilter(String alias) {
    setFieldAlias(alias);
  }
  
  public void setFieldAlias(String alias) {
    m_fieldAlias = alias;
  }
  
  public String getFieldAlias() {
    return m_fieldAlias;
  }
  
  public void setFieldType(String type) {
    m_fieldType = type;
  }
  
  public String getFieldType() {
    return m_fieldType;
  }
  
  public void setComparisonOperator(ComparisonType c) {
    m_comparison = c;
  }
  
  public ComparisonType getComparisonOperator() {
    return m_comparison;
  }
  
  public void setSignedComparison(boolean signed) {
    m_signedComparison = signed;
  }
  
  public boolean getSignedComparison() {
    return m_signedComparison;
  }
  
  public void setConstant(String constant) {
    m_constant = constant;
  }
  
  public String getConstant() {
    return m_constant;
  }
  
  public void setFormat(String format) {
    m_format = format;
  }
  
  public String getFormat() {
    return m_format;
  }
  
  public void appendXML(StringBuffer buff) {
    if (Const.isEmpty(m_fieldAlias) || Const.isEmpty(m_constant) || m_comparison == null) {
      return;
    }
    buff.append("\n        ").append(XMLHandler.openTag("filter"));
    buff.append("\n            ").append(XMLHandler.addTagValue("alias", m_fieldAlias));
    buff.append("\n            ").append(XMLHandler.addTagValue("type", m_fieldType));
    buff.append("\n            ").append(XMLHandler.addTagValue("comparison_opp", 
        m_comparison.toString()));
    buff.append("\n            ").append(XMLHandler.addTagValue("signed_comp", m_signedComparison));

    buff.append("\n            ").append(XMLHandler.addTagValue("constant", m_constant));
    if (!Const.isEmpty(m_format)) {
      buff.append("\n            ").append(XMLHandler.addTagValue("format", m_format.trim()));
    }
    
    buff.append("\n        ").append(XMLHandler.closeTag("filter"));
  }
  
  public static ColumnFilter getFilter(Node filterNode) {
    String alias = XMLHandler.getTagValue(filterNode, "alias");
    
    ColumnFilter returnVal = new ColumnFilter(alias);
    String type = XMLHandler.getTagValue(filterNode, "type");
    returnVal.setFieldType(type);
    String comp = XMLHandler.getTagValue(filterNode, "comparison_opp");
    returnVal.setComparisonOperator(stringToOpp(comp));
    String signed = XMLHandler.getTagValue(filterNode, "signed_comp");
    returnVal.setSignedComparison(signed.equalsIgnoreCase("Y"));
    String constant = XMLHandler.getTagValue(filterNode, "constant");
    returnVal.setConstant(constant);
    String format = XMLHandler.getTagValue(filterNode, "format");
    returnVal.setFormat(format);
    
    return returnVal;
  }
  
  public void saveRep(Repository rep, ObjectId id_transformation, 
      ObjectId id_step, int filterNum) throws KettleException {
    
    if (Const.isEmpty(m_fieldAlias) || Const.isEmpty(m_constant) || m_comparison == null) {
      return;
    }
    
    rep.saveStepAttribute(id_transformation, id_step, filterNum, "cf_alias", m_fieldAlias);
    rep.saveStepAttribute(id_transformation, id_step, filterNum, "cf_type", m_fieldType);
    rep.saveStepAttribute(id_transformation, id_step, filterNum, "cf_comparison_opp", 
        m_comparison.toString());
    rep.saveStepAttribute(id_transformation, id_step, filterNum, "cf_signed_comp", 
        m_signedComparison);
    rep.saveStepAttribute(id_transformation, id_step, filterNum, "cf_constant", m_constant);
    if (!Const.isEmpty(m_format)) {
      rep.saveStepAttribute(id_transformation, id_step, filterNum, "cf_format", m_format.trim());
    }
  }
  
  public static ColumnFilter getFilter(Repository rep, int nodeNum, ObjectId id_step)
    throws KettleException {
    String alias = rep.getStepAttributeString(id_step, nodeNum, "cf_alias");
    
    ColumnFilter returnVal = new ColumnFilter(alias);
    String type = rep.getStepAttributeString(id_step, nodeNum, "cf_type");
    returnVal.setFieldType(type);
    String comp = rep.getStepAttributeString(id_step, nodeNum, "cf_comparison_opp");
    returnVal.setComparisonOperator(stringToOpp(comp));
    returnVal.setSignedComparison(rep.getStepAttributeBoolean(id_step, nodeNum, "cf_signed_comp"));
    String constant = rep.getStepAttributeString(id_step, nodeNum, "cf_constant");
    returnVal.setConstant(constant);
    String format = rep.getStepAttributeString(id_step, nodeNum, "cf_format");
    returnVal.setFormat(format);
    
    return returnVal;
  }
  
  public static ComparisonType stringToOpp(String opp) {
    ComparisonType c = null;
    
    for (ComparisonType t : ComparisonType.values()) {
      if (t.toString().equals(opp)) {
        c = t;
        break;
      }
    }
    
    return c;
  }
  
  public static String[] getAllOperators() {
    String[] ops = new String[8];
    
    ops[0] = ComparisonType.EQUAL.toString();
    ops[1] = ComparisonType.NOT_EQUAL.toString();
    ops[2] = ComparisonType.GREATER_THAN.toString();
    ops[3] = ComparisonType.GREATER_THAN_OR_EQUAL.toString();
    ops[4] = ComparisonType.LESS_THAN.toString();
    ops[5] = ComparisonType.LESS_THAN_OR_EQUAL.toString();
    ops[6] = ComparisonType.SUBSTRING.toString();
    ops[7] = ComparisonType.REGEX.toString();
    
    return ops;
  }
  
  public static String[] getStringOperators() {
    String[] ops = new String[2];
    
    ops[0] = ComparisonType.SUBSTRING.toString();
    ops[1] = ComparisonType.REGEX.toString();
    
    return ops;
  }
  
  public static String[] getNumericOperators() {
    String[] ops = new String[6];
                              
    ops[0] = ComparisonType.EQUAL.toString();
    ops[1] = ComparisonType.NOT_EQUAL.toString();
    ops[2] = ComparisonType.GREATER_THAN.toString();
    ops[3] = ComparisonType.GREATER_THAN_OR_EQUAL.toString();
    ops[4] = ComparisonType.LESS_THAN.toString();
    ops[5] = ComparisonType.LESS_THAN_OR_EQUAL.toString();
    
    return ops;
  }
}
