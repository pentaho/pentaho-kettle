 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Samatar Hassan and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Samatar Hassan.
 * The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/


package org.pentaho.di.ui.trans.steps.httppost;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
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
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.httppost.HTTPPOSTMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;


public class HTTPPOSTDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = HTTPPOSTMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	private static final String[] YES_NO_COMBO = new String[] { BaseMessages.getString(PKG, "System.Combo.No"), BaseMessages.getString(PKG, "System.Combo.Yes") };
	private static final String YES = BaseMessages.getString(PKG, "System.Combo.Yes"); //$NON-NLS-1$
	private static final String NO = BaseMessages.getString(PKG, "System.Combo.No"); //$NON-NLS-1$
	
	private Label        wlUrl;
	private TextVar      wUrl;
	private FormData     fdlUrl, fdUrl;

	private Label        wlResult;
	private TextVar      wResult;
	private FormData     fdlResult, fdResult;

	private Label        wlResultCode;
	private TextVar      wResultCode;
	private FormData     fdlResultCode, fdResultCode;

	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;
	
	private Label        wlQuery;
	private TableView    wQuery;
	private FormData     fdlQuery, fdQuery;
	
	private Label        wlUrlInField;
    private Button       wUrlInField;
    private FormData     fdlUrlInField, fdUrlInField;
	
	private Label        wlUrlField;
	private ComboVar     wUrlField;
	private FormData     fdlUrlField, fdUrlField;
	
	private Label        wlrequestEntity;
	private ComboVar     wrequestEntity;
	private FormData     fdlrequestEntity, fdrequestEntity;

    private Label wlHttpLogin;
    private TextVar wHttpLogin;

    private Label wlHttpPassword;
    private TextVar wHttpPassword;

    private Label wlProxyHost;
    private TextVar wProxyHost;

    private Label wlProxyPort;
    private TextVar wProxyPort;
	
	private HTTPPOSTMeta input;
	
    private Map<String, Integer> inputFields;
    
    private ColumnInfo[] colinf;
    private ColumnInfo[] colinfquery;
    
    private  String fieldNames[];
    
    private boolean gotPreviousFields=false;
    
	private Button		 wGetBodyParam;
	private FormData	 fdGetBodyParam;
	private Listener	 lsGetBodyParam;
	
    private Label        wlEncoding;
    private ComboVar     wEncoding;
    private FormData     fdlEncoding, fdEncoding;
    
	private Label        wlPostAFile;
	private Button       wPostAFile;

    private boolean      gotEncodings = false;
    
	public HTTPPOSTDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(HTTPPOSTMeta)in;
        inputFields =new HashMap<String, Integer>();
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
		shell.setText(BaseMessages.getString(PKG, "HTTPPOSTDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin=Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "HTTPPOSTDialog.Stepname.Label")); //$NON-NLS-1$
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
		
		wlUrl=new Label(shell, SWT.RIGHT);
		wlUrl.setText(BaseMessages.getString(PKG, "HTTPPOSTDialog.URL.Label")); //$NON-NLS-1$
 		props.setLook(wlUrl);
		fdlUrl=new FormData();
		fdlUrl.left = new FormAttachment(0, 0);
		fdlUrl.right= new FormAttachment(middle, -margin);
		fdlUrl.top  = new FormAttachment(wStepname, margin*2);
		wlUrl.setLayoutData(fdlUrl);
		
		wUrl=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wUrl);
		wUrl.addModifyListener(lsMod);
		fdUrl=new FormData();
		fdUrl.left = new FormAttachment(middle, 0);
		fdUrl.top  = new FormAttachment(wStepname, margin*2);
		fdUrl.right= new FormAttachment(100, 0);
		wUrl.setLayoutData(fdUrl);
		
		// UrlInField line
        wlUrlInField=new Label(shell, SWT.RIGHT);
        wlUrlInField.setText(BaseMessages.getString(PKG, "HTTPPOSTDialog.UrlInField.Label"));
        props.setLook(wlUrlInField);
        fdlUrlInField=new FormData();
        fdlUrlInField.left = new FormAttachment(0, 0);
        fdlUrlInField.top  = new FormAttachment(wUrl, margin);
        fdlUrlInField.right= new FormAttachment(middle, -margin);
        wlUrlInField.setLayoutData(fdlUrlInField);
        wUrlInField=new Button(shell, SWT.CHECK );
        props.setLook(wUrlInField);
        fdUrlInField=new FormData();
        fdUrlInField.left = new FormAttachment(middle, 0);
        fdUrlInField.top  = new FormAttachment(wUrl, margin);
        fdUrlInField.right= new FormAttachment(100, 0);
        wUrlInField.setLayoutData(fdUrlInField);
        wUrlInField.addSelectionListener(new SelectionAdapter() 
            {
                public void widgetSelected(SelectionEvent e) 
                {
                	input.setChanged();
                	activeUrlInfield();
                }
            }
        );

		// UrlField Line
		wlUrlField=new Label(shell, SWT.RIGHT);
		wlUrlField.setText(BaseMessages.getString(PKG, "HTTPPOSTDialog.UrlField.Label")); //$NON-NLS-1$
 		props.setLook(wlUrlField);
		fdlUrlField=new FormData();
		fdlUrlField.left = new FormAttachment(0, 0);
		fdlUrlField.right= new FormAttachment(middle, -margin);
		fdlUrlField.top  = new FormAttachment(wUrlInField, margin);
		wlUrlField.setLayoutData(fdlUrlField);
		
        wUrlField=new ComboVar(transMeta, shell, SWT.BORDER | SWT.READ_ONLY);
        wUrlField.setEditable(true);
        props.setLook(wUrlField);
        wUrlField.addModifyListener(lsMod);
        fdUrlField=new FormData();
        fdUrlField.left = new FormAttachment(middle, 0);
        fdUrlField.top  = new FormAttachment(wUrlInField, margin);
        fdUrlField.right= new FormAttachment(100, -margin);
        wUrlField.setLayoutData(fdUrlField);
        wUrlField.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    setStreamFields();
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );      
        
        wlEncoding=new Label(shell, SWT.RIGHT);
        wlEncoding.setText(BaseMessages.getString(PKG, "HTTPPOSTDialog.Encoding.Label"));
        props.setLook(wlEncoding);
        fdlEncoding=new FormData();
        fdlEncoding.left = new FormAttachment(0, 0);
        fdlEncoding.top  = new FormAttachment(wUrlField, margin);
        fdlEncoding.right= new FormAttachment(middle, -margin);
        wlEncoding.setLayoutData(fdlEncoding);
        wEncoding=new ComboVar(transMeta, shell, SWT.BORDER | SWT.READ_ONLY);
        wEncoding.setEditable(true);
        props.setLook(wEncoding);
        wEncoding.addModifyListener(lsMod);
        fdEncoding=new FormData();
        fdEncoding.left = new FormAttachment(middle, 0);
        fdEncoding.top  = new FormAttachment(wUrlField, margin);
        fdEncoding.right= new FormAttachment(100, -margin);
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
        
        
       // requestEntity Line
		wlrequestEntity=new Label(shell, SWT.RIGHT);
		wlrequestEntity.setText(BaseMessages.getString(PKG, "HTTPPOSTDialog.requestEntity.Label")); //$NON-NLS-1$
 		props.setLook(wlrequestEntity);
		fdlrequestEntity=new FormData();
		fdlrequestEntity.left = new FormAttachment(0, 0);
		fdlrequestEntity.right= new FormAttachment(middle, -margin);
		fdlrequestEntity.top  = new FormAttachment(wEncoding, margin);
		wlrequestEntity.setLayoutData(fdlrequestEntity);
		
        wrequestEntity=new ComboVar(transMeta, shell, SWT.BORDER | SWT.READ_ONLY);
        wrequestEntity.setEditable(true);
        props.setLook(wrequestEntity);
        wrequestEntity.addModifyListener(lsMod);
        fdrequestEntity=new FormData();
        fdrequestEntity.left = new FormAttachment(middle, 0);
        fdrequestEntity.top  = new FormAttachment(wEncoding, margin);
        fdrequestEntity.right= new FormAttachment(100, -margin);
        wrequestEntity.setLayoutData(fdrequestEntity);
        wrequestEntity.addFocusListener(new FocusListener()
        {
            public void focusLost(org.eclipse.swt.events.FocusEvent e)
            {
            }
        
            public void focusGained(org.eclipse.swt.events.FocusEvent e)
            {
                Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                shell.setCursor(busy);
                setStreamFields();
                shell.setCursor(null);
                busy.dispose();
            }
        }
    ); 
        
		 // Post file?
        wlPostAFile=new Label(shell, SWT.RIGHT);
        wlPostAFile.setText(BaseMessages.getString(PKG, "HTTPPOSTDialog.postAFile.Label")); //$NON-NLS-1$
        props.setLook(wlPostAFile);
        FormData fdlPostAFile=new FormData();
        fdlPostAFile.left   = new FormAttachment(0, 0);
        fdlPostAFile.right  = new FormAttachment(middle, -margin);
        fdlPostAFile.top    = new FormAttachment(wrequestEntity, margin);
        wlPostAFile.setLayoutData(fdlPostAFile);
        wPostAFile=new Button(shell, SWT.CHECK);
        wPostAFile.setToolTipText(BaseMessages.getString(PKG, "HTTPPOSTDialog.postAFile.Tooltip")); //$NON-NLS-1$
        props.setLook(wPostAFile);
        FormData fdPostAFile=new FormData();
        fdPostAFile.left = new FormAttachment(middle, 0);
        fdPostAFile.top  = new FormAttachment(wrequestEntity, margin);
        fdPostAFile.right= new FormAttachment(100, 0);
        wPostAFile.setLayoutData(fdPostAFile);

		// Result line...
		wlResult=new Label(shell, SWT.RIGHT);
		wlResult.setText(BaseMessages.getString(PKG, "HTTPPOSTDialog.Result.Label")); //$NON-NLS-1$
 		props.setLook(wlResult);
		fdlResult=new FormData();
		fdlResult.left = new FormAttachment(0, 0);
		fdlResult.right= new FormAttachment(middle, -margin);
		fdlResult.top  = new FormAttachment(wPostAFile, margin*2);
		wlResult.setLayoutData(fdlResult);
		wResult=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wResult);
		wResult.addModifyListener(lsMod);
		fdResult=new FormData();
		fdResult.left = new FormAttachment(middle, 0);
		fdResult.top  = new FormAttachment(wPostAFile, margin*2);
		fdResult.right= new FormAttachment(100, -margin);
		wResult.setLayoutData(fdResult);

		// Resultcode line...
		wlResultCode=new Label(shell, SWT.RIGHT);
		wlResultCode.setText(BaseMessages.getString(PKG, "HTTPPOSTDialog.ResultCode.Label")); //$NON-NLS-1$
 		props.setLook(wlResultCode);
		fdlResultCode=new FormData();
		fdlResultCode.left = new FormAttachment(0, 0);
		fdlResultCode.right= new FormAttachment(middle, -margin);
		fdlResultCode.top  = new FormAttachment(wResult, margin*2);
		wlResultCode.setLayoutData(fdlResultCode);
		wResultCode=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wResultCode);
		wResultCode.addModifyListener(lsMod);
		fdResultCode=new FormData();
		fdResultCode.left = new FormAttachment(middle, 0);
		fdResultCode.top  = new FormAttachment(wResult, margin*2);
		fdResultCode.right= new FormAttachment(100, -margin);
		wResultCode.setLayoutData(fdResultCode);
		
        //////////////////////////
        // START HTTP AUTH GROUP

        Group gHttpAuth = new Group(shell, SWT.SHADOW_ETCHED_IN);
        gHttpAuth.setText(BaseMessages.getString(PKG, "HTTPPOSTDialog.HttpAuthGroup.Label")); //$NON-NLS-1$;
        FormLayout httpAuthLayout = new FormLayout();
        httpAuthLayout.marginWidth = 3;
        httpAuthLayout.marginHeight = 3;
        gHttpAuth.setLayout(httpAuthLayout);
        props.setLook(gHttpAuth);

        // HTTP Login
        wlHttpLogin = new Label(gHttpAuth, SWT.RIGHT);
        wlHttpLogin.setText(BaseMessages.getString(PKG, "HTTPPOSTDialog.HttpLogin.Label")); //$NON-NLS-1$
        props.setLook(wlHttpLogin);
        FormData fdlHttpLogin = new FormData();
        fdlHttpLogin.top = new FormAttachment(0, margin);
        fdlHttpLogin.left = new FormAttachment(0, 0);
        fdlHttpLogin.right = new FormAttachment(middle, -margin);
        wlHttpLogin.setLayoutData(fdlHttpLogin);
        wHttpLogin = new TextVar(transMeta, gHttpAuth, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wHttpLogin.addModifyListener(lsMod);
        wHttpLogin.setToolTipText(BaseMessages.getString(PKG, "HTTPPOSTDialog.HttpLogin.Tooltip")); //$NON-NLS-1$
        props.setLook(wHttpLogin);
        FormData fdHttpLogin = new FormData();
        fdHttpLogin.top = new FormAttachment(0, margin);
        fdHttpLogin.left = new FormAttachment(middle, 0);
        fdHttpLogin.right = new FormAttachment(100, 0);
        wHttpLogin.setLayoutData(fdHttpLogin);

        // HTTP Password
        wlHttpPassword = new Label(gHttpAuth, SWT.RIGHT);
        wlHttpPassword.setText(BaseMessages.getString(PKG, "HTTPPOSTDialog.HttpPassword.Label")); //$NON-NLS-1$
        props.setLook(wlHttpPassword);
        FormData fdlHttpPassword = new FormData();
        fdlHttpPassword.top = new FormAttachment(wHttpLogin, margin);
        fdlHttpPassword.left = new FormAttachment(0, 0);
        fdlHttpPassword.right = new FormAttachment(middle, -margin);
        wlHttpPassword.setLayoutData(fdlHttpPassword);
        wHttpPassword = new TextVar(transMeta, gHttpAuth, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wHttpPassword.addModifyListener(lsMod);
        wHttpPassword.setToolTipText(BaseMessages.getString(PKG, "HTTPPOSTDialog.HttpPassword.Tooltip")); //$NON-NLS-1$
        wHttpPassword.setEchoChar('*');
        props.setLook(wHttpPassword);
        FormData fdHttpPassword = new FormData();
        fdHttpPassword.top = new FormAttachment(wHttpLogin, margin);
        fdHttpPassword.left = new FormAttachment(middle, 0);
        fdHttpPassword.right = new FormAttachment(100, 0);
        wHttpPassword.setLayoutData(fdHttpPassword);
        
        FormData fdHttpAuth = new FormData();
        fdHttpAuth.left = new FormAttachment(0, 0);
        fdHttpAuth.right = new FormAttachment(100, 0);
        fdHttpAuth.top = new FormAttachment(wResultCode, margin);
        gHttpAuth.setLayoutData(fdHttpAuth);

        // END HTTP AUTH GROUP
        //////////////////////////

        //////////////////////////
        // START PROXY GROUP

        Group gProxy = new Group(shell, SWT.SHADOW_ETCHED_IN);
        gProxy.setText(BaseMessages.getString(PKG, "HTTPPOSTDialog.ProxyGroup.Label")); //$NON-NLS-1$;
        FormLayout proxyLayout = new FormLayout();
        proxyLayout.marginWidth = 3;
        proxyLayout.marginHeight = 3;
        gProxy.setLayout(proxyLayout);
        props.setLook(gProxy);

        // HTTP Login
        wlProxyHost = new Label(gProxy, SWT.RIGHT);
        wlProxyHost.setText(BaseMessages.getString(PKG, "HTTPPOSTDialog.ProxyHost.Label")); //$NON-NLS-1$
        props.setLook(wlProxyHost);
        FormData fdlProxyHost = new FormData();
        fdlProxyHost.top = new FormAttachment(0, margin);
        fdlProxyHost.left = new FormAttachment(0, 0);
        fdlProxyHost.right = new FormAttachment(middle, -margin);
        wlProxyHost.setLayoutData(fdlProxyHost);
        wProxyHost = new TextVar(transMeta, gProxy, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wProxyHost.addModifyListener(lsMod);
        wProxyHost.setToolTipText(BaseMessages.getString(PKG, "HTTPPOSTDialog.ProxyHost.Tooltip")); //$NON-NLS-1$
        props.setLook(wProxyHost);
        FormData fdProxyHost = new FormData();
        fdProxyHost.top = new FormAttachment(0, margin);
        fdProxyHost.left = new FormAttachment(middle, 0);
        fdProxyHost.right = new FormAttachment(100, 0);
        wProxyHost.setLayoutData(fdProxyHost);

        // HTTP Password
        wlProxyPort = new Label(gProxy, SWT.RIGHT);
        wlProxyPort.setText(BaseMessages.getString(PKG, "HTTPPOSTDialog.ProxyPort.Label")); //$NON-NLS-1$
        props.setLook(wlProxyPort);
        FormData fdlProxyPort = new FormData();
        fdlProxyPort.top = new FormAttachment(wProxyHost, margin);
        fdlProxyPort.left = new FormAttachment(0, 0);
        fdlProxyPort.right = new FormAttachment(middle, -margin);
        wlProxyPort.setLayoutData(fdlProxyPort);
        wProxyPort = new TextVar(transMeta, gProxy, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wProxyPort.addModifyListener(lsMod);
        wProxyPort.setToolTipText(BaseMessages.getString(PKG, "HTTPPOSTDialog.ProxyPort.Tooltip")); //$NON-NLS-1$
        props.setLook(wProxyPort);
        FormData fdProxyPort = new FormData();
        fdProxyPort.top = new FormAttachment(wProxyHost, margin);
        fdProxyPort.left = new FormAttachment(middle, 0);
        fdProxyPort.right = new FormAttachment(100, 0);
        wProxyPort.setLayoutData(fdProxyPort);

        FormData fdProxy = new FormData();
        fdProxy.left = new FormAttachment(0, 0);
        fdProxy.right = new FormAttachment(100, 0);
        fdProxy.top = new FormAttachment(gHttpAuth, margin);
        gProxy.setLayoutData(fdProxy);

        // END HTTP AUTH GROUP
        //////////////////////////

		
		wlFields=new Label(shell, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "HTTPPOSTDialog.Parameters.Label")); //$NON-NLS-1$
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(gProxy, margin);
		wlFields.setLayoutData(fdlFields);
		
		final int FieldsRows=input.getArgumentField().length;
		
		  colinf=new ColumnInfo[] { 
		  new ColumnInfo(BaseMessages.getString(PKG, "HTTPPOSTDialog.ColumnInfo.Name"),       ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false), //$NON-NLS-1$
		  new ColumnInfo(BaseMessages.getString(PKG, "HTTPPOSTDialog.ColumnInfo.Parameter"),  ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
		  new ColumnInfo(BaseMessages.getString(PKG, "HTTPPOSTDialog.ColumnInfo.Header"),  ColumnInfo.COLUMN_TYPE_CCOMBO,   YES_NO_COMBO), //$NON-NLS-1$
		 };
		  colinf[1].setUsingVariables(true);
		wFields=new TableView(transMeta, shell, 
							  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
							  colinf, 
							  FieldsRows,  
							  lsMod,
							  props
							  );
		
		wGetBodyParam = new Button(shell, SWT.PUSH);
		wGetBodyParam.setText(BaseMessages.getString(PKG, "HTTPPOSTDialog.GetFields.Button")); //$NON-NLS-1$
		fdGetBodyParam = new FormData();
		fdGetBodyParam.top   = new FormAttachment(wlFields, margin);
		fdGetBodyParam.right = new FormAttachment(100, 0);
		wGetBodyParam.setLayoutData(fdGetBodyParam);
		
		fdFields=new FormData();
		fdFields.left  = new FormAttachment(0, 0);
		fdFields.top   = new FormAttachment(wlFields, margin);
		fdFields.right = new FormAttachment(wGetBodyParam, -margin);
		fdFields.bottom= new FormAttachment(wlFields, 150);
		wFields.setLayoutData(fdFields);
		
		wlQuery=new Label(shell, SWT.NONE);
		wlQuery.setText(BaseMessages.getString(PKG, "HTTPPOSTDialog.QueryParameters.Label")); //$NON-NLS-1$
 		props.setLook(wlQuery);
		fdlQuery=new FormData();
		fdlQuery.left = new FormAttachment(0, 0);
		fdlQuery.top  = new FormAttachment(wFields, margin);
		wlQuery.setLayoutData(fdlQuery);
		
		final int QueryRows=input.getQueryParameter().length;
		
		  colinfquery=new ColumnInfo[] { 
		  new ColumnInfo(BaseMessages.getString(PKG, "HTTPPOSTDialog.ColumnInfo.QueryName"),       ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false), //$NON-NLS-1$
		  new ColumnInfo(BaseMessages.getString(PKG, "HTTPPOSTDialog.ColumnInfo.QueryParameter"),  ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
		 };
		 colinfquery[1].setUsingVariables(true);
		wQuery=new TableView(transMeta, shell, 
							  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
							  colinfquery, 
							  QueryRows,  
							  lsMod,
							  props
							  );

		wGet = new Button(shell, SWT.PUSH);
		wGet.setText(BaseMessages.getString(PKG, "HTTPPOSTDialog.GetFields.Button")); //$NON-NLS-1$
		fdGet = new FormData();
		fdGet.top   = new FormAttachment(wlQuery, margin);
		fdGet.right = new FormAttachment(100, 0);
		wGet.setLayoutData(fdGet);
		
		fdQuery=new FormData();
		fdQuery.left  = new FormAttachment(0, 0);
		fdQuery.top   = new FormAttachment(wlQuery, margin);
		fdQuery.right = new FormAttachment(wGet, -margin);
		fdQuery.bottom= new FormAttachment(100, -50);
		wQuery.setLayoutData(fdQuery);



		  // 
        // Search the fields in the background
        //
        
        final Runnable runnable = new Runnable()
        {
            public void run()
            {
                StepMeta stepMeta = transMeta.findStep(stepname);
                if (stepMeta!=null)
                {
                    try
                    {
                    	RowMetaInterface row = transMeta.getPrevStepFields(stepMeta);
                        
                        // Remember these fields...
                        for (int i=0;i<row.size();i++)
                        {
                        	inputFields.put(row.getValueMeta(i).getName(), Integer.valueOf(i));
                        }
                        
                        setComboBoxes();
                    }
                    catch(KettleException e)
                    {
                    	logError(BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"));
                    }
                }
            }
        };
        new Thread(runnable).start();
        
		
		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wQuery);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();        } };
		lsGet      = new Listener() { public void handleEvent(Event e) { getQueryFields();        } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();    } };
		lsGetBodyParam = new Listener()  {public void handleEvent(Event e){get();}	};
		
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wGet.addListener   (SWT.Selection, lsGet   );
		wCancel.addListener(SWT.Selection, lsCancel);
		wGetBodyParam.addListener(SWT.Selection, lsGetBodyParam);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
        wUrl.addSelectionListener( lsDef );
        wResult.addSelectionListener( lsDef );
        wResultCode.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		lsResize = new Listener() 
		{
			public void handleEvent(Event event) 
			{
				Point size = shell.getSize();
				wFields.setSize(size.x-10, size.y-50);
				wFields.table.setSize(size.x-10, size.y-50);
				wFields.redraw();
			}
		};
		shell.addListener(SWT.Resize, lsResize);

		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		activeUrlInfield();
		input.setChanged(changed);

		shell.open();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	protected void setComboBoxes()
    {
        // Something was changed in the row.
        //
        final Map<String, Integer> fields = new HashMap<String, Integer>();
        
        // Add the currentMeta fields...
        fields.putAll(inputFields);
        
        Set<String> keySet = fields.keySet();
        List<String> entries = new ArrayList<String>(keySet);
        
        fieldNames = (String[]) entries.toArray(new String[entries.size()]);

        Const.sortStrings(fieldNames);
        colinf[0].setComboValues(fieldNames);
        colinfquery[0].setComboValues(fieldNames);
    }
	 private void setStreamFields()
	 {
		 if(!gotPreviousFields)
		 {
			 String urlfield=wUrlField.getText();
			 wUrlField.removeAll();
			 wUrlField.setItems(fieldNames);										
			 if(urlfield!=null)  wUrlField.setText(urlfield);	
			 
			 String request=wrequestEntity.getText();
			 wrequestEntity.removeAll();
			 wrequestEntity.setItems(fieldNames);										
			 if(request!=null)  wrequestEntity.setText(request);
			 
			 gotPreviousFields=true;
		 }
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
	        }
	    }
	private void activeUrlInfield()
	{
		wlUrlField.setEnabled(wUrlInField.getSelection());
		wUrlField.setEnabled(wUrlInField.getSelection());
		wlUrl.setEnabled(!wUrlInField.getSelection());
		wUrl.setEnabled(!wUrlInField.getSelection());       
	}
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		int i;
		if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "HTTPPOSTDialog.Log.GettingKeyInfo")); //$NON-NLS-1$
		
		if (input.getArgumentField()!=null)
		{
			for (i=0;i<input.getArgumentField().length;i++)
			{
				TableItem item = wFields.table.getItem(i);
				if (input.getArgumentField()[i]      !=null) item.setText(1, input.getArgumentField()[i]);
				if (input.getArgumentParameter()[i]  !=null) item.setText(2, input.getArgumentParameter()[i]);
				item.setText(3, (input.getArgumentHeader()[i]) ? YES : NO );
			}
		}
		if (input.getQueryField()!=null)
		{
			for (i=0;i<input.getQueryField().length;i++)
			{
				TableItem item = wQuery.table.getItem(i);
				if (input.getQueryField()[i]      !=null) item.setText(1, input.getQueryField()[i]);
				if (input.getQueryParameter()[i]  !=null) item.setText(2, input.getQueryParameter()[i]);
			}
		}
		if (input.getUrl() !=null)      wUrl.setText(input.getUrl());
        wUrlInField.setSelection(input.isUrlInField());
        if (input.getUrlField() !=null) wUrlField.setText(input.getUrlField());
        if (input.getRequestEntity() !=null)      wrequestEntity.setText(input.getRequestEntity());
		if (input.getFieldName()!=null) wResult.setText(input.getFieldName());
		if (input.getResultCodeFieldName()!=null) wResultCode.setText(input.getResultCodeFieldName());
		if (input.getEncoding()!=null) wEncoding.setText(input.getEncoding());
		wPostAFile.setSelection(input.isPostAFile());
		
	    if(input.getHttpLogin() != null) wHttpLogin.setText(input.getHttpLogin());
	    if(input.getHttpPassword() != null) wHttpPassword.setText(input.getHttpPassword());
	    if(input.getProxyHost() != null) wProxyHost.setText(input.getProxyHost());
	    if(input.getProxyPort() != null) wProxyPort.setText(input.getProxyPort());
		
		wFields.setRowNums();
		wFields.optWidth(true);
		wStepname.selectAll();
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

		int nrargs = wFields.nrNonEmpty();
		input.allocate(nrargs);

		if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "HTTPPOSTDialog.Log.FoundArguments",String.valueOf(nrargs))); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i=0;i<nrargs;i++)
		{
			TableItem item = wFields.getNonEmpty(i);
			input.getArgumentField()[i]       = item.getText(1);
			input.getArgumentParameter()[i]    = item.getText(2);
			input.getArgumentHeader()[i] = YES.equals(item.getText(3));			
		}
		
		int nrqueryparams = wQuery.nrNonEmpty();
		input.allocateQuery(nrqueryparams);

		if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "HTTPPOSTDialog.Log.FoundQueryParameters",String.valueOf(nrqueryparams))); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i=0;i<nrqueryparams;i++)
		{
			TableItem item = wQuery.getNonEmpty(i);
			input.getQueryField()[i]       = item.getText(1);
			input.getQueryParameter()[i]    = item.getText(2);
		}
        
		input.setUrl( wUrl.getText() );
		input.setUrlField(wUrlField.getText() );
		input.setRequestEntity(wrequestEntity.getText() );
		input.setUrlInField(wUrlInField.getSelection() );
		input.setFieldName( wResult.getText() );
		input.setResultCodeFieldName( wResultCode.getText() );
		input.setEncoding( wEncoding.getText() );
		input.setPostAFile(wPostAFile.getSelection());
		input.setHttpLogin(wHttpLogin.getText());
		input.setHttpPassword(wHttpPassword.getText());
		input.setProxyHost(wProxyHost.getText());
		input.setProxyPort(wProxyPort.getText());
		
		stepname = wStepname.getText(); // return value

		dispose();
	}

	private void get()
	{
		try
		{
			RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r!=null && !r.isEmpty())
			{
				TableItemInsertListener listener = new TableItemInsertListener()
                {
                    public boolean tableItemInserted(TableItem tableItem, ValueMetaInterface v)
                    {
                        tableItem.setText(3, NO); // default is "N"
                        return true;
                    }
                };	
                BaseStepDialog.getFieldsFromPrevious(r, wFields, 1, new int[] { 1, 2 }, null, -1, -1, listener);
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "HTTPPOSTDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "HTTPPOSTDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}
	private void getQueryFields()
	{
		try
		{
			RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r!=null && !r.isEmpty())
			{
                BaseStepDialog.getFieldsFromPrevious(r, wQuery, 1, new int[] { 1, 2 }, new int[] { 3 }, -1, -1, null);
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "HTTPPOSTDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "HTTPPOSTDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}

}
