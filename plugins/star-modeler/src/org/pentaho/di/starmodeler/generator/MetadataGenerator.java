package org.pentaho.di.starmodeler.generator;

import java.util.List;
import java.util.Set;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.starmodeler.ConceptUtil;
import org.pentaho.di.starmodeler.DefaultIDs;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.SqlDataSource;
import org.pentaho.metadata.model.SqlDataSource.DataSourceType;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.metadata.model.SqlPhysicalTable;
import org.pentaho.metadata.model.concept.Concept;
import org.pentaho.metadata.model.concept.types.LocalizedString;

public class MetadataGenerator {
  private Domain logicalDomain;
  private List<DatabaseMeta> databases;

  /**
   * @param logicalDomain The logical domain containing star schemas without physical layer.
   * @param databases The list of databases to reference
   */
  public MetadataGenerator(Domain logicalDomain, List<DatabaseMeta> databases) {
    this.logicalDomain = logicalDomain;
    this.databases = databases;
  }
  
  public Domain generatePhysicalMetadataModel() throws KettleException {
    
    // First do some checking and lookups...
    //
    String targetDatabaseName = ConceptUtil.getString(logicalDomain, DefaultIDs.DOMAIN_TARGET_DATABASE);
    if (Const.isEmpty(targetDatabaseName)) {
      throw new KettleException("Please specify a target database!");
    }
    DatabaseMeta targetDatabaseMeta = DatabaseMeta.findDatabase(databases, targetDatabaseName);
    if (targetDatabaseMeta==null) {
      throw new KettleException("Target database with name '"+targetDatabaseName+"' can't be found!");
    }

    // Now start creation of a new domain with physical underpinning.
    //
    Domain domain = new Domain();
    
    // Copy the domain information...
    //
    domain.setId( createId("DOMAIN", null, domain) );
    domain.setName(logicalDomain.getName());
    domain.setDescription(logicalDomain.getDescription());
    
    // Now copy all the models...
    //
    for (LogicalModel logicalModel : logicalDomain.getLogicalModels()) {
      // Copy model information...
      //
      LogicalModel model = new LogicalModel();
      model.setId( createId("MODEL", domain, model));
      model.setName(logicalModel.getName());
      model.setDescription(logicalModel.getDescription());
      
      // Create a physical model...
      //
      SqlPhysicalModel sqlModel = new SqlPhysicalModel();
      sqlModel.setDatasource(createSqlDataSource(targetDatabaseMeta));
      model.setPhysicalModel(sqlModel);
      
      for (LogicalTable logicalTable : logicalModel.getLogicalTables()) {
        LogicalTable table = new LogicalTable();
        table.setId( createId("LOGICAL_TABLE", logicalModel, logicalTable) );
        table.setName(logicalTable.getName());
        table.setDescription(logicalTable.getDescription());
        
        String targetTable = ConceptUtil.getString(logicalTable, DefaultIDs.LOGICAL_TABLE_PHYSICAL_TABLE_NAME);
        
        SqlPhysicalTable sqlTable = new SqlPhysicalTable(sqlModel);
        table.setPhysicalTable(sqlTable);
        
        // Copy name & description from physical level...
        //
        sqlTable.setId( createId("PHYSICAL_TABLE", logicalModel, logicalTable));
        sqlTable.setName(logicalTable.getName());
        sqlTable.setDescription(logicalTable.getDescription());
        sqlTable.setTableType(ConceptUtil.getTableType(logicalTable));
        sqlTable.setTargetSchema(targetDatabaseMeta.getPreferredSchemaName());
        sqlTable.setTargetTable(targetTable);
        
        
      }
    }
    
    return domain;
  }
  
  private SqlDataSource createSqlDataSource(DatabaseMeta databaseMeta) {
    SqlDataSource dataSource = new SqlDataSource();
    dataSource.setDatabaseName(databaseMeta.getDatabaseName());
    dataSource.setHostname(databaseMeta.getHostname());
    dataSource.setUsername(databaseMeta.getUsername());
    dataSource.setPassword(databaseMeta.getPassword());
    dataSource.setPort(databaseMeta.getDatabasePortNumberString());
    dataSource.setAttributes(databaseMeta.getExtraOptions());
    DataSourceType dataSourceType;
    switch(databaseMeta.getAccessType()) {
    case DatabaseMeta.TYPE_ACCESS_NATIVE : dataSourceType = DataSourceType.NATIVE; break;
    case DatabaseMeta.TYPE_ACCESS_ODBC   : dataSourceType = DataSourceType.ODBC; break;
    case DatabaseMeta.TYPE_ACCESS_JNDI   : dataSourceType = DataSourceType.JNDI; break;
    case DatabaseMeta.TYPE_ACCESS_OCI    : dataSourceType = DataSourceType.OCI; break;
    default: dataSourceType = DataSourceType.CUSTOM; break;
    }
    dataSource.setType(dataSourceType);
    
    return dataSource;
  }

  private String createId(String prefix, Concept parent, Concept item) {
    StringBuilder id = new StringBuilder(prefix);
    
    if (parent!=null) {
      id.append("_");
      id.append( extractId(parent));
    }
    id.append("_");
    id.append( extractId(item));
    
    return id.toString();
  }

  private String extractId(Concept item) {
    LocalizedString localizedName = item.getName();
    Set<String> locales = localizedName.getLocales();
    if (locales.isEmpty()) return "";
    // Just grab the first locale we come across
    // This should normally only one for the star modeler
    //
    String locale = locales.iterator().next(); 
    
    String id = localizedName.getLocalizedString(locale);
    id = id.toUpperCase().replace(" ", "_");
    
    return id;
  }

  /**
   * @return the logicalDomain
   */
  public Domain getLogicalDomain() {
    return logicalDomain;
  }

  /**
   * @param logicalDomain the logicalDomain to set
   */
  public void setLogicalDomain(Domain logicalDomain) {
    this.logicalDomain = logicalDomain;
  }

  /**
   * @return the databases
   */
  public List<DatabaseMeta> getDatabases() {
    return databases;
  }

  /**
   * @param databases the databases to set
   */
  public void setDatabases(List<DatabaseMeta> databases) {
    this.databases = databases;
  }
}
