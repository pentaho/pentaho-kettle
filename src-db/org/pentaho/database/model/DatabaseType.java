package org.pentaho.database.model;

import java.io.Serializable;
import java.util.List;

public class DatabaseType implements Serializable, IDatabaseType {
  
  private static final long serialVersionUID = 1955013893420806385L;

  private String name;
  private String shortName;
  private int defaultPort;
  private List<DatabaseAccessType> supportedAccessTypes;
  private String extraOptionsHelpUrl;
  
  public DatabaseType() {
    
  }
  
  public DatabaseType(String name, String shortName, List<DatabaseAccessType> supportedAccessTypes, int defaultPort, String extraOptionsHelpUrl) {
    this.name = name;
    this.shortName = shortName;
    this.defaultPort = defaultPort;
    this.supportedAccessTypes = supportedAccessTypes;
    this.extraOptionsHelpUrl = extraOptionsHelpUrl;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseType#getName()
   */
  public String getName() {
    return name;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseType#getShortName()
   */
  public String getShortName() {
    return shortName;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseType#getSupportedAccessTypes()
   */
  public List<DatabaseAccessType> getSupportedAccessTypes() {
    return supportedAccessTypes;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseType#getDefaultDatabasePort()
   */
  public int getDefaultDatabasePort() {
    return defaultPort;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.model.IDatabaseType#getExtraOptionsHelpUrl()
   */
  public String getExtraOptionsHelpUrl() {
    return extraOptionsHelpUrl;
  }

  public boolean equals(Object obj) {
    DatabaseType dbtype = (DatabaseType)obj;
    return (getShortName().equals(dbtype.getShortName()));
  }
  
  public int hashCode() {
    return getShortName().hashCode();
  }
  
}
