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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.SourceToTargetMapping;
import be.ibridge.kettle.core.dialog.EnterMappingDialog;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.core.widget.TextVar;
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

    private Group       gTransGroup;
    private FormData    fdTransGroup;
    
    // File
    private Button      wFileRadio;
    private FormData    fdFileRadio;
    
    private Button       wbbFilename; // Browse: add file or directory
    private TextVar      wFilename;
    private FormData     fdbFilename, fdFilename;
    
    // Repository
    private Button      wRepRadio;
    private FormData    fdRepRadio;
    
    private TextVar     wTransName, wTransDir;
    private FormData    fdTransName, fdTransDir;
    private Button      wbTrans;
    private FormData    fdbTrans;
    
    private Button      wEditTrans;
    private FormData    fdEditTrans;
    
    private TableView   wInputFields;
    private FormData    fdInputFields;

    private Button      wbInput;
    private FormData    fdbInput;

    private TableView   wOutputFields;
    private FormData    fdOutputFields;
    
    private Button      wbOutput;
    private FormData    fdbOutput;
    
    TransMeta mappingTransMeta = null;

    protected boolean transModified;


    
	public MappingDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(MappingMeta)in;
        transModified=false;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
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

        // Show a group with 2 main options: a transformation in the repository or on file
        //
        
        ////////////////////////////////////////////////////
        // The key creation box
        ////////////////////////////////////////////////////
        //
        gTransGroup = new Group(shell, SWT.SHADOW_ETCHED_IN);
        gTransGroup.setText(Messages.getString("MappingDialog.TransGroup.Label")); //$NON-NLS-1$;
        gTransGroup.setBackground(shell.getBackground()); // the default looks ugly
        FormLayout transGroupLayout = new FormLayout();
        transGroupLayout.marginLeft=margin*2;
        transGroupLayout.marginTop=margin*2;
        transGroupLayout.marginRight=margin*2;
        transGroupLayout.marginBottom=margin*2;
        gTransGroup.setLayout(transGroupLayout);
        
        // Radio button: The mapping is in a file 
        // 
        wFileRadio=new Button(gTransGroup, SWT.RADIO);
        props.setLook(wFileRadio);
        wFileRadio.setSelection(false);
        wFileRadio.setText(Messages.getString("MappingDialog.RadioFile.Label")); //$NON-NLS-1$
        wFileRadio.setToolTipText(Messages.getString("MappingDialog.RadioFile.Tooltip",Const.CR)); //$NON-NLS-1$ //$NON-NLS-2$
        fdFileRadio=new FormData();
        fdFileRadio.left   = new FormAttachment(0, 0);
        fdFileRadio.right  = new FormAttachment(100, 0);
        fdFileRadio.top    = new FormAttachment(0, 0); 
        wFileRadio.setLayoutData(fdFileRadio);
        
        wbbFilename=new Button(gTransGroup, SWT.PUSH| SWT.CENTER); // Browse
        props.setLook(wbbFilename);
        wbbFilename.setText(Messages.getString("System.Button.Browse"));
        wbbFilename.setToolTipText(Messages.getString("System.Tooltip.BrowseForFileOrDirAndAdd"));
        fdbFilename=new FormData();
        fdbFilename.right= new FormAttachment(100, 0);
        fdbFilename.top  = new FormAttachment(wFileRadio, margin);
        wbbFilename.setLayoutData(fdbFilename);
        wbbFilename.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { selectFileTrans(); }});

        wFilename=new TextVar(gTransGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wFilename);
        wFilename.addModifyListener(lsMod);
        fdFilename=new FormData();
        fdFilename.left = new FormAttachment(0, 25);
        fdFilename.right= new FormAttachment(wbbFilename, -margin);
        fdFilename.top  = new FormAttachment(wbbFilename, 0, SWT.CENTER);
        wFilename.setLayoutData(fdFilename);
        wFilename.addModifyListener(new ModifyListener() { public void modifyText(ModifyEvent e) { wFileRadio.setSelection(true); wRepRadio.setSelection(false); }});
        
        // Radio button: The mapping is in the repository
        // 
        wRepRadio=new Button(gTransGroup, SWT.RADIO);
        props.setLook(wRepRadio);
        wRepRadio.setSelection(false);
        wRepRadio.setText(Messages.getString("MappingDialog.RadioRep.Label")); //$NON-NLS-1$
        wRepRadio.setToolTipText(Messages.getString("MappingDialog.RadioRep.Tooltip",Const.CR)); //$NON-NLS-1$ //$NON-NLS-2$
        fdRepRadio=new FormData();
        fdRepRadio.left   = new FormAttachment(0, 0);
        fdRepRadio.right  = new FormAttachment(100, 0);
        fdRepRadio.top    = new FormAttachment(wbbFilename, 2*margin); 
        wRepRadio.setLayoutData(fdRepRadio);

        wbTrans=new Button(gTransGroup, SWT.PUSH| SWT.CENTER); // Browse
        props.setLook(wbTrans);
        wbTrans.setText(Messages.getString("MappingDialog.Select.Button"));
        wbTrans.setToolTipText(Messages.getString("System.Tooltip.BrowseForFileOrDirAndAdd"));
        fdbTrans=new FormData();
        fdbTrans.right= new FormAttachment(100, 0);
        fdbTrans.top  = new FormAttachment(wRepRadio, 2*margin);
        wbTrans.setLayoutData(fdbTrans);
        wbTrans.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { selectRepositoryTrans(); }});
        
        wTransDir=new TextVar(gTransGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wTransDir);
        wTransDir.addModifyListener(lsMod);
        fdTransDir=new FormData();
        fdTransDir.left = new FormAttachment(middle+(100-middle)/2, 0);
        fdTransDir.right= new FormAttachment(wbTrans, -margin);
        fdTransDir.top  = new FormAttachment(wbTrans, 0, SWT.CENTER);
        wTransDir.setLayoutData(fdTransDir);

        wTransName=new TextVar(gTransGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wTransName);
        wTransName.addModifyListener(lsMod);
        fdTransName=new FormData();
        fdTransName.left = new FormAttachment(0, 25);
        fdTransName.right= new FormAttachment(wTransDir, -margin);
        fdTransName.top  = new FormAttachment(wbTrans, 0, SWT.CENTER);
        wTransName.setLayoutData(fdTransName);
        
        wEditTrans=new Button(gTransGroup, SWT.PUSH| SWT.CENTER); // Browse
        props.setLook(wEditTrans);
        wEditTrans.setText(Messages.getString("MappingDialog.Edit.Button"));
        wEditTrans.setToolTipText(Messages.getString("System.Tooltip.BrowseForFileOrDirAndAdd"));
        fdEditTrans=new FormData();
        fdEditTrans.left = new FormAttachment(0,   0);
        fdEditTrans.right= new FormAttachment(100, 0);
        fdEditTrans.top  = new FormAttachment(wTransName, 3*margin);
        wEditTrans.setLayoutData(fdEditTrans);
        wEditTrans.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { editTrans(); }});

        fdTransGroup=new FormData();
        fdTransGroup.left   = new FormAttachment(0, 0);
        fdTransGroup.top    = new FormAttachment(wStepname, 2*margin); 
        fdTransGroup.right  = new FormAttachment(100, 0);
        // fdTransGroup.bottom = new FormAttachment(wStepname, 350);
        gTransGroup.setLayoutData(fdTransGroup);

        


        

        /*
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
        fdInputFields.top    = new FormAttachment(gTransGroup, margin*2);
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
        fdOutputFields.top = new FormAttachment(gTransGroup, margin*2);
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

		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);

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
	
    private void selectRepositoryTrans()
    {
        try
        {
            SelectObjectDialog sod = new SelectObjectDialog(shell, repository);
            String transName = sod.open();
            RepositoryDirectory repdir = sod.getDirectory();
            if (transName!=null && repdir!=null)
            {
                loadRepositoryTrans(transName, repdir);
                wTransName.setText(mappingTransMeta.getName());
                wTransDir.setText(mappingTransMeta.getDirectory().getPath());
                wFilename.setText("");
                wRepRadio.setSelection(true);
                wFileRadio.setSelection(false);
            }
        }
        catch(KettleException ke)
        {
            new ErrorDialog(shell, Messages.getString("MappingDialog.ErrorSelectingObject.DialogTitle"), Messages.getString("MappingDialog.ErrorSelectingObject.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    private void loadRepositoryTrans(String transName, RepositoryDirectory repdir) throws KettleException
    {
        // Read the transformation...
        //
        mappingTransMeta = new TransMeta(repository, StringUtil.environmentSubstitute(transName), repdir);
        mappingTransMeta.clearChanged();
    }

    private void selectFileTrans()
    {
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setFilterExtensions(Const.STRING_TRANS_FILTER_EXT);
        dialog.setFilterNames(Const.getTransformationFilterNames());
        String fname = dialog.open();
        if (fname!=null)
        {
            try
            {
                loadFileTrans(fname);
                wFilename.setText(mappingTransMeta.getFilename());
                wTransName.setText(Const.NVL(mappingTransMeta.getName(), ""));
                wTransDir.setText("");
                wFileRadio.setSelection(true);
                wRepRadio.setSelection(false);
            }
            catch(KettleException e)
            {
                new ErrorDialog(shell, Messages.getString("MappingDialog.ErrorLoadingTransformation.DialogTitle"), Messages.getString("MappingDialog.ErrorLoadingTransformation.DialogMessage"), e); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    private void loadFileTrans(String fname) throws KettleException
    {
        mappingTransMeta = new TransMeta(StringUtil.environmentSubstitute(fname));
        mappingTransMeta.clearChanged();
    }

    private void editTrans()
    {
        // Load the transformation again to make sure it's still there and refreshed
        // It's an extra check to make sure it's still OK...
        
        try
        {
            loadTransformation();
            
			// If we're still here, mappingTransMeta is valid.
			Spoon spoon = Spoon.getInstance();
			if (spoon != null)
			{
				spoon.addSpoonGraph(mappingTransMeta);
			}
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, Messages.getString("MappingDialog.ErrorShowingTransformation.Title"), Messages.getString("MappingDialog.ErrorShowingTransformation.Message"), e);
        }
    }

    private void loadTransformation() throws KettleException
    {
        if (wFileRadio.getSelection() && !Const.isEmpty(wFilename.getText())) // Read from file...
        {
            loadFileTrans(wFilename.getText());
        }
        else
        {
            if (wRepRadio.getSelection() && repository!=null && !Const.isEmpty(wTransName.getText()) && !Const.isEmpty(wTransDir.getText()) )
            {
                RepositoryDirectory repdir = repository.getDirectoryTree().findDirectory(wTransDir.getText());
                if (repdir==null)
                {
                    throw new KettleException(Messages.getString("MappingDialog.Exception.UnableToFindRepositoryDirectory)"));
                }
                loadRepositoryTrans(wTransName.getText(), repdir);
            }
            else
            {
                 throw new KettleException(Messages.getString("MappingDialog.Exception.NoValidMappingDetailsFound"));
            }
        }    
    }

    private void getInput()
    {
        try
        {
            loadTransformation();
            
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
                    	new ErrorDialog(shell, Messages.getString("MappingDialog.ErrorGettingPreviousFields.DialogTitle"), Messages.getString("MappingDialog.ErrorGettingPreviousFields.DialogMessage"), e); //$NON-NLS-1$ //$NON-NLS-2$
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
        catch(Exception e)
        {
            new ErrorDialog(shell, Messages.getString("MappingDialog.ErrorLoadingSpecifiedTransformation.Title"), 
                    Messages.getString("MappingDialog.ErrorLoadingSpecifiedTransformation.Message"), e);

        }
    }

    private void getOutput()
    {
        try
        {
            loadTransformation();
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
        catch(Exception e)
        {
            new ErrorDialog(shell, Messages.getString("MappingDialog.ErrorLoadingSpecifiedTransformation.Title"), 
                    Messages.getString("MappingDialog.ErrorLoadingSpecifiedTransformation.Message"), e);
        }
    }

    /**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		wStepname.selectAll();
        
        wFilename.setText(Const.NVL(input.getFileName(), ""));
        wTransName.setText(Const.NVL(input.getTransName(), ""));
        wTransDir.setText(Const.NVL(input.getDirectoryPath(), ""));

        // if we have a filename, then we use the filename, otherwise we go with the repository...
        if (!Const.isEmpty(input.getFileName()))
        {
            wFileRadio.setSelection(true);
        }
        else
        {
            if (repository!=null && !Const.isEmpty(input.getTransName()) && !Const.isEmpty(input.getDirectoryPath()))
            {
                wRepRadio.setSelection(true);
            }
        }
        
        setFlags();
        
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
    
    private void setFlags()
    {
        if (repository==null)
        {
            wRepRadio.setEnabled(false);
            wbTrans.setEnabled(false);
            wTransName.setEnabled(false);
            wTransDir.setEnabled(false);
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
		if (Const.isEmpty(wStepname.getText())) return;

        try
        {
            stepname = wStepname.getText(); // return value

            loadTransformation();
            
            input.setFileName(wFilename.getText());
            input.setTransName(wTransName.getText());
            input.setDirectoryPath(wTransDir.getText());

            int nrInput = wInputFields.nrNonEmpty();
            int nrOutput = wOutputFields.nrNonEmpty();

            input.allocate(nrInput, nrOutput);

            for (int i = 0; i < nrInput; i++)
            {
                TableItem item = wInputFields.getNonEmpty(i);
                input.getInputField()[i] = item.getText(1);
                input.getInputMapping()[i] = item.getText(2);
            }

            for (int i = 0; i < nrOutput; i++)
            {
                TableItem item = wOutputFields.getNonEmpty(i);
                input.getOutputMapping()[i] = item.getText(1);
                input.getOutputField()[i] = item.getText(2);
            }
            
            dispose();
        }
        catch (KettleException e)
        {
            new ErrorDialog(shell, Messages.getString("MappingDialog.ErrorLoadingSpecifiedTransformation.Title"), 
                    Messages.getString("MappingDialog.ErrorLoadingSpecifiedTransformation.Message"), e);
        }
    }
}
