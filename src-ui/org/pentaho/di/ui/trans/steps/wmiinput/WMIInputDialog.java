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



/**
 * @author Samatar
 * @since 01-10-2011
 */

package org.pentaho.di.ui.trans.steps.wmiinput;


import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.wmiinput.WMIInputMeta;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.widget.ControlSpaceKeyAdapter;
import org.pentaho.di.ui.core.widget.StyledTextComp;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.steps.tableinput.SQLValuesHighlight;

public class WMIInputDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = WMIInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label        wlWMI;
	private StyledTextComp         wWMI;
	private FormData     fdlWMI, fdWMI;

    private Label        wlVariables;
    private Button       wVariables;
    private FormData     fdlVariables, fdVariables;
	

	private WMIInputMeta input;
	
	private Label        wlPosition;
	private FormData     fdlPosition;
	

	private Label        wlLimit;
	private TextVar         wLimit;
	private FormData     fdlLimit, fdLimit;
	
	

	private Label        wlDomain;
	private TextVar         wDomain;
	private FormData     fdlDomain, fdDomain;


	private Label        wlHost;
	private TextVar         wHost;
	private FormData     fdlHost, fdHost;

	private Label        wlUserName;
	private TextVar         wUserName;
	private FormData     fdlUserName, fdUserName;
	

	private Label        wlPassword;
	private TextVar         wPassword;
	private FormData     fdlPassword, fdPassword;
	

	public WMIInputDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(WMIInputMeta)in;
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
		shell.setText(BaseMessages.getString(PKG, "WMIInputDialog.WMIInput")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

        // Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "WMIInputDialog.StepName")); //$NON-NLS-1$
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

	
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
        wPreview=new Button(shell, SWT.PUSH);
        wPreview.setText(BaseMessages.getString(PKG, "System.Button.Preview")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wPreview, wCancel }, margin, null);

		
		// Password ...
		wlPassword=new Label(shell, SWT.RIGHT);
		wlPassword.setText(BaseMessages.getString(PKG, "WMIInputDialog.Password")); //$NON-NLS-1$
 		props.setLook(wlPassword);
		fdlPassword=new FormData();
		fdlPassword.left = new FormAttachment(0, 0);
		fdlPassword.right= new FormAttachment(middle, -margin);
		fdlPassword.bottom = new FormAttachment(wOK, -2*margin);
		wlPassword.setLayoutData(fdlPassword);
		wPassword=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wPassword);
		wPassword.addModifyListener(lsMod);
		fdPassword=new FormData();
		fdPassword.left = new FormAttachment(middle, 0);
		fdPassword.right= new FormAttachment(100, 0);
		fdPassword.bottom = new FormAttachment(wOK, -2*margin);
		wPassword.setLayoutData(fdPassword);
	    wPassword.addModifyListener(
			 new ModifyListener() 
			 {
				public void modifyText(ModifyEvent e) 
				{
					input.setChanged();
					checkPasswordVisible(wPassword);
				}
		});
	    
		// UserName ...
		wlUserName=new Label(shell, SWT.RIGHT);
		wlUserName.setText(BaseMessages.getString(PKG, "WMIInputDialog.UserName")); //$NON-NLS-1$
 		props.setLook(wlUserName);
		fdlUserName=new FormData();
		fdlUserName.left = new FormAttachment(0, 0);
		fdlUserName.right= new FormAttachment(middle, -margin);
		fdlUserName.bottom = new FormAttachment(wPassword, -margin);
		wlUserName.setLayoutData(fdlUserName);
		wUserName=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wUserName);
		wUserName.addModifyListener(lsMod);
		fdUserName=new FormData();
		fdUserName.left = new FormAttachment(middle, 0);
		fdUserName.right= new FormAttachment(100, 0);
		fdUserName.bottom = new FormAttachment(wPassword, -margin);
		wUserName.setLayoutData(fdUserName);

		// Host ...
		wlHost=new Label(shell, SWT.RIGHT);
		wlHost.setText(BaseMessages.getString(PKG, "WMIInputDialog.Host")); //$NON-NLS-1$
 		props.setLook(wlHost);
		fdlHost=new FormData();
		fdlHost.left = new FormAttachment(0, 0);
		fdlHost.right= new FormAttachment(middle, -margin);
		fdlHost.bottom = new FormAttachment(wUserName, -margin);
		wlHost.setLayoutData(fdlHost);
		wHost=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wHost);
		wHost.addModifyListener(lsMod);
		fdHost=new FormData();
		fdHost.left = new FormAttachment(middle, 0);
		fdHost.right= new FormAttachment(100, 0);
		fdHost.bottom = new FormAttachment(wUserName, -margin);
		wHost.setLayoutData(fdHost);
		
		
		// Domain input ...
		wlDomain=new Label(shell, SWT.RIGHT);
		wlDomain.setText(BaseMessages.getString(PKG, "WMIInputDialog.Domain")); //$NON-NLS-1$
 		props.setLook(wlDomain);
		fdlDomain=new FormData();
		fdlDomain.left = new FormAttachment(0, 0);
		fdlDomain.right= new FormAttachment(middle, -margin);
		fdlDomain.bottom = new FormAttachment(wHost, -margin);
		wlDomain.setLayoutData(fdlDomain);
		wDomain=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wDomain);
		wDomain.addModifyListener(lsMod);
		fdDomain=new FormData();
		fdDomain.left = new FormAttachment(middle, 0);
		fdDomain.right= new FormAttachment(100, 0);
		fdDomain.bottom = new FormAttachment(wHost, -margin);
		wDomain.setLayoutData(fdDomain);

		
	
		// Limit input ...
		wlLimit=new Label(shell, SWT.RIGHT);
		wlLimit.setText(BaseMessages.getString(PKG, "WMIInputDialog.LimitSize")); //$NON-NLS-1$
 		props.setLook(wlLimit);
		fdlLimit=new FormData();
		fdlLimit.left = new FormAttachment(0, 0);
		fdlLimit.right= new FormAttachment(middle, -margin);
		fdlLimit.bottom = new FormAttachment(wDomain, -2*margin);
		wlLimit.setLayoutData(fdlLimit);
		wLimit=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLimit);
		wLimit.addModifyListener(lsMod);
		fdLimit=new FormData();
		fdLimit.left = new FormAttachment(middle, 0);
		fdLimit.right= new FormAttachment(100, 0);
		fdLimit.bottom = new FormAttachment(wDomain, -2*margin);
		wLimit.setLayoutData(fdLimit);



        // Variable?
        wlVariables = new Label(shell, SWT.RIGHT);
        wlVariables.setText(BaseMessages.getString(PKG, "WMIInputDialog.ReplaceVariables")); //$NON-NLS-1$
        props.setLook(wlVariables);
        fdlVariables = new FormData();
        fdlVariables.left = new FormAttachment(0, 0);
        fdlVariables.right = new FormAttachment(middle, -margin);
        fdlVariables.bottom = new FormAttachment(wLimit, -margin);
        wlVariables.setLayoutData(fdlVariables);
        wVariables = new Button(shell, SWT.CHECK);
        props.setLook(wVariables);
        fdVariables = new FormData();
        fdVariables.left = new FormAttachment(middle, 0);
        fdVariables.right = new FormAttachment(100, 0);
        fdVariables.bottom = new FormAttachment(wLimit, -margin);
        wVariables.setLayoutData(fdVariables);
        wVariables.addSelectionListener(
        		new SelectionAdapter() { 
        			public void widgetSelected(SelectionEvent arg0) { 
        				input.setChanged();
        				setWMIToolTip();
        				} 
        			}
        		);
		
		wlPosition=new Label(shell, SWT.NONE);
		props.setLook(wlPosition);
		fdlPosition=new FormData();
		fdlPosition.left  = new FormAttachment(0,0);
		fdlPosition.right = new FormAttachment(100, 0);
		fdlPosition.bottom = new FormAttachment(wVariables, -margin);
		wlPosition.setLayoutData(fdlPosition);
		
        
		// Table line...
		wlWMI=new Label(shell, SWT.NONE);
		wlWMI.setText(BaseMessages.getString(PKG, "WMIInputDialog.WMI")); //$NON-NLS-1$
 		props.setLook(wlWMI);
		fdlWMI=new FormData();
		fdlWMI.left = new FormAttachment(0, 0);
		fdlWMI.top  = new FormAttachment(wStepname, margin*2);
		wlWMI.setLayoutData(fdlWMI);
		
		
		
		wWMI=new StyledTextComp(transMeta, shell, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, "");
		props.setLook(wWMI, Props.WIDGET_STYLE_FIXED);
		fdWMI=new FormData();
		fdWMI.left  = new FormAttachment(0, 0);
		fdWMI.top   = new FormAttachment(wlWMI, margin );
		fdWMI.right = new FormAttachment(100, -2*margin);
		fdWMI.bottom= new FormAttachment(wlPosition, 0 );
		wWMI.setLayoutData(fdWMI);
		wWMI.addModifyListener(new ModifyListener()
            {
                public void modifyText(ModifyEvent arg0)
                {
                    setWMIToolTip();
                    setPosition();
                }

            }
        );
		
		wWMI.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e) { setPosition(); }
			public void keyReleased(KeyEvent e) { setPosition(); }
			} 
		);
		wWMI.addFocusListener(new FocusAdapter(){
			public void focusGained(FocusEvent e) { setPosition(); }
			public void focusLost(FocusEvent e) { setPosition(); }
			}
		);
		wWMI.addMouseListener(new MouseAdapter(){
			public void mouseDoubleClick(MouseEvent e) { setPosition(); }
			public void mouseDown(MouseEvent e) { setPosition(); }
			public void mouseUp(MouseEvent e) { setPosition(); }
			}
		);
		wWMI.addModifyListener(lsMod);
		
		
		// Text Highlighting
		wWMI.addLineStyleListener(new SQLValuesHighlight());
        
        wWMI.addKeyListener(new ControlSpaceKeyAdapter(transMeta, wWMI));
	        
		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();  } };
        lsPreview  = new Listener() { public void handleEvent(Event e) { preview(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();      } };
        
		wCancel.addListener  (SWT.Selection, lsCancel);
        wPreview.addListener (SWT.Selection, lsPreview);
		wOK.addListener      (SWT.Selection, lsOK    );

        
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wLimit.addSelectionListener( lsDef );
		
		
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
		
		String scr = wWMI.getText();
		int linenr = wWMI.getLineAtOffset(wWMI.getCaretOffset())+1;
		int posnr  = wWMI.getCaretOffset();
				
		// Go back from position to last CR: how many positions?
		int colnr=0;
		while (posnr>0 && scr.charAt(posnr-1)!='\n' && scr.charAt(posnr-1)!='\r')
		{
			posnr--;
			colnr++;
		}
		wlPosition.setText(BaseMessages.getString(PKG, "WMIInputDialog.Position.Label",""+linenr,""+colnr));

	}
	protected void setWMIToolTip()
    {
       if (wVariables.getSelection())
       {
           wWMI.setToolTipText(transMeta.environmentSubstitute(wWMI.getText()));
       }
    }

    /**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		if (input.getWMI() != null) wWMI.setText(input.getWMI());
		if (input.getDomain() != null) wDomain.setText(input.getDomain());
		if (input.getHost() != null) wHost.setText(input.getHost());
		if (input.getUserName() != null) wUserName.setText(input.getUserName());
		if (input.getPassword() != null) wPassword.setText(input.getPassword());
		
		wLimit.setText(Const.NVL(input.getRowLimit(), "0")); //$NON-NLS-1$
		
        wVariables.setSelection(input.isVariableReplacementActive());
               
		wStepname.selectAll();
        setWMIToolTip();
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	
    private void getInfo(WMIInputMeta meta, boolean preview)
    {
        meta.setWMIQuery(preview && !Const.isEmpty(wWMI.getSelectionText())?wWMI.getSelectionText():wWMI.getText());
        meta.setVariableReplacementActive(wVariables.getSelection());
        meta.setRowLimit( Const.NVL(wLimit.getText(), "0") );
        meta.setDomain(wDomain.getText());
        meta.setHost(wHost.getText());
        meta.setUserName(wUserName.getText());
        meta.setPassword(wPassword.getText());
    }
    
	private void ok()
	{
		  if(Const.isEmpty(wStepname.getText())) {
	 			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
	 			mb.setMessage("Veuillez svp donner un nom à cette étape!");
	 			mb.setText("Etape non nommée");
	 			mb.open(); 
	 			return;
	       }

		stepname = wStepname.getText(); // return value
        
        getInfo(input, false);
        
		
		
		dispose();
	}
	
	public void dispose()
	{
		super.dispose();
	}
	
	
	

    /**
     * Preview the data generated by this step.
     * This generates a transformation using this step & a dummy and previews it.
     *
     */
    private void preview()
    {    	
        // Create the table input reader step...
        WMIInputMeta oneMeta = new WMIInputMeta();
        getInfo(oneMeta, true);
        
        TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, wStepname.getText());
        
        EnterNumberDialog numberDialog = new EnterNumberDialog(shell, props.getDefaultPreviewSize(), BaseMessages.getString(PKG, "WMIInputDialog.EnterPreviewSize"), BaseMessages.getString(PKG, "WMIInputDialog.NumberOfRowsToPreview")); //$NON-NLS-1$ //$NON-NLS-2$
        int previewSize = numberDialog.open();
        if (previewSize>0)
        {
            TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(shell, previewMeta, new String[] { wStepname.getText() }, new int[] { previewSize } );
            progressDialog.open();

            Trans trans = progressDialog.getTrans();
            String loggingText = progressDialog.getLoggingText();

            boolean error=false;
            List<Object[]> rows= null;
            if (!progressDialog.isCancelled())
            {
            	// Get previewed rows
            	rows=progressDialog.getPreviewRows(wStepname.getText());
                if (trans.getResult()!=null && trans.getResult().getNrErrors()>0 && rows.size()==0)
                {
                	error=true;
                	EnterTextDialog etd = new EnterTextDialog(shell, BaseMessages.getString(PKG, "System.Dialog.PreviewError.Title"),  
                		BaseMessages.getString(PKG, "System.Dialog.PreviewError.Message"), loggingText, true );
                	etd.setReadOnly();
                	etd.open();
                }
            }
            
            if(!error) {
            	 PreviewRowsDialog prd =new PreviewRowsDialog(shell, transMeta, SWT.NONE, wStepname.getText(), progressDialog.getPreviewRowsMeta(wStepname.getText()), progressDialog.getPreviewRows(wStepname.getText()), loggingText);
                 prd.open();
            }
        }
    }
    
    public static final void checkPasswordVisible(TextVar control)
	   {
	       String password = control.getText();
	       java.util.List<String> list = new ArrayList<String>();
	       StringUtil.getUsedVariables(password, list, true);
	       // ONLY show the variable in clear text if there is ONE variable used
	       // Also, it has to be the only string in the field.
	       //

	       if (list.size() != 1)
	       {
	       	control.setEchoChar('*');
	       }
	       else
	       {
	       	String variableName = null;
	           if ((password.startsWith(StringUtil.UNIX_OPEN) && password.endsWith(StringUtil.UNIX_CLOSE)))
	           {
	           	//  ${VAR}
	           	//  012345
	           	// 
	           	variableName = password.substring(StringUtil.UNIX_OPEN.length(), password.length()-StringUtil.UNIX_CLOSE.length());
	           }
	           if ((password.startsWith(StringUtil.WINDOWS_OPEN) && password.endsWith(StringUtil.WINDOWS_CLOSE)))
	           {
	           	//  %VAR%
	           	//  01234
	           	// 
	           	variableName = password.substring(StringUtil.WINDOWS_OPEN.length(), password.length()-StringUtil.WINDOWS_CLOSE.length());
	           }
	           
	           // If there is a variable name in there AND if it's defined in the system properties...
	           // Otherwise, we'll leave it alone.
	           //
	           /*if (variableName!=null && System.getProperty(variableName)!=null)
	           {
	           	control.setEchoChar('\0'); // Show it all...
	           }
	           else
	           {
	           	control.setEchoChar('*');
	           }*/
	           if(variableName!=null) {
	        	   control.setEchoChar('\0'); // Show it all...  
	           }else {
	        	   control.setEchoChar('*');
	           }
	       }
	       control.setToolTipText("Password");
	   }

}
