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

package org.pentaho.di.resource;

import org.pentaho.di.repository.ObjectId;

public interface ResourceHolderInterface {

  /**
   * @return The name of the holder of the resource
   */
  public String getName();

  /**
   * @return The description of the holder of the resource
   */
  public String getDescription();

  /**
   * @return The ID of the holder of the resource
   */
  public ObjectId getObjectId();

  /**
   * @return The Type ID of the resource holder. The Type ID is the system-defined type identifier (like TRANS or SORT).
   */
  public String getTypeId();

  /**
   * Gets the high-level type of resource holder.
   *
   * @return JOBENTRY, STEP, etc.
   */
  public String getHolderType();

}
