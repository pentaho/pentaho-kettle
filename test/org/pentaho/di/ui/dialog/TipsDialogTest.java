/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.dialog;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.spoon.dialog.TipsDialog;

import static org.junit.Assert.fail;

/**
 * User: Dzmitry Stsiapanau Date: 10/10/13 Time: 3:14 PM
 */
public class TipsDialogTest {
  private TipsDialog tipsDialog;
  private Boolean showTips;
  private String prevUserDir;

  @Before
  public void setUp() throws Exception {
    prevUserDir = System.getProperty( "user.dir" );
    String separator = System.getProperty( "file.separator" );
    System.setProperty( "user.dir", prevUserDir + separator + "assembly" + separator + "package-res" );
    KettleEnvironment.init();

    Display display = Display.getDefault();
    if ( !PropsUI.isInitialized() ) {
      PropsUI.init( display, 1 );
      showTips = PropsUI.getInstance().showTips();
    }
    PropsUI.getInstance().setShowTips( true );

    display = PropsUI.getDisplay();
    Shell rootShell = new Shell( display );

    tipsDialog = new TipsDialog( rootShell );

  }

  @After
  public void tearDown() throws Exception {
    PropsUI.getInstance().setShowTips( showTips );
    System.setProperty( "user.dir", prevUserDir );
  }

  @Test
  public void testOpen() throws Exception {
    try {
      Display.getDefault().asyncExec( new Runnable() {
        @Override
        public void run() {
          tipsDialog.open();
        }
      } );

    } catch ( Exception e ) {
      fail( "Test failed due to exception: " + e.getLocalizedMessage() );
    }
    try {
      Display.getDefault().asyncExec( new Runnable() {
        @Override
        public void run() {
          tipsDialog.dispose();
        }
      } );

    } catch ( Exception e ) {
      fail( "Test failed due to exception: " + e.getLocalizedMessage() );
    }
  }
}
