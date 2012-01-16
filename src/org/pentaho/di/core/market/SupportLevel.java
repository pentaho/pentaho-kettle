package org.pentaho.di.core.market;

/**
 * This is an indicator for the support level of a certain software component (plugin)
 * 
 * @author matt
 */
public enum SupportLevel {
  // Supported by ...
  //
  PROFESSIONALY_SUPPORTED("Professionally supported"),
  
  // Supported by the community
  //
  COMMUNITY_SUPPORTED("Community Supported"),

  // Unsupported by anyone: you're on your own.
  //
  NOT_SUPPORTED("Not supported"),
  ;
  
  private String description;
  
  private SupportLevel(String description) {
    this.description = description;
  }
  
  public String getDescription() {
    return description;
  }
  
  /**
   * Get the SupportLevel for a given support level code 
   * @param code The code to search for
   * @return the corresponding SupportLevel or NOT_SUPPORTED if not found.
   */
  public static SupportLevel getSupportLevel(String code) {
    for (SupportLevel level : values()) {
      if (level.name().equalsIgnoreCase(code)) return level;
    }
    return NOT_SUPPORTED;
  }
}
