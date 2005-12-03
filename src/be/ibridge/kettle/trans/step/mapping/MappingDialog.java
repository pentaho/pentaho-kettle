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

/*
 * Created on 18-mei-2003
 *
 */

package be.ibridge.kettle.trans.step.mapping;

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

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.repository.RepositoryDirectory;
import be.ibridge.kettle.repository.dialog.SelectObjectDialog;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDialogInterface;


public class MappingDialog extends BaseStepDialog implements StepDialogInterface
{
	private MappingMeta input;
    
    private Label wlTransformation;
    private FormData fdlTransformation, fdTransformation;
    private Text wTransformation;

    private Button wbTransformation;

    private FormData fdbTransformation;

    TransMeta mappingTransMeta = null;
    
	public MappingDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(MappingMeta )in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
 		props.setLook(shell);

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
		shell.setText("Mapping (excute sub-transformation)");
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText("Step name ");
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
        
        // Transformation line
        wlTransformation=new Label(shell, SWT.RIGHT);
        wlTransformation.setText("Transformation ");
        props.setLook(wlTransformation);
        fdlTransformation=new FormData();
        fdlTransformation.left = new FormAttachment(0, 0);
        fdlTransformation.right= new FormAttachment(middle, -margin);
        fdlTransformation.top  = new FormAttachment(wStepname, margin);
        wlTransformation.setLayoutData(fdlTransformation);
        
        wbTransformation=new Button(shell, SWT.PUSH );
        wbTransformation.setText("...");
        props.setLook(wbTransformation);
        fdbTransformation=new FormData();
        fdbTransformation.right= new FormAttachment(100, 0);
        fdbTransformation.top  = new FormAttachment(wStepname, margin);
        wbTransformation.setLayoutData(fdbTransformation);
        
        wTransformation=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wTransformation.setEditable(false);
        props.setLook(wTransformation);
        wTransformation.addModifyListener(lsMod);
        fdTransformation=new FormData();
        fdTransformation.left = new FormAttachment(middle, 0);
        fdTransformation.right= new FormAttachment(wbTransformation, -margin);
        fdTransformation.top  = new FormAttachment(wStepname, margin);
        wTransformation.setLayoutData(fdTransformation);
  
        wbTransformation.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    if (repository!=null)
                    {
                        try
                        {
                            SelectObjectDialog sod = new SelectObjectDialog(shell, props, repository, true, false, false);
                            String transName = sod.open();
                            RepositoryDirectory repdir = sod.getDirectory();
                            if (transName!=null && repdir!=null)
                            {
                                // Read the transformation...
                                //
                                mappingTransMeta = new TransMeta(repository, transName, repdir);
                                System.out.println("transformation id: "+transMeta.getID());
                                
                                updateTransformationPath(mappingTransMeta);
                            }
                        }
                        catch(KettleException ke)
                        {
                            new ErrorDialog(shell, props, "Error", "Error selecting object from repository", ke);
                        }
                    }
                    else
                    {
                        // TODO: get the filename with a FileDialog
                        new ErrorDialog(shell, props, "Sorry", "XML Files are not yet supported", new KettleException("XML Files are not yet accepted"));
                    }
                }
            }
        );
		
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText("  &OK  ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText("  &Cancel  ");

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wTransformation);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		input.setChanged(changed);
	
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		wStepname.selectAll();
        mappingTransMeta = input.getMappingTransMeta();
        if (repository!=null)
        {
            updateTransformationPath(mappingTransMeta);
        }
	}
    
    private void updateTransformationPath(TransMeta tm )
    {
        if (tm!=null)
        {
            String transName = tm.getName();
            RepositoryDirectory repdir = tm.getDirectory();
            String fileName = tm.getFilename();
            
            if (repdir!=null)
            {
                if (repdir.isRoot()) wTransformation.setText( repdir+transName ); 
                else wTransformation.setText( repdir+Const.FILE_SEPARATOR+transName );
            }
            else
            {
                if (fileName!=null)
                {
                    wTransformation.setText(fileName);
                }
            }
        }
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
		
        input.setMappingTransMeta(mappingTransMeta);
        
		dispose();
	}
}
