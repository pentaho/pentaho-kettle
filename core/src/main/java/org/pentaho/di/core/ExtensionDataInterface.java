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



package org.pentaho.di.core;

import java.util.Map;

/**
 * This interface gives you access to a map which is associated with an object to store the state of non-standard
 * objects. It can be used by plugins to store state-information details in transformations, jobs, steps, job-entries...
 * at runtime.
 *
 * @author matt
 *
 */
public interface ExtensionDataInterface {
  public Map<String, Object> getExtensionDataMap();
}
