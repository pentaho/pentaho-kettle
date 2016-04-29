/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.repo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.ui.spoon.Spoon;

public class RepositoryConnectMenu {

  private static Class<?> PKG = RepositoryConnectMenu.class;
  private static Log log = LogFactory.getLog( RepositoryConnectMenu.class );

  private Spoon spoon;
  private ToolBar toolBar;
  private ToolItem connectButton;
  private ToolItem connectDropdown;
  private RepositoriesMeta repositoriesMeta;
  private final RepositoryConnectController repoConnectController;

  public RepositoryConnectMenu( Spoon spoon, ToolBar toolBar ) {
    this.toolBar = toolBar;
    this.spoon = spoon;
    repoConnectController = new RepositoryConnectController();
  }

  public void update() {
    Rectangle rect = toolBar.getBounds();
    if ( connectDropdown != null && !connectDropdown.isDisposed() ) {
      if ( spoon.rep != null ) {
        StringBuilder connectionLabel = new StringBuilder();
        if ( spoon.rep.getUserInfo() != null ) {
          connectionLabel.append( spoon.rep.getUserInfo().getLogin() );
          connectionLabel.append( " | " );
        }
        connectionLabel.append( spoon.rep.getName() );
        connectDropdown.setText( connectionLabel.toString() );
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
      log.error( BaseMessages.getString( "RepositoryConnectMenu.ErrorLoadingRepositories" ), e );
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
  }

  private void renderConnectButton() {
    connectButton = new ToolItem( toolBar, toolBar.getItems().length );
    connectButton.setText( BaseMessages.getString( PKG, "RepositoryConnectMenu.Connect" ) );
    connectButton.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        new RepositoryDialog( spoon.getShell(), repoConnectController ).openCreation();
        renderAndUpdate();
      }
    } );
  }

  private void renderConnectDropdown() {
    connectDropdown = new ToolItem( toolBar, SWT.DROP_DOWN, toolBar.getItems().length );
    connectDropdown.setText( BaseMessages.getString( PKG, "RepositoryConnectMenu.Connect" ) );
    connectDropdown.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent event ) {
        final Menu connectionMenu = new Menu( toolBar.getShell() );
        if ( repositoriesMeta != null ) {
          for ( int i = 0; i < repositoriesMeta.nrRepositories(); i++ ) {
            MenuItem item = new MenuItem( connectionMenu, SWT.CHECK );
            if ( spoon.rep != null && spoon.rep.getName().equals( repositoriesMeta.getRepository( i ).getName() ) ) {
              item.setSelection( true );
            }
            item.setText( repositoriesMeta.getRepository( i ).getName() );
            item.addSelectionListener( new SelectionAdapter() {
              @Override public void widgetSelected( SelectionEvent selectionEvent ) {
                String repoName = ( (MenuItem) selectionEvent.widget ).getText();
                RepositoryMeta repositoryMeta = repositoriesMeta.findRepository( repoName );
                if ( repositoryMeta != null ) {
                  if ( repositoryMeta.getId().equals( "PentahoEnterpriseRepository" ) ) {
                    new RepositoryDialog( spoon.getShell(), repoConnectController ).openLogin( repositoryMeta );
                  } else {
                    repoConnectController.connectToRepository( repositoryMeta );
                  }
                  renderAndUpdate();
                }
              }
            } );
          }
        }

        new MenuItem( connectionMenu, SWT.SEPARATOR );
        MenuItem managerItem = new MenuItem( connectionMenu, SWT.NONE );
        managerItem.setText( BaseMessages.getString( PKG, "RepositoryConnectMenu.RepositoryManager" ) );
        managerItem.addSelectionListener( new SelectionAdapter() {
          @Override public void widgetSelected( SelectionEvent selectionEvent ) {
            new RepositoryDialog( spoon.getShell(), repoConnectController ).openManager();
            renderAndUpdate();
          }
        } );

        new MenuItem( connectionMenu, SWT.SEPARATOR );
        MenuItem disconnectItem = new MenuItem( connectionMenu, SWT.NONE );
        disconnectItem.setEnabled( spoon.rep != null );
        disconnectItem.setText( BaseMessages.getString( PKG, "RepositoryConnectMenu.Disconnect" ) );
        disconnectItem.addSelectionListener( new SelectionAdapter() {
          @Override public void widgetSelected( SelectionEvent selectionEvent ) {
            spoon.closeRepository();
            renderAndUpdate();
          }
        } );

        ToolItem item = (ToolItem) event.widget;
        Rectangle rect = item.getBounds();
        org.eclipse.swt.graphics.Point pt =
          item.getParent().toDisplay( new org.eclipse.swt.graphics.Point( rect.x, rect.y + rect.height ) );

        connectionMenu.setLocation( pt.x, pt.y );
        connectionMenu.setVisible( true );
      }
    } );
  }
}
