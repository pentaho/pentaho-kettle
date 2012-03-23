package org.pentaho.di.starmodeler.generator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobHopMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.sql.JobEntrySQL;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.starmodeler.AttributeType;
import org.pentaho.di.starmodeler.ConceptUtil;
import org.pentaho.di.starmodeler.DefaultIDs;
import org.pentaho.di.starmodeler.DimensionType;
import org.pentaho.di.starmodeler.StarDomain;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.combinationlookup.CombinationLookupMeta;
import org.pentaho.di.trans.steps.dimensionlookup.DimensionLookupMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.concept.types.TableType;

/**
 * The job generator creates a template job based on a star domain.
 * It creates one job to create all possible target dimensions, fact table, lookup indexes and so forth.
 * 
 * @author matt
 *
 */
public class JobGenerator {
  private static Class<?> PKG = JobGenerator.class; // for i18n

  
  protected StarDomain starDomain;
  protected Repository repository;
  protected RepositoryDirectoryInterface targetDirectory;
  protected String locale;
  protected Domain domain;
  protected List<DatabaseMeta> databases;

  private static int GRAPH_MARGIN = 250;
  private static int GRAPH_TOP = 100;
  private static int GRAPH_LEFT   = 100;
  private static int GRAPH_MAX_WIDTH  = 1000;

  /**
   * @param starDomain
   * @param repository
   * @param targetDirectory
   * @param databases the list of shared database connections to reference for source and target databases.
   * @param locale
   */
  public JobGenerator(StarDomain starDomain, Repository repository, RepositoryDirectoryInterface targetDirectory, List<DatabaseMeta> databases, String locale) {
    this.starDomain = starDomain;
    this.repository = repository;
    this.targetDirectory = targetDirectory;
    this.databases = databases;
    this.locale = locale;
    
    this.domain = starDomain.getDomain();
  }
  
  public JobMeta generateSqlJob() throws KettleException {
    DatabaseMeta databaseMeta = findTargetDatabaseMeta();
    Database db = new Database(Spoon.loggingObject, databaseMeta);
    
    try {
      db.connect();
      
      JobMeta jobMeta = new JobMeta();
      
      jobMeta.setName("Create tables for '"+ConceptUtil.getName(domain, locale)+"'");
      jobMeta.setDescription(ConceptUtil.getDescription(domain, locale));
      
      // Let's not forget to add the database connection
      //
      jobMeta.addDatabase(databaseMeta);
      
      Point location = new Point(GRAPH_LEFT, GRAPH_TOP);
  
      // Create a job entry
      //
      JobEntryCopy startEntry = JobMeta.createStartEntry();
      startEntry.setLocation(location.x, location.y);
      startEntry.setDrawn();
      jobMeta.addJobEntry(startEntry);
      JobEntryCopy lastEntry = startEntry;
      nextLocation(location);
      
      // Create one SQL entry for all the physically unique dimensions and facts
      // We need to get a list of all known dimensions with physical table name.
      //
      List<LogicalTable> tables = getUniqueLogicalTables();
      for (LogicalTable logicalTable : tables) {
        String phTable = ConceptUtil.getString(logicalTable, DefaultIDs.LOGICAL_TABLE_PHYSICAL_TABLE_NAME);
        String tableName = ConceptUtil.getName(logicalTable, locale);
        String tableDescription = ConceptUtil.getDescription(logicalTable, locale);
        TableType tableType = ConceptUtil.getTableType(logicalTable);
        DimensionType dimensionType = ConceptUtil.getDimensionType(logicalTable);
        boolean isFact = tableType==TableType.FACT;
        boolean isDimension = tableType==TableType.DIMENSION;
        boolean isJunk = isDimension && dimensionType==DimensionType.JUNK_DIMENSION;
        
        JobEntrySQL sqlEntry = new JobEntrySQL(phTable);
        sqlEntry.setDatabase(databaseMeta);
        
        // Get the SQL for this table...
        //
        String schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, phTable);
        String phKeyField = null;
        
        // The technical key is the first KEY field...
        //
        LogicalColumn keyColumn = null;
        if (isDimension) {
          keyColumn = ConceptUtil.findLogicalColumn(logicalTable, AttributeType.TECHNICAL_KEY);
        }
        if (keyColumn!=null) {
          phKeyField = ConceptUtil.getString(keyColumn, DefaultIDs.LOGICAL_COLUMN_PHYSICAL_COLUMN_NAME);
        }

        // Get all the fields for the logical table...
        //
        RowMetaInterface fields = getRowForLogicalTable(databaseMeta, logicalTable);
        
        // Generate the required SQL to make this happen
        //
        String sql = db.getCreateTableStatement(schemaTable, fields, phKeyField, databaseMeta.supportsAutoinc() && !isFact, null, true);
        
        // Also generate an index for the technical key field
        //
        if (keyColumn!=null) {
          ValueMetaInterface keyValueMeta = getValueForLogicalColumn(databaseMeta, keyColumn);
          String indexName = databaseMeta.quoteField( "IDX_" + phTable.replace(" ", "_").toUpperCase() + "_" + phKeyField.toUpperCase() );
          String indexSql = db.getCreateIndexStatement(schemaTable, indexName, new String[] { keyValueMeta.getName(), }, true, false, true, true);
          sql+=Const.CR+indexSql;
        }
        
        // In case it's a fact table generate an index for each TK column
        //
        if (isFact) {
          List<LogicalColumn> fks = ConceptUtil.findLogicalColumns(logicalTable, AttributeType.TECHNICAL_KEY);
          for (LogicalColumn fk : fks) {
            ValueMetaInterface keyValueMeta = getValueForLogicalColumn(databaseMeta, fk);
            String phColumn = ConceptUtil.getString(fk, DefaultIDs.LOGICAL_COLUMN_PHYSICAL_COLUMN_NAME);
            if (!Const.isEmpty(phColumn)) {
              String indexName = databaseMeta.quoteField( "IDX_" + phTable.replace(" ", "_").toUpperCase() + "_" + phColumn.toUpperCase() );
              String indexSql = db.getCreateIndexStatement(schemaTable, indexName, new String[] { keyValueMeta.getName(), }, true, false, true, true);
              sql+=Const.CR+indexSql;
            }
          }
        }
        
        // Put an index on all natural keys too...
        //
        if (isDimension) {
          List<LogicalColumn> naturalKeys = ConceptUtil.findLogicalColumns(logicalTable, AttributeType.NATURAL_KEY);
          if (!naturalKeys.isEmpty()) {
            String indexName = databaseMeta.quoteField( "IDX_" + phTable.replace(" ", "_").toUpperCase() + "_LOOKUP" );
            String[] fieldNames = new String[naturalKeys.size()];
            for (int i=0;i<fieldNames.length;i++) {
              ValueMetaInterface keyValueMeta = getValueForLogicalColumn(databaseMeta, naturalKeys.get(i));
              fieldNames[i] = keyValueMeta.getName();
            }
            String indexSql = db.getCreateIndexStatement(schemaTable, indexName, fieldNames, false, false, false, true);
            sql+=Const.CR+indexSql;
          }
        }
        if (isJunk) {
          List<LogicalColumn> attributes = ConceptUtil.findLogicalColumns(logicalTable, AttributeType.ATTRIBUTE);
          if (!attributes.isEmpty()) {
            String indexName = databaseMeta.quoteField( "IDX_" + phTable.replace(" ", "_").toUpperCase() + "_LOOKUP" );
            String[] fieldNames = new String[attributes.size()];
            for (int i=0;i<fieldNames.length;i++) {
              ValueMetaInterface attrValueMeta = getValueForLogicalColumn(databaseMeta, attributes.get(i));
              fieldNames[i] = attrValueMeta.getName();
            }
            String indexSql = db.getCreateIndexStatement(schemaTable, indexName, fieldNames, false, false, false, true);
            sql+=Const.CR+indexSql;
          }
        }
        
        
        // If it's 
        
        sqlEntry.setSQL(sql);
        
        sqlEntry.setDescription("Generated based on logical table '"+tableName+"'"+Const.CR+Const.CR+Const.NVL(tableDescription, ""));
        
        JobEntryCopy sqlCopy = new JobEntryCopy(sqlEntry);
        sqlCopy.setLocation(location.x, location.y);
        sqlCopy.setDrawn();
        nextLocation(location);
        jobMeta.addJobEntry(sqlCopy);
        
        // Hook up with the previous job entry too...
        //
        JobHopMeta jobHop = new JobHopMeta(lastEntry, sqlCopy);
        jobHop.setEnabled();
        jobHop.setConditional();
        jobHop.setEvaluation(true);
        if (lastEntry.isStart()) {
          jobHop.setUnconditional();
        }
        
        jobMeta.addJobHop(jobHop);
        lastEntry = sqlCopy;
      }
      
      return jobMeta;
    } catch(Exception e) {
      throw new KettleException("There was an error during the generation of the SQL job", e);
    } finally {
      if (db!=null) {
        db.disconnect();
      }
    }
  }
  
  protected DatabaseMeta findTargetDatabaseMeta() throws KettleException {
    
    String targetDbName = ConceptUtil.getString(starDomain.getDomain(), DefaultIDs.DOMAIN_TARGET_DATABASE);
    if (Const.isEmpty(targetDbName)) {
      throw new KettleException(BaseMessages.getString(PKG, "LogicalModelerPerspective.MessageBox.NoTargetDBSpecified.Message"));
    }
    DatabaseMeta databaseMeta = DatabaseMeta.findDatabase(databases, targetDbName);
    if (databaseMeta==null) {
      throw new KettleException(BaseMessages.getString(PKG, "LogicalModelerPerspective.MessageBox.TargetDBNotFound.Message", targetDbName));
    }
    return databaseMeta;
  }
  
  protected DatabaseMeta findSourceDatabaseMeta(String databaseName) throws KettleException {
    
    DatabaseMeta databaseMeta = DatabaseMeta.findDatabase(databases, databaseName);
    if (databaseMeta==null) {
      throw new KettleException(BaseMessages.getString(PKG, "LogicalModelerPerspective.MessageBox.SourceDBNotFound.Message", databaseName));
    }
    return databaseMeta;
  }

  private RowMetaInterface getRowForLogicalTable(DatabaseMeta databaseMeta, LogicalTable logicalTable) {
    RowMetaInterface fields = new RowMeta();
    for (LogicalColumn column : logicalTable.getLogicalColumns()) {
      ValueMetaInterface valueMeta = getValueForLogicalColumn(databaseMeta, column);
      fields.addValueMeta(valueMeta);
    }
    return fields;
  }
  
  private ValueMetaInterface getValueForLogicalColumn(DatabaseMeta databaseMeta, LogicalColumn column) {
    String columnName = ConceptUtil.getName(column, locale);
    String phColumnName = ConceptUtil.getString(column, DefaultIDs.LOGICAL_COLUMN_PHYSICAL_COLUMN_NAME);
    DataType columnType = column.getDataType();
    String lengthString = ConceptUtil.getString(column, DefaultIDs.LOGICAL_COLUMN_LENGTH);
    int length = Const.toInt(lengthString, -1);
    String precisionString = ConceptUtil.getString(column, DefaultIDs.LOGICAL_COLUMN_PRECISION);
    int precision = Const.toInt(precisionString, -1);

    int type=ValueMetaInterface.TYPE_STRING;
    switch(columnType) {
    case UNKNOWN:
    case URL:
    case STRING: precision=-1; break;
    case IMAGE: 
    case BINARY: type = ValueMetaInterface.TYPE_BINARY; precision=-1; break;
    case BOOLEAN: type = ValueMetaInterface.TYPE_BOOLEAN; length=-1; precision=-1; break;
    case DATE: type = ValueMetaInterface.TYPE_DATE; length=-1; precision=-1; break;
    case NUMERIC: 
      if (precision<=0 && length<15) {
        type = ValueMetaInterface.TYPE_INTEGER;
      } else {
        if (length>=15) {
          type = ValueMetaInterface.TYPE_BIGNUMBER;
        } else {
          type = ValueMetaInterface.TYPE_NUMBER;
        }
      }
      break;
    }
    ValueMetaInterface value = new ValueMeta(databaseMeta.quoteField(Const.NVL(phColumnName, columnName)), type);
    value.setLength(length, precision);
    return value;
  }

  /**
   * Get a list of all unique physical table names wrapped in their logical tables
   * @return
   */
  protected List<LogicalTable> getUniqueLogicalTables() {
    List<LogicalTable> tables = new ArrayList<LogicalTable>();
    List<String> phTabs = new ArrayList<String>();
    for (LogicalModel model : domain.getLogicalModels()) {
      for (LogicalTable table : model.getLogicalTables()) {
        String phTable = ConceptUtil.getString(table, DefaultIDs.LOGICAL_TABLE_PHYSICAL_TABLE_NAME);
        if (!Const.isEmpty(phTable)) {
          if (!phTabs.contains(phTable)) {
            phTabs.add(phTable);
            tables.add(table);
          }
        }
      }
    }
    
    return tables;
  }

  /**
   * Calculate the next location for a job entry to be placed.
   * 
   * @param location
   */
  private void nextLocation(Point location) {
    location.x += GRAPH_MARGIN;
    if (location.x >= GRAPH_MAX_WIDTH) {
      location.x = GRAPH_LEFT;
      location.y += 150;
    }
  }

  /**
   * This method generates a list of transformations: one for each dimension.
   * 
   * @return the list of generated transformations
   */
  public List<TransMeta> generateDimensionTransformations() throws KettleException {
    DatabaseMeta databaseMeta = findTargetDatabaseMeta();
    
    List<TransMeta> transMetas = new ArrayList<TransMeta>();
    
    List<LogicalTable> logicalTables = getUniqueLogicalTables();
    
    for (LogicalTable logicalTable : logicalTables) {
      TableType tableType = ConceptUtil.getTableType(logicalTable);
      DimensionType dimensionType = ConceptUtil.getDimensionType(logicalTable);
      if (tableType == TableType.DIMENSION) {
        switch(dimensionType) {
        case SLOWLY_CHANGING_DIMENSION:
        case JUNK_DIMENSION:
          {
            TransMeta transMeta = generateDimensionTransformation(databaseMeta, logicalTable);
            transMetas.add(transMeta);
          }
          break;
        case DATE: // TODO: generate a standard date transformation
          {
            TransMeta transMeta = generateDateTransformation(databaseMeta, logicalTable);
            transMetas.add(transMeta);
          }
          break;
        case TIME: // TODO: generate a standard time transformation
          {
            TransMeta transMeta = generateTimeTransformation(databaseMeta, logicalTable);
            transMetas.add(transMeta);
          }
          break;
        }
      }
    }
    
    return transMetas;
  }

  private TransMeta generateDateTransformation(DatabaseMeta databaseMeta, LogicalTable logicalTable) throws KettleException {
    // We actually load the transformation from a template and then slightly modify it.
    //
    String filename = "/org/pentaho/di/resources/Generate date dimension.ktr";
    InputStream inputStream = getClass().getResourceAsStream(filename);
    TransMeta transMeta = new TransMeta(inputStream, Spoon.getInstance().rep, true, new Variables(), null);
    
    // Find the table output step and inject the target table name and database...
    //
    StepMeta stepMeta = transMeta.findStep("TARGET");
    if (stepMeta!=null) {
      TableOutputMeta meta = (TableOutputMeta) stepMeta.getStepMetaInterface();
      meta.setDatabaseMeta(databaseMeta);
      String phTable = ConceptUtil.getString(logicalTable, DefaultIDs.LOGICAL_TABLE_PHYSICAL_TABLE_NAME);
      meta.setTablename(phTable);
    }
    
    return transMeta;
  }

  private TransMeta generateTimeTransformation(DatabaseMeta databaseMeta, LogicalTable logicalTable) throws KettleException {
    // We actually load the transformation from a template and then slightly modify it.
    //
    String filename = "/org/pentaho/di/resources/Generate time dimension.ktr";
    InputStream inputStream = getClass().getResourceAsStream(filename);
    TransMeta transMeta = new TransMeta(inputStream, Spoon.getInstance().rep, true, new Variables(), null);
    
    // Find the table output step and inject the target table name and database...
    //
    StepMeta stepMeta = transMeta.findStep("TARGET");
    if (stepMeta!=null) {
      TableOutputMeta meta = (TableOutputMeta) stepMeta.getStepMetaInterface();
      meta.setDatabaseMeta(databaseMeta);
      String phTable = ConceptUtil.getString(logicalTable, DefaultIDs.LOGICAL_TABLE_PHYSICAL_TABLE_NAME);
      meta.setTablename(phTable);
    }
    
    return transMeta;
  }

  /**
   * Generates a template 
   * @param databaseMeta 
   * @param logicalModel
   * @return
   */
  public TransMeta generateDimensionTransformation(DatabaseMeta databaseMeta, LogicalTable logicalTable) {
    TransMeta transMeta = new TransMeta();
    
    String tableName = ConceptUtil.getName(logicalTable, locale);
    String tableDescription = ConceptUtil.getDescription(logicalTable, locale);
    DimensionType dimensionType = ConceptUtil.getDimensionType(logicalTable);
    
    transMeta.setName("Update dimension '"+tableName+"'");
    transMeta.setDescription(tableDescription);
    
    // Let's not forget to add the target database
    // 
    transMeta.addDatabase(databaseMeta);
    
    Point location = new Point(GRAPH_LEFT, GRAPH_TOP);
    
    // Find all the source columns and source tables and put them into a table input step...
    //
    StepMeta inputStep = generateTableInputStepFromLogicalTable(logicalTable);
    DatabaseMeta sourceDatabaseMeta = ((TableInputMeta)inputStep.getStepMetaInterface()).getDatabaseMeta();
    if (sourceDatabaseMeta!=null) transMeta.addOrReplaceDatabase(sourceDatabaseMeta);
    inputStep.setLocation(location.x, location.y);
    nextLocation(location);
    transMeta.addStep(inputStep);
    StepMeta lastStep = inputStep;
    
    // Generate an dimension lookup/update step for each table
    //
    StepMeta dimensionStep;
    if (dimensionType==DimensionType.SLOWLY_CHANGING_DIMENSION) {
      dimensionStep = generateDimensionLookupStepFromLogicalTable(databaseMeta, logicalTable);
    } else {
      dimensionStep = generateCombinationLookupStepFromLogicalTable(databaseMeta, logicalTable);
    }
    dimensionStep.setLocation(location.x, location.y);
    nextLocation(location);
    transMeta.addStep(dimensionStep);
    
    TransHopMeta transHop = new TransHopMeta(lastStep, dimensionStep);
    transMeta.addTransHop(transHop);
    
    return transMeta;
  }

  private StepMeta generateTableInputStepFromLogicalTable(LogicalTable logicalTable) {
    
    String name = ConceptUtil.getName(logicalTable, locale);
    String description = ConceptUtil.getDescription(logicalTable, locale);
    
    TableInputMeta meta = new TableInputMeta();
    
    // Source database, retain first
    // Source table, retain first
    // Source columns, retain all
    //
    DatabaseMeta sourceDatabaseMeta = null;
    String sourceTable = null;
    List<String> sourceColumns = new ArrayList<String>();
    for (LogicalColumn column : logicalTable.getLogicalColumns()) {
      String phDb = ConceptUtil.getString(column, DefaultIDs.LOGICAL_COLUMN_SOURCE_DB);
      String phTable = ConceptUtil.getString(column, DefaultIDs.LOGICAL_COLUMN_SOURCE_TABLE);
      String phCol = ConceptUtil.getString(column, DefaultIDs.LOGICAL_COLUMN_SOURCE_COLUMN);
      if (!Const.isEmpty(phDb) && sourceDatabaseMeta==null) {
        sourceDatabaseMeta = DatabaseMeta.findDatabase(databases, phDb);
      }
      if (!Const.isEmpty(phTable)) {
        sourceTable = phDb;
      }
      if (!Const.isEmpty(phCol)) {
        sourceColumns.add(phCol);
      }
    }
    String sql = "SELECT * FROM --< Source query for dimension '"+name+"'";
    
    meta.setDatabaseMeta(sourceDatabaseMeta);
    
    if (sourceDatabaseMeta!=null && !Const.isEmpty(sourceTable)) {
      sql = "SELECT ";
      if (sourceColumns.isEmpty()) {
        sql+=" * ";
      } else {
        sql+=Const.CR;
      }
      boolean first=true;
      for (String sourceColumn : sourceColumns) {
        if (first) {
          first=false;
        } else {
          sql+="      , ";
        }
        sql+=sourceDatabaseMeta.quoteField(sourceColumn)+Const.CR;
      }
      sql+="FROM "+sourceDatabaseMeta.getQuotedSchemaTableCombination(null, sourceTable);
    }
    meta.setSQL(sql);
    
    // Wrap it up...
    //
    StepMeta stepMeta = new StepMeta("Source data for '"+name+"'", meta);
    stepMeta.drawStep();
    stepMeta.setDescription("Reads data for '"+name+"' : "+description);
    
    return stepMeta;
  }

  protected StepMeta generateDimensionLookupStepFromLogicalTable(DatabaseMeta databaseMeta, LogicalTable logicalTable) {
    String name = ConceptUtil.getName(logicalTable, locale);
    String description = ConceptUtil.getDescription(logicalTable, locale);
    String phTable = ConceptUtil.getString(logicalTable, DefaultIDs.LOGICAL_TABLE_PHYSICAL_TABLE_NAME);
    String schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, Const.NVL(phTable, name));
    
    DimensionLookupMeta meta = new DimensionLookupMeta();
    meta.setDatabaseMeta(databaseMeta);
    meta.setSchemaName(null); // TODO
    meta.setTableName(schemaTable);
    meta.setAutoIncrement(databaseMeta.supportsAutoinc());
    meta.setCacheSize(5000);
    meta.setCommitSize(500);
    meta.setUpdate(true);
    
    // Find the technical key (if any defined)
    //
    LogicalColumn keyColumn = ConceptUtil.findLogicalColumn(logicalTable, AttributeType.TECHNICAL_KEY);;
    if (keyColumn!=null) {
      ValueMetaInterface keyValue = getValueForLogicalColumn(databaseMeta, keyColumn);
      meta.setKeyField(keyValue.getName());
    }

    // Simply add all the NATURAL_KEY columns...
    //
    List<LogicalColumn> naturalKeys = ConceptUtil.findLogicalColumns(logicalTable, AttributeType.NATURAL_KEY);
    meta.setKeyLookup(new String[naturalKeys.size()]);
    meta.setKeyStream(new String[naturalKeys.size()]);
    for (int i=0;i<naturalKeys.size();i++) {
      LogicalColumn logicalColumn = naturalKeys.get(i);
      ValueMetaInterface valueMeta = getValueForLogicalColumn(databaseMeta, logicalColumn);
      meta.getKeyLookup()[i] = valueMeta.getName();
      meta.getKeyStream()[i] = valueMeta.getName();
    }
    
    // All other attribute columns go in the fields tab
    //
    List<LogicalColumn> attributes = new ArrayList<LogicalColumn>();
    for (LogicalColumn logicalColumn : logicalTable.getLogicalColumns()) {
      AttributeType attributeType = ConceptUtil.getAttributeType(logicalColumn);
      if (attributeType.isAttribute()) {
          attributes.add(logicalColumn);
      }
    }
    meta.setFieldLookup(new String[attributes.size()]);
    meta.setFieldStream(new String[attributes.size()]);
    meta.setFieldUpdate(new int[attributes.size()]);
    for (int i=0;i<attributes.size();i++) {
      LogicalColumn logicalColumn = attributes.get(i);
      AttributeType attributeType = ConceptUtil.getAttributeType(logicalColumn);
      ValueMetaInterface valueMeta = getValueForLogicalColumn(databaseMeta, logicalColumn);
      meta.getFieldLookup()[i] = valueMeta.getName();
      meta.getFieldStream()[i] = valueMeta.getName();
      if (attributeType == AttributeType.ATTRIBUTE_OVERWRITE) {
        meta.getFieldUpdate()[i] = DimensionLookupMeta.TYPE_UPDATE_DIM_PUNCHTHROUGH;
      } else {
        // Historical or default: keep versions of the dimension records...
        //
        meta.getFieldUpdate()[i] = DimensionLookupMeta.TYPE_UPDATE_DIM_INSERT;
      }
    }
    
    // The version field...
    //
    LogicalColumn versionColumn = ConceptUtil.findLogicalColumn(logicalTable, AttributeType.VERSION_FIELD);
    if (versionColumn!=null) {
      String phName = ConceptUtil.getString(versionColumn, DefaultIDs.LOGICAL_COLUMN_PHYSICAL_COLUMN_NAME);
      meta.setVersionField(phName);
    }
    // Start of the date range
    //
    LogicalColumn startRangeColumn = ConceptUtil.findLogicalColumn(logicalTable, AttributeType.DATE_START);
    if (startRangeColumn!=null) {
      String phName = ConceptUtil.getString(startRangeColumn, DefaultIDs.LOGICAL_COLUMN_PHYSICAL_COLUMN_NAME);
      meta.setDateFrom(phName);
    }
    // End of the date range
    //
    LogicalColumn endRangeColumn = ConceptUtil.findLogicalColumn(logicalTable, AttributeType.DATE_END);
    if (endRangeColumn!=null) {
      String phName = ConceptUtil.getString(endRangeColumn, DefaultIDs.LOGICAL_COLUMN_PHYSICAL_COLUMN_NAME);
      meta.setDateTo(phName);
    }

    StepMeta stepMeta = new StepMeta(name, meta);
    stepMeta.drawStep();
    stepMeta.setDescription(description);

    return stepMeta;
  }
  
  protected StepMeta generateCombinationLookupStepFromLogicalTable(DatabaseMeta databaseMeta, LogicalTable logicalTable) {
    String name = ConceptUtil.getName(logicalTable, locale);
    String description = ConceptUtil.getDescription(logicalTable, locale);
    String phTable = ConceptUtil.getString(logicalTable, DefaultIDs.LOGICAL_TABLE_PHYSICAL_TABLE_NAME);
    String schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, Const.NVL(phTable, name));
    
    CombinationLookupMeta meta = new CombinationLookupMeta();
    meta.setDatabaseMeta(databaseMeta);
    meta.setSchemaName(null); // TODO
    meta.setTablename(schemaTable);
    meta.setUseAutoinc(databaseMeta.supportsAutoinc());
    meta.setCacheSize(5000);
    meta.setCommitSize(500);
    meta.setReplaceFields(true); // replace attribute fields with a TK
    
    // Find the technical key (if any defined)
    //
    LogicalColumn keyColumn = ConceptUtil.findLogicalColumn(logicalTable, AttributeType.TECHNICAL_KEY);;
    if (keyColumn!=null) {
      ValueMetaInterface keyValue = getValueForLogicalColumn(databaseMeta, keyColumn);
      meta.setTechnicalKeyField(keyValue.getName());
    }

    // Simply add all the attributes as key columns...
    //
    List<LogicalColumn> attributes = ConceptUtil.findLogicalColumns(logicalTable, AttributeType.ATTRIBUTE);
    meta.setKeyLookup(new String[attributes.size()]);
    meta.setKeyField(new String[attributes.size()]);
    for (int i=0;i<attributes.size();i++) {
      LogicalColumn logicalColumn = attributes.get(i);
      ValueMetaInterface valueMeta = getValueForLogicalColumn(databaseMeta, logicalColumn);
      meta.getKeyLookup()[i] = valueMeta.getName();
      meta.getKeyField()[i] = valueMeta.getName();
    }
    
    StepMeta stepMeta = new StepMeta(name, meta);
    stepMeta.drawStep();
    stepMeta.setDescription(description);

    return stepMeta;
  }
}
