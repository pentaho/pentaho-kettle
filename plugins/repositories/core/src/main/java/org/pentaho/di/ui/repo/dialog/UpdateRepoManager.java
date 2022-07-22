package org.pentaho.di.ui.repo.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.*;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;
import org.pentaho.di.ui.repo.model.RepositoryModel;

import java.util.Map;
import java.util.stream.Stream;

public class UpdateRepoManager extends Shell {
	private Text text_reponame;
	private Text text_repourl;
	private Text text_description;
	Map<String, String> repodetailsmap;
	String reponame;
	private RepositoryConnectController controller;


	public UpdateRepoManager(Map<String, String> repodetailsmap, String reponame) {

		this.repodetailsmap=repodetailsmap;
		this.reponame=reponame;
		System.out.println("update default constructor called");
	}


	public  void updateArepoManager(RepositoryConnectController controller) {
		this.controller=controller;
		System.out.println("method called updateArepoManager");

		Stream.of(repodetailsmap.keySet().toString())
				.forEach(System.out::println);
		System.out.println("reponame :"+reponame);
		//System.out.println(" selection event in update : "+selectionEvent.getSource());
		try {
			Display display = Display.getDefault();
			UpdateRepoManager shell = new UpdateRepoManager(display,controller,repodetailsmap,reponame);
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
	 */
	public UpdateRepoManager(Display display,RepositoryConnectController controller,Map<String, String> repodetailsmap, String reponame) {
		super(display, SWT.SHELL_TRIM);

		System.out.println("update repo manager constructor 2");
		System.out.println("repo name :"+reponame);


		Label lblRepoName = new Label(this, SWT.NONE);
		lblRepoName.setBounds(37, 134, 240, 25);
		lblRepoName.setText("Repo name");

		text_reponame = new Text(this, SWT.BORDER);
		text_reponame.setBounds(37, 165, 297, 31);
		text_reponame.setText(reponame);
		text_reponame.setEditable(false);
		
		Label lblRepoUrl = new Label(this, SWT.NONE);
		lblRepoUrl.setBounds(37, 218, 81, 25);
		lblRepoUrl.setText("Repo url");

		text_repourl = new Text(this, SWT.BORDER);
		text_repourl.setBounds(37, 249, 297, 31);
		text_repourl.setText(repodetailsmap.get(reponame).substring(0,repodetailsmap.get(reponame).indexOf("~")));
		
		Label lblDescription = new Label(this, SWT.NONE);
		lblDescription.setBounds(37, 297, 174, 25);
		lblDescription.setText("Description");

		text_description = new Text(this, SWT.BORDER);
		text_description.setBounds(37, 328, 297, 31);
		text_description.setText(repodetailsmap.get(reponame).substring(repodetailsmap.get(reponame).lastIndexOf("~") + 1));
		
		Button btnUpdate = new Button(this, SWT.NONE);
		btnUpdate.setBounds(37, 388, 105, 35);
		btnUpdate.setText("update");
		btnUpdate.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				System.out.println("clicked on update shell");

				RepositoryModel model = new RepositoryModel();
				model.setId("PentahoEnterpriseRepository");
				model.setDisplayName(text_reponame.getText());
				model.setUrl(text_repourl.getText());
				model.setDescription(text_description.getText());
				model.setDefault(false);
				model.setEdit(true);
				model.setOriginalName(text_reponame.getText());
				boolean updateresult= false;
				updateresult=controller.updateRepository( model.getId(), controller.modelToMap( model ) );
				System.out.println("update result :"+updateresult);
				System.out.println("repo updation successful");
				getShell().close();

			}
		});

		createContents();
	}

	/**
	 * Create contents of the shell.
	 */
	protected void createContents() {
		setText("SWT Application");
		setSize(1071, 634);

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
