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



package org.pentaho.di.junit.rules;

import org.pentaho.di.core.KettleEnvironment;

public class RestorePDIEngineEnvironment extends RestorePDIEnvironment {

  @Override void defaultInit() throws Throwable {
    super.defaultInit();
    KettleEnvironment.init();
  }

  @Override void cleanUp() {
    KettleEnvironment.reset();
    super.cleanUp();
  }
}
