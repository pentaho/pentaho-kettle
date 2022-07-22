package org.pentaho.di.ui.repo.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.json.simple.JSONObject;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;
import java.util.HashMap;
import java.util.Map;

public class RepositoryManagerSWT extends Shell {

	private Display display;
	private Shell shell;
	private static Class<?> PKG = RepositoryConnectionSWT.class;
	private static final String MANAGER_TITLE = BaseMessages.getString( PKG, "RepositoryDialog.Dialog.Manager.Title" );
	private LogChannelInterface log =
			KettleLogStore.getLogChannelInterfaceFactory().create( RepositoryManagerSWT.class );
	private static final Image LOGO = GUIResource.getInstance().getImageLogoSmall();

	public RepositoryManagerSWT(Shell shell, RepositoryConnectController controller ) {
		//  this.controller = controller;
		this.shell = shell;
		this.display = shell.getDisplay();
	}
	/**
	 * Launch the application.
	 *
	 */
	public  void createDialog(RepositoryConnectController controller) {
		try {
			Display display = Display.getDefault();
			RepositoryManagerSWT shell = new RepositoryManagerSWT(display,controller);
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
	public RepositoryManagerSWT(Display display, RepositoryConnectController controller) {
		super(display, SWT.SHELL_TRIM);

		Composite composite = new Composite(this, SWT.NONE);
		composite.setBounds(10, 10, 907, 582);

		Label lblExistingRepos = new Label(composite, SWT.NONE);
		lblExistingRepos.setBounds(268, 21, 147, 25);
		lblExistingRepos.setText("Existing repos");

		List list = new List(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP);
		list.setBounds(10, 60, 652, 496);

		java.util.List<JSONObject> repolist = controller.getRepositories();

		Map<String,String> repodetailsmap = new HashMap<>();
		for (int i = 0; i < repolist.size(); i++) {
			System.out.println(" value of i:"+i+" "+repolist.get(i).get("displayName"));
			String listitem="";
			listitem=repolist.get(i).get("displayName").toString();
			System.out.println("listitem :"+listitem);

			repodetailsmap.put(repolist.get(i).get("displayName").toString(),repolist.get(i).get("url").toString()+"~"+repolist.get(i).get("description").toString());

			list.add(listitem);
		}


		Button btnCreateNew = new Button(composite, SWT.NONE);
		btnCreateNew.setBounds(739, 126, 105, 35);
		btnCreateNew.setText("  Create new  ");
		
		Button btnUpdate = new Button(composite, SWT.NONE);
		btnUpdate.setBounds(739, 183, 105, 35);
		btnUpdate.setText("   Update   ");
		
		Button btnDelete = new Button(composite, SWT.NONE);
		btnDelete.setBounds(739, 251, 105, 35);
		btnDelete.setText("  Delete   ");

		btnCreateNew.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				System.out.println("clicked on create new repo button");
				new CreateRepoManager().createArepoManager(controller);
			}
	});
		btnUpdate.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				System.out.println("clicked on update new repo button");
				int i =list.getSelectionIndex();
				new UpdateRepoManager(repodetailsmap,list.getItem(i)).updateArepoManager(controller);
			}
		});

		btnDelete.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				System.out.println("clicked on delete repo button");
				int i =list.getSelectionIndex();
				new DeleteRepoManager(repodetailsmap,list.getItem(i)).deleteArepoManager(controller);
			}
		});

		createContents();
	}

	/**
	 * Create contents of the shell.
	 */
	protected void createContents() {
		setText(MANAGER_TITLE);
		setSize(937, 646);
		setImage(LOGO);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
