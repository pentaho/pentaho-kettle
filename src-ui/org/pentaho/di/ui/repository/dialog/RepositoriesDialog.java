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

package org.pentaho.di.ui.repository.dialog;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ProfileMeta;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryCapabilities;
import org.pentaho.di.repository.RepositoryLoader;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryPluginMeta;
import org.pentaho.di.repository.RoleInfo;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.repository.ProfileMeta.Permission;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.gui.XulHelper;
import org.pentaho.di.ui.core.widget.LabelText;
import org.pentaho.di.ui.spoon.XulMessages;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.xul.swt.toolbar.Toolbar;



/**
 * This dialog allows you to select, create or update a repository and log in to it.
 * 
 * @author Matt
 * @since 19-jun-2003
 */
public class RepositoriesDialog
{
	private static Class<?> PKG = KettleDatabaseRepositoryDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private static final String XUL_FILE_TOOLBAR = "ui/repository-login-toolbar.xul";
	private static final String XUL_FILE_TOOLBAR_PROPERTIES = "ui/repository-login-toolbar.properties";

    private Label wlKettle;
    private FormData fdlKettle;

   private org.eclipse.swt.widgets.List wRepository;
    private FormData fdRepository;

    private LabelText wUsername;
    private FormData fdUsername;

    private LabelText wPassword;
    private FormData fdPassword;

    
    private Label     imageLabel;
    
    private LabelText wRepName;
    private FormData fdRepName;
    private LabelText wRepDesc;
    private FormData fdRepDesc;
    private LabelText wRepTypeName;
    private FormData fdRepType;

    
    
    private Button wOK, wNorep, wCancel;
    private Listener lsOK, lsNorep, lsCancel;
    
    private Button wShow;
    private FormData fdShow;

    private SelectionListener lsDef;
    private KeyListener lsRepo, lsJmp;

    private Display display;
    private Shell shell;
    private PropsUI props;
    private RepositoriesMeta input;
    private RepositoryMeta repinfo;
    private UserInfo userinfo;
    private String prefRepositoryName;
    private boolean cancelled;
    private String toolName;
    private Permission[] toolsPermissions;

	private Toolbar	toolbar;

	private ToolBar	swtToolBar;
    
    public RepositoriesDialog(Display disp, String toolName, Permission...permissions)
    {
        display = disp;
        toolsPermissions = permissions;
        this.toolName = toolName;

        shell = new Shell(disp, SWT.DIALOG_TRIM | SWT.MAX | SWT.MIN | SWT.RESIZE);
        shell.setText(BaseMessages.getString(PKG, "RepositoriesDialog.Dialog.Main.Title"));

        props = PropsUI.getInstance();
        input = new RepositoriesMeta();
        repinfo = null;
        userinfo = null;
        cancelled = false;

        try {
        	input.readData();
        } catch(Exception e) {
        	new ErrorDialog(shell, "Error", "Unexpected error reading repository definitions", e);
        }
    }

    public void setRepositoryName(String repname)
    {
        prefRepositoryName = repname;
    }

    public boolean open()
    {
        props.setLook(shell);

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN*2;
        formLayout.marginHeight = Const.FORM_MARGIN*2;

        shell.setLayout(formLayout);
        shell.setText(BaseMessages.getString(PKG, "RepositoriesDialog.Label.SelectRepository"));
        shell.setImage(GUIResource.getInstance().getImageSpoon());

        int middle = 20;
        int margin = Const.MARGIN;
        int right = 30;

        // Kettle welcome
        wlKettle = new Label(shell, SWT.CENTER);
        wlKettle.setText(BaseMessages.getString(PKG, "RepositoriesDialog.Label.Welcome", toolName, Const.VERSION));
        props.setLook(wlKettle);
        final Font f = new Font(shell.getDisplay(), "Arial", 18, SWT.NORMAL);
        wlKettle.addDisposeListener(new DisposeListener()
        {
            public void widgetDisposed(DisposeEvent e)
            {
                f.dispose();
            }
        });
        wlKettle.setFont(f);
        fdlKettle = new FormData();
        fdlKettle.left = new FormAttachment(0, 0);
        fdlKettle.right = new FormAttachment(100, -right);
        fdlKettle.top = new FormAttachment(0, 0);
        wlKettle.setLayoutData(fdlKettle);
        
        Label line = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
        FormData fdLine = new FormData();
        fdLine.left = new FormAttachment(0,0);
        fdLine.right = new FormAttachment(100,0);
        fdLine.top = new FormAttachment(wlKettle,margin);
        line.setLayoutData(fdLine);

        // Create a toolbar for the repository mgt buttons
        //
        addToolBar();
        addToolBarListeners();

        props.setLook(swtToolBar);
        FormData fdToolbar = new FormData();
        fdToolbar.left = new FormAttachment(0,0);
        // fdToolbar.right = new FormAttachment(middle,-margin);
        fdToolbar.top = new FormAttachment(line, 3*margin);
        swtToolBar.setLayoutData(fdToolbar);
        
        wRepository = new org.eclipse.swt.widgets.List(shell, SWT.READ_ONLY | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE);
        props.setLook(wRepository);
        fdRepository = new FormData();
        fdRepository.left = new FormAttachment(0, 0);
        fdRepository.right = new FormAttachment(middle, -margin);
        fdRepository.top = new FormAttachment(swtToolBar, margin);
        fdRepository.bottom = new FormAttachment(100, -50);
        wRepository.setLayoutData(fdRepository);
        
        // Repository name
        wRepName = new LabelText(shell, SWT.SINGLE | SWT.LEFT, BaseMessages.getString(PKG, "RepositoriesDialog.Label.RepName"), BaseMessages.getString(PKG, "RepositoriesDialog.Label.RepName"), middle, margin);
        props.setLook(wRepName);
        fdRepName = new FormData();
        fdRepName.left = new FormAttachment(middle, 2*margin);
        fdRepName.right = new FormAttachment(100, -right);
        fdRepName.top = new FormAttachment(line, 3*margin);
        wRepName.setLayoutData(fdRepName);
        wRepName.getTextWidget().setEditable(false);
        
        // Repository Description
        wRepDesc = new LabelText(shell, SWT.SINGLE | SWT.LEFT, BaseMessages.getString(PKG, "RepositoriesDialog.Label.RepDesc"), BaseMessages.getString(PKG, "RepositoriesDialog.Label.RepDesc"), middle, margin);
        props.setLook(wRepDesc);
        fdRepDesc = new FormData();
        fdRepDesc.left = new FormAttachment(middle, 2*margin);
        fdRepDesc.right = new FormAttachment(100, -right);
        fdRepDesc.top   = new FormAttachment(wRepName, margin);
        wRepDesc.setLayoutData(fdRepDesc);
        wRepDesc.getTextWidget().setEditable(false);
        
        // Repository type
        wRepTypeName = new LabelText(shell, SWT.SINGLE | SWT.LEFT, BaseMessages.getString(PKG, "RepositoriesDialog.Label.RepType"), BaseMessages.getString(PKG, "RepositoriesDialog.Label.RepType"), middle, margin);
        props.setLook(wRepTypeName);
        fdRepType = new FormData();
        fdRepType.left = new FormAttachment(middle, 2*margin);
        fdRepType.right = new FormAttachment(100, -right);
        fdRepType.top   = new FormAttachment(wRepDesc, margin);
        wRepTypeName.setLayoutData(fdRepType);
        wRepTypeName.getTextWidget().setEditable(false);
        
        imageLabel = new Label(shell, SWT.NONE);
        imageLabel.setImage(GUIResource.getInstance().getImagePentaho());
        FormData fdImageLabel = new FormData();
        fdImageLabel.left = new FormAttachment(middle, 2*margin);
        fdImageLabel.top = new FormAttachment(wRepTypeName, margin*3);
        imageLabel.setLayoutData(fdImageLabel);

        // Username
        wUsername = new LabelText(shell, BaseMessages.getString(PKG, "RepositoriesDialog.Label.Login"), BaseMessages.getString(PKG, "RepositoriesDialog.Label.Login"), middle, margin);
        props.setLook(wUsername);
        fdUsername = new FormData();
        fdUsername.left = new FormAttachment(middle, margin);
        fdUsername.right = new FormAttachment(100, -right);
        fdUsername.top = new FormAttachment(imageLabel, margin*3);
        wUsername.setLayoutData(fdUsername);

        // Password
        wPassword = new LabelText(shell, BaseMessages.getString(PKG, "RepositoriesDialog.Label.Password"), BaseMessages.getString(PKG, "RepositoriesDialog.Label.Password"), middle, margin);
        props.setLook(wPassword);
        wPassword.getTextWidget().setEchoChar('*');
        fdPassword = new FormData();
        fdPassword.left = new FormAttachment(middle, margin);
        fdPassword.right = new FormAttachment(100, -right);
        fdPassword.top = new FormAttachment(wUsername, margin);
        wPassword.setLayoutData(fdPassword);

        // Don't show this dialog at startup...
        //
        wShow = new Button(shell, SWT.CHECK);
        wShow.setText(BaseMessages.getString(PKG, "RepositoriesDialog.Button.Show"));
        wShow.setToolTipText(BaseMessages.getString(PKG, "RepositoriesDialog.Button.Show.ToolTip"));
        props.setLook(wShow);
        fdShow = new FormData();
        // fdHide.left  = new FormAttachment(wOK, 0);
        fdShow.right = new FormAttachment(100, -right);
        fdShow.top = new FormAttachment(wPassword, margin*3);
        wShow.setLayoutData(fdShow);

        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        wNorep = new Button(shell, SWT.PUSH);
        wNorep.setText(BaseMessages.getString(PKG, "RepositoriesDialog.Button.NoRepository"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel , wNorep}, Const.MARGIN, wShow);

        wRepository.addTraverseListener(new TraverseListener()
        {
            public void keyTraversed(TraverseEvent e)
            {
                wUsername.setFocus();
                e.doit = false;
            }
        });
        
        wRepository.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent event) {
        		// Someone clicked on a repository name. 
        		// Show the selected details to the right...
        		//
        		showSelectedRepository();
        		wUsername.setFocus();
        	}
		});

        // Add listeners
        lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
        lsNorep    = new Listener() { public void handleEvent(Event e) { norep(); } };
        lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };

        wOK.addListener(SWT.Selection, lsOK);
        wNorep.addListener(SWT.Selection, lsNorep);
        wCancel.addListener(SWT.Selection, lsCancel);

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener( new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

        // Clean up used resources!

        lsDef = new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
        lsRepo = new KeyAdapter()      { public void keyPressed(KeyEvent e) { if (e.character == SWT.CR) wUsername.setFocus(); } };
        lsJmp = new KeyAdapter()
        {
            public void keyPressed(KeyEvent e)
            {
                if (e.character == SWT.CR)
                {
                    if (wPassword.getText().length() != 0)
                        ok();
                    else
                    {
                        wPassword.getTextWidget().setFocus();
                        wPassword.getTextWidget().selectAll();
                    }
                }
            }
        };

        wRepository.addKeyListener(lsRepo);
        wUsername.getTextWidget().addKeyListener(lsJmp);
        wPassword.getTextWidget().addSelectionListener(lsDef);
        wRepository.addSelectionListener(new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); }});

        getData();

        shell.layout();
        shell.pack();

        /*
        BaseStepDialog.setMinimalShellHeight(shell, new Control[] { wlKettle, imageLabel, weRepository, wRepository, wUsername, wPassword, wOK }, margin, 3 * margin);
        Rectangle dialogBounds = shell.getBounds();

        if (!Const.isOSX())
        {
        	shell.setSize(dialogBounds.width + 18, dialogBounds.height+40);
        }
        else
        {
        	shell.setSize(dialogBounds.width + 20, dialogBounds.height+50);
        }
        */
        
        shell.setTabList(new Control[] { wUsername, wPassword, wOK, wCancel, wNorep, wRepository, });

        // MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
        // mb.setText("Warning");
        // mb.setMessage("Developers & beta-testers beware: you need to upgrade your repository because we are making additional last-minute changes to the repository.\n\n\nThank you for your understanding and help,\n\nMatt\n");
        // mb.open();

        shell.open();
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch()) display.sleep();
        }
        return repinfo != null;
    }
    
    public void newRepository() {
    	List<RepositoryPluginMeta> pluginMetaList = RepositoryLoader.getInstance().getPluginMetaList();
    	String[] names = new String[pluginMetaList.size()];
    	for (int i=0;i<names.length;i++) { 
    		RepositoryPluginMeta meta = pluginMetaList.get(i);
    		names[i] = meta.getName() + " : " + meta.getDescription(); 
    	}
    	
    	// TODO: make this a bit fancier!
    	//
    	EnterSelectionDialog selectRepositoryType = new EnterSelectionDialog(shell, names, "Select the repository type", "Select the repository type to create");
    	String choice = selectRepositoryType.open();
    	if (choice!=null) {
    		int index = selectRepositoryType.getSelectionNr();
    		RepositoryPluginMeta pluginMeta = pluginMetaList.get(index);
    		String id = pluginMeta.getId();
    		
    		try {
        		// With this ID we can create a new Repository object...
        		//
        		RepositoryMeta repositoryMeta = RepositoryLoader.createRepositoryMeta(id);
        		RepositoryDialogInterface dialog = getRepositoryDialog(pluginMeta, repositoryMeta, input);
        		RepositoryMeta meta = dialog.open();
        		if (meta!=null) {
                    input.addRepository(meta);
                    fillRepositories();
                    int idx = input.indexOfRepository(meta);
                    wRepository.select(idx);
                }
    		}
    		catch(Exception e) {
    			new ErrorDialog(shell, "Error", "Error creating new repository", e);
    		}
    	}
    }
    
    public void editRepository() {
    	try {
    		int index = wRepository.getSelectionIndex();
    		if (index>=0) {
    			String name = wRepository.getItem(index);
                RepositoryMeta ri = input.searchRepository(name);
                if (ri != null)
                {
                	RepositoryPluginMeta pluginMeta = RepositoryLoader.getInstance().findPluginMeta(ri.getId());
        	    	if (pluginMeta==null) {
        	    		throw new KettleException("Unable to find repository plugin for id ["+ri.getId()+"]");
        	    	}

            		RepositoryDialogInterface dd = getRepositoryDialog(pluginMeta, ri, input);
                    if (dd.open() != null)
                    {
                        fillRepositories();
                        int idx = input.indexOfRepository(ri);
                        wRepository.select(idx);
                    }
                }
    		}
    	}
    	catch(Exception e) {
			new ErrorDialog(shell, "Error", "Error editing repository", e);
		}
    }
    
    public void deleteRepository() {
    	
		int index = wRepository.getSelectionIndex();
		if (index>=0) {
			String name = wRepository.getItem(index);
            RepositoryMeta repositoryMeta = input.searchRepository(name);
            if (repositoryMeta != null)
            {
            	MessageBox messageBox = new MessageBox(shell, SWT.YES | SWT.NO);
            	messageBox.setText(BaseMessages.getString(PKG, "RepositoriesDialog.DelRepo.AreYouSure.Title"));
            	messageBox.setMessage(BaseMessages.getString(PKG, "RepositoriesDialog.DelRepo.AreYouSure.Message"));
            	int answer = messageBox.open();
            	if (answer==SWT.YES) {
	                int idx = input.indexOfRepository(repositoryMeta);
	                input.removeRepository(idx);
	                fillRepositories();
            	}
            }
		}
    }
    
    private void addToolBar()
	{
		try {
			toolbar = XulHelper.createToolbar(XUL_FILE_TOOLBAR, shell, RepositoriesDialog.this, new XulMessages());
			
			// Add a few default key listeners
			//
			swtToolBar = (ToolBar) toolbar.getNativeObject();
			addToolBarListeners();
		} catch (Throwable t ) {
			new ErrorDialog(shell, BaseMessages.getString(PKG, "RepositoriesDialog.Exception.ErrorReadingXULFile.Title"), BaseMessages.getString(PKG, "RepositoriesDialog.Exception.ErrorReadingXULFile.Message", XUL_FILE_TOOLBAR), new Exception(t));
		}
	}

	public void addToolBarListeners()
	{
		try
		{
			// first get the XML document
			URL url = XulHelper.getAndValidate(XUL_FILE_TOOLBAR_PROPERTIES);
			Properties props = new Properties();
			props.load(url.openStream());
			String ids[] = toolbar.getMenuItemIds();
			for (int i = 0; i < ids.length; i++)
			{
				String methodName = (String) props.get(ids[i]);
				if (methodName != null)
				{
					toolbar.addMenuListener(ids[i], this, methodName);
				}
			}

		} catch (Throwable t ) {
			t.printStackTrace();
			new ErrorDialog(shell, BaseMessages.getString(PKG, "RepositoriesDialog.Exception.ErrorReadingXULFile.Title"), 
					BaseMessages.getString(PKG, "RepositoriesDialog.Exception.ErrorReadingXULFile.Message", XUL_FILE_TOOLBAR_PROPERTIES), new Exception(t));
		}
	}


    protected void showSelectedRepository() {

		wRepName.setText("");
		wRepDesc.setText("");
		wRepTypeName.setText("");

		String[] selection = wRepository.getSelection();
		if (!Const.isEmpty(selection) && selection.length==1) {
			String name = selection[0];
			RepositoryMeta meta = input.findRepository(name);
			if (meta!=null) {
				RepositoryPluginMeta pluginMeta = RepositoryLoader.getInstance().findPluginMeta(meta.getId());
				wRepName.setText(name);
				wRepDesc.setText(Const.NVL(meta.getDescription(), ""));
				wRepTypeName.setText(pluginMeta.getName());
				
				RepositoryCapabilities capabilities = meta.getRepositoryCapabilities();
				wUsername.setEnabled(capabilities.supportsUsers());
				wPassword.setEnabled(capabilities.supportsUsers());
			}
		}
	}

	protected RepositoryDialogInterface getRepositoryDialog(RepositoryPluginMeta pluginMeta, RepositoryMeta repositoryMeta, RepositoriesMeta input2) throws Exception {
		ClassLoader classLoader = RepositoryLoader.getInstance().getClassLoader(pluginMeta);
		Class<?> dialogClass = classLoader.loadClass(pluginMeta.getDialogClassName());
		Constructor<?> constructor = dialogClass.getConstructor(Shell.class, Integer.TYPE, RepositoryMeta.class, RepositoriesMeta.class);
		return (RepositoryDialogInterface) constructor.newInstance(new Object[] { shell, Integer.valueOf(SWT.NONE), repositoryMeta, input, });
	}

	public void dispose()
    {
        props.setRepositoriesDialogAtStartupShown(wShow.getSelection());
        props.setScreen(new WindowProperty(shell));
        shell.dispose();
    }

    /**
     * Copy information from the meta-data input to the dialog fields.
     */
    public void getData()
    {
        fillRepositories();

        String repname = props.getLastRepository();
        if (repname != null)
        {
            int idx = wRepository.indexOf(repname);
            if (idx >= 0)
            {
                wRepository.select(idx);
                wRepository.setFocus();

                // fillUsernames();

                String username = props.getLastRepositoryLogin();
                if (username != null)
                {
                    wUsername.setText(username);
                    wPassword.getTextWidget().setFocus();
                }
            }
        }

        // Do we have a preferred repository name to select
        if (prefRepositoryName != null)
        {
            int idx = wRepository.indexOf(prefRepositoryName);
            if (idx >= 0)
                wRepository.select(idx);
        }
        
        wShow.setSelection(props.showRepositoriesDialogAtStartup());
        
        showSelectedRepository();
    }

    private void norep()
    {
        repinfo = null;
        dispose();
    }

    private void cancel()
    {
        repinfo = null;
        cancelled = true;
        dispose();
    }

    private void ok()
    {
        if (wRepository.getItemCount() != 0)
        {
            int idx = wRepository.getSelectionIndex();

            if (idx >= 0)
            {
                repinfo = input.getRepository(idx);
                Repository rep = null;
                try
                {
                	// OK, now try the username and password
                	//
                	rep = RepositoryLoader.getInstance().createRepositoryObject(repinfo.getId());
            		userinfo = new UserInfo(wUsername.getText());
            		userinfo.setPassword(wPassword.getText());
                	
                	if (!repinfo.getRepositoryCapabilities().managesUsers()) {
                		// TODO find out where do to get appropriate permissions from
                		//
                		ProfileMeta adminProfile = new ProfileMeta("Administrator", "Administrator");
                		adminProfile.addPermission(Permission.ADMIN);
                		userinfo.setProfile(adminProfile);
                	}
                	
                	rep.init(repinfo, userinfo);
                    rep.connect();
                }
                catch (KettleException ke)
                {
                	new ErrorDialog(shell, 
                			BaseMessages.getString(PKG, "RepositoriesDialog.Dialog.RepositoryUnableToConnect.Title"),
                			BaseMessages.getString(PKG, "RepositoriesDialog.Dialog.RepositoryUnableToConnect.Message1") + Const.CR + ke.getSuperMessage(),
                			ke
                			);
                	/*
	                    MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
	                    mb.setMessage(BaseMessages.getString(PKG, "RepositoriesDialog.Dialog.RepositoryUnableToConnect.Message1") + Const.CR + ke.getSuperMessage());
	                    mb.setText(BaseMessages.getString(PKG, "RepositoriesDialog.Dialog.RepositoryUnableToConnect.Title"));
	                    mb.open();
					*/
                    return;
                }

                try
                {
					if (repinfo.getRepositoryCapabilities().managesUsers()) {
						userinfo = rep.getSecurityProvider().loadUserInfo(wUsername.getText(), wPassword.getText());
					}
                    props.setLastRepository(repinfo.getName());
                    props.setLastRepositoryLogin(wUsername.getText());
                }
                catch (KettleException e)
                {
                    userinfo = null;
                    repinfo = null;

                    if (!(e instanceof KettleDatabaseException))
                    {
                        new ErrorDialog(shell, BaseMessages.getString(PKG, "RepositoriesDialog.Dialog.UnexpectedError.Title"), BaseMessages.getString(PKG, "RepositoriesDialog.Dialog.UnexpectedError.Message"), e);
                    }
                }
                finally
                {
                    rep.disconnect();
                }
            }
            else
            {
                MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
                mb.setMessage(BaseMessages.getString(PKG, "RepositoriesDialog.Dialog.PleaseSelectARepsitory.Message"));
                mb.setText(BaseMessages.getString(PKG, "RepositoriesDialog.Dialog.PleaseSelectARepsitory.Title"));
                mb.open();

                return;
            }
        }

        try {
        	input.writeData(); // Save changes to disk!
        } catch(Exception e) {
        	new ErrorDialog(shell, "Error", "Unexpected error writing repository definitions to file", e);
        }
        
        RepositoryCapabilities capabilities = repinfo.getRepositoryCapabilities();

        if (!capabilities.supportsUsers()) {
        	dispose(); // no users support : go right ahead
        	return;
        }
        
        if (userinfo == null)
        {
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
            mb.setMessage(BaseMessages.getString(PKG, "RepositoriesDialog.Dialog.IncorrectUserPassword.Message"));
            mb.setText(BaseMessages.getString(PKG, "RepositoriesDialog.Dialog.IncorrectUserPassword.Title"));
            mb.open();
        }
        else if(userinfo.getProfile() != null) {
            boolean ok = true;
            // Check the permissions of the user
            String mess = "";
            for (int i = 0; i < toolsPermissions.length; i++)
            {
                switch (toolsPermissions[i])
                {
                    case TRANSFORMATION:
                        ok = ok && userinfo.useTransformations();
                        mess += mess.length() > 0 ? ", " : "";
                        mess += "Spoon";
                        break;
                    case SCHEMA:
                        ok = ok && userinfo.useSchemas();
                        mess += mess.length() > 0 ? ", " : "";
                        mess += "Menu";
                        break;
                    case JOB:
                        ok = ok && userinfo.useJobs();
                        mess += mess.length() > 0 ? ", " : "";
                        mess += "Chef";
                        break;
                    default:
                        break;
                }
            }

            // Sorry, you can't use all these tools...
            if (!ok)
            {
                int idx = mess.lastIndexOf(',');
                if (idx > 0)
                    mess = mess.substring(0, idx) + "and" + mess.substring(idx + 1);
                MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
                mb.setMessage(BaseMessages.getString(PKG, "RepositoriesDialog.Dialog.NoPermissions.Message") + mess);
                mb.setText(BaseMessages.getString(PKG, "RepositoriesDialog.Dialog.NoPermissions.Title"));
                mb.open();

                userinfo = null;
                repinfo = null;
            }
            else
            {
                dispose();
            }
        } else {
        	Set<RoleInfo> roleSet = userinfo.getRoles();
        	RoleInfo[] roleArray = new RoleInfo[roleSet.size()];
        	roleSet.toArray(roleArray);
        	for(int i=0;i< roleArray.length;i++) {
        		if(roleArray[i].getName().equalsIgnoreCase("Admin") || roleArray[i].getName().equalsIgnoreCase("Admin")) {
            		// TODO find out where do to get appropriate permissions from
            		//
            		ProfileMeta adminProfile = new ProfileMeta("Administrator", "Administrator");
            		adminProfile.addPermission(Permission.ADMIN);
            		userinfo.setProfile(adminProfile);
            		break;
        		}
        	}
        	dispose();
        }
    }

    public void fillRepositories()
    {
        wRepository.removeAll();
        // Fill in the available repositories...
        for (int i = 0; i < input.nrRepositories(); i++)
        {
            String name = input.getRepository(i).getName();
            if (name != null)
                wRepository.add(name);
        }
    }

    public RepositoryMeta getRepositoryMeta()
    {
        return repinfo;
    }

    public UserInfo getUser()
    {
        return userinfo;
    }

    public boolean isCancelled()
    {
        return cancelled;
    }

    public Shell getShell()
    {
        return shell;
    }
}
