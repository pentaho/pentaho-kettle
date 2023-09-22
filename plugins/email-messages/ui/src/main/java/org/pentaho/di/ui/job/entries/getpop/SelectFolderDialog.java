/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.job.entries.getpop;

import javax.mail.Folder;
import javax.mail.MessagingException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entries.getpop.JobEntryGetPOP;
import org.pentaho.di.job.entries.getpop.MailConnectionMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * This dialog represents an explorer type of interface on a given IMAP connection. It shows the folders defined
 *
 * @author Samatar
 * @since 12-08-2009
 *
 */

public class SelectFolderDialog extends Dialog {
  private static Class<?> PKG = JobEntryGetPOP.class; // for i18n purposes, needed by Translator2!!

  private PropsUI props;
  private Shell shell;

  private Tree wTree;
  private TreeItem tiTree;
  private Button wOK;
  private Button wRefresh;
  private Button wCancel;
  private String selection;
  private Folder folder;
  private GUIResource guiresource = GUIResource.getInstance();

  public SelectFolderDialog( Shell parent, int style, Folder folder ) {
    super( parent, style );
    this.props = PropsUI.getInstance();
    this.folder = folder;
    this.selection = null;
  }

  public String open() {

    Shell parent = getParent();
    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    shell.setText( BaseMessages.getString( PKG, "SelectFolderDialog.Dialog.Main.Title" ) );
    shell.setImage( guiresource.getImageSpoon() );
    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );

    // Tree
    wTree = new Tree( shell, SWT.SINGLE | SWT.BORDER );
    props.setLook( wTree );

    if ( !getData() ) {
      return null;
    }

    // Buttons
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );

    wRefresh = new Button( shell, SWT.PUSH );
    wRefresh.setText( BaseMessages.getString( PKG, "System.Button.Refresh" ) );

    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    FormData fdTree = new FormData();
    fdTree.left = new FormAttachment( 0, 0 ); // To the right of the label
    fdTree.top = new FormAttachment( 0, 0 );
    fdTree.right = new FormAttachment( 100, 0 );
    fdTree.bottom = new FormAttachment( 100, -50 );
    wTree.setLayoutData( fdTree );

    BaseStepDialog.positionBottomButtons( shell, new Button[] { wOK, wRefresh, wCancel }, Const.MARGIN, null );

    // Add listeners
    wCancel.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event e ) {
        dispose();
      }
    } );

    // Add listeners
    wOK.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event e ) {
        handleOK();
      }
    } );

    wTree.addSelectionListener( new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent arg0 ) {
        handleOK();
      }
    } );

    wRefresh.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event e ) {
        getData();
      }
    } );

    BaseStepDialog.setSize( shell );

    shell.open();
    Display display = parent.getDisplay();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return selection;
  }

  private boolean getData() {
    // Clear the tree top entry
    if ( tiTree != null && !tiTree.isDisposed() ) {
      tiTree.dispose();
    }
    wTree.removeAll();
    try {
      buildFoldersTree( this.folder, null, true );
    } catch ( Exception e ) {
      return false;
    }

    return true;
  }

  private void buildFoldersTree( Folder folder, TreeItem parentTreeItem, boolean topfolder ) throws MessagingException {
    if ( ( folder.getType() & Folder.HOLDS_FOLDERS ) != 0 ) {
      Folder[] f = folder.list();
      for ( int i = 0; i < f.length; i++ ) {
        tiTree = topfolder ? new TreeItem( wTree, SWT.NONE ) : new TreeItem( parentTreeItem, SWT.NONE );
        tiTree.setImage( guiresource.getImageBol() );
        tiTree.setText( f[i].getName() );
        // Search for sub folders
        if ( ( f[i].getType() & Folder.HOLDS_FOLDERS ) != 0 ) {
          buildFoldersTree( f[i], tiTree, false );
        }
      }
    }
  }

  public void dispose() {
    if ( this.folder != null ) {
      try {
        this.folder.close( false );
      } catch ( Exception e ) { /* Ignore */
      }
    }
    props.setScreen( new WindowProperty( shell ) );
    shell.dispose();
  }

  public void handleOK() {
    TreeItem[] ti = wTree.getSelection();
    if ( ti.length == 1 ) {
      TreeItem parent = ti[0].getParentItem();
      String fullpath = ti[0].getText();
      while ( parent != null ) {
        fullpath = parent.getText() + MailConnectionMeta.FOLDER_SEPARATOR + fullpath;
        parent = parent.getParentItem();
      }

      selection = fullpath;
      dispose();
    }
  }
}
