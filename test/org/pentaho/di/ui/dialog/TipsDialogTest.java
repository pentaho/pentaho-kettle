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
