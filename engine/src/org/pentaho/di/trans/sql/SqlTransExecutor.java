package org.pentaho.di.trans.sql;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.pentaho.di.core.Condition;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.jdbc.FieldVariableMapping;
import org.pentaho.di.core.jdbc.TransDataService;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.sql.SQL;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransAdapter;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepInterface;

public class SqlTransExecutor {
  private TransMeta serviceTransMeta;
  private String serviceStepName;
  private String sqlQuery;

  private TransMeta genTransMeta;
  
  private Trans serviceTrans;
  private Trans genTrans;
  
  private RowMetaInterface serviceFields;
  private List<TransDataService> services;
  private String serviceName;
  private TransDataService service;
  private SQL sql;
  private Repository repository;
  private RowMetaInterface resultStepFields;
  private int rowLimit;
  private Map<String, String> parameters;
  private List<String> parameterNames;
  private String resultStepName;
  private DecimalFormat sqlNumericFormat;
  private SimpleDateFormat sqlDateFormat;
  private SimpleDateFormat jsonDateFormat;

  /**
   * Create a new SqlTransExecutor without parameters
   * @param sqlQuery
   * @param services
   * @throws KettleException 
   */
  public SqlTransExecutor(String sqlQuery, List<TransDataService> services) throws KettleException {
    this(sqlQuery, services, new HashMap<String, String>(), null, 0);
  }
  
  
  /**
   * @param sqlQuery
   * @param services
   * @param parameters
   * 
   * @throws KettleException 
   */
  public SqlTransExecutor(String sqlQuery, List<TransDataService> services, Map<String, String> parameters) throws KettleException {
    this(sqlQuery, services, parameters, null, 0);
  }
  
  /**
   * @param sqlQuery
   * @param services
   * @param repository
   * @throws KettleException 
   */
  public SqlTransExecutor(String sqlQuery, List<TransDataService> services, Map<String, String> parameters, Repository repository, int rowLimit) throws KettleException {
    this.sqlQuery = sqlQuery;
    this.services = services;
    this.parameters = parameters;
    this.repository = repository;
    this.rowLimit = rowLimit;
    
    
    sqlNumericFormat = new DecimalFormat("0.#");
    sqlNumericFormat.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
    sqlNumericFormat.setGroupingUsed(false);

    sqlDateFormat = new SimpleDateFormat("'\"'yyyy/MM/dd HH:mm:ss'\"'");
    jsonDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); //  "2010-01-01T00:00:00Z"
    
    prepareExecution();
  }
  
  private void prepareExecution() throws KettleException {
    sql = new SQL(sqlQuery);
    serviceName = sql.getServiceName();
    
    // Dual
    if (Const.isEmpty(serviceName) || "dual".equalsIgnoreCase(serviceName)) {
      service = new TransDataService("dual");
      service.setDual(true);
      serviceFields = new RowMeta(); // nothing to report from dual
    } else {
      service = findService(serviceName);
      
      if (service==null) {
        throw new KettleException("Unable to find service with name '"+service+"' and SQL: "+sqlQuery);
      }

      // TODO: allow for repository transformation loading...
      //
      serviceTransMeta = loadTransMeta(repository);
      serviceTransMeta.setName(calculateTransname(sql, true));
      
      serviceTransMeta.activateParameters();
      
      // The dummy step called "Output" provides the output fields...
      //
      serviceStepName = service.getServiceStepName();
      serviceFields = serviceTransMeta.getStepFields(serviceStepName);
    }
  }

  private void setAutomaticParameterValues() throws UnknownParamException, KettleValueException {
    if (sql.getWhereCondition()==null) {
      return; // nothing to do here.
    }
    Condition condition = sql.getWhereCondition().getCondition();
    
    for (FieldVariableMapping mapping : service.getFieldVariableMappings()) {
      
      switch(mapping.getMappingType()) {
      case SQL_WHERE:
        {
          String sql = "WHERE "+convertConditionToSql(condition);
          serviceTransMeta.setParameterValue(mapping.getVariableName(), sql);
        }
        break;
      case JSON_QUERY:
        {
          String json = "{ "+convertConditionToJson(condition)+" }";
          serviceTransMeta.setParameterValue(mapping.getVariableName(), json);
        }
        break;
     }
    }
  }

  private String convertAtomicConditionToSql(Condition atomicCondition) throws KettleValueException {
    StringBuilder sql = new StringBuilder();
    
    String fieldName = atomicCondition.getLeftValuename();
    FieldVariableMapping mapping = FieldVariableMapping.findFieldVariableMappingByFieldName(service.getFieldVariableMappings(), atomicCondition.getLeftValuename());
    if (mapping!=null) {
      fieldName=mapping.getTargetName();
    }
    sql.append(fieldName).append(" ");
    
    switch(atomicCondition.getFunction()) {
    case Condition.FUNC_EQUAL: sql.append("="); break;
    case Condition.FUNC_NOT_EQUAL: sql.append("<>"); break;
    case Condition.FUNC_LARGER: sql.append(">"); break;
    case Condition.FUNC_LARGER_EQUAL: sql.append(">="); break;
    case Condition.FUNC_SMALLER: sql.append("<"); break;
    case Condition.FUNC_SMALLER_EQUAL: sql.append("<="); break;
    case Condition.FUNC_NULL: sql.append("IS NULL"); break;
    case Condition.FUNC_NOT_NULL: sql.append("IS NOT NULL"); break;
    case Condition.FUNC_CONTAINS: sql.append("LIKE"); break;
    }
    sql.append(" ");
    sql.append(getAtomicConditionRightSql(atomicCondition));
    
    return sql.toString();
  }
  
  private String getAtomicConditionRightSql(Condition atomicCondition) throws KettleValueException {
    ValueMetaAndData right = atomicCondition.getRightExact();
    if (right==null) {
      // right value
      // 
      String rightName = atomicCondition.getRightValuename(); 
      FieldVariableMapping mapping = FieldVariableMapping.findFieldVariableMappingByFieldName(service.getFieldVariableMappings(), atomicCondition.getRightValuename());
      if (mapping!=null) {
        rightName = mapping.getVariableName();
      }
      return rightName;
    }
    if (right.getValueMeta().isNull(right.getValueData())) {
      return "NULL";
    }
    switch(right.getValueMeta().getType()) {
    case ValueMetaInterface.TYPE_STRING: 
      return "'"+right.toString()+"'";
    case ValueMetaInterface.TYPE_NUMBER: 
    case ValueMetaInterface.TYPE_INTEGER: 
    case ValueMetaInterface.TYPE_BIGNUMBER: 
        return sqlNumericFormat.format(right.getValueMeta().convertToNormalStorageType(right.getValueData()));
    case ValueMetaInterface.TYPE_DATE: 
      return sqlDateFormat.format(right.getValueMeta().getDate(right.getValueData()));
    case ValueMetaInterface.TYPE_BOOLEAN:
      return right.getValueMeta().getBoolean(right.getValueData()) ? "TRUE" : "FALSE";
    default:
      throw new KettleValueException("Unsupported conversion of value from "+right.getValueMeta().toStringMeta()+" to SQL");
    }
  }


  protected String convertConditionToSql(Condition condition) throws KettleValueException {
    if (condition.isAtomic()) {
      return convertAtomicConditionToSql(condition);
    }
    StringBuilder sql = new StringBuilder();
    if (condition.isNegated()) {
      sql.append("NOT(");
    }
    for (int i=0;i<condition.nrConditions();i++) {
      Condition c = condition.getCondition(i);
      if (i>0) {
        sql.append(" ").append(c.getOperatorDesc());
      }
      sql.append("(");
      sql.append(convertConditionToSql(c));
      sql.append(")");
    }
    
    if (condition.isNegated()) {
      sql.append(")");
    }
    return sql.toString();
  }
  
  private String convertAtomicConditionToJson(Condition atomicCondition) throws KettleValueException {
    StringBuilder sql = new StringBuilder();
    
    if (atomicCondition.getRightValuename()!=null) {
      throw new KettleValueException("Converting a condition that compares 2 fields is not yet supported in a JSON query");
    }
    
    String fieldName = atomicCondition.getLeftValuename();
    FieldVariableMapping mapping = FieldVariableMapping.findFieldVariableMappingByFieldName(service.getFieldVariableMappings(), atomicCondition.getLeftValuename());
    if (mapping!=null) {
      fieldName=mapping.getTargetName();
    }
    
    switch(atomicCondition.getFunction()) {
    case Condition.FUNC_EQUAL: sql.append("'").append(fieldName).append("' : ").append(getJsonString(atomicCondition.getRightExact())); break;
    case Condition.FUNC_NOT_EQUAL: sql.append("'").append(fieldName).append("' : { '$ne' : ").append(getJsonString(atomicCondition.getRightExact())).append(" }"); break;
    case Condition.FUNC_LARGER: sql.append(">"); sql.append("'").append(fieldName).append("' : { '$gt' : ").append(getJsonString(atomicCondition.getRightExact())).append(" }"); break;
    case Condition.FUNC_LARGER_EQUAL: sql.append(">"); sql.append("'").append(fieldName).append("' : { '$gte' : ").append(getJsonString(atomicCondition.getRightExact())).append(" }"); break;
    case Condition.FUNC_SMALLER: sql.append("<"); sql.append(">"); sql.append("'").append(fieldName).append("' : { '$lt' : ").append(getJsonString(atomicCondition.getRightExact())).append(" }"); break;
    case Condition.FUNC_SMALLER_EQUAL: sql.append("<="); sql.append(">"); sql.append("'").append(fieldName).append("' : { '$lte' : ").append(getJsonString(atomicCondition.getRightExact())).append(" }"); break;
    case Condition.FUNC_NULL: sql.append("IS NULL"); sql.append(">"); sql.append("'").append(fieldName).append("' : \"\""); break;
    case Condition.FUNC_NOT_NULL: sql.append("'").append(fieldName).append("' : { '$ne' : ").append("\"\"").append(" }"); break;
    case Condition.FUNC_CONTAINS: sql.append("'").append(fieldName).append("' : { '$regex' : '.*").append(atomicCondition.getRightExactString()).append(".*', '$options' : 'i' }"); break;
    }
    
    return sql.toString();
  }
  
  protected String getJsonString(ValueMetaAndData v) throws KettleValueException {
    ValueMetaInterface meta = v.getValueMeta();
    Object data = v.getValueData();
    
    switch(meta.getType()) {
    case ValueMetaInterface.TYPE_STRING: return '"'+meta.getString(data)+'"';
    case ValueMetaInterface.TYPE_NUMBER: return sqlNumericFormat.format(meta.getNumber(data));
    case ValueMetaInterface.TYPE_INTEGER: return sqlNumericFormat.format(meta.getInteger(data));
    case ValueMetaInterface.TYPE_BIGNUMBER: return sqlNumericFormat.format(meta.getBigNumber(data));
    case ValueMetaInterface.TYPE_DATE: return "{ $date : \""+jsonDateFormat.format(meta.getBigNumber(data))+"\" }";
    default:
      throw new KettleValueException("Converting data type "+meta.toStringMeta()+" to a JSON value is not yet supported");
    }
  }
  
  protected String convertConditionToJson(Condition condition) throws KettleValueException {
    if (condition.isAtomic()) {
      return convertAtomicConditionToJson(condition);
    }
    StringBuilder sql = new StringBuilder();
    if (condition.isNegated()) {
      throw new KettleValueException("Negated conditions can't be converted to JSON");
    }

    for (int i=0;i<condition.nrConditions();i++) {
      Condition c = condition.getCondition(i);
      if (i>0) {
        sql.append(", ");
      }
      sql.append(convertConditionToJson(c));
    }
    
    return sql.toString();
  }
  


  protected void extractAtomicConditions(Condition condition, List<Condition> atomicConditions) {
    if (condition.isAtomic()) {
      atomicConditions.add(condition);
    } else {
      for (Condition sub : condition.getChildren()) {
        extractAtomicConditions(sub, atomicConditions);
      }
    }
  }


  private TransDataService findService(String name) {
    for (TransDataService s : services) {
      if (s.getName().equalsIgnoreCase(name)) return s;
    }
    return null;
  }


  private void extractConditionParameters(Condition condition, Map<String, String> map) {

    if (condition.isAtomic()) {
      if (condition.getFunction()==Condition.FUNC_TRUE) {
        map.put(condition.getLeftValuename(), condition.getRightExactString());
      }
    } else {
      for (Condition sub : condition.getChildren()) {
        extractConditionParameters(sub, map);
      }
    }
  }


  public void executeQuery(RowListener resultRowListener) throws KettleException {
    // Continue parsing of the SQL, map to fields, extract conditions, parameters, ...
    //
    sql.parse(serviceFields);

    parameterNames = new ArrayList<String>();

    if (!service.isDual()) {
      // Parameters: see which ones are defined in the SQL
      //
      Map<String, String> conditionParameters = new HashMap<String, String>();
      if (sql.getWhereCondition()!=null) {
        extractConditionParameters(sql.getWhereCondition().getCondition(), conditionParameters);
      }
      parameters.putAll(conditionParameters); // overwrite the defaults for this query
      
      for (String name : conditionParameters.keySet()) {
        serviceTransMeta.setParameterValue(name, conditionParameters.get(name));
      }
      
      // Activate parameters determined through the field-variable mapping
      // This is push-down optimization version 1.0
      //
      setAutomaticParameterValues();
      
      serviceTransMeta.activateParameters();
      
      // Prepare the execution of this service transformation
      //
      serviceTrans = new Trans(serviceTransMeta);
      serviceTrans.prepareExecution(null);

      for (String parameterName : serviceTransMeta.listParameters()) {
        parameterNames.add(parameterName);
      }
    }
    
    // Generate a transformation
    //
    SqlTransMeta sqlTransMeta = new SqlTransMeta(sql, rowLimit);
    genTransMeta = sqlTransMeta.generateTransMeta();
    resultStepName = sqlTransMeta.getResultStepName();
    
    // Prepare execution of the generated transformation
    //
    genTrans = new Trans(genTransMeta);
    genTrans.prepareExecution(null);

    if (!service.isDual()) {
      // This is where we will inject the rows from the service transformation step
      //
      final RowProducer rowProducer = genTrans.addRowProducer(sqlTransMeta.getInjectorStepName(), 0);
  
      // Now connect the 2 transformations with listeners and injector
      //
      StepInterface serviceStep = serviceTrans.findRunThread(serviceStepName);
      serviceStep.addRowListener(new RowAdapter() { @Override
      public void rowWrittenEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException {
        // Simply pass along the row to the other transformation (to the Injector step)
        //
        LogChannelInterface log = serviceTrans.getLogChannel();
        try {
          if (log.isRowLevel()) {
            log.logRowlevel("Passing along row: "+rowMeta.getString(row));
          }
        } catch (KettleValueException e) {
        }
  
        rowProducer.putRow(rowMeta, row);
      } });
    
      // Let the other transformation know when there are no more rows
      //
      serviceTrans.addTransListener(new TransAdapter() {
        @Override
        public void transFinished(Trans trans) throws KettleException {
          rowProducer.finished();
        }
      });
    }
      
    // Give back the eventual result rows...
    //
    StepInterface resultStep = genTrans.findRunThread(sqlTransMeta.getResultStepName());
    resultStep.addRowListener(resultRowListener);    
    
    // Get the result row metadata
    //
    resultStepFields = genTransMeta.getStepFields(sqlTransMeta.getResultStepName());
    
    // Start both transformations
    //
    genTrans.startThreads();
    if (!service.isDual()) {
      serviceTrans.startThreads();   
    }
  }
  
  private TransMeta loadTransMeta(Repository repository) throws KettleException {
    TransMeta transMeta = null;
    
    if (!Const.isEmpty(service.getFileName())) {
      try {
        // OK, load the meta-data from file...
        //
        // Don't set internal variables: they belong to the parent thread!
        //
        transMeta = new TransMeta(service.getFileName(), false); 
        transMeta.getLogChannel().logDetailed("Service transformation was loaded from XML file [" + service.getFileName()+ "]");
      } catch (Exception e) {
        throw new KettleException("Unable to load service transformation for service '"+serviceName+"'", e);
      }
    } else {
      throw new KettleException("Loading from a repository is not supported yet (or the filename is not specified for a service)");
    }
    return transMeta;
  }

  public void waitUntilFinished() {
    if (!service.isDual()) {
      serviceTrans.waitUntilFinished();
    }
    genTrans.waitUntilFinished();
  }

  /**
   * @return the serviceTransMeta
   */
  public TransMeta getServiceTransMeta() {
    return serviceTransMeta;
  }

  /**
   * @return the serviceStepName
   */
  public String getServiceStepName() {
    return serviceStepName;
  }

  /**
   * @return the sqlQuery
   */
  public String getSqlQuery() {
    return sqlQuery;
  }

  /**
   * @return the genTransMeta
   */
  public TransMeta getGenTransMeta() {
    return genTransMeta;
  }

  /**
   * @return the serviceTrans
   */
  public Trans getServiceTrans() {
    return serviceTrans;
  }

  /**
   * @return the genTrans
   */
  public Trans getGenTrans() {
    return genTrans;
  }

  /**
   * @return the serviceFields
   */
  public RowMetaInterface getServiceFields() {
    return serviceFields;
  }

  /**
   * @return the serviceName
   */
  public String getServiceName() {
    return serviceName;
  }

  /**
   * @return the resultStepFields
   */
  public RowMetaInterface getResultStepFields() {
    return resultStepFields;
  }

  /**
   * @param resultStepFields the resultStepFields to set
   */
  public void setResultStepFields(RowMetaInterface resultStepFields) {
    this.resultStepFields = resultStepFields;
  }

  /**
   * @return the rowLimit
   */
  public int getRowLimit() {
    return rowLimit;
  }
  
  /**
   * Calculate the name of the generated transformation based on the SQL
   * @return the generated name;
   */
  public static String calculateTransname(SQL sql, boolean isService) {
    StringBuilder sbsql = new StringBuilder(sql.getServiceName()+" - "+(isService?"Service data":"Execute SQL")+" - "+sql.getSqlString());
    for (int i=sbsql.length()-1;i>=0;i--)
    {
      if (sbsql.charAt(i)=='\n' || sbsql.charAt(i)=='\r') sbsql.setCharAt(i, ' ');
    }
    return sbsql.toString();
  }


  /**
   * @return the services
   */
  public List<TransDataService> getServices() {
    return services;
  }


  /**
   * @param servicesMap the servicesMap to set
   */
  public void setServices(List<TransDataService> services) {
    this.services = services;
  }


  /**
   * @return the sql
   */
  public SQL getSql() {
    return sql;
  }


  /**
   * @return the parameterNames
   */
  public List<String> getParameterNames() {
    return parameterNames;
  }


  /**
   * @return the resultStepName
   */
  public String getResultStepName() {
    return resultStepName;
  }
}
