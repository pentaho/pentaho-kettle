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
