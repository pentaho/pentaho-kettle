package org.pentaho.di.starmodeler;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.Concept;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.concept.types.FieldType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.model.concept.types.TableType;
import org.pentaho.pms.schema.concept.DefaultPropertyID;


public class ConceptUtil {
  public static String getDescription(Concept concept, String locale) {
    LocalizedString localizedString = (LocalizedString) concept.getProperty(Concept.DESCRIPTION_PROPERTY);
    if (localizedString==null) return null;
    return localizedString.getLocalizedString(locale);
  }

  public static String getName(Concept concept, String locale) {
    LocalizedString localizedString = (LocalizedString) concept.getProperty(Concept.NAME_PROPERTY);
    if (localizedString==null) return null;
    return localizedString.getLocalizedString(locale);
  }
  
  public static TableType getTableType(Concept concept) {
    TableType tableType = (TableType) concept.getProperty(DefaultPropertyID.TABLE_TYPE.getId());
    if (tableType==null) return TableType.OTHER;
    return tableType;
  }
  
  public static TableType getTableType(String typeString) {
    try {
      return TableType.valueOf(typeString);
    } catch(Exception e) {
      return TableType.OTHER;
    }
  }
  
  public static String getString(Concept concept, String id) {
    String string = (String) concept.getProperty(id);
    return string;
  }
  
  public static LogicalColumn findFirstKeyColumn(LogicalTable logicalTable) {
    for (LogicalColumn column : logicalTable.getLogicalColumns()) {
      FieldType fieldType = column.getFieldType();
      if (fieldType!=null && fieldType==FieldType.KEY) {
        return column;
      }
    }
    return null;
  }
  
  public static DimensionType getDimensionType(LogicalTable logicalTable) {
    String typeString = getString(logicalTable, DefaultIDs.LOGICAL_TABLE_DIMENSION_TYPE);
    return DimensionType.getDimensionType(typeString);
  }

  public static AttributeType getAttributeType(LogicalColumn column) {
    String typeString = getString(column, DefaultIDs.LOGICAL_COLUMN_ATTRIBUTE_TYPE);
    return AttributeType.getAttributeType(typeString);
  }
  
  public static LogicalColumn findLogicalColumn(LogicalTable logicalTable, AttributeType attributeType) {
    for (LogicalColumn logicalColumn : logicalTable.getLogicalColumns()) {
      AttributeType type = getAttributeType(logicalColumn);
      if (type == attributeType) return logicalColumn; 
    }
    return null;
  }
  
  public static List<LogicalColumn> findLogicalColumns(LogicalTable logicalTable, AttributeType attributeType) {
    List<LogicalColumn> logicalColumns = new ArrayList<LogicalColumn>();
    for (LogicalColumn logicalColumn : logicalTable.getLogicalColumns()) {
      AttributeType type = getAttributeType(logicalColumn);
      if (type == attributeType) {
        logicalColumns.add(logicalColumn); 
      }
    }
    return logicalColumns;
  }
  
  public static List<LogicalTable> findLogicalTables(LogicalModel logicalModel, TableType tableType) {
    List<LogicalTable> logicalColumns = new ArrayList<LogicalTable>();
    for (LogicalTable logicalTable : logicalModel.getLogicalTables()) {
      TableType type = getTableType(logicalTable);
      if (type == tableType) {
        logicalColumns.add(logicalTable); 
      }
    }
    return logicalColumns;
  }

  public static int indexOfFactTable(LogicalModel logicalModel) {
    for (int i=0;i<logicalModel.getLogicalTables().size();i++) {
      LogicalTable logicalTable = logicalModel.getLogicalTables().get(i);
      TableType type = getTableType(logicalTable);
      if (type == TableType.FACT) {
        return i; 
      }
    }
    return -1;
  }

  public static LogicalTable findDimensionWithName(LogicalModel logicalModel, String dimensionName, String locale) {
    for (LogicalTable table : logicalModel.getLogicalTables()) {
      TableType tableType = ConceptUtil.getTableType(table);
      if (tableType==TableType.DIMENSION) {
        String name = ConceptUtil.getName(table, locale);
        if (name!=null && name.equalsIgnoreCase(dimensionName)) return table;
      }
    }
    return null;
  }

  public static DataType getDataType(String typeString) {
    try {
      return DataType.valueOf(typeString);
    } catch(Exception e) {
      return DataType.UNKNOWN;
    }
  }

}
