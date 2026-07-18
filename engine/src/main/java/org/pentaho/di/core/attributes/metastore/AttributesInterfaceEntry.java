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
