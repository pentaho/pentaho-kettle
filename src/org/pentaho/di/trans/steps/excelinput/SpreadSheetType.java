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
}
