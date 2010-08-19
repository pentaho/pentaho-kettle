package org.pentaho.di.trans.step;

import java.util.ArrayList;
import java.util.List;

/**
 * A set of step injection metadata entries.
 * 
 * @author matt
 *
 */
public class StepInjectionMetaEntrySet {
  private List<StepInjectionMetaEntry> entries;
  
  public StepInjectionMetaEntrySet() {
    entries=new ArrayList<StepInjectionMetaEntry>();
  }
  
  /**
   * @param entries
   */
  public StepInjectionMetaEntrySet(List<StepInjectionMetaEntry> entries) {
    this.entries = entries;
  }

  /**
   * @return the entries
   */
  public List<StepInjectionMetaEntry> getEntries() {
    return entries;
  }

  /**
   * @param entries the entries to set
   */
  public void setEntries(List<StepInjectionMetaEntry> entries) {
    this.entries = entries;
  }
  
  
}
