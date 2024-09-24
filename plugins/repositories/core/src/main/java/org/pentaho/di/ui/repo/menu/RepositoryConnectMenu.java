/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.ui.repo.menu;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;
import org.pentaho.di.ui.repo.dialog.RepositoryConnectionDialog;
import org.pentaho.di.ui.repo.dialog.RepositoryManagerDialog;
import org.pentaho.di.ui.spoon.Spoon;

public class RepositoryConnectMenu {

  private static Class<?> PKG = RepositoryConnectMenu.class;
  private static LogChannelInterface log =
    KettleLogStore.getLogChannelInterfaceFactory().create(
      RepositoryConnectMenu.class );
  private static final int MAX_REPO_NAME_PIXEL_LENGTH = 230;

  private final Spoon spoon;
  private final ToolBar toolBar;
  private ToolItem connectButton;
  private ToolItem connectDropdown;
  private RepositoriesMeta repositoriesMeta;
  private final RepositoryConnectController repoConnectController;
  private static final int REPOMANAGERHEIGHT = 650;
  private static final int REPOMANAGERWIDTH= 700;

  static RepositoryConnectController getRepoControllerInstance() {
    return RepositoryConnectController.getInstance();
  }

  public RepositoryConnectMenu( Spoon spoon, ToolBar toolBar, RepositoryConnectController repoConnectController ) {
    this.toolBar = toolBar;
    this.spoon = spoon;
    this.repoConnectController = getRepoControllerInstance();
    repoConnectController.addListener( this::renderAndUpdate );
  }


  public void update() {
    Rectangle rect = toolBar.getBounds();
    if ( connectDropdown != null && !connectDropdown.isDisposed() ) {
      if ( spoon.rep != null ) {
        StringBuilder connectionLabel = new StringBuilder();
        if ( spoon.rep.getUserInfo() != null ) {
          connectionLabel.append( spoon.rep.getUserInfo().getLogin() );
          connectionLabel.append( "  |  " );
        }
        StringBuilder connectionLabelTip = new StringBuilder( connectionLabel.toString() );
        if ( repoConnectController != null && repoConnectController.getConnectedRepository() != null ) {
          connectionLabel.append( truncateName( spoon.getRepositoryName() ) );
          connectionLabelTip.append( spoon.getRepositoryName() );
        }
        connectDropdown.setText( connectionLabel.toString() );
        connectDropdown.setToolTipText( connectionLabelTip.toString() );
      } else {
        connectDropdown.setText( BaseMessages.getString( PKG, "RepositoryConnectMenu.Connect" ) );
      }
    }
    // This fixes some SWT goofiness
    toolBar.pack();
    toolBar.setBounds( rect );
  }

  public void render() {
    repositoriesMeta = new RepositoriesMeta();
    try {
      if ( repositoriesMeta.readData() && repositoriesMeta.nrRepositories() > 0 ) {
        renderConnectDropdown();
      } else {
        renderConnectButton();
      }
    } catch ( KettleException e ) {
      log.logError( BaseMessages.getString( "RepositoryConnectMenu.ErrorLoadingRepositories" ), e );
    }
  }

  private void renderAndUpdate() {
    if ( connectDropdown != null && !connectDropdown.isDisposed() ) {
      connectDropdown.dispose();
    }
    if ( connectButton != null && !connectButton.isDisposed() ) {
      connectButton.dispose();
    }
    render();
    update();
    spoon.setShellText();
  }

  /**
   * @implNote prompts to create new repository connection if there doesn't exist
   * any.
   */
  private void renderConnectButton() {
    this.connectButton = new ToolItem( toolBar, toolBar.getItems().length );
    connectButton.setText( BaseMessages.getString( PKG, "RepositoryConnectMenu.Connect" ) );
    connectButton.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent selectionEvent ) {
        new RepositoryManagerDialog( spoon.getShell() ).open( REPOMANAGERWIDTH, REPOMANAGERHEIGHT,
          spoon.getRepositoryName(), repositoriesMeta );
        renderAndUpdate();
      }
    } );
  }


  /**
   * @implNote prompts gui for connection to existing repository
   */
  @SuppressWarnings( "squid:S3776" )
  private void renderConnectDropdown() {
    this.connectDropdown = new ToolItem( toolBar, SWT.DROP_DOWN, toolBar.getItems().length );
    connectDropdown.setText( BaseMessages.getString( PKG, "RepositoryConnectMenu.Connect" ) );
    connectDropdown.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent event ) {
        final Menu connectionMenu = new Menu( toolBar.getShell() );
        if ( repositoriesMeta != null ) {
          for ( int i = 0; i < repositoriesMeta.nrRepositories(); i++ ) {
            MenuItem item = new MenuItem( connectionMenu, SWT.CHECK );
            String truncatedName = truncateName( repositoriesMeta.getRepository( i ).getName() );
            item.setText( truncatedName );
            item.setData( repositoriesMeta.getRepository( i ).getName() );
            if ( spoon.rep != null && spoon.getRepositoryName()
              .equals( repositoriesMeta.getRepository( i ).getName() ) ) {
              item.setSelection( true );
              continue;
            }
            item.addSelectionListener( new SelectionAdapter() {
              @Override
              public void widgetSelected( SelectionEvent selectionEvent ) {
                String repoName = (String) ( selectionEvent.widget ).getData();
                RepositoryMeta repositoryMeta = repositoriesMeta.findRepository( repoName );
                if ( repositoryMeta != null ) {
                  try {
                    if ( !spoon.promptForSave() ) {
                      return;
                    }
                  } catch ( KettleException ke ) {
                    log.logError( "Error prompting for save", ke );
                  }
                  if ( repositoryMeta.getId().equals( "KettleFileRepository" ) ) {
                    try {
                      repoConnectController.connectToRepository( repositoryMeta );
                    } catch ( KettleException ke ) {
                      log.logError( "Error connecting to repository", ke );
                    }
                  } else {
                    new RepositoryConnectionDialog( spoon.getShell() ).createDialog( repoName );
                  }
                  renderAndUpdate();
                }
              }
            } );
          }
        }

        /**
         * @implNote prompts repository manager gui for reps crud operations
         *
         */
        new MenuItem( connectionMenu, SWT.SEPARATOR );
        MenuItem managerItem = new MenuItem( connectionMenu, SWT.NONE );
        managerItem.setText( BaseMessages.getString( PKG, "RepositoryConnectMenu.RepositoryManager" ) );
        managerItem.addSelectionListener( new SelectionAdapter() {
          @Override
          public void widgetSelected( SelectionEvent selectionEvent ) {
            new RepositoryManagerDialog( spoon.getShell() ).open( REPOMANAGERWIDTH, REPOMANAGERHEIGHT,
              spoon.getRepositoryName(), repositoriesMeta );
            renderAndUpdate();
          }
        } );

        new MenuItem( connectionMenu, SWT.SEPARATOR );
        MenuItem disconnectItem = new MenuItem( connectionMenu, SWT.NONE );
        disconnectItem.setEnabled( spoon.rep != null );
        disconnectItem.setText( BaseMessages.getString( PKG, "RepositoryConnectMenu.Disconnect" ) );
        disconnectItem.addSelectionListener( new SelectionAdapter() {
          @Override
          public void widgetSelected( SelectionEvent selectionEvent ) {
            boolean cancelled = false;
            try {
              cancelled = !spoon.promptForSave();
            } catch ( KettleException e ) {
              log.logError( "Error saving Job or Transformation", e );
            }
            if ( !cancelled ) {
              spoon.closeRepository();
              repoConnectController.setConnectedRepository( null );
              renderAndUpdate();
            }
          }
        } );

        ToolItem item = (ToolItem) event.widget;
        Rectangle rect = item.getBounds();
        Point pt =
          item.getParent().toDisplay( new org.eclipse.swt.graphics.Point( rect.x, rect.y + rect.height ) );
        connectionMenu.setLocation( pt.x, pt.y );
        connectionMenu.setVisible( true );
      }
    } );
  }

  private String truncateName( String name ) {
    GC gc = new GC( toolBar );
    Point size = gc.textExtent( name );
    if ( size.x <= MAX_REPO_NAME_PIXEL_LENGTH ) { // repository name is small enough to fit just return it.
      gc.dispose();
      return name;
    }
    while ( gc.textExtent( name + "..." ).x > MAX_REPO_NAME_PIXEL_LENGTH ) {
      name = name.substring( 0, name.length() - 1 );
    }
    gc.dispose();
    name = name + "...";
    return name;
  }
}