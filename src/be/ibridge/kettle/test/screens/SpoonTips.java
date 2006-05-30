package be.ibridge.kettle.test.screens;


import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class SpoonTips {

	private Shell sShell = null;  //  @jve:decl-index=0:visual-constraint="10,10"
	private Label lblWhite = null;
	private Label lblGrey = null;
	private Label lblLine = null;
	private Label lblQuestion = null;
	private Label lblAnswer = null;
	private Button btnClose = null;
	private Button btnNext = null;
	private Button chkShowAtStartup = null;
	/**
	 * This method initializes sShell
	 */
	private void createSShell() {
		sShell = new Shell();
		sShell.setText("Spoon - Tip of the day");
		sShell.setSize(new org.eclipse.swt.graphics.Point(516,309));
		lblQuestion = new Label(sShell, SWT.NONE);
		lblQuestion.setBounds(new org.eclipse.swt.graphics.Rectangle(83,24,254,48));
		lblQuestion.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		lblQuestion.setFont(new Font(Display.getDefault(), "Tahoma", 24, SWT.NORMAL));
		lblQuestion.setText("Did you know?");
		lblAnswer = new Label(sShell, SWT.WRAP);
		lblAnswer.setBounds(new org.eclipse.swt.graphics.Rectangle(83,92,386,128));
		lblAnswer.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		lblAnswer.setText("Some text goes here to answer a so called 'frequently asked question'. " +
				"BTW, a questionmark-icon should go above, on the left on the D. And more bla bla bla. And more bla bla bla. And more bla bla bla. And more bla bla bla. And more bla bla bla. And more bla bla bla. ");
		lblLine = new Label(sShell, SWT.NONE);
		lblLine.setBounds(new org.eclipse.swt.graphics.Rectangle(70,80,414,2));
		lblLine.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
		lblLine.setText("");
		lblWhite = new Label(sShell, SWT.BORDER);
		lblWhite.setBounds(new org.eclipse.swt.graphics.Rectangle(70,20,415,220));
		lblWhite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		lblWhite.setText("");
		lblGrey = new Label(sShell, SWT.NONE);
		lblGrey.setBounds(new org.eclipse.swt.graphics.Rectangle(10,20,63,220));
		lblGrey.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
		lblGrey.setText("");
		btnClose = new Button(sShell, SWT.NONE);
		btnClose.setBounds(new org.eclipse.swt.graphics.Rectangle(401,248,82,26));
		btnClose.setText("Close");
		btnNext = new Button(sShell, SWT.NONE);
		btnNext.setBounds(new org.eclipse.swt.graphics.Rectangle(310,248,82,26));
		btnNext.setText("Next Tip");
		chkShowAtStartup = new Button(sShell, SWT.CHECK);
		chkShowAtStartup.setBounds(new org.eclipse.swt.graphics.Rectangle(12,250,220,19));
		chkShowAtStartup.setText("Show tips at startup");
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
        new SpoonTips().showScreen();
    }
}
