
package org.pentaho.di.ui.trans.steps.olapinput;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
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
import org.pentaho.di.core.Props;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.olapinput.OlapInputMeta;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.widget.StyledTextComp;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class OlapInputDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = OlapInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label        wlUrl;
    private TextVar      wUrl;
    private FormData     fdlUrl, fdUrl;
 
    private Label        wlCatalog;
    private TextVar      wCatalog;
    private FormData     fdlCatalog, fdCatalog;
    
    private Label        wlUsername;
    private TextVar      wUsername;
    private FormData     fdlUsername, fdUsername;
 
    private Label        wlPassword;
    private TextVar      wPassword;
    private FormData     fdlPassword, fdPassword;
 
	private Label        wlMDX;
	private StyledTextComp         wMDX;
	private FormData     fdlMDX, fdMDX;
	
	private OlapInputMeta input;
	
	private MDXValuesHighlight lineStyler = new MDXValuesHighlight();
	
	private Label        wlPosition;
	private FormData     fdlPosition;

    private Label        wlVariables;
    private Button       wVariables;
    private FormData     fdlVariables, fdVariables; 

	public OlapInputDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(OlapInputMeta)in;
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
		shell.setText(BaseMessages.getString(PKG, "OlapInputDialog.OlapInput")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

        // Stepname line
		//
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "OlapInputDialog.StepName")); //$NON-NLS-1$
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

		// The URL
		//
		wlUrl=new Label(shell, SWT.RIGHT);
		wlUrl.setText(BaseMessages.getString(PKG, "OlapInputDialog.Url")); //$NON-NLS-1$
 		props.setLook(wlUrl);
		fdlUrl=new FormData();
		fdlUrl.left = new FormAttachment(0, 0);
		fdlUrl.right= new FormAttachment(middle, -margin);
		fdlUrl.top = new FormAttachment(wStepname, margin);
		wlUrl.setLayoutData(fdlUrl);
		wUrl=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wUrl);
		wUrl.addModifyListener(lsMod);
		fdUrl=new FormData();
		fdUrl.left = new FormAttachment(middle, 0);
		fdUrl.right = new FormAttachment(100, 0);
		fdUrl.top = new FormAttachment(wStepname, margin);
		wUrl.setLayoutData(fdUrl);

		// Username
		//
		wlUsername=new Label(shell, SWT.RIGHT);
		wlUsername.setText(BaseMessages.getString(PKG, "OlapInputDialog.Username")); //$NON-NLS-1$
 		props.setLook(wlUsername);
		fdlUsername=new FormData();
		fdlUsername.left = new FormAttachment(0, 0);
		fdlUsername.right= new FormAttachment(middle, -margin);
		fdlUsername.top = new FormAttachment(wUrl, margin);
		wlUsername.setLayoutData(fdlUsername);
		wUsername=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wUsername);
		wUsername.addModifyListener(lsMod);
		fdUsername=new FormData();
		fdUsername.left = new FormAttachment(middle, 0);
		fdUsername.right = new FormAttachment(100, 0);
		fdUsername.top = new FormAttachment(wUrl, margin);
		wUsername.setLayoutData(fdUsername);

		// Password
		//
		wlPassword=new Label(shell, SWT.RIGHT);
		wlPassword.setText(BaseMessages.getString(PKG, "OlapInputDialog.Password")); //$NON-NLS-1$
 		props.setLook(wlPassword);
		fdlPassword=new FormData();
		fdlPassword.left = new FormAttachment(0, 0);
		fdlPassword.right= new FormAttachment(middle, -margin);
		fdlPassword.top = new FormAttachment(wUsername, margin);
		wlPassword.setLayoutData(fdlPassword);
		wPassword=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wPassword);
		wPassword.addModifyListener(lsMod);
		fdPassword=new FormData();
		fdPassword.left = new FormAttachment(middle, 0);
		fdPassword.right = new FormAttachment(100, 0);
		fdPassword.top = new FormAttachment(wUsername, margin);
		wPassword.setLayoutData(fdPassword);

		// Some buttons
		//
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
        wPreview=new Button(shell, SWT.PUSH);
        wPreview.setText(BaseMessages.getString(PKG, "System.Button.Preview")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel , wPreview }, margin, null);

		// Catalog location...
		//
		
		wlCatalog=new Label(shell, SWT.RIGHT);
		wlCatalog.setText(BaseMessages.getString(PKG, "OlapInputDialog.Catalog")); //$NON-NLS-1$
 		props.setLook(wlCatalog);
		fdlCatalog=new FormData();
		fdlCatalog.left = new FormAttachment(0, 0);
		fdlCatalog.right= new FormAttachment(middle, -margin);
		fdlCatalog.bottom = new FormAttachment(wOK, -2*margin);
		wlCatalog.setLayoutData(fdlCatalog);
		wCatalog=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wCatalog);
		wCatalog.addModifyListener(lsMod);
		fdCatalog=new FormData();
		fdCatalog.left = new FormAttachment(middle, 0);
		fdCatalog.right = new FormAttachment(100, 0);
		fdCatalog.bottom = new FormAttachment(wOK, -2*margin);
		wCatalog.setLayoutData(fdCatalog);
		
        // Replace variables in MDX?
		//
        wlVariables = new Label(shell, SWT.RIGHT);
        wlVariables.setText(BaseMessages.getString(PKG, "OlapInputDialog.ReplaceVariables")); //$NON-NLS-1$
        props.setLook(wlVariables);
        fdlVariables = new FormData();
        fdlVariables.left = new FormAttachment(0, 0);
        fdlVariables.right = new FormAttachment(middle, -margin);
        fdlVariables.bottom = new FormAttachment(wCatalog, -margin);
        wlVariables.setLayoutData(fdlVariables);
        wVariables = new Button(shell, SWT.CHECK);
        props.setLook(wVariables);
        wVariables.setToolTipText(BaseMessages.getString(PKG, "OlapInputDialog.ReplaceVariables.Tooltip")); //$NON-NLS-1$
        fdVariables = new FormData();
        fdVariables.left = new FormAttachment(middle, 0);
        fdVariables.right = new FormAttachment(100, 0);
        fdVariables.bottom = new FormAttachment(wCatalog, -margin);
        wVariables.setLayoutData(fdVariables);
        wVariables.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent arg0) { setSQLToolTip(); } });

		
		wlPosition=new Label(shell, SWT.NONE); 
		props.setLook(wlPosition);
		fdlPosition=new FormData();
		fdlPosition.left  = new FormAttachment(0,0);
		fdlPosition.right = new FormAttachment(100, 0);
		fdlPosition.bottom = new FormAttachment(wVariables, -margin);
		wlPosition.setLayoutData(fdlPosition);

		// Table line...
		//
		wlMDX=new Label(shell, SWT.NONE);
		wlMDX.setText(BaseMessages.getString(PKG, "OlapInputDialog.SQL")); //$NON-NLS-1$
 		props.setLook(wlMDX);
		fdlMDX=new FormData();
		fdlMDX.left = new FormAttachment(0, 0);
		fdlMDX.top = new FormAttachment(wPassword, 2*margin);
		wlMDX.setLayoutData(fdlMDX);

		wMDX=new StyledTextComp(shell, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, "");
 		props.setLook(wMDX, Props.WIDGET_STYLE_FIXED);
		wMDX.addModifyListener(lsMod);
		fdMDX=new FormData();
		fdMDX.left  = new FormAttachment(0, 0);
		fdMDX.top   = new FormAttachment(wlMDX, margin );
		fdMDX.right = new FormAttachment(100, 0);
		fdMDX.bottom= new FormAttachment(wlPosition, -margin );
		wMDX.setLayoutData(fdMDX);
		
		wMDX.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent arg0)
            {
                setSQLToolTip();
                setPosition(); 
            }
        }
    );

	
		wMDX.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e) { setPosition(); }
			public void keyReleased(KeyEvent e) { setPosition(); }
			} 
		);
		wMDX.addFocusListener(new FocusAdapter(){
			public void focusGained(FocusEvent e) { setPosition(); }
			public void focusLost(FocusEvent e) { setPosition(); }
			}
		);
		wMDX.addMouseListener(new MouseAdapter(){
			public void mouseDoubleClick(MouseEvent e) { setPosition(); }
			public void mouseDown(MouseEvent e) { setPosition(); }
			public void mouseUp(MouseEvent e) { setPosition(); }
			}
		);
		
		
		
		// Text Higlighting
		lineStyler = new MDXValuesHighlight();
		wMDX.addLineStyleListener(lineStyler);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();  } };
        lsPreview  = new Listener() { public void handleEvent(Event e) { preview(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();      } };
        
		wCancel.addListener  (SWT.Selection, lsCancel);
        wPreview.addListener (SWT.Selection, lsPreview);
		wOK.addListener      (SWT.Selection, lsOK    );

		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wCatalog.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
		
		getData();
		input.setChanged(changed);

		// Set the shell size, based upon previous time...
		setSize();
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	public void setPosition(){
		
		String scr = wMDX.getText();
		int linenr = wMDX.getLineAtOffset(wMDX.getCaretOffset())+1;
		int posnr  = wMDX.getCaretOffset();
				
		// Go back from position to last CR: how many positions?
		int colnr=0;
		while (posnr>0 && scr.charAt(posnr-1)!='\n' && scr.charAt(posnr-1)!='\r')
		{
			posnr--;
			colnr++;
		}
		wlPosition.setText(BaseMessages.getString(PKG, "OlapInputDialog.Position.Label",""+linenr,""+colnr));

	}
	protected void setSQLToolTip()
    {
		if(wVariables.getSelection())
			wMDX.setToolTipText(transMeta.environmentSubstitute(wMDX.getText()));
    }
    /**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
        wUrl.setText(Const.NVL(input.getOlap4jUrl(), ""));
        wUsername.setText(Const.NVL(input.getUsername(), ""));
        wPassword.setText(Const.NVL(input.getPassword(), ""));
		wMDX.setText(Const.NVL(input.getMdx(), ""));
		wCatalog.setText(Const.NVL(input.getCatalog(), ""));
        wVariables.setSelection(input.isVariableReplacementActive());
        
		wStepname.selectAll();
	}

	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	
    private void getInfo(OlapInputMeta meta)
    {
    	meta.setOlap4jUrl( wUrl.getText() );
    	meta.setUsername( wUsername.getText() );
    	meta.setPassword( wPassword.getText() );
        meta.setMdx( wMDX.getText() );
        meta.setCatalog( wCatalog.getText() );
        meta.setVariableReplacementActive(wVariables.getSelection());
    }
    
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		stepname = wStepname.getText(); // return value
		// copy info to TextFileInputMeta class (input)
        
        getInfo(input);
		
		dispose();
	}
	
    /**
     * Preview the data generated by this step.
     * This generates a transformation using this step & a dummy and previews it.
     *
     */
    private void preview()
    {
        // Create the table input reader step...
        OlapInputMeta oneMeta = new OlapInputMeta();
        getInfo(oneMeta);
        
        TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, wStepname.getText());
        
        EnterNumberDialog numberDialog = new EnterNumberDialog(shell, props.getDefaultPreviewSize(), BaseMessages.getString(PKG, "OlapInputDialog.EnterPreviewSize"), BaseMessages.getString(PKG, "OlapDialog.NumberOfRowsToPreview")); //$NON-NLS-1$ //$NON-NLS-2$
        int previewSize = numberDialog.open();
        if (previewSize>0)
        {
            TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(shell, previewMeta, new String[] { wStepname.getText() }, new int[] { previewSize } );
            progressDialog.open();

            Trans trans = progressDialog.getTrans();
            String loggingText = progressDialog.getLoggingText();

            if (!progressDialog.isCancelled())
            {
                if (trans.getResult()!=null && trans.getResult().getNrErrors()>0)
                {
                	EnterTextDialog etd = new EnterTextDialog(shell, BaseMessages.getString(PKG, "System.Dialog.PreviewError.Title"),  
                			BaseMessages.getString(PKG, "System.Dialog.PreviewError.Message"), loggingText, true );
                	etd.setReadOnly();
                	etd.open();
                }
            }
            
            PreviewRowsDialog prd =new PreviewRowsDialog(shell, transMeta, SWT.NONE, wStepname.getText(), progressDialog.getPreviewRowsMeta(wStepname.getText()), progressDialog.getPreviewRows(wStepname.getText()), loggingText);
            prd.open();
        }
    }
}
