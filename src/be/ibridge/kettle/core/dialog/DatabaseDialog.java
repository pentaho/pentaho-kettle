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

 
package be.ibridge.kettle.core.dialog;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.database.GenericDatabaseMeta;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.trans.step.BaseStepDialog;


/**
 * 
 * Dialog that allows you to edit the settings of a database connection.
 * 
 * @see <code>DatabaseInfo</code>
 * @author Matt
 * @since 18-05-2003
 *
 */

public class DatabaseDialog extends Dialog 
{
	private DatabaseMeta connection;
	
	private CTabFolder   wTabFolder;
	private FormData     fdTabFolder;
	
	private CTabItem     wDbTab, wOracleTab, wIfxTab, wSAPTab, wGenericTab;

	private Composite    wDbComp, wOracleComp, wIfxComp, wSAPComp, wGenericComp;
	private FormData     fdDbComp, fdOracleComp, fdIfxComp, fdSAPComp, fdGenericComp;

	private Shell     shell;

    // DB
	private Label    wlConn, wlConnType, wlConnAcc, wlHostName, wlDBName, wlPort, wlServername, wlUsername, wlPassword, wlData, wlIndex;
	private Text     wConn,  wHostName,  wDBName,  wPort,  wServername,  wUsername,  wPassword, wData,  wIndex;
	private List     wConnType,  wConnAcc;
	
	private FormData fdlConn, fdlConnType, fdlConnAcc, fdlPort, fdlHostName, fdlDBName, fdlServername, fdlUsername, fdlPassword, fdlData, fdlIndex;
	private FormData fdConn,  fdConnType, fdConnAcc, fdPort, fdHostName, fdDBName,  fdServername, fdUsername, fdPassword, fdData, fdIndex;

    // SAP
    private Label    wlSAPLanguage, wlSAPSystemNumber, wlSAPClient;
    private Text     wSAPLanguage, wSAPSystemNumber, wSAPClient;

    private FormData fdlSAPLanguage, fdlSAPSystemNumber, fdlSAPClient;
    private FormData fdSAPLanguage, fdSAPSystemNumber, fdSAPClient;

    // Generic
    private Label    wlURL, wlDriverClass;
    private Text     wURL, wDriverClass;

    private FormData fdlURL, fdlDriverClass;
    private FormData fdURL, fdDriverClass;

	private Button    wOK, wTest, wExp, wList, wCancel;
	
	private String connectionName;
	
	private ModifyListener lsMod;

	private boolean changed;
	private Props   props;
	private String previousDatabaseType;
    private ArrayList databases;

	public DatabaseDialog(Shell par, int style, LogWriter lg, DatabaseMeta conn, Props pr)
	{
		super(par, style);
		connection=conn;
		connectionName=conn.getName();
		props=pr;
        this.databases = null;
	}
	
	public String open() 
	{
		Shell parent = getParent();
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
 		props.setLook(shell);
		
		lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				connection.setChanged();
			}
		};
		changed = connection.hasChanged();

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;
		
		shell.setText("Connection information");
		shell.setLayout (formLayout);
 		
		// First, add the buttons...
		
		// Buttons
		wOK     = new Button(shell, SWT.PUSH); 
		wOK.setText(" &OK ");

		wTest    = new Button(shell, SWT.PUSH); 
		wTest.setText(" &Test ");

		wExp    = new Button(shell, SWT.PUSH); 
		wExp.setText(" &Explore ");

        wList   = new Button(shell, SWT.PUSH); 
        wList.setText(" Feature &List ");

		wCancel = new Button(shell, SWT.PUSH); 
		wCancel.setText(" &Cancel ");

		Button[] buttons = new Button[] { wOK, wTest, wExp, wList, wCancel };
		BaseStepDialog.positionBottomButtons(shell, buttons, margin, null);
		
		// The rest stays above the buttons...
		
		wTabFolder = new CTabFolder(shell, SWT.BORDER);
 		props.setLook(wTabFolder, Props.WIDGET_STYLE_TABLE);

		//////////////////////////
		// START OF DB TAB   ///
		//////////////////////////
		wDbTab=new CTabItem(wTabFolder, SWT.NONE);
		wDbTab.setText("General");
		
		wDbComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wDbComp);

		FormLayout GenLayout = new FormLayout();
		GenLayout.marginWidth  = 3;
		GenLayout.marginHeight = 3;
		wDbComp.setLayout(GenLayout);

		// What's the connection name?
		wlConn = new Label(wDbComp, SWT.RIGHT); 
 		props.setLook(wlConn);
		wlConn.setText("Connection name: ");
		fdlConn = new FormData();
		fdlConn.top   = new FormAttachment(0, 0);
		fdlConn.left  = new FormAttachment(0, 0);  // First one in the left top corner
		fdlConn.right = new FormAttachment(middle, -margin);
		wlConn.setLayoutData(fdlConn);

		wConn = new Text(wDbComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
 		props.setLook(wConn);
		wConn.addModifyListener(lsMod);
		fdConn = new FormData();
		fdConn.top  = new FormAttachment(0, 0);
		fdConn.left = new FormAttachment(middle, 0); // To the right of the label
		fdConn.right= new FormAttachment(95, 0);
		wConn.setLayoutData(fdConn);

		// What types are there?
		wlConnType = new Label(wDbComp, SWT.RIGHT); 
		wlConnType.setText("Connection type: "); 
 		props.setLook(wlConnType);
		fdlConnType = new FormData();
		fdlConnType.top    = new FormAttachment(wConn, margin);  // below the line above
		fdlConnType.left   = new FormAttachment(0,0); 
		fdlConnType.right  = new FormAttachment(middle, -margin);
		wlConnType.setLayoutData(fdlConnType);

		wConnType = new List(wDbComp, SWT.BORDER | SWT.READ_ONLY | SWT.SINGLE | SWT.V_SCROLL);
 		props.setLook(wConnType);
		String[] dbtypes=DatabaseMeta.getDBTypeDescLongList();
		for (int i=0;i<dbtypes.length;i++)
		{
			wConnType.add( dbtypes[i] );
		}
 		props.setLook(wConnType);
		fdConnType = new FormData();
		fdConnType.top    = new FormAttachment(wConn, margin);
		fdConnType.left   = new FormAttachment(middle, 0);  // right of the label
		fdConnType.right  = new FormAttachment(95, 0);
		fdConnType.bottom = new FormAttachment(wConn, 150);
		wConnType.setLayoutData(fdConnType);

		// What access types are there?
		wlConnAcc = new Label(wDbComp, SWT.RIGHT); 
		wlConnAcc.setText("Method of access: "); 
 		props.setLook(wlConnAcc);
		fdlConnAcc = new FormData();
		fdlConnAcc.top  = new FormAttachment(wConnType, margin);  // below the line above
		fdlConnAcc.left = new FormAttachment(0,0); 
		fdlConnAcc.right= new FormAttachment(middle, -margin);
		wlConnAcc.setLayoutData(fdlConnAcc);

		wConnAcc = new List(wDbComp, SWT.BORDER | SWT.READ_ONLY | SWT.SINGLE | SWT.V_SCROLL);
 		props.setLook(wConnAcc);
		props.setLook(wConnAcc);
		fdConnAcc = new FormData();
		fdConnAcc.top    = new FormAttachment(wConnType, margin);
		fdConnAcc.left   = new FormAttachment(middle, 0);  // right of the label
		fdConnAcc.right  = new FormAttachment(95, 0);
		//fdConnAcc.bottom = new FormAttachment(wConnType, 50);
		wConnAcc.setLayoutData(fdConnAcc);

		// Hostname
		wlHostName = new Label(wDbComp, SWT.RIGHT); 
		wlHostName.setText("Server host name: "); 
 		props.setLook(wlHostName);

		fdlHostName = new FormData();
		fdlHostName.top  = new FormAttachment(wConnAcc, margin);
		fdlHostName.left = new FormAttachment(0,0);
		fdlHostName.right= new FormAttachment(middle, -margin);
		wlHostName.setLayoutData(fdlHostName);

		wHostName = new Text(wDbComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
 		props.setLook(wHostName);
		wHostName.addModifyListener(lsMod);
		fdHostName = new FormData();
		fdHostName.top  = new FormAttachment(wConnAcc, margin);
		fdHostName.left = new FormAttachment(middle, 0); 
		fdHostName.right= new FormAttachment(95, 0);
		wHostName.setLayoutData(fdHostName);
		
		// DBName
		wlDBName = new Label(wDbComp, SWT.RIGHT ); 
		wlDBName.setText("Database name: "); 
 		props.setLook(wlDBName);
		fdlDBName = new FormData();
		fdlDBName.top  = new FormAttachment(wHostName, margin);
		fdlDBName.left = new FormAttachment(0,0);	
		fdlDBName.right= new FormAttachment(middle, -margin);
		wlDBName.setLayoutData(fdlDBName);

		wDBName = new Text(wDbComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
 		props.setLook(wDBName);
		wDBName.addModifyListener(lsMod);
		fdDBName = new FormData();
		fdDBName.top  = new FormAttachment(wHostName, margin);
		fdDBName.left = new FormAttachment(middle, 0);
		fdDBName.right= new FormAttachment(95, 0);
		wDBName.setLayoutData(fdDBName);
				
		// Port
		wlPort = new Label(wDbComp, SWT.RIGHT ); 
		wlPort.setText("Port number: "); 
 		props.setLook(wlPort);
		fdlPort = new FormData();
		fdlPort.top  = new FormAttachment(wDBName, margin);
		fdlPort.left = new FormAttachment(0,0);
		fdlPort.right= new FormAttachment(middle, -margin);
		wlPort.setLayoutData(fdlPort);

		wPort = new Text(wDbComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
 		props.setLook(wPort);
		wPort.addModifyListener(lsMod);
		fdPort = new FormData();
		fdPort.top  = new FormAttachment(wDBName, margin);
		fdPort.left = new FormAttachment(middle, 0); 
		fdPort.right= new FormAttachment(95, 0);
		wPort.setLayoutData(fdPort);
		
		// Username
		wlUsername = new Label(wDbComp, SWT.RIGHT ); 
		wlUsername.setText("Username: "); 
 		props.setLook(wlUsername);
		fdlUsername = new FormData();
		fdlUsername.top  = new FormAttachment(wPort, margin);
		fdlUsername.left = new FormAttachment(0,0); 
		fdlUsername.right= new FormAttachment(middle, -margin);
		wlUsername.setLayoutData(fdlUsername);

		wUsername = new Text(wDbComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
 		props.setLook(wUsername);
		wUsername.addModifyListener(lsMod);
		fdUsername = new FormData();
		fdUsername.top  = new FormAttachment(wPort, margin);
		fdUsername.left = new FormAttachment(middle, 0); 
		fdUsername.right= new FormAttachment(95, 0);
		wUsername.setLayoutData(fdUsername);

		
		// Password
		wlPassword = new Label(wDbComp, SWT.RIGHT ); 
		wlPassword.setText("Password: "); 
 		props.setLook(wlPassword);
		fdlPassword = new FormData();
		fdlPassword.top  = new FormAttachment(wUsername, margin);
		fdlPassword.left = new FormAttachment(0,0);
		fdlPassword.right= new FormAttachment(middle, -margin);
		wlPassword.setLayoutData(fdlPassword);

		wPassword = new Text(wDbComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
 		props.setLook(wPassword);
		wPassword.setEchoChar('*');
		wPassword.addModifyListener(lsMod);
		fdPassword = new FormData();
		fdPassword.top  = new FormAttachment(wUsername, margin);
		fdPassword.left = new FormAttachment(middle, 0); 
		fdPassword.right= new FormAttachment(95, 0);
		wPassword.setLayoutData(fdPassword);

		
		fdDbComp=new FormData();
		fdDbComp.left  = new FormAttachment(0, 0);
		fdDbComp.top   = new FormAttachment(0, 0);
		fdDbComp.right = new FormAttachment(100, 0);
		fdDbComp.bottom= new FormAttachment(100, 0);
		wDbComp.setLayoutData(fdDbComp);
	
		wDbComp.layout();
		wDbTab.setControl(wDbComp);
		
		/////////////////////////////////////////////////////////////
		/// END OF GEN TAB
		/////////////////////////////////////////////////////////////

		//////////////////////////
		// START OF ORACLE TAB///
		///
		wOracleTab=new CTabItem(wTabFolder, SWT.NONE);
		wOracleTab.setText("Oracle");

		FormLayout oracleLayout = new FormLayout ();
		oracleLayout.marginWidth  = 3;
		oracleLayout.marginHeight = 3;
		
		wOracleComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wOracleComp);
		wOracleComp.setLayout(oracleLayout);

		// What's the data tablespace name?
		wlData = new Label(wOracleComp, SWT.RIGHT); 
 		props.setLook(wlData);
		wlData.setText("Tablespace for data: "); 
		fdlData = new FormData();
		fdlData.top   = new FormAttachment(0, 0);
		fdlData.left  = new FormAttachment(0, 0);  // First one in the left top corner
		fdlData.right = new FormAttachment(middle, -margin);
		wlData.setLayoutData(fdlData);

		wData = new Text(wOracleComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
		wData.setText( NVL(connection.getDataTablespace()==null?"":connection.getDataTablespace(), "") );
 		props.setLook(wData);
		wData.addModifyListener(lsMod);
		fdData = new FormData();
		fdData.top  = new FormAttachment(0, 0);
		fdData.left = new FormAttachment(middle, 0); // To the right of the label
		fdData.right= new FormAttachment(95, 0);
		wData.setLayoutData(fdData);

		// What's the index tablespace name?
		wlIndex = new Label(wOracleComp, SWT.RIGHT); 
 		props.setLook(wlIndex);
		wlIndex.setText("Tablespace for indexes: "); 
		fdlIndex = new FormData();
		fdlIndex.top   = new FormAttachment(wData, margin);
		fdlIndex.left  = new FormAttachment(0, 0);  // First one in the left top corner
		fdlIndex.right = new FormAttachment(middle, -margin);
		wlIndex.setLayoutData(fdlIndex);

		wIndex = new Text(wOracleComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
		wIndex.setText( NVL(connection.getIndexTablespace()==null?"":connection.getIndexTablespace(), "") );
 		props.setLook(wIndex);
		wIndex.addModifyListener(lsMod);
		fdIndex = new FormData();
		fdIndex.top  = new FormAttachment(wData, margin);
		fdIndex.left = new FormAttachment(middle, 0); // To the right of the label
		fdIndex.right= new FormAttachment(95, 0);
		wIndex.setLayoutData(fdIndex);
		
		
		fdOracleComp = new FormData();
		fdOracleComp.left  = new FormAttachment(0, 0);
		fdOracleComp.top   = new FormAttachment(0, 0);
		fdOracleComp.right = new FormAttachment(100, 0);
		fdOracleComp.bottom= new FormAttachment(100, 0);
		wOracleComp.setLayoutData(fdOracleComp);

		wOracleComp.layout();
		wOracleTab.setControl(wOracleComp);

		
		//////////////////////////
		// START OF INFORMIX TAB///
		///
		wIfxTab=new CTabItem(wTabFolder, SWT.NONE);
		wIfxTab.setText("Informix");

		FormLayout ifxLayout = new FormLayout ();
		ifxLayout.marginWidth  = 3;
		ifxLayout.marginHeight = 3;
		
		wIfxComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wIfxComp);
		wIfxComp.setLayout(ifxLayout);

		// Servername
		wlServername = new Label(wIfxComp, SWT.RIGHT ); 
		wlServername.setText("Informix Servername: "); 
 		props.setLook(wlServername);
		fdlServername = new FormData();
		fdlServername.top  = new FormAttachment(0, margin);
		fdlServername.left = new FormAttachment(0,0);
		fdlServername.right= new FormAttachment(middle, -margin);
		wlServername.setLayoutData(fdlServername);

		wServername = new Text(wIfxComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
 		props.setLook(wServername);
		wServername.addModifyListener(lsMod);
		fdServername = new FormData();
		fdServername.top  = new FormAttachment(0, margin);
		fdServername.left = new FormAttachment(middle, 0); 
		fdServername.right= new FormAttachment(95, 0);
		wServername.setLayoutData(fdServername);
		
		fdIfxComp = new FormData();
		fdIfxComp.left  = new FormAttachment(0, 0);
		fdIfxComp.top   = new FormAttachment(0, 0);
		fdIfxComp.right = new FormAttachment(100, 0);
		fdIfxComp.bottom= new FormAttachment(100, 0);
		wIfxComp.setLayoutData(fdIfxComp);

		wIfxComp.layout();
		wIfxTab.setControl(wIfxComp);

        
        //////////////////////////
        // START OF SAP TAB///
        ///
        wSAPTab=new CTabItem(wTabFolder, SWT.NONE);
        wSAPTab.setText("SAP R/3");

        FormLayout sapLayout = new FormLayout ();
        sapLayout.marginWidth  = 3;
        sapLayout.marginHeight = 3;
        
        wSAPComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(        wSAPComp);
        wSAPComp.setLayout(sapLayout);

        // wSAPLanguage, wSSAPystemNumber, wSAPSystemID
        
        // Language
        wlSAPLanguage = new Label(wSAPComp, SWT.RIGHT ); 
        wlSAPLanguage.setText("Language "); 
 		props.setLook(        wlSAPLanguage);
        fdlSAPLanguage = new FormData();
        fdlSAPLanguage.top  = new FormAttachment(0, margin);
        fdlSAPLanguage.left = new FormAttachment(0,0);
        fdlSAPLanguage.right= new FormAttachment(middle, -margin);
        wlSAPLanguage.setLayoutData(fdlSAPLanguage);

        wSAPLanguage = new Text(wSAPComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
 		props.setLook(        wSAPLanguage);
        wSAPLanguage.addModifyListener(lsMod);
        fdSAPLanguage = new FormData();
        fdSAPLanguage.top  = new FormAttachment(0, margin);
        fdSAPLanguage.left = new FormAttachment(middle, 0); 
        fdSAPLanguage.right= new FormAttachment(95, 0);
        wSAPLanguage.setLayoutData(fdSAPLanguage);
   
        
        // SystemNumber
        wlSAPSystemNumber = new Label(wSAPComp, SWT.RIGHT ); 
        wlSAPSystemNumber.setText("System Number "); 
 		props.setLook(        wlSAPSystemNumber);
        fdlSAPSystemNumber = new FormData();
        fdlSAPSystemNumber.top  = new FormAttachment(wSAPLanguage, margin);
        fdlSAPSystemNumber.left = new FormAttachment(0,0);
        fdlSAPSystemNumber.right= new FormAttachment(middle, -margin);
        wlSAPSystemNumber.setLayoutData(fdlSAPSystemNumber);

        wSAPSystemNumber = new Text(wSAPComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
 		props.setLook(        wSAPSystemNumber);
        wSAPSystemNumber.addModifyListener(lsMod);
        fdSAPSystemNumber = new FormData();
        fdSAPSystemNumber.top  = new FormAttachment(wSAPLanguage, margin);
        fdSAPSystemNumber.left = new FormAttachment(middle, 0); 
        fdSAPSystemNumber.right= new FormAttachment(95, 0);
        wSAPSystemNumber.setLayoutData(fdSAPSystemNumber);

        // SystemID
        wlSAPClient = new Label(wSAPComp, SWT.RIGHT ); 
        wlSAPClient.setText("SAP Client"); 
 		props.setLook(        wlSAPClient);
        fdlSAPClient = new FormData();
        fdlSAPClient.top  = new FormAttachment(wSAPSystemNumber, margin);
        fdlSAPClient.left = new FormAttachment(0,0);
        fdlSAPClient.right= new FormAttachment(middle, -margin);
        wlSAPClient.setLayoutData(fdlSAPClient);

        wSAPClient = new Text(wSAPComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
 		props.setLook(        wSAPClient);
        wSAPClient.addModifyListener(lsMod);
        fdSAPClient = new FormData();
        fdSAPClient.top  = new FormAttachment(wSAPSystemNumber, margin);
        fdSAPClient.left = new FormAttachment(middle, 0); 
        fdSAPClient.right= new FormAttachment(95, 0);
        wSAPClient.setLayoutData(fdSAPClient);

        
        fdSAPComp = new FormData();
        fdSAPComp.left  = new FormAttachment(0, 0);
        fdSAPComp.top   = new FormAttachment(0, 0);
        fdSAPComp.right = new FormAttachment(100, 0);
        fdSAPComp.bottom= new FormAttachment(100, 0);
        wSAPComp.setLayoutData(fdSAPComp);

        wSAPComp.layout();
        wSAPTab.setControl(wSAPComp);

        //////////////////////////
        // START OF DB TAB///
        ///
        wGenericTab=new CTabItem(wTabFolder, SWT.NONE);
        wGenericTab.setText("Generic");
        wGenericTab.setToolTipText("Settings in case you want to use a generic database with a non-supported JDBC driver");

        FormLayout genericLayout = new FormLayout ();
        genericLayout.marginWidth  = 3;
        genericLayout.marginHeight = 3;
        
        wGenericComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook( wGenericComp );
        wGenericComp.setLayout(genericLayout);

        // URL
        wlURL = new Label(wGenericComp, SWT.RIGHT ); 
        wlURL.setText("URL "); 
        props.setLook(wlURL);
        fdlURL = new FormData();
        fdlURL.top  = new FormAttachment(0, margin);
        fdlURL.left = new FormAttachment(0,0);
        fdlURL.right= new FormAttachment(middle, -margin);
        wlURL.setLayoutData(fdlURL);

        wURL = new Text(wGenericComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook(        wURL);
        wURL.addModifyListener(lsMod);
        fdURL = new FormData();
        fdURL.top  = new FormAttachment(0, margin);
        fdURL.left = new FormAttachment(middle, 0); 
        fdURL.right= new FormAttachment(95, 0);
        wURL.setLayoutData(fdURL);
   
        
        // Driver class
        wlDriverClass = new Label(wGenericComp, SWT.RIGHT ); 
        wlDriverClass.setText("Driver class "); 
        props.setLook(        wlDriverClass);
        fdlDriverClass = new FormData();
        fdlDriverClass.top  = new FormAttachment(wURL, margin);
        fdlDriverClass.left = new FormAttachment(0,0);
        fdlDriverClass.right= new FormAttachment(middle, -margin);
        wlDriverClass.setLayoutData(fdlDriverClass);

        wDriverClass = new Text(wGenericComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook(        wDriverClass);
        wDriverClass.addModifyListener(lsMod);
        fdDriverClass = new FormData();
        fdDriverClass.top  = new FormAttachment(wURL, margin);
        fdDriverClass.left = new FormAttachment(middle, 0); 
        fdDriverClass.right= new FormAttachment(95, 0);
        wDriverClass.setLayoutData(fdDriverClass);

        
        fdGenericComp = new FormData();
        fdGenericComp.left  = new FormAttachment(0, 0);
        fdGenericComp.top   = new FormAttachment(0, 0);
        fdGenericComp.right = new FormAttachment(100, 0);
        fdGenericComp.bottom= new FormAttachment(100, 0);
        wGenericComp.setLayoutData(fdGenericComp);

        wGenericComp.layout();
        wGenericTab.setControl(wGenericComp);

        
		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(0, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(wOK, -margin);
		wTabFolder.setLayoutData(fdTabFolder);

		
		// Add listeners
		wOK.addListener(SWT.Selection, new Listener ()
			{
				public void handleEvent (Event e) 
				{
					handleOK();
				}
			}
		);
						
		wCancel.addListener(SWT.Selection, new Listener ()
			{
				public void handleEvent (Event e) 
				{
					cancel();
				}
			}
		);
		wTest.addListener(SWT.Selection, new Listener ()
			{
				public void handleEvent (Event e) 
				{
					test();
				}
			}
		);
        wExp.addListener(SWT.Selection, new Listener ()
                {
                    public void handleEvent (Event e) 
                    {
                        explore();
                    }
                }
            );
		wList.addListener(SWT.Selection, new Listener ()
			{
				public void handleEvent (Event e) 
				{
					showFeatureList();
				}
			}
		);
		SelectionAdapter selAdapter=new SelectionAdapter()
			{
				public void widgetDefaultSelected(SelectionEvent e)
				{
					handleOK();	
				}
			};
		wHostName.addSelectionListener(selAdapter);
		wDBName.addSelectionListener(selAdapter);
		wPort.addSelectionListener(selAdapter);
		wUsername.addSelectionListener(selAdapter);
		wPassword.addSelectionListener(selAdapter);
		wConn.addSelectionListener(selAdapter);
		wData.addSelectionListener(selAdapter);
		wIndex.addSelectionListener(selAdapter);
		
		SelectionAdapter lsTypeAcc = 
			new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					enableFields();
					setPortNumber();
				}
			};

		wConnType.addSelectionListener(	lsTypeAcc );
		wConnAcc.addSelectionListener( lsTypeAcc );

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
	
		wTabFolder.setSelection(0);
		
		getData();
		enableFields();

		WindowProperty winprop = props.getScreen(shell.getText());
		if (winprop!=null) winprop.setShell(shell); else shell.pack();
		
		connection.setChanged(changed);
		shell.open();
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		return connectionName;
	}
	
	public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
    
    public void setDatabases(ArrayList databases)
    {
        this.databases = databases;
    }
    
    public void getData()
	{
		wConn.setText( NVL(connection==null?"":connection.getName(), "") );
		wConnType.select( connection.getDatabaseType() - 1);
		wConnType.showSelection();
		previousDatabaseType = DatabaseMeta.getDBTypeDesc(wConnType.getSelectionIndex()+1);
		
		setAccessList();
		
		String accessList[] = wConnAcc.getItems();
		int accessIndex = Const.indexOfString(connection.getAccessTypeDesc(), accessList);
		wConnAcc.select( accessIndex );
		wConnAcc.showSelection();
		
		wHostName.setText( NVL(connection.getHostname(), "") );
		wDBName.setText( NVL(connection.getDatabaseName(), "") );
		wPort.setText( ""+connection.getDatabasePortNumber() );
		wServername.setText( NVL(connection.getServername(), "") );
		wUsername.setText( NVL(connection.getUsername(), "") );
		wPassword.setText( NVL(connection.getPassword(), "") );
		wData.setText( NVL(connection.getDataTablespace(), "") );
		wIndex.setText( NVL(connection.getIndexTablespace(), "") );
        
        wSAPLanguage.setText( connection.getAttributes().getProperty("SAPLanguage", ""));
        wSAPSystemNumber.setText( connection.getAttributes().getProperty("SAPSystemNumber", ""));
        wSAPClient.setText( connection.getAttributes().getProperty("SAPClient", ""));

        wURL.setText(         connection.getAttributes().getProperty(GenericDatabaseMeta.ATRRIBUTE_CUSTOM_URL, ""));
        wDriverClass.setText( connection.getAttributes().getProperty(GenericDatabaseMeta.ATRRIBUTE_CUSTOM_DRIVER_CLASS, ""));
        
		wConn.setFocus();
		wConn.selectAll();
	}
	
	public void enableFields()
	{
		// See if we need to refresh the access list...
		String type = DatabaseMeta.getDBTypeDesc(wConnType.getSelectionIndex()+1);
		if (!type.equalsIgnoreCase(previousDatabaseType)) setAccessList();
		previousDatabaseType=type;
		
		// If the type is not Informix: disable the servername field!
		int idxDBType = wConnType.getSelectionIndex();
		if (idxDBType>=0)
		{
			int dbtype = DatabaseMeta.getDatabaseType( wConnType.getItem(idxDBType) );
            int idxAccType = wConnAcc.getSelectionIndex();
            int acctype = -1;
            if (idxAccType>=0)
            {
                acctype = DatabaseMeta.getAccessType( wConnAcc.getItem(idxAccType) );    
            }

			wlServername.setEnabled( dbtype==DatabaseMeta.TYPE_DATABASE_INFORMIX );
			wServername.setEnabled( dbtype==DatabaseMeta.TYPE_DATABASE_INFORMIX );
            
            wlData.setEnabled( dbtype==DatabaseMeta.TYPE_DATABASE_ORACLE );
            wData.setEnabled( dbtype==DatabaseMeta.TYPE_DATABASE_ORACLE );
            wlIndex.setEnabled( dbtype==DatabaseMeta.TYPE_DATABASE_ORACLE );
            wIndex.setEnabled( dbtype==DatabaseMeta.TYPE_DATABASE_ORACLE );
            
            wlSAPLanguage.setEnabled( dbtype==DatabaseMeta.TYPE_DATABASE_SAPR3 );
            wSAPLanguage.setEnabled( dbtype==DatabaseMeta.TYPE_DATABASE_SAPR3 );
            wlSAPSystemNumber.setEnabled( dbtype==DatabaseMeta.TYPE_DATABASE_SAPR3 );
            wSAPSystemNumber.setEnabled( dbtype==DatabaseMeta.TYPE_DATABASE_SAPR3 );
            wlSAPClient.setEnabled( dbtype==DatabaseMeta.TYPE_DATABASE_SAPR3 );
            wSAPClient.setEnabled( dbtype==DatabaseMeta.TYPE_DATABASE_SAPR3 );
            wlDBName.setEnabled( dbtype!=DatabaseMeta.TYPE_DATABASE_SAPR3 );
            wDBName.setEnabled( dbtype!=DatabaseMeta.TYPE_DATABASE_SAPR3 );
            wlPort.setEnabled( dbtype!=DatabaseMeta.TYPE_DATABASE_SAPR3 );
            wPort.setEnabled( dbtype!=DatabaseMeta.TYPE_DATABASE_SAPR3 );
            wTest.setEnabled( dbtype!=DatabaseMeta.TYPE_DATABASE_SAPR3 );
            wExp.setEnabled( dbtype!=DatabaseMeta.TYPE_DATABASE_SAPR3 );
            
            wlHostName.setEnabled(  !(dbtype==DatabaseMeta.TYPE_DATABASE_GENERIC && acctype == DatabaseMeta.TYPE_ACCESS_NATIVE));
            wHostName.setEnabled(   !(dbtype==DatabaseMeta.TYPE_DATABASE_GENERIC && acctype == DatabaseMeta.TYPE_ACCESS_NATIVE));
            wlDBName.setEnabled(    !(dbtype==DatabaseMeta.TYPE_DATABASE_GENERIC && acctype == DatabaseMeta.TYPE_ACCESS_NATIVE));
            wDBName.setEnabled(     !(dbtype==DatabaseMeta.TYPE_DATABASE_GENERIC && acctype == DatabaseMeta.TYPE_ACCESS_NATIVE));
            wlPort.setEnabled(      !(dbtype==DatabaseMeta.TYPE_DATABASE_GENERIC && acctype == DatabaseMeta.TYPE_ACCESS_NATIVE));
            wPort.setEnabled(       !(dbtype==DatabaseMeta.TYPE_DATABASE_GENERIC && acctype == DatabaseMeta.TYPE_ACCESS_NATIVE));
            
            wlURL.setEnabled(         dbtype==DatabaseMeta.TYPE_DATABASE_GENERIC && acctype == DatabaseMeta.TYPE_ACCESS_NATIVE);
            wURL.setEnabled(          dbtype==DatabaseMeta.TYPE_DATABASE_GENERIC && acctype == DatabaseMeta.TYPE_ACCESS_NATIVE);
            wlDriverClass.setEnabled( dbtype==DatabaseMeta.TYPE_DATABASE_GENERIC && acctype == DatabaseMeta.TYPE_ACCESS_NATIVE);
            wDriverClass.setEnabled(  dbtype==DatabaseMeta.TYPE_DATABASE_GENERIC && acctype == DatabaseMeta.TYPE_ACCESS_NATIVE);
  		}
        
        
	}
	
	public void setPortNumber()
	{
		String type = DatabaseMeta.getDBTypeDesc(wConnType.getSelectionIndex()+1);
		
		// What port should we select?
		String acce = wConnAcc.getItem(wConnAcc.getSelectionIndex());
		int port=DatabaseMeta.getPortForDBType(type, acce);
		if (port<0) wPort.setText("");
		else wPort.setText(""+port);
	}

	public void setAccessList()
	{
		if (wConnType.getSelectionCount()<1) return;
		
		int acc[] = DatabaseMeta.getAccessTypeList(wConnType.getSelection()[0]);
		wConnAcc.removeAll();
		for (int i=0;i<acc.length;i++)
		{
			wConnAcc.add( DatabaseMeta.getAccessTypeDescLong(acc[i]) );
		}
		// If nothing is selected: select the first item (mostly the native driver)
		if (wConnAcc.getSelectionIndex()<0) 
		{
			wConnAcc.select(0);
		}
	}
	
	private void cancel()
	{
		connectionName=null;
		connection.setChanged(changed);
		dispose();
	}
	
	public void getInfo(DatabaseMeta info)
		throws KettleException
	{
		// Name:
		info.setName(wConn.getText());
		
		// Connection type:
		String contype[] = wConnType.getSelection();
		if (contype.length>0)
		{
			info.setDatabaseType( contype[0] );
		}
		
		// Access type:
		String acctype[] = wConnAcc.getSelection();
		if (acctype.length>0)
		{
			info.setAccessType( DatabaseMeta.getAccessType(acctype[0]) );
		}
		
		// Hostname
		info.setHostname( wHostName.getText() );
		
		// Database name
		info.setDBName( wDBName.getText() );
		
		// Port number
		info.setDBPort( Const.toInt( Const.trim(wPort.getText() ), 0) );
		
		// Username
		info.setUsername( wUsername.getText() );
		
		// Password
		info.setPassword( wPassword.getText() );
		
		// Servername
		info.setServername( wServername.getText() );
		
		// Data tablespace
		info.setDataTablespace( wData.getText() );
		
		// Index tablespace
		info.setIndexTablespace( wIndex.getText() );
		
        // SAP Attributes...
        info.getAttributes().put("SAPLanguage",     wSAPLanguage.getText());
        info.getAttributes().put("SAPSystemNumber", wSAPSystemNumber.getText());
        info.getAttributes().put("SAPClient",       wSAPClient.getText());

        // Generic settings...
        info.getAttributes().put(GenericDatabaseMeta.ATRRIBUTE_CUSTOM_URL,          wURL.getText());
        info.getAttributes().put(GenericDatabaseMeta.ATRRIBUTE_CUSTOM_DRIVER_CLASS, wDriverClass.getText());

        String[] remarks = info.checkParameters(); 
		if (remarks.length!=0)
		{
            String message = "";
            for (int i=0;i<remarks.length;i++) message+="    * "+remarks[i]+Const.CR;
			throw new KettleException("Incorrect database paramater(s)!  Check these settings :"+Const.CR+message);
		}
	}
	
	public void handleOK()
	{
		try
		{
			getInfo(connection);
			connectionName = connection.getName(); 
            dispose();
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, props, "Error!", "Please make sure all required parameters are entered correctly!", e);
		}
	}
	
	public String NVL(String str, String rep)
	{
		if (str==null) return rep;
		return str;
	}
	
	public void test()
	{
		try
		{
			System.out.println("Creating new database info object");
			DatabaseMeta dbinfo=new DatabaseMeta();
			System.out.println("Getting info");
			getInfo(dbinfo);
			test(shell, dbinfo, props);
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, props, "Error", "Unable to get the connection information", e);
		}		
	}
	/**
	 *  Test the database connection
	 */
	public static final void test(Shell shell, DatabaseMeta dbinfo, Props props)
	{
		System.out.println("Checking parameters");
        String[] remarks = dbinfo.checkParameters(); 
		if (remarks.length==0)
		{
			System.out.println("Creating database connection");
			Database db = new Database(dbinfo);
			try
			{
				System.out.println("Connecting to database");
				db.connect();

				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
				mb.setText("Connected!");
				mb.setMessage("OK!" + Const.CR);
				mb.open();
			}
			catch (KettleException e)
			{
				// e.printStackTrace();

				new ErrorDialog(shell, props, "Error!", "An error occurred connecting to the database: ", e);
			}
			finally
			{
				db.disconnect();
			}
		}
		else
		{
            String message = "";
            for (int i=0;i<remarks.length;i++) message+="    * "+remarks[i]+Const.CR;

			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			mb.setText("Error!");
			mb.setMessage("Please make sure all required parameters are entered correctly: " + Const.CR+remarks);
			mb.open();
		}
	}
	
	public void explore()
	{
		DatabaseMeta dbinfo = new DatabaseMeta();
		try
		{
			getInfo(dbinfo);
			DatabaseExplorerDialog ded = new DatabaseExplorerDialog(shell, props, SWT.NONE, dbinfo, databases, true );
			ded.open();
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, props, "Error!", "Please make sure all required parameters are entered correctly!", e);
		}
	}
    
    public void showFeatureList()
    {
        DatabaseMeta dbinfo = new DatabaseMeta();
        try
        {
            getInfo(dbinfo);
            ArrayList buffer = (ArrayList) dbinfo.getFeatureSummary();
            PreviewRowsDialog prd = new PreviewRowsDialog(shell, SWT.NONE, "Feature list", buffer);
            prd.setTitleMessage("Feature list", "The list of features:");
            prd.open();
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, props, "Error!", "Unable to get feature list.  Please make sure all required parameters are entered correctly!", e);
        }

    }
}