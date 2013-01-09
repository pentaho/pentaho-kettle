package org.pentaho.di.ui.trans.steps.edi2xml;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.edi2xml.Edi2XmlMeta;

public class Edi2XmlDialog extends BaseStepDialog implements StepDialogInterface {

	private static Class<?> PKG = Edi2XmlMeta.class; // for i18n purposes

	private Edi2XmlMeta input;

	// output field name
	private Label wlXmlField;
	private TextVar wXmlField;
	private FormData fdlXmlField, fdXmlField;

	private ComboVar wEdiField;

	public Edi2XmlDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		input = (Edi2XmlMeta) in;
	}

	public String open() {
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
		props.setLook(shell);
		setShellImage(shell, input);

		ModifyListener lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				input.setChanged();
			}
		};
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "Edi2Xml.Shell.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName"));
		props.setLook(wlStepname);
		fdlStepname = new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right = new FormAttachment(middle, -margin);
		fdlStepname.top = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);

		wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname = new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top = new FormAttachment(0, margin);
		fdStepname.right = new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		// edifact field line
		Label wlEdiField = new Label(shell, SWT.RIGHT);
		wlEdiField.setText(BaseMessages.getString(PKG, "Edi2Xml.InputField.Label")); //$NON-NLS-1$
		props.setLook(wlEdiField);
		FormData fdlEdiField = new FormData();
		fdlEdiField.left = new FormAttachment(0, 0);
		fdlEdiField.right = new FormAttachment(middle, -margin);
		fdlEdiField.top = new FormAttachment(wStepname, margin);
		wlEdiField.setLayoutData(fdlEdiField);

		wEdiField = new ComboVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wEdiField.setToolTipText(BaseMessages.getString(PKG, "Edi2Xml.InputField.Tooltip"));
		props.setLook(wEdiField);
		wEdiField.addModifyListener(lsMod);
		FormData fdEdiField = new FormData();
		fdEdiField.left = new FormAttachment(middle, 0);
		fdEdiField.top = new FormAttachment(wStepname, margin);
		fdEdiField.right = new FormAttachment(100, 0);
		wEdiField.setLayoutData(fdEdiField);
		wEdiField.addFocusListener(new FocusListener() {
			public void focusLost(org.eclipse.swt.events.FocusEvent e) {
			}

			public void focusGained(org.eclipse.swt.events.FocusEvent e) {
				Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
				shell.setCursor(busy);
				BaseStepDialog.getFieldsFromPrevious(wEdiField, transMeta, stepMeta);
				shell.setCursor(null);
				busy.dispose();
			}
		});

		// xml output field value
		wlXmlField = new Label(shell, SWT.RIGHT);
		wlXmlField.setText(BaseMessages.getString(PKG, "Edi2Xml.OutputField.Label"));
		props.setLook(wlXmlField);
		fdlXmlField = new FormData();
		fdlXmlField.left = new FormAttachment(0, 0);
		fdlXmlField.right = new FormAttachment(middle, -margin);
		fdlXmlField.top = new FormAttachment(wEdiField, margin);
		wlXmlField.setLayoutData(fdlXmlField);

		wXmlField = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wXmlField);
		wXmlField.setToolTipText(BaseMessages.getString(PKG, "Edi2Xml.OutputField.Tooltip"));
		wXmlField.addModifyListener(lsMod);
		fdXmlField = new FormData();
		fdXmlField.left = new FormAttachment(middle, 0);
		fdXmlField.right = new FormAttachment(100, 0);
		fdXmlField.top = new FormAttachment(wEdiField, margin);
		wXmlField.setLayoutData(fdXmlField);

		// OK and cancel buttons
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wXmlField);

		// Add listeners
		lsCancel = new Listener() {
			public void handleEvent(Event e) {
				cancel();
			}
		};
		lsOK = new Listener() {
			public void handleEvent(Event e) {
				ok();
			}
		};

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener(SWT.Selection, lsOK);

		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				ok();
			}
		};

		wStepname.addSelectionListener(lsDef);
		wXmlField.addSelectionListener(lsDef);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				cancel();
			}
		});

		// Set the shell size, based upon previous time...
		setSize();

		getData();
		input.setChanged(changed);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return stepname;
	}

	// Read data and place it in the dialog
	public void getData() {
		wStepname.selectAll();
		wXmlField.setText(Const.NVL(input.getOutputField(), ""));
		wEdiField.setText(Const.NVL(input.getInputField(), ""));
	}

	private void cancel() {
		stepname = null;
		input.setChanged(changed);
		dispose();
	}

	// let the plugin know about the entered data
	private void ok() {
		stepname = wStepname.getText(); // return value
		input.setOutputField(wXmlField.getText());
		input.setInputField(wEdiField.getText());
		dispose();
	}
}
