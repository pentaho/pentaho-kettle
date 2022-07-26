/*
FLOW LOGIC:
1.get repo name from selection
2.pull RepositoryModel from controller based on above repo name
3.set new details to model
4.call update service from controller
*/

package org.pentaho.di.ui.repo.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.json.simple.JSONObject;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;
import org.pentaho.di.ui.repo.model.RepositoryModel;

import java.util.List;

public class UpdateRepoManager extends Shell {
	private Text text_reponame;
	private Text text_repourl;
	private Text text_description;
	private RepositoryConnectController controller=RepositoryConnectController.getInstance();
	private PropsUI props;
	private Display display;
	private Shell shell;
	private boolean setDefaultFlag;
	private static final Image LOGO = GUIResource.getInstance().getImageLogoSmall();


	public UpdateRepoManager() {
			//super( shell, SWT.NONE );
			this.props = PropsUI.getInstance();

	}


	public  void repoManagerUpdate( JSONObject selectedrepodetails) {

		//Shell parent = getParent();
		Shell parent=new Shell();
		display = parent.getDisplay();

		shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.RESIZE );
		props.setLook( shell );
		shell.setLayout( new FormLayout() );
		shell.setText( "Repository update" );
		shell.setImage( LOGO );

		try {

			Label lblRepoName = new Label(getParent().getShell(), SWT.NONE);
			lblRepoName.setBounds(10, 10, 240, 25);
			lblRepoName.setText("Display name");

			text_reponame = new Text(getParent().getShell(), SWT.BORDER);
			text_reponame.setBounds(10, 41, 353, 31);
			text_reponame.setText(selectedrepodetails.get( "displayName" ).toString());
			text_reponame.setEditable(false);

			Label lblRepoUrl = new Label(getParent().getShell(), SWT.NONE);
			lblRepoUrl.setBounds(10, 90, 81, 25);
			lblRepoUrl.setText("URL");

			text_repourl = new Text(getParent().getShell(), SWT.BORDER);
			text_repourl.setBounds(10, 121, 353, 31);
			props.setLook( text_repourl );
			text_repourl.setText(selectedrepodetails.get( "url" ).toString());

			Label lblDescription = new Label(getParent().getShell(), SWT.NONE);
			lblDescription.setBounds(10, 169, 174, 25);
			lblDescription.setText("Description");

			text_description = new Text(getParent().getShell(), SWT.BORDER);
			text_description.setBounds(10, 209, 353, 79);
			props.setLook( text_description );
			text_description.setText(selectedrepodetails.get( "description" ).toString());

			Button btnCheckoxdefault = new Button(getParent().getShell(), SWT.CHECK);
			btnCheckoxdefault.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					if(btnCheckoxdefault.getSelection()){
						setDefaultFlag=true;
					}
					else{
						setDefaultFlag=false;
					}
				}
			});
			btnCheckoxdefault.setBounds(10, 313, 297, 25);
			btnCheckoxdefault.setText("Launch connection on startup");

			Button btnUpdate = new Button(getParent().getShell(), SWT.NONE);
			btnUpdate.setBounds(10, 387, 105, 35);
			btnUpdate.setText("update");

			Button btnHelp = new Button(getParent().getShell(), SWT.ICON_INFORMATION);
			btnHelp.setBounds(289, 529, 105, 35);
			btnHelp.setText("Help");

			btnUpdate.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {

					RepositoryModel model = new RepositoryModel();
					model.setId( "PentahoEnterpriseRepository" );
					model.setDisplayName( text_reponame.getText() );
					model.setUrl( text_repourl.getText() );
					model.setDescription( text_description.getText() );
					model.setDefault( setDefaultFlag );
					model.setEdit( true );

					model.setOriginalName( text_reponame.getText() );
					boolean updateresult = false;
					updateresult = controller.updateRepository( model.getId(), controller.modelToMap( model ) );
					shell.close();

				}
				});
					shell.pack();
					shell.setMinimumSize(429, 634 );
					shell.open();
					while ( !shell.isDisposed() ) {
						if ( !display.readAndDispatch() ) {
							display.sleep();
						}
					}
				} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
