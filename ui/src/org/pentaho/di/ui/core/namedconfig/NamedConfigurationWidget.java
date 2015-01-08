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

package org.pentaho.di.ui.core.namedconfig;

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
import org.pentaho.di.base.HasNamedConfigurationsInterface;
import org.pentaho.di.core.namedconfig.model.NamedConfiguration;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.namedconfig.dialog.NamedConfigurationComposite;
import org.pentaho.di.ui.spoon.Spoon;

public class NamedConfigurationWidget extends Composite {

  private static Class<?> PKG = NamedConfigurationComposite.class;
  private Combo nameConfigCombo;

  public NamedConfigurationWidget( Composite parent ) {
    super( parent, SWT.NONE );

    RowLayout layout = new RowLayout( SWT.HORIZONTAL );
    layout.center = true;
    setLayout( layout );

    Label nameLabel = new Label( this, SWT.NONE );
    nameLabel.setText( BaseMessages.getString( PKG, "NamedConfiguarationDialog.Shell.Title" ) + ":" );

    nameConfigCombo = new Combo( this, SWT.NONE );

    Button editButton = new Button( this, SWT.NONE );
    editButton.setText( BaseMessages.getString( PKG, "NamedConfiguarationWidget.NamedConfiguration.Edit" ) );
    editButton.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event e ) {
        editNamedConfiguration();
      }
    } );

    Button newButton = new Button( this, SWT.NONE );
    newButton.setText( BaseMessages.getString( PKG, "NamedConfiguarationWidget.NamedConfiguration.New" ) );
    newButton.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event e ) {
        newNamedConfiguration();
      }
    } );

    initiate();
  }

  private void newNamedConfiguration() {
    Spoon spoon = Spoon.getInstance();
    AbstractMeta meta = (AbstractMeta) spoon.getActiveMeta();
    if ( meta != null ) {
      spoon.delegates.nc.newNamedConfiguration( (HasNamedConfigurationsInterface) meta, getShell() );
      initiate();
    }
  }

  private void editNamedConfiguration() {
    Spoon spoon = Spoon.getInstance();
    AbstractMeta meta = (AbstractMeta) spoon.getActiveMeta();
    if ( meta != null ) {
      List<NamedConfiguration> namedConfigurations = meta.getNamedConfigurations();
      int index = nameConfigCombo.getSelectionIndex();
      if ( index > -1 && namedConfigurations.size() > 0 ) {
        spoon.delegates.nc.editNamedConfiguration( (HasNamedConfigurationsInterface) meta, namedConfigurations
            .get( index ), getShell() );
        initiate();
      }
    }
  }

  private String[] getNamedConfigurations() {
    AbstractMeta meta = (AbstractMeta) Spoon.getInstance().getActiveMeta();
    if ( meta != null ) {
      List<String> configurationNames = new ArrayList<String>();
      List<NamedConfiguration> namedConfigurations = meta.getNamedConfigurations();
      for ( NamedConfiguration namedConfiguration : namedConfigurations ) {
        configurationNames.add( namedConfiguration.getName() );
      }
      return configurationNames.toArray( new String[namedConfigurations.size()] );
    }
    return new String[0];
  }

  public void initiate() {
    int selectedIndex = nameConfigCombo.getSelectionIndex();
    nameConfigCombo.removeAll();
    nameConfigCombo.setItems( getNamedConfigurations() );
    nameConfigCombo.select( selectedIndex );
  }

  public NamedConfiguration getSelectedNamedConfiguration() {
    Spoon spoon = Spoon.getInstance();
    AbstractMeta meta = (AbstractMeta) spoon.getActiveMeta();
    if ( meta != null ) {
      List<NamedConfiguration> namedConfigurations = meta.getNamedConfigurations();
      int index = nameConfigCombo.getSelectionIndex();
      if ( index > -1 && namedConfigurations.size() > 0 ) {
        return namedConfigurations.get( index );
      }
    }
    return null;
  }

  public void addSelectionListener( SelectionListener selectionListener ) {
    nameConfigCombo.addSelectionListener( selectionListener );
  }
}
