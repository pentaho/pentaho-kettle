package org.pentaho.di.core.sql;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleSQLException;
import org.pentaho.di.core.jdbc.ThinUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaAndData;

public class IifFunction {
  private String tableAlias;
  private String conditionClause;
  private SQLCondition sqlCondition;
  private RowMetaInterface serviceFields;
  
  private String trueValueString;
  private ValueMetaAndData trueValue;
  private boolean trueField;
  private String falseValueString;
  private ValueMetaAndData falseValue;
  private boolean falseField;
  
  public IifFunction(String tableAlias, String conditionClause, String trueValueString, String falseValueString, RowMetaInterface serviceFields) throws KettleSQLException {
    this.tableAlias = tableAlias;
    this.conditionClause = conditionClause;
    this.trueValueString = trueValueString;
    this.falseValueString = falseValueString;
    this.serviceFields = serviceFields;

    // Parse the SQL
    this.sqlCondition = new SQLCondition(tableAlias, conditionClause, serviceFields);
    
    // rudimentary string, date, number, integer determination
    //
    trueValue = extractValue(trueValueString, true);
    falseValue = extractValue(falseValueString, false);    
  }

  private ValueMetaAndData extractValue(String string, boolean trueIndicator) throws KettleSQLException {
    if (Const.isEmpty(string)) {
      return null;
    }
    
    ValueMetaAndData value = ThinUtil.attemptDateValueExtraction(string);
    if (value!=null) return value;
    
    value = ThinUtil.attemptStringValueExtraction(string);
    if (value!=null) return value;
    
    // See if it's a field...
    //
    int index = serviceFields.indexOfValue(string);
    if (index>=0) {
      if (trueIndicator) {
        trueField=true;
      } else {
        falseField=true;
      }
      return new ValueMetaAndData(serviceFields.getValueMeta(index), null);
    }

    value = ThinUtil.attemptBooleanValueExtraction(string);
    if (value!=null) return value;

    value = ThinUtil.attemptIntegerValueExtraction(string);
    if (value!=null) return value;

    value = ThinUtil.attemptNumberValueExtraction(string);
    if (value!=null) return value;

    value = ThinUtil.attemptBigNumberValueExtraction(string);
    if (value!=null) return value;

    throw new KettleSQLException("Unable to determine value data type for string: ["+string+"]");
  }

  /**
   * @return the conditionClause
   */
  public String getConditionClause() {
    return conditionClause;
  }

  /**
   * @return the sqlCondition
   */
  public SQLCondition getSqlCondition() {
    return sqlCondition;
  }

  /**
   * @return the serviceFields
   */
  public RowMetaInterface getServiceFields() {
    return serviceFields;
  }

  /**
   * @return the trueValueString
   */
  public String getTrueValueString() {
    return trueValueString;
  }

  /**
   * @return the trueValue
   */
  public ValueMetaAndData getTrueValue() {
    return trueValue;
  }

  /**
   * @return the falseValueString
   */
  public String getFalseValueString() {
    return falseValueString;
  }

  /**
   * @return the falseValue
   */
  public ValueMetaAndData getFalseValue() {
    return falseValue;
  }

  /**
   * @return the falseField
   */
  public boolean isFalseField() {
    return falseField;
  }

  /**
   * @return the trueField
   */
  public boolean isTrueField() {
    return trueField;
  }

  /**
   * @return the tableAlias
   */
  public String getTableAlias() {
    return tableAlias;
  }
  
}
