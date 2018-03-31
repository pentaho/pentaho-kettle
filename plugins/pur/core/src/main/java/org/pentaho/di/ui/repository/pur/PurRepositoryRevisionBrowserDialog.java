/*!
 * Copyright 2010 - 2017 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.di.ui.repository.pur;

import java.util.List;

import org.eclipse.swt.SWT;
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
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.ShowMessageDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.repository.dialog.RepositoryRevisionBrowserDialogInterface;
import org.pentaho.di.ui.repository.pur.services.IRevisionService;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class PurRepositoryRevisionBrowserDialog implements RepositoryRevisionBrowserDialogInterface,
    java.io.Serializable {

  private static final long serialVersionUID = -1596062300653831848L; /* EESOURCE: UPDATE SERIALVERUID */
  private static Class<?> PKG = PurRepositoryRevisionBrowserDialog.class; // for i18n purposes, needed by Translator2!!
                                                                          // $NON-NLS-1$

  private Label wlFields;
  private TableView wFields;
  private FormData fdlFields, fdFields;

  private Button wOpen, wClose;
  private Listener lsOpen, lsClose;

  private Display display;
  private Shell shell;
  private PropsUI props;

  protected Repository repository;
  private IRevisionService revisionService;
  private RepositoryElementInterface element;

  private List<ObjectRevision> revisions;

  private String elementDescription;

  private String selectedRevision;

  public PurRepositoryRevisionBrowserDialog( Shell parent, int style, Repository repository,
      RepositoryElementInterface element ) {
    this.display = parent.getDisplay();
    this.props = PropsUI.getInstance();
    this.repository = repository;
    this.element = element;

    try {
      if ( repository.hasService( IRevisionService.class ) ) {
        revisionService = (IRevisionService) repository.getService( IRevisionService.class );
      } else {
        throw new IllegalStateException();
      }
    } catch ( KettleException e ) {
      throw new IllegalStateException( e );
    }
    String name = element.getRepositoryElementType().toString() + " " + element.getRepositoryDirectory().getPath();
    if ( !name.endsWith( "/" ) )
      name += "/";
    name += element.getName();

    this.elementDescription = name;

    shell = new Shell( display, style | SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    shell.setText( BaseMessages.getString( PKG, "PurRepositoryRevisionBrowserDialog.Dialog.Main.Title" ) ); //$NON-NLS-1$
  }

  public String open() {
    try {
      props.setLook( shell );

      FormLayout formLayout = new FormLayout();
      formLayout.marginWidth = Const.FORM_MARGIN;
      formLayout.marginHeight = Const.FORM_MARGIN;

      shell.setLayout( formLayout );
      shell.setImage( GUIResource.getInstance().getImageVersionBrowser() );

      // int middle = props.getMiddlePct();
      int margin = Const.MARGIN;

      revisions = revisionService.getRevisions( element );

      // Mmm, if we don't get any rows in the buffer: show a dialog box.
      if ( revisions == null || revisions.size() == 0 ) {
        ShowMessageDialog dialog =
            new ShowMessageDialog( shell, SWT.OK | SWT.ICON_WARNING, BaseMessages.getString( PKG,
                "PurRepositoryRevisionBrowserDialog.NoRevisions.Text" ), BaseMessages.getString( PKG,
                "PurRepositoryRevisionBrowserDialog.NoRevisions.Message" ) );
        dialog.open();
        shell.dispose();
        return null;
      }

      wlFields = new Label( shell, SWT.LEFT );
      wlFields.setText( "Revision history of " + elementDescription );
      props.setLook( wlFields );
      fdlFields = new FormData();
      fdlFields.left = new FormAttachment( 0, 0 );
      fdlFields.right = new FormAttachment( 100, 0 );
      fdlFields.top = new FormAttachment( 0, margin );
      wlFields.setLayoutData( fdlFields );

      ColumnInfo[] colinf =
          new ColumnInfo[] {
            new ColumnInfo( BaseMessages.getString( PKG, "PurRepositoryRevisionBrowserDialog.Columns.Revision.Name" ),
                ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
            new ColumnInfo(
                BaseMessages.getString( PKG, "PurRepositoryRevisionBrowserDialog.Columns.Revision.Comment" ),
                ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
            new ColumnInfo( BaseMessages.getString( PKG, "PurRepositoryRevisionBrowserDialog.Columns.Revision.Date" ),
                ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
            new ColumnInfo( BaseMessages.getString( PKG, "PurRepositoryRevisionBrowserDialog.Columns.Revision.Login" ),
                ColumnInfo.COLUMN_TYPE_TEXT, false, true ), };

      wFields =
          new TableView( new Variables(), shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE, colinf, 0, null, props );
      wFields.setReadonly( true );

      fdFields = new FormData();
      fdFields.left = new FormAttachment( 0, 0 );
      fdFields.top = new FormAttachment( wlFields, margin );
      fdFields.right = new FormAttachment( 100, 0 );
      fdFields.bottom = new FormAttachment( 100, -50 );
      wFields.setLayoutData( fdFields );

      wFields.table.addSelectionListener( new SelectionAdapter() {
        public void widgetSelected( SelectionEvent event ) {
          int index = wFields.getSelectionIndex();
          if ( index >= 0 ) {
            String[] item = wFields.getItem( index );
            if ( item != null && item.length > 0 ) {
              selectedRevision = item[0];
            }
          }
        }

        public void widgetDefaultSelected( SelectionEvent event ) {
          ok();
        }
      } );

      wOpen = new Button( shell, SWT.PUSH );
      wOpen.setText( BaseMessages.getString( "System.Button.Open" ) ); //$NON-NLS-1$
      lsOpen = new Listener() {
        public void handleEvent( Event e ) {
          ok();
        }
      };
      wOpen.addListener( SWT.Selection, lsOpen );

      wClose = new Button( shell, SWT.PUSH );
      wClose.setText( BaseMessages.getString( "System.Button.Close" ) ); //$NON-NLS-1$
      lsClose = new Listener() {
        public void handleEvent( Event e ) {
          cancel();
        }
      };
      wClose.addListener( SWT.Selection, lsClose );

      BaseStepDialog.positionBottomButtons( shell, new Button[] { wOpen, wClose }, margin, null );
      // Detect X or ALT-F4 or something that kills this window...
      shell.addShellListener( new ShellAdapter() {
        public void shellClosed( ShellEvent e ) {
          cancel();
        }
      } );

      getData();

      BaseStepDialog.setSize( shell );

      shell.open();
      while ( !shell.isDisposed() ) {
        if ( !display.readAndDispatch() )
          display.sleep();
      }
      return selectedRevision;
    } catch ( Exception e ) {
      new ErrorDialog( shell, "Error browsing versions", "There was an error browsing the version history of element "
          + element, e );
      return null;
    }
  }

  public void dispose() {
    props.setScreen( new WindowProperty( shell ) );
    shell.dispose();
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    wFields.clearAll( false );
    for ( ObjectRevision revision : revisions ) {

      wFields.add( revision.getName(), revision.getComment(), XMLHandler.date2string( revision.getCreationDate() ),
          revision.getLogin() );
    }
    wFields.removeEmptyRows();
    wFields.setRowNums();
    wFields.optWidth( true );
  }

  private void cancel() {
    selectedRevision = null;
    dispose();
  }

  private void ok() {
    dispose();
  }
}
