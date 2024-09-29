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


package org.pentaho.di.plugins.fileopensave.api.providers;

/**
 * Created by bmorrise on 2/13/19.
 */
public interface Directory extends File {
  boolean isCanAddChildren();
  default boolean isHasChildren() {
    return true;
  }
}
