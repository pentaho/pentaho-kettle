package be.ibridge.kettle.test.screens;


import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class AddDatabase {

	private Shell sShell = null;  //  @jve:decl-index=0:visual-constraint="7,6"
	private Label lblTitle = null;
	private Label lblWhiteBack = null;
	private Label lblDescription = null;
	private Button btnRemove = null;
	private Button btnSave = null;
	private Button btnTest = null;
	private Button btnExplore = null;
	private Button btnFeatures = null;
	private Label lblConnectionName = null;
	private Label lblConnectionType = null;
	private Label lblAccessMethod = null;
	private Label lblServerHostname = null;
	private Label lblDatabaseName = null;
	private Label lblPortNr = null;
	private Label lblUsername = null;
	private Label lblPassword = null;
	private Text txtConnectionName = null;
	private Combo cmbConnectionType = null;
	private Combo cmbAccessMethod = null;
	private Text txtServerHostname = null;
	private Text txtDatabaseName = null;
	private Text txtPortNumber = null;
	private Text txtUsername = null;
	private Text txtPassword = null;
	/**
	 * This method initializes sShell
	 */
	private void createSShell() {
		sShell = new Shell();
		sShell.setText("Shell");
		sShell.setSize(new org.eclipse.swt.graphics.Point(643,439));
		lblTitle = new Label(sShell, SWT.NONE);
		lblTitle.setBounds(new org.eclipse.swt.graphics.Rectangle(26,7,285,20));
		lblTitle.setFont(new Font(Display.getDefault(), "Tahoma", 8, SWT.BOLD));
		
		lblTitle.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		lblTitle.setText("Add Database");
		lblDescription = new Label(sShell, SWT.NONE);
		lblDescription.setBounds(new org.eclipse.swt.graphics.Rectangle(26,27,456,30));
		lblDescription.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		lblDescription.setText("Fill in the necessary fields to define a database connection");
		lblWhiteBack = new Label(sShell, SWT.NONE);
		lblWhiteBack.setBounds(new org.eclipse.swt.graphics.Rectangle(0,-1,638,87));
		lblWhiteBack.setText("");
		lblWhiteBack.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		btnRemove = new Button(sShell, SWT.NONE);
		btnRemove.setBounds(new org.eclipse.swt.graphics.Rectangle(401,366,75,30));
		btnRemove.setText("<  Back");
		btnSave = new Button(sShell, SWT.NONE);
		btnSave.setBounds(new org.eclipse.swt.graphics.Rectangle(491,366,75,30));
		btnSave.setText("Save");
		lblConnectionName = new Label(sShell, SWT.NONE);
		lblConnectionName.setBounds(new org.eclipse.swt.graphics.Rectangle(26,112,122,16));
		lblConnectionName.setText("Connection Name:");
		txtConnectionName = new Text(sShell, SWT.BORDER);
		txtConnectionName.setBounds(new org.eclipse.swt.graphics.Rectangle(175,112,152,18));

		lblConnectionType = new Label(sShell, SWT.NONE);
		lblConnectionType.setBounds(new org.eclipse.swt.graphics.Rectangle(26,142,122,16));
		lblConnectionType.setText("Connection Type:");
		createCmbConnectionType();

		lblAccessMethod = new Label(sShell, SWT.NONE);
		lblAccessMethod.setBounds(new org.eclipse.swt.graphics.Rectangle(26,172,122,16));
		lblAccessMethod.setText("Access Method:");
		createCmbAccessMethod();

		lblServerHostname = new Label(sShell, SWT.NONE);
		lblServerHostname.setBounds(new org.eclipse.swt.graphics.Rectangle(26,203,122,16));
		lblServerHostname.setText("Server Hostname:");
		txtServerHostname = new Text(sShell, SWT.BORDER);
		txtServerHostname.setBounds(new org.eclipse.swt.graphics.Rectangle(175,202,152,18));

		lblDatabaseName = new Label(sShell, SWT.NONE);
		lblDatabaseName.setBounds(new org.eclipse.swt.graphics.Rectangle(26,232,122,16));
		lblDatabaseName.setText("Database Name:");
		txtDatabaseName = new Text(sShell, SWT.BORDER);
		txtDatabaseName.setBounds(new org.eclipse.swt.graphics.Rectangle(175,232,152,18));

		lblPortNr = new Label(sShell, SWT.NONE);
		lblPortNr.setBounds(new org.eclipse.swt.graphics.Rectangle(26,260,122,16));
		lblPortNr.setText("Port Number:");
		txtPortNumber = new Text(sShell, SWT.BORDER);
		txtPortNumber.setBounds(new org.eclipse.swt.graphics.Rectangle(175,260,152,18));

		lblUsername = new Label(sShell, SWT.NONE);
		lblUsername.setBounds(new org.eclipse.swt.graphics.Rectangle(26,292,122,16));
		lblUsername.setText("Username:");
		txtUsername = new Text(sShell, SWT.BORDER);
		txtUsername.setBounds(new org.eclipse.swt.graphics.Rectangle(175,292,152,18));

		lblPassword = new Label(sShell, SWT.NONE);
		lblPassword.setBounds(new org.eclipse.swt.graphics.Rectangle(26,321,122,16));
		lblPassword.setText("Password:");
		txtPassword = new Text(sShell, SWT.BORDER);
		txtPassword.setBounds(new org.eclipse.swt.graphics.Rectangle(175,323,152,18));

		btnTest = new Button(sShell, SWT.NONE);
		btnTest.setBounds(new org.eclipse.swt.graphics.Rectangle(71,366,75,30));
		btnTest.setText("Test");

		btnExplore = new Button(sShell, SWT.NONE);
		btnExplore.setBounds(new org.eclipse.swt.graphics.Rectangle(161,366,75,30));
		btnExplore.setText("Explore");

		btnFeatures = new Button(sShell, SWT.NONE);
		btnFeatures.setBounds(new org.eclipse.swt.graphics.Rectangle(251,366,75,30));
		btnFeatures.setText("Features");
	
	}
	/**
	 * This method initializes cmbConnectionType	
	 *
	 */
	private void createCmbConnectionType() {
		cmbConnectionType = new Combo(sShell, SWT.NONE);
		cmbConnectionType.setBounds(new org.eclipse.swt.graphics.Rectangle(175,141,152,18));
	}
	/**
	 * This method initializes cmbAccessMethod	
	 *
	 */
	private void createCmbAccessMethod() {
		cmbAccessMethod = new Combo(sShell, SWT.NONE);
		cmbAccessMethod.setBounds(new org.eclipse.swt.graphics.Rectangle(175,171,152,18));
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
        new AddDatabase().showScreen();
    }
}
