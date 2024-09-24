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

package org.pentaho.di.core.injection;

public class MetaBeanLevel3 {
  @Injection( name = "FILENAME", group = "FILENAME_LINES" )
  private String name;

  public String getName() {
    return name;
  }
}
