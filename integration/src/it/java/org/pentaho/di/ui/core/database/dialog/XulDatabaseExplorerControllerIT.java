/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.core.database.dialog;

import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.eclipse.swt.widgets.Shell;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;

public class XulDatabaseExplorerControllerIT {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleEnvironment.init();
  }

  /**
   * PDI-11394 - Empty database browser dialog appears after unsuccessful connect to DB
   */
  @Test
  public void testPostActionStatusForReflectCalls() {
    Shell shell = new Shell();
    DatabaseMeta meta = new DatabaseMeta();
    List<DatabaseMeta> list = Collections.emptyList();

    XulDatabaseExplorerController dialog = new XulDatabaseExplorerController( shell, meta, list, false );

    UiPostActionStatus actual = dialog.getActionStatus();
    Assert.assertEquals( "By default action status is none", UiPostActionStatus.NONE, actual );

    try {
      dialog.createDatabaseNodes( shell );
    } catch ( Exception e ) {
      // do nothing as it usually used for ui functionality     
    }
    actual = dialog.getActionStatus();

    // this error is caused runtime exception on error dialog show
    Assert.assertEquals( "For reflective calls we have ability to ask for status directly", UiPostActionStatus.ERROR,
        actual );
  }

}
