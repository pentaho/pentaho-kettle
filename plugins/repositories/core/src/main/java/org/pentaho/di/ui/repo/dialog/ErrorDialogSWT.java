package org.pentaho.di.ui.repo.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class ErrorDialogSWT extends Shell {

    ErrorDialogSWT errordialogshell;
    public ErrorDialogSWT(Display errormessage) {

        try {
            Display display = Display.getDefault();
            this.errordialogshell = new ErrorDialogSWT(display);
            this.errordialogshell.open();
            this.errordialogshell.layout();

            MessageBox messageBox = new MessageBox(errordialogshell, SWT.OK |
                    SWT.ICON_WARNING |SWT.CANCEL);
            messageBox.setMessage("Enter the User Name");
            messageBox.open();
            while (!this.errordialogshell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}
