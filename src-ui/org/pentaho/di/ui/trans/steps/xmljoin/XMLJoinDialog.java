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
 * Created on 18-mei-2003
 *
 */

package org.pentaho.di.ui.trans.steps.xmljoin;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.xmljoin.Messages;
import org.pentaho.di.trans.steps.xmljoin.XMLJoinMeta;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class XMLJoinDialog extends BaseStepDialog implements StepDialogInterface
{
   
    private Label        wlComplexJoin;
    private Button       wComplexJoin;
    private FormData     fdlComplexJoin, fdComplexJoin;
    
    private Label        wlTargetXMLstep;
    private CCombo       wTargetXMLstep;
    private FormData     fdlTargetXMLstep, fdTargetXMLstep;
    
    private Label        wlTargetXMLfield;
	private TextVar      wTargetXMLfield;
	private FormData     fdlTargetXMLfield, fdTargetXMLfield;
	
    private Label        wlSourceXMLstep;
    private CCombo       wSourceXMLstep;
    private FormData     fdlSourceXMLstep, fdSourceXMLstep;
    
    private Label        wlSourceXMLfield;
	private TextVar      wSourceXMLfield;
	private FormData     fdlSourceXMLfield, fdSourceXMLfield;

    private Label        wlValueXMLfield;
	private TextVar      wValueXMLfield;
	private FormData     fdlValueXMLfield, fdValueXMLfield;
    
    private Label        wlJoinCompareField;
	private TextVar      wJoinCompareField;
	private FormData     fdlJoinCompareField, fdJoinCompareField;

    private Label        wlTargetXPath;
	private TextVar      wTargetXPath;
	private FormData     fdlTargetXPath, fdTargetXPath;
	
	private Label        wlEncoding;
	private CCombo       wEncoding;
	private FormData     fdlEncoding, fdEncoding;
	
	private Label        wlOmitXMLHeader;
    private Button       wOmitXMLHeader;
    private FormData     fdlOmitXMLHeader, fdOmitXMLHeader;

    private XMLJoinMeta   input;
    
    private Group        gJoin, gTarget, gSource, gResult;
    private FormData     fdJoin, fdTarget, fdSource, fdResult;
    
    // private Button       wMinWidth;
    // private Listener     lsMinWidth;
    
    private boolean      gotEncodings = false; 
    
    public XMLJoinDialog(Shell parent, Object in, TransMeta transMeta, String sname)
    {
        super(parent, (BaseStepMeta)in, transMeta, sname);
        input=(XMLJoinMeta)in;
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
        shell.setText(Messages.getString("XMLJoin.DialogTitle"));
        
        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Step name line
        wlStepname=new Label(shell, SWT.RIGHT);
        wlStepname.setText(Messages.getString("System.Label.StepName"));
        props.setLook(wlStepname);
        fdlStepname=new FormData();
        fdlStepname.left  = new FormAttachment(0, 0);
        fdlStepname.top   = new FormAttachment(0, margin);
        fdlStepname.right = new FormAttachment(middle, -margin);
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

        //Target Group 
        gTarget = new Group(shell, SWT.NONE);
        gTarget.setText(Messages.getString("XMLJoin.TargetGroup.Label"));
        FormLayout targetLayout = new FormLayout();
        targetLayout.marginHeight = margin;
        targetLayout.marginWidth = margin;
        gTarget.setLayout(targetLayout);
        props.setLook(gTarget);
        fdTarget = new FormData();
        fdTarget.left  = new FormAttachment(0,0);
        fdTarget.right = new FormAttachment(100,0);
        fdTarget.top   = new FormAttachment(wStepname, 2*margin);
        gTarget.setLayoutData(fdTarget);
        // Target XML step line
        wlTargetXMLstep=new Label(gTarget, SWT.RIGHT);
        wlTargetXMLstep.setText(Messages.getString("XMLJoin.TargetXMLStep.Label"));
        props.setLook(wlTargetXMLstep);
        fdlTargetXMLstep=new FormData();
        fdlTargetXMLstep.left = new FormAttachment(0, 0);
        fdlTargetXMLstep.top  = new FormAttachment(wStepname, margin);
        fdlTargetXMLstep.right= new FormAttachment(middle, -margin);
        wlTargetXMLstep.setLayoutData(fdlTargetXMLstep);
        wTargetXMLstep=new CCombo(gTarget, SWT.BORDER | SWT.READ_ONLY);
        wTargetXMLstep.setEditable(true);
        props.setLook(wTargetXMLstep);
        wTargetXMLstep.addModifyListener(lsMod);
        fdTargetXMLstep=new FormData();
        fdTargetXMLstep.left = new FormAttachment(middle, 0);
        fdTargetXMLstep.top  = new FormAttachment(wStepname, margin);
        fdTargetXMLstep.right= new FormAttachment(100, 0);
        wTargetXMLstep.setLayoutData(fdTargetXMLstep);

        // Target XML Field line
		wlTargetXMLfield=new Label(gTarget, SWT.RIGHT);
		wlTargetXMLfield.setText(Messages.getString("XMLJoin.TargetXMLField.Label")); //$NON-NLS-1$
 		props.setLook(wlTargetXMLfield);
		fdlTargetXMLfield=new FormData();
		fdlTargetXMLfield.left = new FormAttachment(0, 0);
		fdlTargetXMLfield.right= new FormAttachment(middle, -margin);
		fdlTargetXMLfield.top  = new FormAttachment(wTargetXMLstep, margin);
		wlTargetXMLfield.setLayoutData(fdlTargetXMLfield);

    	wTargetXMLfield=new TextVar(transMeta, gTarget, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wTargetXMLfield);
		wTargetXMLfield.addModifyListener(lsMod);
		fdTargetXMLfield=new FormData();
		fdTargetXMLfield.left = new FormAttachment(middle, 0);
		fdTargetXMLfield.top  = new FormAttachment(wTargetXMLstep, margin);
		fdTargetXMLfield.right= new FormAttachment(100, -margin);
		wTargetXMLfield.setLayoutData(fdTargetXMLfield);
		
		//Source Group 
        gSource = new Group(shell, SWT.NONE);
        gSource.setText(Messages.getString("XMLJoin.SourceGroup.Label"));
        FormLayout SourceLayout = new FormLayout();
        SourceLayout.marginHeight = margin;
        SourceLayout.marginWidth = margin;
        gSource.setLayout(SourceLayout);
        props.setLook(gSource);
        fdSource = new FormData();
        fdSource.left  = new FormAttachment(0,0);
        fdSource.right = new FormAttachment(100,0);
        fdSource.top   = new FormAttachment(gTarget, 2*margin);
        gSource.setLayoutData(fdSource);
		// Source XML step line
        wlSourceXMLstep=new Label(gSource, SWT.RIGHT);
        wlSourceXMLstep.setText(Messages.getString("XMLJoin.SourceXMLStep.Label"));
        props.setLook(wlSourceXMLstep);
        fdlSourceXMLstep=new FormData();
        fdlSourceXMLstep.left = new FormAttachment(0, 0);
        fdlSourceXMLstep.top  = new FormAttachment(wTargetXMLfield, margin);
        fdlSourceXMLstep.right= new FormAttachment(middle, -margin);
        wlSourceXMLstep.setLayoutData(fdlSourceXMLstep);
        wSourceXMLstep=new CCombo(gSource, SWT.BORDER | SWT.READ_ONLY);
        wSourceXMLstep.setEditable(true);
        props.setLook(wSourceXMLstep);
        wSourceXMLstep.addModifyListener(lsMod);
        fdSourceXMLstep=new FormData();
        fdSourceXMLstep.left = new FormAttachment(middle, 0);
        fdSourceXMLstep.top  = new FormAttachment(wTargetXMLfield, margin);
        fdSourceXMLstep.right= new FormAttachment(100, 0);
        wSourceXMLstep.setLayoutData(fdSourceXMLstep);
		
		// Source XML Field line
		wlSourceXMLfield=new Label(gSource, SWT.RIGHT);
		wlSourceXMLfield.setText(Messages.getString("XMLJoin.SourceXMLField.Label")); //$NON-NLS-1$
 		props.setLook(wlSourceXMLfield);
		fdlSourceXMLfield=new FormData();
		fdlSourceXMLfield.left = new FormAttachment(0, 0);
		fdlSourceXMLfield.right= new FormAttachment(middle, -margin);
		fdlSourceXMLfield.top  = new FormAttachment(wSourceXMLstep, margin);
		wlSourceXMLfield.setLayoutData(fdlSourceXMLfield);

    	wSourceXMLfield=new TextVar(transMeta, gSource, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wSourceXMLfield);
		wSourceXMLfield.addModifyListener(lsMod);
		fdSourceXMLfield=new FormData();
		fdSourceXMLfield.left = new FormAttachment(middle, 0);
		fdSourceXMLfield.top  = new FormAttachment(wSourceXMLstep, margin);
		fdSourceXMLfield.right= new FormAttachment(100, -margin);
		wSourceXMLfield.setLayoutData(fdSourceXMLfield);
		
		//Join Group 
        gJoin = new Group(shell, SWT.NONE);
        gJoin.setText(Messages.getString("XMLJoin.JoinGroup.Label"));
        FormLayout JoinLayout = new FormLayout();
        JoinLayout.marginHeight = margin;
        JoinLayout.marginWidth = margin;
        gJoin.setLayout(JoinLayout);
        props.setLook(gJoin);
        fdJoin = new FormData();
        fdJoin.left  = new FormAttachment(0,0);
        fdJoin.right = new FormAttachment(100,0);
        fdJoin.top   = new FormAttachment(gSource, 2*margin);
        gJoin.setLayoutData(fdJoin);

		// Target XPath line
		wlTargetXPath=new Label(gJoin, SWT.RIGHT);
		wlTargetXPath.setText(Messages.getString("XMLJoin.TargetXPath.Label")); //$NON-NLS-1$
 		props.setLook(wlTargetXPath);
		fdlTargetXPath=new FormData();
		fdlTargetXPath.left = new FormAttachment(0, 0);
		fdlTargetXPath.right= new FormAttachment(middle, -margin);
		fdlTargetXPath.top  = new FormAttachment(wSourceXMLfield, margin);
		wlTargetXPath.setLayoutData(fdlTargetXPath);

    	wTargetXPath=new TextVar(transMeta, gJoin, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wTargetXPath);
		wTargetXPath.addModifyListener(lsMod);
		fdTargetXPath=new FormData();
		fdTargetXPath.left = new FormAttachment(middle, 0);
		fdTargetXPath.top  = new FormAttachment(wSourceXMLfield, margin);
		fdTargetXPath.right= new FormAttachment(100, -margin);
		wTargetXPath.setLayoutData(fdTargetXPath);
		
		// Complex Join Line
        wlComplexJoin=new Label(gJoin, SWT.RIGHT);
        wlComplexJoin.setText(Messages.getString("XMLJoin.ComplexJoin.Label"));
        props.setLook(wlComplexJoin);
        fdlComplexJoin=new FormData();
        fdlComplexJoin.left = new FormAttachment(0, 0);
        fdlComplexJoin.top  = new FormAttachment(wTargetXPath, margin);
        fdlComplexJoin.right= new FormAttachment(middle, -margin);
        wlComplexJoin.setLayoutData(fdlComplexJoin);
        wComplexJoin=new Button(gJoin, SWT.CHECK );
        props.setLook(wComplexJoin);
        fdComplexJoin=new FormData();
        fdComplexJoin.left = new FormAttachment(middle, 0);
        fdComplexJoin.top  = new FormAttachment(wTargetXPath, margin);
        fdComplexJoin.right= new FormAttachment(100, 0);
        wComplexJoin.setLayoutData(fdComplexJoin);
        wComplexJoin.addSelectionListener(new SelectionAdapter() 
            {
                public void widgetSelected(SelectionEvent e) 
                {
                	input.setChanged();
                	
                	if(wComplexJoin.getSelection()){
                		wJoinCompareField.setEnabled(true);
                	}
                	else{
                		wJoinCompareField.setEnabled(false);
                	}              
                }
            }
        );

		// Join Compare field line
		wlJoinCompareField=new Label(gJoin, SWT.RIGHT);
		wlJoinCompareField.setText(Messages.getString("XMLJoin.JoinCompareFiled.Label")); //$NON-NLS-1$
 		props.setLook(wlJoinCompareField);
		fdlJoinCompareField=new FormData();
		fdlJoinCompareField.left = new FormAttachment(0, 0);
		fdlJoinCompareField.right= new FormAttachment(middle, -margin);
		fdlJoinCompareField.top  = new FormAttachment(wComplexJoin, margin);
		wlJoinCompareField.setLayoutData(fdlJoinCompareField);

    	wJoinCompareField=new TextVar(transMeta, gJoin, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wJoinCompareField);
		wJoinCompareField.addModifyListener(lsMod);
		fdJoinCompareField=new FormData();
		fdJoinCompareField.left = new FormAttachment(middle, 0);
		fdJoinCompareField.top  = new FormAttachment(wComplexJoin, margin);
		fdJoinCompareField.right= new FormAttachment(100, -margin);
		wJoinCompareField.setLayoutData(fdJoinCompareField);
		wJoinCompareField.setEnabled(false);
		
		//Result Group 
        gResult = new Group(shell, SWT.NONE);
        gResult.setText(Messages.getString("XMLJoin.ResultGroup.Label"));
        FormLayout ResultLayout = new FormLayout();
        ResultLayout.marginHeight = margin;
        ResultLayout.marginWidth = margin;
        gResult.setLayout(ResultLayout);
        props.setLook(gResult);
        fdResult = new FormData();
        fdResult.left  = new FormAttachment(0,0);
        fdResult.right = new FormAttachment(100,0);
        fdResult.top   = new FormAttachment(gJoin, 2*margin);
        gResult.setLayoutData(fdResult);
		// Value XML Field line
		wlValueXMLfield=new Label(gResult, SWT.RIGHT);
		wlValueXMLfield.setText(Messages.getString("XMLJoin.ValueXMLField.Label")); //$NON-NLS-1$
 		props.setLook(wlValueXMLfield);
		fdlValueXMLfield=new FormData();
		fdlValueXMLfield.left = new FormAttachment(0, 0);
		fdlValueXMLfield.right= new FormAttachment(middle, -margin);
		fdlValueXMLfield.top  = new FormAttachment(wJoinCompareField, margin);
		wlValueXMLfield.setLayoutData(fdlValueXMLfield);

    	wValueXMLfield=new TextVar(transMeta, gResult, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wValueXMLfield);
		wValueXMLfield.addModifyListener(lsMod);
		fdValueXMLfield=new FormData();
		fdValueXMLfield.left = new FormAttachment(middle, 0);
		fdValueXMLfield.top  = new FormAttachment(wJoinCompareField, margin);
		fdValueXMLfield.right= new FormAttachment(100, -margin);
		wValueXMLfield.setLayoutData(fdValueXMLfield);
		
		//Encoding Line
		 wlEncoding=new Label(gResult, SWT.RIGHT);
	     wlEncoding.setText(Messages.getString("XMLJoin.Encoding.Label"));
	     props.setLook(wlEncoding);
	     fdlEncoding=new FormData();
	     fdlEncoding.left = new FormAttachment(0, 0);
	     fdlEncoding.top  = new FormAttachment(wValueXMLfield, margin);
	     fdlEncoding.right= new FormAttachment(middle, -margin);
	     wlEncoding.setLayoutData(fdlEncoding);
	     wEncoding=new CCombo(gResult, SWT.BORDER | SWT.READ_ONLY);
	     wEncoding.setEditable(true);
	     props.setLook(wEncoding);
	     wEncoding.addModifyListener(lsMod);
	     fdEncoding=new FormData();
	     fdEncoding.left = new FormAttachment(middle, 0);
	     fdEncoding.top  = new FormAttachment(wValueXMLfield, margin);
	     fdEncoding.right= new FormAttachment(100, 0);
	     wEncoding.setLayoutData(fdEncoding);
	     wEncoding.addFocusListener(new FocusListener()
	            {
	                public void focusLost(org.eclipse.swt.events.FocusEvent e)
	                {
	                }
	            
	                public void focusGained(org.eclipse.swt.events.FocusEvent e)
	                {
	                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
	                    shell.setCursor(busy);
	                    setEncodings();
	                    shell.setCursor(null);
	                    busy.dispose();
	                }
	            }
	        );
		
		// Complex Join Line
        wlOmitXMLHeader=new Label(gResult, SWT.RIGHT);
        wlOmitXMLHeader.setText(Messages.getString("XMLJoin.OmitXMLHeader.Label"));
        props.setLook(wlOmitXMLHeader);
        fdlOmitXMLHeader=new FormData();
        fdlOmitXMLHeader.left = new FormAttachment(0, 0);
        fdlOmitXMLHeader.top  = new FormAttachment(wEncoding, margin);
        fdlOmitXMLHeader.right= new FormAttachment(middle, -margin);
        wlOmitXMLHeader.setLayoutData(fdlOmitXMLHeader);
        wOmitXMLHeader=new Button(gResult, SWT.CHECK );
        props.setLook(wOmitXMLHeader);
        fdOmitXMLHeader=new FormData();
        fdOmitXMLHeader.left = new FormAttachment(middle, 0);
        fdOmitXMLHeader.top  = new FormAttachment(wEncoding, margin);
        fdOmitXMLHeader.right= new FormAttachment(100, 0);
        wOmitXMLHeader.setLayoutData(fdOmitXMLHeader);
        
        
        shell.layout();        
        
        wOK=new Button(shell, SWT.PUSH);
        wOK.setText(Messages.getString("System.Button.OK"));
        
        wCancel=new Button(shell, SWT.PUSH);
        wCancel.setText(Messages.getString("System.Button.Cancel"));

        setButtonPositions(new Button[] { wOK, wCancel }, margin, gResult);

        // Add listeners
        lsOK       = new Listener() { public void handleEvent(Event e) { ok();       } };
        // lsMinWidth    = new Listener() { public void handleEvent(Event e) { setMinimalWidth(); } };
        lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();   } };
        
        wOK.addListener    (SWT.Selection, lsOK    );
       // wGet.addListener   (SWT.Selection, lsGet   );
        //wMinWidth.addListener (SWT.Selection, lsMinWidth );
        wCancel.addListener(SWT.Selection, lsCancel);
        
        lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
        
        wStepname.addSelectionListener( lsDef );
        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener( new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

        lsResize = new Listener() 
        {
            public void handleEvent(Event event) 
            {
            	// TODO - implement if necessary
            }
        };
        shell.addListener(SWT.Resize, lsResize);

        
        // Set the shell size, based upon previous time...
        setSize();
        
        getData();
        input.setChanged(changed);
        
        for (int i=0;i<transMeta.findNrPrevSteps(stepname, true);i++)
		{
			StepMeta stepMeta = transMeta.findPrevStep(stepname, i, true);
			wTargetXMLstep.add(stepMeta.getName());
			wSourceXMLstep.add(stepMeta.getName());
		}
        
        shell.open();
        while (!shell.isDisposed())
        {
                if (!display.readAndDispatch()) display.sleep();
        }
        return stepname;
    }
    
    public void setMinimalWidth(){
    	// TODO - implement when necessary
    }
   
    /**
     * Copy information from the meta-data input to the dialog fields.
     */ 
    public void getData()
    {
        if (input.getTargetXMLstep()     != null) wTargetXMLstep.setText(input.getTargetXMLstep());
        if (input.getTargetXMLfield()     != null) wTargetXMLfield.setText(input.getTargetXMLfield());
        if (input.getSourceXMLstep()     != null) wSourceXMLstep.setText(input.getSourceXMLstep());
        if (input.getSourceXMLfield()     != null) wSourceXMLfield.setText(input.getSourceXMLfield());
        if (input.getValueXMLfield()     != null) wValueXMLfield.setText(input.getValueXMLfield());
        if (input.getTargetXPath()     != null) wTargetXPath.setText(input.getTargetXPath());
        if (input.getEncoding()      != null) wEncoding.setText(input.getEncoding());
        if (input.getJoinCompareField()     != null) wJoinCompareField.setText(input.getJoinCompareField());
        
        wComplexJoin.setSelection(input.isComplexJoin());
        wOmitXMLHeader.setSelection(input.isOmitXMLHeader());
        
        if(input.isComplexJoin()){
        	wJoinCompareField.setEnabled(true);
        }
        
        log.logDebug(toString(), Messages.getString("AddXMLDialog.Log.GettingFieldsInfo"));
        
        wStepname.selectAll();
    }
    
    private void cancel()
    {
        stepname=null;
        
        input.setChanged(backupChanged);

        dispose();
    }
    
    private void getInfo(XMLJoinMeta tfoi)
    {
    	tfoi.setTargetXMLstep(wTargetXMLstep.getText());
    	tfoi.setTargetXMLfield(wTargetXMLfield.getText());
    	tfoi.setSourceXMLstep(wSourceXMLstep.getText());
    	tfoi.setSourceXMLfield(wSourceXMLfield.getText());
    	tfoi.setValueXMLfield(wValueXMLfield.getText());
    	tfoi.setTargetXPath(wTargetXPath.getText());
    	tfoi.setJoinCompareField(wJoinCompareField.getText());
        tfoi.setComplexJoin(wComplexJoin.getSelection() );   
        tfoi.setEncoding( wEncoding.getText() );
        tfoi.setOmitXMLHeader(wOmitXMLHeader.getSelection() ); 
    }
    
    private void ok()
    {
		if (Const.isEmpty(wStepname.getText())) return;
		
        stepname = wStepname.getText(); // return value
        
        getInfo(input);
        
        dispose();
    }
    
   /* private void get()
    {
        try
        {
            RowMetaInterface r = transMeta.getPrevStepFields(stepname);
            
        }
        catch(KettleException ke)
        {
            new ErrorDialog(shell, Messages.getString("System.Dialog.GetFieldsFailed.Title"), Messages.getString("System.Dialog.GetFieldsFailed.Message"), ke);
        }

    }*/
    

    public String toString()
    {
        return this.getClass().getName();
    }
    
    private void setEncodings()
    {
        // Encoding of the text file:
        if (!gotEncodings)
        {
            gotEncodings = true;
            
            wEncoding.removeAll();
            List<Charset> values = new ArrayList<Charset>(Charset.availableCharsets().values());
            for (int i=0;i<values.size();i++)
            {
                Charset charSet = (Charset)values.get(i);
                wEncoding.add( charSet.displayName() );
            }
            
            // Now select the default!
            String defEncoding = Const.getEnvironmentVariable("file.encoding", "UTF-8");
            int idx = Const.indexOfString(defEncoding, wEncoding.getItems() );
            if (idx>=0) wEncoding.select( idx );
            else 
            	wEncoding.select(Const.indexOfString("UTF-8", wEncoding.getItems() ));
        }
    }
}
