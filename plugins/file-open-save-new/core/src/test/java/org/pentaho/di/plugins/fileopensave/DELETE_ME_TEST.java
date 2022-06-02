package org.pentaho.di.plugins.fileopensave;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.plugins.fileopensave.dialog.FileOpenSaveDialog;
import org.pentaho.di.ui.core.FileDialogOperation;
import org.pentaho.di.ui.core.PropsUI;

public class DELETE_ME_TEST {
        @Before
        public void before() throws KettleException {
            KettleClientEnvironment.init();
            KettleEnvironment.init();

        }

        @Test
        public void runTest() {

    /*
    LocalFileProvider localProvider = new LocalFileProvider();

    ProviderService providers = new ProviderService( Arrays.asList( localProvider, localProvider ) );

    FileController controller = new FileController( new FileCache(), providers );

    for ( Tree t : controller.load( "" ) ) {
      System.out.println( t.getName() );
      for ( Object o : t.getChildren() ) {
        if ( o instanceof Directory ) {

          Directory dir = (Directory) o;
          System.out.println( dir.getName() );

          controller.getFile( dir );


        }
      }
    }
    ;
    */

            TestApp win = new TestApp();
            win.setBlockOnOpen( true );
            win.open();
            Display.getCurrent().dispose();
        }

        public class TestApp extends ApplicationWindow {

            public TestApp() {
                super( null );
            }

            public Control createContents(Composite parent ) {
                getShell().setText( "JFace Test" );
                getShell().setSize( 800, 600 );

                PropsUI.init( getShell().getDisplay(), 0 );

                Button b = new Button( parent, SWT.PUSH );
                b.setText( "Open Dialog" );

                b.addSelectionListener( new SelectionAdapter() {
                    public void widgetSelected( org.eclipse.swt.events.SelectionEvent arg0 ) {
                        FileOpenSaveDialog fosd = new FileOpenSaveDialog( getShell(), 800, 540, null );

                        fosd.open();
                    };

                } );

                return parent;
            }

        }
}
