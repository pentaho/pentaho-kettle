package org.pentaho.di.ui.dialog;

import junit.framework.TestCase;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.spoon.dialog.TipsDialog;

/**
 * User: Dzmitry Stsiapanau
 * Date: 10/10/13
 * Time: 3:14 PM
 */
public class TipsDialogTest extends TestCase {
    private TipsDialog tipsDialog;
      private Boolean showTips;

      @Before
      public void setUp() throws Exception {
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
