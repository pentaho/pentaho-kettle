/**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

package be.ibridge.kettle.core.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import be.ibridge.kettle.core.GUIResource;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.trans.step.BaseStepDialog;

/**
 * Dialog to enter a text. (descriptions etc.)
 * 
 * @author Matt
 * @since 19-06-2003
 */
public class EnterFormulaDialog extends Dialog
{
    private String title, message;

    private StyledText wText;

    private Button wOK, wCancel;
    private Listener lsOK, lsCancel;

    private Shell shell;
    private SelectionAdapter lsDef;
    private Props props;
    private String formula;
    private boolean fixed;
    private boolean readonly, modal, singleLine;

    private Row inputFields;

    /**
     * Dialog to allow someone to show or enter a formula
     * 
     * @param parent The parent shell to use
     * @param title The dialog title
     * @param message The message to display
     * @param formula The text to display or edit
     */
    public EnterFormulaDialog(Shell parent, String title, String message, String formula, Row inputFields)
    {
        this(parent, title, message, formula, inputFields, true);
    }

    /**
     * Dialog to allow someone to show or enter a formula in variable or fixed width font
     * 
     * @param parent The parent shell to use
     * @param title The dialog title
     * @param message The message to display
     * @param formula The text to display or edit
     * @param fixed true if you want the font to be in fixed-width
     */
    public EnterFormulaDialog(Shell parent, String title, String message, String formula, Row inputFields, boolean fixed)
    {
        super(parent, SWT.NONE);
        props = Props.getInstance();
        this.title = title;
        this.message = message;
        this.formula = formula;
        this.inputFields = inputFields;
        this.fixed = fixed;
        readonly = false;
        singleLine = false;
    }

    public void setReadOnly()
    {
        readonly = true;
    }

    public void setModal()
    {
        modal = true;
    }

    public void setSingleLine()
    {
        singleLine = true;
    }

    public String open()
    {
        Shell parent = getParent();
        Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN |  (modal?SWT.APPLICATION_MODAL:SWT.NONE));
        props.setLook(shell);

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(title);
		shell.setImage(GUIResource.getInstance().getImageLogoSmall());

        int margin = Const.MARGIN;

        // From step line
        Label wlDesc = new Label(shell, SWT.NONE);
        wlDesc.setText(message);
        props.setLook(wlDesc);
        FormData fdlDesc = new FormData();
        fdlDesc.left = new FormAttachment(0, 0);
        fdlDesc.top = new FormAttachment(0, margin);
        wlDesc.setLayoutData(fdlDesc);

        if (singleLine)
            wText = new StyledText(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        else
            wText = new StyledText(shell, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

        wText.setText("");
        if (fixed)
            props.setLook(wText, Props.WIDGET_STYLE_FIXED);
        else
            props.setLook(wText);
        FormData fdDesc = new FormData();
        fdDesc.left = new FormAttachment(0, 0);
        fdDesc.top = new FormAttachment(wlDesc, margin);
        fdDesc.right = new FormAttachment(100, 0);
        fdDesc.bottom = new FormAttachment(100, -50);
        wText.setLayoutData(fdDesc);
        wText.setEditable(!readonly);

        // Some buttons
        if (!readonly)
        {
            wOK = new Button(shell, SWT.PUSH);
            wOK.setText(Messages.getString("System.Button.OK"));
            wCancel = new Button(shell, SWT.PUSH);
            wCancel.setText(Messages.getString("System.Button.Cancel"));

            BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, null);

            // Add listeners
            lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
            lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };

            wOK.addListener    (SWT.Selection, lsOK     );
            wCancel.addListener(SWT.Selection, lsCancel );
        }
        else
        {
            wOK = new Button(shell, SWT.PUSH);
            wOK.setText(Messages.getString("System.Button.Close"));

            BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK }, margin, null);

            // Add listeners
            lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
            wOK.addListener    (SWT.Selection, lsOK     );
        }

        lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
        wText.addSelectionListener(lsDef);

        // Detect [X] or ALT-F4 or something that kills this window...
        shell.addShellListener( new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

        getData();

        BaseStepDialog.setSize(shell);

        shell.open();
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch()) display.sleep();
        }
        return formula;
    }

    public void dispose()
    {
        props.setScreen(new WindowProperty(shell));
        shell.dispose();
    }

    public void getData()
    {
        if (formula != null)
            wText.setText(formula);
    }

    private void cancel()
    {
        formula = null;
        dispose();
    }

    private void ok()
    {
        formula = wText.getText();
        dispose();
    }

    public boolean isFixed()
    {
        return fixed;
    }

    public void setFixed(boolean fixed)
    {
        this.fixed = fixed;
    }

    /**
     * @return the inputFields
     */
    public Row getInputFields()
    {
        return inputFields;
    }

    /**
     * @param inputFields the inputFields to set
     */
    public void setInputFields(Row inputFields)
    {
        this.inputFields = inputFields;
    }
}
