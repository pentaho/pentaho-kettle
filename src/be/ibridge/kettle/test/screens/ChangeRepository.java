package be.ibridge.kettle.test.screens;


import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ChangeRepository {

	private Shell sShell = null;  //  @jve:decl-index=0:visual-constraint="7,6"
	private Label lblTitle = null;
	private Label lblWhiteBack = null;
	private Label lblDescription = null;
	private Button btnRemove = null;
	private Button btnClose = null;
	private Label lblName = null;
	private Label lblRepoDescrip = null;
	private Label lblDatabase = null;
	private Text txtName = null;
	private Text txtDescription = null;
	private Combo cmbDatabase = null;
	private Button btnAdd = null;

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
		lblTitle.setText("Update Repository");
		lblDescription = new Label(sShell, SWT.NONE);
		lblDescription.setBounds(new org.eclipse.swt.graphics.Rectangle(26,27,456,30));
		lblDescription.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		lblDescription.setText("Fill in the necessary fields to define the repository");
		lblWhiteBack = new Label(sShell, SWT.NONE);
		lblWhiteBack.setBounds(new org.eclipse.swt.graphics.Rectangle(0,-1,638,87));
		lblWhiteBack.setText("");
		lblWhiteBack.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		btnRemove = new Button(sShell, SWT.NONE);
		btnRemove.setBounds(new org.eclipse.swt.graphics.Rectangle(401,366,75,30));
		btnRemove.setText("<  Back");
		btnClose = new Button(sShell, SWT.NONE);
		btnClose.setBounds(new org.eclipse.swt.graphics.Rectangle(491,366,75,30));
		btnClose.setText("Save");
		lblName = new Label(sShell, SWT.NONE);
		lblName.setBounds(new org.eclipse.swt.graphics.Rectangle(56,127,158,15));
		lblName.setText("Repository Name:");
		lblRepoDescrip = new Label(sShell, SWT.NONE);
		lblRepoDescrip.setBounds(new org.eclipse.swt.graphics.Rectangle(55,156,159,17));
		lblRepoDescrip.setText("Repository Description:");
		lblDatabase = new Label(sShell, SWT.NONE);
		lblDatabase.setBounds(new org.eclipse.swt.graphics.Rectangle(56,187,166,17));
		lblDatabase.setText("Database:");
		txtName = new Text(sShell, SWT.BORDER);
		txtName.setBounds(new org.eclipse.swt.graphics.Rectangle(237,127,149,17));
		txtDescription = new Text(sShell, SWT.BORDER);
		txtDescription.setBounds(new org.eclipse.swt.graphics.Rectangle(237,157,149,17));
		createCmbDatabase();
		btnAdd = new Button(sShell, SWT.NONE);
		btnAdd.setBounds(new org.eclipse.swt.graphics.Rectangle(397,184,43,23));
		btnAdd.setText("Add...");
	}

	/**
	 * This method initializes cmbDatabase	
	 *
	 */
	private void createCmbDatabase() {
		cmbDatabase = new Combo(sShell, SWT.NONE);
		cmbDatabase.setBounds(new org.eclipse.swt.graphics.Rectangle(237,184,149,21));
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
        new ChangeRepository().showScreen();
    }

}
