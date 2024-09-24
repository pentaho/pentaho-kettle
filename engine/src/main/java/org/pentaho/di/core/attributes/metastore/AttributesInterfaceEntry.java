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

package org.pentaho.di.core.attributes.metastore;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.IOException;

/**
 * @author nhudak
 */
interface AttributesInterfaceEntry {
  @JsonIgnore String groupName();

  @JsonIgnore String key();

  @JsonIgnore String jsonValue() throws IOException;
}
