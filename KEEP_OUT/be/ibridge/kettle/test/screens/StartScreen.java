package be.ibridge.kettle.test.screens;


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class StartScreen {

	private Shell sShell = null;  //  @jve:decl-index=0:visual-constraint="10,10"
	private Button radStart = null;
	private Button radLogin = null;
	private Label lblUsername = null;
	private Label lblPassword = null;
	private Label lblRepository = null;
	private Text txtUsername = null;
	private Text txtPassword = null;
	private Combo cmbRepository = null;
	private Canvas cvPicture = null;
	private Button btnRepositories = null;
	private Button btnStart = null;
	private Button btnClose = null;
	private Button btnHelp = null;

	/**
	 * This method initializes sShell
	 */
	private void createSShell() {
		sShell = new Shell();
		sShell.setText("Kettle");
		sShell.setSize(new org.eclipse.swt.graphics.Point(483,292));
		radStart = new Button(sShell, SWT.RADIO);
		radStart.setBounds(new org.eclipse.swt.graphics.Rectangle(129,22,144,16));
		radStart.setText("Stand Alone");
		radLogin = new Button(sShell, SWT.RADIO);
		radLogin.setBounds(new org.eclipse.swt.graphics.Rectangle(129,48,144,18));
		radLogin.setText("Login");
		lblUsername = new Label(sShell, SWT.NONE);
		lblUsername.setBounds(new org.eclipse.swt.graphics.Rectangle(131,97,107,13));
		lblUsername.setText("Username:");
		lblPassword = new Label(sShell, SWT.NONE);
		lblPassword.setBounds(new org.eclipse.swt.graphics.Rectangle(131,127,107,13));
		lblPassword.setText("Password:");
		lblRepository = new Label(sShell, SWT.NONE);
		lblRepository.setBounds(new org.eclipse.swt.graphics.Rectangle(131,158,106,13));
		lblRepository.setText("Repository:");
		txtUsername = new Text(sShell, SWT.BORDER);
		txtUsername.setBounds(new org.eclipse.swt.graphics.Rectangle(251,98,153,19));
		txtPassword = new Text(sShell, SWT.BORDER);
		txtPassword.setBounds(new org.eclipse.swt.graphics.Rectangle(252,128,153,19));
		createCmbRepository();
		createCvPicture();
		btnRepositories = new Button(sShell, SWT.NONE);
		btnRepositories.setBounds(new org.eclipse.swt.graphics.Rectangle(419,159,30,20));
		btnRepositories.setText(" ...");
		btnStart = new Button(sShell, SWT.NONE);
		btnStart.setBounds(new org.eclipse.swt.graphics.Rectangle(161,219,92,31));
		btnStart.setText("Start");
		btnClose = new Button(sShell, SWT.NONE);
		btnClose.setBounds(new org.eclipse.swt.graphics.Rectangle(266,219,92,31));
		btnClose.setText("Close");
		btnHelp = new Button(sShell, SWT.NONE);
		btnHelp.setBounds(new org.eclipse.swt.graphics.Rectangle(388,219,61,31));
		btnHelp.setText("Help");
	}

	/**
	 * This method initializes cmbRepository	
	 *
	 */
	private void createCmbRepository() {
		cmbRepository = new Combo(sShell, SWT.NONE);
		cmbRepository.setBounds(new org.eclipse.swt.graphics.Rectangle(251,158,153,21));
	}

	/**
	 * This method initializes cvPicture	
	 *
	 */
	private void createCvPicture() {
		cvPicture = new Canvas(sShell, SWT.NONE);
		cvPicture.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
		cvPicture.setBounds(new org.eclipse.swt.graphics.Rectangle(-1,-1,118,269));
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
        new StartScreen().showScreen();
    }
}
