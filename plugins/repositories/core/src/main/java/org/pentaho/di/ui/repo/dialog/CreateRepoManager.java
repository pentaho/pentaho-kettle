package org.pentaho.di.ui.repo.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;
import org.pentaho.di.ui.repo.model.RepositoryModel;

public class CreateRepoManager extends Shell {
	private Text text_reponame;
	private Text text_repourl;
	private Text text_description;
	private RepositoryConnectController newcontroller;

	public CreateRepoManager() {

		System.out.println("default constructor called");
	}
	/**
	 * Launch the application.
	 * @param
	 * @param newcontroller
	 */
	public  void createArepoManager(RepositoryConnectController newcontroller) {
		this.newcontroller=newcontroller;
		System.out.println("method called createArepoManager");
		try {
			Display display = Display.getDefault();
			CreateRepoManager shell = new CreateRepoManager(display,newcontroller);
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
	public CreateRepoManager(Display display,RepositoryConnectController newcontroller) {
		super(display, SWT.SHELL_TRIM);
		
		Label lblRepoName = new Label(this, SWT.NONE);
		lblRepoName.setBounds(37, 134, 240, 25);
		lblRepoName.setText("Repo name");
		
		text_reponame = new Text(this, SWT.BORDER);
		text_reponame.setBounds(37, 165, 297, 31);
		
		Label lblRepoUrl = new Label(this, SWT.NONE);
		lblRepoUrl.setBounds(37, 218, 81, 25);
		lblRepoUrl.setText("Repo url");
		
		text_repourl = new Text(this, SWT.BORDER);
		text_repourl.setBounds(37, 249, 297, 31);
		
		Label lblDescription = new Label(this, SWT.NONE);
		lblDescription.setBounds(37, 297, 174, 25);
		lblDescription.setText("Description");
		
		text_description = new Text(this, SWT.BORDER);
		text_description.setBounds(37, 328, 297, 31);
		
		Button btnCreate = new Button(this, SWT.NONE);
		btnCreate.setBounds(37, 388, 105, 35);
		btnCreate.setText("create");

		createContents();

		btnCreate.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						System.out.println("button create pressed");

						System.out.println("repo name :"+text_reponame.getText());

						System.out.println("repo url :"+text_repourl.getText());
						System.out.println("repo desc :"+text_description.getText());

						RepositoryModel model = new RepositoryModel();
						model.setId("PentahoEnterpriseRepository");
						model.setDisplayName(text_reponame.getText());
						model.setUrl(text_repourl.getText());
						model.setDescription(text_description.getText());
						model.setDefault(false);




						try{
							boolean connectionresult=false;
							connectionresult = newcontroller.createRepository(model.getId(), newcontroller.modelToMap(model)) != null;
							System.out.println("connection result :"+connectionresult);
							System.out.println("repo creation successful");
							getShell().close();
						}
						catch (Exception e){
							System.out.println("some exception :"+e);
						}




							}
						});

					}

			/**
			 * Create contents of the shell.
			 */
			protected void createContents() {
				setText("Login to repository");
				setSize(739, 707);

			}

			@Override
			protected void checkSubclass() {
				// Disable the check that prevents subclassing of SWT components
			}
		}
