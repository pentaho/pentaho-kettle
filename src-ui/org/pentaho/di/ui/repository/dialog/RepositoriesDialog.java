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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.repository.PermissionMeta;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.ui.repository.dialog.Messages;
import org.pentaho.di.ui.repository.dialog.RepositoryDialog;
import org.pentaho.di.trans.StepLoader;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.LabelText;



/**
 * This dialog allows you to select, create or update a repository and log in to it.
 * 
 * @author Matt
 * @since 19-jun-2003
 */
public class RepositoriesDialog
{
    private LogWriter log;

    private Label wlKettle;
    private FormData fdlKettle;

    private Label wlRepository;
    private Button wnRepository, weRepository, wdRepository;
    private CCombo wRepository;
    private FormData fdlRepository, fdRepository, fdnRepository, fdeRepository, fddRepository;

    private LabelText wUsername;
    private FormData fdUsername;

    private LabelText wPassword;
    private FormData fdPassword;

    private Canvas wCanvas;
    private FormData fdCanvas;

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
    private int toolsPermissions[];
    private StepLoader steploader;
    
    public RepositoriesDialog(Display disp, int perm[], String toolName)
    {
        display = disp;
        toolsPermissions = perm;
        steploader = StepLoader.getInstance();
        this.toolName = toolName;

        shell = new Shell(disp, SWT.DIALOG_TRIM | SWT.MAX | SWT.MIN | SWT.RESIZE);
        shell.setText(Messages.getString("RepositoriesDialog.Dialog.Main.Title"));

        log = LogWriter.getInstance();
        props = PropsUI.getInstance();
        input = new RepositoriesMeta(log);
        repinfo = null;
        userinfo = null;
        cancelled = false;

        input.readData();
    }

    public void setRepositoryName(String repname)
    {
        prefRepositoryName = repname;
    }

    public boolean open()
    {
        props.setLook(shell);

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(Messages.getString("RepositoriesDialog.Label.SelectRepository"));
        shell.setImage(GUIResource.getInstance().getImageSpoon());

        int middle = 20;
        int margin = Const.MARGIN;
        int right = 30;

        final Image logo = GUIResource.getInstance().getImagePentaho();
        final Rectangle bounds = logo.getBounds();

        wCanvas = new Canvas(shell, SWT.NO_BACKGROUND);
        fdCanvas = new FormData();
        fdCanvas.left = new FormAttachment(0, 0);
        fdCanvas.right = new FormAttachment(100, 0);
        fdCanvas.top = new FormAttachment(0, 0);
        fdCanvas.bottom = new FormAttachment(60, 0);
        wCanvas.setLayoutData(fdCanvas);
        wCanvas.setSize(bounds.width, bounds.height);

        wCanvas.addPaintListener(new PaintListener()
        {
            public void paintControl(PaintEvent pe)
            {
                Rectangle canvasBounds = wCanvas.getBounds();
                if (canvasBounds.width > 0 && canvasBounds.height > 0)
                {
                    Image image = new Image(display, canvasBounds.width, canvasBounds.height);
                    GC gc = new GC(image);
                    if (!props.isOSLookShown())
                        gc.setBackground(GUIResource.getInstance().getColorBackground());
                    gc.fillRectangle(0, 0, canvasBounds.width, canvasBounds.height);
                    if (canvasBounds.width > bounds.width && canvasBounds.height > bounds.height)
                    {
                        gc.drawImage(logo, (canvasBounds.width - bounds.width) / 2, (canvasBounds.height - bounds.height) / 2);
                    }
                    else
                    {
                        gc.drawImage(logo, 0, 0, bounds.width, bounds.height, 0, 0, canvasBounds.width, canvasBounds.height);
                    }
                    pe.gc.drawImage(image, 0, 0); // no flicker anymore!
                    gc.dispose();
                    image.dispose();
                }
            }
        });

        // Kettle welcome
        wlKettle = new Label(shell, SWT.CENTER);
        wlKettle.setText(Messages.getString("RepositoriesDialog.Label.Welcome", toolName, Const.VERSION));
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
        fdlKettle.top = new FormAttachment(wCanvas, margin);
        wlKettle.setLayoutData(fdlKettle);

        // First add the buttons at the bottom

        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(Messages.getString("System.Button.OK"));
        wNorep = new Button(shell, SWT.PUSH);
        wNorep.setText(Messages.getString("RepositoriesDialog.Button.NoRepository"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(Messages.getString("System.Button.Cancel"));

        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel , wNorep}, Const.MARGIN, null);

        // Don't show this dialog at startup...
        //
        wShow = new Button(shell, SWT.CHECK);
        wShow.setText(Messages.getString("RepositoriesDialog.Button.Show"));
        wShow.setToolTipText(Messages.getString("RepositoriesDialog.Button.Show.ToolTip"));
        props.setLook(wShow);
        fdShow = new FormData();
        // fdHide.left  = new FormAttachment(wOK, 0);
        fdShow.right = new FormAttachment(100, -right);
        fdShow.bottom = new FormAttachment(wOK, -margin*3);
        wShow.setLayoutData(fdShow);


        // Password
        wPassword = new LabelText(shell, Messages.getString("RepositoriesDialog.Label.Password"), Messages.getString("RepositoriesDialog.Label.Password"), middle, margin);
        props.setLook(wPassword);
        wPassword.getTextWidget().setEchoChar('*');
        fdPassword = new FormData();
        fdPassword.left = new FormAttachment(0, 0);
        fdPassword.right = new FormAttachment(100, -right);
        fdPassword.bottom = new FormAttachment(wShow, -margin);
        wPassword.setLayoutData(fdPassword);

        // Username
        wUsername = new LabelText(shell, Messages.getString("RepositoriesDialog.Label.Login"), Messages.getString("RepositoriesDialog.Label.Login"), middle, margin);
        props.setLook(wUsername);
        fdUsername = new FormData();
        fdUsername.left = new FormAttachment(0, 0);
        fdUsername.right = new FormAttachment(100, -right);
        fdUsername.bottom = new FormAttachment(wPassword, -margin);
        wUsername.setLayoutData(fdUsername);

        wRepository = new CCombo(shell, SWT.READ_ONLY | SWT.BORDER);

        // Add the Repository buttons :
        wnRepository = new Button(shell, SWT.PUSH);
        wnRepository.setText(Messages.getString("System.Button.New"));
        weRepository = new Button(shell, SWT.PUSH);
        weRepository.setText(Messages.getString("System.Button.Edit"));
        wdRepository = new Button(shell, SWT.PUSH);
        wdRepository.setText(Messages.getString("System.Button.Delete"));

        int repMargin = -margin; // -margin*3

        // Button positions...
        fddRepository = new FormData();
        fddRepository.right = new FormAttachment(100, -right);
        fddRepository.bottom = new FormAttachment(wUsername, repMargin);
        wdRepository.setLayoutData(fddRepository);

        fdeRepository = new FormData();
        fdeRepository.right = new FormAttachment(wdRepository, -margin);
        fdeRepository.bottom = new FormAttachment(wUsername, repMargin);
        weRepository.setLayoutData(fdeRepository);

        fdnRepository = new FormData();
        fdnRepository.right = new FormAttachment(weRepository, -margin);
        fdnRepository.bottom = new FormAttachment(wUsername, repMargin);
        wnRepository.setLayoutData(fdnRepository);

        props.setLook(wRepository);
        fdRepository = new FormData();
        fdRepository.left = new FormAttachment(middle, -margin+2);
        fdRepository.right = new FormAttachment(wnRepository, -margin);
        fdRepository.top = new FormAttachment(wnRepository, 0, SWT.TOP);
        fdRepository.bottom = new FormAttachment(wnRepository, 0, SWT.BOTTOM);
        wRepository.setLayoutData(fdRepository);

        // Repository selector
        wlRepository = new Label(shell, SWT.RIGHT);
        wlRepository.setText(Messages.getString("RepositoriesDialog.Label.Repository"));
        props.setLook(wlRepository);
        fdlRepository = new FormData();
        fdlRepository.left = new FormAttachment(0, 0);
        fdlRepository.right = new FormAttachment(middle, -margin);
        fdlRepository.bottom = new FormAttachment(wnRepository, 0, SWT.CENTER);
        wlRepository.setLayoutData(fdlRepository);

        // Add the listeners
        // New repository
        wnRepository.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
                RepositoryMeta ri = new RepositoryMeta();
                RepositoryDialog dd = new RepositoryDialog(shell, SWT.APPLICATION_MODAL, log, props, ri, input, steploader);
                if (dd.open() != null)
                {
                    input.addRepository(ri);
                    fillRepositories();
                    int idx = input.indexOfRepository(ri);
                    wRepository.select(idx);
                }
            }
        });

        // Edit repository
        weRepository.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
                RepositoryMeta ri = input.searchRepository(wRepository.getText());
                if (ri != null)
                {
                    RepositoryDialog dd = new RepositoryDialog(shell, SWT.APPLICATION_MODAL, log, props, ri, input, steploader);
                    if (dd.open() != null)
                    {
                        fillRepositories();
                        int idx = input.indexOfRepository(ri);
                        wRepository.select(idx);
                    }
                }
            }
        });

        // Delete connection
        wdRepository.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
                RepositoryMeta ri = input.searchRepository(wRepository.getText());
                if (ri != null)
                {
                    int idx = input.indexOfRepository(ri);
                    input.removeRepository(idx);
                    fillRepositories();
                }
            }
        });

        wRepository.addTraverseListener(new TraverseListener()
        {
            public void keyTraversed(TraverseEvent e)
            {
                wUsername.setFocus();
                e.doit = false;
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

        getData();

        shell.layout();

        BaseStepDialog.setMinimalShellHeight(shell, new Control[] { wCanvas, wlKettle, wRepository, wUsername, wPassword, wOK }, margin, 3 * margin);
        Rectangle dialogBounds = shell.getBounds();

        if (!Const.isOSX())
        {
        	shell.setSize(bounds.width + 20, dialogBounds.height);
        }
        else
        {
        	shell.setSize(bounds.width + 20, dialogBounds.height+50);
        }

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

                // OK, now try the username and password
                Repository rep = new Repository(log, repinfo, userinfo);

                try
                {
                    rep.connect(getClass().getName());
                }
                catch (KettleException ke)
                {
                    MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
                    mb.setMessage(Messages.getString("RepositoriesDialog.Dialog.RepositoryUnableToConnect.Message1") + Const.CR + ke.getSuperMessage());
                    mb.setText(Messages.getString("RepositoriesDialog.Dialog.RepositoryUnableToConnect.Title"));
                    mb.open();

                    return;
                }

                try
                {
                    userinfo = new UserInfo(rep, wUsername.getText(), wPassword.getText());
                    props.setLastRepository(repinfo.getName());
                    props.setLastRepositoryLogin(wUsername.getText());
                }
                catch (KettleException e)
                {
                    userinfo = null;
                    repinfo = null;

                    if (!(e instanceof KettleDatabaseException))
                    {
                        new ErrorDialog(shell, Messages.getString("RepositoriesDialog.Dialog.UnexpectedError.Title"), Messages.getString("RepositoriesDialog.Dialog.UnexpectedError.Message"), e);
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
                mb.setMessage(Messages.getString("RepositoriesDialog.Dialog.PleaseSelectARepsitory.Message"));
                mb.setText(Messages.getString("RepositoriesDialog.Dialog.PleaseSelectARepsitory.Title"));
                mb.open();

                return;
            }
        }

        input.writeData(); // Save changes to disk!

        if (userinfo == null)
        {
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
            mb.setMessage(Messages.getString("RepositoriesDialog.Dialog.IncorrectUserPassword.Message"));
            mb.setText(Messages.getString("RepositoriesDialog.Dialog.IncorrectUserPassword.Title"));
            mb.open();
        }
        else
        {
            // Check the permissions of the user
            boolean ok = true;
            String mess = "";
            for (int i = 0; i < toolsPermissions.length; i++)
            {
                switch (toolsPermissions[i])
                {
                    case PermissionMeta.TYPE_PERMISSION_TRANSFORMATION:
                        ok = ok && userinfo.useTransformations();
                        mess += mess.length() > 0 ? ", " : "";
                        mess += "Spoon";
                        break;
                    case PermissionMeta.TYPE_PERMISSION_SCHEMA:
                        ok = ok && userinfo.useSchemas();
                        mess += mess.length() > 0 ? ", " : "";
                        mess += "Menu";
                        break;
                    case PermissionMeta.TYPE_PERMISSION_JOB:
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
                mb.setMessage(Messages.getString("RepositoriesDialog.Dialog.NoPermissions.Message") + mess);
                mb.setText(Messages.getString("RepositoriesDialog.Dialog.NoPermissions.Title"));
                mb.open();

                userinfo = null;
                repinfo = null;
            }
            else
            {
                dispose();
            }
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

    public RepositoryMeta getRepository()
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
