 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

 
/*
 * @author Biswapesh
 * @since 24-nov-2006
 */

package org.pentaho.di.ui.trans.steps.multimerge;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.errorhandling.Stream;
import org.pentaho.di.trans.step.errorhandling.StreamIcon;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface.StreamType;
import org.pentaho.di.trans.steps.multimerge.MultiMergeJoinMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


public class MultiMergeJoinDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = MultiMergeJoinMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static final String STRING_SORT_WARNING_PARAMETER = "MergeJoinSortWarning"; //$NON-NLS-1$
    
    CCombo[]  wInputStepArray;
    CCombo joinTypeCombo;
    Text[] keyValTextBox;
    
    private static final int margin = Const.MARGIN;

	private MultiMergeJoinMeta joinMeta;
	
	private String[] inputSteps;
	
	public MultiMergeJoinDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		joinMeta=(MultiMergeJoinMeta)in;
		wInputStepArray = new CCombo[tr.getPrevStepNames(stepname).length];
		keyValTextBox = new Text[wInputStepArray.length];
     }

	/*
	 * (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepDialogInterface#open()
	 */
	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX );
 		props.setLook(shell);
        setShellImage(shell, joinMeta);
		
		final ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				joinMeta.setChanged();
			}
		};
		backupChanged = joinMeta.hasChanged();
			
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "MultiMergeJoinDialog.Shell.Label")); //$NON-NLS-1$
		
		//int middle = props.getMiddlePct();
		
		wlStepname=new Label(shell, SWT.LEFT);
		wlStepname.setText(BaseMessages.getString(PKG, "MultiMergeJoinDialog.Stepname.Label")); //$NON-NLS-1$
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(15, -margin);
		fdlStepname.top  = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname=new Text(shell, SWT.READ_ONLY | SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
 		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(15, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(35, 0);
		wStepname.setLayoutData(fdStepname);
		
		// create  widgets for input stream and join key selections
		createInputStreamWidgets(lsMod);	

		// create widgets for Join type
		createJoinTypeWidget(lsMod);      

		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, joinTypeCombo);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		
		/*lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );*/
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );


		// Set the shell size, based upon previous time...
		setSize();
		 
		// get the data
		getData();
		joinMeta.setChanged(backupChanged);
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}

	/**
	 * Create widgets for join type selection
	 * @param lsMod
	 */
	private void createJoinTypeWidget(final ModifyListener lsMod) {
		Label joinTypeLabel=new Label(shell, SWT.LEFT);
		joinTypeLabel.setText(BaseMessages.getString(PKG, "MultiMergeJoinDialog.Type.Label")); //$NON-NLS-1$
 		props.setLook(joinTypeLabel);
 		FormData fdlType=new FormData();
		fdlType.left = new FormAttachment(0, 0);
		fdlType.right= new FormAttachment(15, -margin);
		if(wInputStepArray.length > 0)
		{
			fdlType.top  = new FormAttachment(wInputStepArray[wInputStepArray.length-1], margin);
		}
		else
		{
		fdlType.top  = new FormAttachment(wStepname, margin);
		}
		joinTypeLabel.setLayoutData(fdlType);
		joinTypeCombo=new CCombo(shell, SWT.BORDER );
 		props.setLook(joinTypeCombo);

        joinTypeCombo.setItems(MultiMergeJoinMeta.join_types);
        
		joinTypeCombo.addModifyListener(lsMod);
		FormData fdType=new FormData();
		if(wInputStepArray.length >0)
		{
			 fdType.top  = new FormAttachment(wInputStepArray[wInputStepArray.length-1], margin);
		}
		else
		{
        fdType.top  = new FormAttachment(wStepname, margin);
		}
		fdType.left = new FormAttachment(15, 0);
		fdType.right= new FormAttachment(35, 0);
		joinTypeCombo.setLayoutData(fdType);
	}

	/**
	 * create widgets for input stream and join keys
	 * @param lsMod
	 */
	private void createInputStreamWidgets(final ModifyListener lsMod) {
		// Get the previous steps ...
		inputSteps = transMeta.getPrevStepNames(stepname);
		
		for(int index=0 ; index<inputSteps.length;index++)
		{
			Label   wlStep;
		   	FormData     fdlStep, fdStep1;
			
			wlStep=new Label(shell, SWT.LEFT);
			//wlStep1.setText(+i+".Label")); //$NON-NLS-1$
			wlStep.setText(BaseMessages.getString(PKG, "MultiMergeJoinMeta.InputStep")+(index+1));
			props.setLook(wlStep);
			fdlStep=new FormData();
			fdlStep.left = new FormAttachment(0, 0);
			fdlStep.right= new FormAttachment(15, -margin);
			if(index==0)
			{
			fdlStep.top  = new FormAttachment(wStepname, margin);	
			}else
			{
			fdlStep.top  = new FormAttachment(wInputStepArray[index-1], margin);		
			}
			
			wlStep.setLayoutData(fdlStep);
			wInputStepArray[index]=new CCombo(shell, SWT.BORDER );
	 		props.setLook(wInputStepArray[index]);

			if (inputSteps!=null)
			{
				wInputStepArray[index].setItems( inputSteps );
			}
			
			wInputStepArray[index].addModifyListener(lsMod);
			fdStep1=new FormData();
			fdStep1.left = new FormAttachment(15, 0);
			if(index==0)
			{
			fdStep1.top  = new FormAttachment(wStepname, margin);
			}
			else
			{
				fdStep1.top  = new FormAttachment(wInputStepArray[index-1], margin);	
			}
			
			fdStep1.right= new FormAttachment(35, 0);
			wInputStepArray[index].setLayoutData(fdStep1);
			
			Label keyLabel =new Label(shell, SWT.LEFT);
			keyLabel.setText(BaseMessages.getString(PKG, "MultiMergeJoinMeta.JoinKeys"));
			props.setLook(keyLabel);
			FormData keyStep=new FormData();
			keyStep.left = new FormAttachment(35, margin+5);
			keyStep.right= new FormAttachment(45, -margin);
			if(index==0)
			{
				keyStep.top  = new FormAttachment(wStepname, margin);	
			}else
			{
				keyStep.top  = new FormAttachment(wInputStepArray[index-1], margin);		
			}			
			keyLabel.setLayoutData(keyStep);
			
			keyValTextBox[index] = new Text(shell, SWT.READ_ONLY | SWT.SINGLE | SWT.LEFT | SWT.BORDER);
			props.setLook(keyValTextBox[index]);
			keyValTextBox[index].setText("");
			keyValTextBox[index].addModifyListener(lsMod);
			FormData keyData = new FormData();
			keyData.left = new FormAttachment(45, margin+5);
			keyData.right= new FormAttachment(65, -margin);
			if(index==0)
			{
				keyData.top  = new FormAttachment(wStepname, margin);	
			}else
			{
				keyData.top  = new FormAttachment(wInputStepArray[index-1], margin);		
			}	
			keyValTextBox[index].setLayoutData(keyData);			
			
			Button button = new Button(shell, SWT.PUSH);
			button.setText(BaseMessages.getString(PKG, "MultiMergeJoinMeta.SelectKeys"));
			//add listener
			button.addListener(SWT.Selection, new ConfigureKeyButtonListener(this, keyValTextBox[index],index,lsMod));			
			FormData buttonData = new FormData();
			buttonData.left = new FormAttachment(65, margin);
			buttonData.right= new FormAttachment(80, -margin);
			if(index==0)
			{
				buttonData.top  = new FormAttachment(wStepname, margin);	
			}else
			{
				buttonData.top  = new FormAttachment(wInputStepArray[index-1], margin);		
			}	
			button.setLayoutData(buttonData);
		}
	}
	
	/**
	 * "Configure join key" shell
	 * @param keyValTextBox
	 * @param lsMod
	 */
	private void configureKeys(final Text keyValTextBox,final int inputStreamIndex, ModifyListener lsMod)
	{
 		final Shell subShell = new Shell(shell, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX );
        final FormLayout formLayout = new FormLayout();		
		formLayout.marginWidth  = 5;
		formLayout.marginHeight = 5;
		subShell.setLayout(formLayout);
		subShell.setSize(200, 150);	
        subShell.setText(BaseMessages.getString(PKG, "MultiMergeJoinMeta.JoinKeys"));   
        
        Label wlKeys = new Label(subShell, SWT.NONE);
        wlKeys.setText(BaseMessages.getString(PKG, "MultiMergeJoinDialog.Keys")); 
        FormData fdlKeys = new FormData();
        fdlKeys.left  = new FormAttachment(0, 0);
        fdlKeys.right   = new FormAttachment(50, -margin);
        fdlKeys.top   = new FormAttachment(0, margin);
        wlKeys.setLayoutData(fdlKeys);	
        
        String[] keys = keyValTextBox.getText().split(",");
        int nrKeyRows = (keys!=null?keys.length:1);
        
        ColumnInfo[] ciKeys=new ColumnInfo[] {
            new ColumnInfo(BaseMessages.getString(PKG, "MultiMergeJoinDialog.ColumnInfo.KeyField"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
        };
            
        final TableView wKeys=new TableView(transMeta, subShell, 
                              SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, 
                              ciKeys, 
                              nrKeyRows,  
                              lsMod,
                              props
                              );

        FormData fdKeys = new FormData();
        fdKeys.top    = new FormAttachment(wlKeys, margin);
        fdKeys.left   = new FormAttachment(0,   0);
        fdKeys.bottom = new FormAttachment(100, -70);
        fdKeys.right  = new FormAttachment(50, -margin);
        wKeys.setLayoutData(fdKeys);

        Button getKeyButton=new Button(subShell, SWT.PUSH);
        getKeyButton.setText(BaseMessages.getString(PKG, "MultiMergeJoinDialog.KeyFields.Button")); //$NON-NLS-1$
        FormData fdbKeys = new FormData();
        fdbKeys.top   = new FormAttachment(wKeys, margin);
        fdbKeys.left  = new FormAttachment(0, 0);
        fdbKeys.right = new FormAttachment(50, -margin);
        getKeyButton.setLayoutData(fdbKeys);
        getKeyButton.addSelectionListener(new SelectionAdapter()
            {
            
                public void widgetSelected(SelectionEvent e)
                {
                    getKeys(wKeys,inputStreamIndex);
                }
            }
        );
     // Some buttons
        Button okButton=new Button(subShell, SWT.PUSH);
        okButton.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		
		setButtonPositions(new Button[] {okButton}, margin, getKeyButton);

		okButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
            	int nrKeys   = wKeys.nrNonEmpty();
    			StringBuilder sb = new StringBuilder();
                for (int i=0;i<nrKeys;i++)
    	        {
    	            TableItem item = wKeys.getNonEmpty(i);
    	            sb.append(item.getText(1));
    	            if(nrKeys >1 && i != nrKeys-1)
    	            {
    	            	 sb.append(",");
    	            }
    	        }
    			keyValTextBox.setText(sb.toString());
    			subShell.close();
            }
        });

		
		/*SelectionAdapter lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );*/
		
		// Detect X or ALT-F4 or something that kills this window...
		subShell.addShellListener(	new ShellAdapter() 
		{
			public void shellClosed(ShellEvent e) { 
				
		} } );
		
		 for (int i=0;i<keys.length;i++)
	        {
	          TableItem item = wKeys.table.getItem(i);
	          if (keys[i]!=null) item.setText(1, keys[i]);
	        }
	        
		subShell.pack();
        subShell.open();
		
	}
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{	
		 List<StreamInterface> infoStreams = joinMeta.getStepIOMeta().getInfoStreams();

		 for(int i=0 ; i<infoStreams.size();i++)
		 {
			wInputStepArray[i].setText(joinMeta.getInputSteps()[i]); 
		 }
		String joinType = joinMeta.getJoinType();
		if (joinType != null && joinType.length() > 0)
			joinTypeCombo.setText(joinType);
		else
			joinTypeCombo.setText(MultiMergeJoinMeta.join_types[0]);
        
		String[] keyFields = joinMeta.getKeyFields();
        for (int i=0;i<keyFields.length;i++)
        {
        	keyValTextBox[i].setText(keyFields[i]);
        }        
        wStepname.selectAll();
	}
	
	private void cancel()
	{
		stepname=null;
		joinMeta.setChanged(backupChanged);
		dispose();
	}
    
	/**
	 * Get the meta data
	 * @param meta
	 */
    private void getMeta(MultiMergeJoinMeta meta)
    {      
    	List<StreamInterface> infoStreams = meta.getStepIOMeta().getInfoStreams();

    	if (infoStreams.size()  == 0 ) {
    		if (inputSteps != null && inputSteps.length != 0) {
    		  for (int i = 0; i < inputSteps.length; i++)
    			    meta.getStepIOMeta().addStream(new Stream(StreamType.INFO, null,
    					    BaseMessages.getString(PKG, "MultiMergeJoin.InfoStream.Description"), 
    					    StreamIcon.INFO, null));
    			infoStreams = meta.getStepIOMeta().getInfoStreams();
    		}
    	}
    	else if(inputSteps != null && infoStreams.size() < inputSteps.length )
    	{
    		int requiredStreams =  inputSteps.length - infoStreams.size() ;
    		
    		for (int i = 0; i < requiredStreams; i++)
			    meta.getStepIOMeta().addStream(new Stream(StreamType.INFO, null,
					    BaseMessages.getString(PKG, "MultiMergeJoin.InfoStream.Description"), 
					    StreamIcon.INFO, null));
			infoStreams = meta.getStepIOMeta().getInfoStreams();
    	}
    	int streamCount = infoStreams.size();
    	meta.allocateInputSteps(streamCount);    
    	meta.allocateKeys(streamCount);
    	
    	for (int i = 0; i < streamCount; i++)
    	{
    		meta.getInputSteps()[i] = wInputStepArray[i].getText();
    		infoStreams.get(i).setStepMeta( transMeta.findStep( wInputStepArray[i].getText()) );
    		meta.getKeyFields()[i] = keyValTextBox[i].getText();
    	}
        meta.setJoinType(joinTypeCombo.getText());
    }

	
	private void ok()
	{		
		if (Const.isEmpty(wStepname.getText())) return;
        getMeta(joinMeta);
        // Show a warning (optional)
        if ( "Y".equalsIgnoreCase( props.getCustomParameter(STRING_SORT_WARNING_PARAMETER, "Y") )) //$NON-NLS-1$ //$NON-NLS-2$
        {
            MessageDialogWithToggle md = new MessageDialogWithToggle(shell, 
                 BaseMessages.getString(PKG, "MultiMergeJoinDialog.InputNeedSort.DialogTitle"),  //$NON-NLS-1$
                 null,
                 BaseMessages.getString(PKG, "MultiMergeJoinDialog.InputNeedSort.DialogMessage", Const.CR )+Const.CR, //$NON-NLS-1$ //$NON-NLS-2$
                 MessageDialog.WARNING,
                 new String[] { BaseMessages.getString(PKG, "MultiMergeJoinDialog.InputNeedSort.Option1") }, //$NON-NLS-1$
                 0,
                 BaseMessages.getString(PKG, "MultiMergeJoinDialog.InputNeedSort.Option2"), //$NON-NLS-1$
                 "N".equalsIgnoreCase( props.getCustomParameter(STRING_SORT_WARNING_PARAMETER, "Y") ) //$NON-NLS-1$ //$NON-NLS-2$
            );
            MessageDialogWithToggle.setDefaultImage(GUIResource.getInstance().getImageSpoon());
            md.open();
            props.setCustomParameter(STRING_SORT_WARNING_PARAMETER, md.getToggleState()?"N":"Y"); //$NON-NLS-1$ //$NON-NLS-2$
            props.saveProps();
        }
        stepname = wStepname.getText(); // return value
		dispose();
	}
    
	/**
	 * Get the input keys 
	 * @param wKeys
	 */
    private void getKeys(TableView wKeys,int inputStreamIndex)
    {
        MultiMergeJoinMeta joinMeta = new MultiMergeJoinMeta();
        getMeta(joinMeta);
        try
        {
            List<StreamInterface> infoStreams = joinMeta.getStepIOMeta().getInfoStreams();

            StepMeta stepMeta = infoStreams.get(inputStreamIndex).getStepMeta();
            if (stepMeta!=null)
            {
                RowMetaInterface prev = transMeta.getStepFields(stepMeta);
                if (prev!=null)
                {
                    BaseStepDialog.getFieldsFromPrevious(prev, wKeys, 1, new int[] { 1 }, new int[] {}, -1, -1, null);
                }
            }
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "MultiMergeJoinDialog.ErrorGettingFields.DialogTitle"), BaseMessages.getString(PKG, "MultiMergeJoinDialog.ErrorGettingFields.DialogMessage"), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    /**
     * Listener for Configure Keys button
     * @author A71481
     *
     */
    private static class ConfigureKeyButtonListener implements  Listener
    {
    	MultiMergeJoinDialog dialog;
		Text textBox;
		int inputStreamIndex;
		ModifyListener listener; 
		
    	public ConfigureKeyButtonListener(MultiMergeJoinDialog dialog,Text textBox, int streamIndex, ModifyListener lsMod)
    	{
    		this.dialog = dialog;
    		this.textBox =textBox;
    		this.listener=lsMod;
    		this.inputStreamIndex=streamIndex;
    	}
		@Override
		public void handleEvent(Event event) 
		{		
			dialog.configureKeys(textBox,inputStreamIndex, listener);
		}
    	
    }
    
}
