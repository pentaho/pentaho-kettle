/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

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
    entries = new ArrayList<StepInjectionMetaEntry>();
  }

  /**
   * @param entries
   */
  public StepInjectionMetaEntrySet( List<StepInjectionMetaEntry> entries ) {
    this.entries = entries;
  }

  /**
   * @return the entries
   */
  public List<StepInjectionMetaEntry> getEntries() {
    return entries;
  }

  /**
   * @param entries
   *          the entries to set
   */
  public void setEntries( List<StepInjectionMetaEntry> entries ) {
    this.entries = entries;
  }

}
