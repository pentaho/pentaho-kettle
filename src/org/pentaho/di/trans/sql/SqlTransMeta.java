package org.pentaho.di.trans.sql;

import java.util.List;

import org.pentaho.di.core.Condition;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.sql.IifFunction;
import org.pentaho.di.core.sql.SQL;
import org.pentaho.di.core.sql.SQLField;
import org.pentaho.di.core.sql.SQLFields;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.steps.calculator.CalculatorMeta;
import org.pentaho.di.trans.steps.calculator.CalculatorMetaFunction;
import org.pentaho.di.trans.steps.constant.ConstantMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.filterrows.FilterRowsMeta;
import org.pentaho.di.trans.steps.injector.InjectorMeta;
import org.pentaho.di.trans.steps.memgroupby.MemoryGroupByMeta;
import org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta;
import org.pentaho.di.trans.steps.samplerows.SampleRowsMeta;
import org.pentaho.di.trans.steps.selectvalues.SelectValuesMeta;
import org.pentaho.di.trans.steps.sort.SortRowsMeta;

public class SqlTransMeta {
  
  private SQL sql;
  private RowMetaInterface serviceFields;

  private String injectorStepName;
  private String resultStepName;
  
  private int xLocation;
  private int rowLimit;
  
  public SqlTransMeta(SQL sql, int rowLimit) {
    this.sql = sql;
    this.rowLimit = rowLimit;
    this.serviceFields = sql.getRowMeta();
  }
  
  /**
   * Generate the transformation metadata for 
   * @return
   */
  public TransMeta generateTransMeta() throws KettleException {
    TransMeta transMeta = new TransMeta();
    transMeta.setName(SqlTransExecutor.calculateTransname(sql, false));
    xLocation = 50;
    
    StepMeta firstStep;
    if (Const.isEmpty(sql.getServiceName()) || "dual".equalsIgnoreCase(sql.getServiceName())) {

      // Generate 1 empty row
      //
      firstStep = generateEmptyRowStep();
      
    } else {
    
      // Add an injector where we will pump in the rows from the service transformation.
      //
      firstStep = generateInjectorStep();
    }
    transMeta.addStep(firstStep);
    injectorStepName = firstStep.getName();
    StepMeta lastStep = firstStep;
        
    // Add possible constants to the rows...
    //
    List<SQLField> constFields = sql.getSelectFields().getConstantFields();
    if (!constFields.isEmpty()) {
      StepMeta constStep = generateConstStep(constFields);
      lastStep = addToTrans(constStep, transMeta, lastStep);
    }

    // Add filters, constants, calculator steps to calculate possible IIF functions...
    // This block is for the IIF methods in the SELECT clause
    //
    List<SQLField> iifFields = sql.getSelectFields().getIifFunctionFields();
    for (SQLField iifField : iifFields) {
      lastStep = generateIifStep(iifField, transMeta, lastStep);
    }
    
    // We optionally need to aggregate the data
    //
    if (sql.getWhereCondition()!=null && !sql.getWhereCondition().isEmpty()) {
      StepMeta filterStep = generateFilterStep(sql.getWhereCondition().getCondition(), false);
      lastStep = addToTrans(filterStep, transMeta, lastStep);
    }

    // We optionally need to aggregate the data
    //
    List<SQLField> aggFields = sql.getSelectFields().getAggregateFields();
    if (sql.getHavingCondition()!=null) {
      List<SQLField> havingFields = sql.getHavingCondition().extractHavingFields(
          sql.getSelectFields().getFields(),
          aggFields,
          transMeta.getStepFields(lastStep));
      aggFields.addAll(havingFields);
    }
    List<SQLField> groupFields = sql.getGroupFields().getFields();
    
    if (aggFields.size()>0 || groupFields.size()>0) {
      StepMeta groupStep = generateGroupByStep(aggFields, groupFields, transMeta.getStepFields(lastStep));
      lastStep = addToTrans(groupStep, transMeta, lastStep);
    }

    // Add filters, constants, calculator steps to calculate possible IIF functions...
    // This block is for the IIF methods in the ORDER clause
    //
    if (sql.getOrderFields()!=null) {
      iifFields = sql.getOrderFields().getIifFunctionFields();
      for (SQLField iifField : iifFields) {
        lastStep = generateIifStep(iifField, transMeta, lastStep);
      }
    }

    // Finally there might be a having clause, which is another filter...
    //
    if (sql.getHavingCondition()!=null && !sql.getHavingCondition().isEmpty()) {
      StepMeta filterStep = generateFilterStep(sql.getHavingCondition().getCondition(), true);
      lastStep = addToTrans(filterStep, transMeta, lastStep);
    }
    
    // See if we need to do a distinct 
    //
    if (sql.getSelectFields().isDistinct()) {
      // Add a Unique Rows By HashSet step
      //
      StepMeta filterStep = generateUniqueStep(transMeta.getStepFields(lastStep));
      lastStep = addToTrans(filterStep, transMeta, lastStep);
    }
    
    // We also may need to order the data...
    //
    if (sql.getOrderFields()!=null && !sql.getOrderFields().isEmpty()) {
      StepMeta sortStep = generateSortStep(transMeta.getStepFields(lastStep));
      lastStep = addToTrans(sortStep, transMeta, lastStep);
    }

    // We need only the fields from the select clause, rename if needed
    // In case we're using a group by clause this is done automatically.
    //
    if (!sql.getSelectFields().hasAggregates() && sql.getSelectFields().getRegularFields().size()>0) {
      StepMeta selectStep = generateSelectStep();
      lastStep = addToTrans(selectStep, transMeta, lastStep);
    }

    // If there is a row limit specified, adhere to it but do not block the transformation from being executed.
    //
    if (rowLimit>0) {
      StepMeta sampleStep = generateSampleStep();
      lastStep = addToTrans(sampleStep, transMeta, lastStep);
    }
    
    // Finally add a dummy step containing the result
    //
    StepMeta resultStep = generateResultStep();
    resultStepName = resultStep.getName();
    lastStep = addToTrans(resultStep, transMeta, lastStep);
    
    return transMeta;
  }
  
  private StepMeta generateEmptyRowStep() {
    RowGeneratorMeta meta = new RowGeneratorMeta();
    meta.allocate(0);
    meta.setRowLimit("1");
    
    StepMeta stepMeta = new StepMeta("dual", meta);
    stepMeta.setLocation(xLocation, 50);
    xLocation+=100;
    stepMeta.setDraw(true);
    return stepMeta;
  
  }

  private StepMeta addToTrans(StepMeta sortStep, TransMeta transMeta, StepMeta lastStep) {
    transMeta.addStep(sortStep);
    transMeta.addTransHop(new TransHopMeta(lastStep, sortStep));
    return sortStep;

  }

  /**
   * This method generates a 4 steps for every IIF clause... TODO: replace with one step...
   * 
   * @param iifField
   * @param lastStep 
   * @param transMeta 
   * @return steps
   */
  private StepMeta generateIifStep(SQLField iifField, TransMeta transMeta, StepMeta lastStep) {
    IifFunction iif = iifField.getIif();
    
    // The Filter condition...
    //
    FilterRowsMeta filterMeta = new FilterRowsMeta();
    filterMeta.setCondition(iifField.getIif().getSqlCondition().getCondition());
    StepMeta filterStep = new StepMeta(iifField.getExpression(), filterMeta);
    filterStep.setLocation(xLocation, 50);
    xLocation+=100;
    filterStep.setDraw(true);
    lastStep = addToTrans(filterStep, transMeta, lastStep);
    
    // The True and false steps...
    //
    StepMetaInterface trueMetaInterface;
    ValueMetaInterface valueMeta = iif.getTrueValue().getValueMeta();
    if (iif.isTrueField()) {
      CalculatorMeta trueMeta = new CalculatorMeta();
      trueMetaInterface=trueMeta;
      trueMeta.allocate(1);
      
      CalculatorMetaFunction function = new CalculatorMetaFunction(); 
      function.setFieldName(Const.NVL(iifField.getAlias(), iifField.getField()));
      function.setCalcType(CalculatorMetaFunction.CALC_COPY_OF_FIELD);
      function.setValueType(valueMeta.getType());
      function.setValueLength(valueMeta.getLength());
      function.setValuePrecision(valueMeta.getPrecision());
      function.setFieldA(iif.getTrueValueString());
      function.setConversionMask(valueMeta.getConversionMask());
      trueMeta.getCalculation()[0] = function;
    } else {
      ConstantMeta trueMeta = new ConstantMeta();
      trueMetaInterface=trueMeta;
      trueMeta.allocate(1);
      trueMeta.getFieldName()[0] = Const.NVL(iifField.getAlias(), iifField.getField());
      trueMeta.getFieldType()[0] = iif.getTrueValue().getValueMeta().getTypeDesc();
      trueMeta.getValue()[0]     = iif.getTrueValue().toString();
      trueMeta.getFieldFormat()[0] = valueMeta.getConversionMask();
    }
    StepMeta trueStep = new StepMeta("TRUE: "+iifField.getExpression(), trueMetaInterface);
    trueStep.setLocation(xLocation, 50);
    trueStep.setDraw(true);
    lastStep = addToTrans(trueStep, transMeta, filterStep);

    StepMetaInterface falseMetaInterface;
    valueMeta = iif.getFalseValue().getValueMeta();
    if (iif.isFalseField()) {
      CalculatorMeta falseMeta = new CalculatorMeta();
      falseMetaInterface=falseMeta;
      falseMeta.allocate(1);
      CalculatorMetaFunction function = new CalculatorMetaFunction();
      function.setFieldName(Const.NVL(iifField.getAlias(), iifField.getField()));
      function.setCalcType(CalculatorMetaFunction.CALC_COPY_OF_FIELD);
      function.setValueType(valueMeta.getType());
      function.setValueLength(valueMeta.getLength());
      function.setValuePrecision(valueMeta.getPrecision());
      function.setFieldA(iif.getFalseValueString());
      function.setConversionMask(valueMeta.getConversionMask());
      falseMeta.getCalculation()[0] = function;
    } else {

      ConstantMeta falseMeta = new ConstantMeta();
      falseMetaInterface=falseMeta;
      falseMeta.allocate(1);
      falseMeta.getFieldName()[0] = Const.NVL(iifField.getAlias(), iifField.getField());
      falseMeta.getFieldType()[0] = iif.getFalseValue().getValueMeta().getTypeDesc();
      falseMeta.getFieldFormat()[0] = valueMeta.getConversionMask();
      falseMeta.getValue()[0]     = iif.getFalseValue().toString();
    }
    StepMeta falseStep = new StepMeta("FALSE: "+iifField.getExpression(), falseMetaInterface);
    falseStep.setLocation(xLocation, 150);
    xLocation+=100;
    falseStep.setDraw(true);
    lastStep = addToTrans(falseStep, transMeta, filterStep);
    
    // specify true/false targets
    List<StreamInterface> targetStreams = filterMeta.getStepIOMeta().getTargetStreams();
    targetStreams.get(0).setSubject( trueStep.getName() ); //$NON-NLS-1$
    targetStreams.get(1).setSubject( falseStep.getName() ); //$NON-NLS-1$
    filterMeta.searchInfoAndTargetSteps(transMeta.getSteps());

    DummyTransMeta dummyMeta = new DummyTransMeta();
    StepMeta dummyStep = new StepMeta("Collect: "+iifField.getExpression(), dummyMeta);
    dummyStep.setLocation(xLocation, 50);
    xLocation+=100;
    dummyStep.setDraw(true);
    lastStep = addToTrans(dummyStep, transMeta, trueStep);
    transMeta.addTransHop(new TransHopMeta(falseStep, dummyStep));
    
    return lastStep;
  }

  private StepMeta generateInjectorStep() {
    InjectorMeta meta = new InjectorMeta();
    meta.allocate(serviceFields.size());
    for (int i=0;i<serviceFields.size();i++) {
      ValueMetaInterface valueMeta = serviceFields.getValueMeta(i);
      meta.getFieldname()[i] = valueMeta.getName();
      meta.getType()[i] = valueMeta.getType();
      meta.getLength()[i] = valueMeta.getLength();
      meta.getPrecision()[i] = valueMeta.getPrecision();
    }
    StepMeta stepMeta = new StepMeta("Injector", meta);
    stepMeta.setLocation(xLocation, 50);
    xLocation+=100;
    stepMeta.setDraw(true);
    return stepMeta;
  }

  private StepMeta generateResultStep() {
    DummyTransMeta meta = new DummyTransMeta();

    StepMeta stepMeta = new StepMeta("RESULT", meta);
    stepMeta.setLocation(xLocation, 50);
    xLocation+=100;
    stepMeta.setDraw(true);
    return stepMeta;
  }
  
  private StepMeta generateUniqueStep(RowMetaInterface rowMeta) {
    SQLFields fields = sql.getSelectFields();
    MemoryGroupByMeta meta = new MemoryGroupByMeta();
    meta.allocate(fields.getFields().size(), 0);
    for (int i=0;i<fields.getFields().size();i++) {
      SQLField field = fields.getFields().get(i);
      if (!Const.isEmpty(field.getAlias()) && rowMeta.searchValueMeta(field.getAlias())!=null) {
        meta.getGroupField()[i] = field.getAlias();
      } else {
        meta.getGroupField()[i] = field.getField();
      }
    }
    
    StepMeta stepMeta = new StepMeta("DISTINCT", meta);
    stepMeta.setLocation(xLocation, 50);
    xLocation+=100;
    stepMeta.setDraw(true);
    return stepMeta;
  }

  private StepMeta generateSampleStep() {
    SampleRowsMeta meta = new SampleRowsMeta();
    meta.setLinesRange("1.."+rowLimit);
    
    StepMeta stepMeta = new StepMeta("Sample rows", meta);
    stepMeta.setLocation(xLocation, 50);
    xLocation+=100;
    stepMeta.setDraw(true);
    return stepMeta;
  }
  
  private StepMeta generateFilterStep(Condition condition, boolean isHaving) {
    FilterRowsMeta meta = new FilterRowsMeta();
    meta.setCondition(condition);
    
    StepMeta stepMeta = new StepMeta(isHaving ? "Having filter" : "Where filter", meta);
    stepMeta.setLocation(xLocation, 50);
    xLocation+=100;
    stepMeta.setDraw(true);
    return stepMeta;
  }
  
  private StepMeta generateConstStep(List<SQLField> fields) throws KettleException {
    ConstantMeta meta = new ConstantMeta();
    meta.allocate(fields.size());
    for (int i=0;i<fields.size();i++) {
      SQLField field = fields.get(i);
      ValueMetaInterface valueMeta = field.getValueMeta();
      meta.getFieldName()[i] = "Constant_"+field.getFieldIndex()+"_"+field.getField();
      meta.getFieldFormat()[i] = valueMeta.getConversionMask();
      meta.getFieldType()[i] = valueMeta.getTypeDesc();
      meta.getFieldLength()[i] = valueMeta.getLength();
      meta.getFieldPrecision()[i] = valueMeta.getPrecision();
      meta.getDecimal()[i] = valueMeta.getDecimalSymbol();
      meta.getGroup()[i] = valueMeta.getGroupingSymbol();
      meta.getValue()[i] = valueMeta.getString(field.getValueData());
    }
    
    StepMeta stepMeta = new StepMeta("Constants", meta);
    stepMeta.setLocation(xLocation, 50);
    xLocation+=100;
    stepMeta.setDraw(true);
    return stepMeta;
  }
  
  private StepMeta generateGroupByStep(List<SQLField> aggFields, List<SQLField> groupFields, RowMetaInterface inputFields) throws KettleException {
    MemoryGroupByMeta meta = new MemoryGroupByMeta();
    meta.allocate(groupFields.size(), aggFields.size());
    
    // See if we need to always return a row or not (0 rows counted scenario)
    //
    boolean returnRow = false;
    
    // The grouping fields
    //
    for (int i=0;i<groupFields.size();i++) {
      SQLField field = groupFields.get(i);
      meta.getGroupField()[i] = field.getField();
    }

    // The aggregates
    //
    for (int i=0;i<aggFields.size();i++) {
      SQLField field = aggFields.get(i);
      ValueMetaInterface valueMeta = field.getValueMeta();
      meta.getAggregateField()[i] = Const.NVL(field.getAlias(), field.getField());
      
      String subjectField;
      if (field.getValueData()==null) {
        // No constant value to aggregate
        //
        if (valueMeta==null) {
          // No specific value to aggregate (count(*))
          // In that case just take the first field in the input stream.
          //
          if (inputFields.size()==0) {
            throw new KettleException("No field fields found to aggregate on.");
          }
          subjectField = inputFields.getValueMeta(0).getName();
        } else {
          subjectField = valueMeta.getName();
        }
      } else {
        // A constant field to aggregate.
        //
        subjectField = "Constant_"+field.getFieldIndex()+"_"+field.getField();
      }
      
      meta.getSubjectField()[i] = subjectField;
      int agg = 0;
      switch(field.getAggregation()) {
      case SUM: agg = MemoryGroupByMeta.TYPE_GROUP_SUM; break;
      case MIN: agg = MemoryGroupByMeta.TYPE_GROUP_MIN; break;
      case MAX: agg = MemoryGroupByMeta.TYPE_GROUP_MAX; break;
      case COUNT: 
        if (field.isCountStar()) {
          agg = MemoryGroupByMeta.TYPE_GROUP_COUNT_ANY;
        } else if (field.isCountDistinct()) {
          agg = MemoryGroupByMeta.TYPE_GROUP_COUNT_DISTINCT;
        } else {
          // Count a particular field
          agg = MemoryGroupByMeta.TYPE_GROUP_COUNT_ALL;
        }
        returnRow=true;
        break;
      case AVG: agg = MemoryGroupByMeta.TYPE_GROUP_AVERAGE; break;
      default:
        throw new KettleException("Unhandled aggregation method ["+field.getAggregation()+"]");
      }
      meta.getAggregateType()[i] = agg;
    }
    
    meta.setAlwaysGivingBackOneRow(returnRow);

    StepMeta stepMeta = new StepMeta("Group by", meta);
    stepMeta.setLocation(xLocation, 50);
    xLocation+=100;
    stepMeta.setDraw(true);
    
    return stepMeta;
  }

  private StepMeta generateSortStep(RowMetaInterface rowMeta) throws KettleException {
    List<SQLField> fields = sql.getOrderFields().getFields();
    List<SQLField> selectFields = sql.getSelectFields().getFields();

    SortRowsMeta meta = new SortRowsMeta();
    meta.allocate(fields.size());
    for (int i=0;i<fields.size();i++) {
      SQLField sqlField = fields.get(i);
      
      ValueMetaInterface valueMeta = rowMeta.searchValueMeta(sqlField.getField());
      if (valueMeta==null) {
        // This could be an alias used in an order by clause.
        // In that case, we need to find the correct original name in the selectFields...
        //
        SQLField selectField = SQLField.searchSQLFieldByFieldOrAlias(selectFields, sqlField.getField());
        if (selectField!=null) {
          // Yep, verify this original name...
          //
          valueMeta = rowMeta.searchValueMeta(selectField.getField());
        } else {
          valueMeta = rowMeta.searchValueMeta(sqlField.getAlias());
        }
        
      }
      if (valueMeta==null) {
        throw new KettleException("Unable to find field to sort on: "+sqlField.getField()+" nor the alias: "+sqlField.getAlias());
      }
      
      meta.getFieldName()[i] = valueMeta.getName();
      meta.getAscending()[i] = sqlField.isAscending();
      meta.getCaseSensitive()[i] = true;
    }
    meta.setSortSize("1000000");
    
    StepMeta stepMeta = new StepMeta("Sort rows", meta);
    stepMeta.setLocation(xLocation, 50);
    xLocation+=100;
    stepMeta.setDraw(true);
    return stepMeta;
  }


  
  private StepMeta generateSelectStep() {
    // Only rename the non function fields
    //
    List<SQLField> fields = sql.getSelectFields().getRegularFields();

    SelectValuesMeta meta = new SelectValuesMeta();
    meta.allocate(fields.size(), 0, 0);
    for (int i=0;i<fields.size();i++) {
      SQLField sqlField = fields.get(i);
      meta.getSelectName()[i] = sqlField.getField(); 
      meta.getSelectRename()[i] = sqlField.getAlias();
    }
    
    StepMeta stepMeta = new StepMeta("Select values", meta);
    stepMeta.setLocation(xLocation, 50);
    xLocation+=100;
    stepMeta.setDraw(true);
    return stepMeta;
  }

  public SQL getSql() {
    return sql;
  }

  /**
   * @return the injectorStepName
   */
  public String getInjectorStepName() {
    return injectorStepName;
  }

  /**
   * @param injectorStepName the injectorStepName to set
   */
  public void setInjectorStepName(String injectorStepName) {
    this.injectorStepName = injectorStepName;
  }

  /**
   * @return the resultStepName
   */
  public String getResultStepName() {
    return resultStepName;
  }

  /**
   * @param resultStepName the resultStepName to set
   */
  public void setResultStepName(String resultStepName) {
    this.resultStepName = resultStepName;
  }

  /**
   * @return the rowLimit
   */
  public int getRowLimit() {
    return rowLimit;
  }  
}
