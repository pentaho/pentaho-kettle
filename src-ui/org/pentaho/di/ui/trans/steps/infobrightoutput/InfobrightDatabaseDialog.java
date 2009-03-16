package org.pentaho.di.ui.trans.steps.infobrightoutput;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.pentaho.di.core.Const;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.infobrightoutput.InfobrightLoaderDialogInterface;
import org.pentaho.di.trans.steps.infobrightoutput.Messages;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * Dialog box for the Infobright loader.
 */
public class InfobrightDatabaseDialog extends BaseStepDialog implements StepDialogInterface {
  
  private static final char PASSWD_ECHO_CHAR = '*';
  
  private int middle;
  private TextVar serverHostText;
  private TextVar databaseNameText;
  private TextVar portNumberText;
  private TextVar usernameText;
  private TextVar passwordText;

  
  private Messages msgs = new Messages();

  protected BaseStepMeta input;
  private InfobrightLoaderDialogInterface dialogInterface;
  
  private ModifyListener lsMod;
  
  /**
   * @param parent
   * @param in
   * @param tr
   * @param sname
   */
  public InfobrightDatabaseDialog(Shell parent, Object in, TransMeta tr, String sname) {
    super(parent, (BaseStepMeta) in, tr, sname);
    input = (BaseStepMeta) in;
    dialogInterface = (InfobrightLoaderDialogInterface) in;
    addMessagePackage(org.pentaho.di.ui.core.dialog.Messages.class.getPackage());
    addMessagePackage(InfobrightDatabaseDialog.class.getPackage());

    lsMod = new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        input.setChanged();
      }
    };
  }
  
  protected void addMessagePackage(Package packageToAdd) {
    msgs.addPackage(packageToAdd);
  }

  /**
   * {@inheritDoc}
   * @see org.pentaho.di.trans.step.StepDialogInterface#open()
   */
  public String open() {
    shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
    props.setLook(shell);
    
    setShellImage(shell, (StepMetaInterface) input);

    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout(formLayout);
    shell.setText(dialogInterface.getDialogTitle());

    middle = props.getMiddlePct();
     
    int margin = Const.MARGIN;

    /************************************** Step name line ***************************/
    // label
    wlStepname = new Label(shell, SWT.RIGHT);
    wlStepname.setText(msgs.get("BrightHouseLoaderDialog.Stepname.Label"));
    wlStepname.setLayoutData(standardLabelSpacing(null));
    props.setLook(wlStepname);

    // text entry
    wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wStepname.setText(stepname);
    wStepname.addModifyListener(lsMod);
    wStepname.setLayoutData(standardInputSpacing(null));
    props.setLook(wStepname);

    Control lastControl = addDbConnectionInputs();
    lastControl = addCustomInputs(lastControl);
    
    addVerticalPadding(2 * margin);
    
    /********************************** OK and Cancel buttons **************************/
    addDefaultButtons(margin, lastControl);
    
    getData();
    input.setChanged(changed);

    shell.open();
    while (!shell.isDisposed()) {
      Display display = getParent().getDisplay();

      if (!display.readAndDispatch())
        display.sleep();
    }
    return stepname;
  }

  /**
   * Adds db connection text boxes for input
   * @return the last control specified
   */
  protected Control addDbConnectionInputs() {
    serverHostText = addStandardTextVar("DatabaseDialog.label.ServerHostname", wStepname);
    databaseNameText = addStandardTextVar("DatabaseDialog.label.DatabaseName", serverHostText);
    portNumberText = addStandardTextVar("DatabaseDialog.label.PortNumber", databaseNameText);
    usernameText = addStandardTextVar("DatabaseDialog.label.Username", portNumberText);
    passwordText = addPasswordTextVar("DatabaseDialog.label.Password", usernameText);
    return passwordText;
  }

  /**
   * Adds any custom inputs
   * @param prevControl
   * @return the last control
   */
  protected Control addCustomInputs(Control prevControl) {
    return prevControl;
  }


  protected CCombo addStandardSelect(String labelMessageKey, Control prevControl, String[] choices) {
    int vertPad = verticalPadding;
    addStandardLabel(labelMessageKey, prevControl);
    verticalPadding = vertPad;
    CCombo combo = new CCombo(shell, SWT.BORDER);
    combo.setItems(choices);
    combo.addModifyListener(lsMod);
    combo.setLayoutData(standardInputSpacing(prevControl));
    return combo;
  }
  
  protected TextVar addStandardTextVar(String labelMessageKey, Control prevControl) {
    int vertPad = verticalPadding;
    addStandardLabel(labelMessageKey, prevControl);
    verticalPadding = vertPad;
    TextVar targetControl = new TextVar(
        dialogInterface.getDatabaseMetadata(), // FIXME make sure this is correct
        shell,
        SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    targetControl.addModifyListener(lsMod);
    targetControl.setLayoutData(standardInputSpacing(prevControl));
    return targetControl;
  }
 
  protected TextVar addPasswordTextVar(String labelMessageKey, Control prevControl) {
    TextVar textVar = addStandardTextVar(labelMessageKey, prevControl);
    textVar.setEchoChar(PASSWD_ECHO_CHAR);
    return textVar;
  }
  
  protected Button addStandardCheckBox(String labelMessageKey, Control prevControl) {
    addStandardLabel(labelMessageKey, prevControl);
    Button targetControl = new Button(shell, SWT.CHECK);
    targetControl.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        input.setChanged();
      }
    }
    );
    targetControl.setLayoutData(standardInputSpacing(prevControl));
    return targetControl;
  }
  
  private Label addStandardLabel(String messageString, Control previousControl) {
    Label label = new Label(shell, SWT.RIGHT);
    label.setText(msgs.get(messageString));
    label.setLayoutData(standardLabelSpacing(previousControl));
    props.setLook(label);
    return label;
  }
  
  private int verticalPadding = 0;
  private void addVerticalPadding(int amount) {
    verticalPadding += amount;
  }
  
  private FormData standardLabelSpacing(Control control) {
    return standardSpacing(control, true);
  }
  
  private FormData standardInputSpacing(Control control) {
    return standardSpacing(control, false);
  }
  
  private FormData standardSpacing(Control control, boolean isLabel) {
    FormData fd = new FormData();
    
    if (isLabel)
      fd.left = new FormAttachment(0, 0);
    else  
      fd.left = new FormAttachment(middle, 0);
    
    if (isLabel)
      fd.right = new FormAttachment(middle, -Const.MARGIN);
    else
      fd.right = new FormAttachment(100, 0);
    
    if (control != null)
      fd.top = new FormAttachment(control, Const.MARGIN+verticalPadding);
    else
      fd.top = new FormAttachment(0, Const.MARGIN+verticalPadding);      
    
    verticalPadding = 0;
    return fd;
  }
  
  private void addDefaultButtons(int margin, Control lastControl) {
    // Some buttons
    wOK = new Button(shell, SWT.PUSH);
    wOK.setText(Messages.getString("System.Button.OK"));
    wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText(Messages.getString("System.Button.Cancel"));

    setButtonPositions(new Button[] { wOK, wCancel }, margin, lastControl);

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

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e) {
        cancel();
      }
    });

    // Set the shell size, based upon previous time...
    setSize();    
  }
  
  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    wStepname.selectAll();
    if (dialogInterface.getDatabaseMetadata().getHostname() != null) serverHostText.setText(dialogInterface.getDatabaseMetadata().getHostname());
    if (dialogInterface.getDatabaseMetadata().getDatabaseName() != null) databaseNameText.setText(dialogInterface.getDatabaseMetadata().getDatabaseName());
    if (dialogInterface.getDatabaseMetadata().getDatabasePortNumberString() != null) portNumberText.setText(dialogInterface.getDatabaseMetadata().getDatabasePortNumberString());
    if (dialogInterface.getDatabaseMetadata().getUsername() != null) usernameText.setText(dialogInterface.getDatabaseMetadata().getUsername());
    if (dialogInterface.getDatabaseMetadata().getPassword() != null) passwordText.setText(dialogInterface.getDatabaseMetadata().getPassword());
  }

  protected void cancel() {
    stepname = null;
    input.setChanged(changed);
    dispose();
  }

  protected void ok() {
    stepname = wStepname.getText(); // return value
    dialogInterface.getDatabaseMetadata().setHostname(serverHostText.getText());
    dialogInterface.getDatabaseMetadata().setDBName(databaseNameText.getText());
    dialogInterface.getDatabaseMetadata().setDBPort(portNumberText.getText());
    dialogInterface.getDatabaseMetadata().setUsername(usernameText.getText());
    dialogInterface.getDatabaseMetadata().setPassword(passwordText.getText());
    
    // the tableoutput just creates a "reference" (by name) to the connection.  if we don't add it
    // here, then it won't get saved/restored when Spoon is closed.
    transMeta.addOrReplaceDatabase(dialogInterface.getDatabaseMetadata());  
    dispose();
  }

  public BaseStepMeta getInput() {
    return input;
  }
  

}
