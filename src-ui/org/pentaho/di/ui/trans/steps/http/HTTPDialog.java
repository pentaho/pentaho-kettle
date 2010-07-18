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
 * Created on 2-jul-2003
 *
 */

package org.pentaho.di.ui.trans.steps.http;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.http.HTTPMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


public class HTTPDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = HTTPMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label        wlUrl;
	private TextVar      wUrl;
	private FormData     fdlUrl, fdUrl;

	private Label        wlResult;
	private Text         wResult;
	private FormData     fdlResult, fdResult;

	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;
	
   private Label        wlHeaders;
   private TableView    wHeaders;
   private FormData     fdlHeaders, fdHeaders;
	
	private Label        wlUrlInField;
    private Button       wUrlInField;
    private FormData     fdlUrlInField, fdUrlInField;
	
	private Label        wlUrlField;
	private ComboVar     wUrlField;
	private FormData     fdlUrlField, fdUrlField;

	private ComboVar     wEncoding;

	private Button wGet, wGetHeaders;
	private Listener lsGet, lsGetHeaders;

    private Label wlHttpLogin;
    private TextVar wHttpLogin;

    private Label wlHttpPassword;
    private TextVar wHttpPassword;

    private Label wlProxyHost;
    private TextVar wProxyHost;

    private Label wlProxyPort;
    private TextVar wProxyPort;
    
	private CTabFolder   wTabFolder;
	
	private CTabItem     wGeneralTab, wAdditionalTab;
	private FormData     fdTabFolder;
	
	private Composite    wGeneralComp, wAdditionalComp;
	private FormData     fdGeneralComp, fdAdditionalComp;
	
	private Label        wlResultCode;
	private Text         wResultCode;
	private FormData     fdlResultCode, fdResultCode;
	
	private HTTPMeta input;
	
	private ColumnInfo[] colinf;
	private ColumnInfo[] colinfHeaders;
	
    private Map<String, Integer> inputFields;

	private boolean gotEncodings = false;

	public HTTPDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(HTTPMeta)in;
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
		shell.setText(BaseMessages.getString(PKG, "HTTPDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin=Const.MARGIN;

		// Stepname line
		//
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "HTTPDialog.Stepname.Label")); //$NON-NLS-1$
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
		Control lastControl = wStepname;
		
		wTabFolder = new CTabFolder(shell, SWT.BORDER);
 		props.setLook(wTabFolder, PropsUI.WIDGET_STYLE_TAB);
		
		//////////////////////////
		// START OF GENERAL TAB   ///
		//////////////////////////
		wGeneralTab=new CTabItem(wTabFolder, SWT.NONE);
		wGeneralTab.setText(BaseMessages.getString(PKG, "HTTPDialog.GeneralTab.Title"));
		
		wGeneralComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wGeneralComp);

		FormLayout fileLayout = new FormLayout();
		fileLayout.marginWidth  = 3;
		fileLayout.marginHeight = 3;
		wGeneralComp.setLayout(fileLayout);
		
		 //////////////////////////
        // START Settings GROUP

        Group gSettings = new Group(wGeneralComp, SWT.SHADOW_ETCHED_IN);
        gSettings.setText(BaseMessages.getString(PKG, "HTTPDialog.SettingsGroup.Label")); //$NON-NLS-1$;
        FormLayout SettingsLayout = new FormLayout();
        SettingsLayout.marginWidth = 3;
        SettingsLayout.marginHeight = 3;
        gSettings.setLayout(SettingsLayout);
        props.setLook(gSettings);

		
		// The URL to use
		//
		wlUrl=new Label(gSettings, SWT.RIGHT);
		wlUrl.setText(BaseMessages.getString(PKG, "HTTPDialog.URL.Label")); //$NON-NLS-1$
 		props.setLook(wlUrl);
		fdlUrl=new FormData();
		fdlUrl.left = new FormAttachment(0, 0);
		fdlUrl.right= new FormAttachment(middle, -margin);
		fdlUrl.top  = new FormAttachment(lastControl, margin);
		wlUrl.setLayoutData(fdlUrl);

		wUrl=new TextVar(transMeta, gSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wUrl);
		wUrl.addModifyListener(lsMod);
		fdUrl=new FormData();
		fdUrl.left = new FormAttachment(middle, 0);
		fdUrl.top  = new FormAttachment(lastControl, margin);
		fdUrl.right= new FormAttachment(100, 0);
		wUrl.setLayoutData(fdUrl);
		lastControl = wUrl;
		
		// UrlInField line
		//
        wlUrlInField=new Label(gSettings, SWT.RIGHT);
        wlUrlInField.setText(BaseMessages.getString(PKG, "HTTPDialog.UrlInField.Label"));
        props.setLook(wlUrlInField);
        fdlUrlInField=new FormData();
        fdlUrlInField.left = new FormAttachment(0, 0);
        fdlUrlInField.top  = new FormAttachment(lastControl, margin);
        fdlUrlInField.right= new FormAttachment(middle, -margin);
        wlUrlInField.setLayoutData(fdlUrlInField);
        wUrlInField=new Button(gSettings, SWT.CHECK );
        props.setLook(wUrlInField);
        fdUrlInField=new FormData();
        fdUrlInField.left = new FormAttachment(middle, 0);
        fdUrlInField.top  = new FormAttachment(lastControl, margin);
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
        lastControl = wUrlInField;
        
		// UrlField Line
        //
		wlUrlField=new Label(gSettings, SWT.RIGHT);
		wlUrlField.setText(BaseMessages.getString(PKG, "HTTPDialog.UrlField.Label")); //$NON-NLS-1$
 		props.setLook(wlUrlField);
		fdlUrlField=new FormData();
		fdlUrlField.left = new FormAttachment(0, 0);
		fdlUrlField.right= new FormAttachment(middle, -margin);
		fdlUrlField.top  = new FormAttachment(lastControl, margin);
		wlUrlField.setLayoutData(fdlUrlField);

    	wUrlField=new ComboVar(transMeta, gSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    	wUrlField.setToolTipText(BaseMessages.getString(PKG, "HTTPDialog.UrlField.Tooltip"));
		props.setLook(wUrlField);
		wUrlField.addModifyListener(lsMod);
		fdUrlField=new FormData();
		fdUrlField.left = new FormAttachment(middle, 0);
		fdUrlField.top  = new FormAttachment(lastControl, margin);
		fdUrlField.right= new FormAttachment(100, 0);
		wUrlField.setLayoutData(fdUrlField);
		wUrlField.setEnabled(false);
		wUrlField.addFocusListener(new FocusListener()
	         {
	            public void focusLost(org.eclipse.swt.events.FocusEvent e)
	             {
	             }
	             public void focusGained(org.eclipse.swt.events.FocusEvent e)
	             {
	                 Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
	                 shell.setCursor(busy);
	                 BaseStepDialog.getFieldsFromPrevious(wUrlField, transMeta, stepMeta);
	                 shell.setCursor(null);
	                 busy.dispose();
	             }
	         }
	     );      
		lastControl = wUrlField;
		
		// Encoding
		//
		Label wlEncoding = new Label(gSettings, SWT.RIGHT);
		wlEncoding.setText(BaseMessages.getString(PKG, "HTTPDialog.Encoding.Label")); //$NON-NLS-1$
 		props.setLook(wlEncoding);
		FormData fdlEncoding = new FormData();
		fdlEncoding.top  = new FormAttachment(lastControl, margin);
		fdlEncoding.left = new FormAttachment(0, 0);
		fdlEncoding.right= new FormAttachment(middle, -margin);
		wlEncoding.setLayoutData(fdlEncoding);
		wEncoding=new ComboVar(transMeta, gSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wEncoding);
		wEncoding.addModifyListener(lsMod);
		FormData fdEncoding = new FormData();
		fdEncoding.top  = new FormAttachment(lastControl, margin);
		fdEncoding.left = new FormAttachment(middle, 0);
		fdEncoding.right= new FormAttachment(100, 0);
		wEncoding.setLayoutData(fdEncoding);
		lastControl = wEncoding;
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
        
        FormData fdSettings = new FormData();
        fdSettings.left = new FormAttachment(0, 0);
        fdSettings.right = new FormAttachment(100, 0);
        fdSettings.top = new FormAttachment(lastControl, margin);
        gSettings.setLayoutData(fdSettings);

        // END Output Settings GROUP
        //////////////////////////
        lastControl=gSettings;
 	   //////////////////////////
        // START Output Fields GROUP

        Group gOutputFields = new Group(wGeneralComp, SWT.SHADOW_ETCHED_IN);
        gOutputFields.setText(BaseMessages.getString(PKG, "HTTPDialog.OutputFieldsGroup.Label")); //$NON-NLS-1$;
        FormLayout OutputFieldsLayout = new FormLayout();
        OutputFieldsLayout.marginWidth = 3;
        OutputFieldsLayout.marginHeight = 3;
        gOutputFields.setLayout(OutputFieldsLayout);
        props.setLook(gOutputFields);


		// Result line...
		//
		wlResult=new Label(gOutputFields, SWT.RIGHT);
		wlResult.setText(BaseMessages.getString(PKG, "HTTPDialog.Result.Label")); //$NON-NLS-1$
 		props.setLook(wlResult);
		fdlResult=new FormData();
		fdlResult.left = new FormAttachment(0, 0);
		fdlResult.right= new FormAttachment(middle, -margin);
		fdlResult.top  = new FormAttachment(lastControl, margin);
		wlResult.setLayoutData(fdlResult);
		wResult=new Text(gOutputFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wResult);
		wResult.addModifyListener(lsMod);
		fdResult=new FormData();
		fdResult.left = new FormAttachment(middle, 0);
		fdResult.top  = new FormAttachment(lastControl, margin);
		fdResult.right= new FormAttachment(100, 0);
		wResult.setLayoutData(fdResult);
		
		// Resultcode line...
		wlResultCode=new Label(gOutputFields, SWT.RIGHT);
		wlResultCode.setText(BaseMessages.getString(PKG, "HTTPDialog.ResultCode.Label")); //$NON-NLS-1$
 		props.setLook(wlResultCode);
		fdlResultCode=new FormData();
		fdlResultCode.left = new FormAttachment(0, 0);
		fdlResultCode.right= new FormAttachment(middle, -margin);
		fdlResultCode.top  = new FormAttachment(wResult, margin);
		wlResultCode.setLayoutData(fdlResultCode);
		wResultCode=new Text(gOutputFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wResultCode);
		wResultCode.addModifyListener(lsMod);
		fdResultCode=new FormData();
		fdResultCode.left = new FormAttachment(middle, 0);
		fdResultCode.top  = new FormAttachment(wResult, margin);
		fdResultCode.right= new FormAttachment(100, 0);
		wResultCode.setLayoutData(fdResultCode);
		
        FormData fdOutputFields = new FormData();
        fdOutputFields.left = new FormAttachment(0, 0);
        fdOutputFields.right = new FormAttachment(100, 0);
        fdOutputFields.top = new FormAttachment(lastControl, margin);
        gOutputFields.setLayoutData(fdOutputFields);

        // END Output Fields GROUP
        //////////////////////////
        
        lastControl=gOutputFields;
        
		  //////////////////////////
        // START HTTP AUTH GROUP

        Group gHttpAuth = new Group(wGeneralComp, SWT.SHADOW_ETCHED_IN);
        gHttpAuth.setText(BaseMessages.getString(PKG, "HTTPDialog.HttpAuthGroup.Label")); //$NON-NLS-1$;
        FormLayout httpAuthLayout = new FormLayout();
        httpAuthLayout.marginWidth = 3;
        httpAuthLayout.marginHeight = 3;
        gHttpAuth.setLayout(httpAuthLayout);
        props.setLook(gHttpAuth);

        // HTTP Login
        wlHttpLogin = new Label(gHttpAuth, SWT.RIGHT);
        wlHttpLogin.setText(BaseMessages.getString(PKG, "HTTPDialog.HttpLogin.Label")); //$NON-NLS-1$
        props.setLook(wlHttpLogin);
        FormData fdlHttpLogin = new FormData();
        fdlHttpLogin.top = new FormAttachment(0, margin);
        fdlHttpLogin.left = new FormAttachment(0, 0);
        fdlHttpLogin.right = new FormAttachment(middle, -margin);
        wlHttpLogin.setLayoutData(fdlHttpLogin);
        wHttpLogin = new TextVar(transMeta, gHttpAuth, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wHttpLogin.addModifyListener(lsMod);
        wHttpLogin.setToolTipText(BaseMessages.getString(PKG, "HTTPDialog.HttpLogin.Tooltip")); //$NON-NLS-1$
        props.setLook(wHttpLogin);
        FormData fdHttpLogin = new FormData();
        fdHttpLogin.top = new FormAttachment(0, margin);
        fdHttpLogin.left = new FormAttachment(middle, 0);
        fdHttpLogin.right = new FormAttachment(100, 0);
        wHttpLogin.setLayoutData(fdHttpLogin);

        // HTTP Password
        wlHttpPassword = new Label(gHttpAuth, SWT.RIGHT);
        wlHttpPassword.setText(BaseMessages.getString(PKG, "HTTPDialog.HttpPassword.Label")); //$NON-NLS-1$
        props.setLook(wlHttpPassword);
        FormData fdlHttpPassword = new FormData();
        fdlHttpPassword.top = new FormAttachment(wHttpLogin, margin);
        fdlHttpPassword.left = new FormAttachment(0, 0);
        fdlHttpPassword.right = new FormAttachment(middle, -margin);
        wlHttpPassword.setLayoutData(fdlHttpPassword);
        wHttpPassword = new TextVar(transMeta, gHttpAuth, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wHttpPassword.addModifyListener(lsMod);
        wHttpPassword.setToolTipText(BaseMessages.getString(PKG, "HTTPDialog.HttpPassword.Tooltip")); //$NON-NLS-1$
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
        fdHttpAuth.top = new FormAttachment(lastControl, margin);
        gHttpAuth.setLayoutData(fdHttpAuth);

        // END HTTP AUTH GROUP
        //////////////////////////

		lastControl = gHttpAuth;
		
        //////////////////////////
        // START PROXY GROUP

        Group gProxy = new Group(wGeneralComp, SWT.SHADOW_ETCHED_IN);
        gProxy.setText(BaseMessages.getString(PKG, "HTTPDialog.ProxyGroup.Label")); //$NON-NLS-1$;
        FormLayout proxyLayout = new FormLayout();
        proxyLayout.marginWidth = 3;
        proxyLayout.marginHeight = 3;
        gProxy.setLayout(proxyLayout);
        props.setLook(gProxy);

        // HTTP Login
        wlProxyHost = new Label(gProxy, SWT.RIGHT);
        wlProxyHost.setText(BaseMessages.getString(PKG, "HTTPDialog.ProxyHost.Label")); //$NON-NLS-1$
        props.setLook(wlProxyHost);
        FormData fdlProxyHost = new FormData();
        fdlProxyHost.top = new FormAttachment(0, margin);
        fdlProxyHost.left = new FormAttachment(0, 0);
        fdlProxyHost.right = new FormAttachment(middle, -margin);
        wlProxyHost.setLayoutData(fdlProxyHost);
        wProxyHost = new TextVar(transMeta, gProxy, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wProxyHost.addModifyListener(lsMod);
        wProxyHost.setToolTipText(BaseMessages.getString(PKG, "HTTPDialog.ProxyHost.Tooltip")); //$NON-NLS-1$
        props.setLook(wProxyHost);
        FormData fdProxyHost = new FormData();
        fdProxyHost.top = new FormAttachment(0, margin);
        fdProxyHost.left = new FormAttachment(middle, 0);
        fdProxyHost.right = new FormAttachment(100, 0);
        wProxyHost.setLayoutData(fdProxyHost);

        // HTTP Password
        wlProxyPort = new Label(gProxy, SWT.RIGHT);
        wlProxyPort.setText(BaseMessages.getString(PKG, "HTTPDialog.ProxyPort.Label")); //$NON-NLS-1$
        props.setLook(wlProxyPort);
        FormData fdlProxyPort = new FormData();
        fdlProxyPort.top = new FormAttachment(wProxyHost, margin);
        fdlProxyPort.left = new FormAttachment(0, 0);
        fdlProxyPort.right = new FormAttachment(middle, -margin);
        wlProxyPort.setLayoutData(fdlProxyPort);
        wProxyPort = new TextVar(transMeta, gProxy, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wProxyPort.addModifyListener(lsMod);
        wProxyPort.setToolTipText(BaseMessages.getString(PKG, "HTTPDialog.ProxyPort.Tooltip")); //$NON-NLS-1$
        props.setLook(wProxyPort);
        FormData fdProxyPort = new FormData();
        fdProxyPort.top = new FormAttachment(wProxyHost, margin);
        fdProxyPort.left = new FormAttachment(middle, 0);
        fdProxyPort.right = new FormAttachment(100, 0);
        wProxyPort.setLayoutData(fdProxyPort);

        FormData fdProxy = new FormData();
        fdProxy.left = new FormAttachment(0, 0);
        fdProxy.right = new FormAttachment(100, 0);
        fdProxy.top = new FormAttachment(lastControl, margin);
        gProxy.setLayoutData(fdProxy);

        // END HTTP Proxy GROUP
        //////////////////////////
        lastControl = gProxy;
        
		fdGeneralComp=new FormData();
		fdGeneralComp.left  = new FormAttachment(0, 0);
		fdGeneralComp.top   = new FormAttachment(wStepname, margin);
		fdGeneralComp.right = new FormAttachment(100, 0);
		fdGeneralComp.bottom= new FormAttachment(100, 0);
		wGeneralComp.setLayoutData(fdGeneralComp);
	
		wGeneralComp.layout();
		wGeneralTab.setControl(wGeneralComp);
		
		/////////////////////////////////////////////////////////////
		/// END OF GENERAL TAB
		/////////////////////////////////////////////////////////////
		// Additional tab...
		//
		wAdditionalTab = new CTabItem(wTabFolder, SWT.NONE);
		wAdditionalTab.setText(BaseMessages.getString(PKG, "HTTPDialog.FieldsTab.Title"));
		
		FormLayout addLayout = new FormLayout ();
		addLayout.marginWidth  = Const.FORM_MARGIN;
		addLayout.marginHeight = Const.FORM_MARGIN;
		
		wAdditionalComp = new Composite(wTabFolder, SWT.NONE);
		wAdditionalComp.setLayout(addLayout);
 		props.setLook(wAdditionalComp);

      wlFields=new Label(wAdditionalComp, SWT.NONE);
      wlFields.setText(BaseMessages.getString(PKG, "HTTPDialog.Parameters.Label")); //$NON-NLS-1$
      props.setLook(wlFields);
      fdlFields=new FormData();
      fdlFields.left = new FormAttachment(0, 0);
      fdlFields.top  = new FormAttachment(lastControl, margin);
      wlFields.setLayoutData(fdlFields);
      lastControl = wlFields;
        
		wGet=new Button(wAdditionalComp, SWT.PUSH);
		wGet.setText(BaseMessages.getString(PKG, "HTTPDialog.GetFields.Button")); //$NON-NLS-1$
		FormData fdGet = new FormData();
		fdGet.top = new FormAttachment(wlFields, margin);
		fdGet.right = new FormAttachment(100, 0);
		wGet.setLayoutData(fdGet);
			
		final int FieldsRows=input.getArgumentField().length;
		
		 colinf=new ColumnInfo[] { 
		  new ColumnInfo(BaseMessages.getString(PKG, "HTTPDialog.ColumnInfo.Name"),      ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false),
		  new ColumnInfo(BaseMessages.getString(PKG, "HTTPDialog.ColumnInfo.Parameter"),  ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
        };
		
		wFields=new TableView(transMeta, wAdditionalComp, 
							  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
							  colinf, 
							  FieldsRows,  
							  lsMod,
							  props
							  );

		fdFields=new FormData();
		fdFields.left  = new FormAttachment(0, 0);
		fdFields.top   = new FormAttachment(wlFields, margin);
		fdFields.right = new FormAttachment(wGet, -margin);
		fdFields.bottom= new FormAttachment(wlFields, 200);
		wFields.setLayoutData(fdFields);
		


		wlHeaders = new Label(wAdditionalComp,SWT.NONE);
		wlHeaders.setText(BaseMessages.getString(PKG, "HTTPDialog.Headers.Label"));
		props.setLook(wlHeaders);
		fdlHeaders = new FormData();
		fdlHeaders.left = new FormAttachment(0,0);
		fdlHeaders.top = new FormAttachment(wFields, margin);
		wlHeaders.setLayoutData(fdlHeaders);
		
		final int HeadersRows = input.getHeaderParameter().length;
		
		colinfHeaders = new ColumnInfo[] {
		      new ColumnInfo(BaseMessages.getString(PKG, "HTTPDialog.ColumnInfo.Field"),ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false),
		      new ColumnInfo(BaseMessages.getString(PKG, "HTTPDialog.ColumnInfo.Header"),ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
		};
		colinfHeaders[1].setUsingVariables(true);
		wHeaders = new TableView(transMeta, wAdditionalComp, 
            SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
            colinfHeaders, 
            HeadersRows,  
            lsMod,
            props
            );

		wGetHeaders=new Button(wAdditionalComp, SWT.PUSH);
		wGetHeaders.setText(BaseMessages.getString(PKG, "HTTPDialog.GetHeaders.Button")); //$NON-NLS-1$
      FormData fdGetHeaders = new FormData();
      fdGetHeaders.top = new FormAttachment(wlHeaders, margin);
      fdGetHeaders.right = new FormAttachment(100, 0);
      wGetHeaders.setLayoutData(fdGetHeaders);
		
      fdHeaders=new FormData();
      fdHeaders.left  = new FormAttachment(0, 0);
      fdHeaders.top   = new FormAttachment(wlHeaders, margin);
      fdHeaders.right = new FormAttachment(wGetHeaders, -margin);
      fdHeaders.bottom= new FormAttachment(100, -margin);
      wHeaders.setLayoutData(fdHeaders);

		  // 
        // Search the fields in the background
		
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

        fdAdditionalComp=new FormData();
 		fdAdditionalComp.left  = new FormAttachment(0, 0);
		fdAdditionalComp.top   = new FormAttachment(wStepname, margin);
		fdAdditionalComp.right = new FormAttachment(100, 0);
		fdAdditionalComp.bottom= new FormAttachment(100, 0);
		wAdditionalComp.setLayoutData(fdAdditionalComp);
		
		wAdditionalComp.layout();
		wAdditionalTab.setControl(wAdditionalComp);
		//////// END of Additional Tab
		
		
		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(wStepname, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);
        
        
		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel}, margin, wTabFolder);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();        } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();        } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();    } };
		lsGetHeaders = new Listener()  {public void handleEvent(Event e){getHeadersFields();}  };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wGet.addListener   (SWT.Selection, lsGet   );
		wCancel.addListener(SWT.Selection, lsCancel);
		wGetHeaders.addListener(SWT.Selection, lsGetHeaders);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
        wUrl.addSelectionListener( lsDef );
        wResult.addSelectionListener( lsDef );
		
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
		wTabFolder.setSelection(0);
		activeUrlInfield();
		input.setChanged(changed);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	private void setEncodings()
    {
        // Encoding of the text file:
        if (!gotEncodings)
        {
            gotEncodings  = true;
            
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
	
	protected void setComboBoxes()
    {
        // Something was changed in the row.
        //
        final Map<String, Integer> fields = new HashMap<String, Integer>();
        
        // Add the currentMeta fields...
        fields.putAll(inputFields);
        
        Set<String> keySet = fields.keySet();
        List<String> entries = new ArrayList<String>(keySet);

        String fieldNames[] = (String[]) entries.toArray(new String[entries.size()]);

        Const.sortStrings(fieldNames);
        colinf[0].setComboValues(fieldNames);
        colinfHeaders[0].setComboValues(fieldNames);
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
		if(isDebug()) logDebug(BaseMessages.getString(PKG, "HTTPDialog.Log.GettingKeyInfo")); //$NON-NLS-1$
		
		if (input.getArgumentField()!=null) {
			for (int i=0;i<input.getArgumentField().length;i++)
			{
				TableItem item = wFields.table.getItem(i);
				item.setText(1, Const.NVL(input.getArgumentField()[i], ""));
				item.setText(2, Const.NVL(input.getArgumentParameter()[i], ""));
			}
		}
		
		if (input.getHeaderField()!=null)
		{
		   for (int i = 0; i < input.getHeaderField().length; i++)
		   {
		      TableItem item = wHeaders.table.getItem(i);
		      if (input.getHeaderField()[i]      !=null) item.setText(1, input.getHeaderField()[i]);
		      if (input.getHeaderParameter()[i]  !=null) item.setText(2, input.getHeaderParameter()[i]);
		   }
		}
		
		wUrl.setText(Const.NVL(input.getUrl(), ""));
        wUrlInField.setSelection(input.isUrlInField());
        wUrlField.setText(Const.NVL(input.getUrlField(), ""));
        wEncoding.setText(Const.NVL(input.getEncoding(), ""));
        
		wResult.setText(Const.NVL(input.getFieldName(), ""));
	    if(input.getHttpLogin() != null) wHttpLogin.setText(input.getHttpLogin());
	    if(input.getHttpPassword() != null) wHttpPassword.setText(input.getHttpPassword());
	    if(input.getProxyHost() != null) wProxyHost.setText(input.getProxyHost());
	    if(input.getProxyPort() != null) wProxyPort.setText(input.getProxyPort());
		if (input.getResultCodeFieldName()!=null) wResultCode.setText(input.getResultCodeFieldName());
		
		wFields.setRowNums();
		wFields.optWidth(true);
		wHeaders.setRowNums();
		wHeaders.optWidth(true);
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
		int nrheaders = wHeaders.nrNonEmpty();
		
		input.allocate(nrargs, nrheaders);

		if(isDebug()) logDebug(BaseMessages.getString(PKG, "HTTPDialog.Log.FoundArguments",String.valueOf(nrargs))); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i=0;i<nrargs;i++)
		{
			TableItem item = wFields.getNonEmpty(i);
			input.getArgumentField()[i]       = item.getText(1);
			input.getArgumentParameter()[i]    = item.getText(2);
		}

		if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "HTTPDialog.Log.FoundHeaders",String.valueOf(nrheaders))); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i=0; i < nrheaders; i++)
		{
		   TableItem item = wHeaders.getNonEmpty(i);
		   input.getHeaderField()[i]       = item.getText(1);
		   input.getHeaderParameter()[i]    = item.getText(2);
		}

		input.setUrl( wUrl.getText() );
		input.setUrlField(wUrlField.getText() );
		input.setUrlInField(wUrlInField.getSelection() );
		input.setFieldName( wResult.getText() );
		input.setEncoding( wEncoding.getText() );
		input.setHttpLogin(wHttpLogin.getText());
		input.setHttpPassword(wHttpPassword.getText());
		input.setProxyHost(wProxyHost.getText());
		input.setProxyPort(wProxyPort.getText());
		input.setResultCodeFieldName( wResultCode.getText() );
		
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
                BaseStepDialog.getFieldsFromPrevious(r, wFields, 1, new int[] { 1, 2 }, new int[] { 3 }, -1, -1, null);
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "HTTPDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "HTTPDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	private void getHeadersFields()
   {
      try
      {
         RowMetaInterface r = transMeta.getPrevStepFields(stepname);
         if (r!=null && !r.isEmpty())
         {
                BaseStepDialog.getFieldsFromPrevious(r, wHeaders, 1, new int[] { 1, 2 }, new int[] { 3 }, -1, -1, null);
         }
      }
      catch(KettleException ke)
      {
         new ErrorDialog(shell, BaseMessages.getString(PKG, "HTTPPOSTDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "HTTPPOSTDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
      }

   }

	public String toString()
	{
		return this.getClass().getName();
	}
}