package org.pentaho.di.ui.repo.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.json.simple.JSONObject;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;
import java.util.HashMap;
import java.util.Map;

public class RepositoryManagerSWT extends Dialog {

	private Display display;
	private Shell shell;
	private PropsUI props;
	private static Class<?> PKG = RepositoryConnectionSWT.class;
	private static final String MANAGER_TITLE = BaseMessages.getString( PKG, "RepositoryDialog.Dialog.Manager.Title" );
	private LogChannelInterface log =
			KettleLogStore.getLogChannelInterfaceFactory().create( RepositoryManagerSWT.class );
	private static final Image LOGO = GUIResource.getInstance().getImageLogoSmall();

	public RepositoryManagerSWT(Shell shell) {
		super( shell, SWT.NONE );
		this.props = PropsUI.getInstance();
	}
	/**
	 * Launch the application.
	 *
	 */
	public  void createDialog() {
		Shell parent = getParent();
		display = parent.getDisplay();

		shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX );
		props.setLook( shell );
		shell.setLayout( new FormLayout() );
		shell.setText( MANAGER_TITLE );
		shell.setImage( LOGO );

		try {

			Composite composite = new Composite(shell, SWT.NONE);
			composite.setBounds(10, 10, 907, 582);

			Label lblExistingRepos = new Label(composite, SWT.NONE);
			lblExistingRepos.setBounds(268, 21, 147, 25);
			lblExistingRepos.setText("Existing repos");

//			List list = new List(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP);
//			list.setBounds(10, 60, 652, 496);

			java.util.List<JSONObject> repolist = RepositoryConnectController.getInstance().getRepositories();

			Map<String,String> repodetailsmap = new HashMap<>();
			for (int i = 0; i < repolist.size(); i++) {
				System.out.println(" value of i:"+i+" "+repolist.get(i).get("displayName"));
				String listitem="";
				listitem=repolist.get(i).get("displayName").toString();
				System.out.println("listitem :"+listitem);

				repodetailsmap.put(repolist.get(i).get("displayName").toString(),repolist.get(i).get("url").toString()+"~"+repolist.get(i).get("description").toString());

				//-----	list.add(listitem);
			}


			Table table = new Table(composite, SWT.MULTI | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
			table.setHeaderVisible(true);
			table.setLinesVisible(true);
			table.setBounds(  10, 60, 652, 496);
			int columnCount = 3;
			TableColumn column_reponame= new TableColumn(table, SWT.NONE);
				TableColumn column_repourl =  new TableColumn(table, SWT.NONE);
				TableColumn column_repodesc = new TableColumn(table, SWT.NONE);
			column_reponame.setText("repo name");
			column_repourl.setText( "repo url" );
			column_repodesc.setText( "repo desc" );

			/*int columnCount = 4;
			for (int i = 0; i < columnCount; i++) {
				TableColumn column = new TableColumn(table, SWT.NONE);
				column.setText("Column " + i);
			}*/
			int itemCount = repolist.size();
			for (int i = 0; i < itemCount; i++) {
				TableItem item = new TableItem(table, SWT.NONE);

				item.setText(new String[] {
					repolist.get( i ).get("displayName"  ).toString(),
					repolist.get( i ).get("url"   ).toString(),
					 repolist.get( i ).get("description"   ).toString()
					 });
			}

			Listener paintListener = new Listener() {
				public void handleEvent(Event event) {
					switch (event.type) {
						case SWT.MeasureItem: {
							TableItem item = (TableItem) event.item;
							String text = getText(item, event.index);
							Point size = event.gc.textExtent(text);
							event.width = size.x;
							event.height = Math.max(event.height, size.y);
							break;
						}
						case SWT.PaintItem: {
							TableItem item = (TableItem) event.item;
							String text = getText(item, event.index);
							Point size = event.gc.textExtent(text);
							int offset2 = event.index == 0 ? Math.max(0, (event.height - size.y) / 2) : 0;
							event.gc.drawText(text, event.x, event.y + offset2, true);
							break;
						}
						case SWT.EraseItem: {
							event.detail &= ~SWT.FOREGROUND;
							break;
						}
					}
				}

				String getText(TableItem item, int column) {
					String text = item.getText(column);
					if (column != 0) {
						int index = table.indexOf(item);
						if ((index + column) % 3 == 1) {
							text += "\n";
						}
						if ((index + column) % 3 == 2) {
							text += "\n\n";
						}
					}
					return text;
				}
			};
			table.addListener(SWT.MeasureItem, paintListener);
			table.addListener(SWT.PaintItem, paintListener);
			table.addListener(SWT.EraseItem, paintListener);
			for (int i = 0; i < columnCount; i++) {
				table.getColumn(i).pack();
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

			//********** button implementation for create new repo ***************
			btnCreateNew.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					new CreateRepoManager().createArepoManager(RepositoryConnectController.getInstance());
				}
			});

			//********** button implementation for update repo ***************
			btnUpdate.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {

					int i =table.getSelectionIndex();
					new UpdateRepoManager(shell).repoManagerUpdate(repodetailsmap,table.getItem(i).getText());
				}
			});

			//********** button implementation for delete repo ***************
			btnDelete.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {

					int i =table.getSelectionIndex();
					new DeleteRepoManager(shell ,repodetailsmap,table.getItem(i).getText()).deleteArepoManager();
				}
			});

			shell.pack();
			shell.setMinimumSize( 900, 700 );
			shell.open();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
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
