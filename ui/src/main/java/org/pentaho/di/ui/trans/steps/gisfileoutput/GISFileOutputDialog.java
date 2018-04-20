package org.pentaho.di.ui.trans.steps.gisfileoutput;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.gisfileoutput.GISFileOutput;
import org.pentaho.di.trans.steps.gisfileoutput.GISFileOutputMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides dialog box and components for the GISFileOutput step.
 * 
 * @author etdub, jmathieu, tbadard
 * @since 27-jan-2008
 */
public class GISFileOutputDialog extends BaseStepDialog implements StepDialogInterface {

	private static Class<?> PKG = GISFileOutput.class;

	final static private String[] GISFILE_FILTER_EXT = new String[] {"*.shp;*.SHP", "*"};
	
	private Label wlFileName;
	private Button wbFileName;
	private TextVar wFileName;
	private FormData fdlFileName, fdbFileName, fdFileName;
	
	private Label wlFileField;
	private Button wFileField;
	private FormData fdlFileField,fdFileField;
      
    private Label wlFileNameField;
    private CCombo wFileNameField;
    private FormData fdFileNameField,fdlFileNameField;
	
    private Label wlEncoding;
    private CCombo wEncoding;
    private FormData fdlEncoding, fdEncoding;
    
    private Label wlAccStep;
	private CCombo wAccStep;
	private FormData fdlAccStep, fdAccStep;
    
	private GISFileOutputMeta input;
	private boolean backupChanged;
	private boolean gotEncodings = false;

	public GISFileOutputDialog(Shell parent, Object out, TransMeta tr, String sname){
		super(parent, (BaseStepMeta)out, tr, sname);
		input=(GISFileOutputMeta)out;
	}

	public String open(){
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);
        setShellImage(shell, input);

		ModifyListener lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				input.setChanged();
			}
		};
		
		backupChanged = input.hasChanged();
		
		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG,"GISFileOutputDialog.Dialog.Title")); //$NON-NLS-1$

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG,"System.Label.StepName")); //$NON-NLS-1$
		props.setLook(wlStepname);
		fdlStepname = new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right = new FormAttachment(middle, -margin);
		fdlStepname.top = new FormAttachment(0, margin*2);
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

		// FileName line
		wlFileName = new Label(shell, SWT.RIGHT);
		wlFileName.setText(BaseMessages.getString(PKG,"System.Label.Filename")); //$NON-NLS-1$
 		props.setLook(wlFileName);
		fdlFileName=new FormData();
		fdlFileName.left = new FormAttachment(0, 0);
		fdlFileName.top  = new FormAttachment(wStepname, margin*2);
		fdlFileName.right= new FormAttachment(middle, -margin);
		wlFileName.setLayoutData(fdlFileName);

		wbFileName=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbFileName);
		wbFileName.setText(BaseMessages.getString(PKG,"System.Button.Browse")); //$NON-NLS-1$
		fdbFileName=new FormData();
		fdbFileName.right= new FormAttachment(100, 0);
		fdbFileName.top  = new FormAttachment(wStepname, margin);
		wbFileName.setLayoutData(fdbFileName);
		
		wFileName=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFileName);
		wFileName.addModifyListener(lsMod);
		fdFileName=new FormData();
		fdFileName.left = new FormAttachment(middle, 0);
		fdFileName.right= new FormAttachment(wbFileName, -margin);
		fdFileName.top  = new FormAttachment(wStepname, margin);
		wFileName.setLayoutData(fdFileName);
		
		//Is FileName defined in a Field				        
	    wlFileField=new Label(shell, SWT.RIGHT);
	    wlFileField.setText(BaseMessages.getString(PKG,"GISFileOutputDialog.FilenameInField.Label"));
        props.setLook(wlFileField);
        fdlFileField=new FormData();
        fdlFileField.left = new FormAttachment(0, 0);
        fdlFileField.right = new FormAttachment(middle, -margin);
        fdlFileField.top  = new FormAttachment(wFileName, margin*2);
        wlFileField.setLayoutData(fdlFileField);
        
        wFileField=new Button(shell, SWT.CHECK);
        wFileField.setToolTipText(BaseMessages.getString(PKG,"GISFileOutputDialog.FilenameInField.Tooltip"));
	    props.setLook(wFileField);
	    fdFileField=new FormData();
	    fdFileField.right  = new FormAttachment(100, 0);
	    fdFileField.top   = new FormAttachment(wFileName, margin);
	    fdFileField.left   = new FormAttachment(middle, 0);
	    wFileField.setLayoutData(fdFileField);
	    wFileField.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent arg0) {
            	activeFileField();
            	input.setChanged();
            }
        });
        
		wlAccStep=new Label(shell, SWT.RIGHT);
		wlAccStep.setText(BaseMessages.getString(PKG,"GISFileOutputDialog.AcceptStep.Label"));
		props.setLook(wlAccStep);
		fdlAccStep=new FormData();
		fdlAccStep.top  = new FormAttachment(wFileField, margin*2);
		fdlAccStep.left = new FormAttachment(0, 0);
		fdlAccStep.right= new FormAttachment(middle, -margin);
		wlAccStep.setLayoutData(fdlAccStep);
		wAccStep=new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wAccStep.setToolTipText(BaseMessages.getString(PKG,"GISFileOutputDialog.AcceptStep.Tooltip"));
		props.setLook(wAccStep);
		fdAccStep=new FormData();
		fdAccStep.top  = new FormAttachment(wFileField, margin);
		fdAccStep.left = new FormAttachment(middle, 0);
		fdAccStep.right= new FormAttachment(100, 0);
		wAccStep.setLayoutData(fdAccStep);

		// Fill in the source steps...
		List<StepMeta> prevSteps = transMeta.findPreviousSteps(transMeta.findStep(stepname));
		for (StepMeta prevStep : prevSteps){
			wAccStep.add(prevStep.getName());
		}		
		
		// FileName field
		wlFileNameField=new Label(shell, SWT.RIGHT);
        wlFileNameField.setText(BaseMessages.getString(PKG,"GISFileOutputDialog.FilenameField.Label"));
        props.setLook(wlFileNameField);
        fdlFileNameField=new FormData();
        fdlFileNameField.left = new FormAttachment(0, 0);
        fdlFileNameField.top  = new FormAttachment(wAccStep,2* margin);
        fdlFileNameField.right= new FormAttachment(middle, -margin);
        wlFileNameField.setLayoutData(fdlFileNameField);
            
        wFileNameField=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
        wFileNameField.setEditable(true);
        props.setLook(wFileNameField);
        wFileNameField.addModifyListener(lsMod);
        fdFileNameField=new FormData();
        fdFileNameField.left = new FormAttachment(middle, 0);
        fdFileNameField.top  = new FormAttachment(wAccStep, margin);
        fdFileNameField.right= new FormAttachment(100, 0);
        wFileNameField.setLayoutData(fdFileNameField);
        wFileNameField.addFocusListener(new FocusListener(){
            public void focusLost(org.eclipse.swt.events.FocusEvent e){
            }
        
            public void focusGained(org.eclipse.swt.events.FocusEvent e){
                Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                shell.setCursor(busy);
                setFileField();
                shell.setCursor(null);
                busy.dispose();
            }
        });                       
		
        //Encoding
        wlEncoding=new Label(shell, SWT.RIGHT);
	    wlEncoding.setText(BaseMessages.getString(PKG,"GISFileOutputDialog.Encoding.Label"));
	    props.setLook(wlEncoding);
	    fdlEncoding=new FormData();
	    fdlEncoding.left = new FormAttachment(0, 0);
	    fdlEncoding.top  = new FormAttachment(wFileNameField, margin*2);
	    fdlEncoding.right= new FormAttachment(middle, -margin);
	    wlEncoding.setLayoutData(fdlEncoding);
	    wEncoding=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
	    wEncoding.setEditable(true);
	    props.setLook(wEncoding);
	    setEncodings();
	    wEncoding.addModifyListener(lsMod);
	    fdEncoding=new FormData();
	    fdEncoding.left = new FormAttachment(middle, 0);
	    fdEncoding.top  = new FormAttachment(wFileNameField, margin);
	    fdEncoding.right= new FormAttachment(100, 0);
	    wEncoding.setLayoutData(fdEncoding);
      
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG,"System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG,"System.Button.Cancel")); //$NON-NLS-1$
		
		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
        wOK.addListener    (SWT.Selection, lsOK    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );

		wFileName.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent arg0){
				wFileName.setToolTipText(transMeta.environmentSubstitute(wFileName.getText()));
			}
		});	

		wbFileName.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				dialog.setFilterExtensions(GISFILE_FILTER_EXT); //$NON-NLS-1$ //$NON-NLS-2$
				if (wFileName.getText()!=null)
					dialog.setFileName(wFileName.getText());
								
				dialog.setFilterNames(new String[] {BaseMessages.getString(PKG,"GISFileOutputDialog.Filter.SHPFiles"), BaseMessages.getString(PKG,"System.FileType.AllFiles")}); //$NON-NLS-1$ //$NON-NLS-2$
				
				if (dialog.open()!=null){
					String str = dialog.getFilterPath()+ Const.FILE_SEPARATOR+dialog.getFileName();
					wFileName.setText(str);
				}
			}
		});
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
		
		getData();
		input.setChanged(changed);

		// Set the shell size, based upon previous time...
		setSize();
		activeFileField();
		shell.open();
		while (!shell.isDisposed()){
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	/**
	 * Copy information from the meta-data Output to the dialog fields.
	 */ 
	public void getData(){
		if (!input.isFileNameInField() ) {
			if (input.getFileName() != null){
				wFileName.setText(input.getFileName());
				wFileName.setToolTipText(transMeta.environmentSubstitute(input.getFileName()));
			}
		}else{
			wFileField.setSelection(true);
			if(input.getAcceptingStep()!=null) 
				wAccStep.setText(input.getAcceptingStep().getName());
			if(input.getFileNameField() !=null)
				wFileNameField.setText(input.getFileNameField());		
		}
		wStepname.selectAll();
	}
	
	
	private void setFileField(){
		try{
			wFileNameField.removeAll();
			if(!Const.isEmpty(wAccStep.getText())){
				RowMetaInterface r = transMeta.getStepFields(wAccStep.getText());
				if (r!=null){
					r.getFieldNames();
					for (int i=0;i<r.getFieldNames().length;i++){	
						wFileNameField.add(r.getFieldNames()[i]);									
					}
				}	
			}				
		}catch(KettleException ke){
			new ErrorDialog(shell, BaseMessages.getString(PKG,"GISFileOutputDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG,"GISFileOutputDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	private void activeFileField(){
		wlFileNameField.setEnabled(wFileField.getSelection());
		wFileNameField.setEnabled(wFileField.getSelection());
		wlAccStep.setEnabled(wFileField.getSelection());
		wAccStep.setEnabled(wFileField.getSelection());
		wlFileName.setEnabled(!wFileField.getSelection());		
		wFileName.setEnabled(!wFileField.getSelection());
		wbFileName.setEnabled(!wFileField.getSelection());
	}
	
	private void setEncodings(){
	      // Encoding of the shapefile:
	      if (!gotEncodings){
	          gotEncodings = true;
	          
	          wEncoding.removeAll();
	          List<Charset> values = new ArrayList<Charset>(Charset.availableCharsets().values());
	          for (int i=0;i<values.size();i++){
	              Charset charSet = (Charset)values.get(i);
	              wEncoding.add( charSet.displayName() );
	          }
	          
	          // Now select the default!
	          String defEncoding;
			  if (input.getGisFileCharset()!=null)
				defEncoding = input.getGisFileCharset();
			  else defEncoding = Const.getEnvironmentVariable("file.encoding", "UTF-8");
	          int idx = Const.indexOfString(defEncoding, wEncoding.getItems() );
	          if (idx>=0) wEncoding.select( idx );
	     }
	}
	
	private void cancel(){
		stepname=null;
		input.setChanged(backupChanged);
		dispose();
	}
	
	public void getInfo(GISFileOutputMeta meta) throws KettleStepException {
		meta.setFileName( wFileName.getText() );
		meta.setFileNameInField(wFileField.getSelection());
		meta.setFileNameField(wFileNameField.getText());	
		meta.setGisFileCharset(wEncoding.getText());
		meta.setAcceptingStepName( wAccStep.getText() );
		meta.setAcceptingStep( transMeta.findStep( wAccStep.getText() ) );
	}
	
	private void ok(){
		try{
			stepname = wStepname.getText(); // return value
			getInfo(input);
			dispose();
		}catch(KettleStepException e){
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(e.toString());
			mb.setText(BaseMessages.getString(PKG,"System.Warning")); //$NON-NLS-1$
			mb.open();

			dispose();
		}
	}	
}
