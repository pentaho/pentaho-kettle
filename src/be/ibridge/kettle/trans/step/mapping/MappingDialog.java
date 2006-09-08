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

import java.util.List;

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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.SourceToTargetMapping;
import be.ibridge.kettle.core.dialog.EnterMappingDialog;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.repository.RepositoryDirectory;
import be.ibridge.kettle.repository.dialog.SelectObjectDialog;
import be.ibridge.kettle.spoon.Spoon;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.mappinginput.MappingInputMeta;
import be.ibridge.kettle.trans.step.mappingoutput.MappingOutputMeta;


public class MappingDialog extends BaseStepDialog implements StepDialogInterface
{
	private MappingMeta input;
    
    private Label wlTransformation;
    private FormData fdlTransformation, fdTransformation;
    private Text wTransformation;
    private Button wbTransformation, weTransformation;
    private FormData fdbTransformation, fdeTransformation;

    
    private TableView    wInputFields;
    private FormData     fdInputFields;

    private Button       wbInput;
    private FormData     fdbInput;

    private TableView    wOutputFields;
    private FormData     fdOutputFields;
    
    private Button       wbOutput;
    private FormData     fdbOutput;
    
    TransMeta mappingTransMeta = null;

    protected boolean transModified;
    
	public MappingDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(MappingMeta )in;
        transModified=false;
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
		shell.setText(Messages.getString("MappingDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("MappingDialog.Stepname.Label")); //$NON-NLS-1$
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
        wlTransformation.setText(Messages.getString("MappingDialog.Transformation.Label")); //$NON-NLS-1$
        props.setLook(wlTransformation);
        fdlTransformation=new FormData();
        fdlTransformation.left = new FormAttachment(0, 0);
        fdlTransformation.right= new FormAttachment(middle, -margin);
        fdlTransformation.top  = new FormAttachment(wStepname, margin);
        wlTransformation.setLayoutData(fdlTransformation);

        weTransformation=new Button(shell, SWT.PUSH );
        weTransformation.setText(Messages.getString("MappingDialog.Edit.Button")); //$NON-NLS-1$
        props.setLook(weTransformation);
        fdeTransformation=new FormData();
        fdeTransformation.right= new FormAttachment(100, 0);
        fdeTransformation.top  = new FormAttachment(wStepname, margin);
        weTransformation.setLayoutData(fdeTransformation);

        wbTransformation=new Button(shell, SWT.PUSH );
        wbTransformation.setText(Messages.getString("MappingDialog.Select.Button")); //$NON-NLS-1$
        props.setLook(wbTransformation);
        fdbTransformation=new FormData();
        fdbTransformation.right= new FormAttachment(weTransformation, 0);
        fdbTransformation.top  = new FormAttachment(wStepname, margin);
        wbTransformation.setLayoutData(fdbTransformation);
        
        wTransformation=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wTransformation.setEditable(true);
        props.setLook(wTransformation);
        wTransformation.addModifyListener(lsMod);
        fdTransformation=new FormData();
        fdTransformation.left = new FormAttachment(middle, 0);
        fdTransformation.right= new FormAttachment(wbTransformation, -margin);
        fdTransformation.top  = new FormAttachment(wStepname, margin);
        wTransformation.setLayoutData(fdTransformation);
  
        wTransformation.addModifyListener(new ModifyListener()
            {
                public void modifyText(ModifyEvent e)
                {
                    transModified = true;
                }
            }
        );
        
        wbTransformation.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    selectTrans();
                }
            }
        );
        
        weTransformation.addSelectionListener(new SelectionAdapter()
            {
            
                public void widgetSelected(SelectionEvent e)
                {
                    editTrans();
                }
            
            }
        );
		
        /*
         * INPUT MAPPING CONNECTORS
         */
        
        ColumnInfo[] colinfo=new ColumnInfo[]
        {
            new ColumnInfo(Messages.getString("MappingDialog.ColumnInfo.InputField"),    ColumnInfo.COLUMN_TYPE_TEXT,    false ), //$NON-NLS-1$
            new ColumnInfo(Messages.getString("MappingDialog.ColumnInfo.InputMapping"),  ColumnInfo.COLUMN_TYPE_TEXT,    false ) //$NON-NLS-1$
        };
        colinfo[ 1].setToolTip(Messages.getString("MappingDialog.InputMapping.ToolTip")); //$NON-NLS-1$
        
        wInputFields = new TableView(shell, 
                              SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER, 
                              colinfo, 
                              input.getInputField()!=null?input.getInputField().length:1,  
                              lsMod,
                              props
                              );
        props.setLook(wInputFields);
        fdInputFields=new FormData();
        fdInputFields.left   = new FormAttachment(0, 0);
        fdInputFields.right  = new FormAttachment(50, -margin);
        fdInputFields.top    = new FormAttachment(wbTransformation, margin);
        fdInputFields.bottom = new FormAttachment(100, -75);
        wInputFields.setLayoutData(fdInputFields);
        
        wbInput = new Button(shell, SWT.PUSH);
        wbInput.setText(Messages.getString("MappingDialog.GetFromMapping.Button")); //$NON-NLS-1$
        fdbInput=new FormData();
        fdbInput.left   = new FormAttachment(0, 0);
        fdbInput.right  = new FormAttachment(50, -margin);
        fdbInput.top    = new FormAttachment(wInputFields, margin);
        wbInput.setLayoutData(fdbInput);
        wbInput.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    getInput();
                }            
            }
        );
       

        /*
         * OUTPUT MAPPING CONNECTORS
         */
        
        ColumnInfo[] colinfoOutput = new ColumnInfo[] 
        { 
            new ColumnInfo(Messages.getString("MappingDialog.ColumnInfo.OutputMapping"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
            new ColumnInfo(Messages.getString("MappingDialog.ColumnInfo.OutputField"),   ColumnInfo.COLUMN_TYPE_TEXT, false)  //$NON-NLS-1$
        };

        wOutputFields = new TableView(shell, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER, colinfoOutput, input.getOutputField() != null ? input
                .getOutputField().length : 1, lsMod, props);
        props.setLook(wOutputFields);
        fdOutputFields = new FormData();
        fdOutputFields.left = new FormAttachment(50, 0);
        fdOutputFields.right = new FormAttachment(100, 0);
        fdOutputFields.top = new FormAttachment(wbTransformation, margin);
        fdOutputFields.bottom = new FormAttachment(100, -75);
        wOutputFields.setLayoutData(fdOutputFields);

        wbOutput = new Button(shell, SWT.PUSH);
        wbOutput.setText(Messages.getString("MappingDialog.GetFromMapping.Button")); //$NON-NLS-1$
        fdbOutput=new FormData();
        fdbOutput.left   = new FormAttachment(50, 0);
        fdbOutput.right  = new FormAttachment(100, 0);
        fdbOutput.top    = new FormAttachment(wOutputFields, margin);
        wbOutput.setLayoutData(fdbOutput);
        wbOutput.addSelectionListener(new SelectionAdapter()
                {
                    public void widgetSelected(SelectionEvent e)
                    {
                        getOutput();
                    }            
                }
            );


		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wbInput);

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
	
    private void selectTrans()
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
                    mappingTransMeta.clearChanged();
                    updateTransformationPath(mappingTransMeta);
                }
            }
            catch(KettleException ke)
            {
                new ErrorDialog(shell, props, Messages.getString("MappingDialog.ErrorSelectingObject.DialogTitle"), Messages.getString("MappingDialog.ErrorSelectingObject.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        else
        {
            FileDialog dialog = new FileDialog(shell, SWT.OPEN);
            dialog.setFilterExtensions(Const.STRING_TRANS_FILTER_EXT);
            dialog.setFilterNames(Const.STRING_TRANS_FILTER_NAMES);
            String fname = dialog.open();
            if (fname!=null)
            {
                try
                {
                    mappingTransMeta = new TransMeta(fname);
                    mappingTransMeta.clearChanged();                    
                    updateTransformationPath(mappingTransMeta);
                }
                catch(KettleException e)
                {
                    new ErrorDialog(shell, props, Messages.getString("MappingDialog.ErrorLoadingTransformation.DialogTitle"), Messages.getString("MappingDialog.ErrorLoadingTransformation.DialogMessage"), e); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }
    }

    private void editTrans()
    {
        LogWriter log = LogWriter.getInstance();
        if (mappingTransMeta!=null)
        {
            Spoon spoon = new Spoon(log, shell.getDisplay(), mappingTransMeta, repository);
            spoon.open();
        }
        else
        {
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
            mb.setMessage(Messages.getString("MappingDialog.TransformationSelecting.DialogMessage")); //$NON-NLS-1$
            mb.setText(Messages.getString("MappingDialog.TransformationSelecting.DialogTitle")); //$NON-NLS-1$
            mb.open();
        }
    }

    private void getInput()
    {
        // Get the fields from the mapping...
        if (mappingTransMeta!=null)
        {
            StepMeta inputStepMeta  = mappingTransMeta.getMappingInputStep();
            
            if (inputStepMeta!=null)
            {
                MappingInputMeta mappingInputMeta = (MappingInputMeta) inputStepMeta.getStepMetaInterface();
                
                String[] source = mappingInputMeta.getFieldName();
                
                Row prev = null;
                try
                {
                	prev=transMeta.getPrevStepFields(stepname);
                }
                catch(KettleException e)
                {
                	new ErrorDialog(shell, props, Messages.getString("MappingDialog.ErrorGettingPreviousFields.DialogTitle"), Messages.getString("MappingDialog.ErrorGettingPreviousFields.DialogMessage"), e); //$NON-NLS-1$ //$NON-NLS-2$
                }
                
                if (prev!=null)
                {
	                String[] target = prev.getFieldNames();
	                
	                EnterMappingDialog dialog = new EnterMappingDialog(shell, source, target);
	                List mappings = dialog.open();
	                if (mappings!=null)
	                {
	                	for (int i=0;i<mappings.size();i++)
	                	{
	                		TableItem item = new TableItem(wInputFields.table, SWT.NONE);
	                		SourceToTargetMapping mapping = (SourceToTargetMapping) mappings.get(i);
	                		item.setText(2, mapping.getSourceString(source));
	                		item.setText(1, mapping.getTargetString(target));
	                	}
	                }
                }
                else
                {
	                log.logDetailed(stepname, Messages.getString("MappingDialog.Log.GettingInputFields")+source.length+")"); //$NON-NLS-1$ //$NON-NLS-2$
	                for (int i=0;i<source.length;i++)
	                {
	                    TableItem item = new TableItem(wInputFields.table, SWT.NONE);
	                    item.setText(2, source[i]);
	                }
                }
                wInputFields.removeEmptyRows();
                wInputFields.setRowNums();
                wInputFields.optWidth(true);
            }
            else
            {
                MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
                mb.setMessage(Messages.getString("MappingDialog.MappingInputStepNeeded.DialogMessage")); //$NON-NLS-1$
                mb.setText(Messages.getString("MappingDialog.MappingInputStepNeeded.DialogTitle")); //$NON-NLS-1$
                mb.open();
            }
        }
        else
        {
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
            mb.setMessage(Messages.getString("MappingDialog.NoMappingSpecified.DialogMessage")); //$NON-NLS-1$
            mb.setText(Messages.getString("MappingDialog.NoMappingSpecified.DialogTitle")); //$NON-NLS-1$
            mb.open();
        }
    }

    private void getOutput()
    {
        // Get the fields from the mapping...
        if (mappingTransMeta!=null)
        {
            StepMeta outputStepMeta  = mappingTransMeta.getMappingOutputStep();
            
            if (outputStepMeta!=null)
            {
                MappingOutputMeta mappingOutputMeta = (MappingOutputMeta) outputStepMeta.getStepMetaInterface();
                System.out.println(Messages.getString("MappingDialog.Log.GettingInputFields")+mappingOutputMeta.getFieldName().length+")"); //$NON-NLS-1$ //$NON-NLS-2$

                for (int i=0;i<mappingOutputMeta.getFieldName().length;i++)
                {
                    if (mappingOutputMeta.getFieldAdded()[i]) // We can only map added fields!
                    {
                        TableItem item = new TableItem(wOutputFields.table, SWT.NONE);
                        item.setText(1, mappingOutputMeta.getFieldName()[i]);
                        item.setText(2, mappingOutputMeta.getFieldName()[i]);
                    }
                }
                
                wOutputFields.removeEmptyRows();
                wOutputFields.setRowNums();
                wOutputFields.optWidth(true);
            }
            else
            {
                MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
                mb.setMessage(Messages.getString("MappingDialog.MappingOutputStepNeeded.DialogMessage")); //$NON-NLS-1$
                mb.setText(Messages.getString("MappingDialog.MappingOutputStepNeeded.DialogTitle")); //$NON-NLS-1$
                mb.open();
            }
        }
        else
        {
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
            mb.setMessage(Messages.getString("MappingDialog.NoMappingSpecified2.DialogMessage")); //$NON-NLS-1$
            mb.setText(Messages.getString("MappingDialog.NoMappingSpecified2.DialogTitle")); //$NON-NLS-1$
            mb.open();
        }
    }

    /**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		wStepname.selectAll();
        mappingTransMeta = input.getMappingTransMeta();
        updateTransformationPath(mappingTransMeta);
        
        if (input.getInputField()!=null)
        for (int i=0;i<input.getInputField().length;i++)
        {
            TableItem item = new TableItem(wInputFields.table, SWT.NONE);
            if (input.getInputField()[i]!=null) item.setText(1, input.getInputField()[i]);
            if (input.getInputMapping()[i]!=null) item.setText(2, input.getInputMapping()[i]);
        }
        
        if (input.getOutputField()!=null)
        for (int i=0;i<input.getOutputField().length;i++)
        {
            TableItem item = new TableItem(wOutputFields.table, SWT.NONE);
            if (input.getOutputMapping()[i]!=null) item.setText(1, input.getOutputMapping()[i]);
            if (input.getOutputField()[i]!=null) item.setText(2, input.getOutputField()[i]);
        }

        wInputFields.removeEmptyRows();
        wInputFields.setRowNums();
        wInputFields.optWidth(true);

        wOutputFields.removeEmptyRows();
        wOutputFields.setRowNums();
        wOutputFields.optWidth(true);
	}
    
    private void updateTransformationPath(TransMeta tm )
    {
        if (tm!=null)
        {
            String transName = tm.getName();
            RepositoryDirectory repdir = tm.getDirectory();
            String fileName = tm.getFilename();
            
            if (fileName!=null)
            {
                wTransformation.setText(fileName);
                transModified=false;
            }
            else
            if (repdir!=null)
            {
                if (repdir.isRoot()) wTransformation.setText( repdir+transName ); 
                else wTransformation.setText( repdir+RepositoryDirectory.DIRECTORY_SEPARATOR+transName );
                
                transModified=false;
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
		
        if (Const.isEmpty(wTransformation.getText()))
        {
            input.setMappingTransMeta(null);
        }
        else
        {
            if (transModified && repository==null) // someone manually entered a transformation to an XML file... 
            {
                try
                {
                    if (repository==null)
                    {
                        input.setFileName(wTransformation.getText());
                        input.loadMappingMeta(repository);
                        input.getMappingTransMeta().setFilename(wTransformation.getText());
                    }
                }
                catch(KettleException e)
                {
                    new ErrorDialog(shell, props, "Error", "There was an error parsing transformation ["+wTransformation.getText()+"]");
                }
            }
            else
            {
                input.setMappingTransMeta(mappingTransMeta);
            }
        }
        
        int nrInput  = wInputFields.nrNonEmpty();
        int nrOutput = wOutputFields.nrNonEmpty();
        
        input.allocate(nrInput, nrOutput);
        
        for (int i=0;i<nrInput;i++)
        {
            TableItem item = wInputFields.getNonEmpty(i);
            input.getInputField()[i] = item.getText(1);
            input.getInputMapping()[i] = item.getText(2);
        }

        for (int i=0;i<nrOutput;i++)
        {
            TableItem item = wOutputFields.getNonEmpty(i);
            input.getOutputMapping()[i] = item.getText(1);
            input.getOutputField()[i] = item.getText(2);
        }

		dispose();
	}
}
