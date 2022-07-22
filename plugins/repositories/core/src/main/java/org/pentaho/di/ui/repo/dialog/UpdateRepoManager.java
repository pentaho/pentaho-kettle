package org.pentaho.di.ui.repo.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;
import org.pentaho.di.ui.repo.model.RepositoryModel;

import java.util.Map;
import java.util.stream.Stream;

public class UpdateRepoManager extends Dialog {
	private Text text_reponame;
	private Text text_repourl;
	private Text text_description;
	Map<String, String> repodetailsmap;
	String reponame;
	private RepositoryConnectController controller=RepositoryConnectController.getInstance();
	private PropsUI props;
	private Display display;
	private Shell shell;
	private static final Image LOGO = GUIResource.getInstance().getImageLogoSmall();


	public UpdateRepoManager(Shell shell) {
			super( shell, SWT.NONE );
			this.props = PropsUI.getInstance();

	}


	public  void repoManagerUpdate(Map<String, String> repodetailsmap, String reponame) {
		this.repodetailsmap=repodetailsmap;
		this.reponame=reponame;
		Shell parent = getParent();
		display = parent.getDisplay();

		shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.RESIZE );
		props.setLook( shell );
		shell.setLayout( new FormLayout() );
		shell.setText( "Repository update" );
		shell.setImage( LOGO );

		try {

			Label lblRepoName = new Label(getParent().getShell(), SWT.NONE);
			lblRepoName.setBounds(37, 134, 240, 25);
			lblRepoName.setText("Repo name");

			text_reponame = new Text(getParent().getShell(), SWT.BORDER);
			text_reponame.setBounds(37, 165, 297, 31);
			text_reponame.setText(reponame);
			text_reponame.setEditable(false);

			Label lblRepoUrl = new Label(getParent().getShell(), SWT.NONE);
			lblRepoUrl.setBounds(37, 218, 81, 25);
			lblRepoUrl.setText("Repo url");

			text_repourl = new Text(getParent().getShell(), SWT.BORDER);
			text_repourl.setBounds(37, 249, 297, 31);
			props.setLook( text_repourl );
			text_repourl.setText(repodetailsmap.get(reponame).substring(0,repodetailsmap.get(reponame).indexOf("~")));

			Label lblDescription = new Label(getParent().getShell(), SWT.NONE);
			lblDescription.setBounds(37, 297, 174, 25);
			lblDescription.setText("Description");

			text_description = new Text(getParent().getShell(), SWT.BORDER);
			text_description.setBounds(37, 328, 297, 31);
			props.setLook( text_description );
			text_description.setText(repodetailsmap.get(reponame).substring(repodetailsmap.get(reponame).lastIndexOf("~") + 1));

			Button btnUpdate = new Button(getParent().getShell(), SWT.NONE);
			btnUpdate.setBounds(37, 388, 105, 35);
			btnUpdate.setText("update");
			btnUpdate.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {

					RepositoryModel model = new RepositoryModel();
					model.setId( "PentahoEnterpriseRepository" );
					model.setDisplayName( text_reponame.getText() );
					model.setUrl( text_repourl.getText() );
					model.setDescription( text_description.getText() );
					model.setDefault( false );
					model.setEdit( true );
					model.setOriginalName( text_reponame.getText() );
					boolean updateresult = false;
					updateresult = controller.updateRepository( model.getId(), controller.modelToMap( model ) );

				}
				});
					shell.pack();
					shell.setMinimumSize( 500, 400 );
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
