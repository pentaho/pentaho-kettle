package be.ibridge.kettle.test.screens;


import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class Repositories {

	private Shell sShell = null;  //  @jve:decl-index=0:visual-constraint="7,6"
	private Table tblRepositories = null;
	private Label lblTitle = null;
	private Label lblWhiteBack = null;
	private Label lblDescription = null;
	private Label lblRepositories = null;
	private Button btnChange = null;
	private Button btnAdd = null;
	private Button btnRemove = null;
	private Button btnClose = null;

	/**
	 * This method initializes sShell
	 */
	private void createSShell() {
		sShell = new Shell();
		sShell.setText("Shell");
		createTblRepositories();
		sShell.setSize(new org.eclipse.swt.graphics.Point(643,439));
		lblTitle = new Label(sShell, SWT.NONE);
		lblTitle.setBounds(new org.eclipse.swt.graphics.Rectangle(26,7,285,20));
		lblTitle.setFont(new Font(Display.getDefault(), "Tahoma", 8, SWT.BOLD));
		
		lblTitle.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		lblTitle.setText("Repositories");
		lblDescription = new Label(sShell, SWT.NONE);
		lblDescription.setBounds(new org.eclipse.swt.graphics.Rectangle(26,27,456,30));
		lblDescription.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		lblDescription.setText("Here you can add and change repositories");
		lblWhiteBack = new Label(sShell, SWT.NONE);
		lblWhiteBack.setBounds(new org.eclipse.swt.graphics.Rectangle(0,-1,638,87));
		lblWhiteBack.setText("");
		lblWhiteBack.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		lblRepositories = new Label(sShell, SWT.NONE);
		lblRepositories.setBounds(new org.eclipse.swt.graphics.Rectangle(27,112,260,13));
		lblRepositories.setText("Spoon knows the following repositories");
		btnChange = new Button(sShell, SWT.NONE);
		btnChange.setBounds(new org.eclipse.swt.graphics.Rectangle(491,143,75,30));
		btnChange.setText("Change...");
		btnAdd = new Button(sShell, SWT.NONE);
		btnAdd.setBounds(new org.eclipse.swt.graphics.Rectangle(491,188,75,30));
		btnAdd.setText("Add...");
		btnRemove = new Button(sShell, SWT.NONE);
		btnRemove.setBounds(new org.eclipse.swt.graphics.Rectangle(491,232,75,30));
		btnRemove.setText("Remove");
		btnClose = new Button(sShell, SWT.NONE);
		btnClose.setBounds(new org.eclipse.swt.graphics.Rectangle(491,366,75,30));
		btnClose.setText("Close");
	}

	/**
	 * This method initializes tblRepositories	
	 *
	 */
	private void createTblRepositories() {
		tblRepositories = new Table(sShell, SWT.NONE);
		tblRepositories.setHeaderVisible(true);
		tblRepositories.setLocation(new org.eclipse.swt.graphics.Point(28,137));
		tblRepositories.setLinesVisible(true);
		tblRepositories.setSize(new org.eclipse.swt.graphics.Point(436,215));
		TableColumn colName = new TableColumn(tblRepositories, SWT.NONE);
		colName.setWidth(80);
		colName.setText("Name");
		TableColumn colDescription = new TableColumn(tblRepositories, SWT.NONE);
		colDescription.setWidth(355);
		colDescription.setText("Description");
	}

    public void showScreen()
    {
        createSShell();
        sShell.open();
        while (!sShell.isDisposed())
        {
                if (!sShell.getDisplay().readAndDispatch()) sShell.getDisplay().sleep();
        }

    }

    public static void main(String[] args)
    {
        new Display();
        new Repositories().showScreen();
    }
}
