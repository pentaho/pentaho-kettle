package org.pentaho.di.core.market.entry;

/**
 * Describes the various types of market entries
 * @author matt
 *
 */
public enum MarketEntryType {
  Step,
  JobEntry,
  Partitioner,
  SpoonPlugin,
  Database,
  Repository,
  ImportRule,
  Mixed,
  ;
  
  public static MarketEntryType getMarketEntryType(String code) {
    for (MarketEntryType type : values()) {
      if (type.name().equalsIgnoreCase(code)) return type;
    }
    return null;
  }
}
