package org.pentaho.di.core.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.pentaho.di.core.Condition;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleSQLException;
import org.pentaho.di.core.jdbc.ThinUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.core.row.ValueMetaInterface;

public class SQLCondition {
  
  private String tableAlias;
  RowMetaInterface serviceFields;
  private Condition condition;
  private String conditionClause;
  private SQLFields selectFields;

  public SQLCondition(String tableAlias, String conditionSql, RowMetaInterface serviceFields) throws KettleSQLException {
    this(tableAlias, conditionSql, serviceFields, null);
  }
  
  public SQLCondition(String tableAlias, String conditionSql, RowMetaInterface serviceFields, SQLFields selectFields) throws KettleSQLException {
    this.tableAlias = tableAlias;
    this.conditionClause = conditionSql;
    this.serviceFields = serviceFields;
    this.selectFields = selectFields;
    
    parse();
  }

  /**
   * Support for conditions is very simple for now:
   * 
   *  <Field> <operator> <value>
   *  <Field> <operator> <other field> 
   *  
   *  TODO: figure out a simple algorithm to split up on brackets, AND, OR
   *  
   * @throws KettleSQLException 
   */
  private void parse() throws KettleSQLException {
    
    // Split the condition clause on brackets and operators (AND, OR)
    // Since the Kettle condition simply evaluates without precedence, we'll just 
    // break the clause down into pieces and then define one or more conditions 
    // depending on the number of pieces we found.
    //
    condition = splitConditionByOperator(conditionClause, null, Condition.OPERATOR_NONE);
    for (int i=0;i<20 && condition.simplify(); i++);    
  }
  
  /**
   * Searches for the given string in a clause and returns the start index if found, -1 if not found.
   * This method skips brackets and single quotes.
   * Case is ignored
   * 
   * @param clause the clause
   * @param string the string to search
   * @param startIndex the index to start searching
   * @return the index if the string is found, -1 if not found
   * @throws KettleSQLException 
   */
  private int searchForString(String clause, String string, int startIndex) throws KettleSQLException {
    int index = startIndex;
    while (index<clause.length()) {
      index=ThinUtil.skipChars(clause, index, '\'', '(');
      if (index+string.length()>clause.length()) return -1; // done.
      if (clause.substring(index).toUpperCase().startsWith(string.toUpperCase())) return index;
      index++;
    }
    return -1;
  }

  private Condition splitConditionByOperator(String clause, Condition parentCondition, int parentOperator) throws KettleSQLException {
    if (parentCondition == null) {
      parentCondition = new Condition();
    } else {
      // add a new condition to the list...
      //
      Condition c = new Condition();
      c.setOperator(parentOperator);
      parentCondition.addCondition(c);
      parentCondition = c;
    }
    
    // First we find bracket pairs, then OR, then AND
    //
    // C='foo' AND ( A=5 OR B=6 )
    // ( A=4 OR B=3 ) AND ( A=5 OR B=6 )
    // ( clause1 ) AND ( clause2) AND ( clause3 )
    //
    
    // First try to split by OR, leaving grouped AND blocks.
    // e.g. A OR B AND C OR D --> A OR ( B AND C ) OR D --> A, B AND C, D
    //
    String orOperatorString = " OR ";
    int orConditionOperator = Condition.OPERATOR_OR;
    int lastIndex = splitByOperator(clause, parentCondition, orOperatorString, orConditionOperator);
    if (lastIndex==0) {
      
      // No AND operator(s) found, now we can look for OR operators in the clause...
      // Try to split by OR
      //
      String andOperatorString = " AND ";
      int andConditionOperator = Condition.OPERATOR_AND;
      lastIndex = splitByOperator(clause, parentCondition, andOperatorString, andConditionOperator);
      if (lastIndex==0) {
        String cleaned = Const.trim(clause);
        boolean negation = false;
        
        // See if it's a PARAMETER
        //
        if (Pattern.matches("^PARAMETER\\s*\\(.*\\)\\s*=\\s*'.*'$", cleaned)) {
          // Grab the string
          //
          int startParamIndex = cleaned.indexOf('(')+1;
          int endParamIndex = ThinUtil.skipChars(cleaned, startParamIndex, '(', '\'');
          String quotedParameter = Const.trim(cleaned.substring(startParamIndex, endParamIndex));
          if (quotedParameter.startsWith("'") && quotedParameter.endsWith("'")) {
            String parameterName = quotedParameter.substring(1, quotedParameter.length()-1);
            
            int startValueIndex = cleaned.indexOf('=', endParamIndex+1)+1;
            String quotedValue = Const.trim(cleaned.substring(startValueIndex));
            if (quotedValue.startsWith("'") && quotedValue.endsWith("'")) {
              String parameterValue = quotedValue.substring(1, quotedValue.length()-1);
              
              // A PARAMETER() function in the where clause always returns true
              //
              Condition subCondition = new Condition(parameterName, Condition.FUNC_TRUE, parameterName, new ValueMetaAndData(new ValueMeta("string", ValueMetaInterface.TYPE_STRING), Const.NVL(parameterValue, "")));
              subCondition.setOperator(orConditionOperator);
              parentCondition.addCondition(subCondition);
              
              if (Const.isEmpty(parameterName)) {
                throw new KettleSQLException("A parameter name can not be empty in : "+clause);
              }
              
            } else {
              throw new KettleSQLException("A parameter value has to always be a string between single quotes in : "+clause);
            }
            
          } else {
            throw new KettleSQLException("Parameter name between single quotes expected in : "+clause);
          }
        } else {
          
          // See if this elementary block is a NOT ( ) construct
          //
          if (Pattern.matches("^NOT\\s*\\(.*\\)$", cleaned.toUpperCase())) {
            negation = true;
            cleaned = Const.trim(cleaned.substring(3));
          }
          
          // No AND or OR operators found, 
          // First remove possible brackets though
          //
          if (cleaned.startsWith("(") && cleaned.endsWith(")")) {
            // Brackets are skipped above so we add a new condition to the list, and remove the brackets
            //
            cleaned = cleaned.substring(1, cleaned.length()-1);
            Condition c = splitConditionByOperator(cleaned, parentCondition, Condition.OPERATOR_NONE);
            c.setNegated(negation);
  
          } else {
            
            // Atomic condition
            //
            Condition subCondition = parseAtomicCondition(cleaned);
            subCondition.setOperator(orConditionOperator);
            parentCondition.addCondition(subCondition);
          }
        }
      }
    }
    
    return parentCondition;
  }

  private int splitByOperator(String clause, Condition parentCondition, String operatorString, int conditionOperator) throws KettleSQLException {
    int lastIndex=0;
    int index=0;
    while ( index<clause.length() && (index = searchForString(clause, operatorString, index))>=0) {
      // Split on the index --> ( clause1 ), ( clause2), (clause 3)
      //
      String left = clause.substring(lastIndex, index);
      splitConditionByOperator(left, parentCondition, conditionOperator);
      index+=operatorString.length();
      lastIndex=index;
    }
    
    // let's not forget to split the last right part or the OR(s)
    //
    if (lastIndex>0) {
      String right = clause.substring(lastIndex);
      splitConditionByOperator(right, parentCondition, conditionOperator);
    }

    
    return lastIndex;
  }

  private Condition parseAtomicCondition(String clause) throws KettleSQLException {
    // First split on spaces...
    //
    List<String> strings = splitConditionClause(clause);
    if (strings.size()>3) {
      throw new KettleSQLException("Unfortunately support for conditions is still very rudimentary, only 1 simple condition is supported ["+clause+"]");
    }
    String left = strings.get(0);
    
    // See if this is not a having clause expression :
    // example:
    //
    // SELECT country, count(distinct id) as customerCount FROM service GROUP BY country HAVING count(distinct id) > 10
    //
    if (selectFields!=null) {
      for (SQLField field : selectFields.getFields()) {
        if (field.getExpression().equalsIgnoreCase(left)) {
          if (!Const.isEmpty(field.getAlias())) {
            left = field.getAlias();
          }
          break;
        }
      }
    }
    
    // Remove the optional table alias prefix from the left field
    //
    left = ThinUtil.stripQuoteTableAlias(left, tableAlias);
    
    String operatorString = strings.get(1);
    String right = strings.get(2);
    
    // If it's another column name, remove possible table alias prefix.
    //
    right = ThinUtil.stripQuoteTableAlias(right, tableAlias);
    
    
    ValueMetaAndData value = null;
    
    int function = Condition.getFunction(operatorString);
    if (function==Condition.FUNC_IN_LIST) {
      // lose the brackets
      //
      String trimmed = Const.trim(right);
      String partClause = trimmed.substring(1, trimmed.length()-1);
      List<String> parts = ThinUtil.splitClause(partClause, ',', '\'');
      StringBuilder valueString = new StringBuilder();
      for (String part : parts) {
        part = Const.trim(part);
        if (valueString.length()>0) {
          valueString.append(";");
        }

        if (part.startsWith("'") && part.endsWith("'")) {
          valueString.append(part.substring(1,part.length()-1));
        } else {
          valueString.append(part);
        }
      }
      value = new ValueMetaAndData(new ValueMeta("constant-in-list", ValueMetaInterface.TYPE_STRING), valueString.toString());
    } else {
      
      // Mondrian, analyzer CONTAINS hack: 
      //  '%' || 'string' || '%'  --> '%string%' 
      //
      String prefix = "'%'";
      String suffix = "'%'";
      if (right.startsWith(prefix) && right.endsWith(suffix)) {
        int leftOrIndex=right.indexOf("||");
        if (leftOrIndex>0) {
          int rightOrIndex = right.indexOf("||", leftOrIndex+2);
          if (rightOrIndex>0) {
            String raw = Const.trim(right.substring(leftOrIndex+2, rightOrIndex));
            if (raw.startsWith("'") && raw.endsWith("'")) {
              right = "'%"+raw.substring(1, raw.length()-1)+"%'";
            }
            
          }
        }
        
      }
      
      value = ThinUtil.extractConstant(right);
    }
    
    
    if (value!=null) {
      return new Condition(left, function, null, value);
    } else {
      return new Condition(left, function, right, null);
    }
  }

  /**
   * We need to split conditions on a single operator (for now)
   * 
   * @param clause
   * @return 3 string list (left, operator, right)
   * @throws KettleSQLException 
   */
  private List<String> splitConditionClause(String clause) throws KettleSQLException {
    List<String> strings = new ArrayList<String>();
    
    String[] operators = new String[] { "<>", ">=", "=>", "<=", "=<", "<", ">", "=", " REGEX ", " IN ", " IS NOT NULL", " IS NULL", " LIKE", "CONTAINS " }; 
    int[] functions = new int[] { 
        Condition.FUNC_NOT_EQUAL,
        Condition.FUNC_LARGER_EQUAL,
        Condition.FUNC_LARGER_EQUAL,
        Condition.FUNC_SMALLER_EQUAL,
        Condition.FUNC_SMALLER_EQUAL,
        Condition.FUNC_SMALLER, 
        Condition.FUNC_LARGER,
        Condition.FUNC_EQUAL,
        Condition.FUNC_REGEXP,
        Condition.FUNC_IN_LIST,
        Condition.FUNC_NOT_NULL,
        Condition.FUNC_NULL,
        Condition.FUNC_LIKE,
        Condition.FUNC_CONTAINS,
      };
    int index=0;
    while (index<clause.length()) {
      index = ThinUtil.skipChars(clause, index, '\'', '"' );
      for (String operator : operators) {
        if (index<=clause.length()-operator.length()) {
          if (clause.substring(index).toUpperCase().startsWith(operator)) {
            int functionIndex = Const.indexOfString(operator, operators);
            
            // OK, we found an operator.
            // The part before is the first string
            //
            String left = Const.trim(clause.substring(0, index));
            String op = Condition.functions[functions[functionIndex]];
            String right = Const.trim(clause.substring(index+operator.length()));
            strings.add(left);
            strings.add(op);
            strings.add(right);
            return strings;
          }
        }
      }
      index++;
    }
    
    return strings;
  }

  /**
   * @return the serviceFields
   */
  public RowMetaInterface getServiceFields() {
    return serviceFields;
  }

  /**
   * @param serviceFields the serviceFields to set
   */
  public void setServiceFields(RowMetaInterface serviceFields) {
    this.serviceFields = serviceFields;
  }

  /**
   * @return the condition
   */
  public Condition getCondition() {
    return condition;
  }

  /**
   * @param condition the condition to set
   */
  public void setCondition(Condition condition) {
    this.condition = condition;
  }

  /**
   * @return the conditionClause
   */
  public String getConditionClause() {
    return conditionClause;
  }

  /**
   * @param conditionClause the conditionClause to set
   */
  public void setConditionClause(String conditionClause) {
    this.conditionClause = conditionClause;
  }

  public boolean isEmpty() {
    return condition.isEmpty();
  }

  /**
   * @return the selectFields
   */
  public SQLFields getSelectFields() {
    return selectFields;
  }

  /**
   * @return the tableAlias
   */
  public String getTableAlias() {
    return tableAlias;
  }

  /**
   * Extract the list of having fields from this having condition
   * @param aggFields 
   * @param rowMeta 
   * @return
   * @throws KettleSQLException 
   */
  public List<SQLField> extractHavingFields(List<SQLField> selectFields, List<SQLField> aggFields, RowMetaInterface rowMeta) throws KettleSQLException {
    List<SQLField> list = new ArrayList<SQLField>();
    
    // Get a list of all the lowest level field names and see if we can parse them as aggregation fields
    //
    List<String> expressions = new ArrayList<String>();
    addExpressions(condition, expressions);
    
    for (String expression : expressions) {
      // See if we already specified the aggregation in the Select clause, let's aggregate twice.
      //
      SQLField aggField = SQLField.searchSQLFieldByFieldOrAlias(aggFields, expression);
      if (aggField==null) {
        
        SQLField field = new SQLField(tableAlias, expression, serviceFields);
        if (field.getAggregation()!=null) {
          field.setField(expression);
          list.add(field);
        }
      }
    }
    
    return list;
  } 
  
  private void addExpressions(Condition condition, List<String> expressions) {
    if (condition.isAtomic()) {
      if (!expressions.contains(condition.getLeftValuename())) {
        expressions.add(condition.getLeftValuename());
      }
    } else {
      for (Condition child : condition.getChildren()) {
        addExpressions(child, expressions);
      }
    }
  }
}
