package be.ibridge.kettle.test.screens;


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class LoginScreen {

	private Shell sShell = null;  //  @jve:decl-index=0:visual-constraint="10,9"
	private Group grpRepository = null;
	private List list = null;
	private Button btnNew = null;
	private Button btnEdit = null;
	private Button btnDelete = null;
	private Group grpAuthentication = null;
	private Label lblUsername = null;
	private Label lblPassword = null;
	private Button btnClose = null;
	private Button btnStart = null;
	private CCombo cmbUsername = null;
	private Text txtPassword = null;
	private Button chkNoRepo = null;
	/**
	 * This method initializes sShell
	 */
	private void createSShell() {
		sShell = new Shell();
		sShell.setText("Welcome to Kettle - Spoon");
		sShell.setSize(new org.eclipse.swt.graphics.Point(538,257));
		
		createGrpRepositories();
		createGrpAuthentication();
	}


	/**
	 * This method initializes group	
	 *
	 */
	private void createGrpRepositories() {
		grpRepository = new Group(sShell, SWT.NONE);
		grpRepository.setText("Repositories");
		grpRepository.setBounds(new org.eclipse.swt.graphics.Rectangle(10,10,245,205));
		list = new List(grpRepository, SWT.NONE);
		list.setBounds(new org.eclipse.swt.graphics.Rectangle(14,15,217,138));
		btnNew = new Button(grpRepository, SWT.NONE);
		btnNew.setBounds(new org.eclipse.swt.graphics.Rectangle(17,165,59,30));
		btnNew.setText("New...");
		btnEdit = new Button(grpRepository, SWT.NONE);
		btnEdit.setBounds(new org.eclipse.swt.graphics.Rectangle(93,165,59,30));
		btnEdit.setText("Edit...");
		btnDelete = new Button(grpRepository, SWT.NONE);
		btnDelete.setBounds(new org.eclipse.swt.graphics.Rectangle(169,165,59,30));
		btnDelete.setText("Delete...");
	}


	/**
	 * This method initializes grpAuthentication	
	 *
	 */
	private void createGrpAuthentication() {
		grpAuthentication = new Group(sShell, SWT.NONE);
		grpAuthentication.setText("Authentication");
		grpAuthentication.setBounds(new org.eclipse.swt.graphics.Rectangle(270,10,245,205));
		lblUsername = new Label(grpAuthentication, SWT.NONE);
		lblUsername.setBounds(new org.eclipse.swt.graphics.Rectangle(9,19,91,18));
		lblUsername.setText("Username:");
		lblPassword = new Label(grpAuthentication, SWT.NONE);
		lblPassword.setBounds(new org.eclipse.swt.graphics.Rectangle(9,43,91,18));
		lblPassword.setText("Password");
		btnClose = new Button(grpAuthentication, SWT.NONE);
		btnClose.setBounds(new org.eclipse.swt.graphics.Rectangle(144,165,89,30));
		btnClose.setText("Close");
		btnStart = new Button(grpAuthentication, SWT.NONE);
		btnStart.setBounds(new org.eclipse.swt.graphics.Rectangle(144,98,89,30));
		btnStart.setText("Start");
		cmbUsername = new CCombo(grpAuthentication, SWT.NONE);
		cmbUsername.setBounds(new org.eclipse.swt.graphics.Rectangle(112,19,121,17));
		txtPassword = new Text(grpAuthentication, SWT.BORDER);
		txtPassword.setBounds(new org.eclipse.swt.graphics.Rectangle(112,43,121,18));
		chkNoRepo = new Button(grpAuthentication, SWT.CHECK);
		chkNoRepo.setBounds(new org.eclipse.swt.graphics.Rectangle(9,69,189,21));
		chkNoRepo.setText("Start Without Repository");
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
        new LoginScreen().showScreen();
    }
}
