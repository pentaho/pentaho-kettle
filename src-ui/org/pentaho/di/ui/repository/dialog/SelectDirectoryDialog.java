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
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.ui.repository.RepositoryDirectoryUI;
import org.pentaho.di.ui.repository.dialog.Messages;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.EnterStringDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;


/**
 * This dialog represents an explorer type of interface on a given database connection. It shows the
 * tables defined in the visible schemas or catalogs on that connection. The interface also allows
 * you to get all kinds of information on those tables.
 * 
 * @author Matt
 * @since 18-05-2003
 * 
 */
public class SelectDirectoryDialog extends Dialog
{
    private PropsUI props;
    private Repository rep;
    private Shell shell;

    private Tree wTree;
    private TreeItem tiTree;
    private Button wOK;
    private Button wRefresh;
    private Button wCancel;
    private RepositoryDirectory selection;
    private Color dircolor;

    /**
     * @deprecated Use CT without <i>log</i> and <i>props</i> parameters
     */
    public SelectDirectoryDialog(Shell parent, PropsUI props, int style, LogWriter log, Repository rep)
    {
        this(parent, style, rep);
        this.props = props;
    }
    
    public SelectDirectoryDialog(Shell parent, int style, Repository rep)
    {
        super(parent, style);
        this.props = PropsUI.getInstance();
        this.rep = rep;

        selection = null;
    }

    public RepositoryDirectory open()
    {
        dircolor = GUIResource.getInstance().getColorDirectory();

        Shell parent = getParent();
        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
        props.setLook(shell);
		shell.setImage(GUIResource.getInstance().getImageConnection());
        shell.setText(Messages.getString("SelectDirectoryDialog.Dialog.Main.Title"));

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);

        // Tree
        wTree = new Tree(shell, SWT.SINGLE | SWT.BORDER);
        props.setLook(wTree);

        try
        {
            rep.refreshRepositoryDirectoryTree();
        }
        catch (KettleException e)
        {
            new ErrorDialog(shell,
                Messages.getString("SelectDirectoryDialog.Dialog.ErrorRefreshingDirectoryTree.Title"),
                Messages.getString("SelectDirectoryDialog.Dialog.ErrorRefreshingDirectoryTree.Message"), e);
            return null;
        }

        if (!getData())
            return null;

        // Buttons
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(Messages.getString("System.Button.OK"));

        wRefresh = new Button(shell, SWT.PUSH);
        wRefresh.setText(Messages.getString("System.Button.Refresh"));

        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(Messages.getString("System.Button.Cancel"));

        FormData fdTree = new FormData();
        FormData fdOK = new FormData();
        FormData fdRefresh = new FormData();
        FormData fdCancel = new FormData();

        int margin = 10;

        fdTree.left = new FormAttachment(0, 0); // To the right of the label
        fdTree.top = new FormAttachment(0, 0);
        fdTree.right = new FormAttachment(100, 0);
        fdTree.bottom = new FormAttachment(100, -50);
        wTree.setLayoutData(fdTree);

        fdOK.left = new FormAttachment(wTree, 0, SWT.CENTER);
        fdOK.bottom = new FormAttachment(100, -margin);
        wOK.setLayoutData(fdOK);

        fdRefresh.left = new FormAttachment(wOK, 10);
        fdRefresh.bottom = new FormAttachment(100, -margin);
        wRefresh.setLayoutData(fdRefresh);

        fdCancel.left = new FormAttachment(wRefresh, 10);
        fdCancel.bottom = new FormAttachment(100, -margin);
        wCancel.setLayoutData(fdCancel);

        // Add listeners
        wCancel.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event e)
            {
                dispose();
            }
        });

        // Add listeners
        wOK.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event e)
            {
                handleOK();
            }
        });

        wTree.addSelectionListener(new SelectionAdapter()
        {
            public void widgetDefaultSelected(SelectionEvent arg0)
            {
                handleOK();
            }
        });

        wRefresh.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event e)
            {
                getData();
            }
        });

        wTree.addMouseListener(new MouseAdapter()
        {
            public void mouseDown(MouseEvent e)
            {
                if (e.button == 3) // right click!
                {
                    setTreeMenu();
                }
            }
        });

        BaseStepDialog.setSize(shell);

        shell.open();
        Display display = parent.getDisplay();
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return selection;
    }

    private boolean getData()
    {
        // Clear the tree top entry
        if (tiTree != null && !tiTree.isDisposed())
            tiTree.dispose();

        tiTree = new TreeItem(wTree, SWT.NONE);
        RepositoryDirectoryUI.getDirectoryTree(tiTree, dircolor, rep.getDirectoryTree());
        tiTree.setExpanded(true);

        return true;
    }

    public void setTreeMenu()
    {
        Menu mTree = null;

        TreeItem ti[] = wTree.getSelection(); // use SWT.SINGLE in wTree!!!!
        if (ti.length == 1)
        {
            mTree = new Menu(wTree);

            /*
             * NEW Sub-directory
             */
            MenuItem miNew = new MenuItem(mTree, SWT.CASCADE);
            miNew.setText(Messages.getString("SelectDirectoryDialog.PopupMenu.Directory.New"));
            miNew.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    if (!rep.getUserInfo().isReadonly())
                    {
                        TreeItem ti = wTree.getSelection()[0];
                        String str[] = ConstUI.getTreeStrings(ti);
                        //
                        // In which directory do we want create a subdirectory?
                        //
                        RepositoryDirectory dir = rep.getDirectoryTree().findDirectory(str);
                        if (dir != null)
                        {
                            //
                            // What's the name of the new directory?
                            //
                            EnterStringDialog etd = new EnterStringDialog(shell,
                                Messages.getString("SelectDirectoryDialog.Dialog.EnterDirectoryName.Title"),
                                Messages.getString("SelectDirectoryDialog.Dialog.EnterDirectoryName.Message"),
                                Messages.getString("SelectDirectoryDialog.Dialog.EnterDirectoryName.Default"));
                            String newdir = etd.open();
                            if (newdir != null)
                            {
                                RepositoryDirectory subdir = new RepositoryDirectory(dir, newdir);
                                if (subdir.addToRep(rep))
                                {
                                    dir.addSubdirectory(subdir);
                                    TreeItem tiNew = new TreeItem(ti, SWT.NONE);
                                    tiNew.setText(newdir);
                                    wTree.setSelection(new TreeItem[] { tiNew });
                                }
                                else
                                {
                                    MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
                                    mb.setMessage(Messages.getString("SelectDirectoryDialog.Dialog.UnableToCreateDirectory.Message"));
                                    mb.setText(Messages.getString("SelectDirectoryDialog.Dialog.UnableToCreateDirectory.Title"));
                                    mb.open();
                                }
                            }
                        }
                        else
                        {
                            MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
                            mb.setMessage(Messages.getString("SelectDirectoryDialog.Dialog.UnableToLocateDirectory.Message"));
                            mb.setText(Messages.getString("SelectDirectoryDialog.Dialog.UnableToLocateDirectory.Title"));
                            mb.open();
                        }
                    }
                    else
                    {
                        MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
                        mb.setMessage(Messages.getString("SelectDirectoryDialog.Dialog.PermissionDenied.Message1") + rep.getUserInfo().getLogin() + Messages.getString("SelectDirectoryDialog.Dialog.PermissionDenied.Message2"));
                        mb.setText(Messages.getString("SelectDirectoryDialog.Dialog.PermissionDenied.Title"));
                        mb.open();
                    }
                }
            });

            /*
             * RENAME directory
             */
            MenuItem miRen = new MenuItem(mTree, SWT.CASCADE);
            miRen.setText(Messages.getString("SelectDirectoryDialog.PopupMenu.Directory.Rename"));
            MenuItem miDel = new MenuItem(mTree, SWT.CASCADE);
            miDel.setText(Messages.getString("SelectDirectoryDialog.PopupMenu.Directory.Delete"));
        }
        wTree.setMenu(mTree);
    }

    public void dispose()
    {
        props.setScreen(new WindowProperty(shell));
        shell.dispose();
    }

    public void handleOK()
    {
        TreeItem ti[] = wTree.getSelection();
        if (ti.length == 1)
        {
            String tree[] = ConstUI.getTreeStrings(ti[0]);
            selection = rep.getDirectoryTree().findDirectory(tree);
            dispose();
        }
    }

    public String toString()
    {
        return this.getClass().getName();
    }

}
