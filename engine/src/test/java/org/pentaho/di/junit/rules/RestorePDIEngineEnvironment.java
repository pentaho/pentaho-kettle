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
