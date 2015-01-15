/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.core.namedcluster;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.namedcluster.NamedClusterManager;
import org.pentaho.di.core.namedcluster.model.NamedCluster;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.namedcluster.dialog.NamedClusterComposite;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

public class NamedClusterWidget extends Composite {

  private static Class<?> PKG = NamedClusterComposite.class;
  private Combo nameClusterCombo;

  public NamedClusterWidget( Composite parent, boolean showLabel ) {
    super( parent, SWT.NONE );

    PropsUI props = PropsUI.getInstance();
    props.setLook( this );
    
    RowLayout layout = new RowLayout( SWT.HORIZONTAL );
    layout.center = true;
    setLayout( layout );

    if ( showLabel ) {
      Label nameLabel = new Label( this, SWT.NONE );
      nameLabel.setText( BaseMessages.getString( PKG, "NamedClusterDialog.Shell.Title" ) + ":" );
      props.setLook( nameLabel );
    }
    
    nameClusterCombo = new Combo( this, SWT.DROP_DOWN | SWT.READ_ONLY );

    Button editButton = new Button( this, SWT.NONE );
    editButton.setText( BaseMessages.getString( PKG, "NamedClusterWidget.NamedCluster.Edit" ) );
    editButton.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event e ) {
        editNamedCluster();
      }
    } );
    props.setLook( editButton );

    Button newButton = new Button( this, SWT.NONE );
    newButton.setText( BaseMessages.getString( PKG, "NamedClusterWidget.NamedCluster.New" ) );
    newButton.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event e ) {
        newNamedCluster();
      }
    } );
    props.setLook( newButton );

    initiate();
  }

  private void newNamedCluster() {
    Spoon spoon = Spoon.getInstance();
    AbstractMeta meta = (AbstractMeta) spoon.getActiveMeta();
    spoon.delegates.nc.newNamedCluster( meta, spoon.getMetaStore(), getShell() );
    initiate();
  }

  private void editNamedCluster() {
    Spoon spoon = Spoon.getInstance();
    AbstractMeta meta = (AbstractMeta) spoon.getActiveMeta();
    if ( meta != null ) {
      List<NamedCluster> namedClusters = null;
      try {
        namedClusters = NamedClusterManager.getInstance().list( spoon.getMetaStore() );
      } catch ( MetaStoreException e ) {
      }
      
      int index = nameClusterCombo.getSelectionIndex();
      if ( index > -1 && namedClusters != null && namedClusters.size() > 0 ) {
        spoon.delegates.nc.editNamedCluster( spoon.getMetaStore(), namedClusters
            .get( index ), getShell() );
        initiate();
      }
    }
  }

  private String[] getNamedClusterNames() {
    try {
      return NamedClusterManager.getInstance().listNames( Spoon.getInstance().getMetaStore() )
          .toArray( new String[0] );
    } catch (MetaStoreException e) {
      return new String[0];
    }
  }

  public void initiate() {
    int selectedIndex = nameClusterCombo.getSelectionIndex();
    nameClusterCombo.removeAll();
    nameClusterCombo.setItems( getNamedClusterNames() );
    nameClusterCombo.select( selectedIndex );
  }

  public NamedCluster getSelectedNamedCluster() {
    Spoon spoon = Spoon.getInstance();
    int index = nameClusterCombo.getSelectionIndex();
    if ( index > -1 ) {
      String name = nameClusterCombo.getItem( index );
      try {
        return NamedClusterManager.getInstance().read( name, spoon.getMetaStore() );
      } catch ( MetaStoreException e ) {
        return null;
      }
    }
    return null;
  }

  public void setSelectedNamedCluster( String name ) {
    for ( int i = 0; i < nameClusterCombo.getItemCount(); i++ ) {
      if ( nameClusterCombo.getItem( i ).equals( name ) ) {
        nameClusterCombo.select( i );
        return;
      }
    }
  }

  public void addSelectionListener( SelectionListener selectionListener ) {
    nameClusterCombo.addSelectionListener( selectionListener );
  }
}
