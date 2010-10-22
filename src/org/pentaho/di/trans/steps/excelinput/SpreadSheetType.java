package org.pentaho.di.trans.steps.excelinput;

public enum SpreadSheetType {
  JXL("Excel 97-2003 XLS (JXL)"), 
  POI("Excel 2007 XLSX (Apache POI)" ), 
  ODS("Open Office ODS (ODFDOM)"), 
  ;
  
  private String description;

  /**
   * @param description
   */
  private SpreadSheetType(String description) {
    this.description = description;
  }
  
  public String getDescription() {
    return description;
  }
  
  public static SpreadSheetType getStpreadSheetTypeByDescription(String description) {
    for (SpreadSheetType type : values()) {
      if (type.getDescription().equalsIgnoreCase(description)) {
        return type;
      }
    }
    return null;
  }
}
