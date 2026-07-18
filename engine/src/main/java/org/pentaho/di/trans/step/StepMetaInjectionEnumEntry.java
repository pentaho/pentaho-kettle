/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.trans.step;

public interface StepMetaInjectionEnumEntry {

  public String name();

  /**
   * @return the valueType
   */
  public int getValueType();

  /**
   * @return the description
   */
  public String getDescription();

  /**
   * @return The parent entry
   */
  public StepMetaInjectionEnumEntry getParent();
}
