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


package org.pentaho.di.core;

import org.pentaho.di.repository.ObjectId;

/**
 *
 * Implementing classes of this interface can provide more information about the source of the CheckResult remark.
 *
 * @author mbatchel 6/25/07
 */
public interface CheckResultSourceInterface {
  /**
   * @return The name of the source generating the CheckResult
   */
  public String getName();

  /**
   * @return The description of the source generating the CheckResult
   */
  public String getDescription();

  /**
   * @return The ID of the source generating the CheckResult
   */
  public ObjectId getObjectId();

  /**
   * @return The Type ID of the source generating the CheckResult. The Type ID is the system-defined type identifier
   *         (like TRANS or SORT).
   */
  public String getTypeId();
}
