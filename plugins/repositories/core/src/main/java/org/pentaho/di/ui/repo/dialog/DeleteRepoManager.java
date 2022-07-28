package org.pentaho.di.ui.repo.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.json.simple.JSONObject;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;

public class DeleteRepoManager extends Shell {

  private static final Image LOGO = GUIResource.getInstance().getImageLogoSmall();
  private PropsUI props;

  public void deleteRepository( JSONObject selectedrepodetails) {
    try {
      Display display = Display.getDefault();

      DeleteRepoManager shell = new DeleteRepoManager(display, selectedrepodetails );
      shell.open();
      shell.layout();
      while (!shell.isDisposed()) {
        if (!display.readAndDispatch()) {
          display.sleep();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Create the shell.
   * @param display
   * @param selectedrepodetails
   */
  public DeleteRepoManager( Display display, JSONObject selectedrepodetails ) {
    super(display, SWT.SHELL_TRIM);
    this.props = PropsUI.getInstance();

    Button btnDelete = new Button(this, SWT.NONE);
    btnDelete.setBounds(342, 106, 105, 35);
    btnDelete.setText("Delete");

    Label lblDoYouWant = new Label(this, SWT.NONE);
    lblDoYouWant.setBounds(10, 27, 447, 25);
    lblDoYouWant.setText("do you want to delete the repository -"+selectedrepodetails.get( "displayName" ));
    props.setLook( lblDoYouWant);

    setText("Delete repository");
    setSize(513, 228);
    setImage( LOGO );
    setBackground( new Color( getShell().getDisplay(), 255, 255, 255 ) );

    btnDelete.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected( SelectionEvent event) {

        boolean deleteresult= false;
        deleteresult= RepositoryConnectController.getInstance().deleteRepository(selectedrepodetails.get( "displayName" ).toString());
        getShell().close();
      }
    });
  }


  @Override
  protected void checkSubclass() {
    // Disable the check that prevents subclassing of SWT components
  }
}
