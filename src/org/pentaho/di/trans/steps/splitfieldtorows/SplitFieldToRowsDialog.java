package org.pentaho.di.trans.steps.splitfieldtorows;


import org.eclipse.swt.SWT;
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
import org.pentaho.di.ui.trans.step.BaseStepDialog;



public class SplitFieldToRowsDialog extends BaseStepDialog implements StepDialogInterface
{
	private Label        wlSplitfield;
	private Text         wSplitfield;
	private FormData     fdlSplitfield, fdSplitfield;

	private Label        wlDelimiter;
	private Text         wDelimiter;
	private FormData     fdlDelimiter, fdDelimiter;

	private Label        wlValName;
	private Text         wValName;
	private FormData     fdlValName, fdValName;

    private SplitFieldToRowsMeta  input;

	public SplitFieldToRowsDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(SplitFieldToRowsMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);
        setShellImage(shell, input);

		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
			}
		};
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("SplitFieldToRowsDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("SplitFieldToRowsDialog.Stepname.Label")); //$NON-NLS-1$
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(middle, -margin);
		fdlStepname.top  = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
 		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		// Typefield line
		wlSplitfield=new Label(shell, SWT.RIGHT);
		wlSplitfield.setText(Messages.getString("SplitFieldToRowsDialog.SplitField.Label")); //$NON-NLS-1$
 		props.setLook(wlSplitfield);
		fdlSplitfield=new FormData();
		fdlSplitfield.left = new FormAttachment(0, 0);
		fdlSplitfield.right= new FormAttachment(middle, -margin);
		fdlSplitfield.top  = new FormAttachment(wStepname, margin);
		wlSplitfield.setLayoutData(fdlSplitfield);
		wSplitfield=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wSplitfield.setText(""); //$NON-NLS-1$
 		props.setLook(wSplitfield);
		wSplitfield.addModifyListener(lsMod);
		fdSplitfield=new FormData();
		fdSplitfield.left = new FormAttachment(middle, 0);
		fdSplitfield.top  = new FormAttachment(wStepname, margin);
		fdSplitfield.right= new FormAttachment(100, 0);
		wSplitfield.setLayoutData(fdSplitfield);

		// Typefield line
		wlDelimiter=new Label(shell, SWT.RIGHT);
		wlDelimiter.setText(Messages.getString("SplitFieldToRowsDialog.Delimiter.Label")); //$NON-NLS-1$
 		props.setLook(wlDelimiter);
		fdlDelimiter=new FormData();
		fdlDelimiter.left = new FormAttachment(0, 0);
		fdlDelimiter.right= new FormAttachment(middle, -margin);
		fdlDelimiter.top  = new FormAttachment(wSplitfield, margin);
		wlDelimiter.setLayoutData(fdlDelimiter);
		wDelimiter=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wDelimiter.setText(""); //$NON-NLS-1$
 		props.setLook(wDelimiter);
		wDelimiter.addModifyListener(lsMod);
		fdDelimiter=new FormData();
		fdDelimiter.left = new FormAttachment(middle, 0);
		fdDelimiter.top  = new FormAttachment(wSplitfield, margin);
		fdDelimiter.right= new FormAttachment(100, 0);
		wDelimiter.setLayoutData(fdDelimiter);

		// ValName line
		wlValName=new Label(shell, SWT.RIGHT);
		wlValName.setText(Messages.getString("SplitFieldToRowsDialog.NewFieldName.Label")); //$NON-NLS-1$
        props.setLook( wlValName );
		fdlValName=new FormData();
		fdlValName.left = new FormAttachment(0, 0);
		fdlValName.right= new FormAttachment(middle, -margin);
		fdlValName.top  = new FormAttachment(wDelimiter, margin);
		wlValName.setLayoutData(fdlValName);
		wValName=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wValName.setText(""); //$NON-NLS-1$
        props.setLook( wValName );
		wValName.addModifyListener(lsMod);
		fdValName=new FormData();
		fdValName.left = new FormAttachment(middle, 0);
		fdValName.right= new FormAttachment(100, 0);
		fdValName.top  = new FormAttachment(wDelimiter, margin);
		wValName.setLayoutData(fdValName);

        wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel}, margin, wValName);
        
		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wValName.addSelectionListener( lsDef );
				
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		input.setChanged(changed);
	
		shell.open();
		while (!shell.isDisposed())
		{
		    if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return stepname;
	}
	
	public void getData()
	{	
		wStepname.selectAll();

		wSplitfield.setText(Const.NVL(input.getSplitField(), ""));
		wDelimiter.setText(Const.NVL(input.getDelimiter(), ""));
		wValName.setText(Const.NVL(input.getNewFieldname(), ""));
	}

	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	
	private void ok()
	{
		stepname = wStepname.getText(); // return value
		input.setSplitField( wSplitfield.getText() );
		input.setDelimiter( wDelimiter.getText() );
		input.setNewFieldname(wValName.getText());
		dispose();
	}
}
